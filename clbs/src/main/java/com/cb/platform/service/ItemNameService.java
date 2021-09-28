package com.cb.platform.service;

import com.cb.platform.domain.ItemNameEntity;
import com.cb.platform.domain.ItemNameQuery;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ItemNameService {

    boolean addItemName(ItemNameEntity itemNameEntity, String ipAddress) throws Exception;

    //不分页查询所有的品名
    List<ItemNameEntity> findList() throws Exception;

    Page<ItemNameEntity> searchItemName(ItemNameQuery query) throws Exception;

    boolean updateItemName(ItemNameEntity itemNameEntity, String ipAddress) throws Exception;

    boolean deleteItemName(List<String> list, String ipAddress) throws Exception;

    boolean deleteById(ItemNameEntity itemNameEntity, String ipAddress) throws Exception;

    List<ItemNameEntity> findByName(String name) throws Exception;

    ItemNameEntity findById(String id) throws Exception;

    List<ItemNameEntity> findByIdList(List<String> list) throws Exception;
    /**
     * 查询危险品类别
     * @return
     */
    List<Map<String,Object>> selectType() throws Exception;

    public Map<String, Object> importItemName(MultipartFile file, String ipAddress) throws  Exception;

    /**
     * 导出
     * @param title
     * @param type
     * @param res
     * @param name
     * @return
     * @throws IOException
     */
    public boolean export(String title, int type, HttpServletResponse res, String name, String ipAddress) throws IOException;

    public boolean download(String title, int type, HttpServletResponse res) throws IOException;

}
