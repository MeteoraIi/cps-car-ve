package cn.vetech.center.usecar.setting.buyerfilter.service;

import cn.vetech.center.car.entity.GyJkxx;
import cn.vetech.center.car.entity.GyShfzShmx;
import cn.vetech.center.car.service.GyJkxxService;
import cn.vetech.center.car.service.GyShfzShmxService;
import cn.vetech.center.usecar.authority.MerchantAuthorityService;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.cache.CacheDict;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.entity.usecar.YcCgsgl;
import cn.vetech.center.usecar.entity.usecar.YcCgsglShmx;
import cn.vetech.center.usecar.entity.usecar.YcCgsglShzmx;
import cn.vetech.center.usecar.entity.usecar.YcCgsglZd;
import cn.vetech.center.usecar.entity.usecar.YcCpfgb;
import cn.vetech.center.usecar.entity.usecar.YcCpfgbMx;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.service.usecar.YcCgsglService;
import cn.vetech.center.usecar.service.usecar.YcCgsglShmxService;
import cn.vetech.center.usecar.service.usecar.YcCgsglShzmxService;
import cn.vetech.center.usecar.service.usecar.YcCgsglZdService;
import cn.vetech.center.usecar.service.usecar.YcCpfgbMxService;
import cn.vetech.center.usecar.service.usecar.YcCpfgbService;
import cn.vetech.center.usecar.setting.buyerfilter.dto.BuyerFilterAddDTO;
import cn.vetech.center.usecar.setting.buyerfilter.dto.BuyerFilterBookDTO;
import cn.vetech.center.usecar.setting.buyerfilter.dto.BuyerFilterDTO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.BuyerFilterCacheVO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.BuyerFilterEditVO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.BuyerFilterVO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.CityVO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.CpVO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.SfVO;
import cn.vetech.center.usecar.setting.buyerfilter.vo.ZdVO;
import com.baomidou.mybatisplus.plugins.Page;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vetech.core.api.RestResponse;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.text.ToPinYin;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vetech on 2017/10/23.
 *
 * @author xujin
 * @since 2017/10/23.
 */
@Service
public class BuyerFilterSetService {

    /**
     * 日志记录类
     */
    private static final Logger logger = LoggerFactory.getLogger(BuyerFilterSetService.class);

    /**
     * 用车采购商过滤 服务
     */
    @Autowired
    private YcCgsglService ycCgsglService;
    /**
     * 用车采购商过滤 商户明细服务
     */
    @Autowired
    private YcCgsglShmxService ycCgsglShmxService;
    /**
     * 用车采购商过滤 商户组明细服务
     */
    @Autowired
    private YcCgsglShzmxService ycCgsglShzmxService;
    /**
     * 用车采购商过滤 商户组明细服务
     */
    @Autowired
    private YcCgsglZdService ycCgsglZdService;
    /**
     * 用车产品授权
     */
    @Autowired
    private YcCpfgbService ycCpfgbService;
    /**
     * 用车产品授权明细
     */
    @Autowired
    private YcCpfgbMxService ycCpfgbMxService;
    /**
     * 查询预订采购过滤
     */
    @Autowired
    private BuyerFilterForFilterService buyerFilterForFilterService;
    /**
     * 供应商服务
     */
    @Autowired
    private GyJkxxService gyJkxxService;
    /**
     * 供应商户分组
     */
    @Autowired
    private GyShfzShmxService gyShfzShmxService;
    /**
     * 缓存 管理器
     */
    @Autowired
    private RedisCacheManage iVeCacheManage;

    @Autowired
    private MerchantAuthorityService merchantAuthorityService;
    /**
     * 公共服务
     */
    @Autowired
    private BuyerBookCommonService buyerBookCommonService;
    /**
     * @param pageDTO 分页查询条件
     * @return page
     */
    public Page<YcCgsgl> selectPage(PageDTO<BuyerFilterDTO> pageDTO) {
        List<String> buyerList = merchantAuthorityService.getBuyerNoByUserNo(pageDTO.getData().getBh());
        if(CollectionUtils.isNotEmpty(buyerList)){
            List<String> ids = ycCgsglShzmxService.authFilter(buyerList);
            pageDTO.getData().setAuthIds(ids);
        }
        Page<YcCgsgl> voPage = ycCgsglService.selectPage(pageDTO);
        return voPage;
    }

    /**
     * @param ycCgsgl 查询条件
     * @return boolean
     */
    public Boolean upDown(YcCgsgl ycCgsgl) {
        logger.info("采购过滤规则，禁用启用或者审核，更新缓存！");
        Date now = new Date();
        if (ycCgsgl.getShzt() != null) {
            ycCgsgl.setShDatetime(now);
        }
        boolean upDownSuccess = ycCgsglService.upDown(ycCgsgl);
//        buyerFilterCacheService.cacheAll();
        logger.info("上下架采购过滤，更新采购过滤缓存");
        getValidCgglMapAndCache();
        return upDownSuccess;
    }

    /**
     * @return boolean
     */
    public List<CpVO> selectCpData() {
        String key = iVeCacheManage.genKey(CacheDict.YCAR_CPFGB_CPDATA_FOR_BUYERFILTER.getCachename(), "");
        Map<String, Object> map = iVeCacheManage.getEntriesHash(key);
        if (map == null || map.size() == 0) {
            map = getCpDataAndCacheAll();
            if (map == null || map.size() == 0) {
                return null;
            }
        }
        return (List<CpVO>) map.get("cpdata");
    }

    /**
     * 从数据库中获取数据并存缓存
     *
     * @return map
     */
    public Map<String, Object> getCpDataAndCacheAll() {
        //先清理缓存
        String key = iVeCacheManage.genKey(CacheDict.YCAR_CPFGB_CPDATA_FOR_BUYERFILTER.getCachename(), "");
        iVeCacheManage.remove(key);

        //1.查出所有产品
        List<YcCpfgbMx> ycCpfgbMxList = ycCpfgbMxService.getList();
        //2.遍历ycCpfgbMxList,得到所有产品(8种产品)
        List<String> cplxidList = new ArrayList<>();
        List<CpVO> cpVOList = new ArrayList<>();
        for (YcCpfgbMx ycCpfgbMx : ycCpfgbMxList) {
            String cplxid = ycCpfgbMx.getCplxid();
            if (!cplxidList.contains(cplxid)) {
                cplxidList.add(cplxid);
                CpVO cpVO = BeanMapper.map(ycCpfgbMx, CpVO.class);
                cpVOList.add(cpVO);
            }
        }
        //3.遍历cpVOList,得到每个产品对应的List<cpfgid>

        for (CpVO cpVO : cpVOList) {
            List<String> cpfgidList = new ArrayList<>();
            for (YcCpfgbMx ycCpfgbMx : ycCpfgbMxList) {
                if (StringUtils.equals(ycCpfgbMx.getCplxid(), cpVO.getCplxid())) {
                    cpfgidList.add(ycCpfgbMx.getCpfgid());
                }
            }
            //4.通过cpfgidList得到yc_cpfgb的列表
            List<YcCpfgb> sfYcCpfgbList = ycCpfgbService.selectBatchIds(cpfgidList);
            //5.遍历ycCpfgbList 找出所有省份sfVOList
            List<SfVO> sfVOList = new ArrayList<>();
            List<String> sfidList = new ArrayList<>();
            for (YcCpfgb ycCpfgb : sfYcCpfgbList) {
                if (ycCpfgb == null) {
                    continue;
                }
                if (!sfidList.contains(ycCpfgb.getSfid())) {
                    sfidList.add(ycCpfgb.getSfid());
                    SfVO sfVO = BeanMapper.map(ycCpfgb, SfVO.class);
                    sfVOList.add(sfVO);
                }
            }
            sfVOList = sortBySf(sfVOList);
            //6.把得到的省份列表放入cpVOList中
            cpVO.setSfVOList(sfVOList);

            //7.遍历sfVOList，得到单个省对应的所有城市
            for (SfVO sfVO : sfVOList) {
                List<YcCpfgb> cityYcCpfgbList = new ArrayList<>();
                List<String> csidList = new ArrayList<>();
                for (YcCpfgb ycCpfgb : sfYcCpfgbList) {
                    if (ycCpfgb == null) {
                        continue;
                    }
                    if (StringUtils.equals(sfVO.getSfid(), ycCpfgb.getSfid())) {
                        if (!csidList.contains(ycCpfgb.getCsid())) {
                            csidList.add(ycCpfgb.getCsid());
                            cityYcCpfgbList.add(ycCpfgb);
                        }
                    }
                }
                List<CityVO> cityVOList = BeanMapper.mapList(cityYcCpfgbList, YcCpfgb.class, CityVO.class);
                //8.把得到的城市列表放入SfVO中
                sfVO.setCityVOList(cityVOList);

                //9.遍历cityVOList，得到城市对应的所有站点
                for (CityVO cityVO : cityVOList) {
                    List<YcCpfgb> zdYcCpfgbList = new ArrayList<>();
                    for (YcCpfgb ycCpfgb : sfYcCpfgbList) {
                        if (ycCpfgb == null) {
                            continue;
                        }
                        if (StringUtils.equals(cityVO.getCsid(), ycCpfgb.getCsid())) {
                            zdYcCpfgbList.add(ycCpfgb);
                        }
                    }
                    List<ZdVO> zdVOList = BeanMapper.mapList(zdYcCpfgbList, YcCpfgb.class, ZdVO.class);
                    //10.把得到的所有站点放入CityVO中
                    cityVO.setZdVOList(zdVOList);
                }
            }
        }

        Map<String, Object> map = new HashedMap();
        if (cpVOList != null) {
            map.put("cpdata", cpVOList);
            iVeCacheManage.putAllHash(key, map);
        }
        return map;
    }

