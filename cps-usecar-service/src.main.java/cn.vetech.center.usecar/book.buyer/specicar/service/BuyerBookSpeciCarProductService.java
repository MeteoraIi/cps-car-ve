package cn.vetech.center.usecar.book.buyer.specicar.service;

import cn.vetech.center.car.entity.GyJkxx;
import cn.vetech.center.car.entity.GyQxgz;
import cn.vetech.center.car.entity.GySjdy;
import cn.vetech.center.car.service.GyJkxxService;
import cn.vetech.center.car.service.GySjdyService;
import cn.vetech.center.link.usecar.dto.LinkQuerySpecialPriceDTO;
import cn.vetech.center.link.usecar.vo.LinkQuerySpecialPriceVO;
import cn.vetech.center.link.usecar.vo.LinkSpecialPriceVO;
import cn.vetech.center.usecar.apiclient.linkusecar.ILinkSpecialCarServiceClient;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCarSearchDTO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookAsmsSpecialCar;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarCommonVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarProductModelVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarProductVO;
import cn.vetech.center.usecar.cache.CarBaseDataCacheService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarSfykjEnum;
import cn.vetech.center.usecar.setting.buyerfilter.dto.BuyerFilterBookDTO;
import cn.vetech.center.usecar.setting.buyerfilter.service.BuyerFilterSetService;
import cn.vetech.center.usecar.setting.citysetting.service.CityLevelsGroupService;
import cn.vetech.center.usecar.setting.profit.dto.CpsaUseCarProfitCacheDTO;
import cn.vetech.center.usecar.setting.profit.dto.MemberDiscountInfo;
import cn.vetech.center.usecar.setting.profit.service.ChannelMemberDiscountService;
import cn.vetech.center.usecar.setting.profit.service.CpsaProfitCacheService;
import cn.vetech.center.usecar.setting.profit.service.CpsaUseCarProfitCacheService;
import cn.vetech.center.usecar.setting.profit.vo.CpsaProfitCacheVO;
import com.google.common.collect.Lists;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.BigDecimalUtil;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.text.ToPinYin;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.CPS_DD_SHXZ;
import static cn.vetech.center.usecar.common.UseCarConstant.YES;
import static cn.vetech.center.usecar.common.enums.UsecarGysApiEnum.HELLO;

/**
 * 专快车采购查询预订产品服务 获取自签产品和link 产品集合
 * @author houshuang
 * @since 2017-11-03
 */
@Service
public class BuyerBookSpeciCarProductService {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(BuyerBookSpeciCarProductService.class);

    /**
     * 用车专快车接口对象
     */
    @Autowired
    private ILinkSpecialCarServiceClient iLinkSpecialCarServiceClient;

    /**
     * 专快车缓存对象
     */
    @Autowired
    private BuyerBookSpeciCarCacheService buyerBookSpeciCarCacheService;
    /**
     * 过滤，主要用来获取请求link的供应商商户id
     */
    @Autowired
    private BuyerFilterSetService filterSetService;
    /**
     * 公用取消规则对象
     */
    @Autowired
    private CarBaseDataCacheService carBaseDataCacheService;

    /**
     * 产品查询公共方法service
     */
    @Autowired
    private BuyerBookSpeciCarCommonService buyerBookSpeciCarCommonService;

    /**
     * 获取数据映射
     */
    @Autowired
    private GySjdyService gySjdyService;

    /**
     * 控润设置
     */
    @Autowired
    private ProfitSettingService profitSettingService;

    /**
     * 控润规则查询对象
     */
    @Autowired
    private CpsaUseCarProfitCacheService cpsaUseCarProfitCacheService;
    /**
     *
     */
    @Autowired
    private CpsaProfitCacheService cpsaProfitCacheService;
    /**
     * 供应商接口配置服务
     */
    @Autowired
    private GyJkxxService gyJkxxService;

    @Autowired
    private CityLevelsGroupService cityLevelsGroupService;


    @Autowired
    private ChannelMemberDiscountService memberDiscountService;

