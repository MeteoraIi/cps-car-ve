package cn.vetech.center.usecar.order.buyer.dto;

import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

/**
 * 用于正常单申请退款 ，取消订单操作所需传的参数
 * Created by vetech on 2017/9/18.
 * @author  nwt
 */
public class BuyerNormalOrderOperateDTO {
    /**正常单订单编号*/
    @ApiModelProperty(value = "正常单订单编号", dataType = "string",required = false)
    private String ddbh;
    /**取消原因*/
    @ApiModelProperty(value = "采购取消原因", dataType = "string")
    private String cgQxyy;
    /**采购商户编号*/
    @ApiModelProperty(value = "采购商户编号", dataType = "string")
    private String cgShbh;
    /**
     * 采购取消人
     */
    @ApiModelProperty(value = "采购取消人", dataType = "string")
    private String cgQxr;
    /**
     * 数据版本号(记录数据当前操作变更次数，每次有操作累加1)
     */
    @ApiModelProperty(value = "数据版本号(记录数据当前操作变更次数，每次有操作累加1)", dataType = "Long")
    private Long version;
    /**
     * 采购退单申请人(	退单申请人编号)
     */
    @ApiModelProperty(value = "采购退单申请人(	退单申请人编号)", dataType = "string")
    private String cgTdsqr;
    /**
     * 是否强制取消(true或false)默认false
     */
    @ApiModelProperty(value = "是否强制取消(true或false)默认false", dataType = "string")
    private String force;

    /**
     * 缓存id(传给ASMS的缓存id)
     */
    @ApiModelProperty(value = "缓存id(传给ASMS的缓存id)", dataType = "string")
    private String cpid;

    /**
     * 扣费金额(LINK返回)
     */
    @ApiModelProperty(value = "扣费金额(LINK返回)", dataType = "string")
    private String kfje;

    /**取消状态 true表示订单取消成功  false表示订单未取消
     *(LINK返回)
     **/
    @ApiModelProperty(value = "取消状态", dataType = "string")
    private String qszt;
    /**
     *手续费缓存ID主键
     */
    private String refundCacheId;

    /**
     * 是否测试，为1表示本次调用为测试。
     * 一般情况需要控制，不能下单到供应商，不能有实际支付，不能发短信，更不能让供应商出票
     * 如果能走缓存的尽量使用缓存数据
     * 走测试时，要能自动完成后续流程，如：不下单到供应商，但需要能自动回填票号
     */
    private String autotest;

    /**
     * cps主单编号
     */
    private String cpsMainOrderNo;

    /**
     *  是否取消
     */
    private String notCancel;

    /**
     * 重新取消
     */
    private String reCancel;

    private String sourceData;

    /**
     *  请求来自标准采购商
     */
    private String bzcgs;
    public String getBzcgs() {
        return bzcgs;
    }

    public void setBzcgs(String bzcgs) {
        this.bzcgs = bzcgs;
    }

    public String getSourceData() {
        return sourceData;
    }

    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    public String getReCancel() {
        return reCancel;
    }

    public void setReCancel(String reCancel) {
        this.reCancel = reCancel;
    }

    public String getNotCancel() {
        return notCancel;
    }

    public void setNotCancel(String notCancel) {
        this.notCancel = notCancel;
    }

    public String getCpsMainOrderNo() {
        return cpsMainOrderNo;
    }

    public void setCpsMainOrderNo(String cpsMainOrderNo) {
        this.cpsMainOrderNo = cpsMainOrderNo;
    }

    public String getDdbh() {
        return ddbh;
    }

    public void setDdbh(String ddbh) {
        this.ddbh = ddbh;
    }

    public String getCgShbh() {
        return cgShbh;
    }

    public void setCgShbh(String cgShbh) {
        this.cgShbh = cgShbh;
    }

    public String getCgQxyy() {
        return cgQxyy;
    }

    public void setCgQxyy(String cgQxyy) {
        this.cgQxyy = cgQxyy;
    }

    public String getCgQxr() {
        return cgQxr;
    }

    public void setCgQxr(String cgQxr) {
        this.cgQxr = cgQxr;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCgTdsqr() {
        return cgTdsqr;
    }

    public void setCgTdsqr(String cgTdsqr) {
        this.cgTdsqr = cgTdsqr;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    public String getCpid() {
        return cpid;
    }

    public void setCpid(String cpid) {
        this.cpid = cpid;
    }

    public String getKfje() {
        return kfje;
    }

    public void setKfje(String kfje) {
        this.kfje = kfje;
    }

    public String getQszt() {
        return qszt;
    }

    public void setQszt(String qszt) {
        this.qszt = qszt;
    }

    public String getRefundCacheId() {
        return refundCacheId;
    }

    public void setRefundCacheId(String refundCacheId) {
        this.refundCacheId = refundCacheId;
    }

    public String getAutotest() {
        return autotest;
    }

    public void setAutotest(String autotest) {
        this.autotest = autotest;
    }
    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
    }