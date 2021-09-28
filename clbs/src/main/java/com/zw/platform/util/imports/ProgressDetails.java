/*
 * Copyright (c) 2017.  All Rights Reserved.
 */

package com.zw.platform.util.imports;

import java.io.Serializable;

/**
 * This class is used for Progress Bar
 * @author Chen Feng
 * @version 1.0 2017/11/27
 */
public class ProgressDetails implements Serializable {
    private static final long serialVersionUID = -72688621966747641L;
    private int progress;

    public void addProgress(int progress) {
        this.progress += progress;
    }

    public int getProgress() {
        return Math.min(progress, 100);
    }
}
