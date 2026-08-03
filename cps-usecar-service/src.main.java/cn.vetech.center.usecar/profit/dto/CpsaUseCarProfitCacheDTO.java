package cn.vetech.center.usecar.setting.profit.service;

import cn.vetech.center.usecar.common.cache.CacheDict;
import cn.vetech.center.usecar.entity.usecar.YcKrJgmx;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.service.usecar.YcKrJgmxService;
import cn.vetech.center.usecar.service.usecar.YcKrService;
import cn.vetech.center.usecar.setting.profit.vo.CpsaProfitCacheVO;
import cn.vetech.center.usecar.setting.profit.vo.CpsaUseCarProfitCacheVO;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用车控润缓存服务
 *
 * @author chenyong
 * @since 2017-11-13
 */
@Service
public class CpsaProfitCacheService {
    /**
     * 日志记录工具
     */
    private static final Logger logger = LoggerFactory.getLogger(CpsaProfitCacheService.class);
    /**
     * 用车控润操作service
     */
    @Autowired
    private YcKrService ycKrService;
    /**
     * 缓存工具
     */
    @Autowired
    private RedisCacheManage cacheManager;
    /**
     * 控润信息分组服务
     */
    @Autowired
    private CpsaProfitGroupService cpsaProfitGroupService;

    /**
     * 控润价格明细服务
     */
    @Autowired
    private YcKrJgmxService ycKrJgmxService;

    /**
     * 获取有效的 控润信息列表
     *
     * @return 获取的有效控润信息
     */
    public List<CpsaProfitCacheVO> selectAllValidProfit() {
        Date currentDate = VeDate.getNow();
        List<CpsaProfitCacheVO> cpsProfitCacheVOs = ycKrService.selectAllValidProfit(currentDate);
        if (CollectionUtil.isNotEmpty(cpsProfitCacheVOs)) {
            logger.info("获取有效的控润信息列表:{}", cpsProfitCacheVOs.size());
            for (CpsaProfitCacheVO cpsaProfitCacheVO : cpsProfitCacheVOs) {
                List<YcKrJgmx> ycKrJgmxes = ycKrJgmxService.selectByKrId(cpsaProfitCacheVO.getId());
                List<CpsaUseCarProfitCacheVO> krDetails = BeanMapper.mapList(ycKrJgmxes, YcKrJgmx.class, CpsaUseCarProfitCacheVO.class);
                if(CollectionUtils.isNotEmpty(krDetails)){
                    //价格明细在前，为了兼容旧数据，2024-06-06 之后可以去掉这个逻辑
                    List<CpsaUseCarProfitCacheVO> jgmx = krDetails.stream().filter(e -> Lists.newArrayList("1", "2", "3").contains(e.getKrgz()) || StringUtils.isBlank(e.getKrgz())).collect(Collectors.toList());
                    jgmx.addAll(krDetails.stream().filter(e -> !Lists.newArrayList("1", "2", "3").contains(e.getKrgz())).collect(Collectors.toList()));
                    krDetails = jgmx;
                }
                String krmxJson = JsonMapper.nonEmptyMapper().toJson(krDetails);
                cpsaProfitCacheVO.setKrmxJson(krmxJson);
            }
        }
        return cpsProfitCacheVOs;
    }

    /**
     * 按产品类型 从缓存中获取控润信息
     *
     * @param cplxId 产品类型ID
     * @return 获取的分组控润信息
     */
    public Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> getCpsaUseCarProfitCacheByCplx(String cplxId) {
        String key = cacheManager.genKey(CacheDict.YCAR_KR_QUERY.getCachename(), cplxId);
        Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> groupProfit = getProfitFromCache(key);
        return groupProfit;
    }

    /**
     * 从缓存中获取值
     *
     * @param key 产品类型ID
     * @return 产品类型下所有控润信息
     */
    private Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> getProfitFromCache(String key) {
        // 系统重启或者缓存被清空后重新加载缓存
        return CpsaProfitCacheHolder.get(key);
    }

    /**
     * 按照产品类型分组 后 缓存控润信息
     */
    public void cacheAll(String source) {
        long s = System.currentTimeMillis();
        try {
            logger.info("更新来源：{}", source);
            cacheAll2();
        } catch (Exception e) {
            logger.error("控润缓存异常", e);
        } finally {
            logger.info("刷新缓存耗时毫秒{}", System.currentTimeMillis() - s);
        }
    }

