package com.huifenqi.search.query;

import org.apache.lucene.document.Document;

import com.huifenqi.search.context.entity.house.solr.HouseSolrResult;
import com.huifenqi.search.context.entity.house.solr.RoomSolrResult;

public class SearchUtil {
	
	
	public static HouseSolrResult getHouseDatabean(Document doc) {

		HouseSolrResult bean = new HouseSolrResult();
		bean.getInstance(doc);
		return bean;
	}
	
	public static RoomSolrResult getRoomDatabean(Document doc) {

	    RoomSolrResult bean = new RoomSolrResult();
	    bean.getInstance(doc);
        return bean;
    }
	
	
}
