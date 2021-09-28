package com.zw.talkback.service.baseinfo;

import java.util.Collection;
import java.util.List;

/***
 @Author zhengjc
 @Date 2019/8/1 16:34
 @Description 组呼和个呼号码service
 @version 1.0
 **/
public interface IntercomCallNumberService {

    /**
     * 更新并返回需要的个呼号码,因为会存在号码不够用了，所以显示将异常抛给调用方法
     */
    String updateAndReturnPersonCallNumber() throws Exception;

    /**
     * 更新并返回需要的组呼号码
     */
    String updateAndReturnGroupCallNumber() throws Exception;

    /**
     * 更新并回收个呼号码
     * @param callNumber
     * @throws Exception
     */
    void updateAndRecyclePersonCallNumber(String callNumber) throws Exception;

    /**
     * 更新并回收组呼号码
     * @param callNumber
     * @throws Exception
     */
    void updateAndRecycleGroupCallNumber(String callNumber) throws Exception;

    /**
     * 初始化所有的可用的个呼和组呼号码到redis中
     */
    void addAndInitCallNumberToRedis();

    /**
     * 批量回收个呼号码
     * @param personNumbers personNumbers
     */
    void updateAndRecyclePersonCallNumberBatch(Collection<String> personNumbers);

    /**
     * 更新并返回需要的个呼号码,因为会存在号码不够用了，所以显示将异常抛给调用方法
     */
    List<String> updateAndReturnPersonCallNumbers(int length) throws Exception;

}
