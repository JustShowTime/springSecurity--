package com.example.demo.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.exception.VerificationCodeException;

/**
 * 专门用于校验验证码的过滤器
 * @author   czq
 * @Date 2020-08-18 17:20:59    
 */
public class VerificationCodeFilter extends OncePerRequestFilter{

	//引入自定义校验失败类，仅用于身份校验失败处理
	private AuthenticationFailureHandler authenticationFailureHandler=new MyAuthenticationFailureHandler();
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		/*
		 * 通过loginProcessingUrl:/auth/form，识别是否为登陆页面发过来的请求
		 * 如果是:需要校验验证码
		 * 如果不是:跳过验证码校验，但仍要做其他普通校验
		 */
		if(!"/auth/form".equals(request.getRequestURI())) {
			filterChain.doFilter(request, response);
		}else {
			try {
				verificationCode(request);
				filterChain.doFilter(request, response);
			}catch (VerificationCodeException e) {
				//身份校验失败，自定义校验失败类authenticationFailureHandler接住异常进行处理
				authenticationFailureHandler.onAuthenticationFailure(request, response, e);
			}
		}
	}
	
	public void verificationCode(HttpServletRequest request) throws VerificationCodeException{
		String requestCode=request.getParameter("captcha");
		HttpSession session=request.getSession();
		String savedCode=(String) session.getAttribute("captcha");
		//无论成功失败，清除验证码，放便下次刷新时，更新验证码
		if(!StringUtils.isEmpty(savedCode)) {		
			session.removeAttribute("captcha");
		}
		//身份校验失败，抛出异常VerificationCodeException，这样方便authenticationFailureHandler接住异常进行相应处理
		if(StringUtils.isEmpty(requestCode)||StringUtils.isEmpty(savedCode)||!requestCode.equals(savedCode)) {
			throw new VerificationCodeException();
		}
	}

}
