package com.da2win.xunwu.entity;

import com.da2win.xunwu.ApplicationTests;
import com.da2win.xunwu.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Darwin on 2018/6/29.
 */
public class UserRepositoryTest extends ApplicationTests{
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindOne() throws Exception {
        User user = userRepository.findOne(1L);
        Assert.assertEquals("wali", user.getName());

    }
}
