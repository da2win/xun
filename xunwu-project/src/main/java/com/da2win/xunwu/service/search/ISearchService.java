package com.da2win.xunwu.service.search;

import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.service.ServiceResult;
import com.da2win.xunwu.web.form.RentSearch;

import java.util.List;

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

    /**
     * 获取补全建议关键字
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合特定小区的房间数
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);
}
