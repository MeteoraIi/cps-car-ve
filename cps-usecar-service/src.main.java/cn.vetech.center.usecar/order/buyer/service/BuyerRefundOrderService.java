package cn.vetech.center.usecar.order.buyer.service;

import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.mapper.order.YcDdMapper;
import cn.vetech.center.usecar.order.buyer.dto.BuyerRefundOrderDTO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerRefundOrderDetailVO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerRefundOrderVO;
import cn.vetech.center.usecar.order.dto.OrderSearchDTO;
import cn.vetech.center.usecar.service.order.YcDdService;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vetech.core.base.PageDTO;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.PageCopyUtil;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购退款单服务
 * @author chenyong
 * @since 2017-10-10
 */
@Service
public class BuyerRefundOrderService extends ServiceImpl<YcDdMapper, YcDd> {
    /**
     *打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(BuyerRefundOrderService.class);

    /**
     *用车订单dao
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 分页查询采购退款订单列表
     * @param pageDTO 退款订单查询分页信息
     * @return 采购退款订单分页信息
     */
    public Page<BuyerRefundOrderVO> searchBuyerRefundOrderList(PageDTO<OrderSearchDTO> pageDTO){

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
        Page<YcDd> zcDdPage  = ycDdService.searchBuyerRefundOrderList(pageDTO);
        Page<BuyerRefundOrderVO> resultPage = PageCopyUtil.copy(zcDdPage,YcDd.class,BuyerRefundOrderVO.class);

        return resultPage;
    }

    /**
     * 采购获取退款单订单状态个数
     * @param dto 查询参数
     * @return 退款单订单状态个数
     */
    public List<BuyerRefundOrderVO> selectBuyerRefundOrderTopNum(OrderSearchDTO dto) {
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

        List<BuyerRefundOrderVO> list = null;
        try {
            list = ycDdService.selectBuyerRefundOrderTopNum(dto);
        } catch (Exception e) {
            logger.error(UsecarOrderCode.UCAR_10005.getMessage(), e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10005, e);
        }
        return list;
    }

    /**
     * 用车采购退款单订单详情获取
     *
     * @param dto 获取订单详细数据参数
     * @return 订单详细信息
     */
    public BuyerRefundOrderDetailVO selectRefundOrderDetail(OrderSearchDTO dto) {
        //如果商户编号为空就要抛异常
        if (StringUtils.isBlank(dto.getGyShbh())) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
        }
        YcDd ycDd = ycDdService.selectYcDd(dto.getDdbh());
        //如果没有查到数据抛出异常提示
        if (ycDd == null) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10007, dto.getDdbh());
        }
        BuyerRefundOrderDetailVO buyerRefundOrderDetailVO = BeanMapper.map(ycDd, BuyerRefundOrderDetailVO.class);
        return buyerRefundOrderDetailVO;
    }
    /**
     * 采购取消退款订单
     * @param buyerNormalOrderOperateDTO 采购操作订单DTO类
     * @return 是否取消成功
     */
    @Transactional
    public Boolean cancelApply(BuyerRefundOrderDTO buyerNormalOrderOperateDTO){
        Boolean result = Boolean.FALSE;
        logger.info("请求CPS执行支付操作[订单编号:" + buyerNormalOrderOperateDTO.getDdbh() + "]");
        YcDd ycDdVe = ycDdService.selectYcDd(buyerNormalOrderOperateDTO.getDdbh());//获取单条 正常单信息
        if (ycDdVe != null && ycDdVe.getCgShbh().equals(buyerNormalOrderOperateDTO.getCgShbh())) {
//            ycDdVe.setDdzt(UsecarOrderStatusEnum.YC1F);// 订单状态未定
            ycDdVe.setDdbh(buyerNormalOrderOperateDTO.getDdbh());//订单编号
            ycDdVe.setVersion(buyerNormalOrderOperateDTO.getVersion()); //版本号加"1"
            result = ycDdService.updateYcDd(ycDdVe);//更新正常单信息
            if (result) {
                logger.info("CPS本地订单数据支付更新完成");
            }else{
                logger.info("CPS本地订单数据支付更新失败");
            }
        } else {
            logger.info("通过订单编号及版本号未能查到数据");
        }
        return result;
    }
}