package com.factoryflow.app

import com.factoryflow.app.core.data.*
import com.factoryflow.app.core.network.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository : AuthRepository {
    private val session = MutableStateFlow(false)
    private val expired = MutableStateFlow(false)
    override val authenticated: StateFlow<Boolean> = session
    override val sessionExpired: StateFlow<Boolean> = expired
    var loginResult: Result<UserDto> = Result.success(UserDto(1, "Nadia Amrani", "nadia@factoryflow.local", true))
    var currentUserResult: Result<UserDto>? = null
    var hasStoredSession = false
    var loginCalls = 0
    var logoutCalls = 0
    override suspend fun login(email: String, password: String): UserDto { loginCalls++; return loginResult.getOrThrow().also { session.value = true } }
    override suspend fun currentUser() = (currentUserResult ?: loginResult).getOrThrow()
    override suspend fun hasSession() = hasStoredSession
    override fun logout() { logoutCalls++; session.value = false }
}

open class FakeReportsRepository : ReportsRepository {
    var analyzed: AnalyzeReportResponse? = null
    var analyzeFailure: Throwable? = null
    var created: ReportDto? = null
    var updated: ReportDto? = null
    var draftValue: ReportDto? = null
    var confirmed: ReportDto? = null
    var reportList = PageDto<ReportSummaryDto>()
    var reportValue: ReportDto? = null
    var definitionsValue = emptyList<KpiDefinitionDto>()
    var approvedAlias: KpiDefinitionDto? = null
    var lastAnalyzedRawText: String? = null
    var lastAnalyzedSource: String? = null
    val createdDraftRequests = mutableListOf<DraftReportRequest>()
    val approvedAliasCalls = mutableListOf<Pair<Long, String>>()
    val deletedDraftIds = mutableListOf<Long>()
    var lastConfirmRequest: ConfirmReportRequest? = null
    var confirmCalls = 0
    var addDetectedKpiCalls = mutableListOf<Pair<Long, Long>>()
    var addDetectedKpiResult: ReportDto? = null
    var ignoreSafeCalls = 0
    var ignoreSafeResult: ReportDto? = null
    val resolvedUnknownCalls = mutableListOf<Pair<Long, UnknownLineResolutionRequest>>()
    var resolvedUnknownResult: ReportDto? = null
    val removedEntryCalls = mutableListOf<Pair<Long, Long>>()
    var removedEntryResult: ReportDto? = null
    override suspend fun definitions() = definitionsValue
    override suspend fun analyze(rawText: String, source: String): AnalyzeReportResponse {
        lastAnalyzedRawText = rawText
        lastAnalyzedSource = source
        analyzeFailure?.let { throw it }
        return checkNotNull(analyzed)
    }
    override suspend fun createDraft(request: DraftReportRequest): ReportDto {
        createdDraftRequests += request
        return checkNotNull(created)
    }
    override suspend fun updateDraft(id: Long, request: DraftReportRequest) = checkNotNull(updated ?: draftValue)
    override suspend fun draft(id: Long) = checkNotNull(draftValue)
    override suspend fun addDetectedKpi(id: Long, entryId: Long): ReportDto {
        addDetectedKpiCalls += id to entryId
        return checkNotNull(addDetectedKpiResult)
    }
    override suspend fun ignoreSafeUnrecognizedLines(id: Long): ReportDto {
        ignoreSafeCalls++
        return checkNotNull(ignoreSafeResult)
    }
    override suspend fun resolveUnrecognizedLine(id: Long, request: UnknownLineResolutionRequest): ReportDto {
        resolvedUnknownCalls += id to request
        return checkNotNull(resolvedUnknownResult ?: draftValue)
    }
    override suspend fun removeDraftEntry(id: Long, entryId: Long): ReportDto {
        removedEntryCalls += id to entryId
        return checkNotNull(removedEntryResult ?: draftValue)
    }
    override suspend fun deleteDraft(id: Long) { deletedDraftIds += id }
    override suspend fun approveAlias(kpiDefinitionId: Long, alias: String): KpiDefinitionDto {
        approvedAliasCalls += kpiDefinitionId to alias
        return approvedAlias
            ?: definitionsValue.firstOrNull { it.id == kpiDefinitionId }
            ?: error("No KPI definition configured for alias approval: $kpiDefinitionId")
    }
    override suspend fun confirm(id: Long, request: ConfirmReportRequest): ReportDto {
        confirmCalls++
        lastConfirmRequest = request
        return checkNotNull(confirmed)
    }
    override suspend fun reports(status: String?) = reportList
    override suspend fun report(id: Long) = checkNotNull(reportValue)
}

class FakeGeneratedReportsRepository : GeneratedReportsRepository {
    var listValue = PageDto<GeneratedReportDto>()
    var detailValue: GeneratedReportDto? = null
    var generatedValue: GeneratedReportDto? = null
    var downloadedFile: java.io.File? = null
    val generationRequests = mutableListOf<GenerateReportRequest>()
    val individualGenerationRequests = mutableListOf<IndividualReportExportRequest>()
    val downloadedReports = mutableListOf<GeneratedReportDto>()

    override suspend fun list() = listValue
    override suspend fun detail(id: Long) = checkNotNull(detailValue)
    override suspend fun generateConsolidated(request: GenerateReportRequest): GeneratedReportDto {
        generationRequests += request
        return checkNotNull(generatedValue)
    }
    override suspend fun generateIndividual(request: IndividualReportExportRequest): GeneratedReportDto {
        individualGenerationRequests += request
        return checkNotNull(generatedValue)
    }
    override suspend fun download(report: GeneratedReportDto): java.io.File {
        downloadedReports += report
        return checkNotNull(downloadedFile)
    }
}

fun reportDto(status: String = "DRAFT", id: Long = 12) = ReportDto(
    id, status, "2026-08-12", "PASTE", "Température: 42", "2026-08-12T08:00:00Z", "2026-08-12T08:00:00Z",
    if (status == "CONFIRMED") "2026-08-12T08:10:00Z" else null, 0, SubmittedByDto(1, "Nadia Amrani"),
    listOf(ReportEntryDto(1, 10, "TEMP", "Température", "Température", "Température: 42", 42.toBigDecimal(), 42.toBigDecimal(), if (status == "CONFIRMED") 42.toBigDecimal() else null, 0.98.toBigDecimal(), false, "°C")),
)
