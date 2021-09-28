package com.zw.talkback.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/9/9 11:34
 */
@Data
public class TempAssignmentInterlocutor implements Serializable {

    private static final long serialVersionUID = 1413671147273304643L;
    /**
     * id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 分组id
     */
    private String assignmentId;

    /**
     * 对讲对象id
     */
    private Long interlocutorId;

    /**
     * 对讲群组id
     */
    private Long intercomGroupId;
}
