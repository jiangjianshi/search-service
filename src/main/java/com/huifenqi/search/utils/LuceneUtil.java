/** 
 * Project Name: hzf_platform_project 
 * File Name: SolrUtil.java 
 * Package Name: com.huifenqi.hzf_platform.utils 
 * Date: 2016年5月17日下午6:29:49 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.search.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.data.solr.core.geo.Point;
import org.springframework.data.solr.core.query.Criteria;
import com.huifenqi.search.context.Constants;
import com.huifenqi.search.context.dto.request.house.HouseSearchDto;
import com.huifenqi.search.context.enums.ApproveStatusEnum;

/**
 * @version
 * @since JDK 1.8
 */
public class LuceneUtil {

    private static final Log logger = LogFactory.getLog(LuceneUtil.class);
    
    
    /**
     * 添加区域条件
     * 
     * @param query
     * @param value
     */
    public static void addRegionCriteria(BooleanQuery.Builder  builder, String propertyName, String value) {
        
        
        
        Object low = getLowValue(value);
        Object high = getHighValue(value);
        
        Query criteria = getRegionCriteria(propertyName, low, high);
        
      
        if (!criteria.toString().equals("")) {
            builder.add(criteria, BooleanClause.Occur.MUST);
        }
        
    }
    
    /**
     * 获取区间较小值
     * 
     * @param region
     * @return
     */
    public static Object getLowValue(String region) {
        
        if (StringUtil.isEmpty(region)) {
            return null;
        }
        int leftIndex = region.indexOf(StringUtil.LEFT_SQUARE_BRACKET);
        int middleIndex = region.indexOf(StringUtil.COMMA);
        if (leftIndex >= 0 && middleIndex > leftIndex) {
            String left = region.substring(leftIndex + 1, middleIndex);
            if (StringUtil.STAR.equals(left)) {
                return null;
            } else {
                return left;
            }
        }

        return null;
    }

    /**
     * 获取区间较大值
     * 
     * @param region
     * @return
     */
    public static Object getHighValue(String region) {
        if (StringUtil.isEmpty(region)) {
            return null;
        }
        int middleIndex = region.indexOf(StringUtil.COMMA);
        int rightIndex = region.indexOf(StringUtil.RIGHT_SQUARE_BRACKET);
        if (middleIndex >= 0 && rightIndex > middleIndex) {
            String right = region.substring(middleIndex + 1, rightIndex);
            if (StringUtil.STAR.equals(right)) {
                return null;
            } else {
                return right;
            }
        }

        return null;
    }
    
    
    
    /**
     * 获取区间查询条件
     * 
     * @param propertyName
     * @param low
     * @param high
     * @return
     */
    public static Query getRegionCriteria(String propertyName, Object low, Object high) {
        
        Query query=null;
        if (StringUtil.isEmpty(propertyName)) {
            return null;
        }
        
        if (low == null) { // low 为空
            if (high == null) {
                
                query =LongPoint.newRangeQuery(propertyName, Long.MIN_VALUE, Long.MAX_VALUE);
            } else {
                
                query = LongPoint.newRangeQuery(propertyName, Long.MIN_VALUE, new  Long( (String)high).longValue());
            }
        } else {
            if (high == null) {
                
                query = LongPoint.newRangeQuery(propertyName, new Long((String)low).longValue(), Long.MAX_VALUE);
                
            } else {
            
                query = LongPoint.newRangeQuery(propertyName, new Long((String)low).longValue(), new Long((String)high).longValue());
            }
        }
            
        
        return query;
    }

