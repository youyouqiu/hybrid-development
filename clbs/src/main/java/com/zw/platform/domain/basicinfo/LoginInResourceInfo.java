package com.zw.platform.domain.basicinfo;

import com.zw.platform.domain.core.Resource;
import lombok.Data;

import java.util.List;

@Data
public class LoginInResourceInfo {
    private List<Resource> resources;

    public LoginInResourceInfo(List<Resource> resources) {
        this.resources = resources;
    }
}
