package com.da2win.xunwu.web.controller.house;

import com.da2win.xunwu.base.ApiResponse;
import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.service.house.IAddressService;
import com.da2win.xunwu.service.house.SupportAddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Darwin
 * @date 2018/7/3
 */
@Controller
public class HouseController {
    @Autowired
    private IAddressService addressService;

    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities() {
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
        if (result.getResultSize() == 0) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }


}
