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
data class OcrResultDto(
    val fullText: String,
    val lines: List<OcrLineDto> = emptyList(),
    val confidence: BigDecimal?,
    val engine: String,
    val processingTimeMs: Long,
    val warnings: List<String> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class OcrLineDto(val text: String, val confidence: BigDecimal?, val boundingBox: OcrBoundingBoxDto?)

@JsonClass(generateAdapter = false)
data class OcrBoundingBoxDto(val left: Int, val top: Int, val right: Int, val bottom: Int)

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
    val todayGeneratedDocumentCount: Long = 0,
    val todayConfirmedMissingValueCount: Long,
    val todayHasConfirmedReport: Boolean,
    val activityTrend: List<DashboardActivityDto> = emptyList(),
    val latestKpis: List<LatestKpiDto> = emptyList(),
    val recentReports: List<RecentReportDto> = emptyList(),
    val recentGeneratedReports: List<RecentGeneratedReportDto> = emptyList(),
    val upcomingSchedule: UpcomingScheduleDto? = null,
)
@JsonClass(generateAdapter = false)
data class DashboardActivityDto(val date: String, val confirmedReportCount: Long, val missingValueCount: Long)


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
data class ApproveAliasRequest(val alias: String)

@JsonClass(generateAdapter = false)
data class AnalyzeReportRequest(val rawText: String, val source: String = "PASTE")

@JsonClass(generateAdapter = false)
data class AnalyzeReportResponse(
    val source: String, val rawText: String, val recognizedCount: Int, val readyCount: Int = 0,
    val attentionCount: Int = 0, val missingCount: Int = 0, val unresolvedCount: Int = 0,
    val needsReviewCount: Int, val unrecognizedCount: Int, val entries: List<ParsedEntryDto> = emptyList(),
    val ignoredLines: List<IgnoredSourceLineDto> = emptyList(),
    val unrecognizedLines: List<ParsedUnknownLineDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class ParsedEntryDto(
    val candidateId: String, val kpiDefinitionId: Long?, val kpiCode: String?, val kpiDisplayName: String?,
    val sourceLabel: String?, val sourceLine: String, val extractedValue: BigDecimal?, val capturedUnit: String?,
    val expectedUnit: String?, val confidenceScore: BigDecimal?, val confidenceLevel: String?,
    val matchMethod: String? = null, val reviewState: String? = null,
    val suggestions: List<KpiSuggestionDto> = emptyList(),
    val warnings: List<ParserWarningDto> = emptyList(),
    val secondaryExtractedValue: BigDecimal? = null,
    val secondaryUnit: String? = null,
)

@JsonClass(generateAdapter = false)
data class ParserWarningDto(val code: String, val message: String, val severity: String)

@JsonClass(generateAdapter = false)
data class ParsedUnknownLineDto(val lineId: String, val sourceLine: String, val reason: String, val sourceLabel: String? = null, val suggestions: List<KpiSuggestionDto> = emptyList())

@JsonClass(generateAdapter = false)
data class KpiSuggestionDto(
    val kpiDefinitionId: Long, val kpiCode: String, val displayName: String, val unit: String?,
    val score: BigDecimal, val matchMethod: String, val strength: String = "WEAK",
)

@JsonClass(generateAdapter = false)
data class IgnoredSourceLineDto(val lineId: String, val sourceLine: String, val classification: String)

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
    val suggestedKpiDefinitionId: Long? = null, val suggestionScore: BigDecimal? = null,
    val suggestionStrength: String? = null, val suggestionMatchMethod: String? = null,
    val secondaryExtractedValue: BigDecimal? = null, val secondaryCurrentValue: BigDecimal? = null,
    val secondaryUnit: String? = null,
)

@JsonClass(generateAdapter = false)
data class DraftUnknownLineRequest(
    val sourceLine: String,
    val resolution: String = "UNRESOLVED",
    val resolvedKpiDefinitionId: Long? = null,
    val kind: String = "KPI_LIKE",
    val classificationReason: String = "UNCLASSIFIED",
    val safeToIgnore: Boolean = false,
)

@JsonClass(generateAdapter = false)
data class ConfirmReportRequest(
    val entries: List<ConfirmationEntryRequest>,
    val unrecognizedLineResolutions: List<UnknownLineResolutionRequest>,
)

@JsonClass(generateAdapter = false)
data class ConfirmationEntryRequest(
    val kpiDefinitionId: Long,
    val finalValue: BigDecimal?,
    val secondaryFinalValue: BigDecimal? = null,
    val entryId: Long = -1,
)

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
    val suggestedKpiDefinitionId: Long? = null, val suggestedKpiDisplayName: String? = null,
    val suggestedKpiUnit: String? = null, val suggestionScore: BigDecimal? = null,
    val suggestionStrength: String? = null, val suggestionMatchMethod: String? = null,
    val secondaryExtractedValue: BigDecimal? = null, val secondaryCurrentValue: BigDecimal? = null,
    val secondaryFinalValue: BigDecimal? = null, val secondaryUnit: String? = null,
)

@JsonClass(generateAdapter = false)
data class UnknownLineDto(
    val id: Long,
    val sourceLine: String,
    val resolution: String,
    val resolvedKpiDefinitionId: Long?,
    val kind: String = "KPI_LIKE",
    val classificationReason: String = "UNCLASSIFIED",
    val safeToIgnore: Boolean = false,
)

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
data class GenerateReportRequest(
    val type: String,
    val format: String,
    val periodStart: String,
    val periodEnd: String,
)

@JsonClass(generateAdapter = false)
data class IndividualReportExportRequest(
    val reportId: Long,
    val format: String,
)

@JsonClass(generateAdapter = false)
data class StatisticsDto(val dateFrom: String, val dateTo: String, val kpis: List<KpiStatisticsDto> = emptyList())

@JsonClass(generateAdapter = false)
data class KpiStatisticsDto(
    val kpiDefinitionId: Long, val code: String, val displayName: String, val unit: String?,
    val latest: BigDecimal?, val minimum: BigDecimal?, val maximum: BigDecimal?, val average: BigDecimal?,
    val sampleCount: Long, val reportCount: Long, val missingValueCount: Long, val points: List<StatisticsPointDto> = emptyList(),
    val range: BigDecimal? = null, val standardDeviation: BigDecimal? = null, val periodDelta: BigDecimal? = null,
    val trend: String = "INSUFFICIENT_DATA", val first: BigDecimal? = null, val last: BigDecimal? = null,
    val validCount: Long = 0, val completenessRate: BigDecimal? = null,
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

@JsonClass(generateAdapter = false)
data class NotificationDto(
    val id: Long, val type: String, val title: String, val message: String,
    val relatedReportId: Long?, val relatedGeneratedReportId: Long?, val createdAt: String, val readAt: String?,
)
