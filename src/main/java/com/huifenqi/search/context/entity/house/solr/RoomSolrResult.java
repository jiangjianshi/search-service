/** 
 * Project Name: hzf_platform_project 
 * File Name: RoomSolrResult.java 
 * Package Name: com.huifenqi.hzf_platform.context.entity.house.solr 
 * Date: 2016年5月16日下午7:39:16 
 * Copyright (c) 2016, www.huizhaofang.com All Rights Reserved. 
 * 
 */
package com.huifenqi.search.context.entity.house.solr;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.huifenqi.search.context.Constants;

/**
 * ClassName: RoomSolrResult date: 2016年5月16日 下午7:39:16 Description:
 * 
 * @author changmingwei
 * @version
 * @since JDK 1.8
 */
@SolrDocument(solrCoreName = Constants.SolrConstant.CORE_ROOM)
public class RoomSolrResult {

	  private float  textscore;
	  private float  finalscore;
	    
	   
	  public float getTextscore() {
		  return textscore;
	  }

	  public void setTextscore(float textscore) {
		  this.textscore = textscore;
	  }

	  public float getFinalscore() {
		  return finalscore;
	   }

	  public void setFinalscore(float finalscore) {
		 this.finalscore = finalscore;
	  }
	
	
	@Id
	private long id;

	/**
	 * 房源Id
	 */
	@Field("hsId")
	private String sellId;

	/**
	 * 城市Id
	 */
	private long cityId;

	/**
	 * 发布时间
	 */
	@Field("pubDateStr")
	private String pubDate;

	/**
	 * 小区名
	 */
	@Field
	private String communityName;

	/**
	 * 月租金
	 */
	@Field("rRentPriceMonth")
	private int price;


	/**
	 * 月租金
	 */
	@Field("rRentPriceZero")
	private int priceZero;

	/**
	 * 图片
	 */
	@Field("picRootPath")
	private List<String> imgs;
	
	   /**
     * 图片
     */
    @Field("picWebPath")
    private List<String> webImgs;

	/**
	 * 地址
	 */
	@Field
	private String address;

	/**
	 * 客厅数
	 */
	@Field
	private int livingroomNums;

	/**
	 * 卧室数
	 */
	@Field
	private int bedroomNums;

	/**
	 * 卫生间数
	 */
	@Field
	private int toiletNums;

	/**
	 * 朝向
	 */
	@Field("roomori")
	private int orientations;

	/**
	 * 装饰
	 */
	@Field("rdecoration")
	private int decoration;

	/**
	 * 房间面积
	 */
	@Field("rArea")
	private double area;

	/**
	 * 房源面积
	 */
	@Field("rHouseArea")
	private double houseArea;

	/**
	 * 楼层
	 */
	@Field
	private String flowNo;

	/**
	 * 总楼层
	 */
	@Field
	private String flowTotal;

	/**
	 * 整租/分租
	 */
	@Field
	private int entireRent;

	/**
	 * 距离
	 */
	@Field("_dist_")
	private int distance;

	/**
	 * 房间标签
	 */
	@Field("roomtag")
	private String roomTag;

	/**
	 * 房间类型
	 */
	@Field
	private int roomType;

	/**
	 * 房间类型名称
	 */
	@Field("rtName")
	private String rtName;

	/**
	 * 房间名称
	 */
	@Field("roomname")
	private String roomName;

	/**
	 * 房间状态
	 */
	@Field("rapproveStatus")
	private int roomApproveStatus;
	
	/**
	 * 房间状态
	 */
	@Field("rStatus")
	private int status;

	/**
	 * 入住日期
	 */
	@Field("rCanCheckinDateStr")
	private String checkIn;

	/**
	 * 押几个月
	 */
	@Field("rDepositMonth")
	private int depositMonth;

	/**
	 * 付几个月
	 */
	@Field("rPeriodMonth")
	private int periodMonth;

	/**
	 * 更新日期
	 */
	@Field("rupdateTimeStr")
	private String updateDate;

	/**
	 * 百度经度
	 */
	@Field("baiduLo")
	private String baiduLongitude;

	/**
	 * 百度纬度
	 */
	@Field("baiduLa")
	private String baiduLatitude;

