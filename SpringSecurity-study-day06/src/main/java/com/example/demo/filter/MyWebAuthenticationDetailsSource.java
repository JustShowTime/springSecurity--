package com.example.demo.filter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * @author   czq
 * @Date 2020-08-19 11:54:17    
 */
@Component
public class MyWebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest,WebAuthenticationDetails>{


	@Override
	public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
		// TODO Auto-generated method stub
		return new MyWebAuthenticationDetails(context);
	}

}
