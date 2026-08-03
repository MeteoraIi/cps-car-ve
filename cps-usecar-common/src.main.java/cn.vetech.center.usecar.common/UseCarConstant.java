package cn.vetech.center.usecar.common;


import cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum;
import com.google.common.collect.Lists;
import io.swagger.models.auth.In;
import org.jboss.netty.util.Timeout;

import java.util.Arrays;
import java.util.List;

import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC1B;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC1D;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC1E;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC1G;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2A;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2B;
import static cn.vetech.center.usecar.common.enums.UsecarOrderStatusEnum.YC2C;

/**
 * 用车常量类
 * @author chenyong
 * @since 2017-10-16
 */
public class UseCarConstant {
    /**
     * 有效订单
     */
    public static final List<String> EFFECTIVE_STATUS =Arrays.asList("YC4A","YC4B","YC4C","YC2M","YC2O","YC2P");
    /**
     * 有违约
     */
    public static final List<String> BREACH_STATUS =Arrays.asList("YC2M","YC2O","YC2P");
    /**
     * 用车最终状态
     */
    public static final List<String> YYC_STATES = Arrays.asList("YC4A","YC4B","YC4C");
    /**
     * 预存款
     */
    public static final String PRE_DEPOSIT_CODE = "312013300";
    /**
     * 预存款
     */
    public static final String PRE_DEPOSIT_NAME = "预存款";
    /**
     * 投诉类型
     */
    public static final String COMPLAINT_TYPE = "2";
    /**
     * 文件服务名称
     */
    public static final String SYSTEM_DFS = "system-dfs";
    /**
     * 差评星级
     */
    public static final String NEGATIVE_COMMENT = "12";
    /**
     * 用车订单编号前缀
     */
    public static final String DDBHPRE = "YC";

    /**
     * ZD 开头订单编号
     */
    public static final String ZD = "ZD";
    /**
     * 产品编号
     */
    public static final String CPBH = "1000";
    /**
     * 用车补差单号前缀
     */
    public static final String YCBHPRE = "BC";
    /**
     * 用车退款单号前缀
     */
    public static final String TKDDBHPRE = "";

    /**
     * 图片查看地址
     */
    public static final String IMGLOOKURL = "http://vefile.vetech.cn:40006/";//http://vefile.vetech.cn:40006/

    /**
     * 服务商logo图片地址
     */
    public static final String NEWLOGOURL="http://vefile.vetech.cn:40006/";
    /**产品上架*/
    public static final int YC_CPSXJ_UP = 1;
    /**产品下架*/
    public static final int YC_CPSXJ_DOWN = 0;
    /**对CPS销售*/
    public static final String YC_PRODUCT_SALETOCPS_YES = "1";
    /**不对CPS销售*/
    public static final String YC_PRODUCT_SALETOCPS_NO = "0";
    /**未审核状态*/
    public static final int SHZT_UN_CHECKED = 0;
    /**审核通过*/
    public static final int SHZT_CHECKED = 1;
    /**审核未通过*/
    public static final int SHZT_2 = 2;
    /**
     * 采购单号长度
     */
    public static final int CUSTOMER_ORDER_NO_LEN = 20;

    /**数字*/
    public static final int SEVEN = 7;
    /**数字*/
    public static final int SL8 = 8;
    /**数字*/
    public static final int FOUR = 4;
    /**数字*/
    public static final int SIX = 6;
    /**数字*/
    public static final int TWELEVE = 12;
    /**
     * 检查订单分页数量
     */
    public static final int CHECK_ORDER_PAGE_SIZE = 500;
    /**
     * 检查订单最大分页数量
     */
    public static final int CHECK_ORDER_MAX_PAGE = 80;
    /**
     * 产品分类-用车编码 用车(一级)
     */
    public static final String YC_CPBH = "1000";
    /**
     * 接机(二级)
     */
    public static final String CPFL_YC_JJ = "100001";
    /**
     * 送机
     */
    public static final String CPFL_YC_SJ = "100002";
    /**
     * 接站
     */
    public static final String CPFL_YC_JHC = "100003";

    /**
     * 送站
     */
    public static final String CPFL_YC_SHC = "100004";
    /** 专车的产品类型 专(快)车(二级)**/
    public static final String CPFL_YC_ZC = "100005";

