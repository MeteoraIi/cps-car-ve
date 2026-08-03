package cn.vetech.center.car.service;

import cn.vetech.center.car.dto.DataMappingDTO;
import cn.vetech.center.car.dto.DataMappingSearchDTO;
import cn.vetech.center.car.dto.UpdateCancelLimitDTO;
import cn.vetech.center.car.entity.GySjdy;
import cn.vetech.center.car.mapper.GySjdyMapper;
import cn.vetech.center.car.vo.DataMappingVO;
import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.mapper.JsonMapper;
import org.vetech.core.modules.utils.mapper.PageCopyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 共用数据对应表 服务实现类
 * </p>
 *
 * @author chenjunfeng
 * @since 2017-10-19
 */
@Service
public class GySjdyService extends CarCacheBaseServiceImpl<GySjdyMapper, GySjdy> {

    /**
     * 数据对应表，数据类型：火车站
     */
    public static final String SJDY_LX_TRAIN = "1";
    /**
     * 数据对应表，数据类型：机场
     */
    public static final String SJDY_LX_AIRPORT = "2";
    /**
     * 数据对应表，数据类型：航站楼
     */
    public static final String SJDY_LX_HZL = "3";
    /**
     * 数据对应表，数据类型：城市
     */
    public static final String SJDY_LX_CITY = "4";
    /**
     * 数据是否手工维护过-未维护过(默认)
     */
    public static final String DATA_SFSGWH_N = "0";
    /**
     * 数据是否手工维护过-维护过
     */
    public static final String DATA_SFSGWH_Y = "1";
    /**
     * 日志信息
     */
    private static final Logger logger = LoggerFactory.getLogger(GySjdyService.class);

    @Autowired
    private RedisCacheManage redisCacheManage;

    /**
     * 获取多商户对应城市列表
     * 注意：这里传入的商户编号是要满足 in  检索格式的参数
     *
     * @param paramEntity 城市ID对应数据查询数据
     * @return 多商户指定城市的对应数据列表
     */
    public List<GySjdy> getCitySjdyList(GySjdy paramEntity) {
        EntityWrapper ew = new EntityWrapper();
        String gyshbhlike = paramEntity.getGyShbh();
        paramEntity.setGyShbh(null);
        ew.setEntity(paramEntity);
        ew.in("GY_SHBH", gyshbhlike);
        return this.selectList(ew);
    }

    /**
     * 依据调价查询符合要求的数据列表
     *
     * @param param 查询参数
     * @return 符合条件的检索结果列表
     */
    public List<GySjdy> queryList(GySjdy param) {
        EntityWrapper ew = new EntityWrapper();
        ew.setEntity(param);
        return super.selectList(ew);
    }

    /**
     * 根据供应商城市编号查询系统城市编号
     *
     * @return 符合条件的检索结果列表
     */
    public String selectCityName(String supplierCityId, String supplerNo) {
        EntityWrapper<GySjdy> ew = new EntityWrapper();
        ew.eq("gy_sjbh", supplierCityId);
        ew.eq("sjlx", "4");
        ew.eq("gy_shbh", supplerNo);
        GySjdy gySjdy = super.selectOne(ew);
        return gySjdy == null ? null : gySjdy.getSjbh();
    }

    /**
     * 依据主键获取数据对象
     *
     * @param id 主键
     * @return 符合主键匹配的数据对象
     */
    public GySjdy queryById(String id) {
        return super.selectById(id);
    }

