package cn.vetech.center.usecar.apiclient.linkusecar;

import cn.vetech.center.common.ApplicationName;
import cn.vetech.center.link.usecar.ILinkSpecialCarService;
import org.springframework.cloud.netflix.feign.FeignClient;

/**
 * CPS专快车请求link入口
 * Created by vetech on 2017/11/03.
 * @author houshuang
 */
// 远程调用代理，让 CPS 像调本地方法一样调用 LINK 系统，底层的 HTTP 通信、序列化、服务发现全部由 Feign 框架自动处理
@FeignClient(value = ApplicationName.LINK_USECAR)
public interface ILinkSpecialCarServiceClient extends ILinkSpecialCarService{
}
