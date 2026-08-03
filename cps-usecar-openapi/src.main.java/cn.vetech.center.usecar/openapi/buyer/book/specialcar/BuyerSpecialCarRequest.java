package cn.vetech.center.usecar.openapi.buyer.book.specialcar;

import cn.vetech.center.system.openapi.OpenApiRequest;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 采购类
 * 专车产品查询列表接口的request
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class BuyerSpecialCarRequest extends OpenApiRequest {
    /**
     * 订单类型
     */
    private String ddlx;
    /**
     * 上车城市id
     */
    private String sccs;
    /**
     * 上车城市名称
     */
    private String sccsMc;
    /**
     * 上车城市poi
     */
    private String sccsPoi;
    /**
     * 上车城市详细地址
     */
    private String sccsXxdz;
    /**
     * 目的城市ID
     */
    private String mdcs;
    /**
     * 目的城市名称
     */
    private String mdcsMc;
    /**
     * 目的城市Poi
     */
    private String mdcsPoi;
    /**
     * 目的城市详细地址
     */
    private String mdcsXxdz;
    /**
     * 上车经度
     */
    private String scjd;
    /**
     * 上车纬度
     */
    private String scwd;
    /**
     * 送达经度
     */
    private String sdjd;
    /**
     * 送达纬度
     */
    private String sdwd;
    /**
     * 用车日期
     */
    private String ycrq;
    /**
     * 用车时间 hh:mm
     */
    private String ycsj;
    /**
     * 车型  1.舒适型，2.豪华型，3.商务型
     */
    private String cx;
    /**
     * 计价模式类别
     */
    private String jjmslb;
    /**
     * soso出发地经度
     */
    @XmlElement(name="soso_cfd_x")
    private String sosoCfdX;
    /**
     * soso出发地维度
     */
    @XmlElement(name="soso_cfd_y")
    private String sosoCfdY;
    /**
     * soso目的地经度
     */
    @XmlElement(name="soso_mdd_x")
    private String sosoMddX;
    /**
    * soso目的地维度
     */
    @XmlElement(name="soso_mdd_y")
    private String sosoMddY;

    /**
     * 始发地名称
     */
    private String jsfwsfdmc;

    /**
     * 目的地名称
     */
    private String jsfwmddmc;
    //APP过来传的这几个字段
    /**出发地名称(如家酒店)*/
    private String cfdMc;
    /**出发地详细地址(嵩山大道2289号)*/
    private String cfdXxdz;
    /**目的地名称(顺丰快递)*/
    private String mddMc;
    /**出发地详细地址(乔心街39号)*/
    private String mddXxdz;
    /**
     * 渠道来源 CPS/ASMS
     */
    private String qdly;

    /**
     * 请求来自标准用车采购商
     */
    private String bzcgs;

    /**
     * 滴滴是否开启修改目的地
     */
    private String ddEnableModifyDestination;
    /**
     * 出行人手机号
     */
    private String cxrsj;

    /**
     * 游标id,异步查询时使用
     */
    private String cursorId;
    /**
     * 异步加载查询
     */
    private String asyn;

    /**
     * 1，查询所有顺风车，其他否
     */
    private String rideShare;
    /**
     * 最晚计划出发时间
     */
    @ApiModelProperty(value = "最晚计划出发时间", example = "2025-12-20 10:22")
    private String endPlanStartTime;
    /**
     * 乘坐人数
     */
    @ApiModelProperty(value = "乘坐人数", example = "4")
    private Integer paxNum;
    /**
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id", dataType = "string")
    private String channelId;
    /**
     * 是否全量数据
     */
    private boolean queryAll;

    /**
     *  是否查询优惠券
     */
    private boolean sfcxyhq;

    // 后面全是get、set

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}