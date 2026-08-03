package cn.vetech.center.car.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import com.baomidou.mybatisplus.enums.IdType;
import io.swagger.annotations.ApiModelProperty;
import org.vetech.core.base.BaseEntity;
import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.math.BigDecimal;

/**
 * <p>
 * 共用数据对应表
 * </p>
 *
 * @author chenjunfeng
 * @since 2017-10-19
 */
@TableName("gy_sjdy")
public class GySjdy extends BaseEntity {

    private static final long serialVersionUID = 1L;

        /**
     * ID号(主键)
     */
    @TableId(value = "id",type = IdType.ID_WORKER)

    @ApiModelProperty(value = "ID号(主键)", dataType = "string")
        private String id;
        /**
     * 数据编号
     */
    @ApiModelProperty(value = "数据编号", dataType = "string")
        private String sjbh;
        /**
     * 数据名称
     */
    @ApiModelProperty(value = "数据名称", dataType = "string")
        private String sjmc;
        /**
     * 数据类型(1:火车站,2:机场,3:航站楼,4:城市)
     */
    @ApiModelProperty(value = "数据类型(1:火车站,2:机场,3:航站楼,4:城市)", dataType = "string")
        private String sjlx;
        /**
     * 供应数据编号
     */
    @TableField("gy_sjbh")
    @ApiModelProperty(value = "供应数据编号", dataType = "string")
        private String gySjbh;
        /**
     * 供应数据名称
     */
    @TableField("gy_sjmc")
    @ApiModelProperty(value = "供应数据名称", dataType = "string")
        private String gySjmc;
        /**
     * 供应商户编号
     */
    @TableField("gy_shbh")
    @ApiModelProperty(value = "供应商户编号", dataType = "string")
        private String gyShbh;
        /**
     * 数据版本号
     */
    @Version
    @ApiModelProperty(value = "数据版本号", dataType = "bigdecimal")
        private BigDecimal version;
        /**
     * 是否手工维护(0.否，1.是)
     */
    @ApiModelProperty(value = "是否手工维护(0.否，1.是)", dataType = "string")
        private String sfsgwh;
    /**
     * 取消时限(秒)
     */
    @TableField("cancel_limit")
    @ApiModelProperty(value = "取消时限(秒)", dataType = "string")
    private String cancelLimit;

    public String getCancelLimit() {
        return cancelLimit;
    }

    public void setCancelLimit(String cancelLimit) {
        this.cancelLimit = cancelLimit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSjbh() {
        return sjbh;
    }

    public void setSjbh(String sjbh) {
        this.sjbh = sjbh;
    }

    public String getSjmc() {
        return sjmc;
    }

    public void setSjmc(String sjmc) {
        this.sjmc = sjmc;
    }

    public String getSjlx() {
        return sjlx;
    }

    public void setSjlx(String sjlx) {
        this.sjlx = sjlx;
    }

    public String getGySjbh() {
        return gySjbh;
    }

    public void setGySjbh(String gySjbh) {
        this.gySjbh = gySjbh;
    }

    public String getGySjmc() {
        return gySjmc;
    }

    public void setGySjmc(String gySjmc) {
        this.gySjmc = gySjmc;
    }

    public String getGyShbh() {
        return gyShbh;
    }

    public void setGyShbh(String gyShbh) {
        this.gyShbh = gyShbh;
    }

    public BigDecimal getVersion() {
        return version;
    }

    public void setVersion(BigDecimal version) {
        this.version = version;
    }

    public String getSfsgwh() {
        return sfsgwh;
    }

    public void setSfsgwh(String sfsgwh) {
        this.sfsgwh = sfsgwh;
    }

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}