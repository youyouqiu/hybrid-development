package com.zw.adas.utils;

import com.google.common.collect.ImmutableMap;
import com.zw.adas.domain.enums.AdasEventRankEn;
import com.zw.adas.repository.mysql.common.AdasCommonDao;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmMapping;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/***
 * 主动安全相关的帮助实体bean
 * @author zhengjc
 * @version 1.0
 **/
@Component
public class AdasCommonHelper {

    public Map<Integer, Map<Integer, Integer>> alarmTypeMapping = new HashMap<>();

    @Autowired
    private AdasCommonDao adasCommonDao;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;
    /**
     * 事件code的map
     */
    private final Map<String, String> eventCodeMap = new HashMap<>();

    /**
     * 事件对应的风险类型map
     */
    private final Map<String, String> eventCodeRiskTypeMap = new HashMap<>();

    /**
     * 等级code的map
     */
    private final Map<String, String> riskLevelCodeMap = new HashMap<>();

    /**
     * 等级code的map
     */
    private final Map<String, String> alarmLevelCodeMap = new HashMap<>();
    /**
     * 预警和报警等级map
     */
    private final Map<String, String> noArmLevelCodeMap = ImmutableMap.of("1", "预警", "2", "报警");

    private final Map<String, String> alarmLevelCodeMapOfBeijing = new HashMap<>();

    /**
     * 合并后的事件和风险类型之间的code的map
     */
    private final Map<String, String> mergeEventCodeMap = new HashMap<>();

    /**
     * 事件code和808报警关系map
     */
    private final Map<String, String> event808Map = new HashMap<>();

    /**
     * 司机评分评语的map
     */
    private final Map<String, String> driverScoreCommentsMap = new HashMap<>();

    /**
     * 清新度、警惕性、专注度、自觉性、平稳性分析评语 驾驶点评的map
     */
    private final Map<String, String> travelCommentsMap = new HashMap<>();
    /**
     * 事件的统称和统称字段关系mmap
     */
    private final Map<String, String> eventCommonNameAndFieldMap = new HashMap<>();
    /**
     * 通用字段下的各个functionId关系
     */
    private final Map<String, List<Integer>> eventCommonFieldEventMap = new HashMap<>();

    private final Map<String, String> eventFieldAndCommonNameMap = new HashMap<>();

    public AdasCommonHelper() {
    }

    @PostConstruct
    public void init() {
        initCodeMap();
        initCommentsMap();
        initTravelComments();
        initAlarmTypeMapping();
        initEventNameAndCommonField();
        initCommonFieldAndEvents();
    }

    private void initCommonFieldAndEvents() {
        List<Map<String, String>> commonNmeFields = adasCommonDao.queryCommonFieldEvents();
        for (Map<String, String> commonFieldName : commonNmeFields) {
            eventCommonFieldEventMap.put(commonFieldName.get("field"), getEvents(commonFieldName));
        }
    }

    private List<Integer> getEvents(Map<String, String> commonFieldName) {
        String[] eventArr = commonFieldName.get("event").split(",");
        List<Integer> events = new ArrayList<>(eventArr.length);
        for (String event : eventArr) {
            events.add(Integer.parseInt(event));
        }
        return events;
    }

