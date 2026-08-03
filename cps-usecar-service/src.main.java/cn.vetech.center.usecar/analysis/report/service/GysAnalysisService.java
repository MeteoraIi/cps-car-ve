package cn.vetech.center.usecar.analysis.report.serivce;

import cn.vetech.center.base.api.vo.VeCspzVO;
import cn.vetech.center.cdsbase.api.service.IVeCityService;
import cn.vetech.center.cdsbase.api.vo.VeCityVO;
import cn.vetech.center.usecar.analysis.enums.SjdEnum;
import cn.vetech.center.usecar.analysis.report.dto.StatisticsReportDTO;
import cn.vetech.center.usecar.analysis.report.vo.GysAnalysisReportVO;
import cn.vetech.center.usecar.analysis.report.vo.OrderStatisticsReport;
import cn.vetech.center.usecar.analysis.report.vo.RecommendGysInfo;
import cn.vetech.center.usecar.apiclient.base.IVeCsPzServiceClient;
import cn.vetech.center.usecar.entity.analysis.GysAnalysisReport;
import cn.vetech.center.usecar.entity.analysis.GysSjdDataUnit;
import cn.vetech.center.usecar.analysis.report.vo.OrderAnalysisInfo;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.service.orderes.YcDdEsService;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.number.BigDecimalUtil;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GysAnalysisService {
    /**
    * 日志工具
    */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private YcDdEsService ycDdService;

    @Autowired
    private GysSjdDataService gysSjdDataService;

    @Autowired
    private GysAnalysisReportService gysAnalysisReportService;

    @Autowired
    private IVeCsPzServiceClient veCsPzServiceClient;

    @Autowired
    private IVeCityService cityService;

        public Page<OrderStatisticsReport> findOrderStatisticsReport(PageDTO<StatisticsReportDTO> pageDTO){
        Page page = pageDTO.genPage();
        StatisticsReportDTO dto = pageDTO.getData();
        List<UsecarGysApiEnum> gysApiEnums = Lists.newArrayList(UsecarGysApiEnum.AMAP,UsecarGysApiEnum.DDYC,UsecarGysApiEnum.T3CX,UsecarGysApiEnum.CCZC,UsecarGysApiEnum.CCZCBJ,UsecarGysApiEnum.SQYC,UsecarGysApiEnum.TJ,UsecarGysApiEnum.DZCX,UsecarGysApiEnum.XDCX,UsecarGysApiEnum.RQCX,UsecarGysApiEnum.SZYC,UsecarGysApiEnum.WSECAR,UsecarGysApiEnum.BJLC);
        if(StringUtils.isNotBlank(dto.getGysbh())){
            if(CollectionUtils.isNotEmpty(dto.getGysbhs())){
                dto.getGysbhs().addAll(Lists.newArrayList(dto.getGysbh()));
            }else{
                dto.setGysbhs(Lists.newArrayList(dto.getGysbh()));
            }

        }
        if(CollectionUtils.isEmpty(dto.getGysbhs())){
            gysApiEnums = Lists.newArrayList(gysApiEnums);
        }else{
            gysApiEnums = Lists.newArrayList(gysApiEnums).stream().filter(e->dto.getGysbhs().contains(e.getShbh())).collect(Collectors.toList());
        }
        if(StringUtils.isBlank(dto.getStartTime()) || StringUtils.isBlank(dto.getEndTime())){
            LocalDate now = LocalDate.now();
            LocalDate weeks = now.minusDays(8);
            String startTime = weeks.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
        }
        List<String> list = gysSjdDataService.getReportPageData(page, dto);
        Page<OrderStatisticsReport> reportPage = new Page<>(page.getCurrent(), page.getSize());
        reportPage.setSize(page.getSize());
        reportPage.setTotal(page.getTotal());
        if(CollectionUtils.isEmpty(list)){
            return reportPage;
        }
        List<String> csids = list.stream().collect(Collectors.toList());
        dto.setCsids(csids);
        Map<String,OrderStatisticsReport> map = Maps.newHashMap();
        for (UsecarGysApiEnum gysApiEnum : gysApiEnums) {
            dto.setGysbhs(Lists.newArrayList(gysApiEnum.getShbh()));
            List<GysAnalysisReportVO> orderStatisticsReport = gysSjdDataService.findOrderStatisticsReport(dto);
            if(CollectionUtils.isNotEmpty(orderStatisticsReport)){
                Map<String, List<GysAnalysisReportVO>> csidMap = orderStatisticsReport.stream().collect(Collectors.groupingBy(GysAnalysisReportVO::getCsid));
                Set<Map.Entry<String, List<GysAnalysisReportVO>>> entries = csidMap.entrySet();
                for (Map.Entry<String, List<GysAnalysisReportVO>> entry : entries) {
                    String key = entry.getKey();
                    if(!map.containsKey(key)){
                        OrderStatisticsReport report = new OrderStatisticsReport();
                        if(StringUtils.isNotBlank(dto.getSjd())){
                            SjdEnum sjdEnum = SjdEnum.getByCode(dto.getSjd());
                            Map<String,OrderStatisticsReport.OrderReportData> sjdMap = Maps.newHashMap();
                            switch (sjdEnum){
                                case ZGF:
                                    report.setZgfMap(sjdMap);
                                    break;
                                case RPF:
                                    report.setRpfMap(sjdMap);
                                    break;
                                case WGF:
                                    report.setWgfMap(sjdMap);
                                    break;
                                case YPF:
                                    report.setYpfMap(sjdMap);
                                    break;
                            }
                        }else{
                            Map<String,OrderStatisticsReport.OrderReportData> zgfMap = Maps.newLinkedHashMap();
                            Map<String,OrderStatisticsReport.OrderReportData> rpfMap = Maps.newLinkedHashMap();
                            Map<String,OrderStatisticsReport.OrderReportData> wgfMap = Maps.newLinkedHashMap();
                            Map<String,OrderStatisticsReport.OrderReportData> ypfMap = Maps.newLinkedHashMap();
                            report.setZgfMap(zgfMap);
                            report.setRpfMap(rpfMap);
                            report.setWgfMap(wgfMap);
                            report.setYpfMap(ypfMap);
                        }
                        String mc = null;
                        RestResponse<VeCityVO> restResponse = cityService.get(key);
                        if(restResponse!=null && restResponse.getResult()!=null){
                            mc = restResponse.getResult().getMc();
                        }
                        report.setCsmc(StringUtils.defaultIfBlank(mc,key));
                        map.put(key,report);
                    }
                    OrderStatisticsReport report = map.get(key);
                    List<GysAnalysisReportVO> gysSjdDataUnits = entry.getValue();
                    if(CollectionUtils.isEmpty(gysSjdDataUnits)){
                        Map<String, OrderStatisticsReport.OrderReportData> zgfMap = report.getZgfMap();
                        Map<String, OrderStatisticsReport.OrderReportData> rpfMap = report.getRpfMap();
                        Map<String, OrderStatisticsReport.OrderReportData> wgfMap = report.getWgfMap();
                        Map<String, OrderStatisticsReport.OrderReportData> ypfMap = report.getYpfMap();
                        OrderStatisticsReport.OrderReportData data = new OrderStatisticsReport.OrderReportData();
                        data.setGysmc(gysApiEnum.getShmc());
                        if(zgfMap!=null){
                            zgfMap.put(gysApiEnum.getShbh(),data);
                        }
                        if(rpfMap!=null){
                            rpfMap.put(gysApiEnum.getShbh(),data);
                        }
                        if(wgfMap!=null){
                            wgfMap.put(gysApiEnum.getShbh(),data);
                        }
                        if(ypfMap!=null){
                            ypfMap.put(gysApiEnum.getShbh(),data);
                        }
                    }else{
                        for (GysAnalysisReportVO vo :gysSjdDataUnits) {
                            vo.setGysbh(gysApiEnum.getShbh());
                            Map<String, OrderStatisticsReport.OrderReportData> zgfMap = report.getZgfMap();
                            Map<String, OrderStatisticsReport.OrderReportData> rpfMap = report.getRpfMap();
                            Map<String, OrderStatisticsReport.OrderReportData> wgfMap = report.getWgfMap();
                            Map<String, OrderStatisticsReport.OrderReportData> ypfMap = report.getYpfMap();
                            OrderStatisticsReport.OrderReportData zgfData = getOrderReportData(gysApiEnum, vo.getXdCounta(),vo.getZjea(),vo.getZlca(),vo.getJdCounta(),vo.getJdsca());
                            OrderStatisticsReport.OrderReportData rpfData = getOrderReportData(gysApiEnum, vo.getXdCountb(),vo.getZjeb(),vo.getZlcb(),vo.getJdCountb(),vo.getJdscb());
                            OrderStatisticsReport.OrderReportData wgfData = getOrderReportData(gysApiEnum, vo.getXdCountc(),vo.getZjec(),vo.getZlcc(),vo.getJdCountc(),vo.getJdscc());
                            OrderStatisticsReport.OrderReportData ypfData = getOrderReportData(gysApiEnum, vo.getXdCountd(),vo.getZjed(),vo.getZlcd(),vo.getJdCountd(),vo.getJdscd());
                            if(zgfMap!=null){
                                zgfMap.put(gysApiEnum.getShbh(),zgfData);
                            }
                            if(rpfMap!=null){
                                rpfMap.put(gysApiEnum.getShbh(),rpfData);
                            }
                            if(wgfMap!=null){
                                wgfMap.put(gysApiEnum.getShbh(),wgfData);
                            }
                            if(ypfMap!=null){
                                ypfMap.put(gysApiEnum.getShbh(),ypfData);
                            }
                        }
                    }
                }
            }

        }
        List<OrderStatisticsReport> records = Lists.newArrayList();
        //按之前查询出的城市数排序
        for (String csbh : list) {
            records.add(map.get(csbh));
        }
        reportPage.setRecords(records);
        return reportPage;
    }

    private OrderStatisticsReport.OrderReportData getOrderReportData(UsecarGysApiEnum gysApiEnum, BigDecimal xdCount, BigDecimal zje,BigDecimal zlc,BigDecimal jdCount,BigDecimal jdsc) {
        OrderStatisticsReport.OrderReportData zgfData = new OrderStatisticsReport.OrderReportData();
        zgfData.setGysmc(gysApiEnum.getShmc());
        zgfData.setXdCount(xdCount);
        zgfData.setLcdj(BigDecimalUtil.divide(zje,zlc,2));
        zgfData.setJdCount(jdCount);
        BigDecimal jdzb = BigDecimalUtil.divide(jdCount, xdCount, 2);
        if(jdzb!=null && !BigDecimalUtil.isEqualZero(jdzb)){
            zgfData.setJdzb(jdzb!=null?BigDecimalUtil.multiply(jdzb,BigDecimal.valueOf(100)).toPlainString()+"%":null);
        }
        zgfData.setPjpcsc(BigDecimalUtil.divide(jdsc,jdCount));
        return zgfData;
    }

    private void createReport(){
        UsecarGysApiEnum[] gysApiEnums = UsecarGysApiEnum.values();
        LocalDate now = LocalDate.now();
        LocalDate weeks = now.minusDays(8);
        String startTime = weeks.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        for (UsecarGysApiEnum gysApiEnum:gysApiEnums) {
            long t1 = System.currentTimeMillis();
            logger.info("开始统计供应商{}最近一个星期的数据",gysApiEnum.getShbh());
            List<GysSjdDataUnit> gysSjdDataUnits = gysSjdDataService.findReport(gysApiEnum.getShbh(), startTime, endTime);
            if(CollectionUtils.isNotEmpty(gysSjdDataUnits)){
                logger.info("查询供应商数据条数={}",gysSjdDataUnits.size());
                Map<String, List<GysSjdDataUnit>> map = gysSjdDataUnits.stream().collect(Collectors.groupingBy(GysSjdDataUnit::getCsid));
                Set<Map.Entry<String, List<GysSjdDataUnit>>> entries = map.entrySet();
                for (Map.Entry<String, List<GysSjdDataUnit>> entry : entries) {
                    GysAnalysisReport report = getGysAnalysisReport(endTime, gysApiEnum, entry);
                    insertReport(report);
                }
            }
            long t2 = System.currentTimeMillis();
            logger.info("统计供应商{}最近七天分析数据耗时={}ms",gysApiEnum.getShbh(),t2-t1);
        }
    }

    private GysAnalysisReport getGysAnalysisReport(String endTime, UsecarGysApiEnum gysApiEnum, Map.Entry<String, List<GysSjdDataUnit>> entry) {
        GysAnalysisReport report = new GysAnalysisReport();
        report.setGysbh(gysApiEnum.getShbh());
        report.setId(IdGenerator.getHexId());
        report.setCsid(entry.getKey());
        report.setTjrq(VeDate.formatToDate(endTime,"yyyy-MM-dd"));
        for (GysSjdDataUnit gysSjdDataUnit : entry.getValue()) {
            String sjd = gysSjdDataUnit.getSjd();
            SjdEnum sjdEnum = SjdEnum.getByCode(sjd);
            switch (sjdEnum){
                case ZGF:
                    report.setJdCounta(gysSjdDataUnit.getJdCount());
                    report.setXdCounta(gysSjdDataUnit.getXdCount());
                    report.setWcdCounta(gysSjdDataUnit.getWcdCount());
                    report.setZlca(gysSjdDataUnit.getZlc());
                    report.setZjea(gysSjdDataUnit.getZje());
                    report.setCkqxCounta(gysSjdDataUnit.getCkqxCount());
                    report.setJdsca(gysSjdDataUnit.getJdsc());
                    break;
                case RPF:
                    report.setJdCountb(gysSjdDataUnit.getJdCount());
                    report.setXdCountb(gysSjdDataUnit.getXdCount());
                    report.setWcdCountb(gysSjdDataUnit.getWcdCount());
                    report.setZlcb(gysSjdDataUnit.getZlc());
                    report.setZjeb(gysSjdDataUnit.getZje());
                    report.setCkqxCountb(gysSjdDataUnit.getCkqxCount());
                    report.setJdscb(gysSjdDataUnit.getJdsc());
                    break;
                case WGF:
                    report.setJdCountc(gysSjdDataUnit.getJdCount());
                    report.setXdCountc(gysSjdDataUnit.getXdCount());
                    report.setWcdCountc(gysSjdDataUnit.getWcdCount());
                    report.setZlcc(gysSjdDataUnit.getZlc());
                    report.setZjec(gysSjdDataUnit.getZje());
                    report.setCkqxCountc(gysSjdDataUnit.getCkqxCount());
                    report.setJdscc(gysSjdDataUnit.getJdsc());
                    break;
                case YPF:
                    report.setJdCountd(gysSjdDataUnit.getJdCount());
                    report.setXdCountd(gysSjdDataUnit.getXdCount());
                    report.setWcdCountd(gysSjdDataUnit.getWcdCount());
                    report.setZlcd(gysSjdDataUnit.getZlc());
                    report.setZjed(gysSjdDataUnit.getZje());
                    report.setCkqxCountd(gysSjdDataUnit.getCkqxCount());
                    report.setJdscd(gysSjdDataUnit.getJdsc());
                    break;
            }
        }
        return report;
    }

    private synchronized void insertReport(GysAnalysisReport report){
        try {
            GysAnalysisReport old = gysAnalysisReportService.findByGysAndRq(report.getGysbh(), report.getTjrq(),report.getCsid());
            if(old==null){
                gysAnalysisReportService.insertGysAnalysisReport(report);
            }else{
                report.setId(old.getId());
                logger.info("供应商={}时间={}的分析数据已存在",report.getGysbh(),report.getTjrq());
                gysAnalysisReportService.updateReport(report);
            }
        }catch (Exception e){
            logger.error("插入或更新数据异常",e);
        }
    }

    public void analysis(String tjrq){
        logger.info("统计任务开始");
        long t0 = System.currentTimeMillis();
        if(StringUtils.isBlank(tjrq)){
            LocalDate yesterday = LocalDate.now().minusDays(2);
            tjrq = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        List<UsecarGysApiEnum> gysApiEnums = Lists.newArrayList(UsecarGysApiEnum.AMAP,UsecarGysApiEnum.DDYC,UsecarGysApiEnum.T3CX,UsecarGysApiEnum.CCZC,UsecarGysApiEnum.CCZCBJ,UsecarGysApiEnum.SQYC,UsecarGysApiEnum.TJ,UsecarGysApiEnum.DZCX,UsecarGysApiEnum.XDCX,UsecarGysApiEnum.RQCX,UsecarGysApiEnum.SZYC,UsecarGysApiEnum.WSECAR,UsecarGysApiEnum.BJLC);
        //将固定供应商排在前面
        for (UsecarGysApiEnum usecarGysApiEnum : gysApiEnums) {
            gysSjdDataService.deleteByTjrqAndGysbh(tjrq,usecarGysApiEnum.getShbh());
            long t1 = System.currentTimeMillis();
            SjdEnum[] sjdEnums = SjdEnum.values();
            for (SjdEnum sjdEnum : sjdEnums) {
                List<String> se = getDate(tjrq, sjdEnum);
                List<OrderAnalysisInfo> orderAnalysisInfos = ycDdService.selectOrderAnalysisInfos(usecarGysApiEnum.getShbh(), se.get(0), se.get(1));
                //处理里程和时长单位
                if(CollectionUtils.isNotEmpty(orderAnalysisInfos)){
                    orderAnalysisInfos.forEach(e->{
                        e.setBdlc(BigDecimalUtil.divide(new BigDecimal(StringUtils.defaultIfBlank(e.getBdlc(),"0")),BigDecimal.valueOf(1000),0).toPlainString());
                    });
                }
                dealwith(orderAnalysisInfos,sjdEnum,tjrq,usecarGysApiEnum.getShbh());
            }
            long t2 = System.currentTimeMillis();
            logger.info("供应商{}订单统计数据耗时={}ms",usecarGysApiEnum.getShbh(),t2-t1);
        }
        //生成报表
        createReport();
        long t3 = System.currentTimeMillis();
        logger.info("统计任务结束，耗时={}ms",t3-t0);
    }

    private void dealwith(List<OrderAnalysisInfo> orderAnalysisInfos,SjdEnum sjdEnum,String date,String gysbh){
        if(CollectionUtils.isNotEmpty(orderAnalysisInfos)){
            //按城市分组
            Map<String, List<OrderAnalysisInfo>> groupBycs = orderAnalysisInfos.stream().collect(Collectors.groupingBy(OrderAnalysisInfo::getCfdCsid));
            Set<Map.Entry<String, List<OrderAnalysisInfo>>> groupBycsEntrys = groupBycs.entrySet();

            List<GysSjdDataUnit> units = Lists.newArrayList();
            for (Map.Entry<String, List<OrderAnalysisInfo>> groupBycsEntry : groupBycsEntrys) {
                String csid = groupBycsEntry.getKey();
                List<OrderAnalysisInfo> orderList = groupBycsEntry.getValue();
                if(CollectionUtils.isEmpty(orderList)){
                    continue;
                }
                //按采购订单分组
                Map<String, List<OrderAnalysisInfo>> groupBycgs = orderList.stream().collect(Collectors.groupingBy(e -> {
                    String cgDdbh = e.getCgDdbh();
                    if (StringUtils.isNotBlank(cgDdbh) &&(cgDdbh.startsWith("YC") || cgDdbh.startsWith("ZD"))) {
                        cgDdbh = cgDdbh.substring(0, cgDdbh.length() - 2);
                    }
                    return cgDdbh;
                }));
                Set<String> cgsDdbhs = groupBycgs.keySet();
                Collection<List<OrderAnalysisInfo>> subOrders = groupBycgs.values();
                GysSjdDataUnit gysSjdDataUnit = new GysSjdDataUnit();
                gysSjdDataUnit.setId(IdGenerator.getHexId());
                gysSjdDataUnit.setSjd(sjdEnum.getCode());
                gysSjdDataUnit.setTjrq(VeDate.formatToDate(date,"yyyy-MM-dd"));
                gysSjdDataUnit.setGysbh(gysbh);
                gysSjdDataUnit.setCsid(csid);
                gysSjdDataUnit.setXdCount(BigDecimal.valueOf(cgsDdbhs.size()));
                //同批次下单的子单
                int jdCount = 0;
                int wcdCount = 0;
                int gyjdCount = 0;
                int ckqxCount = 0;
                int jdsc = 0;
                int size = subOrders.size();
                BigDecimal zlc = BigDecimal.ZERO;
                BigDecimal zje = BigDecimal.ZERO;
                for (List<OrderAnalysisInfo> subOrderList : subOrders) {
                    Optional<OrderAnalysisInfo> opt1 = subOrderList.stream().filter(e -> e.getGyPcsj() != null).findFirst();
                    boolean isPc = opt1.isPresent();
                    if(isPc){
                        jdCount++;
                        OrderAnalysisInfo orderAnalysisInfo = opt1.get();
                        Date gyPcsj = orderAnalysisInfo.getGyPcsj();
                        Date xdsj = orderAnalysisInfo.getXdsj();
                        if(gyPcsj!=null && xdsj!=null){
                            int twoSec = VeDate.getTwoSec(gyPcsj, xdsj);
                            jdsc+=twoSec;
                        }

                    }
                    Optional<OrderAnalysisInfo> opt = subOrderList.stream().filter(e -> Lists.newArrayList("YC4A", "YC4B", "YC4C").contains(e.getDdzt())).findFirst();
                    boolean isFinish = opt.isPresent();
                    if(isFinish){
                        wcdCount++;
                        OrderAnalysisInfo orderAnalysisInfo = opt.get();
                        String bdlc = orderAnalysisInfo.getBdlc();
                        BigDecimal gyJsje = orderAnalysisInfo.getGyJsje();
                        zlc = BigDecimalUtil.add(zlc,new BigDecimal(StringUtils.defaultIfBlank(bdlc,"0")));
                        zje = BigDecimalUtil.add(zje,gyJsje);
                    }
                    boolean isGyjd = subOrderList.stream().filter(e -> StringUtils.isNotBlank(e.getGyJudyy())).collect(Collectors.toList()).size()==size;
                    if(isGyjd){
                        gyjdCount++;
                    }
                    boolean isCkqx = subOrderList.stream().filter(e ->  StringUtils.equals(e.getDdzt(),"YC1E")).collect(Collectors.toList()).size()==size;
                    if(isCkqx){
                        ckqxCount++;
                    }
                }
                gysSjdDataUnit.setJdCount(BigDecimal.valueOf(jdCount));
                gysSjdDataUnit.setWcdCount(BigDecimal.valueOf(wcdCount));
                gysSjdDataUnit.setGyjdCount(BigDecimal.valueOf(gyjdCount));
                gysSjdDataUnit.setCkqxCount(BigDecimal.valueOf(ckqxCount));
                gysSjdDataUnit.setZlc(zlc);
                gysSjdDataUnit.setZje(zje);
                gysSjdDataUnit.setJdsc(BigDecimal.valueOf(jdsc));
                units.add(gysSjdDataUnit);
            }
            Set<String> keys = Sets.newHashSet();
            //去重
            Iterator<GysSjdDataUnit> it = units.iterator();
            while (it.hasNext()){
                GysSjdDataUnit one = it.next();
                String wyz =  one.getCsid()+"-"+one.getSjd()+"-"+one.getGysbh()+"-"+one.getTjrq();
                if(keys.contains(wyz)){
                    it.remove();
                }else{
                    keys.add(wyz);
                }
            }
            //保存数据
            gysSjdDataService.insert(units);
        }
        return;
    }



    private List<String> getDate(String date,SjdEnum sjdEnum){
        List<String> list = Lists.newArrayList();
        switch (sjdEnum){
            case ZGF:
                list.add(date+" 07:00:00");
                list.add(date+" 08:59:59");
                break;
            case RPF:
                list.add(date+" 09:00:00");
                list.add(date+" 16:59:59");
                break;
            case WGF:
                list.add(date+" 17:00:00");
                list.add(date+" 19:59:59");
                break;
            case YPF:
                list.add(date+" 20:00:00");
                String format = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                list.add(format+" 07:00:00");
                break;
        }
        return list;
    }

    public List<RecommendGysInfo> getRecommendGysInfo(Collection<String> gysbhs,String ycsj,String csid){
        try{
            List<String> filters = Lists.newArrayList("DDYC","CCZC","CCZCBJ","T3CX","WSECAR","HQZC","SZYC","SQYC");
            if(CollectionUtils.isEmpty(gysbhs)){
                return Lists.newArrayList();
            }
            gysbhs.retainAll(filters);
            if(CollectionUtils.isEmpty(gysbhs)){
                return Lists.newArrayList();
            }
            String factor = null;
            long t0 = System.currentTimeMillis();
            RestResponse<VeCspzVO> response = veCsPzServiceClient.getVeCspzByBh("1000YC200");
            long t1 = System.currentTimeMillis();
            logger.info("查询供应商推荐配置耗时={}ms",t1-t0);
            if(response!=null && response.getResult()!=null){
                VeCspzVO result = response.getResult();
                if(StringUtils.equals(result.getYdhm(),"jdl:lcdj")){
                    factor = result.getYw();
                }
            }
            if(StringUtils.isBlank(factor)){
                logger.info("未配置供应商推荐分数计算比");
                return Lists.newArrayList();
            }
            if(StringUtils.isBlank(ycsj)){
                ycsj = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            SjdEnum sjdEnum = SjdEnum.getSjdEnumByTime(ycsj);
            LocalDate now = LocalDate.now();
            String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String yesterday = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long t2 = System.currentTimeMillis();
            List<GysAnalysisReport> list = gysAnalysisReportService.findByGysbhs(gysbhs,today,csid);
            if(CollectionUtils.isEmpty(list)){
                list = gysAnalysisReportService.findByGysbhs(gysbhs, yesterday,csid);
            }
            long t3 = System.currentTimeMillis();
            logger.info("查询推荐供应商原始数据耗时={}ms",t3-t2);
            if(CollectionUtils.isNotEmpty(list)){
                List<RecommendGysInfo> recommendGysInfos = Lists.newArrayList();
                for (GysAnalysisReport gysAnalysisReport : list) {
                    RecommendGysInfo recommendGysInfo = new RecommendGysInfo();
                    recommendGysInfos.add(recommendGysInfo);
                    recommendGysInfo.setFactor(factor);
                    recommendGysInfo.setGysbh(gysAnalysisReport.getGysbh());
                    recommendGysInfo.setGysmc(UsecarGysApiEnum.getShmc(gysAnalysisReport.getGysbh()));
                    recommendGysInfo.setCsid(gysAnalysisReport.getCsid());
                    BigDecimal jdCount = null;
                    BigDecimal wcdCount = null;
                    BigDecimal zje = null;
                    BigDecimal zlc = null;
                    BigDecimal jdsc = null;
                    BigDecimal xdCount = null;
                    switch (sjdEnum){
                        case ZGF:
                            xdCount = gysAnalysisReport.getXdCounta();
                            jdCount = gysAnalysisReport.getJdCounta();
                            wcdCount = gysAnalysisReport.getWcdCounta();
                            zje = gysAnalysisReport.getZjea();
                            zlc = gysAnalysisReport.getZlca();
                            jdsc = gysAnalysisReport.getJdsca();
                            break;
                        case RPF:
                            xdCount = gysAnalysisReport.getXdCountb();
                            jdCount = gysAnalysisReport.getJdCountb();
                            wcdCount = gysAnalysisReport.getWcdCountb();
                            zje = gysAnalysisReport.getZjeb();
                            zlc = gysAnalysisReport.getZlcb();
                            jdsc = gysAnalysisReport.getJdscb();
                            break;
                        case WGF:
                            xdCount = gysAnalysisReport.getXdCountc();
                            jdCount = gysAnalysisReport.getJdCountc();
                            wcdCount = gysAnalysisReport.getWcdCountc();
                            zje = gysAnalysisReport.getZjec();
                            zlc = gysAnalysisReport.getZlcc();
                            jdsc = gysAnalysisReport.getJdscc();
                            break;
                        case YPF:
                            xdCount = gysAnalysisReport.getXdCountd();
                            jdCount = gysAnalysisReport.getJdCountd();
                            wcdCount = gysAnalysisReport.getWcdCountd();
                            zje = gysAnalysisReport.getZjed();
                            zlc = gysAnalysisReport.getZlcd();
                            jdsc = gysAnalysisReport.getJdscd();
                            break;
                    }

                    if(jdsc!=null && jdCount !=null){
                        BigDecimal divide = BigDecimalUtil.divide(jdsc, jdCount, 2);
                        recommendGysInfo.setJdsc(divide!=null?divide.toPlainString():"0");
                    }
                    if(zje!=null && zlc!=null){
                        BigDecimal divide = BigDecimalUtil.divide(zje, zlc, 2);
                        recommendGysInfo.setLcdj(divide!=null?divide.toPlainString():"0");
                    }
                    recommendGysInfo.setWcdCount(wcdCount!=null?wcdCount.toPlainString():"0");
                    recommendGysInfo.setXdCount(xdCount!=null?xdCount.toPlainString():"0");
                    recommendGysInfo.setJdCount(jdCount!=null?jdCount.toPlainString():"0");
                }
                //计算得分
                calcScore(recommendGysInfos,factor);
                long t4 = System.currentTimeMillis();
                logger.info("获取推荐供应商耗时={}ms",t4-t3);
                return recommendGysInfos;
            }
        }catch (Exception e){
            logger.error("获取推荐供应商数据异常",e);
        }
        return Lists.newArrayList();
    }

    /**
     *
     * @param recommendGysInfos
     * @param factor
     */
    private void calcScore(List<RecommendGysInfo> recommendGysInfos,String factor){
        try {
            if(StringUtils.isBlank(factor)){
                logger.error("得分权重配置为空");
                return;
            }else{
                String regx = "[1-9]{1,2}:[1-9]{1,2}(:[1-9]{1,2})*";
                boolean matches = Pattern.matches(regx, factor);
                if(!matches){
                    logger.error("得分权重配置不正确");
                    return;
                }
            }
            String[] split = factor.split(":");
            BigDecimal x1 = new BigDecimal(split[0]);
            BigDecimal x2 = new BigDecimal(split[1]);
            BigDecimal zb1 = BigDecimalUtil.divide(x1, BigDecimalUtil.add(x1, x2), 2);
            BigDecimal zb2 = BigDecimalUtil.divide(x2, BigDecimalUtil.add(x1, x2), 2);
            List<BigDecimal> lcdjList = recommendGysInfos.stream().map(e -> new BigDecimal(StringUtils.defaultIfBlank(e.getLcdj(),"0"))).filter(e -> BigDecimalUtil.isGreaterThanZero(e) && BigDecimalUtil.isLessThan(e, BigDecimal.valueOf(15))).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(lcdjList)){
                BigDecimal max = lcdjList.stream().max(BigDecimal::compareTo).get();
                BigDecimal min = lcdjList.stream().min(BigDecimal::compareTo).get();
                BigDecimal b = BigDecimalUtil.subtract(max, min);
                for (RecommendGysInfo recommendGysInfo : recommendGysInfos) {
                    BigDecimal lcdj = new BigDecimal(StringUtils.defaultIfBlank(recommendGysInfo.getLcdj(),"0"));
                    BigDecimal lcdjScore = BigDecimal.ZERO;
                    if(BigDecimalUtil.isGreaterThanZero(lcdj)){
                        BigDecimal a = BigDecimalUtil.subtract(lcdj, min);
                        lcdjScore = BigDecimalUtil.subtract(BigDecimal.ONE,BigDecimalUtil.divide(a,b,3));
                    }
                    BigDecimal xdCount = new BigDecimal(StringUtils.defaultIfBlank(recommendGysInfo.getXdCount(),"0"));
                    BigDecimal jdCount = new BigDecimal(StringUtils.defaultIfBlank(recommendGysInfo.getJdCount(),"0"));
                    BigDecimal jdlScore = BigDecimalUtil.divide(jdCount, xdCount, 2);
                    BigDecimal y1 = BigDecimalUtil.multiply(jdlScore, zb1).setScale(3, RoundingMode.HALF_UP);
                    BigDecimal y2 = BigDecimalUtil.multiply(lcdjScore, zb2).setScale(3, RoundingMode.HALF_UP);
                    recommendGysInfo.setScore(BigDecimalUtil.multiply(BigDecimalUtil.add(y1,y2),BigDecimal.valueOf(1000)).toPlainString());
                }
            }else{
                logger.error("合法的里程单价集合为空，不能计算得分!!!!!");
            }
            //按分数排序
            recommendGysInfos.sort(((o1, o2) -> {
                BigDecimal score1 = new BigDecimal(StringUtils.defaultIfBlank(o1.getScore(),"0"));
                BigDecimal score2 = new BigDecimal(StringUtils.defaultIfBlank(o2.getScore(),"0"));
                return score2.compareTo(score1);
            }));
        }catch (Exception e){
            logger.error("计算得分异常",e);
        }
    }

}