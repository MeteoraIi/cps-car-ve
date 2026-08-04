package cn.vetech.center.usecar.service.usecar;

import cn.vetech.center.usecar.entity.usecar.YcDdEx;
import cn.vetech.center.usecar.mapper.usecar.YcDdExMapper;
import cn.vetech.center.usecar.service.UsecarCacheBaseServiceImpl;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Use car order extension service.
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

    public int updateMainInvoiceStatus(String pDdbh, String mainInvoiceStatus) {
        if (StringUtils.isBlank(pDdbh)) {
            return 0;
        }
        YcDdEx ycDdEx = new YcDdEx();
        ycDdEx.setMainInvoiceStatus(mainInvoiceStatus);
        EntityWrapper<YcDdEx> wrapper = new EntityWrapper<>();
        wrapper.eq("pddbh", pDdbh);
        // Only update main_invoice_status, do not overwrite other extension fields.
        return this.baseMapper.update(ycDdEx, wrapper);
    }
}
