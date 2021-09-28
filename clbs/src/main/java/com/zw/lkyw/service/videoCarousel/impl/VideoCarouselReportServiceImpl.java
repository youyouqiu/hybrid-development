package com.zw.lkyw.service.videoCarousel.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.lkyw.domain.VideoCarouselReportQuery;
import com.zw.lkyw.domain.videoCarouselReport.VideoCarouselReport;
import com.zw.lkyw.domain.videoCarouselReport.VideoCarouselReportDTO;
import com.zw.lkyw.domain.videoCarouselReport.VideoInspectionDetail;
import com.zw.lkyw.domain.videoCarouselReport.VideoInspectionDetailDTO;
import com.zw.lkyw.service.videoCarousel.VideoCarouselReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.report.PaasCloudUrlEnum.SAVE_VIDEO_INSPECTION_DETAILS_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.VIDEO_INSPECTION_STATISTICS_URL;

@Service
@Log4j
public class VideoCarouselReportServiceImpl implements VideoCarouselReportService {

    @Autowired
    private UserService userService;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private OrganizationService organizationService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Override
    public Page<VideoCarouselReport> getListPage(VideoCarouselReportQuery query) throws Exception {
        byte flag = query.getFlag();
        String userUuid = userService.getCurrentUserUuid();
        if (flag == 1) {
            RedisKey redisKey = HistoryRedisKeyEnum.VIDEO_CAROUSEL_REPORT.of(userUuid);
            RedisHelper.delete(redisKey);
            List<VideoCarouselReport> hbaseList = Lists.newArrayList();
            String str = HttpClientUtil.send(VIDEO_INSPECTION_STATISTICS_URL, query.getQueryParam());
            if (StringUtils.isBlank(str)) {
                return new Page<>();
            }
            try {
                VideoCarouselReportDTO<VideoCarouselReport> dto =
                    JSON.parseObject(str, new TypeReference<VideoCarouselReportDTO<VideoCarouselReport>>() {
                    });
                if (dto == null || dto.getData() == null) {
                    return new Page<>();
                }
                hbaseList.addAll(dto.getData());
            } catch (Exception e) {
                log.error("调用paas_cloud接口错误！,返回的数据为：" + str, e);
            }
            if (hbaseList.size() == 0) {
                return new Page<>();
            }
            Map<String, Map<String, String>> configMap =
                RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(query.getMonitorIds())).stream()
                    .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
            for (VideoCarouselReport videoCarousel : hbaseList) {
                Map<String, String> config = configMap.get(videoCarousel.getMonitorId());
                if (config != null) {
                    videoCarousel.setColor(CommonUtil.getVehicleColor(Integer.parseInt(config.get("plateColor"))));
                    videoCarousel.setGroupName(config.get("orgName"));
                    videoCarousel.setMonitorName(config.get("name"));
                    videoCarousel.setObjectType(cacheManger.getVehicleType(config.get("vehicleType")).getType());
                }
            }
            //将全部数据存储到redis中
            RedisHelper.addToList(redisKey, hbaseList);
            RedisHelper.expireKey(redisKey, 60 * 60 * 24);
            //返回给前端分页数据
            return RedisQueryUtil.getListToPage(getResult(query, hbaseList), query, hbaseList.size());
        } else if (flag == 2) {
            //直接从redis中获取,进行分页
            RedisKey key = HistoryRedisKeyEnum.VIDEO_CAROUSEL_REPORT.of(userUuid);
            //带车牌
            //获取所有数据
            List<VideoCarouselReport> allDatas = RedisHelper.getList(key, VideoCarouselReport.class);
            if (CollectionUtils.isEmpty(allDatas)) {
                return new Page<>();
            }
            String queryMonitorName = query.getMonitorName();
            if (StringUtils.isNotBlank(queryMonitorName)) {
                allDatas = allDatas.stream().filter(obj -> obj.getMonitorName().contains(queryMonitorName))
                    .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(allDatas)) {
                    return new Page<>();
                }
            }
            //获取redis中所有数据进行分页
            return RedisQueryUtil.getListToPage(getResult(query, allDatas), query, allDatas.size());
        }
        return new Page<>();
    }

    @Override
    public String export(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception {
        //获取数据源
        String userUuid = userService.getCurrentUserUuid();
        RedisKey key = HistoryRedisKeyEnum.VIDEO_CAROUSEL_REPORT.of(userUuid);
        if (!RedisHelper.isContainsKey(key)) {
            return "请先执行查询数据";
        }
        List<VideoCarouselReport> allDatas = RedisHelper.getList(key, VideoCarouselReport.class);
        if (allDatas == null || allDatas.size() == 0) {
            return "请先执行查询数据";
        }
        String queryMonitorName = query.getMonitorName();
        if (StringUtils.isNotBlank(queryMonitorName)) {
            allDatas = allDatas.stream().filter(obj -> obj.getMonitorName().contains(queryMonitorName))
                .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(allDatas)) {
                return "请先执行查询数据";
            }
        }
        ExportExcelUtil.setResponseHead(response, "视频巡检统计报表");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, allDatas, VideoCarouselReport.class, null, response.getOutputStream()));
        return null;
    }

    @Override
    public Page<VideoInspectionDetail> detail(VideoCarouselReportQuery query) {
        //获取数据源
        String userUuid = userService.getCurrentUserUuid();
        List<VideoCarouselReport> vehicleInfo = newVehicleDao.getVehicleInfoById(query.getMonitorIds());
        if (vehicleInfo.size() == 0) {
            return new Page<>();
        }
        String str = HttpClientUtil.send(SAVE_VIDEO_INSPECTION_DETAILS_URL, query.getQueryParam());
        if (str == null) {
            return new Page<>();
        }
        try {
            VideoCarouselReportDTO<VideoInspectionDetailDTO> dto =
                JSON.parseObject(str, new TypeReference<VideoCarouselReportDTO<VideoInspectionDetailDTO>>() {
                });
            if (dto == null) {
                return new Page<>();
            }
            List<VideoInspectionDetailDTO> data = dto.getData();
            if (data == null || data.size() == 0) {
                return new Page<>();
            }
            //只有一天数据
            List<VideoInspectionDetail> list = data.get(0).getDetail();
            if (list.size() == 0) {
                return new Page<>();
            }
            VideoCarouselReport videoCarouselReport = vehicleInfo.get(0);
            //根据UUID查询企业
            OrganizationLdap org = organizationService.getOrganizationByUuid(videoCarouselReport.getGroupId());
            if (org == null) {
                return new Page<>();
            }

            for (VideoInspectionDetail detail : list) {
                detail.setMonitorName(videoCarouselReport.getMonitorName());
                detail.setColor(CommonUtil.getVehicleColor(videoCarouselReport.getSignColor()));
                detail.setGroupName(org.getName());
                detail.setObjectType(videoCarouselReport.getObjectType());
            }
            //将全部数据存储到redis中
            String status = query.getStatus();
            if (!StringUtils.isEmpty(status) && list.size() > 0) {
                //如果status不为空，进行过滤
                Iterator<VideoInspectionDetail> iterator = list.iterator();
                VideoInspectionDetail next;
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (!next.getStatus().equals(status)) {
                        iterator.remove();
                    }
                }
            }
            RedisKey redisKey = HistoryRedisKeyEnum.VIDEO_CAROUSEL_REPORT_DETAIL.of(userUuid);
            RedisHelper.delete(redisKey);
            RedisHelper.addToList(redisKey, list);
            RedisHelper.expireKey(redisKey, 60 * 60 * 24);
            return RedisQueryUtil.getListToPage(getResult(query, list), query, list.size());
        } catch (Exception e) {
            log.error("调用paas_cloud接口错误！,返回的数据为：" + str, e);
            throw e;
        }
    }

    @Override
    public String batchExport(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception {
        Map<String, Map<String, String>> configMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(query.getMonitorIds())).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        //获取数据源
        List<VideoInspectionDetailDTO> dataList = Lists.newLinkedList();
        String str = HttpClientUtil.send(SAVE_VIDEO_INSPECTION_DETAILS_URL, query.getQueryParam());
        if (StringUtils.isBlank(str)) {
            return "暂未查询到可以导出的数据";
        }
        VideoCarouselReportDTO<VideoInspectionDetailDTO> dto =
            JSON.parseObject(str, new TypeReference<VideoCarouselReportDTO<VideoInspectionDetailDTO>>() {
            });

        if (dto == null) {
            return "暂未查询到可以导出的数据";
        }
        List<VideoInspectionDetailDTO> data = dto.getData();
        if (data == null || data.size() == 0) {
            return "暂未查询到可以导出的数据";
        }
        dataList.addAll(data);
        List<VideoInspectionDetail> details = Lists.newLinkedList();
        for (VideoInspectionDetailDTO vi : dataList) {
            List<VideoInspectionDetail> detail = vi.getDetail();
            if (detail == null || detail.size() == 0) {
                continue;
            }
            Map<String, String> config = configMap.get(vi.getMonitorId());
            if (config == null) {
                continue;
            }
            for (VideoInspectionDetail de : detail) {
                de.setMonitorName(config.get("name"));
                de.setColor(CommonUtil.getVehicleColor(Integer.parseInt(config.get("plateColor"))));
                de.setGroupName(config.get("orgName"));
                de.setObjectType(cacheManger.getVehicleType(config.get("vehicleType")).getType());
                details.add(de);
            }
        }
        if (dataList.isEmpty()) {
            return "暂未查询到可以导出的数据";
        }
        ExportExcelUtil.setResponseHead(response, "视频巡检明细统计报表");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, details, VideoInspectionDetail.class, null, response.getOutputStream()));
        return null;
    }

    @Override
    public String exportDetail(HttpServletResponse response, VideoCarouselReportQuery query) throws Exception {
        //获取数据源
        RedisKey key = HistoryRedisKeyEnum.VIDEO_CAROUSEL_REPORT_DETAIL.of(userService.getCurrentUserUuid());
        if (!RedisHelper.isContainsKey(key)) {
            return "请先执行查询数据";
        }
        List<VideoInspectionDetail> allDatas = RedisHelper.getList(key, VideoInspectionDetail.class);
        if (allDatas == null || allDatas.size() == 0) {
            return "请先执行查询数据";
        }
        ExportExcelUtil.setResponseHead(response, "视频巡检明细统计报表");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, allDatas, VideoInspectionDetail.class, null, response.getOutputStream()));
        return null;
    }

    private <K> List<K> getResult(VideoCarouselReportQuery query, List<K> list) {
        int listSize = list.size();
        int curPage = query.getPage().intValue();// 当前页
        int pageSize = query.getLimit().intValue(); // 每页条数
        int lst = (curPage - 1) * pageSize;// 遍历开始条数
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数
        List<K> result = Lists.newArrayList();
        for (int i = lst; i < ps; i++) {
            result.add(list.get(i));
        }
        return result;
    }
}
