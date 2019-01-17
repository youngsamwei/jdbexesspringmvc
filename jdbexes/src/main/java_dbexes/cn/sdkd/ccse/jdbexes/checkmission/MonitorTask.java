package cn.sdkd.ccse.jdbexes.checkmission;

import cn.sdkd.ccse.jdbexes.service.ICheckMissionService;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by sam on 2019/1/5.
 */
@Component
public class MonitorTask {
    private static final Log logger = LogFactory.getLog(MonitorTask.class);
    @Autowired
    private ICheckMissionService checkMissionService;

    @Autowired
    private IJPlagService jPlagService;

    @Scheduled(cron = "0 * * * * ?")
    public void monitor(){
        checkMissionService.monitorJob();
        jPlagService.monitorJob();
    }

}
