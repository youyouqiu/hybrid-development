package com.zw.platform.util.excel.validator;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.annotation.ExcelField;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ImportValidator<E> {
    private StringBuilder invalidInfo;

    private HashSet<Integer> validatorSet;

    public ImportValidator() {
        invalidInfo = new StringBuilder();
        validatorSet = new HashSet<>();
    }

    public boolean validateRequired(ExcelField field, Object value) {
        return !field.required() || !Objects.equals(value.toString().trim(), "");
    }

    public JsonResultBean validate(List<E> list, boolean isCheckGroupName, List<OrganizationLdap> organizations) {
        return new JsonResultBean(true, "");
    }



    public void recordInvalidInfo(String info) {
        invalidInfo.append(info);
    }

    public String getInvalidInfo() {
        return invalidInfo.toString();
    }

    public boolean getRequiredOrRepeatable(Integer column) {
        return validatorSet.contains(column);
    }

    public void setValidatorSet(Integer column) {
        validatorSet.add(column);
    }
}
