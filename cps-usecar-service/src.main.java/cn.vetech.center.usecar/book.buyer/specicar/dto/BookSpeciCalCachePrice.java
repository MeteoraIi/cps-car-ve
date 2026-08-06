package cn.vetech.center.usecar.book.buyer.specicar.dto;

import cn.vetech.center.usecar.common.enums.UsecarProductTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;

/**
 * 专快车采购查询预订价格缓存对象
 * @author houshuang
 * @since 2017-11-03
 */
public class BookSpeciCalCachePrice {

    /**
     * 缓存ID
     */
    @ApiModelProperty(value = "缓存ID", dataType = "string")
    private String priceCacheId;

    /**
     * 供应商编号
     */
    @ApiModelProperty(value = "供应商编号", dataType = "string")
    private String gysbh;
    /**
     * 供应商编号
     */
    @ApiModelProperty(value = "供应商编号", dataType = "string")
    private String gysmc;
    /**
     * 报价
     */
    @ApiModelProperty(value = "报价", dataType = "string")
    private BigDecimal price;
    /**
     * CPMS）产品描述
     */
    @ApiModelProperty(value = "CPMS）产品描述", dataType = "string")
    private String cpms;

    /**
     * 10000101：标准(接口)接机，10000102：豪华接机，10000201：标准(接口)送机，
     * 10000202：豪华送机，10000301：标准接火车，10000302：豪华接火车10000401：
     * 标准送火车，10000402：豪华送火车10000501：专(快车)
     */
    @ApiModelProperty(value = "订单类型",
            dataType = "string")
    private UsecarProductTypeEnum ddlx;
    /**
     * 产品id(自签产品有ID)
     */
    @ApiModelProperty(value = "产品id(自签产品有ID)", dataType = "string")
    private String cpid;
    /**
     * 用车时间(2016/1/21 15:30 精确到小时分钟)
     */
    @ApiModelProperty(value = "用车时间(2016/1/21 15:30 精确到小时分钟)", dataType = "string")
    private String ycsj;

    /**
     * 站点所在城市编号(接送产品)
     */
    @ApiModelProperty(value = "站点所在城市编号(接送产品)", dataType = "string")
    private String cfdCsid;
    /**
     * 站点编号(接送产品)
     */
    @ApiModelProperty(value = "站点编号(接送产品)", dataType = "string")
    private String jsfwzdid;
    /**
     * 服务城市编号(接送产品)
     */
    @ApiModelProperty(value = "服务城市编号(接送产品)", dataType = "string")
    private String mmdCsid;
    /**
     * 服务城市区域编号(接送产品)
     */
    @ApiModelProperty(value = "服务城市区域编号(接送产品)", dataType = "string")
    private String jsfwqyid;
    /**
     * 车型组编号
     */
    @ApiModelProperty(value = "车型组编号", dataType = "string")
    private String cxzbh;
    /**
     * 车型组名称
     */
    @ApiModelProperty(value = "车型组名称", dataType = "string")
    private String cxzmc;
    /**
     * 订单备注
     */
    @ApiModelProperty(value = "订单备注", dataType = "string")
    private String ddbz;
    /**
     * 服务内容(对应服务标准字段)
     */
    @ApiModelProperty(value = "服务内容(对应服务标准字段)", dataType = "string")
    private String fwnr;
    /**
     * 服务标准(对应服务保障字段)
     */
    @ApiModelProperty(value = "服务标准(对应服务保障字段)", dataType = "string")
    private String fwbz;
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
     * 采购用户编号
     */
    @ApiModelProperty(value = "采购用户编号", dataType = "string")
    private String cgYhbh;
    /**
     * 采购商户编号
     */
    @ApiModelProperty(value = "采购商户编号", dataType = "string")
    private String cgShbh;
    /**
     * 采购商户简称
     */
    @ApiModelProperty(value = "采购商户简称", dataType = "string")
    private String cgShjc;

