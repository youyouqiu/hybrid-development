package com.cb.platform.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
public enum ChatOpCode {
    UC_ERROR("0"),
    CREATE_USER_GROUP("1"),
    UPDATE_USER_GROUP("2"),
    GET_USER_GROUP("3"),
    GET_USER_GROUP_LIST("4"),
    DELETE_USER_GROUP("5"),
    GET_USER_GROUP_TREE("6"),
    GET_USER_GROUP_RECURSION("7"),
    CREATE_USER("10"),
    GET_USER("11"),
    UPDATE_USER("12"),
    GET_USER_LIST("13"),
    GET_USER_IMG("14"),
    GET_USER_BY_EMAIL("15"),
    CHECK_USER_BY_USER_NAME("16"),
    CHECK_USER_PHONE("17"),
    GET_TOKEN("20"),
    UPDATE_TOKEN("21"),
    DELETE_TOKEN("22"),
    GET_ADMIN_TOKEN("23"),
    SEND_OFFLINE_MSG("24");

    private String value;

    private static final Map<String, ChatOpCode> lookup = new HashMap<>();
    static {
        for (ChatOpCode chatOpCode : EnumSet.allOf(ChatOpCode.class)) {
            lookup.put(chatOpCode.value, chatOpCode);
        }
    }

    ChatOpCode(String value) {
        this.value = value;
    }

    public static ChatOpCode find(String value) {
        return lookup.get(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
