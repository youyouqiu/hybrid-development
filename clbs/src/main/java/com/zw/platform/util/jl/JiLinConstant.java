package com.zw.platform.util.jl;

import com.zw.platform.util.Translator;

/**
 * @author create by zhouzongbo on 2020/6/12.
 */
public final class JiLinConstant {

    /**
     * 上传时使用
     * B: CLBS-车牌颜色：1蓝，2黄，3黑，4白，9其他,90:农蓝, 91农黄,92农绿,93黄绿色,94渐变绿色
     * p: 吉林-车牌颜色: 1蓝，2黄，3黑，4白，5农黄， 6：农蓝, 7: 农绿,9其他
     */
    public static final Translator<Integer, Integer> CLBS_COLOR_JILIN_TRANSLATOR = Translator.of(
        1, 1,
        2, 2,
        3, 3,
        4, 4,
        91, 5,
        90, 6,
        Translator.Pair.of(92, 7),
        Translator.Pair.of(9, 9)
        );

    /**
     * 违规类型: 1:扭动镜头; 2:遮挡镜头; 3: 无照片; 4: 无定位; 5: 轨迹异常；6：超员; 7: 超速; 8:脱线运行
     */
    public static final Translator<Integer, String> VIOLATE_TYPE = Translator.of(
        1, "扭动镜头",
        2, "遮挡镜头",
        3, "无照片",
        4, "无定位",
        5, "轨迹异常",
        6, "超员",
        Translator.Pair.of(7, "超速"),
        Translator.Pair.of(8, "脱线运行")
    );

    public static final Translator<Integer, String> ALARM_TYPE = Translator.of(
        0, "紧急报警",
        10, "疲劳报警",
        200, "禁入报警",
        201, "禁出报警",
        210, "偏航报警",
        41, "超速报警",
        Translator.Pair.of(53, "夜间行驶报警")
    );

    public static final Translator<Integer, String> ALARM_HANDLE_STATUS_TYPE = Translator.of(
        1, "处理中",
        2, "已处理完毕",
        3, "不作处理",
        4, "将来处理"
    );

    public static final Translator<Integer, String> RUN_STATUS = Translator.of(
        10, "营运",
        21, "停运",
        22, "挂失",
        31, "迁出(过户)",
        32, "迁出(转籍)",
        33, "报废",
        Translator.Pair.of(34, "歇业"),
        Translator.Pair.of(80, "注销"),
        Translator.Pair.of(90, "其他")
    );

    public static final Translator<Integer, String> RESULT_TS = Translator.of(0, "上传失败", 1, "上传成功");

    /**
     * 上传失败
     */
    public static final int RESULT_FAULT = 0;
    /**
     * 上传成功
     */
    public static final int RESULT_SUCCESS = 1;

    public static final String RESULT = "result";

    public static final String MSG = "msg";
}
