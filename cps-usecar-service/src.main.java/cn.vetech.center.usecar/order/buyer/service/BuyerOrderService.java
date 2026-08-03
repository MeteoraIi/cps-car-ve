package cn.vetech.center.usecar.order.buyer.service;

import cn.vetech.center.link.usecar.dto.LinkCancelPickUpOrderDTO;
import cn.vetech.center.link.usecar.dto.LinkCancelSpecialOrderDTO;
import cn.vetech.center.link.usecar.dto.LinkQueryCancelStatusPickUpOrderDTO;
import cn.vetech.center.link.usecar.dto.LinkQueryCancelStatusSpecialOrderDTO;
import cn.vetech.center.link.usecar.dto.LinkUseCarOrderComplaintDTO;
import cn.vetech.center.link.usecar.dto.LinkUseCarOrderScoreDTO;
import cn.vetech.center.link.usecar.dto.LinkUseCarQueryComplaintOptionsDTO;
import cn.vetech.center.link.usecar.vo.LinkCancelPickUpOrderVO;
import cn.vetech.center.link.usecar.vo.LinkCancelSpecialOrderVO;
import cn.vetech.center.link.usecar.vo.LinkComplaintOptionsBean;
import cn.vetech.center.link.usecar.vo.LinkQueryCancelStatusPickUpOrderVO;
import cn.vetech.center.link.usecar.vo.LinkQueryCancelStatusSpecialOrderVO;
import cn.vetech.center.link.usecar.vo.LinkUseCarOrderComplaintVO;
import cn.vetech.center.link.usecar.vo.LinkUseCarOrderScoreVO;
import cn.vetech.center.link.usecar.vo.LinkUseCarQueryComplaintOptionsVO;
import cn.vetech.center.usecar.apiclient.linkusecar.ILinkPickUpCarServiceClient;
import cn.vetech.center.usecar.apiclient.linkusecar.ILinkSpecialCarServiceClient;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookService;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarService;
import cn.vetech.center.usecar.book.buyer.specicar.service.DefaultCancelService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarCzlxEnum;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarPjtsPjdjEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarXxidEnum;
import cn.vetech.center.usecar.common.util.ExecutorServiceUtil;
import cn.vetech.center.usecar.coupon.CouponConsumeService;
import cn.vetech.center.usecar.entity.order.*;
import cn.vetech.center.usecar.entity.usecar.YcCp;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.entity.usecar.YcPjtsBq;
import cn.vetech.center.usecar.listener.CpsEventPublisher;
import cn.vetech.center.usecar.listener.entity.CpsEventEnum;
import cn.vetech.center.usecar.notice.buyer.dto.ComplaintOrderToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.dto.ConfirmOrderInfoToBuyerAsmsDTO;
import cn.vetech.center.usecar.notice.buyer.dto.FymxDTO;
import cn.vetech.center.usecar.notice.buyer.dto.OrderAnswerToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.dto.OrderScoreToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.service.BuyerNoticeService;
import cn.vetech.center.usecar.notice.seller.dto.ApplyRefundAsmsOrderToSellerDTO;
import cn.vetech.center.usecar.notice.seller.dto.ComplaintOrderToSellerDTO;
import cn.vetech.center.usecar.notice.seller.dto.ConfirmOrderNotifyToSellerDTO;
import cn.vetech.center.usecar.notice.seller.dto.OrderScoreToSellerDTO;
import cn.vetech.center.usecar.notice.seller.service.SellerNoticeService;
import cn.vetech.center.usecar.order.buyer.dto.BuyerInsertNoteDTO;
import cn.vetech.center.usecar.order.buyer.dto.BuyerNormalOrderOperateDTO;
import cn.vetech.center.usecar.order.buyer.dto.PjtsOptionBean;
import cn.vetech.center.usecar.order.buyer.dto.QueryComplaintDTO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerNormalOrderSpVO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerNormalRefundCacheVO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerOrderDetailVO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerOrderNoteVO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerOrderVO;
import cn.vetech.center.usecar.order.cpsa.vo.CpsaMakeupOrderVO;
import cn.vetech.center.order.dto.OrderSearchDTO;
import cn.vetech.center.usecar.order.dto.YcPjtsDTO;
import cn.vetech.center.usecar.order.dto.YcPjtsMxDTO;
import cn.vetech.center.usecar.order.seller.dto.SellerNormalOrderOperateDTO;
import cn.vetech.center.usecar.order.seller.dto.SellerRefundOrderOprateDTO;
import cn.vetech.center.usecar.order.seller.service.SellerOrderService;
import cn.vetech.center.usecar.order.seller.service.SellerRefundOrderService;
import cn.vetech.center.usecar.order.vo.YcPjtsMxVO;
import cn.vetech.center.usecar.order.vo.YcPjtsVO;
import cn.vetech.center.usecar.pay.service.UsecarPayHandleService;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.errorevent.ErrorEventPublisher;
import cn.vetech.center.usecar.service.order.*;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.ordermq.YcDdMqSendService;
import cn.vetech.center.usecar.service.ordermq.YcXxMdCommonService;
import cn.vetech.center.usecar.service.usecar.YcCpService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.service.usecar.YcPjtsBqService;
import cn.vetech.center.usecar.service.usecar.YcSupplierInterfaceCountService;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.vetech.core.api.RestResponse;
import org.vetech.core.api.ResultCode;
import org.vetech.core.base.PageDTO;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.mapper.PageCopyUtil;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cn.vetech.center.usecar.common.UseCarConstant.COMPLAINT_TYPE;
import static cn.vetech.center.usecar.common.UseCarConstant.NEGATIVE_COMMENT;
import static cn.vetech.center.usecar.common.UseCarConstant.YES;
import static cn.vetech.center.usecar.common.enums.SupplierInterfaceOperateEnum.CANCEL;
import static cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum.SFC;
import static cn.vetech.center.usecar.setting.buyerfilter.service.CarSupplierChannelTypeService.setChannelType;

/**
 * 采购订单管理服务类
 *
 * @author chenyong
 * @since 2017-10-10
 */
@Service
public class BuyerOrderService  {
    /**
     * 打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(BuyerOrderService.class);

    /**
     * 用车正常单dao
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 采购商通知
     */
    @Autowired
    private BuyerNoticeService buyerNoticeService;
    /**
     * 用车服务项目
     */
    @Autowired
    private YcDdFymxService ycDdFymxService;
    /**
     * 用车订单备注
     */
    @Autowired
    private BuyerYcDdBzService buyerYcDdBzService;

    /**
     * 专快车采购查询预订服务
     */
    @Autowired
    private BuyerBookSpeciCarService buyerBookSpeciCarService;
    /**
     * link接送车入口
     */
    @Autowired
    private ILinkPickUpCarServiceClient iLinkPickUpCarService;
    /**
     * link专快车入口
     */
    @Autowired
    private ILinkSpecialCarServiceClient iLinkSpecialCarService;
    /**
     * link专快车入口
     */
    @Autowired
    private DefaultCancelService defaultCancelService;

    /**
     * link专快车入口
     */
    @Autowired
    private UsecarPayHandleService usecarPayHandleService;
    /**
     * 接送车下单到供应商入口
     */
    @Autowired
    private BuyerBookService jscBookService;

    /**
     * 租车缓存服务
     */
    @Autowired
    private UsecarCacheService usecarCacheService;
    /**
     * 消息推送
     */
    @Autowired
    private SellerNoticeService sellerNoticeService;
    /**
     * 接送车查询预订service
     */
    @Autowired
    private BuyerBookService buyerBookService;

    /**
     * 供应订单服务类
     */
    @Autowired
    private SellerOrderService sellerOrderService;
    /**
     * 用车订单分账Service
     */
    @Autowired
    private YcDdFzService ycDdFzService;
    /**
     * 供应退款订单服务类
     */
    @Autowired
    private SellerRefundOrderService sellerRefundOrderService;
    /**
     * 补差单
     */
    @Autowired
    private YcDdBcdService ycDdBcdService;
    /**
     * 用车订单评价投诉 服务
     */
    @Autowired
    private YcPjtsService ycPjtsService;
    /**
     * 用车订单评价投诉明细 服务（即评价投诉回复明细）
     */
    @Autowired
    private YcPjtsMxService ycPjtsMxService;
    /**
     * 用车订单评价标签
     */
    @Autowired
    private YcPjtsBqService ycPjtsBqService;
    /**
     * 订单编号处理服务类
     */
    @Autowired
    private UsecarOrderNoService usecarOrderNoService;

    /**
     * 用车产品服务类
     */
    @Autowired
    private YcCpService cpService;

    /**
     * 用车消息埋点服务
     */
    @Autowired
    private YcXxMdCommonService ycXxMdCommonService;
    /**
     * 用车主订单
     */
    @Autowired
    private YcDdMainService ycDdMainService;

    /**
     * 消息服务
     */
    @Autowired
    private YcDdMqSendService ycDdMqSendService;

    @Autowired
    private ErrorEventPublisher errorEventPublisher;

    @Autowired
    private CpsEventPublisher cpsEventPublisher;
    /**
     * 接口记录
     */
    @Autowired
    private YcSupplierInterfaceCountService ycSupplierInterfaceCountService;

    @Autowired
    private CouponConsumeService couponConsumeService;

