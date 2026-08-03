package cn.vetech.center.usecar.cpsa.setting.applyinvoice;

import cn.vetech.center.car.dto.ApplyInvoiceDTO;
import cn.vetech.center.cps.invoice.api.dto.InvoiceDetail;
import cn.vetech.center.cps.invoice.api.dto.OpenInvoiceDTO;
import cn.vetech.center.cps.invoice.api.vo.OpenInvoiceVO;
import cn.vetech.center.usecar.apiclient.invocie.ICpsaApplyInvoiceServiceClient;
import cn.vetech.center.usecar.cpsa.CpsaBaseController;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.entity.usecar.YcKhSjzfxx;
import cn.vetech.center.usecar.notice.buyer.dto.ApplyInvoiceNotifyDetail;
import cn.vetech.center.usecar.notice.buyer.dto.UseCarApplyInvoiceNotifyDTO;
import cn.vetech.center.usecar.notice.buyer.service.BuyerNoticeService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.service.usecar.YcKhSjzfxxService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vetech.core.api.RestResponse;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.sequence.IdGenerator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * cpsa申请开票管理
 * @author xufei
 * @since 2023/4/3
 */
@RestController
@RequestMapping("/cpsa/applyInvoice")
public class CpsaApplyInvoiceController extends CpsaBaseController {
    /**
     * 产品编号
     */
    private static final String CPBH = "1000";
    /**
     * 订单类型
     */
    private static final String DDLX = "10001";
    /**
     * CPS客服代申请
     */
    public static final String CPS_OPEN_INVOICE = "2";
    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(CpsaApplyInvoiceController.class);
    /**
     * 用车订单服务
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     *  用车订单主单服务
     */
    @Autowired
    private YcDdMainService ycDdMainService;
    /**
     * 开票服务
     */
    @Autowired
    private ICpsaApplyInvoiceServiceClient iCpsaApplyInvoiceServiceClient;
    /**
     * 通知服务
     */
    @Autowired
    private BuyerNoticeService buyerNoticeService;
    /**
     * 用车客户实际支付方式服务
     */
    @Autowired
    private YcKhSjzfxxService ycKhSjzfxxService;
    /**
     * 申请开票
     * @param dto 申请开票入参
     * @return 开票结果
     */
     @PostMapping(value = "sqkp")
    public RestResponse<Boolean> applyInvoice(@RequestBody ApplyInvoiceDTO dto){
        String ddbh = dto.getDdbh();
        YcDd ycDd = ycDdService.selectById(ddbh);
        if (ycDd == null) {
            logger.info("根据订单编号" + ddbh + ",查询数据失败");
            RestResponse resp = new RestResponse();
            resp.setMessage("根据订单编号" + ddbh + ",查询数据失败");
            resp.setResult(false);
            return resp;
        }
        if(!checkSkFs(dto, ycDd)){
            RestResponse resp = new RestResponse();
            resp.setMessage("根据订单编号" + ddbh + ",开票失败，缺少实际支付方式");
            resp.setResult(false);
            return resp;
        }

        OpenInvoiceDTO invoiceDTO = buildInvoiceDto(ycDd, dto,loginUser.getXm());
        invoiceDTO.setUserId(loginUser.getQyyggh());
        try {
            //获得本机IP
            InetAddress addr = InetAddress.getLocalHost();
            invoiceDTO.setClientip(addr.getHostAddress());

        } catch (UnknownHostException e) {
            logger.error("获得本机IP异常：{}", e);
        }
        try {
            logger.info("用车开票入参：{}", JsonMapper.nonEmptyMapper().toJson(invoiceDTO));
            OpenInvoiceVO invoiceVO = iCpsaApplyInvoiceServiceClient.openInvoice(invoiceDTO);
            logger.info("用车开票出参：{}", JsonMapper.nonEmptyMapper().toJson(invoiceVO));
            if(invoiceVO == null || invoiceVO.getStatus()!=0){
                logger.info("用车开票失败",invoiceVO.getErrorMessage());
                RestResponse resp = new RestResponse();
                if(invoiceVO != null){
                    resp.setMessage(invoiceVO.getErrorMessage());
                }
                resp.setResult(false);
                return resp;
            }
            //开票成功通知
            UseCarApplyInvoiceNotifyDTO useCarApplyInvoiceNotifyDTO = new UseCarApplyInvoiceNotifyDTO();
            useCarApplyInvoiceNotifyDTO.setCpbh(CPBH);
            useCarApplyInvoiceNotifyDTO.setBusinessNo(ycDd.getCgShbh());
            useCarApplyInvoiceNotifyDTO.setCpsDdbh(ycDd.getDdbh());
            useCarApplyInvoiceNotifyDTO.setGysBh(ycDd.getGyShbh());
            List<ApplyInvoiceNotifyDetail> fpList = BeanMapper.mapList(invoiceDTO.getFplist(), InvoiceDetail.class , ApplyInvoiceNotifyDetail.class);
            useCarApplyInvoiceNotifyDTO.setFplist(fpList);
            buyerNoticeService.applyInvoiceNotify(useCarApplyInvoiceNotifyDTO);
        }catch (Exception e){
            logger.error("用车开票异常", e);
            return new RestResponse<>(false);
        }
        return new RestResponse<>(true);
    }

