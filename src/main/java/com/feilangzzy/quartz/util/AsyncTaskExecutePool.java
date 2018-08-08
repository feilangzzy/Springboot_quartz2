package com.feilangzzy.quartz.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Component
@EnableAsync
public class AsyncTaskExecutePool implements AsyncConfigurer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${task.core_pool_size}")
    private String corePoolSize;
    @Value("${task.max_pool_size}")
    private String maxPoolSize;
    @Value("${task.queue_capacity}")
    private String queueCapacity;
    @Value("${task.keep_alive_seconds}")
    private String keepAliveSeconds;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Integer.valueOf(corePoolSize));
        executor.setMaxPoolSize(Integer.valueOf(maxPoolSize));
        executor.setQueueCapacity(Integer.valueOf(queueCapacity));
        executor.setKeepAliveSeconds(Integer.valueOf(keepAliveSeconds));
//        executor.setThreadNamePrefix("taskExecutor-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务   默认abortPolicy
        // CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {// 异步任务中异常处理
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
                logger.error("=============async method:"+method.getName()+"=============exception:"+ ExceptionUtils.getStackTrace(throwable)+"=======================");
            }
        };
    }
}
