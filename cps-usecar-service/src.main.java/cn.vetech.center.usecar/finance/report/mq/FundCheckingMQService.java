package cn.vetech.center.usecar.finance.report.mq;

import cn.vetech.center.reconcile.api.vo.FetchPtdzVO;
import cn.vetech.center.system.mq.entity.MqParam;
import cn.vetech.center.system.mq.entity.MqSend;
import cn.vetech.center.system.mq.service.IMQProducerService;
import cn.vetech.center.usecar.common.enums.CapitalStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.finance.report.FundCheckingService;
import cn.vetech.center.usecar.service.order.YcDdBcdService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.orderes.YcDdEsService;
import cn.vetech.center.usecar.service.usecar.YcKhSjzfxxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.vetech.core.modules.utils.concurrent.VeExecutorServiceFactory;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static cn.vetech.center.usecar.common.enums.UsecarCommonEnum.NO;

/**
 * 资金对账消息服务
 *
 * @author : Y
 * @since 2023/4/23 14:12
 */
@Service
public class FundCheckingMQService {
    /**
     * log
     */
    private Logger logger = LoggerFactory.getLogger(FundCheckingMQService.class);
    /**
     * 消息服务
     */
    @Autowired
    private IMQProducerService mqService;
    /**
     * 资金对账服务
     */
    @Autowired
    private FundCheckingService fundCheckingService;
    /**
     * 用车es服务
     */
    @Autowired
    private YcDdEsService ycDdEsService;
    /**
     * 用车订单服务
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 用车订单服务
    */
    @Autowired
    private YcDdBcdService ycDdBcdService;
    /**
     * 支付记录表
     */
    @Autowired
    private YcKhSjzfxxService ycKhSjzfxxService;
    /**
     * 线程池
     */
    public static final ExecutorService CAR_FUND_CHECKING =
            VeExecutorServiceFactory.newExecuteor(5, 10, 200, "用车资金对账");

    /**
     * 资金对账
     *
     * @param order 订单
     */
    public void sendFundCheckingMessage(Object order) {
        CAR_FUND_CHECKING.submit(() -> sendFundCheckingMessage2(order));
    }
    /**
     * 资金对账
     *
     * @param order 订单
     */
    public void sendFundCheckingMessage2(Object order) {
        MqSend send = new MqSend("pay", "RECONCILE_ORDER_FETCH");
        MqParam param;
        send.setCpbh("1000");
        if (order instanceof YcDdBcd) {
            YcDdBcd fillOrder = YcDdBcd.class.cast(order);
            if (EnumSet.of(CapitalStatusEnum.FAIL, CapitalStatusEnum.SUCCESS).contains(CapitalStatusEnum.getEnum(fillOrder.getCapitalStatus()))) {
                return;
            }
            send.setDdbh(fillOrder.getBcdh());
            send.setDdlx(fillOrder.getDdlx());
            param = getFundCheckingMqParam(fillOrder);
            fillOrder = new YcDdBcd();
            fillOrder.setBcdh(fillOrder.getBcdh());
            fillOrder.setCapitalStatus(NO.getCode());
            ycDdBcdService.updateYcDdBcd(fillOrder);
        } else {
            YcDd normalOrder = YcDd.class.cast(order);
            if (EnumSet.of(CapitalStatusEnum.FAIL, CapitalStatusEnum.SUCCESS).contains(CapitalStatusEnum.getEnum(normalOrder.getCapitalStatus()))) {
                return;
            }
            send.setDdbh(normalOrder.getDdbh());
            send.setDdlx(normalOrder.getDdlx());
            param = getFundCheckingMqParam(normalOrder);
            normalOrder = new YcDd();
            normalOrder.setDdbh(normalOrder.getDdbh());
            normalOrder.setCapitalStatus(NO.getCode());
            ycDdService.updateYcDd(normalOrder);
        }
        if (param == null) {
            return;
        }
        LinkedHashMap<String, MqParam> map = new LinkedHashMap<>();
        map.put("data", param);
        send.setData(map);
        logger.info("用车推送资金平台消息发送对象{}", JsonMapper.nonEmptyMapper().toJson(send));
        mqService.send(send);
    }

    /**
     * 构建mq消息
     *
     * @param ycDdBcd 补差单
     * @return 回参
     */
    public MqParam getFundCheckingMqParam(YcDdBcd ycDdBcd) {
        MqParam param = new MqParam();
        OrderEsVO orderEsVO = ycDdEsService.convertOrderEsVO(ycDdBcd);
        orderEsVO.setOrderType("2");
        Map<String,String> map = new HashMap<>();
        map.put(ycDdBcd.getBcdh(),ycDdBcd.getDdbh());
        FetchPtdzVO fundCheck = fundCheckingService.getFundCheck(orderEsVO,map, new HashSet<>(0));
        if (CollectionUtils.isEmpty(fundCheck.getFzList())) {
            return null;
        }
        param.setValue(JsonMapper.nonEmptyMapper().toJson(fundCheck));
        return param;

    }

    /**
     * 构建mq消息
     *
     * @param order 订单
     * @return 回参
     */
    public MqParam getFundCheckingMqParam(YcDd order) {
        MqParam param = new MqParam();
        OrderEsVO orderEsVO = BeanMapper.map(order, OrderEsVO.class);
        orderEsVO.setOrderType("1");
        Set<String> childPayOrderNoSet = ycKhSjzfxxService.selectChildPayOrderNoSet(Arrays.asList(order.getDdbh()));
        FetchPtdzVO fundCheck = fundCheckingService.getFundCheck(orderEsVO, new HashMap<>(), childPayOrderNoSet);
        if (CollectionUtils.isEmpty(fundCheck.getFzList())) {
            return null;
        }
        param.setValue(JsonMapper.nonEmptyMapper().toJson(fundCheck));
        return param;
    }

    /**
     * 推送资金平台
     *
     * @param orderNo 订单编号
     */
    public void sendFundCheckingMessageByOrderNo(String orderNo) {
        OrderEsVO orderEsVO = ycDdEsService.getCarOrderByOrderNo(orderNo);
        YcDd ycDd = BeanMapper.map(orderEsVO, YcDd.class);
        sendFundCheckingMessage2(ycDd);
    }
}