package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.controller.oilmassmgt.FuelTankManageController;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.FuelTankQuery;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.OilMassMgtUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 油箱管理Controller <p>Title: fuelTankManageController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:24:19
 */
@RestController
@RequestMapping("/swagger/v/oilmassmgt/fueltankmgt")
@Api(tags = { "油箱管理" }, description = "油箱相关api接口")
public class SwaggerFuelTankManageController implements ServletContextAware {
    private static Logger log = LogManager.getLogger(FuelTankManageController.class);

    private static final String DELETE_ERROR_MSSAGE = "部分油箱已经和车辆绑定了，到【油量车辆设置】中解除绑定后才可以删除哟！";

    // Spring这里是通过实现ServletContextAware接口来注入ServletContext对象
    private ServletContext servletContext;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 查询油箱列表
     */
    @ApiOperation(value = "分页查询油箱列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照油箱型号进行模糊搜索", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(final FuelTankQuery query) {
        try {
            Page<FuelTankForm> result = fuelTankManageService.findFuelTankByPage(query);
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询分组（findFuelTankByPage）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 油箱新增页面
     */
    @ApiOperation(value = "保存油箱信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "type", value = "油箱型号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "shape", value = "油箱形状", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "boxLength", value = "长度(mm)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "width", value = "宽度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "height", value = "高度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "thickness", value = "壁厚(mm)", required = true, paramType = "query",
            dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "realVolume", value = "油箱容量(L)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "theoryVolume", value = "理论容积(L)", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addFuelTank(@Validated({ ValidGroupAdd.class }) FuelTankForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            FuelTankForm f = fuelTankManageService.getOilBoxByType(form.getType());
            if (null != f) {
                return new JsonResultBean(JsonResultBean.FAULT, "油箱型号已存在，请重新输入！");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            fuelTankManageService.addFuelTank(form, ipAddress);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("新增油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 删除油箱
     */
    @ApiOperation(value = "根据id删除油箱", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/delete_{id}" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        try {
            // 判断油箱是否已经和车辆绑定，如果已经绑定，则给予提示
            boolean isBond = fuelTankManageService.findIsBond(id);
            String ipAddress = new GetIpAddr().getIpAddr(request);
            if (!isBond) { // 没有绑定
                fuelTankManageService.deleteFuelTankById(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, DELETE_ERROR_MSSAGE);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("删除油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据ids批量删除油箱", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除id集合String(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(HttpServletRequest request) {
        try {
            String items = request.getParameter("deltems");
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            if (!"".equals(items)) {
                return fuelTankManageService.deleteBatchFuelTankById(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 查询油箱详细信息
     */
    @ApiOperation(value = "根据id查询油箱详细信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/fuelTankDetail_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detail(@PathVariable("id") String id) {
        try {
            List<FuelTankForm> list = fuelTankManageService.getFuelTankDetail(id);
            if (null != list && list.size() > 0) {
                list.get(0).setShapeStr(fuelTankManageService.getOilBoxShapeStr(list.get(0).getShape()));
                return new JsonResultBean(list.get(0));

            } else {
                return new JsonResultBean();
            }
        } catch (Exception e) {
            log.error("查询油箱详细信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 油箱管理-修改功能提交
     */
    @ApiOperation(value = "修改油箱信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "油箱id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "油箱型号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "shape", value = "油箱形状", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "boxLength", value = "长度(mm)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "width", value = "宽度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "height", value = "高度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "thickness", value = "壁厚(mm)", required = true, paramType = "query",
            dataType = "string", defaultValue = "3"),
        @ApiImplicitParam(name = "realVolume", value = "油箱容量(L)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "theoryVolume", value = "理论容积(L)", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public JsonResultBean edit(@Validated({ ValidGroupUpdate.class }) final FuelTankForm form,
        final BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            FuelTankForm f = fuelTankManageService.getOilBoxByType(form.getId(), form.getType());
            if (null != f) {
                return new JsonResultBean(JsonResultBean.FAULT, "油箱型号已存在，请重新输入！");
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return fuelTankManageService.updateFuelTank(form, ipAddress);
        } catch (Exception e) {
            log.error("修改油箱信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据页面填写的长度、宽度、高度、壁厚计算油箱的理论容积
     */
    @ApiOperation(value = "根据长度、宽度、高度、壁厚计算油箱的理论容积", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "length", value = "长度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "width", value = "宽度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "height", value = "高度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "thickness", value = "壁厚", required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "box", value = "油箱形状", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "buttomRadius", value = "下圆角半径", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "topRadius", value = "上圆角半径", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getTheoryVol" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTheoryVol(FuelTank fuelTank) {
        JSONObject msg = new JSONObject();
        String theoryVol = OilMassMgtUtil.get_theory_Volume_by_shape(fuelTank);
        msg.put("theoryVol", theoryVol);
        return new JsonResultBean(msg);
    }

    /**
     * 计算油箱标定数据
     */
    @ApiOperation(value = "计算油箱标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "length", value = "长度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "width", value = "宽度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "height", value = "高度(mm)", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "thickness", value = "壁厚", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "shape", value = "油箱形状", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorLength", value = "传感器长度(mm)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets", value = "标定组数", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "upperBlindZone", value = "上盲区", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "lowerBlindArea", value = "下盲区", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "measuringRange", value = "量程", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "realVolume", value = "实际容积", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "theoryVolume", value = "理论容积", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id", value = "油箱车辆绑定id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "boxNum", value = "油箱编号(1表示油箱1/2表示油箱2)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "buttomRadius", value = "下圆角半径", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "topRadius", value = "上圆角半径", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/calCalibration" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean calCalibration(OilVehicleSetting oilVehicleSetting) {
        try {
            JSONObject msg = new JSONObject();
            fuelTankManageService.addOilCalibration(oilVehicleSetting);
            List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(oilVehicleSetting.getId());
            msg.put("result", list);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("计算油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 修改油箱标定数据
     */
    @ApiOperation(value = "编辑标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oilValues", value = "油量值，一组值以逗号隔开", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeights", value = "油量高度值，一组高度值以逗号隔开", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id", value = "油箱1与车辆关联id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "油箱1id)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilValue2s", value = "油箱2油量值，一组值以逗号隔开", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeights2", value = "油箱2油量高度值，一组高度值以逗号隔开", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "id2", value = "油箱2与车辆关联id", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId2", value = "油箱2id)", required = false, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/editOilCal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editOilCal(DoubleOilVehicleSetting bean) {
        try {
            JSONObject msg = new JSONObject();
            fuelTankManageService.updateOilCalibration(bean);
            msg.put("result", bean);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修改油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 导入标定数据
     */
    @ApiOperation(value = "导入标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FuelTankForm form = fuelTankManageService.importOilCalibration(file);
            JSONObject msg = new JSONObject();

            if (Converter.toBlank(form.getBoxLength()).equals("") || Converter.toBlank(form.getWidth()).equals("")
                || Converter.toBlank(form.getHeight()).equals("") || Converter.toBlank(form.getSensorLength())
                .equals("") || Converter.toBlank(form.getThickness()).equals("") || Converter.toBlank(
                form.getOilLevelHeightList()).equals("") || Converter.toBlank(form.getOilValueList()).equals("")) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            FuelTank fuelTank = new FuelTank();
            fuelTank.setShape("1");
            fuelTank.setBoxLength(form.getBoxLength());
            fuelTank.setWidth(form.getWidth());
            fuelTank.setHeight(form.getHeight());
            fuelTank.setThickness(form.getThickness());
            fuelTank.setButtomRadius(form.getButtomRadius());
            fuelTank.setTopRadius(form.getTopRadius());
            String theoryVol = OilMassMgtUtil.get_theory_Volume_by_shape(fuelTank);
            form.setTheoryVolume(theoryVol);
            form.setRealVolume(theoryVol);
            msg.put("result", form);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("标定数据导入异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 导出标定模板:将文件存放到服务器上，然后从服务器上下载
     */
    @ApiOperation(value = "导出标定模板", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/exportTemplate", method = RequestMethod.POST)
    public void fileDownload(HttpServletResponse response, String value) {
        ServletOutputStream out = null;
        FileInputStream inputStream = null;
        try {
            response.setCharacterEncoding("utf-8");
            // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
            String path = servletContext.getRealPath("/");

            String filename = "";
            // 通过文件路径获得File对象(假如此路径中有一个download.pdf文件)
            File file;
            if (Converter.toBlank(value).equals("1")) { // AD值标定法
                filename = "油箱标定导入表-AD值标定法";
                file = new File(path + "file/vas/" + "01.油箱标定导入表-AD值标定法.xlsx");
            } else { // 标尺标定法
                filename = "油箱标定导入表-标尺标定法";
                file = new File(path + "file/vas/" + "02.油箱标定导入表-标尺标定法.xlsx");
            }
            // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("application/msexcel;charset=UTF-8");
            // 2.设置文件头：最后一个参数是设置下载文件名(假如我们叫a.pdf)
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xlsx");

            if (file != null) {
                inputStream = new FileInputStream(file);
            }
            // 3.通过response获取ServletOutputStream对象(out)
            out = response.getOutputStream();
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1) {
                b = inputStream.read(buffer);
                // 4.写到输出流(out)中
                out.write(buffer, 0, b);
            }

            out.close();
            out.flush();
            inputStream.close();
        } catch (IOException e) {
            log.error("标定模板导出异常", e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 去重
     */
    @ApiOperation(value = "判断油箱型号是否已存在", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String type) {
        try {
            FuelTankForm form = fuelTankManageService.getOilBoxByType(type);
            return null == form;
        } catch (Exception e) {
            log.error("判断油箱型号异常", e);
            return true;
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @ApiOperation(value = "导入油箱数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/importTank", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTank(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            Map resultMap = fuelTankManageService.importTank(file, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入油箱数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 下载模板
     */
    @ApiOperation(value = "下载导入油箱模块", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/downloadTank", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "油箱信息列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.generateTankTemplate(response);
        } catch (Exception e) {
            log.error("下载导入油箱模块异常", e);
        }
    }

    /**
     * 导出
     */
    @ApiOperation(value = "导出油箱信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/exportTank", method = RequestMethod.GET)
    public void exportTank(HttpServletResponse response) {
        try {
            String filename = "油箱信息列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.exportTank(null, 1, response);
        } catch (Exception e) {
            log.error("导出油箱信息列表异常", e);
        }
    }

}