    /**
     * 获取朝向条件
     * 
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addOrientationCriteria(BooleanQuery.Builder  builder, String orientations, String propertyName) {
        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        
        String[] orientationArr = orientations.split(StringUtil.COMMA);
        if (orientationArr == null || orientationArr.length == 0) {
            return;
        }
        
    
        for (String orientation : orientationArr) { 
            Query query=new TermQuery(new Term(propertyName,orientation));
            tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
            
        }
        
        BooleanQuery tmpQuery=tmpbuilder.build();
        if (!tmpQuery.toString().equals("")) {
            builder.add(tmpQuery, BooleanClause.Occur.MUST);
        }
    }
    
    
    
    /**
     * 获取距离条件
     * 
     * @param query
     * @param value
     */
    /*
    public static void addDistanceCriteria(BooleanQuery.Builder  builder, String propertyName, String locationValue, int distanceValue) {
        
    
        if (StringUtil.isEmpty(propertyName)) {
            return;
        }
        
        Point location = getPoint(locationValue);
        if (location == null) {
            logger.error(LogUtils.getCommLog("坐标解析失败, locationValue:" + locationValue));
            return;
        }

        double locationDouble = (double) distanceValue;
        locationDouble = locationDouble / 1000; // 米转千米
        Distance distance = new Distance(locationDouble);
        Criteria criteria = new Criteria(propertyName).within(location, distance);
        
        if (criteria != null) {
            query.addCriteria(criteria);
        }

        if (query instanceof SimpleQuery) {
            SimpleQuery simpleQuery = (SimpleQuery) query;
            simpleQuery.addProjectionOnField("*");
            simpleQuery.addProjectionOnField("_dist_:" + getGeoDistStr(propertyName, location.getX(), location.getY()));
        }

    }
      
      */
    /**
     * 获取距离字符串
     * 
     * @param propertyName
     * @param x
     * @param y
     * @return
     */
    public static String getGeoDistStr(String propertyName, double x, double y) {
        if (StringUtil.isEmpty(propertyName)) {
            return null;
        }
        return "geodist(" + propertyName + "," + x + "," + y + ")";
    }

    /**
     * 获取距离字符串
     * 
     * @param houseSearchDto
     * @return
     */
    public static String getLocation(HouseSearchDto houseSearchDto) {
        if (houseSearchDto == null) {
            return null;
        }

        String location = null;

        String currentLocation = houseSearchDto.getLocation();
        String stationPosition = houseSearchDto.getStationLocation();
        if (StringUtil.isNotEmpty(currentLocation)) { // 当前位置
            location = currentLocation;
        } else if (StringUtil.isNotEmpty(stationPosition)) { // 地铁位置
            location = stationPosition;
        }

        return location;
    }
    

    /**
     * 获取坐标
     * 
     * @param locationValue
     *            传入参数先经度后纬度
     * @return
     */
    public static Point getPoint(String locationValue) {
        if (StringUtil.isEmpty(locationValue)) {
            return null;
        }
        String[] split = locationValue.split(StringUtil.COMMA);
        if (split == null || split.length != 2) {
            return null;
        }
        String positionY = split[0];
        String positionX = split[1];
        Double x = StringUtil.parseDouble(positionX);
        if (x == null) {
            return null;
        }
        Double y = StringUtil.parseDouble(positionY);
        if (y == null) {
            return null;
        }
        Point location = new Point(x, y);
        return location;
    }

    

    /**
     * 获取标签类条件
     * 
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addTagCriteria(BooleanQuery.Builder  builder, String tag, String propertyName) {
        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        
        if (StringUtil.isEmpty(propertyName)) {
            return;
        }
        
        
        List<Query> houseTagCriteriaList = getTagCriteriaList(tag, propertyName);
        if (CollectionUtils.isEmpty(houseTagCriteriaList)) {
            return;
        }
        for (Query criteria : houseTagCriteriaList) {
            tmpbuilder.add(criteria, BooleanClause.Occur.MUST);
        }
        
        BooleanQuery tmpQuery=tmpbuilder.build();
        if(!tmpQuery.toString().equals(""))
            builder.add(tmpQuery,BooleanClause.Occur.MUST);
        
    }

    
    /**
     * 获取标签条件
     * 
     * @param tag
     * @param propertyName
     * @return
     */
    public static List<Query> getTagCriteriaList(String tag, String propertyName) {
        if (StringUtil.isEmpty(tag)) {
            return null;
        }
        if (StringUtil.isEmpty(propertyName)) {
            return null;
        }
        String[] split = tag.split(StringUtil.COMMA);
        if (split == null || split.length == 0) {
            return null;
        }

        List<Query> houseTagCriteriaList = new ArrayList<Query>();
        for (String value : split) {
            Query houseTagCriteria = getSingleTagCriteria(value, propertyName);
            if (houseTagCriteria != null) {
                houseTagCriteriaList.add(houseTagCriteria);
            }
        }
        return houseTagCriteriaList;
    }
    
