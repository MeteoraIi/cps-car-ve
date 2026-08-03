package cn.vetech.center.usecar.book.buyer.specicar.service;

import cn.vetech.center.usecar.book.buyer.specicar.dto.BookSpeciCalCachePrice;
import cn.vetech.center.usecar.book.buyer.specicar.vo.BookSpeciCarProductModelVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.enums.UsecarGysApiEnum;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.service.UsecarCacheService;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.sequence.IdGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专快车用车车产品查询时将价格信息放入缓存，用户在下单时从缓存中取价格信息计算
 *
 * @author houshuang
 * @since 2017-11-03
 */
@Service
public class BuyerBookSpeciCarCacheService {
    /**
     * 日志记录类
     */
    private static Logger logger = LoggerFactory.getLogger(BuyerBookSpeciCarCacheService.class);

    /**
     * 用车价格缓存
     */
    @Autowired
    private UsecarCacheService usecarCacheService;

    /***
     * 用车专快车公共service
     */
    @Autowired
    private BuyerBookSpeciCarCommonService buyerBookSpeciCarCommonService;

    /**
     * @param list 缓存对象集合
     * @throws Exception 缓存异常
     * @Title CPS专快车价格缓存 查询一次缓存一次
     * @author houshuang
     * @Date 2017年9月14日10:58:33
     */
    public void priceCaCheBc(List<BookSpeciCarProductModelVO> list,String cursorId,String asyn) throws Exception {
        logger.warn("进入缓存方法,总条数:" + list.size());
        long start = System.currentTimeMillis();
        if (CollectionUtil.isNotEmpty(list)) {
            Type<BookSpeciCarProductModelVO> carProductModelVOType = BeanMapper.getType(BookSpeciCarProductModelVO.class);
            Type<BookSpeciCalCachePrice> carPriceCacheType = BeanMapper.getType(BookSpeciCalCachePrice.class);
            String mapkey = IdGenerator.getHexId();
            if(StringUtils.equals(asyn, UseCarConstant.YES)){
                if(StringUtils.isBlank(cursorId)){
                    logger.error("异步加载查询游标id为空");
                    throw new SystemRuntimeException(UsecarOrderCode.UCAR_30008,"专快车产品价格缓存异常");
                }
                mapkey = cursorId;
            }
            Map<String, Object> map = new HashMap<>();
            for (BookSpeciCarProductModelVO bcprice : list) {
                BookSpeciCalCachePrice pricecache = null;
                try {
                    String keyMx = bcprice.getPriceCacheId();
                    String key = mapkey + "-" + keyMx;
                    //快速对象拷贝
                    pricecache = BeanMapper.map(bcprice, carProductModelVOType, carPriceCacheType);
                    pricecache.setPriceCacheId(keyMx);
                    /** 默认为10分钟有效期 **/
                    map.put(keyMx, pricecache);
                    bcprice.setPriceCacheId(key);
                } catch (Exception e) {
                    logger.error("包车产品价格缓存失败", e);
                    throw new SystemRuntimeException(UsecarOrderCode.UCAR_30008, "专快车产品价格缓存异常");
                }
            }
            usecarCacheService.putProductQueryCache(mapkey, map);
            logger.warn("缓存耗时{}毫秒",System.currentTimeMillis()-start);
        }

    }


