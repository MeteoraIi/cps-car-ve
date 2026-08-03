package cn.vetech.center.usecar.coupon;

import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.coupon.api.consume.dto.*;
import cn.vetech.center.coupon.api.consume.vo.*;
import cn.vetech.center.coupon.api.dto.CouponSearchParamDTO;
import cn.vetech.center.coupon.api.vo.CarUseRule;
import cn.vetech.center.coupon.api.vo.ShYhqSygzRqmxVO;
import cn.vetech.center.usecar.apiclient.coupon.ICouponBatchConsumeServiceClient;
import cn.vetech.center.usecar.apiclient.coupon.ICouponOrderServiceClient;
import cn.vetech.center.usecar.apiclient.coupon.IUsableCouponsServiceClient;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.vo.UseCarPorductModelVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.*;
import cn.vetech.center.usecar.coupon.dto.ConsumeCouponDTO;
import cn.vetech.center.usecar.coupon.dto.CouponInfo;
import cn.vetech.center.usecar.coupon.dto.FreezeOrderCouponEn;
import cn.vetech.center.usecar.coupon.dto.MatchUsableCouponDTO;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.entity.usecar.OrderCoupon;
import cn.vetech.center.usecar.entity.usecar.YcDdCoupon;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.entity.usecar.YcDdYhxx;
import cn.vetech.center.usecar.listener.CpsEventPublisher;
import cn.vetech.center.usecar.listener.entity.CpsEventEnum;
import cn.vetech.center.usecar.notice.buyer.dto.ConfirmApplyRefundBcNotifyToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.dto.ConfirmOrderInfoToBuyerAsmsDTO;
import cn.vetech.center.usecar.order.cpsa.vo.OrderYhxxVO;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.order.YcDdBcdService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.service.usecar.YcDdYhxxService;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
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
import org.vetech.core.exception.SystemException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
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
public class CouponConsumeService {

    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(CouponConsumeService.class);

    @Autowired
    private ICouponBatchConsumeServiceClient couponConsumeClient;


    @Autowired
    private ICouponOrderServiceClient iCouponOrderServiceClient;

    @Autowired
    private IUsableCouponsServiceClient usableCouponsServiceClient;

    @Autowired
    private ICouponBatchConsumeServiceClient iCouponBatchConsumeServiceClient;

    @Autowired
    private UsecarCacheService usecarCacheService;

    @Autowired
    private YcDdService ycDdService;

    @Autowired
    private YcDdMainService ycDdMainService;
    /**
     *  优惠券下单记录
     */
    @Autowired
    private YcDdCouponService ycDdCouponService;

    @Autowired
    private YcDdYhxxService ycDdYhxxService;

    @Autowired
    private ThreeOrderService threeOrderService;

    @Autowired
    private YcDdBcdService ycDdBcdService;

    @Autowired
    private CpsEventPublisher cpsEventPublisher;

    @Autowired
    private OrderCouponCommonService orderCouponCommonService;

