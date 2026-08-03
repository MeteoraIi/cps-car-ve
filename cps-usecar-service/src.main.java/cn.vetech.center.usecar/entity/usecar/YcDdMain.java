package cn.vetech.center.usecar.entity.usecar;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import com.baomidou.mybatisplus.annotations.TableId;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import org.vetech.core.base.BaseEntity;
import org.vetech.core.modules.utils.mapper.JsonMapper;

/**
 * <p>
 * 用车订单主表
 * </p>
 *
 * @since 2023-08-14
 */
@TableName("yc_dd_main")
public class YcDdMain extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主订单号
     */
    @TableId("p_ddbh")
    @ApiModelProperty(value = "主订单号", dataType = "string")
    private String pDdbh;
    /**
     * 订单状态(枚举同用车订单表)
     */
    @ApiModelProperty(value = "订单状态(枚举同用车订单表)", dataType = "string")
    private String ddzt;
    /**
     * 采购订单号
     */
    @ApiModelProperty(value = "采购订单号", dataType = "string")
    private String cgddbh;
    /**
     * 订单总金额
     */
    @ApiModelProperty(value = "订单总金额", dataType = "bigdecimal")
    private BigDecimal ysje;
    /**
     * 支付状态(0：未付；1：已付；2：支付中；3：支付失败)
     */
    @ApiModelProperty(value = "支付状态(0：未付；1：已付；2：支付中；3：支付失败)", dataType = "string")
    private String zfzt;
    /**
     * 支付时间
     */
    @ApiModelProperty(value = "支付时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date zfsj;
    /**
     * 退款状态(0：未退；1：已全退；2：退款中；3：部分已退)
     */
    @ApiModelProperty(value = "退款状态(0：未退；1：已全退；2：退款中；3：部分已退)", dataType = "string")
    private String tkzt;
    /**
     * 退款时间
     */
    @ApiModelProperty(value = "退款时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date tksj;
    /**
     * 分账状态(0：未分账；1：已分账；2：分账中)
     */
    @ApiModelProperty(value = "分账状态(0：未分账；1：已分账；2：分账中)", dataType = "string")
    private String fzzt;
    /**
     * 分账时间
     */
    @ApiModelProperty(value = "分账时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date fzsj;
    /**操作类型(特殊字段非数据库字段)*/
    @TableField(exist = false)
    private String tszdczlx;
    /**
     * 支付方式编号
     */
    @ApiModelProperty(value = "支付方式编号", dataType = "string")
    private String zffs;
    /**
     * 支付方式名称
     */
    @ApiModelProperty(value = "支付方式名称", dataType = "string")
    private String zffsmc;
    /**
     * 支付内部流水号
     */
    @ApiModelProperty(value = "支付内部流水号", dataType = "string")
    private String nbjylsh;
    /**
     * 支付外部流水号
     */
    @ApiModelProperty(value = "支付外部流水号", dataType = "string")
    private String wbjylsh;
    /**
     * 采购商户编号
     */
    @ApiModelProperty(value = "采购商户编号", dataType = "string")
    private String cgshbh;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date cjsj;
    /**
     * 有效订单编号
     */
    @TableField("valid_order_no")
    @ApiModelProperty(value = "有效订单编号", dataType = "string")
    private String validOrderNo;
    /**
     * 下单时间
     */
    @ApiModelProperty(value = "下单时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date xdsj;
    /**
     * 10000101：标准(接口)接机，10000102：豪华接机，10000201：标准(接口)送机，10000202：豪华送机，10000301：标准接火车，10000302：豪华接火车10000401：标准送火车，10000402：豪华送火车10000501：专(快车)
     */
    @ApiModelProperty(value = "10000101：标准(接口)接机，10000102：豪华接机，10000201：标准(接口)送机，10000202：豪华送机，10000301：标准接火车，10000302：豪华接火车10000401：标准送火车，10000402：豪华送火车10000501：专(快车)", dataType = "string")
    private String ddlx;
    /**
     * 用车时间(2016-01-21 15:30 精确到小时分钟)
     */
    @ApiModelProperty(value = "用车时间(2016-01-21 15:30 精确到小时分钟)", dataType = "string")
    private String ycsj;
    /**
     * 乘客姓名
     */
    @ApiModelProperty(value = "乘客姓名", dataType = "string")
    private String ckxm;
    /**
     * 乘客手机
     */
    @ApiModelProperty(value = "乘客手机", dataType = "string")
    private String cksj;
    /**
     * 站点名称（专快车存“专快车”三个字）
     */
    @ApiModelProperty(value = "站点名称（专快车存“专快车”三个字）", dataType = "string")
    private String jsfwzdmc;
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
     * 司机姓名
     */
    @ApiModelProperty(value = "司机姓名", dataType = "string")
    private String sjxm;
    /**
     * 司机电话
     */
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
     /**
     * 供应订单编号
     */
    @TableField("gy_ddbh")
    @ApiModelProperty(value = "供应订单编号", dataType = "string")
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
     * 供应服务完成时间
     */
    @TableField("gy_fwwcsj")
    @ApiModelProperty(value = "供应服务完成时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date gyFwwcsj;
    /**
     * 出发地详细地址
     */
    @TableField("cfd_xxdz")
    @ApiModelProperty(value = "出发地详细地址", dataType = "string")
    private String cfdXxdz;
    /**
     * 目的地详细地址
     */
    @TableField("mdd_xxdz")
    @ApiModelProperty(value = "目的地详细地址", dataType = "string")
    private String mddXxdz;
    /**
     * 是否一口价（0.非一口价，1.一口价）
     */
    @ApiModelProperty(value = "是否一口价（0.非一口价，1.一口价）", dataType = "string")
    private String sfykj;
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
     * 改派车次数
     */
    @ApiModelProperty(value = "改派车次数", dataType = "bigdecimal")
    private BigDecimal gpccs;
    /**
     * 数据版本号
     */
    @Version
    @ApiModelProperty(value = "数据版本号", dataType = "bigdecimal")
    private BigDecimal version;
    /**
     * 修改来源 (A 或 B）
     */
    @TableField("xg_ly")
    @ApiModelProperty(value = "修改来源 (A 或 B）", dataType = "string")
    private String xgLy;
    /**
     * 预约类型 1 预约用车 2及时用车
     */
    @TableField("yy_lx")
    @ApiModelProperty(value = "预约类型 1 预约用车 2及时用车", dataType = "string")
    private String yyLx;
    /**
     * 分账时间
     */
    @TableField("fz_datetime")
    @ApiModelProperty(value = "分账时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss" ,timezone="GMT+8")
    private Date fzDatetime;
    /**
     * 采购订单来源（如：差旅安卓APP)
     */
    @TableField("cg_ddly")
    @ApiModelProperty(value = "采购订单来源（如：差旅安卓APP)", dataType = "string")
    private String cgDdly;
    /**
     * 初始预估金额
     */
    @ApiModelProperty(value = "初始预估金额", dataType = "bigdecimal")
    private BigDecimal csygje;
    /**
     * 服务保障级别
     */
    @ApiModelProperty(value = "服务保障级别", dataType = "string")
    private String fwbzjb;
    /**
     * 服务保障标准
     */
    @ApiModelProperty(value = "服务保障标准", dataType = "string")
    private String fwbzbz;
    /**
     * 商户性质
     */
    @ApiModelProperty(value = "商户性质", dataType = "string")
    private String shxz;
    /**
     * 商户性质名称
     */
    @ApiModelProperty(value = "商户性质名称", dataType = "string")
    private String shxzmc;
    /**
     * 因公：1;因私：2
     */
    @ApiModelProperty(value = "因公：1;因私：2", dataType = "string")
    private String clyy;
    /**
     * 采购预估价
     */
    @TableField("buyer_estimated_price")
    @ApiModelProperty(value = "采购预估价", dataType = "bigdecimal")
    private BigDecimal buyerEstimatedPrice;
    /**
     * 是否自动支付 是/否
     */
    @TableField("auto_pay")
    @ApiModelProperty(value = "是否自动支付 是/否", dataType = "string")
    private String autoPay;
    /**
     * 订单类型0单一车型订单1批量下单3一键三单
     */
    @TableField("order_type")
    @ApiModelProperty(value = "订单类型0单一车型订单1批量下单3一键三单", dataType = "string")
    private String orderType;

    @ApiModelProperty(value = "实际上车时间", dataType = "string")
    private String sjscsj;
    /**行程结束时间*/
    @ApiModelProperty(value = "行程结束时间", dataType = "string")
    private String xcjssj;

     /*** 站点所在城市编号(接送产品)*/
    @TableField("cfd_csid")
    @ApiModelProperty(value = "站点所在城市编号(接送产品)", dataType = "string")
    private String cfdCsid;
    /*** 出发城市名称*/
    @TableField("cfd_csmc")
    @ApiModelProperty(value = "出发地城市名称", dataType = "string")
    private String cfdCsmc;

    /*** 服务城市编号(接送产品)*/
    @TableField("mdd_csid")
    @ApiModelProperty(value = "服务城市编号(接送产品)", dataType = "string")
    private String mddCsid;
    /***目的的城市名称*/
    @TableField("mdd_csmc")
    @ApiModelProperty(value = "目的地城市名称", dataType = "string")
    private String mddCsmc;
    /**
     * 出发地POI
     */
    private String cfd;

    /**
     * 目的地POI
     */
    private String mdd;

    /**
     *  采购商商户简称
     */
    @TableField("cg_shjc")
    private String cgShjc;

    /**
     *  子单编号
     */
    private String ddbh;
    /**
     *  乘客手机号国家编码
     */
    @TableField("ck_sj_gjbm")
    private String ckSjGjbm;

    /**
     * 联系人手机国家编码
     */
    @TableField("lxrdh_gjbm")
    private String lxrdhGjbm;

    /**
     * 采购取消时间
     */
    @TableField("cg_qxsj")
    private Date cgQxsj;

    /**
     * 供应派车时间
     */
    @TableField("gy_pcsj")
    private Date gyPcsj;
    /**
     * 采购用户编码
     */
    @TableField("cg_yhbh")
    private String cgYhbh;


    /*** 产品id(自签产品有ID)*/
    @ApiModelProperty(value = "产品id(自签产品有ID)", dataType = "string")
    private String cpid;

    /**供应退款手续费*/
    @TableField("gy_tksxf")
    @ApiModelProperty(value = "供应退款手续费", dataType = "bigdecimal")
    private BigDecimal gyTksxf;
    /**
     *  1:新下单模式 默认旧下单模式
     */
    @TableField("new_order")
    private String newOrder;

    /**
     *  采购申请退款时间
     */
    @TableField("cg_tdsqsj")
    private Date cgTdsqsj;
    /**
     * 采购结算金额
     */
    @TableField("cg_jsje")
    private BigDecimal cgJsje;

    /**
     * 实际上车地址
     */
    @TableField("sjscdz")
   private String sjscdz;

    /**
     * 实际下车地址
     */
    @TableField("sjxcdz")
    private String sjxcdz;
    /**
     * 联系人
     */
    @TableField("lxr")
    private String lxr;
    /**
     * 联系人电话
     */
    @TableField("lxrdh")
    private String lxrdh;

    /**
     * 最后修改时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 最后更新人员姓名
     */
    private String updater;

    /**
     * 最后更新人员id
     */
    @TableField("update_id")
    private String updaterId;

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
     * 评价等级(1.一星，2.二星...5.五星)
     */
    @ApiModelProperty(value = "评价等级(1.一星，2.二星...5.五星)", dataType = "string")
    private String pjdj;
    /**
     * 评价标签(多个用英文逗号隔开)
     */
    @ApiModelProperty(value = "评价标签(多个用英文逗号隔开)", dataType = "string")
    private String pjbq;
    /**
     * 评价内容
     */
    @ApiModelProperty(value = "评价内容", dataType = "string")
    private String pjnr;
    /**
     * 特殊保障人员，1是0否
     */
    @ApiModelProperty(value = "特殊保障人员，1是0否", dataType = "string")
    @TableField("special_service")
    private String specialService;

    /**
     * 服务商服务费
     */
    @TableField("fwsfwf")
    private BigDecimal fwsfwf;
    /**
     * 支付是否带上服务商服务费
     */
    @TableField("with_fwf")
    private String withFwf;
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
     * 服务费收费模式  收费模式：1按单/2按票/3按人/4按间夜-按固定金额/5比例 /6绑定报销收费,
     */
    @TableField("fwf_charge_mode")
    private String fwfChargeMode;


    /**
     *  采购的收款状态
     */
    @TableField("cg_skzt")
    private String cgSkzt;

    /**
     * 里程单价
     */
    @ApiModelProperty(value = "里程单价", dataType = "bigdecimal")
    private BigDecimal lcdj;
    /**
     * 取消原因
     */
    @TableField("cg_qxyy")
    private String cgQxyy;

    /**
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id", dataType = "string")
    @TableField("channel_id")
    private String channelId;

    /**
     * 采购订单状态
     */
    @TableField("buyer_order_status")
    private String buyerOrderStatus;

    /**
     * 采购订单状态中文
     */
    @TableField("buyer_order_status_cn")
    private String buyerOrderStatusCn;
    /**
     * 司机订单数量
     */
    @TableField("driver_order_count")
    private Integer driverOrderCount;

    /**
     * 多是否多运力 1 ：是 0：否
     */
    @ApiModelProperty(value = "是否多运力 1 ：是 0：否", dataType = "string")
    @TableField("batch_create")
    private String batchCreate;
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
    * 一键三单，是否已确认0否1是，空默认否
     */
    @TableField("confirm_order")
    @ApiModelProperty(value = " 一键三单，是否已确认0否1是，空默认否", dataType = "string")
    private String confirmOrder;
    /**
     * 呼叫规则0接单最快1时间最短2价格最低
     */
    @TableField("call_rule")
    @ApiModelProperty(value = "呼叫规则0接单最快1时间最短2价格最低", dataType = "string")
    private String callRule;
    /**
     * 异常订单类型：
     * 1、费用异常 ：用采购结算金额 —下单预估金额，高速费，路桥费，停车费，调度费，控润）＞5元
     *   且【用采购结算金额 —（下单预估金额，高速费，路桥费，停车费，调度费，控润）】/下单预估价格*100%＞20%
     *   且 实际公里数-预估公里数≥2公里
     * 2、订单总金额异常判断逻辑：订单采购结算金额超过500元
     * 3、附加费异常判断逻辑：附加费金额大于200或者附加费金额（高速费，路桥费，停车费，调度费）占采购结算金额50%及以上
     * 4、上下车地址异常判断逻辑：上车位置偏移1公里或下车位置偏移1公里
     * 5、里程数异常：预估里程和实际里程相差超4公里且比例超出30%
     */
    @TableField("abnormal_order_type")
    private String abnormalOrderType;
    /**
     * 发票回收状态（0：未处理，1：处理中 2：已处理）默认未处理
     */
    @TableField("invoice_recovery_status")
    @ApiModelProperty(value = "发票回收状态（0：未处理，1：处理中 2：已处理）默认未处理", dataType = "string")
    private String invoiceRecoveryStatus;
    /**
     * 线下流水认领状态：0-待认领、1-认领成功、2-部分认领、3-认领失败、4-已拒回、5-认领中
     */
    @TableField("offline_binding_status")
    @ApiModelProperty(value = "线下流水认领状态（0：未处理，1：处理中 2：已处理）默认未处理", dataType = "string")
    private String offlineBindingStatus;

    /**
     * 是否为国际业务：0-国内，1-国际
     */
    private String overseas;

    /**
     * 开票状态：0=未开票，1=已开票，2=开票中，3=开票失败
     */
    @TableField("invoice_status")
    @ApiModelProperty(value = "开票状态：0=未开票，1=已开票，2=开票中，3=开票失败", dataType = "string")
    private String invoiceStatus;

    /**
     *  优惠类型，1 积分， 2 优惠券
     */
    private String yhlx;

    /**
     *  CPS平台优惠金额 优惠券的总金额多个优惠券的合计，优惠券是平台发的，要从贴点账户贴钱，元
     */
    private String ptyhqzyhje;

    /**
     *  CPS平台积分抵扣金额,平台积分金额要从单独的积分账户贴钱，元
     */
    private String jfzyhje;

    /**
     *  抵扣的积分值
     */
    private String jfz;

    /**
     *  是否查询会员价
     */
    private String sfcxhyj;
    /**
     *  会员折扣描述
     */
      private String yhzkms;

    public String getYhzkms() {
        return yhzkms;
    }
     @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}
