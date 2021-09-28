package com.zw.platform.controller.oilmassmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.FuelTankQuery;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.OilMassMgtUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * 油箱管理Controller <p>Title: fuelTankManageController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:24:19
 */
@Controller
@RequestMapping("/v/oilmassmgt/fueltankmgt")
public class FuelTankManageController implements ServletContextAware {
    private static Logger log = LogManager.getLogger(FuelTankManageController.class);

    private static final String DELETE_ERROR_MSSAGE = "该油箱已经和车辆绑定了，到【油量车辆设置】中解除绑定后才可以删除哟！";

    private static final String LIST_PAGE = "vas/oilmassmgt/fueltankmanage/list";

    private static final String ADD_PAGE = "vas/oilmassmgt/fueltankmanage/add";

    private static final String DETAIL_PAGE = "vas/oilmassmgt/fueltankmanage/detail";

    private static final String EDIT_PAGE = "vas/oilmassmgt/fueltankmanage/edit";

    private static final String IMPORT_PAGE = "vas/oilmassmgt/fueltankmanage/import";

    private static final String OIL_CALIBRATION_PAGE = "vas/oilmassmgt/fueltankmanage/oilCalibration";

    private static final String IMPORT_TANK_PAGE = "vas/oilmassmgt/fueltankmanage/importTank";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private String oilLevelHeights = "";

    private String oilValues = "";

    private String oilLevelHeights2 = "";

    private String oilValues2 = "";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${delete.fuel.tank}")
    private String deleteFuelTank;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${add.fail}")
    private String addFail;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    // Spring这里是通过实现ServletContextAware接口来注入ServletContext对象
    private ServletContext servletContext;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 进入油箱管理列表页面
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: listPage
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    /**
     * 查询油箱列表
     * @param query
     * @return PageGridBean
     * @throws BusinessException
     * @throws NamingException
     * @throws @author           Liubangquan
     * @Title: list
     */
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
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: add
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String add() throws BusinessException {
        return ADD_PAGE;
    }

