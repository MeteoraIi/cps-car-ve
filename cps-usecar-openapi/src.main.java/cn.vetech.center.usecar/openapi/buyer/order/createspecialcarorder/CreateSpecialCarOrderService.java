package cn.vetech.center.usecar.openapi.buyer.order.createspecialcarorder;

import cn.vetech.center.system.openapi.IOpenApiService;
import cn.vetech.center.system.openapi.OpenApiException;
import cn.vetech.center.system.openapi.OpenApiLog;
import cn.vetech.center.system.openapi.OpenApiShShbDTO;
import cn.vetech.center.system.openapi.OpenApiShYhbDTO;
import cn.vetech.center.system.openapi.annotation.OpenApiOperation;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarCommonService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UseCarTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarCodeEnum;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarSfykjEnum;
import cn.vetech.center.usecar.common.enums.UsecarZfztEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.unpay.YcUnpayLimitLogicService;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;

/**
 * 1w 才用到这个接口
 * 采购创建专车订单到CPS库以及接口方
 *
 * @author chenyong
 * @since 2017-11-09
 */
@OpenApiOperation(value = "car_createSpecialCarOrder", title = "采购创建专车订单")
public class CreateSpecialCarOrderService implements IOpenApiService<CreateSpecialCarOrderRequest, CreateSpecialCarOrderResponse> {
    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(CreateSpecialCarOrderService.class);

    /**
     * 用车生产订单编号Service
     */
    @Autowired
    private UsecarOrderNoService orderNoService;

    /**
     * 用车订单Service
     */
    @Autowired
    private YcDdService ycDdService;

    /**
     * 用车价格缓存
     */
    @Autowired
    private UsecarCacheService usecarCacheService;

