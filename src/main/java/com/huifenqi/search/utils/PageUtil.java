package com.huifenqi.search.utils;

import java.util.ArrayList;
import java.util.List;

import com.huifenqi.search.context.dto.response.house.HouseSearchResultInfo;

public class PageUtil {
    //已知数据
    private int pageNum;    //当前页,从请求那边传过来。
    private int pageSize;    //每页显示的数据条数。
    private int totalRecord;    //总的记录条数。查询数据库得到的数据
     
    //需要计算得来
    private int totalPage;    //总页数，通过totalRecord和pageSize计算可以得来
    
    //开始索引，也就是我们在数据库中要从第几行数据开始拿，有了startIndex和pageSize，
    //就知道了limit语句的两个数据，就能获得每页需要显示的数据了
    private int startIndex;       
    
    private int endIndex;
    
    private Boolean pageNumflag;
    //通过pageNum，pageSize，totalRecord计算得来tatalPage和startIndex
    
    //构造方法中将pageNum，pageSize，totalRecord获得
    public PageUtil(int pageNum,int pageSize,int totalRecord) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalRecord = totalRecord;
        this.pageNumflag = true;
        //totalPage 总页数
        if(totalRecord%pageSize==0){
            //说明整除，正好每页显示pageSize条数据，没有多余一页要显示少于pageSize条数据的
            this.totalPage = totalRecord / pageSize;
        }else{
            //不整除，就要在加一页，来显示多余的数据。
            this.totalPage = totalRecord / pageSize +1;
        }
        
        //开始索引
        this.startIndex = (pageNum-1)*pageSize ;
        
        //结束索引
        this.endIndex = pageNum*pageSize;
        
        if(pageNum == totalPage){
            this.endIndex = totalRecord;
        }
        
        
        if(pageNum > totalPage){
            this.pageNumflag = false;
        }
    }
    
    
    public int getPageNum() {
        return pageNum;
    }
 
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
 
    public int getPageSize() {
        return pageSize;
    }
 
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
 
    public int getTotalRecord() {
        return totalRecord;
    }
 
    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }
 
    public int getTotalPage() {
        return totalPage;
    }
 
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
 
    public int getStartIndex() {
        return startIndex;
    }
 
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }


    public int getEndIndex() {
        return endIndex;
    }


    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }


    public Boolean getPageNumflag() {
        return pageNumflag;
    }


    public void setPageNumflag(Boolean pageNumflag) {
        this.pageNumflag = pageNumflag;
    }
 
    
}