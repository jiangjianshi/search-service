/** 
* Project Name: hzf_platform 
* File Name: HouseDetailRepository.java 
* Package Name: com.huifenqi.hzf_platform.dao.repository 
* Date: 2016年4月26日下午2:31:30 
* Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
* 
*/
package com.huifenqi.search.dao.repository.company;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.huifenqi.search.context.Constants;
import com.huifenqi.search.context.entity.house.CompanyOffConfig;

/**
 * ClassName: CompanyOffConfigRepository date: 2016年11月20日 下午2:31:30 Description:
 * 产品下架
 * @author changmingwei
 * @version
 * @since JDK 1.8
 */
public interface CompanyOffConfigRepository extends JpaRepository<CompanyOffConfig, Long> {



	@Query("select a from CompanyOffConfig a" + " where a.cityId=?1 and status="+ Constants.CompanyOffConfig.OPEN_YES_STATUS)
	public List<CompanyOffConfig> findCompanyOffConfigByCityId(long cityId);
	
	@Query("select a from CompanyOffConfig a"+" where status="+ Constants.CompanyOffConfig.OPEN_YES_STATUS)
	public List<CompanyOffConfig> findAllCompanyOffConfig();
}