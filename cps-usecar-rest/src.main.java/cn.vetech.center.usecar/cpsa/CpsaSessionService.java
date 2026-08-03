package cn.vetech.center.usecar.cpsa;

import cn.vetech.center.base.api.vo.VeUserVO;
import cn.vetech.center.usecar.apiclient.base.ICpsaLoginServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by vetech on 2017/9/3.
 * 获取CPSA系统登陆用户
 *
 * @author houya
 */
@Service
public class CpsaSessionService {
    /**
     * 日志
     *///
    private Logger logger = LoggerFactory.getLogger(CpsaSessionService.class);
    /**
     * 服务
     */
    @Autowired
    private ICpsaLoginServiceClient iCpsaLoginService;

    /**
     * 获取CPSA系统登陆用户
     * 这个方法只能在controller中使用。
     *
     * @param request 请求
     * @return 登陆的用户对象
     */
    public VeUserVO getLoginUser(HttpServletRequest request) {
        String authorization = request.getHeader("cpsa-access-token");
        RestResponse<VeUserVO> restResponse = iCpsaLoginService.getLoginUser(authorization);
        return restResponse.getResult();
    }
}
