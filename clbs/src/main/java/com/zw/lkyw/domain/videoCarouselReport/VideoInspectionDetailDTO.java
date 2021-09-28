package com.zw.lkyw.domain.videoCarouselReport;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoInspectionDetailDTO extends VideoCarouselReport {
    //详情
    private List<VideoInspectionDetail> detail;
}
