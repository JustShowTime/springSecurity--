package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.example.demo.dao.UserSelfDao;
import com.example.demo.entity.UserSelf;

/**
 * @author   czq
 * @Date 2020-08-18 15:12:15    
 */
@Service
public class MyUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserSelfDao userSelfDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		

		UserSelf userSelf=userSelfDao.findByUserName(username);
		Assert.isTrue(userSelf!=null, "用户不存在");
		
		userSelf.setAuthrities(AuthorityUtils.commaSeparatedStringToAuthorityList(userSelf.getRoles()));
		userSelf.setAuthrities(generateAuthorities(userSelf.getRoles()));
		return userSelf;
	}
	
	private List<GrantedAuthority> generateAuthorities(String roles){
		List<GrantedAuthority> authorities=new ArrayList<GrantedAuthority>();
		String[] roleArray=roles.split(",");
		if(roles!=null&&!"".equals(roles)) {
			for(String role:roleArray) {
				authorities.add(new SimpleGrantedAuthority(role));
			}
		}
		return authorities;
	}

	
}
