package cn.vetech.center.usecar.openapi.buyer.order.createorder;


import cn.vetech.center.system.openapi.IOpenApiService;
import cn.vetech.center.system.openapi.OpenApiException;
import cn.vetech.center.system.openapi.OpenApiLog;
import cn.vetech.center.system.openapi.OpenApiShShbDTO;
import cn.vetech.center.system.openapi.OpenApiShYhbDTO;
import cn.vetech.center.system.openapi.annotation.OpenApiOperation;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarCodeEnum;
import cn.vetech.center.usecar.coupon.CouponConsumeService;
import cn.vetech.center.usecar.listener.CpsEventPublisher;
import cn.vetech.center.usecar.listener.entity.CpsEventEnum;
import cn.vetech.center.usecar.risk.RiskMamageCenterService;
import cn.vetech.center.usecar.service.unpay.YcUnpayLimitLogicService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vetech.core.api.Code;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import static cn.vetech.center.usecar.common.UseCarConstant.CREATE_ORDER_V2;
import static cn.vetech.center.usecar.common.UseCarConstant.YES;
import static cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum.SFC;

/**
 * 预订下单
 * 应用场景：采购下单时调用 ,将订单入库到CPS订单表
 *
 * @author chenyong
 * @since 2017-11-09
 */
@OpenApiOperation(value = "car_createOrder", title = "采购预订下单")
public class CreateOrderService implements IOpenApiService<CreateOrderRequest, CreateOrderResponse> {
    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(CreateOrderService.class);
    /**
     * 公用service
     */
    @Autowired
    private BuyerBookCommonService commService;
    /**
     * v1 服务
     */
    @Autowired
    private CreateOrderV1Service createOrderV1Service;
    /**
     * v2 服务
     */
    @Autowired
    private CreateOrderV2Service createOrderV2Service;
    /**
     * 未付订单检查
     */
    @Autowired
    private YcUnpayLimitLogicService ycUnpayLimitLogicService;

    @Autowired
    private CpsEventPublisher cpsEventPublisher;

    @Autowired
    private CouponConsumeService couponConsumeService;

    @Autowired
    private RiskMamageCenterService riskMamageCenterService;

    @Override
    public CreateOrderResponse execute(CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO,
                                       OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) throws OpenApiException {
        openApiLog.setYwdh(request.getCgDdbh());
        logger.info("临时记录下，就是为了看到底是不是进来了(对比用)....>>" + "shdto=" + JsonMapper.nonEmptyMapper().toJson(openApiShShbDTO) + "\r\nshyhdto=" + JsonMapper.nonEmptyMapper().toJson(openApiShYhbDTO));
        logger.info("下单业务参数：" + request);
        CreateOrderResponse response = new CreateOrderResponse();
        if (request != null) {
            //检查乘客是否在用车黑名单里
            boolean checkFlag = commService.checkYcHmd(request.getCkxm(), request.getCksj());
            if (!checkFlag) {
                logger.info("{}在黑名单中，不允许下单", request.getCkxm());
                return error(UsecarOrderCode.UCAR_10001);
            }

            if(StringUtils.equalsAny(request.getDdly(), UseCarConstant.B2C,UseCarConstant.CPSC_PC)){
                boolean b = riskMamageCenterService.cpscCancelOrderCountRiskControl(request.getMemberId(),request.getChannelId(),request.getLxrdh());
                if(b){
                    logger.info("{}在黑名单中，不允许下单", request.getCkxm());
                    return error(UsecarOrderCode.UCAR_10001);
                }
            }
            if(!StringUtils.equals(request.getIsCharCar(),YES) || (StringUtils.isBlank(request.getXjdh()) && StringUtils.isBlank(request.getBjdh()))){
                UsecarOrderCode code = ycUnpayLimitLogicService.checkUnpayOrder(openApiShShbDTO.getShbh(), request.getCkxm());
                if (code != null) {
                    return error(code);
                }
            }
            if(StringUtils.equals(request.getIsCharCar(),YES)){
                String ddbh = createOrderV1Service.simpleCreateOrder(request, openApiShShbDTO, openApiShYhbDTO);
                response.setDdbh(ddbh);
                return response;
            }
            if (StringUtils.equals(request.getNewOrder(), CREATE_ORDER_V2)) {
                response = createOrderV2Service.createOrder(request, openApiShShbDTO, openApiShYhbDTO, openApiLog);
                couponConsumeService.freezeCoupon(response.getGysMddbh());
                return response;
            }
            if (StringUtils.isNotBlank(request.getDdlx()) && ("10000501".equals(request.getDdlx()) || SFC.getCode().equals(request.getDdlx()))) {
                openApiLog.add("**进入专快车下单到CPS模块**");
                response = createOrderV1Service.specialCarSubmitOrderToCps(request, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            } else {
                openApiLog.add("**进入接送车下单到CPS模块**");
                response = createOrderV1Service.shuttleCarSubmitOrderToCps(request, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            }
            couponConsumeService.freezeCoupon(response.getGysMddbh());
        } else {
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
        }
        cpsEventPublisher.send(CpsEventEnum.SH_CONFIG_CHECK,response.getDdbh());
        openApiLog.setDdbh(response.getDdbh());
        return response;
    }

    public static CreateOrderResponse error(Code code) {
        CreateOrderResponse response = new CreateOrderResponse();
        response.setStatus(UsecarCodeEnum.FAIL.getCode());
        response.setErrorCode(code.getCode());
        response.setErrorMessage(code.getMessage());
        return response;
    }

}