package cn.vetech.center.usecar.openapi.buyer.order.createorder;

import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.system.openapi.OpenApiLog;
import cn.vetech.center.system.openapi.OpenApiShShbDTO;
import cn.vetech.center.system.openapi.OpenApiShYhbDTO;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.vo.UseCarPorductModelVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.UseCarTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarCodeEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.YcDdEx;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.usecar.YcDdExService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.concurrent.VeExecutorServiceFactory;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.CpscUseCarConstant.BOOKING_TYPE_APPOINTMENT;
import static cn.vetech.center.usecar.common.CpscUseCarConstant.BOOKING_TYPE_IMMEDIATE;
import static cn.vetech.center.usecar.common.UseCarConstant.B2C;
import static cn.vetech.center.usecar.common.UseCarConstant.THREE_ORDERS;
import static cn.vetech.center.usecar.common.UseCarConstant.YES;
import static cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum.SFC;
import static cn.vetech.charge.base.CommonMagicNumber.INT1;
import static cn.vetech.charge.cloud.modules.consts.MagicNumberConst.INT10;

/**
 * 下单2.0 订单大融合
 *
 * @author : Y
 * @since 2023/8/14 10:53
 */
@Service
public class CreateOrderV2Service {
    /**
     * 打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(CreateOrderV2Service.class);
    /**
     * v1 服务
     */
    @Autowired
    private CreateOrderV1Service createOrderV1Service;
    /**
     * 用车订单一键三单主单服务
     */
    @Autowired
    private YcDdMainService ycDdMainService;
    /**
     * 用车订单Service
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 用车下单线程池
     */
    public static final ExecutorService BATCH_CREATE_ORDER_EXECUTOR_SERVICE =
            VeExecutorServiceFactory.newExecuteor(100, 200, 50, "用车下单线程池");
    /**
     * 等段时间
     */
    private static final int CREATE_AWAIT = 15;
    /**
     * 等段时间
     */
    private static final int CREATE_TIMEOUT = 20;
    /**
     * 用车生产订单编号Service
     */
    @Autowired
    private UsecarOrderNoService orderNoService;
    /**
     * 公用service
     */
    @Autowired
    private BuyerBookCommonService commService;
    /**
     * 三单服务
     */
    @Autowired
    private ThreeOrderService threeOrderService;

    /**
     * 分布式锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;

    /**
     * KEY
     */
    private static final String USECAR_CREATE_MAIN_ORDER_LOCK = "USECAR_CREATE_MAIN_ORDER_LOCK";
    /**
     * 过期时间
     */
    private static final int CREATE_MAIN_ORDER_LOCK_WAIT_TIME = 1000 * 50;

    @Autowired
    private UsecarCacheService usecarCacheService;

    @Autowired
    private YcDdExService ycDdExService;

    @Autowired
    private RedisCacheManage redisCacheManage;
    /**
     * @param request         入参
     * @param openApiShShbDTO 商户
     * @param openApiShYhbDTO 用户
     * @param openApiLog      日志
     * @return 回参
     */