    /**
     * 阿拉伯数字
     */
    public static final int ZERO =0;
    /**
     * 阿拉伯数字
     */
    public static final int ONE =1;
    /**
     * 阿拉伯数字
     */
    public static final int FIVE =5;
    /**
     * 阿拉伯数字
     */
    public static final int EIGHT =8;
    /**
     * 阿拉伯数字
     */
    public static final int TWO = 2;
    /***
     *负2的表示
     */
    public static final  int FE= -2;
    /**
     * 阿拉伯数字
     */
    public static final int  THREE=3;
    /**
     * 阿拉伯数字,这个值是固定的，不会更改，别的地方也可以用这个
     */
    public static final int SZ24 = 24;
    /**
     * 阿拉伯数字,这个值是固定的，不会更改，别的地方也可以用这个
     */
    public static final int SZ60 = 60;
    /**
     * 阿拉伯数字，这个值是固定的，不会更改，别的地方也可以用这个
     */
    public static final int SZ1000 = 1000;
    /**
     * 阿拉伯数字
     */
    public static final int NUM =-1;
    /**
     * b2c
     */
    public static final String B2C = "CPSC-APP预订";
    /**
     * CPSC pc
     */
    public static final String CPSC_PC = "CPSC-PC预订";
    /**
     * CPS_A
     */
    public static final String CPS_A = "CPS-A预定";
    /**
     * CPS-A手工单
     */
    public static final String CPS_A_MAN = "CPS-A手工单";
    /**
     * CPS_A 老的
     */
    public static final String CPS_A_O = "CPS客服代订";
    /**
     * 派车短信发送标识
     */
    public static final String SEND_CAR_ASSIGNED_MESSAGE = "1";
    /**
     * 改派车短信发送标识
     */
    public static final String SEND_CAR_REASSIGNED_MESSAGE = "2";
    /**
     * 无需发送短信标识
     */
    public static final String SEND_NO_MESSAGE = "0";
    /***
     *
     * 状态0Stirng 类型
     */
    public static  final  String ZT_ZERO="0";
    /***
     *
     * 状态1 Stirng 类型
     */
    public static  final  String ZT_ONE="1";
    /***
     *
     * 状态2 Stirng 类型
     */
    public static  final  String ZT_TWO="2";
    /***
     *
     * 状态3 Stirng 类型
     */
    public static  final  String ZT_THREE="3";
    /***
     *
     * 状态3 Stirng 类型
     */
    public static  final  String ZT_FOUR="4";
    /***
     *
     * 状态3 Stirng 类型
     */
    public static  final  String ZT_FIVE="5";
    /**
     * 新版下单标识
     */
    public static final String CREATE_ORDER_V2 = "1";
    /**
     * YES
     */
    public static final String YES = "1";
    /**
     * NO
     */
    public static final String NO = "0";
    /**
     * 绑定流水
     */
    public static final String BIND_FLOW_STATUS = "2";
    /**
     * 百分比小数点（计算手续费用到）
     */
    public static final double BFB=0.01;
    /***
     * 小数点（计算手续费用到）
     */
    public static final double XSZ=0.0;
    /**
     * 60(计算手续费用到)
     */
    public static final int SIXTY=60;
    /***
     *1000(计算手续费用到)
     */
    public static final int THOUSAND=1000;

    /**
     * 类型为Long型的数值
     */
    public static final long VALUEONE=1L;

    /**
     * 派车类型(表示派车--派车和改派车操作要用到)
     */
    public static final String PCLX_PC ="PC";
    /**
     * 派车类型(表示改派车--派车和改派车操作要用到)
     */
    public static final String PCLX_GPC ="GPC";

    /**
     * 0 (字符串类型)
     */
    public static final String NUMZERO = "0";

    /**
     * 1(字符串类型)
     */
    public static final String NUMONE = "1";
    /**
     * 2 (字符串类型)
     */
    public static final String NUMTWO = "2";

    /**
     * 3 (字符串类型)
     */
    public static final String NUMTHREE = "3";

    /**
     * 小时转分钟单位
     */
    public static final int  MINUTE =60;

