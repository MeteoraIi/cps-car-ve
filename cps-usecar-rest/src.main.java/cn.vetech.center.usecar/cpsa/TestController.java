package cn.vetech.center.usecar.cpsa;

import cn.vetech.center.system.es.service.ElasticSearchService;
import cn.vetech.center.usecar.api.CpsUsecarAuthServiceImpl;
import cn.vetech.center.usecar.api.auth.dto.OrderStatusChangeNoticeDTO;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.notice.buyer.dto.ArrangeCarNotifyToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.service.BuyerNoticeService;
import cn.vetech.center.usecar.order.cpsa.service.CpsaOrderService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.setting.citysetting.service.CityLevelsGroupService;
import cn.vetech.center.usecar.setting.profit.dto.CpsaUseCarProfitCacheDTO;
import cn.vetech.center.usecar.setting.profit.service.CpsaProfitCacheService;
import cn.vetech.center.usecar.setting.profit.service.CpsaUseCarProfitCacheService;
import cn.vetech.center.usecar.setting.profit.service.CpsaYcDdProfitRuleJlService;
import cn.vetech.center.usecar.setting.profit.vo.CpsaProfitCacheVO;
import cn.vetech.center.usecar.setting.profit.vo.CpsaUseCarProfitCacheVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.vetech.core.api.RestResponse;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/dd/notice")
public class TestController {
    /**
     * 日志
     *///
    private Logger logger = LoggerFactory.getLogger(TestController.class);
    /**
     * 推送订单到差旅平台工具
     */
    @Autowired
    private CpsUsecarAuthServiceImpl cpsUsecarAuthServiceImpl;

    @Autowired
    private ElasticSearchService elasticSearchService;


    @Autowired
    private YcDdService ycDdService;

    @Autowired
    private CityLevelsGroupService cityLevelsGroupService;

    /**
     * 用车退款订单 ES  search 服务类
     */
    @Autowired
    private CpsaUseCarProfitCacheService useCarProfitCacheService;

    /**
     *
     */
    @Autowired
    private CpsaProfitCacheService cpsaProfitCacheService;

    @GetMapping("test")
    public String notice(@RequestParam("str") String str){
        try {
            Map notice = JsonMapper.nonEmptyMapper().fromJson(str, new TypeReference<Map<String, Object>>() {
            });
            Map<String, String> data = (Map<String, String>)notice.get("data");
            String orderId = data.get("order_id");
            String companyId = data.get("company_id");
            OrderStatusChangeNoticeDTO dto = new OrderStatusChangeNoticeDTO();
            if(StringUtils.isNotBlank(orderId) && StringUtils.isNotBlank(companyId)){
                dto.setOrderId(orderId);
                dto.setCompanyId(companyId);
                dto.setData(JsonMapper.nonEmptyMapper().toJson(data));
                dto.setGyshbh(UsecarGysApiEnum.DDYC_ERP.getShbh());
                cpsUsecarAuthServiceImpl.noticeOrderStatus(dto);
            }
        } catch (Exception e) {
            return "false";
        }
        return "true";
    }


    @PostMapping("/es/search")
    public Object esTest(@RequestBody Map<String,String> map){
        TransportClient client = elasticSearchService.getClient();
        try{
            SearchTemplateRequestBuilder searchTemplateRequestBuilder = new SearchTemplateRequestBuilder(client);
            searchTemplateRequestBuilder.setScript(map.get("dsl"));
            searchTemplateRequestBuilder.setScriptType(ScriptType.INLINE);
            searchTemplateRequestBuilder.setRequest(new SearchRequest().indices(map.get("index")).types(map.get("type")));
            SearchResponse response = searchTemplateRequestBuilder.get().getResponse();
            return response.toString();
        } catch (Exception e) {
            logger.error("异常",e);
        }
        return null;
    }


