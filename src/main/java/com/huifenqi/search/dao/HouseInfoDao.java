/** 
 * Project Name: hzf_platform 
 * File Name: HouseDao.java 
 * Package Name: com.huifenqi.hzf_platform.dao 
 * Date: 2016年4月26日下午2:20:01 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.search.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.huifenqi.search.context.dto.request.house.HouseSearchDto;
import com.huifenqi.search.context.dto.response.house.HouseQueryDto;
import com.huifenqi.search.context.entity.house.Agency;
import com.huifenqi.search.context.entity.house.CompanyOffConfig;
import com.huifenqi.search.context.entity.house.FootmarkHistory;
import com.huifenqi.search.dao.repository.house.AgencyManageRepository;
import com.huifenqi.search.dao.repository.house.FootmarkHistoryRepository;
import com.huifenqi.search.dao.repository.house.HouseCollectionRepository;
import com.huifenqi.search.dao.repository.platform.PlatformCustomerRepository;
import com.huifenqi.search.query.ChineseToEnglish;
import com.huifenqi.search.query.SearchUtil;
import com.huifenqi.search.query.SearcherFactory;
import com.huifenqi.search.context.Constants;
import com.huifenqi.search.context.dto.response.house.RoomQueryDto;
import com.huifenqi.search.context.entity.house.HouseCollection;
import com.huifenqi.search.context.entity.house.solr.HouseSolrResult;
import com.huifenqi.search.context.entity.house.solr.RoomSolrResult;
import com.huifenqi.search.context.entity.platform.PlatformCustomer;
import com.huifenqi.search.utils.HouseUtil;
import com.huifenqi.search.utils.LuceneUtil;
import com.huifenqi.search.utils.StringUtil;
import com.huifenqi.search.comm.Request;

/**
 * ClassName: HouseDao date: 2016年4月26日 下午2:20:01 Description:
 * 
 * @author maoxinwu
 * @version
 * @since JDK 1.8
 * 
 */
@Repository
public class HouseInfoDao {

	private static final Log logger = LogFactory.getLog(HouseInfoDao.class);

	@Autowired
	private AgencyManageRepository agencyManageRepository;
	
	@Autowired
    private HouseCollectionRepository houseCollectionRepository;
	
	@Autowired
    private PlatformCustomerRepository platformCustomerRepository;
	
	@Autowired
    private FootmarkHistoryRepository footmarkHistoryRepository;


	/**
     * 查询房源详情
     * 
     * @param sellId
     * @param roomId
     * @param userId
     * @return HouseQueryDto
     */
    public HouseQueryDto getHouseQueryDto(String sellId, int roomId, long userId) {

        // 查询品牌公寓集合
        List<String> agencyIdList = new ArrayList<String>();
        List<Agency> agencyList = agencyManageRepository.findAllAgency();
        if (CollectionUtils.isNotEmpty(agencyList)) {
            for (Agency agency : agencyList) {
                if (!agencyIdList.contains(agency.getCompanyId())) {
                    agencyIdList.add(agency.getCompanyId());
                }
            }
        }

        // 查询房源详情
        HouseQueryDto houseQueryDto = getHouseQueryDtoFromSolr(sellId, roomId, userId, agencyIdList);

        return houseQueryDto;
    }
    
    /**
     * 从Solr中查询房源详情
     * 
     * @param sellId
     * @param roomId
     * @param userId
     * @return HouseQueryDto
     */
    private HouseQueryDto getHouseQueryDtoFromSolr(String sellId, int roomId, long userId, List<String> agencyIdList) {
        HouseQueryDto houseQueryDto = null;

        // 收藏标识
        int collectFlag = 0;
        
        // 用户登录-查询房源是否被收藏
        if (userId > 0) {
            HouseCollection houseCollection = houseCollectionRepository.findHouseCollectionItem(userId, sellId, roomId);
            if (houseCollection != null) {
                collectFlag = 1;
            }
        }

        if (roomId == 0) { // 整租查询
            Query query = findBySellId(sellId);
            HouseSolrResult houseSolrResult = searchHouseCall(query);
            if (houseSolrResult != null) {
                int communityHouseCount = 0;
                int companyHouseCount = 0;
                int companyCityCount = 0;
                // 获取品牌公寓简称--拼接房源标题
                String companyName = "";

                // 判读图片是否美化
                String imageCss = "";
                PlatformCustomer customer = platformCustomerRepository.findBySource(houseSolrResult.getSource());
                if (customer != null) {
                    if (customer.getIsImg() == Constants.platform.IS_IMG_YES) {
                        imageCss = customer.getImageCss();
                    }
                }
                houseQueryDto = HouseUtil.getHouseQueryDto(houseSolrResult, communityHouseCount, companyHouseCount,
                        companyCityCount, collectFlag, agencyIdList, companyName, imageCss);
            }
        } else { // 合租查询
            // 查询房间信息
            Query query = findBySellIdAndRoomId(sellId,roomId);
            RoomSolrResult roomSolrResult = searchRoomCall(query);
            if (roomSolrResult != null) {

                int communityHouseCount = 0;
                int companyHouseCount = 0;
                int companyCityCount = 0;

                // 查询品牌公寓简称-拼接房源标题
                String companyName = "";
                // 判读图片是否美化
                String imageCss = "";
                PlatformCustomer customer = platformCustomerRepository.findBySource(roomSolrResult.getSource());
                if (customer != null) {
                    if (customer.getIsImg() == Constants.platform.IS_IMG_YES) {
                        imageCss = customer.getImageCss();
                    }
                }

                houseQueryDto = HouseUtil.getHouseQueryDto(roomSolrResult, communityHouseCount, companyHouseCount,
                        companyCityCount, collectFlag, agencyIdList, companyName, imageCss);
            }
            if (houseQueryDto != null) {//查询套内房间
                Query queryAll = findBySellId(sellId);
                List<RoomSolrResult> rsrList = searchRoomReferCall(queryAll);
                //List<RoomQueryDto> referHouse = HouseUtil.getReferHouse(houseSolrResult, null);
                List<RoomQueryDto> referHouse = HouseUtil.getNewReferHouse(rsrList);
                houseQueryDto.setReferHouse(referHouse);
            }
        }

        return houseQueryDto;
    }

    
    /**
     * @Title: getFootmarkHistory
     * @Description: 通过用户ID获取浏览房源足迹数据
     * @return List<FootmarkHistory>
     */
    public List<FootmarkHistory> getFootmarkHistory(long userId, String sellId, int roomId) throws Exception {
        return footmarkHistoryRepository.getFootmarkHistory(userId, sellId, roomId);
    }