    /**数据对应表，数据类型：城市*/
    public static final String SJDY_LX_CITY="4";
    /**数据对应表，数据类型：机场*/
    public static final String SJDY_LX_JC="2";
    /**数据对应表，数据类型：火车站*/
    public static final String SJDY_LX_HCZ="1";

    /** 支付到CPS的支付状态:1.已支付 */
    public static final String ZF_ZT_YZF = "1";
    /** 支付到CPS的支付状态:0.未支付*/
    public static final String ZF_ZT_WZF = "0";
    /**
     * 平台A
     */
    public static final String YC_PAY_PT_A = "A";
    /**
     * 平台B
     */
    public static final String YC_PAY_PT_B = "B";
    /**
     * 用车订单类型
     */
    public static final String NORMAL_ORDER_TYPE_CODE = "10001";
    /**
     * 用车订单类型名称
     */
    public static final String NORMAL_ORDER_TYPE_NAME = "用车正常单";
    /**
     * 用车退单订单类型
     */
    public static final String REFUND_ORDER_TYPE_CODE = "10002";
    /**
     * 用车退单订单类型名称
     */
    public static final String REFUND_ORDER_TYPE_NAME = "用车退单";
    /**
     * 包车业务类型
     */
    public static final String YC_PAY_YWTYPE_1000 = "1000";
    /**
     * 订单类型
     */
    public static final String YC_PAY_YWLX_01 = "YC01";
    /**
     * 订单补差类型
     */
    public static final String YC_PAY_YWLX_02 = "YC15";
    /**
     * 业务URL，用于收银台加载页面
     */
    public static final String YC_PAY_YWURL = "/airs/common/yc_order_pay!zfdetail.shtml?ddglQuery.ddbh=";
    /**
     * 成功数据
     */
    public static final int SUCCESS = 0;

    /**
     *CPSB 上传图片 文件来源
     * */
    public static final  String CPSB="CPSB";
    /**
     *CPS来源
     * */
    public static final  String CPS="CPS";
    /**
     *ASMS来源
     * */
    public static final  String ASMS="ASMS";

    /**
     * 应用名称 usecar
     * */
    public static final String USECAR="usecar";

    /***
     * 失败数据
     */
    public static final int FAIL = -1;
    /***double 类型的 1000表示**/
    public static final double NUM_YQ=1000;
    /**double类型的0 表示**/
    public static final double NUM_ZERO=0;
    /**
     * 自签产品线程等待时间
     */
    public static final int ZQTHREADWAIT = 3;
    /**
     * Link查询线程等待时间
     */
    public static final int LINKTHREADWAIT = 20;


    /**
     * yyyy-MM-dd HH:mm 时间长度
     */
    public static final int NYRSF=16;
    /**
     *成功
     */
    public static final String JK_TS_SUCESS = "200";
    /**
     *ES 查询操作成功编码
     */
    public static final int ES_SUCCESS_CODE = 200;
    /**
     * ES 创建成功编码
     */
    public static final int ES_SUCCESS_CREATED = 201;

    /**
     * 常量数字10
     */
    public static final int TEN = 10;
    /**
     * 常量数字11
     */
    public static final int ELEVEN = 10;
    /**
     * 常量数字11
     */
    public static final int NO_ELEVEN = 11;
    /**
     * 复杂业务逻辑主线程休眠时间
     */
    public static final int NOSIMPLEBUS_SLEEP = 5000;
    /**
     * 常量数字100
     */
    public static final int BAI = 100;
    /**
     * 常量数字1000
     */
    public static final int QIAN = 1000;

    /**
     * false
     */
    public static final String FALSE = "false";
    /**
     * true
     */
    public static final String TRUE = "true";
    /**
     * 退单申请通知接口、推送退单申请到供应商ASMS、在供应系统生成反向单
     */
    public static final int ONE_THOUSAND_AND_THREE = 1003;
    /**
     * 供应商执行：与客户完成  、这里推送相关信息给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_FOUR = 1004;
    /**
     * 支付到CPS成功后、推送支付结果给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_FIVE = 1005;
    /**
     * 供应商拒单后、推送拒单信息给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_SEVEN = 1007;

    /**
     * 平台拒单后、推送拒单信息给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_EIGHT_THREE = 1083;
    /**
     * CPS从费控拉取订单
     */
    public static final int ONE_THOUSAND_AND_EIGHT_EIGHT = 1988;

