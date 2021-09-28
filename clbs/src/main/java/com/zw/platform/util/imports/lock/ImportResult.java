package com.zw.platform.util.imports.lock;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 导入返回实体
 * @author create by zhouzongbo on 2020/9/3.
 */
@Data
@AllArgsConstructor
public class ImportResult implements Serializable {
    private static final long serialVersionUID = 4872720661021208701L;
    /**
     * true: 成功; false:失败
     */
    private Boolean success;

    public static ImportResult success() {
        return new ImportResult(true);
    }

    public static ImportResult failure() {
        return new ImportResult(false);
    }

    public Boolean getFailure() {
        return !success;
    }
}
