package com.zw.platform.util.imports;

import com.google.common.collect.Lists;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.Translator;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.BeanUtil;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.dto.ImportProgress;
import com.zw.platform.util.imports.lock.dto.ProgressDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 进度条
 * <table>使用方法
 * <tr>1.
 *
 * @author Zhang Yanhui
 * @since 2020/9/15 10:28
 */

@Data
@Slf4j
public class ProgressBar {
    /**
     * 进度条变化小于此百分比时不执行推送
     */
    private static final double PUSH_THRESHOLD = 0.01d;
    /**
     * 阶段状态
     */
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_SUCCEED = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_WAITING = 3;

    /**
     * 模块阶段
     */
    public static final Translator<ImportModule, ArrayList<String>> MODULE_STAGE_NAMES = Translator
            .<ImportModule, ArrayList<String>>builder()
            .add(ImportModule.CONFIG, Lists.newArrayList("导入监控对象", "导入终端信息", "导入SIM卡信息", "导入分组信息", "导入绑定关系"))
            .add(ImportModule.VEHICLE, Lists.newArrayList("导入车辆"))
            .add(ImportModule.PEOPLE, Lists.newArrayList("导入人员"))
            .add(ImportModule.THING, Lists.newArrayList("导入物品"))
            .add(ImportModule.DEVICE, Lists.newArrayList("导入终端"))
            .add(ImportModule.SIM_CARD, Lists.newArrayList("导入SIM卡"))
            .add(ImportModule.PROFESSIONAL, Lists.newArrayList("导入从业人员"))
            .add(ImportModule.ASSIGNMENT, Lists.newArrayList("导入分组"))
            .build();

    /**
     * 合并函数
     */
    public static final BinaryOperator<ProgressBar> MERGER = (o, p) -> {
        final int totalProgress =
                o.getTotalProgress() * o.getScore() + p.getTotalProgress() * p.getScore();
        final int currentProgress =
                o.getCurrentProgress() * o.getScore() + p.getCurrentProgress() * p.getScore();
        final int status = mergeStatus(o.getStatus(), p.getStatus());
        final ProgressBar bar = new ProgressBar(o.getModule(), o.getStage(), status);
        bar.setTotalProgress(totalProgress);
        bar.setCurrentProgress(currentProgress);
        return bar;
    };

    private static int mergeStatus(int status1, int status2) {
        // 边界时（左边已完成，右边等待中），由于合并进度，所以整体是进行中
        if (status1 == STATUS_SUCCEED && status2 == STATUS_WAITING
                || status1 == STATUS_WAITING && status2 == STATUS_SUCCEED) {
            return STATUS_RUNNING;
        }
        return IntStream.of(STATUS_FAILED, STATUS_RUNNING, STATUS_SUCCEED, STATUS_WAITING)
                .filter(i -> i == status1 || i == status2)
                .findFirst()
                .orElse(STATUS_WAITING);
    }

    /**
     * 模块
     */
    private ImportModule module;

    /**
     * 状态 0进行中 1成功 2失败 3等待中
     */
    private int status;

    public void setStatus(int status) {
        this.status = status;
        if (status == STATUS_SUCCEED) {
            tryPushProgress();
        }
    }

    /**
     * 推送阈值计算标记
     */
    private double lastPushedRatio;

    /**
     * 进度条总长（总任务数）
     */
    private int totalProgress = -1;

    /**
     * 进度条当前已走长度
     */
    private int currentProgress = 0;

    /**
     * 进度权重，默认1
     */
    private int score = 1;

    public void addProgress(int progress) {
        currentProgress += progress;
        tryPushProgress();
    }

    /**
     * 当前阶段，默认0
     */
    private int stage;

    /**
     * 当前状态
     */
    private String stageName;

    public ProgressBar(ImportModule module, int stage) {
        this(module, stage, STATUS_WAITING);
    }

    public ProgressBar(ImportModule module, int stage, int status) {
        this.module = module;
        this.stage = stage;
        final Optional<ArrayList<String>> names = MODULE_STAGE_NAMES.b2pOptional(module);
        this.stageName = names.filter(list -> list.size() > stage).map(list -> list.get(stage)).orElse("");
        this.status = status;
    }

    /**
     * 查询当前进度
     */
    public ProgressDTO getProgress() {
        double ratio;
        switch (status) {
            case STATUS_RUNNING:
            case STATUS_FAILED:
                final double rawRatio = totalProgress == 0 ? .99d : ((double) currentProgress) / totalProgress;
                ratio = Math.min(.99d, Math.max(.01d, rawRatio));
                break;
            case STATUS_SUCCEED:
                ratio = 1d;
                break;
            default:
                ratio = 0d;
        }
        ratio = new BigDecimal(String.valueOf(ratio)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return new ProgressDTO(totalProgress * score, currentProgress * score, ratio, status, stage, stageName);
    }

    /**
     * ws推送进度
     */
    public void tryPushProgress() {
        final ProgressDTO progress = getProgress();
        if (progress.getRatio() > .999d || progress.getRatio() - lastPushedRatio > PUSH_THRESHOLD) {
            if (log.isDebugEnabled()) {
                log.debug("推送进度：{}", new DecimalFormat("#.##%").format(progress.getRatio()));
            }
            pushProgress(module, null);
        } else {
            log.debug("无需推送进度");
        }
    }

    /**
     * 推送进度和错误信息
     *
     * @param errorMsg 错误信息
     */
    public static void pushProgress(ImportModule module, List<String> errorMsg) {
        String userName = SystemHelper.getCurrentUsername();
        final List<BaseImportHandler> handlers = ImportCache.CACHE.getOrDefault(module, Collections.emptyMap())
                .getOrDefault(userName, Collections.emptyList());
        final List<ProgressDTO> progressList = getProgressDTO(handlers);
        final ImportProgress progresses = new ImportProgress(progressList, module, errorMsg);
        // 有错终止时，把进行中（或第一个等待中）的那个阶段的状态置为失败
        if (CollectionUtils.isNotEmpty(errorMsg)) {
            progresses.getProgress().stream()
                    .filter(o -> o.getStatus() != STATUS_SUCCEED)
                    .findFirst()
                    .ifPresent(o -> o.setStatus(STATUS_FAILED));
        }
        final SimpMessagingTemplate simpMessagingTemplate = BeanUtil.getBean(SimpMessagingTemplate.class);
        simpMessagingTemplate.convertAndSendToUser(userName, ConstantUtil.WEB_SOCKET_IMPORT_PROGRESS, progresses);
    }

    public static List<ProgressDTO> getProgressDTO(List<BaseImportHandler> handlers) {
        final Collection<ProgressBar> progressBars = handlers.stream()
                .map(BaseImportHandler::getProgressBar)
                // 设置推送标记
                .peek(progressBar -> progressBar.setLastPushedRatio(progressBar.getProgress().getRatio()))
                .collect(Collectors.toMap(ProgressBar::getStage, Function.identity(), MERGER)).values();
        return progressBars.stream()
                .map(ProgressBar::getProgress)
                .sorted(Comparator.comparing(ProgressDTO::getStage))
                .collect(Collectors.toList());
    }
}
