package cn.vetech.center.usecar.setting.buyerfilter.dto;

import cn.vetech.center.usecar.setting.buyerfilter.vo.BuyerFilterVO;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.io.Serializable;

/**
 * Created by xujin on 2017/12/14.
 * @since 2017/12/14.
 * @author xujin
 */
public class BuyerFilterBookDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**采购商户编号*/
    private String cgShbh;
    /**供应商户编号*/
    private String gyShbh;
    /**产品类型*/
    private String cplx;
    /**站点id*/
    private String zdid;

    /**
     * 匹配得到的规则
     */
    private BuyerFilterVO buyerFilterVO;

    /**
     * 渠道id
     */
    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public BuyerFilterVO getBuyerFilterVO() {
        return buyerFilterVO;
    }

    public void setBuyerFilterVO(BuyerFilterVO buyerFilterVO) {
        this.buyerFilterVO = buyerFilterVO;
    }

    public String getCgShbh() {
        return cgShbh;
    }

    public void setCgShbh(String cgShbh) {
        this.cgShbh = cgShbh;
    }

    public String getGyShbh() {
        return gyShbh;
    }

    public void setGyShbh(String gyShbh) {
        this.gyShbh = gyShbh;
    }

    public String getCplx() {
        return cplx;
    }

    public void setCplx(String cplx) {
        this.cplx = cplx;
    }

    public String getZdid() {
        return zdid;
    }

    public void setZdid(String zdid) {
        this.zdid = zdid;
    }

    /**
     * @return 返回json
     */
    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}
