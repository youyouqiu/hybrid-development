package com.zw.platform.task;

import com.github.pagehelper.util.StringUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserRepo;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimingUserAccreditJob implements Job {

    private UserRepo userRepo;

    private LdapTemplate ldapTemplate;

    /**
     * 定时任务,每天00:00时启动触发器,获取数据库数据,对比授权截止时间与今天的差值, 如果相差86400000,则将这个用户的状态更改为停用
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx = null;
        SimpleDateFormat stringDate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (appCtx != null) {
                userRepo = appCtx.getBean(UserRepo.class);
                UserService userService = appCtx.getBean(UserService.class);
                ldapTemplate = appCtx.getBean(LdapTemplate.class);
                // 将今天的日期转换为时间戳格式
                Date nowDate = new Date();
                String newDate = stringDate.format(nowDate);
                Iterable<UserBean> userBean = userRepo.findAll();
                for (UserBean user : userBean) {
                    String authorDate = user.getAuthorizationDate();
                    if (authorDate != null && !authorDate.isEmpty() && !"null".equals(authorDate)) {
                        Date authorDa = stringDate.parse(authorDate);
                        Date d = stringDate.parse(newDate);
                        if (d.getTime() - authorDa.getTime() == 86400000) {
                            String userid = user.getId().toString();
                            user.setState("0");// 0是停用,1是启用
                            update(userid, user);
                            final RedisKey stateKey = HistoryRedisKeyEnum.USER_STATE.of(user.getUsername());
                            RedisHelper.setString(stateKey, user.getState());
                            userService.expireUserSession(user.getUsername());
                        }
                    }
                }
            }
        } catch (SchedulerException | ParseException e) {
            e.printStackTrace();
        }

    }

    public void update(String userId, UserBean user) {
        LdapName dn = LdapUtils.newLdapName(userId);
        UserBean existingUser = userRepo.findOne(dn);
        String groupId = "";
        if (user.getGroupId() != null && StringUtils.isNotEmpty(user.getGroupId())) {
            groupId = user.getGroupId();
        }
        List<ModificationItem> modifList = new ArrayList<>();
        if (StrUtil.isNotBlank(user.getState())) {
            modifList
                .add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("st", user.getState())));
        } else {
            if (StringUtil.isNotEmpty(existingUser.getState())) {
                modifList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    new BasicAttribute("st", existingUser.getState())));
            }
        }
        if (!modifList.isEmpty()) {
            ModificationItem[] modifArray = new ModificationItem[modifList.size()];
            for (int i = 0; i < modifList.size(); i++) {
                modifArray[i] = modifList.get(i);
            }
            ldapTemplate.modifyAttributes(dn, modifArray);
        }
    }

}
