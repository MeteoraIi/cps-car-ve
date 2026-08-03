package cn.vetech.center.usecar.order.buyer.service;

import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.usecar.api.vo.LinkUseCarOrderPushVO;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.notice.buyer.dto.ArrangeCarNotifyToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.dto.CancelOrder;
import cn.vetech.center.usecar.order.buyer.dto.BuyerNormalOrderOperateDTO;
import cn.vetech.center.usecar.point.MemberPointService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.YcDdEsV2Service;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.concurrent.VeExecutorService;
import org.vetech.core.modules.utils.mapper.BeanMapper;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.*;

/**
 * 批量取消服务
 *
 * @author : Y
 * @since 2023/8/21 9:42
 */
@Service
public class BatchCancelService {
    /**
     * 打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(BatchCancelService.class);
    /**
     * 用车采购订单正常单服务类
     */
    @Autowired
    private BuyerOrderService buyerOrderService;
    /**
     * 一键三单服务
     */
    @Autowired
    private ThreeOrderService threeOrderService;
    /**
     * es
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 主单服务
     */
    @Autowired
    private YcDdMainService ycDdMainService;

    @Autowired
    private YcDdEsV2Service ycDdEsV2Service;
    /**
     * 等待时间
     */
    private static final int CANCEL_AWAIT_TIMEOUT = 30;
    /**
     * 等待时间
     */
    private static final int USECAR_BATCH_CANCEL_LOCK_WAIT = 30 * 1000;
    /**
     * 取消锁前缀
     */
    private static final String USECAR_BATCH_CANCEL_LOCK = "USECAR_BATCH_CANCEL_LOCK";
    /**
     * KEY
     */
    private static final String USECAR_CREATE_MAIN_ORDER_LOCK = "USECAR_CREATE_MAIN_ORDER_LOCK";
    /**
     * 过期时间
     */
    private static final int CREATE_MAIN_ORDER_LOCK_WAIT_TIME = 1000 * 50 + 1000;
    /**
     * 锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;

    @Autowired
    private Tracer tracer;
    /**
     * 用车取消线程池
     */
    public ExecutorService BATCH_CANCEL_EXECUTOR_SERVICE = null;
    /**
     * 积分
     */
    @Autowired
    private MemberPointService memberPointService;

