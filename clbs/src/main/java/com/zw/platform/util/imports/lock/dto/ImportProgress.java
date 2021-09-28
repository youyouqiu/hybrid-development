package com.zw.platform.util.imports.lock.dto;

import com.zw.platform.util.imports.lock.ImportModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 进度条DTO
 *
 * @author Zhang Yanhui
 * @since 2020/9/14 17:16
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportProgress {

    private List<ProgressDTO> progress;

    private ImportModule module;

    private List<String> errors;
}
