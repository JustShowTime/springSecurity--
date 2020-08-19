package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author   czq
 * @Date 2020-08-17 15:38:22    
 */
@RestController
@RequestMapping(value = "/user/api")
public class UserController {


	@RequestMapping(method = RequestMethod.GET,value = "hello")
	public String hello() {
		return "hello,user";
	}
}
