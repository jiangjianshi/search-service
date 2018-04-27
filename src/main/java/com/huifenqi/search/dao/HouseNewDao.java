
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huifenqi.search.context.Constants;
import com.huifenqi.search.context.dto.request.house.HouseSearchDto;
import com.huifenqi.search.context.dto.response.house.HouseSearchResultDto;
import com.huifenqi.search.context.dto.response.house.HouseSearchResultInfo;
import com.huifenqi.search.context.entity.house.CompanyOffConfig;
import com.huifenqi.search.context.entity.house.solr.HouseSolrResult;
import com.huifenqi.search.context.entity.house.solr.RoomSolrResult;
import com.huifenqi.search.context.entity.platform.PlatformCustomer;
import com.huifenqi.search.dao.repository.company.CompanyOffConfigRepository;
import com.huifenqi.search.dao.repository.platform.PlatformCustomerRepository;
import com.huifenqi.search.query.ChineseToEnglish;
import com.huifenqi.search.query.QueryPreProcess;
import com.huifenqi.search.query.SearchUtil;
import com.huifenqi.search.query.SearcherFactory;
import com.huifenqi.search.utils.DateUtil;
import com.huifenqi.search.utils.GroupUtils;
import com.huifenqi.search.utils.HouseUtil;
import com.huifenqi.search.utils.LuceneUtil;
import com.huifenqi.search.utils.PageUtil;
import com.huifenqi.search.utils.SolrUtil;
import com.huifenqi.search.utils.StringUtil;


/**
 * ClassName: HouseDao date: 2016年4月26日 下午2:20:01 Description:
 * 
 * @author changmingwei
 * @version
 * @since JDK 1.8
 * 
 */
@Component
public class HouseNewDao {

    private static final Log logger = LogFactory.getLog(HouseNewDao.class);
  
    
    public HouseSolrResult findBySellId(String sellId) {
        return null;
    }
    
    @Autowired
    private CompanyOffConfigRepository companyOffConfigRepository;
    
    @Autowired
    private PlatformCustomerRepository platformCustomerRepository;
    
 
    
    