    /**
     * 锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;

    public Map<String,List<ApiCouponVO>> couponInfos(String memberId, String cgshbh, String ddqdly, String useCarType){
        try {
            APIGetCouponDTO dto = new APIGetCouponDTO();
            dto.setMemberId(memberId);
            dto.setBuyerNo(cgshbh);
            dto.setCouponStatus(CouponStatusEnum.UNUSED.getStatus());
            dto.setDdqdly(ddqdly);
            dto.setProductNo("1000");
            dto.setUseCarType(useCarType);
            logger.info("查询可用优惠券入参={}",JsonMapper.nonEmptyMapper().toJson(dto));
            RestResponse<APIGetCouponVO> restResponse = usableCouponsServiceClient.getCoupons(dto);
            logger.info("查询可用优惠券回参={}",JsonMapper.nonEmptyMapper().toJson(restResponse));
            if(restResponse!=null && restResponse.getResult()!=null){
                APIGetCouponVO result = restResponse.getResult();
                List<ApiCouponVO> couponVOS = result.getCoupons();
                if(CollectionUtils.isNotEmpty(couponVOS)){
                    List<ApiCouponVO> list = Lists.newArrayList();
                    for (ApiCouponVO apiCouponVO : couponVOS) {
                        CarUseRule carUseRule = apiCouponVO.getCarUseRule();
                        List<String> cclxs = Lists.newArrayList();
                        List<String> yclxs = Lists.newArrayList();
                        if(StringUtils.isNotBlank(carUseRule.getCclx())){
                            cclxs = Lists.newArrayList(carUseRule.getCclx().split(","));
                        }
                        if(StringUtils.isNotBlank(carUseRule.getYclx())){
                            yclxs = Lists.newArrayList(carUseRule.getYclx().split(","));
                        }
                        if(CollectionUtils.isNotEmpty(yclxs)){
                            for (String yclx : yclxs) {
                                if(CollectionUtils.isNotEmpty(cclxs)){
                                    for (String cclx :cclxs) {
                                        ApiCouponVO vo = BeanMapper.map(apiCouponVO, ApiCouponVO.class);
                                        CarUseRule rule = BeanMapper.map(carUseRule, CarUseRule.class);
                                        rule.setCclx(cclx);
                                        rule.setYclx(yclx);
                                        vo.setCarUseRule(rule);
                                        list.add(vo);
                                    }
                                }else{
                                    ApiCouponVO vo = BeanMapper.map(apiCouponVO, ApiCouponVO.class);
                                    CarUseRule rule = BeanMapper.map(carUseRule, CarUseRule.class);
                                    rule.setYclx(yclx);
                                    rule.setCclx(null);
                                    vo.setCarUseRule(rule);
                                    list.add(vo);
                                }
                            }
                        }else if(CollectionUtils.isNotEmpty(cclxs)){
                            for (String cclx :cclxs) {
                                ApiCouponVO vo = BeanMapper.map(apiCouponVO, ApiCouponVO.class);
                                CarUseRule rule = BeanMapper.map(carUseRule, CarUseRule.class);
                                rule.setYclx(null);
                                rule.setCclx(cclx);
                                vo.setCarUseRule(rule);
                                list.add(vo);
                            }
                        }else{
                            list.add(apiCouponVO);
                        }
                    }
                     return list.stream().filter(e->e.getCarUseRule()!=null)
                            .collect(Collectors.groupingBy(e -> getKey(e.getCarUseRule().getYclx(), e.getCarUseRule().getCclx(),e.getCouponCode())));
                }
            }
        }catch (Exception e){
            logger.error("查询可用优惠券异常",e);
        }
        return Maps.newHashMap();
    }

    public List<CouponInfo> match(MatchUsableCouponDTO matchDto,Map<String,List<ApiCouponVO>> map){
        try {
            logger.info("匹配优惠券入参={}", JsonMapper.nonEmptyMapper().toJson(matchDto));
            if(map==null || map.isEmpty()){
                logger.info("优惠券为空");
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
                    CarUseRule carUseRule = e.getCarUseRule();
                    String ycsj = matchDto.getYcsj();
                    if(StringUtils.isBlank(ycsj)){
                        ycsj = VeDate.dateToStrLong(VeDate.getNow());
                    }
                    String t1 = e.getExpiryStartDate();
                    String t2 = e.getExpiryEndDate();
                    if(StringUtils.isNotBlank(t1) && StringUtils.isNotBlank(t2)){
                        //过期
                        if(VeDate.after(t1,ycsj) || VeDate.after(ycsj,t2)){
                            return false;
                        }
                    }
                    //用车城市
                    if(StringUtils.isNotBlank(carUseRule.getSycs())){
                        //不包含在指定城市中
                        if(!Lists.newArrayList(carUseRule.getSycs().split(",")).contains(matchDto.getCsbh())){
                            return false;
                        }
                    }
                    //不可用时间
                    List<ShYhqSygzRqmxVO> exceptDateList = carUseRule.getExceptDateList();
                    if(CollectionUtils.isNotEmpty(exceptDateList)){
                        for (ShYhqSygzRqmxVO shYhqSygzRqmxVO :exceptDateList) {
                            Date ksrq = shYhqSygzRqmxVO.getKsrq();
                            Date jsrq = shYhqSygzRqmxVO.getJsrq();
                            if(VeDate.after(VeDate.dateToStrLong(ksrq),ycsj) || VeDate.after(ycsj,VeDate.dateToStrLong(jsrq))){
                                continue;
                            }else{
                                return false;
                            }
                        }
                    }
                    //判断金额
                    if(e.getFullDiscount()!=null && BigDecimalUtil.isGreaterThanZero(e.getFullDiscount()) && BigDecimalUtil.isLessThan(matchDto.getPrice(),e.getFullDiscount())){
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(apiCouponVOS)){
                    List<CouponInfo> result = apiCouponVOS.stream().map(e -> {
                        CouponInfo couponInfo = new CouponInfo();
                        couponInfo.setActivityId(e.getActivityId());
                        couponInfo.setActivityName(e.getActivityName());
                        couponInfo.setName(e.getCouponName());
                        couponInfo.setShopName(e.getShopName());
                        couponInfo.setCouponCode(e.getCouponCode());
                        couponInfo.setId(e.getCouponId());
                        couponInfo.setLx(e.getAmountType());
                        couponInfo.setAmount(e.getAmount());
                        //计算优惠金额
                        if(StringUtils.equals(e.getAmountType(),CouponLxEnum.MJQ.getCode())){
                            couponInfo.setYhje(e.getAmount());
                        }
                        if (StringUtils.equals(e.getAmountType(), CouponLxEnum.ZKQ.getCode())) {
                            BigDecimal amount = matchDto.getPrice().subtract(BigDecimalUtil.multiply(e.getAmount().divide(BigDecimal.TEN,2,RoundingMode.HALF_UP), matchDto.getPrice())).setScale(2,RoundingMode.HALF_UP);
                            BigDecimal discountLimit = e.getZkqsx();
                            if (discountLimit != null && BigDecimalUtil.isGreaterThanZero(discountLimit) && BigDecimalUtil.isGreaterThan(amount, discountLimit)) {
                                amount = discountLimit;
                            }
                            couponInfo.setYhje(amount);
                        }
                        couponInfo.setMs(e.getLimitDesc());
                        couponInfo.setFullAmountDiscount(e.getFullDiscount());
                        couponInfo.setDiscountLimit(e.getZkqsx());
                        CarUseRule carUseRule = e.getCarUseRule();
                        couponInfo.setCpsygzid(carUseRule.getCpsygzid());
                        couponInfo.setYclx(carUseRule.getYclx());
                        couponInfo.setCclx(carUseRule.getCclx());
                        couponInfo.setSycs(carUseRule.getSycs());
                        couponInfo.setPriceCacheId(matchDto.getPriceCacheId());
                        return couponInfo;
                    }).collect(Collectors.toList());
                    return getMaxDiscountCoupon(matchDto, result);
                }
            }
        }catch (Exception e){
            logger.error("匹配优惠券异常",e);
        }
        return Lists.newArrayList();
    }

    /**
     *  获取最大折扣券返回
     * @param matchDto
     * @param result
     * @return
     */
    private List<CouponInfo> getMaxDiscountCoupon(MatchUsableCouponDTO matchDto, List<CouponInfo> result) {
        BigDecimal yhje = BigDecimal.ZERO;
        CouponInfo one = null;
        BigDecimal price = matchDto.getPrice();
        //取最优惠的一张券返回
        for (CouponInfo coupon : result) {
            if(StringUtils.equals(coupon.getLx(),CouponLxEnum.MJQ.getCode())){
                BigDecimal amount = coupon.getAmount();
                if(BigDecimalUtil.isGreaterThan(amount,yhje)){
                    yhje = amount;
                    one = coupon;
                }
            }
            if(StringUtils.equals(coupon.getLx(),CouponLxEnum.ZKQ.getCode())){
                BigDecimal amount = price.subtract(BigDecimalUtil.multiply(coupon.getAmount().divide(BigDecimal.TEN,2,RoundingMode.HALF_UP),price)).setScale(2,RoundingMode.HALF_UP);
                BigDecimal discountLimit = coupon.getDiscountLimit();
                if(discountLimit!=null && BigDecimalUtil.isGreaterThanZero(discountLimit) && BigDecimalUtil.isGreaterThan(amount,discountLimit)){
                    amount = discountLimit;
                }
                if(BigDecimalUtil.isGreaterThan(amount,yhje)){
                    yhje = amount;
                    one = coupon;
                }
            }
        }
        if(one==null){
            one = result.get(0);
        }
        return Lists.newArrayList(one);
    }


    /**
     *
     * @param ddlx 订单类型
     * @param cclx 乘车类型
     * @return
     */
    private String getKey(String ddlx,String cclx,String code){
        StringJoiner sj = new StringJoiner("|");
        sj.add(StringUtils.defaultIfBlank(ddlx,"-"));
        sj.add(StringUtils.defaultIfBlank(cclx,"-"));
        sj.add(StringUtils.defaultIfBlank(code,"-"));
        return sj.toString();
    }