    /**
     * @Title 查询LINK中的专快车询价接口（CPS查询和ASMS查询公共方法，修改时需谨慎）
     * @param searchDTO LINK接口入参对象
     * @return 返回LINK产品集合
     * @throws Exception 调用LINK异常信息
     * @author houshuang
     * @Date 2017年11月03日
     */
     public BookSpeciCarCommonVO getZcLinkCpsList(BookSpeciCarSearchDTO searchDTO){
        BookSpeciCarCommonVO bookSpeciCarCommonVO = new BookSpeciCarCommonVO();
        try{
            //快速对象拷贝至入参
            Type<LinkQuerySpecialPriceDTO> linkQuerySpecialPriceDTOType = BeanMapper.getType(LinkQuerySpecialPriceDTO.class);
            Type<BookSpeciCarSearchDTO> bookSpeciCarSearchDTOType = BeanMapper.getType(BookSpeciCarSearchDTO.class);
            LinkQuerySpecialPriceDTO queryPrice = BeanMapper.map(searchDTO, bookSpeciCarSearchDTOType, linkQuerySpecialPriceDTOType);
            //只检索没有被过滤掉的接口供应商
            BuyerFilterBookDTO fdto=new BuyerFilterBookDTO();
            fdto.setCgShbh(searchDTO.getCgShbh());
            fdto.setCplx(searchDTO.getCplx());
            queryPrice.setFrombzcps(searchDTO.getBzcgs());
            List<String> gysIdList=filterSetService.bookingBeforeFilter(fdto);
            if (StringUtils.equals(searchDTO.getRideShare(), YES)) {
                if (gysIdList.contains(HELLO.getShbh())) {
                    gysIdList = Arrays.asList(HELLO.getShbh());
                } else {
                    gysIdList = new ArrayList<>();
                }
            } else {
                gysIdList.remove(HELLO.getShbh());
            }
            if(gysIdList.size()==0){
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_10008,"过滤掉了所有的接口供应商，不请求link了！");
            }
            logger.info("*****查询匹配到的商户编号信息为：{}",JsonMapper.nonEmptyMapper().toJson(gysIdList));
            StringBuffer sb = new StringBuffer();
            for(String shid:gysIdList){
                sb.append(",").append(shid);
            }
            queryPrice.setShid(sb.toString().substring(1));
            logger.info("--查询用车专快车产品价格初始入参--》" + queryPrice);
            specialCarConvertToLink(queryPrice,searchDTO);//link入参赋值方法
            logger.info("--查询用车专快车产品价格接口入参--》》》" + queryPrice);
            long sarteQueryTime = System.currentTimeMillis();
            RestResponse response = iLinkSpecialCarServiceClient.queryPrice(queryPrice);
            long endQueryTime = System.currentTimeMillis();
            logger.info("==#3CPS专快车请求link耗时:"+(endQueryTime-sarteQueryTime));
            LinkQuerySpecialPriceVO specialPriceVO = (LinkQuerySpecialPriceVO)response.getResult();
            List<LinkSpecialPriceVO> priceList = specialPriceVO.getPriceList();
            logger.info("--查询用车专快车产品价格接口出参--》》》" + specialPriceVO+"--请求状态--》"+response.getStatus()+"--消息描述-->"+response.getMessage());
            //原始供应商同车型过滤
            priceList = filterGysMinPrice(fdto, priceList);
            // 兴业证券过滤享道
            priceList = filterXyzq(searchDTO, priceList);
            // 长园集团过滤
            priceList = filterCykj(searchDTO, priceList);
            BigDecimal distance = specialPriceVO.getDistance() == null? new BigDecimal(UseCarConstant.NUMZERO):specialPriceVO.getDistance();//询价里程预计里程数(米)
            BigDecimal duration = specialPriceVO.getDuration() == null? new BigDecimal(UseCarConstant.NUMZERO):specialPriceVO.getDuration();//询价里程预计时长数(分钟)
            searchDTO.setDistance(distance);
            searchDTO.setDuration(duration);
            bookSpeciCarCommonVO.setDistance(distance.toString());
            bookSpeciCarCommonVO.setDuration(duration.toString());
            bookSpeciCarCommonVO.setCursorId(specialPriceVO.getCursorId());
            bookSpeciCarCommonVO.setFinishFlag(specialPriceVO.getFinishFlag());
            bookSpeciCarCommonVO.setCacheTimeout(specialPriceVO.getCacheTimeout());
            if(StringUtils.isNotBlank(searchDTO.getYdfs()) && UseCarConstant.NUMONE.equals(searchDTO.getYdfs())){
                searchDTO.setYcsj(searchDTO.getCfrq());
            }
            if (CollectionUtil.isNotEmpty(priceList)) {
                List<BookSpeciCarProductModelVO> productZcInList = new ArrayList<>();
//                List<String> corpIds = priceList.stream()
//                        .map(LinkSpecialPriceVO::getCorpId).collect(Collectors.toList());
                List<String> corpIds = priceList.stream().filter(p->p!= null && StringUtils.isNotBlank(p.getCorpId()) && StringUtils.isNotBlank(p.getWbcxzbh()))
                        .map(p->{
                            String supplierName = p.getCorpId();
                            if(StringUtils.equals(p.getGysbh(),UsecarGysApiEnum.AMAP.getShbh()) && StringUtils.isNotBlank(p.getWbcxzbh())){
                                supplierName = p.getWbcxzbh().substring(p.getWbcxzbh().lastIndexOf("_")+1);
                            }else if(StringUtils.equals(p.getGysbh(),UsecarGysApiEnum.TJ.getShbh()) && StringUtils.isNotBlank(p.getCorpId())){
                                supplierName = ToPinYin.getPinYinHeadChar(p.getGysmc()).toUpperCase();
                            }
                            return supplierName;
                        }).collect(Collectors.toList());
                logger.info("***专快车外部供应商平台编号为：{}",JsonMapper.nonEmptyMapper().toJson(corpIds));
                List<GySjdy> ptmcList = null;
                if (CollectionUtils.isNotEmpty(corpIds)) {
                    ptmcList = gySjdyService.selectPtmcList(corpIds);
                }
                Map<String,GySjdy> ptmcMap = null;
                if (CollectionUtils.isNotEmpty(ptmcList)) {
                    ptmcMap = ptmcList
                            .stream()
                            .collect(Collectors.toMap(e -> e.getGyShbh() + e.getGySjbh(),
                             Function.identity(), (key1, key2) -> key2));
                }
                logger.info("平台名称数据为：{}",JsonMapper.nonEmptyMapper().toJson(ptmcMap));
                String ifProfit = profitSettingService.getProfitSetting();
                String cplxId = searchDTO.getCplx();
                Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> groupProfit = cpsaProfitCacheService.getCpsaUseCarProfitCacheByCplx(cplxId);
                //没有匹配到直接返回空
                if (groupProfit == null) {
                    logger.error("*****未获取到用车控润信息....");
                    return null;
                }
                //按照采购商户编号过滤
                String cgshBh = searchDTO.getCgShbh();
                List<Map<String, Map<String, List<CpsaProfitCacheVO>>>> cgshProfit = cpsaUseCarProfitCacheService.filterByCgsh(groupProfit, cgshBh);
                GyJkxx gyJkxx = gyJkxxService.selectGyjkxx(UsecarGysApiEnum.XDCX.getShbh(), "1", "1");
                searchDTO.setCityLevel(cityLevelsGroupService.selectCityLevelByCityId(searchDTO.getJsfwcsid()));

                MemberDiscountInfo memberLevelDiscount = memberDiscountService.findMemberLevelDiscount(searchDTO.getChannelId(), searchDTO.getMemberId(), searchDTO.getQueryMemberPrice());
                searchDTO.setMemberDiscountInfo(memberLevelDiscount);
                for(LinkSpecialPriceVO linkSpecialPriceVO:priceList){
                    if(StringUtils.isBlank(linkSpecialPriceVO.getCxzbh())){
                        continue;
                    }
                    BookSpeciCarProductModelVO productZcIn = specialCarConvertToCps(linkSpecialPriceVO,searchDTO,ifProfit,cgshProfit);//CPS产品对象赋值方法
                    if (MapUtils.isNotEmpty(ptmcMap)) {
                        String ptmcKey = linkSpecialPriceVO.getGysbh() + linkSpecialPriceVO.getCorpId();
                        if(StringUtils.equals(linkSpecialPriceVO.getGysbh(),UsecarGysApiEnum.YXCX.getShbh())
                                && StringUtils.startsWith(linkSpecialPriceVO.getWbcxzbh(),"15")){
                            ptmcKey = linkSpecialPriceVO.getGysbh()+"15";
                        }
                        if(StringUtils.equals(linkSpecialPriceVO.getGysbh(),UsecarGysApiEnum.AMAP.getShbh()) && StringUtils.isNotBlank(linkSpecialPriceVO.getWbcxzbh())){
                            ptmcKey = linkSpecialPriceVO.getGysbh()+linkSpecialPriceVO.getWbcxzbh().substring(linkSpecialPriceVO.getWbcxzbh().lastIndexOf("_")+1);
                        }
                        GySjdy gySjdy = ptmcMap.get(ptmcKey);
                        if (gySjdy != null) {
                            if (!StringUtils.equals(gySjdy.getSjbh(), "1")) {
                                continue;
                            }
                            productZcIn.setWbgysbh(gySjdy.getGySjbh());
                            productZcIn.setWbgysmc(gySjdy.getGySjmc());
                            productZcIn.setGysmc(gySjdy.getSjmc());
                        }
                    }
                    if(Lists.newArrayList(UsecarGysApiEnum.AMAP.getShbh(),UsecarGysApiEnum.TJ.getShbh(),UsecarGysApiEnum.RQCX.getShbh(),UsecarGysApiEnum.DIBO.getShbh()).contains(linkSpecialPriceVO.getGysbh()) && StringUtils.isNotBlank(linkSpecialPriceVO.getCorpId())){
                        productZcIn.setWbgysmc(linkSpecialPriceVO.getCorpId());
                        productZcIn.setGysmc(linkSpecialPriceVO.getCorpId());
                    }
                    if(StringUtils.equals(linkSpecialPriceVO.getGysbh(),UsecarGysApiEnum.XDCX.getShbh()) && StringUtils.equals(linkSpecialPriceVO.getCxzbh(),"CZC")){
                        if(gyJkxx!=null && StringUtils.equals(gyJkxx.getEnableLogo(),YES)){
                            productZcIn.setCzcLogo(gyJkxx.getLogoUrl());
                            productZcIn.setBrandIconUrl(gyJkxx.getLogoUrl());
                            productZcIn.setGysmc("申程出租车");
                        }
                    }
                    productZcInList.add(productZcIn);
                }
                //专快车价格缓存
                if (CollectionUtil.isNotEmpty(productZcInList)) {
                    buyerBookSpeciCarCommonService.setCxzmc(productZcInList);
                    buyerBookSpeciCarCacheService.priceCaCheBc(productZcInList,specialPriceVO.getCursorId(),searchDTO.getAsyn());
                    if("CPS".equals(searchDTO.getQdly())){//CPS产品查询
                        List<BookSpeciCarProductVO> productZcOutList = new ArrayList<BookSpeciCarProductVO>();
                        for(BookSpeciCarProductModelVO productToCps:productZcInList){
                            //快速对象拷贝至输出VO
                            Type<BookSpeciCarProductModelVO> bookSpeciCarProductModelVOType = BeanMapper.getType(BookSpeciCarProductModelVO.class);
                            Type<BookSpeciCarProductVO> bookSpeciCarProductVOType = BeanMapper.getType(BookSpeciCarProductVO.class);
                            BookSpeciCarProductVO  productZcOut = BeanMapper.map(productToCps, bookSpeciCarProductModelVOType, bookSpeciCarProductVOType);
                            productZcOut.setPriceDetail(productToCps.getPriceDetail());
                            productZcOutList.add(productZcOut);
                        }
                        bookSpeciCarCommonVO.setBookSpeciCarProductVOs(productZcOutList);
                    }else if("ASMS".equals(searchDTO.getQdly())){//ASMS产品查询
                        List<BookAsmsSpecialCar> productAsmsList = new ArrayList<BookAsmsSpecialCar>();
                        for(BookSpeciCarProductModelVO productToAsms:productZcInList){
                            BookAsmsSpecialCar bookAsmsSpecialCar = new BookAsmsSpecialCar();
                            specialCarConvertToAsms(productToAsms,bookAsmsSpecialCar);
                            productAsmsList.add(bookAsmsSpecialCar);
                        }
                        /**ASMS询价接口查询供应商信息（logo）**/
                        buyerBookSpeciCarCommonService.getSellerLog(productAsmsList);
                        /**ASMS询价接口查询车型组信息（设置车型组名称）**/
                        bookSpeciCarCommonVO.setBookAsmsSpecialCars(productAsmsList);
                    }
                }
                if(StringUtils.equals(searchDTO.getBzcgs(),YES)){
                    Map<String, String> priceCacheIdMap = productZcInList.stream().collect(Collectors.toMap(BookSpeciCarProductModelVO::getJgmd5, BookSpeciCarProductModelVO::getPriceCacheId, (o1, o2) -> o1));
                    priceList.forEach(e->{
                        e.setCpsCacheId(priceCacheIdMap.get(e.getJgmd5()));
                    });
                    bookSpeciCarCommonVO.setLinkResult(JsonMapper.nonEmptyMapper().toJson(specialPriceVO));
                }
            }
            long f4 = System.currentTimeMillis();
            logger.info("==#4对数据进行后续处理耗时:"+(f4-endQueryTime));
        }catch (Exception e){
            logger.error("*****查询用车专快车LINK产品异常*****",e);
        }
        return bookSpeciCarCommonVO;
    }

    /**
     * 兴业证券过滤享道
     * @param searchDTO
     * @param priceList
     * @return
     */
    private List<LinkSpecialPriceVO> filterXyzq(BookSpeciCarSearchDTO searchDTO, List<LinkSpecialPriceVO> priceList) {
        if (CollectionUtils.isEmpty(priceList)) {
            return priceList;
        }
        priceList = priceList.stream().filter(e -> {
            String gysmc = e.getGysmc();
            String corpId = e.getCorpId();
            // 兴业证券过滤享道
            Boolean xyFilterXd = false;
            if (StringUtils.equals(searchDTO.getCgShbh(),"XYZQ") && (StringUtils.contains(gysmc,"享道") || StringUtils.contains(corpId,"享道"))) {
                xyFilterXd = true;
            }
            return !xyFilterXd;
        }).collect(Collectors.toList());
        return priceList;
    }

    /**
     * 兴业证券过滤享道
     * @param searchDTO
     * @param priceList
     * @return
     */
    private List<LinkSpecialPriceVO> filterCykj(BookSpeciCarSearchDTO searchDTO, List<LinkSpecialPriceVO> priceList) {
        if (CollectionUtils.isEmpty(priceList)) {
            return priceList;
        }
        priceList = priceList.stream().filter(e -> {
            String gysmc = e.getGysmc();
            String corpId = e.getCorpId();
            // 长园科技过滤 深圳出租和飞豹
            Boolean xyFilterXd = false;
            if (StringUtils.equals(searchDTO.getCgShbh(),"CHANGYYUAN") && (StringUtils.containsAny(gysmc,"飞豹","深圳出租") || (StringUtils.containsAny(corpId,"飞豹","深圳出租"))) ) {
                xyFilterXd = true;
            }
            return !xyFilterXd;
        }).collect(Collectors.toList());
        return priceList;
    }

    private List<LinkSpecialPriceVO> filterGysMinPrice(BuyerFilterBookDTO fdto, List<LinkSpecialPriceVO> priceList) {
        try{
            if(fdto.getBuyerFilterVO()!=null && StringUtils.equals(fdto.getBuyerFilterVO().getSfkqdjgl(),YES)){
                logger.info("开启了供应商低价过滤");
                if(CollectionUtils.isNotEmpty(priceList)){
                    Map<String, LinkSpecialPriceVO> gysPriceMap = priceList.stream().collect(Collectors.toMap(e -> e.getCorpId() + "_" + e.getCxzbh(), Function.identity(), (o1, o2) -> {
                        BigDecimal ygjg = o1.getYgje();
                        BigDecimal ygjg1 = o2.getYgje();
                        if (ygjg != null && ygjg1 != null && BigDecimalUtil.isGreaterThan(ygjg, ygjg1)) {
                            return o2;
                        }
                        if (ygjg != null && ygjg1 != null && BigDecimalUtil.isGreaterThan(ygjg1, ygjg)) {
                            return o1;
                        }
                        return o1;
                    }));
                    priceList = Lists.newArrayList(gysPriceMap.values());
                }
            }
        }catch (Exception e){
            logger.error("过滤异常",e);
        }
        return priceList;
    }

     /**
     * 专快车link产品转为ASMS产品对象
     * @param productToAsms link产品对象
     * @param bookAsmsSpecialCar ASMS产品对象
     */
    private void specialCarConvertToAsms(BookSpeciCarProductModelVO productToAsms,BookAsmsSpecialCar bookAsmsSpecialCar){
        bookAsmsSpecialCar.setBrandIconUrl(productToAsms.getBrandIconUrl());
        bookAsmsSpecialCar.setSupplierBookType(productToAsms.getSupplierBookType());
        bookAsmsSpecialCar.setGysbh(productToAsms.getGysbh());
        bookAsmsSpecialCar.setGysmc(productToAsms.getGysmc());
        bookAsmsSpecialCar.setCxzbh(productToAsms.getCxzbh());
        bookAsmsSpecialCar.setCxzmc(productToAsms.getCxzmc());
        bookAsmsSpecialCar.setWbcxzbh(productToAsms.getWbcxzbh());
        bookAsmsSpecialCar.setWbcxzmc(productToAsms.getWbcxzmc());
        bookAsmsSpecialCar.setCpms(productToAsms.getCpms());
        bookAsmsSpecialCar.setSfykj(productToAsms.getSfykj());
        bookAsmsSpecialCar.setCzcLogo(productToAsms.getCzcLogo());
        bookAsmsSpecialCar.setDestinationChangeable(productToAsms.getDestinationChangeable());
        bookAsmsSpecialCar.setModifyDestCount(productToAsms.getModifyDestCount());
        bookAsmsSpecialCar.setPriceDetail(productToAsms.getPriceDetail());
        if(productToAsms.getGyFyfs() != null){//供应返佣方式
            bookAsmsSpecialCar.setGyFyfs(productToAsms.getGyFyfs().intValue());
        }
        if(productToAsms.getGyFybl() != null){//供应返佣比例
            bookAsmsSpecialCar.setGyFybl(productToAsms.getGyFybl().doubleValue());
        }
        if(productToAsms.getGyFyje() != null){//供应返佣金额
            bookAsmsSpecialCar.setGyFyje(productToAsms.getGyFyje().doubleValue());
        }
        if(productToAsms.getPtkrfs() != null){//平台控润方式
            bookAsmsSpecialCar.setPtkrfs(productToAsms.getPtkrfs().intValue());
        }
        if(productToAsms.getPtkrbl() != null){//平台控润比例
            bookAsmsSpecialCar.setPtkrbl(productToAsms.getPtkrbl().doubleValue());
        }
        if(productToAsms.getPtkrje() != null){//平台控润金额
            bookAsmsSpecialCar.setPtkrje(productToAsms.getPtkrje().doubleValue());
        }
        if(StringUtils.isNotBlank(productToAsms.getPtkrgz())){//平台控润规则
            bookAsmsSpecialCar.setPtkrgz(productToAsms.getPtkrgz());
        }
        if(StringUtils.isNotBlank(productToAsms.getPttdfs())){//平台贴点方式
            bookAsmsSpecialCar.setPttdfs(Integer.parseInt(productToAsms.getPttdfs()));
        }
        if(productToAsms.getPttdbl() != null){//平台贴点比例
            bookAsmsSpecialCar.setPttdbl(productToAsms.getPttdbl().doubleValue());
        }
        if(productToAsms.getPttdje() != null){//平台贴点金额
            bookAsmsSpecialCar.setPttdje(productToAsms.getPttdje().doubleValue());
        }
        if(productToAsms.getGyQhf() !=null){//供应前后返
            bookAsmsSpecialCar.setGyQhf(productToAsms.getGyQhf().intValue());
        }
        if(productToAsms.getPrice() != null){
            bookAsmsSpecialCar.setYgje(productToAsms.getPrice().toString());
        }
        bookAsmsSpecialCar.setJsj(productToAsms.getJsj());
        bookAsmsSpecialCar.setJgmd5(productToAsms.getJgmd5());
        bookAsmsSpecialCar.setJjmslb(productToAsms.getJjmslb());
        bookAsmsSpecialCar.setFwnr(productToAsms.getFwnr());
        bookAsmsSpecialCar.setFwbz(productToAsms.getFwbz());
        if(productToAsms.getMfqxsx() != null){
            bookAsmsSpecialCar.setMfqxsx(productToAsms.getMfqxsx().doubleValue());
        }
        if(productToAsms.getYdsjhfy() != null){
            bookAsmsSpecialCar.setYdsjhfy(productToAsms.getYdsjhfy().doubleValue());
        }
        if(productToAsms.getDydsjsqfy() != null){
            bookAsmsSpecialCar.setDydsjsqfy(productToAsms.getDydsjsqfy().doubleValue());
        }
        bookAsmsSpecialCar.setPriceCaCheId(productToAsms.getPriceCacheId());
        bookAsmsSpecialCar.setShowYkjFlag(productToAsms.getShowYkjFlag());

        bookAsmsSpecialCar.setUseMemberPrice(productToAsms.getUseMemberPrice());
        bookAsmsSpecialCar.setMemberDesc(productToAsms.getMemberDesc());
    }

    /**
     * 将LINK产品对象转为CPS产品对象
     * @param linkSpecialPriceVO link对象
     * @param searchDTO 入参对象
     * @param ifProfit
     * @param cgshProfit
     * @return 返回缓存对象
     */
    private BookSpeciCarProductModelVO specialCarConvertToCps(LinkSpecialPriceVO linkSpecialPriceVO,
                                                              BookSpeciCarSearchDTO searchDTO, String ifProfit,List<Map<String, Map<String, List<CpsaProfitCacheVO>>>> cgshProfit){
        //快速对象拷贝(接口对象拷贝至输入VO)
        Type<LinkSpecialPriceVO> linkSpecialPriceVOType = BeanMapper.getType(LinkSpecialPriceVO.class);
        Type<BookSpeciCarProductModelVO> productZcInType = BeanMapper.getType(BookSpeciCarProductModelVO.class);
        BookSpeciCarProductModelVO  productZcIn = BeanMapper.map(linkSpecialPriceVO, linkSpecialPriceVOType, productZcInType);
        productZcIn.setBdsc(searchDTO.getDuration());//本单时长
        productZcIn.setBdlc(searchDTO.getDistance());//本单里程
        productZcIn.setPriceDetail(linkSpecialPriceVO.getPriceDetail());
        productZcIn.setPrice(linkSpecialPriceVO.getYgje().setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));//预估金额
        productZcIn.setScj(linkSpecialPriceVO.getYgje().setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));//市场价
        productZcIn.setCfdCsid(searchDTO.getJsfwcsid());//出发地城市编号
        productZcIn.setMmdCsid(searchDTO.getMmdCsid());//目的地城市编号
        productZcIn.setCfdCsmc(searchDTO.getJsfwsfdmc());//出发地城市名称
        productZcIn.setMddCsmc(searchDTO.getJsfwmddmc());//目的地城市名称
        productZcIn.setCfd(searchDTO.getCfd());//出发地poi
        productZcIn.setMdd(searchDTO.getMdd());//目的地poi
        productZcIn.setCfdX(searchDTO.getCfdX());//出发地经度
        productZcIn.setCfdY(searchDTO.getCfdY());//出发地纬度
        productZcIn.setMddX(searchDTO.getMddX());//目的地经度
        productZcIn.setMddY(searchDTO.getMddY());//目的地维度
        productZcIn.setSosoCfdX(searchDTO.getSosoCfdX());
        productZcIn.setSosoCfdY(searchDTO.getSosoCfdY());
        productZcIn.setSosoMddX(searchDTO.getSosoMddX());
        productZcIn.setSosoMddY(searchDTO.getSosoMddY());
        productZcIn.setCfdXxdz(searchDTO.getJsfwsfd());//出发地详细地址
        productZcIn.setMddXxdz(searchDTO.getJsfwmdd());//目的地详细地址
        productZcIn.setYdfs(searchDTO.getYdfs());//预订方式
        productZcIn.setYcsj(searchDTO.getYcsj());//用车时间
        productZcIn.setWbcxzbh(linkSpecialPriceVO.getWbcxzbh());//外部车型组
        productZcIn.setWbcxzmc(linkSpecialPriceVO.getWbcxzmc());//外部车型组名称

        /**获取供应商对应的取消规则实例**/
        GyQxgz dto = new GyQxgz();
        dto.setCplx(searchDTO.getCplx());
        dto.setGyShbh(productZcIn.getGysbh());
        dto.setZt(BigDecimal.ONE);//启用
        dto.setShzt(BigDecimal.ONE);//审核状态 启用
        dto.setSycxz(productZcIn.getCxzbh());
        if (!StringUtils.equals(CPS_DD_SHXZ, searchDTO.getShxz())) {
            matchCancelRule(dto,productZcIn);
        }
        if(!searchDTO.getCgShbh().equals(linkSpecialPriceVO.getGysbh()) && StringUtils.equals(ifProfit, "1")){//采购商户编号不等于供应商户编号
            // 企业用户变价预估价不控润
//            if ("102408".equals(searchDTO.getShxz()) && !StringUtils.equals(productZcIn.getSfykj(), "1")) {
//                productZcIn.setJsj(linkSpecialPriceVO.getYgje());
//                productZcIn.setGyJsje(linkSpecialPriceVO.getYgje());
//            } else {
                /***控润，贴点，返佣计算***/
                CpsaUseCarProfitCacheDTO carProfitCacheDTO = new CpsaUseCarProfitCacheDTO();
                carProfitCacheDTO.setCgshbh(searchDTO.getCgShbh());
                carProfitCacheDTO.setCplxid(searchDTO.getCplx());
                carProfitCacheDTO.setCpjeThree(linkSpecialPriceVO.getYgje());
                carProfitCacheDTO.setGyshbh(linkSpecialPriceVO.getGysbh());
                carProfitCacheDTO.setCityLevel(searchDTO.getCityLevel());
                carProfitCacheDTO.setYcsj(searchDTO.getYcsj());
                carProfitCacheDTO.setXdsj(VeDate.getNow());
                BigDecimal cj = new BigDecimal(0);
                carProfitCacheDTO.setCpjeTwo(cj);//差价(建议销售价-供应结算价)
                carProfitCacheDTO.setCpjeOne(linkSpecialPriceVO.getYgje());
                carProfitCacheDTO.setChannelId(searchDTO.getChannelId());
                if(StringUtils.equals(searchDTO.getQueryMemberPrice(),"1")){
                    carProfitCacheDTO.setMemberId(searchDTO.getMemberId());
                    carProfitCacheDTO.setQueryMemberPrice(searchDTO.getQueryMemberPrice());
                    carProfitCacheDTO.setMemberDiscountInfo(searchDTO.getMemberDiscountInfo());
                }
            carProfitCacheDTO.setYclc(productZcIn.getBdlc()!=null?productZcIn.getBdlc().divide(BigDecimal.valueOf(1000),2, RoundingMode.HALF_UP):null);
            carProfitCacheDTO.setYcsc(productZcIn.getBdsc());
                buyerBookSpeciCarCommonService.productKrSetting(carProfitCacheDTO,productZcIn,cgshProfit);
//            }
        }else{
            productZcIn.setJsj(linkSpecialPriceVO.getYgje());
            productZcIn.setGyJsje(linkSpecialPriceVO.getYgje());
        }
