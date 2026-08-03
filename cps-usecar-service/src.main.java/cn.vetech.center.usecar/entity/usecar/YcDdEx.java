package cn.vetech.center.usecar.entity.usecar;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;

// 加字段，防止重复加票
@TableName("yc_dd_ex")
public class YcDdEx {

    @TableId("pddbh")
    private String pddbh;

    @TableField("sfxgmdd")
    private String sfxgmdd;

    @TableField("old_cfd")
    private String oldCfd;

    @TableField("old_cfdxxd")
    private String oldCfdxxd;

    @TableField("old_mdd")
    private String oldMdd;

    @TableField("old_mddxxd")
    private String oldMddxxd;

    @TableField("company_id")
    private String companyId;

    @TableField("company_name")
    private String companyName;

    private String tjd;

    // 后面都是get set
}