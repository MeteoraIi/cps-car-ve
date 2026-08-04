/**
 * Title: UsecarOrderStatusEnum.java
 * Package cn.vetech.center.charcar.communal.enums;
 * Description: 包车订单状态枚举类和ve_ddzt表保持一致
 *
 * @author vetech
 * date 2017-8-16 下午10:30:11
 */
package cn.vetech.center.usecar.common.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.vetech.core.api.ICode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UsecarOrderStatusEnum implements ICode {
    /***格式：CPS订单状态编号,[A系统订单状态显示,采购订单状态显示,供应订单状态显示]*/
    /**
     * 已预订
     */
    YC1A("YC1A", "已预订", "已预订，待接单", "已预订，待接单", ""),
    /**
     * 供应接单超时自动取消
     */
    YC1B("YC1B", "超时取消", "超时取消", "超时取消", ""),
    /**
     * 供应商接单(供应商接单后如果采购申请退单，需要供应商审核退款申请)
     */
    YC1C("YC1C", "已预订，待支付", "已预订，待支付", "已预订，待支付", "red"),
    /**
     * 支付超时自动取消
     */
    YC1D("YC1D", "支付超时取消", "支付超时订单取消", "支付超时取消", ""),
    /**
     * 支付前采购取消(支付前取消订单直接取消订单)
     */
    YC1E("YC1E", "未支付，已取消", "未支付，已取消", "未支付，已取消", ""),
    /**
     * 采购商下单(供应商未接单，采购未支付)
     */
    YC1F("YC1F", "已支付，待派车", "已支付，待派车", "已支付，待派车", "purple"),
    /**
     * 采购付款前供应拒单
     */
    YC2A("YC2A", "已拒单", "已拒单", "已拒单", ""),
    /**
     * 供应拒单后平台成功退款
     */
    YC2C("YC2C", "已拒单，已退款", "已拒单，已退款", "已拒单", ""),
    /**
     * 平台退款失败
     */
    YC2B("YC2B", "已拒单，待退款", "已拒单，待退款", "已拒单", "blue"),
    /**
     * 供应商已派车（"1.采购商确认服务完成，系统自动分账。2.供应商确认服务完成，系统不自动分账由平台线下确认并手动分账"）
     */
    YC2D("YC2D", "已派车", "已派车", "已派车", "blue"),
    /**
     * "1.提醒客户相关取消费用并让客户选择是否确认申请退单(提醒客户最终取消费用供应商确认为准)2.已支付派车前退单申请，供应同意后自动解冻退款给采购商"
     */
    YC3A("YC3A", "已申请，待审核", "已申请，待审核", "已申请，待审核", ""),
    /**
     * 神州预约用车订单，当天没有派车的，需要先确定订单
     */
    YC2F("YC2F", "已确定，待派车", "已确定，待派车", "已确定，待派车", "blue"),
    /**
     * 司机已出发
     */
    YC2G("YC2G", "司机已出发", "司机已出发", "司机已出发", ""),
    /**
     * 司机已就位
     */
    YC2H("YC2H", "司机已就位", "司机已就位", "司机已就位", ""),
    /**
     * 有违约，待支付
     */
    YC2M("YC2M", "有违约，待支付", "有违约，待支付", "有违约，待支付", ""),
    /**
     * 有违约，待分账
     */
    YC2O("YC2O", "有违约，待分账", "有违约，待分账", "有违约，待分账", ""),
    /**
     * 有违约，已分账
     */
    YC2P("YC2P", "有违约，已分账", "有违约，已分账", "有违约，已分账", ""),
    /**
     * 运营退款(退款完成后系统自动调执行分账，分账失败)
     */
    YC3C("YC3C", "已审核，待退款", "已审核，待退款", "已审核，待退款", ""),
    /**
     * 供应商确认完成("1.自动分账分账失败的情况2.供应商确认完成代分账这里由平台操作分账。")
     */
    YC4A("YC4A", "已用车，待分账", "已用车", "已用车，待分账", "purple"),
    /**
     * 分账完成
     */

    YC4B("YC4B", "已用车，已分账", "已用车", "已用车，已分账", "#FF33CC"),
    /**
     * 运营分账(退款后自动分账成功或者手动分账成功)
     */
    YC3D("YC3D", "已审核，已退款", "已审核，已退款", "已审核，已退款", ""),
    /**
     * 供应方的司机在接到乘客时，更新的状态
     */
    YC2E("YC2E", "已上车", "已上车", "已上车", "#009688"),

    /**
     * 采购商使用超出了双倍预付金额,拆单的订单状态
     */
    YC4C("YC4C", "已用车，未支付", "已用车，未支付", "已用车，未支付", ""),

    /**
     * 采购商使用超出了双倍预付金额,拆单的订单状态
     */
    YC1G("YC1G", "已拒单", "已拒单", "已拒单", ""),
    /**
     * 未支付，待派车
     */
    YC1H("YC1H", "未支付，待派车", "未支付，待派车", "未支付，待派车", ""),

    /**
     * 滴滴状态，供应商接单后无法联系用车人，司机取消订单后的状态
     */
    YC2N("YC2N", "行程异常结束", "行程异常结束", "行程异常结束", "");

    /****订单状态****/
    private String orderStatusCode;
    /****CPS-A系统订单状态名称****/
    private String cpsOrderStatus;
    /****CPS-b-采购系统订单状态名称****/
    private String purchaseOrderStatus;
    /****CPS-b-供应系统订单状态名称****/
    private String supplyOrderStatus;
    /**
     * html颜色
     */
    private String color;

    /**
     * 包车订单状态枚举构造类
     *
     * @param orderStatusCode     订单状态
     * @param cpsOrderStatus      *CPS-A系统订单状态名称
     * @param purchaseOrderStatus CPS-b-采购系统订单状态名称
     * @param supplyOrderStatus   CPS-b-供应系统订单状态名称
     */
    UsecarOrderStatusEnum(String orderStatusCode, String cpsOrderStatus, String purchaseOrderStatus, String supplyOrderStatus, String color) {
        this.orderStatusCode = orderStatusCode;
        this.cpsOrderStatus = cpsOrderStatus;
        this.purchaseOrderStatus = purchaseOrderStatus;
        this.supplyOrderStatus = supplyOrderStatus;
        this.color = color;
    }

    /**
     * 依据订单状态编号获取对应的CPS-A系统看到的订单状态名称
     *
     * @param orderStatusCode 订单状态编号
     * @return CPS-A系统订单状态名称
     */
    public static String getCpsOrderStatus(String orderStatusCode) {
        return getCpsOrderStatusByCode(orderStatusCode);
    }

    /**
     * 依据订单状态编号获取对应的CPS-B_采购系统看到的订单状态名称
     *
     * @param orderStatusCode 订单状态编号
     * @return CPS-b-采购系统订单状态名称
     **/
    public static String getPurchaseOrderStatus(String orderStatusCode) {
        if (UsecarOrderStatusEnum.valueOf(orderStatusCode) != null) {
            return UsecarOrderStatusEnum.valueOf(orderStatusCode).getPurchaseOrderStatus();
        } else {
            return null;
        }
    }

    /**
     * 依据订单状态编号获取对应的CPS-B_供应系统看到的订单状态名称
     *
     * @param orderStatusCode 订单状态编号
     * @return 供应系统订单状态名称
     **/
    public static String getSupplyOrderStatus(String orderStatusCode) {
        if (UsecarOrderStatusEnum.valueOf(orderStatusCode) != null) {
            return UsecarOrderStatusEnum.valueOf(orderStatusCode).getSupplyOrderStatus();
        } else {
            return null;
        }
    }

    /**
     * 依据订单状态编号获取对应的状态颜色
     *
     * @param orderStatusCode 订单状态编号
     * @return 订单状态颜色
     **/
    public static String getColor(String orderStatusCode) {
        if (UsecarOrderStatusEnum.valueOf(orderStatusCode) != null) {
            return UsecarOrderStatusEnum.valueOf(orderStatusCode).getColor();
        } else {
            return null;
        }
    }

    /**
     * 页面的JSON 值 转为枚举
     *
     * @param value 页面传过来的值
     * @return 转换后的枚举值
     */
    @JsonCreator
    public static UsecarOrderStatusEnum getEnum(String value) {
        UsecarOrderStatusEnum[] usecarOrderStatusEnums = UsecarOrderStatusEnum.values();
        for (UsecarOrderStatusEnum usecarOrderStatusEnum : usecarOrderStatusEnums) {
            if (usecarOrderStatusEnum.getCode().equals(value)) {
                return usecarOrderStatusEnum;
            }
        }
        return null;
    }

    public static String getCpsOrderStatusByCode(String value) {
        UsecarOrderStatusEnum[] usecarOrderStatusEnums = UsecarOrderStatusEnum.values();
        for (UsecarOrderStatusEnum usecarOrderStatusEnum : usecarOrderStatusEnums) {
            if (usecarOrderStatusEnum.getCode().equals(value)) {
                return usecarOrderStatusEnum.cpsOrderStatus;
            }
        }
        return null;
    }
    /**
     * 获取采购单申请退款单时，又不
     *
     * @return ddzt
     */
    public String getOrderStatusCode() {
        return orderStatusCode;
    }

    /**
     * 获取CPS-A系统订单状态名称
     *
     * @return cps_a_ztmc
     */
    public String getCpsOrderStatus() {
        return cpsOrderStatus;
    }

    /**
     * 获取CPS-b-采购系统订单状态名称
     *
     * @return cps_b_cg_ztmc
     */
    public String getPurchaseOrderStatus() {
        return purchaseOrderStatus;
    }

    /**
     * 获取CPS-b-供应系统订单状态名称
     *
     * @return cps_c_gy_ztmc
     */
    public String getSupplyOrderStatus() {
        return supplyOrderStatus;
    }

    @Override
    public String getCode() {
        return orderStatusCode;
    }

    /**
     * 获取订单状态颜色
     *
     * @return cps_color
     */
    public String getColor() {
        return color;
    }
}