    @GetMapping("/krgz")
    public Object krgz(@RequestParam("ddbh")String ddbh){
        YcDd order = ycDdService.selectYcDd(ddbh);
        CpsaUseCarProfitCacheDTO cpsaUseCarProfitCacheDTO = new CpsaUseCarProfitCacheDTO();
        cpsaUseCarProfitCacheDTO.setCgshbh(order.getCgShbh());
        cpsaUseCarProfitCacheDTO.setGyshbh(order.getGyShbh());
        cpsaUseCarProfitCacheDTO.setCplxid(order.getDdlx());
        cpsaUseCarProfitCacheDTO.setZdid(order.getJsfwzdid());
        BigDecimal bdlc = order.getBdlc();
        cpsaUseCarProfitCacheDTO.setYclc(bdlc==null?BigDecimal.ZERO:bdlc.divide(BigDecimal.valueOf(1000),2, RoundingMode.HALF_UP));
        cpsaUseCarProfitCacheDTO.setYcsc(order.getBdsc());
        cpsaUseCarProfitCacheDTO.setYcsj(order.getYcsj());
        cpsaUseCarProfitCacheDTO.setXdsj(order.getXdsj());
        cpsaUseCarProfitCacheDTO.setCityLevel(cityLevelsGroupService.selectCityLevelByCityId(order.getCfdCsid()));
        //供应结算价
        BigDecimal gyjsj = order.getGyJsje();
        if (BigDecimal.ONE.equals(order.getGyQhf())) {
            gyjsj = order.getGyJsje().add(order.getGyFyje());
        }
        cpsaUseCarProfitCacheDTO.setCpjeOne(gyjsj);//供应结算价
        //差价
        BigDecimal cj = gyjsj.subtract(gyjsj);
        cpsaUseCarProfitCacheDTO.setCpjeTwo(cj);//差价(建议销售价-供应结算价)
        //建议销售价
        BigDecimal jyxsj = gyjsj;
        cpsaUseCarProfitCacheDTO.setCpjeThree(jyxsj);//建议销售价
        logger.info("订单[" + order.getDdbh() + "]开始获取控润返佣信息，查询参数为：{}", cpsaUseCarProfitCacheDTO.toString());
        CpsaUseCarProfitCacheVO fyKrVO = useCarProfitCacheService.getCpsaUseCarProfitCacheVO(cpsaUseCarProfitCacheDTO);
        return fyKrVO;
    }


    /**
     *
     * @param ddbh
     * @return
     */
    @GetMapping("/allKrgz")
    public Object allkrgz(@RequestParam("ddbh")String ddbh,@RequestParam("all")String all){
        if(StringUtils.equals(all,"1")){
            List<CpsaProfitCacheVO> cpsaProfitCacheVOS = cpsaProfitCacheService.selectAllValidProfit();
            return JsonMapper.nonEmptyMapper().toJson(cpsaProfitCacheVOS);
        }else{
            YcDd order = ycDdService.selectYcDd(ddbh);
            Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> cpsaUseCarProfitCacheByCplx = cpsaProfitCacheService.getCpsaUseCarProfitCacheByCplx(order.getDdlx());
            return JsonMapper.nonEmptyMapper().toJson(cpsaUseCarProfitCacheByCplx);
        }
    }


    @GetMapping("/checkBcd")
    public Object checkBcd(@RequestParam("start") String start,@RequestParam("end") String end){
        start = start+" 00:00:00";
        end = end+" 23:59:59";
        List<OrderEsVO> list = Lists.newArrayList();
        String scrollId = null;
        do{
            list = Lists.newArrayList();
            SearchResponse response = null;
            if(StringUtils.isBlank(scrollId)){
                SearchRequestBuilder searchRequest = elasticSearchService.searchRequest("yc_dd", "yc_dd");
                searchRequest.setSize(100);
                searchRequest.setFetchSource(new String[]{"ddbh","gyDdbh"},null);
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termQuery("orderType","2"));
                boolQuery.must(QueryBuilders.rangeQuery("xdsj").gte(start).lt(end));
                searchRequest.setScroll(TimeValue.timeValueSeconds(60L));
                searchRequest.setQuery(boolQuery);
                response = searchRequest.get(TimeValue.timeValueSeconds(20));
            }else{
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueSeconds(60L));
                try {
                    response = elasticSearchService.getClient().searchScroll(scrollRequest).get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
 logger.error("查询异常",e);
                }
            }

            if(response!=null){
                scrollId = response.getScrollId();
                SearchHit[] hits = response.getHits().getHits();
                if(hits!=null){
                    list = Stream.of(hits).map(e -> JsonMapper.nonEmptyMapper().fromJson(e.getSourceAsString(), OrderEsVO.class)).collect(Collectors.toList());
                }
            }

