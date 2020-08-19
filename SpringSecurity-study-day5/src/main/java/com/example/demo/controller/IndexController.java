package com.example.demo.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author   czq
 * @Date 2020-08-17 15:38:22    
 */
@RestController
public class IndexController {
	
	@RequestMapping(method = RequestMethod.GET,value = "/")
	public String hello(HttpServletRequest request,HttpServletResponse response) throws IOException{
		return "hello SpringSecurity";
	}
}
