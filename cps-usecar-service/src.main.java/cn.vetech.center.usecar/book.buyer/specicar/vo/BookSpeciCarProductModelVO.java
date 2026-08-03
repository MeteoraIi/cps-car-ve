package cn.vetech.center.usecar.book.buyer.specicar.vo;

import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;

/**
 * 用车专快车产品对内VO
 * @author houshuang
 * Created by vetech on 2017/11/03
 */
public class BookSpeciCarProductModelVO {

    /**
     * 专快车价格缓存ID
     */
    @ApiModelProperty(value = "专快车价格缓存ID", dataType = "String")
    private String priceCacheId;
    /**
     * 供应商编号
     */
    @ApiModelProperty(value = "供应商编号", dataType = "String")
    private String gysbh;
    /**
     * 供应商名称
     */
    @ApiModelProperty(value = "供应商名称", dataType = "String")
    private String gysmc;
    /**
     * 车型组编号
     */
    @ApiModelProperty(value = "车型组编号", dataType = "String")
    private String cxzbh;
    /**
     * 车型组名称
     */
    @ApiModelProperty(value = "车型组名称", dataType = "String")
    private String cxzmc;
    /**
     * 报价
     */
    @ApiModelProperty(value = "报价", dataType = "String")
    private BigDecimal price;
    /**
     * CPMS）产品描述
     */
    @ApiModelProperty(value = "CPMS）产品描述", dataType = "String")
    private String cpms;
    /**
     * 价格md5,通过 新的预估价接口获得
     */
    @ApiModelProperty(value = "价格md5,通过 新的预估价接口获得", dataType = "String")
    private String jgmd5;
    /**
     * 计价模式类别：201 专车，301快车
     */
    @ApiModelProperty(value = "价模式类别：201 专车，301快车", dataType = "String")
    private String jjmslb;
    /**
     * 服务内容
     */
    @ApiModelProperty(value = "服务内容", dataType = "String")
    private String fwnr;

    /**
     * 取消规则公式
     */
    @ApiModelProperty(value = "取消规则公式", dataType = "String")
    private String qxgs;

    /**
     * 取消备注
     */
    @ApiModelProperty(value = "取消备注", dataType = "String")
    private String qxbz;

    /**
     * 预订规则
     */
    @ApiModelProperty(value = "预订规则", dataType = "String")
    private String ydgz;

    /**
     * 市场价
     */
    @ApiModelProperty(value = "市场价", dataType = "BigDecimal")
    private BigDecimal scj;
    /**
     * 结算价
     */
    @ApiModelProperty(value = "结算价", dataType = "BigDecimal")
    private BigDecimal jsj;

    /**
     * 取消规则
     */
    @ApiModelProperty(value = "取消规则", dataType = "String")
    private String qxgz;

    /**
     * 价格说明
     */
    @ApiModelProperty(value = "价格说明", dataType = "String")
    private String jgsm;

    /**
     * 本单里程(单位米)
     */
    @ApiModelProperty(value = "本单里程(单位米)", dataType = "bigdecimal")
    private BigDecimal bdlc;
    /**
     * 本单时长(单位分钟)
     */
    @ApiModelProperty(value = "本单时长(单位分钟)", dataType = "bigdecimal")
    private BigDecimal bdsc;

    /**
     * 站点所在城市编号(接送产品)
     */
    @ApiModelProperty(value = "站点所在城市编号(接送产品)", dataType = "string")
    private String cfdCsid;

    /**
     * 服务城市编号(接送产品)
     */
    @ApiModelProperty(value = "服务城市编号(接送产品)", dataType = "string")
    private String mmdCsid;

    /**
     * 出发的POI
     */
    @ApiModelProperty(value = "出发的POI", dataType = "string")
    private String cfd;

    /**
     * 目的地POI
     */
    @ApiModelProperty(value = "目的地POI", dataType = "string")
    private String mdd;

    /**
     * 出发地详细地址
     */
    @ApiModelProperty(value = "出发地详细地址", dataType = "string")
    private String cfdXxdz;

