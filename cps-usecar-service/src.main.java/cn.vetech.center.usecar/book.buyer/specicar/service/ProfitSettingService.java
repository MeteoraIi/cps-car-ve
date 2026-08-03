package cn.vetech.center.usecar.book.buyer.specicar.service;

import cn.vetech.center.base.api.vo.VeCspzVO;
import cn.vetech.center.common.ApplicationName;
import cn.vetech.center.usecar.apiclient.base.IVeCsPzServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.cache.annotation.Cache;

/**
 * 控润查询
 *
 * @author : Y
 * @since 2023/7/5 14:42
 */
@Service
public class ProfitSettingService {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(ProfitSettingService.class);
    /**
     * base  参数配置服务
     */
    @Autowired
    private IVeCsPzServiceClient veCsPzServiceClient;

    /**
     *
     * 请求超时去掉
     * 获取控润设置
     * 在CPS增加公共参数  10297，参数值1:0/1 0不包含，1包含
     *
     * @return 获取控润设置
     */
//    @Cache(appname = ApplicationName.USECAR, table = "GET_PROFITSETTING", key = "10297", expiremin = 2)
    public String getProfitSetting() {
//        try {
//            RestResponse<VeCspzVO> response = veCsPzServiceClient.getVeCspzByBh("10297");
//            if (response != null && response.getResult() != null) {
//                return response.getResult().getYdhm();
//            }
//        } catch (Exception e) {
//            logger.error("获取利润设置异常", e);
//        }
        return "1";
    }
}