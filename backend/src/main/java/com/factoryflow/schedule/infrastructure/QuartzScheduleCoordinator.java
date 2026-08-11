package com.factoryflow.schedule.infrastructure;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.persistence.ReportScheduleRepository;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class QuartzScheduleCoordinator {
    private static final String GROUP = "factoryflow-reports";
    private final Scheduler scheduler; private final ReportScheduleRepository schedules;
    public QuartzScheduleCoordinator(Scheduler scheduler, ReportScheduleRepository schedules) {
        this.scheduler = scheduler; this.schedules = schedules;
    }
    @EventListener(ApplicationReadyEvent.class)
    public void restore() { schedules.findAllByEnabledTrue().forEach(this::synchronize); }
    public void synchronize(ReportSchedule schedule) {
        JobKey jobKey = jobKey(schedule.getId()); TriggerKey triggerKey = triggerKey(schedule.getId());
        try {
            if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
            if (!schedule.isEnabled()) return;
            var job = newJob(FactoryFlowReportJob.class).withIdentity(jobKey)
                    .usingJobData("scheduleId", schedule.getId()).build();
            var trigger = newTrigger().withIdentity(triggerKey).forJob(job)
                    .withSchedule(cron(schedule)).build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz schedule could not be synchronized", exception);
        }
    }
    public Optional<Instant> nextRun(Long scheduleId) {
        try {
            var trigger = scheduler.getTrigger(triggerKey(scheduleId));
            Date next = trigger == null ? null : trigger.getNextFireTime();
            return Optional.ofNullable(next).map(Date::toInstant);
        } catch (SchedulerException exception) { return Optional.empty(); }
    }
    private CronScheduleBuilder cron(ReportSchedule schedule) {
        String expression = switch (schedule.getType()) {
            case DAILY -> "%d %d %d * * ?".formatted(schedule.getTime().getSecond(), schedule.getTime().getMinute(), schedule.getTime().getHour());
            case WEEKLY -> "%d %d %d ? * %s".formatted(schedule.getTime().getSecond(), schedule.getTime().getMinute(),
                    schedule.getTime().getHour(), schedule.getDayOfWeek().name().substring(0, 3));
            case MONTHLY -> "%d %d %d 1 * ?".formatted(schedule.getTime().getSecond(), schedule.getTime().getMinute(), schedule.getTime().getHour());
        };
        return cronSchedule(expression).inTimeZone(TimeZone.getTimeZone(schedule.getTimezone()))
                .withMisfireHandlingInstructionFireAndProceed();
    }
    private JobKey jobKey(Long id) { return new JobKey("schedule-" + id, GROUP); }
    private TriggerKey triggerKey(Long id) { return new TriggerKey("schedule-" + id, GROUP); }
}