	/**
	 * 地铁信息
	 */
	@Field
	private String subway;

	/**
	 * 到地铁站距离
	 */
	@Field
	private String subwayDistance;

	/**
	 * 附近公交
	 */
	@Field
	private String busStations;

	/**
	 * 周边信息
	 */
	@Field
	private String surround;
	
	/**
	 * 商圈名称
	 */
	@Field("bizname")
	private String bizName;

	/**
	 * 是否有独立卫生间
	 */
	@Field("rtoilet")
	private int toilet;

	/**
	 * 是否有独立阳台
	 */
	@Field("rbalcony")
	private int balcony;

	/**
	 * 是否有家财险
	 */
	@Field("rinsurance")
	private int insurance;

	/**
	 * 房间描述
	 */
	@Field("rcomment")
	private String comment;

	/**
	 * 来源
	 */
	@Field
	private String source;

	/**
	 * 联系号码
	 */
	@Field
	private String agencyPhone;

	/**
	 * 配置编码
	 */
	@Field("settingCode")
	private List<Integer> settingCodes;

	/**
	 * 配置类型
	 */
	@Field("categoryType")
	private List<Integer> categoryTypes;

	/**
	 * 配置数量
	 */
	@Field("settingNums")
	private List<Integer> settingNums;

	/**
	 * 出租方式
	 */
	@Field("rentName")
	private String rentName;
	
	/**
	 * 出租方式
	 */
	@Field("risTop")
	private int isTop;


	/**
	 * 公寓Id
	 */
	@Field("companyId")
	private String companyId;
	
	/**
	 * 公寓名称
	 */
	@Field("companyName")
	private String companyName;

	/**
	 * 是否支持月付
	 */
	@Field("risPayMonth")
	private int risPayMonth;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSellId() {
		return sellId;
	}

	public void setSellId(String sellId) {
		this.sellId = sellId;
	}

	public long getCityId() {
		return cityId;
	}

	public void setCityId(long cityId) {
		this.cityId = cityId;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public List<String> getImgs() {
		return imgs;
	}

	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}

	
	public List<String> getWebImgs() {
        return webImgs;
    }

    public void setWebImgs(List<String> webImgs) {
        this.webImgs = webImgs;
    }

    public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getLivingroomNums() {
		return livingroomNums;
	}

	public void setLivingroomNums(int livingroomNums) {
		this.livingroomNums = livingroomNums;
	}

	public int getBedroomNums() {
		return bedroomNums;
	}

	public void setBedroomNums(int bedroomNums) {
		this.bedroomNums = bedroomNums;
	}

	public int getToiletNums() {
		return toiletNums;
	}

	public void setToiletNums(int toiletNums) {
		this.toiletNums = toiletNums;
	}

	public int getOrientations() {
		return orientations;
	}

	public void setOrientations(int orientations) {
		this.orientations = orientations;
	}

	public int getDecoration() {
		return decoration;
	}

