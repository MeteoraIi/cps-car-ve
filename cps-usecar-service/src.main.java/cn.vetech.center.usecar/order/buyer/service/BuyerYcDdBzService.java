package cn.vetech.center.usecar.order.buyer.service;

import cn.vetech.center.usecar.common.UseCarConstant;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.entity.order.YcDdBz;
import cn.vetech.center.usecar.mapper.order.YcDdBzMapper;
import cn.vetech.center.usecar.order.buyer.dto.BuyerInsertNoteDTO;
import cn.vetech.center.usecar.order.buyer.vo.BuyerOrderNoteVO;
import cn.vetech.center.usecar.service.order.YcDdBzService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vetech.core.exception.SystemRuntimeException;
import org.vetech.core.modules.utils.collection.CollectionUtil;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.time.VeDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用车采购正常订单备注服务service
 *
 * @author yangxianglin
 * @since 2017-10-31
 */
@Service
public class BuyerYcDdBzService extends ServiceImpl<YcDdBzMapper, YcDdBz> {
    /**
     * 打印日志
     */
    private final Logger logger = LoggerFactory.getLogger(BuyerYcDdBzService.class);
    /**
     * 用车订单备注dao
     */
    @Autowired
    private YcDdBzService ycDdBzService;
    /**
     * 新增采购用车订单备注
     * @param dto  新增采购用车订单备注
     * @return d
     */
    public Boolean insertBuyerYcDdBcd(BuyerInsertNoteDTO dto) {
        //供应商户编号为空就要抛异常
        if(dto != null){
            if (StringUtils.isBlank(dto.getCgShbh())) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
            }

        }
        YcDdBz ycDdBz =new YcDdBz();
        ycDdBz.setId(VeDate.getNo(UseCarConstant.SEVEN)); //设置主键ID
        ycDdBz.setBznr(dto.getBznr()); //备注内容
        ycDdBz.setBzYhbh(dto.getBzYhbh()); //备注用户编号
        ycDdBz.setBzYhxm(dto.getBzYhxm()); //备注用户姓名
        ycDdBz.setBzDatetime(VeDate.getNow()); //备注时间
        ycDdBz.setBzlx(new BigDecimal(UseCarConstant.ZT_ONE)); //备注类型：1、采购 2、供应
        ycDdBz.setDdbh(dto.getDdbh()); //订单编号
        ycDdBz.setSjkly(dto.getSjkly()); //数据库路由
        ycDdBz.setShbh(dto.getCgShbh()); //商户编号
        return ycDdBzService.insertYcDdBcd(ycDdBz);
    }

    /**
     * 查询采购订单备注集合
     * @param dto  查询采购订单备注集合
     * @return List<BuyerOrderNoteVO>
     */
    public List<BuyerOrderNoteVO> selectYcDdBzList(BuyerInsertNoteDTO dto) {
        //采购商户编号为空就要抛异常
        if(dto != null){
            if (StringUtils.isBlank(dto.getCgShbh())) {
                throw new SystemRuntimeException(UsecarOrderCode.UCAR_10002);
            }

        }
        YcDdBz ycDdBz = new YcDdBz();
        ycDdBz.setDdbh(dto.getDdbh());
        ycDdBz.setShbh(dto.getCgShbh());
        List<YcDdBz> list= ycDdBzService.selectYcDdBzList(ycDdBz);
        List<BuyerOrderNoteVO> newList = new ArrayList<BuyerOrderNoteVO>();
        if(CollectionUtil.isNotEmpty(list)){
            for(YcDdBz ddBz:list){
                BuyerOrderNoteVO buyerOrderNoteVO = BeanMapper.map(ddBz, BuyerOrderNoteVO.class);
                newList.add(buyerOrderNoteVO);
            }
        }
        return newList;
    }
}