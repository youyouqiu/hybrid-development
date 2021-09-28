package com.cb.platform.service;

import com.cb.platform.domain.VehicleTravelForm;
import com.cb.platform.domain.VehicleTravelQuery;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * @author zhengjc
 * @date 2018/5/3 13:54
 */
public interface VehicleTravelService extends IpAddressService {

    void addVehicleTravel(VehicleTravelForm vehicleTravelForm);

    void deleteVehicleTravelById(String id);

    void updateVehicleTravel(VehicleTravelForm form);

    VehicleTravelForm findVehicleTravelById(String id);

    List<VehicleTravelForm> searchVehicleTravels(VehicleTravelQuery query, boolean doPage);

    boolean isRepeateTravelId(String travelId, String id);

    boolean export(String title, int type, HttpServletResponse res, List<VehicleTravelForm> vehicleTravelForms)
        throws IOException;

    JsonResultBean importVehicleTravel(MultipartFile file, String ipAddress);

    void generateTemplateType(OutputStream out)
        throws IOException;

    void deleteVehicleTravelByIds(String ids);

    void deleteVehicleTravelByVehicleIds(List<String> vehicleIds, boolean deleteSuccess);
}
