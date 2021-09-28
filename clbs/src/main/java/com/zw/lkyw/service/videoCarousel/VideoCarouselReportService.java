package com.zw.lkyw.service.videoCarousel;

import javax.servlet.http.HttpServletResponse;

import com.github.pagehelper.Page;
import com.zw.lkyw.domain.VideoCarouselReportQuery;
import com.zw.lkyw.domain.videoCarouselReport.VideoCarouselReport;
import com.zw.lkyw.domain.videoCarouselReport.VideoInspectionDetail;

public interface VideoCarouselReportService {
    Page<VideoCarouselReport> getListPage(VideoCarouselReportQuery query) throws Exception;

    String export(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception;

    Page<VideoInspectionDetail> detail(VideoCarouselReportQuery query);

    String batchExport(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception;

    String exportDetail(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception;
}
