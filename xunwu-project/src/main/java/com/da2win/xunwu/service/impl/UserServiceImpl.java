package com.da2win.xunwu.service.impl;

import com.da2win.xunwu.entity.Role;
import com.da2win.xunwu.entity.User;
import com.da2win.xunwu.repository.RoleRepository;
import com.da2win.xunwu.repository.UserRepository;
import com.da2win.xunwu.service.IUserService;
import com.da2win.xunwu.service.ServiceResult;
import com.da2win.xunwu.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Darwin
 * @date 2018/7/2
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public User findUserByName(String userName) {
        User user = userRepository.findByName(userName);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        User user = userRepository.findOne(userId);
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }
}
