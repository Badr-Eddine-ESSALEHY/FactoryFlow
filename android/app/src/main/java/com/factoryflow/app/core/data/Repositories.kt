package com.factoryflow.app.core.data

import android.content.Context
import com.factoryflow.app.core.auth.SecureTokenStore
import com.factoryflow.app.core.network.ApiExecutor
import com.factoryflow.app.core.network.FactoryFlowApi
import com.factoryflow.app.core.network.dto.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun login(email: String, password: String): UserDto
    suspend fun currentUser(): UserDto
    fun hasSession(): Boolean
    val authenticated: StateFlow<Boolean>
    fun logout()
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: FactoryFlowApi,
    private val executor: ApiExecutor,
    private val tokens: SecureTokenStore,
) : AuthRepository {
    override val authenticated: StateFlow<Boolean> = tokens.authenticated
    override suspend fun login(email: String, password: String): UserDto = executor.execute {
        api.login(LoginRequest(email.trim(), password)).also { tokens.save(it.accessToken) }.user
    }
    override suspend fun currentUser(): UserDto = executor.execute { api.me() }
    override fun hasSession() = tokens.accessToken() != null
    override fun logout() = tokens.clear()
}

interface DashboardRepository { suspend fun dashboard(): DashboardDto }
class DefaultDashboardRepository @Inject constructor(private val api: FactoryFlowApi, private val executor: ApiExecutor) : DashboardRepository {
    override suspend fun dashboard() = executor.execute { api.dashboard() }
}

interface ReportsRepository {
    suspend fun definitions(): List<KpiDefinitionDto>
    suspend fun analyze(rawText: String, source: String = "PASTE"): AnalyzeReportResponse
    suspend fun createDraft(request: DraftReportRequest): ReportDto
    suspend fun updateDraft(id: Long, request: DraftReportRequest): ReportDto
    suspend fun draft(id: Long): ReportDto
    suspend fun deleteDraft(id: Long)
    suspend fun approveAlias(kpiDefinitionId: Long, alias: String): KpiDefinitionDto
    suspend fun confirm(id: Long, request: ConfirmReportRequest): ReportDto
    suspend fun reports(status: String? = null): PageDto<ReportSummaryDto>
    suspend fun report(id: Long): ReportDto
}

class DefaultReportsRepository @Inject constructor(private val api: FactoryFlowApi, private val executor: ApiExecutor) : ReportsRepository {
    override suspend fun definitions() = executor.execute { api.kpiDefinitions() }
    override suspend fun analyze(rawText: String, source: String) = executor.execute { api.analyze(AnalyzeReportRequest(rawText, source)) }
    override suspend fun createDraft(request: DraftReportRequest) = executor.execute { api.createDraft(request) }
    override suspend fun updateDraft(id: Long, request: DraftReportRequest) = executor.execute { api.updateDraft(id, request) }
    override suspend fun draft(id: Long) = executor.execute { api.draft(id) }
    override suspend fun deleteDraft(id: Long) = executor.execute { api.deleteDraft(id) }
    override suspend fun approveAlias(kpiDefinitionId: Long, alias: String) = executor.execute { api.approveAlias(kpiDefinitionId, ApproveAliasRequest(alias)) }
    override suspend fun confirm(id: Long, request: ConfirmReportRequest) = executor.execute { api.confirm(id, request) }
    override suspend fun reports(status: String?) = executor.execute { api.reports(status = status) }
    override suspend fun report(id: Long) = executor.execute { api.report(id) }
}

interface GeneratedReportsRepository {
    suspend fun list(): PageDto<GeneratedReportDto>
    suspend fun detail(id: Long): GeneratedReportDto
    suspend fun generate(request: GenerateReportRequest): GeneratedReportDto
    suspend fun download(report: GeneratedReportDto): File
}

class DefaultGeneratedReportsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: FactoryFlowApi,
    private val executor: ApiExecutor,
) : GeneratedReportsRepository {
    override suspend fun list() = executor.execute { api.generatedReports() }
    override suspend fun detail(id: Long) = executor.execute { api.generatedReport(id) }
    override suspend fun generate(request: GenerateReportRequest) = executor.execute { api.generateReport(request) }
    override suspend fun download(report: GeneratedReportDto): File = executor.execute {
        val directory = File(context.cacheDir, "generated-reports").apply { mkdirs() }
        directory.listFiles()?.sortedByDescending(File::lastModified)?.drop(12)?.forEach(File::delete)
        val safeName = report.fileName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(100)
            .ifBlank { "factoryflow-${report.id}.${if (report.format == "PDF") "pdf" else "xlsx"}" }
        val target = File(directory, safeName)
        api.generatedFile(report.id).use { body -> target.outputStream().use { output -> body.byteStream().copyTo(output) } }
        target
    }
}

interface StatisticsRepository { suspend fun statistics(kpiId: Long?, dateFrom: String, dateTo: String): StatisticsDto }
class DefaultStatisticsRepository @Inject constructor(private val api: FactoryFlowApi, private val executor: ApiExecutor) : StatisticsRepository {
    override suspend fun statistics(kpiId: Long?, dateFrom: String, dateTo: String) = executor.execute { api.statistics(kpiId, dateFrom, dateTo) }
}

interface SchedulesRepository {
    suspend fun list(): List<ReportScheduleDto>
    suspend fun detail(id: Long): ReportScheduleDto
    suspend fun create(request: ReportScheduleRequest): ReportScheduleDto
    suspend fun update(id: Long, request: ReportScheduleRequest): ReportScheduleDto
    suspend fun setEnabled(id: Long, enabled: Boolean): ReportScheduleDto
    suspend fun runs(id: Long): PageDto<ScheduleRunDto>
}

class DefaultSchedulesRepository @Inject constructor(private val api: FactoryFlowApi, private val executor: ApiExecutor) : SchedulesRepository {
    override suspend fun list() = executor.execute { api.schedules() }
    override suspend fun detail(id: Long) = executor.execute { api.schedule(id) }
    override suspend fun create(request: ReportScheduleRequest) = executor.execute { api.createSchedule(request) }
    override suspend fun update(id: Long, request: ReportScheduleRequest) = executor.execute { api.updateSchedule(id, request) }
    override suspend fun setEnabled(id: Long, enabled: Boolean) = executor.execute { api.setScheduleEnabled(id, ScheduleEnabledRequest(enabled)) }
    override suspend fun runs(id: Long) = executor.execute { api.scheduleRuns(id) }
}