    public Query getHouseAllByMultiCondition(HouseSearchDto houseSearchDto, List<String> agencyIdList,String operator) {
        //1.拼接请求参数
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        //3.过滤限制渠道的房源
        if (CollectionUtils.isNotEmpty(houseSearchDto.getDenyList())) {
            for(String agencyId : houseSearchDto.getDenyList()){
                Query query=new TermQuery(new Term("fsource",agencyId.toLowerCase()));
                builder.add(query, BooleanClause.Occur.MUST_NOT);
            }
        }
        
        //4.按城市+公司过滤中介黑名称房源
        List<CompanyOffConfig> companyOffList =  companyOffConfigRepository.findCompanyOffConfigByCityId(houseSearchDto.getCityId());
        if(!companyOffList.isEmpty()){
            for(CompanyOffConfig config : companyOffList){  
                Query query=new TermQuery(new Term("companyId",config.getCompanyId().toLowerCase()));
                builder.add(query, BooleanClause.Occur.MUST_NOT);
            }
        }
        
        //收藏列表
        if (CollectionUtils.isNotEmpty(houseSearchDto.getCollectHouseList())) {
            LuceneUtil.addCollectCriteria(builder,"hsId",houseSearchDto.getCollectHouseList());
        }
        //足迹列表
        if (CollectionUtils.isNotEmpty(houseSearchDto.getFootHouseList())) {
            LuceneUtil.addCollectCriteria(builder,"hsId",houseSearchDto.getFootHouseList());
        }
        
        //5.城市
        long cityId = houseSearchDto.getCityId();
        if (cityId > 0) {
            Query query=new TermQuery(new Term("cityId",new Long(cityId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);
            
        }
        if (StringUtils.isNotEmpty(houseSearchDto.getSellerId())) {
            Query query=new TermQuery(new Term("hsId",houseSearchDto.getSellerId()));
            builder.add(query, BooleanClause.Occur.MUST);
        }

        //6.房型
        String eBedRoomNums = houseSearchDto.geteBedRoomNums();// 自在整租
        String sBedRoomNums = houseSearchDto.getsBedRoomNums();// 优选合租
        LuceneUtil.addHouseStyleCriteria(builder, eBedRoomNums, sBedRoomNums, "bedroomNums", "entireRent");
        
        
        // 租房宝典,白领优选筛选合租主卧和整租一居室的房源
        String cBedRoomNums = houseSearchDto.getcBedRoomNums();
        if (!StringUtil.isEmpty(cBedRoomNums)) {    
            Query query=new TermQuery(new Term("bedroomNums",cBedRoomNums));
            builder.add(query, BooleanClause.Occur.MUST);
        }
    
                
        // 整租
        int entireRent = houseSearchDto.getEntireRent();    
        
        BooleanQuery.Builder  tmpbuildType=new BooleanQuery.Builder();
        Query query1=new TermQuery(new Term("entireRent",new Integer(entireRent).toString()));
        tmpbuildType.add(query1, BooleanClause.Occur.SHOULD);
         
        Query query2=new TermQuery(new Term("entireRent",new Integer(Constants.HouseDetail.RENT_TYPE_BOTH).toString()));
        tmpbuildType.add(query2, BooleanClause.Occur.SHOULD);
         
        BooleanQuery tmpQuery=tmpbuildType.build();
        builder.add(tmpQuery,BooleanClause.Occur.MUST);
                
        // 品牌公寓筛选
        String companyId = houseSearchDto.getCompanyId();
        LuceneUtil.addCompanyCriteria(builder, companyId, "companyId", agencyIdList);
        
        
    
        
        // 商圈Id
        long bizId = houseSearchDto.getBizId();
        if (bizId != 0) {
            Query query=new TermQuery(new Term("bizId",new Long(bizId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);
        }
        
        // 行政区Id
        long disId = houseSearchDto.getDistrictId();
        if (disId != 0) {
            Query query=new TermQuery(new Term("districtId",new Long(disId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);
        }
        
        
        // 地铁线路Id
        long lineId = houseSearchDto.getLineId();
        if (lineId != 0) {
            String lineIdTag = String.valueOf(lineId);
            LuceneUtil.addTagCriteria(builder, lineIdTag, "subwayLineId");
        }
        
        // 地铁站Id
        long stationId = houseSearchDto.getStationId();
        if (stationId != 0) {
            String stationIdTag = String.valueOf(stationId);
            LuceneUtil.addTagCriteria(builder, stationIdTag, "subwayStationId");
        }
        
        /*
        // 附近距离
        String distanceKey = "google";
        String location = houseSearchDto.getLocation();
        if (StringUtil.isNotEmpty(location)) { // 当前位置
            int nearybyDistance = searchConfiguration.getNearybyDistance();
            LuceneUtil.addDistanceCriteria(builder, distanceKey, location, nearybyDistance);
        } else if (stationId != 0) { // 地铁位置
            String stationPosition = houseSearchDto.getStationLocation();
            int stationDistance = searchConfiguration.getStationDistance();
            LuceneUtil.addDistanceCriteria(builder, distanceKey, stationPosition, stationDistance);
        }*/
        
        
        String location = houseSearchDto.getLocation();
        if (StringUtil.isNotEmpty(location)) { // 当前位置 
            getSpatial(builder,location);   
        }
        
        //按离地铁的距离搜索
        String distance = houseSearchDto.getDistance();
        if (StringUtil.isNotEmpty(distance)) {
            LuceneUtil.addRegionCriteria(builder, "subwayDistance", distance);
        }
        
        
        // 价格Id
        String price = houseSearchDto.getPrice();
        if (StringUtil.isNotEmpty(price)) {
            LuceneUtil.addRegionCriteria(builder, "rentPriceMonth", price);
        }
        
        
        // 朝向
        String orientations = houseSearchDto.getOrientationStr();
        if (StringUtil.isNotEmpty(orientations)) {
            LuceneUtil.addOrientationCriteria(builder, orientations, "orientations");
        }


        
        // houseTag
        String houseTag = houseSearchDto.getHouseTag();
        if (StringUtil.isNotEmpty(houseTag)) {
            LuceneUtil.addTagCriteria(builder, houseTag, "housedTag");
        }
        
        
        
        // 付款方式 (默认-1查询全部)
        String payType = houseSearchDto.getPayType();
        if (StringUtil.isNotBlank(payType)) {
            LuceneUtil.addPayTypeCriteria(builder, payType, "depositMonth", "periodMonth");
        }
        

        // 关键词        
        String processkeyword =houseSearchDto.getProcesskeyword();
        if (StringUtil.isNotEmpty(processkeyword)) {
            
            
            if (processkeyword.contains("支持月付") || processkeyword.contains("分期") || processkeyword.contains("月付")) {
                
                Query query=new TermQuery(new Term("isPayMonth","1"));
                builder.add(query, BooleanClause.Occur.MUST);
                
            } else {
                
                BooleanQuery.Builder  tmpbuilder = new BooleanQuery.Builder();
                Query  tmpquery=null;
                
                //根据query长度获取slop距离
                int flop =  getSlop(processkeyword);
                
                Query query= getKeyQuery(processkeyword, "address",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                query= getKeyQuery(processkeyword, "companyName",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                query= getKeyQuery(processkeyword, "title",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                
                if(processkeyword.matches("[a-zA-Z]+") || operator.equals("or")){//全字母、二次调度
                    //xierqi  
                    String pinyin=ChineseToEnglish.getPingYin(processkeyword);
                    query = new WildcardQuery(new Term("title_pinyin","*"+pinyin+"*"));  
                    tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                    
                    query = new WildcardQuery(new Term("subway_pinyin","*"+pinyin+"*"));  
                    tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                }
                
                
                
                query=new TermQuery(new Term("rentName",processkeyword));
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                
                query= getKeyQuery(processkeyword, "subway",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                

                
                
                tmpquery=tmpbuilder.build();
                builder.add(tmpquery,BooleanClause.Occur.MUST);
                
   
                
            }
        }
        
        
        
        // 小区名称
        String communityName = houseSearchDto.getCommunityName();
        if (StringUtil.isNotEmpty(communityName)) {
            
            communityName = ClientUtils.escapeQueryChars(communityName);
            Query query= getKeyQuery(communityName, "communityName",1,"and") ;
            builder.add(query, BooleanClause.Occur.MUST);
            
        }
        
        
        // 是否查询品牌公寓房源（1：品牌公寓房源；2：非品牌公寓房源）
        String companyType = houseSearchDto.getCompanyType();
        if (StringUtil.isNotBlank(companyType)) {
            LuceneUtil.addCompanyQueryCriteria(builder, companyType, "companyId", agencyIdList);
        }
        
        
        // 1 新房源，6 部分出租
        LuceneUtil.addHouseStatusCriteria(builder, "status");
        
        
        
        // 1 程序审核通过；3 图片审核通过
        LuceneUtil.addHouseApproveStatusCriteria(builder, "approveStatus");
        
        
        Query query=builder.build();
        
        
        logger.info("solr整租查询请求参数为：" +query.toString() );
        
        return query;
    }
    
    
    public Query getRoomAllByMultiCondition(HouseSearchDto houseSearchDto, List<String> agencyIdList,String operator) {
        //1.拼接请求参数
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        
        //3.过滤限制渠道的房源
        if (CollectionUtils.isNotEmpty(houseSearchDto.getDenyList())) {
            for(String agencyId : houseSearchDto.getDenyList()){
                Query query=new TermQuery(new Term("source",agencyId.toLowerCase()));
                builder.add(query, BooleanClause.Occur.MUST_NOT);
            }
        }

        //4.按城市+公司过滤中介黑名称房源
        List<CompanyOffConfig> companyOffList =  companyOffConfigRepository.findCompanyOffConfigByCityId(houseSearchDto.getCityId());
        if(!companyOffList.isEmpty()){
            for(CompanyOffConfig config : companyOffList){  
                Query query=new TermQuery(new Term("companyId",config.getCompanyId().toLowerCase()));
                builder.add(query, BooleanClause.Occur.MUST_NOT);
            }
        }
        
        //收藏列表
        if (CollectionUtils.isNotEmpty(houseSearchDto.getCollectRoomList())) {
            LuceneUtil.addCollectCriteria(builder,"roomId",houseSearchDto.getCollectRoomList());
        }
        
        //足迹列表
        if (CollectionUtils.isNotEmpty(houseSearchDto.getFootRoomList())) {
            LuceneUtil.addCollectCriteria(builder,"roomId",houseSearchDto.getFootRoomList());
        }
        
        //5.城市
        long cityId = houseSearchDto.getCityId();
        if (cityId > 0) {
            Query query=new TermQuery(new Term("cityId",new Long(cityId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);
            
        }

        //6.房型
        String eBedRoomNums = houseSearchDto.geteBedRoomNums();// 自在整租
        String sBedRoomNums = houseSearchDto.getsBedRoomNums();// 优选合租
        LuceneUtil.addRoomStyleCriteria(builder, sBedRoomNums, eBedRoomNums, "bedroomNums", "entireRent");
        
        // 租房宝典,白领优选筛选合租主卧和整租一居室的房源
        String cBedRoomNums = houseSearchDto.getcBedRoomNums();
        if (!StringUtil.isEmpty(cBedRoomNums)) {
            Query queryBedNums=IntPoint.newRangeQuery("bedroomNums", Integer.valueOf(cBedRoomNums), Integer.valueOf(cBedRoomNums));
            builder.add(queryBedNums, BooleanClause.Occur.MUST);
        }
        
        // 分租+整分皆可
        int entireRent = houseSearchDto.getEntireRent();    
        BooleanQuery.Builder  tmpbuilderType=new BooleanQuery.Builder();
        Query query1=new TermQuery(new Term("entireRent",new Integer(entireRent).toString()));
        tmpbuilderType.add(query1, BooleanClause.Occur.SHOULD);
         
        Query query2=new TermQuery(new Term("entireRent",new Integer(Constants.HouseDetail.RENT_TYPE_BOTH).toString()));
        tmpbuilderType.add(query2, BooleanClause.Occur.SHOULD);
         
        BooleanQuery tmpQuery=tmpbuilderType.build();
        builder.add(tmpQuery,BooleanClause.Occur.MUST);
                
          
        // 品牌公寓筛选
        String companyId = houseSearchDto.getCompanyId();
        LuceneUtil.addCompanyCriteria(builder, companyId, "companyId", agencyIdList);
        
        
    
        
        // 商圈Id
        long bizId = houseSearchDto.getBizId();
        if (bizId != 0) {
            Query query=new TermQuery(new Term("bizId",new Long(bizId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);
        }
        
        // 行政区Id
        long disId = houseSearchDto.getDistrictId();
        if (disId != 0) {
            Query query=new TermQuery(new Term("districtId",new Long(disId).toString()));
            builder.add(query, BooleanClause.Occur.MUST);
        }
        
        
        // 地铁线路Id
        long lineId = houseSearchDto.getLineId();
        if (lineId != 0) {
            String lineIdTag = String.valueOf(lineId);
            LuceneUtil.addTagCriteria(builder, lineIdTag, "subwayLineId");
        }
        
        // 地铁站Id
        long stationId = houseSearchDto.getStationId();
        if (stationId != 0) {
            String stationIdTag = String.valueOf(stationId);
            LuceneUtil.addTagCriteria(builder, stationIdTag, "subwayStationId");
        }
        
        
        
        /*
        // 附近距离
        String distanceKey = "google";
        String location = houseSearchDto.getLocation();
        if (StringUtil.isNotEmpty(location)) { // 当前位置
            int nearybyDistance = searchConfiguration.getNearybyDistance();
            LuceneUtil.addDistanceCriteria(builder, distanceKey, location, nearybyDistance);
        } else if (stationId != 0) { // 地铁位置
            String stationPosition = houseSearchDto.getStationLocation();
            int stationDistance = searchConfiguration.getStationDistance();
            LuceneUtil.addDistanceCriteria(builder, distanceKey, stationPosition, stationDistance);
        }
        */
        String location = houseSearchDto.getLocation();
        if (StringUtil.isNotEmpty(location)) { // 当前位置 
            getSpatial(builder,location); 
        }

        
        //按离地铁的距离搜索
        String distance = houseSearchDto.getDistance();
        if (StringUtil.isNotEmpty(distance)) {
            LuceneUtil.addRegionCriteria(builder, "subwayDistance", distance);
        }
        
        
        // 价格Id
        String price = houseSearchDto.getPrice();
        if (StringUtil.isNotEmpty(price)) {
            LuceneUtil.addRegionCriteria(builder, "rRentPriceMonth", price);
        }
        
       
        // 朝向
        String orientations = houseSearchDto.getOrientationStr();
        if (StringUtil.isNotEmpty(orientations)) {
            LuceneUtil.addOrientationCriteria(builder, orientations, "roomori");
        }


        
        // houseTag
        String houseTag = houseSearchDto.getHouseTag();
        if (StringUtil.isNotEmpty(houseTag)) {
            LuceneUtil.addTagCriteria(builder, houseTag, "roomtag");
        }
        
        
       
        // 付款方式 (默认-1查询全部)
        String payType = houseSearchDto.getPayType();
        if (StringUtil.isNotBlank(payType)) {
            LuceneUtil.addPayTypeCriteria(builder, payType, "rDepositMonth", "rPeriodMonth");
        }
       
         

        
        //------------------------------------------
        // 关键词
        
        String processkeyword =houseSearchDto.getProcesskeyword();
        if (StringUtil.isNotEmpty(processkeyword)) {
            
            if (processkeyword.contains("支持月付") || processkeyword.contains("分期") || processkeyword.contains("月付")) {
                
                Query query=new TermQuery(new Term("risPayMonth","1"));
                builder.add(query, BooleanClause.Occur.MUST);
                
            } else {
                
                BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
                Query  tmpquery=null;
                
                //根据query长度获取slop距离
                int flop=  getSlop(processkeyword);
                
                Query query= getKeyQuery(processkeyword, "address",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                query= getKeyQuery(processkeyword, "companyName",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                query= getKeyQuery(processkeyword, "title",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                if(processkeyword.matches("[a-zA-Z]+") || operator.equals("or")){//全字母、二次调度
                    String pinyin=ChineseToEnglish.getPingYin(processkeyword);
                    query = new WildcardQuery(new Term("title_pinyin","*"+pinyin+"*"));  
                    tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                    
                    query = new WildcardQuery(new Term("subway_pinyin","*"+pinyin+"*"));  
                    tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                }
                
                query=new TermQuery(new Term("rentName",processkeyword));
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                
                
                query= getKeyQuery(processkeyword, "subway",flop,operator) ;
                tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                

                
                
                tmpquery=tmpbuilder.build();
                builder.add(tmpquery,BooleanClause.Occur.MUST);
              
                
            }
        }
        
        
        
        // 小区名称
        String communityName = houseSearchDto.getCommunityName();
        if (StringUtil.isNotEmpty(communityName)) {
            
            communityName = ClientUtils.escapeQueryChars(communityName);
            Query query= getKeyQuery(communityName, "communityName",1,"and") ;
            builder.add(query, BooleanClause.Occur.MUST);
            
        }
        
        
        // 是否查询品牌公寓房源（1：品牌公寓房源；2：非品牌公寓房源）
        String companyType = houseSearchDto.getCompanyType();
        if (StringUtil.isNotBlank(companyType)) {
            LuceneUtil.addCompanyQueryCriteria(builder, companyType, "companyId", agencyIdList);
        }
        
        
        // 1 新房源，6 部分出租
        LuceneUtil.addHouseStatusCriteria(builder, "rStatus");
        
        
        
        // 1 程序审核通过；3 图片审核通过
        LuceneUtil.addHouseApproveStatusCriteria(builder, "rapproveStatus");
        
        
        Query query=builder.build();
        
        
        logger.info("solr合租查询请求参数为：" +query.toString() );
        
        return query;
    }
    
    
    
    
    
    
    public static Query getKeyQuery(String key, String fieldName,int slop,String operator) {
        
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        Query tmpQ = builder.build();
        
        try {
            Analyzer analyzer = new StandardAnalyzer();  
            QueryParser qp = new QueryParser(fieldName, analyzer); 
            if(operator.equals("and"))
                qp.setDefaultOperator(Operator.AND);
            else
                qp.setDefaultOperator(Operator.OR);
            qp.setAllowLeadingWildcard(false);
            if(slop != 0){//非slop
                qp.setPhraseSlop(slop);
                tmpQ = qp.parse(QueryParser.escape(key));
            }if ( slop == 0) {//slop
                tmpQ = qp.createPhraseQuery(fieldName, QueryParser.escape(key), 0);
            }  
            
        } catch (Exception e) {
            
            builder=new BooleanQuery.Builder();
            tmpQ = builder.build();
            
        }
        if (tmpQ == null) {
            builder=new BooleanQuery.Builder();
            tmpQ = builder.build();
        }
        return tmpQ;
    }

    private static int getSlop(String key) {

        if (key.length() <= 3) {
            return 0;
        } else {
            return key.length();
        }
    }


    /**
     * 条件筛选 搜索房源
     * 
     * @return
     */
    public HouseSearchResultDto getHouseResultDto(HouseSearchDto houseSearchDto, List<String> agencyIdList) {
      
        int entireRent = houseSearchDto.getEntireRent();
        HouseSearchResultDto houseSearchResultDto = new HouseSearchResultDto();
        
        //1query 预处理
        QueryPreProcess queryPreProcess= QueryPreProcess.getInstance();
        queryPreProcess.process(houseSearchDto);
        
        logger.info("query:"+houseSearchDto.getKeyword()+" "+houseSearchDto.getProcesskeyword());
        
        //2.拼接参数,发起search请求
        List<HouseSolrResult> resulHousetList = null;
        List<RoomSolrResult> resultRoomList = null;
        List<HouseSearchResultInfo> houseSearchResultInfoList = null;
        List<HouseSearchResultInfo> groupInfoList = null;
        List<HouseSearchResultInfo> pageResultInfoList = null;
        if(entireRent == Constants.HouseDetail.RENT_TYPE_ENTIRE){//整租
            
            resulHousetList = queryHouse(houseSearchDto,agencyIdList,"and");
            
            //无结果情况下的二次调度
            if(resulHousetList.size() == 0 && !houseSearchDto.getProcesskeyword().equals("")){
                resulHousetList = queryHouse(houseSearchDto,agencyIdList,"or");
            }
            
            if(houseSearchDto.getOrderType().isEmpty()){//不排序 
                resulHousetList=rankTiaoquanHouse(resulHousetList); //调权
            }
  
            houseSearchResultInfoList = HouseUtil.getHouseSearchResultInfoListByHouse(resulHousetList, houseSearchDto.getCollectFlagHouseList());
        }
        if(entireRent == Constants.HouseDetail.RENT_TYPE_SHARE){//合租
      
            resultRoomList = queryRoom(houseSearchDto,agencyIdList,"and");
            
            //无结果情况下的二次调度
            if(resultRoomList.size()==0 && !houseSearchDto.getProcesskeyword().equals("")){
                resultRoomList = queryRoom(houseSearchDto,agencyIdList,"or");
            }
            
            if(houseSearchDto.getOrderType().isEmpty()){//不排序  
                resultRoomList=rankTiaoquanRoom(resultRoomList);//调权
            }
            
            houseSearchResultInfoList = HouseUtil.getHouseSearchResultInfoListByRoom(resultRoomList, houseSearchDto.getCollectFlagRoomList());
            
            
        }if(entireRent == Constants.HouseDetail.RENT_TYPE_ALL){//全部
            
            resulHousetList = queryHouse(houseSearchDto,agencyIdList,"and");
            resultRoomList = queryRoom(houseSearchDto,agencyIdList,"and");

            //无结果情况下的二次调度
            if(resulHousetList.size()==0 && !houseSearchDto.getProcesskeyword().equals("") && resultRoomList.size()==0)
            {
                resulHousetList = queryHouse(houseSearchDto,agencyIdList,"or");
                resultRoomList = queryRoom(houseSearchDto,agencyIdList,"or");

            }
            
            if(houseSearchDto.getOrderType().isEmpty()){//不排序 
                //调权
                resulHousetList=rankTiaoquanHouse(resulHousetList);
                resultRoomList=rankTiaoquanRoom(resultRoomList);
            }
            
            List<HouseSearchResultInfo> houseSearchResultInfoListA = HouseUtil.getHouseSearchResultInfoListByHouse(resulHousetList, houseSearchDto.getCollectFlagHouseList());
            List<HouseSearchResultInfo> houseSearchResultInfoListB = HouseUtil.getHouseSearchResultInfoListByRoom(resultRoomList, houseSearchDto.getCollectFlagRoomList());
           
            houseSearchResultInfoList=new LinkedList<HouseSearchResultInfo>();
            if(houseSearchResultInfoListA!=null)
                houseSearchResultInfoList.addAll(houseSearchResultInfoListA);
            if(houseSearchResultInfoListB!=null)
                houseSearchResultInfoList.addAll(houseSearchResultInfoListB);
        }
        
        if (CollectionUtils.isNotEmpty(houseSearchResultInfoList)) {
            if(houseSearchDto.getOrderType().isEmpty()){//不排序 
                //4房源打散
                groupInfoList = getGroupInfoList(houseSearchResultInfoList);
            }else{
                groupInfoList = houseSearchResultInfoList;
            }
            
            //5分页
            PageUtil page = new PageUtil(houseSearchDto.getPageNum(),houseSearchDto.getPageSize(),groupInfoList.size());
            pageResultInfoList = getInfoList(page,groupInfoList);
            houseSearchResultDto.setSearchHouses(pageResultInfoList);
        }
       
        return houseSearchResultDto;
    }  
 
    /**
     * 条件筛选 搜索房源
     * 
     * @return
     */
    public HouseSearchResultDto getCollectFoot(HouseSearchDto houseSearchDto, List<String> agencyIdList) {
      
        int entireRent = houseSearchDto.getEntireRent();
        HouseSearchResultDto houseSearchResultDto = new HouseSearchResultDto();
        
        //1query 预处理
        QueryPreProcess queryPreProcess= QueryPreProcess.getInstance();
        queryPreProcess.process(houseSearchDto);
        
        logger.info("query:"+houseSearchDto.getKeyword()+" "+houseSearchDto.getProcesskeyword());
        
        //2.拼接参数,发起search请求
        List<HouseSolrResult> resulHousetList = null;
        List<RoomSolrResult> resultRoomList = null;
        List<HouseSearchResultInfo> houseSearchResultInfoList = null;
        List<HouseSearchResultInfo> groupInfoList = null;
        List<HouseSearchResultInfo> pageResultInfoList = null;
        if(entireRent == Constants.HouseDetail.RENT_TYPE_ENTIRE){//整租
            
            resulHousetList = queryHouse(houseSearchDto,agencyIdList,"and");
            
            if(houseSearchDto.getOrderType().isEmpty()){//不排序 
                resulHousetList=rankTiaoquanHouse(resulHousetList); //调权
            }
  
            houseSearchResultInfoList = HouseUtil.getHouseSearchResultInfoListByHouse(resulHousetList, houseSearchDto.getCollectFlagHouseList());
        }
        if(entireRent == Constants.HouseDetail.RENT_TYPE_SHARE){//合租
      
            resultRoomList = queryRoom(houseSearchDto,agencyIdList,"and");
            
            if(houseSearchDto.getOrderType().isEmpty()){//不排序  
                resultRoomList=rankTiaoquanRoom(resultRoomList);//调权
            }

            houseSearchResultInfoList = HouseUtil.getHouseSearchResultInfoListByRoom(resultRoomList, houseSearchDto.getCollectFlagRoomList());
            
            
        }if(entireRent == Constants.HouseDetail.RENT_TYPE_ALL){//全部
            
            resulHousetList = queryHouse(houseSearchDto,agencyIdList,"and");
            resultRoomList = queryRoom(houseSearchDto,agencyIdList,"and");
 
            if(houseSearchDto.getOrderType().isEmpty()){//不排序 
                //调权
                resulHousetList=rankTiaoquanHouse(resulHousetList);
                resultRoomList=rankTiaoquanRoom(resultRoomList);
            }
            
            List<HouseSearchResultInfo> houseSearchResultInfoListA = HouseUtil.getHouseSearchResultInfoListByHouse(resulHousetList, houseSearchDto.getCollectFlagHouseList());
            List<HouseSearchResultInfo> houseSearchResultInfoListB = HouseUtil.getHouseSearchResultInfoListByRoom(resultRoomList, houseSearchDto.getCollectFlagRoomList());
           
            houseSearchResultInfoList=new LinkedList<HouseSearchResultInfo>();
            if(houseSearchResultInfoListA!=null)
                houseSearchResultInfoList.addAll(houseSearchResultInfoListA);
            if(houseSearchResultInfoListB!=null)
                houseSearchResultInfoList.addAll(houseSearchResultInfoListB);
        }
        
        if (CollectionUtils.isNotEmpty(houseSearchResultInfoList)) {
            if(houseSearchDto.getOrderType().isEmpty()){//不排序 
                //4房源打散
                groupInfoList = getGroupInfoList(houseSearchResultInfoList);
            }else{
                groupInfoList = houseSearchResultInfoList;
            }
            
            //5分页
            PageUtil page = new PageUtil(houseSearchDto.getPageNum(),houseSearchDto.getPageSize(),groupInfoList.size());
            pageResultInfoList = getInfoList(page,groupInfoList);
            houseSearchResultDto.setSearchHouses(pageResultInfoList);
        }
       
        return houseSearchResultDto;
    }  
    
    
    
    public   List<HouseSolrResult> rankTiaoquanHouse(List<HouseSolrResult> resulHousetList)
    {        

        if(CollectionUtils.isNotEmpty(resulHousetList))
        {
              //优质公寓调权
              HashMap<String,Float> map = null;
              List<PlatformCustomer> platList = platformCustomerRepository.findByisWeight();
              if(CollectionUtils.isNotEmpty(platList)){
                  map = new LinkedHashMap<String,Float>();
                  for(PlatformCustomer p :platList){
                      map.put(p.getSource(), p.getWeightValue());
                  }   
              }
              
              for(HouseSolrResult houseSolrResult:resulHousetList)
              {
                  float textscore=houseSolrResult.getTextscore();
                  float finalscore=textscore;
                  //发布时间调权
                  String pubDate=houseSolrResult.getPubDate();
                  int diffday=DateUtil.getDiffDaytime(pubDate);
                  if(diffday<=3)
                      finalscore=textscore* (float)1.2;
                  //图片数量调权
                  List<String> images=houseSolrResult.getImgs();
                  List<String> webImags=houseSolrResult.getWebImgs();

                  if(images.size()>0 && checkImg(webImags))
                      finalscore=finalscore*(float)2.0;
                  
                  //优质公寓调权
                  if(map != null && map.containsKey(houseSolrResult.getSource())){
                      finalscore = finalscore*map.get(houseSolrResult.getSource());
                  }
                  
                  houseSolrResult.setFinalscore(finalscore);
   
              }
              
              
              Collections.sort(resulHousetList, new Comparator<HouseSolrResult>() {
                @Override
                public int compare(HouseSolrResult result1, HouseSolrResult result2) {
                    try {

                        float score1 = result1.getFinalscore();
                        float score2 = result2.getFinalscore();
                        
                        return new Float(score2).compareTo(score1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
               
            
        }
        return resulHousetList;
        
        
    }
    
    
    public   List<RoomSolrResult> rankTiaoquanRoom(List<RoomSolrResult> resulRoomList)
    {
        

        if(CollectionUtils.isNotEmpty(resulRoomList))
        {
        	  //优质公寓调权
        	  HashMap<String,Float> map = null;
        	  List<PlatformCustomer> platList = platformCustomerRepository.findByisWeight();
        	  if(CollectionUtils.isNotEmpty(platList)){
        		  map = new LinkedHashMap<String,Float>();
        		  for(PlatformCustomer p :platList)
                    map.put(p.getSource(), p.getWeightValue());
                
        	  }	
        	
        	
        	  for(RoomSolrResult roomSolrResult:resulRoomList)
              {
                  float textscore=roomSolrResult.getTextscore();
                  float finalscore=textscore;
                  //发布时间调权
                  String pubDate=roomSolrResult.getPubDate();
                  int diffday=DateUtil.getDiffDaytime(pubDate);
                  if(diffday<=3)
                      finalscore=textscore* (float)1.2;
                  
                  //图片调权
                  List<String> images=roomSolrResult.getImgs();
                  List<String> webImgs=roomSolrResult.getWebImgs();
                  if(images.size()>0 && checkImg(webImgs))
                      finalscore=finalscore*(float)2.0;
                  
                  
                  //优质公寓调权
                  if(map != null &&  map.containsKey(roomSolrResult.getSource())){
                      finalscore = finalscore*map.get(roomSolrResult.getSource());
                  }
                  
                  roomSolrResult.setFinalscore(finalscore);
   
              }
              
              
              Collections.sort(resulRoomList, new Comparator<RoomSolrResult>() {
                @Override
                public int compare(RoomSolrResult result1, RoomSolrResult result2) {
                    try {

                        float score1 = result1.getFinalscore();
                        float score2 = result2.getFinalscore();
                        
                        return new Float(score2).compareTo(score1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
               
            
        }
        return resulRoomList;
        
        
    }
    
    
    
  
    //发起search请求
    public List<HouseSolrResult> searchHouseCall(IndexSearcher searcher,Query query,HouseSearchDto houseSearchDto){
        List<HouseSolrResult> resultList=new LinkedList<HouseSolrResult>();
        TopDocs hits = null;
        if (searcher != null) {
            try {
            	
            	
                Sort sort = getSort(houseSearchDto);
                if(sort!=null)
                     hits = searcher.search(query,300,sort,true,false);
                else {
                     hits = searcher.search(query,300,Sort.RELEVANCE,true,false);
                }
                logger.info("search result total:"+hits.totalHits);
                // 组装
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                                
                    Document doc = searcher.doc(scoreDoc.doc);
                    HouseSolrResult bean = SearchUtil.getHouseDatabean(doc);
                    bean.setTextscore(scoreDoc.score);
                    resultList.add(bean);
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return resultList; 
    }
    
    //发起search请求
    public List<RoomSolrResult> searchRoomCall(IndexSearcher searcher,Query query,HouseSearchDto houseSearchDto){
        List<RoomSolrResult> resultList=new LinkedList<RoomSolrResult>();
        TopDocs hits = null;
        if (searcher != null) {
            try {
                Sort sort = getSort(houseSearchDto);
                
                if(sort!=null)
                     hits = searcher.search(query,300,sort,true,false);
                else {
                     hits = searcher.search(query,300,Sort.RELEVANCE,true,false);
                }
                logger.info("search result total:"+hits.totalHits);
                // 组装
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    
                    RoomSolrResult bean = SearchUtil.getRoomDatabean(doc);
                    bean.setTextscore(scoreDoc.score);
                    
                    resultList.add(bean);
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return resultList; 
    }
    
    /**
     * 获取排序规则
     * 
     * @param houseSearchDto
     * @return
     */
    private Sort getSort(HouseSearchDto houseSearchDto) {
        if (houseSearchDto == null) {
            return null;
        }
        //排序字段
        String orderType = houseSearchDto.getOrderType();
        //排序
        String order = houseSearchDto.getOrder();
        
        //query
        String query = houseSearchDto.getKeyword();
        SortField sortFiled = null;
        //默认排序
        if(StringUtils.isEmpty(query) && StringUtils.isEmpty(orderType)){
            sortFiled = getDefaultSortField(houseSearchDto);
        }else{
            sortFiled = getSortField(orderType, order,houseSearchDto); 
        }

        if (sortFiled == null) {
            return null;
        }
        Sort sort=new Sort();  
        sort.setSort(sortFiled);
        return sort;
    }
    
    
    
    /**
     * 获取默认时间排序
     * 
     * @param orderType
     * @param order
     * @param location
     * @return
     */
    
    private SortField getDefaultSortField (HouseSearchDto houseSearchDto) {
        SortField sortField = null;
        String houseOrderType = null;
        // 传入的排序字段(主要排序条件)
        if(houseSearchDto.getEntireRent() == Constants.HouseDetail.RENT_TYPE_ENTIRE){
             houseOrderType = "updateDate_sort";
            
        }if(houseSearchDto.getEntireRent() == Constants.HouseDetail.RENT_TYPE_SHARE){
             houseOrderType = "rupdateTime_sort";
        }
        
        if (StringUtil.isNotEmpty(houseOrderType)) {
            Type type = Type.LONG;
            sortField = new SortField(houseOrderType, type, true);//时间降序
        }
        return sortField;
    }
    
    
    /**
     * 获取排方向
     * 
     * @param orderType
     * @param order
     * @param location
     * @return
     */
    
    private SortField getSortField(String orderType, String order,HouseSearchDto houseSearchDto) {
        SortField sortField = null;
        String houseOrderType = null;
        // 传入的排序字段(主要排序条件)
        if(houseSearchDto.getEntireRent() == Constants.HouseDetail.RENT_TYPE_ENTIRE){
             houseOrderType = getHouseOrderType(orderType);
            
        }if(houseSearchDto.getEntireRent() == Constants.HouseDetail.RENT_TYPE_SHARE){
             houseOrderType = getRoomOrderType(orderType);
        }
        
        if (StringUtil.isNotEmpty(houseOrderType)) {
            Type type = Type.LONG;
            
            if(StringUtils.isEmpty(order)){//默认升序
                sortField = new SortField(houseOrderType, type, false);  //升序 
            }else{
                if("ASC".equals(order.toUpperCase())){
                    sortField = new SortField(houseOrderType, type, false);  //升序
               }else{
                   sortField = new SortField(houseOrderType, type, true);  //降序
               } 
            }
            
        }
        return sortField;
    }
    
    /**
     * 获取排序字段
     * 
     * @param orderType
     * @return
     */
    private String getHouseOrderType(String orderType) {

        if (StringUtil.isEmpty(orderType)) {
            return null;
        }

        if (orderType.equals(Constants.Search.ORDER_TYPE_PRICE)) {
            return "rentPriceMonth"+"_sort";
        } else if (orderType.equals(Constants.Search.ORDER_TYPE_AREA)) {
            return "fArea"+"_sort";
        } else if (orderType.equals(Constants.Search.ORDER_TYPE_PUBDATE)) {
            return "pubDate"+"_sort";
        } else {
            return null;
        }
    }
    
    /**
     * 获取排序字段
     * 
     * @param orderType
     * @return
     */
    private String getRoomOrderType(String orderType) {

        if (StringUtil.isEmpty(orderType)) {
            return null;
        }

        if (orderType.equals(Constants.Search.ORDER_TYPE_PRICE)) {
            return "rRentPriceMonth"+"_sort";
        } else if (orderType.equals(Constants.Search.ORDER_TYPE_AREA)) {
            return "rArea"+"_sort";
        } else if (orderType.equals(Constants.Search.ORDER_TYPE_PUBDATE)) {
            return "rpubdate"+"_sort";
        } else {
            return null;
        }
    }
    
    public static void getSpatial(BooleanQuery.Builder builder ,String location){
        SpatialContext ctx = SpatialContext.GEO;  
        int maxLevels = 11;    
        SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);     
        
        
        SpatialStrategy  strategy = new RecursivePrefixTreeStrategy(grid, "google");
        //获取用户当前位置坐标
        org.springframework.data.solr.core.geo.Point lo = SolrUtil.getPoint(location);
        Point pt = ctx.makePoint(lo.getY(),lo.getX()); 
        //Point pt = ctx.makePoint(40.01536,40.344478);
        //SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,  ctx.makeCircle(pt, DistanceUtils.dist2Degrees(3.0, DistanceUtils.EARTH_MEAN_RADIUS_KM)));  
        SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,  ctx.getShapeFactory().circle(lo.getY(), lo.getX(), DistanceUtils.dist2Degrees(3, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
        Query query = strategy.makeQuery(args);  
        builder.add(query, BooleanClause.Occur.MUST);  
    }
    
    public static List<HouseSearchResultInfo> getInfoList(PageUtil page,List<HouseSearchResultInfo> listInfo) {
        List<HouseSearchResultInfo> pageListInfo = new ArrayList<HouseSearchResultInfo>();
        int startIndex = page.getStartIndex();//索引开始位置
        int endIndex = page.getEndIndex();//索引结束位置
        int i;
        if(page.getPageNumflag()){//查询页未超限
            for (i = startIndex; i < endIndex;i++){
                pageListInfo.add(listInfo.get(i));
            }   
        }
        
        return pageListInfo;
    }
    
    public static List<HouseSearchResultInfo> getGroupInfoList(List<HouseSearchResultInfo> houseSearchResultInfoList){
        int maxSize = 30;
        List<HouseSearchResultInfo> groupInfoList = new ArrayList<HouseSearchResultInfo>();
        long time = System.currentTimeMillis(); 
        
        //1.集合group
        Map<String, List<HouseSearchResultInfo>> mapGroup = new LinkedHashMap<String, List<HouseSearchResultInfo>>();  
        GroupUtils.listGroup2Map(houseSearchResultInfoList, mapGroup, HouseSearchResultInfo.class, "getCompanyId");// 输入方法名        
        
        //2.循环遍历map 
        for(int i = 0;i<= maxSize;i++){
            Iterator<Map.Entry<String, List<HouseSearchResultInfo>>> it = mapGroup.entrySet().iterator(); 
            if(groupInfoList.size() == maxSize)
                break;
            while(it.hasNext()){  
                Map.Entry<String, List<HouseSearchResultInfo>> entry=it.next();  
                String key=entry.getKey();
                if(i >= mapGroup.get(key).size() || groupInfoList.size() == maxSize)
                    continue;
                HouseSearchResultInfo info = mapGroup.get(key).get(i);
                if(!info.getPic().equals(StringUtil.EMPTY) && checkImg(info.getWebPic()))
                	groupInfoList.add(info);  
            } 
        }
        long duration = System.currentTimeMillis() - time;  
        
        houseSearchResultInfoList.removeAll(groupInfoList);      
        groupInfoList.addAll(houseSearchResultInfoList);
        logger.info("分组执行时间：" + duration + "毫秒!");  
        return groupInfoList;
    }
    
    public  List<HouseSolrResult> queryHouse(HouseSearchDto houseSearchDto,List<String> agencyIdList,String queryType){
        houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_ENTIRE);
        IndexSearcher searcher = null;
        Query query = null ;
        searcher = SearcherFactory.getSearcher("house");
        query = getHouseAllByMultiCondition(houseSearchDto, agencyIdList,queryType);
        return searchHouseCall(searcher,query,houseSearchDto); 
    }
    
    public  List<RoomSolrResult> queryRoom(HouseSearchDto houseSearchDto,List<String> agencyIdList,String queryType){
        houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_SHARE);
        IndexSearcher searcher = null;
        Query query = null ;
        searcher = SearcherFactory.getSearcher("room");
        query = getRoomAllByMultiCondition(houseSearchDto, agencyIdList,queryType);
        return searchRoomCall(searcher,query,houseSearchDto);
    }
    
    public static Boolean checkImg(List<String> imgList){
         String distinkImg ="http://cdn.mse.mlwplus.com/default/room_default/room_default_l.png";
         if(imgList.contains(distinkImg)){
             return false;
         }
        return true;
    }
    
    public static Boolean checkImg(String imgHome){
        String distinkImg ="http://cdn.mse.mlwplus.com/default/room_default/room_default_l.png";
        if(imgHome.equals(distinkImg)){
            return false;
        }
       return true;
   }
}