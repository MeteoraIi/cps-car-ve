package cn.vetech.center.usecar.finance.report.service;


import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.number.Arith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC4A;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC4B;
import static java.math.BigDecimal.ZERO;


/**
 * 双倍预付已完成
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class PrepayFinishedGetPayInfoListService extends AbstractGetPayInfoListService {
    @Override
    public List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        List<FetchFzVO> payList = new ArrayList<>();
        FetchFzVO buyerPrepay = getBuyerPrepayInfo(orderEsVO);
        payList.add(buyerPrepay);
        FetchFzVO buyerCollection = getBuyerCollectionInfo(orderEsVO);
        payList.add(buyerCollection);
        FetchFzVO profit = getPrepayFinishedProfitInfo(orderEsVO);
        BigDecimal supplierAmount = buyerPrepay.getTradeAmt();
        if (profit != null) {
            payList.add(profit);
            supplierAmount = Arith.sub(supplierAmount, profit.getTradeAmt());
        }
        if (Arith.add(orderEsVO.getPttdje(), ZERO).compareTo(ZERO) > 0) {
            payList.add(getSubsidy(orderEsVO,PAY));
        }
        if (StringUtils.isBlank(orderEsVO.getGldh()) && Arith.add(buyerCollection.getTradeAmt(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            payList.add(getSupplierCollectionInfo(orderEsVO));
        } else {
            payList.add(getSupplierCollectionHaveAssociatedOrder(orderEsVO, supplierAmount));
        }
        return payList;
    }

    /**
     * 关联订单获取供应应付
     *
     * @param orderEsVO      订单
     * @param supplierAmount
     * @return 供应应付
     */
    private FetchFzVO getSupplierCollectionHaveAssociatedOrder(OrderEsVO orderEsVO, BigDecimal supplierAmount) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(SUPPLIER);
        buyPayVO.setMerchantName(orderEsVO.getGyShjc());
        buyPayVO.setMerchantNo(orderEsVO.getGyShbh());
        buyPayVO.setTradeAmt(supplierAmount);
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 获取控润
     *
     * @param orderEsVO 订单
     * @return 控润
     */
    protected FetchFzVO getPrepayFinishedProfitInfo(OrderEsVO orderEsVO) {
        BigDecimal profitAmount = Arith.sub(orderEsVO.getCgJsje(), orderEsVO.getGyJsje()).setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        if (profitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(PROFIT);
        buyPayVO.setTradeAmt(profitAmount);
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }


    @Override
    public boolean canMatch(OrderEsVO orderEsVO) {
        UsecarOrderStatusEnum usecarOrderStatusEnum = UsecarOrderStatusEnum.getEnum(orderEsVO.getDdzt());
        return isDoublePrePay(orderEsVO) && EnumSet.of(YC4A, YC4B).contains(usecarOrderStatusEnum) && StringUtils.isBlank(orderEsVO.getBcdzt());
    }


}