    /**
     * 取消规则(自有产品依据产品表取消规则字段组合成一句话存入这里，接口方订单存入接口方固定的取消规则)
     */
    @ApiModelProperty(value = "取消规则(自有产品依据产品表取消规则字段组合成一句话存入这里，接口方订单存入接口方固定的取消规则)", dataType = "string")
    private String qxgz;
    /**
     * 取消公式(自签产品 0,100%:50%:48,25%)  0，100%：50%：免费取消时限*60  0%
     */
    @ApiModelProperty(value = "取消公式(自签产品 0,100%:50%:48,25%)  0，100%：50%：免费取消时限*60  0%", dataType = "string")
    private String qxgs;
    /**
     * 供应商商户编号(包括龙腾，神州，滴滴也会做为商户进入系统)
     */
    @ApiModelProperty(value = "供应商商户编号(包括龙腾，神州，滴滴也会做为商户进入系统)", dataType = "string")
    private String gyShbh;
    /**
     * 供应商户简称
     */
    @ApiModelProperty(value = "供应商户简称", dataType = "string")
    private String gyShjc;
    /**
     * 预估金额(采购支付预估金额)
     */
    @ApiModelProperty(value = "预估金额(采购支付预估金额)", dataType = "bigdecimal")
    private BigDecimal ygje;
    /**
     * 采购结算金额(供应确认价格后计算)
     */
    @ApiModelProperty(value = "采购结算金额(供应确认价格后计算)", dataType = "bigdecimal")
    private BigDecimal cgJsje;
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
     * 出发的POI
     */
    @ApiModelProperty(value = "出发的POI", dataType = "string")
    private String cfd;
    /**
     * 出发地详细地址
     */
    @ApiModelProperty(value = "出发地详细地址", dataType = "string")
    private String cfdXxdz;
    /**
     * 目的地POI
     */
    @ApiModelProperty(value = "目的地POI", dataType = "string")
    private String mdd;
    /**
     * 目的地详细地址
     */
    @ApiModelProperty(value = "目的地详细地址", dataType = "string")
    private String mddXxdz;
    /**
     * 司机承诺提前到达时间(	数值是按分钟计算,参考下单指定的用车时间)
     */
    @ApiModelProperty(value = "司机承诺提前到达时间(	数值是按分钟计算,参考下单指定的用车时间)", dataType = "bigdecimal")
    private BigDecimal gySjtqddsj;
    /**
     * 司机到达后免费等待时间(数值是按分钟计算,参考下单指定的用车时间)
     */
    @ApiModelProperty(value = "司机到达后免费等待时间(数值是按分钟计算,参考下单指定的用车时间)", dataType = "bigdecimal")
    private BigDecimal gySjmfddsj;
    /**
     * 联系电话(订单联系人电话)
     */
    @ApiModelProperty(value = "联系电话(订单联系人电话)", dataType = "string")
    private String lxrdh;
    /**
     * 联系人(订单联系人姓名)
     */
    @ApiModelProperty(value = "联系人(订单联系人姓名)", dataType = "string")
    private String lxr;

