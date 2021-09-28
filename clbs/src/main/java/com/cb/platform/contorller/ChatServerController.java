package com.cb.platform.contorller;

import com.cb.platform.domain.chat.ChatResponse;
import com.cb.platform.service.ChatServerService;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Controller
public class ChatServerController {

    @Autowired
    private ChatServerService chatServerService;

    @Value("${chat.server.url}")
    private String chatServerUrl;

    private static final String CHAT_PAGE = "modules/chat/anychat";
    private static final String ATT_CHAT_URL = "chatUrl";
    private static final String ATT_OP_CODE = "hOpCode";

    @RequestMapping(path = "/cb/chatPage", method = RequestMethod.GET)
    public ModelAndView getChatPage() {
        ModelAndView mav = new ModelAndView(CHAT_PAGE);
        mav.addObject(ATT_CHAT_URL, chatServerUrl);
        return mav;
    }

    @CrossOrigin("*")
    @RequestMapping(path = "/cb/chatServer", method = RequestMethod.POST)
    @ResponseBody
    public ChatResponse getToken(@RequestBody String body, HttpServletRequest request) {
        String opCode = request.getHeader(ATT_OP_CODE);
        return chatServerService.handle(opCode, body);
    }
}