    /**
     * 目的地详细地址
     */
    @ApiModelProperty(value = "目的地详细地址", dataType = "string")
    private String mddXxdz;

    /**
     * 出发地经度
     */
    @ApiModelProperty(value = "出发地经度", dataType = "string")
    private String cfdX;
    /**
     * 出发地纬度
     */
    @ApiModelProperty(value = "出发地纬度", dataType = "string")
    private String cfdY;

    /**
     * 目的地经度
     */
    @ApiModelProperty(value = "目的地经度", dataType = "string")
    private String mddX;
    /**
     * 目的地纬度
     */
    @ApiModelProperty(value = "目的地纬度", dataType = "string")
    private String mddY;

    /**
     * 预订方式 1立即用车，2预约用车
     */
    @ApiModelProperty(value="预订方式",dataType = "String",required = false)
    private String ydfs;

    /**
     * (用车时间(2016/1/21 15:30 精确到小时分钟))
     */
    @ApiModelProperty(value="(用车时间(2016/1/21 15:30 精确到小时分钟))",dataType = "String",required = false)
    private String ycsj;

    /**
     * 是否一口价（0.非一口价，1.一口价）
     */
    @ApiModelProperty(value = "是否一口价（0.非一口价，1.一口价）", dataType = "string")
    private String sfykj;
    /**
     * 出发地城市名称
     */
    @ApiModelProperty(value = "出发地城市名称", dataType = "string")
    private String cfdCsmc;

    /**
     * 目的地城市名称
     */
    @ApiModelProperty(value = "目的地城市名称", dataType = "string")
    private String mddCsmc;

    /**
     * soso出发地经度
     */
    @ApiModelProperty(value="soso出发地经度",dataType = "String",required = false)
    private String sosoCfdX;
    /**
     * soso出发地维度
     */
    @ApiModelProperty(value="soso出发地维度",dataType = "String",required = false)
    private String sosoCfdY;
    /**
     * soso目的地经度
     */
    @ApiModelProperty(value="soso目的地经度",dataType = "String",required = false)
    private String sosoMddX;
    /**
     * soso目的地维度
     */
    @ApiModelProperty(value="soso目的地维度",dataType = "String",required = false)
    private String sosoMddY;

    /**
     * 免费取消时限
     */
    @ApiModelProperty(value="免费取消时限",dataType = "String",required = false)
    private BigDecimal mfqxsx;
    /**
     * 到预定时间时前返佣
     */
    @ApiModelProperty(value="到预定时间时前返佣",dataType = "String",required = false)
    private BigDecimal dydsjsqfy;
    /**
     * 预定时间后返佣
     */
    @ApiModelProperty(value="预定时间后返佣",dataType = "String",required = false)
    private BigDecimal ydsjhfy;
    /**
     * 服务标准
     */
    @ApiModelProperty(value="服务标准",dataType = "String",required = false)
    private String fwbz;

    /**
     * 外部车型组编号
     */
    @ApiModelProperty(value="外部车型组编号",dataType = "String",required = false)
    private String wbcxzbh;

    /**
     * 外部车型组名称
     */
    @ApiModelProperty(value="外部车型组名称",dataType = "String",required = false)
    private String wbcxzmc;

    /**
     * 供应结算金额
     */
    @ApiModelProperty(value="供应结算金额",dataType = "供应结算金额",required = false)
    private BigDecimal gyJsje;

    /**
     * 平台贴点方式(1.金额,2.百分比)
     */
    @ApiModelProperty(value = "平台贴点方式(1.金额,2.百分比)", dataType = "string")
    private String pttdfs;
    /**
     * 平台贴点比例（方式为百分比时，存值为5，表示5%，或为金额则为实际的值）
     */
    @ApiModelProperty(value = "平台贴点比例（方式为百分比时，存值为5，表示5%，或为金额则为实际的值）", dataType = "bigdecimal")
    private BigDecimal pttdbl;
    /**
     * 平台贴点金额
     */
    @ApiModelProperty(value = "平台贴点金额", dataType = "bigdecimal")
    private BigDecimal pttdje;