    /**
     * 供应商派车后、推送相关司机及车辆信息给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_EIGHT = 1008;
    /**
     * 采购确认服务完成、推送完成结果给供应商ASMS
     */
    public static final int ONE_THOUSAND_AND_NINE = 1009;
    /**
     * 供应商确认服务完成、推送订单信息给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_TWENTY = 1020;
    /**
     * 供应商确认服务完成(CPS上操作)、推送信息给供应商ASMS
     */
    public static final int ONE_THOUSAND_AND_TENTYONE = 1021;
    /**
     * CPS退款分账完成、推送相关信息给供应商ASMS
     */
    public static final int ONE_THOUSAND_AND_TENTYTWO = 1022;
    /**
     * 查询费控退款明细
     */
    public static final int QUERY_CHARGE_CAR_REFUND_DETAIL = 1051;
    /**
     * 国际打车通知
     */
    public static final int OVERSEAS_CAR_NOTICE = 1593;
    /**
     * 顺风车通知
     */
    public static final int RIDE_SHARE_CAR_NOTICE = 1599;
    /**
     * 供应商确认接单、推送相关信息给采购商ASMS
     */
    public static final int ONE_THOUSAND_AND_THIRTY = 1030;
    /**
     * 供应商确认接单(CPS上操作)、推送相关信息给供应商ASMS
     */
    public static final int ONE_THOUSAND_AND_THIRTYONE = 1031;
    /**
     * 采购确认服务完成后推送用车完成给供应商ASMS
     */
    public static final int ONE_SERVICECOMFIRM_NOTICE_SELLER = 1022;
    /**
     * 采购取消订单、推送相关信息给供应商ASMS
     * #####目前这个业务场景应该是没用到的#####
     */
    @Deprecated
    public static final int ONE_THOUSAND_AND_SIX=1006;
    /**
     * 常量数字1001
     */
    public static final int ONE_THOUSAND_AND_ONE = 1001;
    /**
     * 线程默认睡眠时间
     */
    public static final int THREAD_SLEEP_TIME = 500;
    /**
     * 修改订单金额
     */
    public static final int UPDATE_ORDER_PRICE = 1077;
    /**
     * CPS上的投诉单推送给采购商ASMS系统
     */
    public static final Integer COMPLAINT_SEND_BUYER_ASMS = 1080;
    /**
     * CPS上的评价单推送给采购商ASMS系统
     */
    public static final Integer SCORE_SEND_BUYER_ASMS = 1081;
    /**
     * CPS上的回复推送给采购商ASMS系统
     */
    public static final Integer ANSWER_SEND_BUYER_ASMS = 1082;
    /**
     * CPS上的投诉单推送给供应商ASMS系统
     */
    public static final Integer COMPLAINT_SEND_SELLER_ASMS = 1085;
    /**
     * CPS上的评价单推送给供应商ASMS系统
     */
    public static final Integer SCORE_SEND_SELLER_ASMS = 1086;
    /**
     * CPS上的供应回复推送给供应商ASMS系统
     */
    public static final Integer ANSWER_SEND_SELLER_ASMS = 1087;
    /**
     * CPS平台推送双倍预付补差退款采购ASMS平台失败
     */
    public static final Integer ANSWER_SEND_BC_ASMS = 1089;
    /**
     * 推送开票成功到采购商ASMS系统
     */
    public static final Integer ANSWER_SEND_INVOICE_ASMS = 1090;
    /**
     * 司机到达前取消订单、产生该派单情况推送数据到采购商ASMS系统
     */
    public static final Integer DRIVERCGP_SEND_BUYER_ASMS = 1088;

