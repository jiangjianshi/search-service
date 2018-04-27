package com.huifenqi.search.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.huifenqi.search.context.dto.request.house.HouseSearchDto;


/**
 * @author majianchun
 * 
 */
public class QueryPreProcess {
	
	
	private final static Log logger = LogFactory.getLog(QueryPreProcess.class);

	private static volatile QueryPreProcess instance = null;
	
	private static HashMap<String,String> normalizeMap=new HashMap<String,String>();
	static{
		
		normalizeMap.put("一", "1");
		normalizeMap.put("两", "2");
		normalizeMap.put("俩", "2");
		normalizeMap.put("二", "2");
		normalizeMap.put("三", "3");
		normalizeMap.put("四", "4");
		normalizeMap.put("五", "5");
		normalizeMap.put("六", "6");
		normalizeMap.put("七", "7");
		normalizeMap.put("八", "8");
		normalizeMap.put("九", "9");
	    
	}

	
	public static QueryPreProcess getInstance() {
		if (null == instance) {
			synchronized (QueryPreProcess.class) {
				if (null == instance) {
					instance = new QueryPreProcess();
				}
			}
		}
		return instance;
	}

	/***
	 * 
	 * @param queryInfo
	 */
	public void process(HouseSearchDto houseSearchDto) {

		String query = houseSearchDto.getKeyword();
		if (query == null) {
			query = "";
		}


		// 转小写
		query=normalizeStr(query);
		query = query.toLowerCase();
		query=query.trim();
		
		boolean hasDigit=hasDigit(query);
		
		
		houseSearchDto.setProcesskeyword(query);
		houseSearchDto.setHasDigit(hasDigit);
	
	}
	
	
	public boolean hasDigit(String  str)
	{
		boolean result=false;
		if(str.length()<=3)
		{	
			for (int i = 0; i < str.length(); i++) {
				if (Character.isDigit(str.charAt(i))) {
					result=true;
				}
			}
		}
		return result;
	}

	
	public String normalizeStr(String str)
	{
		String result="";
		for (int i = 0; i < str.length(); i++) {
			String sub=str.substring(i, i+1);
			if (normalizeMap.containsKey(sub))
				result=result+normalizeMap.get(sub);
			else
				result=result+sub;
		    
		}
		return result;
		
	}
	
}