    /**
     * 通过省份名称拼音排序
     *
     * @param sfVOList 省份列表
     * @return list
     */
    private List<SfVO> sortBySf(List<SfVO> sfVOList) {
        Collections.sort(sfVOList, new Comparator<SfVO>() {
            @Override
            public int compare(SfVO o1, SfVO o2) {
                String szm1 = ToPinYin.getPinYinHeadChar(o1.getSfmc());
                String szm2 = ToPinYin.getPinYinHeadChar(o2.getSfmc());
                return szm1.compareTo(szm2);
            }
        });

        return sfVOList;
    }

    /**
     * 根据id查主表子表的所有数据
     *
     * @param id 主表id
     * @return vo
     */
    public BuyerFilterEditVO getDetail(String id) {
        YcCgsgl ycCgsgl = ycCgsglService.selectById(id);
        BuyerFilterEditVO editVO = BeanMapper.map(ycCgsgl, BuyerFilterEditVO.class);

        String cgsglid = ycCgsgl.getId();
        if (ycCgsgl.getCgsglfs().compareTo(BigDecimal.valueOf(UseCarConstant.TWO)) == 0) {          //采购商商户列表
            List<YcCgsglShmx> cgsShmxList = ycCgsglShmxService.getListByYcCgsglid(cgsglid, "1");
            editVO.setZdcgsList(cgsShmxList);
        }
        if (ycCgsgl.getCgsglfs().compareTo(BigDecimal.valueOf(UseCarConstant.THREE)) == 0) {        //采购商商户组列表
            List<YcCgsglShzmx> cgsShzmxList = ycCgsglShzmxService.getListByYcCgsglid(cgsglid, "1");
            editVO.setZdcgszList(cgsShzmxList);
        }

        if (ycCgsgl.getGysglfs().compareTo(BigDecimal.valueOf(UseCarConstant.TWO)) == 0) {          //供应商商户列表
            List<YcCgsglShmx> gysShmxList = ycCgsglShmxService.getListByYcCgsglid(cgsglid, "2");
            editVO.setZdgysList(gysShmxList);
        }
        if (ycCgsgl.getGysglfs().compareTo(BigDecimal.valueOf(UseCarConstant.THREE)) == 0) {        //供应商商户组列表
            List<YcCgsglShzmx> gysShzmxList = ycCgsglShzmxService.getListByYcCgsglid(cgsglid, "2");
            editVO.setZdgyszList(gysShzmxList);
        }

        if (StringUtils.isNotBlank(ycCgsgl.getCplx())) {      //处理产品类型
            String[] cplxArr = null;
            if (StringUtils.equals(ycCgsgl.getCplx(), "--")) {
                cplxArr = new String[]{"10000101", "10000102", "10000201", "10000202", "10000301", "10000302", "10000401", "10000402","10000501"};
                editVO.setCplxAll("true");
            } else {
                cplxArr = ycCgsgl.getCplx().split("/");
            }
            editVO.setCplxArr(cplxArr);
        }

        if (StringUtils.equals(ycCgsgl.getSfzdzd(), "1")) {        //指定站点列表
            List<YcCgsglZd> zdList = ycCgsglZdService.getListByYcCgsglid(cgsglid);
            editVO.setZdList(zdList);
        }


        return editVO;
    }

