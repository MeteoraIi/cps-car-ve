package cn.vetech.center.usecar.finance.report.service;

import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.usecar.service.UserCarPriceHelper;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.vetech.core.modules.utils.number.Arith;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;

/**
 * 资金对账服务抽象类
 * <p>
 * 订单类型 1-正常单 2-改签单 3-退单 -4服务费
 * 账户类型：1采购商、2供应商、3控润、4贴点、5手续费
 * 类型：1应付、2应收
 *
 * @author : Y
 * @since 2023/4/21 13:44
 */
public abstract class AbstractGetPayInfoListService implements IGetPayInfoListService {
    /**
     * 1采购商、2供应商、3控润
     */
    public static final String BUYER = "1";
    /**
     * 1采购商、2供应商、3控润
     */
    protected static final String SUPPLIER = "2";
    /**
     * 1采购商、2供应商、3控润
     */
    protected static final String PROFIT = "3";

    /**
     * 4 贴点
     */
    protected static final String SUBSIDY = "4";
    /**
     * 类型：1应付、2应收
     */
    protected static final String PAY = "1";
    /**
     * 类型：1应付、2应收
     */
    protected static final String COLLECTION = "2";

    @Autowired
    protected UserCarPriceHelper userCarPriceHelper;

    /**
     * 是否先用后付
     *
     * @param orderEsVO 订单
     * @return 是否先用后付
     */
    protected boolean isPayLater(OrderEsVO orderEsVO) {
        return !isDoublePrePay(orderEsVO);
    }

    /**
     * 是否先用后付
     *
     * @param orderEsVO 订单
     * @return 是否先用后付
     */
    protected boolean isDoublePrePay(OrderEsVO orderEsVO) {
        if (StringUtils.equals(orderEsVO.getFfgz(), "1")) {
            return false;
        }
        if (orderEsVO.getFksj() == null ||
                (StringUtils.isNotBlank(orderEsVO.getSjscsj()) &&
                        VeDate.compareDate(VeDate.dateToStrLong(orderEsVO.getFksj()), orderEsVO.getSjscsj()) > 0)) {
            return false;
        }
        return true;
    }

    /**
     * 获取采购应付
     *
     * @param orderEsVO 订单
     * @return 采购应付
     */
    protected FetchFzVO getBuyerPayInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(BUYER);
        buyPayVO.setMerchantName(orderEsVO.getCgShjc());
        buyPayVO.setMerchantNo(orderEsVO.getCgShbh());
        buyPayVO.setTradeAmt(getBuyerSettlementAmount(orderEsVO));
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
    protected FetchFzVO getSupplierCollectionInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(SUPPLIER);
        buyPayVO.setMerchantName(orderEsVO.getGyShjc());
        buyPayVO.setMerchantNo(orderEsVO.getGyShbh());
        buyPayVO.setTradeAmt(getSupplierSettlementAmount(orderEsVO));
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 双倍预付采购应付
     *
     * @param orderEsVO 订单
     * @return 双倍预付采购应付
     */
    protected FetchFzVO getBuyerPrepayInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(BUYER);
        buyPayVO.setMerchantName(orderEsVO.getCgShjc());
        buyPayVO.setMerchantNo(orderEsVO.getCgShbh());
        buyPayVO.setTradeAmt(orderEsVO.getFkje());
        buyPayVO.setType(PAY);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 双倍预付采购应收
     *
     * @param orderEsVO 订单
     * @return 双倍预付采购应收
     */
    protected FetchFzVO getBuyerCollectionInfo(OrderEsVO orderEsVO) {
        FetchFzVO buyPayVO = new FetchFzVO();
        buyPayVO.setTradeAccountType(BUYER);
        buyPayVO.setMerchantName(orderEsVO.getCgShjc());
        buyPayVO.setMerchantNo(orderEsVO.getCgShbh());
        buyPayVO.setTradeAmt(Arith.sub(orderEsVO.getFkje(), getBuyerSettlementAmount(orderEsVO)));
        buyPayVO.setType(COLLECTION);
        buyPayVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return buyPayVO;
    }

    /**
     * 获取采购结算金额
     *
     * @param ycDdEsVO 订单
     * @return 购结算金额
     */
    protected BigDecimal getBuyerSettlementAmount(OrderEsVO ycDdEsVO) {
        return userCarPriceHelper.getAmount(ycDdEsVO).getBuyerSettlementAmount();
    }

    /**
     * 获取供应结算金额
     *
     * @param ycDdEsVO 订单
     * @return 回参
     */
    protected BigDecimal getSupplierSettlementAmount(OrderEsVO ycDdEsVO) {
        return userCarPriceHelper.getAmount(ycDdEsVO).getSupplierSettlementAmount();
    }

    /**
     * 获取贴点
     *
     * @param orderEsVO 订单对象
     * @param type 类型
     * @return 贴点
     */
    protected FetchFzVO getSubsidy(OrderEsVO orderEsVO, String type) {
        FetchFzVO fetchFzVO = new FetchFzVO();
        fetchFzVO.setTradeAccountType(SUBSIDY);
        fetchFzVO.setTradeAmt(orderEsVO.getPttdje());
        fetchFzVO.setType(type);
        fetchFzVO.setTradeTime(ObjectUtils.defaultIfNull(orderEsVO.getFzDatetime(), orderEsVO.getFksj()));
        return fetchFzVO;
    }

}