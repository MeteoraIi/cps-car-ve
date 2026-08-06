package cn.vetech.center.usecar.book.buyer.specicar.service;

import cn.vetech.center.config.mybatisplus.cipher.annotation.CryptService;
import cn.vetech.center.link.usecar.dto.LinkCancelSpecialOrderDTO;
import cn.vetech.center.link.usecar.dto.LinkChildOrderDTO;
import cn.vetech.center.link.usecar.dto.LinkCreateSpecialOrderDTO;
import cn.vetech.center.link.usecar.vo.LinkCancelSpecialOrderVO;
import cn.vetech.center.link.usecar.vo.LinkCreateSpecialOrderVO;
import cn.vetech.center.usecar.apiclient.linkusecar.ILinkSpecialCarServiceClient;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCarOrderDTO;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCarSearchDTO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarCommonVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarOrderVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarProductVO;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarRiskCodeEnum;
import cn.vetech.center.usecar.common.enums.UsecarXxidEnum;
import cn.vetech.center.usecar.common.enums.UsecarZfztEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdCk;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.mq.send.SendMessageService;
import cn.vetech.center.usecar.notice.buyer.dto.ConfirmOrderStatusNotifyToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.service.BuyerNoticeService;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.risk.UsecarRiskControlService;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.UsecarGysDdbhCacheMap;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdCkService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.orderes.YcDdEsV2Service;
import cn.vetech.center.usecar.service.ordermq.YcXxMdCommonService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.service.usecar.YcSupplierInterfaceCountService;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vetech.core.api.RestResponse;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.time.VeDate;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.SupplierInterfaceOperateEnum.ORDER;
import static cn.vetech.center.usecar.common.enums.UsecarGysApiEnum.*;
import static cn.vetech.center.usecar.mq.send.SendMessageService.USE_CAR_CREATE_ORDER_TITLE;
import static cn.vetech.center.usecar.setting.buyerfilter.service.CarSupplierChannelTypeService.setChannelType;

/**
 * 专快车采购查询预订服务
 *
 * @author chenyong
 * @since 2017-10-10
 */
@Service
public class BuyerBookSpeciCarService {
    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(BuyerBookSpeciCarService.class);
    /**
     * 产品查询对象
     */
    @Autowired
    private BuyerBookSpeciCarProductService buyerBookSpeciCarProductService;

    /**
     * 产品查询公共方法service
     */
    @Autowired
    private BuyerBookSpeciCarCommonService buyerBookSpeciCarCommonService;
    /**
     * link专快车入口
     */
    @Autowired
    private DefaultCancelService defaultCancelService;
    /**
     * 用车生产订单编号Service
     */
    @Autowired
    private UsecarOrderNoService orderNoService;
    /**
     * 用车风险管控服务
     */
    @Autowired
    private UsecarRiskControlService usecarRiskControlService;
    /**
     * 用车专快车缓存对象
     */
    @Autowired
    private BuyerBookSpeciCarCacheService buyerBookSpeciCarCacheService;

    /**
     * 用车订单Service
     */
    @Autowired
    private YcDdService ycDdService;

    /**
     * 用车订单乘客
     */
    @Autowired
    private YcDdCkService ycDdCkService;

    /**
     * 缓存
     */
    @Autowired
    private RedisCacheManage veCacheManage;

    /**
     * 用车专快车接口对象
     */
    @Autowired
    private ILinkSpecialCarServiceClient iLinkSpecialCarServiceClient;

    /**
     * 推送给采购ASMS工具类
     */
    @Autowired
    private BuyerNoticeService buyerNoticeService;
    /**
     * 缓存服务
     */
    @Autowired
    private UsecarCacheService cacheService;
    /**
     * 缓存时间
     **/
    private static final int CACHE_M_5 = 300;
    /**
     * 消息埋点服务
     */
    @Autowired
    private YcXxMdCommonService ycXxMdCommonService;
    /**
     * 用车缓存服务
     */
    @Autowired
    private UsecarCacheService usecarCacheService;
    /**
     * 下单公共服务
     */
    @Autowired
    private BuyerBookCommonService buyerBookCommonService;

