package com.zw.platform.service.basicinfo;

import com.zw.platform.basic.domain.DictionaryDO;

import java.util.List;

public interface DictionaryService {

    List<DictionaryDO> getBusinessScope();

    List<DictionaryDO> getBusinessLicenseType();
}
