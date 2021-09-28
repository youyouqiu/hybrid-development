package com.zw.platform.service.carbonmgt.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.repository.vas.BasicManagementDao;
import com.zw.platform.repository.vas.EquipDao;
import com.zw.platform.service.carbonmgt.ComprehensiveEnergyReportService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.CollectionsSortUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.RegexUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class ComprehensiveEnergyReportServiceImpl implements ComprehensiveEnergyReportService {
    private static Logger log = LogManager.getLogger(ComprehensiveEnergyReportServiceImpl.class);
    // 保留4位小数
    private static DecimalFormat df = new DecimalFormat("0.0000");
    private static DecimalFormat df_1 = new DecimalFormat("0.0"); // 保留一位小数
    private static DecimalFormat df_2 = new DecimalFormat("0.00"); // 保留两位小数
    @Autowired
    private UserService userService;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private EquipDao equipDao;
    @Autowired
    private BasicManagementDao basicManagementDao;

    @Override
    public List<VehicleInfo> getVehicleInfoList(String groupId) throws BusinessException {
        // 获取当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<OrganizationLdap> userOrgListId = userService.getOrgChild(groupId);
        List<String> orgChildList = new ArrayList<>();
        if (null != userOrgListId && userOrgListId.size() > 0) {
            for (OrganizationLdap o : userOrgListId) {
                orgChildList.add(Converter.toBlank(o.getUuid()));
            }
        }
        List<VehicleInfo> list = null;
        try {
            list = newVehicleDao.findVehicleByUserAndGroupForVasMileage(userId, orgChildList);
        } catch (Exception e) {
            log.error("getVehicleInfoList异常" + e);
        }
        return list;
    }

    @Override
    public List<MobileSourceEnergyReportForm> queryByDate(String startDate, String endDate, String groupId,
        String vehicleId, String year, String month) {
        List<MobileSourceEnergyReportForm> list = new ArrayList<>();
        List<Positional> positionalList = null;
        // step1:查询出符合条件的数据
        final long startDate1 = Converter.convertToUnixTimeStamp(startDate);
        final long endDate1 = Converter.convertToUnixTimeStamp(endDate);
        if (Converter.toBlank(vehicleId).equals("")) {
            List<VehicleInfo> vehicleInfoList = null;
            try {
                vehicleInfoList = getVehicleInfoList(groupId);
            } catch (BusinessException e) {
                log.error("error", e);
            }
            List<String> vidList = new ArrayList<>(); // 车辆id集合
            if (null != vehicleInfoList && vehicleInfoList.size() > 0) {
                for (VehicleInfo v : vehicleInfoList) {
                    vidList.add(Converter.toBlank(v.getId()));
                }
            }
            try {
                positionalList = getPositionalList_statistics_time(vehicleId, startDate1, endDate1, vidList);
            } catch (Exception e) {
                log.error("queryByDate异常" + e);
            }
        } else {
            try {
                positionalList = getPositionalList_statistics_time(vehicleId, startDate1, endDate1, null);
            } catch (Exception e) {
                log.error("queryByDate异常" + e);
            }
        }
        String queryWay = "";
        if (month.equals("")) { // month为空表示按年统计
            queryWay = "list3";
        } else { // 否则是按月统计
            queryWay = "list2";
        }
        // step2:提取出所有的时间：时间格式yyyy-MM-dd（List<String> timeList）
        List<String> timeList = getTimeList(positionalList);
        // step3:把一个时间下面的所有记录整理出来：（Map<String, Map<String, List<Positional>>> map）
        Map<String, Map<String, List<Positional>>> map = getMap(timeList, positionalList);
        // step4:按查询方式，计算出相应统计方式下，对应的显示值
        list = setFormValue_date(map, "list2");

        if (queryWay.equals("list3")) { // 按年统计
            // step2:提取出所有的时间：时间格式yyyy-MM-dd（List<String> timeList）
            List<String> timeList1 = getTimeList1(list);
            // step3:把一个时间下面的所有记录整理出来：（Map<String, Map<String, List<Positional>>> map）
            Map<String, Map<String, List<MobileSourceEnergyReportForm>>> map1 = getMap1(timeList1, list);
            // step4:按查询方式，计算出相应统计方式下，对应的显示值
            list = setFormValue_date1(map1, "list3");
        }

        setTotalData(list);
        return list;
    }

    private List<Positional> getPositionalList_statistics_time(String vehicleId, long startDate1, long endDate1,
                                                               List<String> vidList) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vidList", JSON.toJSONString(vidList));
        params.put("startTime", String.valueOf(startDate1));
        params.put("endTime", String.valueOf(endDate1));
        params.put("vehicle", vehicleId);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_POSITIONAL_LIST_STATISTICS_TIME, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    /**
     * 计算当月或者当年的总的数据
     * @param list
     * @return void
     * @throws
     * @Title: setTotalData
     * @author Liubangquan
     */
    private void setTotalData(List<MobileSourceEnergyReportForm> list) {
        if (null != list && list.size() > 0) {
            double totalSavingEnergy = 0;
            double totalBaseBenchmark = 0;
            double totalReduceEmissions = 0;
            for (MobileSourceEnergyReportForm form : list) {
                totalSavingEnergy += Converter.toDouble(form.getSavingEnergy());
                totalBaseBenchmark += Converter.toDouble(form.getBaseBenchmark());
                totalReduceEmissions += Converter.toDouble(form.getReduceEmissionsAmount());
            }
            MobileSourceEnergyReportForm f = list.get(0);
            String fuelType = getFuelTypeName(f.getFuelType());
            double savingRate =
                Converter.toDouble(totalBaseBenchmark) > 0 ? totalSavingEnergy / totalBaseBenchmark * 100 : 0;
            double totalSavingFee = Converter.toDouble(f.getEnergyPrice()) * totalReduceEmissions;
            String reduceEmissionsCo2 =
                CalculateUtil.getReduceEmissions_CO2(Converter.toBlank(totalReduceEmissions), fuelType);

            f.setEnergySaving_curPeriod(df.format(totalSavingEnergy)); // 当期节能
            f.setEnergySavingRate_curPeriod(Converter.toBlank(savingRate)); // 当期节能率
            f.setEnergySavingFee_curPeriod(df.format(totalSavingFee)); // 当期节省费用
            f.setCurTotalReduceEmissions_CO2(df.format(Converter.toDouble(reduceEmissionsCo2))); // 当其总的减排量-CO2
        }
    }

    /**
     * step2:提取出所有的时间：时间格式yyyy-MM-dd（List<String> timeList）
     * @param positionalList
     * @return List<String>
     * @throws
     * @Title: getTimeList
     * @author Liubangquan
     */
    private List<String> getTimeList(List<Positional> positionalList) {
        List<String> timeList = null;
        if (null != positionalList && positionalList.size() > 0) {
            for (Positional p : positionalList) {
                boolean flag = false; // 用来标记相同的时间是否已经放到list中
                String vtimeStr =
                    Converter.toBlank(p.getVtime()).equals("") ? "" : Converter.convertUnixToDatetime(p.getVtime());
                p.setVtimeStr(vtimeStr);
                String time = Converter.toBlank(vtimeStr).equals("") ? "" : vtimeStr.substring(0, 10);
                if (null == timeList) {
                    timeList = new ArrayList<>();
                    if (!"".equals(time)) {
                        timeList.add(time);
                    }
                } else {
                    for (String str : timeList) {
                        if (time.equals(str)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        if (!"".equals(time)) {
                            timeList.add(time);
                        }
                    }
                }
            }
        }
        return timeList;
    }

    /**
     * step2:提取出所有的时间：时间格式yyyy-MM（List<String> timeList1）
     * @param positionalList
     * @return List<String>
     * @throws
     * @Title: getTimeList1
     * @author Liubangquan
     */
    private List<String> getTimeList1(List<MobileSourceEnergyReportForm> positionalList) {
        List<String> timeList = null;
        if (null != positionalList && positionalList.size() > 0) {
            for (MobileSourceEnergyReportForm p : positionalList) {
                boolean flag = false; // 用来标记相同的时间是否已经放到list中
                String vtimeStr = p.getTime();
                String time = Converter.toBlank(vtimeStr).equals("") ? "" : vtimeStr.substring(0, 7);
                if (null == timeList) {
                    timeList = new ArrayList<>();
                    if (!"".equals(time)) {
                        timeList.add(time);
                    }
                } else {
                    for (String str : timeList) {
                        if (time.equals(str)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        if (!"".equals(time)) {
                            timeList.add(time);
                        }
                    }
                }
            }
        }
        return timeList;
    }

    /**
     * step3:把一个时间下面的所有记录整理出来：（Map<String, Map<String, List<Positional>>> map）
     * @param timeList
     * @param positionalList
     * @return Map<String, Map < String, List < Positional>>>
     * @throws
     * @Title: getMap
     * @author Liubangquan
     */
    private Map<String, Map<String, List<Positional>>> getMap(List<String> timeList, List<Positional> positionalList) {
        Map<String, Map<String, List<Positional>>> map = null;
        Map<String, List<Positional>> positionalMap = null;
        if (null != timeList && timeList.size() > 0) {
            map = new HashMap<>();
            for (String str : timeList) {
                List<Positional> tempPList = new ArrayList<>(); // 相同时间下所有的车
                for (Positional p : positionalList) {
                    String time = Converter.toBlank(p.getVtimeStr()).equals("") ? "" :
                        Converter.toBlank(p.getVtimeStr()).substring(0, 10);
                    if (str.equals(time)) {
                        tempPList.add(p);
                    }
                }
                if (null != tempPList && tempPList.size() > 0) {
                    List<String> vidList = new ArrayList<>(); // 车辆id集合
                    // 把所有不重复的vid放入vidList
                    for (Positional p1 : tempPList) {
                        String vid = String.valueOf(UuidUtils.getUUIDFromBytes(p1.getVehicleId()));
                        vidList.add(vid);
                    }
                    // 去重
                    if (null != vidList && vidList.size() > 0) {
                        vidList = removeDuplicateValue(vidList);
                    }
                    // 把vid一样的记录放入map
                    if (null != vidList && vidList.size() > 0) {
                        positionalMap = new HashMap<>();
                        List<Positional> poList = null;
                        for (String v : vidList) {
                            poList = new ArrayList<>();
                            for (Positional p : tempPList) {
                                if (v.equals(UuidUtils.getUUIDStrFromBytes(p.getVehicleId()))) {
                                    poList.add(p);
                                }
                            }
                            positionalMap.put(v, poList);
                        }
                    }
                }
                map.put(str, positionalMap);
            }
        }
        return map;
    }

    /**
     * step3:把一个时间下面的所有记录整理出来：（Map<String, Map<String, List<MobileSourceEnergyReportForm>>> map）
     * @param timeList
     * @param positionalList
     * @return Map<String, Map < String, List < Positional>>>
     * @throws
     * @Title: getMap
     * @author Liubangquan
     */
    private Map<String, Map<String, List<MobileSourceEnergyReportForm>>> getMap1(List<String> timeList,
        List<MobileSourceEnergyReportForm> positionalList) {
        Map<String, Map<String, List<MobileSourceEnergyReportForm>>> map = null;
        Map<String, List<MobileSourceEnergyReportForm>> positionalMap = null;
        if (null != timeList && timeList.size() > 0) {
            map = new HashMap<>();
            for (String str : timeList) {
                List<MobileSourceEnergyReportForm> tempPList = new ArrayList<>(); // 相同时间下所有的车
                for (MobileSourceEnergyReportForm p : positionalList) {
                    String time =
                        Converter.toBlank(p.getTime()).equals("") ? "" : Converter.toBlank(p.getTime()).substring(0, 7);
                    if (str.equals(time)) {
                        p.setTime(time);
                        tempPList.add(p);
                    }
                }
                if (null != tempPList && tempPList.size() > 0) {
                    List<String> vidList = new ArrayList<>(); // 车辆id集合
                    // 把所有不重复的vid放入vidList
                    for (MobileSourceEnergyReportForm p1 : tempPList) {
                        String vid = p1.getVehicleId();
                        vidList.add(vid);
                    }
                    // 去重
                    if (null != vidList && vidList.size() > 0) {
                        vidList = removeDuplicateValue(vidList);
                    }
                    // 把vid一样的记录放入map
                    if (null != vidList && vidList.size() > 0) {
                        positionalMap = new HashMap<>();
                        List<MobileSourceEnergyReportForm> poList = null;
                        for (String v : vidList) {
                            poList = new ArrayList<>();
                            for (MobileSourceEnergyReportForm p : tempPList) {
                                if (v.equals(p.getVehicleId())) {
                                    poList.add(p);
                                }
                            }
                            positionalMap.put(v, poList);
                        }
                    }
                }
                map.put(str, positionalMap);
            }
        }
        return map;
    }

    /**
     * step4:按查询方式，计算出相应统计方式下，对应的显示值
     * @param map
     * @param queryWay
     * @return List<MobileSourceEnergyReportForm>
     * @throws
     * @Title: setFormValue_date
     * @author Liubangquan
     */
    private List<MobileSourceEnergyReportForm> setFormValue_date(Map<String, Map<String, List<Positional>>> map,
        String queryWay) {
        List<MobileSourceEnergyReportForm> list = new ArrayList<>();
        if (null != map && map.size() > 0) {
            for (Map.Entry<String, Map<String, List<Positional>>> entry : map.entrySet()) {
                String time = entry.getKey();
                Map<String, List<Positional>> map1 = entry.getValue();
                if (null != map1 && map1.size() > 0) {
                    for (Map.Entry<String, List<Positional>> entry1 : map1.entrySet()) {
                        List<MobileSourceEnergyReportForm> list1 = new ArrayList<>();
                        List<Positional> positionalList = entry1.getValue();
                        mergePositional(time, positionalList, list1);
                        calculateForm(list, list1);
                    }
                }
            }

        }
        CollectionsSortUtil sort = new CollectionsSortUtil();
        sort.sortByMethod_mobileS(list, "getTime", false);
        return list;
    }

    /**
     * step4:按查询方式，计算出相应统计方式下，对应的显示值
     * @param map
     * @param queryWay
     * @return List<MobileSourceEnergyReportForm>
     * @throws
     * @Title: setFormValue_date1
     * @author Liubangquan
     */
    private List<MobileSourceEnergyReportForm> setFormValue_date1(
        Map<String, Map<String, List<MobileSourceEnergyReportForm>>> map, String queryWay) {
        List<MobileSourceEnergyReportForm> list = new ArrayList<>();
        if (null != map && map.size() > 0) {
            for (Map.Entry<String, Map<String, List<MobileSourceEnergyReportForm>>> entry : map.entrySet()) {
                Map<String, List<MobileSourceEnergyReportForm>> map1 = entry.getValue();
                if (null != map1 && map1.size() > 0) {
                    for (Map.Entry<String, List<MobileSourceEnergyReportForm>> entry1 : map1.entrySet()) {
                        calculateForm(list, entry1.getValue());
                    }
                }
            }

        }
        CollectionsSortUtil sort = new CollectionsSortUtil();
        sort.sortByMethod_mobileS(list, "getTime", false);
        return list;
    }

    /**
     * List去重
     * @param list
     * @return List<String>
     * @throws
     * @Title: removeDuplicateValue
     * @author Liubangquan
     */
    private List<String> removeDuplicateValue(List<String> list) {
        if (null != list && list.size() > 0) {
            HashSet<String> h = new HashSet<String>(list);
            list.clear();
            list.addAll(h);
            return list;
        }
        return null;
    }

    /**
     * 将acc的开与关的两条记录合并成一条记录
     * @param time
     * @param positionalList
     * @param list
     * @return void
     * @throws
     * @Title: mergePositional
     * @author Liubangquan
     */
    private void mergePositional(String time, List<Positional> positionalList,
        List<MobileSourceEnergyReportForm> list) {
        boolean flag = false; // acc开关标识
        int acc = 0; // acc状态默认为0
        int air = 0; // 空调开启状态默认为0
        String startDate = ""; // 空调开启时间
        String endDate = ""; // 空调关闭时间
        double airTime = 0; // 空调开启时长

        MobileSourceEnergyReportForm tesf = null;
        if (null != positionalList && positionalList.size() > 0) {
            for (int i = 0; i < positionalList.size(); i++) {
                Positional p = positionalList.get(i);
                acc = CalculateUtil.getStatus(Converter.toBlank(p.getStatus())).getInteger("acc");
                air = Converter.toInteger(p.getAirConditionStatus(), 0);
                String longtitude = String.valueOf(p.getLongtitude());
                String latitude = String.valueOf(p.getLatitude());
                String coordinate = null;
                String date =
                    Converter.toBlank(p.getVtimeStr()).equals("") ? Converter.convertUnixToDatetime(p.getVtime()) :
                        Converter.toBlank(p.getVtimeStr());

                //-------------空调时长计算-start--------------
                if (acc == 1 && air != 0 && startDate.equals("")) {
                    startDate = date;
                }
                if (acc == 1 && air == 0) { // 空调关
                    if (endDate.equals("") && !startDate.equals("")) {
                        endDate = date;
                        double duration = Converter.toDouble(CalculateUtil.toDateTime(endDate, startDate));
                        airTime += duration;
                        startDate = "";
                        endDate = "";
                    }
                } else if (endDate.equals("") && !startDate.equals("")) {
                    if (i == positionalList.size() - 1) {
                        endDate =
                            Converter.toBlank(positionalList.get(positionalList.size() - 1).getVtimeStr()).equals("")
                                ?
                                Converter
                                    .convertUnixToDatetime(positionalList.get(positionalList.size() - 1).getVtime()) :
                                Converter.toBlank(positionalList.get(positionalList.size() - 1).getVtimeStr());
                        double duration = Converter.toDouble(CalculateUtil.toDateTime(endDate, startDate));
                        airTime += duration;
                        startDate = "";
                        endDate = "";
                    }
                }
                // --------------空调时长计算-end-------------------

                if (longtitude.equals("") || longtitude.equals("null") || latitude.equals("") || latitude.equals("null")
                    || longtitude == null || latitude == null) {
                    coordinate = "未定位";
                } else {
                    coordinate = String.valueOf(longtitude + "," + latitude);
                }
                if (flag) { // 表示acc为1了，已经打火用于判断打火熄火这一过程是否满足2次，如不满足则不记录
                    if (acc == 0) {
                        tesf.setEndDate(date);
                        tesf.setEndCoordinate(Converter.toBlank(coordinate));
                        tesf.setEndOil(Converter.toBlank(p.getTotalOilwearOne()));
                        tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                        tesf.setTotalFuelConsumption(
                            Converter.toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                        tesf.setEndMileage(Converter.toBlank(p.getGpsMile(), "0"));
                        Double start = Double.parseDouble(tesf.getStartMileage());
                        Double end = Double.parseDouble(Converter.toBlank(p.getGpsMile()));
                        String mileage = String.valueOf((end - start) / 10);
                        tesf.setMileage(Converter.toBlank(mileage));
                        // 当【熄火时间】减去【打火时间】小于60秒的话过滤掉该条记录；
                        String duration = CalculateUtil.toDateTime(tesf.getEndDate(), tesf.getStartDate());
                        if (Converter.toDouble(duration) >= Converter.toDouble(1 / 60)) {
                            tesf.setDuration(CalculateUtil.toDateTime(tesf.getEndDate(), tesf.getStartDate()));
                            list.add(tesf);
                        }

                        tesf = null;
                    }
                    flag = false;
                }
                if (acc == 1 && tesf == null) { // 表示打火开始
                    flag = true;
                    tesf = new MobileSourceEnergyReportForm();
                    tesf.setTime(time);
                    tesf.setVehicleId(Converter.toBlank(p.getVehicleId()));
                    tesf.setVehicleType(Converter.toBlank(p.getVehicleType()));
                    tesf.setFuelType(Converter.toBlank(p.getFuelType()));
                    tesf.setStartDate(date);
                    tesf.setStartCoordinate(Converter.toBlank(coordinate));
                    tesf.setBrand(Converter.toBlank(p.getPlateNumber()));
                    tesf.setStartOil(Converter.toBlank(p.getTotalOilwearOne()));
                    tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                    tesf.setStartMileage(Converter.toBlank(p.getGpsMile(), "0"));
                }
                if (tesf != null && !flag) {
                    if (acc == 1) {
                        // 行驶过程，每次更新行驶末尾状态
                        tesf.setEndDate(date);
                        tesf.setEndCoordinate(Converter.toBlank(coordinate));
                        tesf.setEndOil(Converter.toBlank(p.getTotalOilwearOne()));
                        tesf.setDuration(CalculateUtil.toDateTime(tesf.getEndDate(), tesf.getStartDate()));
                        tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                        tesf.setTotalFuelConsumption(
                            Converter.toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                        tesf.setEndMileage(Converter.toBlank(p.getGpsMile(), "0"));
                        Double start = Double.parseDouble(tesf.getStartMileage());
                        Double end = Double.parseDouble(Converter.toBlank(p.getGpsMile()));
                        String mileage = String.valueOf((end - start) / 10);
                        tesf.setMileage(Converter.toBlank(mileage));
                        //如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                        if (i == positionalList.size() - 1) {
                            list.add(tesf);
                            tesf = null;
                        }
                    } else {
                        // 行驶结束，写入list
                        // 如果只有开始时间，则舍弃这条数据
                        if (tesf != null && tesf.getEndDate() != null) {
                            list.add(tesf);
                        }
                        tesf = null;
                    }
                }
            }
        }
    }

    private void calculateForm(List<MobileSourceEnergyReportForm> list, List<MobileSourceEnergyReportForm> list1) {
        if (null != list1 && list1.size() > 0) {
            double totalFuel = 0;
            double duration = 0;
            double mileage = 0;
            MobileSourceEnergyReportForm t = list1.get(0);
            EquipForm ef = null;
            try {
                ef = equipDao.findBenchmarkByVehicleId(Converter.toBlank(t.getVehicleId()));
            } catch (Exception e) {
                log.error("calculateForm异常" + e);
            }

            String baseBenchmark = (null == ef ? "" :
                (Converter.toBlank(ef.getMileageBenchmark()).equals("") ? "" : ef.getMileageBenchmark()));
            VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
            String fuelType = getFuelTypeName(vehicleDTO.getFuelType());
            String carCity = RegexUtils.getAreaByBrand(Converter.toBlank(vehicleDTO.getName())); // 根据车牌号首字获取其所在的省份
            List<TimingStored> priceList = null;
            try {
                priceList = basicManagementDao.oilPricesQuery(t.getTime(), t.getTime(), carCity, fuelType);
            } catch (Exception e) {
                log.error("calculateForm异常" + e);
            }
            String oilPrice = (null != priceList && priceList.size() > 0) ? priceList.get(0).getOilPrice() : "0";
            for (MobileSourceEnergyReportForm t1 : list1) {
                totalFuel += Converter.toDouble(t1.getTotalFuelConsumption());
                duration += Converter.toDouble(t1.getDuration());
                mileage += Converter.toDouble(t1.getMileage());
            }
            baseBenchmark = Converter.toBlank(Converter.toDouble(baseBenchmark) * (mileage / 100));
            String savingEnergy = Converter.toBlank(Converter.toDouble(baseBenchmark) - Converter.toDouble(totalFuel));
            String averageSpeed = CalculateUtil.averageSpeed(Converter.toBlank(mileage), duration);
            String energySavingRate = (!"".equals(baseBenchmark) && Converter.toDouble(baseBenchmark) > 0)
                ?
                Converter.toBlank(Converter.toDouble(savingEnergy) / Converter.toDouble(baseBenchmark) * 100) : "";
            String actualEmissions = CalculateUtil.energyToEmissions(Converter.toBlank(totalFuel), fuelType);
            String baseEmissions = CalculateUtil.energyToEmissions(Converter.toBlank(baseBenchmark), fuelType);
            String reduceEmissions =
                Converter.toBlank(Converter.toDouble(baseEmissions) - Converter.toDouble(actualEmissions));
            String reduceEmissionsRate = (!"".equals(baseEmissions) && Converter.toDouble(baseEmissions) > 0)
                ?
                Converter.toBlank(Converter.toDouble(reduceEmissions) / Converter.toDouble(baseEmissions) * 100) : "";
            String reduceEmissionsCo2 = CalculateUtil.getReduceEmissions_CO2(reduceEmissions, fuelType);

            t.setVehicleType(vehicleDTO.getVehicleType());
            t.setFuelType(vehicleDTO.getFuelType());
            t.setEnergyPrice(df_2.format(Converter.toDouble(oilPrice)));
            t.setMileage(df_1.format(Converter.toDouble(mileage))); // 里程
            t.setTotalFuelConsumption(df_2.format(totalFuel)); // 实际能耗
            t.setBaseBenchmark(df_2.format(Converter.toDouble(baseBenchmark))); // 基准能耗
            t.setSavingEnergy(df_2.format(Converter.toDouble(savingEnergy))); // 节约能耗
            t.setAverageSpeed(df_2.format(Converter.toDouble(averageSpeed))); // 平均速度
            t.setEnergySavingRate(df_2.format(Converter.toDouble(energySavingRate))); // 节能率
            t.setActualEmissions(df_2.format(Converter.toDouble(actualEmissions))); // 实际排放
            t.setBaseEmissions(df_2.format(Converter.toDouble(baseEmissions))); // 基准排放
            t.setReduceEmissionsAmount(df_2.format(Converter.toDouble(reduceEmissions))); // 减排量
            t.setReduceEmissionsRate(df_2.format(Converter.toDouble(reduceEmissionsRate))); // 减排率

            t.setReduceEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo2)));
            t.setReduceEmissions_SO2("");
            t.setReduceEmissions_NOX("");
            t.setReduceEmissions_HCX("");

            list.add(t);
        }
    }

    /**
     * 获取燃料类型名称
     * @param fuelType
     * @return String
     * @throws
     * @Title: getFuelTypeName
     * @author Liubangquan
     */
    private String getFuelTypeName(String fuelType) {
        String result = "";
        if (Converter.toBlank(fuelType).indexOf("柴油") != -1) {
            result = "柴油";
        } else if (Converter.toBlank(fuelType).indexOf("汽油") != -1) {
            result = "汽油";
        } else if (Converter.toBlank(fuelType).indexOf("CNG") != -1) {
            result = "CNG";
        }
        return result;
    }

    /**
     * step2:提取出所有的车辆id（List<String> vidList）
     * @param positionalList
     * @return List<String>
     * @throws
     * @Title: getVidList
     * @author Liubangquan
     */
    private List<String> getVidList(List<Positional> positionalList) {
        List<String> list = new ArrayList<>();
        if (null != positionalList && positionalList.size() > 0) {
            for (Positional p : positionalList) {
                String vid = Converter.toBlank(p.getVehicleId());
                list.add(vid);
            }
            if (null != list && list.size() > 0) {
                list = removeDuplicateValue(list); // list去重
            }
        }
        return list;
    }

    /**
     * step3:按车辆id把一个车的所有数据整理出来：（Map<String, List<Positional>> map）
     * @param vidList
     * @param positionalList
     * @return Map<String, List < Positional>>
     * @throws
     * @Title: getVehicleMap
     * @author Liubangquan
     */
    private Map<String, List<Positional>> getVehicleMap(List<String> vidList, List<Positional> positionalList) {
        Map<String, List<Positional>> map = null;
        if (null != vidList && vidList.size() > 0) {
            map = new HashMap<>();
            for (String str : vidList) {
                List<Positional> tempList = new ArrayList<>(); // 同一个车的所有数据
                for (Positional p : positionalList) {
                    String vid = Converter.toBlank(p.getVehicleId());
                    if (str.equals(vid)) {
                        tempList.add(p);
                    }
                }
                map.put(str, tempList);
            }
        }
        return map;
    }

    /**
     * step4:计算车辆在统计时间段的相应数据
     * @param map
     * @return List<MobileSourceEnergyReportForm>
     * @throws
     * @Title: setFormValue_month_quarter_year
     * @author Liubangquan
     */
    private List<MobileSourceEnergyReportForm> setFormValue_month_quarter_year(Map<String, List<Positional>> map) {
        List<MobileSourceEnergyReportForm> list = new ArrayList<>();
        if (null != map && map.size() > 0) {
            for (Map.Entry<String, List<Positional>> entry : map.entrySet()) {
                List<MobileSourceEnergyReportForm> list1 = new ArrayList<>();
                List<Positional> positionalList = entry.getValue();
                String vtimeStr = Converter.toBlank(positionalList.get(0).getVtime()).equals("") ? "" :
                    Converter.convertUnixToDatetime(positionalList.get(0).getVtime());
                String date = Converter.toBlank(vtimeStr).equals("") ? "" : vtimeStr.substring(0, 10);
                String year = date.substring(0, 4);
                String month = date.substring(5, 7);
                String time = year + "-" + month;
                mergePositional(time, positionalList, list1);
                calculateForm(list, list1);
            }
        }
        return list;
    }

}
