package com.factoryflow.app.core.network.dto

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = false)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = false)
data class LoginResponse(val accessToken: String, val tokenType: String, val expiresInSeconds: Long, val user: UserDto)

@JsonClass(generateAdapter = false)
data class UserDto(val id: Long, val name: String, val email: String, val active: Boolean)

@JsonClass(generateAdapter = false)
data class PageDto<T>(
    val content: List<T> = emptyList(), val page: Int = 0, val size: Int = 0,
    val totalElements: Long = 0, val totalPages: Int = 0, val first: Boolean = true, val last: Boolean = true,
)

@JsonClass(generateAdapter = false)
data class DashboardDto(
    val businessDate: String,
    val todayConfirmedReportCount: Long,
    val todayDraftOrPendingReportCount: Long,
    val todayConfirmedMissingValueCount: Long,
    val todayHasConfirmedReport: Boolean,
    val latestKpis: List<LatestKpiDto> = emptyList(),
    val recentReports: List<RecentReportDto> = emptyList(),
    val recentGeneratedReports: List<RecentGeneratedReportDto> = emptyList(),
    val upcomingSchedule: UpcomingScheduleDto? = null,
)

@JsonClass(generateAdapter = false)
data class LatestKpiDto(
    val kpiDefinitionId: Long, val code: String, val displayName: String, val unit: String?,
    val value: BigDecimal, val effectiveDate: String, val reportId: Long, val confirmedAt: String,
)

@JsonClass(generateAdapter = false)
data class RecentReportDto(
    val id: Long, val effectiveDate: String, val status: String, val submittedAt: String,
    val confirmedAt: String?, val submittedBy: String,
)

@JsonClass(generateAdapter = false)
data class RecentGeneratedReportDto(
    val id: Long, val type: String, val format: String, val periodStart: String, val periodEnd: String, val generatedAt: String,
)

@JsonClass(generateAdapter = false)
data class UpcomingScheduleDto(
    val id: Long, val type: String, val nextRunAt: String?, val generateExcel: Boolean, val generatePdf: Boolean, val emailEnabled: Boolean,
)

@JsonClass(generateAdapter = false)
data class KpiDefinitionDto(
    val id: Long, val code: String, val displayName: String, val category: String?, val unit: String?,
    val plausibleMin: BigDecimal?, val plausibleMax: BigDecimal?, val aliases: List<String> = emptyList(), val active: Boolean,
)

@JsonClass(generateAdapter = false)
data class AnalyzeReportRequest(val rawText: String, val source: String = "PASTE")

