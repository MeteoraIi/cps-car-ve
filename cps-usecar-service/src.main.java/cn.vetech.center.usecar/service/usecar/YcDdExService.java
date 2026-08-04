package cn.vetech.center.usecar.service.usecar;

import cn.vetech.center.usecar.entity.usecar.YcDdEx;
import cn.vetech.center.usecar.mapper.usecar.YcDdExMapper;
import cn.vetech.center.usecar.service.UsecarCacheBaseServiceImpl;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用车订单扩展表服务
 * </p>
 *
 * @author vetech
 * @since 2021-09-01
 */
@Service
public class YcDdExService extends UsecarCacheBaseServiceImpl<YcDdExMapper, YcDdEx> {

    public int insertYcDdEx(YcDdEx ycddex) {
        return this.baseMapper.insert(ycddex);
    }

    public YcDdEx selectBypDdbh(String ddbh) {
        return this.baseMapper.selectById(ddbh);
    }

    public int updateYcDdEx(YcDdEx ycDdEx) {
        return this.baseMapper.updateById(ycDdEx);
    }

    /**
     * 只更新主单发票状态，避免覆盖扩展表其他字段。
     *
     * @param pDdbh             主单编号
     * @param mainInvoiceStatus 主单发票状态
     * @return 更新行数
     */
    public int updateMainInvoiceStatus(String pDdbh, String mainInvoiceStatus) {
        if (StringUtils.isBlank(pDdbh)) {
            return 0;
        }
        YcDdEx ycDdEx = new YcDdEx();
        ycDdEx.setMainInvoiceStatus(mainInvoiceStatus);
        EntityWrapper<YcDdEx> wrapper = new EntityWrapper<>();
        wrapper.eq("pddbh", pDdbh);
        return this.baseMapper.update(ycDdEx, wrapper);
    }
}
