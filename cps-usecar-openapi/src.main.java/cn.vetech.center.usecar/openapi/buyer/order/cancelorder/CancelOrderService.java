package cn.vetech.center.usecar.openapi.buyer.order.cancelorder;

import cn.vetech.center.system.openapi.IOpenApiService;
import cn.vetech.center.system.openapi.OpenApiException;
import cn.vetech.center.system.openapi.OpenApiLog;
import cn.vetech.center.system.openapi.OpenApiShShbDTO;
import cn.vetech.center.system.openapi.OpenApiShYhbDTO;
import cn.vetech.center.system.openapi.annotation.OpenApiOperation;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarCodeEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.order.buyer.dto.BuyerNormalOrderOperateDTO;
import cn.vetech.center.usecar.order.buyer.dto.CancelResp;
import cn.vetech.center.usecar.order.buyer.service.BuyerOrderService;
import cn.vetech.center.usecar.order.buyer.service.CancelOrderV2Service;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.workorder.WorkOrderService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.Arith;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.util.EnumSet;

/**
 * 采购商自己取消订单
 * 应用场景：采购在订单未支付到CPS之前，可以自己取消订单。
 *
 * @author chenyong
 * @since 2017-11-09
 */
@OpenApiOperation(value = "car_cancelOrder", title = "采购商取消订单")
public class CancelOrderService implements IOpenApiService<CancelOrderRequest, CancelOrderResponse> {
    /**
     * 打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(CancelOrderService.class);

    /**
     * 用车采购订单正常单服务类
     */
    @Autowired
    private BuyerOrderService buyerOrderService;
    /**
     * 用车正常单dao
     */
    @Autowired
     private YcDdService ycDdService;
    /**
     * 取消服务
     */
    @Autowired
    private CancelOrderV2Service cancelOrderV2Service;
    /**
     * 工单服务
     */
    @Autowired
    private WorkOrderService workOrderService;

