package cn.vetech.center.usecar.coupon;

import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.coupon.api.consume.dto.CouponAfterSaleCancelDTO;
import cn.vetech.center.coupon.api.consume.dto.CouponBatchAfterSaleCancelDTO;
import cn.vetech.center.coupon.api.consume.dto.CouponBatchReceiveDTO;
import cn.vetech.center.coupon.api.consume.vo.*;
import cn.vetech.center.coupon.api.dto.CouponSearchParamDTO;
import cn.vetech.center.coupon.api.vo.CarIssueRule;
import cn.vetech.center.coupon.api.vo.CouponCommonRuleApiVO;
import cn.vetech.center.usecar.apiclient.coupon.ICouponBatchConsumeServiceClient;
import cn.vetech.center.usecar.apiclient.coupon.ICouponOrderServiceClient;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.vo.UseCarPorductModelVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.*;
import cn.vetech.center.usecar.coupon.dto.*;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.entity.usecar.OrderCoupon;
import cn.vetech.center.usecar.listener.CpsEventPublisher;
import cn.vetech.center.usecar.listener.entity.CpsEventEnum;
import cn.vetech.center.usecar.order.cpsa.vo.OrderCouponVO;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.order.YcDdBcdService;
import cn.vetech.center.usecar.service.order.YcDdService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.BigDecimalUtil;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderCouponCommonService {

    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(OrderCouponCommonService.class);

    @Autowired
    private ICouponBatchConsumeServiceClient couponConsumeClient;

    @Autowired
    private ICouponOrderServiceClient iCouponOrderServiceClient;

    @Autowired
    private OrderCouponService orderCouponService;

    @Autowired
    private UsecarCacheService usecarCacheService;

    @Autowired
    private YcDdService ycDdService;

    @Autowired
    private YcDdBcdService ycDdBcdService;

    @Autowired
    private CpsEventPublisher cpsEventPublisher;

    /**
     * 锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;

    /**
     *  领取卷码
     * @param dto
     * @return
     */

     private ClaimCouponVO claim(ClaimCouponDTO dto){
        try {
            ClaimCouponVO vo = new ClaimCouponVO();
            logger.info("领取优惠券入参={}", JsonMapper.nonEmptyMapper().toJson(dto));
            RestResponse<CouponReceiveVO> restResponse = couponConsumeClient.receiveCoupons(BeanMapper.map(dto,CouponBatchReceiveDTO.class));
            logger.info("领取优惠券出参={}", JsonMapper.nonEmptyMapper().toJson(restResponse));
            if(restResponse!=null && restResponse.getResult()!=null){
                CouponReceiveVO result = restResponse.getResult();
                List<CouponReceiveDetailVO> couponList = result.getCouponList();
                List<CouponInfo> couponInfos = couponList.stream().filter(e -> StringUtils.isNotBlank(e.getCouponCode())).map(e -> {
                    CouponInfo couponInfo = new CouponInfo();
                    couponInfo.setCouponCode(e.getCouponCode());
                    couponInfo.setActivityId(e.getActivityId());
                    return couponInfo;
                }).collect(Collectors.toList());
                vo.setSuccess(result.getSuccess());
                vo.setCouponList(couponInfos);
                return vo;
            }
        }catch (Exception e){
            logger.error("领取优惠券异常",e);
        }
        return null;
    }

    /**
     * 获取用车礼券
     * @param memberId 会员id
     * @param channelSource 渠道来源
     * @param sceneType
     * @param useCarType 0/空 用车 1代驾
     * @return
     */
    public Map<String,List<ApiCouponVO>> findUnclaimedCoupon(String memberId, String channelSource, String sceneType, String useCarType){
        try {
            CouponSearchParamDTO dto = new CouponSearchParamDTO();
            dto.setProductCode("1000");
            dto.setMemberId(memberId);
            dto.setChannelSource(channelSource);
            dto.setSceneType(sceneType);
            dto.setUseCarType(useCarType);
            logger.info("下单赠品列表入参={}",JsonMapper.nonEmptyMapper().toJson(dto));
            RestResponse<APIGetCouponVO> restResponse = iCouponOrderServiceClient.getCouponsByParam(dto);
            logger.info("下单赠品列表回参={}",JsonMapper.nonEmptyMapper().toJson(restResponse));
            if(restResponse!=null && restResponse.getResult()!=null){
                APIGetCouponVO result = restResponse.getResult();
                List<ApiCouponVO> coupons = result.getCoupons();
                if(CollectionUtils.isNotEmpty(coupons)){
                    List<ApiCouponVO> couponVOS = coupons.stream().filter(e -> {
                        if(!Lists.newArrayList(CouponLxEnum.MJQ.getCode(),CouponLxEnum.ZKQ.getCode()).contains(e.getAmountType())){
                           return false;
                        }
                        //用车只能按金额
                        if(!StringUtils.equals(e.getFullDiscountType(),"0")){
                           return false;
                        }
                        if (e.getIssueRule() != null) {
                            CarIssueRule rule = e.getCarIssueRule();
                            //是否过期
                            String expiryStartDate = VeDate.dateToStrLong(rule.getKsrq());
                            String expiryEndDate = VeDate.dateToStrLong(rule.getJsrq());
                            if (StringUtils.isNotBlank(expiryStartDate) && StringUtils.isNotBlank(expiryEndDate)) {
                                String now = VeDate.dateToStrLong(VeDate.getNow());
                                if (VeDate.after(expiryStartDate, now) || VeDate.after(now, expiryEndDate)) {
                                    return false;
                                }
                            }
                        } else {
                            return false;
                        }
                        return true;
                    }).collect(Collectors.toList());
                    Map<String, List<ApiCouponVO>> map = Maps.newHashMap();
                    if(CollectionUtils.isNotEmpty(couponVOS)){
                        List<ApiCouponVO> list = Lists.newArrayList();
                        for (ApiCouponVO apiCouponVO : couponVOS) {
                            CarIssueRule carIssueRule = apiCouponVO.getCarIssueRule();
                            List<String> cclxs = Lists.newArrayList();
                            List<String> yclxs = Lists.newArrayList();
                            if(StringUtils.isNotBlank(carIssueRule.getCclx())){
                                cclxs = Lists.newArrayList(carIssueRule.getCclx().split(","));
                            }
                            if(StringUtils.isNotBlank(carIssueRule.getYclx())){
                                yclxs = Lists.newArrayList(carIssueRule.getYclx().split(","));
                            }

                            if(CollectionUtils.isNotEmpty(yclxs)){
                                for (String yclx : yclxs) {
                                    if(CollectionUtils.isNotEmpty(cclxs)){
                                        for (String cclx :cclxs) {
                                            ApiCouponVO vo = BeanMapper.map(apiCouponVO, ApiCouponVO.class);
                                            CarIssueRule rule = BeanMapper.map(carIssueRule, CarIssueRule.class);
                                            rule.setCclx(cclx);
                                            rule.setYclx(yclx);
                                            vo.setCarIssueRule(rule);
                                            list.add(vo);
                                        }
                                    }else{
                                        ApiCouponVO vo = BeanMapper.map(apiCouponVO, ApiCouponVO.class);
                                        CarIssueRule rule = BeanMapper.map(carIssueRule, CarIssueRule.class);
                                        rule.setYclx(yclx);
                                        rule.setCclx(null);
                                        vo.setCarIssueRule(rule);
                                        list.add(vo);
                                    }
                                }
                            }else if(CollectionUtils.isNotEmpty(cclxs)){
                                for (String cclx :cclxs) {
                                    ApiCouponVO vo = BeanMapper.map(apiCouponVO, ApiCouponVO.class);
                                    CarIssueRule rule = BeanMapper.map(carIssueRule, CarIssueRule.class);
                                    rule.setYclx(null);
                                    rule.setCclx(cclx);
                                    vo.setCarIssueRule(rule);
                                    list.add(vo);
                                }
                            }else{
                                list.add(apiCouponVO);
                            }
                        }
                        return list.stream().filter(e->e.getCarIssueRule()!=null)
                                .collect(Collectors.groupingBy(e -> getKey(e.getCarIssueRule().getYclx(), e.getCarIssueRule().getCclx(),e.getActivityId())));
                    }
                }
            }
        }catch (Exception e){
            logger.error("查询优惠券异常",e);
        }
        return Maps.newHashMap();
    }

    /**
     *  匹配礼券
     * @return
     */
    public List<CouponInfo> match(MatchGiftCouponDTO matchDto,Map<String, List<ApiCouponVO>> map){
        try {
            logger.info("匹配礼券入参={}",JsonMapper.nonEmptyMapper().toJson(matchDto));
            if(map==null || map.isEmpty()){
                logger.info("礼赠优惠券为空");
                return null;
            }
            List<ApiCouponVO> list = Lists.newArrayList();
            Set<Map.Entry<String, List<ApiCouponVO>>> entries = map.entrySet();
            for (Map.Entry<String, List<ApiCouponVO>> entry : entries) {
                String key = entry.getKey();
                String[] parts = key.split("\\|");
                String keyDdlx = parts[0];
                String keyCclx = parts[1];
                boolean matchDdlx = "-".equals(keyDdlx) || keyDdlx.equals(matchDto.getDdlx());
                boolean matchCclx = "-".equals(keyCclx) || keyCclx.equals(matchDto.getCplx());
                if(matchDdlx && matchCclx){
                    list.addAll(entry.getValue());
                }
            }
            if(CollectionUtils.isNotEmpty(list)){
                List<ApiCouponVO> apiCouponVOS = list.stream().filter(e -> {
                    CarIssueRule carIssueRule = e.getCarIssueRule();
                    //判断用车时间
                    Date ksrq = carIssueRule.getKsrq();
                    Date jsrq = carIssueRule.getJsrq();
                    if (ksrq != null && jsrq != null) {
                        String ycsj = matchDto.getYcsj();
                        if(StringUtils.isBlank(ycsj)){
                            ycsj = VeDate.dateToStrLong(VeDate.getNow());
                        }
                        if (VeDate.after(VeDate.dateToStrLong(ksrq), ycsj) ||
                                VeDate.after(ycsj, VeDate.dateToStrLong(jsrq))) {
                            return false;
                        }
                    }
                    //用车城市
                    if(StringUtils.isNotBlank(carIssueRule.getSycs())){
                        //不包含在指定城市中
                        if(!Lists.newArrayList(carIssueRule.getSycs().split(",")).contains(matchDto.getCsbh())){
                            return false;
                        }
                    }
                    //判断金额
                    BigDecimal syddjes = carIssueRule.getSyddjes();
                    BigDecimal syddjez = carIssueRule.getSyddjez();
                    if (syddjes != null && syddjez != null && matchDto.getPrice() != null) {
                        BigDecimal price = matchDto.getPrice();
                        if (BigDecimalUtil.isLessThan(price, syddjes) || BigDecimalUtil.isLessThan(syddjez, price)) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(apiCouponVOS)){
                    return apiCouponVOS.stream().map(e->{
                        CouponInfo couponInfo = new CouponInfo();
                        couponInfo.setActivityId(e.getActivityId());
                        couponInfo.setActivityName(e.getActivityName());
                        couponInfo.setName(e.getCouponName());
                        couponInfo.setShopName(e.getShopName());
                        couponInfo.setCouponCode(e.getCouponCode());
                        couponInfo.setId(e.getCouponId());
                        couponInfo.setLx(e.getAmountType());
                        couponInfo.setAmount(e.getAmount());
                        couponInfo.setFullAmountDiscount(e.getFullDiscount());
                        couponInfo.setDiscountLimit(e.getZkqsx());
                        couponInfo.setMs(e.getLimitDesc());
                        CarIssueRule carIssueRule = e.getCarIssueRule();
                        couponInfo.setCpfqgzid(carIssueRule.getCpfqgzid());
                        couponInfo.setCclx(carIssueRule.getCclx());
                        couponInfo.setYclx(carIssueRule.getYclx());
                        couponInfo.setKsrq(carIssueRule.getKsrq());
                        couponInfo.setJsrq(carIssueRule.getJsrq());
                        couponInfo.setSycs(carIssueRule.getSycs());
                        couponInfo.setSyddjez(carIssueRule.getSyddjez());
                        couponInfo.setSyddjes(carIssueRule.getSyddjes());
                        couponInfo.setPriceCacheId(matchDto.getPriceCacheId());
                        return couponInfo;
                    }).collect(Collectors.toList());
                }
            }
        }catch (Exception e){
            logger.error("匹配礼赠券异常",e);
        }
        return Lists.newArrayList();
    }

    /**
     *
     * @param ddlx 订单类型
     * @param cclx 乘车类型
     * @return
     */
    private String getKey(String ddlx,String cclx,String id){
        StringJoiner sj = new StringJoiner("|");
        sj.add(StringUtils.defaultIfBlank(ddlx,"-"));
        sj.add(StringUtils.defaultIfBlank(cclx,"-"));
        sj.add(StringUtils.defaultIfBlank(id,"-"));
        return sj.toString();
    }


    /**
     *  领取优惠券，插入优惠券领取记录
     * @param dto
     */
    public void initialingClaimCoupon(CreateOrderClaimCouponDTO dto){
        try {
            logger.info("保存领取优惠券记录");
            //
            if(CollectionUtils.isEmpty(dto.getCouponInfos()) || StringUtils.equals(dto.getClyy(),"1")){
                logger.info("因公不能领取礼赠券");
                return;
            }
            Map<String, List<CouponInfo>> couponInfoMap = dto.getCouponInfos().stream().collect(Collectors.groupingBy(CouponInfo::getPriceCacheId));
            //找对订单对应的优惠券信息，并记录
            String priceCacheIds = dto.getPriceCacheIds();
            List<OrderCoupon> orderCoupons = Lists.newArrayList();
            for (String priceCacheId :priceCacheIds.split(",")) {
                List<CouponInfo> couponInfos = couponInfoMap.get(priceCacheId);
                for (CouponInfo couponInfo: couponInfos) {
                    String gysbh = dto.getGysbh();
                    String wbgysmc = dto.getWbgysmc();
                    if (StringUtils.isBlank(gysbh)){
                        if(StringUtils.equals(dto.getSfzkc(),"1")) {
                            BookSpeciCalCachePrice carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(priceCacheId);
                            gysbh = carcache.getGysbh();
                            wbgysmc = carcache.getWbgysmc();
                        }else{
                            UseCarPorductModelVO priceCaCheBean= usecarCacheService.getPriceCaCheYc(priceCacheId);
                            gysbh = priceCaCheBean.getGysbh();
                            wbgysmc = priceCaCheBean.getWbgysbh();
                        }
                    }
                    //记录优惠券领取记录
                    Date now = VeDate.getNow();
                    OrderCoupon orderCoupon = new OrderCoupon();
                    orderCoupon.setCreateTime(now);
                    orderCoupon.setUpdateTime(now);
                    orderCoupon.setDdbh(dto.getDdbh());
                    orderCoupon.setId(IdGenerator.getHexId());
                    orderCoupon.setLqsj(now);
                    orderCoupon.setLx(couponInfo.getLx());
                    orderCoupon.setMs(couponInfo.getMs());
                    orderCoupon.setRuleId(couponInfo.getCpfqgzid());
                    orderCoupon.setRuleName(couponInfo.getActivityName());
                    orderCoupon.setSn(couponInfo.getCouponCode());
                    orderCoupon.setName(couponInfo.getName());
                    orderCoupon.setShopName(couponInfo.getShopName());
                    orderCoupon.setDdlx(StringUtils.defaultIfBlank(couponInfo.getYclx(),"-"));
                    orderCoupon.setCxzbh(StringUtils.defaultIfBlank(couponInfo.getCclx(),"-"));
                    orderCoupon.setGysbh(gysbh);
                    orderCoupon.setWbgysmc(wbgysmc);
                    orderCoupon.setLqzt(CouponLqztEnum.INITIALING.getCode());
                    orderCoupon.setAmount(couponInfo.getAmount());
                    orderCoupon.setActivityId(couponInfo.getActivityId());
                    orderCoupon.setPriceCacheId(priceCacheId);
                    orderCoupon.setFullAmountDiscount(couponInfo.getFullAmountDiscount());
                    orderCoupon.setDiscountLimit(couponInfo.getDiscountLimit());
                    orderCoupons.add(orderCoupon);
                }
            }
            int count = 0;
            if(CollectionUtils.isNotEmpty(orderCoupons)){
                count = orderCouponService.insertList(orderCoupons);
            }
            logger.info("保存领取优惠券记录结束数量={}",count);
        }catch (Exception e){
            logger.error("保存优惠券领取记录异常",e);
        }
    }


    /**
     * 先用后付场景 准备领取礼赠优惠券
     * @param pddbh 主单
     */
    public void prepareClaimCoupon(String pddbh){
        String prefix = "USECAR_ORDER_GIFT_COUPON_CLAIM";
        InterProcessMutex lock = null;
        try {
            lock = zookeeperLockService.tryLock(LockType.LOCK, prefix, pddbh, UseCarConstant.NUM_1000 * 40);
            List<YcDd> ycDdList = ycDdService.selectBypDdbh(pddbh);
            if(CollectionUtils.isEmpty(ycDdList)){
                return;
            }
            if(!StringUtils.equals(ycDdList.get(0).getFfgz(),"1")){
                logger.info("先付后用支付时不能领取礼赠券");
                return;
            }
            YcDd ycDd = ycDdList.stream().filter(e -> Lists.newArrayList("YC4A", "YC4B", "YC4C").contains(e.getDdzt())).findFirst().orElse(null);
            if(ycDd==null){
                ycDd = ycDdList.get(0);
            }
            if(ycDd==null || StringUtils.equals(ycDd.getClyy(),"1")){
                logger.info("因公不能预领礼赠券");
                return;
            }
            List<OrderCoupon> exist = orderCouponService.selectByDdhbs(ycDdList.stream().map(YcDd::getDdbh).collect(Collectors.toList()));
            if(CollectionUtils.isNotEmpty(exist)){
                Optional<OrderCoupon> first = exist.stream().filter(e -> StringUtils.equalsAny(e.getLqzt(), CouponLqztEnum.PREPARE.getCode(), CouponLqztEnum.CLAIMED.getCode())).findFirst();
                if(first.isPresent()){
                    logger.info("{}重复领取{},{}",pddbh,first.get().getName(),first.get().getActivityId());
                    return;
                }
            }
            String wbgysmc = ycDd.getWbgysmc();
            String cxzbh = ycDd.getCxzbh();
            List<OrderCoupon> orderCoupons = orderCouponService.selectByDdbh(ycDd.getDdbh(), CouponLqztEnum.INITIALING,ycDd.getGyShbh());
            if(CollectionUtils.isNotEmpty(orderCoupons)){
                OrderCoupon unClaimCoupon = enableClaimOrderCoupon(ycDd, wbgysmc, cxzbh, orderCoupons);
                logger.info("预领取礼赠券ddbh={}",unClaimCoupon.getDdbh());
                orderCouponService.prepareClaimByPriceCacheId(unClaimCoupon.getDdbh(),unClaimCoupon.getPriceCacheId(),CouponLqztEnum.PREPARE);
            }
        }catch (Exception e){
            logger.error("预领券异常",e);
        }finally {
            if(lock!=null){
                zookeeperLockService.unlock(lock);
            }
        }
    }

    private OrderCoupon enableClaimOrderCoupon(YcDd ycDd, String wbgysmc, String cxzbh, List<OrderCoupon> orderCoupons) {
        orderCoupons.sort(Comparator.comparing(OrderCoupon::getActivityId));
        if(StringUtils.isNotBlank(cxzbh)){
            orderCoupons = orderCoupons.stream().filter(e -> StringUtils.equals(e.getCxzbh(),"-") || StringUtils.equals(cxzbh, e.getCxzbh())).collect(Collectors.toList());
        }

        if(StringUtils.isNotBlank(wbgysmc)){
            //寻找相同供应商的 优惠券领取记录
            orderCoupons = orderCoupons.stream().filter(e ->StringUtils.equals(wbgysmc, e.getWbgysmc())).collect(Collectors.toList());
        }

        OrderCoupon unClaimCoupon = orderCoupons.get(0);
        BigDecimal yhje = BigDecimal.valueOf(0);

        //确定最合适的礼赠券
        for (OrderCoupon coupon : orderCoupons) {
            if(StringUtils.equals(coupon.getLx(),"1")){
                BigDecimal amount = coupon.getAmount();
                if(BigDecimalUtil.isGreaterThan(amount,yhje)){
                    yhje = amount;
                    unClaimCoupon = coupon;
                }
            }
            if(StringUtils.equals(coupon.getLx(), CouponLxEnum.ZKQ.getCode())){
                BigDecimal amount = ycDd.getCgJsje().subtract(BigDecimalUtil.multiply(coupon.getAmount().divide(BigDecimal.TEN,2, RoundingMode.HALF_UP),ycDd.getCgJsje())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal discountLimit = coupon.getDiscountLimit();
                if(discountLimit!=null && BigDecimalUtil.isGreaterThanZero(discountLimit) && BigDecimalUtil.isGreaterThan(amount,discountLimit)){
                    amount = discountLimit;
                }
                if(BigDecimalUtil.isGreaterThan(amount,yhje)){
                    yhje = amount;
                    unClaimCoupon = coupon;
                }
            }
        }
        return unClaimCoupon;
    }

    /**
     *  领取操作
     * @param pddbh
     */
     public void claim(String pddbh){
        String prefix = "USECAR_ORDER_GIFT_COUPON_CLAIMING";
        InterProcessMutex lock = null;
        try {
            lock = zookeeperLockService.tryLock(LockType.LOCK, prefix, pddbh, UseCarConstant.NUM_1000 * 40);
            logger.info("领取订单={}礼赠券",pddbh);
            List<YcDd> ycDdList = ycDdService.selectBypDdbh(pddbh);
            if(CollectionUtils.isEmpty(ycDdList)){
                return;
            }
            YcDd ycDd = ycDdList.stream().filter(e -> Lists.newArrayList("YC4A", "YC4B", "YC4C").contains(e.getDdzt())).findFirst().orElse(null);
            if(ycDd==null){
                ycDd = ycDdList.get(0);
            }
            if(ycDd==null || StringUtils.equals(ycDd.getClyy(),"1")){
                logger.info("不能领礼赠券");
                return;
            }
            List<OrderCoupon> coupons = orderCouponService.selectByDdbh(ycDd.getDdbh(), CouponLqztEnum.CLAIMED, ycDd.getGyShbh());
            if(CollectionUtils.isNotEmpty(coupons)){
                logger.info("不能重复领取");
                return;
            }
            //查询要领取的优惠券
            logger.info("查询订单={}可领取礼赠券",ycDd.getDdbh());
            List<OrderCoupon> orderCoupons = orderCouponService.selectByDdbh(ycDd.getDdbh(), CouponLqztEnum.PREPARE, ycDd.getGyShbh());
            logger.info("查询订单={}可领取礼赠券回参={}",ycDd.getDdbh(),JsonMapper.nonEmptyMapper().toJson(orderCoupons));
            if(CollectionUtils.isNotEmpty(orderCoupons)){
                Set<String> aids = orderCoupons.stream().map(OrderCoupon::getActivityId).collect(Collectors.toSet());
                ClaimCouponDTO dto = new ClaimCouponDTO();
                dto.setChannelSource(ycDd.getDdqdly());
                dto.setMemberId(ycDd.getMemberId());
                dto.setOrderNo(pddbh);
                dto.setActivityIdList(Lists.newArrayList(aids));
                dto.setProductCode("1000");
                dto.setSceneType(CouponSceneTypeEnum.CREATE_ORDER.getCode());
                //领取优惠券
                ClaimCouponVO claim = claim(dto);
                List<CouponInfo> couponList = claim.getCouponList();
                if(CollectionUtils.isNotEmpty(couponList)){
                    Map<String, CouponInfo> map = couponList.stream().collect(Collectors.toMap(CouponInfo::getActivityId, Function.identity(), (o1, o2) -> o1));
                    for (OrderCoupon coupon : orderCoupons) {
                        CouponInfo couponInfo = map.get(coupon.getActivityId());
                        if(couponInfo!=null){
                            coupon.setSn(couponInfo.getCouponCode());
                            coupon.setLqzt(CouponLqztEnum.CLAIMED.getCode());
                            coupon.setLqsj(VeDate.getNow());
                            //更新es
                            cpsEventPublisher.send(CpsEventEnum.UPDATE_ORDER_ES_VO, ImmutableMap.of("ddbh",coupon.getDdbh(),"sfyzq","true","zqmc",coupon.getName(),"zqm",coupon.getSn()));
                        }else{
                            coupon.setLqzt(CouponLqztEnum.FAIL.getCode());
                        }
                    }
                    orderCouponService.updateList(orderCoupons);
                }
            }else{
                logger.info("{}未查询到可领取的优惠券记录",pddbh);
            }
        }catch (Exception e){
            logger.error("领券操作异常",e);
        }finally {
            if(lock!=null){
                zookeeperLockService.unlock(lock);
            }
        }
    }

    /**
     *  是否退礼赠券,只有全退才会将礼赠券退还
     * @param ycDdBcd 补差单
     */
    public void cancelGiftCoupon(YcDdBcd ycDdBcd){
        try {
            //检查是否全退，不是全退则不退礼赠券
            List<YcDdBcd> ycDdBcds = ycDdBcdService.selectYcDdBcdByDdbh(ycDdBcd.getDdbh());
            if(CollectionUtils.isEmpty(ycDdBcds)){
                logger.info("无补差记录");
                return;
            }
            YcDd ycDd = ycDdService.selectYcDd(ycDdBcd.getDdbh());
            if(ycDd==null){
                logger.info("原订单为空={}",ycDdBcd.getDdbh());
                return;
            }
            BigDecimal gytkzje = ycDdBcds.stream().filter(e -> StringUtils.equals(e.getSqrlx(), "2") && StringUtils.equals(e.getZt(), "6")).map(e -> e.getBcje()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            //供应全退
            if(BigDecimalUtil.equals(ycDd.getGyJsje(),gytkzje)){
                List<OrderCoupon> orderCoupons = orderCouponService.selectByDdbh(ycDdBcd.getDdbh(), CouponLqztEnum.CLAIMED, ycDd.getGyShbh());
                if(CollectionUtils.isNotEmpty(orderCoupons)){
                    List<CouponAfterSaleCancelDTO> list = orderCoupons.stream().map(e -> {
                        CouponAfterSaleCancelDTO dto = new CouponAfterSaleCancelDTO();
                        dto.setActivityId(e.getActivityId());
                        dto.setCouponCode(e.getSn());
                        return dto;
                    }).collect(Collectors.toList());
                    CouponBatchAfterSaleCancelDTO dto = new CouponBatchAfterSaleCancelDTO();
                    dto.setAllRefund(YesOrNoEnum.YES.getCode());
                    dto.setAfterSaleCancelList(list);
                    dto.setProductCode("1000");
                    dto.setRefundNo(ycDdBcd.getBcdh());
                    logger.info("取消礼赠券入参={}",JsonMapper.nonEmptyMapper().toJson(dto));
                    RestResponse<CouponAfterSaleCancelVO> restResponse = couponConsumeClient.giftCouponCancel(dto);
                    logger.info("取消礼赠券回参={}",JsonMapper.nonEmptyMapper().toJson(restResponse));
                    if(restResponse!=null && restResponse.getResult()!=null){
                        CouponAfterSaleCancelVO vo = restResponse.getResult();
                        if(StringUtils.equals(vo.getSuccess(),YesOrNoEnum.YES.getCode())){
                            orderCoupons.forEach(e->e.setLqzt(CouponLqztEnum.CANCEL.getCode()));
                            orderCouponService.updateList(orderCoupons);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("取消礼赠券异常",e);
        }
    }

    public List<OrderCouponVO> getGiftCouponList(String ddbh){
        List<OrderCoupon> coupons = orderCouponService.selectByDdhbs(Lists.newArrayList(ddbh));
        if(CollectionUtils.isNotEmpty(coupons)){
            return coupons.stream().map(e->{
                OrderCouponVO vo = new OrderCouponVO();
                vo.setLxmc("优惠券");
                vo.setLastTime(e.getUpdateTime());
                if(StringUtils.equals(CouponLqztEnum.CANCEL.getCode(),e.getLqzt())){
                    vo.setHszt("已回收");
                }
                vo.setLqzt(CouponLqztEnum.getNameByCode(e.getLqzt()));
                vo.setMs(e.getMs());
                vo.setRuleName(e.getRuleName());
                vo.setSceneMc("下单领取");
                vo.setSn(e.getSn());
                vo.setActivityId(e.getActivityId());
                vo.setName(e.getName());
                return vo;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public OrderCoupon getAfterUsecarGiftCouponInfo(YcDd ycDd){
        try {
            if(ycDd==null || !StringUtils.equals(ycDd.getClyy(),"2")){
                return null;
            }
            String wbgysmc = ycDd.getWbgysmc();
            String cxzbh = ycDd.getCxzbh();
            List<OrderCoupon> orderCoupons = orderCouponService.selectByDdbh(ycDd.getDdbh(), CouponLqztEnum.INITIALING,ycDd.getGyShbh());
            if(CollectionUtils.isNotEmpty(orderCoupons)){
                OrderCoupon unClaimCoupon = enableClaimOrderCoupon(ycDd, wbgysmc, cxzbh, orderCoupons);
                logger.info("预领取礼赠券ddbh={}",unClaimCoupon.getDdbh());
                return unClaimCoupon;
            }
        }catch (Exception e){
            logger.error("设置礼赠券异常");
        }
        return null;
    }
}