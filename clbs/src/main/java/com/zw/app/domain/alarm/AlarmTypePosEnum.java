package com.zw.app.domain.alarm;

import com.google.common.collect.Lists;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */

public enum AlarmTypePosEnum {
    /**
     * 视频信号丢失
     */
    POS_125("125",
        new Integer[] { 12511, 12512, 12513, 12514, 12515, 12516, 12517, 12518, 12519, 12520, 12521, 12522, 12523,
            12524, 12525, 12526, 12527, 12528, 12529, 12530, 12531, 12532, 12533, 12534, 12535, 12536, 12537, 12538,
            12539, 12540, 12541, 12542 }),
    /**
     * 视频信号遮挡
     */
    POS_126("126",
        new Integer[] { 12611, 12612, 12613, 12614, 12615, 12616, 12617, 12618, 12619, 12620, 12621, 12622, 12623,
            12624, 12625, 12626, 12627, 12628, 12629, 12630, 12631, 12632, 12633, 12634, 12635, 12636, 12637, 12638,
            12639, 12640, 12641, 12642 }),
    /**
     * 主存储器故障
     */
    POS_1271("1271",
        new Integer[] { 12711, 12712, 12713, 12714, 12715, 12716, 12717, 12718, 12719, 12720, 12721, 12722 }),
    /**
     * 灾备存储器故障
     */
    POS_1272("1272", new Integer[] { 12723, 12724, 12725, 12726 }),
    /**
     * 异常驾驶行为
     */
    POS_130("130", new Integer[] { 13011, 13012, 13013 }),
    /**
     * 高温报警
     */
    POS_651("651", new Integer[] { 6511, 6521, 6531, 6541, 6551 }),
    /**
     * 低温报警
     */
    POS_652("652", new Integer[] { 6512, 6522, 6532, 6542, 6552 }),
    /**
     * 异常报警
     */
    POS_653("653", new Integer[] { 6513, 6523, 6533, 6543, 6553 }),
    /**
     * 高湿度报警
     */
    POS_661("661", new Integer[] { 6611, 6621, 6631, 6641, 6651 }),
    /**
     * 低湿度报警
     */
    POS_662("662", new Integer[] { 6612, 6622, 6632, 6642, 6652 }),
    /**
     * 低湿度报警
     */
    POS_663("663", new Integer[] { 6613, 6623, 6633, 6643, 6653 }),
    /**
     * 加油报警
     */
    POS_681("681", new Integer[] { 6811, 6821, 6831, 6841 }),
    /**
     * 漏油报警
     */
    POS_682("682", new Integer[] { 6812, 6822, 6832, 6842 }),
    /**
     * 油箱异常报警
     */
    POS_683("683", new Integer[] { 6813, 6823, 6833, 6843 }),
    /**
     * 工时传感器异常报警
     */
    POS_1321("1321", new Integer[] { 13213, 13214 }),
    /**
     * 载重传感器异常报警
     */
    POS_701("701", new Integer[] { 7011, 7021 }),
    /**
     * 载重报警
     */
    POS_702("702", new Integer[] { 7012, 7022 }),
    /**
     * 胎压过低报警
     */
    POS_14300("14300",
        new Integer[] { 14300, 14310, 14320, 14330, 14340, 14350, 14360, 14370, 14380, 14390, 143100, 143110, 143120,
            143130, 143140, 143150, 143160, 143170, 143180, 143190 }),
    /**
     * 胎压过高报警
     */
    POS_14301("14301",
        new Integer[] { 14301, 14311, 14321, 14331, 14341, 14351, 14361, 14371, 14381, 14391, 143101, 143111, 143121,
            143131, 143141, 143151, 143161, 143171, 143181, 143191 }),
    /**
     * 胎温过高报警
     */
    POS_14302("14302",
        new Integer[] { 14302, 14312, 14322, 14332, 14342, 14352, 14362, 14372, 14382, 14392, 143102, 143112, 143122,
            143132, 143142, 143152, 143162, 143172, 143182, 143192 }),
    /**
     * 单个轮胎传感器异常报警
     */
    POS_14303("14303",
        new Integer[] { 14303, 14313, 14323, 14333, 14343, 14353, 14363, 14373, 14383, 14393, 143103, 143113, 143123,
            143133, 143143, 143153, 143163, 143173, 143183, 143193, 14399 }),
    /**
     * 胎压不平衡报警
     */
    POS_14304("14304",
        new Integer[] { 14304, 14314, 14324, 14334, 14344, 14354, 14364, 14374, 14384, 14394, 143104, 143114, 143124,
            143134, 143144, 143154, 143164, 143174, 143184, 143194 }),
    /**
     * 慢漏气报警
     */
    POS_14305("14305",
        new Integer[] { 14305, 14315, 14325, 14335, 14345, 14355, 14365, 14375, 14385, 14395, 143105, 143115, 143125,
            143135, 143145, 143155, 143165, 143175, 143185, 143195 }),
    /**
     * 传感器电量过低报警
     */
    POS_14306("14306",
        new Integer[] { 14306, 14316, 14326, 14336, 14346, 14356, 14366, 14376, 14386, 14396, 143106, 143116, 143126,
            143136, 143146, 143156, 143166, 143176, 143186, 143196 }),
    /**
     * 24小时疲劳驾驶
     */
    POS_7901("7091", new Integer[] { 203 }),
    ;

    private static final Map<String, List<Integer>> TYPE_AND_POS_LIST_MAP = new HashMap<>(16);

    static {
        for (AlarmTypePosEnum value : AlarmTypePosEnum.values()) {
            TYPE_AND_POS_LIST_MAP.put(value.getType(), value.getPos());
        }
    }

    private final String type;
    private final Integer[] posArr;

    AlarmTypePosEnum(String type, Integer[] posArr) {
        this.type = type;
        this.posArr = posArr;
    }

    public List<Integer> getPos() {
        return new ArrayList<>(Arrays.asList(posArr));
    }

    public String getType() {
        return type;
    }

    public static List<Integer> getPosListByType(@NotBlank String pos) {
        return TYPE_AND_POS_LIST_MAP.getOrDefault(pos, Lists.newArrayList(Integer.valueOf(pos.trim())));
    }
}