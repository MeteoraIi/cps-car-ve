package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
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

import static cn.vetech.center.usecar.common.UseCarConstant.TWO;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.*;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.ZERO;

/**
 * 先用后付正常
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class PayLaterNormalGetPayInfoListService extends AbstractGetPayInfoListService {
    @Override
    public List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        List<FetchFzVO> payList = new ArrayList<>();
        payList.add(getBuyerPayInfo(orderEsVO));
        payList.add(getSupplierCollectionInfo(orderEsVO));
        if (Arith.add(orderEsVO.getPttdje(), ZERO).compareTo(ZERO) > 0) {
            payList.add(getSubsidy(orderEsVO,PAY));
        }
        FetchFzVO fetchFzVO = getProfitInfo(orderEsVO);
        if (fetchFzVO != null) {
            payList.add(fetchFzVO);
        }

        return payList;
    }

    /**
     * 获取控润
     *
     * @param orderEsVO 订单
     * @return 控润
     */
    protected FetchFzVO getProfitInfo(OrderEsVO orderEsVO) {
        BigDecimal profitAmount = Arith.sub(getBuyerSettlementAmount(orderEsVO), getSupplierSettlementAmount(orderEsVO)).setScale(TWO, ROUND_HALF_UP);
        profitAmount = Arith.add(profitAmount, orderEsVO.getPttdje());
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
        return isPayLater(orderEsVO) && EnumSet.of(YC4C, YC4A, YC4B).contains(usecarOrderStatusEnum) && StringUtils.isBlank(orderEsVO.getBcdzt());
    }


}