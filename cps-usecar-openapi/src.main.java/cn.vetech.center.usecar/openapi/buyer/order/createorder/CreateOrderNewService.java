package cn.vetech.center.usecar.openapi.buyer.order.createorder;


import cn.vetech.center.car.service.GyShfzService;
import cn.vetech.center.car.service.GySjdyService;
import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.customer.api.vo.ShYhbVO;
import cn.vetech.center.system.openapi.*;
import cn.vetech.center.system.openapi.annotation.OpenApiOperation;
import cn.vetech.center.usecar.apiclient.customer.IShYhbServiceClient;
import cn.vetech.center.usecar.book.buyer.dto.BookCreateOrderDTO;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookCommonService;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookService;
import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarCommonService;
import cn.vetech.center.usecar.book.buyer.vo.UseCarCreateOrderVO;
import cn.vetech.center.usecar.book.buyer.vo.UseCarPorductModelVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.*;
import cn.vetech.center.usecar.common.util.UseCarCommonUtil;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdCk;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.GetPickUpFlightParamService;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.bean.PickUpFlightBean;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.request.PickUpFlightRequest;
import cn.vetech.center.usecar.openapi.buyer.book.pickupcar.response.PickUpFlightResponse;
import cn.vetech.center.usecar.service.UsecarCacheService;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdCkService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.setting.profit.service.CpsaYcDdProfitRuleJlService;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vetech.core.api.RestResponse;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.vetech.center.usecar.common.UseCarConstant.CPSC_PC;
import static cn.vetech.center.usecar.common.UseCarConstant.CPS_A;

/**
 * 预订下单 CPSA 用到
 * 应用场景：采购下单时调用 ,将订单入库到CPS订单表
 *
 * @author chenyong
 * @since 2017-11-09
 */
@OpenApiOperation(value = "car_createNewOrder", title = "采购预订下单")
public class CreateOrderNewService implements IOpenApiService<CreateOrderRequest, CreateOrderResponse> {
    /**
     * 日志记录类
     */
    private final Logger logger = LoggerFactory.getLogger(CreateOrderNewService.class);
    /**
     * 用车生产订单编号Service
     */
    @Autowired
    private UsecarOrderNoService orderNoService;
    /**
     * 分布式锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;
    /**
     * 下单公共服务
     */
    @Autowired
    private CreateOrderCommonService createOrderCommonService;
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
     * 商户分组
     */
    @Autowired
    private GyShfzService gyShfzService;
    /**
     * 供应对接对应服务
     */
    @Autowired
    private GySjdyService gySjdyService;
    /**
     * 一键三单服务
     */
    @Autowired
    private ThreeOrderService threeOrderService;
    /**
     * 用车订单乘客服务
     */
    @Autowired
    private YcDdCkService ycDdCkService;
    /**
     * 商户用户信息
     */
    @Autowired
    private IShYhbServiceClient shYhbServiceClient;
    /**
     * 用车订单对象成本对象
     */
    @Autowired
    private GetPickUpFlightParamService getPickUpFlightParamService;

    @Autowired
    private CpsaYcDdProfitRuleJlService cpsaYcDdProfitRuleJlService;
    /**
     * 下单公共服务
     */
    @Autowired
    private BuyerBookCommonService buyerBookCommonService;