    private void initCommentsMap() {

        driverScoreCommentsMap.put("0", "驾驶情况极差，请加强自身安全意识！");
        driverScoreCommentsMap.put("2", "驾驶情况差，请加强自身安全意识！");
        driverScoreCommentsMap.put("4", "驾驶情况较差，请加强自身安全意识！");
        driverScoreCommentsMap.put("6", "驾驶情况一般，要继续争优哦！");
        driverScoreCommentsMap.put("8", "驾驶情况还不错，要继续保持争优哦！");

        driverScoreCommentsMap.put("0_1", "驾驶员驾驶行为得分上升%s%%，请继续保持！");
        driverScoreCommentsMap.put("0_-1", "驾驶员驾驶行为得分下降%s%%，努力加油！");
        driverScoreCommentsMap.put("0_0", "驾驶员综合得分与上月持平，请继续保持！");

        driverScoreCommentsMap.put("2_1", "驾驶员驾驶行为得分上升%s%%，请继续保持！");
        driverScoreCommentsMap.put("2_-1", "驾驶员驾驶行为得分下降%s%%，请加强自身安全意识");
        driverScoreCommentsMap.put("2_0", "驾驶员综合得分与上月持平，请继续保持！");

        driverScoreCommentsMap.put("4_1", "驾驶员驾驶行为得分上升%s%%，请继续保持！");
        driverScoreCommentsMap.put("4_-1", "驾驶员驾驶行为得分下降%s%%，请加强自身安全意识!");
        driverScoreCommentsMap.put("4_0", "驾驶员综合得分与上月持平，请继续保持！");

        driverScoreCommentsMap.put("6_1", "驾驶员驾驶行为得分上升%s%%，请继续保持！");
        driverScoreCommentsMap.put("6_-1", "驾驶员驾驶行为得分下降%s%%，请加强自身安全意识！");
        driverScoreCommentsMap.put("6_0", "驾驶员综合得分与上月持平，请继续保持！");

        driverScoreCommentsMap.put("8_1", "驾驶员驾驶行为得分上升%s%%，请继续保持！");
        driverScoreCommentsMap.put("8_-1", "驾驶员驾驶行为得分下降%s%%，请加强自身安全意识！");
        driverScoreCommentsMap.put("8_0", "驾驶员综合得分与上月持平，请继续保持！");

    }

    private void initTravelComments() {
        travelCommentsMap.put("0_-1", "驾驶清醒度分值不佳，要注意适当休息哦！疲劳驾驶是开车事故中最大安全隐患");
        travelCommentsMap.put("0_1", "驾驶清醒度得分超高，精神状态很好嘛，这种习惯值得发扬哦！");

        travelCommentsMap.put("1_-1", "驾驶警惕性分值不佳，多注意路面情况，提前做出正确判断！");
        travelCommentsMap.put("1_1", "驾驶专注度得分超高，开车很认真嘛，这种习惯值得发扬哦！");

        travelCommentsMap.put("2_-1", "驾驶专注度分值不佳，行驶中接打电话、抽烟、注意力分散都是开车事故安全隐患，要尽量避免哦！ ");
        travelCommentsMap.put("2_1", "驾驶自觉性得分超高，遵纪守法，这种习惯值得发扬哦！");

        travelCommentsMap.put("3_-1", "驾驶自觉性分值不佳，自觉遵守规章制度，才能更有效工作!");
        travelCommentsMap.put("3_1", "驾驶自觉性得分超高，遵纪守法，这种习惯值得发扬哦！");

        travelCommentsMap.put("4_-1", "驾驶平稳性分值不佳，频繁变道、车道偏离、急转弯、急加速、急减速、超速都是开车事故隐患，要尽量避免哦！");
        travelCommentsMap.put("4_1", "驾驶平稳性得分超高，这种习惯值得发扬哦！");
    }

    private void initEventNameAndCommonField() {
        List<Map<String, String>> commonNmeFields = adasCommonDao.queryEventCommonFieldAndName();
        for (Map<String, String> commonFieldName : commonNmeFields) {
            eventCommonNameAndFieldMap.put(commonFieldName.get("name"), commonFieldName.get("field"));
            eventFieldAndCommonNameMap.put(commonFieldName.get("field"), commonFieldName.get("name"));
        }

    }

    public Map<String, String> getEventCommonNameAndFieldMap() {
        return eventCommonNameAndFieldMap;
    }

    public Map<String, String> getEventFieldAndCommonNameMap() {
        return eventFieldAndCommonNameMap;
    }

    /**
     * 通过通用字段可以查询该字段对应的所有该事件类型的function_id
     */
    public List<Integer> getAllEventByCommonField(String commonField) {
        return eventCommonFieldEventMap.get(commonField);
    }

    public String getComments(String commentsKey) {
        return driverScoreCommentsMap.get(commentsKey);

    }

    public String getTravelComments(String travelCommentsKey) {
        return travelCommentsMap.get(travelCommentsKey);

    }

