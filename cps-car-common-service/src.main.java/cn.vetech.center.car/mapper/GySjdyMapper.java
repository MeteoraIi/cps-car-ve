package cn.vetech.center.car.mapper;

import cn.vetech.center.car.dto.UpdateCancelLimitDTO;
import cn.vetech.center.car.entity.GySjdy;
import org.apache.ibatis.annotations.Param;
import org.vetech.core.base.BaseMapper;

import java.util.List;

/**
 * <p>
  * 共用数据对应表 Mapper 接口
 * </p>
 *
 * @author chenjunfeng
 * @since 2017-10-19
 */
public interface GySjdyMapper extends BaseMapper<GySjdy> {

    /**
     * 查询平台名称集合
     *
     * @param corpIds  查询条件
     * @return list集合
     */
    List<GySjdy> selectPtmcList(@Param("corpIds") List<String> corpIds);

    /**
     * 修改取消时限
     * @param dto 入参
     * @return 是否成功
     */
    boolean updateCancelLimit(@Param("dto") UpdateCancelLimitDTO dto);
}