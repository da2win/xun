package com.da2win.xunwu.service;

import com.da2win.xunwu.entity.User;

/**
 *
 * @author Darwin
 * @date 2018/7/2
 */
public interface IUserService {
    User findUserByName(String userName);
}
