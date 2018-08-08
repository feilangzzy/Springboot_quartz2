package com.feilangzzy.quartz.quartz.config;

import com.feilangzzy.quartz.job.TestJob;
import com.feilangzzy.quartz.util.JobBeanJobFactory;
import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

@Configuration
public class QuartzConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Value("${spring.profiles.active}")
    private String profilesActive;

    @Autowired
    JobBeanJobFactory jobBeanJobFactory;

    @Bean
    public SchedulerFactoryBean quartzScheduler() throws IOException {
        //一个定时任务
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(jobBeanJobFactory);
//        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactoryBean.setQuartzProperties(quartzProperties());
        //生产环境，quartz需要用主库
        if(!"production".equals(profilesActive)){
            DataSource dataSource = applicationContext.getBean(DataSource.class);
            schedulerFactoryBean.setDataSource(dataSource);
        }
        List<Trigger> triggerList = new ArrayList<>();

        triggerList.add(hySelfPushJobCronTrigger().getObject());

        Trigger[] triggerArray = new Trigger[triggerList.size()];
        for (int i=0;i<triggerList.size();i++){
            triggerArray[i] = triggerList.get(i);
        }
        schedulerFactoryBean.setTriggers(triggerArray);

        return schedulerFactoryBean;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean
    public JobDetailFactoryBean hySelfPushJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setDurability(true);
        jobDetailFactoryBean.setJobClass(TestJob.class);
        return jobDetailFactoryBean;
    }
    @Bean
    public CronTriggerFactoryBean hySelfPushJobCronTrigger() {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(hySelfPushJobDetail().getObject());
        cronTriggerFactoryBean.setCronExpression("0 20 9,10,11 * * ?");
        return cronTriggerFactoryBean;
    }

}
