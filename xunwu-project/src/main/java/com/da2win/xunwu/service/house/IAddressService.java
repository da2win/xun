package com.da2win.xunwu.service.house;

import com.da2win.xunwu.service.ServiceMultiResult;

/**
 *
 * @author Darwin
 * @date 2018/7/3
 */
public interface IAddressService {
    ServiceMultiResult<SupportAddressDTO> findAllCities();
}
