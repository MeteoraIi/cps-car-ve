package cn.vetech.center.usecar.service.unpay;

import cn.vetech.center.base.api.vo.VeUserVO;
import cn.vetech.center.customer.api.vo.ShShbVO;
import cn.vetech.center.usecar.apiclient.customer.IShShbServiceClient;
import cn.vetech.center.usecar.cache.CarBaseDataCacheService;
import cn.vetech.center.usecar.common.code.UsecarOrderCode;
import cn.vetech.center.usecar.common.util.RestResponseUtil;
import cn.vetech.center.usecar.entity.usecar.YcUnpayLimit;
import cn.vetech.center.usecar.service.orderes.YcDdEsV2Service;
import cn.vetech.center.usecar.service.unpay.dto.UnpayLimitDTO;
import cn.vetech.center.usecar.service.unpay.dto.UnpayLimitDeleteDTO;
import cn.vetech.center.usecar.service.unpay.dto.YcUnpayLimitXlsDTO;
import cn.vetech.center.usecar.service.usecar.YcUnpayLimitService;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.plugins.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.vetech.core.api.RestResponse;
import org.vetech.core.base.PageDTO;
import org.vetech.core.modules.utils.mapper.BeanMapper;
import org.vetech.core.modules.utils.sequence.IdGenerator;
import org.vetech.core.modules.utils.time.VeDate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.vetech.center.usecar.common.UseCarConstant.YES;
import static cn.vetech.center.usecar.common.code.UsecarOrderCode.UCAR_30221;
import static cn.vetech.center.usecar.common.code.UsecarOrderCode.UCAR_30222;

/**
 * 未支付限制用户使用设置
 *
 * @author : Y
 * @since 2024/8/5 14:42
 */
@Slf4j
@Service
public class YcUnpayLimitLogicService {
    /**
     * 未付限制范围
    */
    @Autowired
    private YcUnpayLimitService ycUnpayLimitService;
    /**
     * 调用CUSTOMER接口对象
     */
    @Autowired
    private IShShbServiceClient iShShbServiceClient;
    /**
     * es服务
     */
    @Autowired
    private YcDdEsV2Service ycDdEsV2Service;

    /**
     * 新增或者修改
     *
     * @param dto       入参
     * @param loginUser 登录用户
     * @return 回参
     */
    public RestResponse<String> addOrUpdate(UnpayLimitDTO dto, VeUserVO loginUser) {
        if (dto == null) {
            return RestResponseUtil.error("入参为空！");
        }
        YcUnpayLimit ycUnpayLimit = BeanMapper.map(dto, YcUnpayLimit.class);
        ycUnpayLimit.setUpdateUser(loginUser.getBh());
        ycUnpayLimit.setUpdateTime(VeDate.getNow());
        ycUnpayLimit.setUpdateUserId(loginUser.getBh());
        if (StringUtils.isBlank(dto.getId())) {
            ycUnpayLimit.setId(String.valueOf(IdGenerator.getId()));
        }
        Boolean successStatus = ycUnpayLimitService.insertUpdate(ycUnpayLimit);
        return new RestResponse<>(successStatus.toString());
    }

