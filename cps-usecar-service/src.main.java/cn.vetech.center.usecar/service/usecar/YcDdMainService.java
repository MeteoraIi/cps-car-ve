package cn.vetech.center.usecar.service.usecar;

import cn.vetech.center.config.mybatisplus.cipher.annotation.EnableCipher;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.enums.UseCarTypeEnum;
import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import cn.vetech.center.usecar.common.enums.bill.YcCapitalFlowStateEnum;
import cn.vetech.center.usecar.common.enums.bill.YcRecycleStatusEnum;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.cpsc.dto.CarCanOpenInvoiceListDTO;
import cn.vetech.center.usecar.cpsc.dto.CarQueryOrderListAllDTO;
import cn.vetech.center.usecar.cpsc.dto.CarQueryOrderListDTO;
import cn.vetech.center.usecar.cpsc.dto.CarRepeatOrderDTO;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.MainOrderVO;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.mapper.usecar.YcDdMainMapper;
import cn.vetech.center.usecar.order.cpsa.vo.CpsaOrderVO;
import cn.vetech.center.usecar.order.cpsa.vo.CpsaRefundOrderVO;
import cn.vetech.center.usecar.order.dto.OrderSearchDTO;
import cn.vetech.center.usecar.report.cpsa.dto.CpsaExportInnerDataDTO;
import cn.vetech.center.usecar.report.cpsa.vo.CpsaExportInnerDataVO;
import cn.vetech.center.usecar.service.UsecarCacheBaseServiceImpl;
import cn.vetech.center.usecar.service.UsecarOrderNoService;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.YcDdEsService;
import cn.vetech.changelog.api.annotation.Change;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.base.BaseServiceImpl;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.concurrent.VeExecutorServiceFactory;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.mapper.PageCopyUtil;
import org.vetech.core.modules.utils.time.VeDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.CpscUseCarConstant.*;
import static cn.vetech.center.usecar.common.UseCarConstant.*;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.*;

/**
 * <p>
 * 用车订单主表 服务实现类
 * </p>
 *
 * @author vetech
 * @since 2021-09-01
 */
@Service
public class YcDdMainService extends BaseServiceImpl<YcDdMainMapper, YcDdMain> {

    /**
     * 日志记录类
     */
    private static Logger logger = LoggerFactory.getLogger(YcDdMainService.class);
    /**
     * 子订单服务
     */
    @Autowired
    private YcDdService ycDdService;
    /**
     * es
     */
    @Autowired
    private YcDdEsService ycDdEsService;
    /**
     * 用车订单编号Service
     */
    @Autowired
    private UsecarOrderNoService usecarOrderNoService;
    /**
     * 缓存服务
     */
    @Autowired
    private RedisCacheManage cacheManage;

    /**
     * 插入采购补差单
     *
     * @param ycDdMain 采购补差单
     * @return 是否插入成功
     */
    @Change
    public boolean insert(YcDdMain ycDdMain) {
        return super.insert(ycDdMain);
    }

    /**
     * 插入采购补差单
     *
     * @param ycDdMain 采购补差单
     * @return 是否插入成功
     */
    @Change
    public boolean insertOrUpdate(YcDdMain ycDdMain) {
        return super.insertOrUpdate(ycDdMain);
    }

    /**
     * 插入采购补差单
     *
     * @param ycDdMain 采购补差单
     * @return 是否插入成功
     */
    @Change
    public boolean updateById(YcDdMain ycDdMain) {
        if (ycDdMain == null || StringUtils.isBlank(ycDdMain.getpDdbh())) {
            return false;
        }
        boolean success = super.updateById(ycDdMain);
        if (!success) {
            YcDdMain update = new YcDdMain();
            update.setDdzt(ycDdMain.getDdzt());
            update.setDdbh(ycDdMain.getDdbh());
            update.setpDdbh(ycDdMain.getpDdbh());
            success = baseMapper.updateById(update) > 0;
            logger.info("{}更新关键字段：{}", ycDdMain.getpDdbh(), success);
        }
        return success;
    }

