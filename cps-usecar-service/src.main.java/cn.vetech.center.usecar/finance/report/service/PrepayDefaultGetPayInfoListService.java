package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2M;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2O;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2P;

/**
 * 双倍预付有违约
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class PrepayDefaultGetPayInfoListService extends AbstractGetPayInfoListService {
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
        return isDoublePrePay(orderEsVO) && EnumSet.of(YC2M, YC2O, YC2P).contains(usecarOrderStatusEnum) && StringUtils.isBlank(orderEsVO.getBcdzt());
    }


}