    /**
     * 申请开票
     * @param dto 申请开票入参
     * @return 开票结果
     */
    @PostMapping(value = "mainOrderSqkp")
    public RestResponse<Boolean> applyMainOrderInvoice(@RequestBody ApplyInvoiceDTO dto){
        String ddbh = dto.getDdbh();
        YcDdMain ycDd = ycDdMainService.selectByIdCache(ddbh);
        if (ycDd == null) {
            logger.info("根据订单编号" + ddbh + ",查询数据失败");
            RestResponse resp = new RestResponse();
            resp.setMessage("根据订单编号" + ddbh + ",查询数据失败");
            resp.setResult(false);
            return resp;
        }
        if(!checkSkFs(ycDd.getpDdbh())){
            RestResponse resp = new RestResponse();
            resp.setMessage("根据订单编号" + ddbh + ",开票失败，缺少实际支付方式");
            resp.setResult(false);
            return resp;
        }

        OpenInvoiceDTO invoiceDTO = buildInvoiceDto(ycDd, dto);
        invoiceDTO.setUserId(loginUser.getQyyggh());
        try {
            //获得本机IP
            InetAddress addr = InetAddress.getLocalHost();
            invoiceDTO.setClientip(addr.getHostAddress());

        } catch (UnknownHostException e) {
            logger.error("获得本机IP异常：{}", e);
        }
        try {
            logger.info("用车开票入参：{}", JsonMapper.nonEmptyMapper().toJson(invoiceDTO));
            OpenInvoiceVO invoiceVO = iCpsaApplyInvoiceServiceClient.openInvoice(invoiceDTO);
            logger.info("用车开票出参：{}", JsonMapper.nonEmptyMapper().toJson(invoiceVO));
            if(invoiceVO == null || invoiceVO.getStatus()!=0){
                logger.info("用车开票失败",invoiceVO.getErrorMessage());
                RestResponse resp = new RestResponse();
                if(invoiceVO != null){
                    resp.setMessage(invoiceVO.getErrorMessage());
                }
                resp.setResult(false);
                return resp;
            }
            //开票成功通知
            UseCarApplyInvoiceNotifyDTO useCarApplyInvoiceNotifyDTO = new UseCarApplyInvoiceNotifyDTO();
            useCarApplyInvoiceNotifyDTO.setCpbh(CPBH);
            useCarApplyInvoiceNotifyDTO.setBusinessNo(ycDd.getCgshbh());
            useCarApplyInvoiceNotifyDTO.setCpsDdbh(ycDd.getpDdbh());
            useCarApplyInvoiceNotifyDTO.setGysBh(ycDd.getGyShbh());
            List<ApplyInvoiceNotifyDetail> fpList = BeanMapper.mapList(invoiceDTO.getFplist(), InvoiceDetail.class , ApplyInvoiceNotifyDetail.class);
            useCarApplyInvoiceNotifyDTO.setFplist(fpList);
            buyerNoticeService.applyInvoiceNotify(useCarApplyInvoiceNotifyDTO);
        }catch (Exception e){
            logger.error("用车开票异常", e);
            return new RestResponse<>(false);
        }
        return new RestResponse<>(true);
    }

