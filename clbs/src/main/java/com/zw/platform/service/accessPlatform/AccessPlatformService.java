package com.zw.platform.service.accessPlatform;

import com.github.pagehelper.Page;
import com.zw.platform.domain.accessPlatform.AccessPlatform;
import com.zw.platform.domain.accessPlatform.AccessPlatformQuery;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * @author LiaoYuecai
 * @create 2018-01-05 10:01
 * @desc
 */
public interface AccessPlatformService {

    /**
     * 根据id查询接入平台IP信息
     * @param id
     * @return
     * @throws Exception
     */
    AccessPlatform getByID(String id) throws Exception;

    /**
     * 查询接入平台IPList
     * @param query
     * @return
     * @throws Exception
     */
    Page<AccessPlatform> find(AccessPlatformQuery query);

    /**
     * 新增接入平台IP
     * @param accessPlatform
     * @return
     * @throws Exception
     */
    int add(AccessPlatform accessPlatform,String ipAddress) throws Exception;

    /**
     * 修改接入平台IP
     * @param accessPlatform
     * @param ipAddress
     * @return
     */
    int update(AccessPlatform accessPlatform,String ipAddress) throws Exception;

    /**
     * 删除接入平台IP
     * @param id
     * @param ipAddress
     * @return
     * @throws Exception
     */
    boolean deleteById(String id,String ipAddress) throws Exception;
    
    /**
     * 校验808接入平台名称唯一性
     * @author hujun
     * @Date 创建时间：2018年4月9日 上午11:27:06
     * @param platFormName
     * @param pid
     * @return
     * @throws Exception
     */
    boolean check808InputPlatFormSole(String platFormName,String pid) throws Exception;


    /**
     * 下载导入模板
     * @param response
     * @throws Exception
     */
    void downLoadFileTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导入接入平台IP
     * @param multipartFile
     * @param ipAddress
     * @return
     */
    Map importJoinUpPlateformIp(MultipartFile multipartFile, String ipAddress) throws Exception;

    /**
     * 导出接入平台IP
     * @param title
     * @param type
     * @param response
     * @return
     * @throws Exception
     */
    boolean exportFile(String title,int type,HttpServletResponse response) throws Exception;



}
