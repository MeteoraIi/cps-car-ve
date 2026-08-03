package cn.vetech.center.usecar.openapi.buyer.book.specialcar;

import cn.vetech.center.usecar.coupon.dto.CouponInfo;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 专车产品查询列表接口的根节点response下的返回字段
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BuyerSpecialCar implements Serializable {
    /**
     * 是否一口价
     */
    private String sfykj;
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
    @XmlElement(name = "gy_fyfs")
    private Integer gyFyfs;
    /**
     * 供应返佣值
     */
    @XmlElement(name = "gy_fybl")
    private Double gyFybl;
    /**
     * 供应返佣金额
     */
    @XmlElement(name = "gy_fyje")
    private Double gyFyje;
    /**
     * 供应结算金额
     */
    @XmlElement(name = "gy_jsje")
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
    @XmlElement(name = "gy_qhf")
    private Integer gyQhf;

    /**
     * 外部车型组编号
     */
    private String wbcxzbh;

    /**
     * 外部车型组名称
     */
    private String wbcxzmc;
    /**供应商logo链接*/
    private String brandIconUrl;
    /**
     * 供应商下单类型 0或空-单个下单 1-批量下单
     */
    /**
     * 出租车logo
     */
    private String czcLogo;

    /**
     * 预估价格明细
     */
    private String priceDetail;

    private String showYkjFlag;

    /**
     * 车型分类
     */
    @ApiModelProperty(value = "车型分类", dataType = "string")
    private String cxfl;
    /**
     * 车型分类名称
     */
    @ApiModelProperty(value = "车型分类名称", dataType = "string")
    private String cxflmc;

    /**
     * 是否可以修改目的地 0-可以 1-不可以
     */
    private String destinationChangeable;
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
    /**
     *  礼赠券
     */
    private List<CouponInfo> giftCouponInfos;

    /**
     *  优惠券
     */
    private List<CouponInfo> couponInfos;

    private BigDecimal jsj;

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}