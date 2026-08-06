package cn.vetech.center.usecar.openapi.buyer.order.createorder;

/**
 * 子订单
 *
 * @author : Y
 * @since 2023/8/14 11:03
 */
public class ChildOrderDTO {
    /**
     * 供应编号
     */
    private String supplierNo;
    /**
     * 采购子订单编号
     */
    private String buyerChildOrderNo;
    /**
     * 价格缓存id
     */
    private String priceCacheId;
    /**
     * 供应子订单编号
     */
    private String cpsChildOrderNo;
    /**
     * 订单状态
     */
    private String orderStatus;

    public String getSupplierNo() {
        return supplierNo;
    }

    public void setSupplierNo(String supplierNo) {
        this.supplierNo = supplierNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCpsChildOrderNo() {
        return cpsChildOrderNo;
    }

    public void setCpsChildOrderNo(String cpsChildOrderNo) {
        this.cpsChildOrderNo = cpsChildOrderNo;
    }

    public String getBuyerChildOrderNo() {
        return buyerChildOrderNo;
    }

    public void setBuyerChildOrderNo(String buyerChildOrderNo) {
        this.buyerChildOrderNo = buyerChildOrderNo;
    }

    public String getPriceCacheId() {
        return priceCacheId;
    }

    public void setPriceCacheId(String priceCacheId) {
        this.priceCacheId = priceCacheId;
    }
}