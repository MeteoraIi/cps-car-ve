package cn.vetech.center.usecar.workorder;

import cn.vetech.center.base.api.dto.GdDdbCzDTO;
import cn.vetech.center.base.api.dto.GdDdbInsertRecordDTO;
import cn.vetech.center.customer.api.vo.ShShbVO;
import cn.vetech.center.usecar.apiclient.base.IGdDdbServiceClient;
import cn.vetech.center.usecar.apiclient.customer.IShShbServiceClient;
import cn.vetech.center.usecar.common.enums.CarLevelEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarXxidEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.ordermq.YcXxMdCommonService;
import cn.vetech.center.usecar.workorder.enums.MonitoryPointEnum;
import cn.vetech.center.usecar.workorder.enums.ProductSubclassEnum;
import cn.vetech.center.usecar.workorder.enums.TaskNoEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.time.DateUtil;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.UseCarConstant.CPBH;

/**
 * 用车工单服务
 *
 * @author : yanguowei
 * 2020/1/19 10:50
 */
@Service
public class WorkOrderService {

    /**
     * 日志信息
     */
    private final Logger logger = LoggerFactory.getLogger(WorkOrderService.class);

    /**
     * 工单服务
     */
    @Autowired
    private IGdDdbServiceClient iGdDdbServiceClient;
    /**
     * 数据库订单服务
     */
   @Autowired
    private YcDdService ycDdService;
    /**
     * 产品名称
     */
    private static final String CPMC = "用车";
    /**
     * 6
     */
    private static final int SIX = 6;

    /**
     * 查询商户信息
     */
    @Autowired
    private IShShbServiceClient iShShbServiceClient;

    /**
     * 已预订待支付 查询最近21分钟数据
     */
    private static final int BOOKED_PAYMENT_BEFORE_MINUTES = -21;

    /**
     * 已取消无需退款 查询最近11分钟数据
     */
    private static final int CANCELLED_NO_REFUND_BEFORE_MINUTES = -11;
    /**
     * 已支付待派车 查询最近21分钟数据
     */
    private static final int PAID_WAIT_DISPATCH = -21;

    /**
     * 已拒单已退款 查询最近11分钟数据
     */
    private static final int REJECTED_REFUNDED_BEFORE_MINUTES = -11;
    /**
     * 已拒单待退款 查询最近121分钟数据
     */
    private static final int REFUSED_WAIT_REFUND_BEFORE_MINUTES = -121;

    /**
     * 格式化
     */
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 格式化
     */
    private static final String TIME_MIN_FORMAT = "yyyy-MM-dd HH:mm";
    /**
     * 已用车-待分账 查询最近121分钟数据
     */
    private static final int USED_CAR_WAIT_SPLIT_BEFORE_MINUTES = -121;
    /**
     * 已用车-未支付 查询最近48小时数据
     */
    private static final int USED_CAR_UNPAID_BEFORE_HOUR = -48;
    /**
     * 预约用车派车提醒时间
     */
    private static final int USED_CAR_PREDICT_BEFORE_HOUR = -1;
    /**
     * 已申请-待审核
     * 每10分钟执行一次查询21分钟内数据
     */
    private static final int APPLIED_WAIT_APPROVAL_BEFORE_MINUTES = -21;
 
    /**
     * 已申请-待审核 查询最近121分钟数据
     */
    private static final int APPROVAL_WAIT_REFUND_BEFORE_MINUTES = -121;

    /**
     * start
     */
    private static final int ADD_START_TIME = 40;

    /**
     * end
     */
    private static final int ADD_END_TIME = 30;
    /**
     * 用车消息埋点
     */
    @Autowired
    private YcXxMdCommonService ycXxMdCommonService;


