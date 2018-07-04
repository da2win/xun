package com.da2win.xunwu.service.house;

import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.service.ServiceResult;
import com.da2win.xunwu.web.dto.HouseDTO;
import com.da2win.xunwu.web.form.DataTableSearch;
import com.da2win.xunwu.web.form.HouseForm;
import com.da2win.xunwu.web.form.RentSearch;

/**
 * 房屋管理接口
 *
 * @author Darwin
 * @date 2018/7/4
 */
public interface IHouseService {

    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody);

    /**
     * 查询房源信息集
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);
}
