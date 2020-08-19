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
