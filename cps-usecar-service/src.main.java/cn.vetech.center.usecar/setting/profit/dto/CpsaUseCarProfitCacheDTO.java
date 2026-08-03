package cn.vetech.center.usecar.setting.profit.dto;


import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用车控润处理DTO
 * Created by vetech on 2017/11/8.
 * @author nwt
 */
public class CpsaUseCarProfitCacheDTO implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 采购商户编号
     */
    @ApiModelProperty(value = "商户编号", dataType = "string")
    private String cgshbh;

    /**
     * 供应商户编号
     */
    @ApiModelProperty(value = "商户编号", dataType = "string")
    private String gyshbh;

    /**
     * 产品类型ID
     */
    @ApiModelProperty(value = "产品类型ID", dataType = "string")
    private String cplxid;

    /**
     * 站点类型 1：火车站，2：机场，3：航站楼
     */
    private String zdlx;

    /**
     * 站点id
     */
    @ApiModelProperty(value = "站点id", dataType = "string")
    private String  zdid;

    /***
     * 产品价格（注：此参数不用传）
     */
    @ApiModelProperty(value = "产品价格", dataType = "bigDecimal")
    private BigDecimal cpjg;

    /***
     * 产品价格
     */
    @ApiModelProperty(value = "供应结算价", dataType = "bigDecimal")
    private BigDecimal cpjeOne;

    /***
     * 产品价格
     */
    @ApiModelProperty(value = "差价(建议销售价-供应结算价)", dataType = "bigDecimal")
    private BigDecimal cpjeTwo;

    /***
     * 产品价格(接口供应商用到)
     */
    @ApiModelProperty(value = "建议销售价", dataType = "bigDecimal")
    private BigDecimal cpjeThree;

    /**
     * 当前日期（此字段不用传）
     */
    @ApiModelProperty(value = "供应结算价", dataType = "date")
    private Date nowtime;

    /**
     * 最优的控润id(此字段不用传)
     */
    @ApiModelProperty(value = "最优的控润id(此字段不用传)", dataType = "string")
    private String gzidZd;
    /**
     * 不打印日志
     */
    private boolean notLog;

    /**
     *  用车里程
     */
    private BigDecimal yclc;

    /**
     * 用车时长
     */
    private BigDecimal ycsc;

    /**
     * 用车时间  hh:mm:ss
     */
    private String ycsj;

    private String cityLevel;
    /**
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id", dataType = "string")
    private String channelId;

    private String memberId;

    private String queryMemberPrice;

    private MemberDiscountInfo memberDiscountInfo;

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }

    // 一堆get，set
}