    /**
     * 将合并后的数据入库到数据对应表
     *
     * @param sjdyList 合并后的数据(依接口数据为标准，和本地基础数据匹配，匹配上的就到这里来进行入库)
     */
    public void updateCity(List<GySjdy> sjdyList) {
        if (CollectionUtil.isNotEmpty(sjdyList)) {
            GySjdy tempSjdy = sjdyList.get(0);
            EntityWrapper<GySjdy> ew = new EntityWrapper<>();
            logger.info("先删除供应商:" + tempSjdy.getGyShbh() + ",数据类型:" + tempSjdy.getSjlx() + "的所有对应数据！");
            //第一步，先删除当前进来数据所属供应商下当前的数据类型的所有数据
            GySjdy sjdy = new GySjdy();
            sjdy.setSjlx(tempSjdy.getSjlx());//指定数据类型
            sjdy.setGyShbh(tempSjdy.getGyShbh());//指定所属供应商
            sjdy.setSfsgwh(DATA_SFSGWH_N);
            ew.setEntity(sjdy);
            this.delete(ew);
            logger.info("未手工维护过的城市数据删除完成！");
            //第二步，提出掉手工维护过的，不做下一步的插入操作
            sjdy.setSfsgwh(DATA_SFSGWH_Y);
            ew.setEntity(sjdy);
            List<GySjdy> sgwhList = this.selectList(ew);
            logger.info("手工维护过数据{}条、本次从供应商那里得到需要入库数据{}条！",CollectionUtil.isEmpty(sgwhList)?"0":sgwhList.size(),sjdyList.size());
            List<GySjdy> needInsertList = rejectData(sjdyList, sgwhList);
            //第三步，将当前数据批量入库
            if (CollectionUtils.isNotEmpty(needInsertList)) {
                this.insertBatch(needInsertList);
            }
            logger.info("重新批量插入完成！");
        }
    }