    @Autowired
    private YcDdEsV2Service ycDdEsV2Service;
    /**
     * 加解密服务
     */
    @Autowired
    private CryptService cryptService;
    /**
     * 接口记录
     */
    @Autowired
    private YcSupplierInterfaceCountService ycSupplierInterfaceCountService;
    /**
     * 用车主订单
     */
    @Autowired
    private YcDdMainService ycDdMainService;
    /**
     * 用车专快车产品询价入口SERVICE
     *
     * @param searchDTO 查询条件对象
     * @return 专快车产品集合
     */
    public BookSpeciCarVO searchSpeciCarProducts(BookSpeciCarSearchDTO searchDTO) {
        logger.info("专快车产品询价===>入参：" + searchDTO.toString());
        long f1 = System.currentTimeMillis();
        if (StringUtils.isBlank(searchDTO.getCfd())) {
            throw new RuntimeException("出发地POI不能为空");
        }
        if (StringUtils.isBlank(searchDTO.getMdd())) {
            throw new RuntimeException("目的地POI不能为空");
        }
        if (StringUtils.isNotBlank(searchDTO.getYdfs()) && UseCarConstant.NUMONE.equals(searchDTO.getYdfs())) {
            searchDTO.setCfrq(VeDate.getStringDateShort());
            searchDTO.setCfsj(VeDate.getTimeShort());
        } else if (StringUtils.isNotBlank(searchDTO.getYdfs()) && UseCarConstant.NUMTWO.equals(searchDTO.getYdfs())) {
            searchDTO.setYcsj(searchDTO.getCfrq() + " " + searchDTO.getCfsj());
        }
        searchDTO.setQdly("CPS");
        BookSpeciCarVO bookSpeciCarVO = new BookSpeciCarVO();
        bookSpeciCarVO.setBookSpeciCarSearchDTO(searchDTO);
        long f2 = System.currentTimeMillis();
        logger.info("==#2CPS专快车询价参数准备耗时:" + (f2 - f1));
        BookSpeciCarCommonVO bookSpeciCarCommonVO = buyerBookSpeciCarProductService.getZcLinkCpsList(searchDTO);
        bookSpeciCarVO.setFinishFlag(bookSpeciCarCommonVO.getFinishFlag());
        bookSpeciCarVO.setCursorId(bookSpeciCarCommonVO.getCursorId());
        long f5 = System.currentTimeMillis();
        logger.info("==#5询价方法调用完成:" + (f5 - f2));
        List<BookSpeciCarProductVO> bookSpeciCarProductVOs = bookSpeciCarCommonVO.getBookSpeciCarProductVOs();
        if (CollectionUtil.isNotEmpty(bookSpeciCarProductVOs)) {
            logger.info("--进行产品其他条件匹配--");
            buyerBookSpeciCarCommonService.getProductFwnr(bookSpeciCarProductVOs);
            buyerBookSpeciCarCommonService.setSellerLog(bookSpeciCarProductVOs, bookSpeciCarVO);//查询供应商对应的商户logo
            buyerBookSpeciCarCommonService.getCxzJcxxLst(bookSpeciCarProductVOs, bookSpeciCarVO);//查询车型组对应的车品牌以及产品数据层级关系
        }
        if (StringUtils.isNotBlank(searchDTO.getCfrq())) {
            searchDTO.setWeek(VeDate.getWeekXq(searchDTO.getCfrq()));
            searchDTO.setCfrq(VeDate.formatToStr(VeDate.formatToDate(searchDTO.getCfrq(), "yyyy-MM-dd"), "yyyy年MM月dd日"));
        }
        logger.info("专快车产品询价===>出参：" + bookSpeciCarVO.toString());
        long f6 = System.currentTimeMillis();
        logger.info("==#6对专快车产品做后续处理耗时:" + (f6 - f5));
        return bookSpeciCarVO;
    }

