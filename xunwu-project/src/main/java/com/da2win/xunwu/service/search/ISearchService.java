package com.da2win.xunwu.service.search;

import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.web.form.RentSearch;

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

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

}
