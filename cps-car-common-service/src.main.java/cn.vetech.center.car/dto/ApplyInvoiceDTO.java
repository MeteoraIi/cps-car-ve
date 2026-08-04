package cn.vetech.center.car.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author xufei
 * @since 2023/4/3
 */
@XmlRootElement(name="request")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplyInvoiceDTO {
    /**
     * 订单编号
     */
    private String ddbh ;

    /**
     * 开票金额
     */
    private String  fpje;

    /**
     * 发票抬头
     */
    private String  fptt;

    /**
     * 纳税人识别号
     */
    private String nsrsbh ;

    /**
     * 开户行
     */
    private String  khyh;

    /**
     * 开户行账号
     */
    private String  yhzh;

    /**
     * 电话
     */
    private String zcdh ;

    /**
     * 公司地址
     */
    private String zcdz;
    /**
     *  发票内容
     */
    private String fpnr ;

    /**
     * 发票备注
     */
    private String fpbz ;
    /**
     * 邮箱
     */
    private String email ;
    /**
     * 发票类型：1-增值税专票 2-增值税普票 24-全电发票（专票） 25-全电专票（普票）
     */
    private String fplx;

    // 后面都是get，set
}
