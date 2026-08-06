package cn.vetech.center.usecar.finance.service;

import cn.vetech.center.finance.report.dto.FetchGyzdDTO;
import cn.vetech.center.finance.report.vo.FetchGyzdVO;
import cn.vetech.center.usecar.entity.order.YcDd;
import cn.vetech.center.usecar.mapper.order.YcDdMapper;
import cn.vetech.center.usecar.service.UsecarCacheBaseServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.vetech.core.api.RestResponse;
import org.vetech.core.base.PageDTO;

import java.util.List;

/**
 * 用车业务
 *
 * @author : yanguowei
 */
@Service
public class UsecarFinanceGyZdCallbackService extends UsecarCacheBaseServiceImpl<YcDdMapper, YcDd> {

    /**
     * CPS结算对账业务线提供CPS供应数据服务
     *
     * @param dto 入参的值
     * @return 分页数据
     */
    public RestResponse<Page<FetchGyzdVO>> supplierBill(PageDTO<FetchGyzdDTO> dto) {
        logger.info("supplierBill req:{}", dto.toString());
        Page<FetchGyzdVO> page = new Page(dto.getCurrent(), dto.getSize(), dto.getOrderByField());
        page.setAsc(dto.isAsc());
        FetchGyzdDTO searchDTO = dto.getData();
        RestResponse resp = new RestResponse();
        if (!checkFetchGyzdDTO(searchDTO)) {
            resp.setMessage("请求参数不能为空！");
            return resp;
        }
        List<FetchGyzdVO> list = baseMapper.selectSupplierBillPage(page, searchDTO);
        page.setRecords(list);
        resp.setResult(page);
        return resp;
    }

    /**
     * 核对入参
     *
     * @param searchDTO 入参的值
     * @return 是否正确
     */
    private boolean checkFetchGyzdDTO(FetchGyzdDTO searchDTO) {
        return searchDTO != null && CollectionUtils.isNotEmpty(searchDTO.getShbhList()) && searchDTO.getYwkssj() != null && searchDTO.getYwjssj() != null;
    }
}