    /**
     *
     * @param dto  入参
     * @param ycDd 订单
     * @return 检查是否有支付方式，异常返回true，有支付方式放回true，没有返回false
     */
    private boolean checkSkFs(@RequestBody ApplyInvoiceDTO dto, YcDd ycDd) {
        List<YcKhSjzfxx> ycKhSjzfxxList = Lists.newLinkedList();
        try {
            // 查询用车客户实际支付方式
            ycKhSjzfxxList = ycKhSjzfxxService.selectListByDdbh(dto.getDdbh());
            // 一键三单通过主表查询支付记录
            if (CollectionUtil.isEmpty(ycKhSjzfxxList) && org.apache.commons.lang3.StringUtils.isNotBlank(ycDd.getpDdbh())) {
                ycKhSjzfxxList = ycKhSjzfxxService.selectListByDdbh(ycDd.getpDdbh());
            }
        }catch (Exception e){
            logger.error("查询支付明细异常",e);
            return true;
        }
        boolean hasPayWay = false;
        if(CollectionUtils.isNotEmpty(ycKhSjzfxxList)){
            for (YcKhSjzfxx e :ycKhSjzfxxList) {
                String skFs = e.getSkKm();
                if(StringUtils.isNotBlank(skFs)){
                    hasPayWay = true;
                    break;
                }

            }
        }
        return true;
    }

    /**
     *
     * @param ddbh 订单
     * @return 检查是否有支付方式，异常返回true，有支付方式放回true，没有返回false
     */
    private boolean checkSkFs(String ddbh) {
        List<YcKhSjzfxx> ycKhSjzfxxList = Lists.newLinkedList();
        try {
            // 查询用车客户实际支付方式
            ycKhSjzfxxList = ycKhSjzfxxService.selectListByDdbh(ddbh);
        }catch (Exception e){
            logger.error("查询支付明细异常",e);
            return true;
        }
        boolean hasPayWay = false;
        if(CollectionUtils.isNotEmpty(ycKhSjzfxxList)){
            for (YcKhSjzfxx e :ycKhSjzfxxList) {
                String skFs = e.getSkKm();
                if(StringUtils.isNotBlank(skFs)){
                    hasPayWay = true;
                    break;
                }

            }
        }
        return hasPayWay;
    }
    /**
     * 构建开票入参
     * @param ycDd 用车订单
     * @param dto 发票入参
     * @return 开票入参
     */
    private OpenInvoiceDTO buildInvoiceDto(YcDd ycDd, ApplyInvoiceDTO dto,String userName) {
        OpenInvoiceDTO invoiceDTO = new OpenInvoiceDTO();
        List<InvoiceDetail> fpList = new ArrayList<>();
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        String kpsqid = IdGenerator.getHexId();
        invoiceDetail.setId(kpsqid);
        invoiceDetail.setDdbh(ycDd.getDdbh());
        invoiceDetail.setCpbh(CPBH);
        invoiceDetail.setSendMail(dto.getEmail());
        invoiceDetail.setNsrsbh(dto.getNsrsbh());
        invoiceDetail.setFptt(dto.getFptt());
        invoiceDetail.setZcdh(dto.getZcdh());
        invoiceDetail.setZcdz(dto.getZcdz());
        invoiceDetail.setKhyh(dto.getKhyh());
        invoiceDetail.setYhzh(dto.getYhzh());
        invoiceDetail.setFpbz(dto.getFpbz());
        invoiceDetail.setFpxxSkr(userName);
        invoiceDetail.setFpnr(StringUtils.defaultIfBlank(dto.getFpnr(),"代订用车"));
        if(StringUtils.contains(invoiceDetail.getFpnr(),"旅游服务-")){
            invoiceDetail.setFpnr(invoiceDetail.getFpnr().replace("旅游服务-",""));
        }
        invoiceDetail.setDdlx(DDLX);
        invoiceDetail.setXh("1");
        invoiceDetail.setRzrxm(ycDd.getCkxm());
        invoiceDetail.setClkid(loginUser.getBh());
        invoiceDetail.setFpje(StringUtils.trimToEmpty(dto.getFpje()));
        //默认电子票
        invoiceDetail.setFplb("2");
        //默认按单
        invoiceDetail.setFplx(StringUtils.defaultIfBlank(dto.getFplx(),"2"));
        invoiceDetail.setFpzt("0");
        invoiceDetail.setFpdw("次");
        invoiceDetail.setFpsl("0.06");
        invoiceDetail.setFphwsl("1.0");
        invoiceDetail.setCllx(ycDd.getClyy());
        fpList.add(invoiceDetail);
        invoiceDTO.setFplist(fpList);
        invoiceDTO.setIsApplyOffline("1");
        invoiceDTO.setShbh(ycDd.getCgShbh());
        invoiceDTO.setShmc(ycDd.getCgShjc());
        invoiceDTO.setDataSources(CPS_OPEN_INVOICE);
        return invoiceDTO;
    }

