package cn.vetech.center.usecar.book.buyer.specicar.service;

import cn.vetech.center.car.entity.GySjdy;
import cn.vetech.center.car.service.GySjdyService;
import cn.vetech.center.cdsbase.api.vo.VeCityVO;
import cn.vetech.center.cdsbase.api.vo.YcCppVO;
import cn.vetech.center.cdsbase.api.vo.YcCxzVO;
import cn.vetech.center.customer.api.vo.ShShbVO;
import cn.vetech.center.usecar.apiclient.cds.IVeCityServiceClient;
import cn.vetech.center.usecar.apiclient.cds.IYcCxzServiceClient;
import cn.vetech.center.usecar.apiclient.customer.IShShbServiceClient;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookAsmsSpecialCar;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarProductModelVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarProductVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BuyerBookCxzVO;
import cn.vetech.center.usecar.cache.CarBaseDataCacheService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.UsecarConfig;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.YcDsxdRw;
import cn.vetech.center.usecar.service.usecar.YcDsxdRwService;
import cn.vetech.center.usecar.setting.profit.dto.CpsaUseCarProfitCacheDTO;
import cn.vetech.center.usecar.setting.profit.dto.KrjeTdjeHolder;
import cn.vetech.center.usecar.setting.profit.service.CpsaProfitFilterUtil;
import cn.vetech.center.usecar.setting.profit.service.CpsaUseCarProfitCacheService;
import cn.vetech.center.usecar.setting.profit.vo.CpsaProfitCacheVO;
import cn.vetech.center.usecar.setting.profit.vo.CpsaUseCarProfitCacheVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.collection.VeCollectionUtils;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.BigDecimalUtil;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 查询预订通用信息服务类
 * 通过API 接口获取CDS CUSTOMER 模块的信息
 *
 * @author houshuang
 * Created by vetech on 2017/11/03.
 */
@Service
public class BuyerBookSpeciCarCommonService {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(BuyerBookSpeciCarCommonService.class);
    /**
     * 县级市类型
     */
    private static final String COUNTY_LEVEL_CITY = "4";

    /**
     * 调用CUSTOMER接口对象
     */
    @Autowired
    private IShShbServiceClient iShShbServiceClient;

    /**
     * 调用CDS车型组服务API
     */
    @Autowired
    private IYcCxzServiceClient iYcCxzServiceClient;

    /**
     * 接口供应商查询数据对应
     */
    @Autowired
    private GySjdyService gySjdyService;

    /**
     * 控润规则查询对象
     */
    @Autowired
    private CpsaUseCarProfitCacheService cpsaUseCarProfitCacheService;

    /**
     * 用车定时任务Service
     */
    @Autowired
    private YcDsxdRwService dsxdRwService;
    /**
     * 图片地址服务
     */
    @Autowired
    private UsecarConfig usecarConfig;

    /**
     * 调用CDS城市查询API
     */
    @Autowired
    private IVeCityServiceClient iVeCityServiceClient;
    /**
     * 用车基础数据缓存
     */
    @Autowired
    private CarBaseDataCacheService carBaseDataCacheService;

    /**
     * 产品服务内容整理
     *
     * @param carProductVOs 产品信息
     */
    public void getProductFwnr(List<BookSpeciCarProductVO> carProductVOs) {
        if (CollectionUtil.isNotEmpty(carProductVOs)) {
            for (BookSpeciCarProductVO product : carProductVOs) {
                //服务内容
                List<String> strlist = new ArrayList<String>();
                if (product.getFwnr() != null) {
                    String[] fwnr = product.getFwnr().split(",");
                    for (String string : fwnr) {
                        //存入list 用于页面气泡包裹展示
                        strlist.add(string);
                    }
                }
                product.setFwnrlist(strlist);
            }
        }
    }

    /**
     * 设置产品的 供应商Log  信息
     *
     * @param bookSpeciCarVO 采购查询预订页面VO
     * @param carProductVOs  产品信息
     *                       设置供应商产品LOG 信息
     */
    public void setSellerLog(List<BookSpeciCarProductVO> carProductVOs, BookSpeciCarVO bookSpeciCarVO) {
        try {
            Map<String, List<BookSpeciCarProductVO>> carProductGroup = VeCollectionUtils.group(carProductVOs, "gysbh");
            Set<String> gysbhs = carProductGroup.keySet();
            //通过API 接口获取 查询产品供应商商户信息
            String[] gysbhsS = gysbhs.toArray(new String[]{});
            List<ShShbVO> shShbVOs = carBaseDataCacheService.getShbByIds(gysbhsS);
            if(CollectionUtils.isEmpty(shShbVOs)){
                RestResponse<List<ShShbVO>> shShbVOsRest = iShShbServiceClient.getShbByIds(gysbhsS);
                shShbVOs = shShbVOsRest.getResult();
            }
            if (CollectionUtil.isEmpty(shShbVOs)) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
            }
            Map<String, ShShbVO> shShbVOMap = toMap(shShbVOs);
            //将通过接口查到的商户信息设置产品内
            setSellerInfo(carProductGroup, shShbVOMap, bookSpeciCarVO);
        } catch (Exception e) {
            logger.error("查询供应商信息失败", e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
        }
    }

