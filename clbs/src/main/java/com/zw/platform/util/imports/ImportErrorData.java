package com.zw.platform.util.imports;

import java.io.Serializable;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/7
 **/
public abstract class ImportErrorData implements Serializable {

    private static final long serialVersionUID = -3734896495690627714L;

    public abstract String getErrorMsg();

    public abstract void setErrorMsg(String errorMsg);
}
