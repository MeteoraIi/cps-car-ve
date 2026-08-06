package cn.vetech.center.usecar.order.buyer.service;

import cn.vetech.center.usecar.apiclient.pay.IUsecarPayWithholdingServiceClient;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarMakeupStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDdBcd;
import cn.vetech.center.usecar.mapper.order.YcDdBcdMapper;
import cn.vetech.center.usecar.order.cpsa.dto.CpsaMakeupOrderDTO;
import cn.vetech.center.usecar.order.cpsa.vo.CpsaMakeupOrderVO;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdBcdService;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.base.PageDTO;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.ArrayList;
import java.util.List;


/**
 * 补差单服务类
 *
 * @author chenyong
 * @since 2017-10-11
 */
@Service
public class BuyerMakeupService extends ServiceImpl<YcDdBcdMapper, YcDdBcd> {
    /**
     * logger 日志记录
     */
    private final Logger logger = LoggerFactory.getLogger(BuyerMakeupService.class);


    /**
     * 用车补差单 服务
     */
    @Autowired
    private YcDdBcdService ycDdBcdService;
    /**
     * 订单编号服务类
     */
    @Autowired
    private UsecarOrderNoService usecarOrderNoService;
    /**
     * 采购操作业务服务类
     */
    @Autowired
    private IUsecarPayWithholdingServiceClient usecarPayWithholdingServiceClient;

