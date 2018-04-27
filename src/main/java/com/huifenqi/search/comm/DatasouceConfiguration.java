package com.huifenqi.search.comm;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatasouceConfiguration {

	@Bean(name = "hzfPlatformDB")
	@Primary
	@ConfigurationProperties(prefix = "huifenqi.hzf.hzfplatform.datasource")
	@Qualifier("hzfPlatformDB")
	public DataSource hzfPlatformDBDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name = "usercommDB")
    @ConfigurationProperties(prefix = "huifenqi.hzf.usercomm.datasource")
    @Qualifier("usercommDB")
    public DataSource usercommDataSource() {
        return DataSourceBuilder.create().build();
    }
}