    /**
     * 采购查询自己的订单列表
     *
     * @param pageDTO 分页与查询条件
     * @return 采购订单列表分页对象
     */
    public Page<BuyerOrderVO> searchbuyerOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        //首先验证商户编号是否为空
        if (pageDTO.getData() != null) {
            //如果商户编号为空就要抛异常
            if (StringUtils.isBlank(pageDTO.getData().getCgShbh())) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
            }
            //如果起始时间为空
            if (StringUtils.isBlank(pageDTO.getData().getStarttime()) || StringUtils.isBlank(pageDTO.getData().getStarttime())) {
                //起始时间
                pageDTO.getData().setStarttime(VeDate.getNextDay(VeDate.getStringDateShort(), "-2"));
                //截止时间
                pageDTO.getData().setEndtime(VeDate.getStringDateShort());
            }

        }

        //传多个订单状态查询
        List<String> ddztList = new ArrayList<String>();
        if (StringUtils.isNotBlank(pageDTO.getData().getDdzt())) {
            String ddzts = pageDTO.getData().getDdzt();
            for (String ddzt : ddzts.split(",")) {
                if (StringUtils.isNotBlank(ddzt)) {
                    ddztList.add(ddzt);
                }
            }
        }

        pageDTO.getData().setDdztList(ddztList);
        //将页面的查询条件拷贝到分页对象Page里面
        Page<OrderEsVO> ycDdPage = ycDdService.searchSellerOrderList(pageDTO);
        Page<BuyerOrderVO> resultPage = PageCopyUtil.copy(ycDdPage, OrderEsVO.class, BuyerOrderVO.class);

        if(resultPage != null){
            if (CollectionUtil.isNotEmpty(resultPage.getRecords())) {
                for (BuyerOrderVO vo : resultPage.getRecords()) {
                    List<CpsaMakeupOrderVO> bcdVoList = new ArrayList<CpsaMakeupOrderVO>();
                    List<YcDdBcd>  bcdList =ycDdBcdService.selectYcDdBcdByDdbh(vo.getDdbh());
                    if(CollectionUtil.isNotEmpty(bcdList)){
                        vo.setSfybcd(UseCarConstant.NUMONE); //表示有补差单
                        for (YcDdBcd ycDdBcd : bcdList) {
                            CpsaMakeupOrderVO bcVo = BeanMapper.map(ycDdBcd, CpsaMakeupOrderVO.class);
                            bcdVoList.add(bcVo);
                        }
                        vo.setBcdList(bcdVoList); //补差单集合
                    }
                }
            }
        }
        return resultPage;
    }

    /**
     * 采购获取正常单订单状态个数
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<BuyerOrderVO> selectBuyerOrderTopNum(OrderSearchDTO dto) {
        //如果商户编号为空就要抛异常
        if (StringUtils.isBlank(dto.getCgShbh())) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
        }
        //如果起始时间为空
        if (StringUtils.isBlank(dto.getStarttime()) || StringUtils.isBlank(dto.getStarttime())) {
            //起始时间
            dto.setStarttime(VeDate.getNextDay(VeDate.getStringDateShort(), "-2"));
            //截止时间
            dto.setEndtime(VeDate.getStringDateShort());
        }

        List<BuyerOrderVO> list = null;
        try {
            list = ycDdService.selectBuyerOrderTopNum(dto);
        } catch (Exception e) {
            logger.error(UsecarOrderCode.UCAR_10005.getMessage(), e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10005, e);
        }
        return list;
    }

    /**
     * 采购根据订单编号查询订单详情
     *
     * @param dto 获取订单详细数据参数
     * @return 订单详情
     */
    public BuyerOrderDetailVO selectOrderDetail(OrderSearchDTO dto) {
        //如果商户编号为空就要抛异常
        if (StringUtils.isBlank(dto.getCgShbh())) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
        }
        YcDd ycDd = ycDdService.selectYcDd(dto.getDdbh());
        //如果没有查到数据抛出异常提示
        if (ycDd == null || !dto.getCgShbh().equals(ycDd.getCgShbh())) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10007, dto.getDdbh());
        }
        BuyerOrderDetailVO buyerOrderDetailVO = BeanMapper.map(ycDd, BuyerOrderDetailVO.class);
        //查询订单备注集合
        BuyerInsertNoteDTO buyerInsertNoteDTO = new BuyerInsertNoteDTO();
        buyerInsertNoteDTO.setDdbh(dto.getDdbh());
        buyerInsertNoteDTO.setCgShbh(dto.getCgShbh());
        List<BuyerOrderNoteVO> ddBzList = buyerYcDdBzService.selectYcDdBzList(buyerInsertNoteDTO);
        buyerOrderDetailVO.setDdBzList(ddBzList); //订单备注集合
        //查询有木有补差单信息
        List<YcDdBcd> bcdList =ycDdBcdService.selectYcDdBcdByDdbh(ycDd.getDdbh());
        List<CpsaMakeupOrderVO> bcdVoList = new ArrayList<CpsaMakeupOrderVO>();
        if (CollectionUtil.isNotEmpty(bcdList)) {
            for (YcDdBcd ycDdBcd : bcdList) {
                CpsaMakeupOrderVO bcVo = BeanMapper.map(ycDdBcd, CpsaMakeupOrderVO.class);
                bcdVoList.add(bcVo);
            }
        }
        buyerOrderDetailVO.setBcdList(bcdVoList); //补差单集合
        //用车订单评价投诉（回复）VO
        YcPjtsVO ycPjtsVO = getYcPjtsVOByDdbh(dto.getDdbh());
        buyerOrderDetailVO.setYcPjtsVO(ycPjtsVO);

        return buyerOrderDetailVO;
    }

    /**
     * 采购取消订单
     *
     * @param buyerNormalOrderOperateDTO 采购操作订单DTO
     * @return 是否取消成功
     */
    public Boolean cancelOrder(BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO) {
        Boolean result = Boolean.FALSE;
        logger.info("请求CPS执行取消订单操作[订单编号:" + buyerNormalOrderOperateDTO.getDdbh() + ",取消原因:"
                + buyerNormalOrderOperateDTO.getCgQxyy() + "]" + "userId:" + buyerNormalOrderOperateDTO.getCgTdsqr());
        YcDd ycDdVe = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
        if (ycDdVe != null && ycDdVe.getCgShbh().equals(buyerNormalOrderOperateDTO.getCgShbh())) {
            if (EnumSet.of(UsecarOrderStatusEnum.YC3D, UsecarOrderStatusEnum.YC1G, UsecarOrderStatusEnum.YC3C,
                    UsecarOrderStatusEnum.YC2A, UsecarOrderStatusEnum.YC2C, UsecarOrderStatusEnum.YC2B,
                    UsecarOrderStatusEnum.YC1E, UsecarOrderStatusEnum.YC2M, UsecarOrderStatusEnum.YC2O,
                    UsecarOrderStatusEnum.YC2P, UsecarOrderStatusEnum.YC1B, UsecarOrderStatusEnum.YC1D,
                    UsecarOrderStatusEnum.YC1E).contains(UsecarOrderStatusEnum.getEnum(ycDdVe.getDdzt()))
                    && !StringUtils.equals(buyerNormalOrderOperateDTO.getReCancel(),YES)) {
                return Boolean.TRUE;
            }
            if(StringUtils.equals(ycDdVe.getZfZt(),"1")){
                ycDdVe.setDdzt(UsecarOrderStatusEnum.YC3C.getCode());
            }else {
                ycDdVe.setDdzt(UsecarOrderStatusEnum.YC1E.getCode());// 订单状态改为已取消-无需退款
            }
            //先用后付，未支付此时设置订单状态为有违约
            ycDdVe.setCgQxr(buyerNormalOrderOperateDTO.getCgQxr());  //采购取消人姓名
            ycDdVe.setCgQxsj(VeDate.getNow());  //采购取消时间
            ycDdVe.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy()); //采购取消原因
            ycDdVe.setVersion(buyerNormalOrderOperateDTO.getVersion()); //版本号加"1"
            ycDdVe.setTszdczlx(UsecarCzlxEnum.CGSQXDD.getCode());
            // 处理供应订单编号还没有返回采购商就取消了
            if (StringUtils.isBlank(ycDdVe.getGyDdbh())
                    && VeDate.getTwoSec(VeDate.getNow(), ycDdVe.getXdsj()) < 60) {
                try {
                    logger.info("供应还没有返回订单编号，10s后取消");
                    TimeUnit.SECONDS.sleep(10);
                    ycDdVe = ycDdService.selectYcDd(ycDdVe.getDdbh());
                    logger.info("供应订单编号：{}",ycDdVe.getGyDdbh());
                } catch (InterruptedException e) {
                   logger.info("休眠异常",e);
                    Thread.currentThread().interrupt();
                }
            }
            // 供应商不为空则取消
            if (StringUtils.isNotEmpty(ycDdVe.getGyDdbh())) {
                BuyerNormalOrderSpVO vo = new BuyerNormalOrderSpVO();
                vo.setForce(UseCarConstant.TRUE);
                vo.setGyDdbh(ycDdVe.getGyDdbh());
                vo.setDdbh(ycDdVe.getDdbh());
                vo.setDdlx(UsecarProductTypeEnum.toEnum(ycDdVe.getDdlx()));
                vo.setCgQxyy(ycDdVe.getCgQxyy());
                vo.setGyShbh(ycDdVe.getGyShbh());
                vo.setGyShjc(ycDdVe.getGyShjc());
                vo.setCgShbh(ycDdVe.getCgShbh());
                vo.setCgShjc(ycDdVe.getCgShjc());
                vo.setAutotest(buyerNormalOrderOperateDTO.getAutotest());
                vo.setJjmslb(ycDdVe.getJjmslb());
                vo.setGyJsje(ycDdVe.getGyJsje());
                vo.setNotCancel(buyerNormalOrderOperateDTO.getNotCancel());
                vo.setBzcgs(buyerNormalOrderOperateDTO.getBzcgs());
                vo.setLxrdh(ycDdVe.getLxrdh());
                cancelApiGysOrder(vo,ycDdVe);
                logger.info("********订单取消状态为{}", vo.getQszt());
                if (!"true".equals(vo.getQszt())) {
                    logger.info("CPS用车调用供应商取消接口失败,单号:{}", ycDdVe.getDdbh());
                    return false;
                }
                result = true;
            }else {
                result = ycDdService.updateYcDd(ycDdVe);
            }
            ycDdVe = ycDdService.selectYcDd(ycDdVe.getDdbh());
            final YcDd ycDd = ycDdVe;
            ExecutorServiceUtil.XXMD_MQ_SEND_EXECUTO.submit(()->{
                ycXxMdCommonService.usecarXxmdMqSend(UsecarXxidEnum.XX_CAR_0015, ycDd.getDdbh(), ycDd.getDdlx()
                        , "", ycDd.getCgShbh());
            });

            logger.info("当前待取消订单{}的支付状态{}，订单状态{}", ycDdVe.getDdbh(), ycDdVe.getZfZt(), ycDdVe.getDdzt());
            if ("1".equals(ycDdVe.getZfZt())) {
                try {
                    usecarPayHandleService.refund(ycDdVe.getDdbh(), ycDdVe.getCgShbh(),
                            buyerNormalOrderOperateDTO.getCgQxr(), UseCarConstant.NUMTWO, UseCarConstant.YC_PAY_PT_A);
                } catch (Exception e) {
                    logger.error("供应商退款异常", e);
                }
            } else {
                //先用后付未付
                logger.info("退款订单" + ycDdVe.getDdbh() + "的付费规则为" + ycDdVe.getFfgz());
                if (!StringUtils.equals(ycDdVe.getFfgz(), "1")) {
                    ycDdService.updateYcDd(ycDdVe);
                    cpsEventPublisher.send(CpsEventEnum.SENIOR_EXECUTIVE_MONITOR, ImmutableMap.of("ddbh",ycDdVe.getDdbh(),"ddzt","YC1E"));
                    return true;
                }
                logger.info("************订单" + ycDdVe.getDdbh() + "退款手续费为" + ycDdVe.getTksxf() + "*********");
                if (ycDdVe.getTksxf() != null && ycDdVe.getTksxf().doubleValue() > 0) {
                    logger.info("开始更新订单{}的违约金额信息...", ycDdVe.getDdbh());
                    ycDdVe.setGyJsje(ycDdVe.getGyTksxf());
                    ycDdVe.setCgQxsj(VeDate.getNow());
                    ycDdVe.setCgJsje(ycDdVe.getTksxf());
                    ycDdVe.setPtkrje(ycDdVe.getTksxf().subtract(ycDdVe.getGyTksxf()));
                    ycDdVe.setYfje(ycDdVe.getTksxf());
                    ycDdVe.setPttdje(BigDecimal.ZERO);
                    if(StringUtils.equals(ycDdVe.getZfZt(),"1")){
                        ycDdVe.setDdzt(UsecarOrderStatusEnum.YC2O.getCode());
                    }else {
                        ycDdVe.setDdzt(UsecarOrderStatusEnum.YC2M.getCode());
                    }
                } else {
                    ycDdVe.setDdzt(UsecarOrderStatusEnum.YC1E.getCode());
                }
                ycDdVe.setCgQxsj(VeDate.getNow());
                result = ycDdService.updateYcDd(ycDdVe);
            }
            if(result){
                cpsEventPublisher.send(CpsEventEnum.SENIOR_EXECUTIVE_MONITOR, ImmutableMap.of("ddbh",ycDdVe.getDdbh(),"ddzt","YC1E"));
                cpsEventPublisher.send(CpsEventEnum.CANCEL_ORDER_REVERT_POINTS, ImmutableMap.of("ddbh",ycDdVe.getDdbh(),"ddzt","YC1E"));
            }
        } else {
            logger.info("通过订单编号未能查到数据库数据");
        }
        return result;
    }

    /**
     * 采购支付订单
     *
     * @param ddbh ddbh
     * @return 是否支付成功
     */
    public Boolean payComplete(String ddbh) {
        Boolean result = Boolean.FALSE;
        logger.info("请求CPS执行支付操作[订单编号:" + ddbh + "]");
        YcDd ycDdVe = ycDdService.selectByIdNoCache(ddbh);//获取单条 正常单信息
        if (ycDdVe != null) {
            logger.info("CPS本地订单数据支付更新完成");
            Boolean flag = false;
            if (StringUtils.isNotBlank(ycDdVe.getGyShbh()) && UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh())) {
                if (null != ycDdVe.getDdlx() && UsecarProductTypeEnum.zc.getCode().equals(ycDdVe.getDdlx())) {
                    //专车下单到供应商
                    flag = buyerBookSpeciCarService.createOrderToGys(ddbh);
                } else {
                    //接送车下单到供应商
                    flag = jscBookService.createOrderToGys(ddbh);
                }
                if(!flag){
                    try {
                        flag = true;
                        Thread.sleep(UseCarConstant.THREAD_SLEEP_TIME);//睡2s等上面的支付信息先推送到客户那边
                        /******************************支付失败调用拒单操作***********************************/
                        SellerNormalOrderOperateDTO sellerNormalOrderOperateDTO = new SellerNormalOrderOperateDTO();
                        sellerNormalOrderOperateDTO.setGyShbh(ycDdVe.getGyShbh());
                        sellerNormalOrderOperateDTO.setDdbh(ycDdVe.getDdbh());
                        sellerNormalOrderOperateDTO.setXguserxm(ycDdVe.getGyShbh());
                        sellerNormalOrderOperateDTO.setXguserid(ycDdVe.getGyShjc());
                        sellerNormalOrderOperateDTO.setJdSxf(UseCarConstant.NUMZERO);
                        sellerOrderService.refuseOrder(sellerNormalOrderOperateDTO);  //支付失败调用拒单
                        /***************************************************************************************/
                    } catch (Exception e) {
                        flag = false;
                        logger.error("订单["+ddbh+"]支付成功后,下单到供应商失败,供应商拒单异常:",e);
                    }
                }
            } else {
                //自签产品,推送给ASMS的供应商
                try {
                    flag = true;
                    logger.info("推送采购信息给供应商ASMS平台采购订单编号为：【" + ycDdVe.getDdbh() + "】");
                    buyerBookService.createOrderToAsmsSeller(ycDdVe);
                }catch(Exception e){
                    logger.error("推送采购信息给供应商ASMS平台异常", e);
                    logger.info("推送【" + ycDdVe.getDdbh() + "】推送采购信息到供应商【" + ycDdVe.getGyShbh() + "】ASMS平台出现异常：\n" + e.getMessage());
                    flag = false;
                }
            }
            result = flag;
        } else {
            logger.info("通过订单编号及版本号未能查到数据");
        }
        return result;
    }

    /**
     * 采购退款订单详情
     * @param buyerNormalOrderOperateDTO 入参
     * @return 采购退款订单详情
     */
    private BuyerNormalOrderSpVO getNormalSpec(BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO){
        BuyerNormalOrderSpVO normalSpe = new BuyerNormalOrderSpVO();
        if(StringUtils.isNotBlank(buyerNormalOrderOperateDTO.getRefundCacheId())){
            logger.info("CPS用车申请退款根据缓存id查询，缓存id为："+buyerNormalOrderOperateDTO.getRefundCacheId()
                    +"，订单编号"+buyerNormalOrderOperateDTO.getDdbh());
            try{
                BuyerNormalRefundCacheVO normalSpee = (BuyerNormalRefundCacheVO) usecarCacheService.getRefundCache(buyerNormalOrderOperateDTO.getRefundCacheId());
                normalSpe.setTksxf(normalSpee.getTksxf());
                normalSpe.setGyTksxf(normalSpee.getGyTksxf());//供应手续费
                //ASMS调用需要返回的参数
                normalSpe.setKfje(normalSpee.getTksxf().toString());//退款手续费
                normalSpe.setRefundCacheId(normalSpee.getRefundCacheId());//缓存id
                normalSpe.setQszt(normalSpee.getQszt());//link返回的取消状态
            }catch (Exception e){
                logger.error(UsecarOrderCode.UCAR_30013.getMessage(), e);
                normalSpe = getOrderDetailSpecial(buyerNormalOrderOperateDTO);
                //                throw new SystemRuntimeException(UsecarOrderCode.UCAR_30013, e);
            }
        }else{
            logger.info("CPS用车申请退款缓存id为空，重新走询价的方法");
            normalSpe = getOrderDetailSpecial(buyerNormalOrderOperateDTO);
        }
        return normalSpe;
    }

    /**
     * 采购申请退款
     *
     * @param buyerNormalOrderOperateDTO 采购操作订单DTO
     * @return 是否申请退款成功
     */
    public boolean refundOrder(BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO) {
        logger.info("进入订单[" + buyerNormalOrderOperateDTO.getDdbh() + "]的退款方法");
        Boolean result = Boolean.FALSE;
        Boolean ret = Boolean.FALSE;
        int isResult = UseCarConstant.ONE;
        BuyerNormalOrderSpVO normalSpe = getNormalSpec(buyerNormalOrderOperateDTO);
        YcDd ycDdVe = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
        BuyerNormalOrderSpVO normalSpc = BeanMapper.map(ycDdVe, BuyerNormalOrderSpVO.class);
        normalSpc.setForce(buyerNormalOrderOperateDTO.getForce());//是否强制取消，接口供应商需要用到，默认给false
        normalSpc.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy());//采购取消原因
        normalSpc.setAutotest(buyerNormalOrderOperateDTO.getAutotest());//自动化测试
        normalSpc.setJjmslb(ycDdVe.getJjmslb());
        logger.info("此订单进行申请退款操作，订单信息为："+JsonMapper.nonEmptyMapper().toJson(ycDdVe));
        //订单存在且采购商户编号一致
        if (ycDdVe != null && ycDdVe.getCgShbh().equals(buyerNormalOrderOperateDTO.getCgShbh())) {
            logger.info("此订单进行申请退款操作，订单状态为："+ycDdVe.getDdzt());
            if(StringUtils.equals(ycDdVe.getDdzt(),UsecarOrderStatusEnum.YC2C.getCode())
                    || StringUtils.equals(ycDdVe.getDdzt(),UsecarOrderStatusEnum.YC2A.getCode())
                    || StringUtils.equals(ycDdVe.getDdzt(),UsecarOrderStatusEnum.YC3D.getCode())
                    || StringUtils.equals(ycDdVe.getDdzt(),UsecarOrderStatusEnum.YC1E.getCode())
            || StringUtils.equals(ycDdVe.getDdzt(),UsecarOrderStatusEnum.YC1D.getCode())){
                logger.error(UsecarOrderCode.UCAR_30017.getMessage());
                //20200703 modify by wufeng 如果是已取消状态则直接返回成功
//                throw new SystemRuntimeException(UsecarOrderCode.UCAR_30017);
                buyerNormalOrderOperateDTO.setQszt("true");//asms返回需要取消状态，自签供应商的也需要
                buyerNormalOrderOperateDTO.setKfje(ycDdVe.getGyTksxf()==null?"0":ycDdVe.getGyTksxf().toPlainString());
                return Boolean.TRUE;
            }
            YcCp ycCp = cpService.selectYcCp(ycDdVe.getCpid());//为空为自签供应商 避免 供应商编号和接口供应商编号相同
            if (UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh()) && StringUtils.isNotBlank(ycDdVe.getGyDdbh())&&ycCp==null) {//接口供应商
                logger.info("CPS用车申请退款接口供应商走确定退款，供应商户编号为"+ycDdVe.getGyShbh()+"，供应订单编号为："+ycDdVe.getGyDdbh());
                //接口供应商取消订单时,link要区分是供应商拒单还是采购商取消，用采购退单申请人做区分
                YcDd ycDd= new YcDd();
                ycDd.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
                logger.info("退款传入的采购退单申请人为[" + buyerNormalOrderOperateDTO.getDdbh() + "]");
                if(StringUtils.isNotBlank(buyerNormalOrderOperateDTO.getCgTdsqr())){
                    ycDd.setCgTdsqr(buyerNormalOrderOperateDTO.getCgTdsqr());//采购退单申请人为当前登录人
                }else{
                    ycDd.setCgTdsqr("默认退单人");//如果为空则给默认值
                }
                logger.info("退款更新订单完毕，订单编号为[" + ycDd.getDdbh() + "]，采购退单人为：["+ ycDd.getCgTdsqr() + "]");
                //接口供应商要通过link通知接口供应商取消订单(一种是有取消公式，查询未调用link，
                // 确认时需要走link；另一种查询走了link，有手续费，确认退款走link)
                cancelApiGysOrder(normalSpc, ycDdVe);
                if ("true".equals(normalSpc.getQszt())) {
                    logger.info("CPS用车申请退款接口供应商走确定退款，取消订单成功");
                    ret = Boolean.TRUE;
                    ycDdVe.setDdzt(UsecarOrderStatusEnum.YC3A.getCode());// 订单状态改为已申请-待审核
                }else{
                    result = Boolean.FALSE;
                    logger.info("CPS接口供应商订单link申请退款失败");
                }
            } else {
                logger.info("CPS用车申请退款自签供应商确定退款");
                ret = Boolean.TRUE;
                ycDdVe.setDdzt(UsecarOrderStatusEnum.YC3A.getCode());// 订单状态改为已申请-待审核
                normalSpe.setQszt("true");//asms返回需要取消状态，自签供应商的也需要
            }
            if (ret) {//接口供应商取消订单成功后更新，自签供应商直接更新
                ycDdVe.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
                ycDdVe.setVersion(buyerNormalOrderOperateDTO.getVersion()); //版本号加"1"
                ycDdVe.setCgTdsqr(buyerNormalOrderOperateDTO.getCgTdsqr());//采购退单申请人编号
                ycDdVe.setCgTdsqsj(VeDate.getNow());//采购退单申请时间
                ycDdVe.setCgQxsj(VeDate.getNow());
                //退款时取消原因不能为空，所以在这里做判断，如果为空给个默认值
                if(StringUtils.isBlank(buyerNormalOrderOperateDTO.getCgQxyy())){
                    buyerNormalOrderOperateDTO.setCgQxyy("行程临时变更");
                }
                ycDdVe.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy());//退款原因
                logger.info("返回给ASMS的退款手续费:Tksxf"+normalSpc.getTksxf());//ASMS调用需要返回的参数
                if(normalSpc.getTksxf()==null){
                    buyerNormalOrderOperateDTO.setKfje("");
                }else{
                    buyerNormalOrderOperateDTO.setKfje(normalSpc.getTksxf().toString());//退款手续费
                }
                buyerNormalOrderOperateDTO.setCpid(normalSpe.getRefundCacheId());//缓存id
                buyerNormalOrderOperateDTO.setQszt(normalSpe.getQszt());//link返回的取消状态
                result = ycDdService.updateYcDd(ycDdVe);//更新正常单信息
                logger.info("CPS接口供应商申请退款更新订单列表:result"+result);
                //更新成功之后接口供应商调用退款
                if (result) {
                    //接口供应商调用退款(不再控制gyDdbh是否为空，eg.八点预约八点半使用的滴滴的车，
                    // 此时订单未下到供应商，所以无供应订单编号，在八点到八点半之间申请退款，直接全额退掉，走审核退款)
                    //                    if (UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh()) && StringUtils.isNotBlank(ycDdVe.getGyDdbh())) {
                    if (UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh())&&ycCp==null) {
                        logger.info("CPS接口供应商申请退款开始走退款");
                        try {
                            //退款
                            //                            isResult = usecarPayHandleService.refund(ycDdVe.getDdbh(),
                            // ycDdVe.getCgShbh(),ycDdVe.getCgTdsqr(),UseCarConstant.NUMONE,UseCarConstant.YC_PAY_PT_B);
                            SellerRefundOrderOprateDTO sellerRefundOrderOperateDTO =new SellerRefundOrderOprateDTO();
                            sellerRefundOrderOperateDTO.setDdbh(ycDdVe.getDdbh());
                            sellerRefundOrderOperateDTO.setGyShbh(ycDdVe.getGyShbh());
                            sellerRefundOrderOperateDTO.setXguserid(ycDdVe.getCgTdsqr());
                            logger.info("CPS接口供应商申请退款走审核退款，订单编号："+sellerRefundOrderOperateDTO.getDdbh());
                            isResult = sellerRefundOrderService.checkRefund(sellerRefundOrderOperateDTO);
                            logger.info("CPS接口供应商申请审核退款走退款后返回的值："+isResult);
                        } catch (Exception e) {
                            logger.error(UsecarOrderCode.UCAR_30012.getMessage(), e);
                            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30012, e);
                        }
                    }
                    logger.info("CPS本地订单数据申请退款更新完成");
                    /*************** 推送供应商退单申请给ASMS-start平台 ***************************/
                    //上面调退款成功后订单状态改了需要重新查一下去新的订单状态推送到ASMS
                    boolean isApi = UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh());
                    if(!isApi) {
                        YcDd ycDd = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
                        try {
                            ApplyRefundAsmsOrderToSellerDTO applyRefundAsmsOrderDTO = new ApplyRefundAsmsOrderToSellerDTO();
                            applyRefundAsmsOrderDTO.setGyShbh(ycDdVe.getGyShbh());
                            applyRefundAsmsOrderDTO.setGyDdbh(ycDdVe.getGyDdbh());
                            applyRefundAsmsOrderDTO.setBzbz(ycDdVe.getDdbz());
                            applyRefundAsmsOrderDTO.setTksxf(converToDouble(ycDdVe.getTksxf()));
                            applyRefundAsmsOrderDTO.setGyPtzt(ycDd.getDdzt()); //要取最新的订单状态
                            sellerNoticeService.applyRefundAsmsOrderNotify(applyRefundAsmsOrderDTO);
                            logger.info("推送订单[" + ycDdVe.getDdbh() + "]供应商退单申请给采购商ASMS平台完成！");
                        } catch (Exception e) {
                            logger.error("推送采购信息给供应商ASMS平台异常", e);
                            logger.info("推送订单[" + ycDdVe.getDdbh() + "]供应商退单申请给采购商ASMS平台失败：\n" + e.getMessage());
                        }
                    }
                    /*************** 推送供应商退单申请给ASMS-end平台 ***************************/

                } else {
                    logger.info("CPS本地订单数据申请退款更新失败");
                }
            }
        } else {
            logger.info("通过订单编号及版本号未能查到数据");
        }
        if(isResult!=UseCarConstant.ONE){//退款失败
            result =Boolean.FALSE;
        }
        logger.info("CPS采购申请退款后返回的值："+result+",取消状态："+buyerNormalOrderOperateDTO.getQszt()+",dto："+buyerNormalOrderOperateDTO);
        return result;
    }

    /**
     * 申请退款 页面值的获取
     *
     * @param buyerNormalOrderOperateDTO 申请退款 页面展示需要传的参
     * @return BuyerNormalOrderSpVo
     */
    public BuyerNormalOrderSpVO getOrderDetailSpecial(BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO) {
        logger.info("进入订单[" + buyerNormalOrderOperateDTO.getDdbh() + "]的预退款查询方法,参数："+buyerNormalOrderOperateDTO);
        //接口供应商取消订单时,link要区分是供应商拒单还是采购商取消，用采购退单申请人做区分
        YcDd ycDd= new YcDd();
        ycDd.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
        logger.info("预取消传入的采购退单申请人为[" + buyerNormalOrderOperateDTO.getDdbh() + "]");
        if(StringUtils.isNotBlank(buyerNormalOrderOperateDTO.getCgTdsqr())){
            ycDd.setCgTdsqr(buyerNormalOrderOperateDTO.getCgTdsqr());//采购退单申请人为当前登录人
        }else{
            ycDd.setCgTdsqr("默认退单人");//如果为空则给默认值
        }
        /***********************link不需要传入采购退单申请人字段2018/2/5修改************************************/
//        ycDdService.updateYcDd(ycDd);//更新正常单信息
        /***********************************************************/
        logger.info("预取消更新订单完毕，订单编号为[" + ycDd.getDdbh() + "]，采购退单人为：["+ ycDd.getCgTdsqr() + "]");
        //获取单条正常单
        YcDd ycDdVe = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
        logger.info("预取消根据订单编号查询数据，订单编号为[" + ycDd.getDdbh() + "]，查询后的ycDdVe：["+ ycDdVe + "]");
        if (ycDdVe == null) {
            logger.info("预申请退款的订单对应的编号为：【" + buyerNormalOrderOperateDTO.getDdbh() + "】");
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10007, buyerNormalOrderOperateDTO.getDdbh());
        }
        String ddzt = ycDdVe.getDdzt();
        Boolean isTrue =  UsecarOrderStatusEnum.YC1H.getOrderStatusCode().equals(ddzt)||UsecarOrderStatusEnum.YC1F
                .getOrderStatusCode().equals(ddzt)||UsecarOrderStatusEnum.YC1C.getOrderStatusCode().equals(ddzt)
                || UsecarOrderStatusEnum.YC2B.getOrderStatusCode().equals(ddzt)
                || UsecarOrderStatusEnum.YC2D.getOrderStatusCode().equals(ddzt) || UsecarOrderStatusEnum.YC2F.getOrderStatusCode().equals(ddzt)
                || UsecarOrderStatusEnum.YC2G.getOrderStatusCode().equals(ddzt) || UsecarOrderStatusEnum.YC2H.getOrderStatusCode().equals(ddzt);
        if(isTrue){
            BuyerNormalOrderSpVO normalSpc = BeanMapper.map(ycDdVe, BuyerNormalOrderSpVO.class);
            normalSpc.setForce(buyerNormalOrderOperateDTO.getForce());//是否强制取消，接口供应商需要用到，默认给false
            normalSpc.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy());//采购取消原因
            //asms返回需要取消状态，自签供应商的也需要,默认值给false
            normalSpc.setQszt("false");
            normalSpc.setJjmslb(ycDdVe.getJjmslb());
            tkjs(normalSpc, buyerNormalOrderOperateDTO,ycDdVe);
            //ASMS调用需要返回的参数
            logger.info("ASMS调用需要返回的参数,退款手续费[" + normalSpc.getTksxf() + "]" + normalSpc);
            if(normalSpc.getTksxf()==null){
                buyerNormalOrderOperateDTO.setKfje("");
            }else{
                buyerNormalOrderOperateDTO.setKfje(normalSpc.getTksxf().toString());//退款手续费
            }
            buyerNormalOrderOperateDTO.setCpid(normalSpc.getRefundCacheId());//缓存id
            buyerNormalOrderOperateDTO.setQszt(normalSpc.getQszt());//link返回的取消状态
            return normalSpc;
        }else{
            logger.info("当前订单状态{}无需调用取消",ddzt);
            BuyerNormalOrderSpVO normalSpc = BeanMapper.map(ycDdVe, BuyerNormalOrderSpVO.class);
            normalSpc.setForce("true");
            normalSpc.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy());
            //asms返回需要取消状态，自签供应商的也需要,默认值给false
            logger.info("*******预取消返回数据为：{}**********",JsonMapper.nonEmptyMapper().toJson(normalSpc));
            buyerNormalOrderOperateDTO.setForce("true");
            buyerNormalOrderOperateDTO.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy());
            buyerNormalOrderOperateDTO.setQszt("true");
            return normalSpc;
