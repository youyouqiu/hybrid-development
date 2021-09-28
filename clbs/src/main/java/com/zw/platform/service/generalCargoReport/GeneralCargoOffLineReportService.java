package com.zw.platform.service.generalCargoReport;

import com.zw.platform.domain.generalCargoReport.CargoOffLineReport;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

@Service
public interface GeneralCargoOffLineReportService {
    /**
     * 查询离线报表信息
     * @param vehicleSet 车辆id
     * @param day        天数
     * @return CargoOffLineReport
     * @throws Exception e
     */
    List<CargoOffLineReport> getList(Set<String> vehicleSet, Integer day) throws Exception;

    /**
     * 离线查询报表导出
     * @param title         title
     * @param type          类型
     * @param res           res
     * @param offLineReport List<CargoOffLineReport
     * @return boolean
     * @throws Exception e
     */
    boolean export(String title, int type, HttpServletResponse res, List<CargoOffLineReport> offLineReport)
        throws Exception;

    /**
     * 获取导出的list
     * @param simpleQueryParam 模糊查询条件
     */
    List<CargoOffLineReport> getExportList(String simpleQueryParam, List<CargoOffLineReport> list);
}
