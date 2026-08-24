package com.factoryflow.app.feature.review

import androidx.lifecycle.SavedStateHandle
import com.factoryflow.app.*
import com.factoryflow.app.core.network.dto.KpiDefinitionDto
import com.factoryflow.app.core.network.dto.ReportEntryDto
import com.factoryflow.app.core.network.dto.UnknownLineDto
import java.math.BigDecimal
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `user correction remains authoritative through confirmation`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply { draftValue = reportDto(); updated = reportDto(); confirmed = reportDto("CONFIRMED") }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle(); viewModel.edit(1, "43,5"); var confirmedId: Long? = null; viewModel.confirm { confirmedId = it }; advanceUntilIdle()
        assertEquals(12L, confirmedId); assertFalse(viewModel.state.value.confirming)
        assertEquals(1L, repository.lastConfirmRequest!!.entries.single().entryId)
    }

    @Test fun `composite percentage remains linked and editable through confirmation`() = runTest(dispatcher.dispatcher) {
        val compositeDraft = reportDto().copy(entries = reportDto().entries.map {
            it.copy(secondaryExtractedValue = BigDecimal("77"), secondaryCurrentValue = BigDecimal("77"), secondaryUnit = "%")
        })
        val repository = FakeReportsRepository().apply {
            draftValue = compositeDraft
            updated = compositeDraft
            confirmed = compositeDraft.copy(status = "CONFIRMED")
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()

        viewModel.editSecondary(1, "78")
        viewModel.confirm { }
        advanceUntilIdle()

        assertEquals(BigDecimal("78"), repository.lastConfirmRequest!!.entries.single().secondaryFinalValue)
        assertEquals("77", viewModel.state.value.entries.single().secondaryExtractedValue)
    }

    @Test fun `explicitly missing optional value does not block confirmation`() {
        val missing = ReviewEntry(
            id = 1,
            kpiDefinitionId = 10,
            displayName = "Vrac",
            value = "",
            extractedValue = null,
            unit = "t",
            confidenceScore = "100",
            warnings = setOf("MISSING_VALUE"),
            sourceLabel = "Vrac",
            sourceLine = "Vrac: ---",
            edited = false,
            suggestedKpiDefinitionId = null,
            suggestedKpiDisplayName = null,
            suggestedKpiUnit = null,
            suggestionScore = null,
        )
        assertTrue(ReviewUiState(loading = false, report = reportDto(), entries = listOf(missing)).canConfirm)
    }

    @Test fun repeatedConfirmationTapsCreateOneAuthoritativeOutcome() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply {
            draftValue = reportDto()
            confirmed = reportDto("CONFIRMED")
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()

        viewModel.confirm { }
        viewModel.confirm { }
        advanceUntilIdle()

        assertEquals(1, repository.confirmCalls)
    }

    @Test fun `unresolved mutations retain selected tab`() = runTest(dispatcher.dispatcher) {
        val report = reportDto().copy(
            entries = listOf(unresolvedEntry()),
            unrecognizedLines = listOf(UnknownLineDto(8, "Message", "UNRESOLVED", null)),
        )
        val repository = FakeReportsRepository().apply { draftValue = report }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()

        viewModel.selectTab(ReviewState.UNRESOLVED)
        viewModel.resolve(8, "IGNORED")

        assertEquals(ReviewState.UNRESOLVED, viewModel.state.value.selectedTab)
        assertEquals("IGNORED", viewModel.state.value.unknownLines.single().resolution)
    }

    @Test fun `ignore one preserves its source line and unresolved tab`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply {
            draftValue = reportDto().copy(entries = listOf(unresolvedEntry()))
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()
        viewModel.selectTab(ReviewState.UNRESOLVED)

        viewModel.remove(2)

        assertEquals(ReviewState.UNRESOLVED, viewModel.state.value.selectedTab)
        assertTrue(viewModel.state.value.entries.isEmpty())
        assertEquals("Enzyme 3: 92417313", viewModel.state.value.unknownLines.single().sourceLine)
        assertEquals("IGNORED", viewModel.state.value.unknownLines.single().resolution)
    }

    @Test fun `validate moves an attention value to ready without changing tab`() = runTest(dispatcher.dispatcher) {
        val report = reportDto().copy(entries = listOf(
            reportDto().entries.single().copy(
                extractedValue = BigDecimal("30.197"),
                currentValue = BigDecimal("30.197"),
                warnings = setOf("AMBIGUOUS_NUMBER"),
            ),
        ))
        val repository = FakeReportsRepository().apply { draftValue = report }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canConfirm)
        viewModel.validate(1)

        assertEquals(ReviewState.ATTENTION, viewModel.state.value.selectedTab)
        assertEquals(ReviewState.READY, viewModel.state.value.entries.single().reviewState)
        assertEquals("30.197", viewModel.state.value.entries.single().value)
        assertTrue(viewModel.state.value.canConfirm)
    }

    @Test fun `inline add reuses backend result refreshes catalog and keeps unresolved tab`() = runTest(dispatcher.dispatcher) {
        val unresolved = reportDto().copy(entries = listOf(unresolvedEntry()))
        val createdDefinition = kpiDefinition(77, "ENZYME_3", "Enzyme 3")
        val resolved = unresolved.copy(entries = listOf(unresolved.entries.single().copy(
            kpiDefinitionId = 77,
            kpiCode = "ENZYME_3",
            kpiDisplayName = "Enzyme 3",
            warnings = emptySet(),
        )))
        val repository = FakeReportsRepository().apply {
            draftValue = unresolved
            definitionsValue = listOf(createdDefinition)
            addDetectedKpiResult = resolved
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()
        viewModel.selectTab(ReviewState.UNRESOLVED)

        viewModel.addDetectedKpi(2)
        advanceUntilIdle()

        assertEquals(listOf(12L to 2L), repository.addDetectedKpiCalls)
        assertEquals(ReviewState.UNRESOLVED, viewModel.state.value.selectedTab)
        assertEquals(77L, viewModel.state.value.entries.single().kpiDefinitionId)
        assertEquals(createdDefinition, viewModel.state.value.definitions.single())
    }

    @Test fun `ignore all delegates safe classification to backend and retains tab`() = runTest(dispatcher.dispatcher) {
        val unresolved = reportDto().copy(
            unrecognizedLines = listOf(UnknownLineDto(8, "Message", "UNRESOLVED", null)),
        )
        val repository = FakeReportsRepository().apply {
            draftValue = unresolved
            ignoreSafeResult = unresolved.copy(
                unrecognizedLines = listOf(UnknownLineDto(8, "Message", "IGNORED", null)),
            )
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()
        viewModel.selectTab(ReviewState.UNRESOLVED)

        viewModel.ignoreSafeUnrecognizedLines()
        advanceUntilIdle()

        assertEquals(1, repository.ignoreSafeCalls)
        assertEquals(ReviewState.UNRESOLVED, viewModel.state.value.selectedTab)
        assertEquals("IGNORED", viewModel.state.value.unknownLines.single().resolution)
    }

    @Test fun `existing suggestion remains assignable without creating a KPI`() = runTest(dispatcher.dispatcher) {
        val suggested = kpiDefinition(10, "VRAC", "Vrac", "t")
        val report = reportDto().copy(entries = listOf(unresolvedEntry(
            suggestedKpiDefinitionId = 10,
            suggestedKpiDisplayName = "Vrac",
        )))
        val repository = FakeReportsRepository().apply {
            draftValue = report
            definitionsValue = listOf(suggested)
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()
        viewModel.selectTab(ReviewState.UNRESOLVED)

        viewModel.assignEntry(2, suggested)

        assertEquals(ReviewState.UNRESOLVED, viewModel.state.value.selectedTab)
        assertEquals(10L, viewModel.state.value.entries.single().kpiDefinitionId)
        assertTrue(repository.addDetectedKpiCalls.isEmpty())
    }

    @Test fun `duplicate observation remains blocking until explicitly validated`() {
        val duplicate = reviewEntry(
            value = "118.2",
            warnings = setOf("DUPLICATE_KPI"),
        )

        assertEquals(ReviewPresentationType.ATTENTION_DUPLICATE, duplicate.presentationType)
        assertTrue(duplicate.canValidate)
        assertEquals(1, ReviewUiState(loading = false, report = reportDto(), entries = listOf(duplicate)).blockingCount)
    }

    @Test fun `typing a missing value creates a corrected blocking state until validation`() {
        val missing = reviewEntry(value = "", warnings = setOf("MISSING_VALUE"))
        val corrected = missing.copy(value = "2255", edited = true)

        assertEquals(ReviewPresentationType.MISSING, missing.presentationType)
        assertFalse(missing.blocksConfirmation)
        assertEquals(ReviewPresentationType.MISSING_CORRECTED, corrected.presentationType)
        assertTrue(corrected.blocksConfirmation)
        assertTrue(corrected.canValidate)
    }

    @Test fun `weak suggestion preserves the new KPI presentation path and numeric score`() {
        val weak = reviewEntry(
            kpiDefinitionId = null,
            value = "1882312",
            warnings = setOf("UNKNOWN_KPI"),
            suggestedKpiDefinitionId = 20,
            suggestedKpiDisplayName = "Choline",
            suggestionScore = "50",
            suggestionStrength = "WEAK",
        )

        assertEquals(ReviewPresentationType.UNRESOLVED_WEAK_SUGGESTION, weak.presentationType)
        assertEquals("50", weak.suggestionScore)
        assertTrue(weak.blocksConfirmation)
    }

    @Test fun `only pending safe noise is eligible for the safe noise group`() {
        val noise = ReviewUnknown(
            id = 8,
            sourceLine = "15:01",
            resolution = "UNRESOLVED",
            resolvedKpiDefinitionId = null,
            kind = "SAFE_NOISE",
            classificationReason = "WHATSAPP_METADATA",
            safeToIgnore = true,
        )
        val unresolvedKpi = noise.copy(id = 9, sourceLine = "Methionine: 1882312", kind = "KPI_LIKE", safeToIgnore = false)

        assertEquals(ReviewPresentationType.SAFE_NOISE_PENDING, noise.presentationType)
        assertEquals(ReviewPresentationType.UNRESOLVED_NEW, unresolvedKpi.presentationType)
        assertEquals(2, ReviewUiState(unknownLines = listOf(noise, unresolvedKpi)).blockingCount)
    }

    @Test fun `bulk ignore eligibility follows the safe flag across the Non tab`() {
        val safeNoise = listOf("PF", "Aymane", "Lokbiche").mapIndexed { index, sourceLine ->
            ReviewUnknown(
                id = index.toLong(),
                sourceLine = sourceLine,
                resolution = "UNRESOLVED",
                resolvedKpiDefinitionId = null,
                kind = if (index == 0) "SAFE_NOISE" else "KPI_LIKE",
                classificationReason = "NO_VALUE_SEPARATOR",
                safeToIgnore = true,
            )
        }
        val meaningfulEntries = listOf("Methionine", "Terminé 8", "Eau").mapIndexed { index, sourceLine ->
            ReviewUnknown(
                id = (index + 10).toLong(),
                sourceLine = sourceLine,
                resolution = "UNRESOLVED",
                resolvedKpiDefinitionId = null,
                kind = "KPI_LIKE",
                classificationReason = "NO_VALUE_SEPARATOR",
                safeToIgnore = false,
            )
        }

        val state = ReviewUiState(unknownLines = safeNoise + meaningfulEntries)

        assertEquals(3, state.bulkIgnorableUnknownCount)
    }

    private fun reviewEntry(
        kpiDefinitionId: Long? = 10,
        value: String,
        warnings: Set<String>,
        suggestedKpiDefinitionId: Long? = null,
        suggestedKpiDisplayName: String? = null,
        suggestionScore: String? = null,
        suggestionStrength: String? = null,
    ) = ReviewEntry(
        id = 1,
        kpiDefinitionId = kpiDefinitionId,
        displayName = "Vrac",
        value = value,
        extractedValue = value.ifBlank { null },
        unit = "t",
        confidenceScore = "80",
        warnings = warnings,
        sourceLabel = "Vrac",
        sourceLine = "Vrac: $value",
        edited = false,
        suggestedKpiDefinitionId = suggestedKpiDefinitionId,
        suggestedKpiDisplayName = suggestedKpiDisplayName,
        suggestedKpiUnit = null,
        suggestionScore = suggestionScore,
        suggestionStrength = suggestionStrength,
    )

    private fun unresolvedEntry(
        suggestedKpiDefinitionId: Long? = null,
        suggestedKpiDisplayName: String? = null,
    ) = ReportEntryDto(
        id = 2,
        kpiDefinitionId = null,
        kpiCode = null,
        kpiDisplayName = null,
        sourceLabel = "Enzyme 3",
        sourceLine = "Enzyme 3: 92417313",
        extractedValue = BigDecimal("92417313"),
        currentValue = BigDecimal("92417313"),
        finalValue = null,
        confidenceScore = BigDecimal("0.4"),
        editedByUser = false,
        capturedUnit = null,
        warnings = setOf("UNKNOWN_KPI"),
        suggestedKpiDefinitionId = suggestedKpiDefinitionId,
        suggestedKpiDisplayName = suggestedKpiDisplayName,
    )

    private fun kpiDefinition(id: Long, code: String, name: String, unit: String? = null) = KpiDefinitionDto(
        id = id,
        code = code,
        displayName = name,
        category = null,
        unit = unit,
        plausibleMin = null,
        plausibleMax = null,
        active = true,
    )
}