    /**
     * 获取用车专快车产品缓存
     *
     * @param priceCaCheId 缓存ID
     * @param ycDd         订单入库对象
     */
    public void getPriceCaCheYc(String priceCaCheId, YcDd ycDd) {
        if (StringUtils.isBlank(priceCaCheId)) {
            return;
        }
        logger.info("进入价格缓存,缓存ID：" + priceCaCheId);
        BookSpeciCalCachePrice carcache = (BookSpeciCalCachePrice) usecarCacheService.getProductQueryCache(priceCaCheId);
        if (carcache == null) {
            return;
        }
        if (StringUtils.isBlank(carcache.getCfd())) {
            throw new RuntimeException("出发地POI不能为空");
        }
        if (StringUtils.isBlank(carcache.getMdd())) {
            throw new RuntimeException("目的地的POI不能为空");
        }
        if (StringUtils.isNotBlank(carcache.getYcsj())) {
            ycDd.setYcsj(carcache.getYcsj().trim());//用车时间
        }
        ycDd.setCfdCsid(carcache.getCfdCsid());
        ycDd.setMddCsid(carcache.getMmdCsid());
        ycDd.setCfdCsmc(carcache.getCfdCsmc());
        ycDd.setMddCsmc(carcache.getMddCsmc());
        ycDd.setCxzbh(carcache.getCxzbh());
        ycDd.setCxzmc(carcache.getCxzmc());
        ycDd.setFwnr(carcache.getFwnr());//服务内容
        ycDd.setBdlc(carcache.getBdlc());//本单里程
        ycDd.setBdsc(carcache.getBdsc());//本单时长
        ycDd.setQxgs(carcache.getQxgs());//取消公式
        ycDd.setActualSupplierNo(carcache.getActualSupplierNo());
        ycDd.setGyShbh(carcache.getGysbh());//供应商户编号
        ycDd.setGyShjc(UsecarGysApiEnum.getShmc(carcache.getGysbh()));//供应商户简称
        ycDd.setPtkrfs(carcache.getPtkrfs());//平台控润方式
        ycDd.setPtkrbl(carcache.getPtkrbl());
        ycDd.setPtkrje(carcache.getPtkrje());//平台控润金额
        ycDd.setCfd(carcache.getCfd());
        if (StringUtils.isBlank(carcache.getCfdXxdz())) {
            ycDd.setCfdXxdz(carcache.getCfd());
        } else {
            ycDd.setCfdXxdz(carcache.getCfdXxdz());
        }
        ycDd.setMdd(carcache.getMdd());
        if (StringUtils.isBlank(carcache.getMddXxdz())) {
            ycDd.setMddXxdz(carcache.getMdd());
        } else {
            ycDd.setMddXxdz(carcache.getMddXxdz());
        }
        ycDd.setYdgz(carcache.getYdgz());//预订规则
        ycDd.setCpbz(carcache.getCpbz());//产品备注
        if(StringUtils.isBlank(ycDd.getCpbz())){
            ycDd.setCpbz(carcache.getCpms());
        }
        ycDd.setJgsm(carcache.getJgsm());//价格说明
        if ("DDYC".equals(carcache.getGysbh())) {//如果为供应商为滴滴则坐标为搜搜坐标
            ycDd.setCfdX(carcache.getSosoCfdX());
            ycDd.setCfdY(carcache.getSosoCfdY());
            ycDd.setMddX(carcache.getSosoMddX());
            ycDd.setMddY(carcache.getSosoMddY());
        } else {
            ycDd.setCfdX(carcache.getCfdX());
            ycDd.setCfdY(carcache.getCfdY());
            ycDd.setMddX(carcache.getMddX());
            ycDd.setMddY(carcache.getMddY());
        }
        ycDd.setJgmd5(carcache.getJgmd5());//价格MD5
        ycDd.setJjmslb(carcache.getJjmslb());//计价模式类别
        ycDd.setWbcxzmc(carcache.getWbcxzmc());//外部车型组名称
        ycDd.setWbcxzbh(carcache.getWbcxzbh());//外部车型组编号
        ycDd.setYgje(carcache.getJsj());//预估金额
        ycDd.setCsygje(carcache.getPrice());
        ycDd.setBuyerEstimatedPrice(carcache.getJsj());
        ycDd.setCgJsje(carcache.getJsj());//采购结算金额
        ycDd.setGyJsje(carcache.getGyJsje());
        ycDd.setGyCbj(carcache.getPrice());//供应成本价
        ycDd.setJyxsje(carcache.getJyxsje());//建议销售金额
        ycDd.setYfje(carcache.getYfje());//应付金额
        ycDd.setPttdfs(carcache.getPttdfs());//平台贴点方式
        ycDd.setPttdbl(carcache.getPttdbl());//平台贴点比例
        ycDd.setPttdje(carcache.getPttdje());//平台贴点金额
        ycDd.setPtkrgz(carcache.getPtkrgz());//平台控润规则
        ycDd.setPtkrfs(carcache.getPtkrfs());//平台控润方式
        ycDd.setPtkrbl(carcache.getPtkrbl());//平台控润比例
        ycDd.setPtkrje(carcache.getPtkrje());//平台控润金额
        ycDd.setGyFyfs(carcache.getGyFyfs());//供应返佣方式
        ycDd.setGyFybl(carcache.getGyFybl());//供应返佣比例
        ycDd.setGyFyje(carcache.getGyFyje());//供应返佣金额
        ycDd.setGyQhf(carcache.getGyQhf());//供应前后返
        ycDd.setSfykj(carcache.getSfykj());//是否一口价
        ycDd.setGyCbj(carcache.getPrice());//供应成本价
        String qxgz = buyerBookSpeciCarCommonService.qxgzCheckStyle(carcache);
        ycDd.setQxgz(qxgz);//取消规则
    }
}