    /**
     * 通过采购订单编号获取主单信息
     *
     * @param cgDdbh  采购订单编号
     * @param buyerNo 商户编号
     * @return 主单
     */
    public YcDdMain selectByCgddbh(String cgDdbh, String buyerNo) {
        Wrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.where("cgddbh={0} and cgshbh={1}", cgDdbh, buyerNo);
        return super.selectOne(wrapper);
    }

    /**
     * 通过主单查询主单
     *
     * @param pDdbh 主单单号
     * @return 主单信息
     */
    public YcDdMain selectByIdCache(String pDdbh) {
        if (StringUtils.isBlank(pDdbh)) {
            return null;
        }
        // 走缓存
        return super.selectById(pDdbh);
    }

    /**
     * 通过主单查询主单
     *
     * @param pDdbh 主单单号
     * @return 主单信息
     */
    public YcDdMain selectById(String pDdbh) {
        if (StringUtils.isBlank(pDdbh)) {
            return null;
        }
        // 不走缓存
        return this.baseMapper.selectById(pDdbh);
    }

    /**
     * 用车供应正常单分页查询符合条件的订单编号列表
     *
     * @param pageDTO 分页查询条件
     * @return 符合条件的 订单编号列表
     */
    public Page<MainOrderVO> searchSellerOrderList(PageDTO<OrderSearchDTO> pageDTO) {
        //TODO 查询数据库
        OrderSearchDTO orderSearchDTO = pageDTO.getData();
        orderSearchDTO.setStarttime(orderSearchDTO.getStarttime() + " 00:00:00");
        orderSearchDTO.setEndtime(orderSearchDTO.getEndtime() + " 23:59:59");
        Page page = new Page(pageDTO.getCurrent(), pageDTO.getSize(), pageDTO.getOrderByField());
        page.setAsc(pageDTO.isAsc());
        List<YcDdMain> ycDdMainList = baseMapper.searchEsOrderList(page, orderSearchDTO);
        List<MainOrderVO> result = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(ycDdMainList)) {
            result = ycDdMainList.stream().map(e -> {
                MainOrderVO mainOrderVO = BeanMapper.map(e, MainOrderVO.class);
                mainOrderVO.setCgShjc(e.getCgShjc());
                mainOrderVO.setCgDdbh(e.getCgddbh());
                mainOrderVO.setCgShbh(e.getCgshbh());
                mainOrderVO.setSubDdbh(e.getDdbh());
                mainOrderVO.setInvoiceRecoveryStatusDesc(YcRecycleStatusEnum.getNameByCode(e.getInvoiceRecoveryStatus()));
                mainOrderVO.setOfflineBindingStatusDesc(YcCapitalFlowStateEnum.getNameByCode(e.getOfflineBindingStatus()));
                return mainOrderVO;
            }).collect(Collectors.toList());
        }
        page.setRecords(result);
        return page;
    }

    /**
     * @param pageDTO 入参
     * @return 返回
     */
    public Page<MainOrderVO> selectJsjdgzddList(PageDTO<OrderSearchDTO> pageDTO) {
        OrderSearchDTO orderSearchDTO = pageDTO.getData();
        Page page = new Page(pageDTO.getCurrent(), pageDTO.getSize(), "ycsj");
        page.setAsc(pageDTO.isAsc());
        orderSearchDTO.setStarttime(VeDate.getPreMin(VeDate.getStringDate(), UseCarConstant.NUM_30));
        orderSearchDTO.setEndtime(VeDate.getPreMin(VeDate.getStringDate(), UseCarConstant.NUM_60));
List<YcDdMain> list = baseMapper.selectJsjdgzddList(page, orderSearchDTO);
        page.setRecords(list);
        Page<MainOrderVO> resultPage = PageCopyUtil.copy(page, YcDdMain.class, MainOrderVO.class);
        return resultPage;
    }

    /**
     * @param ddbhs 订单编号
     * @return 返回集合
     */
    public List<YcDdMain> selectYcDds(Collection<String> ddbhs) {
        EntityWrapper<YcDdMain> ew = new EntityWrapper<>();
        ew.in("p_ddbh", ddbhs);
        return super.selectList(ew);
    }

    /**
     * 通过订单编号查询订单
     *
     * @param ddbh 订单编号
     * @return 用车订单详情
     */
    @EnableCipher
    public YcDdMain selectYcDd(String ddbh) {
        if (StringUtils.isBlank(ddbh)) {
            return null;
        }
        return super.selectById(ddbh);
    }

    /**
     * @param dto 入参
     * @return 正常单数据
     */
    public List<CpsaOrderVO> selectOrderTopNum(OrderSearchDTO dto) {
        List<CpsaOrderVO> cpsaOrderVOs = new ArrayList<>();
        //传多个评价星级
        List<String> pjdjList = new ArrayList<String>();
        if (StringUtils.isNotBlank(dto.getPjdjs())) {
            String pjdjs = dto.getPjdjs();
            for (String pjdj : pjdjs.split(",")) {
                if (StringUtils.isNotBlank(pjdj)) {
                    pjdjList.add(pjdj);
                }
            }
        }
        dto.setPjdjList(pjdjList);
        CpsaOrderVO cpsaOrder = new CpsaOrderVO();
        if (CollectionUtils.isNotEmpty(pjdjList)) {
            MainOrderVO cpOrder = baseMapper.selectCpOrder(dto);
            cpsaOrder.setSfcp("1");
            cpsaOrder.setCou(String.valueOf(cpOrder.getCou()));
        } else {
            cpsaOrder.setSfcp("0");
            cpsaOrder.setCou("0");
        }
        // 添加差评订单数量
        cpsaOrderVOs.add(cpsaOrder);

        // 按ddzt分组查数量时不用按评价等级筛选
        dto.setPjdjList(new ArrayList<>());
        List<MainOrderVO> mainOrderVOList = baseMapper.selectOrderTopNum(dto);
        int count = baseMapper.selectJsjdgzddCount(dto);
        if (CollectionUtils.isNotEmpty(mainOrderVOList)) {
            List<CpsaOrderVO> mainOrderVs = mainOrderVOList.stream().map(e -> {
                CpsaOrderVO cpsaOrderVO = new CpsaOrderVO();
                cpsaOrderVO.setDdzt(UsecarOrderStatusEnum.getEnum(e.getDdzt()));
                cpsaOrderVO.setCou(String.valueOf(e.getCou()));
                cpsaOrderVO.setJsjnum(count);
                return cpsaOrderVO;
            }).collect(Collectors.toList());
            cpsaOrderVOs.addAll(mainOrderVs);
        }
        return cpsaOrderVOs;
    }


    /**
     * @param dto 查询条件
     * @return 返回集合
     */
    public List<CpsaRefundOrderVO> selectRefundOrderTopNum(OrderSearchDTO dto) {
        dto.setStarttime(dto.getStarttime() + " 00:00:00");
        dto.setEndtime(dto.getEndtime() + " 23:59:59");
        List<MainOrderVO> mainOrderVOList = baseMapper.selectRefundOrderTopNum(dto);
        if (CollectionUtils.isNotEmpty(mainOrderVOList)) {
            return mainOrderVOList.stream().map(e -> {
                CpsaRefundOrderVO cpsaRefundOrderVO = new CpsaRefundOrderVO();
                cpsaRefundOrderVO.setDdzt(UsecarOrderStatusEnum.getEnum(e.getDdzt()));
                cpsaRefundOrderVO.setCou(String.valueOf(e.getCou()));
                return cpsaRefundOrderVO;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 根据采购商户和采购订单编号查询主表
     *
     * @param cgShbh 主表单号
     * @param cgDdbh 采购订单编号
     * @return 已完成订单
     */
    public boolean checkCustomerOrderExists(String cgShbh, String cgDdbh) {
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.eq("cgddbh", cgDdbh);
        wrapper.eq("cgshbh", cgShbh);
        YcDdMain order = super.selectOne(wrapper);
        return order != null;
    }

    /**
     * 同步最近三个个月的数据
     *
     * @param map 入参
     */
    public void syncFromYcDd(Map<String, Object> map) {
        String uuid = UUID.randomUUID().toString();
        ExecutorService executorService = VeExecutorServiceFactory.newExecuteor(1, 1, 1, "同步的yc_dd_main线程");
        executorService.submit(() -> {
            // CountDownLatch downLatch = new CountDownLatch(9);
            logger.info("开始同步，入参:{}", JsonMapper.defaultMapper().toJson(map));
            List<Callable<String>> tasks = Lists.newArrayList();
            Date updateDate = VeDate.getNow();
            //同一天的批次号相同
            String batchNoStr = null;
            if (map.get("batchNo") != null && StringUtils.isNotBlank((String) map.get("batchNo"))) {
                batchNoStr = (String) map.get("batchNo");
            } else {
                batchNoStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).replaceAll("-", "");
            }
            //  ExecutorService executorService = VeExecutorServiceFactory.newExecuteor(9, 9, 1, "同步的yc_dd_main线程");
            try {
                for (int i = 0; i < 9; i++) {
                    final String sjkly = String.valueOf(i);
                    final String batchNo = batchNoStr;
                    tasks.add(() -> {
                        String info = null;
                        try {
                            String infotemplate = "syncFromYcDd|car%s同步数量:%s,耗时:%ss";
                            long start = System.currentTimeMillis();
                            int s = syncOneMainOrder(updateDate, batchNo + "-" + sjkly, map, sjkly);
                            long end = System.currentTimeMillis();
                            logger.info("car{}同步结束", sjkly);
                            info = String.format(infotemplate, sjkly, s, (end - start) / 1000);
                        } catch (Exception e) {
                            logger.error("同步car{}异常!", sjkly, e);
                            info = "同步car" + sjkly + "异常!";
                        }
                        //   downLatch.countDown();
                        return info;
                    });
                }
          /*  List<Future<String>> futures = executorService.invokeAll(tasks);
            downLatch.await();
            for (Future<String> future : futures) {
                String info = future.get();
                logger.info(info);
            }*/
                StringBuilder infos = new StringBuilder();
                for (Callable<String> task : tasks) {
                    String info = task.call();
                    infos.append(info + "\r\n");
                }
                logger.info(infos.toString());
            } catch (Exception e) {
                logger.error("downLatch同步异常", e);
            }
            cacheManage.remove("sync.mainOrder");
            //  executorService.shutdown();
        });
        executorService.shutdown();
    }

    /**
     * @param updateDate
     * @param batchNo    传入的批次号，先删除改批次数据，后重新插入该批次
     * @param map
     * @return
     */
    private int syncOneMainOrder(Date updateDate, String batchNo, Map<String, Object> map, String sjkly) {
        logger.info("开始同步car{}的数据,入参{}", sjkly, JsonMapper.defaultMapper().toJson(map));
        List<YcDd> ycDds = Lists.newArrayList();
        int current = 1;
        int total = 0;
        //删除同一批次数据
        Wrapper<YcDdMain> deleteEn = new EntityWrapper<>();
        deleteEn.between("ycsj", map.get("startTime"), map.get("endTime"));
        deleteEn.eq("xg_ly", batchNo);
        this.baseMapper.delete(deleteEn);
        do {
            ycDds = ycDdService.searchInCondition(map, sjkly, current, 200);
            current++;
      if (CollectionUtils.isNotEmpty(ycDds)) {
                total += ycDds.size();
                //待更新的子订单数据
                List<YcDd> updatedList = Lists.newArrayList();
                //待插入的主单
                List<YcDdMain> insertedList = Lists.newArrayList();
                for (YcDd ycDd : ycDds) {
                    String pDdbh = usecarOrderNoService.getMainOrderNo(ycDd.getCgShbh());
                    if (StringUtils.isNotBlank(ycDd.getpDdbh())) {
                        pDdbh = ycDd.getpDdbh();
                        if (pDdbh.startsWith("YC")) {
                            pDdbh = usecarOrderNoService.getMainOrderNo(ycDd.getCgShbh());
                        }
                    }
                    //更新的子单
                    YcDd updatedYcdd = new YcDd();
                    updatedYcdd.setpDdbh(pDdbh);
                    updatedYcdd.setDdbh(ycDd.getDdbh());
                    updatedList.add(updatedYcdd);
                    //插入的主单
                    YcDdMain ycDdMain = BeanMapper.map(ycDd, YcDdMain.class);
                    ycDdMain.setpDdbh(pDdbh);
                    ycDdMain.setCgddbh(StringUtils.isNotBlank(ycDd.getZbddbh()) ? ycDd.getZbddbh() : ycDd.getCgDdbh());
                    ycDdMain.setCgshbh(ycDd.getCgShbh());
                    ycDdMain.setOrderType(UseCarTypeEnum.ONE.getCode());
                    ycDdMain.setUpdateTime(updateDate);
                    ycDdMain.setXgLy(batchNo);
                    ycDdMain.setDdbh(ycDd.getDdbh());
                    insertedList.add(ycDdMain);
                }
                //更新子子单数据
                ycDdService.updateBatch(updatedList);
                //更新子订单ES
                int i = ycDdEsService.updateBatch(updatedList);
                logger.info("任务批次号[{}],更新ES订单成功数量:{}", batchNo, i);
                if (CollectionUtils.isNotEmpty(insertedList)) {
                    // super.insertBatch(insertedList);
                      this.baseMapper.batchInsert(insertedList);
                }
                logger.info("任务批次号[{}]，将car{}表订单同步主单,数量:{},已同步:{}", batchNo, sjkly, insertedList.size(), total);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("异常", e);
                }
            }
        } while (CollectionUtils.isNotEmpty(ycDds));
        return total;
    }

    public boolean update(YcDdMain updateYcDdMain) {
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.eq("p_ddbh", updateYcDdMain.getpDdbh());
        return baseMapper.update(updateYcDdMain, wrapper) > 0;
    }

    /**
     * 订单列表查询
     *
     * @param page     分页入参
     * @param queryDOT 查询入参
     * @return 回参
     */
    public Page<YcDdMain> selectOrderList(Page<YcDdMain> page, CarQueryOrderListDTO queryDOT) {
        if (StringUtils.isBlank(queryDOT.getMemberId())) {
            return page;
        }
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.eq("member_id", queryDOT.getMemberId());
        if (BY_CREATE_TIME.equals(queryDOT.getRqlx())) {
            wrapper.ge("xdsj", queryDOT.getRqs());
            wrapper.le("xdsj", queryDOT.getRqz());
            wrapper.orderBy("xdsj", false);
        } else if (BY_USE_CAR_TIME.equals(queryDOT.getRqlx())) {
            wrapper.ge("ycsj", queryDOT.getRqs());
            wrapper.le("ycsj", queryDOT.getRqz());
            wrapper.orderBy("ycsj", false);
        }
        if (StringUtils.isNotBlank(queryDOT.getDdzt())) {
            wrapper.in("ddzt", queryDOT.getDdztList());
        }
        if (StringUtils.isNotBlank(queryDOT.getDdbh())) {
            wrapper.eq("p_ddbh", queryDOT.getDdbh());
        }
        if (StringUtils.isNotBlank(queryDOT.getYwlx())) {
            wrapper.eq("ddlx", queryDOT.getYwlx());
        }
       if (StringUtils.equals(queryDOT.getZfzt(), YES)) {
            wrapper.eq("zfzt", YES);
        } else if (StringUtils.equals(queryDOT.getZfzt(), NO)) {
            wrapper.isNull("zfzt");
        }

        if (StringUtils.equals(queryDOT.getSflj(), YES)) {
            wrapper.eq("yy_lx", BOOKING_TYPE_APPOINTMENT);
        } else if (StringUtils.equals(queryDOT.getSflj(), NO)) {
            wrapper.eq("yy_lx", BOOKING_TYPE_IMMEDIATE);
        }

        return super.selectPage(page, wrapper);
    }

    public List<YcDdMain> selectOrderListByDhs(List<String> ddbhs) {
        if (CollectionUtils.isEmpty(ddbhs)) {
            return null;
        }
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.in("p_ddbh", ddbhs);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查询未付订单
     *
     * @param orderDTO 入参
     * @return 回参
     */
    public YcDdMain selectUnpaidOrders(CarRepeatOrderDTO orderDTO) {
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.eq("member_id", orderDTO.getMemberId());
        wrapper.in("ddzt", UNPAID_ORDER_STATUS);
        wrapper.in("ddlx", orderDTO.getOrderTypeList());
        wrapper.gt("xdsj", orderDTO.getStartTime());
        return super.selectOne(wrapper);
    }

    /**
     * 进行中订单
     *
     * @param orderDTO 入参
     * @return 回参
     */
    public List<YcDdMain> selectInProgressOrders(CarRepeatOrderDTO orderDTO) {
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.eq("member_id", orderDTO.getMemberId());
        wrapper.in("ddzt", IN_PROGRESS_ORDER_STATUS);
        wrapper.ge("ycsj", VeDate.formatToStr(orderDTO.getStartTime(), PATTERN_ON_MINUTE));
        wrapper.le("ycsj", VeDate.formatToStr(orderDTO.getEndTime(), PATTERN_ON_MINUTE));
        wrapper.in("ddlx", orderDTO.getOrderTypeList());
        return super.selectList(wrapper);
    }

    /**
     * 查询一键三单待确认的订单
  * select  * from yc_dd d where d.ddzt = '1003' and sfyqr = '0'
     * and d.ydsj &gt;= #{startTime}
     *
     * @return 待确认的订单
     */
    public List<YcDdMain> selectWaitConfirm3Order() {
        final int preMin = -60 * 12;
        Date startTime = VeDate.getPreMin(VeDate.getNow(), preMin);
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.in("ddzt", Arrays.asList(YC2D.getOrderStatusCode(),YC2G.getOrderStatusCode(),YC2H.getOrderStatusCode(),YC2E.getOrderStatusCode()));
        wrapper.gt("xdsj", startTime);
        wrapper.eq("confirm_order", "0");
        return super.selectList(wrapper);
    }

    /**
     * 销售报表查询
     *
     * @param pageDTO 入参
     * @return 回参
     */
    public Page<CpsaExportInnerDataVO> searchExportDetailList(PageDTO<CpsaExportInnerDataDTO> pageDTO) {
        Page page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        CpsaExportInnerDataDTO queryDTO = pageDTO.getData();
        if (StringUtils.equals(queryDTO.getDateType(), "2")) {
            queryDTO.setKssj(queryDTO.getKssj() + " 00:00");
            queryDTO.setJssj(queryDTO.getJssj() + " 23:59");
        } else {
            queryDTO.setKssj(queryDTO.getKssj() + " 00:00:00");
            queryDTO.setJssj(queryDTO.getJssj() + " 23:59:59");
        }
        if (StringUtils.isNotBlank(queryDTO.getDdlx())) {
            queryDTO.setDdlxList(Arrays.asList(queryDTO.getDdlx().split(",")));
        }
        //  List<CpsaExportInnerDataVO> records = baseMapper.searchExportDetailList(page, pageDTO.getData());
        CpsaExportInnerDataDTO data = pageDTO.getData();
        data.setStart((pageDTO.getCurrent() - 1) * pageDTO.getSize() + 1);
        data.setEnd(pageDTO.getCurrent() * pageDTO.getSize());
        List<CpsaExportInnerDataVO> records = baseMapper.searchExportDetailList1(pageDTO.getData());
        if (CollectionUtils.isNotEmpty(records)) {
            int total = records.get(0).getTotal();
     page.setTotal(total);
        }
        page.setRecords(records);
        return page;
    }

    /**
     * 金额总计
     *
     * @param dto 入参
     * @return 回参
     */
    public CpsaExportInnerDataVO searchExportDetailTotal(CpsaExportInnerDataDTO dto) {
        return baseMapper.searchExportDetailTotal(dto);
    }

    /**
     * 异常报表统计
     *
     * @param dataDTO 入参
     * @return 回参
     */
    public YcDdMain sumAbnormalOrder(CpsaExportInnerDataDTO dataDTO) {
        return baseMapper.sumAbnormalOrder(dataDTO);
    }

    /**
     * 通过
     *
     * @param updateMains 主表信息
     * @return 回参
     */
    public boolean updateByIds(List<YcDdMain> updateMains) {
        if (CollectionUtils.isEmpty(updateMains)) {
            return false;
        }
        return super.updateBatchById(updateMains);
    }

    /**
     * 通过下单时间查询有效订单
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 回参
     */
    public List<YcDdMain> selectByCreateTime(String startDate, String endDate) {
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate))
            return new ArrayList<>(0);
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.in("ddzt", EFFECTIVE_STATUS);
        wrapper.ge("xdsj", startDate);
        wrapper.le("xdsj", endDate);
        return super.selectList(wrapper);
    }

    public Page<YcDdMain> selectOrderListAll(Page<YcDdMain> page, CarQueryOrderListAllDTO queryDOT) {
        if (StringUtils.isAnyBlank(queryDOT.getMemberId(), queryDOT.getCksjh(), queryDOT.getRqs())) {
            return page;
        }
        List<YcDdMain> ycDdMains = baseMapper.selectOrderList(page, queryDOT);
        page.setRecords(ycDdMains);
        return page;
    }

    public List<YcDdMain> selectBookCar(String startDate, String endDate) {
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
             return new ArrayList<>(0);
        }
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.in("ddzt", EN_ROUTE);
        wrapper.ge("ycsj", startDate);
        wrapper.le("ycsj", endDate);
        return super.selectList(wrapper);
    }

    /**
     * 获取可开票列表
     *
     * @param page     分页参数
     * @param queryDOT 入参
     * @return 回参
     */
    public Page<YcDdMain> selectCanInvoicedList(Page<YcDdMain> page, CarCanOpenInvoiceListDTO queryDOT) {
        if (StringUtils.isBlank(queryDOT.getMemberId())) {
            return page;
        }
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.eq("member_id", queryDOT.getMemberId());
        wrapper.in("invoice_status", Arrays.asList("0","3"));
        wrapper.in("ddzt", Arrays.asList(YC4B.getOrderStatusCode(), YC2P.getOrderStatusCode()));
        wrapper.ge("xdsj", VeDate.strToDateLong(queryDOT.getRqs() + " 00:00:00"));
        wrapper.le("xdsj", VeDate.strToDateLong(queryDOT.getRqz() + " 23:59:59"));
        wrapper.orderBy("xdsj", false);
        return super.selectPage(page, wrapper);
    }

    /**
     * 查询会员订单
     *
     * @param ddbhs    订单编号
     * @param memberId 会员id
     * @return 回参
     */
    public List<YcDdMain> selectByDdbhsMemberId(List<String> ddbhs, String memberId) {
        if (CollectionUtils.isEmpty(ddbhs)) {
            return null;
        }
        EntityWrapper<YcDdMain> wrapper = new EntityWrapper<>();
        wrapper.in("p_ddbh", ddbhs);
        wrapper.eq("member_id", memberId);
        return baseMapper.selectList(wrapper);
    }
}