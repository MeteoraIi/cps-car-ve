package cn.vetech.center.usecar.risk;

import cn.vetech.center.common.ApplicationName;
import cn.vetech.center.customer.api.dto.CpscRiskBaseQueryDTO;
import cn.vetech.center.customer.api.dto.CpscRiskHitLogApiDTO;
import cn.vetech.center.customer.api.vo.CpscRiskSettingApiVO;
import cn.vetech.center.system.es.service.ElasticSearchService;
import cn.vetech.center.usecar.apiclient.risk.ICpscRiskSettingServiceClient;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import com.beust.jcommander.internal.Lists;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.vetech.core.api.RestResponse;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.time.VeDate;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;


@Service
public class RiskMamageCenterService {

    /**
     * 日志工具
     */
    private Logger logger = LoggerFactory.getLogger(RiskMamageCenterService.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private ICpscRiskSettingServiceClient iCpscRiskSettingServiceClient;

    @Autowired
    private RedisCacheManage redisCacheManage;


    private static final String  BEHAVING_RISK_KEY = "BEHAVING_RISK";


    /**
     *  取消订单风险控制
     */
    public boolean cpscCancelOrderCountRiskControl(String memberId,String channelId,String ydrSj){

        CpscRiskSettingApiVO cpscRiskSettingApiVO = getRiskRule(channelId, "YC-FX02");
        if(cpscRiskSettingApiVO!=null){
            Integer thresholdNum = cpscRiskSettingApiVO.getThresholdNum();
            long count = count(memberId,cpscRiskSettingApiVO.getTimeWindow());
            if(thresholdNum!=null && count>=thresholdNum){
                //保存风控记录
                CpscRiskHitLogApiDTO record = new CpscRiskHitLogApiDTO();
                record.setChannelId(channelId);
                record.setHitTime(VeDate.getNow());
                record.setDictId(cpscRiskSettingApiVO.getDictId());
                record.setRiskCode(cpscRiskSettingApiVO.getRiskCode());
                record.setRiskDesc(cpscRiskSettingApiVO.getRiskDesc());
                record.setControlType(cpscRiskSettingApiVO.getControlType());
                record.setReleaseControlTime(VeDate.getPreMin(VeDate.getNow(),cpscRiskSettingApiVO.getControlHours()*60));
                record.setUid(memberId);
                record.setMobile(ydrSj);
                record.setClientIp(getClientRealIp());
                record.setDeviceId("");
                record.setHitRemark("同一个账号取消订单次数过多限制下单");
                record.setRuleDimension("ACCOUNT");
                record.setSettingId(cpscRiskSettingApiVO.getId());
                record.setCreateUser(ApplicationName.USECAR);
                iCpscRiskSettingServiceClient.saveHitLogBatch(Lists.newArrayList(record));
                return true;
            }
        }
        return false;
    }

    private CpscRiskSettingApiVO getRiskRule(String channelId,String code){
        CpscRiskSettingApiVO o = (CpscRiskSettingApiVO)redisCacheManage.get(BEHAVING_RISK_KEY, code+"_"+channelId);
        if(o!=null){
            return o;
        }
        CpscRiskBaseQueryDTO dto = new CpscRiskBaseQueryDTO();
        dto.setChannelId(channelId);
        dto.setRiskCodeList(Lists.newArrayList(code));
        dto.setProductCodeList(Lists.newArrayList("1000"));
        logger.info("查询风控取消规则入参={}", JsonMapper.nonEmptyMapper().toJson(dto));
        RestResponse<List<CpscRiskSettingApiVO>> response = iCpscRiskSettingServiceClient.selectOrderRiskRules(dto);
        logger.info("查询风控取消规则回参={}", JsonMapper.nonEmptyMapper().toJson(response));
        if(response!=null && response.getResult()!=null && CollectionUtil.isNotEmpty(response.getResult())){
            List<CpscRiskSettingApiVO> result = response.getResult();
            redisCacheManage.put(BEHAVING_RISK_KEY, code+"_"+channelId,result.get(0),60*60);
            return result.get(0);
        }
        return null;
    }

    public String getClientRealIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null; // 当前线程没有绑定请求（比如在定时任务中调用）
        }

        // 2. 从上下文中拿到 HttpServletRequest
        HttpServletRequest request = attributes.getRequest();
        // 1. 尝试从 X-Forwarded-For 头获取
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 如果存在多个代理地址，只取第一个，即真实客户端 IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            } else {
                return ip;
            }
        }

        // 2. 如果 X-Forwarded-For 没有，再尝试从 X-Real-IP 头获取
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 3. 以上都获取不到，才降级使用 RemoteAddr
        return request.getRemoteAddr();
    }

    private long count(String memberId,int days){
        Date now = VeDate.getNow();
        String end = VeDate.dateToStrLong(now);
        String start = VeDate.dateToStrLong(VeDate.getPreDay(now,-days));
        SearchRequestBuilder searchRequest = elasticSearchService.searchRequest("yc_dd", "yc_dd");
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termsQuery("cgDdly", UseCarConstant.B2C,UseCarConstant.CPSC_PC));
        boolQuery.filter(QueryBuilders.termsQuery("memberId.keyword",memberId));
        boolQuery.filter(QueryBuilders.rangeQuery("xdsj").gte(start).lte(end));
        boolQuery.filter(QueryBuilders.termsQuery("ddzt","YC1E"));
        //取消订单数据
        searchRequest.addAggregation(AggregationBuilders.cardinality("cancelOrderCount").field("pDdbh.keyword").precisionThreshold(100000).missing("YCMISS0001"));
        searchRequest.setQuery(boolQuery);
        searchRequest.setSize(0);
        SearchResponse response = searchRequest.get(TimeValue.timeValueSeconds(20));
        Aggregations aggregations = response.getAggregations();
        Aggregation aggregation = aggregations.getAsMap().get("cancelOrderCount");
        InternalCardinality internalFilter = (InternalCardinality) aggregation;
        return internalFilter.getValue();
    }
}