    /**
     * 产品查询公共方法service
     */
    @Autowired
    private BuyerBookSpeciCarCommonService buyerBookSpeciCarCommonService;
    /**
     * 公用service
     */
    @Autowired
    private BuyerBookCommonService commService;
    /**
     * 未付订单检查
     */
    @Autowired
    private YcUnpayLimitLogicService ycUnpayLimitLogicService;
    /**
     * 一键三单服务
     */
    @Autowired
    private ThreeOrderService threeOrderService;
    @Override
    public CreateSpecialCarOrderResponse execute(CreateSpecialCarOrderRequest request, OpenApiShShbDTO openApiShShbDTO,
                                                 OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) throws OpenApiException {
        openApiLog.setYwdh(request.getCgDdbh());
        openApiLog.add("ASMS用户：" + openApiShShbDTO.getShbh() + "进入cps专快车下单接口");
        long starttime = System.currentTimeMillis();
        CreateSpecialCarOrderResponse response = new CreateSpecialCarOrderResponse();
        if (request == null) {
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
            throw new RuntimeException("APP专快车下单到CPS对象request不能为空！");
        }
        if (StringUtils.isBlank(request.getPricecacheid())) {
            return response;
        }
        //检查乘客是否在用车黑名单里
        boolean checkFlag = commService.checkYcHmd(request.getCkxm(), request.getCksj());
        if (!checkFlag) {
            logger.info("{}在黑名单中，直接返回下单失败", request.getCkxm());
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
            response.setErrorCode(UsecarOrderCode.UCAR_10001.getCode());
            response.setErrorMessage(UsecarOrderCode.UCAR_10001.getMessage());
            return response;
        }
        UsecarOrderCode code = ycUnpayLimitLogicService.checkUnpayOrder(openApiShShbDTO.getShbh(), request.getCkxm());
        if (code != null) {
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
            response.setErrorCode(code.getCode());
            response.setErrorMessage(code.getMessage());
            return response;
        }
        BookSpeciCalCachePrice carcache = null;
        try {
            carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(request.getPricecacheid());
        } catch (Exception e) {
            openApiLog.add("**进入接送车内的专车下单到CPS模块**");
        }
        if (carcache != null) {
            boolean falg = false;
            logger.info("APP专快车下单到CPS缓存对象：【" + carcache.toString() + "】");
            YcDd ycDd = getYcDd(request, carcache, openApiShShbDTO, openApiShYhbDTO);
            //缓存中取价格信息
            if (carcache.getPrice() != null) {
                //供应结算金额
                ycDd.setGyJsje(carcache.getGyJsje());
                //采购结算金额
                ycDd.setCgJsje(carcache.getJsj());
                //供应成本价
                ycDd.setGyCbj(carcache.getPrice());
                //预估金额
                ycDd.setYgje(carcache.getJsj());
                //建议销售金额
                ycDd.setJyxsje(carcache.getPrice());
                //初始预估金额
                ycDd.setCsygje(carcache.getPrice());
                ycDd.setBuyerEstimatedPrice(carcache.getJsj());
                //2020-07-09 费控传入的是cps商户编号，非直连供应商编号信息，这里直接从缓存取，避免判断出错
//                if (UsecarGysApiEnum.checkIsNoAbsPriceSh(request.getGyShbh(), UsecarProductTypeEnum.zc.getCode())) {
                if (UsecarGysApiEnum.checkIsNoAbsPriceSh(carcache.getGysbh(), UsecarProductTypeEnum.zc.getCode())) {
                    //非一口价
                    ycDd.setSfykj(UsecarSfykjEnum.NO.getCode());
                    ycDd.setYfje(carcache.getJsj().multiply(new BigDecimal("2")));
                    falg = true;
                } else {
                    //一口价
                    ycDd.setSfykj(UsecarSfykjEnum.YES.getCode());
                    ycDd.setYfje(carcache.getJsj());
                }
            }
            commService.yesOrNoCloudOrder(ycDd);
            //如果订单来源 是直销 且是非一口价 不让下单
//            if (falg && StringUtils.isNotEmpty(request.getDdly()) && request.getDdly().indexOf(UseCarCommonUtil.DDLY) != -1) {
//                logger.info("直销订单不能下单");
//                response.setStatus(UsecarCodeEnum.FAIL.getCode());
//                response.setErrorCode(UsecarOrderCode.UCAR_10001.getCode());
//                response.setErrorMessage(UsecarOrderCode.UCAR_10001.getMessage());
//                return response;
//            }
            boolean flag = ycDdService.insertYcDd(ycDd);
            threeOrderService.createMainOrder(ycDd, UseCarTypeEnum.ONE);
            long endtime = System.currentTimeMillis();
            if (flag) {
                response.setDdbh(ycDd.getDdbh());
                openApiLog.setDdbh(ycDd.getDdbh());
                response.setDdzt(ycDd.getDdzt());
                response.setTime(new Long(endtime - starttime));
                if (StringUtils.isNotBlank(request.getAutotest()) && UseCarConstant.NUMONE.equals(request.getAutotest())) {
                    //如是测试订单，将订单数据缓存
                    usecarCacheService.putAutoTestCache(ycDd.getDdbh(), ycDd.getDdbh());
                }
            } else {
                response.setTime(new Long(endtime - starttime));
                response.setStatus(UsecarCodeEnum.FAIL.getCode());
            }
        }
        return response;
    }

