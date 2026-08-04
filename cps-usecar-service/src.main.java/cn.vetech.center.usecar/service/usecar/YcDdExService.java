package cn.vetech.center.usecar.service.usecar;


import cn.vetech.center.usecar.entity.usecar.YcDdEx;
import cn.vetech.center.usecar.mapper.usecar.YcDdExMapper;
import cn.vetech.center.usecar.service.UsecarCacheBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用车订单拓展变
 * </p>
 *
 * @author vetech
 * @since 2021-09-01
 */
@Service
public class YcDdExService extends UsecarCacheBaseServiceImpl<YcDdExMapper, YcDdEx> {

    public int insertYcDdEx(YcDdEx ycddex){
        return this.baseMapper.insert(ycddex);
    }

    public YcDdEx selectBypDdbh(String ddbh){
        return this.baseMapper.selectById(ddbh);
    }


    public int updateYcDdEx(YcDdEx ycDdEx){
        return this.baseMapper.updateById(ycDdEx);
    }
}