    /**
     * 批量删除
     *
     * @param dto 入参
     * @return 回参
     */
    public RestResponse<String> deleteCarBillTemplateById(UnpayLimitDeleteDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIdList())) {
            return RestResponseUtil.error("入参为空！");
        }
        Boolean deleteStatus = ycUnpayLimitService.batchDelete(dto.getIdList());
        return new RestResponse<>(deleteStatus.toString());
    }

    /**
     * 分页查询
     *
     * @param pageDTO 分页入参
     * @return 回参
     */
    public RestResponse<Page<YcUnpayLimit>> list(PageDTO<UnpayLimitDTO> pageDTO) {
        RestResponse<Page<YcUnpayLimit>> response = new RestResponse<>();
        Page<YcUnpayLimit> page = ycUnpayLimitService.list(pageDTO);
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            List<String> customerList = page.getRecords().stream().map(YcUnpayLimit::getCustomer).distinct().collect(Collectors.toList());
            RestResponse<List<ShShbVO>> shShbVOsRest = iShShbServiceClient.getShbByIds(customerList.toArray(new String[customerList.size()]));
            if (shShbVOsRest != null && !CollectionUtils.isEmpty(shShbVOsRest.getResult())) {
                Map<String, ShShbVO> shbVOMap = shShbVOsRest.getResult().stream().collect(Collectors.toMap(ShShbVO::getShbh, Function.identity(), (k1, k2) -> k1));
                for (YcUnpayLimit unpayLimit : page.getRecords()) {
                    if (shbVOMap.containsKey(unpayLimit.getCustomer())) {
                        unpayLimit.setCustomerName(shbVOMap.get(unpayLimit.getCustomer()).getMc());
                    }
                }
            }
        }
        response.setResult(page);
        return response;
    }

    /**
     * 批量导入
     *
     * @param file      文件
     * @param loginUser 登录用户
     * @return 回参
     */
    public RestResponse<String> batchImport(MultipartFile file, VeUserVO loginUser) {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            log.error("读取流异常", e);
        }
        YcUnpayLimitXlsListener jdGoodsBillListener = new YcUnpayLimitXlsListener();
        EasyExcel.read(inputStream, YcUnpayLimitXlsDTO.class, jdGoodsBillListener).sheet().doRead();
        List<YcUnpayLimitXlsDTO> dataList = jdGoodsBillListener.getCachedDataList();
        List<YcUnpayLimit> ycUnpayLimits = BeanMapper.mapList(dataList, YcUnpayLimitXlsDTO.class, YcUnpayLimit.class);
        for (YcUnpayLimit ycUnpayLimit : ycUnpayLimits) {
            ycUnpayLimit.setUpdateUser(loginUser.getBh());
            ycUnpayLimit.setUpdateTime(VeDate.getNow());
            ycUnpayLimit.setUpdateUserId(loginUser.getBh());
            ycUnpayLimit.setId(String.valueOf(IdGenerator.getId()));
        }
        Boolean success = ycUnpayLimitService.batchInsert(ycUnpayLimits);
        return new RestResponse<>(success.toString());
    }

    /**
     * 未付检查
     *
     * @param customerNo 商户编号
     * @param phone      电话号码
     * @return 错误码null 标识可以继续下单，不能继续下单返回错误原因代码
     */
    public UsecarOrderCode checkUnpayOrder(String customerNo, String phone) {
        if (StringUtils.isEmpty(customerNo) || StringUtils.isBlank(phone)) {
            log.info("检查未付入参为空");
            return null;
        }
        YcUnpayLimit ycUnpayLimit = ycUnpayLimitService.selectByCustomer(customerNo);
        if (ycUnpayLimit == null || !StringUtils.equals(ycUnpayLimit.getOpen(), YES)) {
            return null;
        }
        if (ycUnpayLimit.getEnterpriseLimitNum() == null || ycUnpayLimit.getEnterpriseLimitNum() == 0) {
            return null;
        }
        if (ycUnpayLimit.getTimeCycle() == null || ycUnpayLimit.getTimeCycle() == 0) {
            return null;
        }
        String date = VeDate.getPreDay(VeDate.getStringDateShort(), -ycUnpayLimit.getTimeCycle());
        int enterpriseUnpayNum = ycDdEsV2Service.searchEnterpriseUnpayNum(customerNo, date);
        if (enterpriseUnpayNum >= ycUnpayLimit.getEnterpriseLimitNum()) {
            return UCAR_30221;
        }
        if (!StringUtils.equals(ycUnpayLimit.getPersonLimitOpen(), YES)) {
            return null;
        }
        int phoneUnpayNum = ycDdEsV2Service.searchPersonUnpayNum(phone, date);
        if (phoneUnpayNum >= ycUnpayLimit.getPersonLimitNum()) {
            return UCAR_30222;
        }
        return null;
    }
}