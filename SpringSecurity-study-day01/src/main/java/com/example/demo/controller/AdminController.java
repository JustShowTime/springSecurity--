package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author czq
 * @Date 2020-08-17 15:38:22
 */
@RestController
public class AdminController {

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public String hello() {
		return "hello SpringSecurity";
	}
}