    /**
     * 专快车下单方法
     *
     * @param param 专快车下单DTO
     * @return 下单成功后订单对象
     */
    @Transactional
    public BookSpeciCarOrderVO createSpeciCarOrder(BookSpeciCarOrderDTO param) {
        if (null == param) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30009);
        }
        logger.info("专快车下单==>页面入参：" + param.toString());
        BookSpeciCarOrderVO bookSpeciCarOrderVO = new BookSpeciCarOrderVO();
        try {
            String ddbh = orderNoService.getNormalOrderNo(param.getCgShbh());
            //快速对象拷贝
            Type<BookSpeciCarOrderDTO> paramType = BeanMapper.getType(BookSpeciCarOrderDTO.class);
            Type<YcDd> ycddType = BeanMapper.getType(YcDd.class);
            YcDd ycDd = BeanMapper.map(param, paramType, ycddType);
            ycDd.setCgDdly(param.getCgDdly());
            ycDd.setDdbh(ddbh);
            ycDd.setChannelId(buyerBookCommonService.getChannelIdByShbh(param.getCgShbh()));
            ycDd.setXdsj(VeDate.getNow());//下单时间
            ycDd.setDdlx(UsecarProductTypeEnum.zc.getCode());//订单类型
            ycDd.setDdzt(UsecarOrderStatusEnum.YC1C.getCode());//订单状态
            ycDd.setSjkly(String.valueOf(orderNoService.getSjkly(param.getCgShbh())));//数据库路由
            ycDd.setZfZt(UsecarZfztEnum.WZF.getCode());//支付状态
            ycDd.setFkje(new BigDecimal(UseCarConstant.NUMZERO));//支付金额默认为0
            ycDd.setFkfs(UseCarConstant.NUMONE);//付款方式，默认值：1预存款抵扣
            ycDd.setPtkrgz(UseCarConstant.NUMTHREE);//控润规则，默认值：3建议价
            /**由于用车报表查询需要，专快车订单需增加站点ID和站点名称字段默认值**/
            ycDd.setJsfwzdid(UseCarConstant.ZC_ZDID);
            ycDd.setJsfwzdmc(UseCarConstant.ZC_ZDMC);
            String priceCacheId = param.getPriceCacheId();
            if (StringUtils.isNotBlank(priceCacheId)) {
                buyerBookSpeciCarCacheService.getPriceCaCheYc(priceCacheId, ycDd);
            }
            logger.info("专快车下单==>ycDd入参：" + ycDd);
            boolean flag = ycDdService.insertYcDd(ycDd);
            if (flag) {
                logger.info("订单号：" + ddbh + "-》下单成功");
                //快速对象拷贝
                Type<YcDd> ycDdType = BeanMapper.getType(YcDd.class);
                Type<BookSpeciCarOrderVO> bookSpeciCarOrderVOType = BeanMapper.getType(BookSpeciCarOrderVO.class);
                bookSpeciCarOrderVO = BeanMapper.map(ycDd, ycDdType, bookSpeciCarOrderVOType);
                bookSpeciCarOrderVO.setYdfs(param.getYdfs());
                bookSpeciCarOrderVO.setZfr(param.getCgShbh());//返回给支付使用
            }
        } catch (Exception e) {
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_10001, "下单失败\t\r" + e);
        }
        logger.info("专快车下单==>出参：" + bookSpeciCarOrderVO);
        return bookSpeciCarOrderVO;
    }

    /**
     * @param ddbh 订单编号
     * @return 是否成功
     * @title 创建专车供应商订单
     * @author houshuang
     * @date 2017年11月07日15:18:48
     */
    public boolean createOrderToGys(String ddbh) {
        logger.info("订单号：" + ddbh + ",进入专快车下单到供应商方法");
        if (StringUtils.isNotBlank(ddbh)) {
            YcDd ycdd = ycDdService.selectByIdNoCache(ddbh);
            if (null != ycdd) {
                if (StringUtils.isNotBlank(ycdd.getGyDdbh())) {
                    logger.info("订单号：" + ddbh + ",已存在供应订单编号，这里不做再次下单处理");
                    return true;//这种情况需要返回true，不然会被做拒单处理
                }
                if(cacheService.isHaveAutoTestCache(ddbh)){//如果是测试订单
                    ycdd.setGyDdbh(VeDate.getNo());
                    String ddzt = ycDdService.getCompleteDdzt(ycdd);
                    ycdd.setDdzt(ddzt);
//                    ycdd.setDdzt("1".equals(ycdd.getFfgz())&&"1".equals(ycdd.getClyy())?UsecarOrderStatusEnum.YC1H.getCode():UsecarOrderStatusEnum.YC1F.getCode());
                    return ycDdService.updateYcDd(ycdd);
                }
//                if(buyerBookSpeciCarCommonService.dd30MinutesReservedCar(ycdd)){
//                    logger.info("订单号：" + ddbh + ",为滴滴的30分钟以内预约用车，这里不做下单处理");
//                    return true;//这种情况需要返回true，不然会被做拒单处理
//                }
                return creatOrderToGysByYcdd(ycdd);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 下单到接口供应商（因为定时任务需要调用这块，所以提取出来）
     *
     * @param ycdd 用车订单对象
     * @return flag true 下单成功 false 下单失败
     */
    @Transactional
    public boolean creatOrderToGysByYcdd(YcDd ycdd) {
        logger.info("下单到接口供应商==>入参：" + ycdd.toString());
        logger.info("开始对专快车订单{}进行下单供应前用车风险检查....",ycdd.getDdbh());
        UsecarRiskCodeEnum riskCodeEnum = usecarRiskControlService.checkRisk(ycdd.getCgShbh(),ycdd.getCksj(),ycdd.getClyy(),ycdd.getYgje());
        logger.info("专快车订单{}下单供应前用车风险检查结果为{}",ycdd.getDdbh(),riskCodeEnum.getRiskCode()+"【"+riskCodeEnum.getRiskDesc()+"】");
        if(!StringUtils.equals(riskCodeEnum.getRiskCode(),UsecarRiskCodeEnum.RISK_00.getRiskCode())){
            logger.error("专快车订单{}下单存在风险，终止下单",ycdd.getDdbh());
            YcDd order = ycDdService.selectYcDd(ycdd.getDdbh());
            order.setGyJudyy(riskCodeEnum.getRiskDesc());
            ycDdService.updateYcDd(order);
            return false;
        }
        String bzcgs = getBzcgsFlag(ycdd);
        YcDdMain ycDdMain = ycDdMainService.selectById(ycdd.getpDdbh());
        boolean flag = true;
        //判断是自签还是接口供应商
        if (StringUtils.isNotBlank(ycdd.getGyShbh()) && UsecarGysApiEnum.checkIsApiGys(ycdd.getGyShbh())) {
            /**专快车入参城市ID转换**/
            String gysbh = ycdd.getGyShbh();
            if("CCZCBJ".equals(gysbh)){
                gysbh = "CCZC";
            }
            String cfcsid = buyerBookSpeciCarCommonService.getGysCsid(gysbh, ycdd.getCfdCsid(), UseCarConstant.SJDY_LX_CITY);
            String mdcsid = buyerBookSpeciCarCommonService.getGysCsid(gysbh, ycdd.getMddCsid(), UseCarConstant.SJDY_LX_CITY);
            LinkCreateSpecialOrderDTO linkorder = new LinkCreateSpecialOrderDTO();
            linkorder.setGyddbh(getGyddbh(ycdd));
            linkorder.setCxzBh(ycdd.getWbcxzbh());
            linkorder.setEndPlanStartTime(ycdd.getEndPlanStartTime());
            linkorder.setPaxNum(ycdd.getPaxNum());
            linkorder.setBearHighwayFeeType(ycdd.getBearHighwayFeeType());
            linkorder.setCxzMc(ycdd.getWbcxzmc());
            linkorder.setEstimatedAmount(ycdd.getCgJsje());
            linkorder.setCfCsid(cfcsid);
            linkorder.setCfdMc(ycdd.getCfd());
            linkorder.setCfCsmc(ycdd.getCfdCsmc());
            linkorder.setMddCsid(mdcsid);
            linkorder.setMddCsmc(ycdd.getMddCsmc());
            linkorder.setMddMc(ycdd.getMdd());
            linkorder.setCfdXxdz(ycdd.getCfdXxdz());
            linkorder.setMddXxdz(ycdd.getMddXxdz());
            linkorder.setCkXm(ycdd.getCkxm());
            String ckxm = linkorder.getCkXm();
            // 乘客姓名脱敏处理
            if (StringUtils.isNotBlank(ckxm) && ckxm.length() > 1) {
                StringBuilder sb = new StringBuilder(ckxm);
                for (int i = 1; i < sb.length(); i++) {
                    sb.setCharAt(i, '*');
                }
                ckxm = sb.toString();
                linkorder.setCkXm(ckxm);
            }
            linkorder.setCkSj(ycdd.getCksj());
            linkorder.setFrombzcps(bzcgs);
            if (ycdd.getGyJsje() != null) {
                linkorder.setOrdermoney(ycdd.getGyJsje().toString());
            }
            if (ycdd.getGyCbj() != null) {
                linkorder.setMarkprice(ycdd.getGyCbj().toString());
            }
            if (StringUtils.isNotBlank(ycdd.getYcsj()) && ycdd.getYcsj().length() > UseCarConstant.TEN) {
                linkorder.setYcsj(ycdd.getYcsj());
            }
            linkorder.setJjmslb(ycdd.getJjmslb());
            String jgmd5 = StringUtils.defaultIfBlank(ycdd.getJgmd5(),ycdd.getCarmodelid());
            String carmodelId = (String)cacheService.getCreateJgmd5Cache(ycdd.getDdbh());
            logger.info("carModelId:{}",carmodelId);
            carmodelId = StringUtils.defaultIfBlank(jgmd5,carmodelId);
            linkorder.setJgmd5(carmodelId);
            linkorder.setCfdX(ycdd.getCfdX());
            linkorder.setCfdY(ycdd.getCfdY());
            linkorder.setMddX(ycdd.getMddX());
            linkorder.setMddY(ycdd.getMddY());
            linkorder.setShid(ycdd.getGyShbh());
            linkorder.setShjc(ycdd.getGyShjc());
            linkorder.setCgshbh(ycdd.getCgShbh());
            linkorder.setCgshjc(ycdd.getCgShjc());
            linkorder.setDdlx(ycdd.getDdlx());
            linkorder.setDdbz(ycdd.getDdbz());
            linkorder.setCpsDdbh(ycdd.getDdbh());
            if(StringUtils.isBlank(ycdd.getLxrdh())) {
                linkorder.setContactName(ycdd.getCkxm());
                linkorder.setContactPhone(ycdd.getCksj());
            }else{
                linkorder.setContactName(ycdd.getLxr());
                linkorder.setContactPhone(ycdd.getLxrdh());
            }
            linkorder.setLoginPhone(linkorder.getContactPhone());
            linkorder.setMddbh(ycdd.getpDdbh());
            if (StringUtils.equals(ycdd.getGyShbh(), XDCX.getShbh()) || StringUtils.contains(ycdd.getGyShbh(), CCZC.getShbh()) || StringUtils.equals(ycdd.getGyShbh(), DDYC.getShbh())) {
                List<LinkChildOrderDTO> orders = getChildOrder(ycdd);
                linkorder.setOrders(orders);
            }
            logger.info("订单号：" + ycdd.getDdbh() + "--用车专快车产品下单接口入参--》" + linkorder.toString());
            setChannelType(linkorder,ycdd);
            RestResponse<LinkCreateSpecialOrderVO> response = iLinkSpecialCarServiceClient.createOrder(linkorder);
            logger.info("下单到接口供应商返回信息：\r\n" + JsonMapper.defaultMapper().toJson(response));
            if (response == null || response.getResult() == null) {
                logger.warn("供应商创建订单异常：" + response.getResult());
                return false;
            }
            ycSupplierInterfaceCountService.logRequest(ycdd.getGyShbh(), ORDER, response.getResult());
            LinkCreateSpecialOrderVO linkCreateOrderVO = response.getResult();
            logger.info("订单号：" + ycdd.getDdbh() + "--进入专快车供应商下单方法接口出参--》》》" + linkCreateOrderVO.toString());
            if (StringUtils.isNotBlank(linkCreateOrderVO.getDdzt())) {
                ycdd.setDdzt(linkCreateOrderVO.getDdzt());
            }else {
                ycdd.setDdzt(UsecarOrderStatusEnum.YC1F.getCode());
            }
            //先用后付，且未支付，订单状态设置为未支付，待派车
            if(StringUtils.equals(ycdd.getFfgz(),"1") && !StringUtils.equals(ycdd.getZfZt(),"1")){
                ycdd.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
            }
            String gyddbh = linkCreateOrderVO.getGyDdbh();
            YcDd existingOrder = ycDdService.selectBySupplierOrderNoBuyerNo(gyddbh, ycdd.getCgShbh());
            if (existingOrder != null) {
                logger.info("高德重复下单{},供应单号{}", existingOrder.getDdbh(), gyddbh);
                gyddbh = null;
            }
            if (StringUtils.isBlank(gyddbh)) {
                if (StringUtils.isNotEmpty(linkCreateOrderVO.getMsg()) && linkCreateOrderVO.getMsg().contains("余额不足")) {
                    ycXxMdCommonService.usecarExcessXxmdMqSend(UsecarXxidEnum.XX_CAR_0016, ycdd.getCgShbh(), ycdd);
                }
                YcDd order = ycDdService.selectYcDd(ycdd.getDdbh());
                order.setGyJudyy(linkCreateOrderVO.getMsg());
                ycDdService.updateYcDd(order);
                return false;
            } else {
                ycdd.setGyDdbh(gyddbh);
                ycdd.setGyDzdh(linkCreateOrderVO.getGyDzdh());
                ycdd.setGysMddbh(linkCreateOrderVO.getGysMddbh());
                //放入缓存，后续会用到
                String key = ycdd.getGyShbh() + "API" + ycdd.getGyDdbh();
                String value = ycdd.getDdbh();
//                veCacheManage.put("USECAR_DD", key, value, CACHE_M_5);
                UsecarGysDdbhCacheMap.setDdbh(veCacheManage,ycdd.getGyShbh(),ycdd.getGyDdbh(),ycdd.getDdbh());
                logger.info("下单到供应商成功后，缓存key:" + key + "，value:" + value + ",缓存时效:" + CACHE_M_5 + "秒");
                try{
                    flag = ycDdService.updateYcDd(ycdd);
                }catch (Exception e){
                    flag=false;
                    logger.error("下单到供应商成功后更新本地业务数据异常",e);
                    try {
                        LinkCancelSpecialOrderDTO creq=new LinkCancelSpecialOrderDTO();
                        creq.setSfqzqx(Boolean.TRUE.toString());
                        creq.setUserxm(ycdd.getCkxm());
                        creq.setCpsDdbh(ycdd.getDdbh());
                        creq.setDdbh(gyddbh);
                        creq.setShid(ycdd.getGyShbh());
                        creq.setDdlx(ycdd.getDdlx());
                        creq.setJjmslb(ycdd.getJjmslb());
                        creq.setCgQxyy("业务侧数据处理失败，申请退单。");
                        creq.setCgshbh(ycdd.getCgShbh());
                        creq.setCgshjc(ycdd.getCgShjc());
                        creq.setShjc(ycdd.getGyShjc());
                        creq.setContactPhone(ycdd.getLxrdh());
                        creq.setLoginPhone(ycdd.getLxrdh());
                        creq.setMddbh(ycdd.getpDdbh());
                        creq.setGysMddbh(ycdd.getGysMddbh());
                        RestResponse<LinkCancelSpecialOrderVO> cres = defaultCancelService.cancelOrder(creq,ycdd);
                        logger.info("下单到供应商后业务处理失败，强制取消供应商订单结果："+JsonMapper.nonEmptyMapper().toJson(cres));
                    }catch (Exception ex){
                        logger.error("下单到供应商后业务处理失败，强制取消供应商订单异常",ex);
                    }
                }
            }
            if (flag && StringUtils.isNotEmpty(ycdd.getDdbz())) {
                SendMessageService.sendMessage(ycdd.getDdbh(),  USE_CAR_CREATE_ORDER_TITLE);
            }
            logger.info("采购订单号：" + ycdd.getDdbh() + "下单到供应商订单编号为：" + gyddbh + ",修改本地状态：" + flag);
        }
        logger.info("采购订单号：" + ycdd.getDdbh() + "--订单状态为：--" + ycdd.getDdzt());
        //如果接口供应商是自动应单，那么要把这个应单信息推送给采购ASMS
        if (StringUtils.equals(UsecarOrderStatusEnum.YC2F.getCode(), ycdd.getDdzt())) {
            try {
                logger.info("当前供应商自动应单，即将推送应单状态到采购ASMS系统。");
                ConfirmOrderStatusNotifyToBuyerDTO confirmOrderStatusNotifyDTO = new ConfirmOrderStatusNotifyToBuyerDTO();
                confirmOrderStatusNotifyDTO.setCgDdbh(ycdd.getCgDdbh());
                confirmOrderStatusNotifyDTO.setCgShbh(ycdd.getCgShbh());
                confirmOrderStatusNotifyDTO.setCpsDdbh(ycdd.getDdbh());
                confirmOrderStatusNotifyDTO.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycdd.getDdzt()));
                buyerNoticeService.confirmOrderStatusNotify(confirmOrderStatusNotifyDTO);
            } catch (Exception e) {
                logger.error("推送供应商确认结果到采购商ASMS平台异常", e);
            }
        }
        return flag;
    }

    private String getGyddbh(YcDd ycdd) {
        if (!StringUtils.equals(ycdd.getGyShbh(), DIBO.getShbh()))
            return null;
        OrderEsVO orderEsVO = ycDdEsV2Service.searchInProgressOrderByPhone(cryptService.encrypt(ycdd.getCksj()),DIBO.getShbh());
        if (orderEsVO != null) {
            logger.info("进行中订单不为空{}",ycdd.getCksj());
            return orderEsVO.getGyDdbh();
        }
        return null;
    }

    /**
     *  判断订单是否来自标准采购商
     * @param ycdd
     * @return
     */
    private String getBzcgsFlag(YcDd ycdd) {
        List<YcDdCk> ckList = ycDdCkService.selectPassengerListByOrderNo(ycdd.getDdbh());
        if(CollectionUtil.isNotEmpty(ckList)){
            for (YcDdCk ycDdCk : ckList) {
                return StringUtils.isNotBlank(ycDdCk.getBzcgs())?ycDdCk.getBzcgs():null;
            }
        }
        return null;
    }

    /**
     * 子单信息处理
     * @param ycdd 订单
     * @return 获取账单信息
     */
    private List<LinkChildOrderDTO> getChildOrder(YcDd ycdd) {
        String carProductId = (String)cacheService.getCreateCpidCache(ycdd.getDdbh());
        String[] cpids = StringUtils.split(StringUtils.defaultIfBlank(ycdd.getCpid(),carProductId),",");
        if (cpids == null || cpids.length == 1) {
            return null;
        }
        List<LinkChildOrderDTO> childOrders = new ArrayList<>(cpids.length);
        for (String cpid : cpids) {
            BookSpeciCalCachePrice calCachePrice = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(cpid);
            if (calCachePrice == null){
                continue;
            }
            LinkChildOrderDTO dto = new LinkChildOrderDTO();
            dto.setCarType(calCachePrice.getWbcxzbh());
            dto.setEstimateId(calCachePrice.getJgmd5());
            dto.setEstimatePrice(calCachePrice.getGyJsje());
            dto.setFixedPrice(ycdd.getSfykj());
            childOrders.add(dto);
        }
        return childOrders;
    }
}