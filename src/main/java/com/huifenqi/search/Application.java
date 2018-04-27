/** 
 * Project Name: fileservice_project 
 * File Name: Application.java 
 * Package Name: com.huifenqi.file 
 * Date: 2015年12月30日上午11:44:39 
 * Copyright (c) 2015, www.huizhaofang.com All Rights Reserved. 
 * 
 */  
package com.huifenqi.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.huifenqi.search.query.SearcherFactory;



/** 
 * ClassName: Application
 * date: 2015年12月30日 上午11:44:39
 * Description: 启动接口
 * 
 * @author xiaozhan 
 * @version  
 * @since JDK 1.8 
 */
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter{
	
	@Autowired
	private PlatformInterceptor interceptor;
	
	public static void main(String args[]) {
		SearcherFactory.init();
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor);
	}
}
