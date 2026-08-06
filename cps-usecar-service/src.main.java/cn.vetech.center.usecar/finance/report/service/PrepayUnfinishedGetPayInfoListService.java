package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC1F;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2D;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2E;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2F;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2G;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2H;

/**
 * 双倍预付未完成
 *
 * @author : Y
 * @since 2023/4/21 11:25
 */
@Service
public class PrepayUnfinishedGetPayInfoListService extends AbstractGetPayInfoListService {
    @Override
    public List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        List<FetchFzVO> payList = new ArrayList<>();
        payList.add(getBuyerPrepayInfo(orderEsVO));
        return payList;
    }


    @Override
    public boolean canMatch(OrderEsVO orderEsVO) {
        UsecarOrderStatusEnum usecarOrderStatusEnum = UsecarOrderStatusEnum.getEnum(orderEsVO.getDdzt());
        return isDoublePrePay(orderEsVO) && EnumSet.of(YC1F, YC2D, YC2G, YC2H, YC2E, YC2F).contains(usecarOrderStatusEnum)
                && StringUtils.isBlank(orderEsVO.getBcdzt());
    }


}