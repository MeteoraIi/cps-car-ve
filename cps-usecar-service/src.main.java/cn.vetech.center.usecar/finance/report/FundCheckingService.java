package cn.vetech.center.usecar.finance.report;

import cn.vetech.center.reconcile.api.dto.FetchPtdzDTO;
import cn.vetech.center.reconcile.api.vo.FetchFzVO;
import cn.vetech.center.reconcile.api.vo.FetchPtdzVO;
import cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.finance.report.service.IGetPayInfoListService;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.orderes.YcDdBcdEsService;
import cn.vetech.center.usecar.service.orderes.YcDdEsService;
import cn.vetech.center.usecar.service.usecar.YcKhSjzfxxService;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.Arith;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.*;
import static cn.vetech.center.usecar.common.enums.UsecarCommonEnum.NO;
import static cn.vetech.center.usecar.common.enums.UsecarCommonEnum.YES;
import static cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum.*;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.*;
import static cn.vetech.center.usecar.finance.report.service.AbstractGetPayInfoListService.BUYER;


/**
 * 资金对账服务
 *
 * @author : Y
 * @since 2023/4/20 15:09
 */
@Service
public class FundCheckingService implements ApplicationContextAware {
    /**
     * es 服务
     */
    @Autowired
    private YcDdEsService ycDdEsService;
    /**
     * es 服务
     */
    @Autowired
    private YcDdBcdEsService ycDdBcdEsService;
    /**
     * log
     */
    private Logger log = LoggerFactory.getLogger(FundCheckingService.class);
    /**
     * 上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 支付服务集合
     */
    public static final List<IGetPayInfoListService> GET_PAY_INFO_LIST_SERVICE_LIST = new ArrayList<>();
    /**
     * 正常单
     */
    private static final String NORMAL_ORDER = "1";
    /**
     * 退单
     */
    private static final String REFUND_ORDER = "3";
    /**
     * 支付记录表
     */
    @Autowired
    private YcKhSjzfxxService ycKhSjzfxxService;
    /**
     * 资金对账
     *
     * @param pageDTO 入参
     * @return 回参
     */
    public List<FetchPtdzVO> getFundCheckingList(PageDTO<FetchPtdzDTO> pageDTO) {
        List<OrderEsVO> orderList = getFundCheckingOrderList(pageDTO);
        if (CollectionUtils.isEmpty(orderList)) {
            log.info("用车查询为空");
            return Lists.newArrayList();
        }
        List<String> fillOrderNoList = orderList.stream().filter(e -> StringUtils.isNotBlank(e.getBcdzt())).map(OrderEsVO::getDdbh).collect(Collectors.toList());
        List<YcDdBcd> fillOrderList = ycDdBcdEsService.selectByFillOrders(fillOrderNoList);
        Map<String, String> fillOrderMap = fillOrderList.stream().collect(Collectors.toMap(YcDdBcd::getBcdh, YcDdBcd::getDdbh, (key1, key2) -> key2));
        List<FetchPtdzVO> fundCheckList = new ArrayList<>(orderList.size());
        List<String> ddbhList = orderList.stream().map(OrderEsVO::getDdbh).collect(Collectors.toList());
        Set<String> childPayOrderNoSet = ycKhSjzfxxService.selectChildPayOrderNoSet(ddbhList);
        for (OrderEsVO orderEsVO : orderList) {
            FetchPtdzVO fundCheck = getFundCheck(orderEsVO, fillOrderMap, childPayOrderNoSet);
            if (CollectionUtils.isEmpty(fundCheck.getFzList())) {
                continue;
            }
            fundCheckList.add(fundCheck);
        }
        if (!CollectionUtils.isEmpty(pageDTO.getData().getDdbhlist())) {
            log.info("资金对账查询回参：{}", JsonMapper.nonEmptyMapper().toJson(fundCheckList));
        }
        return fundCheckList;
    }

    public List<OrderEsVO> getFundCheckingOrderList(PageDTO<FetchPtdzDTO> pageDTO) {
        List<UsecarOrderStatusEnum> doublerPayStatus = Arrays.asList(YC1F, YC2D, YC2G, YC2H, YC2E, YC2F, YC4A, YC4B, YC2O, YC2P, YC3C, YC3D, YC2B, YC2C);
        List<UsecarOrderStatusEnum> laterPayStatus = Arrays.asList(YC2M, YC2O, YC2P, YC4C, YC4A, YC4B);
        List<OrderEsVO> orderList = ycDdEsService.getFundCheckingList(pageDTO, doublerPayStatus, laterPayStatus);
        return orderList;
    }

