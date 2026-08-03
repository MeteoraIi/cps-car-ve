package cn.vetech.center.usecar.openapi.buyer.order.cancelorder;

import java.math.BigDecimal;

/**
 * 小交通业务
 *
 * @author : Y
 * @since 2023/8/16 17:31
 */
public class CancelResp {
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 取消费
     */
    private BigDecimal cost;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}