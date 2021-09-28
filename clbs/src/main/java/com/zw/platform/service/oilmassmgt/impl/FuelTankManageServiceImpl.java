package com.zw.platform.service.oilmassmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankImportForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.FuelTankQuery;
import com.zw.platform.repository.vas.FuelTankManageDao;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.FuelTankVolumeUtil;
import com.zw.platform.util.OilMassMgtUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportOilBoxExcel;
import com.zw.platform.util.excel.ImportExcel;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 油箱管理Service实现类 <p>Title: FuelTankManageServiceImpl.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:32:08
 */
@Service
public class FuelTankManageServiceImpl implements FuelTankManageService {

    private static DecimalFormat df = new DecimalFormat("0.00"); // 保留两位小数

    private static DecimalFormat dfInt = new DecimalFormat("#"); // 整数

    private static DecimalFormat df_1 = new DecimalFormat("0.0"); // 保留一位小数

    private static final Integer MAX_LENGTH_TYPE = 50; // 导入时限制油箱型号最大长度

    private static final Integer MAX_LENGTH_BOX = 5; // 导入时限制油箱的长、宽、高的最大长度

    private static final Integer DEFAULT_THICKNESS = 3; // 默认油箱壁厚

    private static final Integer DEFAULT_RADIUS = 50; // 默认上下圆导角半径

    private static final String DELETE_ERROR_MSSAGE = "该油箱已经和车辆绑定了，到【油量车辆设置】中解除绑定后才可以删除哟！";

    private static final String TEMPLATE_COMMENT = "注：红色标注为必填；壁厚如果不填写，默认3mm，上下圆角如果不填写，默认50mm(另：整理前请删除示例数据，谢谢)";

