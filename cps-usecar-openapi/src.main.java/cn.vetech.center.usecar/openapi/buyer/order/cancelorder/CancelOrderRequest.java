package cn.vetech.center.usecar.openapi.buyer.order.cancelorder;

import cn.vetech.center.system.openapi.OpenApiRequest;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 采购类
 * 取消订单接口的request
 *
 * @author vetech
 */
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class CancelOrderRequest extends OpenApiRequest {
    /**
     * 采购取消人姓名
     */
    private String qxr;
    /**
     * 订单编号  CPS的订单编号
     */
    private String ddbh;
    /**
     * 采购取消原因
     */
    @XmlElement(name = "cg_qxyy")
    private String cgQxyy;
    /**
     * 产品类型
     */
    private String ddlx;
    /**
     * 是否强制取消(true或false)默认false
     */
    private String force;

    /**
     * 采购订单编号
     */
    private String buyerOrderNo;

    /**
     * cps主单编号
     */
    private String cpsMainOrderNo;

    /**
     *  请求来自标准采购商
     */
    private String bzcgs;

    public String getBzcgs() {
        return bzcgs;
    }

    public void setBzcgs(String bzcgs) {
        this.bzcgs = bzcgs;
    }

    public String getCpsMainOrderNo() {
        return cpsMainOrderNo;
    }

    public void setCpsMainOrderNo(String cpsMainOrderNo) {
        this.cpsMainOrderNo = cpsMainOrderNo;
    }

    public String getBuyerOrderNo() {
        return buyerOrderNo;
    }

    public void setBuyerOrderNo(String buyerOrderNo) {
        this.buyerOrderNo = buyerOrderNo;
    }

    public String getDdlx() {
        return ddlx;
    }

    public void setDdlx(String ddlx) {
        this.ddlx = ddlx;
    }

    public String getQxr() {
        return qxr;
    }

    public void setQxr(String qxr) {
        this.qxr = qxr;
    }

    public CancelOrderRequest() {
        super();
    }

 public String getDdbh() {
        return ddbh;
    }

    public void setDdbh(String ddbh) {
        this.ddbh = ddbh;
    }

    public String getCgQxyy() {
        return cgQxyy;
    }

    public void setCgQxyy(String cgQxyy) {
        this.cgQxyy = cgQxyy;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }

}