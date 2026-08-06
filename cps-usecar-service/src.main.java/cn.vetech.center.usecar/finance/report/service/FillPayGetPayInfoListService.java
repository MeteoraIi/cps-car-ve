package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.number.Arith;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum.YSHDZF;
import static cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum.YZFDFZ;
import static cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum.YZFYFZ;
import static java.math.BigDecimal.ZERO;

/**
 * 补差收款
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class FillPayGetPayInfoListService extends AbstractGetPayInfoListService {
    @Override
    public List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        List<FetchFzVO> payList = new ArrayList<>();
        payList.add(getFillBuyerPayInfo(orderEsVO));
        payList.add(getFillSupplierCollectionInfo(orderEsVO));
        return payList;
    }

    private FetchFzVO getFillBuyerPayInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(BUYER);
        buyPayVO.setMerchantName(orderEsVO.getCgShjc());
        buyPayVO.setMerchantNo(orderEsVO.getCgShbh());
        buyPayVO.setTradeAmt(Arith.add(orderEsVO.getTysyjdje(), ZERO).abs());
        buyPayVO.setType(PAY);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 获取供应应收应付
     *
     * @param orderEsVO 订单
     * @return 采购应付
     */
    private FetchFzVO getFillSupplierCollectionInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(SUPPLIER);
        buyPayVO.setMerchantName(orderEsVO.getGyShjc());
        buyPayVO.setMerchantNo(orderEsVO.getGyShbh());
        buyPayVO.setTradeAmt(Arith.add(orderEsVO.getTysyjdje(), ZERO).abs());
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }


    @Override
    public boolean canMatch(OrderEsVO orderEsVO) {
        UsecarMakeupStatusEnum usecarMakeupStatusEnum = UsecarMakeupStatusEnum.getEnum(orderEsVO.getBcdzt());
        return EnumSet.of(YZFYFZ, YZFDFZ, YSHDZF).contains(usecarMakeupStatusEnum);
    }


}