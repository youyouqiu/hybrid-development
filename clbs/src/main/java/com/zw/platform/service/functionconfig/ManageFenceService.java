package com.zw.platform.service.functionconfig;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.functionconfig.ManageFenceInfo;
import com.zw.platform.domain.functionconfig.form.AdministrationForm;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.GpsLine;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.LineSegmentContentForm;
import com.zw.platform.domain.functionconfig.form.LineSpotForm;
import com.zw.platform.domain.functionconfig.form.MarkForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.functionconfig.form.RectangleForm;
import com.zw.platform.domain.functionconfig.form.SegmentContentForm;
import com.zw.platform.domain.functionconfig.form.TravelLineForm;
import com.zw.platform.domain.functionconfig.query.ManageFenceQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.ws.entity.t808.location.defence.T8080x8606;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/4.
 */
@Service
public interface ManageFenceService {

    JsonResultBean add(final LineForm form, String ipAddress) throws Exception;

    /**
     * 修改线路
     */
    JsonResultBean updateLine(final LineForm form, String ipAddress);

    JsonResultBean addMarker(final MarkForm form, String ipAddress);

    /**
     * 修改标注
     */
    JsonResultBean updateMarker(final MarkForm form, String ipAddress);

    JsonResultBean addCircles(final CircleForm form, String ipAddress);

    /**
     * 修改圆形区域
     */
    JsonResultBean updateCircle(final CircleForm form, String ipAddress);

    JsonResultBean addRectangles(final RectangleForm form, String ipAddress);

    /**
     * 修改矩形区域
     */
    JsonResultBean updateRectangle(final RectangleForm form, String ipAddress);

    /**
     * 新增多边形电子围栏
     */
    JsonResultBean addPolygons(final PolygonForm form, String ipAddress);

    /**
     * 修改多边形区域
     * @return void
     * @author Liubangquan
     */
    JsonResultBean updatePolygon(final PolygonForm form, String ipAddress);

    /**
     * 分页查询围栏信息
     */
    Page<ManageFenceInfo> findByPage(ManageFenceQuery query, String simpleQueryParam);

    /**
     * 删除 User
     */
    JsonResultBean delete(final String id, String ipAddress);

    int select(String id);

    /**
     * 关键点
     */
    void addMonitoringTag(LineSpotForm form);

    /**
     * 根据围栏id查找围栏的类型、名称
     * @return Object(对应类型的实体类)
     */
    String[] findType(String id);

    /**
     * 根据围栏id查询围栏具体类型id(shape)
     */
    String findFenceTypeId(String fenceId);

    JsonResultBean addSegment(LineSegmentContentForm form);

    boolean addSegmentContent(List<SegmentContentForm> segmentContentForms);

    boolean resetSegment(String lineId);

    boolean unbundleSegment(String lineId) throws Exception;

    /**
     * 根据标注名称查询标注实体
     */
    List<MarkForm> findMarkByName(String name);

    /**
     * 根据线路名称查询线路实体
     */
    List<LineForm> findLineByName(String name);

    /**
     * 根据矩形名称查询矩形实体
     */
    List<RectangleForm> findRectangleByName(String name);

    /**
     * 根据圆形名称查询圆形实体
     */
    List<CircleForm> findCircleByName(String name);

    /**
     * 根据多边形名称查询多边形实体
     */
    List<PolygonForm> findPolygonByName(String name);

    /**
     * 根据id查询标注围栏实体
     */
    MarkForm getMarkForm(String id);

    /**
     * 根据id查询线路围栏实体
     */
    LineForm getLineForm(String id);

    /**
     * 根据id查询矩形围栏实体
     */
    RectangleForm getRectangleForm(String id);

    /**
     * 根据id查询圆形围栏实体
     */
    CircleForm getCircleForm(String id);

    /**
     * 根据id查询多边形围栏实体
     */
    PolygonForm getPolygonForm(String id);

    /**
     * 根据id查询导航路线实体
     */
    TravelLineForm getTravelLineForm(String id);

    /**
     * 根据行驶路线查询导航路线实体
     */
    List<TravelLineForm> findTravelLineByName(String name);

    /**
     * 新增导航路线
     */
    JsonResultBean addTravelLine(final GpsLine form, String ipAddress);

    /**
     * 修改导航线路
     */
    JsonResultBean updateTravelLine(final GpsLine form, String ipAddress);

    /**
     * 新增行政区划
     */
    JsonResultBean addAdministration(final AdministrationForm form, String ipAddress);

    Map addCoordinates(MultipartFile multipartFile, String name, String type, String excursion) throws Exception;

    /**
     * 黑标809下发的线路信息新增
     * @param fence
     * @param lineId
     */
    String add809FenceLineInfo(T8080x8606 fence, String lineId, String vehicleId);

    /**
     * 黑标809下发的线路信息更新
     * @param fence
     * @param lineId
     */
    String update809FenceLineInfo(T8080x8606 fence, String lineId, String vehicleId);

    /**
     * 黑标809下发的线路信息新增
     * @param fence
     * @param vehicleInfo
     * @param lineUuid
     * @return
     */
    void add809Line(T8080x8606 fence, VehicleInfo vehicleInfo, String lineUuid);

    /**
     * 黑标809线路信息修改
     * @param fence
     * @param vehicleInfo
     */
    String update809FenceLineInfo(T8080x8606 fence, VehicleInfo vehicleInfo, String lineUuid);

    /**
     * 黑标809线路信息修改
     * @param fence
     * @param lineUuid
     */
    void update809Line(T8080x8606 fence, String lineUuid);

    /**
     * 下发上级平台的线路信息到终端
     * @param fence
     * @param vehicleInfo
     */
    Integer sendLineToVehicle(T8080x8606 fence, VehicleInfo vehicleInfo, String t809PlatId, String fenceConfigId);
}
