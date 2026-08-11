package com.factoryflow.schedule.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.schedule.api.ReportScheduleRequest;
import com.factoryflow.schedule.api.ReportScheduleResponse;
import com.factoryflow.schedule.api.ScheduleRunResponse;
import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.infrastructure.QuartzScheduleCoordinator;
import com.factoryflow.schedule.persistence.ReportScheduleRepository;
import com.factoryflow.schedule.persistence.ScheduleRunRepository;
import com.factoryflow.shared.api.PageResponse;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportScheduleService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of(ReportSchedule.BUSINESS_TIMEZONE);
    private final ReportScheduleRepository schedules; private final ScheduleRunRepository runs;
    private final AuthenticationService authentication; private final QuartzScheduleCoordinator quartz;
    public ReportScheduleService(ReportScheduleRepository schedules, ScheduleRunRepository runs,
                                 AuthenticationService authentication, QuartzScheduleCoordinator quartz) {
        this.schedules = schedules; this.runs = runs; this.authentication = authentication; this.quartz = quartz;
    }
    @Transactional(readOnly = true)
    public List<ReportScheduleResponse> list() { return schedules.findAllByOrderByTypeAscTimeAsc().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public ReportScheduleResponse get(Long id) { return response(require(id)); }
    @Transactional
    public ReportScheduleResponse create(String email, ReportScheduleRequest request) {
        ReportSchedule schedule;
        try {
            schedule = ReportSchedule.create(authentication.requireUser(email), request.type(), request.time(), request.dayOfWeek(),
                    request.generateExcel(), request.generatePdf(), request.emailEnabled(), recipients(request), request.enabled());
        } catch (IllegalArgumentException exception) { throw invalid(exception); }
        schedule = schedules.saveAndFlush(schedule); quartz.synchronize(schedule); return response(schedule);
    }
    @Transactional
    public ReportScheduleResponse update(Long id, ReportScheduleRequest request) {
        ReportSchedule schedule = require(id);
        try { schedule.configure(request.type(), request.time(), request.dayOfWeek(), request.generateExcel(),
                request.generatePdf(), request.emailEnabled(), recipients(request), request.enabled()); }
        catch (IllegalArgumentException exception) { throw invalid(exception); }
        schedules.flush(); quartz.synchronize(schedule); return response(schedule);
    }
    @Transactional
    public ReportScheduleResponse setEnabled(Long id, boolean enabled) {
        ReportSchedule schedule = require(id); schedule.setEnabled(enabled); schedules.flush(); quartz.synchronize(schedule);
        return response(schedule);
    }
    @Transactional(readOnly = true)
    public PageResponse<ScheduleRunResponse> runs(Long id, Pageable pageable) {
        require(id); return PageResponse.from(runs.findAllByScheduleId(id, pageable), ScheduleRunResponse::from);
    }
    private Set<String> recipients(ReportScheduleRequest request) {
        Set<String> values = new LinkedHashSet<>();
        request.recipients().stream().map(String::trim).map(value -> value.toLowerCase(Locale.ROOT)).forEach(values::add);
        return values;
    }
    private ReportScheduleResponse response(ReportSchedule schedule) {
        var next = schedule.isEnabled() ? quartz.nextRun(schedule.getId()).map(value -> value.atZone(BUSINESS_ZONE)).orElse(null) : null;
        return ReportScheduleResponse.from(schedule, next);
    }
    private ReportSchedule require(Long id) { return schedules.findById(id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.SCHEDULE_NOT_FOUND, "Report schedule not found.")); }
    private ApiException invalid(IllegalArgumentException exception) {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.SCHEDULE_VALIDATION_FAILED, exception.getMessage());
    }
}
