package cn.vetech.center.usecar.setting.profit.service;

import cn.vetech.center.car.service.GyGysfyService;
import cn.vetech.center.car.service.GyShfzShmxService;
import cn.vetech.center.car.vo.SellerRebateSearchVO;
import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.service.usecar.YcKrJgmxService;
import cn.vetech.center.usecar.service.usecar.YcKrService;
import cn.vetech.center.usecar.service.usecar.YcKrShmxService;
import cn.vetech.center.usecar.service.usecar.YcKrShzmxService;
import cn.vetech.center.usecar.setting.profit.dto.CpsaUseCarProfitCacheDTO;
import cn.vetech.center.usecar.setting.profit.vo.CpsaProfitCacheVO;
import cn.vetech.center.usecar.setting.profit.vo.CpsaUseCarProfitCacheVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用车控润接口
 * Created by vetech on 2017/11/8.
 *
 * @author nwt
 */
@Service
public class CpsaUseCarProfitCacheService {

    /**
     * 日志记录
     */
    private final Logger logger = LoggerFactory.getLogger(CpsaUseCarProfitCacheService.class);

    /***
     * 用车控润商户明细操作service
     */
    @Autowired
    private YcKrShmxService ycKrShmxService;

    /**
     * 用车控润商户组明细操作service
     */
    @Autowired
    private YcKrShzmxService ycKrShzmxService;

    /**
     * 用车控润操作service
     */
    @Autowired
    private YcKrService ycKrService;

    /**
     * 商户分组商户明细子表操作service
     */
    @Autowired
    private GyShfzShmxService gyShfzShmxService;

    /**
     * 用车控润价格明细操作service
     */
    @Autowired
    private YcKrJgmxService ycKrJgmxService;

    /**
     * 公用供应商返佣
     */
    @Autowired
    private GyGysfyService gyGysfyService;

    /**
     * 控润缓存过滤
     */
    @Autowired
    private CpsaProfitFilterService cpsaProfitFilterService;

    /**
     * 根据所给参数返回用车控润信息
     *
     * @param cpsaUseCarProfitCacheDTO 控润入参
     * @return CpsaUseCarProfitCacheVO
     */
    public CpsaUseCarProfitCacheVO getCpsaUseCarProfitCacheVO(CpsaUseCarProfitCacheDTO cpsaUseCarProfitCacheDTO) {
        return getCpsaUseCarProfitCacheVO(cpsaUseCarProfitCacheDTO,null);
    }
    /**
     * 根据所给参数返回用车控润信息
     * @param cgshProfit 采购商户控润
     * @param cpsaUseCarProfitCacheDTO 控润入参
     * @return CpsaUseCarProfitCacheVO
     */
    public CpsaUseCarProfitCacheVO getCpsaUseCarProfitCacheVO(CpsaUseCarProfitCacheDTO cpsaUseCarProfitCacheDTO, List<Map<String, Map<String, List<CpsaProfitCacheVO>>>> cgshProfit) {
//        logger.info("开始获取用车控润值信息......");
        /**走缓存的用车控润信息**/
        CpsaUseCarProfitCacheVO cpsaUseCarProfitCacheVO = new CpsaUseCarProfitCacheVO();
        try {
            cpsaUseCarProfitCacheVO = cpsaProfitFilterService.getCpsaUseCarProfitCacheVO(cpsaUseCarProfitCacheDTO,cgshProfit);
            if(!cpsaUseCarProfitCacheDTO.isNotLog()) {
                logger.info("用车控润值信息为：{}", JsonMapper.defaultMapper().toJson(cpsaUseCarProfitCacheVO));
            }
            if (cpsaUseCarProfitCacheVO != null) {
                cpsaUseCarProfitCacheVO.setSfygz(UseCarConstant.ZT_ONE);
            } else {
                cpsaUseCarProfitCacheVO = new CpsaUseCarProfitCacheVO();
                cpsaUseCarProfitCacheVO.setSfygz(UseCarConstant.ZT_ZERO);
            }
            /**返佣相关信息获取***/
            List<SellerRebateSearchVO> gygyfy = gyGysfyService.selectFyzMx(cpsaUseCarProfitCacheDTO.getGyshbh(), cpsaUseCarProfitCacheDTO.getCplxid());
            if (gygyfy != null && gygyfy.size() > UseCarConstant.ZERO) {
                SellerRebateSearchVO sellerRebateSearchVO = gygyfy.get(UseCarConstant.ZERO);
                if (BigDecimal.ONE.compareTo(sellerRebateSearchVO.getQhf()) == UseCarConstant.ZERO) {
                    cpsaUseCarProfitCacheVO.setQhffyds(sellerRebateSearchVO.getQffyds());//前后返返佣点数
                } else if (new BigDecimal(UseCarConstant.TWO).compareTo(sellerRebateSearchVO.getQhf()) == UseCarConstant.ZERO) {
                    cpsaUseCarProfitCacheVO.setQhffyds(sellerRebateSearchVO.getHffyds());//前后返返佣点数
                }
                cpsaUseCarProfitCacheVO.setQhffyfs(sellerRebateSearchVO.getQffyfs());//返佣方式
                cpsaUseCarProfitCacheVO.setQhf(sellerRebateSearchVO.getQhf());//前后反
                cpsaUseCarProfitCacheVO.setSfyfy(UseCarConstant.ZT_ONE);//是否有返佣
            } else {
//                logger.info("未能获取用车返佣信息");
                cpsaUseCarProfitCacheVO.setSfyfy(UseCarConstant.ZT_ZERO);
            }
        } catch (Exception e) {
            logger.info("用车控润信息 走缓存异常");
            cpsaUseCarProfitCacheVO.setSfygz(UseCarConstant.ZT_ZERO);
            cpsaUseCarProfitCacheVO.setSfyfy(UseCarConstant.ZT_ZERO);
        }
        return cpsaUseCarProfitCacheVO;
    }

    /**
     * 分组控润设置
     * @param groupProfit 分组控润
     * @param cgshBh 采购商户编号
     * @return 控润信息
     */
    public List<Map<String, Map<String, List<CpsaProfitCacheVO>>>> filterByCgsh(Map<String, Map<String, Map<String, List<CpsaProfitCacheVO>>>> groupProfit, String cgshBh) {
        return  cpsaProfitFilterService.filterByCgsh(groupProfit,cgshBh);
    }
}