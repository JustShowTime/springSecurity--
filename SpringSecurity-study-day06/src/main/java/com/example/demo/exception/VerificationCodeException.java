package com.example.demo.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 自定义验证码校验失败异常
 * @author   czq
 * @Date 2020-08-18 17:18:50    
 */
public class VerificationCodeException extends AuthenticationException{

	public VerificationCodeException() {
		super("图像验证码校验失败");
	}

}
