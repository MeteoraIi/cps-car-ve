package cn.vetech.center.usecar.openapi.buyer.book.specialcar;

import cn.vetech.center.system.openapi.OpenApiResponse;
import cn.vetech.center.usecar.analysis.report.vo.RecommendGysInfo;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 专车产品查询列表接口的根节点response
 *
 * @author chenyong
 * @since 2017-11-09
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class BuyerSpecialCarResponse extends OpenApiResponse {


    /**
     * 专车产品集合
     */
    private List<BuyerSpecialCar> cplist;
    /**
     * 上车经度
     */
    private String scjd;
    /**
     * 上车纬度
     */
    private String scwd;
    /**
     * 送达经度
     */
    private String sdjd;
    /**
     * 送达维度
     */
    private String sdwd;
    /**
     * 上车城市id
     */
    private String sccs;
    /**
     * 距离
     */
    private String distance;
    /**
     * 时长
     */
    private String duration;
    /**
     * soso出发地经度
     **/
    @XmlElement(name = "soso_cfd_x")
    private String sosoCfdX;
    /**
     * soso出发地维度X
     **/
    @XmlElement(name = "soso_cfd_y")
    private String sosoCfdY;
    /**
     * soso目的地经度
     **/
    @XmlElement(name = "soso_mdd_x")
    private String sosoMddX;
    /**
     * soso目的地维度
     **/
    @XmlElement(name = "soso_mdd_y")
    private String sosoMddY;

    private String linkResult;
    /**
     * 游标Id，异步加载查询时返回
     */
    private String cursorId;

    /**
     *  默认查询完成,异步加载查询时返回
     */
    private Boolean finishFlag;

    private String cacheTimeout;

    private List<RecommendGysInfo> recommendGysInfos;

    private String distanceUnit = "m";

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
    // 后面全是get，set
}
