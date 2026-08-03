package cn.vetech.center.usecar.openapi.buyer.order.createorder;

import cn.vetech.center.system.openapi.OpenApiResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 下单接口的response
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateOrderResponse extends OpenApiResponse {

    /**
     * 订单编号
     */
    private String ddbh;

    /**
     * 订单状态
     */
    private String ddzt;

    /**取消时限**/
    private Integer qxsx;
    /**
     * 一键三单,供应主单单号
     */
    private String gysMddbh;

    /**
     * 子订单
     */
    private List<ChildOrderDTO> orders;
    // 都是get set
}