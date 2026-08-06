package cn.vetech.center.usecar.finance.report.query;

import cn.vetech.center.reconcile.api.dto.FetchPtdzDTO;
import cn.vetech.center.reconcile.api.dto.ReconcileQueryDTO;
import cn.vetech.center.reconcile.api.vo.CapitalOrderVO;
import cn.vetech.center.reconcile.api.vo.ReconcileQueryVO;
import cn.vetech.center.usecar.apiclient.reconcile.IReconcileQueryServiceClient;
import cn.vetech.center.usecar.common.enums.CapitalStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarFinaceEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.finance.report.FundCheckingService;
import cn.vetech.center.usecar.service.order.YcDdBcdService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.ordermq.YcDdDzMqSendService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资金对账程序服务
 *
 * @author : Y
 * @since 2023/4/24 9:55
 */
@Service
public class ReconcileQueryService {
    /**
     * 资金对账查询服务
     */
    @Autowired
    private IReconcileQueryServiceClient iReconcileQueryServiceClient;
    /**
     * 资金对账服务
     */
    @Autowired
    private FundCheckingService fundCheckingService;
    /**
     * 订单服务
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 补差单服务
     */
    @Autowired
    private YcDdBcdService ycDdBcdService;
    /**
     * 用车对账mq服务
     */
    @Autowired
    private YcDdDzMqSendService ycDdDzMqSendService;
    /**
     * log
     */
    private Logger log = LoggerFactory.getLogger(ReconcileQueryService.class);

    /**
     * 同步资金对账数据
     *
     * @param orderNoList 代打编号
     * @return 回参
     */
    public List<CapitalOrderVO> syncReconcile(List<String> orderNoList) {
        if (CollectionUtils.isEmpty(orderNoList)) {
            return Lists.newArrayList();
        }
        ReconcileQueryDTO dto = new ReconcileQueryDTO();
        dto.setOrderList(orderNoList);
        log.info("查询订单数量：{}", orderNoList.size());
        RestResponse<ReconcileQueryVO> reconcileResp = iReconcileQueryServiceClient.queryOrderReconcileList(dto);
        if (reconcileResp == null || reconcileResp.getResult() == null) {
            return null;
        }
        return reconcileResp.getResult().getCapitalOrderVOList();
    }

    /**
     * 同步代打数据
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    public void sync(String startDate, String endDate) {
        PageDTO<FetchPtdzDTO> pageDTO = new PageDTO<>();
        FetchPtdzDTO fetchPtdzDTO = new FetchPtdzDTO();
        fetchPtdzDTO.setTjsjs(VeDate.strToDateLong(startDate));
        fetchPtdzDTO.setTjsjz(VeDate.strToDateLong(endDate));
        pageDTO.setCurrent(1);
        pageDTO.setSize(10000);
        pageDTO.setData(fetchPtdzDTO);
        List<OrderEsVO> orderEsVOList = fundCheckingService.getFundCheckingOrderList(pageDTO);
        if (CollectionUtils.isEmpty(orderEsVOList)) {
            return;
        }
        List<String> orderList = orderEsVOList.stream()
                .filter(this::filterNotSyncOrder)
                .map(OrderEsVO::getDdbh).collect(Collectors.toList());
        syncOrders(orderList);
    }

    /**
     * 同步订单
     *
     * @param orderList 订单列表
     */
    private void syncOrders(List<String> orderList) {
        List<CapitalOrderVO> tradeQueryList = syncReconcile(orderList);
        if (CollectionUtils.isEmpty(tradeQueryList)) {
            log.info("资金查询条数{}", 0);
            return;
        }
        log.info("资金查询条数{}", tradeQueryList.size());
        tradeQueryList = tradeQueryList.stream().filter(this::filterSyncOrder).collect(Collectors.toList());
        tradeQueryList.forEach(tradeQuery -> {
            if (StringUtils.startsWith(tradeQuery.getOrderNo(), "BC")) {
                YcDdBcd ycDdBcd = new YcDdBcd();
                ycDdBcd.setBcdh(tradeQuery.getOrderNo());
                ycDdBcd.setCapitalStatus(tradeQuery.getReconciliationStatus());
                ycDdBcd.setCapitalTime(tradeQuery.getReconciliationTime());
                ycDdBcd.setCapitalId(null);
                ycDdBcdService.updateYcDdBcd(ycDdBcd);
                ycDdBcd = ycDdBcdService.selectYcDdBcd(tradeQuery.getOrderNo());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("延时处理失败！");
                }
                ycDdDzMqSendService.sendMessageBc(ycDdBcd,ycDdBcd.getZt());
            } else {
                YcDd order = new YcDd();
                order.setDdbh(tradeQuery.getOrderNo());
                order.setCapitalStatus(tradeQuery.getReconciliationStatus());
                order.setCapitalTime(tradeQuery.getReconciliationTime());
                order.setCapitalId(null);
                ycDdService.updateYcDd(order);
                order = ycDdService.selectYcDd(tradeQuery.getOrderNo());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("延时处理失败！");
                }
                ycDdDzMqSendService.sendMessage(order, UsecarFinaceEnum.USECARORDER.getCode(),"00",order.getDdzt());
            }
        });
    }

    /**
     * 未对账过滤
     *
     * @param orderEsVO 订单
     * @return 回参
     */
    private boolean filterNotSyncOrder(OrderEsVO orderEsVO) {
        return StringUtils.isBlank(orderEsVO.getCapitalStatus()) || StringUtils.equals(CapitalStatusEnum.NOT.getCode(), orderEsVO.getCapitalStatus());
    }

    /**
     * 未对账过滤
     *
     * @param order 订单
     * @return 回参
     */
    private boolean filterSyncOrder(CapitalOrderVO order) {
        return EnumSet.of(CapitalStatusEnum.FAIL, CapitalStatusEnum.SUCCESS).contains(CapitalStatusEnum.getEnum(order.getReconciliationStatus()));
    }

    /**
     * 同步单个订单
     *
     * @param orderNo 订单编号
     */
    public void pullFundCheckingResult(String orderNo) {
        syncOrders(Arrays.asList(orderNo));
    }
    
    /**
     * 同步单个订单
     *
     * @param orderNo 订单编号
     */
    public void pullFundCheckingResult(List<String> orderNo) {
        syncOrders(orderNo);
    }
}