    public String geEventName(String eventCode) {
        return eventCodeMap.getOrDefault(eventCode, eventCode);
    }

    public String getMergeRiskType(String eventName) {
        return mergeEventCodeMap.getOrDefault(eventName, eventName);
    }

    public String getLevelName(String eventCode) {
        return alarmLevelCodeMap.getOrDefault(eventCode, eventCode);
    }

    public String getNoLevelName(String eventCode) {
        return noArmLevelCodeMap.getOrDefault(eventCode, eventCode);
    }

    public String getLevelNameOfBeijing(String eventCode) {
        return alarmLevelCodeMapOfBeijing.getOrDefault(eventCode, eventCode);
    }

    public String geRiskTypeName(String eventCode) {
        return eventCodeRiskTypeMap.getOrDefault(eventCode, eventCode);
    }

    public Integer getT808AlarmType(String eventCode) {
        String data = event808Map.getOrDefault(eventCode, eventCode);
        return Integer.parseInt(data);
    }

    public Integer getT808AlarmType(Integer eventCode) {
        return getT808AlarmType(eventCode + "");
    }

    /**
     * 获取风险等级
     */
    public String geRiskLevel(String code) {
        return riskLevelCodeMap.getOrDefault(code, code);
    }

    private void initCodeMap() {
        List<Map<String, String>> riskEventMaps = adasCommonDao.getEventMap();
        for (Map<String, String> riskEventMap : riskEventMaps) {
            eventCodeMap.put(riskEventMap.get("functionId"), riskEventMap.get("riskEvent"));
            eventCodeRiskTypeMap.put(riskEventMap.get("functionId"), riskEventMap.get("riskType"));
        }

        List<Map<String, String>> riskLevelMaps = adasCommonDao.getRiskLevelMap();
        for (Map<String, String> riskLevelMap : riskLevelMaps) {
            riskLevelCodeMap.put(riskLevelMap.get("riskValue"), riskLevelMap.get("riskLevel"));
        }

        alarmLevelCodeMap.put("1", "一级报警");
        alarmLevelCodeMap.put("2", "二级报警");
        alarmLevelCodeMapOfBeijing.put("1", "低风险报警");
        alarmLevelCodeMapOfBeijing.put("2", "一般风险报警");
        alarmLevelCodeMapOfBeijing.put("3", "高风险报警");

        for (AdasEventRankEn adasEventRankEn : AdasEventRankEn.values()) {
            mergeEventCodeMap.put(adasEventRankEn.getEventName(), adasEventRankEn.getRiskType());
        }

        try {
            List<Map<String, String>> event808Maps = adasCommonDao.getEvent808Map();
            for (Map<String, String> event808 : event808Maps) {
                event808Map.put(event808.get("eventId"), event808.get("808Id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isStreamMedia(Integer deviceType) {
        return deviceType != 1;
    }

    public boolean isStreamMedia(String deviceType) {
        return Integer.parseInt(deviceType) != 1;
    }

    private void initAlarmTypeMapping() {
        try {
            List<T809AlarmMapping> t809AlarmMappings = connectionParamsSetDao.get808PosAnd809PosByProtocolType(null);
            if (CollectionUtils.isEmpty(t809AlarmMappings)) {
                return;
            }
            Pattern pos809 = Pattern.compile("^0[x|X]");
            for (T809AlarmMapping t809AlarmMapping : t809AlarmMappings) {
                Integer proType = t809AlarmMapping.getProtocolType();
                Map<Integer, Integer> t89Map = alarmTypeMapping.getOrDefault(proType, new HashMap<>());
                int t809pos = Integer.parseInt(pos809.matcher(t809AlarmMapping.getPos809()).replaceAll(""), 16);
                int t808pos = Integer.parseInt(t809AlarmMapping.getPos808());
                //针对超速报警(路网)非主动安全报警，避免冲突，暂时写死后续需求确认
                if (t809pos == 1 && t808pos == 164) {
                    continue;
                }
                t89Map.put(t809pos, t808pos);
                alarmTypeMapping.put(proType, t89Map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
