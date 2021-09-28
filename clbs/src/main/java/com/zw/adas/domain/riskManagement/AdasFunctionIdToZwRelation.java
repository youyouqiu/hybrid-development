package com.zw.adas.domain.riskManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 各个协议转中位标准的事件关系表
 *
 * create in 2021/3/25 13:41
 * @author zjc
 */
public class AdasFunctionIdToZwRelation {

    /**
     * 桂标以(14)及川标(12)和中位(21)标准时间function_id关联映射表
     * key 川标事件或者桂标事件（function_id）
     * value为中位标准事件
     */
    private static final Map<Integer, Integer> functionIdToZwMap = new HashMap<>();

    private static void initFunctionIdToZwMap() {
        /*
         * 前向碰撞报警
         */
        functionIdToZwMap.put(126401, 216401);
        functionIdToZwMap.put(146401, 216401);
        /*
         * 车道偏离报警
         */
        functionIdToZwMap.put(126402, 216402);
        functionIdToZwMap.put(146402, 216402);
        /*
         * 车距过近
         */
        functionIdToZwMap.put(126403, 216403);
        functionIdToZwMap.put(146403, 216403);
        /*
         * 行人碰撞
         */
        functionIdToZwMap.put(126404, 216404);
        functionIdToZwMap.put(146404, 216404);
        /*
         * 频繁变道
         */
        functionIdToZwMap.put(126405, 216405);
        functionIdToZwMap.put(146405, 216405);
        /*
         * 道路标识超限
         */
        functionIdToZwMap.put(126409, 216409);
        functionIdToZwMap.put(146409, 216409);
        /*
         * 急加速
         */
        functionIdToZwMap.put(1264081, 2164081);
        functionIdToZwMap.put(1464081, 2164081);
        /*
         * 急减速
         */
        functionIdToZwMap.put(1264083, 2164083);
        functionIdToZwMap.put(1464083, 2164083);
        /*
         * 外设状态异常(驾驶辅助功能失效)
         */
        functionIdToZwMap.put(126519, 216406);
        functionIdToZwMap.put(146410, 216406);
        /*
         * 长时间不目视前方
         */
        functionIdToZwMap.put(126508, 216508);
        functionIdToZwMap.put(146508, 216508);
        /*
         * 抽烟
         */
        functionIdToZwMap.put(126503, 216503);
        functionIdToZwMap.put(146503, 216503);
        /*
         * 接打手持电话
         */
        functionIdToZwMap.put(126502, 216502);
        functionIdToZwMap.put(146502, 216502);
        /*
         * 驾驶员不在驾驶位置-未检测到驾驶员
         */
        functionIdToZwMap.put(126510, 216503);
        functionIdToZwMap.put(146510, 216503);
        /*
         * 双手同时脱离方向盘
         */
        functionIdToZwMap.put(126517, 216518);
        functionIdToZwMap.put(146517, 216518);

        /*
         * 外设状态异常报警（DMS）-驾驶员行为监测功能失效
         */
        functionIdToZwMap.put(126514, 216517);
        functionIdToZwMap.put(146514, 216517);

        /*
         * 胎压过高
         */
        functionIdToZwMap.put(126601, 216601);
        functionIdToZwMap.put(146601, 216601);

        /*
         * 胎压过低
         */
        functionIdToZwMap.put(126602, 216602);
        functionIdToZwMap.put(146602, 216602);

        /*
         * 轮胎温度过高
         */
        functionIdToZwMap.put(126603, 216603);
        functionIdToZwMap.put(146603, 216603);

        /*
         * 传感器异常
         */
        functionIdToZwMap.put(126604, 216604);
        functionIdToZwMap.put(146604, 216604);

        /*
         * 胎压不平衡
         */
        functionIdToZwMap.put(146605, 216605);
        functionIdToZwMap.put(126605, 216605);

        /*
         * 慢漏气
         */
        functionIdToZwMap.put(146606, 216606);
        functionIdToZwMap.put(126606, 216606);

        /*
         * 电池电压低
         */
        functionIdToZwMap.put(146607, 216607);
        functionIdToZwMap.put(126607, 216607);
        /*
         * 后方接近
         */
        functionIdToZwMap.put(146701, 216701);
        functionIdToZwMap.put(126701, 216701);
        /*
         * 左侧后方接近
         */
        functionIdToZwMap.put(146702, 216702);
        functionIdToZwMap.put(126702, 216702);
        /*
         * 右侧后方接近
         */
        functionIdToZwMap.put(146703, 216703);
        functionIdToZwMap.put(126703, 216703);

    }

    static {
        initFunctionIdToZwMap();
    }

    /**
     * 川 桂标协议到中位标准转换
     */
    public static Integer convertZwFunctionId(Integer functionId) {
        if (functionId == null) {
            return 0;
        }
        return Optional.ofNullable(functionIdToZwMap.get(functionId)).orElse(0);
    }
}
