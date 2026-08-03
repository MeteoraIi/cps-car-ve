package cn.vetech.center.usecar.openapi.buyer.order.createorder;

import cn.vetech.center.car.service.GyShfzService;
import cn.vetech.center.car.service.GySjdyService;
import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.system.openapi.OpenApiLog;
import cn.vetech.center.system.openapi.OpenApiShShbDTO;
import cn.vetech.center.system.openapi.OpenApiShYhbDTO;
import cn.vetech.center.usecar.book.buyer.dto.BookCreateOrderDTO;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarCommonService;
import cn.vetech.center.usecar.book.buyer.vo.UseCarCreateOrderVO;
import cn.vetech.center.usecar.book.buyer.vo.UseCarPorductModelVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.*;
import cn.vetech.center.usecar.common.util.UseCarCommonUtil;
import cn.vetech.center.usecar.coupon.CouponConsumeService;
import cn.vetech.center.usecar.coupon.OrderCouponCommonService;
import cn.vetech.center.usecar.coupon.dto.ConsumeCouponDTO;
import cn.vetech.center.usecar.coupon.dto.CreateOrderClaimCouponDTO;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdCk;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.GetPickUpFlightParamService;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.bean.PickUpFlightBean;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.request.PickUpFlightRequest;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.response.PickUpFlightResponse;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdCkService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.setting.profit.service.CpsaYcDdProfitRuleJlService;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import cn.vetech.center.usecar.travel.CpsTravelService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.exception.SystemException;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.Arith;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static cn.vetech.center.usecar.common.UseCarConstant.B2C;
import static cn.vetech.center.usecar.common.UseCarConstant.CREATE_ORDER_V2;
import static cn.vetech.center.usecar.common.code.CarErrorCode.CAR_0130;
import static cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum.SFC;

/**
 * 1.0 原始服务
 *
 * @author : Y
 * @since 2023/8/14 11:18
 */
@Service
public class CreateOrderV1Service {
    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(CreateOrderV1Service.class);
    /**
     * 下单公共服务
     */
    @Autowired
    private CreateOrderCommonService createOrderCommonService;
    /**
     * 用车生产订单编号Service
     */
    @Autowired
    private UsecarOrderNoService orderNoService;

    /**
     * 用车订单Service
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 接送车下单Service
     */
    @Autowired
    private BuyerBookService jscBookService;
    /**
     * 用车价格缓存
     */
    @Autowired
    private UsecarCacheService usecarCacheService;
    /**
     * 产品查询公共方法service
     */
    @Autowired
    private BuyerBookSpeciCarCommonService buyerBookSpeciCarCommonService;
    /**
     * 公用service
     */
    @Autowired
    private BuyerBookCommonService commService;
    /**
     * 商户分组
     */
    @Autowired
    private GyShfzService gyShfzService;
    /**
     * 供应对接对应服务
     */
    @Autowired
    private GySjdyService gySjdyService;
    /**
     * 一键三单服务
     */
    @Autowired
    private ThreeOrderService threeOrderService;

    /**
     * 分布式锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;
    /**
     * 用车订单乘客服务
     */
    @Autowired
    private YcDdCkService ycDdCkService;
    /**
     * 下单公共服务
     */
    @Autowired
    private BuyerBookCommonService buyerBookCommonService;
    /**
     * 用车订单对象成本对象
     */
    @Autowired
    private GetPickUpFlightParamService getPickUpFlightParamService;

    @Autowired
    private CpsaYcDdProfitRuleJlService cpsaYcDdProfitRuleJlService;

    @Autowired
    private CpsTravelService cpsTravelService;

    @Autowired
    private OrderCouponCommonService couponCommonService;

    @Autowired
    private CouponConsumeService couponConsumeService;

