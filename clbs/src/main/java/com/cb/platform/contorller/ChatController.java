package com.cb.platform.contorller;

import com.cb.platform.domain.ChatDo;
import com.cb.platform.domain.query.ChatQuery;
import com.cb.platform.service.ChatService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
@RequestMapping("/cb/chat")
public class ChatController {

    private static Logger logger = LogManager.getLogger(ChatController.class);

    private static final String LIST_PAGE = "modules/chat/list";

    @Autowired
    private ChatService chatService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 获取表格页面
     * @return list page
     */
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询
     * @param query
     * @return
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getListPage(final ChatQuery query) {
        try {
            List<ChatDo> result = chatService.findAll(query);
            return new JsonResultBean(result);
        } catch (Exception e) {
            logger.error("分页查询通讯日志信息异常："+e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT,sysErrorMsg);
        }
    }

    /**
     * 导出
     * @param chatContent
     * @param fromUserName
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public boolean exportChat(String chatContent, String fromUserName, String startTime, String endTime) {
        try {
            ChatQuery query = new ChatQuery();
            query.setChatContent(chatContent);
            query.setFromUserName(fromUserName);
            query.setStartTime(startTime);
            query.setEndTime(endTime);
            RedisUtil.storeExportDataToRedis("exportChatInformation",chatService.findAll(query));
            return true;
        } catch (Exception e) {
            logger.error("导出聊天记录信息异常"+e.getMessage(), e);
        }
        return false;
    }

    /**
     * 导出(生成excel文件)
     * @param res
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "聊天记录信息列表");
            chatService.export(null, 1, res, RedisUtil.getExportDataFromRedis("exportChatInformation"));
        } catch (Exception e) {
            logger.error("超速报警统计页面导出通讯日志信息列表(get)异常", e);
        }
    }

}
