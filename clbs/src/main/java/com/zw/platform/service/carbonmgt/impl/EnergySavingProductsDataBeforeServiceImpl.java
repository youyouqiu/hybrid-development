package com.zw.platform.service.carbonmgt.impl;

import com.alibaba.fastjson.JSON;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.carbonmgt.EnergySavingProductsDataBeforeExport1;
import com.zw.platform.domain.vas.carbonmgt.EnergySavingProductsDataBeforeExport2;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.domain.vas.carbonmgt.form.MileageForm;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;
import com.zw.platform.repository.vas.BasicManagementDao;
import com.zw.platform.service.carbonmgt.EnergySavingProductsDataBeforeService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.CollectionsSortUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.RegexUtils;
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
public class EnergySavingProductsDataBeforeServiceImpl implements EnergySavingProductsDataBeforeService {
    private static Logger log = LogManager.getLogger(EnergySavingProductsDataBeforeServiceImpl.class);
    private static DecimalFormat df = new DecimalFormat("0.0000");
    private static DecimalFormat df_1 = new DecimalFormat("0.0"); // 保留一位小数
    private static DecimalFormat df_2 = new DecimalFormat("0.00"); // 保留两位小数
    @Autowired
    private UserService userService;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private BasicManagementDao basicManagementDao;

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
        List<VehicleInfo> vehicleInfoList = null;
        try {
            vehicleInfoList = getVehicleInfoList(groupId);
        } catch (BusinessException e) {
            log.error("error", e);
        }
        List<Positional> positionalList = null;
        // step1:查询出时间段内所有数据
        final long startDate1 = Converter.convertToUnixTimeStamp(startDate);
        final long endDate1 = Converter.convertToUnixTimeStamp(endDate);
        if (Converter.toBlank(vehicleId).equals("")) {
            List<String> vidList = new ArrayList<>();
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

    private void mergePositional(String time, List<Positional> positionalList,
        List<MobileSourceEnergyReportForm> list) {
        boolean flag = false; // acc开关标识
        int acc = 0; // acc状态默认为0
        int air = 0; // 空调开启状态默认为0
        String startDateAir = ""; // 空调开启时间
        String endDateAir = ""; // 空调关闭时间
        double airTime = 0; // 空调开启时长

        MobileSourceEnergyReportForm tesf = null;
        if (null != positionalList && positionalList.size() > 0) {
            for (int i = 0; i < positionalList.size(); i++) {
                Positional p = positionalList.get(i);
                acc = CalculateUtil.getStatus(Converter.toBlank(p.getStatus())).getInteger("acc");
                air = Converter.toInteger(p.getAirConditionStatus(), 0);
                String date =
                    Converter.toBlank(p.getVtimeStr()).equals("") ? Converter.convertUnixToDatetime(p.getVtime()) :
                        Converter.toBlank(p.getVtimeStr());

                //-------------空调时长计算-start--------------
                if (acc == 1 && air != 0 && startDateAir.equals("")) {
                    startDateAir = date;
                }
                if (acc == 1 && air == 0) { // 空调关
                    if (endDateAir.equals("") && !startDateAir.equals("")) {
                        endDateAir = date;
                        double duration = Converter.toDouble(CalculateUtil.toDateTime(endDateAir, startDateAir));
                        airTime += duration;
                        startDateAir = "";
                        endDateAir = "";
                    }
                } else if (endDateAir.equals("") && !startDateAir.equals("")) {
                    if (i == positionalList.size() - 1) {
                        endDateAir =
                            Converter.toBlank(positionalList.get(positionalList.size() - 1).getVtimeStr()).equals("")
                                ?
                                Converter
                                    .convertUnixToDatetime(positionalList.get(positionalList.size() - 1).getVtime()) :
                                Converter.toBlank(positionalList.get(positionalList.size() - 1).getVtimeStr());
                        double duration = Converter.toDouble(CalculateUtil.toDateTime(endDateAir, startDateAir));
                        airTime += duration;
                        startDateAir = "";
                        endDateAir = "";
                    }
                }
                // --------------空调时长计算-end-------------------

                if (flag) { // 表示acc为1了，已经打火用于判断打火熄火这一过程是否满足2次，如不满足则不记录
                    if (acc == 0) {
                        tesf.setEndDate(date);
                        tesf.setEndOil(Converter.toBlank(p.getTotalOilwearOne()));
                        tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                        tesf.setTotalFuelConsumption(
                            Converter.toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                        tesf.setEndMileage(Converter.toBlank(p.getGpsMile(), "0"));
                        Double start = Double.parseDouble(tesf.getStartMileage());
                        Double end = Double.parseDouble(tesf.getEndMileage());
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
                    tesf.setBrand(Converter.toBlank(p.getPlateNumber()));
                    tesf.setVehicleType(Converter.toBlank(p.getVehicleType()));
                    tesf.setFuelType(Converter.toBlank(p.getFuelType()));
                    tesf.setStartDate(date);
                    tesf.setStartMileage(Converter.toBlank(p.getGpsMile(), "0"));
                    tesf.setStartOil(Converter.toBlank(p.getTotalOilwearOne()));
                    tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                }
                if (tesf != null && !flag) {
                    if (acc == 1) {
                        // 行驶过程，每次更新行驶末尾状态
                        tesf.setEndDate(date);
                        tesf.setEndOil(Converter.toBlank(p.getTotalOilwearOne()));
                        tesf.setDuration(CalculateUtil.toDateTime(tesf.getEndDate(), tesf.getStartDate()));
                        tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                        tesf.setTotalFuelConsumption(
                            Converter.toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                        tesf.setEndMileage(Converter.toBlank(p.getGpsMile(), "0"));
                        Double start = Double.parseDouble(tesf.getStartMileage());
                        Double end = Double.parseDouble(tesf.getEndMileage());
                        String mileage = String.valueOf((end - start) / 10);
                        tesf.setMileage(Converter.toBlank(mileage));

                        //如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                        String tempS = Converter.toBlank(tesf.getStartDate()).substring(0, 10);
                        String tempE = Converter.toBlank(tesf.getEndDate()).substring(0, 10);
                        if (!tempS.equals(tempE)) { // 不是同一天的数据
                            // 找到前一天的最后一条记录
                            long begin1 = Converter.convertToUnixTimeStamp(tempS + " 00:00:00");
                            long end1 = Converter.convertToUnixTimeStamp(tempS + " 23:59:59");
                            final String vehicleId = String.valueOf(UuidUtils.getUUIDFromBytes(p.getVehicleId()));
                            List<Positional> tempList = getLastPositional(begin1, end1, vehicleId);
                            if (null != tempList && tempList.size() > 0) {
                                Positional pi = tempList.get(0);
                                tesf.setEndDate(Converter.convertUnixToDatetime(pi.getVtime()));
                                tesf.setEndOil(Converter.toBlank(pi.getTotalOilwearOne()));
                                tesf.setDuration(CalculateUtil.toDateTime(tesf.getEndDate(), tesf.getStartDate()));
                                tesf.setAirConditionerDuration(Converter.toBlank(airTime));
                                tesf.setTotalFuelConsumption(Converter
                                    .toBlank(CalculateUtil.getTotalFuel(tesf.getStartOil(), tesf.getEndOil())));
                                list.add(tesf);
                            }
                            tesf = null;
                        }
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

    private List<Positional> getLastPositional(long startTime, long endTime, String vehicleId) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_LAST_POSITIONAL, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    private void calculateForm(List<MobileSourceEnergyReportForm> list, List<MobileSourceEnergyReportForm> list1) {
        if (null != list1 && list1.size() > 0) {
            double totalFuel = 0;
            double duration = 0;
            double mileage = 0;
            double airDuration = 0;
            MobileSourceEnergyReportForm t = list1.get(0);
            VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
            String fuelType = getFuelTypeName(vehicleDTO.getFuelType());
            String carCity = RegexUtils.getAreaByBrand(Converter.toBlank(vehicleDTO.getName())); // 根据车牌号首字获取其所在的省份
            List<TimingStored> priceList =
                basicManagementDao.oilPricesQuery(t.getTime(), t.getTime(), carCity, fuelType);
            String oilPrice = (null != priceList && priceList.size() > 0) ? priceList.get(0).getOilPrice() : "0";
            for (MobileSourceEnergyReportForm t1 : list1) {
                totalFuel += Converter.toDouble(t1.getTotalFuelConsumption());
                duration += Converter.toDouble(t1.getDuration());
                airDuration += Converter.toDouble(t1.getAirConditionerDuration());
                mileage += Converter.toDouble(t1.getMileage());
            }
            String averageSpeed = CalculateUtil.averageSpeed(Converter.toBlank(mileage), duration);
            double totalFuelFee = Converter.toDouble(totalFuel, 0.0) * Converter.toDouble(oilPrice, 0.0);
            String energy100 = Converter.toDouble(mileage) > 0
                ?
                df.format(Converter.toDouble(totalFuel) / (Converter.toDouble(mileage) * 100)) : "0";
            String reduceEmissionsCo2 = CalculateUtil.getReduceEmissions_CO2(Converter.toBlank(totalFuel), fuelType);

            t.setVehicleType(vehicleDTO.getVehicleType());
            t.setFuelType(vehicleDTO.getFuelType());
            t.setDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(duration)));
            t.setAirConditionerDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(airDuration)));
            t.setTotalFuelConsumption(df_2.format(totalFuel));
            t.setAverageSpeed(df_2.format(Converter.toDouble(averageSpeed)));
            t.setMileage(df_1.format(Converter.toDouble(mileage)));
            t.setEnergyPrice(df_2.format(Converter.toDouble(oilPrice)));
            t.setEnergyTotalFee(df_2.format(totalFuelFee));
            t.setEnergy_100(df_2.format(Converter.toDouble(energy100)));
            t.setEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo2)));
            t.setEmissions_SO2("");
            t.setEmissions_NOX("");
            t.setEmissions_HCX("");
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
                    VehicleDTO vehicleDTO = newVehicleDao.getDetailById(t.getVehicleId());
                    String fuelType = getFuelTypeName(vehicleDTO.getFuelType());
                    String carCity =
                        RegexUtils.getAreaByBrand(Converter.toBlank(vehicleDTO.getName())); // 根据车牌号首字获取其所在的省份
                    List<TimingStored> priceList =
                        basicManagementDao.oilPricesQuery(t.getTime(), t.getTime(), carCity, fuelType);
                    String oilPrice =
                        (null != priceList && priceList.size() > 0) ? priceList.get(0).getOilPrice() : "0";
                    totalFuel = Converter.toDouble(t.getTotalFuelConsumption());
                    double totalFuelFee =
                        Converter.toDouble(t.getTotalFuelConsumption(), 0.0) * Converter.toDouble(oilPrice, 0.0);
                    duration = Converter.toDouble(t.getDuration());
                    airDuration = Converter.toDouble(t.getAirConditionerDuration());
                    mileage = Converter.toDouble(t.getMileage());
                    String averageSpeed = CalculateUtil.averageSpeed(t.getMileage(), duration);
                    String energy100 = Converter.toDouble(mileage) > 0
                        ?
                        df.format(Converter.toDouble(totalFuel) / (Converter.toDouble(mileage) * 100)) : "0";
                    String reduceEmissionsCo2 =
                        CalculateUtil.getReduceEmissions_CO2(Converter.toBlank(totalFuel), fuelType);

                    t.setVehicleType(vehicleDTO.getVehicleType());
                    t.setFuelType(vehicleDTO.getFuelType());
                    t.setDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(duration)));
                    t.setAirConditionerDuration(CalculateUtil.converHourToYYYYMMDD(Converter.toBlank(airDuration)));
                    t.setTotalFuelConsumption(df_2.format(totalFuel));
                    t.setAverageSpeed(df_2.format(Converter.toDouble(averageSpeed)));
                    t.setMileage(df_1.format(Converter.toDouble(mileage)));
                    t.setEnergyPrice(df_2.format(Converter.toDouble(oilPrice, 0.0)));
                    t.setEnergyTotalFee(df_2.format(totalFuelFee));
                    t.setEnergy_100(df_2.format(Converter.toDouble(energy100)));
                    t.setEmissions_CO2(df_2.format(Converter.toDouble(reduceEmissionsCo2)));
                    t.setEmissions_SO2("");
                    t.setEmissions_NOX("");
                    t.setEmissions_HCX("");
                    list.add(t);
                }
            }
        }
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
            // 表示前一次数据开始记录行驶，用于判断行驶状态是否满足2次，如不满足则不记录
            if (flag) {
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
    public boolean export(String title, int type, HttpServletResponse response, MileageQuery query) throws Exception {
        String startTime = query.getStartDate();
        String endTime = query.getEndDate();
        String queryWay = query.getQueryWay();
        String groupId = query.getGroupId();
        String vehicleId = query.getBrand();
        OrganizationLdap org = userService.findOrganization(groupId);
        String groupName = org.getName();
        String time = null;

        if (queryWay.equals("list1") || queryWay.equals("list2")) {
            time = startTime + "--" + endTime;
        }
        String groups = "企业名称:" + groupName;
        ExportExcel export = null;
        
        List<MobileSourceEnergyReportForm> list = queryByDate(queryWay, startTime, endTime, groupId, vehicleId);
        if (queryWay.equals("list1")) {
            export = new ExportExcel(title, EnergySavingProductsDataBeforeExport1.class, 1, time, groups, null);
            List<EnergySavingProductsDataBeforeExport1> exportList2 =
                new ArrayList<EnergySavingProductsDataBeforeExport1>();
            for (MobileSourceEnergyReportForm info : list) {
                EnergySavingProductsDataBeforeExport1 form = new EnergySavingProductsDataBeforeExport1();
                info.setGroupName(groupName);
                BeanUtils.copyProperties(info, form);
                exportList2.add(form);
            }
            export.setDataList(exportList2);
        } else {
            export = new ExportExcel(title, EnergySavingProductsDataBeforeExport2.class, 1, time, groups, null);
            List<EnergySavingProductsDataBeforeExport2> exportList1 =
                new ArrayList<EnergySavingProductsDataBeforeExport2>();
            for (MobileSourceEnergyReportForm info : list) {
                EnergySavingProductsDataBeforeExport2 form = new EnergySavingProductsDataBeforeExport2();
                info.setGroupName(groupName);
                BeanUtils.copyProperties(info, form);
                exportList1.add(form);
            }
            export.setDataList(exportList1);
        }
        OutputStream out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }
}
