package cn.vetech.center.usecar.service.order;

import cn.vetech.center.car.entity.GyGdjl;
import cn.vetech.center.car.service.GyGdjlService;
import cn.vetech.center.common.ApplicationName;
import cn.vetech.center.config.mybatisplus.cipher.annotation.EnableCipher;
import cn.vetech.center.config.redis.zookeeper.LockType;
import cn.vetech.center.config.redis.zookeeper.ZookeeperLockService;
import cn.vetech.center.finance.api.dto.GetDkzhOfShDTO;
import cn.vetech.center.jrdata.apiclient.dto.JrDataDTO;
import cn.vetech.center.usecar.api.vo.LinkUseCarOrderPushVO;
import cn.vetech.center.usecar.api.vo.PriceProjectBean;
import cn.vetech.center.usecar.book.buyer.service.BuyerBookService;
import cn.vetech.center.usecar.book.buyer.specicar.service.BuyerBookSpeciCarService;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.UsecarCzlxEnum;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import cn.vetech.center.usecar.cpsc.dto.CarQueryOrderListAllDTO;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.order.YcDdFz;
import cn.vetech.center.usecar.entity.usecar.StartingPriceMatchRule;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.entity.usecar.YcDdProfitRuleJl;
import cn.vetech.center.usecar.listener.CpsEventPublisher;
import cn.vetech.center.usecar.listener.entity.CpsEvent;
import cn.vetech.center.usecar.listener.entity.CpsEventEnum;
import cn.vetech.center.usecar.listener.entity.MsgNoticeDto;
import cn.vetech.center.usecar.mapper.order.YcDdMapper;
import cn.vetech.center.usecar.notice.buyer.dto.PayResultNotifyDTO;
import cn.vetech.center.usecar.notice.buyer.dto.RefuseOrderNotifyToBuyerDTO;
import cn.vetech.center.usecar.notice.buyer.service.BuyerNoticeService;
import cn.vetech.center.usecar.order.buyer.vo.BuyerRefundOrderVO;
import cn.vetech.center.usecar.order.cpsa.vo.CpsaOrderVO;
import cn.vetech.center.usecar.order.cpsa.vo.CpsaRefundOrderVO;
import cn.vetech.center.usecar.order.dto.OrderCheckDTO;
import cn.vetech.center.usecar.order.dto.OrderSearchDTO;
import cn.vetech.center.usecar.order.dto.UpdateYcOrderNullDTO;
import cn.vetech.center.usecar.order.seller.vo.SellerOrderVO;
import cn.vetech.center.usecar.order.seller.vo.SellerRefundOrderVO;
import cn.vetech.center.usecar.order.vo.OrderCheckVO;
import cn.vetech.center.usecar.pay.service.UsecarShZhglService;
import cn.vetech.center.usecar.service.ShCpSzService;
import cn.vetech.center.usecar.service.UsecarCacheBaseServiceImpl;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.orderes.OrderEsVO;
import cn.vetech.center.usecar.service.orderes.YcDdEsService;
import cn.vetech.center.usecar.service.orderes.YcTkDdEsService;
import cn.vetech.center.usecar.service.ordermq.YcDdMqSendService;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.setting.citysetting.service.CityLevelsGroupService;
import cn.vetech.center.usecar.setting.profit.dto.CpsaUseCarProfitCacheDTO;
import cn.vetech.center.usecar.setting.profit.dto.KrjeTdjeHolder;
import cn.vetech.center.usecar.setting.profit.service.*;
import cn.vetech.center.usecar.setting.profit.vo.CpsaUseCarProfitCacheVO;
import cn.vetech.center.usecar.threeorder.ThreeOrderService;
import cn.vetech.changelog.api.annotation.Change;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.base.ChangeEvent;
import org.vetech.core.base.PageDTO;
import org.vetech.core.cache.annotation.Cache;
import org.vetech.core.database.dynamic.annotation.DynamicDataSource;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.mapper.PageCopyUtil;
import org.vetech.core.modules.utils.number.Arith;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.EFFECTIVE_STATUS;
import static cn.vetech.center.usecar.common.UseCarConstant.rejectDdzts;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author chenyong
 * @since 2017-10-10
 */
@Service
@DynamicDataSource(name = "order")
@EnableCipher
public class YcDdService extends UsecarCacheBaseServiceImpl<YcDdMapper, YcDd> {
    /**
     * 开始时间入参
     */
    private final static String START_TIME = "startTime";
    /**
     * 结束时间入参
     */
    private final static String END_TIME = "endTime";
    /**
     * 前缀;
     */
    private static final String USECAR_UPDATE_YCDD_LOCK = "USECAR_UPDATE_YCDD_LOCK";
    /**
     * 过期时间10s
     */
    private static final long USECAR_UPDATE_YCDD_LOCK_WAIT_TIME = 1000 * 10;
    /**
     * 用车订单编号服务类
     */
    @Autowired
    private UsecarOrderNoService usecarOrderNoService;
    /**
     * 日志记录类
     */
    private static Logger logger = LoggerFactory.getLogger(YcDdService.class);

    /**
     * 发布订单消息
     */
    @Autowired
    private YcDdMqSendService ycDdMqService;

    /**
     * 用车订单ES search 服务类
     */
    @Autowired
    private YcDdEsService ycDdEsService;

    /**
     * 用车退款订单 ES  search 服务类
     */
    @Autowired
    private YcTkDdEsService ycTkDdEsService;

    /**
     * 用车退款订单 ES  search 服务类
     */
    @Autowired
    private CpsaUseCarProfitCacheService useCarProfitCacheService;

    /**
     * 商户账户管理
     */
    @Autowired
    private UsecarShZhglService shZhglService;
    /**
     * 接送车下单到供应商入口
     */
    @Autowired
    private BuyerBookService jscBookService;
    /**
     * 专快车采购查询预订服务
     */
     @Autowired
    private BuyerBookSpeciCarService buyerBookSpeciCarService;
    /**
     * 通知服务
     */
    @Autowired
    private BuyerNoticeService buyerNoticeService;
    /**
     * 用车订单分账服务
     */
    @Autowired
    private YcDdFzService ycDdFzService;
    /**
     * 用车跟单dao
     */
    @Autowired
    private GyGdjlService gyGdjlService;
    /**
     * 一键三单服务
     */
    @Autowired
    private ThreeOrderService threeOrderService;
    /**
     * 分布式锁
     */
    @Autowired
    private ZookeeperLockService zookeeperLockService;
    /**
     * 主单服务
     */
    @Autowired
    private YcDdMainService ycDdMainService;

    /**
     * 服务费计算服务
     */
    @Autowired
    private ShCpSzService shCpSzService;

    @Autowired
    private CpsEventPublisher cpsEventPublisher;

    @Autowired
    private CityLevelsGroupService cityLevelsGroupService;

    @Autowired
    private CpsaStartingPriceRuleService cpsaStartingPriceRuleService;

    @Autowired
    private CpsaYcDdProfitRuleJlService cpsaYcDdProfitRuleJlService;

    @Autowired
    private ChannelMemberDiscountService memberDiscountService;

    /**
     * 通过订单编号查询订单
     *
     * @param ddbh 订单编号
     * @return 用车订单详情
     */
    @EnableCipher
    public YcDd selectYcDd(String ddbh) {
        if(StringUtils.isBlank(ddbh)){
            return null;
        }
        String sjkly = usecarOrderNoService.getSjklyByDdbh(ddbh);
        YcDd ycDd = selectByIdAndSjkly(ddbh, sjkly);
        return ycDd;
    }

    /**
     * 查询缓存5分钟过期（不是最新数据）
     * @param ddbh 订单编号
     * @return 回参
     */
    @Cache(appname = ApplicationName.USECAR, table = "yc_dd", pre = "selectYcDdCache", key = "{1}", expiremin = 5)
    public YcDd selectYcDdCache(String ddbh) {
        if(StringUtils.isBlank(ddbh)){
            return null;
        }
        String sjkly = usecarOrderNoService.getSjklyByDdbh(ddbh);
        YcDd ycDd = selectByIdAndSjkly(ddbh, sjkly);
        return ycDd;
    }

    /**
     *  批量查询
     * @param ddbhs 入参
     * @param sjkly 入参
     * @return 返回
     */
    public List<YcDd> selectYcDds(Collection<String> ddbhs,String sjkly) {
        if(CollectionUtils.isEmpty(ddbhs)){
            return null;
        }
        EntityWrapper<YcDd> ew = new EntityWrapper<>();
        ew.eq("sjkly",sjkly);
        ew.in("ddbh",ddbhs);
        return super.baseMapper.selectList(ew);
    }

    /**
     *  通过订单编号批量查询
     * @param ddbhs 入参
     * @return 返回
     */
    public List<YcDd> selectByOrderNoList(List<String> ddbhs) {
        if (CollectionUtils.isEmpty(ddbhs)) {
            return new ArrayList<>(0);
        }
        Map<String, List<String>> orderNoMap = ddbhs.stream().collect(Collectors.groupingBy(e -> usecarOrderNoService.getSjklyByDdbh(e)));
        List<YcDd> orderList = new ArrayList<>();
        orderNoMap.forEach((sjkly, orderNoList) -> {
            EntityWrapper<YcDd> ew = new EntityWrapper<>();
            ew.eq("sjkly", sjkly);
            ew.in("ddbh", orderNoList);
            List<YcDd> oneOrderList = super.baseMapper.selectList(ew);
            if (CollectionUtils.isNotEmpty(oneOrderList)) {
           orderList.addAll(oneOrderList);
            }
        });
        return orderList;
    }
    /**
     *  通过订单编号批量查询
     * @param mianOrderNos 入参
     * @return 返回
     */
    public List<YcDd> selectByMainOrderNoList(List<String> mianOrderNos) {
        if (CollectionUtils.isEmpty(mianOrderNos)) {
            return new ArrayList<>(0);
        }
        Map<String, List<String>> orderNoMap = mianOrderNos.stream().collect(Collectors.groupingBy(e -> usecarOrderNoService.getSjklyByDdbh(e)));
        List<YcDd> orderList = new ArrayList<>();
        orderNoMap.forEach((sjkly, orderNoList) -> {
            EntityWrapper<YcDd> ew = new EntityWrapper<>();
            ew.eq("sjkly", sjkly);
            ew.in("p_ddbh", orderNoList);
            List<YcDd> oneOrderList = super.baseMapper.selectList(ew);
            if (CollectionUtils.isNotEmpty(oneOrderList)) {
                orderList.addAll(oneOrderList);
            }
        });
        return orderList;
    }

    /**
     * 获取已用车订单状态
     *
     * @param ycdd 用车订单
     * @return 完成状态
     */
    public String getCompleteDdzt(YcDd ycdd) {
        if (!StringUtils.equals(ycdd.getZfZt(), "1") && StringUtils.equals(ycdd.getFfgz(), "1")) {
            return UsecarOrderStatusEnum.YC1F.getCode();
        }
        return UsecarOrderStatusEnum.YC1H.getCode();
    }