    /**
     * 订单预订规则说明(主要存储外部接口产品的产品预订规则说明信息，也可能是多个字段拼接结果)
     */
    @ApiModelProperty(value = "订单预订规则说明(主要存储外部接口产品的产品预订规则说明信息，也可能是多个字段拼接结果)", dataType = "string")
    private String ydgz;
    /**
     * 产品备注信息(外部接口的产品备注或者自签产品的备注信息)
     */
    @ApiModelProperty(value = "产品备注信息(外部接口的产品备注或者自签产品的备注信息)", dataType = "string")
    private String cpbz;
    /**
     * 产品价格说明(主要存储外部接口产品的产品价格说明信息，也可能是多个字段拼接的结果)
     */
    @ApiModelProperty(value = "产品价格说明(主要存储外部接口产品的产品价格说明信息，也可能是多个字段拼接的结果)", dataType = "string")
    private String jgsm;

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
     * 建议销售金额
     */
    @ApiModelProperty(value = "建议销售金额", dataType = "bigdecimal")
    private BigDecimal jyxsje;
    /**
     * 采购系统订单编号
     */
    @ApiModelProperty(value = "采购系统订单编号", dataType = "string")
    private String cgDdbh;
    /**
     * 供应退款手续费
     */
    @ApiModelProperty(value = "供应退款手续费", dataType = "bigdecimal")
    private BigDecimal gyTksxf;
    /**
     * 应付金额 (下订单时计算出来的，应该支付的金额)
     */
    @ApiModelProperty(value = "应付金额 (下订单时计算出来的，应该支付的金额)", dataType = "bigdecimal")
    private BigDecimal yfje;
    /**
     * 处理效率
     */
    @ApiModelProperty(value = "处理效率", dataType = "string")
    private String clxl;
    /**
     * 退款效率
     */
    @ApiModelProperty(value = "退款效率", dataType = "string")
    private String tkxl;
    /**
     * 是否一口价（0.非一口价，1.一口价）
     */
    @ApiModelProperty(value = "是否一口价（0.非一口价，1.一口价）", dataType = "string")
    private String sfykj;
    /**
     * 丽程订单必须参数
     */
    @ApiModelProperty(value = "丽程订单必须参数", dataType = "string")
    private String carmodelid;
    /**
     * 供应成本价(自签产品的需要记录存发布的成本价，接口的产品可以不用记录)
     */
    @ApiModelProperty(value = "供应成本价(自签产品的需要记录存发布的成本价，接口的产品可以不用记录)", dataType = "bigdecimal")
    private BigDecimal gyCbj;
    /**
     * 价格MD5
     */
    @ApiModelProperty(value = "价格MD5", dataType = "string")
    private String jgmd5;
    /**
     * 计价模式类别 （201专车，301快车）
     */
    @ApiModelProperty(value = "计价模式类别 （201专车，301快车）", dataType = "string")
    private String jjmslb;
    /**
     * 外部车型组编号
     */
    @ApiModelProperty(value = "外部车型组编号", dataType = "string")
    private String wbcxzbh;
    /**
     * 外部车型组名称
     */
    @ApiModelProperty(value = "外部车型组名称", dataType = "string")
    private String wbcxzmc;

    /**
     * 控润规则（1.结算价，2.差价,3.建议价）
     */
    @ApiModelProperty(value = "控润规则（1.结算价，2.差价,3.建议价）", dataType = "string")
    private String ptkrgz;
    /**
     * 改派车次数
     */
    @ApiModelProperty(value = "改派车次数", dataType = "bigdecimal")
    private BigDecimal gpccs;

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
     */
    @ApiModelProperty(value = "供应实际订单金额(非一口价价格服务完成后供应计算出的最终实际订单金额)", dataType = "bigdecimal")
    private BigDecimal gySjddje;
    /**
     * 供应结算金额(CPS与供应结算金额)
     */
    @ApiModelProperty(value = "供应结算金额(CPS与供应结算金额)", dataType = "bigdecimal")
    private BigDecimal gyJsje;
    /**
     * 预订方式 1立即用车，2预约用车
     */
    @ApiModelProperty(value="预订方式",dataType = "String",required = false)
    private String ydfs;

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
     * 供应前后返（1，前返，2后返）
     */
    @ApiModelProperty(value = "供应前后返（1，前返，2后返）", dataType = "bigdecimal")
    private BigDecimal gyQhf;
    /**外部供应商编号*/
    @ApiModelProperty(value = "外部供应商编号")
    private String wbgysbh;
    /**外部供应商名称*/
    @ApiModelProperty(value = "外部供应商名称")
    private String wbgysmc;
    /**
     * 供应商下单类型 0或空-单个下单 1-批量下单
     */
    private String supplierBookType;
    /**
     * 真实供应商名称
     */
    private String realSupplierName;

    /**
     * 控润规则Id
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
    // 后面都是get set 所以没粘    
}