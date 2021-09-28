package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.DictionaryDO;
import com.zw.platform.basic.repository.NewDictionaryDao;
import com.zw.platform.basic.service.CacheService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 数据字典管理实现类
 *
 * @author zhangjuan
 */
@Service("dictionaryService")
public class DictionaryServiceImpl implements CacheService {
    private static final Logger log = LogManager.getLogger(DictionaryServiceImpl.class);
    @Autowired
    private NewDictionaryDao dictionaryDao;

    @PostConstruct
    @Override
    public void initCache() {
        log.info("开始进行数据字典的本地初始化~");
        List<DictionaryDO> data = dictionaryDao.getList();
        TypeCacheManger.getInstance().clearDictionary();
        if (data.isEmpty()) {
            return;
        }
        for (DictionaryDO dictionaryDO : data) {
            TypeCacheManger.getInstance().saveDictionary(dictionaryDO);
        }
        log.info("完成数据字典的本地初始化~");
    }
}
