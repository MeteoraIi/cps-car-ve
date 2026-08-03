package cn.vetech.center.usecar.book.buyer.specicar.vo;

import org.vetech.core.modules.utils.mapper.JsonMapper;

import java.util.List;

/**
 * 专快车采购产品查询公共VO（CPS和ASMS公用，修改时请谨慎）
 * @author houshuang
 * @since 2017-11-06
 */
public class BookSpeciCarCommonVO {

    /**
     * 距离
     */
    private String distance;
    /**
     * 时长
     */
    private String duration;

    /**
     * link返回的结果
     */
    private String linkResult;

    private String cacheTimeout;


    public String getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(String cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public String getLinkResult() {
        return linkResult;
    }

    public void setLinkResult(String linkResult) {
        this.linkResult = linkResult;
    }

    /**
     * CPS专快车产品查询集合
     */
    private List<BookSpeciCarProductVO> bookSpeciCarProductVOs;

    /**
     * ASMS专快车产品查询集合
     */
    private List<BookAsmsSpecialCar> bookAsmsSpecialCars;


    /**
     * 游标Id，异步加载查询时返回
     */
    private String cursorId;

    /**
     *  默认查询完成,异步加载查询时返回
     */
    private Boolean finishFlag;

    public String getCursorId() {
        return cursorId;
    }

    public void setCursorId(String cursorId) {
        this.cursorId = cursorId;
    }

    public Boolean getFinishFlag() {
        return finishFlag;
    }

    public void setFinishFlag(Boolean finishFlag) {
        this.finishFlag = finishFlag;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<BookSpeciCarProductVO> getBookSpeciCarProductVOs() {
        return bookSpeciCarProductVOs;
    }

    public void setBookSpeciCarProductVOs(List<BookSpeciCarProductVO> bookSpeciCarProductVOs) {
        this.bookSpeciCarProductVOs = bookSpeciCarProductVOs;
    }

    public List<BookAsmsSpecialCar> getBookAsmsSpecialCars() {
        return bookAsmsSpecialCars;
    }

    public void setBookAsmsSpecialCars(List<BookAsmsSpecialCar> bookAsmsSpecialCars) {
        this.bookAsmsSpecialCars = bookAsmsSpecialCars;
    }

    @Override
    public String toString() {
        return JsonMapper.nonEmptyMapper().toJson(this);
    }
}