    /**
     * ASMS产品获取产品的车型组信息
     *
     * @param cpList 产品信息
     *               设置产品的车型组名称信息
     */
    public void setCxzmc(List<BookSpeciCarProductModelVO> cpList) {
        try {
            Map<String, List<BookSpeciCarProductModelVO>> carProductGroup = VeCollectionUtils.group(cpList, "cxzbh");
            Set<String> cxzbhs = carProductGroup.keySet();
            //通过API 接口获取 查询产品车型组基础信息
            String[] cxzbhS = cxzbhs.toArray(new String[]{});
            List<YcCxzVO> ycCxzVOs = carBaseDataCacheService.getYcCxzByBh(cxzbhS);
            logger.info("***车型组信息返回{}",JsonMapper.nonEmptyMapper().toJson(ycCxzVOs));
            if(CollectionUtils.isEmpty(ycCxzVOs)){
                ycCxzVOs = iYcCxzServiceClient.getYcCxzByBh(cxzbhS);
            }
            if (CollectionUtil.isEmpty(ycCxzVOs)) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_CDS_FAIL);
            }
            List<BuyerBookCxzVO> buyerBookCxzVOs = BeanMapper.mapList(ycCxzVOs, YcCxzVO.class, BuyerBookCxzVO.class);
            Map<String, BuyerBookCxzVO> buyerBookCxzVOMap = toCxzMap(buyerBookCxzVOs);
            for (BookSpeciCarProductModelVO carProductVO : cpList) {
                String cxzbh = carProductVO.getCxzbh();
                BuyerBookCxzVO cxz = buyerBookCxzVOMap.get(cxzbh);
                carProductVO.setCxzmc(cxz != null ? cxz.getCxzmc() : "");
            }
        } catch (Exception e) {
            logger.error("ASMS专快车设置车型组名称失败", e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
        }
    }

    /**
     * ASMS产品获取产品的 供应商Log  信息
     *
     * @param cpList 产品信息
     *               设置供应商产品LOG 信息
     */
    public void getSellerLog(List<BookAsmsSpecialCar> cpList) {
        try {
            Map<String, List<BookAsmsSpecialCar>> carProductGroup = VeCollectionUtils.group(cpList, "gysbh");
            Set<String> gysbhs = carProductGroup.keySet();
            //通过API 接口获取 查询产品供应商商户信息
            String[] gysbhsS = gysbhs.toArray(new String[]{});
            List<ShShbVO> shShbVOs = carBaseDataCacheService.getShbByIds(gysbhsS);
            if(CollectionUtils.isEmpty(shShbVOs)){
                RestResponse<List<ShShbVO>> shShbVOsRest = iShShbServiceClient.getShbByIds(gysbhsS);
                shShbVOs = shShbVOsRest.getResult();
            }
            if (CollectionUtil.isEmpty(shShbVOs)) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
            }
            Map<String, ShShbVO> shShbVOMap = new HashMap<String, ShShbVO>();
            for (ShShbVO shShbVO : shShbVOs) {
                shShbVOMap.put(shShbVO.getShbh(), shShbVO);
            }
            for (BookAsmsSpecialCar carProductVO : cpList) {
                String shbh = carProductVO.getGysbh();
                ShShbVO shvo = shShbVOMap.get(shbh);
                if(shvo == null){
                    logger.error("商户{}信息为空,获取的商户信息为：{}！",shbh,JsonMapper.nonEmptyMapper().toJson(shShbVOs));
                }else{
                    carProductVO.setShlogodz(shvo.getLogo());
                }
            }
        } catch (Exception e) {
            logger.error("查询供应商信息失败", e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
        }
    }

    /**
     * 将商户信息按商户编号 存入hash 表
     *
     * @param shShbVOs 商户列表
     * @return 商户信息HASH
     */
    private Map<String, ShShbVO> toMap(List<ShShbVO> shShbVOs) {
        if (CollectionUtil.isNotEmpty(shShbVOs)) {
            Map<String, ShShbVO> shShbVOMap = new HashMap<String, ShShbVO>();
            for (ShShbVO shShbVO : shShbVOs) {
                shShbVOMap.put(shShbVO.getShbh(), shShbVO);
            }
            return shShbVOMap;
        } else {
            return null;
        }
    }

    /**
     * 将通过接口查到的商户信息设置产品内
     *
     * @param bookSpeciCarVO  采购查询预订页面VO
     * @param carProductGroup 按商户编号分组的产品信息
     * @param shShbVOMap      按商户编号hash 的商户信息
     */
    private void setSellerInfo(Map<String, List<BookSpeciCarProductVO>> carProductGroup, Map<String, ShShbVO> shShbVOMap, BookSpeciCarVO bookSpeciCarVO) {
        List<String> shShbVOs = new ArrayList<String>();
        for (Map.Entry<String, List<BookSpeciCarProductVO>> entity : carProductGroup.entrySet()) {
            List<BookSpeciCarProductVO> carProductList = entity.getValue();
            String shbh = entity.getKey();
            ShShbVO shShbVO = shShbVOMap.get(shbh);
            shShbVOs.add(shShbVO.getJc());
            setSellerInfo(carProductList, shShbVO);
        }
        bookSpeciCarVO.setShShbVOs(shShbVOs);
    }

    /**
     * 设置查询新产品商户信息
     *
     * @param carProductList 查询预订单个商户的产品信息
     * @param shShbVO        商户信息
     */
    private void setSellerInfo(List<BookSpeciCarProductVO> carProductList, ShShbVO shShbVO) {
        if (CollectionUtil.isNotEmpty(carProductList) && shShbVO != null) {
            for (BookSpeciCarProductVO carProductVO : carProductList) {
                if (StringUtils.isNotBlank(shShbVO.getLogo())) {
                    carProductVO.setShlog(usecarConfig.getVefileserver() + "/" + shShbVO.getLogo());
                }
                carProductVO.setGysmc(shShbVO.getJc());
            }
        }
    }

    /**
     * 通过CDS 接口查询车型组信息
     *
     * @param bookSpeciCarVO 采购查询预订页面VO
     * @param carProductVOs  产品信息
     */
    public void getCxzJcxxLst(List<BookSpeciCarProductVO> carProductVOs, BookSpeciCarVO bookSpeciCarVO) {
        try {
            Map<String, List<BookSpeciCarProductVO>> carProductGroup = VeCollectionUtils.group(carProductVOs, "cxzbh");
            Set<String> cxzbhs = carProductGroup.keySet();
            //通过API 接口获取 查询产品车型组基础信息
            String[] cxzbhS = cxzbhs.toArray(new String[]{});
            List<YcCxzVO> ycCxzVOs = carBaseDataCacheService.getYcCxzByBh(cxzbhS);
            logger.info("车型组信息返回{}",JsonMapper.nonEmptyMapper().toJson(ycCxzVOs));
            if(CollectionUtils.isEmpty(ycCxzVOs)) {
                ycCxzVOs = iYcCxzServiceClient.getYcCxzByBh(cxzbhS);
            }
            if (CollectionUtil.isEmpty(ycCxzVOs)) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_CDS_FAIL);
            }
            List<BuyerBookCxzVO> buyerBookCxzVOs = BeanMapper.mapList(ycCxzVOs, YcCxzVO.class, BuyerBookCxzVO.class);
            Map<String, BuyerBookCxzVO> buyerBookCxzVOMap = toCxzMap(buyerBookCxzVOs);
            setCxzInfo(carProductGroup, buyerBookCxzVOMap, bookSpeciCarVO);
        } catch (Exception e) {
            logger.error("查询车型组信息失败", e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_CDS_FAIL);
        }

    }

    /**
     * 将通过接口查到的车型信息设置到查询预订页面VO
     *
     * @param bookSpeciCarVO    采购查询预订页面VO
     * @param carProductGroup   按车型编号分组的产品信息
     * @param buyerBookCxzVOMap 车型组基础数据信息
     */
    private void setCxzInfo(Map<String, List<BookSpeciCarProductVO>> carProductGroup, Map<String, BuyerBookCxzVO> buyerBookCxzVOMap,
                            BookSpeciCarVO bookSpeciCarVO) {
        List<String> cxzmcs = new ArrayList<String>();
        List<BuyerBookCxzVO> buyerBookCxzVOs = new ArrayList<BuyerBookCxzVO>();
        for (Map.Entry<String, List<BookSpeciCarProductVO>> entity : carProductGroup.entrySet()) {
            List<BookSpeciCarProductVO> carProductList = entity.getValue();
            String cxzbh = entity.getKey();
            BuyerBookCxzVO buyerBookCxzVO = buyerBookCxzVOMap.get(cxzbh);
            if (null == buyerBookCxzVO) {
                bookSpeciCarVO.setYcCxzs(cxzmcs);
                bookSpeciCarVO.setBuyerBookCxzVOs(buyerBookCxzVOs);
                return;
            }
            cxzmcs.add(buyerBookCxzVO.getCxzmc());
            /**接口产品不在提供车型组名称转换，修改为根据车型组编号从基础数据中获取车型组名称**/
            for (BookSpeciCarProductVO bookSpeciCarProductVO : carProductList) {
                bookSpeciCarProductVO.setCxzmc(buyerBookCxzVO.getCxzmc());
            }
            //产品价格排序
            Collections.sort(carProductList, (v1, v2) -> {
                int one = v1.getJsj().intValue();
                int two = v2.getJsj().intValue();
                if (one > two) {
                    return UseCarConstant.ONE;
                } else if (one == two) {
                    return UseCarConstant.ZERO;
                } else {
                    return UseCarConstant.NUM;
                }
            });
            if (StringUtils.isNotBlank(buyerBookCxzVO.getCxztp())) {
                buyerBookCxzVO.setCxztp(usecarConfig.getVefileserver() + "/" + buyerBookCxzVO.getCxztp());
            }
            buyerBookCxzVO.setCxzzdzdj(carProductList.get(0).getJsj());
            buyerBookCxzVO.setProductlist(carProductList);
            buyerBookCxzVOs.add(buyerBookCxzVO);
            if (CollectionUtils.isEmpty(buyerBookCxzVO.getYcCppVOs())) {
                continue;
            }
            for (YcCppVO ycCppVO : buyerBookCxzVO.getYcCppVOs()) {
                if (StringUtils.isNotBlank(ycCppVO.getCpptp())) {
                    ycCppVO.setCpptp(usecarConfig.getVefileserver() + "/" + ycCppVO.getCpptp());
                }
            }
        }
        //车型组价格排序
        Collections.sort(buyerBookCxzVOs, (v1, v2) -> {
            int one = v1.getSxh().intValue();
            int two = v2.getSxh().intValue();
            if (one > two) {
                return UseCarConstant.ONE;
            } else if (one == two) {
                return UseCarConstant.ZERO;
            } else {
                return UseCarConstant.NUM;
            }
        });
        bookSpeciCarVO.setYcCxzs(cxzmcs);
        bookSpeciCarVO.setBuyerBookCxzVOs(buyerBookCxzVOs);
    }

    /**
     * 将车型组信息按车型组编号 存入hash 表
     *
     * @param buyerBookCxzVOs 车型组列表
     * @return 车型信息HASH
     */
    private Map<String, BuyerBookCxzVO> toCxzMap(List<BuyerBookCxzVO> buyerBookCxzVOs) {
        if (CollectionUtil.isEmpty(buyerBookCxzVOs)) {
            return null;
        }
        Map<String, BuyerBookCxzVO> buyerBookCxzVOMap = new HashMap<String, BuyerBookCxzVO>();
        for (BuyerBookCxzVO buyerBookCxzVO : buyerBookCxzVOs) {
            buyerBookCxzVOMap.put(buyerBookCxzVO.getCxzbm(), buyerBookCxzVO);
        }
        return buyerBookCxzVOMap;
    }
    /**
     * 获取城市数据对应
     *
     * @param cityId  城市ID
     * @param supplierIds 供应商户ID
     * @param dataType  数据类型
     * @return 城市编号
     */
    public String getGysCsid(String supplierIds, String cityId, String dataType) {
        logger.info("获取城市数据对应入参,供应商户为：" + supplierIds + ",城市id为：" + cityId + ",数据类型为：" + dataType);
        String citys = carBaseDataCacheService.getSupplierCityIds(supplierIds,cityId);
        if(StringUtils.isNotBlank(citys)){
            logger.info("供应商{}城市{}对应信息返回{}",supplierIds,cityId,citys);
            return citys;
        }
        if (StringUtils.isNotBlank(supplierIds) && StringUtils.isNotBlank(cityId)) {
            List<String> supplierCityList = new ArrayList<>();
            for (String supplierId : supplierIds.split(",")) {
                String supplierCity = getSupplierCityId(cityId,dataType,supplierId);
                supplierCityList.add(supplierCity);
            }
            return StringUtils.join(supplierCityList,",");
        }
        return "";
    }

    /**
     * 获取供应商城市编号
     * @param cityId 城市id
     * @param dataType 数据类型
     * @param supplierId 供应商编号
     * @return 供应城市id
     */
    private String getSupplierCityId(String cityId, String dataType, String supplierId) {
        if (StringUtils.equals(supplierId,UsecarGysApiEnum.TJ.getShbh())) {
            return cityId;
        }
        //T3或高德不需要城市编号
        if(StringUtils.equalsAny(supplierId,UsecarGysApiEnum.T3CX.getShbh(),UsecarGysApiEnum.AMAP.getShbh())){
            return "";
        }
        GySjdy dto = new GySjdy();
        dto.setSjbh(cityId);
        dto.setSjlx(dataType);
        dto.setGyShbh(supplierId);
        logger.info("供应{}专快车城市转换入参：{}", supplierId, JsonMapper.defaultMapper().toJson(dto));
        List<GySjdy> supplierCityMappingList = gySjdyService.queryList(dto);
        logger.info("供应{}专快车城市转换回参：{}", supplierId, JsonMapper.defaultMapper().toJson(supplierCityMappingList));
        if (CollectionUtils.isNotEmpty(supplierCityMappingList) &&
                StringUtils.isNotBlank(supplierCityMappingList.get(UseCarConstant.ZERO).getGySjbh())) {
            return supplierCityMappingList.get(UseCarConstant.ZERO).getGySjbh();
        }
        /**判断城市ID是否是县级城市，如果是县级城市则取上级城市**/
        VeCityVO cityVO = null;
        try {
            RestResponse<VeCityVO> response = iVeCityServiceClient.get(cityId);
            logger.info("供应{}专快车县级市上级转换回参：{}", supplierId, JsonMapper.defaultMapper().toJson(response));
            cityVO = response.getResult();
        } catch (Exception ex) {
            logger.error("获取供应{}城市{}信息异常", supplierId, cityId, ex);
        }
        if (cityVO == null || StringUtils.isBlank(cityVO.getBy3()) || !StringUtils.equals(COUNTY_LEVEL_CITY, cityVO.getSfsh())) {
            return "";
        }
        dto.setSjbh(cityVO.getBy3());
        supplierCityMappingList = gySjdyService.queryList(dto);
        if (CollectionUtils.isNotEmpty(supplierCityMappingList) &&
                StringUtils.isNotBlank(supplierCityMappingList.get(UseCarConstant.ZERO).getGySjbh())) {
            return supplierCityMappingList.get(UseCarConstant.ZERO).getGySjbh();
        }
        return "";
    }

    /**
     * 用车专快车贴点，控润，返佣规则查询
     * @param  cgshProfit 采购商户控润
     * @param profitCacheDTO 入参对象
     * @param productZcIn    产品对内对象
     */
    public void productKrSetting(CpsaUseCarProfitCacheDTO profitCacheDTO, BookSpeciCarProductModelVO productZcIn,List<Map<String, Map<String, List<CpsaProfitCacheVO>>>> cgshProfit) {
        BigDecimal tdz = null;//贴点值
        BigDecimal krz = null;//控润值
        BigDecimal fyz = null;//返佣值
        krfyMrzSetting(productZcIn);
        profitCacheDTO.setNotLog(true);
        CpsaUseCarProfitCacheVO krVo = cpsaUseCarProfitCacheService.getCpsaUseCarProfitCacheVO(profitCacheDTO,cgshProfit);
        productZcIn.setJsj(productZcIn.getPrice());
        productZcIn.setGyJsje(productZcIn.getPrice());
        if (krVo == null) {
            return;
        }
        // 预估价都要返回控润
//        if (!StringUtils.equals(UseCarConstant.NUMTHREE, krVo.getKrgz())) {
//            return;
//        }
        if (StringUtils.equals(UseCarConstant.NUMONE, krVo.getSfygz())) {//1有控润 0无控润
            productZcIn.setKrinfo(JsonMapper.nonEmptyMapper().toJson(krVo));
            productZcIn.setPttdfs(krVo.getTdfs());//贴点方式
            productZcIn.setPtkrfs(new BigDecimal(krVo.getKrfs()));//控润方式
            productZcIn.setPtkrgz(krVo.getKrgz());//平台控润规则
            if (StringUtils.equals(UseCarConstant.NUMONE, krVo.getTdfs())) {//贴点方式,按金额
                tdz = krVo.getTdz();
                productZcIn.setPttdbl(tdz);
                productZcIn.setPttdje(tdz);
            } else if (StringUtils.equals(UseCarConstant.NUMTWO, krVo.getTdfs())) {//按比例
                tdz = productZcIn.getPrice().multiply(krVo.getTdz().multiply(new BigDecimal(UseCarConstant.BAIBL)));
                tdz = tdz.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
                productZcIn.setPttdbl(krVo.getTdz());
                productZcIn.setPttdje(tdz);
            }
            if (StringUtils.equals(UseCarConstant.NUMONE, krVo.getKrfs())) {//控润方式,按金额
                krz = krVo.getKrz();
                productZcIn.setPtkrbl(krz);
                productZcIn.setPtkrje(krz);
            } else if (StringUtils.equals(UseCarConstant.NUMTWO, krVo.getKrfs())) {//按比例
                krz = productZcIn.getPrice().multiply(krVo.getKrz().multiply(new BigDecimal(UseCarConstant.BAIBL)));
                krz = krz.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
                productZcIn.setPtkrbl(krVo.getKrz());
                productZcIn.setPtkrje(krz);
            }else if (StringUtils.equals(UseCarConstant.NUMTHREE, krVo.getKrfs())) {//按百分比+固定值
//                logger.info("平台控润金额（百分比+固定值）之前:{}" + JsonMapper.defaultMapper().toJson(productZcIn));
                // 获取固定值
                BigDecimal gdz = krVo.getGdz() == null ? BigDecimal.ZERO : krVo.getGdz();
//                logger.info("获取固定值：{}", gdz);
                krz = productZcIn.getPrice().multiply(krVo.getKrz().multiply(new BigDecimal(UseCarConstant.BAIBL)));
                krz = krz.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
                krz = krz.add(gdz);
                productZcIn.setPtkrbl(krVo.getKrz());
                productZcIn.setPtkrje(krz);
//                logger.info("平台控润金额（百分比+固定值）之后1:{}", krz);
            }
            //除价格维度的其他控润维度计算
            KrjeTdjeHolder krjeTdjeHolder = CpsaProfitFilterUtil.calcKrjeAndTdjeWithMember(krVo, profitCacheDTO, krz, tdz);
            if(StringUtils.equals(krjeTdjeHolder.getSfhyj(),"1")){
                productZcIn.setUseMemberPrice(krjeTdjeHolder.getSfhyj());
                productZcIn.setMemberDesc(krjeTdjeHolder.getHyzkms());
            }
            krz = krjeTdjeHolder.getKrje();
            tdz = krjeTdjeHolder.getTdje();
            productZcIn.setPtkrje(krz);
            productZcIn.setPttdje(tdz);
            /***采购结算金额=建议销售价-贴点值+控润值***/
            BigDecimal cgJsje = productZcIn.getPrice().subtract(tdz).add(krz);
            productZcIn.setJsj(cgJsje);
        }
        //是否有返佣 0，无；1，有
        if (StringUtils.equals(UseCarConstant.NUMONE, krVo.getSfyfy())) {
            //供应返佣方式(2:百分比1:数值 3：无返佣) 默认3
            productZcIn.setGyFyfs(krVo.getQhffyfs());
            productZcIn.setGyFybl(krVo.getQhffyds());
            productZcIn.setGyFyje(krVo.getQhffyds());
            if (StringUtils.equals(UseCarConstant.NUMTWO, krVo.getQhffyfs().toString())) {
                if (!StringUtils.equals(UsecarGysApiEnum.SQYC.getShbh(), productZcIn.getGysbh())) {
                    //按百分比返佣
                    BigDecimal gyFyje = productZcIn.getGyJsje().multiply(krVo.getQhffyds().multiply(BigDecimal.valueOf(UseCarConstant.BFB)));
                    productZcIn.setGyFyje(gyFyje);
                }
            }
            //供应前后返 1前返 2后返 0无返
            productZcIn.setGyQhf(krVo.getQhf());
            if (new BigDecimal(UseCarConstant.ONE).compareTo(krVo.getQhf()) == UseCarConstant.ZERO) {
                //如果不是首汽且不是曹操
                if (!StringUtils.equals(UsecarGysApiEnum.SQYC.getShbh(), productZcIn.getGysbh())
                        &&!StringUtils.equals(UsecarGysApiEnum.CCZC.getShbh(), productZcIn.getGysbh())) {
                    //如果是前返 算出gy_jsje gyjsje - gyfyje
                    productZcIn.setGyJsje(productZcIn.getGyJsje().subtract(productZcIn.getGyFyje())
                            .setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                } else {
                    if(StringUtils.equals(UseCarConstant.NUMTWO, krVo.getQhffyfs().toString())){
                        //如果是首汽或者曹操   报价 / (1+返佣比例)*0.01
                        BigDecimal bj = productZcIn.getGyJsje();
                        BigDecimal bl = BigDecimal.ONE.add(krVo.getQhffyds().multiply(BigDecimal.valueOf(UseCarConstant.BFB)));
                        productZcIn.setGyJsje(bj.divide(bl, UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                        productZcIn.setGyFyje(bj.subtract(productZcIn.getGyJsje()));
                    }else{
                        productZcIn.setGyJsje(productZcIn.getGyJsje().subtract(productZcIn.getGyFyje())
                                .setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                    }
                }
            }
        }
        //2020-08-02 一口价新增采购结算金额计算
        if(StringUtils.equals(productZcIn.getSfykj(),"1")) {
            BigDecimal cgJsje = productZcIn.getJsj().add(productZcIn.getPtkrje()).subtract(productZcIn.getPttdje());
            productZcIn.setJsj(cgJsje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
            //接口供应商 经过控润后导致结算价大于市场价，这时将市场价改为结算价展示
            if (UsecarGysApiEnum.checkIsApiGys(productZcIn.getGysbh())) {
                if (Double.compare(productZcIn.getJsj().doubleValue(), productZcIn.getScj().doubleValue()) > 0) {
                    productZcIn.setScj(productZcIn.getJsj());
                }
            }
        }
    }

    /**
     * 控润返佣等默认值设置
     *
     * @param productZcIn 产品VO
     */
     private void krfyMrzSetting(BookSpeciCarProductModelVO productZcIn) {
        //平台控润方式(2:百分比1:数值 3：无返佣) 默认3
        productZcIn.setPtkrfs(new BigDecimal(UseCarConstant.THREE));
        productZcIn.setPtkrbl(new BigDecimal(UseCarConstant.ZERO));
        productZcIn.setPtkrje(new BigDecimal(UseCarConstant.ZERO));
        //供应返佣方式(2:百分比1:数值 3：无返佣) 默认3
        productZcIn.setGyFyfs(new BigDecimal(UseCarConstant.THREE));
        productZcIn.setGyFybl(new BigDecimal(UseCarConstant.ZERO));
        productZcIn.setGyFyje(new BigDecimal(UseCarConstant.ZERO));
        //平台贴点方式(1.金额,2.百分比 3 无贴点)
        productZcIn.setPttdfs(UseCarConstant.NUMONE);
        productZcIn.setPttdbl(new BigDecimal(UseCarConstant.ZERO));
        productZcIn.setPttdje(new BigDecimal(UseCarConstant.ZERO));
        //供应前后返 1前返 2后返 0无返
        productZcIn.setGyQhf(new BigDecimal(UseCarConstant.ZERO));
    }

    /**
     * 判断该订单是否为滴滴30分钟以内的预约用车
     *
     * @param ycdd 用车订单实体
     * @return true 是 false 不是
     */
     public boolean dd30MinutesReservedCar(YcDd ycdd) {
        boolean flag = false;
        try {
            logger.info("订单编号：" + ycdd.getDdbh() + ",进入判断该订单是否为滴滴30分钟以内的预约用车方法");
            if (StringUtils.isNotBlank(ycdd.getGyShbh()) && StringUtils.equals(ycdd.getGyShbh(), UsecarGysApiEnum.DDYC.getShbh())) {
                if (VeDate.isDatetime(ycdd.getYcsj(), "yyyy-MM-dd HH:mm")) {//如果是预约用车
                    logger.info("订单编号：" + ycdd.getDdbh() + ",专车预约用车时间：" + ycdd.getYcsj() + "现在时间：" + VeDate.formatToStr(VeDate.getNow(), "yyyy-MM-dd HH:mm"));
                    Date ycsjDate = VeDate.formatToDate(ycdd.getYcsj(), "yyyy-MM-dd HH:mm");
                    Date nowDate = VeDate.getNow();
                    long a = UseCarConstant.NUM_30 * UseCarConstant.NUM_60 * UseCarConstant.NUM_1000;//30分钟的毫秒数
                    if ((ycsjDate.getTime() - nowDate.getTime()) < a) { //用车时间减去现在时间小于30分钟即30分钟以内的预约用车
                        logger.info("订单编号：" + ycdd.getDdbh() + ",该订单为30分钟内的预约用车");
                        flag = true;
                        //数据存入定时任务表
                        updateYcdsxdrw(ycdd);
                    }
                }
            }
        } catch (Exception e) {
            flag = false;
            logger.error("订单编号：" + ycdd.getDdbh() + ",在判断是否该订单为30分钟内的预约用车时异常", e);
        }
        logger.info("订单编号：" + ycdd.getDdbh() + ",进入判断该订单是否为滴滴30分钟以内的预约用车方法返回结果：" + flag);
        return flag;
    }

    /**
     * 更新定时下单任务表
     *
     * @param ycdd 用车订单
     * @return true or false
     */
    public boolean updateYcdsxdrw(YcDd ycdd) {
        YcDsxdRw ycdsxdrw = new YcDsxdRw();
        ycdsxdrw.setId(VeDate.getNo(UseCarConstant.SEVEN));
        ycdsxdrw.setDdbh(ycdd.getDdbh());
        Date ycsjDate = VeDate.formatToDate(ycdd.getYcsj(), "yyyy-MM-dd HH:mm");
        String yysj = VeDate.formatToStr(ycsjDate, "yyyy-MM-dd HH:mm:ss");
        ycdsxdrw.setYysj(yysj);
        ycdsxdrw.setZt(UseCarConstant.ZT_ZERO);
        ycdsxdrw.setVersion(1L);
        //ycdsxdrw.setZxDatetime(VeDate.formatToStr(VeDate.getNow(),"yyyy-MM-dd HH:mm:ss"));
        return dsxdRwService.insertDsrw(ycdsxdrw);
    }

    /**
     * 取消规则内容
     *
     * @param carcache 专快车缓存对象
     * @return 返回拼接的内容
     * @author houshuang
     * @since 2017-12-22
     */
    public String qxgzCheckStyle(BookSpeciCalCachePrice carcache) {
        String qxgs = carcache.getQxgs();//取消公式
        String yysj = carcache.getYcsj();//用车时间
        logger.info("专快车下单到CPS订单-->取消规则内容拼接：-->取消公式：【" + qxgs + "】");
        String qxgznr = "";
        try {
            /**取消规则和取消公式拼接计算**/
            if (StringUtils.isNotBlank(yysj) && StringUtils.isNotBlank(qxgs)) {
                if (yysj.length() <= UseCarConstant.TEN) {//即时用车，取系统当前时间
                    yysj = VeDate.formatToStr(new Date(), "yyyy-MM-dd HH:mm");
                }
                logger.info("专快车下单到CPS订单-->取消规则内容拼接：-->用车时间：【" + yysj + "】");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date yysjdate = sdf.parse(yysj);
                String ysxfsj = new SimpleDateFormat("MM月dd日 HH:mm").format(yysjdate);
                Calendar c = Calendar.getInstance();
                c.setTime(yysjdate);
                /**取消规则费用取值计算**/
                String minute = qxgs.split(",")[UseCarConstant.ONE].split(":")[UseCarConstant.TWO];
                String dydsjsqfy = qxgs.split(",")[UseCarConstant.ONE].split(":")[UseCarConstant.ONE];
                String ydsjhfy = qxgs.split(",")[UseCarConstant.ONE].split(":")[UseCarConstant.ZERO];
                c.add(Calendar.MINUTE, -(new Double(minute)).intValue());
                String wsxfsj = (new SimpleDateFormat("MM月dd日 HH:mm")).format(c.getTime());
                qxgznr = wsxfsj + "前可以免费取消,在" + wsxfsj + "到" + ysxfsj + "之间取消订单收取"
                        + dydsjsqfy + "，" + ysxfsj + "之后取消订单收取" + ydsjhfy;
            } else {
                if (StringUtils.isNotBlank(carcache.getGysbh()) && UsecarGysApiEnum.DDYC.getShbh().equals(carcache.getGysbh())) {
                    qxgznr = "如果您在滴滴司机接单后取消，可能产生取消手续费，费用以取消时滴滴返回的金额为准!";
                } else {
                    qxgznr = "暂无取消规则";
                }
            }
        } catch (Exception e) {
            logger.error("取消规则和取消公式拼接计算异常", e);
        }
        logger.info("专快车下单到CPS订单-->取消规则内容:【" + qxgznr + "】");
        return qxgznr;
    }
}