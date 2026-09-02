package com.factoryflow.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavigationTest {

    @Test
    fun `report and document destinations retain reports shell selection`() {
        assertEquals(Routes.REPORTS, selectedTopLevelRoute(Routes.REPORT_DETAIL))
        assertEquals(Routes.REPORTS, selectedTopLevelRoute(Routes.CONFIRMED))
        assertEquals(Routes.REPORTS, selectedTopLevelRoute(Routes.GENERATED_DETAIL))
        assertEquals(Routes.REPORTS, selectedTopLevelRoute(Routes.SCHEDULES))
        assertEquals(Routes.REPORTS, selectedTopLevelRoute(Routes.SCHEDULE_FORM))
    }

    @Test
    fun `acquisition and review destinations retain create shell selection`() {
        assertEquals(Routes.CREATE, selectedTopLevelRoute(Routes.PASTE))
        assertEquals(Routes.CREATE, selectedTopLevelRoute(Routes.MANUAL))
        assertEquals(Routes.CREATE, selectedTopLevelRoute(Routes.REVIEW))
    }

    @Test
    fun `top-level and unknown destinations resolve predictably`() {
        assertEquals(Routes.DASHBOARD, selectedTopLevelRoute(Routes.DASHBOARD))
        assertEquals(Routes.STATISTICS, selectedTopLevelRoute(Routes.STATISTICS))
        assertEquals(Routes.STATISTICS, selectedTopLevelRoute(Routes.INTELLIGENCE_OVERVIEW))
        assertEquals(Routes.STATISTICS, selectedTopLevelRoute(Routes.INTELLIGENCE_WORKSPACE))
        assertEquals(Routes.STATISTICS, selectedTopLevelRoute(Routes.INTELLIGENCE_ALERTS))
        assertEquals(Routes.STATISTICS, selectedTopLevelRoute(Routes.INTELLIGENCE_ALERT_DETAIL))
        assertEquals(Routes.NOTIFICATIONS, selectedTopLevelRoute(Routes.NOTIFICATIONS))
        assertEquals(Routes.DASHBOARD, selectedTopLevelRoute(Routes.PROFILE))
        assertEquals(Routes.DASHBOARD, selectedTopLevelRoute(null))
    }

    @Test
    fun `bottom navigation exposes four destinations around the create action`() {
        assertEquals(true, showsBottomNavigation(Routes.DASHBOARD))
        assertEquals(true, showsBottomNavigation(Routes.REPORTS))
        assertEquals(true, showsBottomNavigation(Routes.CREATE))
        assertEquals(true, showsBottomNavigation(Routes.STATISTICS))
        assertEquals(true, showsBottomNavigation(Routes.NOTIFICATIONS))
    }

    @Test
    fun `focused workflows do not show bottom navigation`() {
        assertEquals(false, showsBottomNavigation(Routes.PASTE))
        assertEquals(false, showsBottomNavigation(Routes.MANUAL))
        assertEquals(false, showsBottomNavigation(Routes.REVIEW))
        assertEquals(false, showsBottomNavigation(Routes.CONFIRMED))
        assertEquals(false, showsBottomNavigation(Routes.REPORT_DETAIL))
        assertEquals(false, showsBottomNavigation(Routes.GENERATED_DETAIL))
        assertEquals(false, showsBottomNavigation(Routes.SCHEDULE_FORM))
        assertEquals(false, showsBottomNavigation(Routes.PROFILE))
        assertEquals(false, showsBottomNavigation(Routes.INTELLIGENCE_OVERVIEW))
        assertEquals(false, showsBottomNavigation(Routes.INTELLIGENCE_WORKSPACE))
        assertEquals(false, showsBottomNavigation(Routes.INTELLIGENCE_ALERTS))
        assertEquals(false, showsBottomNavigation(Routes.INTELLIGENCE_ALERT_DETAIL))
        assertEquals(false, showsBottomNavigation(null))
    }
}