    /**
     * @param buyerFilterAddDTO 条件
     * @return boolean
     */
    @Transactional
    public RestResponse addYcgl(BuyerFilterAddDTO buyerFilterAddDTO) {
        //检查是否存在设置类型相同的数据
        RestResponse restResponse = checkeSameFilter(buyerFilterAddDTO);
        if (!(boolean) restResponse.getResult()) {
            return restResponse;
        }
        YcCgsgl ycCgsgl = BeanMapper.map(buyerFilterAddDTO, YcCgsgl.class);
        String id = VeDate.getNo(UseCarConstant.SEVEN);
        ycCgsgl.setId(id);
        ycCgsgl.setZt(BigDecimal.ONE);      //默认启用
        ycCgsgl.setShzt(BigDecimal.ZERO);   //0
        ycCgsgl.setXgDatetime(VeDate.getNow());
        boolean result = ycCgsglService.insertData(ycCgsgl);        //采购过滤表中插入数据
        if (result) {     //采购过滤站点表中插入数据
            List<YcCgsglZd> zdList = buyerFilterAddDTO.getZdList();
            if (CollectionUtil.isNotEmpty(zdList)) {
                result = insertZdData(zdList, id);
            }
        }
        if (result) {     //采购商过滤商户明细表中插入数据
            List<YcCgsglShmx> ycCgsglShmxList = new ArrayList<>();
            if (buyerFilterAddDTO.getCgsglfs().compareTo(BigDecimal.valueOf(UseCarConstant.TWO)) == 0) {     //2 指采购商
                ycCgsglShmxList.addAll(buyerFilterAddDTO.getZdcgsList());
            }
            if (buyerFilterAddDTO.getGysglfs().compareTo(BigDecimal.valueOf(UseCarConstant.TWO)) == 0) {
                ycCgsglShmxList.addAll(buyerFilterAddDTO.getZdgysList());
            }
            if (CollectionUtil.isNotEmpty(ycCgsglShmxList)) {
                result = insertShmxData(ycCgsglShmxList, id);
            }
        }
        if (result) {
            List<YcCgsglShzmx> ycCgsglShzmxList = new ArrayList<>();
            if (buyerFilterAddDTO.getCgsglfs().compareTo(BigDecimal.valueOf(UseCarConstant.THREE)) == 0) {     //3 指采购商组
                ycCgsglShzmxList.addAll(buyerFilterAddDTO.getZdcgszList());
            }
            if (buyerFilterAddDTO.getGysglfs().compareTo(BigDecimal.valueOf(UseCarConstant.THREE)) == 0) {
                ycCgsglShzmxList.addAll(buyerFilterAddDTO.getZdgyszList());
            }
            if (CollectionUtil.isNotEmpty(ycCgsglShzmxList)) {
                result = insertShzmxData(ycCgsglShzmxList, id);
            }
        }
        String msg = result ? "数据加载成功" : "数据加载失败";
        RestResponse rest = new RestResponse();
        rest.setResult(result);
        rest.setResult(msg);
        return rest;
    }
    /**
     * @param buyerFilterAddDTO 条件
     * @return boolean
     */
    @Transactional
    public RestResponse editYcgl(BuyerFilterAddDTO buyerFilterAddDTO) {
        //检查是否存在设置类型相同的数据
        RestResponse restResponse = checkeSameFilter(buyerFilterAddDTO);
        if (!(boolean) restResponse.getResult()) {
            return restResponse;
        }
        YcCgsgl ycCgsgl = BeanMapper.map(buyerFilterAddDTO, YcCgsgl.class);
        String id = buyerFilterAddDTO.getId();
        ycCgsgl.setZt(BigDecimal.ONE);      //默认启用
        ycCgsgl.setShzt(BigDecimal.ZERO);   //0
        ycCgsgl.setXgDatetime(VeDate.getNow());

        boolean result = ycCgsglService.updataData(ycCgsgl);        //采购过滤表中,更新数据
        if (result) {     //采购过滤站点表中，先删除数据再插入数据
            //先删除
            deleteZdByCgsglid(id);
            //再插入
            List<YcCgsglZd> zdList = buyerFilterAddDTO.getZdList();
            if (CollectionUtil.isNotEmpty(zdList)) {
                result = insertZdData(zdList, id);
            }
        }
        if (result) {     //采购商过滤商户明细表中先删除，再插入数据
            //先删除
            deleteShmxByCgsglid(id);
            //再插入
            List<YcCgsglShmx> ycCgsglShmxList = new ArrayList<>();
            if (buyerFilterAddDTO.getCgsglfs().compareTo(BigDecimal.valueOf(UseCarConstant.TWO)) == 0) {     //2 指采购商
                ycCgsglShmxList.addAll(buyerFilterAddDTO.getZdcgsList());
            }
            if (buyerFilterAddDTO.getGysglfs().compareTo(BigDecimal.valueOf(UseCarConstant.TWO)) == 0) {
                ycCgsglShmxList.addAll(buyerFilterAddDTO.getZdgysList());
            }
            if (CollectionUtil.isNotEmpty(ycCgsglShmxList)) {
                result = insertShmxData(ycCgsglShmxList, id);
            }
        }
        if (result) {
            //先删除
            deleteShzmxByCgsglid(id);
            //再插入
            List<YcCgsglShzmx> ycCgsglShzmxList = new ArrayList<>();
            if (buyerFilterAddDTO.getCgsglfs().compareTo(BigDecimal.valueOf(UseCarConstant.THREE)) == 0) {     //3 指采购商组
                ycCgsglShzmxList.addAll(buyerFilterAddDTO.getZdcgszList());
            }
            if (buyerFilterAddDTO.getGysglfs().compareTo(BigDecimal.valueOf(UseCarConstant.THREE)) == 0) {
                ycCgsglShzmxList.addAll(buyerFilterAddDTO.getZdgyszList());
            }
            if (CollectionUtil.isNotEmpty(ycCgsglShzmxList)) {
                result = insertShzmxData(ycCgsglShzmxList, id);
            }
//            buyerFilterCacheService.cacheAll();
            logger.info("编辑采购过滤，更新采购过滤缓存");
            getValidCgglMapAndCache();
        }
        String msg = result ? "数据加载成功" : "数据加载失败";
        RestResponse rest = new RestResponse();
        rest.setResult(result);
        rest.setResult(msg);
        return rest;
    }


    /**
     * 检查是否存在设置类型相同的数据
     *
     * @param buyerFilterAddDTO 入参
     * @return RestResponse 检查结果
     */
    private RestResponse checkeSameFilter(BuyerFilterAddDTO buyerFilterAddDTO) {
        RestResponse rest = new RestResponse();
        rest.setResult(false);
        String gllx = buyerFilterAddDTO.getGllx();
        if (StringUtils.isBlank(gllx) || buyerFilterAddDTO.getCgsglfs() == null) {
            rest.setMessage("传入数据有误");
            return rest;
        }
        //查询表数据
        List<YcCgsgl> list = ycCgsglService.getListByGllxAndCgsglfs(gllx, String.valueOf(buyerFilterAddDTO.getCgsglfs()));
        if (CollectionUtils.isEmpty(list)) {
            rest.setResult(true);
            return rest;
        }
        //应该过滤掉和自己相同的那条数据
        List<YcCgsgl> cgsglList = new ArrayList<>();
        for (YcCgsgl cgsgl : list) {
            if (StringUtils.equals(buyerFilterAddDTO.getId(), cgsgl.getId())) {
                continue;
            }
            cgsglList.add(cgsgl);
        }
        if (CollectionUtils.isEmpty(cgsglList)) {
            rest.setResult(true);
            return rest;
        }
        //开始验证商户管理方式
        if ("1".equals(String.valueOf(buyerFilterAddDTO.getCgsglfs()))) {
            //如果是设置的全部采购商
            rest.setMessage("已有相同类型的设置，请检查！已存在的过滤规则名称："+list.get(0).getGlmc());
            return rest;
        }
        if ("2".equals(String.valueOf(buyerFilterAddDTO.getCgsglfs()))) {
            List<YcCgsglShmx> editList = buyerFilterAddDTO.getZdcgsList();
            if (CollectionUtils.isEmpty(editList)) {
                rest.setMessage("传入数据有误");
                return rest;
            }
            //指定采购商,查询规则对应的商户明细
            List<YcCgsglShmx> exsistList = new ArrayList<>();
            for (YcCgsgl ycCgsgl1 : cgsglList) {
                List<YcCgsglShmx> shmxList = ycCgsglShmxService.getListByYcCgsglid(ycCgsgl1.getId(), "1");
                if (CollectionUtils.isEmpty(shmxList)) {
                    continue;
                }
                exsistList.addAll(shmxList);
            }
            if (CollectionUtils.isEmpty(exsistList)) {
                rest.setResult(true);
                return rest;
            }
            List<String> shbhList = new ArrayList<>();
            for (YcCgsglShmx ycCgsglShmx : exsistList) {
                if (!shbhList.contains(ycCgsglShmx.getShid())) {
                    shbhList.add(ycCgsglShmx.getShid());
                    for (YcCgsglShmx shmx : editList) {
                        if (StringUtils.equals(ycCgsglShmx.getShid(),shmx.getShid())) {
                            String cgsglid = ycCgsglShmx.getCgsglid();
                            YcCgsgl ycCgsgl = ycCgsglService.selectById(cgsglid);
                            rest.setMessage("已有相同类型的设置，请检查！已存在的过滤规则名称："+ycCgsgl.getGlmc());
                            return rest;
                        }
                    }
                }
            }
            //对比保存的商户明细
            for (YcCgsglShmx shmx : editList) {
                if (shbhList.contains(shmx.getShid())) {
                    rest.setMessage("已有相同类型的设置，请检查");
                    return rest;
                }
            }
        }
        if ("3".equals(String.valueOf(buyerFilterAddDTO.getCgsglfs()))) {
            List<YcCgsglShzmx> editList = buyerFilterAddDTO.getZdcgszList();
            if (CollectionUtils.isEmpty(editList)) {
                rest.setMessage("传入数据有误");
                return rest;
            }
            //指定采购商,查询规则对应的商户分组明细
            List<YcCgsglShzmx> exsistList = new ArrayList<>();
            for (YcCgsgl ycCgsgl1 : cgsglList) {
                List<YcCgsglShzmx> shfzList = ycCgsglShzmxService.getListByYcCgsglid(ycCgsgl1.getId(), "1");
                if (CollectionUtils.isEmpty(shfzList)) {
                    continue;
                }
                exsistList.addAll(shfzList);
            }
            if (CollectionUtils.isEmpty(exsistList)) {
                rest.setResult(true);
                return rest;
            }
            List<String> shfzList = new ArrayList<>();
            for (YcCgsglShzmx ycCgsglShzmx : exsistList) {
                if (!shfzList.contains(ycCgsglShzmx.getShzid())) {
                    shfzList.add(ycCgsglShzmx.getShzid());
                    //对比保存的商户明细
                    for (YcCgsglShzmx shmx : editList) {
                        if (StringUtils.equals(ycCgsglShzmx.getShzid(),shmx.getShzid())) {
                            String cgsglid = ycCgsglShzmx.getCgsglid();
                            YcCgsgl ycCgsgl = ycCgsglService.selectById(cgsglid);
                            rest.setMessage("已有相同类型的设置，请检查！已存在的过滤规则名称："+ycCgsgl.getGlmc());
                            return rest;
                        }
                    }
                }
            }
            //对比保存的商户明细
            for (YcCgsglShzmx shmx : editList) {
                if (shfzList.contains(shmx.getShzid())) {
                    rest.setMessage("已有相同类型的设置，请检查");
                    return rest;
                }
            }
        }
        rest.setResult(true);
        return rest;
    }