    /**
     *  erp统一通知接口
     */
    public static final Integer ERP_COMMON_NOTICE = 11711;
    /**
     * 控润方式：固定金额
     */
    public static final int KR_KRFS_GDJE = 1;
    /**
     * 控润方式：百分百
     */
    public static final int KR_KRFS_BFB = 2;
    /**
     * 无返佣
     */
    public static final int KR_KRFS_WFY = 3;
    /**
     * 可取消状态
     */
    public static final List<String> CAN_CANCEL_STATUS = Arrays.asList("YC1A", "YC1C", "YC1F", "YC2D", "YC2F", "YC2G", "YC2H", "YC2E", "YC1H");
    /**
     * 新下单模式
     */
    public static final String NEW_ORDER = "1";
    /**
     * 一键三单
     */
    public static final String THREE_ORDERS = "3";
    /**
     * 取消状态
     */
    public static final List<String> CANCEL_STATUS = Arrays.asList("YC3D", "YC1G", "YC3C", "YC2A", "YC2C", "YC2B"
            , "YC1E", "YC2M", "YC2O", "YC2P", "YC1B", "YC1D", "YC1E");
    /**
     * 无前返
     */
    public static final Integer YC_WQHF = 0;
    /**
     * 前返
     */
    public static final Integer YC_QF = 1;
    /**
     * 后返
     */
    public static final Integer YC_HF = 2;
    /**
     * link 推送CPS成功状态
     */
    public static final String BACK_CPS_OK="200";
    /**
     * link 推送CPS处理失败状态
     */
    public static final String BACK_CPS_BAD="-500";

    /**
     * 百分比类型
     */
    public static final String BAIBL = "0.01";
    /**
     * 拒绝取消(锦华有这个状态，拒绝取消的情况视为操作取消的客户需要全损) link系统中也有这个状态值
     */
    public static final int ERROR_CANNOT = 707;
    /**
     * 数字30
     */
    public static final int NUM_30=30;
    /**
     * 数字60
     */
    public static final int NUM_60=60;
    /**
     * 数字1000
     */
    public static final int NUM_1000=1000;
    /**
     * 数字2000
     */
    public static final int NUM_2000=2000;
    /**
     * 4000
     */
    public static final int NUM_5000=5000;
    /**
     * 未检索到订单
     */
    public static final String NOORDER="NO_ORDER";
    /**
     * 自定义一个采购商户编号,采购过滤查询时专用
     */
    public static final String ALLCGS = "BUYER_FILTER_ONLY_USE_FOR_ALLCGS";

    /**
     * 自定义专快车站点ID
     */
    public static final String ZC_ZDID ="ZKC";

    /**
     * 自定义专快车站点名称
     */
    public static final String ZC_ZDMC ="专快车";

    /**
     * 控润选择全部站点
     */
    public static final String ALL_ZD = "4";
    /**
     * 数字10000
     */
    public static final int NUM_10000=10000;
    /**评价投诉类型：评价**/
    public static final String PJTS_PJ="1";
    /**评价投诉类型：投诉**/
    public static final String PJTS_TS="2";
    /**投诉情况下默认给的评分**/
    public static final String TS_DEFAULT_PF="1";

    /**评价投诉回复状态：未回复**/
    public static final String PJTS_HFZT_WHF="0";
    /**评价投诉回复状态：已回复**/
    public static final String PJTS_HFZT_YHF="1";


    /** 站点类型***/
    /**火车站**/
    public static final String ZDLX_TRAIN="1";
    /**机场**/
    public static final String ZDLX_AIRPORT="2";
    /**城市**/
    public static final String ZDLX_CITY="4";
    /**平台名称**/
    public static final String PT_NAME = "5";

    /**
     * 订单类型
     */
    /**
     * 正常单
     */
    public static final String USECAR_ORDER_TYPE_ZCD = "1";
    /**
     * 补差单
     */
    public static final String USECAR_ORDER_TYPE_BCD = "2";
    /**
     * 企业差旅平台商户性质常量
     */
    public static final String CPS_DD_SHXZ="102408";

    /**
     * 年月日时分格式化
     */
    public static final String PATTERN_ON_MINUTE = "yyyy-MM-dd HH:mm";
    /**
     * 待派车状态
     */
    public static final String[] WAIT_STATE = {"YC1A","YC1C","YC1F","YC1H"};

