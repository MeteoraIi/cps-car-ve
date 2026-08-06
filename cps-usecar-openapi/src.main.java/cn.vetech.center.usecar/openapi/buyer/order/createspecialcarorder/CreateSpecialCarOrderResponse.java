package cn.vetech.center.usecar.openapi.buyer.order.createspecialcarorder;

import cn.vetech.center.system.openapi.OpenApiResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 下单接口的response
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateSpecialCarOrderResponse extends OpenApiResponse {

    /**
     * 订单编号
     */
    private String ddbh;
    /**
     * 订单状态
     */
    private String ddzt;

    public String getDdzt() {
        return ddzt;
    }

    public void setDdzt(String ddzt) {
        this.ddzt = ddzt;
    }

    public CreateSpecialCarOrderResponse() {
        super();
    }

    public String getDdbh() {
        return ddbh;
    }

    public void setDdbh(String ddbh) {
        this.ddbh = ddbh;
    }

}
