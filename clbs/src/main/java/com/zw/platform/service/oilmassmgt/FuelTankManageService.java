package com.zw.platform.service.oilmassmgt;
/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */


import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.FuelTankQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 油箱管理Service <p>Title: FuelTankManageService.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年10月24日下午4:28:42
 * @version 1.0
 */
public interface FuelTankManageService {

    /**
     * 分页查询油箱列表
     * @Title: findVehicleByPage
     * @param query
     * @return
     * @throws BusinessException
     * @return Page<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public Page<FuelTankForm> findFuelTankByPage(FuelTankQuery query) throws Exception;

    /**
     * 获取油杆传感器列表
     * @Title: findRodSensorList
     * @param boxHeight
     *            油箱高度
     * @return
     * @throws BusinessException
     * @return List<RodSensor>
     * @throws @author
     *             Liubangquan
     */
    public List<RodSensor> findRodSensorList(String boxHeight) throws Exception;

    /**
     * 根据传感器id查询传感器详细信息
     * @Title: getSensorDetail
     * @param sensorId
     * @return
     * @throws BusinessException
     * @return List<RodSensor>
     * @throws @author
     *             Liubangquan
     */
    public List<RodSensorForm> getSensorDetail(String sensorId) throws Exception;

    /**
     * 新增油箱
     * @Title: addFuelTank
     * @param form
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public JsonResultBean addFuelTank(FuelTankForm form, String ipAddress) throws Exception;

    /**
     * 保存油箱标定数据
     * @Title: addOilCalibration
     * @param bean
     * @param type
     *            单油箱还是双油箱：1-单油箱；2-双油箱
     * @return
     * @throws BusinessException
     * @return void
     * @throws @author
     *             Liubangquan
     */
    public void addOilCalibration(OilVehicleSetting bean) throws Exception;

    /**
     * 修改油箱标定数据
     * @Title: editOilCalibration
     * @param bean
     * @throws BusinessException
     * @return void
     * @throws @author
     *             Liubangquan
     */
    public void updateOilCalibration(DoubleOilVehicleSetting bean) throws Exception;

    /**
     * 根据油箱id读取油箱标定表数据
     * @Title: getOilCalibrationList
     * @param oilBoxVehicleId
     *            油量车辆设置表id
     * @return
     * @throws BusinessException
     * @return List<OilCalibrationForm>
     * @throws @author
     *             Liubangquan
     */
    public List<OilCalibrationForm> getOilCalibrationList(String oilBoxVehicleId) throws Exception;

    /**
     * 删除油箱
     * @Title: deleteFuelTankById
     * @param id
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public JsonResultBean deleteFuelTankById(String id, String ipAddress) throws Exception;

    /**
     * 批量删除油箱
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    public JsonResultBean deleteBatchFuelTankById(String ids, String ipAddress) throws Exception;

    /**
     * 判断油箱是否已经被绑定
     * @Title: checkIsBond
     * @param id
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean findIsBond(String id) throws Exception;

    /**
     * 查询油箱详细信息
     * @Title: getFuelTankDetail
     * @param id
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getFuelTankDetail(String id) throws Exception;

    /**
     * 修改油箱
     * @Title: editFuelTank
     * @param form
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public JsonResultBean updateFuelTank(FuelTankForm form,String ipAddress) throws Exception;

    /**
     * 导入油箱标定数据
     * @Title: importOilCalibration
     * @param file
     * @return
     * @throws BusinessException
     * @throws InvalidFormatException
     * @throws IOException
     * @return FuelTankForm
     * @throws @author
     *             Liubangquan
     */
    public FuelTankForm importOilCalibration(MultipartFile file) throws Exception;

    /**
     * 根据车辆id获取油箱信息及其标定
     * @Title: getOilCalibrationByVid
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getOilCalibrationByVid(String vehicleId) throws Exception;

    /**
     * 根据车辆与油箱绑定id获取与车辆绑定的油箱的标定数据
     * @Title: getOilCalibrationByVid
     * @param id
     * @return
     * @throws BusinessException
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    public List<FuelTankForm> getOilCalibrationByBindId(String id) throws Exception;

    /**
     * 根据油箱形状编号获取油箱形状名称
     * @Title: getOilBoxShapeStr
     * @param shape
     * @return
     * @throws BusinessException
     * @return String
     * @throws @author
     *             Liubangquan
     */
    public String getOilBoxShapeStr(String shape) throws Exception;

    /**
     * 根据油箱型号查询油箱
     * @Title: getOilBoxByType
     * @param type
     * @return
     * @throws BusinessException
     * @return FuelTankForm
     * @throws @author
     *             Liubangquan
     */
    public FuelTankForm getOilBoxByType(String type) throws Exception;

    public FuelTankForm getOilBoxByType(String id, String type) throws Exception;

    /**
     * 导入油箱
     * @param multipartFile
     * @return
     */
    Map importTank(MultipartFile multipartFile,String ipAddress) throws Exception;

    /**
     * 生成导入模板
     * @param filePath
     * @return
     */
    boolean generateTankTemplate(HttpServletResponse response) throws Exception;

    /**
     * 导出
     * @param title
     *            excel名称
     * @param type
     *            导出类型（1:导出数据；2：导出模板）
     * @param filePath
     *            文件
     * @return
     */
    boolean exportTank(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 根据车辆id获取与其绑定的油箱信息
     * @Title: getFuelTankDetailByVehicleId
     * @param vehicleId
     * @return
     * @return List<FuelTankForm>
     * @throws @author
     *             Liubangquan
     */
    List<FuelTankForm> getFuelTankDetailByVehicleId(String vehicleId) throws Exception;

    /**
     * 校验油箱是否被绑定
     * @Title: checkBoxBound
     * @param oilBoxId
     * @return
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    boolean findBoxBound(String oilBoxId) throws Exception;

    /**
     * 根据id查询油箱
     * @param id
     * @return
     */
    FuelTank findFuelTankById(String id) throws Exception;

}
