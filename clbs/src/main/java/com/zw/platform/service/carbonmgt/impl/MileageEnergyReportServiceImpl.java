package com.zw.platform.service.carbonmgt.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.carbonmgt.MileageEnergyReportExport1;
import com.zw.platform.domain.vas.carbonmgt.MileageEnergyReportExport2;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.repository.vas.EquipDao;
import com.zw.platform.service.carbonmgt.MileageEnergyReportService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.CollectionsSortUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class MileageEnergyReportServiceImpl implements MileageEnergyReportService {
    private static Logger log = LogManager.getLogger(MileageEnergyReportServiceImpl.class);
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

    @Override
    public List<VehicleInfo> getVehicleInfoList(String groupId) throws Exception {
        // 获取当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<OrganizationLdap> userOrgListId = userService.getOrgChild(groupId);
        List<String> orgChildList = new ArrayList<>();
        if (null != userOrgListId && userOrgListId.size() > 0) {
            for (OrganizationLdap o : userOrgListId) {
                orgChildList.add(Converter.toBlank(o.getUuid()));
            }
        }
        List<VehicleInfo> list = newVehicleDao.findVehicleByUserAndGroupForVasMileage(userId, orgChildList);
        return list;
    }

    @Override
    public List<MobileSourceEnergyReportForm> queryByDate(String queryWay, String startDate, String endDate,
        String groupId, String vehicleId) throws Exception {
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
                e.printStackTrace();
            }

            List<String> vidList = new ArrayList<>(); // 车辆id集合
            if (null != vehicleInfoList && vehicleInfoList.size() > 0) {
                for (VehicleInfo v : vehicleInfoList) {
                    vidList.add(Converter.toBlank(v.getId()));
                }
            }
            positionalList = getPositionalList_statistics_time(vehicleId, startDate1, endDate1, vidList);
        } else {
            positionalList = getPositionalList_statistics_time(vehicleId, startDate1, endDate1, null);
        }

        if (Converter.toBlank(queryWay).equals("list1") || Converter.toBlank(queryWay).equals("list2")) { // 按日期统计
            // step2:提取出所有的时间：时间格式yyyy-MM-dd（List<String> timeList）
            List<String> timeList = getTimeList(positionalList);
            // step3:把一个时间下面的所有记录整理出来：（Map<String, Map<String, List<Positional>>> map）
            Map<String, Map<String, List<Positional>>> map = getMap(timeList, positionalList);
            // step4:按查询方式，计算出相应统计方式下，对应的显示值
            list = setFormValue_date(map, queryWay);
        } else if (Converter.toBlank(queryWay).equals("list3") || Converter.toBlank(queryWay).equals("list4")
            || Converter.toBlank(queryWay).equals("list5")) { // 按月份统计
            // step2:提取出所有的车辆id（List<String> vidList）
            List<String> vehicleIdList = getVidList(positionalList);
            // step3:按车辆id把一个车的所有数据整理出来：（Map<String, List<Positional>> map）
            Map<String, List<Positional>> map = getVehicleMap(vehicleIdList, positionalList);
            // step4:计算车辆在统计时间段的相应数据
            list = setFormValue_month_quarter_year(queryWay, map);
        }
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
                        if (Converter.toBlank(queryWay).equals("list1")) {
                            calculateForm_queryByDate(list, list1);
                        } else {
                            calculateForm(list, list1);
                        }
                    }
                }
            }

        }
        CollectionsSortUtil sort = new CollectionsSortUtil();
        sort.sortByMethod_mobileS(list, "getTime", true);
        return list;
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
            double airDuration = 0;
            MobileSourceEnergyReportForm t = list1.get(0);
            EquipForm ef = equipDao.findBenchmarkByVehicleId(Converter.toBlank(t.getVehicleId()));
            String baseBenchmark = (null == ef ? "" :
                (Converter.toBlank(ef.getMileageBenchmark()).equals("") ? "" : ef.getMileageBenchmark()));
            String baseBenchmarkAmount = Converter.toBlank(Converter.toDouble(baseBenchmark, 0.0) * 100);
            String currentEnergyConsumptionAmount =
                Converter.toBlank(Converter.toDouble(t.getTotalFuelConsumption(), 0.0));
            VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
            String fuelType = getFuelTypeName(vehicleDTO.getFuelType());
            for (MobileSourceEnergyReportForm t1 : list1) {
                totalFuel += Converter.toDouble(t1.getTotalFuelConsumption());
                duration += Converter.toDouble(t1.getDuration());
                airDuration += Converter.toDouble(t1.getAirConditionerDuration());
                mileage += Converter.toDouble(t1.getMileage());
            }
            String averageEnergy =
                CalculateUtil.getCurAverageEnergy_by_mile(Converter.toBlank(totalFuel), Converter.toBlank(mileage));
            String energySavingFuelByMile =
                CalculateUtil.getEnergySaving_fuel_by_mile(baseBenchmark, averageEnergy, Converter.toBlank(mileage));
            String energySavingCoal = CalculateUtil.getEnergySaving_coal(energySavingFuelByMile, fuelType);
            String reduceEmissionsCo2 = CalculateUtil.getReduceEmissions_CO2(energySavingFuelByMile, fuelType);
            String reduceEmissionsCo21 = CalculateUtil.getReduceEmissions_CO2(baseBenchmarkAmount, fuelType);
            String reduceEmissionsCo22 = CalculateUtil.getReduceEmissions_CO2(currentEnergyConsumptionAmount, fuelType);
            String energySavingRate = "".equals(baseBenchmark) ? "" :
                CalculateUtil.getEnergySavingRate_mile(energySavingFuelByMile, baseBenchmark, mileage);
            String averageSpeed = CalculateUtil.averageSpeed(Converter.toBlank(mileage), duration);

            t.setVehicleType(vehicleDTO.getVehicleType());
            t.setFuelType(vehicleDTO.getFuelType());
            t.setDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(duration)));
            t.setAirConditionerDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(airDuration)));
            t.setMileage(df_1.format(Converter.toDouble(mileage)));
            t.setAverageSpeed(df_2.format(Converter.toDouble(averageSpeed)));
            t.setBaseBenchmark(df_2.format(Converter.toDouble(baseBenchmarkAmount)));
            t.setCurrentAverageEnergyConsumption(df_2.format(Converter.toDouble(averageEnergy, 0.0)));
            t.setBaseBenchmarkAmount(df_2.format(Converter.toDouble(baseBenchmarkAmount)));
            t.setCurrentEnergyConsumptionAmount(df_2.format(Converter.toDouble(currentEnergyConsumptionAmount)));
            t.setEnergySaving_fuel(df_2.format(Converter.toDouble(energySavingFuelByMile)));
            t.setEnergySaving_standardCoal(df_2.format(Converter.toDouble(energySavingCoal)));
            t.setBaseEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo21)));
            t.setBaseEmissions_SO2("");
            t.setBaseEmissions_NOX("");
            t.setBaseEmissions_HCX("");
            t.setCurEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo22)));
            t.setCurEmissions_SO2("");
            t.setCurEmissions_NOX("");
            t.setCurEmissions_HCX("");
            t.setEnergySavingRate(energySavingRate);
            t.setReduceEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo2)));
            t.setReduceEmissions_SO2("");
            t.setReduceEmissions_NOX("");
            t.setReduceEmissions_HCX("");

            list.add(t);
        }
    }

    private void calculateForm_queryByDate(List<MobileSourceEnergyReportForm> list,
        List<MobileSourceEnergyReportForm> list1) {
        if (null != list1 && list1.size() > 0) {
            double totalFuel = 0;
            double duration = 0;
            double airDuration = 0;
            double mileage = 0;
            if (null != list1 && list1.size() > 0) {
                for (MobileSourceEnergyReportForm t : list1) {
                    EquipForm ef = equipDao.findBenchmarkByVehicleId(Converter.toBlank(t.getVehicleId()));
                    String baseBenchmark = (null == ef ? "" : Converter.toBlank(ef.getMileageBenchmark()));
                    VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
                    String fuelType = getFuelTypeName(vehicleDTO.getFuelType());
                    totalFuel = Converter.toDouble(t.getTotalFuelConsumption());
                    duration = Converter.toDouble(t.getDuration());
                    airDuration = Converter.toDouble(t.getAirConditionerDuration());
                    mileage = Converter.toDouble(t.getMileage());

                    // <当期平均能耗>：=(总能耗量/里程)*100；
                    String averageEnergy = CalculateUtil
                        .getCurAverageEnergy_by_mile(Converter.toBlank(totalFuel), Converter.toBlank(mileage));
                    // <能源节约量>：=（基准能耗 - 当期平均能耗）*里程/100；（里程转换成百公里）
                    String energySavingFuelByMile = CalculateUtil
                        .getEnergySaving_fuel_by_mile(baseBenchmark, averageEnergy, Converter.toBlank(mileage));
                    String energySavingCoal = CalculateUtil.getEnergySaving_coal(energySavingFuelByMile, fuelType);
                    String reduceEmissionsCo2 = CalculateUtil.getReduceEmissions_CO2(energySavingFuelByMile, fuelType);
                    String energySavingRate =
                        CalculateUtil.getEnergySavingRate_mile(energySavingFuelByMile, baseBenchmark, mileage);
                    String averageSpeed = CalculateUtil.averageSpeed(t.getMileage(), duration);

                    t.setVehicleType(vehicleDTO.getVehicleType());
                    t.setFuelType(vehicleDTO.getFuelType());
                    t.setDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(duration)));
                    t.setAirConditionerDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(airDuration)));
                    t.setRollingDuration("");
                    t.setTotalFuelConsumption(df_2.format(Converter.toDouble(totalFuel)));
                    t.setBaseBenchmark(df_2.format(Converter.toDouble(baseBenchmark)));
                    t.setCurrentAverageEnergyConsumption(df_2.format(Converter.toDouble(averageEnergy, 0.0)));
                    t.setEnergySaving_fuel(df_2.format(Converter.toDouble(energySavingFuelByMile)));
                    t.setEnergySaving_standardCoal(df_2.format(Converter.toDouble(energySavingCoal)));
                    t.setReduceEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo2)));
                    t.setReduceEmissions_SO2("");
                    t.setReduceEmissions_NOX("");
                    t.setReduceEmissions_HCX("");
                    t.setEnergySavingRate(df_2.format(Converter.toDouble(energySavingRate)));
                    t.setAverageSpeed(df_2.format(Converter.toDouble(averageSpeed)));
                    t.setMileage(df_1.format(Converter.toDouble(mileage)));
                    list.add(t);
                }
            }
        }
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
     * @param queryWay
     * @param map
     * @return List<MobileSourceEnergyReportForm>
     * @throws
     * @Title: setFormValue_month_quarter_year
     * @author Liubangquan
     */
    private List<MobileSourceEnergyReportForm> setFormValue_month_quarter_year(String queryWay,
        Map<String, List<Positional>> map) {
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
                String time = "";
                if ("list3".equals(queryWay)) { // 按月份统计
                    time = year + "年" + month + "月";
                } else if ("list4".equals(queryWay)) { // 按季度统计
                    if (Converter.toInteger(month) >= 1 && Converter.toInteger(month) <= 3) {
                        time = year + "年第一季度";
                    } else if (Converter.toInteger(month) >= 4 && Converter.toInteger(month) <= 6) {
                        time = year + "年第二季度";
                    } else if (Converter.toInteger(month) >= 7 && Converter.toInteger(month) <= 9) {
                        time = year + "年第三季度";
                    } else if (Converter.toInteger(month) >= 10 && Converter.toInteger(month) <= 12) {
                        time = year + "年第四季度";
                    }
                } else if ("list5".equals(queryWay)) { // 按年份统计
                    time = year + "年";
                }
                mergePositional(time, positionalList, list1);
                calculateForm(list, list1);
            }
        }
        return list;
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

    @Override
    public boolean exportMileage(String title, int type, HttpServletResponse response, MileageQuery query)
        throws Exception {
        String startTime = query.getStartDate();
        String endTime = query.getEndDate();
        String queryWay = query.getQueryWay();
        String groupId = query.getGroupId();
        String year = query.getYear();
        String month = query.getMonth();
        String quarter = query.getQuarter();
        String vehicleId = query.getBrand();
        OrganizationLdap org = userService.findOrganization(groupId);
        String groupName = org.getName();
        String time = null;

        if (queryWay.equals("list1") || queryWay.equals("list2")) {
            time = startTime + "--" + endTime;
        } else if (queryWay.equals("list3")) {
            time = year + "年" + month + "月";
        } else if (queryWay.equals("list4")) {
            time = year + "年第" + quarter + "季度";
        } else if (queryWay.equals("list5")) {
            time = year + "年";
        }
        String groups = "企业名称:" + groupName;
        ExportExcel export = null;

        List<MobileSourceEnergyReportForm> list = queryByDate(queryWay, startTime, endTime, groupId, vehicleId);
        if (queryWay.equals("list1")) {
            export = new ExportExcel(title, MileageEnergyReportExport1.class, 1, time, groups, null);
            List<MileageEnergyReportExport1> exportList2 = new ArrayList<MileageEnergyReportExport1>();
            for (MobileSourceEnergyReportForm info : list) {
                MileageEnergyReportExport1 form = new MileageEnergyReportExport1();
                BeanUtils.copyProperties(info, form);
                exportList2.add(form);
            }
            export.setDataList(exportList2);
        } else {
            export = new ExportExcel(title, MileageEnergyReportExport2.class, 1, time, groups, null);
            List<MileageEnergyReportExport2> exportList1 = new ArrayList<MileageEnergyReportExport2>();
            for (MobileSourceEnergyReportForm info : list) {
                MileageEnergyReportExport2 form = new MileageEnergyReportExport2();
                BeanUtils.copyProperties(info, form);
                form.setDate(info.getTime());
                exportList1.add(form);
            }
            export.setDataList(exportList1);
            if (queryWay.equals("list3") || queryWay.equals("list4")) {
                String foot1 = "甲方负责人：                                                    乙方负责人：";
                String foot2 = "甲方统计员：                                                    乙方统计员：";
                export.foot(foot1, foot2);
            }
        }
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();

        return true;
    }
}
