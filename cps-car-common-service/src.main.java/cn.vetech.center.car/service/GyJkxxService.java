package cn.vetech.center.car.service;

import cn.vetech.center.car.dto.SupplierConfigAddDTO;
import cn.vetech.center.car.dto.SupplierConfigDTO;
import cn.vetech.center.car.entity.GyJkxx;
import cn.vetech.center.car.mapper.GyJkxxMapper;
import cn.vetech.center.car.vo.SupplierConfigsVO;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.vetech.core.base.BaseServiceImpl;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.PageCopyUtil;
import org.vetech.core.modules.utils.time.VeDate;

import java.util.List;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author houya
 * @since 2017-12-05
 */
@Service
public class GyJkxxService extends BaseServiceImpl<GyJkxxMapper, GyJkxx> {
    /**
     * 查询供应商配置信息列表
     *
     * @param dto 查询参数
     * @return 供应商集合列表
     * @author caopengfei
     */
    public Page<SupplierConfigsVO> selectPageBySHbh(PageDTO<SupplierConfigDTO> dto) {
        Page page = dto.genPage();
        EntityWrapper ew = new EntityWrapper();

        if (StringUtils.isNotBlank(dto.getData().getShbh())) {
            ew.eq("shbh",dto.getData().getShbh());
        }
        if (StringUtils.isNotBlank(dto.getData().getChannelId())) {
            ew.eq("channel_id",dto.getData().getChannelId());
        }
        if (StringUtils.isNotBlank(dto.getData().getCplx())) {
            ew.eq("cplx", dto.getData().getCplx());
        }
        Page<GyJkxx> gyJkxxPage = super.selectPage(page, ew);
        //copy page
        Page<SupplierConfigsVO> resultPage = PageCopyUtil.copy(gyJkxxPage, GyJkxx.class, SupplierConfigsVO.class);
        return resultPage;
    }

    /**
     * 有效商户
     * @return 有效商户
     */
    public List<GyJkxx> selectEffectGyJkxxList() {
        EntityWrapper ew = new EntityWrapper();
        ew.eq("cplx", 1);
        ew.eq("zt", 1);
        List<GyJkxx> gyJkxxList = this.selectList(ew);
        return gyJkxxList;
    }

    /**
     * 查询供应商配置信息列表
     *
     * @param shbh 商户编号
     * @param cplx 产品类型
     * @param zt   状态
     * @return 供应商配置信息
     * @author caopengfei
     */
    public GyJkxx selectGyjkxx(String shbh, String cplx, String zt) {
        EntityWrapper ew = new EntityWrapper();
        if (StringUtils.isNotBlank(shbh)) {
            ew.eq("shbh", shbh);
        }
        if (StringUtils.isNotBlank(cplx)) {
            ew.eq("cplx", cplx);
        }
        if (StringUtils.isNotBlank(zt)) {
            ew.eq("zt", zt);
        }
        GyJkxx gyJkxx = this.selectOne(ew);
        return gyJkxx;
    }

    /**
     * @param shbh 商户编号
     * @param cplx 产品类型
     * @return 供应商配置信息
     * @author caopengfei
     * 接口配置唯一验证
     */
    public GyJkxx selectCheckGyjkxx(String shbh, String cplx) {
        EntityWrapper ew = new EntityWrapper();

        if (StringUtils.isNotBlank(shbh)) {
            ew.eq("shbh", shbh);
        }
        if (StringUtils.isNotBlank(cplx)) {
            ew.eq("cplx", cplx);
        }
        GyJkxx gyJkxx = this.selectOne(ew);
        return gyJkxx;
    }

    /**
     * 添加供应商配置信息
     *
     * @param dto 添加参数
     * @return 是否成功
     */
    public Boolean insertSupplierConfig(SupplierConfigAddDTO dto) {
        GyJkxx gyJkxx = BeanMapper.map(dto, GyJkxx.class);
        gyJkxx.setXgDatetime(VeDate.getNow());
        return this.insert(gyJkxx);
    }

    /**
     * 编辑时候通过主键id查询数据
     *
     * @param id 主键id
     * @return 配置详细信息
     */
    public GyJkxx selectSuppilerConfigById(String id) {
        return super.selectById(id);
    }


    /**
     * 通过主键删除供应商配置信息
     *
     * @param dto 主键
     * @return 是否删除
     * *
     */
    public Boolean delectById(SupplierConfigAddDTO dto) {
        Boolean isDelete =  super.deleteById(dto.getId());
        return isDelete;
    }

    /**
     * 启用禁用
     *
     * @param dto 主键id
     * @return 是否成功
     */
    public Boolean upDown(SupplierConfigAddDTO dto) {
        GyJkxx gyJkxx = BeanMapper.map(dto, GyJkxx.class);
        Boolean isUpdate =  super.updateById(gyJkxx);
        return isUpdate;
    }

    /**
     * 通过商户编号查询
     * @param gysList 供应商编号
     * @return 回参
     */
    public List<GyJkxx> selectEffectGyJkxxByGysbhs(List<String> gysList) {
        EntityWrapper ew = new EntityWrapper();
        ew.eq("cplx", 1);
        ew.eq("zt", 1);
        ew.in("shbh", gysList);
        List<GyJkxx> gyJkxxList = this.selectList(ew);
        return gyJkxxList;
    }
}