package cn.vetech.center.usecar.book.buyer.specicar.dto;

import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.modules.utils.mapper.JsonMapper;

/**
 * 专快车采购下单DTO
 * @author houshuang
 * @since 2017-11-06
 */
public class BookSpeciCarOrderDTO {
    /** 服务保障级别*/
    private String fwbzjb;
    /**
     * 采购订单来源
     */
    @ApiModelProperty(value="采购订单来源",dataType = "String",required = false)
    private String cgDdly;
    /**
     * 采购商户编号
     */
    @ApiModelProperty(value="采购商户编号",dataType = "String",required = false)
    private String cgShbh;

    /**
     * 采购商户简称
     */
    @ApiModelProperty(value="采购商户简称",dataType = "String",required = false)
    private String cgShjc;

    /**
     * 采购用户编号
     */
    @ApiModelProperty(value="采购用户编号",dataType = "String",required = false)
    private String cgYhbh;

    /**
     * 价格缓存ID
     */
    @ApiModelProperty(value="价格缓存ID",dataType = "String",required = false)
    private String priceCacheId;

    /**
     * 乘客姓名
     */
    @ApiModelProperty(value="乘客姓名",dataType = "String",required = false)
    private String ckxm;

    /**
     * 乘客手机
     */
    @ApiModelProperty(value="乘客手机",dataType = "String",required = false)
    private String cksj;

    /**
     * 联系人
     */
    @ApiModelProperty(value="联系人",dataType = "String",required = false)
    private String lxr;

    /**
     * 联系人电话
     */
    @ApiModelProperty(value="联系人电话",dataType = "String",required = false)
    private String lxrdh;
    /**
     * 特殊需求
     */
    @ApiModelProperty(value="特殊需求",dataType = "String",required = false)
    private String tsxq;

    /**
     * 预订方式 1立即用车，2预约用车
     */
    @ApiModelProperty(value="预订方式",dataType = "String",required = false)
    private String ydfs;
    /**
     * 差旅类型 1-因公 2-因私
     */
    private String clyy;

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }

    // 后面都是get set 所以没粘
}