    /**
     *  冻结优惠券
     * @param req
     * @return
     */
    private boolean doFreezeCoupon(FreezeOrderCouponEn req){
        try {
            CouponBatchFreezeDTO freezeDto = new CouponBatchFreezeDTO();
            List<CouponFreezeDTO> collect = req.getInfos().stream().map(e -> {
                CouponFreezeDTO dto = new CouponFreezeDTO();
                dto.setCouponCode(e.getCouponCode());
                dto.setCouponAmount(e.getDiscountAmount());
                dto.setTotalAmount(e.getPrice());
                dto.setTotalTimes(1);
                dto.setConsumerType("0");
                GoodsDTO goodsDTO = new GoodsDTO();
                goodsDTO.setCount(1);
                goodsDTO.setAmount(e.getPrice());
                goodsDTO.setCategory("");
                goodsDTO.setShopId("");
                goodsDTO.setSkuId("");
                goodsDTO.setSpuId("");
                goodsDTO.setStartCount(1);
                dto.setGoodsList(Lists.newArrayList(goodsDTO));
                return dto;
            }).collect(Collectors.toList());
            freezeDto.setFreezeList(collect);
            freezeDto.setProductCode("1000");
            freezeDto.setOrderNo(req.getDdbh());
            freezeDto.setConsumerType("0");
            freezeDto.setMemberId(req.getMemberId());
            freezeDto.setOrderType("10001");
            logger.info("冻结优惠券入参={}",JsonMapper.nonEmptyMapper().toJson(freezeDto));
            RestResponse<CouponFreezeVO> freeze = couponConsumeClient.freeze(freezeDto);
            logger.info("冻结优惠券回参={}",JsonMapper.nonEmptyMapper().toJson(freeze));
            if(freeze!=null){
                return StringUtils.equals(freeze.getResult().getSuccess(), YesOrNoEnum.YES.getCode());
            }
        }catch (Exception e){
            logger.error("冻结优惠券异常",e);
        }
        return false;
    }

