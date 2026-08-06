package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;

import java.util.List;

/**
 * 获取支付信息
 *
 * @author : Y
 * @since 2023/4/21 10:29
 */
public interface IGetPayInfoListService {

    /**
     * 获取支付信息
     *
     * @param orderEsVO 订单信息
     * @return 支付信息
     */
    List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO);

    /**
     * 是否匹配
     *
     * @param orderEsVO 订单信息
     * @return 是否匹配
     */
    boolean canMatch(OrderEsVO orderEsVO);

}
