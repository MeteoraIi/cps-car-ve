package cn.vetech.center.car.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.base.BaseEntity;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author houya
 * @since 2017-12-06
 */
@TableName("gy_jkxx")
public class GyJkxx extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId("id")

    @ApiModelProperty(value = "id", dataType = "string")
    private String id;
    /**
     * 商户编号
     */
    @ApiModelProperty(value = "商户编号", dataType = "string")
    private String shbh;
    /**
     * 状态（0.无效，1.有效）
     */
    @ApiModelProperty(value = "状态（0.无效，1.有效）", dataType = "string")
    private String zt;
    /**
     * 接口版本号
     */
    @Version
    @ApiModelProperty(value = "接口版本号", dataType = "string")
    private String version;
    /**
     * 产品类型（1.用车，2.租车，3.包车）
     */
    @ApiModelProperty(value = "产品类型（1.用车，2.租车，3.包车）", dataType = "string")
    private String cplx;
    /**
     * 商户简称
     */
    @ApiModelProperty(value = "商户简称", dataType = "string")
    private String shjc;
    /**
     * 修改人
     */
    @TableField("xg_userid")
    @ApiModelProperty(value = "修改人", dataType = "string")
    private String xgUserid;
    /**
     * 修改时间
     */
    @TableField("xg_datetime")
    @ApiModelProperty(value = "修改时间", dataType = "date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date xgDatetime;
    /**
     * 备注备注
     */
    @ApiModelProperty(value = "备注备注", dataType = "string")
    private String bzbz;

    @TableField("logo_url")
    @ApiModelProperty(value = "出租车供应商logo地址")
    private String logoUrl;

    /**
     * 是否启用logo
     */
    @TableField("enable_logo")
    private String enableLogo;
    /**
     * 渠道账号类型
     */
    @TableField("channel_type")
    @ApiModelProperty(value = "渠道账号类型：DDYC，SYYC等")
    private String channelType;

    /**
     * 渠道id
     */
    @TableField("channel_id")
    @ApiModelProperty(value = "渠道id")
    private String channelId;

    /**
     * 租户编号
     */
    @TableField("tn_code")
    @ApiModelProperty(value = "租户编号", dataType = "string")
    private String tnCode;
    
    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }

    // 后面都是get，set
}