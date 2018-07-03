package com.da2win.xunwu.service.house.impl;

import com.da2win.xunwu.entity.SupportAddress;
import com.da2win.xunwu.repository.SupportAddressRepository;
import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.service.house.IAddressService;
import com.da2win.xunwu.service.house.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Darwin
 * @date 2018/7/3
 */
@Service
public class AddressServiceImpl implements IAddressService {
    @Autowired
    private SupportAddressRepository supportAddressRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        List<SupportAddress> addresses = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
        List<SupportAddressDTO> addressDTOS = new ArrayList<>();
        for (SupportAddress address : addresses) {
            SupportAddressDTO target = modelMapper.map(address, SupportAddressDTO.class);
            addressDTOS.add(target);
        }
        return new ServiceMultiResult<>(addressDTOS.size(), addressDTOS);
    }
}