    /**
     * 专快车下单到CPS
     *
     * @param request         订单对象
     * @param openApiShShbDTO 商户对象
     * @param openApiShYhbDTO 用户对象
     * @param openApiLog      日志对象
     * @return 返回下单成功对象
     */
    public CreateOrderResponse specialCarSubmitOrderToCps(CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) {
        CreateOrderResponse response = new CreateOrderResponse();
        try {
            if (StringUtils.isNotBlank(request.getPricecacheid())) {
                BookSpeciCalCachePrice carcache = null;
                try {
                    String cacheId = request.getPricecacheid();
                    if (StringUtils.contains(request.getPricecacheid(), ",")) {
                        cacheId = StringUtils.split(request.getPricecacheid(), ",")[0];
                    }
                    carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(cacheId);
                } catch (Exception e) {
                    response = shuttleCarSubmitOrderToCps(request, openApiShShbDTO, openApiShYhbDTO, openApiLog);
                }
                if (carcache != null) {
                    if (StringUtils.isBlank(carcache.getCfd())) {
                        throw new RuntimeException("出发地POI不能为空");
                    }
                    logger.info("****进入到ASMS专快车下单到CPS接口==>获取价格缓存对象***：" + carcache);
                    long starttime = System.currentTimeMillis();
                    YcDd ycDd = getYcDd(request, carcache, openApiShShbDTO, openApiShYhbDTO);
                    checkOrder(ycDd,request);
                    ycDd.setCfdXxdz(StringUtils.defaultIfBlank(carcache.getCfdXxdz(), carcache.getCfd()));
                    final int maxLength = 100;
                    if (StringUtils.isNotBlank(ycDd.getCfdXxdz()) && ycDd.getCfdXxdz().length() > maxLength) {
                        String cfdXxdz = ycDd.getCfdXxdz();
                        cfdXxdz = cfdXxdz.substring(0, maxLength);
                        ycDd.setCfdXxdz(cfdXxdz);
                    }
                    if (StringUtils.isNotBlank(ycDd.getMddXxdz()) && ycDd.getMddXxdz().length() > maxLength) {
                        String mddXxdz = ycDd.getMddXxdz();
                        mddXxdz = mddXxdz.substring(0, maxLength);
                        ycDd.setMddXxdz(mddXxdz);
                    }
                    ycDd.setFwsbh(request.getFwsbh());
                    ycDd.setFwsmc(request.getFwsmc());
                    ycDd.setClyy(request.getClyy());
                    ycDd.setAutoPay(request.getAutoPay());
                    if (StringUtils.isNotBlank(request.getSfxyhf())) {
                        if (StringUtils.equals(request.getSfxyhf(), "1")) {
                            ycDd.setFfgz(request.getSfxyhf());
                            ycDd.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
                        }
                    } else {
                        //按分组取是否是先用后付
                        Integer isUseBefore = gyShfzService.getShfzCount(ycDd.getCgShbh());
                        if (isUseBefore > 0 && "1".equals(ycDd.getClyy())) {
                            ycDd.setFfgz("1");
                            ycDd.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
                        }
                    }
                    if ("0".equals(ycDd.getSfykj())
                            && StringUtils.isNotEmpty(request.getDdly())
                            && request.getDdly().indexOf(UseCarCommonUtil.DDLY) != -1) {
                        //一口价 且订单来源为直销订单
                        response.setStatus(UsecarCodeEnum.FAIL.getCode());
                        response.setErrorCode(UsecarOrderCode.UCAR_10001.getCode());
                        response.setErrorMessage(UsecarOrderCode.UCAR_10001.getMessage());
                        return response;
                    }
                    ycDd.setAutoPayType(request.getAutoPayType());
                    ycDd.setAutoPay(request.getAutoPay());
                    commService.yesOrNoCloudOrder(ycDd);
                    logger.info("进入到ASMS专快车下单到CPS接口==>YC_DD对象：" + ycDd);
                    boolean flag = ycDdService.insertYcDd(ycDd);
                    long endtime = System.currentTimeMillis();
                    if (flag) {
                        response.setDdbh(ycDd.getDdbh());
                        response.setDdzt(ycDd.getDdzt());
                        response.setTime(new Long(endtime - starttime));
                        List<YcDdCk> passengerList = request.getPassengerList();
                        if (CollectionUtils.isNotEmpty(passengerList)) {
                            for (YcDdCk passenger : passengerList) {
                                passenger.setPassengerId(IdGenerator.getHexId());
                                passenger.setOrderNo(ycDd.getDdbh());
                                passenger.setBzcgs(request.getBzcgs());
                                ycDdCkService.savePassenger(passenger);
                            }
                        }
                        // 缓存是否自动确认订单标识
                        jscBookService.cacheConfirmOrder(ycDd, request.getConfirmOrder());
                        if (null != ycDd) {
                            commService.saveOrUpdateClk(ycDd);
                        }
                        // 下单成功后保存订单的控润记录
                        cpsaYcDdProfitRuleJlService.upsertMatchRecordWhileCreatedOrder(carcache!=null?carcache.getKrinfo():null,ycDd.getDdbh());

                        CreateOrderClaimCouponDTO dto = new CreateOrderClaimCouponDTO();
                        dto.setDdbh(ycDd.getDdbh());
                        dto.setPriceCacheIds(request.getPricecacheid());
                        dto.setCouponInfos(request.getClaimedCouponInfos());
                        dto.setClyy(request.getClyy());
                        dto.setSfzkc(YesOrNoEnum.YES.getCode());
                        couponCommonService.initialingClaimCoupon(dto);
                        ConsumeCouponDTO consumeCouponDTO = new ConsumeCouponDTO();
                        consumeCouponDTO.setCouponInfos(request.getCouponInfos());
                        consumeCouponDTO.setPriceCacheIds(request.getPricecacheid());
                        consumeCouponDTO.setClyy(ycDd.getClyy());
                        consumeCouponDTO.setSfzkc(YesOrNoEnum.YES.getCode());
                        consumeCouponDTO.setDdbh(ycDd.getDdbh());
                        couponConsumeService.initConsumeCoupon(consumeCouponDTO);
                    }

                    if ("1".equals(request.getSfyjsd())) {
                        response.setQxsx(gySjdyService.selectCancelLimitByGysbhAndCityId(ycDd.getCfdCsid(), ycDd.getGyShbh()));
                    }
                    if (!StringUtils.equals(request.getNewOrder(), "1")) {
                        UseCarTypeEnum useCarTypeEnum = UseCarTypeEnum.BATCH;
                        if (StringUtils.equals(ycDd.getShxz(), "102401")) {
                            useCarTypeEnum = UseCarTypeEnum.ONE;
                        }
                        if ("1".equals(request.getSfyjsd())) {
                            useCarTypeEnum = UseCarTypeEnum.THREE_ORDERS;
                        }
                        String pDdbh = threeOrderService.createMainOrder(ycDd, useCarTypeEnum);
                        response.setGysMddbh(pDdbh);
                    }
                    // 下单到供应
                    if ("1".equals(ycDd.getFfgz())) {
                        createOrderToLink(ycDd,request.getBzcgs());
                    }
                    if (flag) {
                        ycDd = ycDdService.selectYcDd(ycDd.getDdbh());
                        if (StringUtils.isNotBlank(request.getAutotest()) && UseCarConstant.NUMONE.equals(request.getAutotest())) {
                            //如是测试订单，将订单数据缓存
                            usecarCacheService.putAutoTestCache(ycDd.getDdbh(), ycDd.getDdbh());
                        }
                    } else {
                        response.setTime(new Long(endtime - starttime));
                        response.setStatus(UsecarCodeEnum.FAIL.getCode());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("订单编号：" + request.getCgDdbh() + "专快车下单到CPS异常", e);
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
        }
        return response;
    }

    /**
     * 检查订单是否合规
     * @param ycDd 订单信息
     * @param request 入参
     */
    private void checkOrder(YcDd ycDd, CreateOrderRequest request) throws SystemException {
        if (StringUtils.isNotBlank(request.getEstimatedAmountExceeds())) {
            if (Arith.sub(new BigDecimal(request.getEstimatedAmountExceeds()),ycDd.getCsygje()).compareTo(BigDecimal.ZERO)>0){
                throw  new SystemException(CAR_0130);
            }
        }
        if (StringUtils.isNotBlank(request.getEstimatedMileageExceeds())) {
            if (Arith.sub(new BigDecimal(request.getEstimatedMileageExceeds()).multiply(new BigDecimal(1000)),ycDd.getCsygje()).compareTo(BigDecimal.ZERO)>0){
                throw  new SystemException(CAR_0130);
            }
        }
    }

    /**
     * 下单到供应
     *
     * @param ycDd     用车订单
     */
    private void createOrderToLink(YcDd ycDd,String bzcgs) {
        InterProcessMutex lock = null;
        try {
            String sourceOrderId = null;
            if (StringUtils.equals(ycDd.getCgDdly(), B2C)) {
                sourceOrderId = ycDd.getCgShbh() + ycDd.getpDdbh();
            } else {
                sourceOrderId = ycDd.getCgShbh() + ycDd.getCgDdbh().substring(UseCarConstant.ZERO, ycDd.getCgDdbh().length() - UseCarConstant.TWO);
            }
            lock = zookeeperLockService.tryLock(LockType.LOCK, UseCarConstant.USECAR_AMAP_LOCK, sourceOrderId, UseCarConstant.USECAR_AMAP_LOCK_WAIT_TIME);
            usecarCacheService.cacheAmapOrder(ycDd);
        } catch (Exception e) {
            logger.error("缓存高德订单信息异常", e);
        } finally {
            if (null != lock) {
                zookeeperLockService.unlock(lock);
            }
        }
        createOrderCommonService.createBeforePay(ycDd);
    }
}