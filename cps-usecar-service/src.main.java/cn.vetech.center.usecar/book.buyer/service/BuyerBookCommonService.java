package cn.vetech.center.usecar.book.buyer.service;

import cn.vetech.center.base.api.vo.CpsClassVO;
import cn.vetech.center.base.api.vo.VeJksqVO;
import cn.vetech.center.car.entity.GyQxgz;
import cn.vetech.center.car.entity.GySjdy;
import cn.vetech.center.car.service.GySjdyService;
import cn.vetech.center.cdsbase.api.service.IVeCityService;
import cn.vetech.center.cdsbase.api.service.IYcCxzService;
import cn.vetech.center.cdsbase.api.vo.TrainZdVO;
import cn.vetech.center.cdsbase.api.vo.VeCityVO;
import cn.vetech.center.cdsbase.api.vo.YcCppVO;
import cn.vetech.center.cdsbase.api.vo.YcCxzVO;
import cn.vetech.center.cdsbase.entity.city.BCity;
import cn.vetech.center.common.ApplicationName;
import cn.vetech.center.customer.api.dto.ShShclkSaveOrUpdateDTO;
import cn.vetech.center.customer.api.dto.ShShclkSaveOrUpdateListDTO;
import cn.vetech.center.customer.api.vo.ShShbVO;
import cn.vetech.center.usecar.apiclient.base.ICpsClassServiceClient;
import cn.vetech.center.usecar.apiclient.base.IVeJksqServiceClient;
import cn.vetech.center.usecar.apiclient.cds.IBCityServiceClient;
import cn.vetech.center.usecar.apiclient.cds.ITrainZdServiceClient;
import cn.vetech.center.usecar.apiclient.cds.IVeCityServiceClient;
import cn.vetech.center.usecar.apiclient.customer.IShShbServiceClient;
import cn.vetech.center.usecar.apiclient.customer.IShShclkServiceClient;
import cn.vetech.center.usecar.book.buyer.dto.BookSearchDTO;
import cn.vetech.center.usecar.book.buyer.vo.UseCarCppVO;
import cn.vetech.center.usecar.book.buyer.vo.UseCarPorductModelVO;
import cn.vetech.center.usecar.cache.CarBaseDataCacheService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.UsecarConfig;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdCk;
import cn.vetech.center.usecar.entity.usecar.YcHmd;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.service.order.YcDdCkService;
import cn.vetech.center.usecar.service.usecar.YcHmdService;
import cn.vetech.center.usecar.setting.buyerfilter.dto.BuyerFilterBookDTO;
import cn.vetech.center.usecar.setting.buyerfilter.service.BuyerFilterSetService;
import cn.vetech.center.usecar.setting.citysetting.service.CityLevelsGroupService;
import cn.vetech.center.usecar.setting.profit.dto.CpsaUseCarProfitCacheDTO;
import cn.vetech.center.usecar.setting.profit.dto.KrjeTdjeHolder;
import cn.vetech.center.usecar.setting.profit.dto.MemberDiscountInfo;
import cn.vetech.center.usecar.setting.profit.service.ChannelMemberDiscountService;
import cn.vetech.center.usecar.setting.profit.service.CpsaProfitCacheService;
import cn.vetech.center.usecar.setting.profit.service.CpsaProfitFilterUtil;
import cn.vetech.center.usecar.setting.profit.service.CpsaUseCarProfitCacheService;
import cn.vetech.center.usecar.setting.profit.vo.CpsaProfitCacheVO;
import cn.vetech.center.usecar.setting.profit.vo.CpsaUseCarProfitCacheVO;
import cn.vetech.center.usecar.setting.sellervaluation.dto.SellerValuationDTO;
import cn.vetech.center.usecar.setting.sellervaluation.service.SellerValuationService;
import cn.vetech.center.usecar.setting.sellervaluation.vo.SellerValuationVO;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.cache.annotation.Cache;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.collection.VeCollectionUtils;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.number.NumberUtil;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 查询预订通用信息服务类
 * 通过API 接口获取CDS CUSTOMER 模块的信息
 *
 * @author chenyong
 * Created by vetech on 2017/10/27.
 */
@Service
public class BuyerBookCommonService {
    /**
     * 日志记录类
     */
    private static final Logger logger = LoggerFactory.getLogger(BuyerBookCommonService.class);
    /**
     * 车型组信息查询服务
     */
    @Autowired
    private IYcCxzService ycCxzService;

    /**
     * 接口供应商查询数据对应
     */
    @Autowired
    private GySjdyService gySjdyService;

    /**
     * b_city服务
     */
    @Autowired
    private IBCityServiceClient ibCityServiceClient;
    /**
     * 调用CUSTOMER接口对象
     */
    @Autowired
    private IShShbServiceClient iShShbServiceClient;
    /**
     * 控润规则查询对象
     */
    @Autowired
    private CpsaUseCarProfitCacheService cpsaUseCarProfitCacheService;
    /**
     *
     */
    @Autowired
    private CpsaProfitCacheService cpsaProfitCacheService;

    /**
     * 产品过滤Service
     */
    @Autowired
    private BuyerFilterSetService filterSetService;
    /**
     * 城市Service
     */
    @Autowired
    private IVeCityService veCityService;

    /**
     * 图片地址服务
     */
    @Autowired
    private UsecarConfig usecarConfig;

    /**
     * 调用CDS城市查询API
     */
    @Autowired
    private IVeCityServiceClient iVeCityServiceClient;
    /**
     * 计费规则信息
     */
    @Autowired
    private SellerValuationService valuationService;
    /**
     * 计费规则信息
     */
    @Autowired
    private ITrainZdServiceClient iTrainZdServiceClient;
    /**
     * 商户接口对象
     */
    @Autowired
    private IShShbServiceClient shShbService;

