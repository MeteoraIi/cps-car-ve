package cn.vetech.center.usecar.book.buyer.specicar.service;

import cn.vetech.center.link.usecar.dto.LinkCancelSpecialOrderDTO;
import cn.vetech.center.link.usecar.vo.LinkCancelSpecialOrderVO;
import cn.vetech.center.usecar.apiclient.linkusecar.ILinkSpecialCarServiceClient;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.listener.service.DelayedTaskConfiguration;
import cn.vetech.center.usecar.service.usecar.YcSupplierInterfaceCountService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;

import java.util.concurrent.TimeUnit;

import static cn.vetech.center.usecar.common.enums.SupplierInterfaceOperateEnum.CANCEL;
import static cn.vetech.center.usecar.setting.buyerfilter.service.CarSupplierChannelTypeService.setChannelType;

/**
 * 取消服务
 *
 * @author : Y
 * @since 2022/1/13 15:06
 */
@Service
public class DefaultCancelService {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(DefaultCancelService.class);
    /**
     * 延迟执行
     */
    @Autowired
    private DelayedTaskConfiguration delayedTaskConfiguration;

    /**
     * 用车专快车接口对象
     */
    @Autowired
    private ILinkSpecialCarServiceClient iLinkSpecialCarServiceClient;
    /**
     * 接口记录
     */
    @Autowired
    private YcSupplierInterfaceCountService ycSupplierInterfaceCountService;
    /**
     * 取消方法 失败2秒后重试，如果再失败异步10s后重试
     *
     * @param dto dto
     * @param ycdd
     * @return 回参
     */
    public RestResponse<LinkCancelSpecialOrderVO> cancelOrder(LinkCancelSpecialOrderDTO dto, YcDd ycdd) {
        setChannelType(dto,ycdd);
        RestResponse<LinkCancelSpecialOrderVO> resp = cancelOrder2(dto);
        final int sleepSeconds = 2;
        if (cancelFail(resp)) {
            try {
                TimeUnit.SECONDS.sleep(sleepSeconds);
            } catch (InterruptedException e) {
                logger.error("休眠异常", e);
            }
            logger.info("重试开始");
            dto.setCgQxyy(dto.getCgQxyy() + "-取消失败重试");
            resp = cancelOrder2(dto);
        } else {
            return resp;
        }
        if (cancelFail(resp)) {
            delayedTaskConfiguration.addTaskAfter1Min(() ->  cancelOrder2(dto));
        }
        return resp;
    }

    /**
     * 取消异常处理
     *
     * @param dto dto
     * @return 回参
     */
    private RestResponse<LinkCancelSpecialOrderVO> cancelOrder2(LinkCancelSpecialOrderDTO dto) {
        try {
            RestResponse<LinkCancelSpecialOrderVO> resp = iLinkSpecialCarServiceClient.cancelOrder(dto);
            ycSupplierInterfaceCountService.logRequest(dto.getShid(), CANCEL, resp.getResult());
            return resp;
        } catch (Exception e) {
            logger.error("link取消异常", e);
        }
        return null;
    }

    /**
     * 是否取消失败
     *
     * @param resp 取消回参
     * @return 是否取消失败
     */
    private boolean cancelFail(RestResponse<LinkCancelSpecialOrderVO> resp) {
        if (resp == null || resp.getResult() == null || "false".equals(resp.getResult().getQszt()) || StringUtils.isBlank(resp.getResult().getQszt())) {
            return true;
        }
        return false;
    }
}