    /**
     * 将手工维护过的数据从本次要入库数据中剔除掉
     *
     * @param dysjList 即将整体入库的数据
     * @param sgwhList 手工维护过的数据
     * @return 剔除掉手工维护过的数据
     */
    private List<GySjdy> rejectData(List<GySjdy> dysjList, List<GySjdy> sgwhList) {
        logger.info("接口给的城市数据:"+ JsonMapper.nonEmptyMapper().toJson(dysjList)+"\r\n手工维护过数据:"+JsonMapper.nonEmptyMapper().toJson(sgwhList));
        List<GySjdy> returnDysj = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(dysjList)) {
            for (GySjdy dY : dysjList) {
                boolean isNew = true;
                for (GySjdy sG : sgwhList) {
                    if (StringUtils.equals(dY.getGySjmc(), sG.getGySjmc()) && StringUtils.equals(dY.getGyShbh(), sG.getGyShbh())) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    returnDysj.add(dY);
                }
            }
        }
        logger.info("需要新入库的城市数据:"+ JsonMapper.nonEmptyMapper().toJson(returnDysj));
        return returnDysj;
    }


    /**
     * 添加数据对应
     *
     * @param dto 添加数据
     * @return 是否成功
     */
    public Boolean addDataMapping(List<DataMappingDTO> dto) {
        List<GySjdy> gySjdy = BeanMapper.mapList(dto, DataMappingDTO.class, GySjdy.class);
        // GySjdy gySjdy = BeanMapper.map(dto, GySjdy.class);
        return super.insertBatch(gySjdy);
    }


    /**
     * 编辑数据对应
     *
     * @param dto 编辑数据
     * @return 是否成功
     */
    public Boolean editDataMapping(DataMappingDTO dto) {
        GySjdy gySjdy = BeanMapper.map(dto, GySjdy.class);
        return super.updateById(gySjdy);
    }


    /**
     * 查询数据对应
     *
     * @param dto 查询条件
     * @return list集合
     */
    public Page<DataMappingVO> searchPage(PageDTO<DataMappingSearchDTO> dto) {
        Page page = dto.genPage();
        EntityWrapper ew = new EntityWrapper();
        if (StringUtils.isNotBlank(dto.getData().getSjlx())) {
            ew.eq("sjlx", dto.getData().getSjlx());
        }
        if (StringUtils.isNotBlank(dto.getData().getGyShbh())) {
            ew.eq("gy_shbh", dto.getData().getGyShbh());
        }
        if (StringUtils.isNotBlank(dto.getData().getGySjmc())) {
            ew.like("gy_sjmc", dto.getData().getGySjmc());
        }
        if(StringUtils.isNotBlank(dto.getData().getGySjbh())){
            ew.eq("gy_sjbh",dto.getData().getGySjbh());
        }
        String sfdy = dto.getData().getSfdy();
        if(StringUtils.isNotBlank(sfdy)){
            if(StringUtils.equals("2", sfdy)) {
                ew.isNull("sjbh");
                ew.isNull("sjmc");
            }
            if(StringUtils.equals("1", sfdy)){
                ew.isNotNull("sjbh");
                ew.isNotNull("sjmc");
            }
        }
        Page<GySjdy> gySjdyPage = super.selectPage(page, ew);
        Page<DataMappingVO> resultPage = PageCopyUtil.copy(gySjdyPage, GySjdy.class, DataMappingVO.class);
        return resultPage;
    }


    /**
     * 模糊查询T3数据对应
     *
     * @param dto 查询条件
     * @return list集合
     */
    public List<GySjdy> searchList(GySjdy dto) {
        EntityWrapper ew = new EntityWrapper();
        ew.eq("sjmc",dto.getSjmc());
        ew.eq("sjlx",4);
        List<GySjdy> list = super.selectList(ew);
        if(CollectionUtils.isEmpty(list)){
            EntityWrapper entityWrapper = new EntityWrapper();
            ew.eq("sjlx",4);
            if (StringUtils.isNotBlank(dto.getSjmc())) {
                if(dto.getSjmc().indexOf("市")>0){
                    entityWrapper.eq("sjmc", dto.getSjmc().substring(0,dto.getSjmc().indexOf("市")));
                }else if(dto.getSjmc().indexOf("州")>0){
                    entityWrapper.eq("sjmc", dto.getSjmc().substring(0,dto.getSjmc().indexOf("州")));
                }else {
                    entityWrapper.eq("sjmc",dto.getSjmc());
                }
            }
            if(StringUtils.isNotBlank(dto.getSjlx())){
                entityWrapper.eq("sjlx", dto.getSjlx());
            }
            if(StringUtils.isNotBlank(dto.getSjbh())){
                entityWrapper.eq("sjbh", dto.getSjbh());
            }
            list = super.selectList(entityWrapper);
        }
        return list;
    }


    /**
     * 删除数据对应
     *
     * @param id 主键
     * @return 是否成功
     */
    public Boolean deleteDataMapping(String id) {
        return super.deleteById(id);
    }

    /**
     * 编辑是查询 数据
     *
     * @param id 主键
     * @return vo
     */
    public DataMappingVO selectDataMapping(String id) {
        GySjdy gySjdy = super.selectById(id);
        if (null != gySjdy) {
            DataMappingVO vo = BeanMapper.map(gySjdy, DataMappingVO.class);
            return vo;
        }
        return null;
    }


    /**
     * 查询平台名称集合
     *
     * @param corpIds  查询条件
     * @return list集合
     */
    public List<GySjdy> selectPtmcList(List<String> corpIds) {
        return baseMapper.selectPtmcList(corpIds);
    }

    /**
     * 数据对应时限 缓存10分钟
     * @param cityId 城市id
     * @param gyShbh 供应商户编号
     * @return 数据对应时限
     */
    public Integer selectCancelLimitByGysbhAndCityId(String cityId, String gyShbh) {
        Integer count = (Integer)redisCacheManage.get("usecar.select.cancel.limit", cityId +"_"+gyShbh);
        if(count!=null){
            return count;
        }

        EntityWrapper ew = new EntityWrapper();
        ew.and("sjbh={0} and gy_shbh={1}", cityId, gyShbh);
        GySjdy gySjdy = super.selectOne(ew);
        count = (gySjdy == null || StringUtils.isBlank(gySjdy.getCancelLimit())) ? null : Integer.valueOf(gySjdy.getCancelLimit());
        redisCacheManage.put("usecar.select.cancel.limit", cityId +"_"+gyShbh,count,600);
        return count;
    }

    /**
     * 修改取消时限
     * @param dto 入参
     * @return 是否成功
     */
    public boolean updateCancelLimit(UpdateCancelLimitDTO dto) {
        if (dto.getCancelLimit() == null || CollectionUtils.isEmpty(dto.getIds())) {
            return false;
        }
        return this.baseMapper.updateCancelLimit(dto);
    }



    /**
     * 删除数据对应
     *
     * @param gySjdy 主键
     * @return 是否成功
     */
    public Boolean deleteEntity(GySjdy gySjdy) {
        if (null == gySjdy) {
            return false;
        }
        EntityWrapper<GySjdy> ew = new EntityWrapper<>();
        ew.setEntity(gySjdy);
        return super.delete(ew);
    }

    /**
     * 批量添加
     *
     * @param sjdyList 添加数据
     * @return 是否成功
     */
    public Boolean insertSjdyBatch(List<GySjdy> sjdyList) {
        return super.insertBatch(sjdyList,100);
    }

}


 