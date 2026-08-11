package com.factoryflow.schedule.infrastructure;

import com.factoryflow.schedule.application.ScheduleExecutionService;
import java.time.Instant;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class FactoryFlowReportJob extends QuartzJobBean {
    @Autowired private ScheduleExecutionService execution;
    @Override protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        long scheduleId = context.getMergedJobDataMap().getLong("scheduleId");
        Instant scheduledFor = context.getScheduledFireTime() == null
                ? Instant.now() : context.getScheduledFireTime().toInstant();
        execution.execute(scheduleId, scheduledFor);
    }
}
