package com.da2win.xunwu.web.controller.admin;

import com.da2win.xunwu.base.ApiDataTableResponse;
import com.da2win.xunwu.base.ApiResponse;
import com.da2win.xunwu.entity.SupportAddress;
import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.service.ServiceResult;
import com.da2win.xunwu.service.house.IAddressService;
import com.da2win.xunwu.service.house.IHouseService;
import com.da2win.xunwu.service.house.IQiNiuService;
import com.da2win.xunwu.web.dto.HouseDTO;
import com.da2win.xunwu.web.dto.QiNiuPutRet;
import com.da2win.xunwu.web.dto.SupportAddressDTO;
import com.da2win.xunwu.web.form.DataTableSearch;
import com.da2win.xunwu.web.form.HouseForm;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Darwin
 * @date 2018/7/2
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private IQiNiuService qiNiuService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IHouseService houseService;
    @Autowired
    private Gson gson;

    @GetMapping("/center")
    public String adminCenterPage() {
        return "admin/center";
    }

    @GetMapping("/welcome")
    public String welcomePage() {
        return "admin/welcome";
    }

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @GetMapping("/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    @PostMapping(value = "/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        String filename = file.getOriginalFilename();
        try {
            InputStream inputStream = file.getInputStream();
            Response response = qiNiuService.uploadFile(inputStream);
            if (response.isOK()) {
                QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            } else {
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }
        } catch (QiniuException e) {
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }

    @PostMapping("/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }
        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (addressMap.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getResult());
        }
        return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
    }

    @PostMapping("/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DataTableSearch searchBody) {
        ServiceMultiResult<HouseDTO> result = houseService.adminQuery(searchBody);
        ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
        response.setData(result.getResult());
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        response.setDraw(searchBody.getDraw());
        return response;
    }
}
