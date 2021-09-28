package com.zw.app.domain.alarm;

import lombok.Data;

import java.io.Serializable;


@Data
public class Monitor implements Serializable {
    private String id;

    private String name;

    private String type;

    private String icon;

}
