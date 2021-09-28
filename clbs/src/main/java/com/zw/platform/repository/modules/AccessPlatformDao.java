package com.zw.platform.repository.modules;

import java.util.List;

import com.zw.platform.domain.accessPlatform.AccessPlatform;
import com.zw.platform.domain.accessPlatform.AccessPlatformForm;
import com.zw.platform.domain.accessPlatform.AccessPlatformQuery;


/**
 * @author LiaoYuecai
 * @create 2018-01-05 10:02
 * @desc
 */
public interface AccessPlatformDao {
    AccessPlatform getByID(String id);
    
    /**
     * 根据平台多个id对应平台信息
     * @author hujun
     * @Date 创建时间：2018年3月19日 下午5:04:32
     * @param ids
     * @return
     */
    List<AccessPlatform> getByIDs(List<String> ids);

    List<AccessPlatform> find(AccessPlatformQuery query);

    int add(AccessPlatform accessPlatform);

    int update(AccessPlatform accessPlatform);

    int deleteById(String id);
    
    /**
     * 批量删除接入平台
     * @author hujun
     * @Date 创建时间：2018年3月19日 下午5:08:31
     * @param ids
     * @return
     */
    boolean deleteByIds(List<String> ids);
    
    /**
     * 校验808接入平台名称唯一性
     * @author hujun
     * @Date 创建时间：2018年4月9日 上午11:27:53
     * @param platFormName
     * @return
     */
    String check808InputPlatFormSole(String platFormName);


    /**
     * 根据接入平台名称查询接入平台名称
     * @param name
     * @return
     */
    List<String>  findPlateformNameByName(String name);

    /**
     * 批量新增接入平台
     * @param accessPlatforms
     * @return
     */
    boolean addPlateformBatch(List<AccessPlatform> accessPlatforms);

    /**
     * 查询全部的接入平台Ip
     * @return
     */
    List<AccessPlatformForm> findAllIp();
}