    /**
     * 构建开票入参
     * @param ycDd 用车订单
     * @param dto 发票入参
     * @return 开票入参
     */
    private OpenInvoiceDTO buildInvoiceDto(YcDdMain ycDd, ApplyInvoiceDTO dto) {
        OpenInvoiceDTO invoiceDTO = new OpenInvoiceDTO();
        List<InvoiceDetail> fpList = new ArrayList<>();
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        String kpsqid = IdGenerator.getHexId();
        invoiceDetail.setId(kpsqid);
        invoiceDetail.setDdbh(ycDd.getpDdbh());
        invoiceDetail.setCpbh(CPBH);
        invoiceDetail.setSendMail(dto.getEmail());
        invoiceDetail.setNsrsbh(dto.getNsrsbh());
        invoiceDetail.setFptt(dto.getFptt());
        invoiceDetail.setZcdh(dto.getZcdh());
        invoiceDetail.setZcdz(dto.getZcdz());
        invoiceDetail.setKhyh(dto.getKhyh());
        invoiceDetail.setYhzh(dto.getYhzh());
        invoiceDetail.setFpbz(dto.getFpbz());
        invoiceDetail.setFpnr(StringUtils.defaultIfBlank(dto.getFpnr(),"代订用车"));
        if(StringUtils.contains(invoiceDetail.getFpnr(),"旅游服务-")){
            invoiceDetail.setFpnr(invoiceDetail.getFpnr().replace("旅游服务-",""));
        }
        invoiceDetail.setDdlx(DDLX);
        invoiceDetail.setXh("1");
        invoiceDetail.setRzrxm(ycDd.getCkxm());
        invoiceDetail.setClkid(loginUser.getBh());
        invoiceDetail.setFpje(dto.getFpje());
        //默认电子票
        invoiceDetail.setFplb("2");
        //默认按单
        invoiceDetail.setFplx("2");
        invoiceDetail.setFpzt("0");
        invoiceDetail.setFpdw("次");
        invoiceDetail.setFpsl("0.06");
        invoiceDetail.setFphwsl("1.0");
        invoiceDetail.setCllx(ycDd.getClyy());
        fpList.add(invoiceDetail);
        invoiceDTO.setFplist(fpList);
        invoiceDTO.setIsApplyOffline("1");
        invoiceDTO.setShbh(ycDd.getCgshbh());
        invoiceDTO.setShmc(ycDd.getCgShjc());
        invoiceDTO.setDataSources(CPS_OPEN_INVOICE);
        return invoiceDTO;
    }
}