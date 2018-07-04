package com.da2win.xunwu.service.house;

import com.da2win.xunwu.service.ServiceResult;
import com.da2win.xunwu.web.dto.HouseDTO;
import com.da2win.xunwu.web.form.HouseForm;

/**
 * 房屋管理接口
 *
 * @author Darwin
 * @date 2018/7/4
 */
public interface IHouseService {

    ServiceResult<HouseDTO> save(HouseForm houseForm);
}
