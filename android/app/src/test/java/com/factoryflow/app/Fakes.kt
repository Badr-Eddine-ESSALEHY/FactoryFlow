package com.factoryflow.app

import com.factoryflow.app.core.data.*
import com.factoryflow.app.core.network.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository : AuthRepository {
    private val session = MutableStateFlow(false)
    override val authenticated: StateFlow<Boolean> = session
    var loginResult: Result<UserDto> = Result.success(UserDto(1, "Nadia Amrani", "nadia@factoryflow.local", true))
    var loginCalls = 0
    override suspend fun login(email: String, password: String): UserDto { loginCalls++; return loginResult.getOrThrow().also { session.value = true } }
    override suspend fun currentUser() = loginResult.getOrThrow()
    override fun hasSession() = false
    override fun logout() { session.value = false }
}

open class FakeReportsRepository : ReportsRepository {
    var analyzed: AnalyzeReportResponse? = null
    var created: ReportDto? = null
    var updated: ReportDto? = null
    var draftValue: ReportDto? = null
    var confirmed: ReportDto? = null
    var reportList = PageDto<ReportSummaryDto>()
    var reportValue: ReportDto? = null
    var definitionsValue = emptyList<KpiDefinitionDto>()
    override suspend fun definitions() = definitionsValue
    override suspend fun analyze(rawText: String) = checkNotNull(analyzed)
    override suspend fun createDraft(request: DraftReportRequest) = checkNotNull(created)
    override suspend fun updateDraft(id: Long, request: DraftReportRequest) = checkNotNull(updated ?: draftValue)
    override suspend fun draft(id: Long) = checkNotNull(draftValue)
    override suspend fun confirm(id: Long, request: ConfirmReportRequest) = checkNotNull(confirmed)
    override suspend fun reports(status: String?) = reportList
    override suspend fun report(id: Long) = checkNotNull(reportValue)
}

fun reportDto(status: String = "DRAFT", id: Long = 12) = ReportDto(
    id, status, "2026-08-12", "PASTE", "Température: 42", "2026-08-12T08:00:00Z", "2026-08-12T08:00:00Z",
    if (status == "CONFIRMED") "2026-08-12T08:10:00Z" else null, 0, SubmittedByDto(1, "Nadia Amrani"),
    listOf(ReportEntryDto(1, 10, "TEMP", "Température", "Température", "Température: 42", 42.toBigDecimal(), 42.toBigDecimal(), if (status == "CONFIRMED") 42.toBigDecimal() else null, 0.98.toBigDecimal(), false, "°C")),
)
