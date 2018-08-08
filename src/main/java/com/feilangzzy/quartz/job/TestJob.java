package com.feilangzzy.quartz.job;

import com.feilangzzy.quartz.service.TestService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class TestJob extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    TestService testService;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("=========TestJob start=========");
        Long startTime = System.currentTimeMillis();
        testService.test();
        logger.info("===========TestJob==========用时" + (System.currentTimeMillis() - startTime) / 1000 + "秒");
    }
}