    /**
     * 缓存移除
     *
     * @param ycDd 用车订单
     */
    public void removeDdCache(YcDd ycDd) {
        removeCache(ycDd);
    }

    /**
     * 通过主键更新订单,解决异动日志没有记录,但是实际修改的问题
     *
     * @param ycDd 要更新的订单订单
     * @return 是否更新成功
     */
    @Change
    public boolean updateYcDd(YcDd ycDd) {
        if (StringUtils.isBlank(ycDd.getDdbh())) {
            return false;
        }
        InterProcessMutex lock = null;
        try {
            lock = zookeeperLockService.tryLock(LockType.LOCK, USECAR_UPDATE_YCDD_LOCK, ycDd.getDdbh(), USECAR_UPDATE_YCDD_LOCK_WAIT_TIME);
             return updateYcDd2(ycDd);
        } catch (Exception e) {
            logger.error("通过主键更新订单异常", e);
        } finally {
            if (null != lock) {
                zookeeperLockService.unlock(lock);
            }
        }
        return false;
    }

    /**
     * 通过主键更新订单
     *
     * @param ycDd 要更新的订单订单
     * @return 是否更新成功
     */
    @Change
    public boolean updateYcDd2(YcDd ycDd) {
        String sjkly = usecarOrderNoService.getSjklyByDdbh(ycDd.getDdbh());
        YcDd oldYcDd = this.selectByIdAndSjkly(ycDd.getDdbh(), sjkly);
        if (oldYcDd != null && StringUtils.isBlank(oldYcDd.getGyDdbh()) && StringUtils.isNotBlank(ycDd.getGyDdbh())){
            EntityWrapper ew = new EntityWrapper();
            YcDd updateGyddbh = new YcDd();
            updateGyddbh.setGyDdbh(ycDd.getGyDdbh());
            updateGyddbh.setDdbh(oldYcDd.getDdbh());
            ew.where("ddbh={0}", oldYcDd.getDdbh()).and("sjkly={0}",sjkly);
            Integer update = baseMapper.update(updateGyddbh, ew);
            if(update > 0){
                ycDdMqService.sendUpdate(ycDd);
            }

        }
        if (!"手工修改".equals(ycDd.getDdbz()) && !checkStatus(oldYcDd.getDdzt(), ycDd.getDdzt(),oldYcDd.getGyDdbh())) {
            logger.info("状态不对,不允许修改!,{}->{},{}", oldYcDd.getDdzt(), ycDd.getDdzt(), oldYcDd.getDdbh());
            return false;
        }
        checkAndRepairData(oldYcDd,ycDd);
        EntityWrapper ew = new EntityWrapper();
        ycDd.setSjkly(sjkly);
        doOtherError(ycDd,oldYcDd);
        ew.where("ddbh={0}", ycDd.getDdbh()).and("sjkly={0}", ycDd.getSjkly());
        Integer updateRow = baseMapper.update(ycDd, ew);
        if (updateRow > 0) {
            //更新订单后清除缓存
            removeCache(ycDd);
            ycDdMqService.sendUpdate(ycDd);
            //更新异动日志信息
            // updateChangeLog(oldYcDd, ycDd, ChangeEvent.UPDATE, getCachename(ycDd)[1]);
            if (StringUtils.isBlank(ycDd.getTszdczlx())) {
                ycDd.setTszdczlx(UsecarCzlxEnum.UPDATE.getCode());
            }
            MsgNoticeDto dto = new MsgNoticeDto();
            dto.setCurrentDdzt(ycDd.getDdzt());
            dto.setDdbh(ycDd.getDdbh());
            dto.setKrgzid(ycDd.getKrgzid());
            cpsEventPublisher.send(CpsEvent.getInstance(CpsEventEnum.YC_MSG_NOTICE,dto));
            /**特殊字段操作类型*/
            if(StringUtils.isNotBlank(oldYcDd.getpDdbh())){
                threeOrderService.updateYcDdMain(ycDd);
            }
            updateChangeLog(oldYcDd,ycDd);
            return true;
        } else {
            logger.info("**更新订单失败{}", ycDd.getDdbh());
            return false;
        }
    }

    /**
     * 处理移动日志
     * @param oldYcDd 旧的订单
     * @param ycDd 修改的订单
     */
    private void updateChangeLog(YcDd oldYcDd, YcDd ycDd) {
        int tszdczlx = Integer.parseInt(ycDd.getTszdczlx());
        oldYcDd.setDdzt(UsecarOrderStatusEnum.getCpsOrderStatusByCode(oldYcDd.getDdzt()) + "(" + oldYcDd.getDdzt() + ")");
        YcDd newYcdd = BeanMapper.map(ycDd,YcDd.class);
        if (StringUtils.isNotBlank(ycDd.getDdzt())) {
            String desc =  UsecarOrderStatusEnum.getCpsOrderStatusByCode(ycDd.getDdzt());
            if (StringUtils.isNotBlank(desc)) {
                newYcdd.setDdzt(desc + "(" + ycDd.getDdzt() + ")");
            }
         }
        updateChangeLog(oldYcDd, newYcdd, tszdczlx, getCachename(newYcdd)[1]);
    }

    /**
     * 检查修复异常数据
     * @param oldYcDd 原来的订单
     * @param ycDd 用车订单
     */
    private void checkAndRepairData(YcDd oldYcDd, YcDd ycDd) {
        if (StringUtils.isBlank(oldYcDd.getGyDdbh()) && StringUtils.isBlank(ycDd.getGyDdbh())
                && StringUtils.isNotBlank(oldYcDd.getGyDzdh())) {
            ycDd.setGyDdbh(oldYcDd.getGyDzdh());
        }
        // 已上车后车牌号不为空，不做更新
        if (StringUtils.isEmpty(oldYcDd.getSjdh()) || StringUtils.isEmpty(ycDd.getSjdh())
                || StringUtils.equals(oldYcDd.getSjdh(), ycDd.getSjdh())) {
            return;
        }
        UsecarOrderStatusEnum orderStatusEnum = UsecarOrderStatusEnum.getEnum(oldYcDd.getDdzt());
        if (Arrays.asList(YC2E, YC4A, YC4B, YC4C).contains(orderStatusEnum)) {
            logger.info("已上车司机电话不修改：{}", ycDd.getDdbh());
            ycDd.setSjdh(null);
        }
    }

    /**
     * 处理数据设置错误问题
     * @param ycDd 订单
     * @param oldYcDd 原始订单
     */
    private void doOtherError(YcDd ycDd, YcDd oldYcDd) {
        // 设置退款申请时间
        if ((YC3A.getCode().equals(ycDd.getDdzt()) || YC3C.getCode().equals(ycDd.getDdzt()) || YC3D.getCode().equals(ycDd.getDdzt()))
                && oldYcDd.getCgTdsqsj() == null) {
            ycDd.setCgTdsqsj(VeDate.getNow());
        }
        if ((YC4A.getCode().equals(ycDd.getDdzt()) || YC4B.getCode().equals(ycDd.getDdzt()) || YC4C.getCode().equals(ycDd.getDdzt()))
                && oldYcDd.getGyFwwcsj() == null) {
            ycDd.setGyFwwcsj(VeDate.getNow());
        }
        //已审核已退款写分账时间
        if (YC3D.getCode().equals(ycDd.getDdzt()) && oldYcDd.getFzDatetime() == null) {
            ycDd.setFzDatetime(VeDate.getNow());
        }
        if(rejectDdzts.contains(ycDd.getDdzt()) && oldYcDd.getGyJudsj()==null){
            ycDd.setGyJudsj(VeDate.getNow());
        }
    }

    /**
     * 状态校验
     * @param oldDdzt 旧的订单状态
     * @param ddzt 新的订单状态
     * @return 是否能修改
     */
    private boolean checkStatus(String oldDdzt, String ddzt,String supplierNo) {
        // 不能从有违约，待支付改成已拒单
        if (YC2A.getCode().equals(ddzt) && YC2M.getCode().equals(oldDdzt)) {
            return false;
        }
        // 不能从已取消改成已拒单
        if (YC2A.getCode().equals(ddzt) && YC1E.getCode().equals(oldDdzt)) {
            return false;
        }
        // 不能从已用车待分账(已分账)改成已用车未支付
        if (YC4C.getCode().equals(ddzt) &&
                (YC4A.getCode().equals(oldDdzt) || YC4B.getCode().equals(oldDdzt))) {
            logger.info("已支付不能改成未支付");
            return false;
        }
        // 不能从 有违约或者已取消，待支付 改成 已审核，已退款
        if (YC3D.getCode().equals(ddzt) &&
                (YC2M.getCode().equals(oldDdzt) || YC1E.getCode().equals(oldDdzt))) {
            return false;
        }
        // 已取消不能变成已派车
        if (YC1E.getCode().equals(oldDdzt)
                && (YC2D.getCode().equals(ddzt) || YC2G.getCode().equals(ddzt) || YC2H.getCode().equals(ddzt) || YC1H.getCode().equals(ddzt))) {
            return false;
        }
        if(StringUtils.isBlank(supplierNo)){
            return true;
        }
        return true;
    }

    /**
     * 更新部分字段为空
     *
     * @param ycDd 待更新数据
     * @return 更新结果
     */
    @Change
    public boolean updateYcDdForGp(YcDd ycDd) {
        String sjkly = usecarOrderNoService.getSjklyByDdbh(ycDd.getDdbh());
        YcDd oldYcDd = this.selectByIdAndSjkly(ycDd.getDdbh(), sjkly);
        EntityWrapper ew = new EntityWrapper();
        ycDd.setSjkly(sjkly);
        ew.where("ddbh={0}", ycDd.getDdbh()).and("sjkly={0}", ycDd.getSjkly());
        logger.warn("开始更新订单改派信息： {}", ycDd.getDdbh());
        UpdateYcOrderNullDTO updateDto = new UpdateYcOrderNullDTO();
        updateDto.setDdbh(ycDd.getDdbh());
        updateDto.setDdzt(ycDd.getDdzt());
       updateDto.setGyDdbh(ycDd.getGyDdbh());
        updateDto.setSjkly(ycDd.getSjkly());
        Integer updateRow = baseMapper.updateYcDdForGp(updateDto);
        if (updateRow > 0) {
            //更新订单后清除缓存
            removeCache(ycDd);
            ycDdMqService.sendUpdate(ycDd);
            //更新异动日志信息
            updateChangeLog(oldYcDd, ycDd, ChangeEvent.UPDATE, getCachename(ycDd)[1]);
            return true;
        } else {
            logger.error("更新订单失败{}", ycDd.getDdbh());
            return false;
        }
    }

    /**
     * 通过司机主键id查询对应的订单集合
     *
     * @param sjid 派车司机信息实体类
     * @return 返回订单集合
     */
    public List<YcDd> getDdListBySjid(String sjid) {
        List<YcDd> ycDds = ycDdEsService.getDdListBySjid(sjid);
        return ycDds;
    }


