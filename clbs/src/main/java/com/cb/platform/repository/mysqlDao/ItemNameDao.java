package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.ItemNameEntity;
import com.cb.platform.domain.ItemNameExportEntity;
import com.cb.platform.domain.ItemNameQuery;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemNameDao {

    boolean addItemName(@Param("entity")ItemNameEntity itemNameEntity);

    //不分页查询所有的品名
    List<ItemNameEntity> findList();

    List<ItemNameEntity> searchItemName(@Param("query") ItemNameQuery query);

    List<ItemNameEntity> findByName(@Param("name") String name);

    boolean updateItemName(@Param("entity")ItemNameEntity itemNameEntity);

    boolean deleteItemName(@Param("list")List<String> list);

    boolean insertList(@Param("list")List<ItemNameEntity> list);

    ItemNameEntity findById(@Param("id")String id);


    /**
     * 查询危险品类别
     * @return
     */
    List<Map<String,Object>> selectType();

    List<ItemNameEntity> findByIdList(List<String> list);

    List<ItemNameExportEntity> findExportByName(@Param("name")String name);

}
