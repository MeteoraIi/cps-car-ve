package cn.vetech.center.usecar.apiclient.invocie;

import cn.vetech.center.common.ApplicationName;
import cn.vetech.center.cps.invoice.api.service.IOpenInvoiceService;
import org.springframework.cloud.netflix.feign.FeignClient;

/**
 * 用车开票服务
 * @author xufei
 * @since 2023/4/4
 */
@FeignClient(value = ApplicationName.INVOICE)
public interface ICpsaApplyInvoiceServiceClient extends IOpenInvoiceService {
}