    /**
     * 初始化用券记录
     * @return
     */
    public void initConsumeCoupon(ConsumeCouponDTO dto){
        try {
            logger.info("优惠券使用记录");
            if(CollectionUtils.isEmpty(dto.getCouponInfos()) || StringUtils.equals(dto.getClyy(),"1")){
                logger.info("因公不能使用优惠券");
                return;
            }
            Map<String, List<CouponInfo>> couponInfoMap = dto.getCouponInfos().stream().collect(Collectors.groupingBy(CouponInfo::getPriceCacheId));
            //找对订单对应的优惠券信息，并记录
            String priceCacheIds = dto.getPriceCacheIds();
            List<YcDdCoupon> ycDdCoupons = Lists.newArrayList();
            for (String priceCacheId : priceCacheIds.split(",")) {
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
                    YcDdCoupon ycDdCoupon = new YcDdCoupon();
                    ycDdCoupon.setCreateTime(now);
                    ycDdCoupon.setDdbh(dto.getDdbh());
                    ycDdCoupon.setId(IdGenerator.getHexId());
                    ycDdCoupon.setLx(couponInfo.getLx());
                    ycDdCoupon.setRuleId(couponInfo.getCpfqgzid());
                    ycDdCoupon.setCouponCode(couponInfo.getCouponCode());
                    ycDdCoupon.setCouponName(couponInfo.getName());
                    ycDdCoupon.setDdlx(StringUtils.defaultIfBlank(couponInfo.getYclx(),"-"));
                    ycDdCoupon.setCxzbh(StringUtils.defaultIfBlank(couponInfo.getCclx(),"-"));
                    ycDdCoupon.setGysbh(gysbh);
                    ycDdCoupon.setWbgysmc(wbgysmc);
                    ycDdCoupon.setCouponStatus(CouponConsumeStatusEnum.INIT.getCode());
                    ycDdCoupon.setAmount(couponInfo.getAmount());
                    ycDdCoupon.setActivityId(couponInfo.getActivityId());
                    ycDdCoupon.setPriceCacheId(priceCacheId);
                    ycDdCoupon.setFullAmountDiscount(couponInfo.getFullAmountDiscount());
                    ycDdCoupon.setDiscountLimit(couponInfo.getDiscountLimit());
                    ycDdCoupons.add(ycDdCoupon);
                }
            }
            int count = 0;
            if(CollectionUtils.isNotEmpty(ycDdCoupons)){
                count = ycDdCouponService.insertList(ycDdCoupons);
            }
            logger.info("保存消费优惠券记录结束数量={}",count);
        }catch (Exception e){
            logger.error("消费优惠券失败step-1",e);
        }
    }

    /**
     *  下单完成后冻结优惠券
     * @param pddbh
     */
    public void freezeCoupon(String pddbh){
        try {
            logger.info("冻结主单={}使用的优惠卷",pddbh);
            YcDdMain ycDdMain = ycDdMainService.selectById(pddbh);

            if(ycDdMain==null || StringUtils.equals(ycDdMain.getClyy(),"1")){
                logger.info("不能冻结优惠券");
                return;
            }

            List<YcDd> ycDds = ycDdService.selectAllByPDdbh(pddbh);
            //冻结优惠券
            if(CollectionUtils.isEmpty(ycDds)){
                logger.info("不存在子单");
                return;
            }
            if(!StringUtils.equals(ycDds.get(0).getFfgz(),"1")){
                logger.info("先付后用支付时不能使用优惠券");
                return;
            }

            Set<String> ddbhs = ycDds.stream().map(YcDd::getDdbh).collect(Collectors.toSet());
            List<YcDdCoupon> ycDdCoupons = ycDdCouponService.selectByDdbhs(ddbhs);
            if(CollectionUtils.isEmpty(ycDdCoupons)){
                logger.info("{}未使用优惠券",pddbh);
                return;
            }
            Map<String, List<YcDdCoupon>> map = ycDdCoupons.stream().collect(Collectors.groupingBy(YcDdCoupon::getCouponCode));
            map.forEach((k,v)->{
                FreezeOrderCouponEn en = new FreezeOrderCouponEn();
                en.setDdbh(v.stream().map(YcDdCoupon::getDdbh).collect(Collectors.joining(",")));
                YcDdCoupon ycDdCoupon = v.get(0);
                FreezeOrderCouponEn.FreezeCouponInfo info = new FreezeOrderCouponEn.FreezeCouponInfo();
                info.setCouponCode(k);

                BigDecimal cgJsje = ycDds.get(0).getCgJsje();
                info.setPrice(cgJsje);
                if(StringUtils.equals(ycDdCoupon.getLx(), CouponLxEnum.MJQ.getCode())){
                    info.setDiscountAmount(ycDdCoupon.getAmount());
                }
                if(StringUtils.equals(ycDdCoupon.getLx(),CouponLxEnum.ZKQ.getCode())){
                    info.setDiscountAmount(cgJsje.subtract(BigDecimalUtil.multiply(cgJsje,ycDdCoupon.getAmount().divide(BigDecimal.TEN,2,RoundingMode.HALF_UP))).setScale(2,RoundingMode.HALF_UP));
                }
                en.setInfos(Lists.newArrayList(info));
                en.setMemberId(ycDdMain.getMemberId());
                //冻结优惠券
                boolean b = doFreezeCoupon(en);
                if(b){
                    v.forEach(e->{
                                e.setCouponStatus(CouponConsumeStatusEnum.FREEZING.getCode());
                            }
                    );
                    ycDdCouponService.updateList(v);
                }else{
                    v.forEach(e->{
                                e.setCouponStatus(CouponConsumeStatusEnum.FREEZE_FAIL.getCode());
                            }
                    );
                    ycDdCouponService.updateList(v);
                }
            });
            YcDdMain updateYcDdMain = new YcDdMain();
            updateYcDdMain.setpDdbh(pddbh);
            updateYcDdMain.setYhlx(YhlxEnum.COUPON.getCode());
            ycDdMainService.update(updateYcDdMain);
        }catch (Exception e){
            logger.error("冻结订单使用的优惠券失败={}",pddbh);
        }

    }

    public void afterUsecarYhxx(YcDd ycDd){
        logger.info("{}用车完成后生成优惠信息",ycDd.getDdbh());
        try {
            if(!StringUtils.equals(ycDd.getClyy(),"2")){
                logger.info("{}不是因私用车",ycDd.getDdbh());
                return;
            }

            List<YcDdCoupon> ycDdCoupons = ycDdCouponService.selectByDdbh(ycDd.getDdbh(), CouponConsumeStatusEnum.FREEZING.getCode());
            if(CollectionUtils.isEmpty(ycDdCoupons)){
                logger.info("{}无优惠券信息",ycDd.getDdbh());
                return;
            }
            YcDdCoupon coupon = getYhjeMaxCoupon(ycDd, ycDd.getWbgysmc(), ycDd.getCxzbh(), ycDdCoupons);
            String useType = StringUtils.equals(ycDd.getDdlx(), UsecarProductTypeEnum.DJ.getCode()) ? "1" : "0";
            //获取优惠券信息
            ApiCouponVO couponDetail = getCouponDetail(coupon.getActivityId(), ycDd.getMemberId(), ycDd.getDdqdly(),useType);
            if(couponDetail==null){
                logger.error("优惠券{}不存在",coupon.getCouponCode());
                return;
            }

            if(!StringUtils.equalsAny(couponDetail.getAmountType(),CouponLxEnum.MJQ.getCode(),CouponLxEnum.ZKQ.getCode())){
                logger.error("优惠券类型不对type={}，用车目前只能使用折扣和满减券",couponDetail.getAmountType());
                return;
            }
            //检查是否已经存在
            List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectCouponYhxxByPddbh(ycDd.getpDdbh());
            if(CollectionUtils.isNotEmpty(ycDdYhxxes)){
                logger.info("{}已经存在优惠券优惠信息，{}",ycDd.getDdbh(),ycDd.getpDdbh());
                return;
            }
            String t1 = couponDetail.getExpiryStartDate();
            String t2 = couponDetail.getExpiryEndDate();
            String now = VeDate.dateToStrLong(VeDate.getNow());
            if(StringUtils.isNotBlank(t1) && StringUtils.isNotBlank(t2)){
                if(VeDate.after(t1,now) || VeDate.after(now,t2)){
                    logger.error("优惠券={}已过期",couponDetail.getCouponCode());
                    return ;
                }
            }

            List<YcDd> ycDdList = ycDdService.selectBypDdbh(ycDd.getpDdbh());
            //记录优惠优惠信息
            YcDdYhxx ycDdYhxx =new YcDdYhxx();
            ycDdYhxx.setDdyhid(IdGenerator.getHexId());
            ycDdYhxx.setDdbh(coupon.getDdbh());
            ycDdYhxx.setpDdbh(ycDd.getpDdbh());
            ycDdYhxx.setYhlx(YhlxEnum.COUPON.getCode());
            ycDdYhxx.setYhqzt(CouponConsumeStatusEnum.CONSUMING.getCode());
            ycDdYhxx.setYhqm(coupon.getCouponCode());
            ycDdYhxx.setZhgxsj(VeDate.getNow());
            ycDdYhxx.setWelfareActivityId(couponDetail.getActivityId());
            ycDdYhxx.setWelfareActivityName(couponDetail.getActivityName());
            BigDecimal cgjsje  = BigDecimal.valueOf(threeOrderService.getTotalPrice(ycDdList));;
            if(StringUtils.equals(couponDetail.getAmountType(),CouponLxEnum.MJQ.getCode())){
                if(BigDecimalUtil.isGreaterThanOrEqual(couponDetail.getAmount(),cgjsje)){
                    ycDdYhxx.setDkje(cgjsje);
                }else{
                    ycDdYhxx.setDkje(couponDetail.getAmount());
                }
            }
            if(StringUtils.equals(couponDetail.getAmountType(),CouponLxEnum.ZKQ.getCode())){
                BigDecimal amount = cgjsje.subtract(BigDecimalUtil.multiply(couponDetail.getAmount().divide(BigDecimal.TEN,2,RoundingMode.HALF_UP),cgjsje)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal discountLimit = couponDetail.getZkqsx();
                if (discountLimit != null && BigDecimalUtil.isGreaterThanZero(discountLimit) && BigDecimalUtil.isGreaterThan(amount, discountLimit)) {
                    amount = discountLimit;
                }
                ycDdYhxx.setDkje(amount);
            }
            boolean insert = ycDdYhxxService.insert(ycDdYhxx);
            if(insert){
                logger.info("准备消费优惠券ddbh={}",coupon.getDdbh());
                //目前只能消费一个优惠券
                coupon.setCouponStatus(CouponConsumeStatusEnum.CONSUMING.getCode());
                ycDdCouponService.update(coupon);
            }
        }catch (Exception e){
            logger.error("用车完成后生成优惠信息异常",e);
        }
    }

    /**
     *  先用后付场景，准备消费优惠券
     * @param pddbh
     */
    public void prepareConsumeCoupon(String pddbh) {
        logger.info("准备使用优惠券ywdh={}",pddbh);
        InterProcessMutex lock = null;
        try {
            String prefix = "USECAR_ORDER_GIFT_COUPON_CONSUME";
            lock = zookeeperLockService.tryLock(LockType.LOCK, prefix, pddbh, UseCarConstant.NUM_1000 * 40);
            YcDdMain ycDdMain = ycDdMainService.selectById(pddbh);
            if(ycDdMain==null || StringUtils.equals(ycDdMain.getClyy(),"1")){
                logger.info("因公不能消费优惠券");
                return;
            }
            List<YcDd> ycDdList = ycDdService.selectBypDdbh(pddbh);
            if (CollectionUtils.isEmpty(ycDdList)) {
                logger.info("子单为空");
                return;
            }
            if (!StringUtils.equals(ycDdList.get(0).getFfgz(), "1")) {
                logger.info("先付后用支付时不能消费券");
                return;
            }
            List<YcDd> ycDds = ycDdList.stream().filter(e -> Lists.newArrayList("YC2M", "YC4C").contains(e.getDdzt())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ycDds)) {
                logger.error("无可支付订单pddbh={}", pddbh);
                return;
            }
            List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectCouponYhxxByPddbh(pddbh);
            if(CollectionUtils.isEmpty(ycDdYhxxes)){
                logger.info("{}优惠券优惠信息为空",pddbh);
                return;
            }
            if(CollectionUtils.isNotEmpty(ycDdYhxxes)){
                Optional<YcDdYhxx> first = ycDdYhxxes.stream().filter(e -> StringUtils.equals(e.getYhqzt(),CouponConsumeStatusEnum.CONSUMED.getCode())).findFirst();
                if(first.isPresent()){
                    logger.info("{}不能重复消费,couponCode={}",pddbh,first.get().getYhqm());
                    return;
                }
            }
            YcDd ycDd = ycDds.stream().filter(e -> StringUtils.equals(e.getDdzt(), "YC4C")).findFirst().orElse(ycDds.get(0));
            //获取冻结中的订单优惠券
            List<YcDdCoupon> ycDdCoupons = ycDdCouponService.selectByDdbh(ycDd.getDdbh(), CouponConsumeStatusEnum.CONSUMING.getCode());
            if (CollectionUtils.isNotEmpty(ycDdCoupons)) {
                YcDdYhxx ycDdYhxx = ycDdYhxxes.stream().filter(e -> StringUtils.equals(e.getYhqzt(), CouponConsumeStatusEnum.CONSUMING.getCode())).findFirst().orElse(null);
                if(ycDdYhxx==null){
                    logger.info("{}优惠券优惠状态不对",pddbh);
                    return;
                }
                //核销
                consumeCoupon(ycDdMain,ycDdYhxx);
                ycDdYhxxService.update(ycDdYhxx);
                YcDdMain updateYcDdMain = new YcDdMain();
                updateYcDdMain.setpDdbh(pddbh);
                if(StringUtils.equals(ycDdYhxx.getYhqzt(),CouponConsumeStatusEnum.CONSUMED.getCode())){
                    updateYcDdMain.setPtyhqzyhje(ycDdYhxx.getDkje().toPlainString());
                }else{
                    updateYcDdMain.setPtyhqzyhje(BigDecimal.ZERO.toPlainString());
                }
                ycDdMainService.update(updateYcDdMain);
            }
        }catch (Exception e){
            logger.error("预消费优惠券异常",e);
        }finally {
            if(lock!=null){
                zookeeperLockService.unlock(lock);
            }
        }

    }

    private YcDdCoupon getYhjeMaxCoupon(YcDd ycDd, String wbgysmc, String cxzbh, List<YcDdCoupon> ycDdCoupons) {
        ycDdCoupons.sort(Comparator.comparing(YcDdCoupon::getCouponCode));
        if (StringUtils.isNotBlank(cxzbh)) {
            ycDdCoupons = ycDdCoupons.stream().filter(e ->StringUtils.equals(e.getCxzbh(),"-") || StringUtils.equals(cxzbh, e.getCxzbh())).collect(Collectors.toList());
        }

        if (StringUtils.isNotBlank(wbgysmc)) {
            //寻找相同供应商的 优惠券领取记录
            ycDdCoupons = ycDdCoupons.stream().filter(e ->StringUtils.equals(wbgysmc, e.getWbgysmc())).collect(Collectors.toList());
        }
        YcDdCoupon ycDdCoupon = ycDdCoupons.get(0);
        BigDecimal yhje = BigDecimal.valueOf(0);

        //确定最合适的优惠券
        for (YcDdCoupon coupon : ycDdCoupons) {
            if (StringUtils.equals(coupon.getLx(), CouponLxEnum.MJQ.getCode())) {
                BigDecimal amount = coupon.getAmount();
                if (BigDecimalUtil.isGreaterThan(amount, yhje)) {
                    yhje = amount;
                    ycDdCoupon = coupon;
                }
            }
            if (StringUtils.equals(coupon.getLx(), CouponLxEnum.ZKQ.getCode())) {
                BigDecimal amount = ycDd.getCgJsje().subtract(BigDecimalUtil.multiply(coupon.getAmount().divide(BigDecimal.TEN,2,RoundingMode.HALF_UP), ycDd.getCgJsje())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal discountLimit = coupon.getDiscountLimit();
                if (discountLimit != null && BigDecimalUtil.isGreaterThanZero(discountLimit) && BigDecimalUtil.isGreaterThan(amount, discountLimit)) {
                    amount = discountLimit;
                }
                if (BigDecimalUtil.isGreaterThan(amount, yhje)) {
                    yhje = amount;
                    ycDdCoupon = coupon;
                }
            }
        }
        return ycDdCoupon;
    }

    /**
     *  根据券码获取优惠券详情
     * @param couponCode
     * @param useType
     */
    public ApiCouponVO getCouponDetail(String ruleId, String memberId, String channelSource, String useType){
        CouponSearchParamDTO dto = new CouponSearchParamDTO();
        dto.setProductCode("1000");
        dto.setMemberId(memberId);
        dto.setChannelSource(channelSource);
        dto.setRuleIdList(Lists.newArrayList(ruleId));
        dto.setUseCarType(useType);
        logger.info("查询优惠券入参={}",JsonMapper.nonEmptyMapper().toJson(dto));
        RestResponse<APIGetCouponVO> response = iCouponOrderServiceClient.getCouponsByParam(dto);
        logger.info("查询优惠券回参={}",JsonMapper.nonEmptyMapper().toJson(response));
        if(response!=null && response.getResult()!=null && CollectionUtils.isNotEmpty(response.getResult().getCoupons())){
            return response.getResult().getCoupons().get(0);
        }
        return null;
    }

    /**
     *  根据券码获取优惠券详情
     * @param ruleIds
     * @param useType
     */
    public List<ApiCouponVO> getCouponDetail(List<String> ruleIds, String memberId, String channelSource, String useType){
        CouponSearchParamDTO dto = new CouponSearchParamDTO();
        dto.setProductCode("1000");
        dto.setMemberId(memberId);
        dto.setChannelSource(channelSource);
        dto.setRuleIdList(ruleIds);
        dto.setUseCarType(useType);
        logger.info("查询优惠券入参={}",JsonMapper.nonEmptyMapper().toJson(dto));
        RestResponse<APIGetCouponVO> response = iCouponOrderServiceClient.getCouponsByParam(dto);
        logger.info("查询优惠券回参={}",JsonMapper.nonEmptyMapper().toJson(response));
        if(response!=null && response.getResult()!=null && CollectionUtils.isNotEmpty(response.getResult().getCoupons())){
            return response.getResult().getCoupons();
        }
        return Lists.newArrayList();
    }

    /**
     *   获取核销完成后的优惠券金额
     * @param pddbh
     * @param ycDdMain
     * @return
     */
    public BigDecimal getCouponDiscountAmount(String pddbh,YcDdMain ycDdMain){
        if(ycDdMain==null){
            ycDdMain = ycDdMainService.selectYcDd(pddbh);
        }
        if(ycDdMain==null|| !StringUtils.equals(ycDdMain.getClyy(),"2")){
            return BigDecimal.ZERO;
        }
        if(StringUtils.equals(ycDdMain.getYhlx(),YhlxEnum.COUPON.getCode())){
            List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectCouponYhxxByPddbh(pddbh);
            if(CollectionUtil.isNotEmpty(ycDdYhxxes)){
                List<YcDdYhxx> collect = ycDdYhxxes.stream().filter(e -> StringUtils.equals(e.getYhqzt(), CouponConsumeStatusEnum.CONSUMED.getCode()) && e.getDkje() != null).collect(Collectors.toList());
                return collect.get(0).getDkje().setScale(2,RoundingMode.HALF_UP);
            }
        }
       return BigDecimal.ZERO;
    }

    /**
     *  核销优惠券
     */
    private boolean consumeCoupon(YcDdMain ycDdMain,YcDdYhxx ycDdYhxx){
        try {
            logger.info("核销订单={}优惠券",ycDdMain.getPDdbh());
            CouponBatchConsumeDTO dto = new CouponBatchConsumeDTO();
            CouponConsumeDTO couponConsumeDTO = new CouponConsumeDTO();
            couponConsumeDTO.setCouponCode(ycDdYhxx.getYhqm());
            couponConsumeDTO.setCouponAmount(ycDdYhxx.getDkje());
            couponConsumeDTO.setTotalAmount(ycDdMain.getCgJsje());
            dto.setConsumeList(Lists.newArrayList(couponConsumeDTO));
            dto.setProductCode("1000");
            dto.setMemberId(ycDdMain.getMemberId());
            dto.setOrderNo(ycDdYhxx.getDdbh());
            dto.setOrderType("10001");
            RestResponse<CouponConsumeVO> restResponse = iCouponBatchConsumeServiceClient.consume(dto);
            if(restResponse!=null && restResponse.getResult()!=null && StringUtils.equals(restResponse.getResult().getSuccess(),YesOrNoEnum.YES.getCode())){
                ycDdYhxx.setYhqzt(CouponConsumeStatusEnum.CONSUMED.getCode());
                //解冻其他卷
                unFreezeCoupon(ycDdYhxx.getYhqm(),ycDdMain);
                return Boolean.TRUE;
            }else{
                ycDdYhxx.setYhqzt(CouponConsumeStatusEnum.CONSUME_FAIL.getCode());
            }
        }catch (Exception e){
            logger.error("核销订单={}优惠券异常",ycDdMain.getPDdbh(),e);
        }
        return Boolean.FALSE;
    }

    /**
     *  解冻除couponCode外的其他优惠券,
     * @param couponCode
     */
    private void unFreezeCoupon(String couponCode,YcDdMain ycDdMain){
        try {

            List<YcDd> ycDds = ycDdService.selectBypDdbh(ycDdMain.getpDdbh(), ycDdMain.getCgshbh());
            if(CollectionUtils.isEmpty(ycDds)){
                return;
            }
            Set<String> ddbhs = ycDds.stream().map(YcDd::getDdbh).collect(Collectors.toSet());
            List<YcDdCoupon> ycDdCoupons = ycDdCouponService.selectByDdbhs(ddbhs);
            if(CollectionUtils.isNotEmpty(ycDdCoupons)){
                List<YcDdCoupon> ycDdCouponList = ycDdCoupons.stream().
                        filter(e -> !StringUtils.equals(e.getCouponCode(), couponCode)).
                        filter(e->StringUtils.equalsAny(e.getCouponStatus(),CouponConsumeStatusEnum.FREEZING.getCode(),CouponConsumeStatusEnum.CONSUMING.getCode())).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(ycDdCouponList)){
                    logger.info("无需解冻");
                    return;
                }
                doUnfreezeCouopon(ycDdMain.getpDdbh(), ycDdCouponList,ycDdMain.getMemberId());
            }
        }catch (Exception e){
            logger.error("解冻订单优惠券异常={}",ycDdMain.getpDdbh(),e);
        }
    }

    /**
     *  解冻优惠券
     * @param pddbh
     * @param ycDdCouponList
     * @throws SystemException
     */
    private void doUnfreezeCouopon(String pddbh, List<YcDdCoupon> ycDdCouponList,String memberId) throws SystemException {
        if(CollectionUtils.isEmpty(ycDdCouponList)){
            return;
        }
        Set<String> couponCodes = ycDdCouponList.stream().map(e -> e.getCouponCode() + "," + e.getActivityId()).collect(Collectors.toSet());
        List<CouponUnfreezeDTO> couponUnfreezeDTOS = couponCodes.stream().map(e -> {
            String[] split = e.split(",");
            CouponUnfreezeDTO couponUnfreezeDTO = new CouponUnfreezeDTO();
            couponUnfreezeDTO.setCouponCode(split[0]);
            couponUnfreezeDTO.setActivityId(split[1]);
            return couponUnfreezeDTO;
        }).collect(Collectors.toList());
        String ddbh = ycDdCouponList.stream().map(YcDdCoupon::getDdbh).collect(Collectors.joining(","));
        //解冻
        CouponBatchUnfreezeDTO dto = new CouponBatchUnfreezeDTO();
        dto.setProductCode("1000");
        dto.setUnfreezeList(couponUnfreezeDTOS);
        dto.setPayAfterRefund(YesOrNoEnum.NO.getCode());
        dto.setConsumerType("0");
        dto.setMemberId(memberId);
        dto.setOrderNo(ddbh);
        dto.setOrderType("10001");
        dto.setAllRefund(YesOrNoEnum.YES.getCode());
        logger.info("订单{}解冻优惠券入参={}",pddbh, JsonMapper.nonEmptyMapper().toJson(dto));
        RestResponse<CouponUnfreezeVO> restResponse = iCouponBatchConsumeServiceClient.unfreeze(dto);
        logger.info("订单{}解冻优惠券回参={}",JsonMapper.nonEmptyMapper().toJson(restResponse));
        if(restResponse.getResult()!=null && restResponse.getResult()!=null){
            CouponUnfreezeVO result = restResponse.getResult();
            String success = result.getSuccess();
            if(StringUtils.equals(success,YesOrNoEnum.NO.getCode())){
                ycDdCouponList.forEach(e->e.setCouponStatus(CouponConsumeStatusEnum.UNFREEZE_FAIL.getCode()));
                ycDdCouponService.updateList(ycDdCouponList);
            }else{
                ycDdCouponList.forEach(e->e.setCouponStatus(CouponConsumeStatusEnum.UNFREEZE.getCode()));
                ycDdCouponService.updateList(ycDdCouponList);
            }
        }
    }

    public void couponConsumeComplete(String pddbh){
        try {
            logger.info("订单={}优惠券核销流程",pddbh);
            List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectCouponYhxxByPddbh(pddbh);
            for (YcDdYhxx ycDdYhxx : ycDdYhxxes) {
                if(StringUtils.equals(ycDdYhxx.getYhqzt(),CouponConsumeStatusEnum.CONSUMED.getCode())){
                    logger.info("订单={}优惠券核销完成",pddbh);
                    List<YcDdCoupon> ycDdCoupons = ycDdCouponService.selectByDdbh(ycDdYhxx.getDdbh(), CouponConsumeStatusEnum.CONSUMING.getCode());
                    YcDdCoupon ycDdCoupon = ycDdCoupons.stream().filter(e -> StringUtils.equals(e.getCouponCode(), ycDdYhxx.getYhqm())).findFirst().orElse(null);
                    if(ycDdCoupon!=null){
                        ycDdCoupon.setCouponStatus(CouponConsumeStatusEnum.CONSUMED.getCode());
                        ycDdCouponService.update(ycDdCoupon);
                        cpsEventPublisher.send(CpsEventEnum.UPDATE_ORDER_ES_VO, ImmutableMap.of("ddbh",ycDdCoupon.getDdbh(),"sfyhq","true","yhqmc",ycDdCoupon.getCouponName(),"yhqm",ycDdCoupon.getCouponCode()));
                    }
                }
            }
        }catch (Exception e){
            logger.error("订单={}优惠券核销异常",pddbh,e);
        }
    }

    /**
     *  订单退款后,优惠券退回
     * @param ycDdBcd
     */
    public void returnCouponAfterRefund(YcDdBcd ycDdBcd, ConfirmApplyRefundBcNotifyToBuyerDTO request){
        try {
            logger.info("补差退款={}退优惠券",ycDdBcd.getBcdh());
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
            List<YcDdYhxx> list = ycDdYhxxService.selectCouponBcYhxxByBcdh(ycDdBcd.getBcdh());
            if(CollectionUtils.isEmpty(list)){
                logger.info("{}无退优惠券记录",ycDdBcd.getBcdh());
                return;
            }
            String allRefund = YesOrNoEnum.NO.getCode();
            BigDecimal gytkzje = ycDdBcds.stream().filter(e -> StringUtils.equals(e.getSqrlx(), "2") && StringUtils.equals(e.getZt(), "6")).map(e -> e.getBcje()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            //供应全退
            if(BigDecimalUtil.equals(ycDd.getGyJsje(),gytkzje)){
                allRefund = YesOrNoEnum.YES.getCode();
            }
            BigDecimal couponRefundAmount = BigDecimal.ZERO;
            for (YcDdYhxx ycDdYhxx : list) {
                CouponBatchAfterSaleCancelDTO dto = new CouponBatchAfterSaleCancelDTO();
                dto.setAllRefund(YesOrNoEnum.NO.getCode());
                dto.setOrderType("10001");
                dto.setProductCode("1000");
                dto.setRefundNo(ycDdBcd.getBcdh());
                dto.setOrderNo(ycDdBcd.getDdbh());
                CouponAfterSaleCancelDTO couponAfterSaleCancelDTO = new CouponAfterSaleCancelDTO();
                couponAfterSaleCancelDTO.setCouponCode(ycDdYhxx.getYhqm());
                couponAfterSaleCancelDTO.setActivityId(ycDdYhxx.getWelfareActivityId());
                dto.setAfterSaleCancelList(Lists.newArrayList(couponAfterSaleCancelDTO));
                logger.info("退款后退回优惠券入参={}",JsonMapper.nonEmptyMapper().toJson(dto));
                RestResponse<CouponAfterSaleCancelVO> restResponse = iCouponBatchConsumeServiceClient.afterSaleCancel(dto);
                logger.info("退款后退回优惠券回参={}",JsonMapper.nonEmptyMapper().toJson(restResponse));
                if(restResponse!=null && restResponse.getResult()!=null
                        && StringUtils.equals(restResponse.getResult().getSuccess(),YesOrNoEnum.YES.getCode())){
                    couponRefundAmount = couponRefundAmount.add(ycDdYhxx.getDkje());
                }
            }
            request.setAllRefund(allRefund);
            request.setCouponRefundAmount(couponRefundAmount);
            request.setRefundCoupon(YesOrNoEnum.YES.getCode());
        }catch (Exception e){
            logger.info("补差退款={}退优惠券异常",ycDdBcd.getBcdh(),e);
        }
    }

    /**
     *  全部子单取消后，解冻优惠券
     * @param ddbh
     */
    public void optCouponAfterCancel(String ddbh){
        try {
            logger.info("取消订单={}解冻优惠券",ddbh);
            //判断关联订单是否全部取消
            YcDd ycDd = ycDdService.selectYcDd(ddbh);
            if(ycDd==null){
                return;
            }
            YcDdMain ycDdMain = ycDdMainService.selectById(ycDd.getpDdbh());
            if(!StringUtils.equals(ycDdMain.getYhlx(),YhlxEnum.COUPON.getCode())){
                return;
            }
            List<YcDd> ycDds = ycDdService.selectBypDdbh(ycDd.getpDdbh(), ycDd.getCgShbh());
            boolean cancelFlag = true;
            for (YcDd one : ycDds) {
                EnumSet<UsecarOrderStatusEnum> cancelStatus = EnumSet.of(UsecarOrderStatusEnum.YC3D, UsecarOrderStatusEnum.YC1G, UsecarOrderStatusEnum.YC3C,
                        UsecarOrderStatusEnum.YC2A, UsecarOrderStatusEnum.YC2C, UsecarOrderStatusEnum.YC2B,
                        UsecarOrderStatusEnum.YC1E, UsecarOrderStatusEnum.YC1B, UsecarOrderStatusEnum.YC1D);
                if(!cancelStatus.contains(UsecarOrderStatusEnum.getEnum(one.getDdzt()))){
                    cancelFlag = false;
                }
            }
            if(cancelFlag){
                logger.info("订单{}关联子单全部取消",ddbh);
                Set<String> ddbhs = ycDds.stream().map(YcDd::getDdbh).collect(Collectors.toSet());
                List<YcDdCoupon> ycDdCoupons = ycDdCouponService.selectByDdbhs(ddbhs);
                if(CollectionUtils.isNotEmpty(ycDdCoupons)){
                    ycDdCoupons = ycDdCoupons.stream().filter(e->StringUtils.equals(e.getCouponStatus(),CouponConsumeStatusEnum.FREEZING.getCode())).collect(Collectors.toList());
                    doUnfreezeCouopon(ycDd.getpDdbh(),ycDdCoupons,ycDd.getMemberId());
                }
            }
        }catch (Exception e){
            logger.error("取消订单后操作优惠券异常",e);
        }
    }

    public List<OrderYhxxVO> getYhxxList(String ddbh, String memberId){
        EntityWrapper<YcDdYhxx> en = new EntityWrapper<>();
        en.eq("ddbh",ddbh);
        List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectByddbh(ddbh);
        YcDd ycDd = ycDdService.selectYcDd(ddbh);
        if(CollectionUtils.isNotEmpty(ycDdYhxxes)){
            List<String> ruleIds = ycDdYhxxes.stream().filter(e -> StringUtils.equals(e.getYhlx(), YhlxEnum.COUPON.getCode())
                    && !StringUtils.equals(e.getYhqzt(),CouponConsumeStatusEnum.CANCEL.getCode())).map(YcDdYhxx::getWelfareActivityId).collect(Collectors.toList());
            String useType = StringUtils.equals(ycDd.getDdlx(), UsecarProductTypeEnum.DJ.getCode()) ? "1" : "0";
            List<ApiCouponVO> couponDetails = getCouponDetail(ruleIds, memberId,ycDd.getDdqdly(),useType);

            if(couponDetails==null) couponDetails = Lists.newArrayList();

            Map<String, ApiCouponVO> couponVOMap = couponDetails.stream().collect(Collectors.toMap(ApiCouponVO::getActivityId, Function.identity(), (o1, o2) -> o1));
            return ycDdYhxxes.stream().map(e->{

                OrderYhxxVO orderYhxxVO = new OrderYhxxVO();
                if(StringUtils.equals(e.getYhlx(),YhlxEnum.COUPON.getCode())){
                    ApiCouponVO apiCouponVO = couponVOMap.get(e.getWelfareActivityId());
                    orderYhxxVO.setMc(apiCouponVO!=null?apiCouponVO.getActivityName():null);
                    orderYhxxVO.setZt(CouponConsumeStatusEnum.geMc(e.getYhqzt()));
                }else{
                    orderYhxxVO.setZt(PointsDeductionEnum.getMc(e.getPointsDeductionStatus()));
                }
                orderYhxxVO.setYhje(e.getDkje());
                orderYhxxVO.setLxmc(YhlxEnum.getMc(e.getYhlx()));
                orderYhxxVO.setDkz(e.getDeductionScore()==null?null:e.getDeductionScore().toPlainString());

                return orderYhxxVO;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public void dealwithCouponWithYcDdBcd(String bcdh,BigDecimal tkje){
        logger.info("补差退款是否退优惠券");
        YcDdBcd ycDdBcd = ycDdBcdService.selectYcDdBcd(bcdh);

        if(ycDdBcd == null){
            logger.warn("补差单为空={}",ycDdBcd.getBcdh());
            return;
        }
        YcDd ycDd = ycDdService.selectYcDd(ycDdBcd.getDdbh());
        if(ycDd==null){
            logger.warn("子订单为空={}",ycDdBcd.getDdbh());
            return;
        }
        YcDdMain ycDdMain = ycDdMainService.selectById(ycDd.getpDdbh());
        if(ycDdMain==null){
            logger.warn("主单为空={}",ycDd.getpDdbh());
            return;
        }
        //查询优惠券消费信息
        List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectCouponYhxxByPddbh(ycDd.getpDdbh());
        if(CollectionUtils.isEmpty(ycDdYhxxes)){
            return;
        }
        ycDdYhxxes = ycDdYhxxes.stream().filter(e->StringUtils.equals(e.getYhqzt(),CouponConsumeStatusEnum.CONSUMED.getCode())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(ycDdYhxxes)){
            return;
        }
        //查询补差单是否已经退积分
        List<YcDdYhxx> bcyhxx = ycDdYhxxService.selectCouponBcYhxxByBcdh(bcdh);
        if(CollectionUtils.isNotEmpty(bcyhxx)){
            logger.error("补差单{}已经退优惠券",bcdh);
            return;
        }

        //找出能直接覆盖退款金额的优惠券
        Optional<YcDdYhxx> full = ycDdYhxxes.stream().filter(e -> BigDecimalUtil.isGreaterThanOrEqual(e.getDkje(), tkje)).findFirst();
        if(full.isPresent()){
            YcDdYhxx ycDdYhxx = full.get();
            if(!StringUtils.equals(ycDdYhxx.getPointsDeductionStatus(),PointsDeductionEnum.YDK.getCode())){
                logger.error("关联的原订单={}无积分抵扣",ycDd.getpDdbh());
                return;
            }
            createBcYhxx(bcdh, tkje, ycDdYhxx);
        }else{
            BigDecimal sytkje = tkje;
            for (YcDdYhxx ycDdYhxx : ycDdYhxxes) {
                if(BigDecimalUtil.isGreaterThanZero(sytkje)){
                    createBcYhxx(bcdh, ycDdYhxx.getDkje(), ycDdYhxx);
                    sytkje = tkje.subtract(ycDdYhxx.getDkje());
                }
            }
        }
    }

    private void createBcYhxx(String bcdh, BigDecimal tkje, YcDdYhxx ycDdYhxx) {
        //创建退优惠券记录
        YcDdYhxx bcdYhxx = new YcDdYhxx();
        bcdYhxx.setDdyhid(cn.vetech.charge.cloud.modules.utils.IdGenerator.getHexId());
        bcdYhxx.setDdbh(bcdh);
        bcdYhxx.setDkje(tkje);
        bcdYhxx.setYhqzt(CouponConsumeStatusEnum.CANCEL.getCode());
        bcdYhxx.setYhlx(YhlxEnum.COUPON.getCode());
        bcdYhxx.setUsePoints("1");
        bcdYhxx.setYhqm(ycDdYhxx.getYhqm());
        bcdYhxx.setWelfareActivityId(ycDdYhxx.getWelfareActivityId());
        bcdYhxx.setWelfareActivityName(ycDdYhxx.getWelfareActivityName());
        bcdYhxx.setpDdbh(bcdh);
        ycDdYhxxService.insert(bcdYhxx);
    }

    public void setCouponInfo(ConfirmOrderInfoToBuyerAsmsDTO dto,YcDd ycdd){
        try {
            if(ycdd==null || !StringUtils.equals(ycdd.getClyy(),"2")){
                return;
            }
            List<YcDdYhxx> ycDdYhxxes = ycDdYhxxService.selectCouponYhxxByPddbh(ycdd.getpDdbh());
            if(CollectionUtil.isNotEmpty(ycDdYhxxes)){
                ycDdYhxxes = ycDdYhxxes.stream().filter(e -> StringUtils.equals(e.getYhqzt(), CouponConsumeStatusEnum.CONSUMING.getCode())).collect(Collectors.toList());
                BigDecimal dkje = ycDdYhxxes.stream().map(YcDdYhxx::getDkje).reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setCouponDeductionAmount(dkje.setScale(2, RoundingMode.HALF_UP));
                dto.setCouponName(ycDdYhxxes.stream().map(YcDdYhxx::getWelfareActivityName).collect(Collectors.joining(",")));
            }
            OrderCoupon orderCoupon = orderCouponCommonService.getAfterUsecarGiftCouponInfo(ycdd);
            if(orderCoupon!=null){
                dto.setGiftCouponName(orderCoupon.getName());
            }
        }catch (Exception e){
            logger.error("设置优惠券信息异常",e);
        }
    }
}