package cn.vetech.center.usecar.openapi.buyer.order.createspecialcarorder;

import cn.vetech.center.system.openapi.OpenApiRequest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 采购类
 * 下单接口的request
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateSpecialCarOrderRequest extends OpenApiRequest {
    /**
     * 服务保障级别
     */
    private String fwbzjb;
    /**
     * 服务保障标准
     */
    private String fwbzbz;
    /**
     * 订单来源
     */
    private String ddly;
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
     * 订单编号(ASMS采购商平台推送过来的订单编号)
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
     * 订单类型
     */
    private String ddlx; //产品类型  100001：接机  100002：送机  100003：接站 100004：送站 100005-A：即时专车 100005-B：预约专车
    /**
     * 产品id
     */
    private String cpid;
    /**
     * 用车时间 (2016/1/21 15:30 精确到小时分钟)
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
     * 支付金额
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
     * 采购返佣值
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
    @XmlElement(name="gy_jsje")
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
    private String carmodelid;//
    /**
     * 平台控润规则（1.结算价，2.差价,3.建议价）
     */
    private String ptkrgz;


    /**
     * 目的城市id
     */
    private String mdcs;
    /**
     * 出发城市id
     */
    private String cfcs;
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

    // 后面是一堆get set 所以没粘
    
}