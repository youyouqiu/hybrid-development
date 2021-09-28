package com.zw.platform.service.other.protocol.jl;

import com.github.pagehelper.Page;
import com.zw.platform.domain.other.protocol.jl.dto.ViolateVehicleDTO;
import com.zw.platform.domain.other.protocol.jl.query.SingleViolateVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehicleExportReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehiclePageReq;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehicleReq;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author create by zhouzongbo on 2020/6/12.
 */
public interface ViolateVehicleService {
    /**
     * 单个/统一设置-违规车辆上传
     * @param vehicleReq vehicleReq
     * @return JsonResultBean
     * @throws Exception ex
     */
    JsonResultBean insertViolateUpload(ViolateVehicleReq vehicleReq) throws Exception;

    /**
     * 批量(分别设置)违规车辆上传
     * @param vehicleReqList vehicleReqList
     * @return JsonResultBean
     * @throws Exception ex
     */
    JsonResultBean insertBatchViolateUpload(List<SingleViolateVehicleReq> vehicleReqList) throws Exception;

    /**
     * 分页查询
     * @param req req
     * @return page
     */
    Page<ViolateVehicleDTO> listViolateVehicle(ViolateVehiclePageReq req);

    /**
     * 导出违规车辆
     * @param res  res
     * @param req req
     * @throws IOException ex
     */
    void exportViolateList(HttpServletResponse res, ViolateVehicleExportReq req) throws IOException;

    /**
     * 获取企业信息数据
     * @param orgId orgId
     * @return AloneCorpInfoResp
     * @throws Exception ex
     */
    JsonResultBean getAloneCorpInfo(String orgId) throws Exception;
}
