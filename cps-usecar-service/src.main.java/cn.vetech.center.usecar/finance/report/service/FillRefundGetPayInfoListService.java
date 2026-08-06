package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.orderes.YcDdBcdEsService;
import cn.vetech.center.usecar.service.orderes.YcDdEsService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.number.Arith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum.*;
import static java.math.BigDecimal.ZERO;

/**
 * 补差退款
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class FillRefundGetPayInfoListService extends AbstractGetPayInfoListService {

    @Autowired
    private YcDdBcdEsService ycDdBcdEsService;
    /**
     * es 服务
     */
    @Autowired
    private YcDdEsService ycDdEsService;

    @Override
    public List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        YcDdBcd ycDdBcd = ycDdBcdEsService.searchByFillOrderNo(orderEsVO.getDdbh());
        OrderEsVO originalOrder = null;
        if (ycDdBcd != null) {
            originalOrder = ycDdEsService.getCarOrderByOrderNo(ycDdBcd.getDdbh());
        }
        List<FetchFzVO> payList = new ArrayList<>();
        FetchFzVO supplierRefund = getFillSupplierPayInfo(orderEsVO);
        payList.add(supplierRefund);
        // 有贴点金额，全退
        BigDecimal pttdje = ZERO;
        if (originalOrder != null && Arith.add(originalOrder.getPttdje(), ZERO).compareTo(ZERO) > 0) {
            BigDecimal totalAmount = getSupplierSettlementAmount(originalOrder);
            if (Arith.sub(totalAmount, supplierRefund.getTradeAmt()).compareTo(BigDecimal.ZERO) == 0) {
                pttdje = originalOrder.getPttdje();
                payList.add(getSubsidy(originalOrder, COLLECTION));
            }
        }
        payList.add(getFillBuyerCollectionInfo(orderEsVO, pttdje));
        BigDecimal profit = Arith.add(orderEsVO.getPtkrje(), BigDecimal.ZERO);
        if (profit.compareTo(BigDecimal.ZERO) > 0) {
            payList.add(getFillProfitInfo(orderEsVO, profit));
        }
        return payList;
    }

    /**
     * 获取补差供应应付
     *
     * @param orderEsVO 订单信息
     * @return 采购应收
     */
    protected FetchFzVO getFillSupplierPayInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(SUPPLIER);
        buyPayVO.setMerchantName(orderEsVO.getGyShjc());
        buyPayVO.setMerchantNo(orderEsVO.getGyShbh());
        buyPayVO.setTradeAmt(orderEsVO.getTysyjdje());
        buyPayVO.setType(PAY);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 利润应付
     *
     * @param orderEsVO 订单
     * @param profit    利润
     * @return 应付
     */
    private FetchFzVO getFillProfitInfo(OrderEsVO orderEsVO, BigDecimal profit) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(PROFIT);
        buyPayVO.setTradeAmt(profit);
        buyPayVO.setType(PAY);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 获取补差采购应收
     *
     * @param orderEsVO 订单信息
     * @param pttdje
     * @return 采购应收
     */
    private FetchFzVO getFillBuyerCollectionInfo(OrderEsVO orderEsVO, BigDecimal pttdje) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(BUYER);
        buyPayVO.setMerchantName(orderEsVO.getCgShjc());
        buyPayVO.setMerchantNo(orderEsVO.getCgShbh());
        buyPayVO.setTradeAmt(Arith.sub(orderEsVO.getGyptkkje(), pttdje));
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    @Override
    public boolean canMatch(OrderEsVO orderEsVO) {
        UsecarMakeupStatusEnum usecarMakeupStatusEnum = UsecarMakeupStatusEnum.getEnum(orderEsVO.getBcdzt());
        return EnumSet.of(YSHDTK, YTKDFZ, YTKYFZ).contains(usecarMakeupStatusEnum);
    }
}