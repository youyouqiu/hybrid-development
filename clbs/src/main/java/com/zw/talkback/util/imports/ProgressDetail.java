package com.zw.talkback.util.imports;

import lombok.Data;

/**
 * 导入进度条
 */
@Data
public class ProgressDetail {
    private int progress;
    private int total;

    public void addProgress(int progress) {
        this.progress += progress;
    }
}
