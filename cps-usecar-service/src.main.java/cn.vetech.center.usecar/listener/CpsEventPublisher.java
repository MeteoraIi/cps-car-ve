package cn.vetech.center.usecar.listener;

import cn.vetech.center.usecar.listener.entity.CpsEvent;
import cn.vetech.center.usecar.listener.entity.CpsEventEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author dingkang
 **/
@Service
public class CpsEventPublisher {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(CpsEventPublisher.class);
    /**
     *  发布事件
     */
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     *
     * @param cpsEvent 入参
     */
    public void send(CpsEvent cpsEvent){
        applicationEventPublisher.publishEvent(cpsEvent);
    }

    /**
     *
     * @param eventEnum 枚举
     * @param param 入参
     */
    public void send(CpsEventEnum eventEnum,Object param){
        this.send(CpsEvent.getInstance(eventEnum,param));
    }
}
