package cn.vetech.center.usecar.book.buyer.specicar.dto;

import cn.vetech.center.usecar.setting.profit.dto.MemberDiscountInfo;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;

/**
 * 专快车采购查询预订 查询条件DTO
 * @author houshuang
 * @since 2017-11-03
 */
public class BookSpeciCarSearchDTO {


    /**
     * 产品类型
     */
    @ApiModelProperty(value="产品类型",dataType = "String",required = false)
    private String cplx;

    /**
     * （服务城市编号），
     */
    @ApiModelProperty(value="服务城市编号",dataType = "String",required = false)
    private String jsfwcsid;

    /**
     * 服务城市编号(目的地)
     */
    @ApiModelProperty(value = "服务城市编号(目的地)", dataType = "string")
    private String mmdCsid;
    /**
     * 滴滴用车专用服务城市ID(取中间表)
     */
    @ApiModelProperty(value="滴滴用车专用服务城市ID(取中间表)",dataType = "String",required = false)
    private String ddjsfwcsid;
    /**
     * (用车时间(2016/1/21 15:30 精确到小时分钟))
     */
    @ApiModelProperty(value="(用车时间(2016/1/21 15:30 精确到小时分钟))",dataType = "String",required = false)
    private String ycsj;
    /**
     * 始发地名称
     */
    @ApiModelProperty(value="始发地名称",dataType = "String",required = false)
    private String jsfwsfdmc;
    /**
     * 始发地详细地址
     */
    @ApiModelProperty(value="始发地详细地址",dataType = "String",required = false)
    private String jsfwsfd;
    /**
     * 目的地名称
     */
    @ApiModelProperty(value="目的地名称",dataType = "String",required = false)
    private String jsfwmddmc;
    /**
     * 目的地详细地址
     */
    @ApiModelProperty(value="目的地详细地址",dataType = "String",required = false)
    private String jsfwmdd;
    /**
     * 专车始发经度(专车) 高德地图
     */
    @ApiModelProperty(value="专车始发经度(专车) 高德地图",dataType = "String",required = false)
    private String cfdX;
    /**
     * 专车始发维度(专车) 高德地图
     */
    @ApiModelProperty(value="专车始发维度(专车) 高德地图",dataType = "String",required = false)
    private String cfdY;
    /**
     * 专车目的地经度(专车) 高德地图
     */
    @ApiModelProperty(value="专车目的地经度(专车) 高德地图",dataType = "String",required = false)
    private String mddX;
    /**
     * 专车目的地度(专车) 高德地图
     */
    @ApiModelProperty(value="专车目的地度(专车) 高德地图",dataType = "String",required = false)
    private String mddY;
    /**
     * soso出发地经度
     */
    @ApiModelProperty(value="soso出发地经度",dataType = "String",required = false)
    private String sosoCfdX;
    /**
     * soso出发地维度
     */
    @ApiModelProperty(value="soso出发地维度",dataType = "String",required = false)
    private String sosoCfdY;
    /**
     * soso目的地经度
     */
    @ApiModelProperty(value="soso目的地经度",dataType = "String",required = false)
    private String sosoMddX;
    /**
     * soso目的地维度
     */
    @ApiModelProperty(value="soso目的地维度",dataType = "String",required = false)
    private String sosoMddY;

    /**
     * 询价里程预计里程数(米)
     */
    @ApiModelProperty(value="询价里程预计里程数(米)",dataType = "String",required = false)
    private BigDecimal distance;

    /**
     * 询价里程预计时长数(分钟)
     */
    @ApiModelProperty(value="询价里程预计时长数(分钟)",dataType = "String",required = false)
    private BigDecimal duration;

    /**
     * 用车日期
     */
    @ApiModelProperty(value="用车日期",dataType = "String",required = false)
    private String cfrq;

    /**
     * 用车时间
     */
    @ApiModelProperty(value="用车时间",dataType = "String",required = false)
    private String cfsj;

    /**
     * 用户编号
     */
    @ApiModelProperty(value="用户编号",dataType = "String",required = false)
    private String yhbh;

    /**
     * 用户名称
     */
    @ApiModelProperty(value="用户名称",dataType = "String",required = false)
    private String yhmc;

    /**
     * 采购商户编号
     */
    @ApiModelProperty(value="采购商户编号",dataType = "String",required = false)
    private String cgShbh;
    /**
     * 商户性质
     */
    @ApiModelProperty(value="商户性质",dataType = "String",required = false)
    private String shxz;
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
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id", dataType = "string")
    private String channelId;
    /**
     * 是否全量数据
     */
    private boolean queryAll;

    public boolean isQueryAll() {
        return queryAll;
    }

    public void setQueryAll(boolean queryAll) {
        this.queryAll = queryAll;
    }


    private String memberId;

    /**
     * 是否查询会员价：true-查询会员价，false-查询普通价
     */
    private String queryMemberPrice;

    private MemberDiscountInfo memberDiscountInfo;

    public MemberDiscountInfo getMemberDiscountInfo() {
        return memberDiscountInfo;
    }

    public void setMemberDiscountInfo(MemberDiscountInfo memberDiscountInfo) {
        this.memberDiscountInfo = memberDiscountInfo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getQueryMemberPrice() {
        return queryMemberPrice;
    }

    public void setQueryMemberPrice(String queryMemberPrice) {
        this.queryMemberPrice = queryMemberPrice;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getEndPlanStartTime() {
        return endPlanStartTime;
    }

    public void setEndPlanStartTime(String endPlanStartTime) {
        this.endPlanStartTime = endPlanStartTime;
    }

    public Integer getPaxNum() {
        return paxNum;
    }

    public void setPaxNum(Integer paxNum) {
        this.paxNum = paxNum;
    }

    public String getShxz() {
        return shxz;
    }

    public void setShxz(String shxz) {
        this.shxz = shxz;
    }

    public String getCgshjc() {
        return cgshjc;
    }

    public void setCgshjc(String cgshjc) {
        this.cgshjc = cgshjc;
    }

    /**
     * 采购商户简称
     */

    private String cgshjc;
    /**
     * 当前星期几
     */
    @ApiModelProperty(value="当前星期几",dataType = "String",required = false)
    private String week;

    /**
     * 计价模式类别
     */
    @ApiModelProperty(value="计价模式类别",dataType = "String",required = false)
    private String jjmslb;

    /**
     * 预订方式 1立即用车，2预约用车
     */
    @ApiModelProperty(value="预订方式",dataType = "String",required = false)
    private String ydfs;

    /**
     * 出发地poi
     */
    @ApiModelProperty(value="出发地poi",dataType = "String",required = false)
    private String cfd;

    /**
     * 目的地poi
     */
    @ApiModelProperty(value="目的地poi",dataType = "String",required = false)
    private String mdd;

    /**
     * 渠道来源（CPS,ASMS）
     */
    @ApiModelProperty(value="渠道来源（CPS,ASMS）",dataType = "String",required = false)
    private String qdly;

    private String bzcgs;

    private String sourceData;

    private String cityLevel;

    private String cxrsj;

    /**
     * 游标id,异步查询时使用
     */
    private String cursorId;
    /**
     * 异步加载查询
     */
    private String asyn;

    /**
     * 1，查询所有顺风车，其他否
     */
    private String rideShare;

    // 后面都是get，set方法，就不粘了

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}