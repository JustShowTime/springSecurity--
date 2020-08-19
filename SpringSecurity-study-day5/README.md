# springSecurity-study
## 1.pom.xml引入jar包
```java
        <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jdbc</artifactId>
		</dependency>
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
		</dependency>
		<dependency>
            <groupId>com.github.penggle</groupId>
            <artifactId>kaptcha</artifactId>
            <version>2.3.2</version>
        </dependency>
```
## 2.自定义验证码校验失败异常，继承AuthenticationException
```java
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
```
## 3.自定义身份验证失败处理逻辑，实现AuthenticationFailureHandler接口
```java
package com.example.demo.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * 自定义身份验证失败处理逻辑，实现AuthenticationFailureHandler类
 * @author   czq
 * @Date 2020-08-18 17:40:43    
 */
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler{

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		// TODO Auto-generated method stub
		System.out.println("身份验证失败");
	}

}
```
## 4.自定义过滤器（针对验证码，并且把captcha生成图片逻辑也贴下来），继承OncePerRequestFilter，也可以平行写好多过滤器
```java
@Bean
	public Producer captcha() {
		Properties properties=new Properties();
		properties.setProperty("kaptcha.image.width", "150");
		properties.setProperty("kaptcha.image.height", "50");
		properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
		properties.setProperty("kaptcha.textproducer.char.length", "4");
		
		Config config=new Config(properties);
		DefaultKaptcha defaultKaptcha=new DefaultKaptcha();
		defaultKaptcha.setConfig(config);
		return defaultKaptcha;
	}
	
package com.example.demo.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.code.kaptcha.Producer;

/**
 * @author   czq
 * @Date 2020-08-17 15:38:22    
 */
@Controller
public class CaptchaController {

	@Autowired
	private Producer captchaProducer;
	@RequestMapping(method = RequestMethod.GET,value = "/captcha.jpg")
	public void getCaptcha(HttpServletRequest request,HttpServletResponse response) throws IOException{
		response.setContentType("image/jpg");
		String capText=captchaProducer.createText();
		request.getSession().setAttribute("captcha", capText);
		BufferedImage bi=captchaProducer.createImage(capText);
		ServletOutputStream out=response.getOutputStream();
		ImageIO.write(bi, "jpg", out);
		try {
			out.flush();
		}finally {
			out.close();
		}
	}
}


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

```
## 5.自定义用户权限校验逻辑MyUserDetailsService，实现UserDetailsService接口
```java
ppackage com.example.demo.service;

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
```
## 6.@EnableWebSecurity，自定义WebSecurityConfig，继承WebSecurityConfigurerAdapter，进行安全认证
```java
package com.example.demo.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.filter.MyAuthenticationFailureHandler;
import com.example.demo.filter.VerificationCodeFilter;
import com.example.demo.service.MyUserDetailsService;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

/**
 * @author   czq
 * @Date 2020-08-17 15:50:57    
 */
/**
 * 步骤:2
 * @EnableWebSecurity已经集成@config
 * EnableWebSecurity注解有两个作用,
 * 1: 加载了WebSecurityConfiguration配置类, 配置安全认证策略。
 * 2: 加载了AuthenticationConfiguration, 配置了认证信息
 * 
 * @author czq
 *
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private MyUserDetailsService myUserDetailsService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.authorizeRequests()
		.antMatchers("/admin/api/**").hasRole("ADMIN")
		.antMatchers("/user/api/**").hasRole("USER")
		.antMatchers("/app/api/**","/captcha.jpg").permitAll()
		.anyRequest().authenticated()
		.and()
		.csrf().disable()
		.formLogin().loginPage("/myLogin.html")
		/*
		 * loginProcessingUrl: 登录请求拦截的url,也就是form表单提交时指定的action
		 * 实际作用：查资料无果，我觉得仅仅是标识登录页面,方便下一步逻辑判断
		 */
		.loginProcessingUrl("/auth/form").permitAll()
		.failureHandler(new MyAuthenticationFailureHandler());
		
		http.addFilterBefore(new VerificationCodeFilter(), UsernamePasswordAuthenticationFilter.class);
		
		
	}
	
	 @Override
	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        //引入自定义的编码
	        authProvider.setPasswordEncoder(passwordEncoder());
	        //引入自定义的类
	        authProvider.setUserDetailsService(myUserDetailsService);

	        auth.authenticationProvider(authProvider);

	    }
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
	
	@Bean
	public Producer captcha() {
		Properties properties=new Properties();
		properties.setProperty("kaptcha.image.width", "150");
		properties.setProperty("kaptcha.image.height", "50");
		properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
		properties.setProperty("kaptcha.textproducer.char.length", "4");
		
		Config config=new Config(properties);
		DefaultKaptcha defaultKaptcha=new DefaultKaptcha();
		defaultKaptcha.setConfig(config);
		return defaultKaptcha;
	}

}
```
## 7.application.properties，增加数据库连接
```
spring.datasource.url=jdbc:mysql://192.168.11.10:3306/test?characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&rewriteBatchedStatements=true
spring.datasource.username=mms
spring.datasource.password=qwe123
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=1
```
## 8.插入数据库，用的默认数据库
```
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `user_self`
-- ----------------------------
DROP TABLE IF EXISTS `user_self`;
CREATE TABLE `user_self` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(60) DEFAULT NULL,
  `enable` tinyint(4) DEFAULT '1',
  `roles` text COMMENT '用户角色，多个角色之间用都好隔开',
  PRIMARY KEY (`id`),
  KEY `username` (`username`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_self
-- ----------------------------
INSERT INTO `user_self` VALUES ('1', 'admin', '123', '1', 'ROLE_ADMIN,ROLE_USER');
INSERT INTO `user_self` VALUES ('2', 'user', '123', '1', 'ROLE_USER');
```
## 9.登录跳转成功后，会访问/路径
```java
@RestController
@RequestMapping(value = "/admin/api")
public class AdminController {


	@RequestMapping(method = RequestMethod.GET,value = "hello")
	public String hello() {
		return "hello,admin";
	}
}
@RestController
@RequestMapping(value = "/app/api")
public class AppController {


	@RequestMapping(method = RequestMethod.GET,value = "hello")
	public String hello() {
		return "hello,app";
	}
}
@RestController
@RequestMapping(value = "/user/api")
public class UserController {


	@RequestMapping(method = RequestMethod.GET,value = "hello")
	public String hello() {
		return "hello,user";
	}
}
```
有权限的情况下，才能访问
## 10.以下是demo
访问：http://localhost:8080/app/api/hello，直接跳转，因为所有权限都能访问
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step1.png)
访问：http://localhost:8080/admin/api/hello，到登陆页面，只有访问这个路径的用户才能访问
用user用户登录
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day5-step2.png)
成功登录，但是跳转失败，无权限访问
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step3.png)
用admin用户登录
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day5-step1.png)
admin用户登录失败（可能验证码，可能账号密码错误）
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day5-step4.png)
成功登录，并返回hello,admin
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step5.png)
