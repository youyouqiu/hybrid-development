package com.zw.talkback.domain.lyxj.tsm3;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * tsm分页返回的数据结果
 */
@Data
class DataRecords<T> {
    private List<T> records = new ArrayList<>();
}