    /**
     * 商户性质查询对象
     */
    @Autowired
    private ICpsClassServiceClient cpsClassService;

    /**
     * 商户性质查询对象
     */
    @Autowired
    private YcHmdService ycHmdService;
    /**
     * 用车基础数据缓存服务
     */
    @Autowired
    private CarBaseDataCacheService carBaseDataCacheService;
    /**
     * 用车订单乘客Service
     */
    @Autowired
    private YcDdCkService ycDdCkService;
    /**
     * 更新商户常旅客信息
     */
    @Autowired
    private IShShclkServiceClient shShclkServiceClient;

    /**
     *  获取商户接口信息
     */
    @Autowired
    private IVeJksqServiceClient iVeJksqServiceClient;

    @Autowired
    private RedisCacheManage iVeCacheManage;

    @Autowired
    private CityLevelsGroupService cityLevelsGroupService;

    @Autowired
    private ChannelMemberDiscountService memberDiscountService;
    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 30;
    /**
     * 最大线程数
     */
    private static final int MAXIMUM_POOL_SIZE = 60;

    /**
     * 当线程数大于核心时，这是剩余空闲线程在终止前等待新任务的最大时间
     */
    private static final long KEEP_ALIVE_TIME = 1000L;
    /**
     * 队列容量
     */
    private static final int CAPACITY = 50;
    /**
     * 查询司机坐标线程池
     */
    private static final String POOL_NAME = "保存常旅客信息线程池";
    /**
     * 修改主单线程池
     */
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(CAPACITY),
            new ThreadFactoryBuilder().setNameFormat(POOL_NAME).setDaemon(true).build(),
            new ThreadPoolExecutor.AbortPolicy());
    /**
     * 获取外部供应商的取消规则等信息
     *
     * @param linkCp CPS标准对象
     * @return linkCp CPS标准对象
     */
    public UseCarPorductModelVO getWbGysQxgz(UseCarPorductModelVO linkCp) {
        GyQxgz dto = new GyQxgz();
        dto.setCplx(linkCp.getDdlx());
        dto.setGyShbh(linkCp.getGysbh());
        dto.setZt(BigDecimal.ONE);//启用
        dto.setShzt(BigDecimal.ONE);//审核状态 启用
        dto.setSycxz(linkCp.getCxzbh());
        GyQxgz ycQxgz = carBaseDataCacheService.getOutQxgz(dto);
        if (null != ycQxgz) {
            linkCp.setFwnr(ycQxgz.getFwnr());// 服务内容
            linkCp.setFwbz(ycQxgz.getFwbz());//服务标准
            linkCp.setJgsm(ycQxgz.getJgsm());//价格说明
            linkCp.setYdgz(ycQxgz.getYdgz());//预订规则
            linkCp.setQxbz(ycQxgz.getQxbz());//取消备注
            linkCp.setBzjh(ycQxgz.getBzjh());//保障计划
            linkCp.setBz(ycQxgz.getBz());//备注
            String dydsjsqfy = ycQxgz.getDydsjsqfy() == null ? "0" : ycQxgz.getDydsjsqfy().toString();//到约定时间收取费用
            String ydsjhfy = ycQxgz.getYdsjhfy() == null ? "0" : ycQxgz.getYdsjhfy().toString();//超出约定时间后收取费用
            String mfqxsx = ycQxgz.getMfqxsx() == null ? "0" : ycQxgz.getMfqxsx().toString();//免费取消时限 2小时
            String ycsj = linkCp.getYcrq() + " " + linkCp.getYcsj();
            String qxgz = calcCancelRule(linkCp.getGysbh(), ycsj, dydsjsqfy, ydsjhfy, mfqxsx);
            linkCp.setQxgz(qxgz);
            //0,YDSJHFY:DYDSJSQFY:MFQXSX,0% 取消公式拼成规则
            linkCp.setQxgs("0," + ycQxgz.getYdsjhfy() + "%:" + ycQxgz.getDydsjsqfy() + "%:" + (ycQxgz.getMfqxsx()).intValue() * UseCarConstant.MINUTE + ",0%");
            linkCp.setMfqxsx(ycQxgz.getMfqxsx());
            linkCp.setDydsjsqfy(ycQxgz.getDydsjsqfy());
            linkCp.setYdsjhfy(ycQxgz.getYdsjhfy());
            linkCp.setXcsm(ycQxgz.getXcsm());
            linkCp.setCxsm(ycQxgz.getCxsm());
        }
        return linkCp;
    }

    /**
     * 通过CDS 接口查询车型组信息
     *
     * @param cpLists 产品信息
     * @param hideCarModelType 隐藏车型信息
     */
    public void getCxzJcxxLst(List<UseCarPorductModelVO> cpLists,String hideCarModelType) {
        try {
            Map<String, List<UseCarPorductModelVO>> carProductGroup = VeCollectionUtils.group(cpLists, "cxzbh");
            Set<String> cxzbhs = carProductGroup.keySet();
            //通过API 接口获取 查询产品车型组基础信息
            String[] cxzbhS = cxzbhs.toArray(new String[]{});
            List<YcCxzVO> ycCxzVOs = carBaseDataCacheService.getYcCxzByBh(cxzbhS);
            if(CollectionUtils.isEmpty(ycCxzVOs)){
                ycCxzVOs = ycCxzService.getYcCxzByBh(cxzbhS);
            }
            if (CollectionUtil.isEmpty(ycCxzVOs)) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_CDS_FAIL);
            }
            Type<YcCppVO> oldCppType = BeanMapper.getType(YcCppVO.class);//接口返回车品牌数据
            Type<UseCarCppVO> newCppType = BeanMapper.getType(UseCarCppVO.class);//CPS标准数据
            for (YcCxzVO ycCxz : ycCxzVOs) {
                for (UseCarPorductModelVO cpVO : cpLists) {
                    if (StringUtils.equals(cpVO.getCxzbh(), ycCxz.getCxzbm())) {
                        cpVO.setCxzmc(ycCxz.getCxzmc());
                        cpVO.setCxztp(usecarConfig.getVefileserver() + "/" + ycCxz.getCxztp());
                        cpVO.setCxzsm(ycCxz.getCxzsm());
                        cpVO.setZdkczrs(ycCxz.getZdkczrs());
                        cpVO.setDxlxsl(ycCxz.getDxlxsl());
                        cpVO.setXxlxsl(ycCxz.getXxlxsl());
                        cpVO.setCxzsxh(ycCxz.getSxh());
                        cpVO.setXlgg(ycCxz.getXlgg());
                        if (CollectionUtil.isNotEmpty(ycCxz.getYcCppVOs()) && !StringUtils.equals(hideCarModelType,"1")) {
                            List<UseCarCppVO> newCppList = new ArrayList<>();
                            for (YcCppVO oldCpp : ycCxz.getYcCppVOs()) {
                                UseCarCppVO newCpp = BeanMapper.map(oldCpp, oldCppType, newCppType);
                                newCpp.setCpptp(usecarConfig.getVefileserver() + "/" + newCpp.getCpptp());
                                newCppList.add(newCpp);
                            }
                            cpVO.setYcCppVOs(newCppList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询车型组信息失败", e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_CDS_FAIL);
        }

    }

    /**
     * 获取城市数据对应
     *
     * @param csid  城市ID,机场ID
     * @param shids 供应商户ID
     * @param sjlx  数据类型
     * @return gyscsid 供应商城市ID
     */

     public String getGysCsid(String shids, String csid, String sjlx) {
        String gyscsid = "";
        if (StringUtils.isNotBlank(shids) && StringUtils.isNotBlank(csid)) {

            /**判断城市ID是否是县级城市，如果是县级城市则取上级城市**/
            RestResponse<VeCityVO> response = iVeCityServiceClient.get(csid);
            VeCityVO cityVO = response.getResult();
            if (cityVO != null && StringUtils.equals("4", cityVO.getSfsh())) {
                csid = cityVO.getBy3();
            }

            GySjdy dto = new GySjdy();
            dto.setSjbh(csid);
            dto.setSjlx(sjlx);
            for (String shid : shids.split(",")) {
                dto.setGyShbh(shid);
                List<GySjdy> gySjdyList = gySjdyService.queryList(dto);
                if (CollectionUtil.isNotEmpty(gySjdyList)) {
                    if (gySjdyList.get(0) != null) {
                        String csbh = gySjdyList.get(0).getGySjbh() == null ? "" : gySjdyList.get(0).getGySjbh();
                        gyscsid += csbh + ",";
                    }
                } else {
                    gyscsid += ",";
                }
            }
            gyscsid = gyscsid.substring(0, gyscsid.lastIndexOf(","));
        }
        return gyscsid;
    }

    /**
     * 取消规则计算
     *
     * @param gyshbh    供应商户编号
     * @param ycsj      用车时间 yyyy-MM-dd HH:mm
     * @param dydsjsqfy 到约定时间收取费用比例
     * @param ydsjhfy   约定时间后收取费用比例
     * @param mfqxsx    免费取消时限 例 2  即120 分钟
     * @return 取消规则
     */
    public String calcCancelRule(String gyshbh, String ycsj, String dydsjsqfy, String ydsjhfy, String mfqxsx) {
        String qxgz = "";
        if (StringUtils.isNotBlank(ycsj) && VeDate.isRightDate(ycsj, "yyyy-MM-dd HH:mm")) {
            String mfqxsj = "";//中文免费取消时间
            if (StringUtils.isNotBlank(mfqxsx) && NumberUtil.isNumber(mfqxsx)) {
                Date date = VeDate.formatToDate(ycsj, "yyyy-MM-dd HH:mm");
                int mfqxsxfz = (int) (NumberUtils.toDouble(mfqxsx) * UseCarConstant.MINUTE);//转换成分钟
                date = VeDate.getPreMin(date, -mfqxsxfz);
                mfqxsj = VeDate.formatToStr(date, "MM月dd日HH:mm");
            }

            if (StringUtils.isNotBlank(dydsjsqfy) && NumberUtil.isNumber(dydsjsqfy)) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                ParsePosition pos = new ParsePosition(0);
                Date strtodate = formatter.parse(ycsj, pos);
                SimpleDateFormat formatter1 = new SimpleDateFormat("MM月dd日HH:mm");
                String ydsj = formatter1.format(strtodate);//约定时间
                qxgz = mfqxsj + "前可以免费取消,在" + mfqxsj + "到" + ydsj + "之间取消订单收取" + dydsjsqfy + "%," + mfqxsj + "之后取消订单收取" + ydsjhfy + "%";
            } else {
                qxgz = "暂无取消规则";
            }
            if (StringUtils.isNotBlank(gyshbh) && UsecarGysApiEnum.DDYC.getShbh().equals(gyshbh)) {
                qxgz = "如果您在滴滴司机接单后取消，可能产生取消手续费，费用以取消时滴滴返回的金额为准!";
            }
        }
        return qxgz;
    }

    /**
     * 根据三字码获取机场站点编号
     *
     * @param szm 机场三字码
     * @return zdid 站点ID
     */
    public String getZdidBySzm(String szm) {
        String zdid = "";
        if (StringUtils.isNotBlank(szm)) {
            BCity bCity = ibCityServiceClient.getBcityBySzm(szm);
            if (null != bCity) {
                zdid = bCity.getBh();
            }
        }
        return zdid;
    }


    /**
     * 获取产品的 供应商Log  信息
     *
     * @param cpList 产品信息
     *               设置供应商产品LOG 信息
     * @return shShbVOMap 商户编号为KEY，商户对象为Value
     */
    public Map<String, ShShbVO> getSellerLog(List<UseCarPorductModelVO> cpList) {
        try {
            Map<String, List<UseCarPorductModelVO>> carProductGroup = VeCollectionUtils.group(cpList, "gysbh");
            Set<String> gysbhs = carProductGroup.keySet();
            //通过API 接口获取 查询产品供应商商户信息
            String[] gysbhsS = gysbhs.toArray(new String[]{});
            RestResponse<List<ShShbVO>> shShbVOsRest = iShShbServiceClient.getShbByIds(gysbhsS);
            List<ShShbVO> shShbVOs = shShbVOsRest.getResult();
            if (CollectionUtil.isEmpty(shShbVOs)) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
            }
            Map<String, ShShbVO> shShbVOMap = toMap(shShbVOs);
            return shShbVOMap;
        } catch (Exception e) {
            logger.error("查询供应商信息失败", e);
            throw new SystemRuntimeException(UsecarOrderCode.UCAR_30007);
        }
    }
    /**
     * 将商户信息按商户编号 存入hash 表
     *
     * @param shShbVOs 商户列表
     * @return 商户信息HASH
     */
    private Map<String, ShShbVO> toMap(List<ShShbVO> shShbVOs) {
        if (CollectionUtil.isNotEmpty(shShbVOs)) {
            Map<String, ShShbVO> shShbVOMap = new HashMap<String, ShShbVO>();
            for (ShShbVO shShbVO : shShbVOs) {
                shShbVOMap.put(shShbVO.getShbh(), shShbVO);
            }
            return shShbVOMap;
        } else {
            return null;
        }
    }

    /**
     * 产品控润返佣等设置
     *
     * @param cpList        产品集合
     * @param bookSearchDTO 查询DTO
     * @return cpList 产品集合
     */
    public List<UseCarPorductModelVO> productKrSetting(List<UseCarPorductModelVO> cpList, BookSearchDTO bookSearchDTO) {
        String cplxId = cpList.get(0).getDdlx();
        Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> groupProfit = cpsaProfitCacheService.getCpsaUseCarProfitCacheByCplx(cplxId);
        //没有匹配到直接返回空
        if (groupProfit == null) {
            logger.error("*****未获取到用车控润信息....");
            return null;
        }
        String cityLevel = cityLevelsGroupService.selectCityLevelByCityId(bookSearchDTO.getCfcsid());
        MemberDiscountInfo memberLevelDiscount = memberDiscountService.findMemberLevelDiscount(bookSearchDTO.getChannelId(), bookSearchDTO.getMemberId(),bookSearchDTO.getQueryMemberPrice());
        //按照采购商户编号过滤
        String cgshBh = bookSearchDTO.getCgshbh();
        List<Map<String, Map<String, List<CpsaProfitCacheVO>>>> cgshProfit = cpsaUseCarProfitCacheService.filterByCgsh(groupProfit, cgshBh);
        for (UseCarPorductModelVO porductModelVO : cpList) {
            String info = bookSearchDTO.getCgshbh() + "_" + porductModelVO.getGysbh() + "_" + porductModelVO.getDdlx() + "_" + bookSearchDTO.getZdid();
            krfyMrzSetting(porductModelVO);
            if (bookSearchDTO.getCgshbh().equals(porductModelVO.getGysbh())) {
                loggeer.info(info + "当前采购商【" + bookSearchDTO.getCgshbh() + "】和当前产品发布商【" + porductModelVO.getGysbh() + "】一致,不予控润!");
                continue;
            }
            CpsaUseCarProfitCacheDTO cpsaUseCarProfitCacheDTO = new CpsaUseCarProfitCacheDTO();
            cpsaUseCarProfitCacheDTO.setCgshbh(bookSearchDTO.getCgshbh());
            cpsaUseCarProfitCacheDTO.setGyshbh(porductModelVO.getGysbh());
            cpsaUseCarProfitCacheDTO.setCplxid(porductModelVO.getDdlx());
            cpsaUseCarProfitCacheDTO.setZdid(bookSearchDTO.getZdid());
            cpsaUseCarProfitCacheDTO.setCpjeOne(porductModelVO.getGyJsje());//供应结算价
            cpsaUseCarProfitCacheDTO.setYcsj(bookSearchDTO.getYcrq()+" "+bookSearchDTO.getYcsj());
            cpsaUseCarProfitCacheDTO.setCityLevel(cityLevel);
            cpsaUseCarProfitCacheDTO.setChannelId(bookSearchDTO.getChannelId());
            BigDecimal cj = new BigDecimal(0);
            if (StringUtils.isNotBlank(porductModelVO.getScj()) && null != porductModelVO.getGyJsje()) {
                cj = new BigDecimal(porductModelVO.getScj()).subtract(porductModelVO.getGyJsje());
            }
            cpsaUseCarProfitCacheDTO.setCpjeTwo(cj);//差价(建议销售价-供应结算价)
            cpsaUseCarProfitCacheDTO.setCpjeThree(new BigDecimal(porductModelVO.getScj()));//建议销售价
            cpsaUseCarProfitCacheDTO.setYcsc(porductModelVO.getBdsc());
            cpsaUseCarProfitCacheDTO.setYclc(porductModelVO.getBdlc());
            logger.info(info + "开始获取控润返佣信息，查询参数为：{}", cpsaUseCarProfitCacheDTO);
            CpsaUseCarProfitCacheVO krVO = cpsaUseCarProfitCacheService.getCpsaUseCarProfitCacheVO(cpsaUseCarProfitCacheDTO,cgshProfit);
            logger.info(info + "获取到的控润返佣信息为：{}", krVO.toString());
            //是否有返佣 0，无；1，有
            if (StringUtils.equals(UseCarConstant.NUMONE, krVO.getSfyfy())) {
                //供应返佣方式(2:百分比1:数值 3：无返佣) 默认3
                porductModelVO.setGyFyfs(krVO.getQhffyfs());
                porductModelVO.setGyFybl(krVO.getQhffyds());
                porductModelVO.setGyFyje(krVO.getQhffyds());
                if (StringUtils.equals(UseCarConstant.NUMTWO, krVO.getQhffyfs().toString())) {
                    if (!StringUtils.equals(UsecarGysApiEnum.SQYC.getShbh(), porductModelVO.getGysbh())) {
                        //按百分比返佣
                        BigDecimal gyFyje = porductModelVO.getGyJsje().multiply(krVO.getQhffyds().multiply(BigDecimal.valueOf(UseCarConstant.BFB)));
                        porductModelVO.setGyFyje(gyFyje);
                    }
                }
                //供应前后返 1前返 2后返 0无返
                porductModelVO.setGyQhf(krVO.getQhf());
                if (new BigDecimal(UseCarConstant.ONE).compareTo(krVO.getQhf()) == UseCarConstant.ZERO) {
                    //如果不是首汽且不是曹操
                    if (!StringUtils.equals(UsecarGysApiEnum.SQYC.getShbh(), porductModelVO.getGysbh())
                        &&!StringUtils.equals(UsecarGysApiEnum.CCZC.getShbh(), porductModelVO.getGysbh())) {
                        //如果是前返 算出gy_jsje gyjsje - gyfyje
                        porductModelVO.setGyJsje(porductModelVO.getGyJsje().subtract(porductModelVO.getGyFyje()).setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                    } else {
                        if(StringUtils.equals(UseCarConstant.NUMTWO, krVO.getQhffyfs().toString())){
                            //如果是首汽或者曹操   供应结算金额 = 报价 / (1+返佣比例*0.01)
                            BigDecimal bj = porductModelVO.getGyJsje();
                            BigDecimal bl = BigDecimal.ONE.add(krVO.getQhffyds().multiply(BigDecimal.valueOf(UseCarConstant.BFB)));
                            porductModelVO.setGyJsje(bj.divide(bl, UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                            porductModelVO.setGyFyje(bj.subtract(porductModelVO.getGyJsje()));
                        }else{
                            porductModelVO.setGyJsje(porductModelVO.getGyJsje().subtract(porductModelVO.getGyFyje()).setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                        }
                    }
                }
            }
            //1有控润 0无控润
            if (StringUtils.equals(UseCarConstant.NUMONE, krVO.getSfygz())) {
                //平台控润方式(2:百分比1:数值 3：无返佣) 默认3
                porductModelVO.setPtkrfs(new BigDecimal(krVO.getKrfs()));
                porductModelVO.setPtkrbl(krVO.getKrz());
                porductModelVO.setPtkrje(krVO.getKrz());
                porductModelVO.setKrinfo(JsonMapper.nonEmptyMapper().toJson(krVO));
                //按百分比控润
                BigDecimal krz = new BigDecimal(0);
                if (StringUtils.equals(UseCarConstant.NUMONE, krVO.getKrgz())) {
                    //1按与供应结算价  结算价 * pt_krbl/100
                    krz = new BigDecimal(porductModelVO.getJsj());
                } else if (StringUtils.equals(UseCarConstant.NUMTWO, krVO.getKrgz())) {
                    //2按差价(建议销售价-与供应结算价)
                    krz = cj;
                } else {
                    //3建议销售价(接口供应商都是按建议销售价)
                    krz = cpsaUseCarProfitCacheDTO.getCpjeThree();
                }
                //百分比
                if (StringUtils.equals(UseCarConstant.NUMTWO, krVO.getKrfs())) {
                    BigDecimal ptkrje = krz.multiply(krVO.getKrz()).divide(new BigDecimal(UseCarConstant.BAI));
                    porductModelVO.setPtkrje(ptkrje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                }
                //百分比+固定值
                if(StringUtils.equals(UseCarConstant.NUMTHREE, krVO.getKrfs())){
                    BigDecimal gdz = krVO.getGdz() == null ? BigDecimal.ZERO : krVO.getGdz();
                    logger.info("****获取固定值：{}", gdz);
                    // 控润类型为3时，根据价格（供应结算价/差价）* 设置的百分比值+固定值 为采购结算价。
                    BigDecimal ptkrje = krz.multiply(krVO.getKrz()).divide(new BigDecimal(UseCarConstant.BAI)).add(gdz);
                    porductModelVO.setPtkrje(ptkrje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                }

                //平台贴点方式(1.金额,2.百分比)
                porductModelVO.setPttdfs(krVO.getTdfs());
                porductModelVO.setPttdbl(krVO.getTdz());
                porductModelVO.setPttdje(krVO.getTdz());
                //按百分比贴点
                if (StringUtils.equals(UseCarConstant.NUMTWO, krVO.getTdfs())) {
                    BigDecimal tdz = new BigDecimal(0);
                    if (StringUtils.equals(UseCarConstant.NUMONE, krVO.getKrgz())) {
                        //1按与供应结算价  结算价 * pt_tdbl/100
                        tdz = new BigDecimal(porductModelVO.getJsj()).multiply(krVO.getTdz().multiply(BigDecimal.valueOf(UseCarConstant.BFB)));
                    } else if (StringUtils.equals(UseCarConstant.NUMTWO, krVO.getKrgz())) {
                        //2按差价(建议销售价-与供应结算价)
                        tdz = cj.multiply(krVO.getTdz().divide(BigDecimal.valueOf(UseCarConstant.BAI)));
                    } else {
                        //3建议销售价(接口供应商都是按建议销售价)
                        tdz = cpsaUseCarProfitCacheDTO.getCpjeThree().multiply(krVO.getTdz().multiply(BigDecimal.valueOf(UseCarConstant.BFB)));
                    }
                    porductModelVO.setPttdje(tdz.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP));
                }
                if(StringUtils.equals(bookSearchDTO.getQueryMemberPrice(),"1")){
                    cpsaUseCarProfitCacheDTO.setMemberId(bookSearchDTO.getMemberId());
                    cpsaUseCarProfitCacheDTO.setQueryMemberPrice(bookSearchDTO.getQueryMemberPrice());
                    cpsaUseCarProfitCacheDTO.setMemberDiscountInfo(memberLevelDiscount);
                }
                //除价格维度的其他控润维度计算
                KrjeTdjeHolder krjeTdjeHolder = CpsaProfitFilterUtil.calcKrjeAndTdjeWithMember(krVO, cpsaUseCarProfitCacheDTO, porductModelVO.getPtkrje(), porductModelVO.getPttdje());
                porductModelVO.setPtkrje(krjeTdjeHolder.getKrje());
                porductModelVO.setPttdje(krjeTdjeHolder.getTdje());
                if(StringUtils.equals(krjeTdjeHolder.getSfhyj(),"1")){
                    porductModelVO.setUseMemberPrice("1");
                    porductModelVO.setMemberDesc(krjeTdjeHolder.getHyzkms());
                }
                //1按与供应结算价2按差价(建议销售价-与供应结算价) 3建议销售价(接口供应商都是按建议销售价)
                porductModelVO.setPtkrgz(krVO.getKrgz());
                //采购结算金额计算
                BigDecimal cgJsje = new BigDecimal(porductModelVO.getJsj()).add(porductModelVO.getPtkrje()).subtract(porductModelVO.getPttdje());
                porductModelVO.setJsj(cgJsje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP).toString());
                //接口供应商 经过控润后导致结算价大于市场价，这时将市场价改为结算价展示
                if (UsecarGysApiEnum.checkIsApiGys(porductModelVO.getGysbh())) {
                    if (Double.valueOf(porductModelVO.getJsj()) > Double.valueOf(porductModelVO.getScj())) {
                        porductModelVO.setScj(porductModelVO.getJsj());
                    }
                }
            }
        }
        return cpList;
    }

    /**
     * 产品过滤设置
     *
     * @param cpList        产品集合
     * @param bookSearchDTO 查询DTO
     * @return 产品集合
     */
    public List<UseCarPorductModelVO> productFiltsering(List<UseCarPorductModelVO> cpList, BookSearchDTO bookSearchDTO) {
        List<UseCarPorductModelVO> newCpList = new ArrayList<>();
        for (UseCarPorductModelVO porductModelVO : cpList) {
            String info = bookSearchDTO.getCgshbh() + "_" + porductModelVO.getGysbh() + "_" + porductModelVO.getDdlx() + "_" + bookSearchDTO.getZdid();
            BuyerFilterBookDTO bookDTO = new BuyerFilterBookDTO();
            bookDTO.setCgShbh(bookSearchDTO.getCgshbh());
            bookDTO.setCplx(porductModelVO.getDdlx());
            bookDTO.setGyShbh(porductModelVO.getGysbh());
            bookDTO.setZdid(bookSearchDTO.getZdid());
            boolean flag = filterSetService.bookingFilter(bookDTO);
            if (flag) {
                newCpList.add(porductModelVO);
            } else {
                logger.info(info + "，被过滤掉");
            }
        }
        return newCpList;
    }
    /**
     * 控润返佣等默认值设置
     *
     * @param carPorductModelVO 产品VO
     * @return carPorductModelVO 产品VO
     */
    private UseCarPorductModelVO krfyMrzSetting(UseCarPorductModelVO carPorductModelVO) {
        //平台控润方式(2:百分比1:数值 3：无返佣) 默认3
        carPorductModelVO.setPtkrfs(new BigDecimal(UseCarConstant.THREE));
        carPorductModelVO.setPtkrbl(new BigDecimal(0));
        carPorductModelVO.setPtkrje(new BigDecimal(0));
        //供应返佣方式(2:百分比1:数值 3：无返佣) 默认3
        carPorductModelVO.setGyFyfs(new BigDecimal(UseCarConstant.THREE));
        carPorductModelVO.setGyFybl(new BigDecimal(0));
        carPorductModelVO.setGyFyje(new BigDecimal(0));
        //平台贴点方式(1.金额,2.百分比 3 无贴点)
        carPorductModelVO.setPttdfs(UseCarConstant.NUMONE);
        carPorductModelVO.setPttdbl(new BigDecimal(0));
        carPorductModelVO.setPttdje(new BigDecimal(0));
        //供应前后返 1前返 2后返 0无返
        carPorductModelVO.setGyQhf(new BigDecimal(0));
        return carPorductModelVO;
    }

    /***
     * 简单的List去重
     * @param list 集合
     * @return list 集合
     */
    public List removeDuplicate(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        return list;
    }


    /**
     * 根据城市编号获取城市名称
     *
     * @param bh 城市编号
     * @return csmc 城市名称
     */
    public String getCsmcByBh(String bh) {
        String csmc = "";
        if (StringUtils.isNotBlank(bh)) {
            RestResponse<VeCityVO> response = veCityService.get(bh);
            if (response != null && response.getResult() != null) {
                VeCityVO veCityVO = response.getResult();
                csmc = veCityVO.getMc();
            }
        }
        return csmc;
    }

    /**
     * 根据站点ID获取三字码
     *
     * @param zdid 站点ID
     * @return szm 三字码
     */
    public String getSzmByZdid(String zdid) {
        String szm = "";
        if (StringUtils.isNotBlank(zdid)) {
            RestResponse<BCity> response = ibCityServiceClient.get(zdid);
            if (response != null && response.getResult() != null) {
                BCity bCity = response.getResult();
                szm = bCity.getNbbh();
            }
        }
        return szm;
    }

    /**
     * 根据机场站点ID或火车站站点ID获取站点所在城市ID
     *
     * @param id   站点ID
     * @param ddlx 订单类型
     * @return csid 城市ID
     */
    public String getCsidBySzmOrZdid(String id, String ddlx) {
        String csid = "";
        if (StringUtils.isBlank(id) || StringUtils.isBlank(ddlx)) {
            return csid;
        }
        //接机
        if (UsecarProductTypeEnum.jj.getCode().equals(ddlx) || UsecarProductTypeEnum.hhjj.getCode().equals(ddlx)
                || UsecarProductTypeEnum.sj.getCode().equals(ddlx) || UsecarProductTypeEnum.hhsj.getCode().equals(ddlx)) {
            BCity bCity = ibCityServiceClient.getBcityBySzm(id);
            if (bCity != null) {
                csid = bCity.getCsbh();
            }else{
                RestResponse<BCity> response = ibCityServiceClient.get(id);
                if (response != null && response.getResult() != null) {
                    bCity = response.getResult();
                    csid = bCity.getCsbh();
                }
            }
        } else if (UsecarProductTypeEnum.jz.getCode().equals(ddlx) || UsecarProductTypeEnum.hhjz.getCode().equals(ddlx)
                || UsecarProductTypeEnum.sz.getCode().equals(ddlx) || UsecarProductTypeEnum.hhsz.getCode().equals(ddlx)) {
            RestResponse<TrainZdVO> response = iTrainZdServiceClient.get(id);
            if (response != null && response.getResult() != null) {
                TrainZdVO trainZdVO = response.getResult();
                csid = trainZdVO.getCsbh();
            }
        }
        return csid;
    }


    /**
     * 获取供应商计价规则
     *
     * @param porductModelVO 缓存对象
     * @return porductModelVO 缓存对象
     */
    public UseCarPorductModelVO getSellerValuation(UseCarPorductModelVO porductModelVO) {
        try {
            SellerValuationDTO dto = new SellerValuationDTO();
            dto.setCplx(porductModelVO.getDdlx());
            dto.setCsid(porductModelVO.getCfdCsid());
            dto.setGyShbh(porductModelVO.getGysbh());
            dto.setCxzbh(porductModelVO.getCxzbh());
            SellerValuationVO vo = valuationService.getGyjfgzDetail(dto);
            porductModelVO.setSellerValuationVO(vo);
        } catch (Exception e) {
            logger.error("获取供应商计价规则异常：\r\n" + e, e);
        }
        return porductModelVO;
    }

    /**
     * 下单时商户性质信息查询赋值
     *
     * @param ycDd 订单对象
     */
    public void yesOrNoCloudOrder(YcDd ycDd) {
        RestResponse<ShShbVO> response = shShbService.getShbById(ycDd.getCgShbh());
        logger.info("输出商户信息：{}", JsonMapper.nonEmptyMapper().toJson(response));
        if (response.getResult() == null) {
            return;
        }
        ShShbVO shShbVO = response.getResult();
        RestResponse<CpsClassVO> restResponse = cpsClassService.getCpsclassById(shShbVO.getShxz());
        logger.info("输出商户性质信息：{}", JsonMapper.nonEmptyMapper().toJson(restResponse));
        if (restResponse.getResult() != null) {
            CpsClassVO cpsClassVO = restResponse.getResult();
            //if (UseCarConstant.CPS_DD_SHXZ.equals(cpsClassVO.getId())) {
                ycDd.setShxz(shShbVO.getShxz());
                ycDd.setShxzmc(cpsClassVO.getMc());
            //}
        }
    }

    /**
     * 通过乘客姓名，手机查询是否在黑名单里
     *
     * @param ckxm 乘客姓名
     * @param cksj 乘客手机
     * @return fasle 在黑名单里面  true 不在黑名单
     */
    public boolean checkYcHmd(String ckxm, String cksj) {
        logger.info("用车下单黑名单检查：{}，{}", ckxm, cksj);
        List<YcHmd> list = ycHmdService.selectBySj(cksj);
        logger.info("查询到的黑名单集合:{}", JsonMapper.nonEmptyMapper().toJson(list));
        return CollectionUtils.isEmpty(list);
    }

    /**
     * 更新商户常旅客信息
     * @param ycdd
     */
    public void saveOrUpdateClk(YcDd ycdd) {
        threadPoolExecutor.submit(() -> saveOrUpdateClk2(ycdd));
    }
    public void saveOrUpdateClk2(YcDd ycdd) {
        //下单成功后更新常旅客信息
        ShShclkSaveOrUpdateListDTO dto = null;
        try {
            dto = new ShShclkSaveOrUpdateListDTO();
            List<ShShclkSaveOrUpdateDTO> list = new ArrayList<>();
            List<YcDdCk> ycDdCks = ycDdCkService.selectPassengerListByOrderNo(ycdd.getDdbh());
            if (!StringUtils.equals(ycdd.getLxr(),ycdd.getCkxm())) {
                ShShclkSaveOrUpdateDTO shclkSaveOrUpdateDTO = new ShShclkSaveOrUpdateDTO();
                shclkSaveOrUpdateDTO.setClkmc(ycdd.getLxr());
                shclkSaveOrUpdateDTO.setSjhm(ycdd.getLxrdh());
                shclkSaveOrUpdateDTO.setProductCode("1000");
                shclkSaveOrUpdateDTO.setShbh(ycdd.getCgShbh());
                shclkSaveOrUpdateDTO.setCzuserid(ycdd.getCgYhbh());
                list.add(shclkSaveOrUpdateDTO);
            }
            for (YcDdCk ddmx : ycDdCks) {
                list.add(getShShclkSaveOrUpdateDTO(ddmx,ycdd));
            }
            dto.setList(list);
            logger.info("更新常旅客信息入参:{}", JsonMapper.nonEmptyMapper().toJson(dto));
            long start = System.currentTimeMillis();
            RestResponse<Boolean> booleanRestResponse = shShclkServiceClient.saveOrUpdate(dto);
            long end = System.currentTimeMillis();
            logger.info("更新常旅客信息回参:{}", JsonMapper.nonEmptyMapper().toJson(booleanRestResponse));
            logger.info("更新常旅客耗时：{}毫秒",end-start);
        } catch (Exception e) {
            logger.error("更新常旅客信息异常:{}", JsonMapper.nonEmptyMapper().toJson(dto), e);
        }
    }

    /**
     * 封装常旅客信息
     *
     * @param ycdd      订单主表
     * @param ycDdCk    订单乘客表
     * @return 常旅客信息
     */
    private ShShclkSaveOrUpdateDTO getShShclkSaveOrUpdateDTO(YcDdCk ycDdCk, YcDd ycdd) {
        ShShclkSaveOrUpdateDTO dto = new ShShclkSaveOrUpdateDTO();
        dto.setShbh(ycdd.getCgShbh());
        dto.setClkmc(ycDdCk.getPassengerName());
        // dto.setClkywmc("");//乘客英文名
        dto.setZjlx(ycDdCk.getPassengerCertificateType());
        dto.setProductCode("1000");
        dto.setZjhm(ycDdCk.getPassengerCertificateNo());
        //dto.setCsrq("");//出生日期
        //dto.setXb("");//性别
        dto.setFwbzjb(ycdd.getFwbzjb());
        //dto.setSfgwy("");//是否公务员
        //dto.setZw("");//职务
        //dto.setSzcs("");//所在城市
        //dto.setBzbz("");//备注
        //dto.setFrgs("");//法人公司
        //dto.setCbzx("");//成本中心
        dto.setCzuserid(ycdd.getCgYhbh());
        dto.setSjhm(ycDdCk.getPassengerPhone());
        dto.setId(ycDdCk.getPassengerEmpId());
        dto.setCbzx(ycDdCk.getCostCenterNo());
        dto.setFrgs(ycDdCk.getSettleDeptNo());
        return dto;
    }

    public String getShNoticeUrl(String shbh){
        String cacheName = "CGTZDZ_CACHE_SHBH_";
        String url = (String)iVeCacheManage.get(cacheName, shbh);
        if(StringUtils.isNotBlank(url)){
            return url;
        }
        logger.info("获取商户[{}]接口授权信息",shbh);
        RestResponse<VeJksqVO> restResponse= iVeJksqServiceClient.getByShbh(shbh);
        logger.info("获取商户[{}]接口授权信息返回{}",shbh,JsonMapper.nonEmptyMapper().toJson(restResponse));
        if(restResponse!=null && restResponse.getResult()!=null){
            VeJksqVO result = restResponse.getResult();
            if(StringUtils.isNotBlank(result.getCgtzdz())){
                iVeCacheManage.put(cacheName,shbh,result.getCgtzdz(),3600);
                return result.getCgtzdz();
            }
        }
        return "";
    }

    /**
     * 获取渠道id
     *
     * @param shbh 商户编号
     */
  //  @Cache(appname = ApplicationName.USECAR, table = "CHANNEL_ID", pre = "getChannelIdByShbh", key = "{1}", expire = 10)
    public String getChannelIdByShbh(String shbh) {
        try {
            String cacheName = "CPSA_GET_CHANNEL_ID_";
            String channelId = (String)iVeCacheManage.get(cacheName, shbh);
            if(StringUtils.isNotBlank(channelId)){
                return channelId;
            }
            RestResponse<ShShbVO> shShbResp = iShShbServiceClient.getShbById(shbh);
            if (!Objects.isNull(shShbResp) && shShbResp.getResult() != null) {
                channelId = shShbResp.getResult().getChannelId();
                iVeCacheManage.put(cacheName, shbh,channelId,10*60);
                return channelId;
            }

        } catch (Exception e) {
            logger.error("获取商户异常", e);
        }
        return null;
    }

    /**
     * 获取商户名称
     *
     * @param shbh 商户编号
     */
    @Cache(appname = ApplicationName.USECAR, table = "CHANNEL_ID", pre = "getShmcByShbh", key = "{1}", expire = 10)
    public String getShmcByShbh(String shbh) {
        try {
            RestResponse<ShShbVO> shShbResp = iShShbServiceClient.getShbById(shbh);
            if (!Objects.isNull(shShbResp) && shShbResp.getResult() != null) {
                return shShbResp.getResult().getJc();
            }
        } catch (Exception e) {
            logger.error("获取商户异常", e);
        }
        return null;
    }
}