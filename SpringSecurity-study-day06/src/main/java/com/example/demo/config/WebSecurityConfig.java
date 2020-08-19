package com.example.demo.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.filter.MyAuthenticationFailureHandler;
import com.example.demo.filter.MyAuthenticationProvider;
import com.example.demo.filter.MyWebAuthenticationDetailsSource;
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
	private MyWebAuthenticationDetailsSource myWebAuthenticationDetailsSource;
	
	@Autowired
	private MyAuthenticationProvider myAuthenticationProvider;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.authorizeRequests()
		.antMatchers("/admin/api/**").hasRole("ADMIN")
		.antMatchers("/user/api/**").hasRole("USER")
		.antMatchers("/app/api/**","/captcha.jpg").permitAll()
		.anyRequest().authenticated()
		.and()
		.csrf().disable()
		.formLogin()
		.authenticationDetailsSource(myWebAuthenticationDetailsSource)
		.loginPage("/myLogin.html")
		/*
		 * loginProcessingUrl: 登录请求拦截的url,也就是form表单提交时指定的action
		 * 实际作用：查资料无果，我觉得仅仅是标识登录页面,方便下一步逻辑判断
		 */
		.loginProcessingUrl("/auth/form").permitAll()
		.failureHandler(new MyAuthenticationFailureHandler())
		.and()
		.csrf().disable();
		
		
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	    auth.authenticationProvider(myAuthenticationProvider);
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