    /**
     * 已预订待支付
     * 每10分钟执行一次查询21分钟内数据
     */
    public void bookedPayment() {
        //截止时间
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, BOOKED_PAYMENT_BEFORE_MINUTES), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectBookedPayment(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_001, TaskNoEnum.GDRQ_CAR_001);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("bookedPayment,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已支付-待派车
     * 每10分钟执行一次查询21分钟内数据
     */
    public void paidWaitDispatch() {
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, PAID_WAIT_DISPATCH), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectPaidWaitDispatch(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_003, TaskNoEnum.GDRQ_CAR_003);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("paidWaitDispatch,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已拒单 已退款
     * 每10分钟执行一次查询11分钟内数据
     */
 public void rejectedRefunded() {
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, REJECTED_REFUNDED_BEFORE_MINUTES), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectRejectedRefunded(time);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_004, TaskNoEnum.GDRQ_CAR_004);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("rejectedRefunded,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已拒单待退款
     * 每小时执行一次查询121分钟内数据
     */
    public void refusedWaitRefund() {
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, REFUSED_WAIT_REFUND_BEFORE_MINUTES), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectRefusedWaitRefund(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_005, TaskNoEnum.GDRQ_CAR_005);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("refusedWaitRefund,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已派车待用车 提示司机重要
     * 每5分钟执行一次 查询30分钟到40分钟用车的订单
     */
    public void dispatchWaitUseCar() {
        Date now = VeDate.getNow();
        String startTime = VeDate.formatToStr(DateUtil.subMinutes(now, ADD_START_TIME), TIME_MIN_FORMAT);
        String endTime = VeDate.formatToStr(DateUtil.subMinutes(now, ADD_END_TIME), TIME_MIN_FORMAT);
        List<YcDd> orderList = ycDdService.selectDispatchWaitUseCar(startTime, endTime);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_006, TaskNoEnum.GDRQ_CAR_006);
            ycXxMdCommonService.usecarXxmdMqSend(UsecarXxidEnum.XX_CAR_0019, order.getDdbh(), order.getDdlx(), "",order.getCgShbh());
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("dispatchWaitUseCar,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }
    /**
     * 已派车待用车 提示司机重要
     * 每5分钟执行一次 查询30分钟到40分钟用车的订单
     */
    public void dispatchWaitUseCarTwo() {
        Date now = VeDate.getNow();
        String startTime = VeDate.formatToStr(DateUtil.subMinutes(now, ADD_START_TIME), TIME_MIN_FORMAT);
        String endTime = VeDate.formatToStr(DateUtil.subMinutes(now, ADD_END_TIME), TIME_MIN_FORMAT);
        List<YcDd> orderList = ycDdService.selectDispatchWaitUseCarTwo(startTime, endTime);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_206, TaskNoEnum.GDRQ_CAR_206);
            ycXxMdCommonService.usecarXxmdMqSend(UsecarXxidEnum.XX_CAR_0019, order.getDdbh(), order.getDdlx(), "",order.getCgShbh());
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("dispatchWaitUseCar,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }


    /**
     * 订单带有需求备注,下单成功实时进工单
     *
     * @param ddbh ddbh
     */
    public void createOrderHaveRemark(String ddbh) {
        YcDd order = ycDdService.selectYcDd(ddbh);
        if (order == null) {
            return;
        }
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_013, TaskNoEnum.GDRQ_CAR_013);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
        logger.info("createOrderHaveRemark,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
    }

    /**
     * 已派车待用车 预约用车出行提醒
     * 每5分钟执行一次 查询30分钟到40分钟用车的订单
     */
    public void dispatchWaitScheduleUseCar() {
        Date now = VeDate.getNow();
        String startTime = VeDate.formatToStr(DateUtil.subMinutes(now, ADD_START_TIME), TIME_MIN_FORMAT);
        String endTime = VeDate.formatToStr(DateUtil.subMinutes(now, ADD_END_TIME), TIME_MIN_FORMAT);
        List<YcDd> orderList = ycDdService.selectDispatchWaitScheduleUseCar(startTime, endTime);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_0012, TaskNoEnum.GDRQ_CAR_0012);
            ycXxMdCommonService.usecarXxmdMqSend(UsecarXxidEnum.XX_CAR_0018, order.getDdbh(), order.getDdlx(), "",order.getCgShbh());
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("dispatchWaitScheduleUseCar,ddbh:{},res:{}", order.getDdbh(), JsonMapper.defaultMapper().toJson(resp));
        });
    }
    /**
     * 已派车待用车 预约用车出行提醒
     * 每5分钟执行一次 查询30分钟到40分钟用车的订单
     */
    public void dispatchWaitScheduleUseCarTwo() {
        Date now = VeDate.getNow();
        String startTime = VeDate.formatToStr(DateUtil.addMinutes(now, ADD_START_TIME), TIME_MIN_FORMAT);
        String endTime = VeDate.formatToStr(DateUtil.addMinutes(now, ADD_END_TIME), TIME_MIN_FORMAT);
        List<YcDd> orderList = ycDdService.selectDispatchWaitScheduleUseCarTwo(startTime, endTime);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_0212, TaskNoEnum.GDRQ_CAR_0212);
           ycXxMdCommonService.usecarXxmdMqSend(UsecarXxidEnum.XX_CAR_0018, order.getDdbh(), order.getDdlx(), "",order.getCgShbh());
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("dispatchWaitScheduleUseCar,ddbh:{},res:{}", order.getDdbh(), JsonMapper.defaultMapper().toJson(resp));
        });
    }

    /**
     * 已用车-待分账
     * 每小时执行一次查询121分钟内数据
     */
    public void usedCarWaitSplit() {
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, USED_CAR_WAIT_SPLIT_BEFORE_MINUTES), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectUsedCarWaitSplit(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_007, TaskNoEnum.GDRQ_CAR_007);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("usedCarWaitSplit,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已用车-未支付
     * 每天执行一次,查询48小时内数据
     */
    public void usedCarUnpaid() {
        //截止时间
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addHours(now, USED_CAR_UNPAID_BEFORE_HOUR), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectUsedCarUnpaid(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_008, TaskNoEnum.GDRQ_CAR_008);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("usedCarUnpaid,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }
    /**
     * 高管已派车订单
     * 预约用车，出发前一小时提醒
     */
     public void leaderCarDispatched() {
        //截止时间
        Date now = VeDate.getNow();
        List<YcDd> orderList = ycDdService.selectLeaderCarDispatched(now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        // and length(ycsj) > 10 and DATE_ADD(#{now},INTERVAL  1 HOUR)>str_to_date(ycsj,'%Y-%m-%d %H:%i:%s')
        List<YcDd> dispatchList = new ArrayList<>();
        for(YcDd carOrder:orderList){
            if(!StringUtils.contains(carOrder.getYcsj(),":")){
                continue;
            }
            Date carUseTime = VeDate.strToDateLong(carOrder.getYcsj()+":00");
            if(VeDate.getTwoMin(carUseTime,VeDate.getNow())<60){
                continue;
            }
            dispatchList.add(carOrder);
        }
        if(CollectionUtils.isEmpty(dispatchList)){
            return;
        }
        dispatchList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_302, TaskNoEnum.GDRQ_CAR_302);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("leaderCarDispatched,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }
    /**
     * 高管已派车订单
     * 已用车已分账5分钟后通知
     */
    public void leaderCarFinished() {
        //截止时间
        Date now = VeDate.getNow();
        List<YcDd> orderList = ycDdService.selectLeaderCarFinished(now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_301, TaskNoEnum.GDRQ_CAR_301);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("leaderCarFinished,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }
    /**
     * 高管待派车订单
     * 已支付待派车工单
     */
  public void leaderCarWaitDispatched() {
        //截止时间
        Date now = VeDate.getNow();
        List<YcDd> orderList = ycDdService.selectLeaderCarWaitDispatch(now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_303, TaskNoEnum.GDRQ_CAR_303);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("leaderCarWaitDispatched,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已申请-待审核
     * 每10分钟执行一次查询21分钟内数据
     */
    public void appliedWaitApproval() {
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, APPLIED_WAIT_APPROVAL_BEFORE_MINUTES), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectAppliedWaitApproval(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_009, TaskNoEnum.GDRQ_CAR_009);
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("appliedWaitApproval,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * 已申请-待退款
     * 每小时执行一次查询121分钟内数据
     */
    public void approvalWaitRefund() {
        Date now = VeDate.getNow();
        String time = VeDate.formatToStr(DateUtil.addMinutes(now, APPROVAL_WAIT_REFUND_BEFORE_MINUTES), TIME_FORMAT);
        List<YcDd> orderList = ycDdService.selectApprovalWaitRefund(time, now);
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        orderList.forEach(order -> {
            GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_010, TaskNoEnum.GDRQ_CAR_010);
  RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("approvalWaitRefund,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
        });
    }

    /**
     * @param order         订单
     * @param monitoryPoint 监控点枚举
     * @param taskNo        任务编号说明
     * @return 工单请求参数
     */
    public GdDdbInsertRecordDTO buildGdDdbInsertRecordDTO(YcDd order, MonitoryPointEnum monitoryPoint, TaskNoEnum taskNo) {
        if (order == null) {
            return null;
        }
        GdDdbInsertRecordDTO dto = new GdDdbInsertRecordDTO();
        dto.setCpbh(CPBH);
        dto.setCpmc(CPMC);
        String ddlx = order.getDdlx();
        String cpzlbh = StringUtils.isEmpty(ddlx) ? "" : ddlx.substring(0, SIX);
        dto.setCpzlbh(cpzlbh);
        dto.setCpzlmc(StringUtils.isEmpty(cpzlbh) ? "" : ProductSubclassEnum.Holder.parse(cpzlbh).getMessage());
        dto.setDdlxbh(order.getDdlx());
        dto.setDdlxmc(UsecarProductTypeEnum.toEnum(order.getDdlx()).getMessage());
        if (UsecarOrderStatusEnum.YC2A.getCode().equals(order.getDdzt()) && order.getCgJsje().doubleValue() == 0d) {
            dto.setDdztbh(UsecarOrderStatusEnum.YC2C.getOrderStatusCode());
            dto.setDdztmc(UsecarOrderStatusEnum.YC2C.getCpsOrderStatus());
        } else {
            dto.setDdztbh(order.getDdzt());
            dto.setDdztmc(UsecarOrderStatusEnum.getCpsOrderStatus(order.getDdzt()));
        }
        dto.setCfcs(order.getCfdCsmc());
        dto.setTkno(order.getSjdh());
        dto.setDdbh(order.getDdbh());
        dto.setYdrxm(order.getLxr());
        dto.setYdrlxdh(order.getLxrdh());
        dto.setCxrxm(order.getCkxm());
        dto.setCxrlxdh(order.getCksj());
        dto.setXcxx(buildTravelInfo(order));
        dto.setLxrxm(order.getLxr());//
        dto.setLxrdh(order.getLxrdh());//
        dto.setShlx("102408".equals(order.getShxz()) ? "2" : "1");
         dto.setCgsbh(order.getCgShbh());
        dto.setCgsmc(order.getCgShjc());
        RestResponse<ShShbVO> cgShShbVO = iShShbServiceClient.getShbById(order.getCgShbh());
        dto.setCgslxdh(cgShShbVO.getResult().getLxrsj());
        dto.setGysbh(order.getGyShbh());
        dto.setGysmc(order.getGyShjc());
        RestResponse<ShShbVO> gyShShbVO = iShShbServiceClient.getShbById(order.getGyShbh());
        String gyslxdh = gyShShbVO.getResult().getLxrsj();
        dto.setGyslxdh(gyslxdh);
        dto.setFwsbh(order.getFwsbh());
        dto.setFwsmc(order.getFwsmc());
        dto.setFwslxdh("");
        dto.setYgbzjb("");
        dto.setJkdbh(monitoryPoint.getCode());
        dto.setJkdmc(monitoryPoint.getMessage());
        dto.setJkdsm(monitoryPoint.getInstruction());
        dto.setGdlxbh(taskNo.getCode());
        dto.setGdlxmc(taskNo.getMessage());
        dto.setJdlxdh("");
        if (StringUtils.isNotBlank(order.getFwbzjb())) {
            dto.setYgbzjbid(order.getFwbzjb());
            dto.setYgbzjbbh(CarLevelEnum.getLevelByCode(order.getFwbzjb()));
        }
        if (StringUtils.isNotBlank(order.getYcsj()) && order.getYcsj().length() > 10) {
            dto.setCfrq(VeDate.formatToDate(order.getYcsj(), "yyyy-MM-dd HH:mm"));
        } else {
            dto.setCfrq(order.getXdsj());
        }
        dto.setDdrq(order.getGyFwwcsj());
        String orderContact = getOrderContact(order, taskNo, gyslxdh);
        dto.setWhhm(orderContact);
        return dto;
    }

    /**
     * @param order   订单
     * @param taskNo  taskNo
     * @param gyslxdh gyslxdh
     * @return 订单联系人
     */
    private String getOrderContact(YcDd order, TaskNoEnum taskNo, String gyslxdh) {
        if (EnumSet.of(TaskNoEnum.GDRQ_CAR_001, TaskNoEnum.GDRQ_CAR_002, TaskNoEnum.GDRQ_CAR_008).contains(taskNo)) {
            return order.getLxrdh();
       } else if (EnumSet.of(TaskNoEnum.GDRQ_CAR_003, TaskNoEnum.GDRQ_CAR_004).contains(taskNo)) {
            return gyslxdh;
        } else if (TaskNoEnum.GDRQ_CAR_006.equals(taskNo)) {
            return order.getSjdh();
        } else {
            return null;
        }
    }

    /**
     * @param order 订单
     * @return 行程信息
     */
    private String buildTravelInfo(YcDd order) {
        StringBuilder traverlInfo = new StringBuilder();
        traverlInfo.append(order.getYcsj());
        traverlInfo.append(order.getCfd());
        traverlInfo.append("-");
        traverlInfo.append(order.getMdd());
        return traverlInfo.toString();
    }

    /**
     * 结束工单
     *
     * @param order         order
     * @param monitoryPoint monitoryPoint
     * @param taskNo        taskNo
     * @return GdDdbCzDTO
     */
    private GdDdbCzDTO buildGdDdbCzDTO(YcDd order, MonitoryPointEnum monitoryPoint, TaskNoEnum taskNo) {
        GdDdbCzDTO dto = new GdDdbCzDTO();
        dto.setCpbh(CPBH);
        String ddlx = order.getDdlx();
        String cpzlbh = StringUtils.isEmpty(ddlx) ? "" : ddlx.substring(0, SIX);
        dto.setCpzlbh(cpzlbh);
        dto.setDdlxbh(order.getDdlx());
        dto.setDdztbh(order.getDdzt());
        dto.setDdbh(order.getDdbh());
        dto.setJkdbh(monitoryPoint.getCode());
        dto.setGdlxbh(taskNo.getCode());
        List<String> jkdbhList = new ArrayList<>();
        jkdbhList.add(monitoryPoint.getCode());
        dto.setJkdbhList(jkdbhList);
        return dto;
    }

    /**
     * 改派进工单
     *
     * @param ddbh 订单编号
     */
    public void changeCar(String ddbh) {
        YcDd order = ycDdService.selectYcDd(ddbh);
        if (order == null) {
            return;
        }
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_014, TaskNoEnum.GDRQ_CAR_014);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
       logger.info("changeCar,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
    }
    /**
     * 已派车拒单进工单
     *
     * @param ddbh 订单编号
     */
    public void carRefusedAfterAssign(String ddbh) {
        YcDd order = ycDdService.selectYcDd(ddbh);
        if (order == null) {
            return;
        }
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_033, TaskNoEnum.GDRQ_CAR_033);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
        logger.info("carRefusedAfterAssign,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
    }
    /**
     * 高管已派车拒单进工单
     *
     * @param ddbh 订单编号
     */
    public void leaderCarRefusedAfterAssign(String ddbh) {
        YcDd order = ycDdService.selectYcDd(ddbh);
        if (order == null) {
            return;
        }
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_332, TaskNoEnum.GDRQ_CAR_332);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
        logger.info("leaderCarRefusedAfterAssign,ddbh:{},res:{}", order.getDdbh(), resp.getResult());
    }

    /**
     *  取消失败进工单
     * @param ddbh
     */
    public void cancelFailedAssign(String ddbh){
        YcDd order = ycDdService.selectYcDd(ddbh);
        if (order == null) {
            return;
        }
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_039, TaskNoEnum.GDRQ_CAR_039);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
        logger.info("创建工单响应={}",JsonMapper.nonEmptyMapper().toJson(resp));
    }

    /**
     * 采购商通知地址异常，并发起工单
     */
    public void shconfigErrorAssign(YcDd order){
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_040, TaskNoEnum.GDRQ_CAR_040);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
        logger.info("创建工单响应={}",JsonMapper.nonEmptyMapper().toJson(resp));
    }

    /**
     * 预约用车，临近用车时间发起工单提醒
     */
    public void yyycNoticeAssign(YcDd order){
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_041, TaskNoEnum.GDRQ_CAR_041);
        RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
        logger.info("创建工单响应={}",JsonMapper.nonEmptyMapper().toJson(resp));
    }
    /**
     * 预约用车，临近用车时间发起工单提醒
     */
    public void resendWorkOrder(YcDd order) {
        GdDdbInsertRecordDTO dto = buildGdDdbInsertRecordDTO(order, MonitoryPointEnum.CAR_053, TaskNoEnum.GDRQ_CAR_053);
        try {
            RestResponse<Boolean> resp = iGdDdbServiceClient.insertRecord(dto);
            logger.info("预约用车，临近用车时间发起工单提醒={}", JsonMapper.nonEmptyMapper().toJson(resp));
        } catch (Exception e) {
            logger.error("临近用车时间发起工单提醒异常", e);
        }
    }
}
