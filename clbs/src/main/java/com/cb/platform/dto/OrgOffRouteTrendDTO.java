package com.cb.platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/24 10:21
 */
@Data
public class OrgOffRouteTrendDTO implements Serializable {
    private static final long serialVersionUID = 5624904248151608778L;

    /**
     * 路线偏离报警数
     */
    private Integer totalCourseDeviation;
    /**
     * 不按规定线路行驶报警数
     */
    private Integer totalNotFollowLine;
    /**
     * 明细
     */
    private List<OffRouteDayDetailDTO> detailList;
}