    @Override
    public CancelOrderResponse execute(CancelOrderRequest request, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) throws OpenApiException {
        openApiLog.add("调用CPS系统采购商取消用车订单接口，请求参数：【" + request + "】");
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        YcDd queryParam = new YcDd();
        openApiLog.setDdbh(StringUtils.isNotBlank(request.getCpsMainOrderNo()) ? request.getCpsMainOrderNo() : request.getDdbh());
        openApiLog.setYwdh(request.getBuyerOrderNo());
        //订单编号
        queryParam.setDdbh(request.getDdbh());
        //商户编号
        queryParam.setCgShbh(request.getBusinessNo());
        if (StringUtils.isNotBlank(request.getBuyerOrderNo()) && StringUtils.isBlank(request.getDdbh())) {
            YcDd ycDd = ycDdService.selectByBuyerOrderNo(request.getBuyerOrderNo(), request.getBusinessNo());
            if (ycDd != null) {
                logger.info("没有传cps订单编号，获取cps订单编号{}", ycDd.getDdbh());
                request.setDdbh(ycDd.getDdbh());
                queryParam.setDdbh(ycDd.getDdbh());
            }
            openApiLog.setYwdh(request.getBuyerOrderNo());
        }
        //采购取消原因
        request.setCgQxyy(StringUtils.defaultIfBlank(request.getCgQxyy(),"行程变更，申请取消！"));
        BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO = new BuyerNormalOrderOperateDTO();
        //订单编号
        buyerNormalOrderOperateDTO.setDdbh(request.getDdbh());
        //商户编号
        buyerNormalOrderOperateDTO.setCgShbh(request.getBusinessNo());
        //采购取消人
        String cgqxr = request.getQxr();
        buyerNormalOrderOperateDTO.setCpsMainOrderNo(request.getCpsMainOrderNo());
         if (StringUtils.isEmpty(cgqxr)) {
            cgqxr = request.getUserId();
        }
        if (StringUtils.isEmpty(cgqxr)) {
            cgqxr = "NULL";
        }
        buyerNormalOrderOperateDTO.setSourceData(JsonMapper.nonEmptyMapper().toJson(request));
        buyerNormalOrderOperateDTO.setCgQxr(cgqxr);
        buyerNormalOrderOperateDTO.setBzcgs(request.getBzcgs());
        //采购取消原因
        buyerNormalOrderOperateDTO.setCgQxyy(request.getCgQxyy());
        //是否强制取消(true或false)默认false
        buyerNormalOrderOperateDTO.setForce(request.getForce());
        //自动化测试
        buyerNormalOrderOperateDTO.setAutotest(request.getAutotest());
        /**Step1：调用CPS本地service取消CPS订单**/
        Boolean result = Boolean.FALSE;
        openApiLog.add("调用CPS系统采购商取消用车订单接口请求后台参数：【" + buyerNormalOrderOperateDTO + "】");
        try {
            if (StringUtils.isNotBlank(request.getCpsMainOrderNo())) {
                CancelResp resp =  cancelOrderV2Service.cancelOrderV2(buyerNormalOrderOperateDTO);
                if (!resp.isSuccess()) {
                    cancelOrderResponse.setStatus(UsecarCodeEnum.FAIL.getCode());
                    cancelOrderResponse.setErrorCode(UsecarOrderCode.UCAR_10010.getCode());
                    cancelOrderResponse.setErrorMessage(UsecarOrderCode.UCAR_10010.getMessage());
                    throw new OpenApiException(UsecarOrderCode.UCAR_10010, "返回状态码:" + UsecarCodeEnum.FAIL.getCode());
                } else {
                    cancelOrderResponse.setStatus(UsecarCodeEnum.OK.getCode());
                    cancelOrderResponse.setCost(Arith.add(resp.getCost(), BigDecimal.ZERO).toPlainString());
                    cancelOrderResponse.setSfqx("true");
                    return cancelOrderResponse;
                }
            } else {
                result = buyerOrderService.cancelOrder(buyerNormalOrderOperateDTO);
            }
        } catch (Exception e) {
           YcDd ycDd = ycDdService.selectYcDd(request.getDdbh());
            //解决供应重复取消报错问题,重复取消,返回采购也是取消成功
            if (ycDd != null && EnumSet.of(UsecarOrderStatusEnum.YC3D, UsecarOrderStatusEnum.YC1G, UsecarOrderStatusEnum.YC3C,
                    UsecarOrderStatusEnum.YC2A, UsecarOrderStatusEnum.YC2C, UsecarOrderStatusEnum.YC2B,
                    UsecarOrderStatusEnum.YC1E, UsecarOrderStatusEnum.YC2M, UsecarOrderStatusEnum.YC2O,
                    UsecarOrderStatusEnum.YC2P, UsecarOrderStatusEnum.YC1B, UsecarOrderStatusEnum.YC1D,
                    UsecarOrderStatusEnum.YC1E).contains(UsecarOrderStatusEnum.getEnum(ycDd.getDdzt()))) {
                cancelOrderResponse.setStatus(UsecarCodeEnum.OK.getCode());
                cancelOrderResponse.setCost(ycDd.getGyTksxf() == null ? "0" : ycDd.getGyTksxf().toPlainString());
                cancelOrderResponse.setSfqx("true");
            }
            logger.error("取消异常", e);
        }
        openApiLog.add("调用CPS系统采购商取消用车订单接口，返回结果：【" + result + "】");
        YcDd ycDd = ycDdService.selectYcDd(request.getDdbh());
        if (!result) {
            cancelOrderResponse.setStatus(UsecarCodeEnum.FAIL.getCode());
            cancelOrderResponse.setErrorCode(UsecarOrderCode.UCAR_10010.getCode());
            cancelOrderResponse.setErrorMessage(UsecarOrderCode.UCAR_10010.getMessage());
            throw new OpenApiException(UsecarOrderCode.UCAR_10010, "返回状态码:" + UsecarCodeEnum.FAIL.getCode());
        }else{
            cancelOrderResponse.setStatus(UsecarCodeEnum.OK.getCode());
            cancelOrderResponse.setCost(ycDd.getGyTksxf()==null?"0":ycDd.getGyTksxf().toPlainString());
            cancelOrderResponse.setSfqx("true");
        }
        if (Arith.add(ycDd.getGpccs(),BigDecimal.ZERO).compareTo(BigDecimal.ZERO)>0
                &&StringUtils.equals("自动取消",ycDd.getCgQxyy()) ) {
            workOrderService.resendWorkOrder(ycDd);
        }
        openApiLog.add("调用CPS系统采购商取消用车订单接口返回参数：【" + cancelOrderResponse + "】");
        return cancelOrderResponse;
    }
}