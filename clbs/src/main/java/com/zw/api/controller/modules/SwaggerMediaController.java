package com.zw.api.controller.modules;

import com.zw.api.domain.MediaInfo;
import com.zw.api.domain.ResultBean;
import com.zw.api.service.SwaggerMediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/swagger/m/media")
@Api(tags = { "多媒体信息" }, description = "多媒体信息相关API接口")
public class SwaggerMediaController {
    private static final Logger log = LogManager.getLogger(SwaggerMediaController.class);

    @Autowired
    private SwaggerMediaService swaggerMediaService;

    @ApiOperation(value = "获取指定监控对象在指定日期上传的多媒体信息URL列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "read", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "monitorName", value = "监控对象名称", paramType = "query", dataType = "string",
            required = true),
        @ApiImplicitParam(name = "start", value = "查询开始日期，格式为yyyy-MM-dd HH:mm:ss", paramType = "query",
            dataType = "LocalDate", required = true),
        @ApiImplicitParam(name = "end", value = "查询结束日期，格式为yyyy-MM-dd HH:mm:ss", paramType = "query",
            dataType = "LocalDate", required = true)
    })
    @ApiResponse(code = 200, message = "true 成功, 其它为错误, 返回格式:{success: true, msg: '', data: []}")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<List<MediaInfo>> list(@RequestParam String monitorName, @RequestParam LocalDateTime start,
        @RequestParam LocalDateTime end) {
        if (Duration.between(start, end).toDays() > 6) {
            return new ResultBean<>(false, "查询时间间隔不能超过7天");
        }
        List<MediaInfo> mediaUrls = swaggerMediaService.listMonitorMedia(monitorName, start, end);
        return new ResultBean<>(true, "", mediaUrls);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultBean<?> handleException(Exception e) {
        return new ResultBean<>(false, e.getMessage());
    }
}