    /**
     * 高德信息缓存锁名称;
     */
    public static final String USECAR_AMAP_LOCK = "USECAR_AMAP_YCDD_LOCK";
    /**
     *  服务费锁
     */
    public static final String USECAR_FWF_LOCK = "USECAR_FWF_LOCK";
    /**
     * 高德信息缓存锁过期时间10s
     */
    public static final long USECAR_AMAP_LOCK_WAIT_TIME = 1000 * 10;
    /**
     *  获取锁超时间
     */
    public static final long USECAR_FWF_LOCL_WAITTIME = 1000 * 30;
    // 已用车
    public static final List<String> yycDdzts = Lists.newArrayList(
            UsecarOrderStatusEnum.YC4A.getCode(), UsecarOrderStatusEnum.YC4B.getCode(), UsecarOrderStatusEnum.YC4C.getCode());

    //接单数量 有些中间态时间太多没有必要统计
    public static final List<String> jdDdzts = Lists.newArrayList(UsecarOrderStatusEnum.YC2M.getCode(), UsecarOrderStatusEnum.YC2O.getCode(), UsecarOrderStatusEnum.YC2P.getCode(),
            UsecarOrderStatusEnum.YC4A.getCode(), UsecarOrderStatusEnum.YC4B.getCode(), UsecarOrderStatusEnum.YC4C.getCode(), UsecarOrderStatusEnum.YC2D.getCode(), UsecarOrderStatusEnum.YC2E.getCode());
    //拒单数
    public static final List<String> rejectDdzts = Lists.newArrayList(YC2A.getCode(), YC2B.getCode(), YC2C.getCode(), YC1G.getCode());
    // 退单
    public static final List<String> refundDdzts = Lists.newArrayList(UsecarOrderStatusEnum.YC3C.getCode(), UsecarOrderStatusEnum.YC3D.getCode(), UsecarOrderStatusEnum.YC3A.getCode());
    // 已退单
    public static final List<String> refundedDdzts = Lists.newArrayList(UsecarOrderStatusEnum.YC3D.getCode());
    // 取消
    public static final List<String> cancelDdzts = Lists.newArrayList(YC1E.getCode());
    // 超时取消
    public static final List<String> TIMEOUT_CANCEL_STATUS = Lists.newArrayList(YC1B.getCode(), YC1D.getCode());
    // 取消拒单超时取消
    public static final List<String> ALL_CANCEL_STATUS = Lists.newArrayList(YC1B.getCode(),YC1D.getCode(),YC1E.getCode(),YC2A.getCode(), YC2B.getCode(), YC2C.getCode(),YC1G.getCode());
    // 有效订单
    public static final List<String> vaildDdzts = Lists.newArrayList(UsecarOrderStatusEnum.YC4A.getCode(),UsecarOrderStatusEnum.YC4B.getCode(),UsecarOrderStatusEnum.YC4C.getCode());
    // 已用车未支付
    public static final String UN_PAY = UsecarOrderStatusEnum.YC4C.getCode();
    // 已用车未支付和有违约未付
    public static final List<String> unpayDdzts = Lists.newArrayList(UsecarOrderStatusEnum.YC4C.getCode(),UsecarOrderStatusEnum.YC2M.getCode());

    public final static String increase_if_key = "INCREASE_LIMIT_COMPARE_%s";
    /**
     * 滴滴平台类型
     */
    public static final String  DDYC_PTLX = "201707";
    /**
     *  采购商为CPS的商户编号
     */
    public static final List<String> CPS_CGS = Lists.newArrayList("CMICS");


    public static final String LINK_SEARCH_TIMEOUT = "1000YC11";
    /**
     * 出租车供应商
     */
    public static final String[] TAXI_SUPPLIER_NAME = {"北方出行","北京的士","北汽出租","成都出租","大众出行","橄榄新出租","好的出租","金银建出行","聚的出租车","奇华出行","深圳出租","天津出租","新月出租","优e出租","渔阳出行","北京的士"};
    /**
     * 出租车供应商
     */
    public static final String[] TAXI_SUPPLIER_SHORT_NAME = {"北方","大众","金银建","奇华","渔阳","优e","橄榄新","好的","新月","吉林","佰联","富安"};


    public static final String SYNC_POI_START_TIME_PREFIX = "SYNC_POI_TASK_START_TIME";

    /**
     * 进行态
     */
    public static final List<String>  EN_ROUTE = Arrays.asList("YC2D", "YC2G", "YC2H");


    public static final String QUERY_MEMBER_PRICE_FLAG = "QUERY_MEMBER_PRICE_FLAG";
}