	public void setDecoration(int decoration) {
		this.decoration = decoration;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public String getFlowNo() {
		return flowNo;
	}

	public void setFlowNo(String flowNo) {
		this.flowNo = flowNo;
	}

	public int getEntireRent() {
		return entireRent;
	}

	public void setEntireRent(int entireRent) {
		this.entireRent = entireRent;
	}

	public String getFlowTotal() {
		return flowTotal;
	}

	public void setFlowTotal(String flowTotal) {
		this.flowTotal = flowTotal;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getRoomTag() {
		return roomTag;
	}

	public void setRoomTag(String roomTag) {
		this.roomTag = roomTag;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(String checkIn) {
		this.checkIn = checkIn;
	}

	public int getDepositMonth() {
		return depositMonth;
	}

	public void setDepositMonth(int depositMonth) {
		this.depositMonth = depositMonth;
	}

	public int getPeriodMonth() {
		return periodMonth;
	}

	public void setPeriodMonth(int periodMonth) {
		this.periodMonth = periodMonth;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getBaiduLongitude() {
		return baiduLongitude;
	}

	public void setBaiduLongitude(String baiduLongitude) {
		this.baiduLongitude = baiduLongitude;
	}

	public String getBaiduLatitude() {
		return baiduLatitude;
	}

	public void setBaiduLatitude(String baiduLatitude) {
		this.baiduLatitude = baiduLatitude;
	}

	public String getSubway() {
		return subway;
	}

	public void setSubway(String subway) {
		this.subway = subway;
	}

	public String getSubwayDistance() {
		return subwayDistance;
	}

	public void setSubwayDistance(String subwayDistance) {
		this.subwayDistance = subwayDistance;
	}

	public String getBusStations() {
		return busStations;
	}

	public void setBusStations(String busStations) {
		this.busStations = busStations;
	}

	public String getSurround() {
		return surround;
	}

	public void setSurround(String surround) {
		this.surround = surround;
	}

	public int getToilet() {
		return toilet;
	}

	public void setToilet(int toilet) {
		this.toilet = toilet;
	}

	public int getBalcony() {
		return balcony;
	}

	public void setBalcony(int balcony) {
		this.balcony = balcony;
	}

	public int getInsurance() {
		return insurance;
	}

	public void setInsurance(int insurance) {
		this.insurance = insurance;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAgencyPhone() {
		return agencyPhone;
	}

	public void setAgencyPhone(String agencyPhone) {
		this.agencyPhone = agencyPhone;
	}

	public List<Integer> getSettingCodes() {
		return settingCodes;
	}

	public void setSettingCodes(List<Integer> settingCodes) {
		this.settingCodes = settingCodes;
	}

	public List<Integer> getCategoryTypes() {
		return categoryTypes;
	}

	public void setCategoryTypes(List<Integer> categoryTypes) {
		this.categoryTypes = categoryTypes;
	}

	public List<Integer> getSettingNums() {
		return settingNums;
	}

	public void setSettingNums(List<Integer> settingNums) {
		this.settingNums = settingNums;
	}

	public String getBizName() {
		return bizName;
	}

	public void setBizName(String bizName) {
		this.bizName = bizName;
	}

	public String getRentName() {
		return rentName;
	}

	public void setRentName(String rentName) {
		this.rentName = rentName;
	}

	public int getIsTop() {
		return isTop;
	}

	public void setIsTop(int isTop) {
		this.isTop = isTop;
	}

	public double getHouseArea() {
		return houseArea;
	}

	public void setHouseArea(double houseArea) {
		this.houseArea = houseArea;
	}

	public String getRtName() {
		return rtName;
	}

	public void setRtName(String rtName) {
		this.rtName = rtName;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getRisPayMonth() {
		return risPayMonth;
	}

	public void setRisPayMonth(int risPayMonth) {
		this.risPayMonth = risPayMonth;
	}

	public int getPriceZero() {
		return priceZero;
	}

	public void setPriceZero(int priceZero) {
		this.priceZero = priceZero;
	}

	public int getRoomApproveStatus() {
		return roomApproveStatus;
	}

	public void setRoomApproveStatus(int roomApproveStatus) {
		this.roomApproveStatus = roomApproveStatus;
	}

    public void getInstance(Document doc) {
        // TODO Auto-generated method stub
    	
    	this.setId(new Long(doc.get("roomId_store")).longValue());
    	 this.setSellId(doc.get("hsId_store"));
    	 this.setCityId(new Long(doc.get("cityId_store")).longValue());
    	 this.setPubDate(doc.get("pubDateStr_store"));
    	  this.setCommunityName(doc.get("communityName_store"));
    	  this.setPrice((int)new Double(doc.get("rRentPriceMonth_store")).doubleValue());
    	  this.setPriceZero(new Integer(doc.get("rRentPriceZero_store")).intValue());
          this.setAddress(doc.get("address_store"));
          this.setLivingroomNums(new Integer(doc.get("livingroomNums_store")).intValue());
          
          this.setBedroomNums(new Integer(doc.get("bedroomNums_store")).intValue());
          this.setToiletNums(new Integer(doc.get("toiletNums_store")).intValue());
          this.setOrientations(new Integer(doc.get("roomori_store")).intValue());
          this.setDecoration(new Integer(doc.get("rdecoration_store")).intValue());
          this.setArea(new Double(doc.get("rArea_store")).doubleValue());
          
          this.setHouseArea(new Double(doc.get("rHouseArea_store")).doubleValue());
          
          this.setFlowNo(doc.get("flowNo_store"));
          this.setFlowTotal(doc.get("flowTotal_store"));
          this.setEntireRent(new Integer(doc.get("entireRent_store")).intValue());
          
          this.setRoomTag(doc.get("roomtag_store"));
          this.setRoomType(new Integer(doc.get("roomType_store")).intValue());
          this.setRtName(doc.get("rtName_store"));
          this.setRoomName(doc.get("roomname_store"));  
          
          this.setRoomApproveStatus(new Integer(doc.get("rapproveStatus_store")).intValue());
          
          this.setStatus(new Integer(doc.get("rStatus_store")).intValue());
          
          this.setCheckIn(doc.get("rCanCheckinDateStr_store"));
          
          this.setDepositMonth( new Integer(doc.get("rDepositMonth_store")).intValue());
          
          this.setPeriodMonth(new Integer(doc.get("rPeriodMonth_store")).intValue());
          
          this.setUpdateDate(doc.get("rupdateTimeStr_store"));
          
          this.setBaiduLongitude(doc.get("baiduLo_store"));
          
          this.setBaiduLatitude(doc.get("baiduLa_store"));
          this.setSubway(doc.get("subway_store"));
          
          this.setSubwayDistance(doc.get("subwayDistance_store"));

          this.setBusStations(doc.get("busStations_store"));
          
          this.setSurround(doc.get("surround_store"));
          
          this.setToilet(new Integer(doc.get("rtoilet_store")).intValue());
          
          
          this.setBalcony(new Integer(doc.get("rbalcony_store")).intValue());
          this.setInsurance(new Integer(doc.get("rinsurance_store")).intValue());
          this.setComment(doc.get("rcomment_store"));
          
          this.setSource(doc.get("source_store"));
          
          this.setAgencyPhone(doc.get("agencyPhone_store"));
          this.setRentName(doc.get("rentName_store"));
          
          this.setIsTop(new Integer(doc.get("risTop_store")).intValue());
          
          this.setCompanyId(doc.get("companyId_store"));
          
          this.setCompanyName(doc.get("companyName_store"));
          this.setBizName(doc.get("bizname_store"));
          
          this.setRisPayMonth(new Integer(doc.get("risPayMonth_store")).intValue());
          
          String image=doc.get("f_pic_root_path_store");
          imgs=new LinkedList<String>();
          if(image!=null && !image.trim().equals(""))
          {	
          	
          	String[] strs=image.split("\\|");
              
          	for(String str:strs)
          	{
          		imgs.add(str);
          	}
          }
          
          String imageWeb=doc.get("f_pic_web_path_store");
          webImgs=new LinkedList<String>();
          if(imageWeb!=null && !imageWeb.trim().equals(""))
          { 
            
            String[] strs=imageWeb.split("\\|");
              
            for(String str:strs)
            {
                webImgs.add(str);
            }
          }
          
          String f_setting_code=doc.get("f_setting_code_store");
          String f_setting_nums=doc.get("f_setting_nums_store");
          String f_category_type=doc.get("f_category_type_store");
          settingCodes=new LinkedList<Integer>();
          categoryTypes=new LinkedList<Integer>();
          settingNums=new LinkedList<Integer>();
          if(f_setting_code!=null && !f_setting_code.trim().equals(""))
          {
              String[] strs=f_setting_code.split("\\|");
          	for(String str:strs)
          	{
          	
          		settingCodes.add(Integer.parseInt(str));
          	}
          }
          
          if(f_category_type!=null && !f_category_type.trim().equals(""))
          {
              String[] strs=f_category_type.split("\\|");
          	for(String str:strs)
          	{
          		categoryTypes.add(Integer.parseInt(str));
          	}
          }
          
          if(f_setting_nums!=null && !f_setting_nums.trim().equals(""))
          {
              String[] strs=f_setting_nums.split("\\|");
          	for(String str:strs)
          	{
          		settingNums.add(Integer.parseInt(str));
          	}
          }  
          

             
          
    }
    
    
   
}
