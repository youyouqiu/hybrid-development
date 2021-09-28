package com.zw.platform.service.intercomplatform;

import com.zw.platform.domain.basicinfo.Personalized;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PersonalizedService {

    boolean updateLogo(Personalized personalized);//修改个性化参数

    boolean update(Personalized personalized, String rootPath, String groupId, String name) throws IOException;

    Personalized find(String groupId);//查询平台个性化设置参数

    Personalized findOrDefault(String groupId);//查询平台个性化设置参数

    boolean add(Personalized personalized);//新增个性化参数

    Personalized findByPermission(String groupId, List<String> roleIds); // 根据所属企业查询

    /**
     * 根据平台网址（platformSite）查询平台个性化设置参数
     */
    Personalized findByPlatformSite(String platformSite);

    String uploadImage(String path, MultipartFile file, List<String> validSuffixes);
}
