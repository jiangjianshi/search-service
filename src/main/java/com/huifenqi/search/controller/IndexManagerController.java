package com.huifenqi.search.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.huifenqi.search.context.response.Responses;
import com.huifenqi.search.handler.HouseRequestHandler;
import com.huifenqi.search.query.SearcherFactory;
import com.huifenqi.search.utils.RequestUtils;



/**
 * @author majianchun
 *
 */
@RestController
public class IndexManagerController {
	
    private static final Log logger = LogFactory.getLog(IndexManagerController.class);

	@RequestMapping(value = "/search/indexmanager", method = RequestMethod.GET)
    public Responses doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {	
		
		request.setCharacterEncoding("UTF-8");
		
		int action = RequestUtils.getParameterInt(request, "action");// 操作类型
		String store = RequestUtils.getParameterString(request, "store");// 目标索引库
		String result = null;
		IndexResult indexResult=new IndexResult();
		switch (action) {
		case 1:// 获取索引路径
			result = SearcherFactory.getIndexPath(store);
			logger.info("不在线索引路径"+result);
			break;
		case 2:// 索引切换
			result = SearcherFactory.changeIndexPath(store) ? "1" : "-1";// 1表示成功，-1失败
			logger.info("不在线索引路径"+result);
			break;
		default:
			break;
		}
		
		indexResult.setResult(result);
		Responses responses = new Responses(indexResult);
		return responses;
	}

	public class IndexResult
	{
	
		private String result;
	
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		
	}

	
}
