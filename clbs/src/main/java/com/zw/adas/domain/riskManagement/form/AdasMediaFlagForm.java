package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

import java.util.Arrays;

@Data
public class AdasMediaFlagForm {

    private byte[] id;

    private Integer picFlag;

    private Integer videoFlag;

    public AdasMediaFlagForm(byte[] id, Integer picFlag, Integer videoFlag) {
        this.id = id;
        this.picFlag = picFlag;
        this.videoFlag = videoFlag;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    //重写equals用Set接收时去重
    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (ob == null) {
            return false;
        }
        if (this.getClass() != ob.getClass()) {
            return false;
        }
        AdasMediaFlagForm wo = (AdasMediaFlagForm) ob;
        boolean idEquals = UuidUtils.getUUIDStrFromBytes(this.id).equals(UuidUtils.getUUIDStrFromBytes(wo.id));
        if (idEquals || this.picFlag.equals(wo.picFlag) || this.videoFlag.equals(wo.videoFlag)) {
            return true;
        }
        return false;
    }
}
