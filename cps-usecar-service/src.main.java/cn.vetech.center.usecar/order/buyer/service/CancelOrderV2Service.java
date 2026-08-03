package cn.vetech.center.usecar.order.buyer.service;

import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.order.buyer.dto.BuyerNormalOrderOperateDTO;
import cn.vetech.center.usecar.order.buyer.dto.CancelResp;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.CANCEL_STATUS;
import static cn.vetech.center.usecar.common.UseCarConstant.CAN_CANCEL_STATUS;

/**
 * 取消2.0 通过主单取消
 *
 * @author : Y
 * @since 2023/8/16 16:34
 */
@Service
public class CancelOrderV2Service {
    /**
     * 打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(CancelOrderV2Service.class);
    /**
     * es
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 主单服务
     */
    @Autowired
    private YcDdMainService ycDdMainService;
    /**
     * 取消订单服务
     */
    @Autowired
    private BatchCancelService batchCancelService;

    public CancelResp cancelOrderV2(BuyerNormalOrderOperateDTO dto) {
        logger.info("通过主单批量取消{}", JsonMapper.nonEmptyMapper().toJson(dto));
        YcDdMain ycDdMain = ycDdMainService.selectById(dto.getCpsMainOrderNo());
        if (ycDdMain == null) {
            logger.info("主单为空");
            return new CancelResp(false, BigDecimal.ZERO);
        }
        if (CANCEL_STATUS.contains(ycDdMain.getDdzt())) {
            return new CancelResp(true, ycDdMain.getGyTksxf());
        }
       List<YcDd> ycDdList = ycDdService.selectAllBypDdbh(dto.getCpsMainOrderNo(), dto.getCgShbh());
        if (CollectionUtil.isEmpty(ycDdList)) {
            logger.info("通过主单查询订单为空");
            return new CancelResp(false, BigDecimal.ZERO);
        }
        List<YcDd> validOrders = ycDdList.stream()
                .filter(e -> CAN_CANCEL_STATUS.contains(e.getDdzt()))
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(validOrders)) {
            logger.info("通过主单查询订单为空");
            return new CancelResp(true, ycDdMain.getGyTksxf());
        }
        BigDecimal penaltyAmount= batchCancelService.cancelChildOrders(validOrders, dto);
        CancelResp resp = new CancelResp();
        resp.setSuccess(true);
        resp.setCost(penaltyAmount);
        return resp;
    }
}