    @Override
    public CreateOrderResponse execute(CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO,
                                       OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) throws OpenApiException {
        openApiLog.setYwdh(request.getCgDdbh());
        logger.info("临时记录下，就是为了看到底是不是进来了(对比用)....>>" + "shdto=" + JsonMapper.nonEmptyMapper().toJson(openApiShShbDTO) + "\r\nshyhdto=" + JsonMapper.nonEmptyMapper().toJson(openApiShYhbDTO));
        logger.info("下单业务参数：" + request.toString());
        CreateOrderResponse response = new CreateOrderResponse();
        if (request != null) {
            if(StringUtils.isNotBlank(openApiShShbDTO.getShbh())){
                RestResponse<List<ShYhbVO>> shRes = null;
                try {
                    shRes = shYhbServiceClient.getListByShzh(openApiShShbDTO.getShbh());
                    if(shRes != null && CollectionUtils.isNotEmpty(shRes.getResult())){
                        openApiShYhbDTO.setYhbh(shRes.getResult().get(0).getYhbh());
                    }
                }catch(Exception ex){
                    logger.error("获取商户信息{}失败",openApiShShbDTO.getShbh(),ex);
                }
            }
            //检查乘客是否在用车黑名单里
            boolean checkFlag = commService.checkYcHmd(request.getCkxm(), request.getCksj());
            if (!checkFlag) {
                logger.info("{}在黑名单中，不允许下单", request.getCkxm());
                response.setStatus(UsecarCodeEnum.FAIL.getCode());
                response.setErrorCode(UsecarOrderCode.UCAR_10001.getCode());
                response.setErrorMessage(UsecarOrderCode.UCAR_10001.getMessage());
                return response;
            }
            if (StringUtils.isNotBlank(request.getDdlx()) && "10000501".equals(request.getDdlx())) {
                openApiLog.add("**进入专快车下单到CPS模块**");
                response = specialCarSubmitOrderToCps(request, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            } else {
                openApiLog.add("**进入接送车下单到CPS模块**");
                response = shuttleCarSubmitOrderToCps(request, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            }
        } else {
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
        }
        openApiLog.setDdbh(response.getDdbh());
        return response;
    }

    /**
     * 专快车下单到CPS
     *
     * @param request         订单对象
     * @param openApiShShbDTO 商户对象
     * @param openApiShYhbDTO 用户对象
     * @param openApiLog      日志对象
     * @return 返回下单成功对象
     */
    private CreateOrderResponse specialCarSubmitOrderToCps(CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) {
        if (StringUtils.isBlank(request.getPricecacheid())) {
            return null;
        }
        String[] cacheIdArr = StringUtils.split(request.getPricecacheid(), ",");
        Map<String, List<String>> groupMap = new HashMap<>();
        for (String cacheId : cacheIdArr) {
            BookSpeciCalCachePrice carcache = null;
            try {
                carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(cacheId);
            } catch (Exception e) {
                logger.error("获取缓存失败");
            }
            if (carcache != null) {
                String supplierNo = carcache.getGyShbh();
                if (StringUtils.equals(carcache.getSupplierBookType(), "1")) {
                    if (groupMap.containsKey(supplierNo)) {
                        groupMap.get(supplierNo).add(cacheId);
                    } else {
                        List<String> cacheIdList = new ArrayList<>();
                        cacheIdList.add(cacheId);
                        groupMap.put(supplierNo, cacheIdList);
                    }
                } else {
                    List<String> cacheIdList = new ArrayList<>();
                    cacheIdList.add(cacheId);
                    groupMap.put(supplierNo + "-" + cacheId, cacheIdList);
                }
            }
        }
        if (groupMap.isEmpty()) {
            return null;
        }
        boolean isBatch = false;
        if(groupMap.size()>1){
            isBatch = true;
            request.setSfxyhf("1");
        }
        boolean mergeBook = false;
        CreateOrderResponse response = new CreateOrderResponse();
        for (Map.Entry<String, List<String>> entry : groupMap.entrySet()) {
            String cacheId = StringUtils.join(entry.getValue(), ",");
            if(isBatch && StringUtils.contains(cacheId,",")){
                mergeBook = true;
                request.setSfxyhf("1");
            }
            request.setPricecacheid(cacheId);
            try {
                if (StringUtils.isNotBlank(request.getPricecacheid())) {
                    long starttime = System.currentTimeMillis();
                    YcDd ycDd = buildCarOrder(request, request.getPricecacheid(), openApiShShbDTO, openApiShYhbDTO, openApiLog);
                    if(ycDd == null){
                        logger.error("进入到ASMS专快车下单到CPS接口调用失败，无法生成订单");
                        continue;
                    }
                    if ("1".equals(request.getSfyjsd())) {
                        response.setQxsx(gySjdyService.selectCancelLimitByGysbhAndCityId(ycDd.getCfdCsid(), ycDd.getGyShbh()));
                    }
                    if (!StringUtils.equals(request.getNewOrder(), "1") || mergeBook) {
                        String pDdbh = threeOrderService.createMainOrder(ycDd, UseCarTypeEnum.THREE_ORDERS);
                        ycDd.setpDdbh(pDdbh);
                        response.setGysMddbh(pDdbh);
                    }
                    logger.info("进入到ASMS专快车下单到CPS接口==>YC_DD对象：" + ycDd);
                    boolean flag = ycDdService.insertYcDd(ycDd);
                    long endtime = System.currentTimeMillis();
                    if (flag) {
                        response.setDdbh(ycDd.getDdbh());
                        response.setDdzt(ycDd.getDdzt());
                        response.setTime(new Long(endtime - starttime));
                        List<YcDdCk> passengerList = request.getPassengerList();
                        if (CollectionUtils.isNotEmpty(passengerList)) {
                            for (YcDdCk passenger : passengerList) {
                                passenger.setPassengerId(IdGenerator.getHexId());
                                passenger.setOrderNo(ycDd.getDdbh());
                                ycDdCkService.savePassenger(passenger);
                            }
                        }
                        // 缓存是否自动确认订单标识
                        jscBookService.cacheConfirmOrder(ycDd, request.getConfirmOrder());
                        if (null != ycDd) {
                            commService.saveOrUpdateClk(ycDd);
                        }
                        // 下单成功后保存订单的控润记录
                        if (StringUtils.contains(request.getPricecacheid(), ",")) {
                            cacheId = StringUtils.split(request.getPricecacheid(), ",")[0];
                        }
                        BookSpeciCalCachePrice carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(cacheId);
                        cpsaYcDdProfitRuleJlService.upsertMatchRecordWhileCreatedOrder(carcache!=null?carcache.getKrinfo():null,ycDd.getDdbh());
                    }
                    if ("1".equals(ycDd.getFfgz()) || isBatch) {
                        createOrderToLink(ycDd);
                    }
                    if (flag) {
                        ycDd = ycDdService.selectYcDd(ycDd.getDdbh());
                        if (StringUtils.isNotBlank(request.getAutotest()) && UseCarConstant.NUMONE.equals(request.getAutotest())) {
                            //如是测试订单，将订单数据缓存
                            usecarCacheService.putAutoTestCache(ycDd.getDdbh(), ycDd.getDdbh());
                        }
                    } else {
                        response.setTime(new Long(endtime - starttime));
                        response.setStatus(UsecarCodeEnum.FAIL.getCode());
                    }
                }
            } catch (Exception e) {
                logger.error("订单编号：" + request.getCgDdbh() + "专快车下单到CPS异常", e);
                response.setStatus(UsecarCodeEnum.FAIL.getCode());
            }
        }
        return response;
    }

    private YcDd buildCarOrder(CreateOrderRequest request, String cacheId, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) {
        BookSpeciCalCachePrice carcache = null;
        try {
            if (StringUtils.contains(request.getPricecacheid(), ",")) {
                cacheId = StringUtils.split(request.getPricecacheid(), ",")[0];
            }
            carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(cacheId);
        } catch (Exception e) {
            logger.error("***获取用车价格缓存失败！",e);
            return null;
        }
        if (carcache == null) {
            logger.error("获取用车价格缓存失败！");
            return null;
        }
        if (StringUtils.isBlank(carcache.getCfd())) {
            throw new RuntimeException("出发地POI不能为空");
        }
        logger.info("进入到ASMS专快车下单到CPS接口==>获取价格缓存对象：" + carcache);

        YcDd ycDd = getYcDd(request, carcache, openApiShShbDTO, openApiShYhbDTO);
        ycDd.setCfdXxdz(StringUtils.defaultIfBlank(carcache.getCfdXxdz(), carcache.getCfd()));
        final int maxLength = 100;
        if (StringUtils.isNotBlank(ycDd.getCfdXxdz()) && ycDd.getCfdXxdz().length() > maxLength) {
            String cfdXxdz = ycDd.getCfdXxdz();
            cfdXxdz = cfdXxdz.substring(0, maxLength);
            ycDd.setCfdXxdz(cfdXxdz);
        }
        if (StringUtils.isNotBlank(ycDd.getMddXxdz()) && ycDd.getMddXxdz().length() > maxLength) {
            String mddXxdz = ycDd.getMddXxdz();
            mddXxdz = mddXxdz.substring(0, maxLength);
            ycDd.setMddXxdz(mddXxdz);
        }
        ycDd.setFwsbh(request.getFwsbh());
        ycDd.setFwsmc(request.getFwsmc());
        ycDd.setClyy(request.getClyy());
        if (StringUtils.isNotBlank(request.getSfxyhf())) {
            if (StringUtils.equals(request.getSfxyhf(), "1")) {
                ycDd.setFfgz(request.getSfxyhf());
                ycDd.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
            }
        } else {
            //按分组取是否是先用后付
            Integer isUseBefore = gyShfzService.getShfzCount(ycDd.getCgShbh());
            if (isUseBefore > 0 && "1".equals(ycDd.getClyy())) {
                ycDd.setFfgz("1");
                ycDd.setDdzt(UsecarOrderStatusEnum.YC1H.getCode());
            }
        }
        if ("0".equals(ycDd.getSfykj())
                && StringUtils.isNotEmpty(request.getDdly())
                && request.getDdly().indexOf(UseCarCommonUtil.DDLY) != -1) {
            //一口价 且订单来源为直销订单
            return null;
        }
        commService.yesOrNoCloudOrder(ycDd);
        return ycDd;
    }

    /**
     * 下单到供应
     *
     * @param ycDd     用车订单
     */
    private void createOrderToLink(YcDd ycDd) {
        InterProcessMutex lock = null;
        try {
            String sourceOrderId = ycDd.getCgShbh()+ycDd.getCgDdbh().substring(UseCarConstant.ZERO,ycDd.getCgDdbh().length()-UseCarConstant.TWO);
            lock = zookeeperLockService.tryLock(LockType.LOCK, UseCarConstant.USECAR_AMAP_LOCK, sourceOrderId, UseCarConstant.USECAR_AMAP_LOCK_WAIT_TIME);
            usecarCacheService.cacheAmapOrder(ycDd);
        } catch (Exception e) {
            logger.error("缓存高德订单信息异常", e);
        } finally {
            if (null != lock) {
                zookeeperLockService.unlock(lock);
            }
        }
        createOrderCommonService.createBeforePay(ycDd);
    }


    /**
     * 接送车下单到CPS接口
     *
     * @param request         订单对象
     * @param openApiShShbDTO 商户对象
     * @param openApiShYhbDTO 用户对象
     * @param openApiLog      日志对象
     * @return 返回下单成功对象
     */
    private CreateOrderResponse shuttleCarSubmitOrderToCps(CreateOrderRequest request, OpenApiShShbDTO openApiShShbDTO,
                                                           OpenApiShYhbDTO openApiShYhbDTO, OpenApiLog openApiLog) {
        CreateOrderResponse response = new CreateOrderResponse();
        BookCreateOrderDTO param = new BookCreateOrderDTO();
        param.setConfirmOrder(request.getConfirmOrder());
        param.setFlightDateTime(request.getFlightDateTime());
        param.setCgShbh(openApiShShbDTO.getShbh());
        param.setCgShjc(openApiShShbDTO.getJc());
        param.setCjYhbh(openApiShYhbDTO.getYhbh());
        param.setCksj(request.getCksj());
        param.setCkxm(request.getCkxm());
        param.setLxr(request.getLxr());
        //param.setLxryx(request.getLxryx());
        param.setLxrsj(request.getLxrdh());
        param.setPriceCaCheId(request.getPricecacheid());
        param.setHbcch(request.getHbcch());
        param.setCgDdbh(request.getCgDdbh());
        param.setTsxq(request.getTsxq());
        param.setCgDdly(request.getDdly());
        param.setFwbzbz(request.getFwbzbz());
        param.setFwbzjb(request.getFwbzjb());
        param.setClyy(request.getClyy());
        param.setFwsbh(request.getFwsbh());
        param.setFwsmc(request.getFwsmc());
        param.setSfxyhf(request.getSfxyhf());
        String cacheId = StringUtils.contains(param.getPriceCaCheId(),",") ? StringUtils.split(param.getPriceCaCheId(),",")[0] : param.getPriceCaCheId();
        UseCarPorductModelVO priceCaCheBean= usecarCacheService.getPriceCaCheYc(cacheId);
        if(request != null && StringUtils.isNotBlank(priceCaCheBean.getHbcch()) && StringUtils.isNotBlank(request.getFlightDateTime())){
            String flightDate = request.getFlightDateTime().substring(0, 10);
            PickUpFlightRequest req = new PickUpFlightRequest();
            req.setFlightNo(priceCaCheBean.getHbcch());
            req.setFlightDate(flightDate);
            PickUpFlightResponse execute = getPickUpFlightParamService.execute(req, openApiShShbDTO, openApiShYhbDTO, openApiLog);
            if (null != execute && CollectionUtils.isNotEmpty(execute.getFlightBeanList())) {
                // 取第一个
                PickUpFlightBean pickUpFlightBean = getPickUpFlightParamService.getFlightInfo(execute,priceCaCheBean.getDdlx(),priceCaCheBean.getYcsj(),priceCaCheBean.getJsfwzdid());
                // 存入缓存
                String key = priceCaCheBean.getHbcch()+":" +flightDate;
                usecarCacheService.putFlightInfoCache(key,pickUpFlightBean);
            }
        }
        try {
            UseCarCreateOrderVO useCarCreateOrderVO = jscBookService.buyerCreateOrderNew(param);
            if (null != useCarCreateOrderVO) {
                response.setDdbh(useCarCreateOrderVO.getDdbh());
                response.setDdzt(useCarCreateOrderVO.getDdzt().getCode());
                if (StringUtils.isNotBlank(request.getAutotest()) && UseCarConstant.NUMONE.equals(request.getAutotest())) {
                    //如是测试订单，将订单数据缓存 Autotest 1 为测试订单
                    usecarCacheService.putAutoTestCache(useCarCreateOrderVO.getDdbh(), useCarCreateOrderVO.getDdbh());
                }
            }
        } catch (Exception e) {
            logger.error("订单编号：" + request.getCgDdbh() + "接送车下单到CPS异常", e);
            openApiLog.add("订单编号：" + request.getCgDdbh() + "接送车下单到CPS异常\r\n" + e);
            response.setStatus(UsecarCodeEnum.FAIL.getCode());
        }
        return response;
    }

    /**
     * 获取用车订单对象
     *
     * @param request         请求对象
     * @param carcache        缓存对象
     * @param openApiShShbDTO 商户信息
     * @param openApiShYhbDTO 用户信息
     * @return 用车订单对象
     */
    private YcDd getYcDd(CreateOrderRequest request, BookSpeciCalCachePrice carcache, OpenApiShShbDTO openApiShShbDTO, OpenApiShYhbDTO openApiShYhbDTO) {
        String ddbh = orderNoService.getNormalOrderNo(openApiShShbDTO.getShbh());
        YcDd ycDd = new YcDd();
        ycDd.setLybjdh(request.getLybjdh());
        ycDd.setLybjdlx(request.getLybjdlx());
        ycDd.setJsfs(request.getJsfs());
        ycDd.setDdbh(ddbh);
        ycDd.setChannelId(buyerBookCommonService.getChannelIdByShbh(openApiShShbDTO.getShbh()));
        ycDd.setCpbz(carcache.getCpms());
        ycDd.setDdzt(UsecarOrderStatusEnum.YC1H.getOrderStatusCode());
        ycDd.setXdsj(VeDate.getNow());
        ycDd.setDdlx(request.getDdlx());
        ycDd.setBookerId(request.getBookerId());
        ycDd.setBookerNo(request.getBookerNo());
        ycDd.setBookerName(request.getBookerName());
        if (StringUtils.isBlank(ycDd.getBookerNo()) && CollectionUtils.isNotEmpty(request.getPassengerList())) {
            for (YcDdCk passenger : request.getPassengerList()) {
                if (StringUtils.equals(passenger.getPassengerEmpId(), ycDd.getBookerId())) {
                    ycDd.setBookerName(passenger.getPassengerName());
                    ycDd.setBookerNo(passenger.getPassengerEmpNo());
                }
            }
        }
        ycDd.setFullDeptName(request.getFullDeptName());
        ycDd.setDdlyyz(request.getDdlyyz());
        ycDd.setProjectId(request.getProjectId());
        ycDd.setProjectName(request.getProjectName());
        ycDd.setProjectNo(request.getProjectNo());
        ycDd.setSettleDeptName(request.getSettleDeptName());
        ycDd.setSettleDeptNo(request.getSettleDeptNo());
        ycDd.setSettleDeptId(request.getSettleDeptId());
        ycDd.setBookerDeptId(request.getBookerDeptId());
        ycDd.setBookerDeptName(request.getBookerDeptName());
        //订单状态
        ycDd.setDdzt(UsecarOrderStatusEnum.YC1C.getCode());
        //数据库路由
        ycDd.setSjkly(String.valueOf(orderNoService.getSjkly(openApiShShbDTO.getShbh())));
        //支付状态
        ycDd.setZfZt(UsecarZfztEnum.WZF.getCode());
        //付款方式，默认值：1预存款抵扣
        ycDd.setFkfs("1");
        //控润规则，默认值：3建议价
        ycDd.setPtkrgz("3");
        //用车时间
        ycDd.setYcsj(createOrderCommonService.getYcsj(carcache.getYcsj()));
        //乘客姓名
        ycDd.setCkxm(request.getCkxm());
        //乘客手机
        String cksj = request.getCksj();
        if (StringUtils.isNotBlank(cksj)) {
            cksj = cksj.replaceAll(" ", "");
        }
        ycDd.setCksj(cksj);
        ycDd.setCfdCsid(carcache.getCfdCsid());
        ycDd.setCfdCsmc(carcache.getCfdCsmc());
        if (StringUtils.isBlank(ycDd.getCfdCsmc())) {
            ycDd.setCfdCsmc(commService.getCsmcByBh(ycDd.getCfdCsid()));
        }
        ycDd.setCfd(carcache.getCfd());
        ycDd.setCfdXxdz(StringUtils.defaultIfBlank(carcache.getCfdXxdz(), carcache.getCfd()));
        ycDd.setMddCsid(carcache.getMmdCsid());
        ycDd.setMddCsmc(carcache.getMddCsmc());
        if (StringUtils.isBlank(ycDd.getMddCsmc())) {
            ycDd.setMddCsmc(commService.getCsmcByBh(ycDd.getMddCsid()));
        }
        ycDd.setMdd(carcache.getMdd());
        ycDd.setMddXxdz(StringUtils.defaultIfBlank(carcache.getMddXxdz(), carcache.getMdd()));
        //车型组编号
        ycDd.setCxzbh(carcache.getCxzbh());
        //车型组名称
        ycDd.setCxzmc(carcache.getCxzmc());
        //订单备注
        ycDd.setDdbz(request.getDdbz());
        //服务内容
        ycDd.setFwnr(carcache.getFwnr());
        //服务备注
        ycDd.setFwbz(carcache.getFwbz());
        ycDd.setCph(request.getCph());
        ycDd.setCsys(request.getCsys());
        ycDd.setCxmc(request.getCxmc());
        ycDd.setSjxm(request.getSjxm());
        ycDd.setSjdh(request.getSjdh());
        ycDd.setSjxb(request.getSjxb());
        if (request.getBdlc() != null) {
            ycDd.setBdlc(carcache.getBdlc());
        }
        if (request.getBdsc() != null) {
            ycDd.setBdsc(carcache.getBdsc());
        }
        ycDd.setCgYhbh(openApiShYhbDTO.getYhbh());
        ycDd.setCgShbh(openApiShShbDTO.getShbh());
        ycDd.setCgShjc(openApiShShbDTO.getJc());
        ycDd.setCgDdbh(request.getCgDdbh());
        ycDd.setQxgs(carcache.getQxgs());
        String qxgz = buyerBookSpeciCarCommonService.qxgzCheckStyle(carcache);
        ycDd.setQxgz(qxgz);
        //供应商户编号
        ycDd.setGyShbh(carcache.getGysbh());
        ycDd.setActualSupplierNo(carcache.getActualSupplierNo());
        //供应商户名称
        ycDd.setGyShjc(UsecarGysApiEnum.getShmc(carcache.getGysbh()));
        ycDd.setWbgysbh(carcache.getWbgysbh());
        ycDd.setWbgysmc(carcache.getWbgysmc());
        if (carcache.getPrice() != null) {
            //缓存中取价格信息
            //供应结算金额
            ycDd.setGyJsje(carcache.getGyJsje());
            //采购结算金额
            ycDd.setCgJsje(carcache.getJsj());
            //供应成本价
            ycDd.setGyCbj(carcache.getPrice());
            //预估金额
            ycDd.setYgje(carcache.getJsj());
            //初始预估金额
            ycDd.setCsygje(carcache.getPrice());
            ycDd.setBuyerEstimatedPrice(carcache.getJsj());
            //建议销售金额
            ycDd.setJyxsje(carcache.getPrice());
            if (UsecarGysApiEnum.checkIsNoAbsPriceSh(carcache.getGysbh(), UsecarProductTypeEnum.zc.getCode())) {
                //非一口价
                ycDd.setSfykj(UsecarSfykjEnum.NO.getCode());
                ycDd.setYfje(carcache.getJsj());
            } else {
                //一口价
                ycDd.setSfykj(UsecarSfykjEnum.YES.getCode());
                ycDd.setYfje(carcache.getJsj());
            }
        }
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
        //联系人
        ycDd.setLxr(request.getLxr());
        ycDd.setLxrdh(request.getLxrdh());
        createOrderCommonService.getJwd(ycDd, carcache);
        ycDd.setFkje(BigDecimal.ZERO);
        String jgmd5 = getJgmd5(carcache, request);
        if(!StringUtils.contains(request.getPricecacheid(),",")) {
            ycDd.setCpid(request.getPricecacheid());
            //价格md5
            ycDd.setJgmd5(jgmd5);
        }else{
            usecarCacheService.putCreateCpidCache(ycDd.getDdbh(),request.getPricecacheid());
            usecarCacheService.putCreateJgmd5Cache(ycDd.getDdbh(),jgmd5);
        }
        //计价模式类别
        ycDd.setJjmslb(carcache.getJjmslb());
        //外部车型组编号
        ycDd.setWbcxzbh(carcache.getWbcxzbh());
        //外部车型组名称
        ycDd.setWbcxzmc(carcache.getWbcxzmc());
        //平台控润规则
        ycDd.setPtkrgz(carcache.getPtkrgz());
        ycDd.setJsfwzdid(UseCarConstant.ZC_ZDID);
        ycDd.setJsfwzdmc(UseCarConstant.ZC_ZDMC);
        if (StringUtils.isNotBlank(request.getMemberId())) {
            ycDd.setCgDdly(CPSC_PC);
            ycDd.setMemberId(request.getMemberId());
        } else {
            ycDd.setCgDdly(CPS_A);
        }
        ycDd.setFwbzjb(request.getFwbzjb());
        ycDd.setFwbzbz(request.getFwbzbz());
        ycDd.setTsxq(request.getTsxq());
        ycDd.setZbddbh(request.getZbddbh());
        ycDd.setDdqdly( DdqdlyEnum.getEnumByQdly(request.getDdly()).getCode());
        return ycDd;
    }

    /**
     * 获取实际的价格MD5信息
     *
     * @param carcache 缓存信息
     * @param request  价格MD5
     * @return
     */
    private String getJgmd5(BookSpeciCalCachePrice carcache, CreateOrderRequest request) {
        if (!StringUtils.contains(request.getPricecacheid(), ",")) {
            return carcache.getJgmd5();
        }
        String[] cacheArr = StringUtils.split(request.getPricecacheid(), ",");
        List<String> cacheList = new ArrayList<>();
        for (String cacheId : cacheArr) {
            BookSpeciCalCachePrice cache = null;
            try {
                cache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(cacheId);
            } catch (Exception e) {
                logger.error("获取产品{}缓存信息失败", cacheId, e);
            }
            if (cache == null) {
                continue;
            }
            cacheList.add(cache.getJgmd5());
        }
        if (CollectionUtils.isEmpty(cacheList)) {
            return carcache.getJgmd5();
        }
        return StringUtils.join(cacheList, ",");
    }



}