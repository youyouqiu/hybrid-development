package com.zw.platform.domain.basicinfo.form;

import io.swagger.annotations.ApiParam;
import lombok.Data;


@Data
public class ApiEditSimcardForm extends ApiAddSimcardForm {
    @ApiParam(value = "sim卡id",required = true)
    private String id;
}
