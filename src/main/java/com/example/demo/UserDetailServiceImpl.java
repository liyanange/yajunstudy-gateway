package com.example.demo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("ss")
public class UserDetailServiceImpl {
    //@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       User user = new User();
       //这个验证密码在DaoAuthenticationProvider 38行自动验的
       user.setUsername("liyanan");
       user.setPassword("123456");
        if(!username.equals(user.getUsername())){
            throw  new UsernameNotFoundException("账户不存在");
        }
        return  user;

    }

}
