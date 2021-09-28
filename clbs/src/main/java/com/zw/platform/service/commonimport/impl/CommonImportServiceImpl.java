package com.zw.platform.service.commonimport.impl;

import com.zw.platform.service.commonimport.CommonImportService;
import com.zw.platform.util.imports.ImportCache;
import com.zw.platform.util.imports.ProgressBar;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.dto.ProgressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用导入服务实现
 *
 * @author Zhang Yanhui
 * @since 2020/9/15 16:17
 */

@Slf4j
@Service
public class CommonImportServiceImpl implements CommonImportService {

    @Override
    public List<ProgressDTO> getImportProgress(ImportModule module, String username) {
        final List<BaseImportHandler> handlers = ImportCache.CACHE
                .getOrDefault(module, Collections.emptyMap())
                .getOrDefault(username, Collections.emptyList());
        final Collection<ProgressBar> progressBars = handlers.stream()
                .map(BaseImportHandler::getProgressBar)
                .collect(Collectors.toMap(ProgressBar::getStage, Function.identity(), ProgressBar.MERGER)).values();
        return progressBars.stream()
                .map(ProgressBar::getProgress)
                .sorted(Comparator.comparing(ProgressDTO::getStage))
                .collect(Collectors.toList());
    }

}