//            return null;
        }

    }

    /**
     * 计算退款费用
     *  @param normalSpc                  申请退款需要传的参数
     * @param buyerNormalOrderOperateDTO 点击申请退款操作带过来的参数
     * @param ycDdVe
     */
    public void tkjs(BuyerNormalOrderSpVO normalSpc, BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO, YcDd ycDdVe) {
        logger.info("进入订单[" + normalSpc.getDdbh() + "]的【tkjs()】方法,开始计算退款相关费用");
        normalSpc.setAutotest(buyerNormalOrderOperateDTO.getAutotest());
        //2020-03-07 解决vdms:用车取消手续费改成以供应商返回的为准
        if (UsecarGysApiEnum.checkIsApiGys(normalSpc.getGyShbh()) && StringUtils.isNotBlank(normalSpc.getGyDdbh())) {
            logger.info("供应商【" + normalSpc.getGyShbh() + "】为接口供应商且当前订单没有取消公式，将调用【cancelApiGysOrder()】方法获取其订单[" + normalSpc.getGyDdbh() + "]的退款手续费。");
            //请求接口，获取取消费用(分为专快车、接送车，还分为预取消和确定取消)
            cancelApiGysOrder(normalSpc, ycDdVe);
            logger.info("调用接口后返回的手续费等信息："+normalSpc);
            //link查询返回的状态为未取消（分为1.无预取消接口的供应商，退款手续费为零，界面显示为退款手续费以实际退款为准；2.退款手续费不为零的时候直接显示金额）
            if (UseCarConstant.FALSE.equals(normalSpc.getQszt()) || StringUtils.isBlank(normalSpc.getQszt())) {
                logger.info("订单未被取消返回值为："+normalSpc);
//                String tksxf = normalSpc.getKfje() == null ? UseCarConstant.NUMZERO : normalSpc.getKfje();//
//                normalSpc.setTksxf(new BigDecimal(tksxf));//退款手续费
                if(normalSpc.getTksxf()==null){
                    normalSpc.setTksxf(BigDecimal.ZERO);
                }
                normalSpc.setCgFyje(BigDecimal.ZERO);//采购返佣金额
                normalSpc.setGyTksxf(normalSpc.getTksxf());//供应退款手续费
                normalSpc.setTkje(normalSpc.getFkje().subtract(normalSpc.getTksxf()));//退款金额
            } else if (UseCarConstant.FALSE.equals(normalSpc.getForce()) && UseCarConstant.TRUE.equals(normalSpc.getQszt())) {
                //如果link查询的取消状态为已取消，说明供应商已取消订单
                logger.info("订单被取消返回值为："+normalSpc);
                Boolean result = Boolean.FALSE;
                ycDdVe = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
                if (ycDdVe != null && ycDdVe.getCgShbh().equals(buyerNormalOrderOperateDTO.getCgShbh())) {
                    ycDdVe.setDdzt(UsecarOrderStatusEnum.YC3A.getCode());// 订单状态改为已申请-待审核
                    ycDdVe.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
                    ycDdVe.setVersion(buyerNormalOrderOperateDTO.getVersion()); //版本号加"1"
                    ycDdVe.setCgTdsqr(buyerNormalOrderOperateDTO.getCgTdsqr());//采购退单申请人编号
                    //退款时取消原因不能为空，所以在这里做判断，如果为空给个默认值
                    if(StringUtils.isBlank(buyerNormalOrderOperateDTO.getCgQxyy())){
                        buyerNormalOrderOperateDTO.setCgQxyy("系统取消");
                    }
                    ycDdVe.setCgQxyy(buyerNormalOrderOperateDTO.getCgQxyy());//退款原因
                    ycDdVe.setCgTdsqsj(VeDate.getNow());//采购退单申请时间
                    ycDdVe.setCgQxsj(VeDate.getNow());
//                    ycDdVe.setTksxf(normalSpc.getTksxf());//采购退款手续费
//                    ycDdVe.setGyTksxf(normalSpc.getTksxf());//供应手续费
                    ycDdVe.setCgFyje(BigDecimal.ZERO);//采购返佣金额
                    result = ycDdService.updateYcDd(ycDdVe);//更新正常单信息
                    if (result) {
                        logger.info("CPS接口供应商申请退款开始走退款");
                        try {
                            //退款
                            //int isResult = usecarPayHandleService.refund(ycDdVe.getDdbh(),ycDdVe.getCgShbh(),
                            // ycDdVe.getCgTdsqr(),UseCarConstant.NUMONE,UseCarConstant.YC_PAY_PT_B);
                            SellerRefundOrderOprateDTO sellerRefundOrderOperateDTO =new SellerRefundOrderOprateDTO();
                            sellerRefundOrderOperateDTO.setDdbh(ycDdVe.getDdbh());
                            sellerRefundOrderOperateDTO.setGyShbh(ycDdVe.getGyShbh());
                            sellerRefundOrderOperateDTO.setXguserid(ycDdVe.getCgTdsqr());
                            logger.info("CPS接口供应商申请退款走审核退款，订单编号："+sellerRefundOrderOperateDTO.getDdbh());
                            int isResult = sellerRefundOrderService.checkRefund(sellerRefundOrderOperateDTO);
                            logger.info("CPS接口供应商申请退款走退款后返回的值："+isResult);
                        } catch (Exception e) {
                            logger.error(UsecarOrderCode.UCAR_30012.getMessage(), e);
                            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30012, e);
                        }
                        logger.info("CPS本地订单数据接口供应商已取消订单的订单申请退款更新完成");
                    } else {
                        logger.info("CPS本地订单数据接口供应商已取消订单的订单申请退款更新失败");
                    }
                } else {
                    logger.info("通过订单编号及版本号未能查到数据");
                }
            }
        } else {//没有取消规则的情况下,且不是接口供应商
            logger.info("没有取消规则且不是接口供应商订单，或者无接口供应商订单编号!");
            normalSpc.setTksxf(BigDecimal.ZERO);//不收取退款手续费
            normalSpc.setCgFyje(BigDecimal.ZERO);//采购返佣金额
            normalSpc.setGyTksxf(BigDecimal.ZERO);//供应退款手续费
            normalSpc.setTkje(normalSpc.getFkje());//全额退款
        }
        logger.info("申请退款数据开始放入缓存中!");
        //将算出的手续费等费用放进缓存中
        normalSpc.setRefundCacheId(VeDate.getNo(UseCarConstant.SEVEN));//TODO 主键暂时用VEDATE生成
        //定义缓存
        BuyerNormalRefundCacheVO buyerNormalRefundCacheVO = new BuyerNormalRefundCacheVO();
        buyerNormalRefundCacheVO.setRefundCacheId(normalSpc.getRefundCacheId());//缓存id
        buyerNormalRefundCacheVO.setCgFyje(normalSpc.getCgFyje());//采购返佣金额
        buyerNormalRefundCacheVO.setTksxf(normalSpc.getTksxf());//退款手续费
        buyerNormalRefundCacheVO.setGyTksxf(normalSpc.getGyTksxf());//供应退款手续费
        buyerNormalRefundCacheVO.setTkje(normalSpc.getTkje());//退款金额
        buyerNormalRefundCacheVO.setQszt(normalSpc.getQszt());//取消状态
        //数据放入缓存中
        usecarCacheService.putRefundCache(buyerNormalRefundCacheVO.getRefundCacheId(), buyerNormalRefundCacheVO);
        logger.info("申请退款数据放入缓存中结束!");
    }
    
    /**
     * 有取消公式的计算退款手续费
     *
     * @param normalSpc 订单退款
     * @param rate      取消费用比率
     */
    private void setSxf(BuyerNormalOrderSpVO normalSpc, BigDecimal rate) {
        logger.info("取消公式为,开始计算");
        //采购退款手续费  由 【yc_dd】采购套餐金额与计算取消公式的值
        normalSpc.setTksxf(normalSpc.getYgje().multiply(rate));
        logger.info("采购退款手续费为【" + normalSpc.getTksxf() + "】");
        //退款金额
        normalSpc.setTkje(normalSpc.getFkje().subtract(normalSpc.getTksxf()));
        logger.info("退款金额为【" + normalSpc.getTkje() + "】");
        //供应手续费
        normalSpc.setGyTksxf(normalSpc.getGyJsje().multiply(rate));
        logger.info("供应手续费为【" + normalSpc.getGyTksxf() + "】");
    }

    /**
     * 接口供应商调用link
     *
     * @param normalSpc 订单退款
     * @return normalSpc link返回
     */
    public BuyerNormalOrderSpVO cancelApiGysOrder(BuyerNormalOrderSpVO normalSpc, YcDd ycDdVe) {
        logger.info("****申请退款接口供应商走link,订单编号:"+normalSpc.getDdbh());
        //预退款查询
        if (UseCarConstant.FALSE.equals(normalSpc.getForce())) {
            logger.info("申请退款接口供应商走link询价,订单编号:"+normalSpc.getDdbh()+",是否预退款:"+normalSpc.getForce());
            //专快车
            if (UsecarProductTypeEnum.zc.getCode().equals(normalSpc.getDdlx().getCode()) || SFC.getCode().equals(normalSpc.getDdlx().getCode())) {
                logger.info("申请退款接口供应商走link专快车询价,订单编号:"+normalSpc.getDdbh()+",订单类型:"+normalSpc.getDdlx().getCode());
                LinkQueryCancelStatusSpecialOrderDTO specialOrderDTO = new LinkQueryCancelStatusSpecialOrderDTO();
                specialOrderDTO.setDdbh(normalSpc.getGyDdbh());//供应订单编号
                specialOrderDTO.setCpsDdbh(normalSpc.getDdbh());//订单编号
                specialOrderDTO.setSfqzqx(normalSpc.getForce());//是否强制取消
                specialOrderDTO.setShid(normalSpc.getGyShbh());//供应商户id
                specialOrderDTO.setShjc(normalSpc.getGyShjc());//供应商户简称
                specialOrderDTO.setCgshbh(normalSpc.getCgShbh());//采购商户编号
                specialOrderDTO.setCgshjc(normalSpc.getCgShjc());//采购商户简称
                specialOrderDTO.setDdlx(normalSpc.getDdlx().getCode());//服务类型ddlx
                specialOrderDTO.setAutotest(normalSpc.getAutotest());//自动化测试
                specialOrderDTO.setJjmslb(normalSpc.getJjmslb());
                specialOrderDTO.setFrombzcps(normalSpc.getBzcgs());
                specialOrderDTO.setCgQxyy(normalSpc.getCgQxyy());
                specialOrderDTO.setMddbh(ycDdVe.getpDdbh());
                specialOrderDTO.setGysMddbh(ycDdVe.getGysMddbh());
                setChannelType(specialOrderDTO,ycDdVe);
                //调用专快车link预退款查询
                RestResponse<LinkQueryCancelStatusSpecialOrderVO> specialOrderVO = iLinkSpecialCarService.cancelOrderState(specialOrderDTO);
                errorEventPublisher.preCancel(specialOrderDTO,specialOrderDTO);
                if(specialOrderVO==null){
                    throw new RuntimeException(UsecarOrderCode.UCAR_30014.getMessage());
                }
                //接口请求成功返回取消状态有值
                if(StringUtils.equals(specialOrderVO.getStatus(), ResultCode.OK.getCode()) && specialOrderVO.getResult() != null){
                    logger.info("申请退款接口供应商走link专快车询价结果,退款手续费扣费金额:"+specialOrderVO.getResult().getKfje()
                            +",取消状态:"+specialOrderVO.getResult().getQszt());
                    normalSpc.setTksxf(specialOrderVO.getResult().getKfje());//退款手续费扣费金额
                    normalSpc.setQszt(specialOrderVO.getResult().getQszt());//取消状态
                }else{
                    normalSpc.setTksxf(null);//退款手续费扣费金额
                    normalSpc.setQszt(null);//取消状态
                }
            } else {//接送车
                logger.info("申请退款接口供应商走link接送车询价,订单编号:"+normalSpc.getDdbh()+",订单类型:"+normalSpc.getDdlx().getCode());
                LinkQueryCancelStatusPickUpOrderDTO pickUpOrderDTO = new LinkQueryCancelStatusPickUpOrderDTO();
                pickUpOrderDTO.setDdbh(normalSpc.getGyDdbh());//供应订单编号
                pickUpOrderDTO.setCpsDdbh(normalSpc.getDdbh());//订单编号
                pickUpOrderDTO.setShid(normalSpc.getGyShbh());//供应商户id
                pickUpOrderDTO.setShjc(normalSpc.getGyShjc());//供应商户简称
                pickUpOrderDTO.setCgshbh(normalSpc.getCgShbh());//采购商户编号
                pickUpOrderDTO.setCgshjc(normalSpc.getCgShjc());//采购商户简称
                pickUpOrderDTO.setDdlx(normalSpc.getDdlx().getCode());//服务类型ddlx
                pickUpOrderDTO.setAutotest(normalSpc.getAutotest());//自动化测试
                pickUpOrderDTO.setJjmslb(normalSpc.getJjmslb());
                pickUpOrderDTO.setFrombzcps(normalSpc.getBzcgs());
                pickUpOrderDTO.setCgqxyy(normalSpc.getCgQxyy());
                pickUpOrderDTO.setMddbh(ycDdVe.getpDdbh());
                pickUpOrderDTO.setGysMddbh(ycDdVe.getGysMddbh());
                setChannelType(pickUpOrderDTO,ycDdVe);
                //调用接送车link预退款查询
                RestResponse<LinkQueryCancelStatusPickUpOrderVO> pickUpOrderVO = iLinkPickUpCarService.queryCancelStatus(pickUpOrderDTO);
                if(pickUpOrderVO==null){
                    throw new RuntimeException(UsecarOrderCode.UCAR_30014.getMessage());
                }
                if(StringUtils.equals(pickUpOrderVO.getStatus(), ResultCode.OK.getCode()) && pickUpOrderVO.getResult() != null){
                    logger.info("申请退款接口供应商走link接送车询价结果,退款手续费扣费金额:"+pickUpOrderVO.getResult().getKfje()
                            +",取消状态:"+pickUpOrderVO.getResult().getQszt());
                    normalSpc.setTksxf(pickUpOrderVO.getResult().getKfje());//退款手续费扣费金额
                    normalSpc.setQszt(pickUpOrderVO.getResult().getQszt());//取消状态
                }else{
                    normalSpc.setTksxf(null);//退款手续费扣费金额
                    normalSpc.setQszt(null);//取消状态
                }
            }
        } else if (UseCarConstant.TRUE.equals(normalSpc.getForce())) {//退款
            logger.info("申请退款接口供应商走link退款,订单编号:"+normalSpc.getDdbh()+",是否预退款:"+normalSpc.getForce());
            //专快车
            if (UsecarProductTypeEnum.zc.getCode().equals(normalSpc.getDdlx().getCode()) || SFC.getCode().equals(normalSpc.getDdlx().getCode())) {
                logger.info("申请退款接口供应商走link专快车退款,订单编号:"+normalSpc.getDdbh()+",订单类型:"+normalSpc.getDdlx().getCode());
                LinkCancelSpecialOrderDTO specialOrderDTO = new LinkCancelSpecialOrderDTO();
                specialOrderDTO.setDdbh(normalSpc.getGyDdbh());//供应订单编号
                specialOrderDTO.setCpsDdbh(normalSpc.getDdbh());//订单编号
                specialOrderDTO.setSfqzqx(normalSpc.getForce());//是否强制取消
                specialOrderDTO.setCgQxyy(normalSpc.getCgQxyy());//采购取消原因
                specialOrderDTO.setShid(normalSpc.getGyShbh());//供应商户id
                specialOrderDTO.setShjc(normalSpc.getGyShjc());//供应商户简称
                specialOrderDTO.setCgshbh(normalSpc.getCgShbh());//采购商户编号
                specialOrderDTO.setCgshjc(normalSpc.getCgShjc());//采购商户简称
                specialOrderDTO.setDdlx(normalSpc.getDdlx().getCode());//服务类型ddlx
                specialOrderDTO.setAutotest(normalSpc.getAutotest());//自动化测试
                specialOrderDTO.setJjmslb(normalSpc.getJjmslb());
                specialOrderDTO.setNotCancel(normalSpc.getNotCancel());
                specialOrderDTO.setFrombzcps(normalSpc.getBzcgs());
                specialOrderDTO.setContactPhone(normalSpc.getLxrdh());
                specialOrderDTO.setLoginPhone(normalSpc.getLxrdh());
                specialOrderDTO.setMddbh(ycDdVe.getpDdbh());
                specialOrderDTO.setGysMddbh(ycDdVe.getGysMddbh());
                //调用专快车link退款
                //{"status":1,"msg":"无效订单","sfxxkf":null,"kfje":null,"qszt":"true"}
                RestResponse<LinkCancelSpecialOrderVO> specialOrderVO = defaultCancelService.cancelOrder(specialOrderDTO, ycDdVe);
                errorEventPublisher.cancel(specialOrderDTO,specialOrderVO);
                if (specialOrderVO == null || specialOrderVO.getResult() == null
                        || "false".equals(specialOrderVO.getResult().getQszt())) {
                    throw new RuntimeException(UsecarOrderCode.UCAR_30015.getMessage());
                }
                logger.info("申请退款接口供应商走link专快车退款结果,退款手续费扣费金额:"+specialOrderVO.getResult().getKfje()
                        +",取消状态:"+specialOrderVO.getResult().getQszt());
                if(specialOrderVO.getResult().getKfje()!=null){
                    normalSpc.setTksxf(specialOrderVO.getResult().getKfje());//退款手续费扣费金额
                }else{
                    /*******s*********!!!!!!!!!处理ASMS提交采购方调用CPS时force为true，无缓存id，接口供应商返回取消费用为null!!!!!!!!********************/
                    normalSpc.setTksxf(BigDecimal.ZERO);
                }
                normalSpc.setQszt(specialOrderVO.getResult().getQszt());//取消状态
            } else {//接送车
                logger.info("申请退款接口供应商走link接送车退款,订单编号:"+normalSpc.getDdbh()+",订单类型:"+normalSpc.getDdlx().getCode());
                LinkCancelPickUpOrderDTO pickUpOrderDTO = new LinkCancelPickUpOrderDTO();
                pickUpOrderDTO.setDdbh(normalSpc.getGyDdbh());//供应订单编号
                pickUpOrderDTO.setCpsDdbh(normalSpc.getDdbh());//订单编号
                pickUpOrderDTO.setForce(normalSpc.getForce());//是否强制取消
                pickUpOrderDTO.setCgqxyy(normalSpc.getCgQxyy());//采购取消原因
                pickUpOrderDTO.setShid(normalSpc.getGyShbh());//供应商户id
                pickUpOrderDTO.setShjc(normalSpc.getGyShjc());//供应商户简称
                pickUpOrderDTO.setCgshbh(normalSpc.getCgShbh());//采购商户编号
                pickUpOrderDTO.setCgshjc(normalSpc.getCgShjc());//采购商户简称
                pickUpOrderDTO.setDdlx(normalSpc.getDdlx().getCode());//服务类型ddlx
                pickUpOrderDTO.setAutotest(normalSpc.getAutotest());//自动化测试
                pickUpOrderDTO.setJjmslb(normalSpc.getJjmslb());
                pickUpOrderDTO.setNotCancel(normalSpc.getNotCancel());
                pickUpOrderDTO.setFrombzcps(normalSpc.getBzcgs());
                pickUpOrderDTO.setLoginPhone(normalSpc.getLxrdh());
                pickUpOrderDTO.setMddbh(ycDdVe.getpDdbh());
                pickUpOrderDTO.setGysMddbh(ycDdVe.getGysMddbh());
                setChannelType(pickUpOrderDTO,ycDdVe);
                //调用接送车link退款
                RestResponse<LinkCancelPickUpOrderVO> pickUpOrderVO = iLinkPickUpCarService.cancelOrder(pickUpOrderDTO);
                ycSupplierInterfaceCountService.logRequest(pickUpOrderDTO.getShid(), CANCEL, pickUpOrderVO.getResult());
                if (pickUpOrderVO == null || pickUpOrderVO.getResult() == null
                        || "false".equals(pickUpOrderVO.getResult().getQszt())) {
                    throw new RuntimeException(UsecarOrderCode.UCAR_30015.getMessage());
                }
                logger.info("申请退款接口供应商走link接送车退款结果,退款手续费扣费金额:"+pickUpOrderVO.getResult().getKfje()
                        +",取消状态:"+pickUpOrderVO.getResult().getQszt());
                if(pickUpOrderVO.getResult().getKfje()!=null){
                    normalSpc.setTksxf(pickUpOrderVO.getResult().getKfje());//退款手续费扣费金额
                }else{
                    //拒绝取消(锦华有这个状态，拒绝取消的情况视为操作取消的客户需要全损)
                    if(pickUpOrderVO.getResult().getStatus()==UseCarConstant.ERROR_CANNOT){
                        //退款手续费为供应结算金额
                        normalSpc.setTksxf(normalSpc.getGyJsje());
                    }
                    /****************处理ASMS提交采购方调用CPS时force为true，无缓存id，接口供应商返回取消费用为null********************/
                    normalSpc.setTksxf(BigDecimal.ZERO);
                }
                normalSpc.setQszt(pickUpOrderVO.getResult().getQszt());//取消状态
            }
        }
        YcDd dd = ycDdService.selectYcDd(normalSpc.getDdbh());
        if(UseCarConstant.TRUE.equals(normalSpc.getForce()) && StringUtils.equals(normalSpc.getQszt(),"true")){
            YcDd ycDd = new YcDd();
            ycDd.setCgQxsj(VeDate.getNow());
            ycDd.setCgQxyy(normalSpc.getCgQxyy());
            ycDd.setDdbh(normalSpc.getDdbh());
            if(normalSpc.getTksxf() != null && normalSpc.getTksxf().doubleValue()>0) {
                ycDd.setGyTksxf(normalSpc.getTksxf());
                ycDd.setTksxf(normalSpc.getTksxf());
                if (dd != null && StringUtils.equals(dd.getZfZt(), "1")) {
                    ycDd.setDdzt(UsecarOrderStatusEnum.YC2O.getCode());
                } else {
                    ycDd.setDdzt(UsecarOrderStatusEnum.YC2M.getCode());
                }
                ycDd.setCgJsje(ycDd.getTksxf());
                ycDd.setGyJsje(ycDd.getTksxf());
            }else{
                if(StringUtils.equals(dd.getZfZt(),"1")){
                    ycDd.setDdzt(UsecarOrderStatusEnum.YC3C.getCode());
                }else {
                    ycDd.setDdzt(UsecarOrderStatusEnum.YC1E.getCode());
                }
            }
            ycDd.setCgQxsj(VeDate.getNow());
            ycDdService.updateYcDd(ycDd);
            YcDdFz ddFzBean = ycDdFzService.selectYcDdFzByDdbh(ycDd.getDdbh());
            if(ddFzBean != null){
                ddFzBean.setBcJe(BigDecimal.ZERO);
                ddFzBean.setGyFzje(normalSpc.getTksxf());
                ddFzBean.setPtSxf(BigDecimal.ZERO);
                ycDdFzService.updateYcDdFz(ddFzBean);
            }
        }
        String qxlx = StringUtils.equals(normalSpc.getForce(),UseCarConstant.TRUE) ? "强制取消":"预取消";
        logger.info("ddbh:【"+normalSpc.getDdbh()+"】申请"+qxlx+"接口供应商走link专快车接送车完毕,返回信息为："+JsonMapper.nonEmptyMapper().toJson(normalSpc));
        return normalSpc;
    }

    /**
     * 采购服务完成
     *
     * @param buyerNormalOrderOperateDTO 采购操作订单DTO
     * @return 采购服务完成是否成功
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Boolean serveComplete(BuyerNormalOrderOperateDTO buyerNormalOrderOperateDTO) {
        Boolean result = Boolean.FALSE;
        logger.warn("订单["+buyerNormalOrderOperateDTO.getDdbh()+"]进入采购服务完成方法");
        YcDd ycDdVe = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
        if (ycDdVe != null && ycDdVe.getCgShbh().equals(buyerNormalOrderOperateDTO.getCgShbh())) {
            try {
                logger.info("用车采购服务完成修改前的订单状态[" + ycDdVe.getDdzt() + "]");
                boolean isXyc = "1".equals(ycDdVe.getFfgz()) && !StringUtils.equals("1",ycDdVe.getZfZt());
                if(!UsecarOrderStatusEnum.YC4B.getCode().equals(ycDdVe.getDdzt()) && !isXyc){
                    logger.warn("订单["+ycDdVe.getDdbh()+"]此时的订单状态是:{}",ycDdVe.getDdzt());
                    ycDdVe.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
                    ycDdVe.setVersion(buyerNormalOrderOperateDTO.getVersion()); //版本号加"1"
                    ycDdVe.setXcjssj(VeDate.getStringDate());//行程结束时间
                    ycDdVe.setDdzt(UsecarOrderStatusEnum.YC4A.getCode());
                    result = ycDdService.updateYcDd(ycDdVe);//更新正常单信息
                    logger.warn("订单["+ycDdVe.getDdbh()+"]修改了订单状态:"+ycDdVe.getDdzt());
                    int fzResult = usecarPayHandleService.orderFz(ycDdVe.getDdbh(),UseCarConstant.YC_PAY_PT_B,UseCarConstant.NUMONE);
                    if (fzResult == UseCarConstant.SUCCESS) {
                        result = true;
                        logger.info("订单编号[" + ycDdVe.getDdbh() + "]调用分账成功");
                        try {
                            sendConfirmOrderInfoToBuyerAsms(ycDdVe,Boolean.TRUE);
                        }catch (Exception e){
                            logger.error("用车完成分账成功后，推送信息到采购商ASMS平台失败",e);
                        }
                        /**用车完成分账成功---推送给供应商***/
                        try {
                            if (!UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh())) {
                                ConfirmOrderNotifyToSellerDTO confirmOrderNotifyDTO = new ConfirmOrderNotifyToSellerDTO();
                                confirmOrderNotifyDTO.setGyShbh(ycDdVe.getGyShbh());
                                confirmOrderNotifyDTO.setGyDdbh(ycDdVe.getGyDdbh());
                                confirmOrderNotifyDTO.setGyPtzt(UsecarOrderStatusEnum.YC4B.getCode());
                                sellerNoticeService.confirmOrderNotify(confirmOrderNotifyDTO);
                                logger.info("订单{}用车完成并分账成功推送给供应商{}完成！",ycDdVe.getDdbh(),ycDdVe.getGyShbh());
                            }
                        } catch (Exception e) {
                            logger.error("订单{}用车完成并分账成功推送给供应商{}失败！",ycDdVe.getDdbh(),ycDdVe.getGyShbh(),e);
                        }
                    } else {
                        if(isXyc){
                            ycDdVe.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
                            ycDdVe.setDdzt(UsecarOrderStatusEnum.YC4C.getCode());
                            result = ycDdService.updateYcDd(ycDdVe);//更新正常单信息
                            logger.warn("先用车，订单["+ycDdVe.getDdbh()+"]修改了订单状态:"+ycDdVe.getDdzt());
                        }
                        result = false;
                        logger.info("订单编号[" + ycDdVe.getDdbh() + "]启动分账请求失败");
                        try {
                            sendConfirmOrderInfoToBuyerAsms(ycDdVe,Boolean.FALSE);
                        } catch (Exception eb) {
                            logger.error("["+ycDdVe.getDdbh()+"]采购服务完成推送相关信息给采购商["+ycDdVe.getCgShbh()+"]异常!",eb);
                            logger.info("["+ycDdVe.getDdbh()+"]采购服务完成推送相关信息给采购商["+ycDdVe.getCgShbh()+"]异常!");
                        }
                        /*************** 采购服务完成，分账失败后推送数据到供应商ASMS平台-start ***************************/
                        try {
                            if (!UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh())) {
                                ConfirmOrderNotifyToSellerDTO confirmOrderNotifyDTO = new ConfirmOrderNotifyToSellerDTO();
                                confirmOrderNotifyDTO.setGyShbh(ycDdVe.getGyShbh());
                                confirmOrderNotifyDTO.setGyDdbh(ycDdVe.getGyDdbh());
                                confirmOrderNotifyDTO.setGyPtzt(UsecarOrderStatusEnum.YC4A.getCode());
                                sellerNoticeService.confirmOrderNotify(confirmOrderNotifyDTO);
                                logger.info("推送订单[" + ycDdVe.getDdbh() + "]供应商采购服务完给供应商ASMS平台完成！");
                            }
                        } catch (Exception e) {
                            logger.error("推送采购信息给供应商ASMS平台异常", e);
                            logger.info("推送订单[" + ycDdVe.getDdbh() + "]供应商退单申请给供应商ASMS平台失败：\n" + e.getMessage());
                        }
                        /*************** 推送供应商采购服务完成给ASMS平台-end ***************************/
                    }
                }else{
                    result = Boolean.TRUE;
                }
            } catch (Exception e) {
                logger.error("用车分账报错=========>", e);
            }
        } else {
            logger.info("通过订单编号及版本号未能查到数据");
        }
        return result;
    }



    /**
     * 采购服务完成
     * 支付调用服务完成的方法
     * @param ycDd 采购操作订单DTO
     * @return 采购服务完成是否成功
     */
    public Boolean serveComplete(YcDd ycDd) {
        Boolean result = Boolean.FALSE;
        logger.warn("支付 进入采购服务完成方法");
        if (ycDd != null) {
            try {
                logger.info("用车采购服务完成修改前的订单状态[" + ycDd.getDdzt() + "]");
                if(!UsecarOrderStatusEnum.YC4B.getCode().equals(ycDd.getDdzt())){
                    logger.warn("订单["+ycDd.getDdbh()+"]此时的订单状态是:{}",ycDd.getDdzt());
                    //2020-07-30 订单状态前面已更新，无需再次设置
//                    ycDd.setDdzt(UsecarOrderStatusEnum.YC4A.getCode());// 订单状态改为已用车-待分账
                    //分账方法缺失，分账成功后订单状态改为已用车-已分账
                    result = ycDdService.updateYcDd(ycDd);//更新正常单信息
                    logger.warn("订单["+ycDd.getDdbh()+"]修改了订单状态:YC4A");
                    int fzResult = usecarPayHandleService.orderFz(ycDd.getDdbh(),UseCarConstant.YC_PAY_PT_B,UseCarConstant.NUMONE);
                    if (fzResult == UseCarConstant.SUCCESS) {
                        result = true;
                        logger.info("订单编号[" + ycDd.getDdbh() + "]调用分账成功");
                    } else {
                        result = false;
                        logger.info("订单编号[" + ycDd.getDdbh() + "]启动分账请求失败");
                    }
                }else{
                    result = Boolean.TRUE;
                }
            } catch (Exception e) {
                logger.error("用车分账报错=========>", e);
            }
        } else {
            logger.info("通过订单编号及版本号未能查到数据");
        }
        return result;
    }

    /**
     * 采购确认服务完成，回推相关数据到采购商asms平台
     * @param ycDd 用车订单信息
     * @param fzResult 分账结果(true 成功，false 失败)
     * @param prices  订单最终的价格明细(接口供应商会有)
     */
    public void sendConfirmOrderInfoToBuyerAsms(YcDd ycDd,boolean fzResult){
        logger.info("采购确认服务完成，回推相关数据到采购商asms平台--->"+(fzResult?"分账成功！":"分账失败"));
        logger.info("采购商["+ycDd.getCgShbh()+"]对订单["+ycDd.getDdbh()+"]/["+ycDd.getCgDdbh()+"]进行服务"
                + "完成操作后，回推到采购商["+ycDd.getCgShbh()+"]-["+ycDd.getCgDdbh()+"]ASMS平台");
        //如果当前订单没有实际上车和实际下车地址，则不推送订单数据
        if(StringUtils.isBlank(ycDd.getSjscdz()) && StringUtils.isBlank(ycDd.getSjxcdz())){
            logger.warn("订单["+ycDd.getDdbh()+"]/["+ycDd.getCgDdbh()+"]缺少实际上(下)车地址，不能推送订单信息到采购");
//            return;
        }
        ConfirmOrderInfoToBuyerAsmsDTO dto = new ConfirmOrderInfoToBuyerAsmsDTO();
        dto.setCgDdbh(ycDd.getCgDdbh());
        dto.setCgShbh(ycDd.getCgShbh());
        double ptkrz=0;
        if(fzResult){
            dto.setCgPtzt(UsecarOrderStatusEnum.YC4B.getCode());
            ptkrz = ycDd.getPtkrbl()==null?0d:converToDouble(ycDd.getPtkrbl());
        }else {
//            dto.setCgPtzt(UsecarOrderStatusEnum.YC4A.getCode());
            dto.setCgPtzt(ycDd.getDdzt());
            ptkrz = (ycDd.getPtkrbl() == null ? 0 : converToDouble(ycDd.getPtkrbl())) - (ycDd.getGyFybl() == null ? 0 : converToDouble(ycDd.getGyFybl()));
        }
        dto.setPtkrz(ptkrz);
        dto.setJsje(converToDouble(ycDd.getCgJsje())); // 此处是推给采购方的,结算金额应该是财股结算金额  周艳 改
        dto.setCgJsje(converToDouble(ycDd.getCgJsje()));
        dto.setYfje(converToDouble(ycDd.getYfje()));
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjxm())) {
            dto.setSjxm(ycDd.getSjxm());
        }
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjdh())) {
            dto.setSjdh(ycDd.getSjdh());
        }
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjxb())) {
            dto.setSjxb(ycDd.getSjxb());
        }
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getCph())) {
            dto.setCph(ycDd.getCph());
        }
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getCxzmc())) {
            dto.setCxzmc(ycDd.getCxzmc());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ycDd.getCxzbh())) { //车型组编号
            dto.setCxzbh(ycDd.getCxzbh());
        }
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getCxmc())) {
            dto.setCxmc(ycDd.getCxmc());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ycDd.getCxzbh())) { //车型组编号
            dto.setCxzbh(ycDd.getCxzbh());
        }
        if(org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getCxmc())) {
            dto.setCxmc(ycDd.getCxmc());
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjscdz())) {
            dto.setSjscdz(ycDd.getSjscdz());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjxcdz())) {
            dto.setSjxcdz(ycDd.getSjxcdz());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjzbX())) {
            dto.setSjzbX(ycDd.getSjzbX());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjzbY())) {
            dto.setSjzbY(ycDd.getSjzbY());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjscsj())) {
            dto.setSjscsj(ycDd.getSjscsj());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getXcjssj())) {
            dto.setXcjssj(ycDd.getXcjssj());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ycDd.getSjpf())) {
            dto.setSjpf(ycDd.getSjpf());
        }
        if (ycDd.getBdlc() != null) {
            dto.setBdlc(converToDouble(ycDd.getBdlc()));
        }
        if (ycDd.getBdsc() != null) {
            dto.setBdsc(converToDouble(ycDd.getBdsc()));
        }
        couponConsumeService.setCouponInfo(dto,ycDd);
        dto.setCpsNewDdbh(ycDd.getGldh());
        List<YcDdFymx> ycDdFyxmList=null;
        try {
            ycDdFyxmList = ycDdFymxService.getYcDdFyxmList(ycDd.getDdbh());
        }catch (Exception e){
            logger.error("CPS采购服务完成推送相关信息给ASMS采购商：根据ddbh="+ycDd.getDdbh()+"查询yc_dd_fwxm表异常！", e);
            logger.info("["+ycDd.getDdbh()+"]CPS采购服务完成推送相关信息给ASMS采购商["+ycDd.getCgShbh()+"]异常!");
        }
        if(CollectionUtil.isNotEmpty(ycDdFyxmList)){
            List<FymxDTO> pricedetails = new ArrayList<FymxDTO>();
            YcDdFymx fymx = ycDdFyxmList.get(0);
            logger.info("依据订单编号[" + ycDd.getDdbh() + "]获取到费用项目明细、艳哥说这里可以取到优惠金额，试试看:yhje="+fymx.getYhje());
            FymxDTO fymxDTO = new FymxDTO();
            fymxDTO.setDdbh(fymx.getDdbh());
            fymxDTO.setFyxm(fymx.getFyxm());
            fymxDTO.setFyje(converToDouble(fymx.getFyje()));
            fymxDTO.setBy1(converToDouble(fymx.getBy1()));
            fymxDTO.setBy2(fymx.getBy2());
            fymxDTO.setBy3(fymx.getBy3());
            fymxDTO.setSjkly(fymx.getSjkly());
            fymxDTO.setZdxf(converToDouble(fymx.getZdxf()));
            fymxDTO.setQbj(converToDouble(fymx.getQbj()));
            fymxDTO.setTcj(converToDouble(fymx.getTcj()));
            fymxDTO.setLcf(converToDouble(fymx.getLcf()));
            fymxDTO.setScf(converToDouble(fymx.getScf()));
            fymxDTO.setZdxfbz(converToDouble(fymx.getZdxfbz()));
            fymxDTO.setJjfy(converToDouble(fymx.getJjfy()));
            fymxDTO.setDttj(converToDouble(fymx.getDttj()));
            fymxDTO.setDsf(converToDouble(fymx.getDsf()));
            fymxDTO.setCclcf(converToDouble(fymx.getCclcf()));
            fymxDTO.setCcscf(converToDouble(fymx.getCcscf()));
            fymxDTO.setYtf(converToDouble(fymx.getYtf()));
            fymxDTO.setCcf(converToDouble(fymx.getCcf()));
            fymxDTO.setYjfy(converToDouble(fymx.getYjfy()));
            fymxDTO.setSjdhf(converToDouble(fymx.getSjdhf()));
            fymxDTO.setGsf(converToDouble(fymx.getGsf()));
            fymxDTO.setLqf(converToDouble(fymx.getLqf()));
            fymxDTO.setTcf(converToDouble(fymx.getTcf()));
            fymxDTO.setJcfwf(converToDouble(fymx.getJcfwf()));
            fymxDTO.setQtfy(converToDouble(fymx.getQtfy()));
            fymxDTO.setGrYfje(converToDouble(fymx.getGrYfje()));
            fymxDTO.setGrSfje(converToDouble(fymx.getGrSfje()));
            fymxDTO.setQyYfje(converToDouble(fymx.getQyYfje()));
            fymxDTO.setQySfje(converToDouble(fymx.getQySfje()));
            fymxDTO.setCpsKr(converToDouble(fymx.getCpsKr()));
            fymxDTO.setYhje(converToDouble(fymx.getYhje()));
            fymxDTO.setTkje(converToDouble(fymx.getTkje()));
            fymxDTO.setQxf(converToDouble(fymx.getQxf()));
            fymxDTO.setDdf(converToDouble(fymx.getDdf()));
            fymxDTO.setTssdf(converToDouble(fymx.getTssdf()));
            pricedetails.add(fymxDTO);
            dto.setPricedetails(pricedetails);
        }else {
            logger.info("依据订单编号【"+ycDd.getDdbh()+"】未能获取到订单费用明细数据！");
        }
        dto.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycDd.getDdzt()));
        buyerNoticeService.confirmOrderNotify(dto, ycDd);
    }

    /**
     * 用车评价标签列表，根据供应商户编号获取
     * 判断供应商类型
     * 1、接口供应商，通过Link获取供应商返回的投诉枚举列表
     * 2、非接口供应商，读取本地供应商自己维护的
     * @param ddbh 订单编号
     * @return map 枚举数据（key=评价等1-5.0：代表投诉）
     */
    public Map<String,List<PjtsOptionBean>> getPjbqMap(String ddbh){
        //定义返回对象
        Map<String,List<PjtsOptionBean>> returnMap=new TreeMap<>();
        Map<String,List<PjtsOptionBean>> mrMap=getPjbqMapFromEnum();
        //根据gyShbh查询评价标签Map，这里暂时返回枚举类的数据
        YcDd order = ycDdService.selectYcDd(ddbh);
        if(order==null){
            logger.info("依据CPS订单编号["+ddbh+"]未能获取到订单数据！");
            return mrMap;
        }
        //供应商商户编号
        String gyShbh = order.getGyShbh();
        //是否是接口供应商
        boolean isApiSeller=UsecarGysApiEnum.checkIsApiGys(gyShbh);
        //接口供应商，通过Link请求供应商的投诉选项数据
        if(isApiSeller){
            logger.info("向Link供应商["+gyShbh+"]发送请求，获取订单["+order.getGyDdbh()+"]的投诉选项数据！");
            returnMap=getLinkPjtsOptions(order);
        }else{
            logger.info("从CPS本地获取自签供应商["+gyShbh+"]的投诉选项数据！");
            //请求本地供应商自己维护的投诉选项
            List<YcPjtsBq> bqList = ycPjtsBqService.selectYcPjtsBq();
            if(CollectionUtil.isNotEmpty(bqList)){
                for(YcPjtsBq bq:bqList){
                    String pjbq = bq.getPjbq();
                    List<PjtsOptionBean> optionBeanList = new ArrayList<>();
                    if(StringUtils.isNotBlank(pjbq)){
                        pjbq = pjbq.replaceAll("，",",");
                        for(String bqOne:pjbq.split(",")){
                            PjtsOptionBean optionBean = new PjtsOptionBean();
                            optionBean.setCode(bq.getPjdj());
                            optionBean.setNote(StringUtils.trim(bqOne));
                            optionBeanList.add(optionBean);
                        }
                    }
                    if(CollectionUtil.isEmpty(optionBeanList)){
                        return mrMap;
                    }
                    if(StringUtils.equals(bq.getPjdj(),UseCarConstant.ZT_ZERO)){
                        returnMap.put(UseCarConstant.ZT_ZERO,optionBeanList);
                    }else if(StringUtils.equals(bq.getPjdj(),UseCarConstant.ZT_ONE)){
                        returnMap.put(UseCarConstant.ZT_ONE,optionBeanList);
                    }else if(StringUtils.equals(bq.getPjdj(),UseCarConstant.ZT_TWO)){
                        returnMap.put(UseCarConstant.ZT_TWO,optionBeanList);
                    }else if(StringUtils.equals(bq.getPjdj(),UseCarConstant.ZT_THREE)){
                        returnMap.put(UseCarConstant.ZT_THREE,optionBeanList);
                    }else if(StringUtils.equals(bq.getPjdj(),UseCarConstant.ZT_FOUR)){
                        returnMap.put(UseCarConstant.ZT_FOUR,optionBeanList);
                    }else if(StringUtils.equals(bq.getPjdj(),UseCarConstant.ZT_FIVE)){
                        returnMap.put(UseCarConstant.ZT_FIVE,optionBeanList);
                    }
                }
            }
        }

        if(returnMap==null || returnMap.size()==0){
            returnMap= getPjbqMapFromEnum();
        }else{
            for(int i=0;i<UseCarConstant.SIX;i++){
                String key = String.valueOf(i);
                if(CollectionUtil.isEmpty(returnMap.get(key))){
                    returnMap.put(key,mrMap.get(key));
                }
            }
        }
        return returnMap;
    }

    /**
     * 请求Link获取接口供应商的投诉选项列表数据
     * @param order 订单信息
     * @return      投诉选项列表
     */
    private Map<String,List<PjtsOptionBean>> getLinkPjtsOptions(YcDd order){
        LinkUseCarQueryComplaintOptionsDTO linkReq=new LinkUseCarQueryComplaintOptionsDTO();
        linkReq.setCpsDdbh(order.getDdbh());
        linkReq.setShid(order.getGyShbh());
        linkReq.setGyDdbh(order.getGyDdbh());
        linkReq.setCgshbh(order.getCgShbh());
        linkReq.setCgshjc(order.getCgShjc());
        linkReq.setShjc(order.getGyShjc());
        linkReq.setDdlx(order.getDdlx());
        linkReq.setUserid(order.getCgYhbh());
        linkReq.setUserxm(order.getCgYhbh());
        setChannelType(linkReq,order);
        Map<String,List<PjtsOptionBean>> returnMap=new TreeMap<>();
        RestResponse<LinkUseCarQueryComplaintOptionsVO>  response= null;
        try{
            response=iLinkSpecialCarService.queryComplaintOptions(linkReq);
        }catch (Exception e){
            logger.error("获取评价标签，请求link异常",e);
        }
        if(response!=null&&response.getResult()!=null){
            LinkUseCarQueryComplaintOptionsVO res=response.getResult();
            logger.info("Link返回投诉选项："+res.toString());
            if(CollectionUtil.isNotEmpty(res.getOptions())){
                List<PjtsOptionBean> linkTsList=new ArrayList<>();
                for (LinkComplaintOptionsBean lb:res.getOptions()){
                    PjtsOptionBean pb=new PjtsOptionBean();
                    pb.setCode(lb.getCode());
                    pb.setNote(lb.getNote());
                    linkTsList.add(pb);
                }
                returnMap.put(UsecarPjtsPjdjEnum.tsbq.getCode(),linkTsList);
            }
        }
        return returnMap;
    }
    /**
     * 从枚举添加用车评价标签列表
     * @return map
     */
    public Map<String,List<PjtsOptionBean>> getPjbqMapFromEnum(){
        Map<String,List<PjtsOptionBean>> map = new HashMap<>();
        for(UsecarPjtsPjdjEnum pjdjEnum:UsecarPjtsPjdjEnum.values()){
            String key = pjdjEnum.getCode();
            String value = pjdjEnum.getMessage();
            if(StringUtils.isNotBlank(value)){
                List<PjtsOptionBean> pjbqList = new ArrayList<>();
                for(String pjbq:value.split(",")){
                    PjtsOptionBean bean = new PjtsOptionBean();
                    bean.setCode(key);
                    bean.setNote(pjbq);
                    pjbqList.add(bean);
                }
                map.put(key,pjbqList);
            }
        }
        return map;
    }

    /**
     * 添加用车评价投诉数据
     * @param pjtsDTO 入参
     * @param doAtCps 是否在CPS上操作、true：在CPS操作的提交评价时间，其他情况为空或者false都是来自ASMS
     * @return b
     */
    public boolean addYcPjts(YcPjtsDTO pjtsDTO,String doAtCps){
        logger.info("采购商操作创建评价投诉单,操作来源doAtCps="+doAtCps);
        boolean result = false;
        //验证：订单中 ddbh、cgShbh与pjtsDTO中的是否相等，相等则可以入库，不等则返回false
        YcDdMain ycDdMainZd = ycDdMainService.selectByIdCache(pjtsDTO.getDdbh());
        YcDd ycDd = ycDdService.selectYcDd(pjtsDTO.getDdbh());
        if (null != ycDdMainZd && null != ycDdMainZd.getDdbh()) {
            ycDd = ycDdService.selectYcDd(ycDdMainZd.getDdbh());
        }
        logger.info("用车订单ycdd商户编号{}",JsonMapper.nonEmptyMapper().toJson(ycDd));
        if(ycDd!=null) {
            YcPjts ycPjts = BeanMapper.map(pjtsDTO, YcPjts.class);
            ycPjts.setHfzt(UseCarConstant.ZT_ZERO);
            ycPjts.setId(String.valueOf(IdGenerator.getId()));
            ycPjts.setPjDatetime(VeDate.getNow());
            if (StringUtils.equals(ycPjts.getPjlx(), UseCarConstant.PJTS_PJ)) {
                ycPjts.setPjzt(UseCarConstant.ZT_ONE);
            } else {
                ycPjts.setPjzt(UseCarConstant.ZT_ZERO);
                //投诉情况下默认给分值：1
                ycPjts.setPjdj(UseCarConstant.TS_DEFAULT_PF);
                ycPjts.setSjpf(UseCarConstant.TS_DEFAULT_PF);
            }
            result =ycPjtsService.addYcDdPjts(ycPjts);
            if(StringUtils.isNotBlank(ycPjts.getDdbh())){
                // 更新当前订单的订单主表信息,添加"评价等级","评价内容","评价标签"
                YcDdMain ycDdMain = ycDdMainService.selectByIdCache(ycPjts.getDdbh());
                if (null != ycDdMain) {
                    ycDdMain.setPjdj(ycPjts.getPjdj());
                    ycDdMain.setPjbq(ycPjts.getPjbq());
                    ycDdMain.setPjnr(ycPjts.getPjnr());
                    ycDdMainService.updateById(ycDdMain);
                }
            }
            ycDdMqSendService.sendUpdate(ycDd);
            //评价投诉入库CPS成功
            if(result){
                logger.info("CPS上订单{}投诉评价信息入库成功、即将推送给采购和供应！",ycDd.getDdbh());
                //--推送给供应商ASMS平台
                //----评价推送给供应商

                Boolean isPj = true;
                if (StringUtils.equals(UseCarConstant.PJTS_TS,ycPjts.getPjlx()) || NEGATIVE_COMMENT.contains(ycPjts.getPjdj())) {
                    isPj = false;
                }
                if(isPj){
                    //如果是接口供应商，还要请求Link
                    if(UsecarGysApiEnum.checkIsApiGys(ycDd.getGyShbh())){
                        sendOrderScoreToSellerLink(ycDd,ycPjts);
                        logger.info("评价信息推送给了接口供应商{}!",ycDd.getGyShbh());
                    }else {
                        sendOrderScoreToSellerAsms(ycDd,ycPjts);
                        logger.info("评价信息推送给了自签供应商{}!",ycDd.getGyShbh());
                    }
                } else {
                    if(UsecarGysApiEnum.checkIsApiGys(ycDd.getGyShbh())){
                        sendComplaintOrderToSellerLink(ycDd,pjtsDTO);
                        logger.info("投诉信息推送给了接口供应商{}!",ycDd.getGyShbh());
                    }else{
                        sendComplaintOrderToSellerAsms(ycDd,ycPjts);
                        logger.info("投诉信息推送给了自签供应商{}!",ycDd.getGyShbh());
                    }
                }
                //--推送给采购商ASMS平台
                /**只有当采购商在CPS-B采购平台发布了一条评价或者投诉信息，才会推送到采购商ASMS平台保持与CPS数据一致**/
                if(StringUtils.equals(doAtCps,Boolean.TRUE.toString())){
                    //----评价推送给采购商
                    if(StringUtils.equals(UseCarConstant.PJTS_PJ,ycPjts.getPjlx())){
                        sendOrderScoreToBuyerAsms(ycDd,ycPjts);
                    }else if(StringUtils.equals(UseCarConstant.PJTS_TS,ycPjts.getPjlx())){
                        sendComplaintOrderToBuyerAsms(ycDd,ycPjts);
                    }
                }
                logger.info("订单{}投诉评价推送给ASMS代理人系统都完成了(包括供应和采购！)!",ycDd.getDdbh());
            }else {
                logger.warn("CPS上订单{}投诉评价信息入库失败！",ycDd.getDdbh());
            }
        }
        return result;
    }


    /**
     * 评价信息推送给Link供应商
     * @param ycDd      订单主表
     * @param ycPjts    评价内容
     */
    public void sendOrderScoreToSellerLink(YcDd ycDd ,YcPjts ycPjts){
        try {
            LinkUseCarOrderScoreDTO ldto=new LinkUseCarOrderScoreDTO();
            ldto.setCpsDdbh(ycDd.getDdbh());
            ldto.setGyDdbh(ycDd.getGyDdbh());
            ldto.setShid(ycDd.getGyShbh());
            ldto.setDdlx(ycDd.getDdlx());
            ldto.setCgshbh(ycDd.getCgShbh());
            ldto.setCgshjc(ycDd.getCgShjc());
            ldto.setUserid(ycDd.getCgYhbh());
            ldto.setUserxm(ycDd.getCgYhbh());
            ldto.setScore(ycPjts.getPjdj());
            ldto.setComment(ycPjts.getPjnr());
            ldto.setUserMobile(ycDd.getCksj());
            setChannelType(ldto,ycDd);
            logger.info("推送评价信息给接口供应商参数:{}",ldto.toString());
            RestResponse<LinkUseCarOrderScoreVO> lRes=iLinkPickUpCarService.orderScore(ldto);
            logger.info("Link返回评价结果{}",JsonMapper.nonNullMapper().toJson(lRes));
        }catch (Exception e){
            logger.error("评价信息推送给Link供应商{}异常",ycDd.getGyShbh(),e);
        }
    }
    /**
     * 推送评价信息给供应商ASMS系统
     * @param ycDd       评价订单
     * @param ycPjts    评价推送db
     */
    public void sendOrderScoreToSellerAsms(YcDd ycDd ,YcPjts ycPjts){
        try {
            OrderScoreToSellerDTO sdto=new OrderScoreToSellerDTO();
            sdto.setGyShbh(ycDd.getGyShbh());
            sdto.setGyDdbh(ycDd.getGyDdbh());
            sdto.setSjpf(ycPjts.getSjpf());
            sdto.setPjdj(ycPjts.getPjdj());
            sdto.setPjbq(ycPjts.getPjbq());
            sdto.setPjnr(ycPjts.getPjnr());
            sdto.setCpsDdbh(ycPjts.getDdbh());
            sellerNoticeService.orderScoreNotify(sdto);
            logger.info("推送订单{}评价信息给供应商{}ASMS平台请求完成!",ycDd.getDdbh(),ycDd.getGyShbh());
        }catch (Exception e){
            logger.error("推送订单{}评价信息给供应商{}ASMS平台异常！",ycDd.getDdbh(),ycDd.getGyShbh(),e);
        }
    }

    /**
     * 推送投诉信息给供应商ASMS系统
     * @param ycDd          待投诉订单
     * @param ycPjts        投诉内容
     */
    public void sendComplaintOrderToSellerAsms(YcDd ycDd ,YcPjts ycPjts){
        try {
            ComplaintOrderToSellerDTO sdto=new ComplaintOrderToSellerDTO();
            sdto.setGyShbh(ycDd.getGyShbh());
            sdto.setGyDdbh(ycDd.getGyDdbh());
            sdto.setCpsDdbh(ycDd.getDdbh());
            sdto.setTsnr(ycPjts.getPjnr());
            sdto.setTsbq(ycPjts.getPjbq());
            sellerNoticeService.complaintOrderNotify(sdto);
            logger.info("推送订单{}投诉信息给供应商{}ASMS平台请求完成!",ycDd.getDdbh(),ycDd.getGyShbh());
        }catch (Exception e){
            logger.error("推送订单{}投诉信息给供应商{}ASMS平台异常！",ycDd.getDdbh(),ycDd.getGyShbh(),e);
        }
    }

    /**
     * 推送投诉信息给供应商ASMS系统
     * @param ycDd          待投诉订单
     * @param ycPjts        投诉内容
     */
     public void sendComplaintOrderToSellerAsms(YcDdMain ycDd ,YcPjts ycPjts){
        try {
            ComplaintOrderToSellerDTO sdto=new ComplaintOrderToSellerDTO();
            sdto.setGyShbh(ycDd.getGyShbh());
            sdto.setGyDdbh(ycDd.getGyDdbh());
            sdto.setCpsDdbh(ycDd.getpDdbh());
            sdto.setTsnr(ycPjts.getPjnr());
            sdto.setTsbq(ycPjts.getPjbq());
            sellerNoticeService.complaintOrderNotify(sdto);
            logger.info("推送订单{}投诉信息给供应商{}ASMS平台请求完成!",ycDd.getpDdbh(),ycDd.getGyShbh());
        }catch (Exception e){
            logger.error("推送订单{}投诉信息给供应商{}ASMS平台异常！",ycDd.getpDdbh(),ycDd.getGyShbh(),e);
        }
    }

    /**
     * 推送投诉给Link接供应商
     * @param ycDd  订单主表
     * @param ycPjts    投诉内容
     */
    public void sendComplaintOrderToSellerLink(YcDdMain ycDd ,YcPjtsDTO ycPjts){
        try {
            LinkUseCarOrderComplaintDTO ldto=new LinkUseCarOrderComplaintDTO();
            ldto.setCpsDdbh(ycDd.getpDdbh());
            ldto.setGyDdbh(ycDd.getGyDdbh());
            ldto.setShid(ycDd.getGyShbh());
            ldto.setDdlx(ycDd.getDdlx());
            ldto.setCgshbh(ycDd.getCgshbh());
            ldto.setCgshjc(ycDd.getCgShjc());
            ldto.setUserid(ycDd.getCgYhbh());
            ldto.setUserxm(ycDd.getCgYhbh());
            ldto.setComplaintId(ycPjts.getPjbqCode());
            ldto.setComplaintName(ycPjts.getPjbq());
            ldto.setOtherNote(ycPjts.getPjnr());
            logger.info("推送投诉信息给接口供应商参数:{}",ldto.toString());
            RestResponse<LinkUseCarOrderComplaintVO> lRes=iLinkPickUpCarService.orderCmplaint(ldto);
            logger.info("推送订单{}投诉信息给供应商{}Link平台请求完成!Link返回结果:{}",
                    ycDd.getpDdbh(),ycDd.getGyShbh(), JsonMapper.defaultMapper().toJson(lRes));
        }catch (Exception e){
            logger.error("推送订单{}投诉信息给供应商{}Link平台异常！",ycDd.getpDdbh(),ycDd.getGyShbh(),e);
        }
    }

    /**
     * 推送评价信息给采购商ASMS系统
     * @param ycDd       评价订单
     * @param ycPjts    评价推送db
     */
    public void sendOrderScoreToBuyerAsms(YcDd ycDd ,YcPjts ycPjts){
        try {
            OrderScoreToBuyerDTO sdto=new OrderScoreToBuyerDTO();
            sdto.setCgShbh(ycDd.getCgShbh());
            sdto.setCgDdbh(ycDd.getCgDdbh());
            sdto.setSjpf(ycPjts.getSjpf());
            sdto.setPjdj(ycPjts.getPjdj());
            sdto.setPjbq(ycPjts.getPjbq());
            sdto.setPjnr(ycPjts.getPjnr());
            sdto.setCpsDdbh(ycPjts.getDdbh());
            sdto.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycDd.getDdzt()));
            buyerNoticeService.orderScoreNotify(sdto);
            logger.info("推送订单{}评价信息给采购商{}ASMS平台请求完成!",ycDd.getDdbh(),ycDd.getCgShbh());
        }catch (Exception e){
            logger.error("推送订单{}评价信息给采购商{}ASMS平台异常！",ycDd.getDdbh(),ycDd.getCgShbh(),e);
        }
    }

    /**
     * 创建投诉单，并推送给采购ASMS平台
     * 如下情况要调用这个方法，推送到采购ASMS平台
     * 1、采购商在CPS-B创建投诉单
     * 2、CPS-A针对订单生成投诉单
     * @param ycDd  订单信息
     * @param ycPjts   CPS投诉主表
     */

     public void sendComplaintOrderToBuyerAsms(YcDd ycDd ,YcPjts ycPjts){
        try {
            ComplaintOrderToBuyerDTO sdto=new ComplaintOrderToBuyerDTO();
            sdto.setCgShbh(ycDd.getCgShbh());
            sdto.setCgDdbh(ycDd.getCgDdbh());
            sdto.setCpsDdbh(ycDd.getDdbh());
            sdto.setTsbq(ycPjts.getPjbq());
            sdto.setTsnr(ycPjts.getPjnr());
            sdto.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycDd.getDdzt()));
            buyerNoticeService.complaintOrderNotify(sdto);
            logger.info("推送订单{}投诉信息给采购商{}ASMS平台请求完成!",ycDd.getDdbh(),ycDd.getCgShbh());
        }catch (Exception e){
            logger.error("推送订单{}投诉信息给采购商{}ASMS平台异常！",ycDd.getDdbh(),ycDd.getCgShbh(),e);
        }
    }

    /**
     * 创建投诉单，并推送给采购ASMS平台
     * 如下情况要调用这个方法，推送到采购ASMS平台
     * 1、采购商在CPS-B创建投诉单
     * 2、CPS-A针对订单生成投诉单
     * @param ycDd  订单信息
     * @param ycPjts   CPS投诉主表
     */
    public void sendComplaintOrderToBuyerAsms(YcDdMain ycDd , YcPjts ycPjts){
        try {
            ComplaintOrderToBuyerDTO sdto=new ComplaintOrderToBuyerDTO();
            sdto.setCgShbh(ycDd.getCgshbh());
            sdto.setCgDdbh(ycDd.getCgddbh());
            sdto.setCpsDdbh(ycDd.getpDdbh());
            sdto.setTsbq(ycPjts.getPjbq());
            sdto.setTsnr(ycPjts.getPjnr());
            sdto.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycDd.getDdzt()));
            buyerNoticeService.complaintOrderNotify(sdto);
            logger.info("推送订单{}投诉信息给采购商{}ASMS平台请求完成!",ycDd.getpDdbh(),ycDd.getCgshbh());
        }catch (Exception e){
            logger.error("推送订单{}投诉信息给采购商{}ASMS平台异常！",ycDd.getpDdbh(),ycDd.getCgshbh(),e);
        }
    }

    /**
     * 添加用车评价投诉-回复
     * @param pjtsMxDTO 入参
     * @param doAtCps 是否在CPS上操作、true：在CPS操作的提交回复信息，其他情况为空或者false都是来自ASMS采购商回复
     * @return b     采购回复操作结果
     */
    public boolean addYcPjtsMx(YcPjtsMxDTO pjtsMxDTO,String doAtCps){
        logger.info("采购商操作评价投诉单的回复,操作来源doAtCps="+doAtCps);
        boolean result = false;
        String ddbh = pjtsMxDTO.getDdbh();
        if(StringUtils.isBlank(ddbh)){
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30016);
        }else{
            //验证：用车订单中 ddbh、cyShbh
            YcDd ycDd = ycDdService.selectYcDd(ddbh);
            if(ycDd!=null && StringUtils.equals(ycDd.getCgShbh(),pjtsMxDTO.getHfShbh())){
                YcPjtsMx ycPjtsMx = BeanMapper.map(pjtsMxDTO,YcPjtsMx.class);
                ycPjtsMx.setSjkly(usecarOrderNoService.getSjklyByDdbh(ddbh));
                ycPjtsMx.setId(String.valueOf(IdGenerator.getId()));
                ycPjtsMx.setHfDatetime(VeDate.getNow());
                result =ycPjtsMxService.addYcDdPjtsMx(ycPjtsMx);
                //如果新增成功，则更新评价投诉表的回复状态值
                if(result){
                    YcPjts ycPjts = new YcPjts();
                    ycPjts.setId(ycPjtsMx.getPjtsid());
                    ycPjts.setHfzt(UseCarConstant.ZT_ONE);
                    result = ycPjtsService.updateYcDdPjts(ycPjts);
                    //推送回复信息给采购商ASMS平台
                    //--如果是采购商在CPS上操作的回复信息，那么这里要推送到采购的ASMS平台，保持数据一致
                    if(StringUtils.equals(Boolean.TRUE.toString(),doAtCps)){
                        sendOrderAnswerToBuyerAsms(ycDd,ycPjtsMx);
                    }
                }else {
                    logger.warn("订单{}下的投诉评价单的回复信息入库CPS失败！",ycDd.getDdbh());
                }
            }

        }

        /**
     * 通过订单编号查询，用车订单的评价信息
     * @param ddbh ddbh
     * @return vo
     */
    private YcPjtsVO getYcPjtsVOByDdbh(String ddbh){
        YcPjts ycPjts = ycPjtsService.selectYcPjtsByDdbh(ddbh);
        if(ycPjts==null){
            return null;
        }
        YcPjtsVO ycPjtsVO = BeanMapper.map(ycPjts,YcPjtsVO.class);
        if(StringUtils.equals(UseCarConstant.ZT_ONE,ycPjts.getHfzt())){
            List<YcPjtsMx> ycPjtsMxList = ycPjtsMxService.selectYcPjtsMx(ycPjts.getId(),ycPjts.getSjkly(),"");
            if(CollectionUtil.isNotEmpty(ycPjtsMxList)) {
                List<YcPjtsMxVO> mxVOList = BeanMapper.mapList(ycPjtsMxList,YcPjtsMx.class,YcPjtsMxVO.class);
                ycPjtsVO.setYcPjtsMxVOList(mxVOList);
            }
        }
        return ycPjtsVO;
    }
    /**
     * Bigdecmal 转 Double-->refundOrder()方法有用到
     *
     * @param aa canshu
     * @return Double
     */
    private Double converToDouble(BigDecimal aa) {
        if (aa != null) {
            return aa.doubleValue();
        } else {
            return null;
        }
    }
    /**
     * 采购或者供应在CPS上的回复明细信息推送给采购商
     * @param order    订单信息
     * @param mx     回复明细信息
     */
    public void sendOrderAnswerToBuyerAsms(YcDd order,YcPjtsMx mx){
        try {
            OrderAnswerToBuyerDTO sendDto=new OrderAnswerToBuyerDTO();
            sendDto.setCgDdbh(order.getCgDdbh());
            sendDto.setCgShbh(order.getCgShbh());
            sendDto.setHfly(mx.getHfly());
            sendDto.setHfnr(mx.getHfnr());
            sendDto.setHfYhbh(mx.getHfYhbh());
            buyerNoticeService.answerNotify(sendDto);
            logger.info("推送订单{}评价推送单的回复信息给采购商{}ASMS平台请求完成!",order.getDdbh(),order.getCgShbh());
        }catch (Exception e){
            logger.error("推送订单{}评价推送单的回复信息给采购商{}ASMS平台请求失败！",order.getDdbh(),order.getCgShbh(),e);
        }
    }
    /**
     * 通过订单编号查询评价投诉
     *
     * @param orderNo 订单编号
     * @return 评价投诉
     */
    public QueryComplaintDTO queryComplaint(String orderNo) {
        YcPjts ycPjts = ycPjtsService.selectYcPjtsByDdbh(orderNo);
        if (ycPjts == null || !(StringUtils.equals(ycPjts.getPjlx(), COMPLAINT_TYPE) || NEGATIVE_COMMENT.contains(ycPjts.getPjdj()))) {
            return null;
        }
        List<YcPjtsMx> ycPjtsMxList = ycPjtsMxService.selectYcPjtsMx2(ycPjts.getId(), ycPjts.getSjkly(), "",true);
        return new QueryComplaintDTO(ycPjts, ycPjtsMxList);
    }
}