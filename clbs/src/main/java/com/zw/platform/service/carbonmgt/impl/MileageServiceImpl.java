package com.zw.platform.service.carbonmgt.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.carbonmgt.MileageList1;
import com.zw.platform.domain.vas.carbonmgt.MileageList2;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.form.MileageForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.repository.vas.EquipDao;
import com.zw.platform.service.carbonmgt.MileageService;
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
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class MileageServiceImpl implements MileageService {
    private static Logger log = LogManager.getLogger(MileageServiceImpl.class);
    private static DecimalFormat df = new DecimalFormat("0.0000");
    @Autowired
    private UserService userService;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private EquipDao equipDao;

    @Override
    public List<VehicleInfo> getVehicleInfoList(String groupId) throws BusinessException {
        // 获取当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<OrganizationLdap> userOrgListId = userService.getOrgChild(groupId);
        List<String> orgChildList = new ArrayList<>();
        if (null != userOrgListId && userOrgListId.size() > 0) {
            for (OrganizationLdap o : userOrgListId) {
                orgChildList.add(Converter.toBlank(o.getId()));
            }
        }
        List<VehicleInfo> list = newVehicleDao.findVehicleByUserAndGroupForVasMileage(userId, orgChildList);
        return list;
    }

    @Override
    public List<MileageForm> queryByDate(String queryWay, String startDate, String endDate, String groupId,
        String vehicleId) {
        List<MileageForm> list = new ArrayList<>();
        List<VehicleInfo> vehicleInfoList = null;
        try {
            vehicleInfoList = getVehicleInfoList(groupId);
        } catch (BusinessException e) {
            log.error("error", e);
        }
        List<String> vidList = new ArrayList<>();
        if (null != vehicleInfoList && vehicleInfoList.size() > 0) {
            for (VehicleInfo v : vehicleInfoList) {
                vidList.add(Converter.toBlank(v.getId()));
            }
        }
        List<Positional> positionalList = null;
        final long startDate1 = Converter.convertToUnixTimeStamp(startDate);
        final long endDate1 = Converter.convertToUnixTimeStamp(endDate);
        if (Converter.toBlank(vehicleId).equals("")) {
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

    private List<MileageForm> setFormValue_date(Map<String, Map<String, List<Positional>>> map, String queryWay) {
        List<MileageForm> list = new ArrayList<>();
        if (null != map && map.size() > 0) {
            for (Map.Entry<String, Map<String, List<Positional>>> entry : map.entrySet()) {
                String time = entry.getKey();
                Map<String, List<Positional>> map1 = entry.getValue();
                if (null != map1 && map1.size() > 0) {
                    for (Map.Entry<String, List<Positional>> entry1 : map1.entrySet()) {
                        List<MileageForm> list1 = new ArrayList<>();
                        List<Positional> positionalList = entry1.getValue();
                        mergePositional(time, positionalList, list1);
                        /*calucateAirDuration(list1);*/
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
        sort.sortByMethod_m(list, "getTime", true);
        return list;
    }

    private void mergePositional(String time, List<Positional> positionalList, List<MileageForm> list) {
        boolean flag = false; // acc开关标识
        int acc = 0; // acc状态默认为0
        int air = 0; // 空调开启状态默认为0
        String startDate = ""; // 空调开启时间
        String endDate = ""; // 空调关闭时间
        double airTime = 0; // 空调开启时长

        MileageForm tesf = null;
        if (null != positionalList && positionalList.size() > 0) {
            for (int i = 0; i < positionalList.size(); i++) {
                Positional p = positionalList.get(i);
                acc = CalculateUtil.getStatus(Converter.toBlank(p.getStatus())).getInteger("acc");
                air = Converter.toInteger(p.getAirConditionStatus(), 0);
                String longtitude = String.valueOf(p.getLongtitude());
                String latitude = String.valueOf(p.getLatitude());
                String coordinate = null;
                //String date = Converter.toBlank(p.getVtimeStr());
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
                        //tesf.setEndDate(Converter.toBlank(p.getVtimeStr()));
                        tesf.setEndDate(date);
                        tesf.setEndCoordinate(Converter.toBlank(coordinate));
                        tesf.setEndOil(Converter.toBlank(p.getTotalOilwearOne()));
                        tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                        tesf.setTotalFuelConsumption(
                            Converter.toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                        if (p.getGpsMile().equals("null") || p.getGpsMile().equals("") || p.getGpsMile() == null) {
                            tesf.setEndMileage("0");
                        } else {
                            tesf.setEndMileage(Converter.toBlank(p.getGpsMile()));
                        }
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
                    tesf = new MileageForm();
                    tesf.setTime(time);
                    tesf.setVehicleId(Converter.toBlank(p.getVehicleId()));
                    tesf.setVehicleType(Converter.toBlank(p.getVehicleType()));
                    tesf.setFuelType(Converter.toBlank(p.getFuelType()));
                    //tesf.setStartDate(Converter.toBlank(p.getVtimeStr()));
                    tesf.setStartDate(date);
                    tesf.setStartCoordinate(Converter.toBlank(coordinate));
                    tesf.setBrand(Converter.toBlank(p.getPlateNumber()));
                    tesf.setStartOil(Converter.toBlank(p.getTotalOilwearOne()));
                    tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                    if (p.getGpsMile().equals("null") || p.getGpsMile().equals("") || p.getGpsMile() == null) {
                        tesf.setEndMileage("0");
                    } else {
                        tesf.setStartMileage(Converter.toBlank(p.getGpsMile()));
                    }
                }
                if (tesf != null && !flag) {
                    if (acc == 1) {
                        // 行驶过程，每次更新行驶末尾状态
                        //tesf.setEndDate(Converter.toBlank(p.getVtimeStr()));
                        tesf.setEndDate(date);
                        tesf.setEndCoordinate(Converter.toBlank(coordinate));
                        tesf.setEndOil(Converter.toBlank(p.getTotalOilwearOne()));
                        tesf.setDuration(CalculateUtil.toDateTime(tesf.getEndDate(), tesf.getStartDate()));
                        tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                        tesf.setTotalFuelConsumption(
                            Converter.toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                        if (p.getGpsMile().equals("null") || p.getGpsMile().equals("") || p.getGpsMile() == null) {
                            tesf.setEndMileage("0");
                        } else {
                            tesf.setEndMileage(Converter.toBlank(p.getGpsMile()));
                        }
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

    private void calculateForm(List<MileageForm> list, List<MileageForm> list1) {
        if (null != list1 && list1.size() > 0) {
            double totalFuel = 0;
            double duration = 0;
            double mileage = 0;
            double airDuration = 0;
            MileageForm t = list1.get(0);
            String fuelType =
                Converter.toBlank(t.getFuelType()).equals("") ? "" : Converter.toBlank(t.getFuelType()).split("#")[1];
            EquipForm ef = equipDao.findBenchmarkByVehicleId(Converter.toBlank(t.getVehicleId()));
            String baseTimeBenchmark = (null == ef ? "" :
                (Converter.toBlank(ef.getMileageBenchmark()).equals("") ? "" : ef.getMileageBenchmark()));
            VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
            for (MileageForm t1 : list1) {
                totalFuel += Converter.toDouble(t1.getTotalFuelConsumption());
                duration += Converter.toDouble(t1.getDuration());
                airDuration += Converter.toDouble(t1.getAirConditionerDuration());
                mileage += Converter.toDouble(t1.getMileage());
            }
            String averageEnergy =
                CalculateUtil.getCurAverageEnergy_by_mile(Converter.toBlank(totalFuel), Converter.toBlank(mileage));
            String energySavingFuelByMile = CalculateUtil
                .getEnergySaving_fuel_by_mile(baseTimeBenchmark, averageEnergy, Converter.toBlank(mileage));
            String energySavingCoal = CalculateUtil.getEnergySaving_coal(energySavingFuelByMile, fuelType);
            String reduceEmissionsCo2 = CalculateUtil.getReduceEmissions_CO2(energySavingFuelByMile, fuelType);
            String reduceEmissionsNox = CalculateUtil.getReduceEmissions_NOX(energySavingFuelByMile, fuelType);
            String reduceEmissionsSo2 = CalculateUtil.getReduceEmissions_SO2(energySavingFuelByMile, fuelType);
            String energySavingRate = "".equals(baseTimeBenchmark) ? "" :
                CalculateUtil.getEnergySavingRate_mile(energySavingFuelByMile, baseTimeBenchmark, mileage);
            /*String time = duration + "";*/
            String averageSpeed = CalculateUtil.averageSpeed(Converter.toBlank(mileage), duration);

            t.setVehicleType(vehicleDTO.getVehicleType());
            t.setFuelType(vehicleDTO.getFuelType());
            t.setDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(duration)));
            t.setAirConditionerDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(airDuration)));
            t.setTotalFuelConsumption(df.format(Converter.toDouble(totalFuel)));
            t.setReferenceEnergyConsumption(baseTimeBenchmark);
            t.setCurrentAverageEnergyConsumption(df.format(Converter.toDouble(averageEnergy, 0.0)));
            t.setEnergySaving_fuel(energySavingFuelByMile);
            t.setEnergySaving_standardCoal(energySavingCoal);
            t.setReduceEmissions_CO2(reduceEmissionsCo2);
            t.setReduceEmissions_HCX("");
            t.setReduceEmissions_NOX(reduceEmissionsNox);
            t.setReduceEmissions_SO2(reduceEmissionsSo2);
            t.setEnergySavingRate(energySavingRate);
            t.setAverageSpeed(averageSpeed);
            t.setMileage(df.format(Converter.toDouble(mileage)));
            list.add(t);
        }
    }

    private void calculateForm_queryByDate(List<MileageForm> list, List<MileageForm> list1) {
        if (null != list1 && list1.size() > 0) {
            double totalFuel = 0;
            double duration = 0;
            double airDuration = 0;
            double mileage = 0;
            if (null != list1 && list1.size() > 0) {
                for (MileageForm t : list1) {
                    String fuelType = Converter.toBlank(t.getFuelType()).equals("") ? "" :
                        Converter.toBlank(t.getFuelType()).split("#")[1];
                    EquipForm ef = equipDao.findBenchmarkByVehicleId(Converter.toBlank(t.getVehicleId()));
                    String baseTimeBenchmark = (null == ef ? "" : Converter.toBlank(ef.getMileageBenchmark()));
                    VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
                    totalFuel = Converter.toDouble(t.getTotalFuelConsumption());
                    duration = Converter.toDouble(t.getDuration());
                    airDuration = Converter.toDouble(t.getAirConditionerDuration());
                    mileage = Converter.toDouble(t.getMileage());

                    totalFuel = Converter.toDouble(t.getTotalFuelConsumption());
                    duration = Converter.toDouble(t.getDuration());
                    airDuration = Converter.toDouble(t.getAirConditionerDuration());
                    String averageEnergy = CalculateUtil
                        .getCurAverageEnergy_by_mile(Converter.toBlank(totalFuel), Converter.toBlank(mileage));
                    String energySavingFuelByMile = CalculateUtil
                        .getEnergySaving_fuel_by_mile(baseTimeBenchmark, averageEnergy, Converter.toBlank(mileage));
                    String energySavingCoal = CalculateUtil.getEnergySaving_coal(energySavingFuelByMile, fuelType);
                    String reduceEmissionsCo2 = CalculateUtil.getReduceEmissions_CO2(energySavingFuelByMile, fuelType);
                    String reduceEmissionsNox = CalculateUtil.getReduceEmissions_NOX(energySavingFuelByMile, fuelType);
                    String reduceEmissionsSo2 = CalculateUtil.getReduceEmissions_SO2(energySavingFuelByMile, fuelType);
                    String energySavingRate =
                        CalculateUtil.getEnergySavingRate_mile(energySavingFuelByMile, baseTimeBenchmark, mileage);
                    /*String time = duration + "";*/
                    String averageSpeed = CalculateUtil.averageSpeed(t.getMileage(), duration);

                    t.setVehicleType(vehicleDTO.getVehicleType());
                    t.setFuelType(vehicleDTO.getFuelType());
                    t.setDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(duration)));
                    t.setAirConditionerDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(airDuration)));
                    t.setTotalFuelConsumption(df.format(Converter.toDouble(totalFuel)));
                    t.setReferenceEnergyConsumption(baseTimeBenchmark);
                    t.setCurrentAverageEnergyConsumption(df.format(Converter.toDouble(averageEnergy, 0.0)));
                    t.setEnergySaving_fuel(energySavingFuelByMile);
                    t.setEnergySaving_standardCoal(energySavingCoal);
                    t.setReduceEmissions_CO2(reduceEmissionsCo2);
                    t.setReduceEmissions_HCX("");
                    t.setReduceEmissions_NOX(reduceEmissionsNox);
                    t.setReduceEmissions_SO2(reduceEmissionsSo2);
                    t.setEnergySavingRate(energySavingRate);
                    t.setAverageSpeed(averageSpeed);
                    t.setMileage(df.format(Converter.toDouble(mileage)));
                    list.add(t);
                }
            }
        }
    }

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

    public List<MileageForm> dealPositionalList(List<Positional> positionalList) {
        List<MileageForm> list = new ArrayList<MileageForm>();
        boolean flag = false;// 判断行驶状态开始标识
        MileageForm energy = null;
        int acc = 0;
        Positional temp = null;
        for (int i = 0, len = positionalList.size(); i < len; i++) {
            temp = positionalList.get(i);
            acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
            String date = String.valueOf(temp.getVtimeStr());
            String longtitude = String.valueOf(temp.getLongtitude());
            String latitude = String.valueOf(temp.getLatitude());
            String coordinate = String.valueOf(longtitude + "," + latitude);
            if (flag) {
                // 表示前一次数据开始记录行驶，用于判断行驶状态是否满足2次，如不满足则不记录
                if (acc == 0) {
                    energy = null;
                }
                flag = false;
            }
            if (acc == 1 && energy == null) {
                flag = true;
                energy = new MileageForm();
                energy.setStartDate(Converter.toBlank(date));
                energy.setStartCoordinate(Converter.toBlank(coordinate));
                energy.setStartOil(Converter.toBlank(temp.getTotalOilwearOne()));
                energy.setFuelType(Converter.toBlank(temp.getFuelType()));
                energy.setVehicleId(Converter.toBlank(temp.getVehicleId()));
                energy.setVehicleType(Converter.toBlank(temp.getVehicleType()));
            }
            if (energy != null && !flag) {
                if (acc == 1) {
                    // 行驶过程，每次更新行驶末尾状态
                    energy.setEndDate(Converter.toBlank(date));
                    energy.setEndCoordinate(Converter.toBlank(coordinate));
                    energy.setEndOil(Converter.toBlank(temp.getTotalOilwearOne()));
                    energy.setDuration(CalculateUtil.toDateTime(energy.getEndDate(), energy.getStartDate()));
                    energy.setBrand(Converter.toBlank(temp.getPlateNumber()));
                    //如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                    if (i == positionalList.size() - 1) {
                        energy.setBrand(Converter.toBlank(temp.getPlateNumber()));
                        list.add(energy);
                    }
                } else {
                    // 行驶结束，写入list
                    // 如果只有开始时间，则舍弃这条数据
                    if (energy != null && energy.getEndDate() != null) {
                        energy.setBrand(Converter.toBlank(temp.getPlateNumber()));
                        list.add(energy);
                    }
                    energy = null;
                }
            }
        }
        return list;
    }

    private List<MileageForm> setFormValue_month_quarter_year(String queryWay, Map<String, List<Positional>> map) {
        List<MileageForm> list = new ArrayList<>();
        if (null != map && map.size() > 0) {
            for (Map.Entry<String, List<Positional>> entry : map.entrySet()) {
                List<MileageForm> list1 = new ArrayList<>();
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
                /*calucateAirDuration(list1);*/
                calculateForm(list, list1);
            }
        }
        return list;
    }

    @Override
    public boolean exportMileage(String title, int type, HttpServletResponse response, MileageQuery query) {
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
        List<MileageForm> list = queryByDate(queryWay, startTime, endTime, groupId, vehicleId);
        if (queryWay.equals("list1")) {
            export = new ExportExcel(title, MileageList2.class, 1, time, groups, null);
            List<MileageList2> exportList2 = new ArrayList<MileageList2>();
            for (MileageForm info : list) {
                MileageList2 form = new MileageList2();
                BeanUtils.copyProperties(info, form);
                exportList2.add(form);
            }
            export.setDataList(exportList2);
        } else {
            export = new ExportExcel(title, MileageList1.class, 1, time, groups, null);
            List<MileageList1> exportList1 = new ArrayList<MileageList1>();
            for (MileageForm info : list) {
                MileageList1 form = new MileageList1();
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
        OutputStream out;
        try {
            out = response.getOutputStream();
            export.write(out);// 将文档对象写入文件输出流
            out.close();
        } catch (IOException e) {
            log.error("error", e);
            return false;
        }
        return true;
    }
}
