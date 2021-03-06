package com.da2win.xunwu.service;

import com.da2win.xunwu.entity.User;
import com.da2win.xunwu.web.dto.UserDTO;

/**
 *
 * @author Darwin
 * @date 2018/7/2
 */
public interface IUserService {
    User findUserByName(String userName);

    ServiceResult<UserDTO> findById(Long userId);
}