    /**
     * 获取用车对象
     *
     * @param request         请求参数对象
     * @param carcache        缓存对象
     * @param openApiShShbDTO 商户信息
     * @param openApiShYhbDTO 用户信息
     * @return 用车订单对象
     */
    private YcDd getYcDd(CreateSpecialCarOrderRequest request, BookSpeciCalCachePrice carcache, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO) {
        String ddbh = orderNoService.getMainOrderNo(openApiShShbDTO.getShbh());
        if (StringUtils.isBlank(carcache.getCfdCsid())) {
            throw new RuntimeException("DDBH:" + ddbh + "APP专快车下单到CPS出发城市ID不能为空!");
        }
        YcDd ycDd = new YcDd();
        ycDd.setDdbh(ddbh);
        ycDd.setCgDdly(request.getDdly());
        ycDd.setXdsj(VeDate.getNow());
        ycDd.setDdlx(request.getDdlx());
        ycDd.setDdzt(UsecarOrderStatusEnum.YC1C.getCode());
        //数据库路由
        ycDd.setSjkly(String.valueOf(orderNoService.getSjkly(openApiShShbDTO.getShbh())));
        ycDd.setZfZt(UsecarZfztEnum.WZF.getCode());
        //付款方式，默认值：1预存款抵扣
        ycDd.setFkfs("1");
        //控润规则，默认值：3建议价
        ycDd.setPtkrgz("3");
        ycDd.setYcsj(getYcsj(request.getYcsj()));
        ycDd.setCkxm(request.getCkxm());
        String cksj=request.getCksj();
        if(StringUtils.isNotBlank(cksj)){
            cksj=cksj.replaceAll(" ","");
        }
        ycDd.setCksj(cksj);
        //服务城市区域编号
        ycDd.setJsfwqyid(request.getJsfwqyid());
        //出发城市id
        ycDd.setCfdCsid(carcache.getCfdCsid());
        //出发城市名称
        ycDd.setCfdCsmc(StringUtils.defaultIfBlank(request.getSccsMc(),carcache.getCfdCsmc()));
        if(StringUtils.isBlank(ycDd.getCfdCsmc())){
            ycDd.setCfdCsmc(commService.getCsmcByBh(ycDd.getCfdCsid()));
        }
        //出发城市poi
        ycDd.setCfd(request.getSccsPoi());
        //用车时间
        ycDd.setYcsj(getYcsj(carcache.getYcsj()));
        //出发城市详细地址
        ycDd.setCfdXxdz(StringUtils.defaultIfBlank(request.getSccsXxdz(), request.getSccsPoi()));
        //目地城市id
        ycDd.setMddCsid(carcache.getMmdCsid());
        //目地城市名称
        ycDd.setMddCsmc(StringUtils.defaultIfBlank(request.getMdcsMc(),carcache.getMddCsmc()));
        if(StringUtils.isBlank(ycDd.getMddCsmc())){
            ycDd.setMddCsmc(commService.getCsmcByBh(ycDd.getMddCsid()));
        }
        //目地城市poi
        ycDd.setMdd(request.getMdcsPoi());
        //目地城市详细地址
        ycDd.setMddXxdz(StringUtils.defaultIfBlank(request.getMdcsXxdz(), request.getMdcsPoi()));
        //车型组编号
        ycDd.setCxzbh(carcache.getCxzbh());
        //车型组名称
        ycDd.setCxzmc(carcache.getCxzmc());
        //订单备注
        ycDd.setDdbz(request.getDdbz());
        //服务内容
        ycDd.setFwnr(request.getFwnr());
        //服务备注
        ycDd.setFwbz(request.getFwbz());
        //车牌号
        ycDd.setCph(request.getCph());
        //车身颜色
        ycDd.setCsys(request.getCsys());
        //车型名称
        ycDd.setCxmc(request.getCxmc());
        //司机姓名
        ycDd.setSjxm(request.getSjxm());
        //司机电话
        ycDd.setSjdh(request.getSjdh());
        //司机性别
        ycDd.setSjxb(request.getSjxb());
        if (carcache.getBdlc() != null) {
            //本单里程
            ycDd.setBdlc(carcache.getBdlc());
        }
        if (carcache.getBdsc() != null) {
            //本单时长
            ycDd.setBdsc(carcache.getBdsc());
        }
        //采购用户编号
        ycDd.setCgYhbh(openApiShYhbDTO.getYhbh());
        //采购商户编号
        ycDd.setCgShbh(openApiShShbDTO.getShbh());
        //采购商户简称
        ycDd.setCgShjc(openApiShShbDTO.getJc());
        //采购订单编号
        ycDd.setCgDdbh(request.getCgDdbh());
        String qxgz = buyerBookSpeciCarCommonService.qxgzCheckStyle(carcache);
        //取消规则
        ycDd.setQxgz(qxgz);
        //取消公式
        ycDd.setQxgs(request.getQxgs());
        //供应商户编号
//        ycDd.setGyShbh(request.getGyShbh());
//        //供应商户名称
//        ycDd.setGyShjc(request.getGyShmc());
        //2020.6.16供应商信息从缓存中取
        ycDd.setActualSupplierNo(carcache.getActualSupplierNo());
        ycDd.setGyShbh(carcache.getGysbh());
        ycDd.setGyShjc(UsecarGysApiEnum.getShmc(carcache.getGysbh()));
        ycDd.setWbgysbh(carcache.getWbgysbh());
        ycDd.setWbgysmc(carcache.getWbgysmc());
        //平台贴点方式
        ycDd.setPttdfs(carcache.getPttdfs());
        //平台贴点比例
        ycDd.setPttdbl(carcache.getPttdbl());
        //平台贴点金额
        ycDd.setPttdje(carcache.getPttdje());
        //平台控润规则
        ycDd.setPtkrgz(carcache.getPtkrgz());
        //平台控润方式
        ycDd.setPtkrfs(carcache.getPtkrfs());
        //平台控润比例
        ycDd.setPtkrbl(carcache.getPtkrbl());
        //平台控润金额
        ycDd.setPtkrje(carcache.getPtkrje());
        //供应返佣方式
        ycDd.setGyFyfs(carcache.getGyFyfs());
        //供应返佣比例
        ycDd.setGyFybl(carcache.getGyFybl());
        //供应返佣金额
        ycDd.setGyFyje(carcache.getGyFyje());
        //供应前后返
        ycDd.setGyQhf(carcache.getGyQhf());
        ycDd.setFkje(BigDecimal.ZERO);
        //联系人
        ycDd.setLxr(request.getLxr());
        //联系人电话
        ycDd.setLxrdh(request.getLxrdh());
        getJwd(ycDd, carcache, request);
        //价格md5
        ycDd.setJgmd5(carcache.getJgmd5());
        //计价模式类别
        ycDd.setJjmslb(carcache.getJjmslb());
        //外部车型组编号
        ycDd.setWbcxzbh(carcache.getWbcxzbh());
        //外部车型组名称
        ycDd.setWbcxzmc(carcache.getWbcxzmc());
        //平台控润规则
        ycDd.setPtkrgz(request.getPtkrgz());
        /**由于用车报表查询需要，专快车订单需增加站点ID和站点名称字段默认值**/
        ycDd.setJsfwzdid(UseCarConstant.ZC_ZDID);
        ycDd.setJsfwzdmc(UseCarConstant.ZC_ZDMC);
        ycDd.setTsxq(request.getTsxq());
        ycDd.setFwbzbz(request.getFwbzbz());
        ycDd.setFwbzjb(request.getFwbzjb());
        return ycDd;
    }

    /**
     * 经纬度处理
     *
     * @param ycDd     订单实体
     * @param carcache 缓存实体
     * @param request  请求request
     * @return ycDd 实体
     */
    private YcDd getJwd(YcDd ycDd, BookSpeciCalCachePrice carcache, CreateSpecialCarOrderRequest request) {
        if (UsecarGysApiEnum.DDYC.getShbh().equals(request.getGyShbh())) {
            if (StringUtils.isBlank(carcache.getSosoCfdX()) || StringUtils.isBlank(carcache.getSosoMddX())) {
                throw new RuntimeException("DDBH:" + ycDd.getDdbh() + "滴滴坐标丢失!");
            }
            //出发地经度
            ycDd.setCfdX(carcache.getSosoCfdX());
            //出发地纬度
            ycDd.setCfdY(carcache.getSosoCfdY());
            //目的地经度
            ycDd.setMddX(carcache.getSosoMddX());
            //目的地纬度
            ycDd.setMddY(carcache.getSosoMddY());
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
     * 获取用车时间
     *
     * @param ycsj 用车时间
     * @return 用车时间
     */
    private String getYcsj(String ycsj) {
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
}