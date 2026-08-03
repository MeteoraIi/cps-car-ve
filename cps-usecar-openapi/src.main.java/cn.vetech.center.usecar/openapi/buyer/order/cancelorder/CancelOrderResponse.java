package cn.vetech.center.usecar.openapi.buyer.order.cancelorder;

import cn.vetech.center.system.openapi.OpenApiResponse;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * 取消订单接口的response
 * @author vetech
 *
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class CancelOrderResponse extends OpenApiResponse {
	/**
	 * 接口调用耗时毫秒
	 */
	private String cost;//扣费金额
	/**
	 * 是否已取消 true已取消 false和空表示未取消
	 */
	private String sfqx;//是否已取消 true已取消 false和空表示未取消

	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public String getSfqx() {
		return sfqx;
	}
	public void setSfqx(String sfqx) {
		this.sfqx = sfqx;
	}
	public CancelOrderResponse() {
		super();
	}

	@Override
	public String toString() {
		return JsonMapper.nonEmptyMapper().toJson(this);
	}
}