    /**
     * 新增补差单
     *
     * @param dto 补差单数据
     * @return d
     */
    public Boolean savebcd(CpsaMakeupOrderDTO dto) {
        try {
            if (null != dto) {
                YcDdBcd zcbcd = BeanMapper.map(dto, YcDdBcd.class);
                String bcdh = usecarOrderNoService.getNormalMakeupNo(dto.getDdbh());
                zcbcd.setBcdh(bcdh);
                zcbcd.setSqJs(VeDate.getNow());    //设置申请时间为当前时间
                zcbcd.setDdlx(dto.getBcdddlx());  //设置订单类型为正常单
                zcbcd.setZt(UseCarConstant.NUMZERO);   //设置发起的补差单为待审核状态
                zcbcd.setSqshbh(dto.getSqshbh());
                zcbcd.setSqshmc(dto.getSqshmc());
                zcbcd.setSqrlx(dto.getSqrlx());
                zcbcd.setCfd(dto.getCfd());
                zcbcd.setMdd(dto.getMdd());
                zcbcd.setCkxm(dto.getCkxm());
                zcbcd.setCksj(dto.getCksj());
                zcbcd.setCgShbh(dto.getCgShbh());
                zcbcd.setGyShbh(dto.getBlShbh());
                ycDdBcdService.insertYcDdBcd(zcbcd);
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新补差单状态  审核，取消
     *
     * @param dto 租车订单补差单
     * @return 是否编辑成功
     */
    public Boolean editOrderMakeup(CpsaMakeupOrderDTO dto) {
        try {
            if (null != dto) {
                YcDdBcd ycDdBcd = BeanMapper.map(dto, YcDdBcd.class);
                ycDdBcd.setShSj(VeDate.getNow());
                ycDdBcdService.updateYcDdBcd(ycDdBcd);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Boolean.TRUE;
    }

    /**
     * 补差单支付前检查,验证完成后自动代扣
     *
     * @param dto 支付检测传的参数
     * @return String 验证后的状态
     */
    public String payBussinessCheck(CpsaMakeupOrderDTO dto) {
        String resultData = "-1";
        if (null == dto) {
            return resultData;
        }
        YcDdBcd ycDdBcd = null;
        logger.info("进入补差单支付前检查,补差单号为:" + dto.getBcdh() + ",供应商编号:" + dto.getGyShbh());
        try {
            ycDdBcd = ycDdBcdService.selectYcDdBcd(dto.getBcdh());
            logger.info("根据补差单号查询结果: 【补差单号: " + ycDdBcd.getBcdh() + ",补差状态：" + ycDdBcd.getZt() + ",数据库路由: " + ycDdBcd.getSjkly() + "】");
        } catch (Exception e) {
            logger.error("根据补差单号查询补差单数据异常", e);
        }
        if (ycDdBcd == null) {
            return resultData;
        }
        if (UsecarMakeupStatusEnum.YSHDZF.getCode().equals(ycDdBcd.getZt())) {
            resultData = "0";
            ycDdBcd.setBlShmc(dto.getBlShmc());
            ycDdBcd.setBlShbh(dto.getBlShbh());
            if (UseCarConstant.ZT_TWO.equals(ycDdBcd.getSqrlx())) {
                ycDdBcd.setBlrLx(UseCarConstant.ZT_ONE);
            } else {
                ycDdBcd.setBlrLx(UseCarConstant.ZT_TWO);
            }
            ycDdBcdService.updateYcDdBcd(ycDdBcd);
        }
        return resultData;
    }

    /**
     * 分页查询补差单数据
     *
     * @param dto 产品分页查询条件
     * @return 产品分页数据
     */
    public Page<CpsaMakeupOrderVO> selectPage(PageDTO<CpsaMakeupOrderDTO> dto) {
        Page<YcDdBcd> bcdPage = ycDdBcdService.selectYcDdBcdPage(dto);
        Page<CpsaMakeupOrderVO> bcdVOPage = getOrdertipVOPage(bcdPage);
        List<CpsaMakeupOrderVO> list = bcdVOPage.getRecords();
        List<CpsaMakeupOrderVO> list1 = new ArrayList<CpsaMakeupOrderVO>();
        for (CpsaMakeupOrderVO vo : list) {
            if (dto.getData().getSqshbh().equals(vo.getSqshbh())) {
                vo.setFzje(vo.getBcje());
            }
            list1.add(vo);
        }
        return bcdVOPage.setRecords(list1);
    }

    /**
     * 将产补差单信息转成 页面显示的产品VO分页信息
     *
     * @param bcBcdPage 产品分页信息
     * @return 产品VO分页信息
     */
    private Page<CpsaMakeupOrderVO> getOrdertipVOPage(Page<YcDdBcd> bcBcdPage) {
        Page<CpsaMakeupOrderVO> bcdVOPage;
        List<YcDdBcd> bcdList = bcBcdPage.getRecords();
        List<CpsaMakeupOrderVO> bcdVOList = BeanMapper.mapList(bcdList, YcDdBcd.class, CpsaMakeupOrderVO.class);
        bcdVOPage = BeanMapper.map(bcBcdPage, Page.class);
        bcdVOPage.setRecords(bcdVOList);
        return bcdVOPage;
    }

    /**
     * 查询表头补差单数量
     *
     * @param dto 补差单数据
     * @return list
     */
    public List<CpsaMakeupOrderVO> getMakeupNum(CpsaMakeupOrderDTO dto) {
        //如果商户编号为空就要抛异常
        if (StringUtils.isBlank(dto.getCgShbh())) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
        }
        List<CpsaMakeupOrderVO> list = null;
        try {
            list = ycDdBcdService.selectBuyerOrderTopNum(dto);
        } catch (Exception e) {
            logger.error(UsecarOrderCode.UCAR_10005.getMessage(), e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10005, e);
        }
        return list;
    }

    /**
     * 补差单详情数据
     *
     * @param dto 查询数据
     * @return 补差单详情
     */
    public CpsaMakeupOrderVO getMakeupDetail(CpsaMakeupOrderDTO dto) {
        if (null != dto) {
            YcDdBcd ycDdBcd = ycDdBcdService.getMakeupDtail(dto);
            CpsaMakeupOrderVO cpsaMakeupOrderVO = BeanMapper.map(ycDdBcd, CpsaMakeupOrderVO.class);
            if (StringUtils.equals(dto.getCgShbh(), cpsaMakeupOrderVO.getSqshbh())) {
                cpsaMakeupOrderVO.setFzje(cpsaMakeupOrderVO.getBcje());
            }
            return cpsaMakeupOrderVO;
        }
        return null;
    }
}