    /**
     * 油箱新增页面
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: add
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean addFuelTank(FuelTankForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fuelTankManageService.addFuelTank(form, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, addFail);
            }
        } catch (Exception e) {
            log.error("新增油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 初始化油杆传感器列表
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: pageBean
     */
    @RequestMapping(value = { "/sensorList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initSensorList(String boxHeight) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("sensorList", fuelTankManageService.findRodSensorList(boxHeight));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("初始化油杆传感器列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据传感器id查询传感器详细信息
     * @param sensorId
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: getSensorDetail
     */
    @RequestMapping(value = { "/sensorDetail" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorDetail(String sensorId) {
        try {
            JSONObject msg = new JSONObject();
            List<RodSensorForm> list = fuelTankManageService.getSensorDetail(sensorId);
            if (null != list && list.size() > 0) {
                msg.put("sensorDetail", list.get(0));
                int oddEvenCheck = list.get(0).getOddEvenCheck() == null ? 3 : list.get(0).getOddEvenCheck();
                int compensationCanMake =
                    list.get(0).getCompensationCanMake() == null ? 2 : list.get(0).getCompensationCanMake();
                String filteringFactor = list.get(0).getFilteringFactor();
                String baudRate = list.get(0).getBaudRate();
                if (oddEvenCheck == 1) {
                    list.get(0).setOddEvenCheckStr("奇校验");
                } else if (oddEvenCheck == 2) {
                    list.get(0).setOddEvenCheckStr("偶校验");
                } else {
                    list.get(0).setOddEvenCheckStr("无校验");
                }
                if (compensationCanMake == 1) {
                    list.get(0).setCompensationCanMakeStr("使能");
                } else {
                    list.get(0).setCompensationCanMakeStr("禁能");
                }
                if ("01".equals(filteringFactor)) {
                    list.get(0).setFilteringFactorStr("实时");
                } else if ("02".equals(filteringFactor)) {
                    list.get(0).setFilteringFactorStr("平滑");
                } else {
                    list.get(0).setFilteringFactorStr("平稳");
                }
                if ("01".equals(baudRate)) {
                    list.get(0).setBaudRateStr("2400");
                } else if ("02".equals(baudRate)) {
                    list.get(0).setBaudRateStr("4800");
                } else if ("03".equals(baudRate)) {
                    list.get(0).setBaudRateStr("9600");
                } else if ("04".equals(baudRate)) {
                    list.get(0).setBaudRateStr("19200");
                } else if ("05".equals(baudRate)) {
                    list.get(0).setBaudRateStr("38400");
                } else if ("06".equals(baudRate)) {
                    list.get(0).setBaudRateStr("57600");
                } else if ("07".equals(baudRate)) {
                    list.get(0).setBaudRateStr("115200");
                } else {
                    list.get(0).setBaudRateStr("9600");
                }
            } else {
                msg.put("sensorDetail", null);
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询传感器详细信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 删除油箱
     * @param id
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: deleteFuelTank
     */
    @RequestMapping(value = { "/delete_{id}" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") String id) {
        try {
            if (!"".equals(id)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fuelTankManageService.deleteFuelTankById(id, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: deleteMore
     */
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fuelTankManageService.deleteBatchFuelTankById(items, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("批量删除油箱异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询油箱详细信息
     * @param id
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: detail
     */
    @RequestMapping(value = { "/fuelTankDetail_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView detail(@PathVariable("id") String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            List<FuelTankForm> list = fuelTankManageService.getFuelTankDetail(id);
            if (null != list && list.size() > 0) {
                list.get(0).setShapeStr(fuelTankManageService.getOilBoxShapeStr(list.get(0).getShape()));
                mav.addObject("result", list.get(0));

            } else {
                mav.addObject("result", null);
            }
            return mav;
        } catch (Exception e) {
            log.error("查询油箱详细信息弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 油箱管理-修改页面
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: editPage
     */
    @RequestMapping(value = "/edit_{id}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView editPage(@PathVariable final String id) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            List<FuelTankForm> list = fuelTankManageService.getFuelTankDetail(id);
            if (null != list && list.size() > 0) {
                mav.addObject("result", list.get(0));
            } else {
                mav.addObject("result", null);
            }
            return mav;
        } catch (Exception e) {
            log.error("油箱管理修改弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 油箱管理-修改功能提交
     * @param form
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: edit
     */
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean edit(final FuelTankForm form) {
        try {
            if (form != null) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fuelTankManageService.updateFuelTank(form, ip);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, setFail);
            }
        } catch (Exception e) {
            log.error("修改油箱信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据页面填写的长度、宽度、高度、壁厚计算油箱的理论容积
     * @param fuelTank 油箱实体类
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: getTheoryVol
     */
    @RequestMapping(value = { "/getTheoryVol" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTheoryVol(FuelTank fuelTank) throws Exception {
        JSONObject msg = new JSONObject();
        String theoryVol = OilMassMgtUtil.get_theory_Volume_by_shape(fuelTank);
        msg.put("theoryVol", theoryVol);
        return new JsonResultBean(msg);
    }

    /**
     * 计算油箱标定数据
     * @param bean 油箱绑定实体
     * @return JosnResultBean
     * @Title: calCalibration
     */
    @RequestMapping(value = { "/calCalibration" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean calCalibration(OilVehicleSetting bean) {
        try {
            if (bean != null) {
                JSONObject msg = new JSONObject();
                fuelTankManageService.addOilCalibration(bean);
                List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(bean.getId());
                msg.put("result", list);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("计算油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 油箱标定数据详情
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: calibrationDetail
     */
    @RequestMapping(value = { "/calibrationDetail" }, method = RequestMethod.GET)
    public String calibrationDetail() throws BusinessException {
        return OIL_CALIBRATION_PAGE;
    }

    /**
     * 修改油箱标定数据
     * @param bean
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: addOilCal
     */
    @RequestMapping(value = { "/editOilCal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editOilCal(DoubleOilVehicleSetting bean) {
        try {
            JSONObject msg = new JSONObject();
            if (StringUtils.isNotBlank(bean.getOilBoxId())) { // 单油箱
                oilLevelHeights = bean.getOilLevelHeights();
                oilValues = bean.getOilValues();
            }
            if (StringUtils.isNotBlank(bean.getOilBoxId2())) { // 双油箱
                oilLevelHeights2 = bean.getOilLevelHeights2();
                oilValues2 = bean.getOilValues2();
            }
            fuelTankManageService.updateOilCalibration(bean);
            msg.put("result", bean);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修改油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 标定详情中点击提交后，重新赋值到详情界面
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: getNewCalData
     */
    @RequestMapping(value = { "/getNewCalData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getNewCalData(String curBox) throws Exception {
        JSONObject msg = new JSONObject();
        if ("1".equals(curBox)) {
            msg.put("oilLevelHeights", oilLevelHeights);
            msg.put("oilValues", oilValues);
        }
        if ("2".equals(curBox)) {
            msg.put("oilLevelHeights2", oilLevelHeights2);
            msg.put("oilValues2", oilValues2);
        }
        return new JsonResultBean(msg);
    }

    /**
     * 导入标定数据界面
     * @return String
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: importPage
     */
    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public String importPage() throws BusinessException {
        return IMPORT_PAGE;
    }

    /**
     * 导入标定数据
     * @param file
     * @return JsonResultBean
     * @throws BusinessException
     * @throws IOException
     * @throws InvalidFormatException
     * @throws @author                Liubangquan
     * @Title: importDevice
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importDevice(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FuelTankForm form = fuelTankManageService.importOilCalibration(file);
            JSONObject msg = new JSONObject();
            if (Converter.toBlank(form.getBoxLength()).equals("") || Converter.toBlank(form.getWidth()).equals("")
                || Converter.toBlank(form.getHeight()).equals("") || Converter.toBlank(form.getSensorLength())
                .equals("") || Converter.toBlank(form.getThickness()).equals("") || Converter
                .toBlank(form.getOilLevelHeightList()).equals("") || Converter.toBlank(form.getOilValueList())
                .equals("")) {
                msg.put("flag", 0);
                msg.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
                msg.put("resultInfo", "导入失败！");
                String errMsg = "导入结果：导入失败!<br/>请检查数据是否填写正确或将导入文件按照模板格式整理后再导入";
                return new JsonResultBean(JsonResultBean.FAULT, errMsg);
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
            log.error("导入标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出标定模板:将文件存放到服务器上，然后从服务器上下载
     * @param response
     * @return void
     * @throws UnsupportedEncodingException
     * @throws @author                      Liubangquan
     * @Title: fileDownload
     */
    @RequestMapping(value = "/exportTemplate", method = RequestMethod.POST)
    public void fileDownload(HttpServletResponse response, String value) {
        try {
            response.setCharacterEncoding("utf-8");
            // 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
            String path = servletContext.getRealPath("/");

            String filename = "";
            // 通过文件路径获得File对象(假如此路径中有一个download.pdf文件)
            File file = null;
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
            ServletOutputStream out;

            FileInputStream inputStream = new FileInputStream(file);
            // 3.通过response获取ServletOutputStream对象(out)
            out = response.getOutputStream();
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1) {
                b = inputStream.read(buffer);
                // 4.写到输出流(out)中
                out.write(buffer, 0, b);
            }
            inputStream.close();
            out.close();
            out.flush();
        } catch (IOException e) {
            log.error("导出标定模板异常", e);
        }
    }

    /**
     * 去重
     * @param type
     * @return JsonResultBean
     * @throws BusinessException
     * @throws InvalidFormatException
     * @throws IOException
     * @throws @author                Liubangquan
     * @Title: repetition
     */
    @RequestMapping(value = "/repetition", method = RequestMethod.POST)
    @ResponseBody
    public boolean repetition(String type, String oilBoxId) {
        try {
            FuelTankForm form = fuelTankManageService.getOilBoxByType(type);
            if (oilBoxId == null || "".equals(oilBoxId)) {
                //新增
                return form == null;
            } else {
                //编辑
                if (form != null && (!oilBoxId.equals(form.getId()))) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            log.error("去除重复异常", e);
            return false;
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
         * @return String
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 导入
     */
    @RequestMapping(value = { "/importTank" }, method = RequestMethod.GET)
    public String importTankPage() throws BusinessException {
        return IMPORT_TANK_PAGE;
    }

    @RequestMapping(value = "/importTank", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean importTank(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
            Map resultMap = fuelTankManageService.importTank(file, ipAddress);
            String msg = "导入结果：" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
            return new JsonResultBean(true, msg);
        } catch (Exception e) {
            log.error("导入数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下载模板
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/downloadTank", method = RequestMethod.GET)
    public void downloadTank(HttpServletResponse response) {
        try {
            String filename = "油箱信息列表模板";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            fuelTankManageService.generateTankTemplate(response);
        } catch (Exception e) {
            log.error("下载油箱信息列表模板异常", e);
        }
    }

    /**
     * 导出
     * @throws UnsupportedEncodingException
     */
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

    /**
     * 校验油箱是否被绑定
     * @param oilBoxId
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: checkBoxBound
     */
    @RequestMapping(value = "/checkBoxBound", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkBoxBound(String oilBoxId) {
        try {
            JSONObject msg = new JSONObject();
            boolean isBound = fuelTankManageService.findBoxBound(oilBoxId);
            msg.put("isBound", isBound);
            msg.put("oilBoxId", oilBoxId);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("油箱是否被绑定校验异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}
