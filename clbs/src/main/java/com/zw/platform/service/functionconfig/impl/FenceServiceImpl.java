package com.zw.platform.service.functionconfig.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.functionconfig.Administration;
import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.functionconfig.Line;
import com.zw.platform.domain.functionconfig.LineContent;
import com.zw.platform.domain.functionconfig.LinePassPoint;
import com.zw.platform.domain.functionconfig.LineSpot;
import com.zw.platform.domain.functionconfig.Mark;
import com.zw.platform.domain.functionconfig.Polygon;
import com.zw.platform.domain.functionconfig.Rectangle;
import com.zw.platform.domain.functionconfig.TravelLine;
import com.zw.platform.domain.functionconfig.query.LineSegmentInfo;
import com.zw.platform.repository.modules.FenceDao;
import com.zw.platform.service.functionconfig.AdministrationService;
import com.zw.platform.service.functionconfig.CircleService;
import com.zw.platform.service.functionconfig.FenceService;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.service.functionconfig.MarkService;
import com.zw.platform.service.functionconfig.PolygonService;
import com.zw.platform.service.functionconfig.RectangleService;
import com.zw.platform.service.functionconfig.TravelLineService;
import com.zw.platform.util.common.MethodLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author wangying
 */
@Service
public class FenceServiceImpl implements FenceService {
    @Autowired
    private FenceDao fenceDao;

    @Autowired
    MarkService markService;

    @Autowired
    LineService lineService;

    @Autowired
    RectangleService rectangleService;

    @Autowired
    PolygonService polygonService;

    @Autowired
    CircleService circleService;

    @Autowired
    AdministrationService administrationService;

    @Autowired
    TravelLineService travelLineService;

    /**
     * 查询围栏
     */
    @MethodLog(name = "查询围栏", description = "查询围栏")
    public List<FenceInfo> findFence() throws Exception {

        return fenceDao.findFence();
    }

    /**
     * 查询围栏类型
     */
    @MethodLog(name = "查询围栏类型", description = "查询围栏类型")
    public List<String> findFenceType() throws Exception {
        return fenceDao.findType();
    }

    /**
     * 根据类型查询围栏
     */
    @MethodLog(name = "根据类型查询围栏", description = "根据类型查询围栏")
    public List<Map<String, Object>> findFenceByType(String type, List<String> orgIds) throws Exception {
        if (type != null && orgIds != null && !orgIds.isEmpty()) {
            return fenceDao.findFenceByType(type, orgIds);
        }
        return null;
    }

