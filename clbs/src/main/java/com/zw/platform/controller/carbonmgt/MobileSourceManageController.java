package com.zw.platform.controller.carbonmgt;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionlQuery;
import com.zw.platform.domain.vas.carbonmgt.MobileSourceManage;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceManageForm;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.basicinfo.VehicleTypeService;
import com.zw.platform.service.carbonmgt.MobileSourceManageService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;


/**
 * Created by 王健宇 on 2017/2/20. 移动源基准能耗管理
 */
@Controller
@RequestMapping("/v/carbonmgt/basicManagement")
public class MobileSourceManageController {
    private static final String LIST_PAGE = "vas/carbonmgt/basicManagement/mobileSourceManage";

    @Autowired
    private MobileSourceManageService mobileSourceManageService;

    @Autowired
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    private static Logger log = LogManager.getLogger(MobileSourceManageController.class);

    private JSONObject msg = new JSONObject();

    private List<MobileSourceManage> mobileInfo = null;

    @Auth
    @RequestMapping(value = {"/mobileSourceManage"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getOilInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilInfo(String band, String startTime, String endTime) {
        try {
            List<Positional> oilInfo;
            oilInfo = mobileSourceManageService.mobileSourceManage(band, startTime, endTime);
            if (oilInfo.isEmpty()) {
                return new JsonResultBean();
            }
            mobileInfo = new ArrayList<>();
            String sumGpsMile = "0";// 阶段里程变量
            int sumTotalOilwearOne = 0;
            int acc;
            int air;
            String startDate = "";
            String endDate = "";
            double airTime = 0.0;
            int i = 0;
            int j = 0;
            String type = "";
            String groupID = "";
            String groupName = "";
            String fuelType = "";
            double speed = 0.0;
            for (Positional p : oilInfo) {
                MobileSourceManage msm = new MobileSourceManage();
                if (type.equals("")) {
                    type = vehicleTypeService.findByVehicleTypet(p.getVehicleType());
                }
                msm.setVehicleType(type);
                msm.setBrand(p.getPlateNumber());
                if (groupID.equals("")) {
                    groupID = vehicleService.getGroupID(p.getPlateNumber());
                }
                if (groupName.equals("")) {
                    groupName = userService.getOrgByUuid(groupID).getName();
                }
                msm.setGroupName(groupName);
                if (fuelType.equals("")) {
                    fuelType = vehicleService.getFuelType(p.getPlateNumber());
                }
                msm.setFuelType(fuelType);
                String vtime = String.valueOf(p.getVtime());
                String date = Converter.timeStamp2Date(vtime, null);
                msm.setVtime(date);

                msm.setSumGpsMile(p.getGpsMile());
                if (i == 0) {
                    msm.setStageMileage(0);
                } else {
                    int stageMileage = Integer.valueOf(p.getGpsMile()) - Integer.valueOf(sumGpsMile);
                    msm.setStageMileage(stageMileage);
                }
                sumGpsMile = p.getGpsMile();
                msm.setSumTotalOilwearOne(p.getTotalOilwearOne());
                if (i == 0) {
                    msm.setStageTotalOilwearOne(0);
                } else {
                    int stageTotalOilwearOne = Converter.toInteger(p.getTotalOilwearOne(), 0)
                        - sumTotalOilwearOne;
                    msm.setStageTotalOilwearOne(stageTotalOilwearOne);
                }
                sumTotalOilwearOne = Converter.toInteger(p.getTotalOilwearOne(), 0);
                msm.setAirConditionStatus(p.getAirConditionStatus().equals("0") ? "关" : "开");
                acc = (Integer) CalculateUtil.getStatus(p.getStatus()).get("acc");
                air = Integer.valueOf(p.getAirConditionStatus());
                if (acc == 1) {
                    speed += Double.parseDouble(p.getSpeed());
                    j++;
                }
                // -------------空调时长计算-start--------------
                if (acc == 1 && air != 0 && startDate.equals("")) {
                    startDate = date;
                }
                if (acc == 1 && air == 0) { // 空调关
                    if (!startDate.equals("")) {
                        endDate = date;
                        double duration = Converter.toDouble(CalculateUtil.toDateTime(endDate, startDate));
                        airTime += duration;
                        startDate = "";
                    }
                } else if (!startDate.equals("") && i == oilInfo.size() - 1) {
                    endDate = date;
                    double duration = Converter.toDouble(CalculateUtil.toDateTime(endDate, startDate));
                    airTime += duration;
                    startDate = "";
                }
                msm.setAirConditioningDuration(airTime);
                // --------------空调时长计算-end-------------------
                mobileInfo.add(msm);
                i++;
            }
            // 总里程
            if (!oilInfo.isEmpty()) {
                double totalMileage = Converter.toDouble(oilInfo.get(oilInfo.size() - 1).getGpsMile(), 0.0)
                    - Converter.toDouble(oilInfo.get(0).getGpsMile(), 0.0);
                DecimalFormat df = new DecimalFormat("#.##");
                Double averageSpeed;
                if (speed == 0) {
                    averageSpeed = 0.0;
                } else {
                    averageSpeed = Double.valueOf(df.format(speed / j));
                }
                // 总油耗
                Double totalFuelConsumption = Converter.toDouble(
                    oilInfo.get(oilInfo.size() - 1).getTotalOilwearOne(), 0.0)
                    - Converter.toDouble(oilInfo.get(0).getTotalOilwearOne(), 0.0);
                // 每百公里油耗
                Double beTotalFuelConsumption;
                if (totalMileage == 0) {
                    beTotalFuelConsumption = 0.0;
                } else {
                    beTotalFuelConsumption = Double.valueOf(df.format(totalFuelConsumption / totalMileage / 100));
                }
                // 二氧化碳排放量
                // 百公里二氧化碳排放量
                double co2;
                double bco2;
                if (fuelType.contains("柴油")) {
                    co2 = Double.valueOf(df.format(totalFuelConsumption / 1163 * 1.4571 * 2.93));
                    bco2 = Double.valueOf(df.format(beTotalFuelConsumption / 1163 * 1.4571 * 2.93));
                } else if (fuelType.contains("汽油")) {
                    co2 = Double.valueOf(df.format(totalFuelConsumption / 1370 * 1.4714 * 2.93));
                    bco2 = Double.valueOf(df.format(beTotalFuelConsumption / 1370 * 1.4714 * 2.93));
                } else {
                    co2 = 0.0;
                    bco2 = 0.0;
                }
                String userName = SystemHelper.getCurrentUser().getUsername();
                putToRedis(mobileInfo, userName);

                msg.put("totalMileage", totalMileage);
                msg.put("speed", averageSpeed);
                msg.put("totalFuelConsumption", totalFuelConsumption);
                msg.put("bTotalFuelConsumption", beTotalFuelConsumption);
                msg.put("co2", co2);
                msg.put("bco2", bco2);
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取移动源基准能耗信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = "/getOilSpillPagiInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getOilSpillPagiInfo(final PositionlQuery query) {
        final String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_MOBILE.of(username);
        final Page<MobileSourceManage> result =
                RedisUtil.queryPageList(key, HistoryRedisKeyEnum.STATS_MOBILE_OBJECT::of, query);
        return new PageGridBean(query, result, true);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean add(String band, String startTime, String endTime) {
        try {
            boolean flag;
            flag = mobileSourceManageService.find(band, startTime, endTime);
            if (flag) {
                flag = false;
            } else {
                flag = mobileSourceManageService.add(msg, band, startTime, endTime);
            }
            return new JsonResultBean(flag);
        } catch (Exception e) {
            log.error("添加移动源基准能耗信息到基准列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @RequestMapping(value = "/del", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean del(String band, String startTime, String endTime) {
        try {
            boolean flag = mobileSourceManageService.del(band, startTime, endTime);
            return new JsonResultBean(flag);
        } catch (Exception e) {
            log.error("从移动源基准列表中移除移动源基准能耗信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    @RequestMapping(value = "/find", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean find(String band) {
        try {
            List<MobileSourceManageForm> findList = mobileSourceManageService.findList(band);
            double totalMileage = 0.0;
            double speed = 0.0;
            double totalFuelConsumption = 0.0;
            double beTotalFuelConsumption = 0.0;
            double co2 = 0.0;// 二氧化碳
            double bco2 = 0.0;// 百公里二氧化碳
            ArrayList<String> list = new ArrayList<>();
            JSONObject object = new JSONObject();
            int i = findList.size();
            for (MobileSourceManageForm f : findList) {
                JSONObject json = new JSONObject();
                json.put("brand", f.getBrand());
                json.put("startTime", f.getStartTime());
                json.put("endTime", f.getEndTime());
                list.add(String.valueOf(json));
                totalMileage += Converter.toDouble(f.getTotalMileage(), 0.0);
                speed += Converter.toDouble(f.getSpeed(), 0.0);
                totalFuelConsumption += Converter.toDouble(f.getTotalFuelConsumption(), 0.0);
                beTotalFuelConsumption += Converter.toDouble(f.getBTotalFuelConsumption(), 0.0);
                co2 += Converter.toDouble((f.getCo2()), 0.0);
                bco2 += Converter.toDouble(f.getBco2(), 0.0);
            }
            object.put("json", list);
            object.put("totalMileage", totalMileage);
            object.put("speed", speed / i);
            object.put("totalFuelConsumption", totalFuelConsumption);
            object.put("bTotalFuelConsumption", beTotalFuelConsumption / i);
            object.put("co2", co2);
            object.put("bco2", bco2 / i);
            return new JsonResultBean(object);
        } catch (Exception e) {
            log.error("查看移动源基准能耗信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 导出excel表
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, HttpServletRequest request) {
        try {
            String filename = "移动源基准能耗管理列表";
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1") + ".xls");
            response.setContentType("application/msexcel;charset=UTF-8");
            mobileSourceManageService.exportMobileSourceManage(null, 1, response, mobileInfo);
        } catch (Exception e) {
            log.error("导出移动源基准能耗管理列表异常", e);
        }
    }

    private void putToRedis(List<MobileSourceManage> mobileSourceManageList, String userName) {
        Jedis jedis = null;
        List<Map<String, Object>> list = new ArrayList<>();
        int len = mobileSourceManageList.size();
        for (int i = len - 1; i >= 0; i--) {
            Map<String, Object> map = new HashMap<>(16);
            final MobileSourceManage mobileSourceManage = mobileSourceManageList.get(i);
            map.put("id", mobileSourceManage.getId());
            map.put("brand", mobileSourceManage.getBrand());
            map.put("groupName", mobileSourceManage.getGroupName());
            map.put("vehicleType", mobileSourceManage.getVehicleType());
            map.put("fuelType", mobileSourceManage.getFuelType());
            map.put("vtime", mobileSourceManage.getVtime());
            map.put("sumGpsMile", mobileSourceManage.getSumGpsMile());
            map.put("stageMileage", mobileSourceManage.getStageMileage());
            map.put("sumTotalOilwearOne", mobileSourceManage.getSumTotalOilwearOne());
            map.put("stageTotalOilwearOne", mobileSourceManage.getStageTotalOilwearOne());
            map.put("airConditionStatus", mobileSourceManage.getAirConditionStatus());
            map.put("airConditioningDuration", mobileSourceManage.getAirConditioningDuration());
            list.add(map);

        }

        try {
            saveCache(list);
        } catch (Exception e) {
            log.error("移动源基准能耗存储到redis异常" + e);
        }
    }

    private void saveCache(List<Map<String, Object>> list) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey keyAll = HistoryRedisKeyEnum.STATS_MOBILE.of(username);
        com.zw.platform.basic.core.RedisHelper.delete(keyAll);

        final List<String> valueAll = new ArrayList<>();

        final List<RedisKey> expiringKeys = new ArrayList<>();
        expiringKeys.add(keyAll);

        for (Map<String, Object> positional : list) {
            final String id = positional.get("id").toString();
            valueAll.add(id);

            final RedisKey objectKey = HistoryRedisKeyEnum.STATS_MOBILE_OBJECT.of(id);
            final Map<String, String> billMap = RedisUtil.getEncapsulationObject(positional);
            com.zw.platform.basic.core.RedisHelper.addToHash(objectKey, billMap);
            expiringKeys.add(objectKey);
        }
        com.zw.platform.basic.core.RedisHelper.addToList(keyAll, valueAll);

        com.zw.platform.basic.core.RedisHelper.expireKeys(expiringKeys, SIX_HOUR_REDIS_EXPIRE);
    }
}
