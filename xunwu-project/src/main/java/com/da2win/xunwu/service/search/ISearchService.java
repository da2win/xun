package com.da2win.xunwu.service.search;

/**
 *
 * @author Darwin
 * @date 2018/7/10
 */
public interface ISearchService {
    /**
     * 索引目标房源
     * @param houseId
     */
    void index(Long houseId);

    /**
     * 移除房源
     * @param houseId
     */
    void remove(Long houseId);
}