    @Override
    public FenceInfo findFenceInfoById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            return fenceDao.findFenceInfoById(id);
        }
        return null;
    }

    @Override
    public int checkBindByOrgId(String orgId) {
        return fenceDao.checkBindByOrgId(orgId);
    }

    @Override
    public JSONObject getFenceDetail(String id, String type) {
        JSONObject msg = new JSONObject();
        if ("zw_m_marker".equals(type)) {
            // 查询标注详情
            Mark mark = markService.findMarkById(id);
            if (mark != null) {
                msg.put("fenceType", type);
                msg.put("fenceData", mark);
            }
        } else if ("zw_m_line".equals(type)) {
            // 查询线的详情
            List<LineContent> lineList = lineService.findLineContentsById(id);
            List<LineSpot> lineSpotList = lineService.findLineSpotByLid(id);
            List<LineSegmentInfo> lineSegmentList = lineService.findSegmentContentByLid(id);
            if (CollectionUtils.isNotEmpty(lineList)) {
                msg.put("fenceType", type);
                msg.put("fenceData", lineList);
                msg.put("lineSpot", lineSpotList);
                msg.put("lineSegment", lineSegmentList);
            }
        } else if ("zw_m_rectangle".equals(type)) {
            // 查询矩形的详情
            Rectangle rectangle = rectangleService.getRectangleByID(id);
            if (rectangle != null) {
                msg.put("fenceType", type);
                msg.put("fenceData", rectangle);
            }
        } else if ("zw_m_circle".equals(type)) {
            // 查询圆形的详情
            Circle circle = circleService.getCircleByID(id);
            if (circle != null) {
                msg.put("fenceType", type);
                msg.put("fenceData", circle);
            }
        } else if ("zw_m_polygon".equals(type)) {
            // 查询多边形的详情
            List<Polygon> polygonList = polygonService.getPolygonByID(id);
            if (polygonList != null && polygonList.size() != 0) {
                msg.put("fenceType", type);
                msg.put("fenceData", polygonList);
            }
        } else if ("zw_m_administration".equals(type)) {
            List<List<List<String>>> administration = administrationService.getAdministrationByID(id);
            if (administration != null && administration.size() != 0) {
                msg.put("fenceType", type);
                msg.put("fenceData", administration);
                msg.put("aId", id);
            }
        } else if ("zw_m_travel_line".equals(type)) {
            // 查询导航线路详情
            // 根据id查询途经点的信息
            List<LinePassPoint> passPointList = travelLineService.getPassPointById(id);
            // 根据id查询导航路线信息
            TravelLine travelLine = travelLineService.getTravelLineById(id);
            List<LineContent> allPoints = travelLineService.getAllPointsById(id);
            if (passPointList != null && passPointList.size() != 0) {
                msg.put("fenceType", type);
                msg.put("passPointData", passPointList);
                msg.put("travelLine", travelLine);
                msg.put("allPoints", allPoints);
            } else {
                msg.put("fenceType", type);
                msg.put("travelLine", travelLine);
                msg.put("allPoints", allPoints);
            }
        }
        return msg;
    }

    @Override
    public JSONObject previewFence(String fenceId, String fenceType) {
        JSONObject msg = new JSONObject();
        if (Objects.equals("zw_m_marker", fenceType)) {
            //查询标准详情
            Mark mark = markService.findMarkById(fenceId);
            if (mark != null) {
                msg.put("fenceType", fenceType);
                msg.put("fenceData", mark);
            }
        } else if (Objects.equals("zw_m_line", fenceType)) {
            // 查询线的详情
            List<LineContent> lineList = lineService.findLineContentById(fenceId);
            Line line = lineService.findLineById(fenceId);
            if (CollectionUtils.isNotEmpty(lineList)) {
                msg.put("fenceType", fenceType);
                msg.put("fenceData", lineList);
                msg.put("line", line);
            }
        } else if (Objects.equals("zw_m_rectangle", fenceType)) {
            // 查询矩形的详情
            Rectangle rectangle = rectangleService.getRectangleByID(fenceId);
            if (rectangle != null) {
                msg.put("fenceType", fenceType);
                msg.put("fenceData", rectangle);
            }
        } else if (Objects.equals("zw_m_circle", fenceType)) {
            // 查询圆形的详情
            Circle circle = circleService.getCircleByID(fenceId);
            if (circle != null) {
                msg.put("fenceType", fenceType);
                msg.put("fenceData", circle);
            }
        } else if (Objects.equals("zw_m_polygon", fenceType)) {
            // 查询多边形的详情
            List<Polygon> polygonList = polygonService.getPolygonByID(fenceId);
            Polygon polygon = polygonService.findPolygonById(fenceId);
            if (CollectionUtils.isNotEmpty(polygonList)) {
                polygonList.get(0).setDescription(polygon.getDescription());
                msg.put("fenceType", fenceType);
                msg.put("fenceData", polygonList);
                msg.put("polygon", polygon);
            }
        } else if (Objects.equals("zw_m_administration", fenceType)) {
            Administration administration = administrationService.findAdministrationById(fenceId);
            if (administration != null) {
                msg.put("fenceType", fenceType);
                msg.put("administration", administration);
            }
        } else if (Objects.equals("zw_m_travel_line", fenceType)) {
            // 查询导航线路的详情
            // 根据id查询途经点的信息
            List<LinePassPoint> passPointList = travelLineService.getPassPointById(fenceId);
            // 根据id查询导航路线信息
            TravelLine travelLine = travelLineService.getTravelLineById(fenceId);
            // 根据id查询所有点信息
            List<LineContent> allPoinsList = travelLineService.getAllPointsById(fenceId);
            if (CollectionUtils.isNotEmpty(passPointList)) {
                msg.put("fenceType", fenceType);
                msg.put("passPointData", passPointList);
                msg.put("allPoinsData", allPoinsList);
                msg.put("travelLine", travelLine);
            } else {
                msg.put("fenceType", fenceType);
                msg.put("travelLine", travelLine);
                msg.put("allPoinsData", allPoinsList);
            }
        }
        return msg;
    }

}
