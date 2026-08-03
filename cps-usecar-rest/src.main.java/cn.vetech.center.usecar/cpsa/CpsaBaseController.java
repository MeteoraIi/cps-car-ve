package cn.vetech.center.usecar.cpsa;

import cn.vetech.center.base.api.vo.VeUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * Cpsa 系统界面RESTful的基类
 *
 * @author houya
 */
public class CpsaBaseController {
    /**
     * 注入request
     */
    @Autowired
    protected HttpServletRequest request;
    /**
     * 获取登陆用户的对象
     */
    @Autowired
    protected CpsaSessionService cpsaSessionService;
    /**
     * 登陆用户
     */
    protected VeUserVO loginUser;

    /**
     * 从请求头中获取登陆用户编号
     *
     * @return
     */

    @ModelAttribute
    public void validLogin() {
        loginUser = cpsaSessionService.getLoginUser(request);
    }
}
