package com.zw.platform.domain.basicinfo;

import lombok.Data;

/**
 * @Author: zjc
 * @Description: 新增userId，因为checkstyle检查mNum进行类扩展
 * @Date: create in 2020/9/14 17:48
 */
@Data
public class AssignmentData extends Assignment {
    private String userId;
}
