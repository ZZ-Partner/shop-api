package com.gexingw.shop.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {

    UserDetails getUserDetailByUserName(String username);

    UserDetails loadUserByUsername(String username);
}