    /**
     * @param ycCgsgl 这里只有主表id
     * @return boolean
     */
    @Transactional
    public boolean toDelete(YcCgsgl ycCgsgl) {
        boolean result = ycCgsglService.toDeleteById(ycCgsgl);        //采购过滤表中,更新数据
        String id = ycCgsgl.getId();
        if (result) {     //采购过滤站点表中，先删除数据再插入数据
            deleteZdByCgsglid(id);
        }
        if (result) {     //采购商过滤商户明细表中先删除，再插入数据
            deleteShmxByCgsglid(id);
        }
        if (result) {
            deleteShzmxByCgsglid(id);
        }
        logger.info("删除采购过滤，更新采购过滤缓存");
        getValidCgglMapAndCache();
        return result;
    }

    /**
     * 批量插入站点
     *
     * @param zdList  站点lisg
     * @param cgsglId 主表id
     * @return boolean
     */
    private boolean insertZdData(List<YcCgsglZd> zdList, String cgsglId) {
        for (YcCgsglZd zd : zdList) {
            zd.setId(VeDate.getNo(UseCarConstant.SEVEN));
            zd.setCgsglid(cgsglId);
        }
        boolean b = ycCgsglZdService.insertBatchData(zdList);
        return b;
    }

    /**
     * 批量删除
     *
     * @param cgsglId 主表id
     * @return boolean
     */
    private boolean deleteZdByCgsglid(String cgsglId) {
        boolean b = ycCgsglZdService.deleteByCgsglid(cgsglId);
        return b;
    }

    /**
     * 批量插入商户
     *
     * @param shmxList 商户list
     * @param cgsglId  主表id
     * @return boolean
     */
    private boolean insertShmxData(List<YcCgsglShmx> shmxList, String cgsglId) {
        for (YcCgsglShmx shmx : shmxList) {
            shmx.setId(VeDate.getNo(UseCarConstant.SEVEN));
            shmx.setCgsglid(cgsglId);
        }
        boolean b = ycCgsglShmxService.insertBatchData(shmxList);
        return b;
    }

    /**
     * 批量删除
     *
     * @param cgsglid 主表id
     * @return boolean
     */
    private boolean deleteShmxByCgsglid(String cgsglid) {
        boolean b = ycCgsglShmxService.deleteShmxByCgsglid(cgsglid);
        return b;
    }

    /**
     * 批量插入商户组
     *
     * @param shzmxList 商户list
     * @param cgsglId   主表id
     * @return boolean
     */
    private boolean insertShzmxData(List<YcCgsglShzmx> shzmxList, String cgsglId) {
        for (YcCgsglShzmx shzmx : shzmxList) {
            shzmx.setId(VeDate.getNo(UseCarConstant.SEVEN));
            shzmx.setCgsglid(cgsglId);
        }
        boolean b = ycCgsglShzmxService.insertBatchData(shzmxList);
        return b;
    }

    **
     * 批量插入商户组
     *
     * @param cgsglId 主表id
     * @return boolean
     */
    private boolean deleteShzmxByCgsglid(String cgsglId) {
        boolean b = ycCgsglShzmxService.deleteShzmxByCgsglid(cgsglId);
        return b;
    }

    /***************对外提供的接口，已废弃，请查看bookingFilter****************/
    /**
     * 查询预订数据 单条过滤
     *
     * @param bookDTO 要查的条件
     * @return boolean result=true是过滤通过(可以显示在页面中)，false是过滤不通过。
     */
    public boolean bookingFilterOld(BuyerFilterBookDTO bookDTO) {
        if (StringUtils.equals(bookDTO.getCplx(), UsecarProductTypeEnum.zc.getCode())) {
            //专快车在gllx=3的时候匹配
            return !isMatchByGllx(bookDTO, "3");
        }
        logger.info("非专快车，开始进入用车查询预订采购过滤。。。");
        try {
            //走缓存的用车采购过滤信息
            BuyerFilterCacheVO cacheVO = buyerFilterForFilterService.getBuyerFilterCacheVO(bookDTO);
            /**
             * 8个产品类型中，如果缓存中有值
             */
            if (cacheVO != null) {
                /**缓存中有过滤规则,匹配gllx=1的情况*/
                String zdid = bookDTO.getZdid();
                String sfzdzd = cacheVO.getSfzdzd();
                List<String> zdidList = cacheVO.getZdidList();
                if (StringUtils.equals(cacheVO.getGllx(), "1")) {
                    if (StringUtils.equals(sfzdzd, "0")) {
                        return false;
                    } else if (StringUtils.equals(sfzdzd, "1") && zdidList.contains(zdid)) {
                        return false;
                    }
                }
                if (StringUtils.equals(cacheVO.getGllx(), "2") || StringUtils.equals(cacheVO.getGllx(), "3")) {
                    if (StringUtils.equals(sfzdzd, "1") && (!zdidList.contains(zdid))) {
                        return false;
                    }
                }
            } else {
                logger.info("没有查询到相关过滤规则。");
            }
        } catch (Exception e) {
            logger.error("用车查询预订采购过滤, 走缓存异常;直接放行。");
            return true;
        }
        return true;
    }

