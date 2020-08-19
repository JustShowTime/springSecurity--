package com.example.demo.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.StringUtils;

/**
 * @author   czq
 * @Date 2020-08-19 11:48:33    
 */
public class MyWebAuthenticationDetails extends WebAuthenticationDetails{

	private String imageCode;
	
	private String savedImageCode;
	
	
	public String getImageCode() {
		return imageCode;
	}


	public void setImageCode(String imageCode) {
		this.imageCode = imageCode;
	}


	public String getSavedImageCode() {
		return savedImageCode;
	}


	public void setSavedImageCode(String savedImageCode) {
		this.savedImageCode = savedImageCode;
	}


	public MyWebAuthenticationDetails(HttpServletRequest request) {
		super(request);
		this.imageCode=request.getParameter("captcha");
		HttpSession session=request.getSession();
		this.savedImageCode=(String)session.getAttribute("captcha");
		if(!StringUtils.isEmpty(this.imageCode)) {
			session.removeAttribute("captcha");
		}
	}

}
