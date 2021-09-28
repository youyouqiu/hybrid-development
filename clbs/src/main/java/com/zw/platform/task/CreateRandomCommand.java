package com.zw.platform.task;

import java.util.List;

import com.zw.protocol.msg.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParamQuery;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StringUtil;
import com.zw.protocol.msg.t809.body.AgingPwdUpData;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;

/**
 * 定时给视频连接参数生成随机归属地平台口令和跨域平台口令(每日0点)
 * @author hujun
 * @Date 创建时间：2018年2月9日 上午10:31:40
 */
@DisallowConcurrentExecution
public class CreateRandomCommand implements Job {

    private static final Logger log = LogManager.getLogger(CreateRandomCommand.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ApplicationContext appCtx;
        try {
            appCtx = (ApplicationContext) context.getScheduler().getContext().get("applicationContextKey");
            if (appCtx != null) {
                ConnectionParamsSetDao connectionParamsSetDao = appCtx.getBean(ConnectionParamsSetDao.class);
                ServerParamList serverParamList = appCtx.getBean(ServerParamList.class);
                List<PlantParam> plantParam = connectionParamsSetDao.get809ConnectionParamsSet(new PlantParamQuery());
                for (PlantParam p : plantParam) {
                    //获取随机生成的归属地平台口令和跨域平台口令
                    String authorizeCode1 = StringUtil.getRandomStringByLength(64);
                    String authorizeCode2 = StringUtil.getRandomStringByLength(64);
                    p.setAuthorizeCode1(authorizeCode1);
                    p.setAuthorizeCode2(authorizeCode2);
                    //更新数据库
                    connectionParamsSetDao.update809ConnectionParamsSet(p);
                    //发送时效指令
                    AgingPwdUpData agingPwdUpData = new AgingPwdUpData();
                    agingPwdUpData.setAuthorizeCode1(p.getAuthorizeCode1());
                    agingPwdUpData.setAuthorizeCode2(p.getAuthorizeCode2());
                    agingPwdUpData.setDataType(ConstantUtil.T809_UP_AUTHORIZE_MSG_STARTUP);
                    agingPwdUpData.setPlatformId(p.getPlatformId());
                    Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_AUTHORIZE_MSG, MsgUtil
                        .getT809Message(ConstantUtil.T809_UP_AUTHORIZE_MSG, p.getIp(), p.getCenterId(), agingPwdUpData))
                        .assembleDesc809(p.getId());
                    WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                        .writeAndFlush(message);
                }
            }
        } catch (Exception e) {
            log.error("自动生成随机归属地平台口令和跨域平台口令异常", e);
        }

    }

}
