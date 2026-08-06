package cn.vetech.center.usecar.entity.usecar;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.base.BaseEntity;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 用车产品表
 * </p>
 *
 * @author chenyong
 * @since 2017-10-13
 */
@TableName("yc_cp")
public class YcCp extends BaseEntity {

    private static final long serialVersionUID = 1L;
    /**
     * 用车产品主键
     */
    @TableId("id")
    @ApiModelProperty(value = "用车产品主键", dataType = "string")
    private String id;
    /**
     * 产品类型ID  取自供应商授权表授权给的三级产品类型ID
     */
    @ApiModelProperty(value = "产品类型ID  取自供应商授权表授权给的三级产品类型ID", dataType = "string")
    private String cplxid;
    /**
     * 用车城市id  提供给供应商的可选城市从产品授权表里筛选
     */
    @ApiModelProperty(value = "用车城市id  提供给供应商的可选城市从产品授权表里筛选", dataType = "string")
    private String yccsid;
    /**
     * 用车站点ID  火车站或者机场的ID，备选数据要从产品授权表里进行筛选
     */
    @ApiModelProperty(value = "用车站点ID  火车站或者机场的ID，备选数据要从产品授权表里进行筛选", dataType = "string")
    private String yczdid;
    /**
     * 产品名称
     */
    @ApiModelProperty(value = "产品名称", dataType = "string")
    private String cpmc;
    /**
     * 实名匿名  	1实名，0匿名
     */
    @ApiModelProperty(value = "实名匿名  	1实名，0匿名", dataType = "bigdecimal")
    private BigDecimal smnm;
    /**
     * 所属车型组ID	从VE_YC_CXZ表中取主键  从VE_YC_CXZ表中取主键
     */
    @ApiModelProperty(value = "所属车型组ID	从VE_YC_CXZ表中取主键  从VE_YC_CXZ表中取主键", dataType = "string")
    private String cxzid;
    /**
     * 免费取消时限  比如：5，代表用车约定时之前5小时外)是可以免费取消的，过了这个时限收取订单费用。
     */
    @ApiModelProperty(value = "免费取消时限  比如：5，代表用车约定时之前5小时外)是可以免费取消的，过了这个时限收取订单费用。", dataType = "bigdecimal")
    private BigDecimal mfqxsx;
    /**
     * 到约定时间取消收取费用  Mfqxsx设定的时间到约定时间之间取消订单收取的取消费用，这里维护的数值是百分比，到时候按照这个百分比收取订单金额
     */
    @ApiModelProperty(value = "到约定时间取消收取费用  Mfqxsx设定的时间到约定时间之间取消订单收取的取消费用，这里维护的数值是百分比，到时候按照这个百分比收取订单金额", dataType = "bigdecimal")
    private BigDecimal dydsjsqfy;
    /**
     * 约定时间之后取消订单费率  约定时间之后再取消订单将收取费用
     */
    @ApiModelProperty(value = "约定时间之后取消订单费率  约定时间之后再取消订单将收取费用", dataType = "bigdecimal")
    private BigDecimal ydsjhfy;
    /**
     * 取消说明  页面上暂时不维护
     */
    @ApiModelProperty(value = "取消说明  页面上暂时不维护", dataType = "string")
    private String qxsm;
    /**
     * 服务城市ID
     */
    @ApiModelProperty(value = "服务城市ID", dataType = "string")
    private String fwcsid;
    /**
     * 服务区域ID
     */
    @ApiModelProperty(value = "服务区域ID", dataType = "string")
    private String fwqyid;
    /**
     * 服务内容  直接从YC_FWNRHBZ表里检索CPLXBD=本表cplxid且FWBZLX=1的所有服务内容数据，然后把信息拼接成串
     */
    @TableField("fwbz_bz")
    @ApiModelProperty(value = "服务内容  直接从YC_FWNRHBZ表里检索CPLXBD=本表cplxid且FWBZLX=1的所有服务内容数据，然后把信息拼接成串", dataType = "string")
    private String fwbzBz;
    /**
     * 服务标准  直接从YC_FWNRHBZ表里检索CPLXBD=本表cplxid且FWBZLX=2的所有服务内容数据，然后把信息拼接成串
     */
    @TableField("fwbaozhang_bz")
    @ApiModelProperty(value = "服务标准  直接从YC_FWNRHBZ表里检索CPLXBD=本表cplxid且FWBZLX=2的所有服务内容数据，然后把信息拼接成串", dataType = "string")
    private String fwbaozhangBz;
    /**
     * 至少提前预定时间  按小时计算
     */
    @ApiModelProperty(value = "至少提前预定时间  按小时计算", dataType = "bigdecimal")
    private BigDecimal zstqydsj;
    /**
     * 发布用户编号
     */
    @TableField("fb_yhbh")
    @ApiModelProperty(value = "发布用户编号", dataType = "string")
    private String fbYhbh;
    /**
     * 发布用户名称
     */
    @TableField("fb_yhmc")
    @ApiModelProperty(value = "发布用户名称", dataType = "string")
    private String fbYhmc;
    /**
     * 发布商户编号
     */
    @TableField("fb_shbh")
    @ApiModelProperty(value = "发布商户编号", dataType = "string")
    private String fbShbh;
    /**
     * 发布商户名称
     */
    @TableField("fb_shmc")
    @ApiModelProperty(value = "发布商户名称", dataType = "string")
    private String fbShmc;
    /**
     * 发布时间
     */
    @TableField("fb_datetime")
    @ApiModelProperty(value = "发布时间", dataType = "date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fbDatetime;
    /**
     * 最后修改用户编号  第一次发布的时候也保存发布人编号
     */
    @TableField("xg_yhbh")
    @ApiModelProperty(value = "最后修改用户编号  第一次发布的时候也保存发布人编号", dataType = "string")
    private String xgYhbh;
    /**
     * 最后修改时间  第一次保存发布时间
     */
    @TableField("xg_datetime")
    @ApiModelProperty(value = "最后修改时间  第一次保存发布时间", dataType = "date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date xgDatetime;
    /**
     * 最后修改人姓名
     */
    @TableField("xg_yhmc")
    @ApiModelProperty(value = "最后修改人姓名", dataType = "string")
    private String xgYhmc;
    /**
     * 上架状态  1：已上架0：已下架
     */
    @ApiModelProperty(value = "上架状态  1：已上架0：已下架", dataType = "bigdecimal")
    private BigDecimal sjzt;
    /**
     * 运营上架状态  1：已上架 0：已下架
     */
    @ApiModelProperty(value = "运营上架状态  1：已上架 0：已下架", dataType = "bigdecimal")
    private BigDecimal yysjzt;
    /**
     * 运营上下架操作人姓名
     */
    @ApiModelProperty(value = "运营上下架操作人姓名", dataType = "string")
    private String yysjyhmc;
    /**
     * 运营上下架操作人编号
     */
    @ApiModelProperty(value = "运营上下架操作人编号", dataType = "string")
    private String yysjyhbh;
    /**
     * 运营上下架操作时间
     */
    @TableField("yysj_datetime")
    @ApiModelProperty(value = "运营上下架操作时间", dataType = "date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date yysjDatetime;
    /**
     * 审核人员编号
     */
    @TableField("sh_hybh")
    @ApiModelProperty(value = "审核人员编号", dataType = "string")
    private String shHybh;
    /**
     * 审核人员姓名
     */
    @TableField("sh_hymc")
    @ApiModelProperty(value = "审核人员姓名", dataType = "string")
    private String shHymc;
    /**
     * 审核状态  1：已通过 0：待审核 -1：未通过
     */
    @TableField("sh_zt")
    @ApiModelProperty(value = "审核状态  1：已通过 0：待审核 -1：未通过", dataType = "bigdecimal")
    private BigDecimal shZt;
    /**
     * 其它备注信息
     */
    @ApiModelProperty(value = "其它备注信息", dataType = "string")
    private String qtbz;
    /**
     * 审核时间
     */
    @TableField("sh_datetime")
    @ApiModelProperty(value = "审核时间", dataType = "date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shDatetime;
    /**
     * 用车站点类型  用车站点类型 1：火车站 2：机场3:航站楼
     */
    @ApiModelProperty(value = "用车站点类型  用车站点类型 1：火车站 2：机场3:航站楼", dataType = "bigdecimal")
    private BigDecimal yczdlx;
    /**
     * 是否免费延误等待	接机，接站产品字段。采购提供航班号，车次号供应商是否免费等待(1:等待，0:不等待)
     */
    @ApiModelProperty(value = "是否免费延误等待	接机，接站产品字段。采购提供航班号，车次号供应商是否免费等待(1:等待，0:不等待)", dataType = "bigdecimal")
    private BigDecimal sfywdd;
    /**
     * 为提供等待时间	接机，接站未提供航班号，车次号等待时间/分钟
     */
    @ApiModelProperty(value = "为提供等待时间	接机，接站未提供航班号，车次号等待时间/分钟", dataType = "bigdecimal")
    private BigDecimal wtgddsj;
    /**
     * 订单最迟处理时间	供应商最迟对订单的处理时间/分钟
     */
    @ApiModelProperty(value = "订单最迟处理时间	供应商最迟对订单的处理时间/分钟", dataType = "bigdecimal")
    private BigDecimal gyqrddsj;
    /**
     * 提前待客时间	提前抵达待客时间
     */
    @ApiModelProperty(value = "提前待客时间	提前抵达待客时间", dataType = "bigdecimal")
    private BigDecimal tqdksj;
    /**
     * 1：是0：不是(高速过路费，停车费等待额外费用需)
     */
    @ApiModelProperty(value = "1：是0：不是(高速过路费，停车费等待额外费用需)", dataType = "bigdecimal")
    private BigDecimal bhlqf;
    /**
     * "备用排序1	备用排序1"
     */
    @ApiModelProperty(value = "备用排序1	备用排序1", dataType = "string")
    private String by1;
    /**
     * "备用排序1	备用排序2"
     */
    @ApiModelProperty(value = "备用排序1	备用排序2", dataType = "string")
    private String by2;
    /**
     * "备用排序1	备用排序3"
     */
    @ApiModelProperty(value = "备用排序1	备用排序3", dataType = "string")
    private String by3;
    /**
     * "备用排序1	备用排序4"
     */
    @ApiModelProperty(value = "备用排序1 备用排序4", dataType = "string")
    private String by4;
    /**
     * "备用排序1	备用排序5"
     */
    @ApiModelProperty(value = "备用排序1	备用排序5", dataType = "string")
    private String by5;
    /**
     * "备用排序1	备用排序6"
     */
    @ApiModelProperty(value = "备用排序1	备用排序6", dataType = "string")
    private String by6;
    /**
     * "备用：产品分类1	"
     */
    @ApiModelProperty(value = "备用：产品分类1	", dataType = "string")
    private String cpfl1;
    /**
     * "备用：产品分类2	"
     */
    @ApiModelProperty(value = "备用：产品分类2	", dataType = "string")
    private String cpfl2;
    /**
     * 备用：产品分类3
     */
    @ApiModelProperty(value = "备用：产品分类3", dataType = "string")
    private String cpfl3;
    /**
     * 备用：产品分类4
     */
    @ApiModelProperty(value = "备用：产品分类4", dataType = "string")
    private String cpfl4;
    /**
     * 备用：产品分类5
     */
    @ApiModelProperty(value = "备用：产品分类5", dataType = "string")
    private String cpfl5;
    /**
     * 备用：产品分类5
     */
    @ApiModelProperty(value = "备用：产品分类5", dataType = "string")
    private String cpfl6;
    /**
     * 是否对CPS销售（0.不对，1.对cps销售）
     */
    @ApiModelProperty(value = "是否对CPS销售（0.不对，1.对cps销售）", dataType = "string")
    private String sfxscps;
    /**
     * 采购单位编号
     */
    @TableField("cg_dwbh")
    @ApiModelProperty(value = "采购单位编号", dataType = "string")
    private String cgDwbh;
    /**
     * 采购单位名称
     */
    @TableField("cg_dwmc")
    @ApiModelProperty(value = "采购单位名称", dataType = "string")
    private String cgDwmc;
    /**
     * 用车城市名称
     */
    @ApiModelProperty(value = "用车城市名称", dataType = "string")
    private String yccsmc;
    /**
     * 用车站点名称
     */
    @ApiModelProperty(value = "用车站点名称", dataType = "string")
    private String yczdmc;
    /**
     * 产品类型名称
     */
    @ApiModelProperty(value = "产品类型名称", dataType = "string")
    private String cplxmc;
    /**
     * 车型组名称
     */
    @ApiModelProperty(value = "车型组名称", dataType = "string")
    private String cxzmc;

    /**
     * 服务城市名称
     */
    @ApiModelProperty(value = "服务城市名称", dataType = "string")
    private String fwcsmc;
    /**
     * 服务区域名称
     */
    @ApiModelProperty(value = "服务区域名称", dataType = "string")
    private String fwqymc;
    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
    // 后面都是get set 所以没粘
}