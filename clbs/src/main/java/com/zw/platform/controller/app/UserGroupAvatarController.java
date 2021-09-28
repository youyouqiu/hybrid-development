package com.zw.platform.controller.app;

import com.zw.platform.commons.Auth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Chen Feng
 * @version 1.0 2018/8/8
 */
@Controller
@RequestMapping("/m/app/group")
public class UserGroupAvatarController {
    private static final String USER_GROUP_AVATAR_PAGE = "modules/intercomplatform/app/userGroupAvatar";

    @Auth
    @RequestMapping(value = "/avatar/page", method = RequestMethod.GET)
    public String configPage() {
        return USER_GROUP_AVATAR_PAGE;
    }
}
