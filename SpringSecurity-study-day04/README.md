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
```
## 2.MyUserDetailsService自定义逻辑，继承UserDetailsService类
```java
package com.example.demo.service;

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
## 3.@EnableWebSecurity配置类,注入自定义逻辑
```java
package com.example.demo.config;

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

import com.example.demo.service.MyUserDetailsService;

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
		.antMatchers("/app/api/**").permitAll()
		.anyRequest().authenticated()
		.and()
		.formLogin().permitAll()
		.and()
		.csrf().disable();
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

}

```
WebSecurityConfigurerAdapter有3中configure配置情况，我们取一个就好
protected void configure(AuthenticationManagerBuilder auth)
public void configure(WebSecurity web) throws Exception
protected void configure(HttpSecurity http) throws Exception

## 4.application.properties，增加数据库连接
```
spring.datasource.url=jdbc:mysql://192.168.11.10:3306/test?characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&rewriteBatchedStatements=true
spring.datasource.username=mms
spring.datasource.password=qwe123
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=1
```
## 4.插入数据库，用的默认数据库
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
下面常规的mybatis配置，我就不写了
## 5.登录跳转成功后，会访问/路径
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
## 4.以下是demo
访问：http://localhost:8080/app/api/hello，直接跳转，因为所有权限都能访问
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step1.png)
访问：http://localhost:8080/admin/api/hello，到登陆页面，只有访问这个路径的用户才能访问
用user用户登录
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step2.png)
成功登录，但是跳转失败，无权限访问
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step3.png)
用admin用户登录
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step4.png)
成功登录，并返回hello,admin
![Image text](https://github.com/JustShowTime/springSecurity-study/raw/master/images/day2-step5.png)
