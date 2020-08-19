package com.example.demo.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.demo.entity.UserSelf;

/**
 * @author   czq
 * @Date 2020-08-18 15:07:29    
 */
public interface UserSelfDao {

	@Select("select * from user_self Where username=#{username}")
	UserSelf findByUserName(@Param("username") String username);
}