    /**
     * 平台控润方式(2:百分比1:数值 3：无返佣)
     */
    @ApiModelProperty(value = "平台控润方式(2:百分比1:数值 3：无返佣)", dataType = "bigdecimal")
    private BigDecimal ptkrfs;
    /**
     * 平台控润值(百分比或者具体数值) 例 5% 存 5  数值 10 存10
     */
    @ApiModelProperty(value = "平台控润值(百分比或者具体数值) 例 5% 存 5  数值 10 存10", dataType = "bigdecimal")
    private BigDecimal ptkrbl;
    /**
     * 平台控润金额(控润金额,平台利润)
     */
    @ApiModelProperty(value = "平台控润金额(控润金额,平台利润)", dataType = "bigdecimal")
    private BigDecimal ptkrje;


    /**
     * 供应返佣方式(2:百分比1:数值 3：无返佣)
     */
    @ApiModelProperty(value = "供应返佣方式(2:百分比1:数值 3：无返佣)", dataType = "bigdecimal")
    private BigDecimal gyFyfs;
    /**
     * 供应返佣值(返佣比例或者钱的值)
     */
    @ApiModelProperty(value = "供应返佣值(返佣比例或者钱的值)", dataType = "bigdecimal")
    private BigDecimal gyFybl;
    /**
     * 供应返佣金额
     */
    @ApiModelProperty(value = "供应返佣金额", dataType = "bigdecimal")
    private BigDecimal gyFyje;

    /**
     * 供应前后返（1，前返，2后返）
     */
    @ApiModelProperty(value = "供应前后返（1，前返，2后返）", dataType = "bigdecimal")
    private BigDecimal gyQhf;

    /**
     * 控润规则（1.结算价，2.差价,3.建议价）
     */
    @ApiModelProperty(value = "控润规则（1.结算价，2.差价,3.建议价）", dataType = "string")
    private String ptkrgz;

    /**
     * 应付金额 (下订单时计算出来的，应该支付的金额)
     */
    @ApiModelProperty(value = "应付金额 (下订单时计算出来的，应该支付的金额)", dataType = "bigdecimal")
    private BigDecimal yfje;
    /**
     * 行程说明
     */
    @ApiModelProperty(value = "行程说明", dataType = "string")
    private String xcsm;
    /**
     * 车型说明
     */
    @ApiModelProperty(value = "车型说明", dataType = "string")
    private String cxsm;
    /**外部供应商编号*/
    @ApiModelProperty(value = "外部供应商编号")
    private String wbgysbh;
    /**外部供应商名称*/
    @ApiModelProperty(value = "外部供应商名称")
    private String wbgysmc;
    /**供应商logo链接*/
    @ApiModelProperty(value = "供应商logo链接")
    private String brandIconUrl;
    /**
     * 供应商下单类型 0或空-单个下单 1-批量下单
     */
    private String supplierBookType;
    /**
     * 是否可以修改目的地 0-可以 1-不可以
     */
    private String destinationChangeable;
    /**
     * 出租车logo
     */
    private String czcLogo;

    /**
     * 预估价格明细
     */
    private String priceDetail;
    /**
     *  匹配的控润规则id
     */
    private String krinfo;

    /**
     * 展示一口价标识，滴滴特有 0：不展示，1：展示
     */
    private String showYkjFlag;

    /**
     * 实际供应商编号
     */
    @ApiModelProperty(value = "实际供应商编号", dataType = "string")
    private String actualSupplierNo;
    /**
     * 可修改的次数
     */
    private String modifyDestCount;
    /**
     * 是否使用会员价：true-是，false-否
     */
    private String useMemberPrice;

    /**
     * 会员描述（如：铂金会员95折、会员专享价、非会员不享受）
     */
    private String memberDesc;

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }

    // 后面都是get set    
}