    /**
     * 获取单个房源标签条件
     * 
     * @param singleTag
     * @param propertyName
     * @return
     */
    public static Query getSingleTagCriteria(String singleTag, String propertyName) {
        
        
        if (StringUtil.isEmpty(singleTag)) {
            return null;
        }
        if (StringUtil.isEmpty(propertyName)) {
            return null;
        }
        
        Query query=new TermQuery(new Term(propertyName,singleTag));
        
        
        return query;
    }
    
    
    
    
    
    /**
     * 不包含该属性
     * @param query
     * @param propertyName
     * @param statusList
     */
    public static void addNotStatusCriteria(BooleanQuery.Builder  builder, String propertyName, List<String> statusList) {

        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        for (String status : statusList) {
            
            Query query=new TermQuery(new Term(propertyName,status));
            tmpbuilder.add(query, BooleanClause.Occur.MUST_NOT);
        }
        BooleanQuery tmpQuery=tmpbuilder.build();
        if(!tmpQuery.toString().contentEquals(""))
            builder.add(tmpQuery,BooleanClause.Occur.MUST);
        

    }
    
    
    
    /**
     * 获取品牌公寓筛选条件
     * 
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addCompanyCriteria(BooleanQuery.Builder  builder, String companyIds, String propertyName, List<String> agencyIdList) {
        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        

        if (!"-1".equals(companyIds)) {
            if (StringUtil.isNotEmpty(companyIds)) {
                String[] companyIdArr = companyIds.split(",");
                if (companyIdArr == null || companyIdArr.length == 0) {
                    return;
                }
                for (String companyId : companyIdArr) {
                    
                        Query query=new TermQuery(new Term(propertyName,new Integer(companyId).toString()));
                        tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                    
                }
            }
            
        } else if ("-1".equals(companyIds)) {
            
            if (CollectionUtils.isNotEmpty(agencyIdList)) {
                for (String agencyId : agencyIdList) {
            
                    Query query=new TermQuery(new Term(propertyName,new Integer(agencyId).toString()));
                    tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                    
                }
            } else {
                return;
            }
        }
        
        
        
        BooleanQuery tmpQuery=tmpbuilder.build();
        if (!tmpQuery.toString().contentEquals("") ) {
            builder.add(tmpQuery, BooleanClause.Occur.MUST);
        }
    
    }

    
    
    
    
    /**
     * @Title: addHouseStyleCriteria
     * @Description: 添加房源查询条件
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addHouseStyleCriteria(BooleanQuery.Builder  builder , String eBedRoomNums, String sBedRoomNums, String propertyNameE, String propertyNameS) {
        BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
        BooleanQuery subtmpQuery = null;
        
        //1.拼接自在整租居室
        if (StringUtil.isNotEmpty(eBedRoomNums)) {  
            String[] a = eBedRoomNums.split(",");
            //1.1数组的第一个元素如果是-1，表示自在整租居室数量不限制
            if (Integer.parseInt(a[0]) != -1) {
                for (String string : a) {
                    //1.2拼接居室条件
                    Query queryBedNums=IntPoint.newRangeQuery(propertyNameE, Integer.valueOf(string), Integer.valueOf(string));
                    //Query queryBedNums=new TermQuery(new Term(propertyNameE,string));
                    subtmpbuilder.add(queryBedNums, BooleanClause.Occur.SHOULD);
                }
            }
        
        } else if (StringUtil.isEmpty(eBedRoomNums) && StringUtil.isNotEmpty(sBedRoomNums)) {//未选择房型-居室条件
            Query queryEmpty=IntPoint.newRangeQuery(propertyNameE, -999, -999);
            //Query queryEmpty=new TermQuery(new Term(propertyNameE,"100"));
            subtmpbuilder.add(queryEmpty, BooleanClause.Occur.MUST);
            
             
        }
        
        subtmpQuery=subtmpbuilder.build();
        logger.info("sbutmpQuery:"+subtmpQuery.toString());
        if(!subtmpQuery.toString().equals(""))
            builder.add(subtmpQuery,BooleanClause.Occur.MUST);
    
    }
    /**
     * @Title: addRoomStyleCriteria
     * @Description: 添加房间查询条件
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addRoomStyleCriteria(BooleanQuery.Builder  builder, String sBedRoomNums, String eBedRoomNums, String propertyNameE, String propertyNameS) {
        BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
        BooleanQuery subtmpQuery = null;
        
        if (StringUtil.isNotEmpty(sBedRoomNums)) {
            String[] a = sBedRoomNums.split(",");
            // 数组的第一个元素如果是-1，表示优选合租居室数量不限制
            if (Integer.parseInt(a[0]) != -1) {
                for (String string : a) {
                        if (Integer.parseInt(string) > 3) {
                            Query queryBedNums=IntPoint.newRangeQuery(propertyNameE, 4, Integer.valueOf(string));
                            subtmpbuilder.add(queryBedNums, BooleanClause.Occur.SHOULD);
                        } else {
                          //1.2拼接居室条件
                            Query queryBedNums=IntPoint.newRangeQuery(propertyNameE,Integer.valueOf(string), Integer.valueOf(string));
                       
                            subtmpbuilder.add(queryBedNums, BooleanClause.Occur.SHOULD);
                        }
                    }
            }

        
        } else if (StringUtil.isEmpty(sBedRoomNums) && StringUtil.isNotEmpty(eBedRoomNums)) {
            Query queryEmpty=IntPoint.newRangeQuery(propertyNameE, -999, -999);
        
            subtmpbuilder.add(queryEmpty, BooleanClause.Occur.MUST);
            
      
        }
        
        subtmpQuery=subtmpbuilder.build();
        logger.info("sbutmpQuery:"+subtmpQuery.toString());
        if(!subtmpQuery.toString().equals(""))
            builder.add(subtmpQuery,BooleanClause.Occur.MUST);
    }
    
    /**
     * @Title: addHouseStyleCriteria
     * @Description: 添加房源查询条件
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addHousesAllStyleCriteria(BooleanQuery.Builder  builder, String eBedRoomNums, String sBedRoomNums,
            String propertyName1, String propertyName2) {

        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        
        
        if (StringUtil.isNotEmpty(eBedRoomNums)) {
            
            BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
            BooleanQuery subtmpQuery=null;
            
            String[] a = eBedRoomNums.split(",");
            // 数组的第一个元素如果是-1，表示自在整租居室数量不限制
            if (Integer.parseInt(a[0]) != -1) {
                for (String string : a) {
                    
                    Query query1=new TermQuery(new Term(propertyName1,string));
                    subtmpbuilder.add(query1, BooleanClause.Occur.SHOULD);
                    
                }
            }
            
            Query query2=new TermQuery(new Term(propertyName2,new Integer(Constants.HouseDetail.RENT_TYPE_ENTIRE).toString()));
            subtmpbuilder.add(query2, BooleanClause.Occur.MUST);
            subtmpQuery=subtmpbuilder.build();
            
            tmpbuilder.add(subtmpQuery,BooleanClause.Occur.SHOULD);
            
        }
        
        
        
        if (StringUtil.isNotEmpty(sBedRoomNums)) {
            
            BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
            BooleanQuery subtmpQuery=null;
            
            String[] a = sBedRoomNums.split(",");
            // 数组的第一个元素如果是-1，表示优选合租居室数量不限制
            if (Integer.parseInt(a[0]) != -1) {
                
                for (String string : a) {
                    
                    if (Integer.parseInt(string) > 3) {
                        
                        Query query1=IntPoint.newRangeQuery(propertyName1, Integer.parseInt(string), Integer.MAX_VALUE);
                        subtmpbuilder.add(query1, BooleanClause.Occur.SHOULD);
                        
                    } else {
                        
                        Query query1=new TermQuery(new Term(propertyName1,string));
                        subtmpbuilder.add(query1, BooleanClause.Occur.SHOULD);
                    }
                
                    Query query1=new TermQuery(new Term(propertyName1,string));
                    subtmpbuilder.add(query1, BooleanClause.Occur.SHOULD);
                
                }
            }
            
            
            Query query2=new TermQuery(new Term(propertyName2,new Integer(Constants.HouseDetail.RENT_TYPE_SHARE).toString()));
            subtmpbuilder.add(query2, BooleanClause.Occur.MUST);
            subtmpQuery=subtmpbuilder.build();
            tmpbuilder.add(subtmpQuery,BooleanClause.Occur.SHOULD);
            
        }
    
        if(!tmpbuilder.build().toString().contentEquals(""))
            builder.add(tmpbuilder.build(),BooleanClause.Occur.MUST);
    
        
    }
    
    
    
    /**
     * 获取房源状态条件
     * 
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addHouseStatusCriteria(BooleanQuery.Builder  builder, String propertyName) {
        
        if (StringUtil.isEmpty(propertyName)) {
            return;
        }

        List<Integer> list = new ArrayList<Integer>();
        list.add(Constants.HouseBase.STATUS_NEW);
//      list.add(Constants.HouseBase.STATUS_PRE_VERIFY);
//      list.add(Constants.HouseBase.STATUS_VERIFY_SUCCESS);
//      list.add(Constants.HouseBase.STATUS_VERIFY_FAIL);
        list.add(Constants.HouseBase.STATUS_PARTLY_RENT); // 部分出租

        addStatusCriteria(builder, propertyName, list);

    }
    
    
    /**
     * 包含该属性
     * @param query
     * @param propertyName
     * @param statusList
     */
    public static void addStatusCriteria(BooleanQuery.Builder  builder, String propertyName, List<Integer> statusList) {
        
        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        
        if (StringUtil.isEmpty(propertyName)) {
            return;
        }
        if (CollectionUtils.isEmpty(statusList)) {
            return;
        }
        
    
        for (Integer status : statusList) {
        
            Query query=new TermQuery(new Term(propertyName,new Integer(status).toString()));
            tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
        }
      
        
        BooleanQuery tmpQuery=tmpbuilder.build();
        if (!tmpQuery.toString().equals("")) {
            builder.add(tmpQuery, BooleanClause.Occur.MUST);
        }
        
    }
    
    
    /**
     * 获取审核状态条件
     * @param query
     * @param propertyName
     */
    public static void addHouseApproveStatusCriteria(BooleanQuery.Builder  builder, String propertyName) {
        
        if (StringUtil.isEmpty(propertyName)) {
            return;
        }
        List<Integer> list = new ArrayList<Integer>();
        list.add(ApproveStatusEnum.SYS_APP_PASS.getCode());
        list.add(ApproveStatusEnum.IMG_APP_PASS.getCode());
        addStatusCriteria(builder, propertyName, list);
        
    }
    
    
    /**
     * 获取付款方式条件
     * 
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addPayTypeCriteria(BooleanQuery.Builder  builder, String payType, String propertyName1, String propertyName2) {
        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        BooleanQuery tmpQuery=null;
    
        
        String[] type = payType.split(",");
        for (String typeCode : type) {
            
            if (Constants.payType.NO_DEPOSIT == Integer.parseInt(typeCode)) {// 免押金
                
                
                BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
                BooleanQuery subtmpQuery=null;
            
                Query query1=new TermQuery(new Term(propertyName1,"0"));
                subtmpbuilder.add(query1, BooleanClause.Occur.MUST);
                
                //这个可以去掉  0，1，2，3，6   majianchun
                //Query query2=IntPoint.newRangeQuery(propertyName2, Integer.MIN_VALUE, 6);
                //subtmpbuilder.add(query2, BooleanClause.Occur.MUST);
                
                subtmpQuery=subtmpbuilder.build();
                
                tmpbuilder.add(subtmpQuery, BooleanClause.Occur.SHOULD);
                
                
                
            } else if (Constants.payType.DEPOSIT_ONE_PAY_ONE == Integer.parseInt(typeCode)) {// 押一付一
                
                BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
                BooleanQuery subtmpQuery=null;
            
                Query query1=new TermQuery(new Term(propertyName1,"1"));
                subtmpbuilder.add(query1, BooleanClause.Occur.MUST);
                
                
                Query query2=new TermQuery(new Term(propertyName2,"1"));
                subtmpbuilder.add(query2, BooleanClause.Occur.MUST);
                
                subtmpQuery=subtmpbuilder.build();
                
                tmpbuilder.add(subtmpQuery, BooleanClause.Occur.SHOULD);
            
                
            } else if (Constants.payType.DEPOSIT_ONE_PAY_THREE == Integer.parseInt(typeCode)) {// 押一付三
                
                
                BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
                BooleanQuery subtmpQuery=null;
            
                Query query1=new TermQuery(new Term(propertyName1,"1"));
                subtmpbuilder.add(query1, BooleanClause.Occur.MUST);
                
                
                Query query2=new TermQuery(new Term(propertyName2,"3"));
                subtmpbuilder.add(query2, BooleanClause.Occur.MUST);
                
                subtmpQuery=subtmpbuilder.build();
                
                tmpbuilder.add(subtmpQuery, BooleanClause.Occur.SHOULD);
                
                
            
                
                
                
            } else if (Constants.payType.YEAR_PAY == Integer.parseInt(typeCode)) {// 半年/年付
                
                BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
                BooleanQuery subtmpQuery=null;
            
                Query query1=new TermQuery(new Term(propertyName2,"6"));
                subtmpbuilder.add(query1, BooleanClause.Occur.SHOULD);
                
                
                Query query2=new TermQuery(new Term(propertyName2,"12"));
                subtmpbuilder.add(query2, BooleanClause.Occur.SHOULD);
                
                subtmpQuery=subtmpbuilder.build();
                
                tmpbuilder.add(subtmpQuery, BooleanClause.Occur.SHOULD);
                
                
            } else if (Constants.payType.MONTH_PAY == Integer.parseInt(typeCode)) {// 1:支持月付；0:不支持月付
                
                BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
                BooleanQuery subtmpQuery=null;
            
                Query query1=new TermQuery(new Term("isPayMonth","1"));
                subtmpbuilder.add(query1, BooleanClause.Occur.MUST);
                
                subtmpQuery=subtmpbuilder.build();
                
                tmpbuilder.add(subtmpQuery, BooleanClause.Occur.SHOULD);    
                
            }
        }
        
        
        tmpQuery=tmpbuilder.build();
        if (!tmpQuery.toString().equals("")) {
            builder.add(tmpQuery,BooleanClause.Occur.MUST);
        }
    }
    
    
    /**
     * 是否查询品牌公寓房源（1：品牌公寓房源；2：非品牌公寓房源）
     * 
     * @param query
     * @param tag
     * @param propertyName
     */
    public static void addCompanyQueryCriteria(BooleanQuery.Builder  builder, String companyType, String propertyName, List<String> agencyIdList) {
        
        
        String[] companyTypeArr = companyType.split(",");
                
        BooleanQuery.Builder  subtmpbuilder=new BooleanQuery.Builder();
        BooleanQuery subtmpQuery=null;
        
        
        if (companyTypeArr.length == 1) {
            
            if (Integer.parseInt(companyTypeArr[0]) == 1) {
                
                if (CollectionUtils.isNotEmpty(agencyIdList)) {
                    
                    for (String agencyId : agencyIdList) {
                        
                        Query query=new TermQuery(new Term(propertyName,agencyId));
                        subtmpbuilder.add(query, BooleanClause.Occur.SHOULD);
                        
                    }
                } else {
                    return;
                }
                
            } else if (Integer.parseInt(companyTypeArr[0]) == 2) {
                
                if (CollectionUtils.isNotEmpty(agencyIdList)) {
                    
                    for (String agencyId : agencyIdList) {
                        
                        Query query=new TermQuery(new Term(propertyName,agencyId));
                        subtmpbuilder.add(query, BooleanClause.Occur.MUST_NOT);
                    }
                    
                } else {
                    return;
                }
            }
        }
        
        subtmpQuery=subtmpbuilder.build();
        if(!subtmpQuery.toString().equals(""))
        {
            builder.add(subtmpQuery, BooleanClause.Occur.MUST);
        }
    
    }
    

    /**
     * 包含该属性
     * @param query
     * @param propertyName
     * @param statusList
     */
    public static void addCollectCriteria(BooleanQuery.Builder  builder, String propertyName, List<String> collectIdList) {
        
        
        BooleanQuery.Builder  tmpbuilder=new BooleanQuery.Builder();
        
        if (StringUtil.isEmpty(propertyName)) {
            return;
        }
        if (CollectionUtils.isEmpty(collectIdList)) {
            return;
        }
        
        for (String collectId : collectIdList) {
        
            Query query=new TermQuery(new Term(propertyName,collectId.toLowerCase()));
            tmpbuilder.add(query, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery tmpQuery=tmpbuilder.build();
        if (!tmpQuery.toString().equals("")) {
            builder.add(tmpQuery, BooleanClause.Occur.MUST);
        }
        
    }
}