    private static final String REGEXP = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]{1,25}$";

    @Autowired
    private FuelTankManageDao fuelTankManageDao;

    @Autowired
    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${add.success}")
    private String addSuccess;

    @Value("${delete.fuel.tank}")
    private String deleteFuelTank;

    @Value("${set.success}")
    private String setSuccess;

    @Value("${set.fail}")
    private String setFail;

    @Override
    public Page<FuelTankForm> findFuelTankByPage(FuelTankQuery query) {
        query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        Page<FuelTankForm> result = PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> fuelTankManageDao.findFuelTankByPage(query));
        if (null != result && result.size() > 0) {
            for (FuelTankForm ftf : result) {
                ftf.setShapeStr(getShapeStr(ftf.getShape()));
                ftf.setOddEvenCheckStr(getOddEvenCheckStr(ftf.getOddEvenCheck()));
                ftf.setCompensationCanMakeStr(getCompensationCanMakeStr(ftf.getCompensationCanMake()));
                ftf.setFilteringFactorStr(getFilteringFactorStr(ftf.getFilteringFactor()));
                ftf.setBaudRateStr(getBaudRateStr(ftf.getBaudRate()));
            }
        }
        return result;
    }

    /**
     * 处理油箱形状数据
     * @param shape
     * @return String
     * @throws @author Liubangquan
     * @Title: getShapeStr
     */
    private String getShapeStr(String shape) {
        String shapeStr = "";
        if (Converter.toBlank(shape).equals("1")) {
            shapeStr = "长方体";
        } else if (Converter.toBlank(shape).equals("2")) {
            shapeStr = "圆柱形";
        } else if (Converter.toBlank(shape).equals("3")) {
            shapeStr = "D形";
        } else if (Converter.toBlank(shape).equals("4")) {
            shapeStr = "椭圆形";
        } else {
            shapeStr = "其他";
        }
        return shapeStr;
    }

    /**
     * 处理奇偶校验数据
     * @param oddEvenCheck
     * @return String
     * @throws @author Liubangquan
     * @Title: getOddEvenCheckStr
     */
    private String getOddEvenCheckStr(String oddEvenCheck) {
        String oddEvenCheckStr = "";
        if (Converter.toBlank(oddEvenCheck).equals("1")) {
            oddEvenCheckStr = "奇校验";
        } else if (Converter.toBlank(oddEvenCheck).equals("2")) {
            oddEvenCheckStr = "偶校验";
        } else {
            oddEvenCheckStr = "无校验";
        }
        return oddEvenCheckStr;
    }

    /**
     * 处理补偿使能数据
     * @param compensationCanMake
     * @return String
     * @throws @author Liubangquan
     * @Title: getCompensationCanMakeStr
     */
    private String getCompensationCanMakeStr(String compensationCanMake) {
        String compensationCanMakeStr = "";
        if (Converter.toBlank(compensationCanMake).equals("0")) {
            compensationCanMakeStr = "禁用";
        } else {
            compensationCanMakeStr = "使能";
        }
        return compensationCanMakeStr;
    }

    /**
     * 处理滤波系数数据
     * @param filteringFactor
     * @return String
     * @throws @author Liubangquan
     * @Title: getFilteringFactorStr
     */
    private String getFilteringFactorStr(String filteringFactor) {
        String filteringFactorStr = "";
        if (Converter.toBlank(filteringFactor).equals("01")) { // 实时
            filteringFactorStr = "实时";
        } else if (Converter.toBlank(filteringFactor).equals("02")) { // 平滑
            filteringFactorStr = "平滑";
        } else if (Converter.toBlank(filteringFactor).equals("03")) { // 平稳
            filteringFactorStr = "平稳";
        }
        return filteringFactorStr;
    }

    /**
     * 处理波特率数据
     * @param baudRate
     * @return String
     * @throws @author Liubangquan
     * @Title: getBaudRateStr
     */
    private String getBaudRateStr(String baudRate) {
        String baudRateStr = "";
        if (Converter.toBlank(baudRate).equals("01")) {
            baudRateStr = "2400";
        } else if (Converter.toBlank(baudRate).equals("02")) {
            baudRateStr = "4800";
        } else if (Converter.toBlank(baudRate).equals("03")) {
            baudRateStr = "9600";
        } else if (Converter.toBlank(baudRate).equals("04")) {
            baudRateStr = "19200";
        } else if (Converter.toBlank(baudRate).equals("05")) {
            baudRateStr = "38400";
        } else if (Converter.toBlank(baudRate).equals("06")) {
            baudRateStr = "57600";
        } else if (Converter.toBlank(baudRate).equals("07")) {
            baudRateStr = "115200";
        }
        return baudRateStr;
    }

    @Override
    public List<RodSensor> findRodSensorList(String boxHeight) throws Exception {
        return fuelTankManageDao.findRodSensorList(boxHeight);
    }

    @Override
    public List<RodSensorForm> getSensorDetail(String sensorId) throws Exception {
        return fuelTankManageDao.getSensorDetail(sensorId);
    }

    @Override
    public JsonResultBean addFuelTank(FuelTankForm form, String ipAddress) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = fuelTankManageDao.addFuelTank(form);
        if (flag) {
            String msg = "新增油箱：" + form.getType();
            logSearchService.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, addSuccess);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public JsonResultBean deleteFuelTankById(String id, String ipAddress) throws Exception {
        // 判断油箱是否已经和车辆绑定，如果已经绑定，则给予提示
        boolean isBond = findBoxBound(id);
        if (!isBond) { // 没有绑定
            FuelTank fuel = findFuelTankById(id);
            // 删除油箱
            boolean flag = fuelTankManageDao.deleteFuelTankById(id);
            if (flag) {
                if (fuel != null) {
                    String message = "删除油箱 : " + fuel.getType();
                    logSearchService.addLog(ipAddress, message, "3", "", "-", "");
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, deleteFuelTank);
        }
    }

    @Override
    public JsonResultBean deleteBatchFuelTankById(String ids, String ipAddress) throws Exception {
        String[] items = ids.split(",");
        StringBuilder deleteFailTank = new StringBuilder();
        JSONObject msg = new JSONObject();
        StringBuilder message = new StringBuilder();
        int boundNumber = 0;
        int noBoundNumber = 0;
        for (String id : items) {
            boolean isBond = findIsBond(id);
            if (!isBond) {
                noBoundNumber++;
                FuelTank fuel = findFuelTankById(id);
                boolean flag = fuelTankManageDao.deleteFuelTankById(id);
                if (flag) {
                    message.append("删除油箱 : ").append(fuel.getType()).append(" <br/>");
                }
            } else {
                boundNumber++;
                List<FuelTankForm> list = getFuelTankDetail(id);
                if (null != list && list.size() > 0) {
                    deleteFailTank.append(list.get(0).getType()).append("</br>");
                }
            }
        }
        if (deleteFailTank.length() > 0) {
            msg.put("msg", DELETE_ERROR_MSSAGE + "</br>" + "已绑定油箱型号如下：</br>" + deleteFailTank);
            // return new JsonResultBean(JsonResultBean.FAULT, DELETE_ERROR_MSSAGE + "</br>" + "已绑定油箱型号如下：</br>" +
            // deleteFailTank);
            if (noBoundNumber != 0) { // 确定在一次批量删中,没有绑定车辆的油箱
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除油箱");
            }
            return new JsonResultBean(msg);
        } else {
            if (noBoundNumber == items.length && boundNumber == 0) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除油箱");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
    }

    @Override
    public List<FuelTankForm> getFuelTankDetail(String id) throws Exception {
        return fuelTankManageDao.getFuelTankDetail(id);
    }

    @Override
    public JsonResultBean updateFuelTank(FuelTankForm form, String ipAddress) throws Exception {
        FuelTank before = findFuelTankById(form.getId());// 修改前油箱实体
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = fuelTankManageDao.updateFuelTank(form);
        if (flag) { //修改成功则更新缓存,记录日志
            // 维护修改传感器后,车 和传感器型号的缓存
            String sensorName = form.getType();
            // 通过油箱id查询和车绑定情况,如果有就更新缓存
            List<OilVehicleSetting> list = fuelTankManageDao.findVehicleBindingOilBox(form.getId());
            if (list != null && list.size() > 0) {
                redisVehicleService.updateVehicleOilBoxCache(sensorName, list);
            }
            String log = "";// 日志语句
            String beforeFT = before.getType(); // 修改前的油箱
            String afterFT = form.getType();// 修改后油箱型号
            if (beforeFT.equals(afterFT)) {
                log = "修改油箱：" + afterFT;
            } else {
                log = "修改油箱：" + beforeFT + " 修改为 " + afterFT;
            }
            logSearchService.addLog(ipAddress, log, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
        }
        return new JsonResultBean(JsonResultBean.FAULT, setFail);
    }

    /**
     * 保存油量标定表数据
     * @param bean
     * @return void
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: addOilCalibration
     */
    public void addOilCalibration(OilVehicleSetting bean) throws Exception {
        // 保存标定数据之前，先删除之前的标定
        fuelTankManageDao.deleteOilCalibration(Converter.toBlank(bean.getId()));
        addOilCalibrationImpl(bean);
    }

    /**
     * 计算并保存长方体油箱标定
     * @param bean 油箱绑定实体
     * @return void
     * @throws BusinessException
     * @throws @author           Liubangquan
     * @Title: addOilCalibrationImpl
     */
    private void addOilCalibrationImpl(OilVehicleSetting bean) throws Exception {
        double l = Converter.toDouble(bean.getBoxLength()) / 10; // 油箱长度
        double w = Converter.toDouble(bean.getWidth()) / 10; // 油箱宽度
        double h = Converter.toDouble(bean.getHeight()) / 10; // 油箱高度
        double t = Converter.toDouble(bean.getThickness()) / 10; // 油箱壁厚
        double rx = 0.0; // 下圆角半径
        double rs = 0.0; // 上圆角半径
        if (!StringUtil.isNullOrBlank(bean.getButtomRadius())) {
            rx = Converter.toDouble(bean.getButtomRadius()) / 10;
        }
        if (!StringUtil.isNullOrBlank(bean.getTopRadius())) {
            rs = Converter.toDouble(bean.getTopRadius()) / 10;
        }
        /*
         * double h1 = Converter.toDouble(bean.getMeasuringRange()) / 10; // 量程 double e =
         * Converter.toDouble(bean.getUpperBlindZone()) / 10; // 上盲区 double d =
         * Converter.toDouble(bean.getLowerBlindArea()) / 10; // 下盲区
         */
        double realV = Converter.toDouble(bean.getRealVolume()); // 油箱容量
        double vc = Converter.toDouble(bean.getRealVolume()) / Converter.toDouble(bean.getTheoryVolume()); // 容积系数
        int calSets = Converter.toInteger(bean.getCalibrationSets());
        double variable = 0.1; // 积分颗粒度
        String shape = bean.getShape(); // 油箱形状
        String oilBoxVehicleId = bean.getId(); // 油量车辆设置绑定表id
        List<Double> list1 = new ArrayList<>();
        List<Double> list2 = new ArrayList<>();
        String[] hms = getCalculateHeight((h - 2 * t) * 10, calSets, variable * 10);
        if (null != hms && hms.length > 0) {
            for (int i = 0; i < hms.length; i++) {
                double tempVol = 0;
                if (Converter.toBlank(shape).equals("1")) { // 长方形
                    tempVol = FuelTankVolumeUtil.rectangleCal(l, w, Converter.toDouble(hms[i]) / 10, t, h, rx, rs);
                } else if (Converter.toBlank(shape).equals("2")) { // 圆形
                    tempVol = FuelTankVolumeUtil.circularCal(l, h, t, Converter.toDouble(hms[i]) / 10, variable);
                } else if (Converter.toBlank(shape).equals("3")) { // D形
                    tempVol = FuelTankVolumeUtil.dxShapeCal(l, w, h, t, Converter.toDouble(hms[i]) / 10, variable);
                } else if (Converter.toBlank(shape).equals("4")) { // 椭圆形
                    tempVol = FuelTankVolumeUtil.ellipseCal(l, w, h, t, Converter.toDouble(hms[i]) / 10, variable);
                }
                list1.add(Converter.toDouble(hms[i]) / 10);
                list2.add(realV > 0 ? tempVol * vc : tempVol);
            }
        }

        if (list1.size() > 0) {
            for (int i = 0; i < list1.size(); i++) {
                OilCalibrationForm form = new OilCalibrationForm();
                form.setOilBoxVehicleId(oilBoxVehicleId);
                form.setOilLevelHeight(df.format(Converter.toDouble(list1.get(i)) * 10));
                form.setOilValue(df.format(Converter.toDouble(list2.get(i))));
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                fuelTankManageDao.addOilCalibration(form);
            }
        }
    }

    /**
     * 根据油杆的量程，得出需要计算的油位高度的油量
     * @param h1       量程
     * @param calSets  标定组数
     * @param variable 积分颗粒度
     * @return String[]
     * @throws @author Liubangquan
     * @Title: getCalculateHeight
     */
    private String[] getCalculateHeight(double h1, int calSets, double variable) {
        String[] hms = new String[calSets];
        if (h1 <= calSets) {
            double hm = 0;
            for (int i = 0; i < hms.length; i++) {
                double temp = (hm += variable);
                if (temp >= h1) {
                    hms[i] = df.format(h1);
                } else {
                    hms[i] = df.format(temp);
                }

            }
        } else {
            double hm = 0;
            for (int i = 0; i < hms.length; i++) {
                double temp = (hm += (h1 / variable) / calSets);
                if (temp >= h1) {
                    hms[i] = df.format(h1);
                } else {
                    hms[i] = df.format(temp);
                }
            }
        }
        return hms;
    }

    @Override
    public FuelTankForm importOilCalibration(MultipartFile file) throws Exception {
        //   @todo  零时处理  后续完善
        String fileName = file.getOriginalFilename();
        if (org.apache.commons.lang3.StringUtils.isBlank(fileName)) {
            return new FuelTankForm();
        } else if (!fileName.toLowerCase().endsWith("xlsx")) {
            return new FuelTankForm();
        }
        File f = multipartToFile(file);
        InputStream is = new FileInputStream(f);
        XSSFWorkbook wb = new XSSFWorkbook(is);
        XSSFSheet sheet = wb.getSheetAt(0);
        Iterator<Row> rows = sheet.rowIterator();

        FuelTankForm form = new FuelTankForm();
        List<String> oilLevelHeightList = new ArrayList<>();
        List<String> oilValueList = new ArrayList<>();
        int rownum = 0; // 当前行
        while (rows.hasNext()) {
            XSSFRow row = (XSSFRow) rows.next();
            rownum = row.getRowNum();
            Iterator<Cell> cells = row.cellIterator();
            int cellnum = 0; // 当前列
            while (cells.hasNext()) {
                XSSFCell cell = (XSSFCell) cells.next();
                cellnum = cell.getColumnIndex();
                if (rownum == 2 && cellnum == 0) { // 长
                    form.setBoxLength(dfInt.format(Converter.toDouble(getCellValue(row, cellnum))));
                }
                if (rownum == 2 && cellnum == 1) { // 宽
                    form.setWidth(dfInt.format(Converter.toDouble(getCellValue(row, cellnum))));
                }
                if (rownum == 2 && cellnum == 2) { // 高
                    form.setHeight(dfInt.format(Converter.toDouble(getCellValue(row, cellnum))));
                }
                if (rownum == 2 && cellnum == 3) { // 油杆长
                    form.setSensorLength(Converter.toBlank(getCellValue(row, cellnum)));
                }
                /*
                 * if (rownum == 2 && cellnum == 8) { // 量程 form.setMeasuringRange(Converter.toBlank(getCellValue(row,
                 * cellnum))); }
                 */
                if (rownum == 4 && cellnum == 1) { // 油箱壁厚
                    form.setThickness(dfInt.format(Converter.toDouble(getCellValue(row, cellnum))));
                }
                if (rownum == 5 && cellnum == 1) { // 上圆角半径
                    form.setButtomRadius(dfInt.format(Converter.toDouble(getCellValue(row, cellnum))));
                }
                if (rownum == 6 && cellnum == 1) { // 下圆角半径
                    form.setTopRadius(dfInt.format(Converter.toDouble(getCellValue(row, cellnum))));
                }
                /*
                 * if (rownum == 6 && cellnum == 1) { // 客户容积（油箱容量）
                 * form.setRealVolume(dfInt.format(Converter.toDouble(getCellValue(row, cellnum)))); }
                 */
                /*
                 * if (rownum == 14 && cellnum == 1) { // 理论容积 form.setTheoryVolume(Converter.toBlank(getCellValue(row,
                 * cellnum))); }
                 */
                if (rownum >= 2 && cellnum == 9) { // 油杆理论高度
                    String cellValue = Converter.toBlank(getCellValue(row, cellnum));
                    if (Converter.isNumber(cellValue)) {
                        // 是数字
                        oilLevelHeightList.add(dfInt.format(Converter.toDouble(cellValue)));
                    }

                }
                if (rownum >= 2 && cellnum == 11) { // 客户油量值
                    String cellValue = Converter.toBlank(getCellValue(row, cellnum));
                    if (Converter.isNumber(cellValue)) {
                        oilValueList.add(df_1.format(Converter.toDouble(cellValue)));
                    }
                }
            }
        }
        form.setOilLevelHeightList(oilLevelHeightList);
        form.setOilValueList(oilValueList);
        return form;
    }

    /**
     * MultipartFile 转换成File
     * @param multfile 原文件类型
     * @return File
     * @throws IOException
     */
    private File multipartToFile(MultipartFile multfile) throws Exception {
        CommonsMultipartFile cf = (CommonsMultipartFile) multfile;
        // 这个myfile是MultipartFile的
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();
        File file = fi.getStoreLocation();
        // 手动创建临时文件
        // if(file.length() < CommonConstants.MIN_FILE_SIZE){
        File tmpFile =
            new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + file.getName());
        multfile.transferTo(tmpFile);
        return tmpFile;
        // }
        // return file;
    }

    public Object getCellValue(Row row, int column) throws Exception {
        Object val = "";

        Cell cell = row.getCell(column);
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    val = HSSFDateUtil.getJavaDate(cell.getNumericCellValue()).toString();
                } else {
                    val = df.format(cell.getNumericCellValue());
                }
            } else if (cell.getCellType() == CellType.STRING) {
                val = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                // val = cell.getCellFormula();
                try {
                    val = df.format(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    val = cell.getRichStringCellValue();
                }
            } else if (cell.getCellType() == CellType.BOOLEAN) {
                val = cell.getBooleanCellValue() + "";
            } else if (cell.getCellType() == CellType.ERROR) {
                val = cell.getErrorCellValue() + "";
            } else if (cell.getCellType() == CellType.BLANK) {
                val = "";
            } else {
                val = cell.getNumericCellValue();
            }
        }
        return val;
    }

    @Override
    public List<OilCalibrationForm> getOilCalibrationList(String oilBoxVehicleId) throws Exception {
        List<OilCalibrationForm> list = fuelTankManageDao.getOilCalibrationList(oilBoxVehicleId);
        if (null != list && list.size() > 0) {
            for (OilCalibrationForm ocf : list) {
                ocf.setOilLevelHeight(String.valueOf(Converter.toDouble(ocf.getOilLevelHeight())));
                ocf.setOilValue(String.valueOf(Converter.toDouble(ocf.getOilValue())));
            }
        }
        return list;
    }

    @Override
    public void updateOilCalibration(DoubleOilVehicleSetting bean) throws Exception {
        if (StringUtils.isNotBlank(bean.getOilBoxId())) { // 单油箱
            String[] oilLvlHeit =
                !Converter.toBlank(bean.getOilLevelHeights()).equals("") ? bean.getOilLevelHeights().split(",") : null;
            String[] oilVal =
                !Converter.toBlank(bean.getOilValues()).equals("") ? bean.getOilValues().split(",") : null;
            if (null != oilLvlHeit && oilLvlHeit.length > 0) {
                fuelTankManageDao.deleteOilCalibration(bean.getId());
                for (int i = 0; i < oilLvlHeit.length; i++) {
                    OilCalibrationForm ocf = new OilCalibrationForm();
                    ocf.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    ocf.setOilBoxVehicleId(bean.getId());
                    ocf.setOilLevelHeight(Converter.toBlank(oilLvlHeit[i]));
                    ocf.setOilValue(Converter.toBlank(oilVal[i]));
                    fuelTankManageDao.addOilCalibration(ocf);
                }
            }
        }
        if (StringUtils.isNotBlank(bean.getOilBoxId2())) { // 双油箱
            String[] oilLvlHeit2 =
                !Converter.toBlank(bean.getOilLevelHeights2()).equals("") ? bean.getOilLevelHeights2().split(",")
                    : null;
            String[] oilVal2 =
                !Converter.toBlank(bean.getOilValues2()).equals("") ? bean.getOilValues2().split(",") : null;
            if (null != oilLvlHeit2 && oilLvlHeit2.length > 0) {
                fuelTankManageDao.deleteOilCalibration(bean.getId2());
                for (int i = 0; i < oilLvlHeit2.length; i++) {
                    OilCalibrationForm ocf = new OilCalibrationForm();
                    ocf.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    ocf.setOilBoxVehicleId(bean.getId2());
                    ocf.setOilLevelHeight(Converter.toBlank(oilLvlHeit2[i]));
                    ocf.setOilValue(Converter.toBlank(oilVal2[i]));
                    fuelTankManageDao.addOilCalibration(ocf);
                }
            }
        }
    }

    @Override
    public List<FuelTankForm> getOilCalibrationByVid(String vehicleId) throws Exception {
        List<FuelTankForm> list = fuelTankManageDao.getOilCalibrationByVid(vehicleId);
        List<String> tankIdList = null;
        // 封装油箱及其标定数据
        List<FuelTankForm> resultList = new ArrayList<>();
        if (null != list && list.size() > 0) {
            tankIdList = getOilBoxIdList(list);
            for (String str : tankIdList) {
                FuelTankForm f = new FuelTankForm();
                List<OilCalibrationForm> ocList = new ArrayList<>();
                for (FuelTankForm ftf : list) {
                    // if (Converter.toBlank(ftf.getOilBoxId()).equals(str)) {
                    if (Converter.toBlank(ftf.getTanktyp()).equals(str)) {
                        f = ftf;
                        OilCalibrationForm oc = new OilCalibrationForm();
                        // oc.setOilBoxId(str);
                        oc.setOilLevelHeight(dfInt.format(Converter.toDouble(ftf.getOilLevelHeight()) * 10));
                        oc.setOilValue(dfInt.format(Converter.toDouble(ftf.getOilValue()) * 10));
                        ocList.add(oc);
                    }
                }
                f.setOilCalList(ocList);
                resultList.add(f);
            }
        }
        return resultList;
    }

    @Override
    public List<FuelTankForm> getOilCalibrationByBindId(String vehicleId) throws Exception {
        List<FuelTankForm> list = fuelTankManageDao.getOilCalibrationByBindId(vehicleId);
        List<String> tankIdList = null;
        // 封装油箱及其标定数据
        List<FuelTankForm> resultList = new ArrayList<>();
        if (null != list && list.size() > 0) {
            tankIdList = getOilBoxIdList(list);
            for (String str : tankIdList) {
                FuelTankForm f = new FuelTankForm();
                List<OilCalibrationForm> ocList = new ArrayList<>();
                for (FuelTankForm ftf : list) {
                    // if (Converter.toBlank(ftf.getOilBoxId()).equals(str)) {
                    if (Converter.toBlank(ftf.getTanktyp()).equals(str)) {
                        f = ftf;
                        OilCalibrationForm oc = new OilCalibrationForm();
                        // oc.setOilBoxId(str);
                        oc.setOilLevelHeight(dfInt.format(Converter.toDouble(ftf.getOilLevelHeight()) * 10));
                        oc.setOilValue(dfInt.format(Converter.toDouble(ftf.getOilValue()) * 10));
                        ocList.add(oc);
                    }
                }
                f.setOilCalList(ocList);
                resultList.add(f);
            }
        }
        return resultList;
    }

    /**
     * 将查询出来的结果，抽取出所有的油箱id
     * @param list
     * @return List<String>
     * @throws @author Liubangquan
     * @Title: getOilBoxIdList
     */
    private List<String> getOilBoxIdList(List<FuelTankForm> list) {
        List<String> tankIdList = null;
        if (null != list && list.size() > 0) {
            for (FuelTankForm ft : list) {
                if (null == tankIdList) {
                    tankIdList = new ArrayList<>();
                    // tankIdList.add(Converter.toBlank(ft.getOilBoxId()));
                    tankIdList.add(Converter.toBlank(ft.getTanktyp()));
                    continue;
                }
                if (null != tankIdList && tankIdList.size() > 0) {
                    boolean flag = false;
                    for (String str : tankIdList) {
                        // if (Converter.toBlank(ft.getOilBoxId()).equals(str)) {
                        if (Converter.toBlank(ft.getTanktyp()).equals(str)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        // tankIdList.add(ft.getOilBoxId());
                        tankIdList.add(ft.getTanktyp());
                    }
                }
            }
        }
        return tankIdList;
    }

    @Override
    public String getOilBoxShapeStr(String shape) throws Exception {
        if ("1".equals(Converter.toBlank(shape)) || "01".equals(Converter.toBlank(shape))) {
            return "长方体";
        } else if ("2".equals(Converter.toBlank(shape)) || "02".equals(Converter.toBlank(shape))) {
            return "圆柱形";
        } else if ("3".equals(Converter.toBlank(shape)) || "03".equals(Converter.toBlank(shape))) {
            return "D形";
        } else if ("4".equals(Converter.toBlank(shape)) || "04".equals(Converter.toBlank(shape))) {
            return "椭圆形";
        } else {
            return "";
        }
    }

    @Override
    public FuelTankForm getOilBoxByType(String type) throws Exception {
        List<FuelTankForm> list = fuelTankManageDao.getOilBoxByType(type);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 批量导入:按最新的模板要求修改 update by liubq 2016/12/10
     */
    @Override
    public Map importTank(MultipartFile multipartFile, String ipAddress) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        int failNum = 0; // 导入失败的记录条数
        int totalNum = 0; // 导入的总条数
        StringBuilder message = new StringBuilder(); //日志语句
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        int cellNum = importExcel.getLastCellNum();
        if (cellNum != 10) {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", "请将导入文件按照模板格式整理后再导入");
            resultMap.put("resultInfo", "导入失败！");
            return resultMap;
        }
        // excel 转换成 list
        List<FuelTankImportForm> list = importExcel.getDataList(FuelTankImportForm.class, null);
        String temp;
        List<FuelTankImportForm> importList = new ArrayList<FuelTankImportForm>();
        StringBuilder errorMsgBuilder = new StringBuilder();
        // 校验需要导入的油箱
        if (list != null && list.size() > 0) {
            totalNum = list.size();
            for (int i = 0; i < list.size(); i++) {
                FuelTankImportForm tank = list.get(i);
                // 判断是不是首行，如果是首行，直接跳过
                if (Converter.toBlank(tank.getType()).equals("油箱型号*")) { // 是标题行
                    continue;
                }
                // 壁厚是否填写，如果没有填写，默认3mm
                if (StringUtils.isBlank(RegexUtils.getNumInStr(tank.getThickness()))) {
                    tank.setThickness(Converter.toBlank(DEFAULT_THICKNESS));
                }
                // 上下圆导角是否填写，如果没有填写，默认50mm
                if (StringUtils.isBlank(RegexUtils.getNumInStr(tank.getButtomRadius()))) {
                    tank.setButtomRadius(Converter.toBlank(DEFAULT_RADIUS));
                }
                if (StringUtils.isBlank(RegexUtils.getNumInStr(tank.getTopRadius()))) {
                    tank.setTopRadius(Converter.toBlank(DEFAULT_RADIUS));
                }
                // 获取导入油箱基本信息
                StringBuilder tankType = new StringBuilder(); // 油箱型号
                String length = RegexUtils.getNumInStr(tank.getBoxLength()); // 长
                String width = RegexUtils.getNumInStr(tank.getWidth()); // 宽
                String height = RegexUtils.getNumInStr(tank.getHeight()); // 高
                String thickness = RegexUtils.getNumInStr(tank.getThickness()); // 壁厚
                String r1 = RegexUtils.getNumInStr(tank.getButtomRadius()); // 下圆角半径
                String r2 = RegexUtils.getNumInStr(tank.getTopRadius()); // 上圆角半径
                // 校验必填字段
                if (StringUtils.isBlank(tank.getType()) || StringUtils.isBlank(tank.getShapeStr()) || StringUtils
                    .isBlank(length) || StringUtils.isBlank(width) || StringUtils.isBlank(height)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据必填字段未填</br>");
                    failNum++;
                    continue;
                }
                if (!Pattern.matches(REGEXP, tank.getType())) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【油箱型号】异常，仅中文、字母、数字或特殊符号*、-、_、#，长度不超过25位</br>");
                    failNum++;
                    continue;
                } else if (!Converter.isNumber(length)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【长度】异常，不是正确的数字 </br>");
                    failNum++;
                    continue;
                } else if (Converter.toBlank(length).length() > MAX_LENGTH_BOX) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【长度】异常，长度最大不超过").append(MAX_LENGTH_BOX)
                        .append("位数 </br>");
                    failNum++;
                    continue;
                } else if (!Converter.isNumber(width)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【宽度】异常，不是正确的数字 </br>");
                    failNum++;
                    continue;
                } else if (Converter.toBlank(width).length() > MAX_LENGTH_BOX) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【宽度】异常，宽度最大不超过").append(MAX_LENGTH_BOX)
                        .append("位数 </br>");
                    failNum++;
                    continue;
                } else if (!Converter.isNumber(height)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【高度】异常，不是正确的数字 </br>");
                    failNum++;
                    continue;
                } else if (Converter.toBlank(height).length() > MAX_LENGTH_BOX) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【高度】异常，高度最大不超过").append(MAX_LENGTH_BOX)
                        .append("位数 </br>");
                    failNum++;
                    continue;
                }
                if (!Converter.isNumber(thickness)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【壁厚】异常，不是正确的数字 </br>");
                    failNum++;
                    continue;
                } else if (Converter.toDouble(thickness) < 1 || Converter.toDouble(thickness) > 10) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【壁厚】异常，壁厚应该介于1mm到10mm之间 </br>");
                    failNum++;
                    continue;
                }

                // 油箱形状
                if (Converter.toBlank(tank.getShapeStr()).equals("长方体")) {
                    tank.setShape("1");
                } else if (Converter.toBlank(tank.getShapeStr()).equals("圆柱形")) {
                    tank.setShape("2");
                } else if (Converter.toBlank(tank.getShapeStr()).equals("D形")) {
                    tank.setShape("3");
                } else if (Converter.toBlank(tank.getShapeStr()).equals("椭圆形")) {
                    tank.setShape("4");
                }

                // 校验各数据的合法性
                if (Converter.toDouble(length) <= Converter.toDouble(thickness) * 2) { // 长度小于2倍壁厚
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【长度】异常，长度必须大于2倍壁厚 </br>");
                    failNum++;
                    continue;
                }
                if (Converter.toDouble(width) <= Converter.toDouble(thickness) * 2) { // 长度小于2倍壁厚
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【宽度】异常，宽度必须大于2倍壁厚 </br>");
                    failNum++;
                    continue;
                }
                if (Converter.toDouble(height) <= Converter.toDouble(thickness) * 2) { // 长度小于2倍壁厚
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i).append("条数据【高度】异常，高度必须大于2倍壁厚 </br>");
                    failNum++;
                    continue;
                }
                tank.setBoxLength(dfInt.format(Converter.toDouble(length)));
                tank.setWidth(dfInt.format(Converter.toDouble(width)));
                tank.setHeight(dfInt.format(Converter.toDouble(height)));
                tank.setThickness(dfInt.format(Converter.toDouble(thickness)));

                // 若为长方体油箱，则校验上下圆导角半径数据合法性
                if ("1".equals(tank.getShape())) {
                    if (Converter.toDouble(r1) < Converter.toDouble(thickness)) {
                        resultMap.put("flag", 0);
                        errorMsg.append("第").append(i).append("条数据【下圆角半径】异常，下圆角半径必须大于等于壁厚 </br>");
                        failNum++;
                        continue;
                    }
                    if (Converter.toDouble(r2) < Converter.toDouble(thickness)) {
                        resultMap.put("flag", 0);
                        errorMsg.append("第").append(i).append("条数据【上圆角半径】异常，上圆角半径必须大于等于壁厚 </br>");
                        failNum++;
                        continue;
                    }
                    if (Converter.toDouble(r1) > Converter.toDouble(width) * (1.0 / 2.0)
                        || Converter.toDouble(r1) > Converter.toDouble(height) * (1.0 / 2.0)) {
                        resultMap.put("flag", 0);
                        errorMsg.append("第").append(i).append("条数据【下圆角半径】异常，下圆角半径必须小于等于宽和高的1/2 </br>");
                        failNum++;
                        continue;
                    }
                    if (Converter.toDouble(r2) > Converter.toDouble(width) * (1.0 / 2.0)
                        || Converter.toDouble(r2) > Converter.toDouble(height) * (1.0 / 2.0)) {
                        resultMap.put("flag", 0);
                        errorMsg.append("第").append(i).append("条数据【上圆角半径】异常，上圆角半径必须小于等于宽和高的1/2 </br>");
                        failNum++;
                        continue;
                    }
                    if (Converter.toDouble(r1) < Converter.toDouble(r2)) {
                        resultMap.put("flag", 0);
                        errorMsg.append("第").append(i).append("条数据【上圆角半径】异常，上圆角半径必须小于等于上圆角半径 </br>");
                        failNum++;
                        continue;
                    }
                    tank.setButtomRadius(r1);
                    tank.setTopRadius(r2);
                } else {
                    tank.setButtomRadius("");
                    tank.setTopRadius("");
                }

                tank.setRealVolume(Converter.isNumber(tank.getCapacity()) ? tank.getCapacity() : "");
                FuelTank fuelTank = new FuelTank();
                fuelTank.setShape(Converter.toBlank(tank.getShape()));
                fuelTank.setBoxLength(Converter.toBlank(length));
                fuelTank.setWidth(Converter.toBlank(width));
                fuelTank.setHeight(Converter.toBlank(height));
                fuelTank.setThickness(Converter.toBlank(thickness));
                fuelTank.setButtomRadius(Converter.toBlank(r1));
                fuelTank.setTopRadius(Converter.toBlank(r2));
                // 计算理论容积
                String theoryVolume = OilMassMgtUtil.get_theory_Volume_by_shape(fuelTank);
                tank.setTheoryVolume(theoryVolume);
                // 列表中重复数据
                boolean f = false;
                for (int j = i + 1; j < list.size(); j++) {
                    FuelTankImportForm fif = list.get(j);
                    if (fif.getType().equals(tank.getType())) {
                        temp = tank.getType();
                        errorMsg.append("第").append(i).append("条跟第").append(j + 1).append("条油箱型号重复，值是：").append(temp)
                            .append("<br/>");
                        failNum++;
                        f = true;
                        break;
                        // list.remove(j);
                    }
                }
                if (f) {
                    continue;
                }
                // 与数据库是否有重复数据
                List<FuelTankForm> tankList = fuelTankManageDao.getOilBoxByType(tank.getType());
                if (tankList != null && tankList.size() > 0) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append("条数据【油箱型号】异常，油箱型号“").append(tank.getType()).append("”已存在<br/>");
                    failNum++;
                    continue;
                }
                tank.setCreateDataTime(new Date());
                tank.setCreateDataUsername(SystemHelper.getCurrentUsername());
                importList.add(tank);
                message.append("导入油箱 : ").append(tank.getType()).append(" <br/>");
            }
        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsgBuilder.toString());
            resultMap.put("resultInfo", "成功导入0条数据!");
            return resultMap;
        }

        // 组装导入结果
        if (importList.size() > 0) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = fuelTankManageDao.addTankByBatch(importList);
            if (flag) {
                resultInfo += "成功" + importList.size() + "条， 失败" + failNum + "条。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg);
                resultMap.put("resultInfo", resultInfo);
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "导入油箱");
        } else {
            resultInfo += "导入成功" + importList.size() + "条数据,导入失败" + (list.size() - 1 - importList.size()) + "条数据。";
            resultMap.put("flag", 1);
            resultMap.put("errorMsg", errorMsg);
            resultMap.put("resultInfo", resultInfo);
        }
        return resultMap;
    }

    /**
     * 批量导入
     */
    /*
     * @Override public Map importTank(MultipartFile multipartFile) { Map<String, Object> resultMap = new
     * HashMap<String, Object>(); resultMap.put("flag", 0); String errorMsg = ""; String resultInfo = ""; int failNum =
     * 0; // 导入失败的记录条数 int totalNum = 0; // 导入的总条数 try { // 导入的文件 ImportExcel importExcel = new
     * ImportExcel(multipartFile, 1, 0); // excel 转换成 list List<FuelTankForm> list =
     * importExcel.getDataList(FuelTankForm.class, null); String temp; List<FuelTankForm> importList = new
     * ArrayList<FuelTankForm>(); // 校验需要导入的油箱 if (list.size() >0) { totalNum = list.size(); for (int i=0;
     * i<list.size(); i++) { FuelTankForm tank = list.get(i); // 校验必填字段 if (StringUtils.isBlank(tank.getType()) ||
     * StringUtils.isBlank(tank.getShape()) ||StringUtils.isBlank(tank.getBoxLength()) ||
     * StringUtils.isBlank(tank.getWidth()) ||StringUtils.isBlank(tank.getHeight()) ||
     * StringUtils.isBlank(tank.getThickness())) { resultMap.put("flag", 0); errorMsg += "第" +(i+1) + "条数据必填字段未填</br>";
     * failNum ++; continue; } else if (!RegexUtils.checkRightfulString1(tank.getType())) { resultMap.put("flag", 0);
     * errorMsg += "第" +(i+1) + "条数据【" + tank.getType() + "】：“油箱型号”填写不规范，包含特殊字符</br>"; failNum ++; continue; } else if
     * (Converter.toBlank(tank.getType()).length() > MAX_LENGTH_TYPE) { resultMap.put("flag", 0); errorMsg += "第" +(i+1)
     * + "条数据【" + tank.getType() + "】：“油箱型号”长度不能超过" + MAX_LENGTH_TYPE + " </br>"; failNum ++; continue; } else if
     * (!Converter.isInteger(tank.getBoxLength())) { resultMap.put("flag", 0); errorMsg += "第" +(i+1) + "条数据【" +
     * tank.getType() + "】：“长度”不是正确的数字 </br>"; failNum ++; continue; } else if
     * (Converter.toBlank(tank.getBoxLength()).length() > MAX_LENGTH_BOX) { resultMap.put("flag", 0); errorMsg += "第"
     * +(i+1) + "条数据【" + tank.getType() + "】：“长度”值过大，最大不超过"+ MAX_LENGTH_BOX + "位数 </br>"; failNum ++; continue; } else
     * if (!Converter.isInteger(tank.getWidth())) { resultMap.put("flag", 0); errorMsg += "第" +(i+1) + "条数据【" +
     * tank.getType() + "】：“宽度”不是正确的数字 </br>"; failNum ++; continue; } else if
     * (Converter.toBlank(tank.getWidth()).length() > MAX_LENGTH_BOX) { resultMap.put("flag", 0); errorMsg += "第" +(i+1)
     * + "条数据【" + tank.getType() + "】：“宽度”值过大，最大不超过"+ MAX_LENGTH_BOX + "位数 </br>"; failNum ++; continue; } else if
     * (!Converter.isInteger(tank.getHeight())) { resultMap.put("flag", 0); errorMsg += "第" +(i+1) + "条数据【" +
     * tank.getType() + "】：“高度”不是正确的数字 </br>"; failNum ++; continue; } else if
     * (Converter.toBlank(tank.getHeight()).length() > MAX_LENGTH_BOX) { resultMap.put("flag", 0); errorMsg += "第"
     * +(i+1) + "条数据【" + tank.getType() + "】：“高度”值过大，最大不超过"+ MAX_LENGTH_BOX + "位数 </br>"; failNum ++; continue; } else
     * if (!Converter.isInteger(tank.getThickness())) { resultMap.put("flag", 0); errorMsg += "第" +(i+1) + "条数据【" +
     * tank.getType() + "】：“壁厚”不是正确的数字 </br>"; failNum ++; continue; } else { // 油箱形状 if
     * (Converter.toBlank(tank.getShape()).equals("长方体")){ tank.setShape("1"); }else if
     * (Converter.toBlank(tank.getShape()).equals("圆柱形")){ tank.setShape("2"); }else
     * if(Converter.toBlank(tank.getShape()).equals("D形")){ tank.setShape("3"); }else if
     * (Converter.toBlank(tank.getShape()).equals("椭圆形")){ tank.setShape("4"); } // 计算理论容积 String theoryVolume =
     * OilMassMgtUtil.get_theory_Volume_by_shape(Converter.toBlank(tank.getShape()),
     * Converter.toBlank(tank.getBoxLength()), Converter.toBlank(tank.getWidth()), Converter.toBlank(tank.getHeight()),
     * Converter.toBlank(tank.getThickness())); tank.setTheoryVolume(theoryVolume); } // 列表中重复数据 boolean f = false;
     * for(int j=i+1;j < list.size();j++){ if(list.get(j).getType().equals(tank.getType())){ temp = tank.getType();
     * errorMsg += "第" + (i + 2) + "行跟第" + (j + 2) + "行重复，值是："+temp+"<br/>"; failNum ++; f = true; break;
     * //list.remove(j); } } if (f) { continue; } // 与数据库是否有重复数据 List<FuelTankForm> tankList =
     * fuelTankManageDao.getOilBoxByType(tank.getType()); if ( tankList != null && tankList.size() > 0) {
     * resultMap.put("flag", 0); errorMsg += "油箱型号为“" + tank.getType() + "”已存在<br/>"; failNum ++; continue; }
     * tank.setCreateDataTime(new Date()); tank.setCreateDataUsername(SystemHelper.getCurrentUsername());
     * importList.add(tank); } } // 组装导入结果 if (importList.size() > 0) { // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定） boolean flag =
     * fuelTankManageDao.addTankByBatch(importList); if (flag) { resultInfo += "成功" + (totalNum - failNum) + "条， 失败" +
     * failNum + "条。"; resultMap.put("flag", 1); resultMap.put("errorMsg", errorMsg); resultMap.put("resultInfo",
     * resultInfo); } else { resultMap.put("flag", 0); resultMap.put("resultInfo", "导入失败！"); return resultMap; } } else
     * { resultMap.put("flag", 0); resultMap.put("errorMsg", errorMsg); resultMap.put("resultInfo", "成功导入0条数据。"); return
     * resultMap; } } catch (InvalidFormatException e) { //log.error("error",e); //return resultMap; } catch
     * (IOException e) { //log.error("error",e); //return resultMap; } catch (InstantiationException e) {
     * //log.error("error",e); //return resultMap; } catch (IllegalAccessException e) { //log.error("error",e); //return
     * resultMap; } catch (BusinessException e) { // TODO Auto-generated catch block e.printStackTrace(); } return
     * resultMap; }
     */

    /**
     * 生成导入模板
     */
    // @Override
    /*
     * public boolean generateTankTemplate(HttpServletResponse response) { List<String> headList = new
     * ArrayList<String>(); List<String> requiredList = new ArrayList<String>(); List<Object> exportList = new
     * ArrayList<Object>(); // 表头 headList.add("油箱型号"); headList.add("油箱形状"); headList.add("长度(mm)");
     * headList.add("宽度(mm)"); headList.add("高度(mm)"); headList.add("壁厚(mm)"); headList.add("理论容积(L)");
     * headList.add("油箱容量(L)"); // 必填字段 requiredList.add("油箱型号"); requiredList.add("油箱形状"); requiredList.add("长度(mm)");
     * requiredList.add("宽度(mm)"); requiredList.add("高度(mm)"); requiredList.add("壁厚(mm)"); // 默认设置一条数据
     * exportList.add("DGRT-30"); exportList.add("长方体"); exportList.add("663"); exportList.add("459");
     * exportList.add("400"); exportList.add("5"); exportList.add("128"); exportList.add("131"); // 组装有下拉框的map
     * Map<String,String[]> selectMap = new HashMap<String,String[]>(); // 油箱形状 String[] tankShape =
     * {"长方体","圆柱形","D形","椭圆形"}; selectMap.put("油箱形状", tankShape); //ExportExcel export = new ExportExcel(headList,
     * requiredList,selectMap); ExportOilBoxExcel export = new ExportOilBoxExcel(headList, requiredList, selectMap); Row
     * row = export.addRow(); for (int j = 0; j < exportList.size(); j++) { export.addCell(row, j, exportList.get(j)); }
     * // 输出导文件 OutputStream out; try { out = response.getOutputStream(); export.write(out);// 将文档对象写入文件输出流 out.close();
     * } catch (IOException e) { // log.error("error",e); return false; } return true; }
     */
    @Override
    public boolean generateTankTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<String>();
        List<String> requiredList = new ArrayList<String>();
        List<Object> exportList = new ArrayList<Object>();
        // 表头
        headList.add("油箱型号");
        headList.add("油箱形状");
        headList.add("长度(mm)");
        headList.add("宽度(mm)");
        headList.add("高度(mm)");
        headList.add("壁厚(mm)");
        headList.add("下圆角半径(mm)");
        headList.add("上圆角半径(mm)");
        headList.add("油箱容量(L)");
        headList.add("备注");
        // 必填字段
        requiredList.add("油箱型号");
        requiredList.add("油箱形状");
        requiredList.add("长度(mm)");
        requiredList.add("宽度(mm)");
        requiredList.add("高度(mm)");
        // 默认设置一条数据
        exportList.add("一汽解放J6M*");
        exportList.add("长方体");
        exportList.add("150MM");
        exportList.add("120MM");
        exportList.add("110MM");
        exportList.add("3MM");
        exportList.add("50MM");
        exportList.add("50MM");
        exportList.add("131");
        exportList.add("备注");

        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<String, String[]>();
        // 油箱形状
        String[] tankShape = { "长方体", "圆柱形", "D形", "椭圆形" };
        selectMap.put("油箱形状", tankShape);
        // ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        ExportOilBoxExcel export = new ExportOilBoxExcel(TEMPLATE_COMMENT, headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 导出
     */
    @Override
    public boolean exportTank(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, FuelTankForm.class, 1, null);
        List<FuelTankForm> exportList = new ArrayList<FuelTankForm>();
        exportList = fuelTankManageDao.findFuelTankByPage(null);
        if (exportList != null && exportList.size() > 0) {
            for (FuelTankForm form : exportList) {
                if (Converter.toBlank(form.getShape()).equals("01") || Converter.toBlank(form.getShape()).equals("1")) {
                    form.setShape("长方体");
                } else if (Converter.toBlank(form.getShape()).equals("02") || Converter.toBlank(form.getShape())
                    .equals("2")) {
                    form.setShape("圆柱形");
                } else if (Converter.toBlank(form.getShape()).equals("03") || Converter.toBlank(form.getShape())
                    .equals("3")) {
                    form.setShape("D形");
                } else if (Converter.toBlank(form.getShape()).equals("04") || Converter.toBlank(form.getShape())
                    .equals("4")) {
                    form.setShape("椭圆形");
                } else {
                    form.setShape("其他");
                }
            }
        }
        export.setDataList(exportList);
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    @Override
    public boolean findIsBond(String id) throws Exception {
        Integer num = fuelTankManageDao.findOilVehicleSettingByOilBoxId(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<FuelTankForm> getFuelTankDetailByVehicleId(String vehicleId) throws Exception {
        List<FuelTankForm> result = fuelTankManageDao.getFuelTankDetailByVehicleId(vehicleId);
        return result;
    }

    @Override
    public boolean findBoxBound(String oilBoxId) throws Exception {
        Integer num = fuelTankManageDao.checkBoxBound(oilBoxId);
        if (num > 0) { // 被绑定了
            return true;
        }
        return false;
    }

    @Override
    public FuelTankForm getOilBoxByType(String id, String type) throws Exception {
        return fuelTankManageDao.isExist(id, type);
    }

    @Override
    public FuelTank findFuelTankById(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            return fuelTankManageDao.findFuelTankById(id);
        }
        return null;
    }
}
