package com.factoryflow.app.feature.acquisition

import com.factoryflow.app.*
import com.factoryflow.app.core.network.dto.*
import java.io.IOException
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PasteViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `analysis creates a resumable draft and preserves unknown lines`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply {
            analyzed = AnalyzeReportResponse(
                source = "PASTE",
                rawText = "Température: 42\nNote équipe",
                recognizedCount = 1,
                readyCount = 1,
                attentionCount = 0,
                missingCount = 0,
                unresolvedCount = 1,
                needsReviewCount = 1,
                unrecognizedCount = 1,
                entries = listOf(
                    ParsedEntryDto(
                        candidateId = "1",
                        kpiDefinitionId = 10,
                        kpiCode = "TEMP",
                        kpiDisplayName = "Température",
                        sourceLabel = "Température",
                        sourceLine = "Température: 42",
                        extractedValue = 42.toBigDecimal(),
                        capturedUnit = "°C",
                        expectedUnit = "°C",
                        confidenceScore = 0.98.toBigDecimal(),
                        confidenceLevel = "HIGH",
                        matchMethod = "EXACT_CANONICAL",
                        reviewState = "READY",
                        suggestions = emptyList(),
                        warnings = emptyList(),
                    ),
                ),
                ignoredLines = emptyList(),
                unrecognizedLines = listOf(
                    ParsedUnknownLineDto(
                        lineId = "2",
                        sourceLine = "Note équipe",
                        reason = "NO_MATCH",
                    ),
                ),
            )
            created = reportDto()
        }
        val viewModel = PasteViewModel(repository); var id: Long? = null
        viewModel.text("Température: 42\nNote équipe"); viewModel.analyze { id = it }; advanceUntilIdle()
        assertEquals(12L, id)
        assertFalse(viewModel.state.value.analyzing)
        assertEquals("Température: 42\nNote équipe", repository.lastAnalyzedRawText)
        assertEquals("PASTE", repository.lastAnalyzedSource)
        assertEquals(1, repository.createdDraftRequests.size)
        assertEquals("Note équipe", repository.createdDraftRequests.single().unrecognizedLines.single().sourceLine)
    }

    @Test fun `analysis failure preserves input avoids navigation and can be retried`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply { analyzeFailure = IOException("offline") }
        val viewModel = PasteViewModel(repository)
        var openedDraft: Long? = null
        val rawText = "Vrac : 15,8\nNote équipe"

        viewModel.text(rawText)
        viewModel.analyze { openedDraft = it }
        advanceUntilIdle()

        assertNull(openedDraft)
        assertEquals(rawText, viewModel.state.value.text)
        assertTrue(viewModel.state.value.analysisFailed)
        assertFalse(viewModel.state.value.analyzing)
        assertTrue(repository.createdDraftRequests.isEmpty())

        repository.analyzeFailure = null
        repository.analyzed = minimalAnalysis(rawText)
        repository.created = reportDto()
        viewModel.analyze { openedDraft = it }
        advanceUntilIdle()

        assertEquals(12L, openedDraft)
        assertFalse(viewModel.state.value.analysisFailed)
    }

    private fun minimalAnalysis(rawText: String) = AnalyzeReportResponse(
        source = "PASTE", rawText = rawText, recognizedCount = 0, readyCount = 0,
        attentionCount = 0, missingCount = 0, unresolvedCount = 1,
        needsReviewCount = 1, unrecognizedCount = 1,
        entries = emptyList(), ignoredLines = emptyList(),
        unrecognizedLines = listOf(ParsedUnknownLineDto("u1", "Note équipe", "NO_MATCH")),
    )
}
