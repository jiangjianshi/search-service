/**
 * Project Name: hzf_platform
 * File Name: HouseRequestHandler.java
 * Package Name: com.huifenqi.hzf_platform.handler
 * Date: 2016年4月26日下午4:40:45
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved.
 *
 */
package com.huifenqi.search.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.huifenqi.search.context.dto.response.house.HouseQueryDto;
import com.huifenqi.search.context.entity.house.FootmarkHistory;
import com.huifenqi.search.context.exception.BaseException;
import com.huifenqi.search.utils.SessionManager;
import com.huifenqi.search.utils.Configuration;
import com.huifenqi.search.comm.LockManager;
import com.huifenqi.search.context.Constants;
import com.huifenqi.search.context.dto.request.house.HouseSearchDto;
import com.huifenqi.search.context.dto.response.house.HouseSearchResultDto;
import com.huifenqi.search.context.dto.response.house.HouseSearchResultInfo;
import com.huifenqi.search.context.entity.house.Agency;
import com.huifenqi.search.context.entity.house.HouseCollection;
import com.huifenqi.search.context.entity.location.Subway;
import com.huifenqi.search.context.entity.platform.PlatformCustomer;
import com.huifenqi.search.context.exception.ErrorMsgCode;
import com.huifenqi.search.context.exception.InvalidParameterException;
import com.huifenqi.search.context.response.Responses;
import com.huifenqi.search.dao.HouseInfoDao;
import com.huifenqi.search.dao.HouseNewDao;
import com.huifenqi.search.dao.repository.house.AgencyManageRepository;
import com.huifenqi.search.dao.repository.house.FootmarkHistoryRepository;
import com.huifenqi.search.dao.repository.house.HouseCollectionRepository;
import com.huifenqi.search.dao.repository.location.SubwayRepository;
import com.huifenqi.search.dao.repository.platform.PlatformCustomerRepository;
import com.huifenqi.search.utils.GsonUtils;
import com.huifenqi.search.utils.HouseUtil;
import com.huifenqi.search.utils.LogUtils;
import com.huifenqi.search.utils.RequestUtils;
import com.huifenqi.search.utils.ResponseUtils;
import com.huifenqi.search.utils.RulesVerifyUtil;
import com.huifenqi.search.utils.StringUtil;


/**
 * ClassName: HouseRequestHandler date: 2016年4月26日 下午4:40:45 Description:
 *
 * @author changmingwei
 * @version
 * @since JDK 1.8
 */
@Service
public class HouseRequestHandler {

    private static final Log logger = LogFactory.getLog(HouseRequestHandler.class);

    @Autowired
    private HouseNewDao houseNewDao;

    @Autowired
    private SubwayRepository subwayRepository;
    
    @Autowired
    private AgencyManageRepository agencyManageRepository;
    
    @Autowired
    PlatformCustomerRepository platformCustomerRepository;
    
    @Autowired
    HouseCollectionRepository houseCollectionRepository;
    
    @Autowired
    FootmarkHistoryRepository footmarkHistoryRepository;
    
 
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private Configuration configuration;
    
    @Autowired
    private LockManager lockManager;
    
    @Autowired
    private HouseInfoDao houseInfoDao;
    