    /**
     * 用车供应正常单分页查询符合条件的订单编号列表
     *
     * @param pageDTO 分页查询条件
     * @return 符合条件的 订单编号列表
     */
    public Page<OrderEsVO> searchSellerOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        return ycDdEsService.searchEsOrderList(pageDTO);
    }
    /**
     * 用车供应正常单分页查询符合条件的订单编号列表
     *
     * @param pageDTO 分页查询条件
     * @return 符合条件的 订单编号列表
     */
    public Page<OrderEsVO> getJrNormalDataPage(PageDTO<JrDataDTO> pageDTO) {
        return ycDdEsService.getJrNormalDataPage(pageDTO);
    }

    /**
     * 用车供应正常单分页查询符合条件的订单编号列表
     *
     * @param pageDTO 分页查询条件
     * @return 符合条件的 订单编号列表
     */
    public Page<OrderEsVO> searchBuyerAndSellerRhd(PageDTO<OrderSearchDTO> pageDTO) {
        return ycDdEsService.searchEsOrderListRhd(pageDTO);
    }

    /**
     * 通过订单编号和数据库路由查询订单
     * 2022-1-20 21:00:41 删除缓存
     * @param dbbh  获取订单
     * @param sjkly 数据库路由
     * @return 用车订单
     */
    @EnableCipher
    public YcDd selectByIdAndSjkly(String dbbh, String sjkly) {
        EntityWrapper<YcDd> ew = new EntityWrapper<>();
        YcDd ycDdSer = new YcDd();
        ycDdSer.setDdbh(dbbh);
        ycDdSer.setSjkly(sjkly);
        ew.setEntity(ycDdSer);
        YcDd ycDd = super.selectOne(ew);
        logger.debug("订单[{}]查询数据库YcDd:{}", dbbh, ycDd);
        return ycDd;
    }

    /**
     * 通过订单编号和数据库路由查询订单
     *
     * @param dbbh 获取订单
     * @return 用车订单
     */
    public YcDd selectByIdNoCache(String dbbh) {
        String sjkly = usecarOrderNoService.getSjklyByDdbh(dbbh);
        EntityWrapper<YcDd> ew = new EntityWrapper<YcDd>();
        YcDd ycDdSer = new YcDd();
        ycDdSer.setDdbh(dbbh);
        ycDdSer.setSjkly(sjkly);
        ew.setEntity(ycDdSer);
        YcDd ycDd = super.selectOne(ew);
        logger.debug("订单[{}]查询数据库YcDd的数据:{}", dbbh, ycDd);
        super.putCache(ycDd);
        return ycDd;
    }

    /**
     * 用车供应或 运营 退款单分页查询符合条件的订单编号列表
     *
     * @param pageDTO 分页查询条件
     * @return 符合条件的 订单编号列表
     */
    public Page<OrderEsVO> searchRefundOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        //使用聚合查询查询
        return ycTkDdEsService.selectRefundOrderPage(pageDTO);
    }


    /**
     * 用车采购获取正常单订单状态数量
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<SellerOrderVO> selectSellerOrderTopNum(OrderSearchDTO dto) {
        if (StringUtils.isBlank(dto.getGyShbh())) {
            return null;
        }
        // return baseMapper.selectSellerOrderTopNum(dto);
        return ycDdEsService.selectSellerOrderTopNum(dto);
    }

    /**
     * 用车供应获取退款单订单状态数量
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<SellerRefundOrderVO> selectSellerRefundOrderTopNum(OrderSearchDTO dto) {
        return ycTkDdEsService.selectSellerRefundOrderTopNum(dto);
        // return baseMapper.selectSellerRefundOrderTopNum(dto);
    }

    /**
     * 用车采购获取正常单订单状态数量
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<BuyerOrderVO> selectBuyerOrderTopNum(OrderSearchDTO dto) {
        /**
         *  采购查询订单状态数量按照采购商户编号获取数据库路由
         */
        dto.setSjkly(usecarOrderNoService.getSjkly(dto.getCgShbh()));
        return baseMapper.selectBuyerOrderTopNum(dto);
    }

    /**
     * 用车供应获取退款单订单状态数量
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<BuyerRefundOrderVO> selectBuyerRefundOrderTopNum(OrderSearchDTO dto) {
        /**
         * 采购查询按照采购商户编号获取数据库路由
         */
        dto.setSjkly(usecarOrderNoService.getSjkly(dto.getCgShbh()));
        return baseMapper.selectBuyerRefundOrderTopNum(dto);
    }

    /**
     * 用车获取正常单订单状态数量
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<CpsaOrderVO> selectOrderTopNum(OrderSearchDTO dto) {
        return ycDdEsService.selectOrderTopNum(dto);
    }

    /**
     * 正常单查询数量
     * @param orderSearchDTO 查询入参
     * @return 数量
     */
    public int getJrNormalDataCount(JrDataDTO orderSearchDTO) {
        return ycDdEsService.getJrNormalDataCount(orderSearchDTO);
    }

    /**
     * 用车获取补差单分页数据
     *
     * @param pageDTO 查询参数
     * @return 正常单订单状态个数
     */
    public Page<OrderEsVO> getFillPriceOrderDataPage(PageDTO<JrDataDTO> pageDTO) {
        return ycDdEsService.getFillPriceOrderDataPage(pageDTO);
    }
    /**
     * 用车获取订单es数据
     *
     * @param orderNo 订单编号
     * @return 正常单es数据
     */
    public OrderEsVO getCarOrderByOrderNo(String orderNo) {
        return ycDdEsService.getCarOrderByOrderNo(orderNo);
    }
    /**
     * 正常单查询数量
     * @param orderSearchDTO 查询入参
     * @return 数量
     */
    public int getFillPriceOrderDataCount(JrDataDTO orderSearchDTO) {
        return ycDdEsService.getFillPriceOrderDataCount(orderSearchDTO);
    }
    /**
     * 用车获取正常单订单状态数量
     *
     * @param dto 查询参数
     * @return 正常单订单状态个数
     */
    public List<CpsaRefundOrderVO> selectRefundOrderTopNum(OrderSearchDTO dto) {
        //采用聚合查询
        return ycTkDdEsService.selectRefundOrderTopNum(dto);
    }

    /**
     * 新增用车订单
     *
     * @param ycDd 用车订单
     * @return 是否新增成功
     */
    @Change
    public boolean insertYcDd(YcDd ycDd) {
        String cgDdly = ycDd.getCgDdly();
        String ffgz = ycDd.getFfgz();
        //费控云的现付后用订单 计算服务费
        if(StringUtils.equals(cgDdly,"差旅费控云") && !StringUtils.equals(ffgz,UseCarConstant.ZT_ONE)){
            BigDecimal je = ycDd.getCgJsje()!=null?ycDd.getCgJsje().multiply(BigDecimal.valueOf(2)):BigDecimal.ZERO;
            if(StringUtils.equals(ycDd.getSfykj(),"1")){
                je = ycDd.getYfje()!=null?ycDd.getYfje():BigDecimal.ZERO;
            }
            BigDecimal fwsfwf = shCpSzService.calcFwsfwf(ycDd, true, UseCarConstant.ZERO,je);
            ycDd.setFwsfwf(fwsfwf);
        }
        Integer insert = this.baseMapper.insert(ycDd);
        if(null != insert && insert >= 1){
            ycDdMqService.sendInsert(ycDd);
        }
        return Boolean.TRUE;
    }

    public boolean simpleInsert(YcDd ycDd,String xjdh,String bjdh){
        boolean b = super.insert(ycDd);
        if(b){
            ycDdEsService.simpleInsert(ycDd,xjdh,bjdh);
        }
        return true;
    }

    @Override
    protected int getTimeToLiveSeconds() {
        return TIMETOLIVESECONDS;
    }

    @Override
    protected String getApplicationName() {
        return ApplicationName.USECAR;
    }

    /**
     * 采购查询订单列表
     *
     * @param pageDTO 查询条件
     * @return 订单分页信息
     */
    public Page<YcDd> searchBuyerOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        Page<String> page = pageDTO.genPage();
        OrderSearchDTO orderSearchDTO = pageDTO.getData();
        String sjkly = usecarOrderNoService.getSjkly(orderSearchDTO.getCgShbh());
        orderSearchDTO.setSjkly(sjkly);
        List<String> dbbhs = baseMapper.selectSellerOrderPage(page, orderSearchDTO);
         Page<YcDd> resultPage = PageCopyUtil.copy(page);
        if (CollectionUtil.isNotEmpty(dbbhs)) {
            List<YcDd> recoders = new ArrayList<YcDd>();
            for (String dbbh : dbbhs) {
                YcDd ycDd = selectByIdAndSjkly(dbbh, sjkly);
                recoders.add(ycDd);
            }
            resultPage.setRecords(recoders);
        }
        return resultPage;
    }

    /**
     * 采购查询退款订单列表
     *
     * @param pageDTO 分页查询对象
     * @return 采购查询退款订单列表
     */
    public Page<YcDd> searchBuyerRefundOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        Page<String> page = pageDTO.genPage();
        OrderSearchDTO orderSearchDTO = pageDTO.getData();
        String sjkly = usecarOrderNoService.getSjkly(orderSearchDTO.getCgShbh());
        orderSearchDTO.setSjkly(sjkly);
        List<String> dbbhs = baseMapper.selectBuyerRefundOrderPage(page, orderSearchDTO);
        Page<YcDd> resultPage = PageCopyUtil.copy(page);
        if (CollectionUtil.isNotEmpty(dbbhs)) {
            List<YcDd> recoders = new ArrayList<YcDd>();
            for (String dbbh : dbbhs) {
                YcDd ycDd = selectByIdAndSjkly(dbbh, sjkly);
                recoders.add(ycDd);
            }
            resultPage.setRecords(recoders);
        }
        return resultPage;
    }

    /**
     * 运营异常单查询订单列表
     *
     * @param pageDTO 查询条件
     * @return 订单分页信息
     */
    public Page<YcDd> searchExceptionOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        Page<String> page = pageDTO.genPage();
        OrderSearchDTO orderSearchDTO = pageDTO.getData();
        List<String> dbbhs = baseMapper.selectExceptionOrderPage(page, orderSearchDTO);
        Page<YcDd> resultPage = PageCopyUtil.copy(page);
        if (CollectionUtil.isNotEmpty(dbbhs)) {
            List<YcDd> recoders = new ArrayList<YcDd>();
            for (String dbbh : dbbhs) {
                YcDd ycDd = selectByIdAndSjkly(dbbh, orderSearchDTO.getSjkly());
        recoders.add(ycDd);
            }
            resultPage.setRecords(recoders);
        }
        return resultPage;
    }

    /**
     * 依据供应商返回的金额重新计算相关费用
     *
     * @param order 订单信息
     * @param zzje  供应商反馈金额
     * @return 计算结果
     */
    public YcDd getGysfy(YcDd order, double zzje) {
        logger.info("进入供应商返佣设置计算方法,参数\r\n" + order.toString());
        /** 供应返佣方式(2:百分比 1：金额) **/
        BigDecimal gyqhf = order.getGyQhf() == null ? new BigDecimal(0) : order.getGyQhf();
        BigDecimal gyfyfs = order.getGyFyfs();
        BigDecimal gyfyz = order.getGyFybl();
        BigDecimal gyfybl = order.getGyFybl() == null ? new BigDecimal(0) : order.getGyFybl();
        BigDecimal gyfyje = order.getGyFyje() == null ? new BigDecimal(0) : order.getGyFyje(); // 供应商返佣金额
        BigDecimal gyjsje = new BigDecimal(zzje); // 供应商结算金额
        // 供应商返佣金额
        /** 如果有供应商控润，到这里需要把控润里面赋值给平台的那一套控润给采购商 **/
        double cgfybl = order.getPtkrbl().doubleValue();// 可正，可负 默认去平台控润比例
        double cgfyje = order.getPtkrje().doubleValue();// 可正(加钱)，可负(让利) 默认去平台控润金额
        // 平台利润，如果是百分比那么利润就是结算价*返佣比例(数据库中如果是返佣百分比是比如：8%数据库存的是0.08)
        if (gyfyfs.intValue() == UseCarConstant.KR_KRFS_BFB) {
            // 供应商给CPS的返佣金额, 注意: int/int型 得出的结果是: int 型,结果会四舍五入成int型.
            gyfyje = new BigDecimal(Arith.round(zzje * gyfyz.intValue() / Double.valueOf(UseCarConstant.BAI), UseCarConstant.TWO));
        } else if (gyfyfs.intValue() == UseCarConstant.KR_KRFS_GDJE) {
            // 供应商给CPS的返佣金额
            gyfyje = gyfyz;
        } else if (gyfyfs.intValue() == UseCarConstant.KR_KRFS_WFY) {
            gyjsje = new BigDecimal(zzje);
        }
        if (gyqhf.intValue() == UseCarConstant.YC_QF) {
            // 与供应商结算价=当日底价(可能含夜间服务费)-供应商返佣金额
            gyjsje = new BigDecimal(zzje - gyfyje.doubleValue());
            BigDecimal payAmount = new BigDecimal (Double.toString(zzje));
             if (UsecarGysApiEnum.SQYC.getShbh().equals(order.getGyShbh()) && (gyfyfs.intValue() == UseCarConstant.KR_KRFS_BFB)) {
                gyjsje = Arith.mul(payAmount,BigDecimal.valueOf(UseCarConstant.BAI)).divide(BigDecimal.valueOf(UseCarConstant.BAI + gyfyz.intValue()),2, BigDecimal.ROUND_HALF_UP);
                gyfyje = payAmount.subtract(gyjsje);
            } else if (UsecarGysApiEnum.SZYC.getShbh().equals(order.getGyShbh()) && (gyfyfs.intValue() == UseCarConstant.KR_KRFS_BFB)) {
                // 神州需要先算返佣，然后在算结算金额；保留小数位要先算了返佣在做结算的处理
                gyjsje = Arith.mul(payAmount,BigDecimal.valueOf(UseCarConstant.BAI)).divide(BigDecimal.valueOf(UseCarConstant.BAI + gyfyz.intValue()),4, BigDecimal.ROUND_HALF_UP);
                gyfyje = Arith.sub(payAmount,gyjsje).setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
                gyjsje =  Arith.sub(payAmount,gyfyje);
                logger.info("进入神州用车结算计算返回：gyjsje：" + gyjsje + "gyfyje:" + gyfyje);
            }
        }
        gyfyje = gyfyje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        gyjsje = gyjsje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        /** 返佣方式统一成供应商返佣方式，要么都百分比，要么都固定值 **/
        order.setGyQhf(gyqhf);
        order.setGyFyfs(gyfyfs);
        order.setGyFybl(gyfybl);
        order.setGyFyje(gyfyje);
        order.setGyJsje(gyjsje);

        order.setCgFyfs(gyfyfs);
        order.setCgFybl(new BigDecimal(Math.abs(cgfybl)));
        order.setCgFyje(new BigDecimal(Math.abs(cgfyje)));
        logger.info("供应商返佣计算完成，返回数据\r\n" + order.toString());
        return order;
    }

    /**
     * 依据供应商返回的金额重新计算控润费用
     *
     * @param order 订单信息
     * @param zzje  供应商反馈金额
     * @return 计算结果
     */
    public YcDd getKr(YcDd order, double zzje,LinkUseCarOrderPushVO upv ) {
        List<PriceProjectBean> priceProjectBeans = upv.getPricedetails();
        logger.info("进入供应商返回的金额控润计算方法,参数\r\n" + order.toString());
       CpsaUseCarProfitCacheDTO cpsaUseCarProfitCacheDTO = new CpsaUseCarProfitCacheDTO();
        cpsaUseCarProfitCacheDTO.setCgshbh(order.getCgShbh());
        cpsaUseCarProfitCacheDTO.setGyshbh(order.getGyShbh());
        cpsaUseCarProfitCacheDTO.setCplxid(order.getDdlx());
        cpsaUseCarProfitCacheDTO.setZdid(order.getJsfwzdid());
        BigDecimal bdlc = upv.getBdlc() != null ? upv.getBdlc() : order.getBdlc();
        cpsaUseCarProfitCacheDTO.setYclc(bdlc==null?BigDecimal.ZERO:bdlc.divide(BigDecimal.valueOf(1000),2, RoundingMode.HALF_UP));
        cpsaUseCarProfitCacheDTO.setYcsc(upv.getBdsc()!=null?upv.getBdsc():order.getBdsc());
        cpsaUseCarProfitCacheDTO.setYcsj(order.getYcsj());
        cpsaUseCarProfitCacheDTO.setXdsj(order.getXdsj());
        cpsaUseCarProfitCacheDTO.setCityLevel(cityLevelsGroupService.selectCityLevelByCityId(order.getCfdCsid()));
        //供应结算价
        BigDecimal gyjsj = order.getGyJsje();
        if (BigDecimal.ONE.equals(order.getGyQhf())) {
            gyjsj = order.getGyJsje().add(order.getGyFyje());
        }
        cpsaUseCarProfitCacheDTO.setCpjeOne(gyjsj);//供应结算价
        //差价
        BigDecimal cj = new BigDecimal(zzje).subtract(gyjsj);
        cpsaUseCarProfitCacheDTO.setCpjeTwo(cj);//差价(建议销售价-供应结算价)
        //建议销售价
        BigDecimal jyxsj = new BigDecimal(zzje);
        cpsaUseCarProfitCacheDTO.setCpjeThree(jyxsj);//建议销售价
        cpsaUseCarProfitCacheDTO.setChannelId(order.getChannelId());
        logger.info("订单[" + order.getDdbh() + "]开始获取控润返佣信息，查询参数为：{}", cpsaUseCarProfitCacheDTO.toString());
        CpsaUseCarProfitCacheVO fyKrVO = useCarProfitCacheService.getCpsaUseCarProfitCacheVO(cpsaUseCarProfitCacheDTO);
        logger.info("订单[" + order.getDdbh() + "]获取到的控润返佣信息为：{}", fyKrVO.toString());
        //平台控润方式(2:百分比1:数值 3：无返佣) 默认3
        BigDecimal ptkrfs = order.getPtkrfs();
        //平台控润比例
        BigDecimal ptkrbl = order.getPtkrbl();
        //1按与供应结算价2按差价(建议销售价-与供应结算价) 3建议销售价(接口供应商都是按建议销售价)
        String ptkrgz = order.getPtkrgz();
        BigDecimal ptkrje = order.getPtkrje();
        //平台贴点方式(1.金额,2.百分比 3 无贴点)
        String pttdfs = order.getPttdfs();
        BigDecimal pttdbl = order.getPttdbl();
        BigDecimal pttdje = order.getPttdje();
        //是否有控润规则 1有控润 0无控润
        if (StringUtils.equals(UseCarConstant.NUMONE, fyKrVO.getSfygz())) {
            //平台控润方式(2:百分比1:数值 3：百分比+固定值)
            ptkrfs = new BigDecimal(fyKrVO.getKrfs());
            //平台控润比例
            ptkrbl = fyKrVO.getKrz();
            //1按与供应结算价2按差价(建议销售价-与供应结算价) 3建议销售价(接口供应商都是按建议销售价)
            ptkrgz = fyKrVO.getKrgz();
            ptkrje = order.getPtkrje();
            BigDecimal krz = BigDecimal.ZERO;
            if (StringUtils.equals(UseCarConstant.NUMONE, ptkrgz)) {
                //1按与供应结算价  结算价 * pt_krbl/100
                //1 前返,要算出供应商给出的结算价
                krz = gyjsj;
            } else if (StringUtils.equals(UseCarConstant.NUMTWO, ptkrgz)) {
                //2按差价(建议销售价-与供应结算价)
                krz = cj;
            } else if (StringUtils.equals(UseCarConstant.NUMTHREE, ptkrgz)) {
                //3建议销售价(接口供应商都是按建议销售价)
                krz = jyxsj;
            }
            krz = krz.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
            //按百分比控润
            if (ptkrfs.intValue() == UseCarConstant.TWO) {
                ptkrje = krz.multiply(ptkrbl).divide(new BigDecimal(UseCarConstant.BAI));
            } else if (ptkrfs.intValue() == UseCarConstant.THREE) {// 百分比+固定值
                logger.info("供应结算价:{}" + gyjsj.toString());
                logger.info("平台控润金额（百分比+固定值）之前:{}" + ptkrje.toString());
                // 平台控润固定值(百分比+固定值模式)
                BigDecimal gdz = fyKrVO.getGdz() == null ? BigDecimal.ZERO : fyKrVO.getGdz();
                logger.info("获取固定值：{}", gdz);
                // 控润类型为3时，根据价格（供应结算价/差价）* 设置的百分比值+固定值 为采购结算价。
                ptkrje = krz.multiply(ptkrbl).divide(new BigDecimal(UseCarConstant.BAI)).add(gdz);
                logger.info("平台控润金额（百分比+固定值）之后:{}" + ptkrje.toString());

            } else {
                ptkrje = ptkrbl;
            }

            //平台贴点方式(1.金额,2.百分比 3 无贴点)
            pttdfs = fyKrVO.getTdfs();
            pttdbl = fyKrVO.getTdz();

            //按百分比贴点
            if (StringUtils.equals(UseCarConstant.NUMTWO, fyKrVO.getTdfs())) {
                pttdje = krz.multiply(pttdbl).divide(new BigDecimal(UseCarConstant.BAI));
            } else {
                pttdje = pttdbl;
            }
            cpsaUseCarProfitCacheDTO.setMemberId(order.getMemberId());
            YcDdMain ycDdMain = ycDdMainService.selectById(order.getpDdbh());
            if(ycDdMain!=null){
                cpsaUseCarProfitCacheDTO.setQueryMemberPrice(ycDdMain.getSfcxhyj());
                cpsaUseCarProfitCacheDTO.setMemberDiscountInfo(memberDiscountService.findMemberLevelDiscount(order.getChannelId(),order.getMemberId()));
            }
            //除价格维度的其他控润维度计算
            logger.info("订单{}计算控润入参={}，{}，{}，{}",order.getDdbh(),JsonMapper.nonEmptyMapper().toJson(fyKrVO),JsonMapper.nonEmptyMapper().toJson(cpsaUseCarProfitCacheDTO),ptkrje,pttdje);
            KrjeTdjeHolder krjeTdjeHolder = CpsaProfitFilterUtil.calcKrjeAndTdjeWithMember(fyKrVO, cpsaUseCarProfitCacheDTO, ptkrje, pttdje);
            logger.info("订单{}的额外控润金额和让利金额={}",order.getDdbh(),JsonMapper.nonEmptyMapper().toJson(krjeTdjeHolder));
            pttdje = krjeTdjeHolder.getTdje();
            ptkrje = krjeTdjeHolder.getKrje();
            //记录匹配的控润规则id
            cpsaYcDdProfitRuleJlService.upsertMatchRecordAfterYcDdComplated(fyKrVO,order.getDdbh());
        } else {
            //如果没有获取到控润规则，则尝试获取订单搜索时历史匹配的控润规则记录
            YcDdProfitRuleJl ycDdProfitRuleJl = cpsaYcDdProfitRuleJlService.selectByDdbh(order.getDdbh());
           logger.info("没有匹配到控润规则，查询控润匹配记录表={}",JsonMapper.nonEmptyMapper().toJson(ycDdProfitRuleJl));
            if(ycDdProfitRuleJl!=null && StringUtils.isNotBlank(ycDdProfitRuleJl.getKrgzInfo())){
                String krgzInfo = ycDdProfitRuleJl.getKrgzInfo();
                CpsaUseCarProfitCacheVO vo = JsonMapper.nonEmptyMapper().fromJson(krgzInfo, CpsaUseCarProfitCacheVO.class);
                //平台控润方式
                ptkrfs = new BigDecimal(vo.getKrfs());
                ptkrbl = vo.getKrz();
                ptkrgz = vo.getKrgz();

                //没有取到控润配置，则取
                BigDecimal krz = BigDecimal.ZERO;
                if (StringUtils.equals(UseCarConstant.NUMONE, ptkrgz)) {
                    //1按与供应结算价  结算价 * pt_krbl/100
                    //1 前返,要算出供应商给出的结算价
                    krz = gyjsj;
                } else if (StringUtils.equals(UseCarConstant.NUMTWO, ptkrgz)) {
                    //2按差价(建议销售价-与供应结算价)
                    krz = cj;
                } else if (StringUtils.equals(UseCarConstant.NUMTHREE, ptkrgz)) {
                    //3建议销售价(接口供应商都是按建议销售价)
                    krz = jyxsj;
                }
                if (StringUtils.isNotBlank(ptkrgz) && !StringUtils.equals(UseCarConstant.NUMZERO, ptkrgz)) {
                    // 固定值
                    if (BigDecimal.ONE.equals(ptkrfs)) {
                        ptkrje = ptkrbl;
                    } else if(ptkrfs.intValue() == UseCarConstant.TWO){
                        ptkrje = krz.multiply(ptkrbl).divide(new BigDecimal(UseCarConstant.BAI));
                    }else if(ptkrfs.intValue() == UseCarConstant.THREE){
                        BigDecimal gdz = fyKrVO.getGdz() == null ? BigDecimal.ZERO : fyKrVO.getGdz();
                        logger.info("****获取固定值：{}", gdz);
                        // 控润类型为3时，根据价格（供应结算价/差价）* 设置的百分比值+固定值 为采购结算价。
                        ptkrje = krz.multiply(ptkrbl).divide(new BigDecimal(UseCarConstant.BAI)).add(gdz);
           }
                }
                //平台贴点方式(1.金额,2.百分比 3 无贴点)
                pttdfs = vo.getTdfs();
                pttdbl = vo.getTdz();

                //按百分比贴点
                if (StringUtils.equals(UseCarConstant.NUMTWO, pttdfs)) {
                    pttdje = krz.multiply(pttdbl).divide(new BigDecimal(UseCarConstant.BAI));
                } else {
                    pttdje = pttdbl;
                }
                YcDdMain ycDdMain = ycDdMainService.selectById(order.getpDdbh());
                cpsaUseCarProfitCacheDTO.setMemberId(order.getMemberId());
                if(ycDdMain!=null){
                    cpsaUseCarProfitCacheDTO.setQueryMemberPrice(ycDdMain.getSfcxhyj());
                    cpsaUseCarProfitCacheDTO.setMemberDiscountInfo(memberDiscountService.findMemberLevelDiscount(order.getChannelId(),order.getMemberId()));
                }
                //除价格维度的其他控润维度计算
                logger.info("订单{}计算控润入参={}，{}，{}，{}",order.getDdbh(),JsonMapper.nonEmptyMapper().toJson(vo),JsonMapper.nonEmptyMapper().toJson(cpsaUseCarProfitCacheDTO),ptkrje,pttdje);
                KrjeTdjeHolder krjeTdjeHolder = CpsaProfitFilterUtil.calcKrjeAndTdjeWithMember(vo, cpsaUseCarProfitCacheDTO, ptkrje, pttdje);
                logger.info("订单{}的额外控润金额和让利金额={}",order.getDdbh(),JsonMapper.nonEmptyMapper().toJson(krjeTdjeHolder));
                pttdje = krjeTdjeHolder.getTdje();
                ptkrje = krjeTdjeHolder.getKrje();
            }
        }

        //判断是否命中起步价补贴规则, 当开启起步价补贴时，不计算控润，将控润值置0。
        if(CpsaProfitFilterUtil.onlyQbj(order.getGyShbh(),priceProjectBeans)){
            StartingPriceMatchRule rule = cpsaStartingPriceRuleService.matchBtRule(order.getCgShbh(), order.getGyShbh(),order.getXdsj(),order.getYcsj());
            logger.info("起步价补贴规则匹配出参={}",JsonMapper.nonEmptyMapper().toJson(rule));
            if(rule != null){
                ptkrje = BigDecimal.ZERO;
     pttdje = cpsaStartingPriceRuleService.getBtjeAndGenerateBtjl(rule,order.getDdbh(),jyxsj);

            }
        }

        // 前台展示给CPS采购看到的采购价格(供应结算价(含夜间服务费)+CPS平台利润)
        jyxsj = jyxsj.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        ptkrje = ptkrje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        pttdje = pttdje.setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        BigDecimal fkje = jyxsj.add(ptkrje).subtract(pttdje).setScale(UseCarConstant.TWO, BigDecimal.ROUND_HALF_UP);
        order.setPtkrfs(ptkrfs);
        order.setPtkrbl(ptkrbl);
        order.setPtkrje(ptkrje);
        order.setYgje(jyxsj);
        order.setCgJsje(fkje);
        order.setGySjddje(jyxsj); //供应实际订单金额
        order.setJyxsje(jyxsj); //建议销售金额
        order.setYfje(fkje);
        order.setPtkrgz(ptkrgz);
        order.setPttdfs(pttdfs);
        order.setPttdbl(pttdbl);
        order.setPttdje(pttdje);
        order.setKrgzid(fyKrVO.getGzid());
        logger.info("订单[" + order.getDdbh() + "]供应商返回的金额控润计算完成，返回数据\r\n" + order.toString());
        return order;
    }

    /**
     * 根据采购商户编号获取所有未支付订单yfje总额
     *
     * @param cgshbh 采购商户编号
     * @return resultJe
     */
    public BigDecimal getNoPayOrderAmountByCgsbh(String cgshbh) {
        BigDecimal resultJe = BigDecimal.ZERO;
        String sjkly = usecarOrderNoService.getSjkly(cgshbh);
        List<YcDd> ycDdList = baseMapper.getNoPayOrderAmountByCgsbhAndSjkly(cgshbh, sjkly);
        //List<YcDd> ycDdList = ycDdEsService.getNoPayOrderAmountByCgsbh(cgshbh); 支付状态未建索引
        if (CollectionUtil.isNotEmpty(ycDdList)) {
            for (YcDd ycDd : ycDdList) {
                resultJe = resultJe.add(ycDd.getYfje() == null ? BigDecimal.ZERO : ycDd.getYfje());
            }
        }
        logger.info("根据采购商户编号【" + cgshbh + "】获取所有未支付订单且在行程中订单yfje总额：" + resultJe);
        return resultJe;
    }

    /**
     * 变价产品下到接口供应商
     *
    * @param ddbh 订单编号
     * @return isSuccess 为false时 需要将提示信息展示到页面 为true 无需做任何处理
     * 无论下单到接口供应商成功还是失败，返回都是 true 无需做任何处理
     */
    public RestResponse<String> createOrderToExternalSupplier(String ddbh) {
        RestResponse<String> response = new RestResponse<String>(UseCarConstant.TRUE);
        if (StringUtils.isBlank(ddbh)) {
            response.setStatus("502");
            response.setMessage("订单编号不能为空");
        }
        YcDd ycDd = selectYcDd(ddbh);//获取单条 正常单信息
        if (null == ycDd) {
            response.setStatus("506");
            response.setMessage("根据订单编号【" + ddbh + "】获取不到订单");
        }
        if (StringUtils.isNotBlank(ycDd.getGyDdbh())) {
            return response;
        }
        logger.debug("开始进入到验证账户账户余额是否支持下单==>订单编号：" + ddbh);
        RestResponse<String> restResponse = checkDkzhOfSh(ycDd.getCgShbh());
        if (!restResponse.isSuccess()) {
            response.setStatus(restResponse.getStatus());
            response.setMessage(restResponse.getMessage());
            logger.debug("验证账户账户余额是否支持下单失败==>订单编号：" + ddbh + "，失败原因==>" + restResponse.getMessage());
            return response;
        }
        //余额验证通过，开始下单
        boolean flag = false;

        if (StringUtils.isNotBlank(ycDd.getGyShbh()) && UsecarGysApiEnum.checkIsApiGys(ycDd.getGyShbh())) {
            logger.info("【" + ddbh + "】,此订单为接口供应商产品，即将进入下单到接口供应商方法");
            if (null != ycDd.getDdlx() && UsecarProductTypeEnum.zc.getCode().equals(ycDd.getDdlx())) {
                //专车下单到供应商
                flag = buyerBookSpeciCarService.createOrderToGys(ddbh);
            } else {
                //接送车下单到供应商
                flag = jscBookService.createOrderToGys(ddbh);
            }
        }
        YcDd ddBean = selectYcDd(ddbh);//获取单条 正常单信息
        if (flag) {
            try {
                logger.info("订单:" + ddbh + "下单到供应商状态成功,推送信息给采购商");
                PayResultNotifyDTO notifyDTO = new PayResultNotifyDTO();
        notifyDTO.setCpsDdbh(ddBean.getDdbh());
                notifyDTO.setCgShbh(ddBean.getCgShbh());
                notifyDTO.setZfJe(0.0);
                notifyDTO.setZfZt(ddBean.getZfZt());
                notifyDTO.setZfFs(ddBean.getFkfs());
                notifyDTO.setPtzt(UsecarOrderStatusEnum.YC1F.getCode());
                notifyDTO.setPayresult("OK");
                notifyDTO.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(notifyDTO.getPtzt()));
                notifyDTO.setZfzh(ddBean.getFkzh());
                notifyDTO.setNotNotice(StringUtils.contains(ddBean.getCgDdly(),"CPSC"));
                buyerNoticeService.payResultNotify(notifyDTO);
            } catch (Exception e) {
                logger.error("订单" + ddbh + "推送给采购商异常", e);
            }
        } else {
            ddBean.setDdzt(UsecarOrderStatusEnum.YC2C.getCode());// 订单状态改为已拒单-已退款
            ddBean.setGyJudr(ddBean.getGyShjc());  //供应拒单人名称
            ddBean.setGyJudrbh(ddBean.getGyShbh());  //供应拒单人编号
            ddBean.setGyJudshbh(ddBean.getGyShbh());  //供应拒单商户编号
            ddBean.setGyJudyy("供应商拒单"); //供应拒单原因
            ddBean.setGyJudsj(VeDate.getNow());  //供应拒单时间
            boolean result = updateYcDd(ycDd);//更新正常单信息
            logger.info("下单失败，走拒单流程，修改订单状态YC2C,结果：" + result);
            try {
                RefuseOrderNotifyToBuyerDTO refuseOrderNotifyDTO = new RefuseOrderNotifyToBuyerDTO();
                refuseOrderNotifyDTO.setCgShbh(ycDd.getCgShbh());
                refuseOrderNotifyDTO.setCpsDdbh(ycDd.getDdbh()); // 对应ASMS采购商订单表里的cg_ddbh字段值
                refuseOrderNotifyDTO.setDdlx(ycDd.getDdlx());
                refuseOrderNotifyDTO.setJdYy(ycDd.getGyJudyy());
                refuseOrderNotifyDTO.setJdSxf(new Double(0));
                refuseOrderNotifyDTO.setCpsztMc(UsecarOrderStatusEnum.getCpsOrderStatus(ycDd.getDdzt()));
                refuseOrderNotifyDTO.setCpszt(ycDd.getDdzt());
                refuseOrderNotifyDTO.setNotNotice(StringUtils.contains(ycDd.getCgDdly(),"CPSC"));
                if(StringUtils.equals(ycDd.getZfZt(),UseCarConstant.ZF_ZT_YZF)) {
                    buyerNoticeService.refuseOrderNotify(refuseOrderNotifyDTO,5000L);
                }else{
                    buyerNoticeService.refuseOrderNotify(refuseOrderNotifyDTO,0);
                }
            } catch (Exception e) {
                logger.info("推送供应商拒单数据给采购商[" + ycDd.getCgShbh() + "]异常：" + e.getMessage());
                logger.error("推送供应商拒单数据给采购商异常", e);
            }
        }
        return response;
    }

    /**
     * 校验指定的商户对应的代扣账号是否可代扣
     *
     * @param shbh 采购商户编号
     * @return RestResponse<String>
     */
    private RestResponse<String> checkDkzhOfSh(String shbh) {
        GetDkzhOfShDTO dto = new GetDkzhOfShDTO();
        dto.setCpbh(UseCarConstant.YC_CPBH);
        dto.setShbh(shbh);
        return shZhglService.checkDkzhOfSh(dto);
    }

    /**
     * 批量查询数据库订单数据
     *
     * @param ly 数据库路由
     * @return 循环路由获取到所有的数据
     */
    public List<YcDd> selectAll(String ly) {
        Page<YcDd> page = new Page<YcDd>(UseCarConstant.ONE, UseCarConstant.BAI);
        EntityWrapper<YcDd> ew = new EntityWrapper<YcDd>();
        ew.eq("sjkly",ly);
        ew.gt("xdsj",VeDate.getPreMonth(VeDate.getNow(),-UseCarConstant.ONE));
        ew.in("ddzt", new String[]{
                UsecarOrderStatusEnum.YC4A.getCode(), UsecarOrderStatusEnum.YC4B.getCode(), UsecarOrderStatusEnum.YC4C.getCode(),
                UsecarOrderStatusEnum.YC2D.getCode(), UsecarOrderStatusEnum.YC2G.getCode(), UsecarOrderStatusEnum.YC2F.getCode(),
                UsecarOrderStatusEnum.YC2H.getCode(), UsecarOrderStatusEnum.YC2E.getCode(), UsecarOrderStatusEnum.YC4A.getCode()
        });
        ew.isNotNull("gy_shbh");
        ew.isNotNull("gy_ddbh");
        ew.isNull("gy_dzdh");
        page = super.selectPage(page, ew);
        List<YcDd> result = page.getRecords();
        while (page.getTotal() > page.getCurrent() * page.getSize()) {
            page.setCurrent(page.getCurrent() + 1);
            page = super.selectPage(page, ew);
            result.addAll(page.getRecords());
        }
        return result;
    }


    /**
     * 根据供应订单编号查询用车订单编号
     *
     * @param gyddbh 供应订单编号
     * @return ddbh 用车订单编号
     */
    public String getDdbhByGyddbh(String gyddbh) {
        if (StringUtils.isNotBlank(gyddbh)) {
            return baseMapper.getDdbhByGyddbh(gyddbh);
        } else {
            return "";
        }
    }

    /**
     * 根据支付来源统计
     *
     * @param dto 查询参数对象
     * @return 金额汇总
     */
    public List<YcDd> countByZfly(OrderSearchDTO dto) {

        return baseMapper.countByZfLy(dto);
    }

    /**
     * @param pageDTO 入参
     * @return 返回
     */
    public Page<OrderEsVO> selectJsjdgzddList(PageDTO<OrderSearchDTO> pageDTO) {
        OrderSearchDTO orderSearchDTO = pageDTO.getData();
        Page page = new Page(pageDTO.getCurrent(), pageDTO.getSize(), "ycsj");
        page.setAsc(pageDTO.isAsc());
        orderSearchDTO.setStarttime(VeDate.getPreMin(VeDate.getStringDate(), UseCarConstant.NUM_30));
        orderSearchDTO.setEndtime(VeDate.getPreMin(VeDate.getStringDate(), UseCarConstant.NUM_60));
        List<OrderEsVO> list = baseMapper.selectJsjdgzddList(page, orderSearchDTO);
        List<OrderEsVO> listVO = new ArrayList<>();
        for (OrderEsVO orderEsVO : list) {
            List<GyGdjl> gyGdjlList = gyGdjlService.queryGenDanRencords(orderEsVO.getDdbh());
            if (CollectionUtils.isNotEmpty(gyGdjlList)) {
                continue;
            }
            YcDdFz ycDdFz = ycDdFzService.selectYcDdFzByDdbhLy(orderEsVO.getDdbh(), orderEsVO.getSjkly());
            if (ycDdFz != null) {
                orderEsVO.setBcje(ycDdFz.getBcJe());
                orderEsVO.setCgZh(ycDdFz.getCgZh());
                orderEsVO.setGyZh(ycDdFz.getGyZh());
            }
            if (StringUtils.equals(orderEsVO.getSjkly(), "1")) {
                orderEsVO.setYkjje(orderEsVO.getCgJsje());
            } else {
                orderEsVO.setYkjje(BigDecimal.ZERO);
            }
            listVO.add(orderEsVO);
            // orderEsVO.setGyptkkje(checkDdzt(ycDd.getDdzt(), cpsatdStatusForA) ? ycDd.getGyTksxf() : ycDd.getGyCbj());
        }
        return page.setRecords(listVO);
    }

    /**
     * @param dto 入参
     * @return 返回
     */
    public int selectJsjdgzddNum(OrderSearchDTO dto) {
        dto.setStarttime(VeDate.getPreMin(VeDate.getStringDate(), UseCarConstant.NUM_30));
        dto.setEndtime(VeDate.getPreMin(VeDate.getStringDate(), UseCarConstant.NUM_60));
        List<String> ddbhList = baseMapper.selectJsjdgzddNum(dto);
        int a = 0;
        for (String ddbh : ddbhList) {
            List<GyGdjl> gyGdjlList = gyGdjlService.queryGenDanRencords(ddbh);
              if (CollectionUtils.isNotEmpty(gyGdjlList)) {
                continue;
            }
            a++;
        }
        return a;
    }

    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 查询已预订待支付同步的工单
     */
    public List<YcDd> selectBookedPayment(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectBookedPayment(time, now);
    }

    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 已支付-待派车
     */
    public List<YcDd> selectPaidWaitDispatch(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectPaidWaitDispatch(time, now);
    }

    /**
     * @param time 截止时间
     * @return 已拒单 已退款
     */
    public List<YcDd> selectRejectedRefunded(String time) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        return baseMapper.selectRejectedRefunded(time);
    }

    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 已拒单待退款
     */
    public List<YcDd> selectRefusedWaitRefund(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectRefusedWaitRefund(time, now);
    }

    /**
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return 已派车待用车
     */
    public List<YcDd> selectDispatchWaitUseCar(String startTime, String endTime) {
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return null;
        }
        return baseMapper.selectDispatchWaitUseCar(startTime, endTime);
    }
    /**
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return 已派车待用车
     */
    public List<YcDd> selectDispatchWaitUseCarTwo(String startTime, String endTime) {
       if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return null;
        }
        return baseMapper.selectDispatchWaitUseCarTwo(startTime, endTime);
    }

    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 已用车-待分账
     */
    public List<YcDd> selectUsedCarWaitSplit(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectUsedCarWaitSplit(time, now);
    }

    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 已用车-待分账
     */
    public List<YcDd> selectUsedCarUnpaid(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectUsedCarUnpaid(time, now);
    }

    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 已申请-待审核
     */
    public List<YcDd> selectAppliedWaitApproval(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectAppliedWaitApproval(time, now);
    }


    /**
     * @param time 截止时间
     * @param now  当前时间
     * @return 已申请-待审核
     */
    public List<YcDd> selectApprovalWaitRefund(String time, Date now) {
        if (StringUtils.isEmpty(time) || now == null) {
            return null;
        }
        return baseMapper.selectApprovalWaitRefund(time, now);
    }

    /**
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return 已派车待用车, 查询预约用车
     */
    public List<YcDd> selectDispatchWaitScheduleUseCar(String startTime, String endTime) {
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return null;
        }
        return baseMapper.selectDispatchWaitScheduleUseCar(startTime, endTime);
    }
    /**
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return 已派车待用车, 查询预约用车
     */
    public List<YcDd> selectDispatchWaitScheduleUseCarTwo(String startTime, String endTime) {
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return null;
        }
        return baseMapper.selectDispatchWaitScheduleUseCarTwo(startTime, endTime);
    }

    /**
     * 查询待分账订单
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return 查询待分账订单
     */
    public List<YcDd> selectWaitSubAccount(Date startTime, Date endTime) {
        return baseMapper.selectWaitSubAccount(startTime, endTime);
    }

    /**
     * @param list 订单编号
     * @return 开发票订单
     */
    public List<YcDd> selectApplyInvoiceList(List<String> list) {
        return baseMapper.selectApplyInvoiceList(list);
    }

    /**
     * 根据主表订单状态查询
     *
     * @param zbddbh 主表订单编号
     * @param buyerNo 采购订单编号
     * @return 关联订单
     */
    public List<YcDd> selectByZbddbh(String zbddbh,String buyerNo) {
        if (StringUtils.isEmpty(zbddbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(buyerNo);
        wrapper.eq("sjkly", sjkly);
        wrapper.eq("zbddbh", zbddbh);
        wrapper.eq("cg_shbh", buyerNo);
        return baseMapper.selectList(wrapper);

    }

    /**
     * 根据主表订单状态查询
     *
     * @param zbddbh 主表订单编号
     * @param buyerNo 采购订单编号
     * @return 关联订单
     */
    public YcDd selectOneByZbddbh(String zbddbh, String buyerNo) {
        if (StringUtils.isEmpty(zbddbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(buyerNo);
        wrapper.eq("sjkly", sjkly);
        wrapper.eq("zbddbh", zbddbh);
        wrapper.eq("cg_shbh", buyerNo);
        List<YcDd> ycDdList = baseMapper.selectList(wrapper);
       if (CollectionUtils.isEmpty(ycDdList)) {
            return null;
        }
        for (YcDd ycdd : ycDdList) {
            if (StringUtils.isNotBlank(ycdd.getpDdbh())) {
                return ycdd;
            }
        }
        return null;
    }

    /**
     *
     * @param pDdbh 入参主单订单编号
     * @param buyerNo 采购商户号
     * @return 返回子单数据
     */
    public List<YcDd> selectBypDdbh(String pDdbh,String buyerNo) {
        if (StringUtils.isEmpty(pDdbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(buyerNo);
        wrapper.eq("sjkly", sjkly);
        wrapper.eq("p_ddbh", pDdbh);
        return baseMapper.selectList(wrapper);

    }
    /**
     * 查主订单编号下的所有子单
     * @param cgshbh 采购商户编号
     * @param zbddbh 主表订单编号
     * @return 关联订单
     */
    public List<YcDd> selectDispatchedByCustomerOrderNo(String zbddbh,String cgshbh) {
        if (StringUtils.isBlank(zbddbh) || StringUtils.isBlank(cgshbh)) {
            return new ArrayList<>();
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(cgshbh);
        wrapper.where("sjkly={0} and zbddbh={1} and cg_shbh={2} and ddzt in ('YC2D','YC2G','YC2H')", sjkly,zbddbh,cgshbh);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查主订单编号下的所有子单
     * @param cgshbh 采购商户编号
     * @param zbddbh 主表订单编号
     * @return 关联订单
     */
    public List<YcDd> getSubOrderByCustomerOrderNo(String zbddbh,String cgshbh) {
        if (StringUtils.isBlank(zbddbh) || StringUtils.isBlank(cgshbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(cgshbh);
        wrapper.where("sjkly={0} and zbddbh={1} and cg_shbh={2}", sjkly,zbddbh,cgshbh);
        return baseMapper.selectList(wrapper);
    }

  /**
     * 根据采购商户编号获取未支付订单
     *
     * @param cgshbh 采购商户编号
     * @param xzts 限制天数
     * @return 未支付订单
     */
    public List<YcDd> selectUnpayYcddList(String cgshbh,Integer xzts,String sjkly) {
        if (StringUtils.isBlank(cgshbh)) {
            return null;
        }
        Date startDate = VeDate.getPreDay(VeDate.getNow(),-xzts);
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.where("sjkly={0} and cg_shbh={1} and ddzt={2} and xdsj>{3}", sjkly,cgshbh,"YC4C",startDate);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页获取mycat中的数据
     *
     * @param sjkly         数据库路由
     * @param orderCheckDTO 条件
     * @return 集合
     */
    private List<OrderCheckVO> getOrderCheckListPage(String sjkly, OrderCheckDTO orderCheckDTO) {
        int current = 1;
        List<OrderCheckVO> ddlist = new ArrayList<>();
        final int size = 50;
        while (true) {
            Page<YcDd> page = new Page();
            page.setSize(size);
            page.setCurrent(current++);
            page.setOrderByField("xdsj");
            EntityWrapper<YcDd> ew = new EntityWrapper<>();
            ew.eq("sjkly", sjkly);
            ew.between("xdsj", orderCheckDTO.getStarttime(), orderCheckDTO.getEndtime());
            ew.isNotNull("gy_ddbh");
            ew.ne("gy_ddbh", "");
            Page<YcDd> ycDdsPage = super.selectPage(page, ew);
            List<YcDd> ycDds = ycDdsPage != null ? ycDdsPage.getRecords() : null;
            if (CollectionUtils.isEmpty(ycDds)) {
                break;
            }
            List<OrderCheckVO> orderCheckVOS = BeanMapper.mapList(ycDds, YcDd.class, OrderCheckVO.class);
            ddlist.addAll(orderCheckVOS);
        }
        logger.info("从数据库中拿到数据{},条数{}", sjkly, ddlist.size());
        return ddlist;
    }

    /**
     * 获取进行中预约单
     * @param cgshbh 采购商户编号
     * @param xzts 限制天数
     * @param sjkly 数据库索引
     * @return 预约单列表
     */
    public List<YcDd> selectJxzDd(String cgshbh, Integer xzts, String sjkly,String sjhm) {
        if (StringUtils.isBlank(cgshbh)) {
            return null;
        }
        Date startDate = VeDate.getPreDay(VeDate.getNow(),-xzts);
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.where("sjkly={0} and cg_shbh={1} and xdsj>{2} and cksj={3} ", sjkly,cgshbh,startDate,sjhm);
        wrapper.and().in("ddzt",new String[]{UsecarOrderStatusEnum.YC2F.getCode(),UsecarOrderStatusEnum.YC2D.getCode(),
                UsecarOrderStatusEnum.YC2G.getCode(),UsecarOrderStatusEnum.YC2H.getCode(),UsecarOrderStatusEnum.YC2E.getCode()});
        return baseMapper.selectList(wrapper);
    }

    /**
     * 通过主单编号和状态查询子单
     * @param pDdbh 主单编号
     * @return 子单
     */
    public List<YcDd> selectBypDdbh(String pDdbh) {
        if (StringUtils.isEmpty(pDdbh)) {
            return null;
        }
        String sjkly = usecarOrderNoService.getSjklyByDdbh(pDdbh);
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("sjkly",sjkly);
        wrapper.eq("p_ddbh", pDdbh);
        List<YcDd> ycDdList = super.selectList(wrapper);
        ycDdList = ycDdList.stream()
                .filter(e-> EnumSet.of(YC4A,YC4B,YC2P,YC4C,YC2M,YC2O).contains(UsecarOrderStatusEnum.getEnum(e.getDdzt())))
                .collect(Collectors.toList());
        return ycDdList;
    }


    /**
     * 通过主单编号和状态查询子单
     * @param pDdbh 主单编号
     * @return 子单
     */
    public List<YcDd> selectAllSubOrder(String pDdbh) {
        if (StringUtils.isEmpty(pDdbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("p_ddbh", pDdbh);
        List<YcDd> ycDdList = super.selectList(wrapper);
        return ycDdList;
    }
    /**
     * 通过主单编号查询子单
     * @param pDdbh 主单编号
     * @return 子单
     */
    public List<YcDd> selectAllByPDdbh(String pDdbh) {
        if (StringUtils.isEmpty(pDdbh)) {
            return null;
        }
        String sjkly = usecarOrderNoService.getSjklyByDdbh(pDdbh);
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("sjkly",sjkly);
        wrapper.eq("p_ddbh", pDdbh);
        return super.selectList(wrapper);
    }

    /**
     * 通过主单编号和状态查询子单
     * @param pDdbh 主单编号
     * @buyerNo 采购商单号
     * @return 子单
     */
    public YcDd selectFinishOrderBypDdbh(String pDdbh,String buyerNo) {
        if (StringUtils.isEmpty(pDdbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(buyerNo);
        wrapper.eq("sjkly", sjkly);
        wrapper.eq("p_ddbh", pDdbh);
        wrapper.in("ddzt", Arrays.asList("YC4A","YC4B","YC4C"));
        return super.selectOne(wrapper);
    }

    /**
     * 通过主单编号和状态查询子单
     * @param pDdbh 主单编号
     * @buyerNo 采购商单号
     * @return 子单
     */
    public List<YcDd> selectAllBypDdbh(String pDdbh,String buyerNo) {
        if (StringUtils.isEmpty(pDdbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        String sjkly = usecarOrderNoService.getSjkly(buyerNo);
        wrapper.eq("sjkly", sjkly);
        wrapper.eq("p_ddbh", pDdbh);
        return super.selectList(wrapper);
    }

    /**
     * 通过采购订单编号查询订单
     * @param buyerOrderNo 采购订单编号
     * @param businessNo 商户编号
     * @return 订单
     */
    public YcDd selectByBuyerOrderNo(String buyerOrderNo, String businessNo) {
        String sjkly = usecarOrderNoService.getSjkly(businessNo);
        if (StringUtils.isEmpty(buyerOrderNo) || StringUtils.isEmpty(businessNo)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("cg_ddbh", buyerOrderNo);
        wrapper.eq("cg_shbh", businessNo);
        wrapper.eq("sjkly", sjkly);
        return super.selectOne(wrapper);
    }

    /**
     * 通过采购订单编号查询订单已支付订单
     * @param buyerOrderNo 采购订单编号
     * @param buyerNo 商户编号
     * @return 订单
     */
    public YcDd getByBuyerOrderNo(String buyerOrderNo, String buyerNo) {
        String sjkly = usecarOrderNoService.getSjkly(buyerNo);
        if (StringUtils.isEmpty(buyerOrderNo) || StringUtils.isEmpty(buyerNo)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("cg_shbh", buyerNo);
        wrapper.eq("zbddbh", buyerOrderNo);
        wrapper.eq("sjkly", sjkly);
        // 添加已审核已退款
        wrapper.in("ddzt",Arrays.asList("YC4B","YC2P","YC3D"));
        return super.selectOne(wrapper);
    }

    /**
     * 通过供应商编号查询
     * @param supplierNo 供应商编号
     * @param supplierOrderNo 供应商订单编号
     * @return 订单
     */
     public YcDd selectBySupplierOrderNo(String supplierNo, String supplierOrderNo) {
        if (StringUtils.isEmpty(supplierNo) || StringUtils.isEmpty(supplierOrderNo) ) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("gy_shbh", supplierNo);
        wrapper.eq("gy_ddbh", supplierOrderNo);
        return super.selectOne(wrapper);
    }
    /**
     * 高管已派车订单查询
     * @param now 当前时间
     * @return 已派车订单列表
     */
    public List<YcDd> selectLeaderCarDispatched(Date now) {
        return this.baseMapper.selectLeaderCarDispatched(now);
    }
    /**
     * 高管已用车已用车
     * @param now 当前时间
     * @return 已用车已分账订单列表
     */
    public List<YcDd> selectLeaderCarFinished(Date now){
        return this.baseMapper.selectLeaderCarFinished(now);
    }

    /**
     * 高管待派车订单
     * @param now 当前时间
     * @return 待派车订单列表
     */
    public List<YcDd> selectLeaderCarWaitDispatch(Date now){
        return this.baseMapper.selectLeaderCarWaitDispatch(now);
    }

    /**
     * 通过主单单号获取订单编号
     *
     * @param mainOrderNo 主单单号
     * @return 订单编号
     */
    public RestResponse<String> getOrderNoByMainOrderNo(String mainOrderNo) {
        if (!StringUtils.startsWith(mainOrderNo, "ZD")) {
            return new RestResponse<>(mainOrderNo);
        }
        YcDdMain ycDdMain = ycDdMainService.selectByIdCache(mainOrderNo);
        if (ycDdMain == null) {
            return new RestResponse<>(mainOrderNo);
        }
        YcDd ycDd = selectByBuyerMainOrderNo(ycDdMain.getCgddbh(), ycDdMain.getCgshbh());
        if (ycDd == null) {
            return new RestResponse<>(mainOrderNo);
        }
        return new RestResponse<>(ycDd.getDdbh());
    }
    /**
     * 通过采购主表单号查询已完成订单
     * @param cgShbh 主表单号
     * @param sjkly 业务单号
     *  @param cgDdbh 采购订单编号
     * @return 已完成订单
     */
   public boolean checkCustomerOrderExists(String cgShbh, String cgDdbh,String sjkly) {
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("cg_ddbh", cgDdbh);
        wrapper.eq("sjkly", sjkly);
        wrapper.eq("cg_shbh", cgShbh);
        YcDd order = super.selectOne(wrapper);
        return order != null;
    }
    /**
     * 通过采购主表单号查询已完成订单
     * @param buyerMainOrderNo 主表单号
     * @param businessNo 业务单号
     * @return 已完成订单
     */
    public YcDd selectByBuyerMainOrderNo(String buyerMainOrderNo, String businessNo) {
        String sjkly = usecarOrderNoService.getSjkly(businessNo);
        if (StringUtils.isEmpty(buyerMainOrderNo) || StringUtils.isEmpty(businessNo)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("zbddbh", buyerMainOrderNo);
        wrapper.in("ddzt",Arrays.asList("YC4B","YC2P"));
        wrapper.eq("cg_shbh", businessNo);
        wrapper.eq("sjkly", sjkly);
        wrapper.orderBy("ddzt",false);
        return super.selectOne(wrapper);
    }

    public int countSupplierByBuyerMainOrderNo(String buyerMainOrderNo, String businessNo) {
        String sjkly = usecarOrderNoService.getSjkly(businessNo);
        if (StringUtils.isEmpty(buyerMainOrderNo) || StringUtils.isEmpty(businessNo)) {
            return 0;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.setSqlSelect("distinct(gy_shbh)");
        wrapper.eq("zbddbh", buyerMainOrderNo);
        wrapper.eq("cg_shbh", businessNo);
        wrapper.eq("sjkly", sjkly);
        return super.selectCount(wrapper);
    }

    /**
     * 通过采购主表单号查询已完成订单
     * @param buyerMainOrderNo 采购商单号
     * @return 已完成订单
     */
    public YcDd selectByBuyerMainOrderNo2(String buyerMainOrderNo) {
        if (StringUtils.isEmpty(buyerMainOrderNo)) {
            return null;
        }
          EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("zbddbh", buyerMainOrderNo);
        wrapper.in("ddzt",EFFECTIVE_STATUS);
        wrapper.orderBy("ddzt",false);
        return super.selectOne(wrapper);
    }

    /**
     * 通过采购主表单号查询已完成订单
     * @param buyerMainOrderNo 采购商单号
     * @return 已完成订单
     */
    public List<YcDd> selectByBuyerMainOrderNo3(String buyerMainOrderNo) {
        if (StringUtils.isEmpty(buyerMainOrderNo)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("zbddbh", buyerMainOrderNo);
        wrapper.orderBy("ddzt",false);
        return super.selectList(wrapper);
    }

    /**
     *  默认查询最近一天的数据
     * @param map 条件
     * @return 返回数据
     */
    public List<YcDd> searchInCondition(Map<String,Object> map,String sjkly,Integer current,Integer size){
        if(Objects.isNull(map)){
            return Lists.newArrayList();
        }
        Object param1 = map.get(START_TIME);
        Object param2 = map.get(END_TIME);
        EntityWrapper<YcDd> entityWrapper = new EntityWrapper<>();
        if(Objects.isNull(param1) || Objects.isNull(param2)){
            LocalDate now = LocalDate.now();
            String startTime = LocalDateTime.of(now.minusDays(1), LocalTime.parse("00:00:00")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String endTIme = LocalDateTime.of(now, LocalTime.parse("23:59:59")).minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            entityWrapper.between("ycsj",startTime,endTIme);
        }else{
            entityWrapper.between("ycsj",map.get(START_TIME),map.get(END_TIME));
        }
        entityWrapper.orderBy("ycsj",false);
        if(Objects.nonNull(map.get("ddzt"))){
            Object ddztParam = map.get("ddzt");
            if(ddztParam instanceof Collection){
                entityWrapper.in("ddzt",(Collection<String>)map.get("ddzt"));
            }
            if(ddztParam instanceof String){
                entityWrapper.eq("ddzt",map.get("ddzt"));
            }
        }
        if(StringUtils.isNotBlank(sjkly)){
            entityWrapper.eq("sjkly",sjkly);
        }
        Page<YcDd> page = new Page<>();
        if(size != null){
            page.setSize(size);
        }else{
            page.setSize(200);
        }
        if(current!=null){
            page.setCurrent(current);
        }else{
            page.setCurrent(1);
        }
        return super.selectPageList(page,entityWrapper);
    }

    /**
     * 批量更新数据
     * @param ycDds 订单编号
     */
    public boolean updateBatch(List<YcDd> ycDds){
        return super.updateBatchById(ycDds,100);
    }

    /**
     * 清楚主单号
     * @param ddbh 订单编号
     */
    public void clearPddbh(String ddbh){
        super.baseMapper.clearPddbh(ddbh);
    }


    /**
     * @param starttime 开始时间
     * @param endtime 结束时间
     * @param sjkly 数据库路由
     * @return 返回
     */
    public List<YcDd> findNotCkDdbh(String starttime,String endtime,String sjkly,String cgs){
        Page<YcDd> page = new Page<>();
        EntityWrapper<YcDd> ew = new EntityWrapper<>();
        ew.eq("sjkly",sjkly);
        ew.between("xdsj",starttime,endtime);
        ew.in("ddzt",EFFECTIVE_STATUS);
        ew.orderBy("xdsj",false);
        ew.eq("cg_shbh",cgs);
        page.setSize(100);
        page.setCurrent(1);
        List<YcDd> temp = Lists.newArrayList();
        List<YcDd> result = Lists.newArrayList();
        do{
            temp = this.selectPageList(page,ew);
            page.setCurrent(page.getCurrent()+1);
            if(CollectionUtils.isNotEmpty(temp)){
                result.addAll(temp.stream().map(e->{
                    YcDd ycDd = new YcDd();
                    ycDd.setDdbh(e.getDdbh());
                    ycDd.setpDdbh(e.getpDdbh());
                    return ycDd;
                }).collect(Collectors.toList()));
            }
        }while (CollectionUtils.isNotEmpty(temp));
       return result;
    }

    /**
     * 计算里程单价
     */
    public void calculateLcdj (YcDd ycDd) {
        if (null != ycDd.getCgJsje() && null != ycDd.getBdlc() && ycDd.getBdlc().doubleValue() != 0) {
            BigDecimal bdlcKilo =Arith.div(ycDd.getBdlc(), new BigDecimal(1000), 2);
            if (null != bdlcKilo && bdlcKilo.doubleValue() != 0) {
                BigDecimal lcdj = Arith.div(ycDd.getCgJsje(), bdlcKilo,2);
                ycDd.setLcdj(lcdj);
            }
        }
    }

    /**
     * 通过供应订单编号查询订单
     * @param gyddbh 供应订单编号
     * @param cgShbh 成功订单编号
     * @return 回参
     */
    public YcDd selectBySupplierOrderNoBuyerNo(String gyddbh, String cgShbh) {
        String sjkly = usecarOrderNoService.getSjkly(cgShbh);
        if (StringUtils.isEmpty(gyddbh) || StringUtils.isEmpty(cgShbh)) {
            return null;
        }
        EntityWrapper<YcDd> wrapper = new EntityWrapper<>();
        wrapper.eq("cg_shbh", cgShbh);
        wrapper.eq("gy_ddbh", gyddbh);
        wrapper.eq("sjkly", sjkly);
        return super.selectOne(wrapper);
    }


    public Page<String> selectOrderListAll(Page page, CarQueryOrderListAllDTO queryDOT) {
//        String sjkly = usecarOrderNoService.getSjkly(cgShbh);
        if (StringUtils.isBlank(queryDOT.getMemberId())) {
            return page;
        }
        long ks = System.currentTimeMillis();
        logger.info("记录sql开始时间{}",ks);
        List<String> ycDdMains = baseMapper.selectOrderList(page, queryDOT);
        long js = System.currentTimeMillis();
        logger.info("记录sql结束时间{},{}",js,js-ks);

        page.setRecords(ycDdMains);
        return page;
    }

    public String getCgddbh(String ddbh,String gyddbh,String gyshbh){
        try {
            YcDd ycDd = this.selectYcDd(ddbh);
            if(ycDd==null && StringUtils.isNotBlank(gyshbh) && StringUtils.isNotBlank(gyddbh)){
                ycDd = ycDdEsService.getDdByGyshbhAndGyDdBh(gyshbh, gyddbh);
                if (ycDd == null) {
                    ycDd = selectBySupplierOrderNo(gyshbh, gyddbh);
                }
            }
            return getCgddbh(ycDd);
        }catch (Exception e){
            logger.error("获取采购订单号异常",e);
        }
        return null;
    }

    public String getCgddbh(YcDd ycDd) {
        String cgDdbh = ycDd.getCgDdbh();

        if(StringUtils.isBlank(cgDdbh)){
            return null;
        }

        if(!StringUtils.startsWith(cgDdbh,"YC")){
            return null;
        }
        if(cgDdbh.length()<=6){
            return null;
        }
        if(StringUtils.isNotBlank(ycDd.getZbddbh())){
            return ycDd.getZbddbh();
        }
        return cgDdbh.substring(0, cgDdbh.length() - 2);
    }
}
