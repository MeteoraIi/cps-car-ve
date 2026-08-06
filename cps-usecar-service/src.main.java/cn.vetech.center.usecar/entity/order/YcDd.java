package cn.vetech.center.usecar.entity.order;

import cn.vetech.center.config.mybatisplus.cipher.annotation.Encrypted;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.base.BaseEntity;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用车订单
 * 
 * @author chenyong
 */
@TableName("yc_dd")
@Api("用车正常单")
public class YcDd extends BaseEntity {
  private static final long serialVersionUID = 1L;
  /*** 订单编号(主键) */
  @TableId("ddbh")
  @ApiModelProperty(value = "订单编号(主键)", dataType = "string")
  private String ddbh;
  /*** 下单时间 */
  @ApiModelProperty(value = "下单时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date xdsj;
  /**
   * 10000101：标准(接口)接机，10000102：豪华接机，10000201：标准(接口)送机，
   * 10000202：豪华送机，10000301：标准接火车，10000302：豪华接火车10000401：
   * 标准送火车，10000402：豪华送火车10000501：专(快车)
   * 代驾10001201
   */
  @ApiModelProperty(value = "订单类型", dataType = "string")
  private String ddlx;
  /*** 订单状态(参考VE_DDZT 表lx=‘10‘的订单状态) */
  @ApiModelProperty(value = "订单状态(参考VE_DDZT 表lx=‘10‘的订单状态)", dataType = "string")
  private String ddzt;
  /*** 产品id(自签产品有ID) */
  @ApiModelProperty(value = "产品id(自签产品有ID)", dataType = "string")
  private String cpid;
  /*** 用车时间(2016/1/21 15:30 精确到小时分钟) */
  @ApiModelProperty(value = "用车时间(2016/1/21 15:30 精确到小时分钟)", dataType = "string")
  private String ycsj;
  /*** 乘客姓名 */
  @ApiModelProperty(value = "乘客姓名", dataType = "string")
  private String ckxm;
  /*** 乘客手机 */
  @Encrypted
  @ApiModelProperty(value = "乘客手机", dataType = "string")
  private String cksj;
  /*** 站点所在城市编号(接送产品) */
  @TableField("cfd_csid")
  @ApiModelProperty(value = "站点所在城市编号(接送产品)", dataType = "string")
  private String cfdCsid;
  /*** 出发城市名称 */
  @TableField("cfd_csmc")
  @ApiModelProperty(value = "出发地城市名称", dataType = "string")
  private String cfdCsmc;
  /*** 站点编号(接送产品) */
  @ApiModelProperty(value = "站点编号(接送产品)", dataType = "string")
  private String jsfwzdid;
  /*** 站点名称 */
  @ApiModelProperty(value = "站点名称(接送产品)", dataType = "string")
  private String jsfwzdmc;
  /*** 服务城市编号(接送产品) */
  @TableField("mdd_csid")
  @ApiModelProperty(value = "服务城市编号(接送产品)", dataType = "string")
  private String mddCsid;
  /*** 目的的城市名称 */
  @TableField("mdd_csmc")
  @ApiModelProperty(value = "目的地城市名称", dataType = "string")
  private String mddCsmc;
  /*** 服务城市区域编号(接送产品) */
  @ApiModelProperty(value = "服务城市区域编号(接送产品)", dataType = "string")
  private String jsfwqyid;
  /*** 车型组编号 */
  @ApiModelProperty(value = "车型组编号", dataType = "string")
  private String cxzbh;
  /**
   * 车型组名称
   */
  @ApiModelProperty(value = "车型组名称", dataType = "string")
  private String cxzmc;
  /**
   * 航班/车次号(航班编号或者火车车次号)
   */
  @ApiModelProperty(value = "航班/车次号(航班编号或者火车车次号)", dataType = "string")
  private String hbcch;
  /*** 舱位编号(辉腾需要) */
  @ApiModelProperty(value = "舱位编号(辉腾需要)", dataType = "string")
  private String cwbh;
  /*** 订单备注 */
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
   * 车辆编号(京.P2913D1)
   */
  @ApiModelProperty(value = "车辆编号(京.P2913D1)", dataType = "string")
  private String cph;
  /**
   * 车身颜色(白色)
   */
  @ApiModelProperty(value = "车身颜色(白色)", dataType = "string")
  private String csys;
  /**
   * 车型名称(新桑塔纳)
   */
  @ApiModelProperty(value = "车型名称(新桑塔纳)", dataType = "string")
  private String cxmc;
  /**
   * 司机id
   */
  @ApiModelProperty(value = "司机id", dataType = "string")
  private String sjid;
  /**
   * 司机姓名
   */
  @ApiModelProperty(value = "司机姓名", dataType = "string")
  private String sjxm;
  /**
   * 司机电话
   */
  @Encrypted
  @ApiModelProperty(value = "司机电话", dataType = "string")
  private String sjdh;
  /**
   * 司机性别(1:男 2:女)
   */
  @ApiModelProperty(value = "司机性别(1:男 2:女)", dataType = "string")
  private String sjxb;
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
  /*** 采购用户编号 */
  @TableField("cg_yhbh")
  @ApiModelProperty(value = "采购用户编号", dataType = "string")
  private String cgYhbh;
  /*** 采购商户编号 */
  @TableField("cg_shbh")
  @ApiModelProperty(value = "采购商户编号", dataType = "string")
  private String cgShbh;
  /*** 采购商户简称 */
  @TableField("cg_shjc")
  @ApiModelProperty(value = "采购商户简称", dataType = "string")
  private String cgShjc;
  /*** 采购取消人 */
  @TableField("cg_qxr")
  @ApiModelProperty(value = "采购取消人", dataType = "string")
  private String cgQxr;
  /*** 采购取消原因 */
  @TableField("cg_qxyy")
  @ApiModelProperty(value = "采购取消原因", dataType = "string")
  private String cgQxyy;
  /*** 采购取消时间 */
  @TableField("cg_qxsj")
  @ApiModelProperty(value = "采购取消时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date cgQxsj;
  /*** 采购投诉人 */
  @TableField("cg_tsr")
  @ApiModelProperty(value = "采购投诉人", dataType = "string")
  private String cgTsr;
  /*** 采购投诉时间 */
  @TableField("cg_tssj")
  @ApiModelProperty(value = "采购投诉时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date cgTssj;
  /**
   * 取消规则(自有产品依据产品表取消规则字段组合成一句话存入这里，接口方订单存入接口方固定的取消规则)
   */
  @ApiModelProperty(value = "取消规则(自有产品依据产品表取消规则字段组合成一句话存入这里，接口方订单存入接口方固定的取消规则)", dataType = "string")
  private String qxgz;
  /**
   * 取消公式(自签产品 0,100%:50%:48,25%) 0，100%：50%：免费取消时限*60 0%
   */
  @ApiModelProperty(value = "取消公式(自签产品 0,100%:50%:48,25%)  0，100%：50%：免费取消时限*60  0%", dataType = "string")
  private String qxgs;
  /**
   * 供应商订单号(外部第三方订单编号)
   */
  @TableField("gy_ddbh")
  @ApiModelProperty(value = "供应商订单号(外部第三方订单编号)", dataType = "string")
  private String gyDdbh;
  /**
   * 供应商商户编号(包括龙腾，神州，滴滴也会做为商户进入系统)
   */
  @TableField("gy_shbh")
  @ApiModelProperty(value = "供应商商户编号(包括龙腾，神州，滴滴也会做为商户进入系统)", dataType = "string")
  private String gyShbh;
  /**
   * 供应商户简称
   */
  @TableField("gy_shjc")
  @ApiModelProperty(value = "供应商户简称", dataType = "string")
  private String gyShjc;
  /**
   * 供应拒单人
   */
  @TableField("gy_judr")
  @ApiModelProperty(value = "供应拒单人", dataType = "string")
  private String gyJudr;
  /**
   * 供应拒单时间
   */
  @TableField("gy_judsj")
  @ApiModelProperty(value = "供应拒单时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyJudsj;
  /**
   * 供应拒单原因(存储描述信息)
   */
  @TableField("gy_judyy")
  @ApiModelProperty(value = "供应拒单原因(存储描述信息)", dataType = "string")
  private String gyJudyy;
  /**
   * 供应派车人
   */
  @TableField("gy_pcr")
  @ApiModelProperty(value = "供应派车人", dataType = "string")
  private String gyPcr;
  /**
   * 供应商派车时间
   */
  @TableField("gy_pcsj")
  @ApiModelProperty(value = "供应商派车时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyPcsj;
  /**
   * 供应服务完成人
   */
  @TableField("gy_fwwcr")
  @ApiModelProperty(value = "供应服务完成人", dataType = "string")
  private String gyFwwcr;
  /**
   * 供应服务完成时间
   */
  @TableField("gy_fwwcsj")
  @ApiModelProperty(value = "供应服务完成时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyFwwcsj;
  /**
   * 付款方式(1：预存款抵扣)
   */
  @ApiModelProperty(value = "付款方式(1：预存款抵扣)", dataType = "string")
  private String fkfs;
  /**
   * 付款时间
   */
  @ApiModelProperty(value = "付款时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date fksj;
  /**
   * 付款账号
   */
  @ApiModelProperty(value = "付款账号", dataType = "string")
  private String fkzh;
  /**
   * 付款流水号
   */
  @ApiModelProperty(value = "付款流水号", dataType = "string")
  private String fkdh;
  /**
   * 支付金额(神州双倍支付预估款)
   */
  @ApiModelProperty(value = "支付金额(神州双倍支付预估款)", dataType = "bigdecimal")
  private BigDecimal fkje;
  /**
   * 预估金额(采购支付预估金额)
   */
  @ApiModelProperty(value = "预估金额(采购支付预估金额)", dataType = "bigdecimal")
  private BigDecimal ygje;
  /**
   * 采购返佣方式(采购返利方式2:百分比1:数值 3：无返佣)
   */
  @TableField("cg_fyfs")
  @ApiModelProperty(value = "采购返佣方式(采购返利方式2:百分比1:数值 3：无返佣)", dataType = "bigdecimal")
  private BigDecimal cgFyfs;
  /**
   * 采购返佣值(百分百值或者是具体数值)
   */
  @TableField("cg_fybl")
  @ApiModelProperty(value = "采购返佣值(百分百值或者是具体数值)", dataType = "bigdecimal")
  private BigDecimal cgFybl;
  /**
   * 采购返佣金额
   */
  @TableField("cg_fyje")
  @ApiModelProperty(value = "采购返佣金额", dataType = "bigdecimal")
  private BigDecimal cgFyje;
  /**
   * 采购结算金额(供应确认价格后计算)
   */
  @TableField("cg_jsje")
  @ApiModelProperty(value = "采购结算金额(供应确认价格后计算)", dataType = "bigdecimal")
  private BigDecimal cgJsje;
  /**
   * 平台控润方式(2:百分比1:数值 3：无返佣)
   */
  @ApiModelProperty(value = "平台控润方式(2:百分比1:数值 3：无返佣)", dataType = "bigdecimal")
  private BigDecimal ptkrfs;
  /**
   * 平台控润值(百分比或者具体数值) 例 5% 存 5 数值 10 存10
   */
  @ApiModelProperty(value = "平台控润值(百分比或者具体数值) 例 5% 存 5  数值 10 存10", dataType = "bigdecimal")
  private BigDecimal ptkrbl;
  /**
   * 平台控润金额(控润金额,平台利润)
   */
  @ApiModelProperty(value = "平台控润金额(控润金额,平台利润)", dataType = "bigdecimal")
  private BigDecimal ptkrje;
  /**
   * 手续费扣费方式(1：按比例2：按单)
   */
  @ApiModelProperty(value = "手续费扣费方式(1：按比例2：按单)", dataType = "bigdecimal")
  private BigDecimal ptsxffs;
  /**
   * 平台手续费率
   */
  @ApiModelProperty(value = "平台手续费率", dataType = "bigdecimal")
  private BigDecimal ptsxfl;
  /**
   * 平台手续费
   */
  @ApiModelProperty(value = "平台手续费", dataType = "bigdecimal")
  private BigDecimal ptsxf;
  /**
   * 平台支付费率
   */
  @ApiModelProperty(value = "平台支付费率", dataType = "bigdecimal")
  private BigDecimal ptzffl;
  /**
   * 平台支付手续费
   */
  @ApiModelProperty(value = "平台支付手续费", dataType = "bigdecimal")
  private BigDecimal ptzfsxf;
  /**
   * 供应返佣方式(2:百分比1:数值 3：无返佣)
   */
  @TableField("gy_fyfs")
  @ApiModelProperty(value = "供应返佣方式(2:百分比1:数值 3：无返佣)", dataType = "bigdecimal")
  private BigDecimal gyFyfs;
  /**
   * 供应返佣值(返佣比例或者钱的值)
   */
  @TableField("gy_fybl")
  @ApiModelProperty(value = "供应返佣值(返佣比例或者钱的值)", dataType = "bigdecimal")
  private BigDecimal gyFybl;
  /**
   * 供应返佣金额
   */
  @TableField("gy_fyje")
  @ApiModelProperty(value = "供应返佣金额", dataType = "bigdecimal")
  private BigDecimal gyFyje;
  /**
   * 供应实际订单金额
   */
  @TableField("gy_sjddje")
  @ApiModelProperty(value = "供应实际订单金额(非一口价价格服务完成后，供应计算出的最终实际订单金额)", dataType = "bigdecimal")
  private BigDecimal gySjddje;
  /**
   * 供应结算金额(CPS与供应结算金额)
   */
  @TableField("gy_jsje")
  @ApiModelProperty(value = "供应结算金额(CPS与供应结算金额)", dataType = "bigdecimal")
  private BigDecimal gyJsje;
  /**
   * 供应商接单人
   */
  @TableField("gy_jdr")
  @ApiModelProperty(value = "供应商接单人", dataType = "string")
  private String gyJdr;
  /**
   * 供应商接单时间
   */
  @TableField("gy_jdsj")
  @ApiModelProperty(value = "供应商接单时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyJdsj;
  /**
   * 出发的POI
   */
  @ApiModelProperty(value = "出发的POI", dataType = "string")
  private String cfd;
  /**
   * 出发地详细地址
   */
  @TableField("cfd_xxdz")
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
  @TableField("mdd_xxdz")
  @ApiModelProperty(value = "目的地详细地址", dataType = "string")
  private String mddXxdz;
  /**
   * 接机服务预估上车时间(航班抵达后大概多少分钟上车)，需要客户下单的时候录入
   */
  @ApiModelProperty(value = "接机服务预估上车时间(航班抵达后大概多少分钟上车)，需要客户下单的时候录入", dataType = "bigdecimal")
  private BigDecimal jjygscsj;
  /**
   * 电子票号，机票出票成功后的票号
   */
  @ApiModelProperty(value = "电子票号，机票出票成功后的票号", dataType = "string")
  private String dzph;
  /**
   * 退单申请审核人( 对采购商的退单申请进行审核的供应商人员)
   */
  @TableField("gy_tdshr")
  @ApiModelProperty(value = "退单申请审核人(	对采购商的退单申请进行审核的供应商人员)", dataType = "string")
  private String gyTdshr;
  /**
   * 退单申请审核时间
   */
  @TableField("gy_tdshsj")
  @ApiModelProperty(value = "退单申请审核时间", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyTdshsj;
  /**
   * 退款手续费退款产生的损失费用(不包括返佣费用)
   */
  @ApiModelProperty(value = "退款手续费退款产生的损失费用(不包括返佣费用)", dataType = "bigdecimal")
  private BigDecimal tksxf;
  /**
   * 航站楼(接送产品)
   */
  @ApiModelProperty(value = "航站楼(接送产品)", dataType = "string")
  private String jsfwhzl;
  /**
   * 采购支付时限(供应确认接单后开始按照供应商承诺时间进行计算)
   */
  @TableField("cg_zfsx")
  @ApiModelProperty(value = "采购支付时限(供应确认接单后开始按照供应商承诺时间进行计算)", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date cgZfsx;
  /**
   * 供应确认接单时限(供应商确认接单时限，从采购下单采购后开始计算得出)
   */
  @TableField("gy_qrjdsx")
  @ApiModelProperty(value = "供应确认接单时限(供应商确认接单时限，从采购下单采购后开始计算得出)", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyQrjdsx;
  /**
   * 供应审核退单时限( 采购商申请退单后，供应商申请退单时限，从采购申请退单开始计算得出)
   */
  @TableField("gy_shtdsx")
  @ApiModelProperty(value = "供应审核退单时限(	采购商申请退单后，供应商申请退单时限，从采购申请退单开始计算得出)", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date gyShtdsx;
  /**
   * 司机承诺提前到达时间( 数值是按分钟计算,参考下单指定的用车时间)
   */
  @TableField("gy_sjtqddsj")
  @ApiModelProperty(value = "司机承诺提前到达时间(	数值是按分钟计算,参考下单指定的用车时间)", dataType = "bigdecimal")
  private BigDecimal gySjtqddsj;
  /**
   * 司机到达后免费等待时间(数值是按分钟计算,参考下单指定的用车时间)
   */
  @TableField("gy_sjmfddsj")
  @ApiModelProperty(value = "司机到达后免费等待时间(数值是按分钟计算,参考下单指定的用车时间)", dataType = "bigdecimal")
  private BigDecimal gySjmfddsj;
  /**
   * 采购退单申请人( 退单申请人编号)
   */
  @TableField("cg_tdsqr")
  @ApiModelProperty(value = "采购退单申请人(	退单申请人编号)", dataType = "string")
  private String cgTdsqr;
  /**
   * 采购退单申请时间 (yyyy-MM-dd HH:mm:ss)
   */
  @TableField("cg_tdsqsj")
  @ApiModelProperty(value = "采购退单申请时间	(yyyy-MM-dd HH:mm:ss)", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date cgTdsqsj;
  /**
   * 联系电话(订单联系人电话)
   */
  @Encrypted
  @ApiModelProperty(value = "联系电话(订单联系人电话)", dataType = "string")
  private String lxrdh;
  @ApiModelProperty(value = "联系人(订单联系人姓名)", dataType = "string")
  private String lxr;
  /**
   * 订单取消备注说明(主要存储外部接口产品的产品取消说明信息，也可能是多个字段拼接结果)
   */
  @ApiModelProperty(value = "订单取消备注说明(主要存储外部接口产品的产品取消说明信息，也可能是多个字段拼接结果)", dataType = "string")
  private String qxbz;
  /**
   * 订单预订规则说明(主要存储外部接口产品的产品预订规则说明信息，也可能是多个字段拼接结果)
   */
  @ApiModelProperty(value = "订单预订规则说明(主要存储外部接口产品的产品预订规则说明信息，也可能是多个字段拼接结果)", dataType = "string")
  private String ydgz;

  /** 产品备注信息(外部接口的产品备注或者自签产品的备注信息) */
  @ApiModelProperty(value = "产品备注信息(外部接口的产品备注或者自签产品的备注信息)", dataType = "string")
  private String cpbz;
  /** 产品价格说明(主要存储外部接口产品的产品价格说明信息，也可能是多个字段拼接的结果) */
  @ApiModelProperty(value = "产品价格说明(主要存储外部接口产品的产品价格说明信息，也可能是多个字段拼接的结果)", dataType = "string")
  private String jgsm;
  /** 支付状态，0.未支付，1.已支付 */
  @TableField("zf_zt")
  @ApiModelProperty(value = "支付状态，0.未支付，1.已支付", dataType = "string")
  private String zfZt;
  /** 内部支付交易流水号 */
  @TableField("zf_lsh_nb")
  @ApiModelProperty(value = "内部支付交易流水号", dataType = "string")
  private String zfLshNb;
  /** 外部交易流水号,对应zf_ptfkjlb_zffs表中正常成功支付记录的外部交易流水号 */
  @TableField("zf_jylsh")
  @ApiModelProperty(value = "外部交易流水号,对应zf_ptfkjlb_zffs表中正常成功支付记录的外部交易流水号", dataType = "string")
  private String zfJylsh;
  /** 支付平台单号 */
  @TableField("zf_ptdh")
  @ApiModelProperty(value = "支付平台单号", dataType = "string")
  private String zfPtdh;
  /** 退款流水号 */
  @ApiModelProperty(value = "退款流水号", dataType = "string")
  private String tklsh;
  /** 平台支付设置ID */
  @ApiModelProperty(value = "平台支付设置ID", dataType = "string")
  private String ptzfszid;
  /** 出发地经度 */
  @TableField("cfd_x")
  @ApiModelProperty(value = "出发地经度", dataType = "string")
  private String cfdX;
  /** 出发地纬度 */
  @TableField("cfd_y")
  @ApiModelProperty(value = "出发地纬度", dataType = "string")
  private String cfdY;
  /** 目的地经度 */
  @TableField("mdd_x")
  @ApiModelProperty(value = "目的地经度", dataType = "string")
  private String mddX;
  /** 目的地纬度 */
  @TableField("mdd_y")
  @ApiModelProperty(value = "目的地纬度", dataType = "string")
  private String mddY;
  /** 供应拒单商户编号 */
  @TableField("gy_judshbh")
  @ApiModelProperty(value = "供应拒单商户编号", dataType = "string")
  private String gyJudshbh;
  /** 供应拒单人编号 */
  @TableField("gy_judrbh")
  @ApiModelProperty(value = "供应拒单人编号", dataType = "string")
  private String gyJudrbh;
  /** 建议销售金额 */
  @ApiModelProperty(value = "建议销售金额", dataType = "bigdecimal")
  private BigDecimal jyxsje;
  /** 采购系统订单编号 */
  @TableField("cg_ddbh")
  @ApiModelProperty(value = "采购系统订单编号", dataType = "string")
  private String cgDdbh;
  /** 供应退款手续费 */
  @TableField("gy_tksxf")
  @ApiModelProperty(value = "供应退款手续费", dataType = "bigdecimal")
  private BigDecimal gyTksxf;
  /** 应付金额 (下订单时计算出来的，应该支付的金额) */
  @ApiModelProperty(value = "应付金额 (下订单时计算出来的，应该支付的金额)", dataType = "bigdecimal")
  private BigDecimal yfje;
  /** 处理效率 */
  @ApiModelProperty(value = "处理效率", dataType = "string")
  private String clxl;
  /** 退款效率 */
  @ApiModelProperty(value = "退款效率", dataType = "string")
  private String tkxl;
  /** 是否一口价（0.非一口价，1.一口价） */
  @ApiModelProperty(value = "是否一口价（0.非一口价，1.一口价）", dataType = "string")
  private String sfykj;
  /** 丽程订单必须参数 */
  @ApiModelProperty(value = "丽程订单必须参数", dataType = "string")
  private String carmodelid;
  /** 供应成本价(自签产品的需要记录存发布的成本价，接口的产品可以不用记录) */
  @TableField("gy_cbj")
  @ApiModelProperty(value = "供应成本价(自签产品的需要记录存发布的成本价，接口的产品可以不用记录)", dataType = "bigdecimal")
  private BigDecimal gyCbj;
  /** 价格MD5 */
  @ApiModelProperty(value = "价格MD5", dataType = "string")
  private String jgmd5;
  /** 计价模式类别 （201专车，301快车） */
  @ApiModelProperty(value = "计价模式类别 （201专车，301快车）", dataType = "string")
  private String jjmslb;
  /** 外部车型组编号 */
  @ApiModelProperty(value = "外部车型组编号", dataType = "string")
  private String wbcxzbh;
  /** 外部车型组名称 */
  @ApiModelProperty(value = "外部车型组名称", dataType = "string")
  private String wbcxzmc;
  /** 实际上车地址 */
  @ApiModelProperty(value = "实际上车地址", dataType = "string")
  private String sjscdz;
  /** 实际下车地址 */
  @ApiModelProperty(value = "实际下车地址", dataType = "string")
  private String sjxcdz;
  /** 司机坐标X */
  @TableField("sjzb_x")
  @ApiModelProperty(value = "司机坐标X", dataType = "string")
  private String sjzbX;
  /** 司机坐标Y */
  @TableField("sjzb_y")
  @ApiModelProperty(value = "司机坐标Y", dataType = "string")
  private String sjzbY;
  /** 实际上车时间 */
  @ApiModelProperty(value = "实际上车时间", dataType = "string")
  private String sjscsj;
  /** 行程结束时间 */
  @ApiModelProperty(value = "行程结束时间", dataType = "string")
  private String xcjssj;
  /** 司机评分 */
  @ApiModelProperty(value = "司机评分", dataType = "string")
  private String sjpf;
  /** 司机工龄 */
  @ApiModelProperty(value = "司机工龄", dataType = "string")
  @TableField("driver_year")
  private String driverYear;
  /** 控润规则（1.结算价，2.差价,3.建议价） */
  @ApiModelProperty(value = "控润规则（1.结算价，2.差价,3.建议价）", dataType = "string")
  private String ptkrgz;
  /** 改派车次数 */
  @ApiModelProperty(value = "改派车次数", dataType = "bigdecimal")
  private BigDecimal gpccs;
  /** 航班车次出发时间（2017-09-12 10:15) */
  @ApiModelProperty(value = "航班车次出发时间（2017-09-12 10:15)", dataType = "string")
  private String hbcccfsj;
  /** 航班车次到达时间（2017-09-12 10:15) */
  @ApiModelProperty(value = "航班车次到达时间（2017-09-12 10:15)", dataType = "string")
  private String hbccddsj;
  /** 数据库路由 */
  @ApiModelProperty(value = "数据库路由", dataType = "string")
  private String sjkly;
  /** 数据版本号 */
  @Version
  @ApiModelProperty(value = "数据版本号", dataType = "Long")
  private Long version;
  /** 平台贴点方式(1.金额,2.百分比) */
  @ApiModelProperty(value = "平台贴点方式(1.金额,2.百分比)", dataType = "string")
  private String pttdfs;
  /** 平台贴点比例（方式为百分比时，存值为5，表示5%，或为金额则为实际的值） */
  @ApiModelProperty(value = "平台贴点比例（方式为百分比时，存值为5，表示5%，或为金额则为实际的值）", dataType = "bigdecimal")
  private BigDecimal pttdbl;
  /** 平台贴点金额 */
  @ApiModelProperty(value = "平台贴点金额", dataType = "bigdecimal")
  private BigDecimal pttdje;
  /** 特殊需求 */
  @ApiModelProperty(value = "特殊需求", dataType = "string")
  private String tsxq;
  /** 司机坐标Y */
  @TableField("xg_ly")
  @ApiModelProperty(value = "修改来源", dataType = "string")
  private String xgly;
  /** 司机坐标Y */
  @TableField("fk_userid")
  @ApiModelProperty(value = "付款用户编号", dataType = "string")
  private String fkUserid;
  /** 供应前后返（1，前返，2后返） */
  @TableField("gy_qhf")
  @ApiModelProperty(value = "供应前后返（1，前返，2后返）", dataType = "bigdecimal")
  private BigDecimal gyQhf;
  /** 预约类型 1 预约用车 2及时用车 */
  @TableField("yy_lx")
  @ApiModelProperty(value = "预约类型 1 预约用车 2及时用车", dataType = "string")
  private String yyLx;
  /** 付款方式名称 */
  @ApiModelProperty(value = "付款方式名称", dataType = "string")
  private String fkfsmc;
  /** 预约类型 1 预约用车 2及时用车 */
  @TableField("fz_datetime")
  @ApiModelProperty(value = "分账时间 (yyyy-MM-dd HH:mm:ss)", dataType = "date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date fzDatetime;
  /** 采购订单来源 */
  @TableField("cg_ddly")
  @ApiModelProperty(value = "采购订单来源", dataType = "string")
  private String cgDdly;
  /** 初始预估金额 */
  @TableField("csygje")
  @ApiModelProperty(value = "初始预估金额", dataType = "string")
  private BigDecimal csygje;
  /** 服务保障级别 */
  @TableField("fwbzjb")
  @ApiModelProperty(value = "服务保障级别", dataType = "string")
  private String fwbzjb;
  /** 服务保障标准 */
  @TableField("fwbzbz")
  @ApiModelProperty(value = "服务保障标准", dataType = "string")
  private String fwbzbz;
  /** 服务保障标准 */
  @TableField("gldh")
  @ApiModelProperty(value = "关联单号", dataType = "string")
  private String gldh;
  /** 商户性质编号 */
  @ApiModelProperty(value = "商户性质编号", dataType = "string")
  private String shxz;
  /** 商户性质名称 */
  @ApiModelProperty(value = "商户性质名称", dataType = "string")
  private String shxzmc;
  /** 操作类型(特殊字段非数据库字段) */
  @TableField(exist = false)
  private String tszdczlx;
  /** 供应支付方式 */
  @TableField("gy_zffs")
  @ApiModelProperty(value = "供应支付方式", dataType = "string")
  private String gyZffs;
  /** 供应支付方式名称 */
  @TableField("gy_zffsmc")
  @ApiModelProperty(value = "供应支付方式名称", dataType = "string")
  private String gyZffsmc;
  /** 供应对账单号 */
  @TableField("gy_dzdh")
  @ApiModelProperty(value = "供应对账单号", dataType = "string")
  private String gyDzdh;
  /** 支付来源（记录手机支付、网页端支付） */
  @TableField("zf_ly")
  @ApiModelProperty(value = "支付来源（记录手机支付、网页端支付 0 是 PC 1 是 APP）", dataType = "string")
  private String zfLy;
  /** 因公：1;因私：2 */
  @ApiModelProperty(value = "因公：1;因私：2", dataType = "string")
  private String clyy;
  /** 服务商编号 */
  @ApiModelProperty(value = "服务商编号", dataType = "string")
  private String fwsbh;
  /** 服务商名称 */
  @ApiModelProperty(value = "服务商名称", dataType = "string")
  private String fwsmc;
  /** 退款状态，正常单多退的情况，多退给采购的状态 */
  @ApiModelProperty(value = "退款状态")
  private String tkzt;
  /** 退款时间 yyyy-MM-dd HH:mm:ss */
  @ApiModelProperty(value = "退款时间")
  private Date tksj;
  /** 付费规则 */
  @ApiModelProperty(value = "付费规则:0先支付后用车1先用车后支付,默认0")
  private String ffgz;
  /** 是否主单 */
  @ApiModelProperty(value = "是否主单:0-否 1-是,默认-是")
  private String sfzb;
  /** 费控主表订单编号 */
  @ApiModelProperty(value = "主表订单编号")
  private String zbddbh;
  /** 外部供应商编号 */
  @ApiModelProperty(value = "外部供应商编号")
  private String wbgysbh;
  /** 外部供应商名称 */
  @ApiModelProperty(value = "外部供应商名称")
  private String wbgysmc;
  /** 支付手续费 */
  @ApiModelProperty(value = "支付手续费")
  private BigDecimal zfsxf;
  /**
   * 一键三单 主订单号
   */
  @ApiModelProperty(value = "主订单号", dataType = "string")
  @TableField("p_ddbh")
  private String pDdbh;
  /**
   * 代扣支付流水号
   */
  @ApiModelProperty(value = "代扣支付流水号", dataType = "string")
  private String dkzflsh;
  /**
   * 代扣退款流水号
   */
  @ApiModelProperty(value = "代扣退款流水号", dataType = "string")
  private String dktklsh;
  /**
   * 预定人id
   */
  @TableField("booker_id")
  @ApiModelProperty(value = "预定人id", dataType = "String")
  private String bookerId;
  /**
   * 预定人部门id
   */
  @TableField("booker_dept_id")
  @ApiModelProperty(value = "预订人部门id", dataType = "String")
  private String bookerDeptId;
  /**
   * 供应商预定id
   */
  @TableField("supplier_book_id")
  @ApiModelProperty(value = "预订人部门id", dataType = "String")
  private String supplierBookId;
  /**
   * 预定人部门名称
   */
  @TableField("booker_dept_name")
  @ApiModelProperty(value = "预定人部门名称", dataType = "String")
  private String bookerDeptName;
  /**
   * 预定人编号
   */
  @TableField("booker_no")
  @ApiModelProperty(value = "预定人编号", dataType = "String")
  private String bookerNo;
  /**
   * 预定人姓名
   */
  @TableField("booker_name")
  @ApiModelProperty(value = "预定人姓名", dataType = "String")
  private String bookerName;
  /**
   * 部门全路径名称
   */
  @TableField("full_dept_name")
  @ApiModelProperty(value = "部门全路径名称", dataType = "String")
  private String fullDeptName;
  /**
   * 结算单位编号
   */
  @TableField("settle_dept_no")
  @ApiModelProperty(value = "结算单位编号", dataType = "String")
  private String settleDeptNo;
  /**
   * 结算单位名称
   */
  @TableField("settle_dept_name")
  @ApiModelProperty(value = "结算单位名称", dataType = "String")
  private String settleDeptName;
  /**
   * 结算单位id
   */
  @TableField("settle_dept_id")
  @ApiModelProperty(value = "结算单位id", dataType = "string")
  private String settleDeptId;
  /**
   * 项目编号
   */
  @TableField("project_no")
  @ApiModelProperty(value = "项目编号", dataType = "String")
  private String projectNo;
  /**
   * 项目名称
   */
  @TableField("project_name")
  @ApiModelProperty(value = "项目名称", dataType = "String")
  private String projectName;
  /**
   * 项目id
   */
  @TableField("project_id")
  @ApiModelProperty(value = "项目id", dataType = "String")
  private String projectId;
  /**
   * 异常单号
   */
  @TableField("error_order_no")
  @ApiModelProperty(value = "异常单号", dataType = "string")
  private String errorOrderNo;
  /**
   * 异常单状态
   */
  @TableField("error_order_status")
  @ApiModelProperty(value = "异常单状态", dataType = "string")
  private String errorOrderStatus;
  /**
   * 混合支付
   */
  @TableField("mixed_pay")
  @ApiModelProperty(value = "混合支付", dataType = "string")
  private String mixedPay;

  /**
   * 资金对账状态 0-未对账 1-对账成功 2-对账失败
   */
  @ApiModelProperty(value = "资金对账状态 0-未对账 1-对账成功 2-对账失败", dataType = "string")
  @TableField("capital_status")
  private String capitalStatus;

  /**
   * 资金对账id
   */
  @ApiModelProperty(value = "资金对账", dataType = "String")
  @TableField("capital_id")
  private String capitalId;

  /**
   * 资金对账时间
   */
  @ApiModelProperty(value = "资金对账时间", dataType = "string")
  @TableField("capital_time")
  private Date capitalTime;

  @TableField(value = "manual_order")
  @ApiModelProperty(value = "是否手工单", dataType = "string")
  private String manualOrder;

  /**
   * 手机尾号校验
   */
  @TableField("tail_check_number")
  private String tailCheckNumber;
  /**
   * 旅游报价单类型
   */
  private String lybjdlx;
  /**
   * 旅游报价单号
   */
  private String lybjdh;
  /**
   * 结算方式
   */
  private String jsfs;
  /**
   * 是否开启24小时自动支付
   */
  @TableField("auto_pay")
  @ApiModelProperty(value = "是否24小时自动支付", dataType = "String")
  private String autoPay;

  /**
   * 自动支付模式 0-立即 24-24小时 48-48小时
   */
  @TableField("auto_pay_type")
  @ApiModelProperty(value = "自动支付模式 0-立即 24-24小时 48-48小时", dataType = "String")
  private String autoPayType;

  /**
   * 是否在线支付 0-否 1-是
   */
  @TableField("pay_online")
  @ApiModelProperty(value = "是否在线支付 0-否 1-是", dataType = "String")
  private String payOnline;
  /**
   * 超标金额
   */
  @TableField("exceed_amount")
  @ApiModelProperty(value = "超标金额", dataType = "bigdecimal")
  private BigDecimal exceedAmount;
  /**
   * 采购预估价
   */
  @ApiModelProperty(value = "采购预估价", dataType = "bigdecimal")
  @TableField("buyer_estimated_price")
  private BigDecimal buyerEstimatedPrice;

  /**
   * 服务商服务费
   */
  @TableField("fwsfwf")
  private BigDecimal fwsfwf;
  /**
   * 支付是否带上服务商服务费 1:是
   */
  @TableField("with_fwf")
  private String withFwf;

  /**
   * 服务商退服务费
   */
  private BigDecimal fwstfwf;

  /**
   * 是否按比例
   */
  @TableField("by_ratio")
  private String byRatio;

  /**
   * 服务费计算系数
   */
  @TableField("fwf_calc_numb")
  private String fwfCalcNumb;

  /**
   * 服务费收费模式 收费模式：1按单/2按票/3按人/4按间夜-按固定金额/5比例 /6绑定报销收费,
   */
  @TableField("fwf_charge_mode")
  private String fwfChargeMode;

  /**
   * 里程单价
   */
  @ApiModelProperty(value = "里程单价", dataType = "bigdecimal")
  private BigDecimal lcdj;
  /**
   * 司机订单数量
   */
  @ApiModelProperty(value = "司机订单数量", dataType = "Integer")
  @TableField("driver_order_count")
  private Integer driverOrderCount;
  /**
   * 司机图片
   */
  @TableField("driver_image")
  private String driverImage;
  /**
   * 渠道id
   */
  @ApiModelProperty(value = "渠道id", dataType = "string")
  @TableField("channel_id")
  private String channelId;

  /**
   * 渠道账号类型：DDYC，SYYC等
   */
  @ApiModelProperty(value = "渠道账号类型：DDYC，SYYC等", dataType = "string")
  @TableField("channel_type")
  private String channelType;
  /**
   * 实际供应商编号
   */
  @ApiModelProperty(value = "实际供应商编号", dataType = "string")
  @TableField("actual_supplier_no")
  private String actualSupplierNo;

  /**
   * 会员ID
   */
  @TableField("member_id")
  @ApiModelProperty(value = "会员ID", dataType = "string")
  private String memberId;
  /**
   * 租户编号
   */
  @TableField("tn_code")
  @ApiModelProperty(value = "租户编号", dataType = "string")
  private String tnCode;
  /**
   * 销售场景
   */
  @TableField("sales_scene")
  @ApiModelProperty(value = "销售场景", dataType = "string")
  private String salesScene;
  /**
   * 预累计积分
   */
  @TableField("pre_points")
  @ApiModelProperty(value = "预累计积分", dataType = "bigdecimal")
  private BigDecimal prePoints;
  /**
   * 是否累计0否1是
   */
  @TableField("add_up")
  @ApiModelProperty(value = "是否累计0否1是", dataType = "string")
  private String addUp;
  /**
   * 自动确认提示 1司机已到达上车点2 距离最近，3价格最低，4附近暂无多的司机接单
   */
  @TableField(exist = false)
  private String autoConfirmType;
  /**
   * 司机接乘客时间 秒
   */
  @TableField(exist = false)
  private Integer costTime;

  /**
   * 是否线下支付 1、已绑定，2、已绑定资金流水
   */
  @TableField("offline_pay")
  @ApiModelProperty(value = "是否线下支付  1是 其他否", dataType = "string")
  private String offlinePay;
  /**
   * 行后联系司机时间限制 空或-1代表行程结束立即解绑不能联系司机的供应商，直接给提示语，
   * 0代表可以调用供应接获取司机手机号，大于0则代表具体的有效时间，单位”小时“
   */
  @TableField("xhlxsjsjxz")
  @ApiModelProperty(value = "行后联系司机时间限制 空或-1代表行程结束立即解绑不能联系司机的供应商，直接给提示语， 0代表可以调用供应接获取司机手机号，大于0则代表具体的有效时间，单位”小时“", dataType = "string")
  private String xhlxsjsjxz;
  /**
   * 是否接力单
   */
  @TableField("sfjld")
  private String sfjld;
  /**
   * 上个乘客下车经度
   */
  @TableField("sgckxcjd")
  private String sgckxcjd;
  /**
   * 上个乘客下车纬度
   */
  @TableField("sgckxcwd")
  private String sgckxcwd;
  /**
   * 上一单是否完成
   */
  @TableField("sdsfwc")
  private String sdsfwc;

  /**
   * 是否为国际业务：0-国内，1-国际
   */
  private String overseas;

  /**
   * 联系人手机国际编码
   **/
  @ApiModelProperty(value = "联系人手机国际编码", example = "86")
  @TableField("lxrsj_gjbm")
  private String lxrsjGjbm;

  /**
   * 控润规则id
   */
  @TableField(exist = false)
  private String krgzid;

  /**
   * 滴滴是否一口价
   */
  @ApiModelProperty(value = "滴滴是否一口价")
  @TableField("show_ykj_flag")
  private String showYkjFlag;

  /**
   * 最晚计划出发时间
   */
  @ApiModelProperty(value = "最晚计划出发时间", example = "2025-12-20 10:22")
  @TableField("end_plan_start_time")
  private String endPlanStartTime;
  /**
   * 乘坐人数
   */
  @ApiModelProperty(value = "乘坐人数", example = "4")
  @TableField("pax_num")
  private Integer paxNum;
  /**
   * 行程确认状态 1 确认上车，2确认下车
   */
  @ApiModelProperty(value = "行程确认状态 1 确认上车，2确认下车", example = "4")
  @TableField("trip_status")
  private String tripStatus;
  /**
   * 高速费承担：1-乘客全部承担，2-车主全部承担，3-愿意协商高速费
   */
  @TableField("bear_highway_fee_type")
  @ApiModelProperty(value = "高速费承担：1-乘客全部承担，2-车主全部承担，3-愿意协商高速费", dataType = "string")
  private String bearHighwayFeeType;

  @TableField("ddqdly")
  private String ddqdly;

  /**
   * 积分状态 0未发放 1已发放 2无积分 4:出发送积分请求
   */
  @TableField("jfzt")
  private String jfzt;
  /**
   * 供应商主单单号
   */
  @TableField("gys_mddbh")
  private String gysMddbh;
  /**
   * 退改标记id
   */
  @TableField("tgbjid")
  private String tgbjid;
  @TableField(exist = false)
  private String sfcxhyj;

  @TableField(exist = false)
  @ApiModelProperty(value = "高德的poiid", dataType = "string")
  private String cfdpoiid;

  @TableField(exist = false)
  @ApiModelProperty(value = "高德的poiid", dataType = "string")
  private String mddpoiid;
  /**
   * 语种
   */
  @ApiModelProperty(value = "订单语种", dataType = "String")
  private String ddlyyz;

  @Override
  public String toString() {
    return JsonMapper.nonEmptyMapper().toJson(this);
  }
  // 全是 get set
}
