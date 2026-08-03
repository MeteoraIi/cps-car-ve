package cn.vetech.center.usecar.openapi.buyer.book.specialcar;

import cn.vetech.center.coupon.api.consume.vo.ApiCouponVO;
import cn.vetech.center.system.openapi.*;
import cn.vetech.center.system.openapi.annotation.OpenApiOperation;
import cn.vetech.center.usecar.analysis.report.serivce.GysAnalysisService;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCarSearchDTO;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarProductService;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookAsmsSpecialCar;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarCommonVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.CouponSceneTypeEnum;
import cn.vetech.center.usecar.common.enums.DdqdlyEnum;
import cn.vetech.center.usecar.common.enums.UsecarCarGroupEnum;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.coupon.CouponConsumeService;
import cn.vetech.center.usecar.coupon.OrderCouponCommonService;
import cn.vetech.center.usecar.coupon.dto.CouponInfo;
import cn.vetech.center.usecar.coupon.dto.MatchGiftCouponDTO;
import cn.vetech.center.usecar.coupon.dto.MatchUsableCouponDTO;
import cn.vetech.center.usecar.openapi.buyer.book.specialcar.bean.SpecialCarPriceBean;
import com.google.common.collect.Sets;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.Arith;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.TWO;
import static cn.vetech.center.usecar.common.UseCarConstant.YES;
import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * 查询专车的产品列表
 *
 * @author chenyong
 * @since 2017-11-09
 */
 @OpenApiOperation(value = "car_searchSpecialCarProductList", title = " 查询专车的产品列表")
    public class BuyerSpecialCarService implements IOpenApiService<BuyerSpecialCarRequest, BuyerSpecialCarResponse> {
    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(BuyerSpecialCarService.class);

    /**
     * 专快车产品查询sevice
     */
    @Autowired
    private BuyerBookSpeciCarProductService buyerBookSpeciCarProductService;

    @Autowired
    private GysAnalysisService gysAnalysisService;

    /**
     *  优惠券公共服务
     */
    @Autowired
    private OrderCouponCommonService couponCommonService;

    @Autowired
    private CouponConsumeService couponConsumeService;

    @Autowired
    private BuyerBookCommonService buyerBookCommonService;

    @Autowired
    private RedisCacheManage redisCacheManage;

    @Override
    public BuyerSpecialCarResponse execute(BuyerSpecialCarRequest request, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) throws OpenApiException {
        openApiLog.add("进入ASMS调用CPS专快车询价接口==>用户：" + openApiShShbDTO.getShbh());
        openApiLog.add("ASMS调用CPS专快车询价接口==>入参：" + request.toString());
        if(StringUtils.isBlank(request.getChannelId())){
            String channelIdByShbh = buyerBookCommonService.getChannelIdByShbh(openApiShShbDTO.getShbh());
            request.setChannelId(channelIdByShbh);
        }
        BuyerSpecialCarResponse responseOut = new BuyerSpecialCarResponse();
        //快速对象拷贝至出参
        Type<BuyerSpecialCarResponse> specialCarResponseType = BeanMapper.getType(BuyerSpecialCarResponse.class);
        Type<BuyerSpecialCarRequest> specialCarRequestType = BeanMapper.getType(BuyerSpecialCarRequest.class);
        responseOut = BeanMapper.map(request, specialCarRequestType, specialCarResponseType);
        List<BuyerSpecialCar> specialCars = new ArrayList<BuyerSpecialCar>();

        BookSpeciCarSearchDTO searchDTO = specialCarConvert(request);
        searchDTO.setYhbh(openApiShYhbDTO.getYhbh());
        searchDTO.setYhmc(openApiShYhbDTO.getXm());
        searchDTO.setCgShbh(openApiShShbDTO.getShbh());
        searchDTO.setCgshjc(openApiShShbDTO.getJc());
        searchDTO.setQdly("ASMS");
        searchDTO.setShxz(openApiShShbDTO.getShxz());
        searchDTO.setSourceData(JsonMapper.nonEmptyMapper().toJson(request));
        searchDTO.setBzcgs(request.getBzcgs());
        logger.info("ASMS调用CPS专快车询价接口==>调用LINK入参：" + searchDTO);
        /***查询专快产品接口***/
        BookSpeciCarCommonVO bookSpeciCarCommonVO = buyerBookSpeciCarProductService.getZcLinkCpsList(searchDTO);
        logger.info("ASMS调用CPS专快车询价接口==>调用LINK出参：" + bookSpeciCarCommonVO.toString());
        if (bookSpeciCarCommonVO != null) {
            responseOut.setDistance(bookSpeciCarCommonVO.getDistance());
            responseOut.setDuration(bookSpeciCarCommonVO.getDuration());
            responseOut.setCursorId(bookSpeciCarCommonVO.getCursorId());
            responseOut.setFinishFlag(bookSpeciCarCommonVO.getFinishFlag());
            responseOut.setCacheTimeout(bookSpeciCarCommonVO.getCacheTimeout());
            List<BookAsmsSpecialCar> bookAsmsSpecialCars = bookSpeciCarCommonVO.getBookAsmsSpecialCars();
            Set<String> gysbhs = Sets.newHashSet();
            if (CollectionUtil.isNotEmpty(bookAsmsSpecialCars)) {
                Map<String,List<ApiCouponVO>> unclaimedCoupon = null;
                Map<String,List<ApiCouponVO>> usableCoupon = null;
                if(request.getSfcxyhq()){
                    String uid = openApiShYhbDTO.getWebApiMemberDTO()!=null?openApiShYhbDTO.getWebApiMemberDTO().getMemberId():null;
                    unclaimedCoupon = couponCommonService.findUnclaimedCoupon(StringUtils.defaultIfBlank(request.getMemberId(),uid), DdqdlyEnum.getEnumByQdly(request.getQdly()).getCode(), CouponSceneTypeEnum.CREATE_ORDER.getCode(), "0");
                    usableCoupon = couponConsumeService.couponInfos(StringUtils.defaultIfBlank(request.getMemberId(),uid),searchDTO.getCgShbh(), DdqdlyEnum.getEnumByQdly(request.getQdly()).getCode(), "0");
                }
                for (BookAsmsSpecialCar bookAsmsSpecialCar : bookAsmsSpecialCars) {
                    gysbhs.add(bookAsmsSpecialCar.getGysbh());
                    //快速对象拷贝至出参
                    Type<BookAsmsSpecialCar> bookAsmsSpecialCarType = BeanMapper.getType(BookAsmsSpecialCar.class);
                    Type<BuyerSpecialCar> buyerSpecialCarType = BeanMapper.getType(BuyerSpecialCar.class);
                    BuyerSpecialCar buyerSpecialCar = BeanMapper.map(bookAsmsSpecialCar, bookAsmsSpecialCarType, buyerSpecialCarType);
                    String destinationChangeable = buyerSpecialCar.getDestinationChangeable();
                    Boolean isDd = false;
                    if (StringUtils.contains(bookAsmsSpecialCar.getGysbh(),"DDYC") || StringUtils.contains(bookAsmsSpecialCar.getGysmc(),"滴滴")) {
                        isDd = true;
                    }
                    if (isDd && StringUtils.equals("1",destinationChangeable)) {
                        buyerSpecialCar.setDestinationChangeable(request.getDdEnableModifyDestination());
                        logger.info("打印一下滴滴隐藏配置看看是否生效:{}",JsonMapper.nonEmptyMapper().toJson(buyerSpecialCar));
                    }
                    String priceDetail = savePriceDetail(bookAsmsSpecialCar);
                    buyerSpecialCar.setPriceDetail(priceDetail);
                    if (StringUtils.contains(buyerSpecialCar.getGysmc(),"出租")
                            || ArrayUtils.contains(UseCarConstant.TAXI_SUPPLIER_NAME,buyerSpecialCar.getGysmc())
                            || ArrayUtils.contains(UseCarConstant.TAXI_SUPPLIER_SHORT_NAME,buyerSpecialCar.getGysmc())) {
                        buyerSpecialCar.setCxzmc(UsecarCarGroupEnum.czc.getMessage());
                        buyerSpecialCar.setCxzbh(UsecarCarGroupEnum.czc.getCode());
                    }
                    if(StringUtils.startsWith(buyerSpecialCar.getWbcxzbh(),"gd_luxury")){
                        buyerSpecialCar.setWbcxzbh(buyerSpecialCar.getWbcxzbh().replace("gd_luxury","gd_fixedPrice"));
                        buyerSpecialCar.setWbcxzmc("特价车");
                        buyerSpecialCar.setCxzbh("BZ");
                        buyerSpecialCar.setCxzmc("标准型");
                    }
                    if(request.getSfcxyhq()){
                        //获取礼赠券
                        MatchGiftCouponDTO matchCouponDto = new MatchGiftCouponDTO();
                        matchCouponDto.setChannelSource( DdqdlyEnum.getEnumByQdly(request.getQdly()).getCode());
                        matchCouponDto.setCplx(buyerSpecialCar.getCxzbh());
                        matchCouponDto.setDdlx(request.getDdlx());
                        matchCouponDto.setCsbh(request.getSccs());
                        if(StringUtils.isNotBlank(request.getYcsj())){
                            if(StringUtils.isNotBlank(request.getYcrq())){
                                matchCouponDto.setYcsj(request.getYcrq()+" "+request.getYcsj()+":00");
                            }else{
                                matchCouponDto.setYcsj(request.getYcsj()+":00");
                            }

                        }
                        matchCouponDto.setMemberId(request.getMemberId());
                        matchCouponDto.setSceneType(CouponSceneTypeEnum.CREATE_ORDER.getCode());
                        matchCouponDto.setPrice(buyerSpecialCar.getJsj());
                        matchCouponDto.setPriceCacheId(buyerSpecialCar.getPriceCaCheId());
                        List<CouponInfo> giftCouponInfos = couponCommonService.match(matchCouponDto, unclaimedCoupon);
                        buyerSpecialCar.setGiftCouponInfos(giftCouponInfos);
                        List<CouponInfo> couponInfos = couponConsumeService.match(BeanMapper.map(matchCouponDto, MatchUsableCouponDTO.class), usableCoupon);
                        buyerSpecialCar.setCouponInfos(couponInfos);
                        if(CollectionUtils.isNotEmpty(couponInfos)){
                            CouponInfo couponInfo = couponInfos.get(0);
                            BigDecimal ygje = buyerSpecialCar.getJsj().subtract(couponInfo.getYhje());
                            buyerSpecialCar.setYgje(ygje.setScale(2, RoundingMode.HALF_UP).toPlainString());
                        }
                    }
                    specialCars.add(buyerSpecialCar);
                }
            }
            if(StringUtils.equals(request.getBzcgs(),YES)){
                responseOut.setLinkResult(bookSpeciCarCommonVO.getLinkResult());
            }
            responseOut.setRecommendGysInfos(gysAnalysisService.getRecommendGysInfo(gysbhs,request.getYcsj(),request.getSccs()));
        }
        if(StringUtils.equals(request.getQueryMemberPrice(),"1") && CollectionUtils.isNotEmpty(specialCars)){
            String s = specialCars.get(0).getPriceCaCheId().split("-")[0];
            redisCacheManage.put(UseCarConstant.QUERY_MEMBER_PRICE_FLAG,s,"1",600);
        }
        responseOut.setCplist(specialCars);
        responseOut.setStatus(OpenApiResponse.SUCCESS);
        return responseOut;
    }

    /**
     * 均摊控润和其他费用后写入费用详情
     * @param bookAsmsSpecialCar
     * @return
     */
    private String savePriceDetail(BookAsmsSpecialCar bookAsmsSpecialCar) {
        try {
            // 控润和服务费均摊
            BigDecimal ptkrje = new BigDecimal(bookAsmsSpecialCar.getPtkrje().toString());
            // 转map
            Map<String, String> map = JsonMapper.nonEmptyMapper().fromJson(bookAsmsSpecialCar.getPriceDetail(), Map.class);
            // 将map转list
            List<SpecialCarPriceBean> specialCarPriceBeans = new ArrayList<>();

            if (null != map) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    BigDecimal value = StringUtils.isBlank(entry.getKey()) ? BigDecimal.ZERO : new BigDecimal(entry.getValue());
                    SpecialCarPriceBean bean = new SpecialCarPriceBean(key, value);
                    specialCarPriceBeans.add(bean);
                }
            }

            // 最终明细
            List<SpecialCarPriceBean> nonProRata = new ArrayList<>();

            // 均摊的费用项目
            if (CollectionUtils.isNotEmpty(specialCarPriceBeans)) {
                List<SpecialCarPriceBean> mileageFareList = specialCarPriceBeans.stream().filter(e -> StringUtils.contains(e.getFyxm(), "里程")).collect(Collectors.toList());
                SpecialCarPriceBean mileagePrice = CollectionUtils.isEmpty(mileageFareList) ? null : mileageFareList.get(0);
                List<SpecialCarPriceBean> durationFareList = specialCarPriceBeans.stream().filter(e -> StringUtils.contains(e.getFyxm(), "时长")).collect(Collectors.toList());
                SpecialCarPriceBean durationPrice = CollectionUtils.isEmpty(durationFareList) ? null : durationFareList.get(0);
                BigDecimal lcf = mileagePrice == null ? BigDecimal.ZERO : mileagePrice.getFyje();
                BigDecimal scf = durationPrice == null ? BigDecimal.ZERO : durationPrice.getFyje();

                // 需要均摊出去的费用项目
                List<SpecialCarPriceBean> needProRata = specialCarPriceBeans.stream().filter(g -> StringUtils.contains(g.getFyxm(), "企业服务") || StringUtils.contains(g.getFyxm(), "信息")).collect(Collectors.toList());
                // 剩下的的费用项目
                nonProRata = specialCarPriceBeans.stream().filter(f -> !StringUtils.contains(f.getFyxm(), "里程") && !StringUtils.contains(f.getFyxm(), "时长")
                        && !StringUtils.contains(f.getFyxm(), "企业服务") && !StringUtils.contains(f.getFyxm(), "信息")
                ).collect(Collectors.toList());

                BigDecimal bigDecimal = BigDecimal.ZERO;
                if (CollectionUtils.isNotEmpty(needProRata)) {
                    for (SpecialCarPriceBean carPriceBean : needProRata) {
                        bigDecimal = Arith.add(bigDecimal, carPriceBean.getFyje() == null ? BigDecimal.ZERO : carPriceBean.getFyje());
                    }
                }
                // 需要被均摊的总金额
                bigDecimal = Arith.add(bigDecimal, ptkrje);

                if (lcf.compareTo(BigDecimal.ZERO) > 0 && scf.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal spitKr1 = Arith.div(bigDecimal, new BigDecimal(2), 2);
                    BigDecimal spitKr2 = Arith.sub(bigDecimal, spitKr1);

                    mileagePrice.setFyje(Arith.add(mileagePrice.getFyje(), spitKr1).setScale(TWO, ROUND_HALF_UP));
                    durationPrice.setFyje(Arith.add(durationPrice.getFyje(), spitKr2).setScale(TWO, ROUND_HALF_UP));
                    nonProRata.add(mileagePrice);
                    nonProRata.add(durationPrice);
                } else if (lcf.compareTo(BigDecimal.ZERO) > 0) {
                    mileagePrice.setFyje(Arith.add(mileagePrice.getFyje(), bigDecimal).setScale(TWO, ROUND_HALF_UP));
                    nonProRata.add(mileagePrice);
                } else if (scf.compareTo(BigDecimal.ZERO) > 0) {
                    durationPrice.setFyje(Arith.add(durationPrice.getFyje(), bigDecimal).setScale(TWO, ROUND_HALF_UP));
                    nonProRata.add(durationPrice);
                } else {
                    // 里程费和时长费都不存在,就将控润存入调度费
                    SpecialCarPriceBean specialCarPriceBean = new SpecialCarPriceBean("调度费", bigDecimal);
                    nonProRata.add(specialCarPriceBean);
                }
            }

            // 将nonProRata转Map

            if (CollectionUtils.isEmpty(nonProRata)) {
                return bookAsmsSpecialCar.getPriceDetail();
            } else {
                Map<String, String> detailmMap = new HashMap<>();
                nonProRata.stream().forEach(e -> detailmMap.put(e.getFyxm(), e.getFyje().toPlainString()));
                return JsonMapper.nonEmptyMapper().toJson(detailmMap);
            }
        } catch (Exception e) {
            logger.error("均摊控润异常", e);
        }
        return null;
    }

    /**
     * 专快车接口查询入参拼接
     *
     * @param request asms入参对象
     * @return 专快车link入参对象
     */
    public BookSpeciCarSearchDTO specialCarConvert(BuyerSpecialCarRequest request) {
        BookSpeciCarSearchDTO searchDTO = new BookSpeciCarSearchDTO();
        searchDTO.setCplx(request.getDdlx());
        searchDTO.setQueryAll(request.isQueryAll());
        searchDTO.setChannelId(request.getChannelId());
        searchDTO.setEndPlanStartTime(request.getEndPlanStartTime());
        searchDTO.setPaxNum(request.getPaxNum());
        searchDTO.setRideShare(request.getRideShare());
        searchDTO.setJsfwcsid(request.getSccs());
        searchDTO.setMmdCsid(request.getMdcs());
        searchDTO.setQueryMemberPrice(request.getQueryMemberPrice());
        searchDTO.setMemberId(request.getMemberId());
        //APP专快车查询参数为空时给默认值
        if (StringUtils.isBlank(request.getYcrq())) {
            request.setYcrq(VeDate.getStringDateShort());
        }

        if (StringUtils.isNotBlank(request.getYcsj())) {
            if (request.getYcsj().length() < UseCarConstant.TEN) {
                searchDTO.setYcsj(request.getYcrq() + " " + request.getYcsj());
            } else {
                searchDTO.setYcsj(request.getYcrq());
            }
            //预约用车
            searchDTO.setYdfs("2");
        } else {
            //立即用车
            searchDTO.setYdfs("1");
        }
        searchDTO.setCfrq(request.getYcrq());

        searchDTO.setJsfwsfdmc(request.getSccsMc());
        searchDTO.setJsfwsfd(request.getSccsXxdz());
        searchDTO.setCfd(request.getSccsPoi());
        searchDTO.setJsfwmddmc(request.getMdcsMc());
        searchDTO.setJsfwmdd(request.getMdcsXxdz());
        searchDTO.setMdd(request.getMdcsPoi());
        if (StringUtils.isBlank(searchDTO.getJsfwmdd())) {
            searchDTO.setJsfwmdd(request.getMddXxdz());
        }
        if (StringUtils.isBlank(searchDTO.getMdd())) {
            searchDTO.setMdd(request.getMddMc());
        }
        if (StringUtils.isBlank(searchDTO.getJsfwsfd())) {
            searchDTO.setJsfwsfd(request.getCfdXxdz());
        }
        if (StringUtils.isBlank(searchDTO.getCfd())) {
            searchDTO.setCfd(request.getCfdMc());
        }
        searchDTO.setCfdX(request.getScjd());
        searchDTO.setCfdY(request.getScwd());
        searchDTO.setMddX(request.getSdjd());
        searchDTO.setMddY(request.getSdwd());
        searchDTO.setSosoCfdX(request.getSosoCfdX());
        searchDTO.setSosoCfdY(request.getSosoCfdY());
        searchDTO.setSosoMddX(request.getSosoMddX());
        searchDTO.setSosoMddY(request.getSosoMddY());
        searchDTO.setCxrsj(request.getCxrsj());
        searchDTO.setAsyn(request.getAsyn());
        searchDTO.setCursorId(request.getCursorId());
        return searchDTO;
    }
}