@JsonClass(generateAdapter = false)
data class AnalyzeReportResponse(
    val source: String, val rawText: String, val recognizedCount: Int, val needsReviewCount: Int,
    val unrecognizedCount: Int, val entries: List<ParsedEntryDto> = emptyList(),
    val unrecognizedLines: List<ParsedUnknownLineDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class ParsedEntryDto(
    val candidateId: String, val kpiDefinitionId: Long?, val kpiCode: String?, val kpiDisplayName: String?,
    val sourceLabel: String?, val sourceLine: String, val extractedValue: BigDecimal?, val capturedUnit: String?,
    val expectedUnit: String?, val confidenceScore: BigDecimal?, val confidenceLevel: String?,
    val warnings: List<ParserWarningDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class ParserWarningDto(val code: String, val message: String, val severity: String)

@JsonClass(generateAdapter = false)
data class ParsedUnknownLineDto(val lineId: String, val sourceLine: String, val reason: String)

@JsonClass(generateAdapter = false)
data class DraftReportRequest(
    val effectiveDate: String, val source: String, val rawText: String?,
    val entries: List<DraftEntryRequest> = emptyList(), val unrecognizedLines: List<DraftUnknownLineRequest> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class DraftEntryRequest(
    val kpiDefinitionId: Long?, val sourceLabel: String?, val sourceLine: String?,
    val extractedValue: BigDecimal?, val currentValue: BigDecimal?, val confidenceScore: BigDecimal?,
    val editedByUser: Boolean, val capturedUnit: String?, val warnings: Set<String> = emptySet(),
)

@JsonClass(generateAdapter = false)
data class DraftUnknownLineRequest(val sourceLine: String, val resolution: String = "UNRESOLVED", val resolvedKpiDefinitionId: Long? = null)

@JsonClass(generateAdapter = false)
data class ConfirmReportRequest(
    val entries: List<ConfirmationEntryRequest>,
    val unrecognizedLineResolutions: List<UnknownLineResolutionRequest>,
)

@JsonClass(generateAdapter = false)
data class ConfirmationEntryRequest(val kpiDefinitionId: Long, val finalValue: BigDecimal?)

@JsonClass(generateAdapter = false)
data class UnknownLineResolutionRequest(val lineId: Long, val resolution: String, val resolvedKpiDefinitionId: Long? = null)

@JsonClass(generateAdapter = false)
data class ReportDto(
    val id: Long, val status: String, val effectiveDate: String, val source: String, val rawText: String?,
    val submittedAt: String, val updatedAt: String, val confirmedAt: String?, val version: Long,
    val submittedBy: SubmittedByDto, val entries: List<ReportEntryDto> = emptyList(),
    val unrecognizedLines: List<UnknownLineDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class SubmittedByDto(val id: Long, val name: String)

@JsonClass(generateAdapter = false)
data class ReportEntryDto(
    val id: Long, val kpiDefinitionId: Long?, val kpiCode: String?, val kpiDisplayName: String?,
    val sourceLabel: String?, val sourceLine: String?, val extractedValue: BigDecimal?, val currentValue: BigDecimal?,
    val finalValue: BigDecimal?, val confidenceScore: BigDecimal?, val editedByUser: Boolean,
    val capturedUnit: String?, val warnings: Set<String> = emptySet(),
)

@JsonClass(generateAdapter = false)
data class UnknownLineDto(val id: Long, val sourceLine: String, val resolution: String, val resolvedKpiDefinitionId: Long?)

@JsonClass(generateAdapter = false)
data class ReportSummaryDto(
    val id: Long, val status: String, val source: String, val effectiveDate: String, val submittedAt: String,
    val confirmedAt: String?, val submittedBy: SubmittedByDto, val kpiCount: Int, val warningCount: Int,
)

@JsonClass(generateAdapter = false)
data class GeneratedReportDto(
    val id: Long, val type: String, val format: String, val periodStart: String, val periodEnd: String,
    val origin: String, val generationStatus: String, val emailDeliveryStatus: String, val version: Int,
    val generatedAt: String, val fileName: String, val generatedBy: Long?, val regeneratedFromId: Long?,
    val scheduleId: Long?, val sourceReportIds: List<Long> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class StatisticsDto(val dateFrom: String, val dateTo: String, val kpis: List<KpiStatisticsDto> = emptyList())

@JsonClass(generateAdapter = false)
data class KpiStatisticsDto(
    val kpiDefinitionId: Long, val code: String, val displayName: String, val unit: String?,
    val latest: BigDecimal?, val minimum: BigDecimal?, val maximum: BigDecimal?, val average: BigDecimal?,
    val sampleCount: Long, val reportCount: Long, val missingValueCount: Long, val points: List<StatisticsPointDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class StatisticsPointDto(val effectiveDate: String, val reportId: Long, val value: BigDecimal)

@JsonClass(generateAdapter = false)
data class ReportScheduleRequest(
    val type: String, val time: String, val dayOfWeek: String?, val timezone: String = "Africa/Casablanca",
    val generateExcel: Boolean, val generatePdf: Boolean, val emailEnabled: Boolean,
    val recipients: List<String>, val enabled: Boolean,
)

@JsonClass(generateAdapter = false)
data class ReportScheduleDto(
    val id: Long, val type: String, val time: String, val dayOfWeek: String?, val timezone: String,
    val enabled: Boolean, val generateExcel: Boolean, val generatePdf: Boolean, val emailEnabled: Boolean,
    val recipients: List<String> = emptyList(), val nextRunAt: String?, val version: Long,
)

@JsonClass(generateAdapter = false)
data class ScheduleEnabledRequest(val enabled: Boolean)

@JsonClass(generateAdapter = false)
data class ScheduleRunDto(
    val id: Long, val format: String, val periodStart: String, val periodEnd: String,
    val scheduledFor: String, val startedAt: String?, val finishedAt: String?, val status: String,
    val generatedReportId: Long?, val emailDeliveryStatus: String?, val errorCode: String?, val errorMessage: String?,
)