    /**
     * 资金对账数据转换
     *
     * @param orderEsVO          订单信息
     * @param fillOrderMap       补差单集合
     * @param childPayOrderNoSet 子单支付订单
     * @return 资金对账回参
     */
    public FetchPtdzVO getFundCheck(OrderEsVO orderEsVO, Map<String, String> fillOrderMap, Set<String> childPayOrderNoSet) {
        if (StringUtils.isNotBlank(orderEsVO.getBcdzt()) &&
                !Arrays.asList("2", "3", "5", "6").contains(orderEsVO.getBcdzt())) {
            return new FetchPtdzVO();
        }
        FetchPtdzVO vo = buildBaseInfo(orderEsVO, fillOrderMap, childPayOrderNoSet);
        List<FetchFzVO> payList = getPayInfoList(orderEsVO);
        if (CollectionUtils.isEmpty(payList)) {
            return vo;
        }
        payList = payList.stream()
                .filter(e -> Arith.add(e.getTradeAmt(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());
        payList.forEach(fetchFzVO -> {
            if (!StringUtils.equals(vo.getRelationNo(), vo.getOrderNo())
                    && StringUtils.equals(fetchFzVO.getTradeAccountType(), BUYER)
                    && StringUtils.isBlank(orderEsVO.getBcdzt())) {
                fetchFzVO.setUnionRecStatus(YES.getCode());
            }
        });
        vo.setFzList(payList);
        return vo;
    }

    /**
     * 获取支付信息
     * 先用后付（正常、有违约）
     * 双倍预付（未完成，已完成，有违约,取消全退）
     * 补差收款
     * 补差退款
     *
     * @param orderEsVO 订单信息
     * @return 支付信息
     */
    private List<FetchFzVO> getPayInfoList(OrderEsVO orderEsVO) {
        IGetPayInfoListService getPayInfoListService = matchGetPayInfoListService(orderEsVO);
        if (getPayInfoListService == null) {
            return null;
        }
        return getPayInfoListService.getPayInfoList(orderEsVO);
    }

    /**
     * 初始化支付服务
     */
    @PostConstruct
    public void init() {
        Map<String, IGetPayInfoListService> map = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, IGetPayInfoListService.class);
        GET_PAY_INFO_LIST_SERVICE_LIST.addAll(map.values());
    }

    /**
     * 匹配服务
     *
     * @param orderEsVO 订单信息
     * @return 支付服务
     */
    private IGetPayInfoListService matchGetPayInfoListService(OrderEsVO orderEsVO) {
        for (IGetPayInfoListService payInfoListService : GET_PAY_INFO_LIST_SERVICE_LIST) {
            if (!payInfoListService.canMatch(orderEsVO)) {
                continue;
            }
            return payInfoListService;
        }
        return null;
    }

    /**
     * 基本数据
     *
     * @param orderEsVO          订单信息
     * @param fillOrderMap       补差单
     * @param childPayOrderNoSet
     * @return 回参
     */
    private FetchPtdzVO buildBaseInfo(OrderEsVO orderEsVO, Map<String, String> fillOrderMap, Set<String> childPayOrderNoSet) {
        FetchPtdzVO vo = new FetchPtdzVO();
        vo.setOrderNo(orderEsVO.getDdbh());
        if (StringUtils.isNotBlank(orderEsVO.getBcdzt())) {
            String originalOrder = fillOrderMap.get(orderEsVO.getDdbh());
            vo.setRelationNo(StringUtils.defaultIfBlank(originalOrder, orderEsVO.getDdbh()));
        } else {
            if (StringUtils.equals(CPS_A, orderEsVO.getCgDdly())
                    || childPayOrderNoSet.contains(orderEsVO.getDdbh())
                    || StringUtils.equals(CPS_A_O, orderEsVO.getCgDdly())) {
                vo.setRelationNo(orderEsVO.getDdbh());
            } else {
                vo.setRelationNo(StringUtils.defaultIfBlank(orderEsVO.getpDdbh(), orderEsVO.getDdbh()));
            }
        }
        UsecarOrderStatusEnum usecarOrderStatusEnum = UsecarOrderStatusEnum.getEnum(orderEsVO.getDdzt());
        UsecarMakeupStatusEnum usecarMakeupStatusEnum = UsecarMakeupStatusEnum.getEnum(orderEsVO.getBcdzt());
        if (StringUtils.equals(orderEsVO.getOrderType(), NORMAL_ORDER)
                || EnumSet.of(YZFYFZ, YZFDFZ, YSHDZF).contains(usecarMakeupStatusEnum)) {
            vo.setOrderStatus(usecarOrderStatusEnum.getCpsOrderStatus());
            vo.setOrderType(NORMAL_ORDER);
        } else {
            vo.setOrderStatus(usecarMakeupStatusEnum.getMsg());
            vo.setOrderType(REFUND_ORDER);
        }
        vo.setProductNo(orderEsVO.getCgDdbh());
        if (EnumSet.of(YC4B, YC2P).contains(usecarOrderStatusEnum) || EnumSet.of(YZFYFZ, YTKYFZ).contains(usecarMakeupStatusEnum)) {
            vo.setOrderFinishStatus(YES.getCode());
        } else {
            vo.setOrderFinishStatus(NO.getCode());
        }
        vo.setProductNo(YC_CPBH);
        vo.setPurchaserNo(orderEsVO.getCgShbh());
        vo.setPurOrderNo(orderEsVO.getCgDdbh());
        vo.setPurchaserName(orderEsVO.getCgShjc());
        vo.setSupplierNo(orderEsVO.getGyShbh());
        vo.setSupplierName(orderEsVO.getGyShjc());
        vo.setTravelType(StringUtils.defaultIfBlank(orderEsVO.getClyy(), "2"));
        return vo;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}