            if(CollectionUtils.isNotEmpty(list)){
                Map<String, OrderEsVO> map = Maps.newHashMap();
                List<String> gyDdbhs = list.stream().map(e -> e.getGyDdbh()).collect(Collectors.toList());
                SearchRequestBuilder searchOrdersBuilder = elasticSearchService.searchRequest("yc_dd", "yc_dd");
                searchOrdersBuilder.setFetchSource(new String[]{"ddbh","clyy","cfdCsid","gyDdbh"},null);
                searchOrdersBuilder.setSize(200);
                searchOrdersBuilder.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("gyDdbh",gyDdbhs)).must(QueryBuilders.termQuery("orderType","1")));
                SearchResponse result = searchOrdersBuilder.get(TimeValue.timeValueSeconds(20L));
                if(result!=null){
                    SearchHit[] hits = result.getHits().getHits();
                    if(hits!=null){
                        map = Stream.of(hits).map(e -> JsonMapper.nonEmptyMapper().fromJson(e.getSourceAsString(), OrderEsVO.class)).collect(Collectors.toMap(OrderEsVO::getGyDdbh, Function.identity(), (o1, o2) -> o1));
                    }
                }
                BulkRequestBuilder bulkRequest = elasticSearchService.bulkRequest();
                for (OrderEsVO one : list) {
                    OrderEsVO orderEsVO = map.get(one.getGyDdbh());
                    if(orderEsVO!=null){
                        one.setCfdCsid(orderEsVO.getCfdCsid());
                        one.setClyy(orderEsVO.getClyy());
                       UpdateRequestBuilder updateRequest = elasticSearchService.updateRequest("yc_dd", "yc_dd", one.getDdbh());
                        updateRequest.setDoc(JsonMapper.nonEmptyMapper().toJson(one), XContentType.JSON);
                        bulkRequest.add(updateRequest);
                    }
                }

                if(bulkRequest.numberOfActions()>0){
                    bulkRequest.get(TimeValue.timeValueSeconds(20));
                }
            }

        }while (CollectionUtils.isNotEmpty(list));

        return new RestResponse<>(Boolean.TRUE);
    }

    @Autowired
    private BuyerNoticeService buyerNoticeService;

    @Autowired
    private CpsaOrderService cpsaOrderService;


    @GetMapping("/reNotify")
    public Object reNotify(@RequestParam("start") String start,@RequestParam("end") String end){

        SearchRequestBuilder searchRequest = elasticSearchService.searchRequest("yc_dd", "yc_dd");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery("cgShbh","AXZQ"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("xdsj").gte(start+" 00:00:00").lt(end+" 23:59:59"));
        searchRequest.setQuery(boolQueryBuilder);
        searchRequest.setFetchSource(new String[]{"ddbh"},null);

        SearchResponse response = searchRequest.get(TimeValue.timeValueSeconds(20));
        SearchHits hits = response.getHits();
        SearchHit[] hits1 = hits.getHits();
        if(hits1==null || hits1.length==0){
            return "无数据";
        }
        logger.info("数量={}",hits1.length);
        List<YcDd> ycDds = Lists.newArrayList();
        for (SearchHit searchHit : hits1) {
            OrderEsVO orderEsVO = JsonMapper.nonEmptyMapper().fromJson(searchHit.getSourceAsString(), OrderEsVO.class);
            YcDd ycDd = ycDdService.selectYcDd(orderEsVO.getDdbh());
            ycDds.add(ycDd);
        }

        List<List<YcDd>> partitions = Lists.partition(ycDds, 100);

        for (List<YcDd> list : partitions) {
            for (YcDd ycDd:list) {
                ArrangeCarNotifyToBuyerDTO arrangeCarNotifyDTO = new ArrangeCarNotifyToBuyerDTO();
                arrangeCarNotifyDTO.setDriverOrderCount(ycDd.getDriverOrderCount());
                arrangeCarNotifyDTO.setSjpf(ycDd.getSjpf());
                arrangeCarNotifyDTO.setCgDdbh(ycDd.getDdbh());// 这个订单编号在ASMS采购商平台里，存在cg_ddbh字段里
                arrangeCarNotifyDTO.setBuyOrderNo(ycDd.getCgDdbh());
                arrangeCarNotifyDTO.setCgShbh(ycDd.getCgShbh());
                arrangeCarNotifyDTO.setSjdh(ycDd.getSjdh());
                arrangeCarNotifyDTO.setSjxb(ycDd.getSjxb());
                arrangeCarNotifyDTO.setSjxm(ycDd.getSjxm());
                arrangeCarNotifyDTO.setCph(ycDd.getCph());
                arrangeCarNotifyDTO.setCxmc(ycDd.getCxmc());
                arrangeCarNotifyDTO.setCpszt(ycDd.getDdzt());
                arrangeCarNotifyDTO.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycDd.getDdzt()));
                arrangeCarNotifyDTO.setGypcsj(VeDate.formatToStr(ycDd.getGyPcsj(), "yyyy-MM-dd HH:mm:ss"));
                arrangeCarNotifyDTO.setXhlxsjsjxz(ycDd.getXhlxsjsjxz());
                arrangeCarNotifyDTO.setNotNotice(StringUtils.contains(ycDd.getCgDdly(),"CPSC"));
                buyerNoticeService.arrangeCarNotify(arrangeCarNotifyDTO);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
            }
                cpsaOrderService.reNotify(ycDd);
            }
        }
        return "success";
    }
}