    /**
     * 按照产品类型分组 后 缓存控润信息
     */
    public synchronized void cacheAll2() {
        if (CpsaProfitCacheHolder.size() > 100000) {
            logger.error("内存溢出");
            CpsaProfitCacheHolder.clearAll();
        }
        List<CpsaProfitCacheVO> cpsaProfitCacheVOs = selectAllValidProfit();
        Map<String, List<CpsaProfitCacheVO>> cplxGroup = groupByCplx(cpsaProfitCacheVOs);
        /**
         * 按产品类型 存储到 缓存的 Map 中
         */
        if (cplxGroup != null) {
            for (Map.Entry<String, List<CpsaProfitCacheVO>> entry : cplxGroup.entrySet()) {
                Map<String, Object> cacheMap = new HashMap<>();
                String key = cacheManager.genKey(CacheDict.YCAR_KR_QUERY.getCachename(), entry.getKey());
                for (CpsaProfitCacheVO cpsaProfitCacheVO : entry.getValue()) {
                    String hashKey = genHashKey(cpsaProfitCacheVO);
                    cacheMap.put(hashKey, cpsaProfitCacheVO);
                }
                Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> map = cpsaProfitGroupService.groupProfit(cacheMap);
                CpsaProfitCacheHolder.put(key, map);
            }
        }
        logger.info("控润缓存大小：{}", CpsaProfitCacheHolder.size());
    }


    /**
     * 存入缓存 Map 中的 hashKey 保证唯一即可
     *
     * @param cpsaProfitCacheVO 控润信息
     * @return 存入缓存 键值
     */
    private String genHashKey(CpsaProfitCacheVO cpsaProfitCacheVO) {
        StringBuffer hashKey = new StringBuffer();
        hashKey.append(cpsaProfitCacheVO.getId());
        if (StringUtils.isNotBlank(cpsaProfitCacheVO.getCgShbh())) {
            hashKey.append("_");
            hashKey.append(cpsaProfitCacheVO.getCgShbh());
        } else {
            hashKey.append("_ALL");
        }
        if (StringUtils.isNotBlank(cpsaProfitCacheVO.getGyShbh())) {
            hashKey.append("_");
            hashKey.append(cpsaProfitCacheVO.getGyShbh());
        } else {
            hashKey.append("_ALL");
        }
        if (StringUtils.isNotBlank(cpsaProfitCacheVO.getZdid())) {
            hashKey.append("_");
            hashKey.append(cpsaProfitCacheVO.getZdid());
        } else {
            hashKey.append("_ALL");
        }
        String key = hashKey.toString();
        return cacheManager.genKey(CacheDict.YCAR_KR_QUERY.getCachename(), key);
    }

    /**
     * 数据库的控润信息按照按照产品类型分组
     *
     * @param cpsaProfitCacheVOs 有效的控润信息列表
     * @return 按照产品类型分组后的控润信息
     */
    private Map<String, List<CpsaProfitCacheVO>> groupByCplx(List<CpsaProfitCacheVO> cpsaProfitCacheVOs) {
        if (CollectionUtil.isNotEmpty(cpsaProfitCacheVOs)) {
            Map<String, List<CpsaProfitCacheVO>> map = new HashMap<>();
            for (CpsaProfitCacheVO cpsaProfitCacheVO : cpsaProfitCacheVOs) {
                if (StringUtils.isNotBlank(cpsaProfitCacheVO.getCplxid())) {
                    String[] cplxIds = cpsaProfitCacheVO.getCplxid().split("\\/");
                    for (String cplxId : cplxIds) {
                        List<CpsaProfitCacheVO> cpsaCacheVOs = map.get(cplxId);
                        if (cpsaCacheVOs == null) {
                            cpsaCacheVOs = new ArrayList<>();
                            cpsaCacheVOs.add(cpsaProfitCacheVO);
                            map.put(cplxId, cpsaCacheVOs);
                        } else {
                            cpsaCacheVOs.add(cpsaProfitCacheVO);
                        }
                    }
                }
            }
            return map;
        }
        return null;
    }
}