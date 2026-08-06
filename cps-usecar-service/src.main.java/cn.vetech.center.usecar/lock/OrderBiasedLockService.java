package cn.vetech.center.usecar.lock;

import cn.vetech.center.car.vo.SupplierConfigMxVO;
import cn.vetech.center.car.vo.SupplierConfigsVO;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.entity.usecar.YcDdMain;
import cn.vetech.center.usecar.service.order.YcDdService;
import cn.vetech.center.usecar.service.orderes.OrderNewEsVO;
import cn.vetech.center.usecar.service.orderes.YcDdEsV2Service;
import cn.vetech.center.usecar.service.usecar.YcDdMainService;
import cn.vetech.center.usecar.setting.suppilerconfig.service.SuppilerConfigService;
import cn.vetech.charge.cloud.modules.utils.mapper.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderBiasedLockService {
    /**
     * 日志工具
     */
    private Logger logger = LoggerFactory.getLogger(OrderBiasedLockService.class);

    private final static String NAME_PREFIX = "CAR_DD_BIASED_LOCK_";

    @Autowired
    private YcDdService ycDdService;

    @Autowired
    private RedisLock redisLock;
    /**
     * 供应商配置信息
     */
    @Autowired
    private SuppilerConfigService suppilerConfigService;

    @Autowired
    private YcDdEsV2Service ycDdEsV2Service;

    @Autowired
    private YcDdMainService ycDdMainService;

    @Autowired
    private RedisCacheManage redisCacheManage;

    public static final String HANDS_UP_PERFIX = "HANDS_UP_PERFIX_";

    public void getBiasedLock(String ddbh,String ddzt){
        try {
            if(!StringUtils.equalsAny(ddzt,"YC2D","YC2G")){
                //不是已派车，和司机已出发则不获取偏向锁
                return;
            }
            YcDd ycDd = ycDdService.selectYcDd(ddbh);
            String name = getName(ycDd);
            if(StringUtils.isBlank(name) || StringUtils.isBlank(ycDd.getGyShbh())){
                return;
            }
            YcDdMain ycDdMain = ycDdMainService.selectById(ycDd.getpDdbh());
            String newOrder = ycDdMain.getNewOrder();
            if(!StringUtils.equals(newOrder,"1")){
                if(!StringUtils.startsWith(ycDd.getCgDdbh(),"YC") || ycDd.getCgDdbh().length()<=6){
                    logger.warn("采购商非费控!");
                    return;
                }
                List<OrderNewEsVO> ycDds = ycDdEsV2Service.getOrderByCgddbh(ycDd.getCgDdbh());
                for (OrderNewEsVO one : ycDds) {
                    if(StringUtils.equalsAny(one.getDdzt(),"YC2D","YC2G","YC2H","YC2E")
                            || (StringUtils.isNotBlank(one.getCph()) && StringUtils.equals("YC2M",one.getDdzt()))){
                        logger.info("订单={}已派车，不需要获取偏向锁了",one.getDdbh(),one.getGyDdbh());
                        return;
                    }
                }
                return;
            }else{
                if(!StringUtils.equals(ycDdMain.getDdzt(),"YC1H")){
                    logger.info("订单={}对应主单不是待派车={}，不需要获取偏向锁了",ddbh,ycDdMain.getGyDdbh());
                   return;
                }
            }
            Pair<String, Long> pair = getTime(ycDd.getCgDdbh());
            if(pair==null){
                return;
            }
            Long biasedTime = pair.getBiasedTime();
            logger.info("获取到偏向时间={}",biasedTime);
            if(biasedTime==null || biasedTime == 0L){
                return;
            }
            if(biasedTime > 9L){
                biasedTime = 8L;
            }
            String gysbh = pair.getGysbh();
            if(!StringUtils.equals(gysbh,ycDd.getGyShbh())){
                long s1 = System.currentTimeMillis();
                logger.info("供应商{}开启了偏向锁设置，主动给该供应商设置偏向锁={}",gysbh,name);
                boolean b = redisLock.tryBiasedLock(name, gysbh, biasedTime);
                long s2 = System.currentTimeMillis();
                logger.info("订单={},{}获取偏向锁:{},耗时={}ms",ddbh,gysbh,b,s2-s1);
            }
            logger.info("订单{},{}尝试获取偏向锁={}",ddbh,ycDd.getGyShbh(),name);
            long s1 = System.currentTimeMillis();
            boolean b = redisLock.tryBiasedLock(name, ycDd.getGyShbh(), biasedTime);
            long s2 = System.currentTimeMillis();
            logger.info("订单={},{}获取偏向锁:{},耗时={}ms",ddbh,ycDd.getGyShbh(),b,s2-s1);
        }catch (Exception e){
            logger.error("订单={}获取偏向锁异常",ddbh,e);
            releaseBiasedLock(ddbh);
        }

    }

    /**
     *  尝试释放订单对应供应商的偏向锁
     * @param ddbh
     * @param ddzt
     */
    public void releaseBiasedLock(String ddbh,String ddzt){
        //已派车和司机已出发 则尝试释放偏向锁
        if(StringUtils.equalsAny(ddzt,"YC2D","YC2G","YC2E")){
            releaseBiasedLock(ddbh);
        }
    }

    private void releaseBiasedLock(String ddbh){
        try {
            YcDd ycDd = ycDdService.selectYcDd(ddbh);
            String name = getName(ycDd);
            if(StringUtils.isBlank(name) || StringUtils.isBlank(ycDd.getGyShbh())){
                return;
            }
            logger.info("订单{},{}尝试释放偏向锁={}",ddbh,ycDd.getGyShbh());
            long s1 = System.currentTimeMillis();
            Object o = redisLock.releaseBiasedLock(name, ycDd.getGyShbh());
            long s2 = System.currentTimeMillis();
            logger.info("订单{},{}尝试释放偏向锁耗时={}",ddbh,ycDd.getGyShbh(),s2-s1);
            if((Long)o == 2L){
                logger.info("订单{},{}不持有偏向锁",ddbh,ycDd.getGyShbh());
            }
            if((Long)o == 1L){
                logger.info("订单{},{}释放偏向锁成功",ddbh,ycDd.getGyShbh());
            }
            if((Long)o == 0L){
                logger.info("订单{},{}释放偏向锁失败",ddbh,ycDd.getGyShbh());
            }
        }catch (Exception e){
            logger.error("订单={}释放偏向锁异常",ddbh,e);
        }

    }

    private Pair<String,Long> getTime(String cgddbh){
        List<OrderNewEsVO> list = ycDdEsV2Service.getOrderByCgddbh(cgddbh);
        if (list == null) return null;
        String biasedTime = null;
        String gyshbh = null;
        logger.info("获取订单供应商的偏向设置={}", JsonMapper.nonEmptyMapper().toJson(list));
        for (OrderNewEsVO orderNewEsVO : list) {
            String handsUpConfirmWaitTime = getHandsUpConfirmWaitTime(orderNewEsVO);
            if(StringUtils.isNotBlank(handsUpConfirmWaitTime)){
                logger.info("订单{}司机举手，优先获取偏向锁,gysbh={}",orderNewEsVO.getDdbh(),orderNewEsVO.getGyShbh());
                biasedTime = handsUpConfirmWaitTime;
                gyshbh = orderNewEsVO.getGyShbh();
                break;
            }else{
                biasedTime = getBiasedTimeConfig(orderNewEsVO);
                if(StringUtils.isNotBlank(biasedTime)){
                    gyshbh = orderNewEsVO.getGyShbh();
                    break;
                }
            }
        }
        if(biasedTime==null){
            return null;
        }
        String regex = "^\\d+$";
        boolean matches = biasedTime.matches(regex);
        if(matches){
            return new Pair<>(gyshbh,Long.parseLong(biasedTime));
        }
        return null;
    }

    private String getHandsUpConfirmWaitTime(OrderNewEsVO orderNewEsVO){
        String ddbh = orderNewEsVO.getDdbh();
        String o = (String)redisCacheManage.get(HANDS_UP_PERFIX, ddbh);
        if(StringUtils.isNotBlank(o)){
            return o;
        }
        return null;
    }

    private String getBiasedTimeConfig(OrderNewEsVO orderNewEsVO) {
        String biasedTime;
        SupplierConfigsVO vo = suppilerConfigService.selectOneGyjkxx(orderNewEsVO.getGyShbh(), "1");
        if (vo != null && CollectionUtils.isNotEmpty(vo.getSupplierConfigMxVOList())) {
            for (SupplierConfigMxVO v : vo.getSupplierConfigMxVOList()) {
                if(StringUtils.equals(v.getSxm(),"biasedTime")){
                    biasedTime = v.getSxz();
                    if(StringUtils.isNotBlank(biasedTime)){
                        return biasedTime;
                    }
                }
            }
        }
        return null;
    }
    private String getName(YcDd ycDd){
        if(ycDd==null){
            return null;
        }
        String cgDdbh = ycDd.getCgDdbh();

        if(StringUtils.isBlank(cgDdbh)){
            return null;
        }

        if(!StringUtils.startsWith(cgDdbh,"YC")){
            return null;
        }
        if(cgDdbh.length()<6){
            return null;
        }
        String substring = null;
        if(StringUtils.isNotBlank(ycDd.getZbddbh())){
            substring = ycDd.getZbddbh();
        }else{
            substring = getCgDdbh(cgDdbh);
        }
        return NAME_PREFIX+substring;
    }

    private String getCgDdbh(String cgDdbh){
        return cgDdbh.substring(0, cgDdbh.length() - 2);
    }


    private class Pair<String,Long>{

        private String gysbh;

        private Long biasedTime;

        public Pair(String gysbh, Long biasedTime) {
            this.gysbh = gysbh;
            this.biasedTime = biasedTime;
        }

        public String getGysbh() {
            return gysbh;
        }

        public Long getBiasedTime() {
            return biasedTime;
        }
    }
}