package com.feilangzzy.quartz.quartz;

import com.feilangzzy.quartz.util.CommonResponse;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Controller
@RequestMapping(value = "/quartz/schedule")
public class ScheduleController{
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    /**
     * 说明：停止任务
     *
     * @return
     * @throws SchedulerException
     */
    @RequestMapping(value = "stop")
    @ResponseBody
    public CommonResponse stopJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException {

        if (jobName == null || jobGroup == null) {
            return new CommonResponse(false,"任务不存在，请联系管理员！");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);

        return new CommonResponse(true,"任务"+jobName+"暂停成功！");
    }

    /**
     * 说明：启动任务
     *
     * @return
     * @throws SchedulerException
     */
    @RequestMapping(value = "resume")
    @ResponseBody
    public CommonResponse resumeJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException {
        if (jobName == null || jobGroup == null) {
            return new CommonResponse(false,"任务不存在，请联系管理员！");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);

        return new CommonResponse(true,"任务"+jobName+"恢复成功！");
    }

    /**
     * 说明：重启任务
     *
     * @return
     * @throws SchedulerException
     */
    @RequestMapping(value = "restart")
    @ResponseBody
    public CommonResponse reStartJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException {
        if (jobName == null || jobGroup == null) {
            return new CommonResponse(false,"任务不存在，请联系管理员！");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);

        return new CommonResponse(true,"任务"+jobName+"重启成功！");
    }

    /**
     * 说明：立即执行任务
     *
     * @return
     * @throws SchedulerException
     */
    @RequestMapping(value = "startnow")
    @ResponseBody
    public CommonResponse toStartNow(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException {

        if (jobName == null || jobGroup == null) {
            return new CommonResponse(false,"任务不存在，请联系管理员！");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);

        return new CommonResponse(true,"任务"+jobName+"执行成功！");
    }

    /**
     * 说明：删除任务
     *
     * @return
     * @throws SchedulerException
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public CommonResponse delJob(@RequestParam String jobName, @RequestParam String jobGroup) throws SchedulerException {
        if (jobName == null || jobGroup == null) {
            return new CommonResponse(false,"数据有错误，请联系管理员！");
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);

        return new CommonResponse(true,"任务"+jobName+"删除成功！");
    }

    //TODO:更新corn表达式
    @RequestMapping(value = "changeCron", method = RequestMethod.POST)
    @ResponseBody
    public CommonResponse reviseCronExpression(@RequestParam String jobName, @RequestParam String jobGroup, @RequestParam String cronExpression) throws SchedulerException {
        if (jobName == null || jobGroup == null) {
            return new CommonResponse(false,"数据有错误，请联系管理员！");
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
//        TriggerKey triggerKey = TriggerKey
//                .triggerKey(jobName, jobGroup);
        JobKey jobKey = new JobKey(jobName,jobGroup);

        // 获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.size()<1){
            return new CommonResponse(false,"没找到对应任务，请联系管理员！");
        }
        CronTrigger trigger = (CronTrigger) triggers.get(0);
        String cornpushtime = cronExpression.trim();
        CronTriggerImpl im = (CronTriggerImpl) trigger;
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule(cornpushtime);
        TriggerKey triggerKey = TriggerKey
                .triggerKey(im.getName(), im.getGroup());
        // 按新的cronExpression表达式重新构建trigger
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                .withSchedule(scheduleBuilder).build();
        // 按新的trigger重新设置job执行

        scheduler.rescheduleJob(triggerKey, trigger);

        return new CommonResponse(true,"任务"+jobName+"修改表达式成功！");
    }

    /**
     * 添加新任务
     */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseBody
    public CommonResponse addJob(ScheduleJob schedulejob) {

        CommonResponse commonResponse = new CommonResponse(true);
        //通过spring工厂获取调度器工厂实例
        Scheduler scheduler = schedulerFactoryBean.getScheduler();//调度实例
        TriggerKey triggerKey = TriggerKey.triggerKey(schedulejob.getJobName(), schedulejob.getJobGroup());
        try {
            // 获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            // 不存在，创建一个
            if (null == trigger) {
                //创建jobDetail
                JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(schedulejob.getClassName()))
                        .withIdentity(schedulejob.getJobName(), schedulejob.getJobGroup()).build();
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                        .cronSchedule(schedulejob.getCronExpression());
                // 按新的cronExpression表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger().withIdentity(schedulejob.getJobName(), schedulejob.getJobGroup())
                        .withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, trigger); //任务和触发器
            } else {
                // Trigger已存在，那么更新相应的定时设置
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                        .cronSchedule(schedulejob.getCronExpression());
                // 按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                        .withSchedule(scheduleBuilder).build();
                // 按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (SchedulerException e) {
            String msg = "失败:" + e.getLocalizedMessage();
            logger.error("addJob fail,msg=" + msg);
            // 返回错误信息
            commonResponse.setSuccess(false);
            commonResponse.setMsg(msg);
        } catch (ClassNotFoundException e) {
            String msg = "失败:" + e.getLocalizedMessage();
            logger.error("addJob fail,msg=" + msg);
            commonResponse.setSuccess(false);
            commonResponse.setMsg(msg);
        }
        return commonResponse;
    }

    /**
     * 添加新任务视图
     */
    @RequestMapping(value = "addjobview", method = RequestMethod.GET)
    public String addJobView() {
        return "addJob";
    }

//    /**
//     * 修正cron视图
//     */
//    @RequestMapping(value = "revisecronview", method = RequestMethod.GET)
//    public String reviseCronView() {
//        return "modules/schedule/revisecron";
//    }

    /**
     * 说明：计划中的任务
     *
     * @return
     * @throws
     */
    @RequestMapping(value = {"joblist", ""})
    public String jobListView(Model model) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();//从调度器工厂获取调度器实例
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List pushJobs = new ArrayList<ScheduleJob>();
        for (JobKey jobKey : jobKeys) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);//通过jobkey获取触发器实例集
            for (Trigger trigger : triggers) {
                ScheduleJob job = new ScheduleJob(); //从触发器中获取job信息封装到新的job中，用于页面展示
                job.setJobName(jobKey.getName()); //名称
                job.setJobGroup(jobKey.getGroup());//组
                job.setDesc("触发器:" + trigger.getKey());//描述
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setJobStatus(triggerState.name()); //状态
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    job.setCronExpression(cronExpression); //cron表达式
                }
                pushJobs.add(job);//加入pushJobs list中
            }
        }
        model.addAttribute("jobs", pushJobs);
        return "jobList";
    }

    /**
     * 说明：运行中的任务
     *
     * @return
     * @throws SchedulerException
     */
    @RequestMapping(value = {"runningjoblist"})
    public String runningJobListView(Model model) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List pushJobs = new ArrayList<ScheduleJob>(executingJobs.size());
        for (JobExecutionContext executingJob : executingJobs) {
            ScheduleJob job = new ScheduleJob();
            JobDetail jobDetail = executingJob.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            Trigger trigger = executingJob.getTrigger();
            job.setJobName(jobKey.getName());
            job.setJobGroup(jobKey.getGroup());
            job.setDesc("trigger:" + trigger.getKey());
            Trigger.TriggerState triggerState = scheduler
                    .getTriggerState(trigger.getKey());
            job.setJobStatus(triggerState.name());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                String cronExpression = cronTrigger.getCronExpression();
                job.setCronExpression(cronExpression);
            }
            pushJobs.add(job);
        }
        model.addAttribute("jobs", pushJobs);
        return "runningJobList";
    }


}