     public CreateOrderResponse createOrder(CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) {
        CreateOrderResponse response = new CreateOrderResponse();
        if (!StringUtils.equals(B2C, request.getDdly()) && (
                CollectionUtils.isEmpty(request.getOrders()) || StringUtils.isBlank(request.getZbddbh()))) {
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
            return response;
        }
        if(StringUtils.isBlank(request.getCgShbh())){
            request.setCgShbh(openApiShShbDTO.getShbh());
        }
        // 主单入库
        YcDd ycdd = ycDdService.selectOneByZbddbh(request.getZbddbh(), openApiShShbDTO.getShbh());
        YcDdMain ycDdMain = null;
        if (ycdd == null || StringUtils.isBlank(ycdd.getpDdbh())) {
            if (StringUtils.isBlank(request.getCpsMainOrderNo())) {
                request.setCpsMainOrderNo(orderNoService.getMainOrderNo(openApiShShbDTO.getShbh()));
                ycDdMain = createLocalMainOrder(request);
            }
        } else {
            ycDdMain = ycDdMainService.selectById(ycdd.getpDdbh());
            request.setCpsMainOrderNo(ycdd.getpDdbh());
        }
        String childOrderNo = null;
        CountDownLatch countDownLatch = new CountDownLatch(request.getOrders().size());
        List<Future<CreateOrderResponse>> createOrderFutures = new ArrayList<>(request.getOrders().size());
        for (ChildOrderDTO dto : request.getOrders()) {
            Future<CreateOrderResponse> createOrderFuture = BATCH_CREATE_ORDER_EXECUTOR_SERVICE.submit(() ->
                    creatChildOrder(dto, request, openApiShShbDTO, openApiShYhbDTO, openApiLog, countDownLatch));
            createOrderFutures.add(createOrderFuture);
        }
        try {
            long start = System.currentTimeMillis();
            countDownLatch.await(CREATE_AWAIT, TimeUnit.SECONDS);
            long end = System.currentTimeMillis();
            if(end-start>3000){
                logger.info("下单接口耗时较长，耗时{}毫秒",(end-start));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("下单等待异常", e);
        }
        for (Future<CreateOrderResponse> createOrderFuture : createOrderFutures) {
            CreateOrderResponse createOrderVO = null;
            try {
                if(createOrderFuture==null){
                    continue;
                }
                createOrderVO = createOrderFuture.get(CREATE_TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                logger.error("下单获取异常", e);
            }
            if (createOrderVO != null && StringUtils.isNotBlank(createOrderVO.getDdbh()) && StringUtils.isBlank(childOrderNo)) {
                childOrderNo = createOrderVO.getDdbh();
                break;
            }
        }
        mapChild2MainOrder(ycDdMain, childOrderNo);
        if (null != ycdd) {
            commService.saveOrUpdateClk(ycdd);
        }
        // 设置单号
        response.setGysMddbh(request.getCpsMainOrderNo());
        response.setDdbh(request.getCpsMainOrderNo());
        response.setOrders(request.getOrders());
        // 设置日志单号
        openApiLog.setDdbh(request.getCpsMainOrderNo());
        openApiLog.setYwdh(request.getOrders().stream().map(ChildOrderDTO::getCpsChildOrderNo).collect(Collectors.joining(",")));
        return response;
    }

    /**
     * 更新主单
     *
     * @param ycDdMain     主单
     * @param childOrderNo 子单
     */
     private void mapChild2MainOrder(YcDdMain ycDdMain, String childOrderNo) {
        InterProcessMutex lock = null;
        try {
            lock = zookeeperLockService.tryLock(LockType.LOCK, USECAR_CREATE_MAIN_ORDER_LOCK, ycDdMain.getpDdbh(), CREATE_MAIN_ORDER_LOCK_WAIT_TIME);
            YcDdMain newMainOrder = ycDdMainService.selectById(ycDdMain.getpDdbh());
            if(newMainOrder!=null && StringUtils.isNotBlank(newMainOrder.getGyDdbh())){
                return;
            }
            YcDd order = ycDdService.selectYcDd(childOrderNo);
            YcDdMain updateMainOrder = BeanMapper.map(order, YcDdMain.class);
            updateMainOrder.setCgshbh(order.getCgShbh());
            updateMainOrder.setGyDdbh(null);
            updateMainOrder.setpDdbh(ycDdMain.getpDdbh());
            updateMainOrder.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
            updateMainOrder.setCgddbh(ycDdMain.getCgddbh());
            updateMainOrder.setOrderType(ycDdMain.getOrderType());
            updateMainOrder.setYyLx(StringUtils.length(order.getYcsj()) == INT10 ? BOOKING_TYPE_IMMEDIATE : BOOKING_TYPE_APPOINTMENT);
            boolean success = ycDdMainService.updateById(updateMainOrder);
            threeOrderService.updateSpecialServiceSupport(updateMainOrder);
            logger.info("{}修改主单{}", ycDdMain.getpDdbh(), success);
        }catch (Exception e){
            logger.error("更新主表数据异常",e);
        }finally {
            if (null != lock) {
                zookeeperLockService.unlock(lock);
            }
        }
    }

    /**
     * 主单入库
     *
     * @param request 入参
     * @return 主单
     */
    private YcDdMain createLocalMainOrder(CreateOrderRequest request) {
        YcDdMain ycDdMain = new YcDdMain();
        ycDdMain.setCallRule(request.getCallRule());
        if (YES.equals(request.getThreeOrders())
                && CollectionUtils.isNotEmpty(request.getOrders())) {
            ycDdMain.setConfirmOrder("0");
        }
        ycDdMain.setDdlx(request.getDdlx());
        ycDdMain.setMemberId(request.getMemberId());
        ycDdMain.setTnCode(request.getTnCode());
        ycDdMain.setChannelId(request.getChannelId());
        ycDdMain.setpDdbh(request.getCpsMainOrderNo());
        ycDdMain.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
        ycDdMain.setCgddbh(request.getCgDdbh());
        ycDdMain.setAutoPay(request.getAutoPay());
        ycDdMain.setAutoPayType(request.getAutoPayType());

        if (StringUtils.equals(request.getSfyjsd(), YES)) {
            ycDdMain.setOrderType(UseCarTypeEnum.THREE_ORDERS.getCode());
        } else {
            ycDdMain.setOrderType(UseCarTypeEnum.BATCH.getCode());
        }
        ycDdMain.setNewOrder(request.getNewOrder());
        ycDdMain.setCgshbh(request.getCgShbh());
        ycDdMain.setXdsj(VeDate.getNow());
        ycDdMain.setBatchCreate(request.getOrders().size() > 1 ? YES : "0");
        ycDdMain.setYyLx(StringUtils.length(request.getYcsj()) == INT10 ? BOOKING_TYPE_IMMEDIATE : BOOKING_TYPE_APPOINTMENT);
        ycDdMain.setInvoiceStatus("0");

        setHyjInfo(request, ycDdMain);

        ycDdMainService.insert(ycDdMain);
        YcDdEx ycDdEx = new YcDdEx();
        ycDdEx.setPddbh(ycDdMain.getpDdbh());
        ycDdEx.setCompanyName(request.getCompanyName());
        ycDdEx.setCompanyId(request.getCompanyId());
        ycDdExService.insertYcDdEx(ycDdEx);
        return ycDdMain;
    }

    /**
     *  设置会员价信息
     * @param request
     * @param ycDdMain
     */
    private void setHyjInfo(CreateOrderRequest request, YcDdMain ycDdMain) {
        String cacheId = request.getPricecacheid();
        if (StringUtils.contains(request.getPricecacheid(), ",")) {
            cacheId = StringUtils.split(request.getPricecacheid(), ",")[0];
        }

        String flag = (String)redisCacheManage.get(UseCarConstant.QUERY_MEMBER_PRICE_FLAG, cacheId.split("-")[0]);

        if(StringUtils.equals(flag,"1")){
            ycDdMain.setSfcxhyj("1");
            if (StringUtils.isNotBlank(request.getDdlx()) && ("10000501".equals(request.getDdlx()) ||  SFC.getCode().equals(request.getDdlx()))) {
                String[] ids = request.getPricecacheid().split(",");
                for (String id : ids ){
                    BookSpeciCalCachePrice carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(id);
                    if(StringUtils.isNotBlank(carcache.getMemberDesc())){
                        ycDdMain.setYhzkms(carcache.getMemberDesc());
                        break;
                    }
                }
            }else{
                String[] ids = request.getPricecacheid().split(",");
                for (String id :ids) {
                    UseCarPorductModelVO priceCaCheBean= usecarCacheService.getPriceCaCheYc(id);
                    if(StringUtils.isNotBlank(priceCaCheBean.getMemberDesc())){
                        ycDdMain.setYhzkms(priceCaCheBean.getMemberDesc());
                        break;
                    }
                }

            }
        }

    }

    /**
     * 子单下单
     *
     * @param dto             子单
     * @param request         入参
     * @param openApiShShbDTO 商户
     * @param openApiShYhbDTO 用户
     * @param openApiLog      日志
     * @param countDownLatch
     */
    private CreateOrderResponse creatChildOrder(ChildOrderDTO dto, CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog, CountDownLatch countDownLatch) {
        try {
            CreateOrderRequest newReq = BeanMapper.map(request, CreateOrderRequest.class);
            newReq.setCgDdbh(dto.getBuyerChildOrderNo());
            newReq.setPricecacheid(dto.getPriceCacheId());
            CreateOrderResponse createOrderResponse = null;
            if (StringUtils.isNotBlank(request.getDdlx()) && ("10000501".equals(request.getDdlx()) ||  SFC.getCode().equals(request.getDdlx()))) {
                createOrderResponse = createOrderV1Service.specialCarSubmitOrderToCps(newReq, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            } else {
                createOrderResponse = createOrderV1Service.shuttleCarSubmitOrderToCps(newReq, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            }
            if (createOrderResponse != null) {
                dto.setCpsChildOrderNo(createOrderResponse.getDdbh());
                dto.setOrderStatus(createOrderResponse.getDdzt());
            }
            return createOrderResponse;
        } catch (Exception e) {
            logger.error("下单异常", e);
        } finally {
            countDownLatch.countDown();
        }
        return null;
    }
}