    /**
     * @Title: getCountByUserId
     * @Description: 通过用户ID获取当前用户下房源浏览足迹总条数
     * @return List
     */
    public List<FootmarkHistory> getCountByUserId(long userId) throws Exception {
        return footmarkHistoryRepository.getCountByUserId(userId);
    }
    
    /**
     * @Title: updateFootmarkHistory
     * @Description: 更新房源浏览足迹数据
     * @return int
     */
    public int updateFootmarkHistory(long footmarkHistoryId, long userId, String sellId, int roomId) throws Exception {
        return footmarkHistoryRepository.updateFootmarkHistory(footmarkHistoryId, userId, sellId, roomId);
    }
    
    /**
     * @Title: saveFootmarkHistory
     * @Description: 保存房源浏览足迹数据
     * @return int
     */
    public long saveFootmarkHistory(FootmarkHistory footmarkHistory) throws Exception {
        footmarkHistory.setUpdateTime(new Date());
        footmarkHistory.setCreateTime(new Date());
        FootmarkHistory footmarkEntity = footmarkHistoryRepository.save(footmarkHistory);
        return footmarkEntity.getId();
    }
    
    
    public Query findBySellId(String sellId) {
        //1.拼接请求参数
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        //2.拼接查询条件
        if (StringUtils.isNotEmpty(sellId)) {
            Query query=new TermQuery(new Term("hsId",sellId.toLowerCase()));
            builder.add(query, BooleanClause.Occur.MUST);  
        }
        
        Query query=builder.build();
        logger.info("solr整租查询请求参数为：" +query.toString() );
        return query;
    }
    
    public Query findBySellIdAndRoomId(String sellId,long roomId) {
        //1.拼接请求参数
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        //2.拼接查询条件
        if (StringUtils.isNotEmpty(sellId)) {
            Query query=new TermQuery(new Term("hsId",sellId.toLowerCase()));
            builder.add(query, BooleanClause.Occur.MUST);
        }
        
        if (roomId > 0) {
            Query query=new TermQuery(new Term("roomId",new Long(roomId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);  
        }
        
        Query query=builder.build();
        logger.info("solr整租查询请求参数为：" +query.toString() );
        return query;
    }
    
    //发起search请求
    public HouseSolrResult searchHouseCall(Query query){
        IndexSearcher searcher = SearcherFactory.getSearcher("house");
        HouseSolrResult rouseSolrResult = null;
        TopDocs hits = null;
        if (searcher != null) {
            try {
                hits = searcher.search(query,300);
                logger.info("search house result total:"+hits.totalHits);
                // 组装
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    rouseSolrResult = SearchUtil.getHouseDatabean(doc);
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
            return rouseSolrResult; 
    }
    
    
    //发起search请求
    public RoomSolrResult searchRoomCall(Query query){
        IndexSearcher searcher = SearcherFactory.getSearcher("room");
        RoomSolrResult roomSolrResult = null;
        TopDocs hits = null;
        if (searcher != null) {
            try {
                hits = searcher.search(query,10);
                logger.info("search room result total:"+hits.totalHits);
                // 组装
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    roomSolrResult= SearchUtil.getRoomDatabean(doc);
                    break;
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return roomSolrResult; 
    }
    
  //发起search请求
    public List<RoomSolrResult> searchRoomReferCall(Query query){
        IndexSearcher searcher = SearcherFactory.getSearcher("room");
        List<RoomSolrResult> rsrList = new ArrayList<RoomSolrResult>();
        RoomSolrResult roomSolrResult = null;
        TopDocs hits = null;
        if (searcher != null) {
            try {
                hits = searcher.search(query,10);
                logger.info("search room result total:"+hits.totalHits);
                // 组装
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    roomSolrResult= SearchUtil.getRoomDatabean(doc);
                    rsrList.add(roomSolrResult);
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return rsrList; 
    }
}
