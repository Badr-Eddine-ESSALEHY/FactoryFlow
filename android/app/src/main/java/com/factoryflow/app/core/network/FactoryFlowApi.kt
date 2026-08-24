package com.factoryflow.app.core.network

import com.factoryflow.app.core.network.dto.*
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface FactoryFlowApi {
    @POST("api/auth/login") suspend fun login(@Body request: LoginRequest): LoginResponse
    @GET("api/users/me") suspend fun me(): UserDto
    @GET("api/dashboard") suspend fun dashboard(): DashboardDto
    @Multipart @POST("api/ocr/recognize") suspend fun recognizeImage(@Part image: okhttp3.MultipartBody.Part): OcrResultDto
    @GET("api/kpi-definitions") suspend fun kpiDefinitions(@Query("active") active: Boolean = true): List<KpiDefinitionDto>
    @POST("api/reports/analyze") suspend fun analyze(@Body request: AnalyzeReportRequest): AnalyzeReportResponse
    @POST("api/reports/drafts") suspend fun createDraft(@Body request: DraftReportRequest): ReportDto
    @POST("api/kpi-definitions/{id}/aliases") suspend fun approveAlias(@Path("id") id: Long, @Body request: ApproveAliasRequest): KpiDefinitionDto
    @PUT("api/reports/{id}/draft") suspend fun updateDraft(@Path("id") id: Long, @Body request: DraftReportRequest): ReportDto
    @GET("api/reports/{id}/draft") suspend fun draft(@Path("id") id: Long): ReportDto
    @POST("api/reports/{id}/draft/entries/{entryId}/add-kpi") suspend fun addDetectedKpi(
        @Path("id") id: Long,
        @Path("entryId") entryId: Long,
    ): ReportDto
    @POST("api/reports/{id}/draft/unrecognized-lines/ignore-safe") suspend fun ignoreSafeUnrecognizedLines(
        @Path("id") id: Long,
    ): ReportDto
    @PUT("api/reports/{id}/draft/unrecognized-lines/{lineId}") suspend fun resolveUnrecognizedLine(
        @Path("id") id: Long,
        @Path("lineId") lineId: Long,
        @Body request: UnknownLineResolutionRequest,
    ): ReportDto
    @DELETE("api/reports/{id}/draft/entries/{entryId}") suspend fun removeDraftEntry(
        @Path("id") id: Long,
        @Path("entryId") entryId: Long,
    ): ReportDto
    @DELETE("api/reports/{id}/draft") suspend fun deleteDraft(@Path("id") id: Long)
    @POST("api/reports/{id}/confirm") suspend fun confirm(@Path("id") id: Long, @Body request: ConfirmReportRequest): ReportDto
    @GET("api/reports") suspend fun reports(
        @Query("dateFrom") dateFrom: String? = null, @Query("dateTo") dateTo: String? = null,
        @Query("status") status: String? = null, @Query("page") page: Int = 0, @Query("size") size: Int = 30,
        @Query("sort") sort: String = "submittedAt,desc",
    ): PageDto<ReportSummaryDto>
    @GET("api/reports/{id}") suspend fun report(@Path("id") id: Long): ReportDto
    @GET("api/generated-reports") suspend fun generatedReports(
        @Query("format") format: String? = null, @Query("page") page: Int = 0, @Query("size") size: Int = 30,
        @Query("sort") sort: String = "generatedAt,desc",
    ): PageDto<GeneratedReportDto>
    @POST("api/generated-reports") suspend fun generateConsolidatedReport(@Body request: GenerateReportRequest): GeneratedReportDto
    @POST("api/generated-reports/individual") suspend fun generateIndividualReport(@Body request: IndividualReportExportRequest): GeneratedReportDto
    @GET("api/generated-reports/{id}") suspend fun generatedReport(@Path("id") id: Long): GeneratedReportDto
    @Streaming @GET("api/generated-reports/{id}/file") suspend fun generatedFile(@Path("id") id: Long): ResponseBody
    @GET("api/statistics") suspend fun statistics(
        @Query("kpiDefinitionId") kpiDefinitionId: Long? = null,
        @Query("dateFrom") dateFrom: String? = null, @Query("dateTo") dateTo: String? = null,
    ): StatisticsDto
    @GET("api/report-schedules") suspend fun schedules(): List<ReportScheduleDto>
    @GET("api/report-schedules/{id}") suspend fun schedule(@Path("id") id: Long): ReportScheduleDto
    @POST("api/report-schedules") suspend fun createSchedule(@Body request: ReportScheduleRequest): ReportScheduleDto
    @PUT("api/report-schedules/{id}") suspend fun updateSchedule(@Path("id") id: Long, @Body request: ReportScheduleRequest): ReportScheduleDto
    @PATCH("api/report-schedules/{id}/enabled") suspend fun setScheduleEnabled(@Path("id") id: Long, @Body request: ScheduleEnabledRequest): ReportScheduleDto
    @GET("api/report-schedules/{id}/runs") suspend fun scheduleRuns(
        @Path("id") id: Long, @Query("page") page: Int = 0, @Query("size") size: Int = 20,
        @Query("sort") sort: String = "startedAt,desc",
    ): PageDto<ScheduleRunDto>
    @GET("api/notifications") suspend fun notifications(): List<NotificationDto>
    @PATCH("api/notifications/{id}/read") suspend fun markNotificationRead(@Path("id") id: Long): NotificationDto
}
