package com.zw.platform.domain.basicinfo.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OBDStreamData {

    private String id;

    private Object value;

    private String valueStr;
}
