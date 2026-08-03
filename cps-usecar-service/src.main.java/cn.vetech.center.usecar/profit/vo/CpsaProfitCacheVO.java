package cn.vetech.center.usecar.setting.profit.vo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * 控润信息缓存
 * @author chenyong
 * @since 2017-11-13
 */
public class CpsaProfitCacheVO {
    /**
     * 控润ID
     */
    private String id;
    /**
     * 优先级
     */
    private Integer yxj;
    /**
     * 控润有效期起
     */
    private String gzyxqq;
    /**
     * 控润有效期止
     */
    private String gzyxqz;
    /**
     * 1按与供应结算价2按差价(建议销售价-与供应结算价) 3建议销售价(接口供应商都是按建议销售价)
     */
    private String krgz;
    /**
     * 采购商选择方式 1：全部采购商 3：单独指定采购商(单独指定的情况下，商户信息存入到子表中)2：按分组指定采购商
     */
    private String cgsxzfs;
    /**
     * 供应商选择方式 1：全部供应商3：指定供应商2：按分组指定
     */
    private String gysxzfs;
    /**
     * 修改时间
     */
    private Date xgDatetime;
    /**
     * 版本号
     */
    private Long version;

    /**
     * 用车 1000  机场服务0500
     */
    private String cpdlid;

    /**
     * 3级产品类型 标准接车
     */
    private String cplxid;

    /**
     * 采购商户编号
     */
    private String cgShbh;
    /**
     * 供应商户编号
     */
    private String gyShbh;

    /**
     * 采购商户组ID
     */
    private String cgShzid;
    /**
     * 供应商户组ID
     */
    private String gyShzid;
    /**
     * 城市ID
     */
    private String csid;
    /**
     * 站点ID
     */
    private String zdid;
    /**
     * 航站楼ID
     */
    private String hzlid;
    /**
     * 站点类型 1：火车站，2：机场，3：航站楼
     */
    private String zdlx;

    /**
     * 是否制定站点
     * 1.全部站点 2 指定站点
     */
    private String sfzdzd;

    /**
     * 控润明细JSON
     */
    private String krmxJson;

    /**
     *  控润规则 1：取最高 2：叠加  （null）默认取最高
     */
    private String krrule;

    /**
     *  让利规则 1：取最高 2：叠加 （null）默认取最高
     */
    private String rlrule;
    /**
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id", dataType = "string")
    private String channelId;

    // 后面都是get set
}