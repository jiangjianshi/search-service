/** 
 * Project Name: hzf_platform 
 * File Name: HouseController.java 
 * Package Name: com.huifenqi.hzf_platform.controller 
 * Date: 2016年4月26日下午4:38:39 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.search.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.huifenqi.search.context.response.Responses;
import com.huifenqi.search.handler.HouseRequestHandler;


/**
 * ClassName: HouseController date: 2018年2月6日 下午4:38:39 Description:
 * 
 * @author changmingwei
 * @version
 * @since JDK 1.8
 */
@RestController
public class HouseController {

	@Autowired
	private HouseRequestHandler houseRequestHandler;
	
	/**
	 * 搜索房源列表
	 */
	@RequestMapping(value = "/search/searchHouseList", method = RequestMethod.POST)
	public Responses searchHouseList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return houseRequestHandler.searchHouseList(request);
	}
	
	
	/**
     * 查询房源详情
     */
    @RequestMapping(value = "/search/getHouseDetail", method = RequestMethod.POST)
    public Responses getHouseInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return houseRequestHandler.getHouseInfo(request);
    }
    
    
    /**
     * 查询收藏和足迹
     */
    @RequestMapping(value = "/search/getCollectFoot", method = RequestMethod.POST)
    public Responses getCollectorFoot(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return houseRequestHandler.getCollectFoot(request);
    }
    
}
