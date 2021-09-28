package com.zw.platform.domain.basicinfo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/1 16:44
 */
@Data
@NoArgsConstructor
public class FuzzySearchFiledAndValueBO {
    private String fuzzySearchFiled;
    private String fuzzySearchValue;


    public FuzzySearchFiledAndValueBO(String fuzzySearchFiled, String fuzzySearchValue) {
        this.fuzzySearchFiled = fuzzySearchFiled;
        this.fuzzySearchValue = fuzzySearchValue;
    }
}