//2020-07-09 去掉渠道来源限定，使cps查询和下单判断保持一致
//        if("CPS".equals(searchDTO.getQdly())){
            /**变价产品需查询枚举，暂时先写死，双倍预付**/
        if(UsecarGysApiEnum.checkIsNoAbsPriceSh(productZcIn.getGysbh(), UsecarProductTypeEnum.zc.getCode())){
            productZcIn.setYfje(productZcIn.getJsj().multiply(new BigDecimal(UseCarConstant.NUMTWO)));
            productZcIn.setSfykj(UsecarSfykjEnum.NO.getCode());
        }else{
            logger.info("*****供应商{}的产品为一口价********",productZcIn.getGysbh());
            productZcIn.setYfje(productZcIn.getJsj());
            productZcIn.setSfykj(UsecarSfykjEnum.YES.getCode());
        }
        if(productZcIn.getScj().compareTo(productZcIn.getJsj())==UseCarConstant.NUM){//当市场价小于结算价时，等于结算价
            productZcIn.setScj(productZcIn.getJsj());
        }
        if(StringUtils.equals(productZcIn.getGysbh(),UsecarGysApiEnum.T3CX.getShbh()) && StringUtils.equals("5",productZcIn.getWbcxzbh())) {
            productZcIn.setSfykj(YES);
        }
        /**生成缓存ID**/
        String priceCacheId = String.valueOf(IdGenerator.getId());
        productZcIn.setPriceCacheId(priceCacheId);
        return productZcIn;
    }

    private void matchCancelRule(GyQxgz dto, BookSpeciCarProductModelVO productZcIn) {
        GyQxgz ycQxgz = carBaseDataCacheService.getOutQxgz(dto);
        logger.info("匹配到的取消规则:{}", JsonMapper.nonEmptyMapper().toJson(ycQxgz));
        if(null != ycQxgz){
            productZcIn.setFwnr(ycQxgz.getFwnr());// 服务内容
            productZcIn.setQxbz(ycQxgz.getQxbz());//取消备注
            productZcIn.setYdgz(ycQxgz.getYdgz());//预订规则
            productZcIn.setJgsm(ycQxgz.getJgsm());//价格说明
            productZcIn.setCxsm(ycQxgz.getCxsm());//车型说明
            productZcIn.setXcsm(ycQxgz.getXcsm());//行程说明
            productZcIn.setMfqxsx(ycQxgz.getMfqxsx());
            productZcIn.setYdsjhfy(ycQxgz.getYdsjhfy());
            productZcIn.setDydsjsqfy(ycQxgz.getDydsjsqfy());
            productZcIn.setFwbz(ycQxgz.getFwbz());
            //0,YDSJHFY:DYDSJSQFY:MFQXSX,0% 取消公式拼成规则
            productZcIn.setQxgs("0,"+ ycQxgz.getYdsjhfy()+"%:"+ ycQxgz.getDydsjsqfy()+"%:"+(ycQxgz.getMfqxsx()).intValue()*UseCarConstant.MINUTE+",0%");
        }
    }
    