    /**
     * 适用于专快车
     *
     * @param bookDTO 过滤条件
     * @param gllx    过滤类型
     * @return ycCgsgl
     */
    private boolean isMatchByGllx(BuyerFilterBookDTO bookDTO, String gllx) {
        /**定义返回,
         * 注意：true表示没有匹配到，false表示匹配到了过滤规则
         */
        boolean result = false;
        String cgShbh = bookDTO.getCgShbh();
        String gyShbh = bookDTO.getGyShbh();
        if (StringUtils.isNotBlank(cgShbh) && StringUtils.isNotBlank(gyShbh)) {
            //查询所有的审核通过的对应gllx的过滤规则，这里gllx=2或者3
            List<YcCgsgl> ycCgsglList = ycCgsglService.getListByGllx(gllx);
            if (CollectionUtil.isNotEmpty(ycCgsglList)) {
                for (YcCgsgl cgsgl : ycCgsglList) {
                    boolean b = isMatchYcCgsgl(cgsgl, bookDTO, gllx);
                    //至少有一条匹配。
                    if (b) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 是否符合规则
     *
     * @param ycCgsgl 采购过滤条件
     * @param bookDTO 过滤比较信息
     * @param gllx    过滤类型
     * @return boolean
     */
    private boolean isMatchYcCgsgl(YcCgsgl ycCgsgl, BuyerFilterBookDTO bookDTO, String gllx) {
        boolean result = true;
        String cgsglId = ycCgsgl.getId();
        int cgsglfs = ycCgsgl.getCgsglfs().intValue();
        int gysglfs = ycCgsgl.getGysglfs().intValue();
        List<String> cgShbhList = new ArrayList<>();
        if (cgsglfs == UseCarConstant.TWO) {
            List<YcCgsglShmx> shmxList = ycCgsglShmxService.getListByYcCgsglid(cgsglId, "1");
            for (YcCgsglShmx shmx : shmxList) {
                if (!cgShbhList.contains(shmx.getShid())) {
                    cgShbhList.add(shmx.getShid());
                }
            }
        } else if (cgsglfs == UseCarConstant.THREE) {
            List<YcCgsglShzmx> shzmxList = ycCgsglShzmxService.getListByYcCgsglid(cgsglId, "1");
            for (YcCgsglShzmx shzmx : shzmxList) {
                String shzmxId = shzmx.getShzid();
                List<GyShfzShmx> gyShfzList = gyShfzShmxService.selectShfzmxList(shzmxId);
                for (GyShfzShmx shmx : gyShfzList) {
                    if (!cgShbhList.contains(shmx.getShbh())) {
                        cgShbhList.add(shmx.getShbh());
                    }
                }
            }
        }

        result = cgsglfs == 1 || cgShbhList.contains(bookDTO.getCgShbh());

        if (result) {
            List<String> gyShbhList = new ArrayList<>();
            if (gysglfs == UseCarConstant.TWO) {
                List<YcCgsglShmx> shmxList = ycCgsglShmxService.getListByYcCgsglid(cgsglId, "2");
                for (YcCgsglShmx shmx : shmxList) {
                    if (!gyShbhList.contains(shmx.getShid())) {
                        gyShbhList.add(shmx.getShid());
                    }
                }
            } else if (gysglfs == UseCarConstant.THREE) {
                List<YcCgsglShzmx> shzmxList = ycCgsglShzmxService.getListByYcCgsglid(cgsglId, "2");
                for (YcCgsglShzmx shzmx : shzmxList) {
                    String shzmxId = shzmx.getShzid();
                    List<GyShfzShmx> gyShfzList = gyShfzShmxService.selectShfzmxList(shzmxId);
                    for (GyShfzShmx shmx : gyShfzList) {
                        if (!gyShbhList.contains(shmx.getShbh())) {
                            gyShbhList.add(shmx.getShbh());
                        }
                    }
                }
            }
            result = gysglfs == 1 || gyShbhList.contains(bookDTO.getCgShbh());
        }

        if (result && StringUtils.equals(gllx, "2")) {
            //另外再匹配产品类型与站点
            String ycCgsglCplx = ycCgsgl.getCplx();
            String bookDTOCplx = bookDTO.getCplx();
            if (StringUtils.equals(ycCgsglCplx, "--")) {
                result = true;
            } else {
                result = ycCgsglCplx.indexOf(bookDTOCplx) >= 0;
            }
            //匹配站点
            if (result) {
                result = ycCgsglZdService.isHaveData(cgsglId, bookDTOCplx, bookDTO.getZdid());
            }
        }

        return result;
    }

    /**
     * 过滤
     *
     * @param bookDTO 要查的条件,bookDTO除了gyshbh不传，zdid可不传，其他两个参数都要传
     * @return 返回过滤后的供应商列表
     */
    public List<String> bookingBeforeFilter(BuyerFilterBookDTO bookDTO) {
        List<String> gysList = bookingBeforeFilterByRule(bookDTO);
        logger.info("前置过滤后的供应商={}",JsonMapper.nonEmptyMapper().toJson(gysList));
        return filterEffectSupplier(gysList);
    }
    /**
     * 返回有效的供应商列表
     *
     * @param gysList 供应商列表
     * @return 返回有效的供应商列表
     */
    private List<String> filterEffectSupplier(List<String> gysList) {
        List<GyJkxx> gyJkxxList = gyJkxxService.selectEffectGyJkxxList();
        if (CollectionUtils.isEmpty(gyJkxxList)) {
            return gysList;
        }
        logger.info("查询到的有效供应商接口信息={}",JsonMapper.nonEmptyMapper().toJson(gyJkxxList.stream().map(GyJkxx::getShbh).collect(Collectors.toList())));
        Map<String, GyJkxx> mapSh = new HashMap<>();
        for (GyJkxx gys : gyJkxxList) {
            mapSh.put(gys.getShbh(), gys);
        }
        List<String> effectList = new ArrayList<>();
        for (String gysBh : gysList) {
            if (mapSh.get(gysBh) != null) {
                effectList.add(gysBh);
            }
        }
        return effectList;
    }
/**************************对查询预订提供的接口-new************************************/
    /**
     * 方法1：所有第三方供应商在查询之前进行过滤。UsecarGysapiEnum.java
     *
     * @param bookDTO 要查的条件,bookDTO除了gyshbh不传，zdid可不传，其他两个参数都要传
     * @return list 返回所有第三方供应商列表。由于在查询得到产品数据之后还要过滤一次，这里只是初步过滤
     */
    public List<String> bookingBeforeFilterByRule(BuyerFilterBookDTO bookDTO) {
        //定义返回的结果
        List<String> gysList = new ArrayList<>();

        //1.先查出所有的第三方供应商,这里要区分cplx==zkc
        List<String> allGysList = new ArrayList<>();
        for (UsecarGysApiEnum gysEnum : UsecarGysApiEnum.values()) {
            if (StringUtils.equals(bookDTO.getCplx(), UsecarProductTypeEnum.zc.getCode())) {
                if (StringUtils.containsIgnoreCase(gysEnum.getServiceInclude(), "S")) {
                    allGysList.add(gysEnum.getShbh());
                }
            } else {
                if (StringUtils.containsIgnoreCase(gysEnum.getServiceInclude(), "P")) {
                    allGysList.add(gysEnum.getShbh());
                }
            }
        }

        //2.从缓存中拿到过滤规则并匹配，如果没有匹配到则返回所有供应商
//        logger.info("查询第三方供应商的产品前，开始从UsecarGysApiEnum中筛选出要查询的供应商列表");
        String key = iVeCacheManage.genKey(CacheDict.YCAR_BUYER_FILTER_CGSHMAP.getCachename(), "");
        Map<String, Object> map = iVeCacheManage.getEntriesHash(key);
        if (map == null || map.size() == 0) {
            map = getValidCgglMapAndCache();
            if (map == null || map.size() == 0) {
                return allGysList;
            }
        }
        String cgShbh = bookDTO.getCgShbh();
        BuyerFilterVO filterVO = (BuyerFilterVO) map.get(cgShbh);
        //根据cgshbh没有查询到规则
        if (filterVO == null || map.size() == 0) {
            filterVO = (BuyerFilterVO) map.get(UseCarConstant.ALLCGS);
            if (filterVO == null || map.size() == 0) {
                logger.info("采购过滤：根据采购商户编号{}没有查询到规则", cgShbh);
                return allGysList;
            }
        }
        logger.info("{}获取到的采购商过滤规则:{}",cgShbh,JsonMapper.nonEmptyMapper().toJson(filterVO));
        //3.如果拿到过滤规则
        String gllx = filterVO.getGllx();
        String cplx = StringUtils.equals(filterVO.getCplx(), "--") ? "10000101/10000102/10000201/10000202/10000301/10000302/10000401/10000402/10000501" : filterVO.getCplx();
        String sfzdzd = filterVO.getSfzdzd();
        int glsglfs = filterVO.getGysglfs() == null ? UseCarConstant.ZERO : filterVO.getGysglfs().intValue();
        if (StringUtils.equals(gllx, "1")) {
            //专快车放行，非专快车在这里判断
            if (StringUtils.indexOf(cplx, bookDTO.getCplx()) >= 0) {
                //全部站点或者满足指定站点
                if (StringUtils.equals(sfzdzd, "0") || (StringUtils.equals(sfzdzd, "1") && matchByCplxMap(filterVO, bookDTO))) {
                    List<String> filterGysList = filterVO.getGyShbhList();
                    for (String gys : allGysList) {
                        if (glsglfs == UseCarConstant.ONE) {
                            continue;
                        }
                        if (CollectionUtils.isNotEmpty(filterGysList) && !filterGysList.contains(gys)) {
                            gysList.add(gys);
                        }
                    }
                    return gysList;
                }
            }
        } else if (StringUtils.equals(gllx, "2")) {
            //非专快车在这里判断，专快车过滤掉
            if (StringUtils.indexOf(cplx, bookDTO.getCplx()) >= 0) {
                //全部站点或者满足指定站点
                if (StringUtils.equals(sfzdzd, "0") || (StringUtils.equals(sfzdzd, "1") && matchByCplxMap(filterVO, bookDTO))) {
                    List<String> filterGysList = filterVO.getGyShbhList();
                    for (String gys : allGysList) {
                        if (glsglfs == UseCarConstant.ONE
                                || (CollectionUtils.isNotEmpty(filterGysList) && filterGysList.contains(gys))) {
                            gysList.add(gys);
                        }
                    }
                    return gysList;
                }
            }
        } else {
            if (StringUtils.indexOf(cplx, bookDTO.getCplx()) >= 0) {
                //全部站点或者满足指定站点
                if (StringUtils.equals(sfzdzd, "0") || (StringUtils.equals(sfzdzd, "1") && matchByCplxMap(filterVO, bookDTO))) {
                    List<String> filterGysList = filterVO.getGyShbhList();
                    for (String gys : allGysList) {
                        if (glsglfs == UseCarConstant.ONE
                                || (CollectionUtils.isNotEmpty(filterGysList) && filterGysList.contains(gys))) {
                            gysList.add(gys);
                        }
                    }
                    return gysList;
                }
            }
        }

        return allGysList;
    }

    /**
     * 方法2：把所有产品查出来之后再过滤
     *
     * @param bookDTO 要查的条件
     * @return boolean result=true是过滤通过(可以显示在页面中)，false是过滤不通过。
     */
    public boolean bookingFilter(BuyerFilterBookDTO bookDTO) {
//        getValidCgglMapAndCache();
        String key = iVeCacheManage.genKey(CacheDict.YCAR_BUYER_FILTER_CGSHMAP.getCachename(), "");
        Map<String, Object> map = iVeCacheManage.getEntriesHash(key);
        if (map == null || map.size() == 0) {
            map = getValidCgglMapAndCache();
            if (map == null || map.size() == 0) {
                return true;
            }
        }

        String cgShbh = bookDTO.getCgShbh();
        BuyerFilterVO filterVO = (BuyerFilterVO) map.get(cgShbh);
        //根据cgshbh没有查询到规则
        if (filterVO == null || map.size() == 0) {
            filterVO = (BuyerFilterVO) map.get(UseCarConstant.ALLCGS);
            if (filterVO == null || map.size() == 0) {
                logger.info("采购过滤：根据采购商户编号{}没有查询到规则", cgShbh);
                return true;
            }
        }
        String gllx = filterVO.getGllx();
        String cplx = StringUtils.equals(filterVO.getCplx(), "--") ? "10000101/10000102/10000201/10000202/10000301/10000302/10000401/10000402/10000501" : filterVO.getCplx();
        String sfzdzd = filterVO.getSfzdzd();
        String gysglfs = String.valueOf(filterVO.getGysglfs());
        //定义一个供应商条件，用来做判断,过滤供应商列表中不包含产品供应商
        boolean gysCondition = false;
        if (StringUtils.equals(gysglfs, "1")) {
            gysCondition = true;
        } else {
            if (filterVO.getGyShbhList().contains(bookDTO.getGyShbh())) {
                gysCondition = true;
            }
        }
        //1.匹配gllx=3的相同情况
        if (StringUtils.equals(gllx, "3")) {
            //匹配专快车
            if (StringUtils.equals(bookDTO.getCplx(), UsecarProductTypeEnum.zc.getCode())) {
                if (!"1".equals(sfzdzd)) {
                    return false;
                }
                Map<String, List<String>> cplxMap = filterVO.getCplxMap();
                for (List<String> zdidList : cplxMap.values()) {
                    if (CollectionUtil.isNotEmpty(zdidList) && zdidList.contains(bookDTO.getZdid())) {
                        return false;
                    }
                }
            }
            //如果匹配到供应商
            if (gysCondition) {
                if (StringUtils.equals(sfzdzd, "1")) {
                    if (!matchByCplxMap(filterVO, bookDTO)) {
                        return false;       //不匹配的站点过滤掉
                    }
                }
            } else {      //如果没有匹配到，则过滤掉匹配的产品类型下的站点
                if (cplx.indexOf(bookDTO.getCplx()) >= 0) {
                    if (StringUtils.equals(sfzdzd, "0")) {
                        return false;
                    } else if (StringUtils.equals(sfzdzd, "1") && matchByCplxMap(filterVO, bookDTO)) {
                        return false;
                    }
                }
            }
            //2.匹配gllx=2的情况
        } else if (StringUtils.equals(gllx, "2")) {
            //匹配专快车
            if (StringUtils.equals(bookDTO.getCplx(), UsecarProductTypeEnum.zc.getCode())) {
                if (!"1".equals(sfzdzd)) {
                    return false;
                }
                Map<String, List<String>> cplxMap = filterVO.getCplxMap();
                for (List<String> zdidList : cplxMap.values()) {
                    if (CollectionUtil.isNotEmpty(zdidList) && zdidList.contains(bookDTO.getZdid())) {
                        return false;
                    }
                }
            }
            //如果匹配到供应商
            if (gysCondition) {
                if (StringUtils.equals(sfzdzd, "1")) {
                    if (!matchByCplxMap(filterVO, bookDTO)) {
                        return false;       //不匹配的站点过滤掉
                    }
                }
            } else {      //如果没有匹配到供应商，则过滤掉匹配的产品类型下的站点
                if (cplx.indexOf(bookDTO.getCplx()) >= 0) {
                    if (StringUtils.equals(sfzdzd, "0")) {
                        return false;
                    } else if (StringUtils.equals(sfzdzd, "1") && matchByCplxMap(filterVO, bookDTO)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            //3.匹配gllx=1的相同情况
        } else {
            if (gysCondition && cplx.indexOf(bookDTO.getCplx()) >= 0) {
                if (StringUtils.equals(sfzdzd, "0")) {
                    logger.info("采购过滤：过滤规则过滤所有！");
                    return false;
                } else {
                    //sfzdzd=1时判断 cplxMap
                    if (matchByCplxMap(filterVO, bookDTO)) {
                        logger.info("采购过滤：过滤规则过滤掉匹配的产品！");
                        return false;
                    }
                }
            }
        }

        
        logger.info("采购过滤：通过过滤规则，放行！采购商过滤id为：{}", StringUtils.isNotBlank(filterVO.getId()) ? filterVO.getId() : "空");
        return true;
    }

    /**
     * sfzdzd=1 的时候调用。
     * 根据cplxMap中判断是否有站点
     *
     * @param filterVO 过滤条件
     * @param bookDTO  查询条件
     * @return b
     */
     public boolean matchByCplxMap(BuyerFilterVO filterVO, BuyerFilterBookDTO bookDTO) {
        Map<String, List<String>> cplxMap = filterVO.getCplxMap();
        //cplxMap 在sfzdzd=1的情况下（一般）不为空，为空的话说明没有站点返回false
        if (cplxMap != null) {
            List<String> zdidList = cplxMap.get(bookDTO.getCplx());
            //有匹配
            return CollectionUtil.isNotEmpty(zdidList) && zdidList.contains(bookDTO.getZdid());
        }
        return false;
    }

    /**
     * 使用须知：获取map.get(cgShbh),若果为空再获取map.get(cgShbh)；若果没有则放行
     * key 为cgShbh,Object为BuyerFilterVO
     *
     * @return 查询所有有效的过滤规则数据
     */
//    @Scheduled(fixedRate= 24*60*60)
    public Map<String, Object> getValidCgglMapAndCache() {
        //先清理缓存
        String key = iVeCacheManage.genKey(CacheDict.YCAR_BUYER_FILTER_CGSHMAP.getCachename(), "");
        iVeCacheManage.remove(key);

        //1.获取所有过滤规则
        List<BuyerFilterVO> cgsAllList = getFilterVOList();
        if (CollectionUtils.isEmpty(cgsAllList)) {
            return null;
        }
        //按采购商分组，且值是最符合的规则
        Map<String, Object> map = getCgsMap(cgsAllList);
        logger.info("用车采购商过滤规则存入缓存中数据:{}", JsonMapper.nonEmptyMapper().toJson(map));
        if (map != null) {
            iVeCacheManage.putAllHash(key, map);
        }
        return map;
    }

    /**
     * 获取采购商过滤规则表中的所有已审核，已启用的数据，
     * 并拆分成每条规则都有采购商编号的集合
     *
     * @return List<BuyerFilterVO> 所有规则集合
     */
    private List<BuyerFilterVO> getFilterVOList() {
        List<BuyerFilterVO> result = new ArrayList<>();
        List<YcCgsgl> ycCgsglList = ycCgsglService.getAllValidCggl();
        if (CollectionUtil.isEmpty(ycCgsglList)) {
            return result;
        }
        /**cgsglfs=1时，定义的所有采购商*/
        //2.遍历所有过滤规则，按照cgshbh拆分，此时一个采购商可能对应多个规则-------->得到cgsAllList
        BuyerFilterVO buyerFilterVO = null;
        Type<YcCgsgl> ycCgsglType = BeanMapper.getType(YcCgsgl.class);
        Type<BuyerFilterVO> filterVOType = BeanMapper.getType(BuyerFilterVO.class);
        for (YcCgsgl ycCgsgl : ycCgsglList) {
            if ("1".equals(String.valueOf(ycCgsgl.getCgsglfs()))) {
                //1全部采购商
                buyerFilterVO = BeanMapper.map(ycCgsgl, ycCgsglType, filterVOType);
                buyerFilterVO.setCgShbh(UseCarConstant.ALLCGS);
                result.add(buyerFilterVO);
                continue;
            }
            if ("2".equals(String.valueOf(ycCgsgl.getCgsglfs()))) {
                //2指定采购商
                List<YcCgsglShmx> shmxList = ycCgsglShmxService.getListByYcCgsglid(ycCgsgl.getId(), "1");
                if (CollectionUtil.isNotEmpty(shmxList)) {
                    for (YcCgsglShmx shmx : shmxList) {
                        buyerFilterVO = BeanMapper.map(ycCgsgl, ycCgsglType, filterVOType);
                        buyerFilterVO.setCgShbh(shmx.getShid());
                        // 不是同一个渠道，无效
                        if (StringUtils.isNotBlank(buyerFilterVO.getChannelId())) {
                            String channelId = buyerBookCommonService.getChannelIdByShbh(shmx.getShid());
                            if (StringUtils.isNotBlank(channelId) && !StringUtils.equals(channelId, buyerFilterVO.getChannelId())) {
                                logger.info("{}无效渠道，{}，{}",shmx.getShid(),channelId,buyerFilterVO.getChannelId());
                                continue;
                            }
                        }
                        result.add(buyerFilterVO);
                    }
                }
                continue;
            }
            if ("3".equals(String.valueOf(ycCgsgl.getCgsglfs()))) {
                //3按分组指定采购商组
                List<String> cgsList = this.getFzCgshbhList(ycCgsgl.getId(), "1");
                if (CollectionUtil.isNotEmpty(cgsList)) {
                    for (String cgs : cgsList) {
                        buyerFilterVO = BeanMapper.map(ycCgsgl, ycCgsglType, filterVOType);
                        buyerFilterVO.setCgShbh(cgs);
                        // 不是同一个渠道，无效
                        if (StringUtils.isNotBlank(buyerFilterVO.getChannelId())) {
                            String channelId = buyerBookCommonService.getChannelIdByShbh(cgs);
                            if (StringUtils.isNotBlank(channelId) && !StringUtils.equals(channelId, buyerFilterVO.getChannelId())) {
                                logger.info("{}无效渠道，{}，{}",cgs,channelId,buyerFilterVO.getChannelId());
                                continue;
                            }
                        }
                        result.add(buyerFilterVO);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 从所有的采购商过滤规则中针对每个供应商筛选出最符合的规则，存入map中。
     * @param filterVOS 过滤规则集合
     * @return 按采购商分组的规则Map
     */
    private Map<String,Object> getCgsMap(List<BuyerFilterVO> filterVOS){
        Map<String, Object> cgshMap = new HashMap<>();
        //1.得到采购商编号集合（去掉重复的编号）
        List<String> cgsList = new ArrayList<>();
        for (BuyerFilterVO filterVO : filterVOS) {
            if (!cgsList.contains(filterVO.getCgShbh())) {
                cgsList.add(filterVO.getCgShbh());
            }
        }
        return result;
    }

    /**
     * 从所有的采购商过滤规则中针对每个供应商筛选出最符合的规则，存入map中。
     * @param filterVOS 过滤规则集合
     * @return 按采购商分组的规则Map
     */
    private Map<String,Object> getCgsMap(List<BuyerFilterVO> filterVOS){
        Map<String, Object> cgshMap = new HashMap<>();
        //1.得到采购商编号集合（去掉重复的编号）
        List<String> cgsList = new ArrayList<>();
        for (BuyerFilterVO filterVO : filterVOS) {
            if (!cgsList.contains(filterVO.getCgShbh())) {
                cgsList.add(filterVO.getCgShbh());
            }
        }
        //2.遍历所有的采购商,得到采购商的多条规则后取最优规则
        List<BuyerFilterVO> cgsVOList = new ArrayList<>();
        for (String cgs : cgsList) {
            List<BuyerFilterVO> filterVOList = new ArrayList<>();
            for (BuyerFilterVO filterVO : filterVOS) {
                //编号相同则可视为此采购商的规则
                if (StringUtils.equals(cgs, filterVO.getCgShbh())) {
                    filterVOList.add(filterVO);
                }
                //如果规则是针对所有采购商的规则也要添加至集合中
                if (UseCarConstant.ALLCGS.equals(filterVO.getCgShbh()) && !UseCarConstant.ALLCGS.equals(cgs)) {
                    filterVOList.add(filterVO);
                }
            }
            //5.得到每个采购商对应的最符合的规则
            BuyerFilterVO filterVO = getMatchVO(filterVOList);
            if (filterVO == null) {
                continue;
            }
            //如果之前这条规则已经有了存入map中，则可以不用进行站点信息这些数据的处理
            if(cgshMap.containsKey(filterVO.getCgShbh())){
                continue;
            }
            //处理供应商，产品信息，站点信息
            handleGysCpZdInfo(filterVO);
            cgshMap.put(filterVO.getCgShbh(), filterVO);
        }
        return cgshMap;
    }

    /**
     * 处理供应商，产品信息，站点信息
     *
     * @param filterVO 规律规则
     */
    private void handleGysCpZdInfo(BuyerFilterVO filterVO) {
        //7.赋值gysList
        if ("2".equals(String.valueOf(filterVO.getGysglfs()))) {
            List<YcCgsglShmx> shmxList = ycCgsglShmxService.getListByYcCgsglid(filterVO.getId(), "2");
            if (CollectionUtil.isNotEmpty(shmxList)) {
                List<String> gysList = new ArrayList<>();
                for (YcCgsglShmx shmx : shmxList) {
                    gysList.add(shmx.getShid());
                }
                filterVO.setGyShbhList(gysList);
            }
        } else if ("3".equals(String.valueOf(filterVO.getGysglfs()))) {
            List<String> gysList = this.getFzCgshbhList(filterVO.getId(), "2");
            filterVO.setGyShbhList(gysList);
        }
        /**获取cplx对应的站点集合*/
        if ("1".equals(filterVO.getSfzdzd())) {
            //8.分析过滤信息，得到cplxMap并赋值。
            Map<String, List<String>> map = getCplxMap(filterVO.getId());
            filterVO.setCplxMap(map);
        }
    }

    /**
     * 过滤类型为3时，商户分组对应的所有商户。
     *
     * @param ycCgsglId 过滤id
     * @param shlx      商户类型1是采购，2是供应
     * @return list 商户明细表中的商户集合
     */
    private List<String> getFzCgshbhList(String ycCgsglId, String shlx) {
        //定义要返回的数据
        List<String> fzCgShbhList = new ArrayList<>();
        List<YcCgsglShzmx> shzmxList = ycCgsglShzmxService.getListByYcCgsglid(ycCgsglId, shlx);
        if (CollectionUtil.isNotEmpty(shzmxList)) {
            //所有商户组id
            List<String> shfzidList = new ArrayList<>();
            for (YcCgsglShzmx shzmx : shzmxList) {
                shfzidList.add(shzmx.getShzid());
            }
            if (CollectionUtil.isNotEmpty(shfzidList)) {
                //所有商户组id对应的 分组明细。里面可能有商户重复
                List<GyShfzShmx> shfzShmxList = gyShfzShmxService.getShfzShmx(shfzidList);
                if (CollectionUtil.isNotEmpty(shfzidList)) {
                    for (GyShfzShmx shfzShmx : shfzShmxList) {
                        if (!fzCgShbhList.contains(shfzShmx.getShbh())) {
                            fzCgShbhList.add(shfzShmx.getShbh());
                        }
                    }
                }
            }
        }
        return fzCgShbhList;
    }

    /**
     * 根据ycCgsglId 得到分类zdList
     *
     * @param cgsglId 采购过滤id
     * @return map
     */
     private Map<String, List<String>> getCplxMap(String cgsglId) {
        Map<String, List<String>> map = new HashMap<>();
        List<YcCgsglZd> zdList = ycCgsglZdService.getListByYcCgsglid(cgsglId);
        if (CollectionUtil.isNotEmpty(zdList)) {
            /**所有产品*/
            List<String> cplxList = new ArrayList<>();
            /**有站点 就有cplx，zdid*/
            for (YcCgsglZd zd : zdList) {
                if (!cplxList.contains(zd.getCplx())) {
                    cplxList.add(zd.getCplx());
                }
            }
            for (String cplx : cplxList) {
                List zdidList = new ArrayList();
                for (YcCgsglZd zd : zdList) {
                    if (StringUtils.equals(cplx, zd.getCplx())) {
                        zdidList.add(zd.getZdid());
                    }
                }
                map.put(cplx, zdidList);
            }
        }
        return map;
    }

    /**
     * 从一条采购商对应的多条规则中，找出最匹配的那条规则
     *  1.优先命中过滤规则为保留的，即保留>过滤。
     *  2.当过滤规则相同时，指定采购商>指定采购商组>全部采购商
     *  3.当采购商过滤方式相同时：去审批时间最近的
     *  (1>)-(2<)-(3>)
     * @param voList 条件list
     * @return vo
     */
    private BuyerFilterVO getMatchVO(List<BuyerFilterVO> voList) {

        BuyerFilterVO filterVO = null;
        if (CollectionUtil.isEmpty(voList)) {
            return filterVO;
        }
        // 优先匹配有渠道的
        List<BuyerFilterVO> newVoList = voList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getChannelId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(newVoList)) {
            voList = newVoList;
        }
        //开始处理，过滤得到符合规则的过滤规则
        int index = 0;
        filterVO = voList.get(index);
        //TODO 此处获取最优的规则可以优化，将jb改成去高值优先，在按gllx+jb+shDatetime排序。
        //直接从 >1 开始中遍历,返回i
        if (voList.size() > 1) {
            int gllx = Integer.parseInt(filterVO.getGllx());
            int jb = convertJb(filterVO.getCgsglfs().intValue());
            Date shDatetime = filterVO.getShDatetime();
            for (int i = 1; i < voList.size(); i++) {
                int gllx1 = Integer.parseInt(voList.get(i).getGllx());
                if (gllx1 > gllx) {
                    index = i;
                    gllx = gllx1;
                } else if (gllx1 == gllx) {
                    int jb1 = convertJb(voList.get(i).getCgsglfs().intValue());
                    if (jb1 < jb) {
                        index = i;
                        jb = jb1;
                    } else if (jb1 == jb) {
                        Date shDatetime1 = voList.get(i).getShDatetime();
                        if (shDatetime1.getTime() > shDatetime.getTime()) {         //shDatetime1在后，后审核的优先
                            index = i;
                            shDatetime = shDatetime1;
                        }
                    }
                }
            }
            filterVO = voList.get(index);
        }
        return filterVO;
    }

    /**
     * 从一条采购商对应的多条规则中，找出最匹配的那条规则
     *  1.优先命中过滤规则为保留的，即保留>过滤。
     *  2.当过滤规则相同时，指定采购商>指定采购商组>全部采购商
     *  3.当采购商过滤方式相同时：去审批时间最近的
     *  (1>)-(2<)-(3>)
     * @param voList 条件list
     * @return vo
     */
    private BuyerFilterVO getNewMatchVO(List<BuyerFilterVO> voList) {
        if(CollectionUtils.isNotEmpty(voList)){
            int index = voList.size()-1;
            voList.sort(Comparator.comparing(this::getSn));
            return voList.get(index);
        }
        return null;
    }

    /**
     *  获取排序的序号
     * @param vo 规则对象
     * @return 返回的序号
     */
    private String getSn(BuyerFilterVO vo){
        String sn = "";
        sn += vo.getGllx();
        sn += (UseCarConstant.FIVE - convertJb(vo.getCgsglfs().intValue()));
        sn += vo.getShDatetime().getTime();
        return sn;
    }

    /**
     * 根据过滤类型确定规则的优先级
     *
     * @param cgsglfs 1全部采购商 2指标采购商 3指定采购商组
     * @return int 优先级1，2 ,3
     */
    private int convertJb(int cgsglfs) {
        if (cgsglfs == UseCarConstant.ONE) {
            return UseCarConstant.THREE;
        }
        if (cgsglfs == UseCarConstant.TWO) {
            return UseCarConstant.ONE;
        }
        if (cgsglfs == UseCarConstant.THREE) {
            return UseCarConstant.TWO;
        }
        return UseCarConstant.FOUR;
    }
}