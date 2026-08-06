package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.number.Arith;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2B;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2C;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC3C;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC3D;

/**
 * 双倍预付取消
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class PrepayCancelGetPayInfoListService extends AbstractGetPayInfoListService {
    @Override
    public List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        List<FetchFzVO> payList = new ArrayList<>();
        payList.add(getBuyerPrepayInfo(orderEsVO));
        payList.add(getBuyerCollectionInfo(orderEsVO));
        payList.add(getSupplierCollectionInfo(orderEsVO));
        return payList;
    }


    @Override
    public boolean canMatch(OrderEsVO orderEsVO) {
        UsecarOrderStatusEnum usecarOrderStatusEnum = UsecarOrderStatusEnum.getEnum(orderEsVO.getDdzt());
        return isDoublePrePay(orderEsVO) && EnumSet.of(YC3C, YC3D, YC2B, YC2C).contains(usecarOrderStatusEnum) && StringUtils.isBlank(orderEsVO.getBcdzt());
    }

    /**
     * 采购应收
     *
     * @param orderEsVO 订单
     * @return 采购应收
     */
    protected FetchFzVO getBuyerCollectionInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(BUYER);
        buyPayVO.setMerchantName(orderEsVO.getCgShjc());
        buyPayVO.setMerchantNo(orderEsVO.getCgShbh());
        buyPayVO.setTradeAmt(Arith.sub(orderEsVO.getFkje(), orderEsVO.getCgJsje()));
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

}