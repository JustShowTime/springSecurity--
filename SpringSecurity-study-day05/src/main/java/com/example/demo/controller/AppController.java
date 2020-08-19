package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author   czq
 * @Date 2020-08-17 15:38:22    
 */
@RestController
@RequestMapping(value = "/app/api")
public class AppController {


	@RequestMapping(method = RequestMethod.GET,value = "hello")
	public String hello() {
		return "hello,app";
	}
}
