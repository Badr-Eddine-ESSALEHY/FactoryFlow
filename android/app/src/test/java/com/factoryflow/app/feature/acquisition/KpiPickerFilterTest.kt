package com.factoryflow.app.feature.acquisition

import com.factoryflow.app.core.network.dto.KpiDefinitionDto
import org.junit.Assert.assertEquals
import org.junit.Test

class KpiPickerFilterTest {
    private val definitions = listOf(
        definition(1, "NIVEAU_MELASSE", "Niveau citerne Mélasse", listOf("Melasse")),
        definition(2, "VRAC", "Vrac", listOf("Varc")),
    )

    @Test fun `search is case and accent insensitive across names codes and aliases`() {
        assertEquals(listOf(1L), filterKpiDefinitions(definitions, "melasse").map { it.id })
        assertEquals(listOf(2L), filterKpiDefinitions(definitions, "VARC").map { it.id })
        assertEquals(listOf(1L), filterKpiDefinitions(definitions, "niveau_melasse").map { it.id })
    }

    @Test fun `empty and unknown searches have predictable results`() {
        assertEquals(definitions, filterKpiDefinitions(definitions, ""))
        assertEquals(emptyList<KpiDefinitionDto>(), filterKpiDefinitions(definitions, "introuvable"))
    }

    private fun definition(id: Long, code: String, name: String, aliases: List<String>) = KpiDefinitionDto(
        id = id,
        code = code,
        displayName = name,
        category = "Production",
        unit = null,
        plausibleMin = null,
        plausibleMax = null,
        aliases = aliases,
        active = true,
    )
}
