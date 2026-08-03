package cn.vetech.center.usecar.openapi.buyer.order.createorder;

import cn.vetech.center.system.openapi.OpenApiRequest;
import cn.vetech.center.usecar.coupon.dto.CouponInfo;
import cn.vetech.center.usecar.entity.order.YcDdCk;
import com.baomidou.mybatisplus.annotations.TableField;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 采购类
 * 下单接口的request
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateOrderRequest extends OpenApiRequest {
    /** 服务保障级别*/
    private String fwbzjb;
    /** 服务保障标准*/
    private String fwbzbz;
    /**
     * 订单来源
     */
    private String ddly;//订单来源
    /**
     * 价格缓存ID
     */
    private String pricecacheid;
    /**
     * 航站楼
     */
    private String hzl;
    /**
     * 取消规则
     */
    private String qxgz;
    /**
     * 取消公式
     */
    private String qxgs;
    /**
     * 供应商户编号
     */
    @XmlElement(name = "gy_shbh")
    private String gyShbh;
    /**
     * 供应商户名称
     */
    @XmlElement(name = "gy_shmc")
    private String gyShmc;
    /**
     * 采购订单编号(ASMS采购商平台推送过来的订单编号)
     * 采购主单
     */
    @XmlElement(name = "cg_ddbh")
    private String cgDdbh;
    /**
     * 本单里程
     */
    private Double bdlc;
    /**
     * 本单时长
     */
    private Double bdsc;
    /**
     * 产品类型ID
     */
    private String cplxid;
    /**
     * 订单类型 100001：接机  100002：送机  100003：接站 100004：送站 100005-A：即时专车 100005-B：预约专车
     */
    private String ddlx;
    /**
     * 产品id
     */
    private String cpid;
    /**
     * 用车时间(2016/1/21 15:30 精确到小时分钟)
     */
    private String ycsj;
    /**
     * 乘客姓名
     */
    private String ckxm;
    /**
     * 乘客手机
     */
    private String cksj;
    /**
     * 乘客手机国际编码
     **/
    @ApiModelProperty(value = "乘客手机国际编码", example = "86")
    private String cksjGjbm;
    /**
     * 站点所在城市编号,站点所在城市编号(接送产品)
     */
    private String jszdcsid;
    /**
     * 站点编号
     */
    private String jsfwzdid;
    /**
     * 服务城市编号
     */
    private String jsfwcsid;
    /**
     * 服务城市区域编号 (接送产品)
     */
    private String jsfwqyid;
    /**
     * 服务详细地址(接车的目的地、送车的始发地)
     */
    private String jsfwxxdz;
    /**
     * 服务地址经度(接送产品)
     */
    @XmlElement(name = "jsfwdz_x")
    private String jsfwdzX;
    /**
     * 服务地址纬度(接送产品)
     */
    @XmlElement(name = "jsfwdz_y")
    private String jsfwdzY;
    /**
     * 专车始发经度(专车)
     */
    @XmlElement(name = "zcsfd_x")
    private String zcsfdX;
    /**
     * 专车始发纬度(专车)
     */
    @XmlElement(name = "zcsfd_y")
    private String zcsfdY;
    /**
     * 专车抵达经度(专车)
     */
    @XmlElement(name = "zcmdd_x")
    private String zcmddX;
    /**
     * 专车抵达纬度(专车)
     */
    @XmlElement(name = "zcmdd_y")
    private String zcmddY;
    /**
     * 车型组编号
     */
    private String cxzbh;
    /**
     * 车型组名称
     */
    private String cxzmc;
    /**
     * 航班/车次号(航班编号或者火车车次号)
     */
    private String hbcch;
    /**
     * 舱位编号
     */
    private String cwbh;
    /**
     * 订单备注
     */
    private String ddbz;
    /**
     * 服务内容
     */
    private String fwnr;
    /**
     * 服务标准
     */
    private String fwbz;
    /**
     * 车辆编号
     */
    private String cph;
    /**
     * 车身颜色
     */
    private String csys;
    /**
     * 车型名称
     */
    private String cxmc;
    /**
     * 司机姓名
     */
    private String sjxm;
    /**
     * 司机电话
     */
    private String sjdh;
    /**
     * 司机性别(1:男 2:女)
     */
    private String sjxb;
    /**
     * 采购用户编号
     */
    @XmlElement(name = "cg_yhbh")
    private String cgYhbh;
    /**
     * 采购商户编号
     */
    @XmlElement(name = "cg_shbh")
    private String cgShbh;
    /**
     * 订单联系人电话
     */
    private String lxrdh;
    /**
     * 联系人手机国际编码
     **/
    @ApiModelProperty(value = "联系人手机国际编码", example = "86")
    private String lxrsjGjbm;
    /**
     * 订单联系人姓名
     */
    private String lxr;
    /**
     * 出发地经度
     */
    @XmlElement(name = "cfd_x")
    private String cfdX;
    /**
     * 出发地纬度
     */
    @XmlElement(name = "cfd_y")
    private String cfdY;
    /**
     * 目的地经度
     */
    @XmlElement(name = "mdd_x")
    private String mddX;
    /**
     * 目的地纬度
     */
    @XmlElement(name = "mdd_y")
    private String mddY;
    /**
     * 付款金额
     */
    private Double fkje;
    /**
     * 预估金额
     */
    private Double ygje;
    /**
     * 采购返佣方式
     */
    @XmlElement(name = "cg_fyfs")
    private Integer cgFyfs;
    /**
     * 采购返佣比例
     */
    @XmlElement(name = "cg_fybl")
    private Double cgFybl;
    /**
     * 采购返佣金额
     */
    @XmlElement(name = "cg_fyje")
    private Double cgFyje;
    /**
     * 供应结算金额
     */
    @XmlElement(name = "gy_jsje")
    private Double gyJsje;
    /**
     * 供应返佣方式
     */
    @XmlElement(name = "gy_fyfs")
    private Integer gyFyfs;
    /**
     * 供应返佣比例
     */
    @XmlElement(name = "gy_fybl")
    private Double gyFybl;
    /**
     * 供应返佣金额
     */
    @XmlElement(name = "gy_fyje")
    private Double gyFyje;
    /**
     * 采购结算金额
     */
    @XmlElement(name = "cg_jsje")
    private Double cgJsje;
    /**
     * 平台控润金额
     */
    private Double ptkrje;
    /**
     * 平台控润方式
     */
    private Double ptkrfs;
    /**
     * 平台控润比例
     */
    private Double ptkrbl;
    /**
     * 建议金额
     */
    private Double jyje;
    /**
     * 接送服务始发地
     */
    private String jsfwsfd;
    /**
     * 接送服务目的地
     */
    private String jsfwmdd;
    /**
     * 滴滴专车下单时需要的接口
     */
    private String jgmd5;
    /**
     * 计价模式类别 滴滴需要
     */
    private String jjmslb;
    /**
     * 丽程需要的东西
     **/
    private String carmodelid;
    /**
     * 平台控润规则（1.结算价，2.差价,3.建议价）
     */
     private String ptkrgz;

    /**
     * 出发城市id
     */
    private String cfcs;

    /**
     * 目的城市
     */
    private String mdcs;
    /**
     * 上车城市名称
     */
    private String sccsMc;
    /**
     * 上车城市poi
     */
    private String sccsPoi;
    /**
     * 上车城市详细地址
     */
    private String sccsXxdz;
    /**
     * 目的城市名称
     */
    private String mdcsMc;
    /**
     * 目的城市Poi
     */
    private String mdcsPoi;
    /**
     * 目的城市详细地址
     */
    private String mdcsXxdz;
    /**
     * 特殊需求
     */
    private String tsxq;

    /**因公、因私*/
    private String clyy;
    /**服务商编号*/
    private String fwsbh;
    /**服务商名称*/
    private String fwsmc;
    /**
     * 是否先用后付 0或空-否 1-是
     */
    private String sfxyhf;

    /**
     * 主表订单编号
     */
    private String zbddbh;
    /**
     * 是否一键三单
     */
    private String sfyjsd;
    /**
     * 0:cps 确认订单，1费控确认订单
     */
    private String confirmOrder;
    /**
     * 起飞时间
     */
    private String flightDateTime;
    /**
     * cps订单数量
     */
    private Integer cpsOrderCount;
    /**
     * 预定人id
     */
    @ApiModelProperty(value = "预定人id", dataType = "String")
    private String bookerId;
    /**
     * 预定人编号
     */
    @ApiModelProperty(value = "预定人编号", dataType = "String")
    private String bookerNo;
    /**
     * 预定人姓名
     */
    @ApiModelProperty(value = "预定人姓名", dataType = "String")
    private String bookerName;
    /**
     * 部门全路径名称
     */
    @ApiModelProperty(value = "部门全路径名称", dataType = "String")
    private String fullDeptName;
    /**
     * 员工信息
     */
    @ApiModelProperty(value = "乘客信息", dataType = "list")
    private List<YcDdCk> passengerList;
    /**
     * 结算单位编号
     */
    @ApiModelProperty(value = "结算单位编号",dataType = "String")
    private String settleDeptNo;
    /**
     * 结算单位名称
     */
    @ApiModelProperty(value = "结算单位名称",dataType = "String")
    private String settleDeptName;
    /**
     * 项目编号
     */
    @ApiModelProperty(value = "项目编号",dataType = "String")
    private String projectNo;
    /**
     * 项目名称
     */
    @ApiModelProperty(value = "项目名称",dataType = "String")
    private String projectName;
    /**
     * 项目id
     */
    @ApiModelProperty(value = "项目id",dataType = "String")
    private String projectId;
    /**
     * 预定人部门id
     */
    @TableField("booker_dept_id")
    @ApiModelProperty(value = "预订人部门id", dataType = "String")
    private String bookerDeptId;
    /**
     * 预定人部门名称
     */
    @TableField("booker_dept_name")
    @ApiModelProperty(value = "预定人部门名称", dataType = "String")
    private String bookerDeptName;
    /**
     * 结算单位id
     */
    @ApiModelProperty(value = "结算单位id", dataType = "string")
    private String settleDeptId;
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
    @ApiModelProperty(value = "是否24小时自动支付", dataType = "String")
    private String autoPay;
    /**
     * 自动支付类型 0-立即 24-24小时 48-48小时
     */
    @ApiModelProperty(value = "自动支付类型 0-立即 24-24小时 48-48小时", dataType = "String")
    private String autoPayType;
    /**
     * 子订单
     */
    private List<ChildOrderDTO> orders;
    /**
     *  1：新下单模式 0 ：旧订单模式 默认旧模式
     */
    private String newOrder;
    /**
     * cps主单编号
     */
    private String cpsMainOrderNo;

    /**
     *  请求来自标准采购商
     */
    private String bzcgs;
    /**
     *  是否包车
     */
    private String isCharCar;
    /**
     *  团单询价单号
     */
    private String xjdh;

    /**
     *  团单报价单号
     */
    private String bjdh;

    private String xdgysbh;
    /**
     * 语种
     */
    @ApiModelProperty(value = "订单语种", dataType = "String")
    private String ddlyyz;
    /**
     * 会员id
     */
    @ApiModelProperty(value = "会员id", dataType = "String")
    private String memberId;
    /**
     * 租户编号
     */
    @ApiModelProperty(value = "租户编号", dataType = "string")
    private String tnCode;
    /**
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id", dataType = "string")
    private String channelId;

    /**
     * 呼叫规则0接单最快1时间最短2价格最低
     */
    @ApiModelProperty(value = "呼叫规则0接单最快1时间最短2价格最低", dataType = "string")
    private String callRule;
    /**
     * 是否一键三单
     */
    @ApiModelProperty(value = "是否一键三单", dataType = "string")
    private String threeOrders;

    @ApiModelProperty(value = "高德poiid", dataType = "string")
    private String cfdpoiid;

    @ApiModelProperty(value = "高德poiid", dataType = "string")
    private String mddpoiid;

    private String showYkjFlag;

    /**
     * 最晚计划出发时间
     */
    @ApiModelProperty(value = "最晚计划出发时间", example = "2025-12-20 10:22")
    private String endPlanStartTime;
    /**
     * 乘坐人数
     */
    @ApiModelProperty(value = "乘坐人数", example = "4")
    private Integer paxNum;


    /**
     * 高速费承担：1-乘客全部承担，2-车主全部承担，3-愿意协商高速费
     */
    @ApiModelProperty(value = "高速费承担：1-乘客全部承担，2-车主全部承担，3-愿意协商高速费", dataType = "string")
    private String bearHighwayFeeType;
    /**
     * 订单预估金额超过
     */
    @ApiModelProperty(value = "订单预估金额超过", dataType = "string")
    private String estimatedAmountExceeds;
    /**
     * 订单预估里程超过km
     */
    @ApiModelProperty(value = "订单预估里程超过km", dataType = "string")
    private String estimatedMileageExceeds;

    @ApiModelProperty(value = "法人公司id", dataType = "string")
    private String companyId;

    @ApiModelProperty(value = "法人公司名称", dataType = "string")
    private String companyName;

    /**
     *  领取的优惠券
     */
    private List<CouponInfo> claimedCouponInfos;
    /**
     *  使用优惠券
     */
    private List<CouponInfo> couponInfos;

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }

    // 后面都是get，set方法

}