    @PostConstruct
    public void initExecutorService(){
        BATCH_CANCEL_EXECUTOR_SERVICE = VeExecutorService.createExecutorService(100, 100, 200, "用车取消线程池",tracer);
    }
    /**
     * 批量取消订单
     *
     * @param orders 订单
     * @param dto    入参
     * @return 回参
     */
    public BigDecimal cancelChildOrders(List<YcDd> orders, BuyerNormalOrderOperateDTO dto) {
        CountDownLatch countDownLatch = new CountDownLatch(orders.size());
        for (YcDd ycDd : orders) {
          BuyerNormalOrderOperateDTO currentDTO = BeanMapper.map(dto, BuyerNormalOrderOperateDTO.class);
            currentDTO.setDdbh(ycDd.getDdbh());
            BATCH_CANCEL_EXECUTOR_SERVICE.submit(() -> cancelOne(currentDTO, countDownLatch));
        }
        try {
            countDownLatch.await(CANCEL_AWAIT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("取消等待异常", e);
        }
        List<YcDd> ycDdList = ycDdService.selectAllBypDdbh(dto.getCpsMainOrderNo(), dto.getCgShbh());
        return threeOrderService.getPenaltyAmount(ycDdList);
    }

    /**
     * 取消单个订单
     *
     * @param dto            入参
     * @param countDownLatch 计算器
     */
    private void cancelOne(BuyerNormalOrderOperateDTO dto, CountDownLatch countDownLatch) {
        try {
            logger.info("批量取消订单{}", dto.getDdbh());
            Boolean success = buyerOrderService.cancelOrder(dto);
            logger.info("批量取消订单{}，{}", dto.getDdbh(), success);
        } catch (Exception e) {
            logger.error("取消异常", e);
        } finally {
            countDownLatch.countDown();
            logger.info("{}取消完成", dto.getDdbh());
        }
    }

    public Boolean cancelOne(BuyerNormalOrderOperateDTO dto){
        logger.info("批量取消订单{}", dto.getDdbh());
        Boolean success = buyerOrderService.cancelOrder(dto);
        logger.info("批量取消订单{}，{}", dto.getDdbh(), success);
        return success;
    }

    /**
     * 已派车取消其他订单
     *
     * @param ycdd 订单
     * @return 已取消的子订单
     */
    public List<YcDd> cancelOtherChildOrders(YcDd ycdd, LinkUseCarOrderPushVO upv) {
        InterProcessMutex lock = null;
        try {
            lock = zookeeperLockService.tryLock(LockType.LOCK, USECAR_CREATE_MAIN_ORDER_LOCK, ycdd.getpDdbh(), CREATE_MAIN_ORDER_LOCK_WAIT_TIME);
            //方案二，锁住采购单，然后查询主表时采用for update互斥锁
         //  lock = zookeeperLockService.tryLock(LockType.LOCK, USECAR_BATCH_CANCEL_LOCK, ycdd.getZbddbh(), USECAR_BATCH_CANCEL_LOCK_WAIT);
            if (lock == null) {
                logger.error("取消其他订单获取锁失败{}", ycdd.getDdbh());
                return Lists.newArrayList(0);
            }
            List<YcDd> orders = cancelOtherChildOrders2(ycdd,upv);
            logger.info("{}取消成功{}条", ycdd.getDdbh(), orders.size());
            if (Arrays.asList(CPSC_PC, B2C).contains(ycdd.getCgDdly())) {
                memberPointService.payPointsAndCheck(ycdd.getDdbh());
            }
            return orders;
        } catch (Exception e) {
            logger.error("取消其他订单异常" + ycdd.getDdbh(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (lock != null) {
                zookeeperLockService.unlock(lock);
            }
        }
    }

    /**
     * 已派车取消其他订单
     *
     * @param ycdd 订单
     * @return 已取消的子订单
     */
    public List<YcDd> cancelOtherChildOrders2(YcDd ycdd,LinkUseCarOrderPushVO upv) {
        if (ycdd == null) {
            return new ArrayList<>(0);
        }
        YcDd newOrder = ycDdService.selectYcDd(ycdd.getDdbh());
        YcDdMain ycDdMain = ycDdMainService.selectById(ycdd.getpDdbh());
        if (ycDdMain == null
                || !StringUtils.equals(ycDdMain.getNewOrder(), NEW_ORDER)
                || StringUtils.equals(THREE_ORDERS, ycDdMain.getOrderType())) {
            if (!upv.isForceCancel()) {
                logger.info("非新下单模式的批量下单", ycdd.getDdbh());
                return new ArrayList<>(0);
            }
        }
        // 判断CPS系统本地子订单是否已经被取消了，已经被取消则尝试取消
        if(Lists.newArrayList("YC1E","YC2A").contains(newOrder.getDdzt())){
            if(!Lists.newArrayList("YC1E","YC2A").contains(upv.getDdzt())){
                //尝试取消该订单，
                BuyerNormalOrderOperateDTO cancelDTO = new BuyerNormalOrderOperateDTO();
                //商户编号
                cancelDTO.setCgShbh(ycdd.getCgShbh());
                //采购取消人
                cancelDTO.setCpsMainOrderNo(ycdd.getpDdbh());
                cancelDTO.setCgQxr("CPSADMIN");
                //采购取消原因
                cancelDTO.setCgQxyy(StringUtils.defaultIfBlank(newOrder.getCgQxyy(),"乘客取消"));
                //是否强制取消(true或false)默认false
                cancelDTO.setForce(TRUE);
                cancelDTO.setDdbh(newOrder.getDdbh());
                cancelDTO.setReCancel(YES);
                if(cancelOne(cancelDTO)){
                    // 已取消订单
                    return Lists.newArrayList(newOrder);
                }
            }
        }
        if (CANCEL_STATUS.contains(newOrder.getDdzt())) {
            logger.info("当前订单已取消，无需处理{}", ycdd.getDdbh());
            throw new RuntimeException("当前订单已取消，无需处理");
        }
        List<YcDd> ycDdList = null;
        if (StringUtils.equals(ycdd.getCgDdly(), B2C)) {
            ycDdList = ycDdService.selectAllBypDdbh(ycdd.getpDdbh(), ycdd.getCgShbh());
        } else {
            ycDdList = ycDdService.selectByZbddbh(ycdd.getZbddbh(), ycdd.getCgShbh());
        }
        // 正常已派车，但是主单供应单号不是自己，代表另一个已派车供应商已经绑定，需要取消自己
        // cpsc 不走这个逻辑
        if(!StringUtils.equals(ycdd.getCgDdly(), B2C) && StringUtils.isNotBlank(ycDdMain.getGyDdbh()) &&  !StringUtils.equals(ycDdMain.getGyDdbh(),ycdd.getGyDdbh())){
            //尝试取消该订单，
            BuyerNormalOrderOperateDTO cancelDTO = new BuyerNormalOrderOperateDTO();
            //商户编号
            cancelDTO.setCgShbh(ycdd.getCgShbh());
            //采购取消人
            cancelDTO.setCpsMainOrderNo(ycdd.getpDdbh());
            cancelDTO.setCgQxr("CPSADMIN");
            //采购取消原因
            cancelDTO.setCgQxyy("乘客取消");
            //是否强制取消(true或false)默认false
            cancelDTO.setForce(TRUE);
            cancelDTO.setDdbh(newOrder.getDdbh());
            cancelDTO.setReCancel(YES);
            if(cancelOne(cancelDTO)){
                // 已取消订单
                return Lists.newArrayList(newOrder);
            }
        }
        //当前主单没有成功绑定子单,则需要绑定自己
        if(StringUtils.isBlank(ycDdMain.getGyDdbh())){
            threeOrderService.updateMainOrder(Lists.newArrayList(newOrder),ycDdMain);
        }
        // 主单供应单号是自己，则绑单成功，取消其他供应商的单子
        if(CollectionUtils.isNotEmpty(ycDdList)){
            List<YcDd> canCancelOrders = ycDdList.stream().filter(e -> !StringUtils.equals(e.getDdbh(), ycdd.getDdbh()) && CAN_CANCEL_STATUS.contains(e.getDdzt())).collect(Collectors.toList());
            BuyerNormalOrderOperateDTO cancelDTO = new BuyerNormalOrderOperateDTO();
            //商户编号
            cancelDTO.setCgShbh(ycdd.getCgShbh());
            //采购取消人
            cancelDTO.setCpsMainOrderNo(ycdd.getpDdbh());
            cancelDTO.setCgQxr("CPSADMIN");
            //采购取消原因
            cancelDTO.setCgQxyy("乘客取消");
            //是否强制取消(true或false)默认false
            cancelDTO.setForce(TRUE);
            cancelDTO.setNotCancel(upv.getNotCancel());
            cancelChildOrders(canCancelOrders, cancelDTO);
            ycDdList = ycDdService.selectAllBypDdbh(ycdd.getpDdbh(), ycdd.getCgShbh());
            // 已取消订单
            List<YcDd> cancelOrders = ycDdList.stream().filter(e -> !StringUtils.equals(e.getDdbh(), ycdd.getDdbh()) && CANCEL_STATUS.contains(e.getDdzt())).collect(Collectors.toList());
            return cancelOrders;
        }
        return Lists.newArrayList();
    }

    /**
     * 设置已派车取消订单
     *
     * @param arrangeCarNotifyDTO 已派车通知
     * @param ycDds               订单
     */
    public void setArrangeCarCancelOrders(ArrangeCarNotifyToBuyerDTO arrangeCarNotifyDTO, List<YcDd> ycDds) {
        if (CollectionUtils.isEmpty(ycDds) || arrangeCarNotifyDTO == null) {
            return;
        }
        List<CancelOrder> cancelOrders = ycDds.stream().map(this::map2CancelOrder).collect(Collectors.toList());
    arrangeCarNotifyDTO.setCancelOrders(cancelOrders);
    }

    /**
     * 转换成取消订单
     *
     * @param ycDd 订单
     * @return 取消订单对象
     */
    private CancelOrder map2CancelOrder(YcDd ycDd) {
        CancelOrder cancelOrder = new CancelOrder();
        cancelOrder.setBuyerOrderNo(ycDd.getCgDdbh());
        cancelOrder.setCpsOrderNo(ycDd.getDdbh());
        cancelOrder.setCost(ycDd.getGyTksxf());
        return cancelOrder;
    }

    public boolean blacklistCheck(String ddbh){
        try {
            YcDd ycDd = ycDdService.selectYcDd(ddbh);
            YcDdMain ycDdMain = ycDdMainService.selectById(ycDd.getpDdbh());
            if (ycDdMain == null
                    || !StringUtils.equals(ycDdMain.getNewOrder(), NEW_ORDER)
                    || StringUtils.equals(THREE_ORDERS, ycDdMain.getOrderType())) {
                return false;
            }
            if(ycDd!=null && StringUtils.equals(ycDd.getDdzt(),"YC2D")){
                if(ycDdEsV2Service.blackListCheck(ycDd.getCksj(),ycDd.getCph())){
                    //司机今天有过拒绝该乘客的情况，取消本单
                    //取消自己
                    BuyerNormalOrderOperateDTO cancelDTO = new BuyerNormalOrderOperateDTO();
                    //商户编号
                    cancelDTO.setCgShbh(ycDd.getCgShbh());
                    //采购取消人
                    cancelDTO.setCpsMainOrderNo(ycDd.getpDdbh());
                    cancelDTO.setCgQxr("CPSADMIN");
                    //采购取消原因
                    cancelDTO.setCgQxyy("乘客取消");
                    //是否强制取消(true或false)默认false
                    cancelDTO.setForce(TRUE);
                    cancelDTO.setDdbh(ycDd.getDdbh());
                    cancelDTO.setReCancel(YES);
                    if(cancelOne(cancelDTO)){
                        // 已取消订单
                        return true;
                    }
                }
            }
        }catch (Exception e){
            logger.error("判断黑名单异常",e);
        }

        return false;

    }
}