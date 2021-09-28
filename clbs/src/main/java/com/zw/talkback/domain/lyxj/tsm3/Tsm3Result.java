package com.zw.talkback.domain.lyxj.tsm3;

import lombok.Data;

import java.util.List;

/**
 * 功能描述: tsm3系统返回的分页数据
 */
@Data
public class Tsm3Result<T> {

    /**
     * 返回的消息
     */
    private String message;
    /**
     * 返回的结果集：0代表成功 1：代表失败
     */
    private int result;
    /**
     * 分页信息
     */
    private PageInfo pageInfo;
    /**
     * 当前页查询记录
     */
    private DataRecords<T> data;

    /**
     * 获取分页的返回数据方法
     * @return
     */
    public List<T> getRecords() {
        return data.getRecords();
    }

}

