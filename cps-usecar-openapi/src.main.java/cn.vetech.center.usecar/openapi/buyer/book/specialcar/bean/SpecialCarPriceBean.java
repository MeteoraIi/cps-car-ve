package cn.vetech.center.usecar.openapi.buyer.book.specialcar.bean;

import java.math.BigDecimal;

public class SpecialCarPriceBean {
    /**
     * 费用项目(中午名称)
     **/
    private String fyxm;
    /**
     * 费用项目(英文名称)
     **/
    private String fyxmEn;
    /**
     * 费用金额
     **/
    private BigDecimal fyje;

    public SpecialCarPriceBean() {
    }

    public SpecialCarPriceBean(String fyxm, BigDecimal fyje) {
        this.fyxm = fyxm;
        this.fyje = fyje;
    }

    public String getFyxm() {
        return fyxm;
    }

    public void setFyxm(String fyxm) {
        this.fyxm = fyxm;
    }

    public String getFyxmEn() {
        return fyxmEn;
    }

    public void setFyxmEn(String fyxmEn) {
        this.fyxmEn = fyxmEn;
    }

    public BigDecimal getFyje() {
        return fyje;
    }

    public void setFyje(BigDecimal fyje) {
        this.fyje = fyje;
    }
}