    public static final String FOOTMARK = "footmark";
    /**
     * 查询房源信息
     *
     * @return
     */
    public Responses getHouseInfo(HttpServletRequest request) throws Exception {
        final String sellId = RequestUtils.getParameterString(request, "sellId");
        final int roomId = RequestUtils.getParameterInt(request, "roomId", 0);
        final String platform = RequestUtils.getParameterString(request, "platform", null);
        final String sessionId = RequestUtils.getParameterString(request, "sid", null);
        long userId = 0;

        //判断用户是否登录      
        if (!StringUtil.isEmpty(platform) && !StringUtil.isEmpty(sessionId)) {
            userId = sessionManager.getUserIdFromSession();
        }
        final long userId2 = userId;
        
        HouseQueryDto houseDto = houseInfoDao.getHouseQueryDto(sellId, roomId, userId);
         if (houseDto == null) {
            return new Responses(ErrorMsgCode.ERROR_MSG_QUERY_HOUSE_FAIL, "房源不存在");
        }

        String companyIds = configuration.searchAgencyId;
        if (!companyIds.contains(houseDto.getCompanyId())) {
            houseDto.setCompanyName("");
        }
        //查询同小区的房源数量
        HouseSearchDto houseSearchDto = new HouseSearchDto();
        houseSearchDto.setCityId(houseDto.getCityId());
        houseSearchDto.setCommunityName(houseDto.getCommunityName());
        houseSearchDto.setOrderType("price");
        Responses res = searchHouseList(houseSearchDto);
        houseDto.setCommunityHouseCount(res.getMeta().getTotalRows());
        Responses responses = new Responses(houseDto);
        ResponseUtils.fillRow(responses.getMeta(), 1, 1);

        // 添加浏览房源足迹记录(请求参数包括：房源字段必填，房间字段(存在 浏览的是房间，否则浏览的是房源)，用户ID)
        if (userId2 > 0) {
            new Thread(new Runnable() {
                String lockResource = FOOTMARK + String.valueOf(userId2);
                // 获取锁
                boolean lock = lockManager.lock(lockResource);

                @Override
                public void run() {
                    try {
                        if (lock) {
                            // 先查询当前用户的houseId和homeId下是否存在浏览房源足迹记录，如果存在
                            // 执行更新；否则：判断条数是否大于20条，如果不大于 执行插入；否则 执行更新最旧的一条数据
                            List<FootmarkHistory> footmarkHistoryList = new ArrayList<FootmarkHistory>();
                            FootmarkHistory footmarkHistory = null;
                            try {
                                footmarkHistoryList = houseInfoDao.getFootmarkHistory(userId2, sellId,
                                        new Long(roomId).intValue());
                                if (CollectionUtils.isNotEmpty(footmarkHistoryList)) {
                                    footmarkHistory = footmarkHistoryList.get(0);
                                }
                            } catch (Exception e) {
                                logger.error(LogUtils.getCommLog("数据解析失败" + e.getMessage()));
                                if (e instanceof BaseException) {
                                    throw (BaseException) e;
                                }
                            }
                            if (footmarkHistory != null) {
                                // 执行更新：更新时间操作
                                try {
                                    houseInfoDao.updateFootmarkHistory(footmarkHistory.getId(), userId2, sellId,
                                            new Long(roomId).intValue());
                                } catch (Exception e) {
                                    logger.error(LogUtils.getCommLog("浏览房源足迹记录更新失败" + e.getMessage()));
                                    throw new BaseException(ErrorMsgCode.ERROR_FMH_ADD_FAIL, "浏览房源足迹记录更新失败");
                                }
                            } else {
                                // 判断条数是否大于20条，如果小于 执行插入；否则 执行更新最旧的一条数据
                                List<FootmarkHistory> footmarkList = houseInfoDao.getCountByUserId(userId2);
                                if (CollectionUtils.isEmpty(footmarkList) || footmarkList.size() < 20) {
                                    footmarkHistory = new FootmarkHistory();
                                    footmarkHistory.setUserId(userId2);
                                    footmarkHistory.setSellId(sellId);
                                    footmarkHistory.setRoomId(new Long(roomId).intValue());
                                    footmarkHistory.setState(1);
                                    try {
                                        houseInfoDao.saveFootmarkHistory(footmarkHistory);
                                    } catch (Exception e) {
                                        logger.error(LogUtils.getCommLog("浏览房源足迹记录保存失败" + e.getMessage()));
                                        throw new BaseException(ErrorMsgCode.ERROR_FMH_UPDATE_FAIL, "浏览房源足迹记录保存失败");
                                    }
                                } else {
                                    houseInfoDao.updateFootmarkHistory(footmarkList.get(0).getId(), userId2, sellId,
                                            new Long(roomId).intValue());
                                    try {
                                    } catch (Exception e) {
                                        logger.error(LogUtils.getCommLog("浏览房源足迹记录更新失败" + e.getMessage()));
                                        throw new BaseException(ErrorMsgCode.ERROR_FMH_ADD_FAIL, "浏览房源足迹记录更新失败");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(LogUtils.getCommLog("浏览房源足迹记录更新失败" + e.getMessage()));
                        throw new BaseException(ErrorMsgCode.ERROR_FMH_ADD_FAIL, "浏览房源足迹记录更新失败");
                    } finally {
                        if (lock) {// 释放锁
                            lockManager.unLock(lockResource);
                        }
                    }
                }
            }).start();
        }

        return responses;

    }

    /**
     * 条件筛选 搜索房源
     * 
     * @return
     */
    public Responses searchHouseList(HttpServletRequest request) throws Exception {
        HouseSearchDto houseSearchDto = getHouseSearchDto(request);
        return searchHouseList(houseSearchDto);
        
    }

    /**
     * 条件筛选 搜索房源
     * 
     * @return
     */
    public Responses getCollectFoot(HttpServletRequest request) throws Exception {
        HouseSearchDto houseSearchDto = getHouseSearchDto(request);
        return getCollectFoot(houseSearchDto);
        
    }
    

    private HouseSearchDto getHouseSearchDto(HttpServletRequest request) throws Exception {

        if (request == null) {
            return null;
        }

        HouseSearchDto houseSearchDto = new HouseSearchDto();
        
        
        //新加  majianchun  2.11  这个必须 请求里传过来
        long userId= RequestUtils.getParameterInt(request, "userId",0);
        houseSearchDto.setUserId(userId);        
        

        int appId = RequestUtils.getParameterInt(request, "appId");
        houseSearchDto.setAppId(appId);

        // 店铺页中介公司ID
        String companyId = RequestUtils.getParameterString(request, "companyId", StringUtil.EMPTY);
        houseSearchDto.setCompanyId(companyId);

        // 只有cityId是必选 ！！！ 去掉cityId必传属性
        long cityId = RequestUtils.getParameterLong(request, "cityId", 0);
        if (cityId != 0) {
            houseSearchDto.setCityId(cityId);
        }

        String keyword = RequestUtils.getParameterString(request, "q", StringUtil.EMPTY);
        houseSearchDto.setKeyword(keyword);
        logger.info("q:"+keyword);
        
        

        long disId = RequestUtils.getParameterLong(request, "disId", 0);
        houseSearchDto.setDistrictId(disId);

        long bizId = RequestUtils.getParameterLong(request, "bizId", 0);
        houseSearchDto.setBizId(bizId);

        long lineId = RequestUtils.getParameterLong(request, "lineId", 0);
        houseSearchDto.setLineId(lineId);

        long stationId = RequestUtils.getParameterLong(request, "stationId", 0);
        houseSearchDto.setStationId(stationId);

        String price = RequestUtils.getParameterString(request, "price", StringUtil.EMPTY);
        checkRegionNumber(price, "price");
        houseSearchDto.setPrice(price);

        int orientation = RequestUtils.getParameterInt(request, "orientation", 0);
        if (orientation != 0) {
            checkOrientation(orientation, "orientation");
        }
        houseSearchDto.setOrientation(orientation);

        String area = RequestUtils.getParameterString(request, "area", StringUtil.EMPTY);
        checkRegionNumber(area, "area");
        houseSearchDto.setArea(area);

        String location = RequestUtils.getParameterString(request, "location", StringUtil.EMPTY);
        checkPosition(location, "location");
        houseSearchDto.setLocation(location);

        String distance = RequestUtils.getParameterString(request, "distance", StringUtil.EMPTY);
        checkRegionNumber(distance, "distance");
        houseSearchDto.setDistance(distance);

        String orderType = RequestUtils.getParameterString(request, "orderType", StringUtil.EMPTY);
        checkOrderType(orderType, "orderType");
        houseSearchDto.setOrderType(orderType);

        String order = RequestUtils.getParameterString(request, "order", StringUtil.EMPTY);
        checkOrder(order, "order");
        houseSearchDto.setOrder(order);

        int pageNum = RequestUtils.getParameterInt(request, "pageNum", 1);
        checkNonNegativeNumber(pageNum, "pageNum");
        houseSearchDto.setPageNum(pageNum);

        int pageSize = RequestUtils.getParameterInt(request, "pageSize", 10);
        checkNonNegativeNumber(pageSize, "pageSize");
        checkPageSize(pageSize, "pageSize");
        houseSearchDto.setPageSize(pageSize);

        // 整租/分租,可选，默认全部
        int entireRent = RequestUtils.getParameterInt(request, "entireRent", Constants.HouseDetail.RENT_TYPE_ALL);
        checkEntireRent(entireRent, "entireRent");
        houseSearchDto.setEntireRent(entireRent);

        String bedroomNums = RequestUtils.getParameterString(request, "bedroomNums", StringUtil.EMPTY);
        checkRegionNumber(bedroomNums, "bedroomNums");
        houseSearchDto.setBedroomNum(bedroomNums);

        String houseTag = RequestUtils.getParameterString(request, "houseTag", StringUtil.EMPTY);
        checkSeparatedNumber(houseTag, "houseTag");
        houseSearchDto.setHouseTag(houseTag);

        String orientationStr = RequestUtils.getParameterString(request, "orientationStr", StringUtil.EMPTY);
        checkSeparatedNumber(orientationStr, "orientationStr");
        houseSearchDto.setOrientationStr(orientationStr);

        String eBedRoomNums = RequestUtils.getParameterString(request, "eBedRoomNums", "0");
        String sBedRoomNums = RequestUtils.getParameterString(request, "sBedRoomNums", "0");
        if (!"0".equals(eBedRoomNums) && !"0".equals(sBedRoomNums)) {
            houseSearchDto.seteBedRoomNums(eBedRoomNums);
            houseSearchDto.setsBedRoomNums(sBedRoomNums);
            houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_ALL);
        } else if (!"0".equals(eBedRoomNums)) {
            houseSearchDto.seteBedRoomNums(eBedRoomNums);
            houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_ENTIRE);
        } else if (!"0".equals(sBedRoomNums)) {
            houseSearchDto.setsBedRoomNums(sBedRoomNums);
            houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_SHARE);
        }

        String cBedRoomNums = RequestUtils.getParameterString(request, "cBedRoomNums", StringUtil.EMPTY);
        houseSearchDto.setcBedRoomNums(cBedRoomNums);

        String payType = RequestUtils.getParameterString(request, "payType", StringUtil.EMPTY);
        houseSearchDto.setPayType(payType);

        String sellerId = RequestUtils.getParameterString(request, "sellerId", StringUtil.EMPTY);
        if (StringUtil.isNotBlank(sellerId)) {
            houseSearchDto.setSellerId(sellerId);
        }

        String communityName = RequestUtils.getParameterString(request, "communityName", StringUtil.EMPTY);
        if (StringUtil.isNotBlank(communityName)) {
            houseSearchDto.setCommunityName(communityName);
        }

        int roomerId = RequestUtils.getParameterInt(request, "roomerId", 0);
        houseSearchDto.setRoomerId(roomerId);

        String companyType = RequestUtils.getParameterString(request, "companyType", StringUtil.EMPTY);
        houseSearchDto.setCompanyType(companyType);

        int isCollect = RequestUtils.getParameterInt(request, "isCollect", 0);
        houseSearchDto.setIsCollect(isCollect);
        
        int isFoot = RequestUtils.getParameterInt(request, "isFoot", 0);
        houseSearchDto.setIsFoot(isFoot);
        
        int isHomePage = RequestUtils.getParameterInt(request, "isHomePage", 0);
        houseSearchDto.setIsHomePage(isHomePage);
        
        return houseSearchDto;

    }

    /**
     * 校验区域字段
     *
     * @param regionNumber
     * @param keyName
     */
    private void checkRegionNumber(String regionNumber, String keyName) {
        if (StringUtil.isNotBlank(regionNumber)) {
            boolean valid = RulesVerifyUtil.verifyNumberRegion(regionNumber);
            if (!valid) {
                throw new InvalidParameterException("参数格式不正确:" + keyName);
            }
        }
    }

    /**
     * 校验坐标字段
     *
     * @param position
     * @param keyName
     */
    private void checkPosition(String position, String keyName) {
        if (StringUtil.isNotBlank(position)) {
            boolean valid = RulesVerifyUtil.verifyPosition(position);
            if (!valid) {
                throw new InvalidParameterException("参数格式不正确:" + keyName);
            }
        }
    }

    /**
     * 校验排序类型字段
     *
     * @param regionNumber
     * @param keyName
     */
    private void checkOrderType(String orderType, String keyName) {
        if (StringUtil.isNotBlank(orderType)) {
            if (!Constants.Search.containsOrderType(orderType)) {
                throw new InvalidParameterException("参数异常:" + keyName);
            }
        }
    }

    /**
     * 校验排序字段
     *
     * @param regionNumber
     * @param keyName
     */
    private void checkOrder(String order, String keyName) {
        if (StringUtil.isNotBlank(order)) {
            if (!Constants.Search.containsOrder(order)) {
                throw new InvalidParameterException("参数异常:" + keyName);
            }
        }
    }

    /**
     * 校验逗号分隔数字
     *
     * @param regionNumber
     * @param keyName
     */
    private void checkSeparatedNumber(String regionNumber, String keyName) {
        if (StringUtil.isNotBlank(regionNumber)) {
            boolean valid = RulesVerifyUtil.verifySeparatedNumber(regionNumber);
            if (!valid) {
                throw new InvalidParameterException("参数格式不正确:" + keyName);
            }
        }
    }

    
   
    /**
     * 校验数字非负
     *
     * @param position
     * @param keyName
     */
    private void checkNonNegativeNumber(long number, String keyName) {
        if (number < 0) {
            throw new InvalidParameterException("参数异常:" + keyName);
        }

    }

    /**
     * 校验分页最大条数
     *
     * @param position
     * @param keyName
     */
    private void checkPageSize(long number, String keyName) {
        if (number > 100) {
            throw new InvalidParameterException("参数异常:" + keyName);
        }

    }

  

    /**
     * 校验朝向
     *
     * @param haskey
     * @param keyName
     */
    private void checkOrientation(int orientation, String keyName) {
        List<Integer> validKeys = new ArrayList<Integer>();
        validKeys.add(Constants.HouseBase.ORIENTATION_EAST);
        validKeys.add(Constants.HouseBase.ORIENTATION_WEST);
        validKeys.add(Constants.HouseBase.ORIENTATION_SOUTH);
        validKeys.add(Constants.HouseBase.ORIENTATION_NORTH);
        validKeys.add(Constants.HouseBase.ORIENTATION_SOUTH_WEST);
        validKeys.add(Constants.HouseBase.ORIENTATION_NORTH_WEST);
        validKeys.add(Constants.HouseBase.ORIENTATION_NORTH_EAST);
        validKeys.add(Constants.HouseBase.ORIENTATION_SOUTH_EAST);
        validKeys.add(Constants.HouseBase.ORIENTATION_NORTH_SOUTH);
        validKeys.add(Constants.HouseBase.ORIENTATION_EAST_WEST);
        validKeys.add(Constants.HouseBase.ORIENTATION_INIT);
        checkIfValid(orientation, validKeys, keyName);
    }

    

    /**
     * 校验整分租
     *
     * @param haskey
     * @param keyName
     */
    private void checkEntireRent(int entireRent, String keyName) {
        List<Integer> validKeys = new ArrayList<Integer>();
        validKeys.add(Constants.HouseDetail.RENT_TYPE_SHARE);
        validKeys.add(Constants.HouseDetail.RENT_TYPE_ENTIRE);
        validKeys.add(Constants.HouseDetail.RENT_TYPE_BOTH);
        validKeys.add(Constants.HouseDetail.RENT_TYPE_ALL);
        checkIfValid(entireRent, validKeys, keyName);
    }

    /**
     * 校验参数是否有效
     *
     * @param value
     * @param validValues
     * @param keyName
     */
    private void checkIfValid(int value, List<Integer> validValues, String keyName) {
        if (CollectionUtils.isEmpty(validValues)) {
            return;
        }
        if (!validValues.contains(value)) {
            throw new InvalidParameterException("参数异常:" + keyName);
        }
    }

  
    /**
     * 获取服务公寓列表
     *
     * @return
     */
    public Responses getServiceApartmentList(HttpServletRequest request) throws Exception {
        //获取请求参数城市ID
        long cityId = RequestUtils.getParameterLong(request, "cityId", 0);
        logger.info("请求服务式公寓接口,城市ID:" + cityId);

        //根据城市ID获取品牌公寓列表
        List<Agency> agencyList = agencyManageRepository.getAgencyListByCityId(cityId);

        //拼接更多品牌
        List<Agency> gdList = agencyManageRepository.queryListByAgencyId(Constants.ServiceApartment.DEFAULT_COMPANY_ID);
        if (CollectionUtils.isNotEmpty(gdList)) {
            Agency aitaAgency = gdList.get(0);
            agencyList.add(aitaAgency);
        }

        //封装返回BODY
        JsonObject result = new JsonObject();
        JsonArray jsonAgencies = new JsonArray();
        if (CollectionUtils.isNotEmpty(agencyList)) {
            jsonAgencies = GsonUtils.getInstace().toJsonTree(agencyList).getAsJsonArray();
        }
        result.add("companyList", jsonAgencies);
        Responses responses = new Responses(result);
        logger.info("服务式公寓返回列表为:" + result.toString());

        //封装返回META
        if (CollectionUtils.isNotEmpty(agencyList)) {
            ResponseUtils.fillRow(responses.getMeta(), agencyList.size(), agencyList.size());
        }
        return responses;
    }

  
    /**
     * @Title: searchHouseList
     * @Description: 条件搜索房源数据
     * @return Responses
     * @author 叶东明
     * @dateTime 2017年8月17日 下午4:07:37
     */
    private Responses searchHouseList(HouseSearchDto houseSearchDto) {
       
    	
    	// 若地铁站id不为空，查询地铁站坐标
        long stationId = houseSearchDto.getStationId();
        if (stationId != 0) {
            Subway subway = subwayRepository.findSubWayByStationId(stationId);
            String stationPosition = HouseUtil.getPosition(subway);
            if (StringUtil.isEmpty(stationPosition)) {
                logger.error(LogUtils.getCommLog("地铁站坐标查询失败 ,stationId:" + stationId));
                return new Responses(ErrorMsgCode.ERROR_MSG_SEARCH_HOUSE_FAIL, "搜索房源失败");
            }
            houseSearchDto.setStationLocation(stationPosition);
        }
        
        
        // 查询当前城市的品牌公寓集合
        List<String> agencyIdList = new ArrayList<String>();
        if ("-1".equals(houseSearchDto.getCompanyId()) || StringUtil.isNotBlank(houseSearchDto.getCompanyType())) {
            List<Agency> agencyList = agencyManageRepository.findAgenciesByCityId(houseSearchDto.getCityId());
            if (CollectionUtils.isNotEmpty(agencyList)) {
                for (Agency agency : agencyList) {
                    if (!agencyIdList.contains(agency.getCompanyId())) {
                        agencyIdList.add(agency.getCompanyId());
                    }
                }
            }
        }

        //按渠道过滤
        List<String> denyList = platformCustomerRepository.findAllWithoutPermission();
        houseSearchDto.setDenyList(denyList);

        // 实现搜索功能
        logger.info(LogUtils.getCommLog("搜房源请求参数 ,houseSearchDto:" + houseSearchDto));
        HouseSearchResultDto houseSearchResultDto = CretateHouseSearchResultDto();

        //获取用户ID
        long userId = houseSearchDto.getUserId();
        
        if(userId > 0){
            List<HouseCollection> houseCollections = houseCollectionRepository.getHouseCollectionListByUserId(userId);
            houseSearchDto = getCollectFlagDto(houseSearchDto,houseCollections);//获取收藏标识
        }
        
        
        //统一一个搜索服务
        houseSearchResultDto = houseNewDao.getHouseResultDto(houseSearchDto, agencyIdList);
        
        //首页支持月付去重
        if(houseSearchDto.getIsHomePage() == Constants.searchType.SEARCH_HOMEPAGE && houseSearchResultDto !=null && houseSearchDto.getPayType().contains(Constants.HouseDetail.PAY_TYPE) ){
            houseSearchResultDto = getPaySearchResultDto(houseSearchResultDto, houseSearchDto, agencyIdList);
        }
        
        if (houseSearchResultDto == null) {
            logger.error(LogUtils.getCommLog("搜索房源结果不存在"));
            return new Responses(ErrorMsgCode.ERROR_MSG_SEARCH_HOUSE_FAIL, "搜索房源失败");
        }

        Responses responses = new Responses(houseSearchResultDto);
        List<HouseSearchResultInfo> searchHouses = houseSearchResultDto.getSearchHouses();

        //请求图片优化渠道列表
        List<PlatformCustomer> customerList = platformCustomerRepository.findByisImgs();
        Map<String, String> map = new HashMap<String, String>();
        if (CollectionUtils.isNotEmpty(customerList)) {
            for (PlatformCustomer customer : customerList) {
                map.put(customer.getSource(), customer.getImageCss());
            }
        }
        if (CollectionUtils.isNotEmpty(searchHouses)) {
            for (HouseSearchResultInfo info : searchHouses) {
                if (StringUtils.isNotEmpty(info.getPic())) {
                    if (map != null) {//图片美化

                        if (map.containsKey(info.getSource())) {
                            String pic = info.getPic() + "?x-oss-process=image/resize,h_240"
                                    + map.get(info.getSource());
                            info.setPic(pic);
                        } else {// 推荐房源列表页显示小图 
                            String pic = info.getPic() + "?x-oss-process=image/resize,h_240";
                            info.setPic(pic);
                        }
                    } else {
                        String pic = info.getPic() + "?x-oss-process=image/resize,h_240";
                        info.setPic(pic);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(searchHouses)) {
            ResponseUtils.fillRow(responses.getMeta(), searchHouses.size(), searchHouses.size());
        }
        return responses;
    }

  

    /**
     * @Title: searchHouseList
     * @Description: 条件搜索房源数据
     * @return Responses
     * @author 叶东明
     * @dateTime 2017年8月17日 下午4:07:37
     */
    private Responses getCollectFoot(HouseSearchDto houseSearchDto) {
       
        // 查询当前城市的品牌公寓集合
        List<String> agencyIdList = new ArrayList<String>();
        if ("-1".equals(houseSearchDto.getCompanyId()) || StringUtil.isNotBlank(houseSearchDto.getCompanyType())) {
            List<Agency> agencyList = agencyManageRepository.findAgenciesByCityId(houseSearchDto.getCityId());
            if (CollectionUtils.isNotEmpty(agencyList)) {
                for (Agency agency : agencyList) {
                    if (!agencyIdList.contains(agency.getCompanyId())) {
                        agencyIdList.add(agency.getCompanyId());
                    }
                }
            }
        }

        //按渠道过滤
        List<String> denyList = platformCustomerRepository.findAllWithoutPermission();
        houseSearchDto.setDenyList(denyList);

        // 实现搜索功能
        logger.info(LogUtils.getCommLog("搜房源请求参数 ,houseSearchDto:" + houseSearchDto));
        HouseSearchResultDto houseSearchResultDto = CretateHouseSearchResultDto();

        //获取用户ID
        long userId = houseSearchDto.getUserId();
        
        if(userId > 0){
            if(houseSearchDto.getIsCollect() == Constants.searchType.SEARCH_COLLECT){//查询收藏列表
                List<HouseCollection> houseCollections = houseCollectionRepository.getHouseCollectionListByUserId(userId);
                houseSearchDto = getCollectDto(houseSearchDto, houseCollections); 
            }
            
            if(houseSearchDto.getIsFoot() == Constants.searchType.SEARCH_FOOT){//查询足迹列表
                List<FootmarkHistory> footmarkHistorys = footmarkHistoryRepository.getHouseListByUserId(userId);
                houseSearchDto = getFootmarkDto(houseSearchDto, footmarkHistorys);   
            }
           
        }
        
        //统一一个搜索服务
        houseSearchResultDto = houseNewDao.getCollectFoot(houseSearchDto, agencyIdList);
             
        if (houseSearchResultDto == null) {
            logger.error(LogUtils.getCommLog("搜索房源结果不存在"));
            return new Responses(ErrorMsgCode.ERROR_MSG_SEARCH_HOUSE_FAIL, "搜索房源失败");
        }

        Responses responses = new Responses(houseSearchResultDto);
        List<HouseSearchResultInfo> searchHouses = houseSearchResultDto.getSearchHouses();

        //请求图片优化渠道列表
        List<PlatformCustomer> customerList = platformCustomerRepository.findByisImgs();
        Map<String, String> map = new HashMap<String, String>();
        if (CollectionUtils.isNotEmpty(customerList)) {
            for (PlatformCustomer customer : customerList) {
                map.put(customer.getSource(), customer.getImageCss());
            }
        }
        if (CollectionUtils.isNotEmpty(searchHouses)) {
            for (HouseSearchResultInfo info : searchHouses) {
                if (StringUtils.isNotEmpty(info.getPic())) {
                    if (map != null) {//图片美化

                        if (map.containsKey(info.getSource())) {
                            String pic = info.getPic() + "?x-oss-process=image/resize,h_240"
                                    + map.get(info.getSource());
                            info.setPic(pic);
                        } else {// 推荐房源列表页显示小图 
                            String pic = info.getPic() + "?x-oss-process=image/resize,h_240";
                            info.setPic(pic);
                        }
                    } else {
                        String pic = info.getPic() + "?x-oss-process=image/resize,h_240";
                        info.setPic(pic);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(searchHouses)) {
            ResponseUtils.fillRow(responses.getMeta(), searchHouses.size(), searchHouses.size());
        }
        return responses;
    }
    


    //获取用户收藏列表
    public List<String> getCollectList(long userId) {
        if (userId <= 0) {//未登录校验
            return null;
        }

        List<String> collectIdList = new ArrayList<String>();
        List<HouseCollection> houseCollectionList = new ArrayList<HouseCollection>();
        try {
            //根据用户ID获取收藏列表
            houseCollectionList = houseCollectionRepository.getHouseCollectionListByUserId(userId);
            if (CollectionUtils.isNotEmpty(houseCollectionList)) {
                for (HouseCollection houseCollection : houseCollectionList) {
                    String sellId = houseCollection.getSellId();
                    if (!collectIdList.contains(sellId)) {
                        collectIdList.add(sellId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(LogUtils.getCommLog("用户登录成功查询收藏列表异常") + e);
            return null;
        }
        return collectIdList;
    }

    

    //创建房源结果对象
    private HouseSearchResultDto CretateHouseSearchResultDto() {
        HouseSearchResultDto houseSearchResultDto = new HouseSearchResultDto();
        return houseSearchResultDto;
    }

    
    //月付房源打散策率
    public HouseSearchResultDto getPaySearchResultDto(HouseSearchResultDto houseSearchResultDto,
                                                      HouseSearchDto houseSearchDto, List<String> agencyIdList) {
        List<HouseSearchResultInfo> infoList = new ArrayList<HouseSearchResultInfo>();
        infoList = houseSearchResultDto.getSearchHouses();
        
        //清空支付类型
        houseSearchDto.setPayType("");
        
        //获取整租数据
        List<String> entireList = new ArrayList<String>();
        HouseSearchResultDto entireDto = new HouseSearchResultDto();
        houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_ENTIRE);
        entireDto = houseNewDao.getHouseResultDto(houseSearchDto, agencyIdList);
        if (entireDto != null && CollectionUtils.isNotEmpty(entireDto.getSearchHouses())) {
            for (HouseSearchResultInfo info : entireDto.getSearchHouses()) {
                if(entireList.size() >= 5 )
                    break;
                entireList.add(info.getSellId().trim());
            }
        }

        //获取合租数据
        List<String> shareList = new ArrayList<String>();
        HouseSearchResultDto shareDto = new HouseSearchResultDto();
        houseSearchDto.setEntireRent(Constants.HouseDetail.RENT_TYPE_SHARE);
        shareDto = houseNewDao.getHouseResultDto(houseSearchDto, agencyIdList);
        if (shareDto != null && CollectionUtils.isNotEmpty(shareDto.getSearchHouses())) {
            for (HouseSearchResultInfo info : shareDto.getSearchHouses()) {
                if(shareList.size() >= 5 )
                    break;
                shareList.add(info.getSellId().trim());
            }
        }

        //数据合并
        shareList.addAll(entireList);
        List<HouseSearchResultInfo> newInfoList = new ArrayList<HouseSearchResultInfo>();
        for (HouseSearchResultInfo info : infoList) {
            if (shareList.contains(info.getSellId().trim())) {
                newInfoList.add(info);
            }
        }
        
        if(infoList.size()-newInfoList.size()>2){//保证首页支付不空白
            infoList.removeAll(newInfoList);  
        }

        houseSearchResultDto.setSearchHouses(infoList);
        return houseSearchResultDto;

    }
    
    /**
     * 根据收藏房源ID封装请求对象
     */
    private HouseSearchDto getCollectDto(HouseSearchDto houseSearchDto ,List<HouseCollection> houseCollections) {
        List<String> collectHouseList = new ArrayList<String>();
        List<String> collectRoomList = new ArrayList<String>();
        
        if (CollectionUtils.isNotEmpty(houseCollections)) {
            for (HouseCollection coll : houseCollections) {
                if (coll.getRoomId() > 0) {//合租房间ID
                    collectRoomList.add(String.valueOf(coll.getRoomId()));
                }else{//房源ID
                    collectHouseList.add(coll.getSellId());
                }
            }
        }
        
        if(CollectionUtils.isEmpty(collectHouseList)){//未收藏设置默认值
            collectHouseList.add(Constants.searchType.NOT_COLLECT_OR_FOOT); 
        }
        
        if(CollectionUtils.isEmpty(collectRoomList)){
            collectRoomList.add(Constants.searchType.NOT_COLLECT_OR_FOOT);         
        }
        
        houseSearchDto.setCollectHouseList(collectHouseList);;          
        houseSearchDto.setCollectRoomList(collectRoomList);
        return houseSearchDto;
    }
    
    /**
     * 根据收藏房源ID封装请求对象
     */
    private HouseSearchDto getCollectFlagDto(HouseSearchDto houseSearchDto ,List<HouseCollection> houseCollections) {
        List<String> collectFlagHouseList = new ArrayList<String>();
        List<String> collectFlagRoomList = new ArrayList<String>();
        
        if (CollectionUtils.isNotEmpty(houseCollections)) {
            for (HouseCollection coll : houseCollections) {
                if (coll.getRoomId() > 0) {//合租房间ID
                    collectFlagRoomList.add(String.valueOf(coll.getRoomId()));
                }else{//房源ID
                    collectFlagHouseList.add(coll.getSellId());
                }
            }
            houseSearchDto.setCollectFlagHouseList(collectFlagHouseList);;          
            houseSearchDto.setCollectFlagRoomList(collectFlagRoomList);
        }
        return houseSearchDto;
    }
    
    
    /**
     * 根据足迹房源列表封装请求对象
     */
    private HouseSearchDto getFootmarkDto(HouseSearchDto houseSearchDto ,List<FootmarkHistory> footmarkHistorys) {
        List<String> footHouseList = new ArrayList<String>();
        List<String> footRoomList = new ArrayList<String>();
        
        if (CollectionUtils.isNotEmpty(footmarkHistorys)) {
            for (FootmarkHistory foot : footmarkHistorys) {
                if (foot.getRoomId() > 0) {//合租房间ID
                    footRoomList.add(String.valueOf(foot.getRoomId()));
                }else{//房源ID
                    footHouseList.add(foot.getSellId());
                }
            }
           
        }
        
        if(CollectionUtils.isEmpty(footHouseList)){//无足迹设置默认值
            footHouseList.add(Constants.searchType.NOT_COLLECT_OR_FOOT); 
        }
        
        if(CollectionUtils.isEmpty(footRoomList)){
            footRoomList.add(Constants.searchType.NOT_COLLECT_OR_FOOT);         
        }
        houseSearchDto.setFootHouseList(footHouseList);         
        houseSearchDto.setFootRoomList(footRoomList);
        return houseSearchDto;
    }
    
    
}
