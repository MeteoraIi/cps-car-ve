package cn.vetech.center.usecar.openapi.buyer.order.createorder;

import cn.vetech.center.usecar.book.buyer.service.BuyerBookService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarService;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.order.seller.dto.SellerNormalOrderOperateDTO;
import cn.vetech.center.usecar.order.seller.service.SellerOrderService;
import cn.vetech.center.usecar.service.order.YcDdService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.time.VeDate;

/**
 * 下单公共
 *
 * @author : Y
 * @since 2023/8/14 10:14
 */
@Service
public class CreateOrderCommonService {
    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(CreateOrderCommonService.class);
    /**
     * 用车订单Service
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * 接送车下单Service
     */
    @Autowired
    private BuyerBookService jscBookService;
    /**
     * 转快车下单
     */
    @Autowired
    private BuyerBookSpeciCarService buyerBookSpeciCarService;
    /**
     * 供应商操作服务类
     */
    @Autowired
    private SellerOrderService sellerOrderService;
    /**
    /**
     * 获取用车时间
     *
     * @param ycsj 用车时间
     * @return 用车时间
     */
    public String getYcsj(String ycsj) {
        if (StringUtils.isBlank(ycsj)) {
            return null;
        }
        ycsj = ycsj.trim();
        logger.info("ycsj:{}", ycsj);
        final int defaultYcsjLength = 16;
        if (ycsj.length() <= defaultYcsjLength) {
            return ycsj;
        } else {
            return VeDate.formatToStr(VeDate.strToDateLong(ycsj), "yyyy-MM-dd HH:mm");
        }
    }
    /**
     * 经纬度处理
     *
     * @param ycDd     订单实体
     * @param carcache 缓存对象
     * @return ycDd 订单实体
     */
    public YcDd getJwd(YcDd ycDd, BookSpeciCalCachePrice carcache) {
        if (UsecarGysApiEnum.DDYC.getShbh().equals(carcache.getGysbh())) {
            if (StringUtils.isNotBlank(carcache.getSosoCfdX()) && StringUtils.isNotBlank(carcache.getSosoMddX())) {
                //出发地经度
                ycDd.setCfdX(carcache.getSosoCfdX());
                //出发地纬度
                ycDd.setCfdY(carcache.getSosoCfdY());
                //目的地经度
                ycDd.setMddX(carcache.getSosoMddX());
                //目的地纬度
                ycDd.setMddY(carcache.getSosoMddY());
            } else {
                //出发地经度
                ycDd.setCfdX(carcache.getCfdX());
                //出发地纬度
                ycDd.setCfdY(carcache.getCfdY());
                //目的地经度
                ycDd.setMddX(carcache.getMddX());
                //目的地纬度
                ycDd.setMddY(carcache.getMddY());
            }
        } else {
            if (StringUtils.isBlank(carcache.getCfdX()) || StringUtils.isBlank(carcache.getMddX())) {
                throw new RuntimeException("DDBH" + ycDd.getDdbh() + "坐标丢失!");
            }
            //出发地经度
            ycDd.setCfdX(carcache.getCfdX());
            //出发地纬度
            ycDd.setCfdY(carcache.getCfdY());
            //目的地经度
            ycDd.setMddX(carcache.getMddX());
            //目的地纬度
            ycDd.setMddY(carcache.getMddY());
        }
        return ycDd;
    }

    /**
     * 专快车 支付前下单供应
     * @param ycDdVe 订单
     */
    public void createBeforePay(YcDd ycDdVe){
        String ddbh=ycDdVe.getDdbh();
        if (StringUtils.isNotBlank(ycDdVe.getGyShbh()) && UsecarGysApiEnum.checkIsApiGys(ycDdVe.getGyShbh())) {
            if(!buyerBookSpeciCarService.createOrderToGys(ddbh)) {
                logger.error("订单[" + ddbh + "]下单到供应商失败:");
                YcDd ycDd = ycDdService.selectYcDd(ycDdVe.getDdbh());
                if (StringUtils.equals(ycDd.getZfZt(), "1")) {
                    ycDd.setDdzt(UsecarOrderStatusEnum.YC2B.getCode());
                } else {
                    ycDd.setDdzt(UsecarOrderStatusEnum.YC2A.getCode());
                }
                ycDdService.updateYcDd(ycDd);
                SellerNormalOrderOperateDTO refuseDto = new SellerNormalOrderOperateDTO();
                refuseDto.setDdbh(ycDd.getDdbh());
                refuseDto.setGyJudr("供应商");
                refuseDto.setGyJudyy("未返回供应订单编号！");
                refuseDto.setGyJudshbh(ycDd.getGyShbh());
                refuseDto.setGyShbh(ycDd.getGyShbh());
                refuseDto.setXguserid(ycDd.getGyShbh());
                refuseDto.setXguserxm(ycDd.getGyShjc());
                refuseDto.setJdSxf("0");
                logger.info("订单{}下单到供应失败，进入退款操作...",ycDd.getDdbh());
                sellerOrderService.refuseOrder(refuseDto);
            }
        } else {
            try {//自签产品,推送给ASMS的供应商
                logger.info("推送采购信息给供应商ASMS平台采购订单编号为：【" + ycDdVe.getDdbh() + "】");
                jscBookService.createOrderToAsmsSeller(ycDdVe);
            }catch(Exception e){
                logger.error("推送采购信息给供应商ASMS平台异常", e);
                logger.info("推送【" + ycDdVe.getDdbh() + "】推送采购信息到供应商【" + ycDdVe.getGyShbh() + "】ASMS平台出现异常：\n" + e.getMessage());
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_30011);
            }
        }
    }
}