package cn.vetech.center.usecar.book.buyer.specicar.vo;

import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;


/**
 * 专快车产品查询对象（ASMS）
 * @author houshuang
 * @since 2017-11-09
 */
public class BookAsmsSpecialCar{
    /**
     * 供应商编号
     */
    private String gysbh;
    /**
     * 供应商名称
     */
    private String gysmc;
    /**
     * 车型组编号
     */
    private String cxzbh;
    /**
     * 车型组名称
     */
    private String cxzmc;
    /**
     * 报价
     */
    private String ygje;
    /**
     * (CPMS）产品描述
     */
    private String cpms;
    /**
     * 价格md5,通过 新的预估价接口获得(滴滴下单时必须要的)
     */
    private String jgmd5;
    /**
     * 产品id
     */
    private String cpid;
    /**2017/0318 汪震加**/
    /**
     * 供应返佣方式
     */
    private Integer gyFyfs;
    /**
     * 供应返佣值
     */
    private Double gyFybl;
    /**
     * 供应返佣金额
     */
    private Double gyFyje;
    /**
     * 供应结算金额
     */
    private Double gyJsje;
    /**
     * 平台控润方式
     */
    private Integer ptkrfs;
    /**
     * 平台控润值 控润比例
     */
    private Double ptkrbl;
    /**
     * 平台控润金额
     */
    private Double ptkrje;
    /**
     * 免费取消时限
     */
    private Double mfqxsx;
    /**
     * 到预定时间时前返佣
     */
    private Double dydsjsqfy;
    /**
     * 预定时间后返佣
     */
    private Double ydsjhfy;
    /**
     * 服务内容
     */
    private String fwnr;
    /**
     * 服务标准
     */
    private String fwbz;
    /**
     * 平台控润规则（1.结算价，2.差价,3.建议价）
     */
    private String ptkrgz;
    /**
     * 计价模式类别  201:专车  301:快车
     */
    private String jjmslb;
    /**
     * 商户logo
     */
    private String shlogodz;

    /**
     * 价格缓存主表ID
     */
    private String priceCaCheId;

    /**
     * 平台贴点方式
     */
    private Integer pttdfs;
    /**
     * 平台贴点比例
     */
    private Double pttdbl;
    /**
     * 平台贴点金额
     */
    private Double pttdje;
    /**
    * 供应前后返(1.前返，2后返）
     */
    private Integer gyQhf;
    /**
     * 外部车型组名称
     */
    private String wbcxzbh;
    /**
     * 外部车型组名称
     */
    private String wbcxzmc;
    /**
     * 是否一口价
     */
    private String sfykj;
    /**供应商logo链接*/
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
     * 展示一口价标识，滴滴特有 0：不展示，1：展示
     */
    private String showYkjFlag;
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

    private BigDecimal jsj;

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
    // 后面全是 get set方法
}