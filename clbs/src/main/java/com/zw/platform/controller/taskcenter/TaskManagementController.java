package com.zw.platform.controller.taskcenter;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.taskmanagement.DesignateInfo;
import com.zw.platform.domain.taskmanagement.DesignateMonitorInfo;
import com.zw.platform.domain.taskmanagement.TaskInfo;
import com.zw.platform.domain.taskmanagement.TaskInfoQuery;
import com.zw.platform.domain.taskmanagement.TaskItem;
import com.zw.platform.service.regionmanagement.FenceManagementService;
import com.zw.platform.service.taskcenter.TaskManagementService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 11:37
 */
@Controller
@RequestMapping("/a/taskManagement")
public class TaskManagementController {
    private Logger logger = LogManager.getLogger(TaskManagementController.class);

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private FenceManagementService fenceManagementService;

    @Autowired
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String syError;

    private static final String LIST_PAGE = "vas/taskManagement/list";
    private static final String TASK_ADD_PAGE = "vas/taskManagement/taskAdd";
    private static final String TASK_EDIT_PAGE = "vas/taskManagement/taskEdit";
    private static final String TASK_DETAIL_PAGE = "vas/taskManagement/taskDetail";
    private static final String DESIGNATE_ADD_PAGE = "vas/taskManagement/designateAdd";
    private static final String DESIGNATE_EDIT_PAGE = "vas/taskManagement/designateEdit";
    private static final String DESIGNATE_DETAIL_PAGE = "vas/taskManagement/designateDetail";
    private static final String ERROR_PAGE = "html/errors/error_exception";

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 获取任务列表
     * @return
     */
    @RequestMapping(value = "/getTaskList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getTaskList(TaskInfoQuery query) {
        try {
            Page<TaskInfo> taskList = (Page<TaskInfo>) taskManagementService.getTaskList(query);
            return new PageGridBean(taskList, true);
        } catch (Exception e) {
            logger.error("获取任务管理任务列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增任务页面
     * @return
     */
    @RequestMapping(value = "/getTaskAddPage", method = RequestMethod.GET)
    public String getTaskAddPage() {
        return TASK_ADD_PAGE;
    }

    @RequestMapping(value = "/checkTaskName", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkTaskName(String name, String id) {
        try {
            if (StringUtils.isNotEmpty(name)) {
                return taskManagementService.checkTaskName(name, id);
            }
            return false;
        } catch (Exception e) {
            logger.error("校验任务名异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/addTask", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addTask(TaskInfo taskInfo) {
        try {
            if (taskInfo != null && StringUtils.isNotEmpty(taskInfo.getTaskItemsStr())) {
                List<TaskItem> taskItems = JSON.parseArray(taskInfo.getTaskItemsStr(), TaskItem.class);
                taskInfo.setTaskItems(taskItems);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.addTask(taskInfo, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增任务异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/getTaskEditPage_{id}", method = RequestMethod.GET)
    public ModelAndView getTaskEditPage(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView mav = new ModelAndView(TASK_EDIT_PAGE);
                TaskInfo taskInfo = taskManagementService.findTaskInfoById(id);
                mav.addObject("taskInfo", taskInfo);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("获取修改任务页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/editTask", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editTask(TaskInfo taskInfo) {
        try {
            if (taskInfo != null && StringUtils.isNotEmpty(taskInfo.getTaskItemsStr())) {
                List<DesignateInfo> designateByTaskId = taskManagementService.findDesignateByTaskId(taskInfo.getId());
                if (CollectionUtils.isNotEmpty(designateByTaskId)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该任务关联了指派，不能修改！");
                }
                List<TaskItem> taskItems = JSON.parseArray(taskInfo.getTaskItemsStr(), TaskItem.class);
                taskInfo.setTaskItems(taskItems);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.updateTask(taskInfo, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改任务异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/deleteTask_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteTask(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                List<DesignateInfo> designateByTaskId = taskManagementService.findDesignateByTaskId(id);
                if (CollectionUtils.isNotEmpty(designateByTaskId)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "该任务关联了指派，不能修改！");
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.deleteTask(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除任务异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/getDetailPage_{id}", method = RequestMethod.GET)
    public ModelAndView getDetailPage(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView mav = new ModelAndView(TASK_DETAIL_PAGE);
                TaskInfo taskInfo = taskManagementService.findTaskInfoById(id);
                mav.addObject("taskInfo", taskInfo);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("获取修改任务页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取指派列表
     * @return
     */
    @RequestMapping(value = "/getDesignateList", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getDesignateList(TaskInfoQuery query) {
        try {
            Page<DesignateInfo> designateList =
                (Page<DesignateInfo>) taskManagementService.getDesignateList(query);
            return new PageGridBean(designateList, true);
        } catch (Exception e) {
            logger.error("获取任务管理指派任务列表异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 新增指派页面
     * @return
     */
    @RequestMapping(value = "/getDesignateAddPage", method = RequestMethod.GET)
    public ModelAndView getDesignateAddPage() {
        try {
            ModelAndView mav = new ModelAndView(DESIGNATE_ADD_PAGE);
            String taskTree = taskManagementService.getTaskTree();
            mav.addObject("taskTree", taskTree);
            return mav;
        } catch (Exception e) {
            logger.error("新增指派页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/getTaskDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTaskDetail(String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                TaskInfo taskInfo = taskManagementService.findTaskInfoById(id);
                return new JsonResultBean(taskInfo);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增任务指派获取任务详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/checkDesignateName", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkDesignateName(String name, String id) {
        try {
            if (StringUtils.isNotEmpty(name)) {
                return taskManagementService.checkDesignateName(name, id);
            }
            return false;
        } catch (Exception e) {
            logger.error("校验任务指派名异常", e);
            return false;
        }
    }

    @RequestMapping(value = "/addDesignate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addDesignate(DesignateInfo designateInfo) {
        try {
            if (designateInfo != null && StringUtils.isNotEmpty(designateInfo.getDesignatePeopleInfosStr())) {
                List<DesignateMonitorInfo> designatePeopleInfos =
                    JSON.parseArray(designateInfo.getDesignatePeopleInfosStr(), DesignateMonitorInfo.class);
                designateInfo.setDesignatePeopleInfos(designatePeopleInfos);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.addDesignate(designateInfo, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("新增指派异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/getDesignateEditPage_{id}", method = RequestMethod.GET)
    public ModelAndView getDesignateEditPage(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView mav = new ModelAndView(DESIGNATE_EDIT_PAGE);
                DesignateInfo designateInfo = taskManagementService.getDesignateById(id);
                designateInfo.setDesignatePeopleInfosStr(JSON.toJSONString(designateInfo.getDesignatePeopleInfos()));
                String taskTree = taskManagementService.getTaskTree();
                mav.addObject("taskTree", taskTree);
                mav.addObject("designate", designateInfo);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("获取修改指派页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    @RequestMapping(value = "/editDesignate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean editDesignate(DesignateInfo designateInfo) {
        try {
            if (designateInfo != null && StringUtils.isNotEmpty(designateInfo.getDesignatePeopleInfosStr())) {
                List<DesignateMonitorInfo> designatePeopleInfos =
                    JSON.parseArray(designateInfo.getDesignatePeopleInfosStr(), DesignateMonitorInfo.class);
                designateInfo.setDesignatePeopleInfos(designatePeopleInfos);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.updateDesignate(designateInfo, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("修改指派异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/deleteDesignate_{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteDesignate(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.deleteDesignate(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("删除指派异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/getDesignateDetail_{id}", method = RequestMethod.GET)
    public ModelAndView getDesignateDetailPage(@PathVariable("id") String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                ModelAndView mav = new ModelAndView(DESIGNATE_DETAIL_PAGE);
                DesignateInfo designate = taskManagementService.getDesignateById(id);
                designate.setDesignatePeopleInfosStr(JSON.toJSONString(designate.getDesignatePeopleInfos()));
                mav.addObject("designate", designate);
                return mav;
            }
            return new ModelAndView(ERROR_PAGE);
        } catch (Exception e) {
            logger.error("获取指派详情页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 强制结束（将结束时间设置为今天）
     * @param id
     * @return
     */
    @RequestMapping(value = "/forcedEnd", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean forcedEnd(String id) {
        try {
            if (StringUtils.isNotEmpty(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return taskManagementService.updateForcedEnd(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("任务指派强制结束异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    @RequestMapping(value = "/fenceTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceTree() {
        try {
            String fenceTree = JSON.toJSONString(fenceManagementService.getFenceTree().getObj());
            return new JsonResultBean(fenceTree);
        } catch (Exception e) {
            logger.error("获取围栏树异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }
}
