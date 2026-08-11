package com.magnetar.janus

import com.magnetar.janus.model.SplitPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitPlannerTest {
    @Test fun presetDurations_areWhatsAppPresets() = assertEquals(listOf(30L, 60L, 90L), SplitPlanner.presetDurations())
    @Test fun validateDuration_acceptsPositiveDurationWithinMedia() = assertTrue(SplitPlanner.validateDuration(60, 277))
    @Test fun validateDuration_rejectsInvalidValues() { assertFalse(SplitPlanner.validateDuration(0, 277)); assertFalse(SplitPlanner.validateDuration(-1, 277)); assertFalse(SplitPlanner.validateDuration(300, 277)) }
    @Test fun automaticSegments_coverWholeMedia() = assertEquals(listOf(60L, 60L, 60L, 60L, 37L), SplitPlanner.automaticSegments(277, 60).map { it.durationSeconds })
    @Test fun manualSegments_sortDeduplicateAndIgnoreOutOfRangeCuts() = assertEquals(listOf(30L, 30L, 40L), SplitPlanner.manualSegments(100, listOf(60, 30, 30, 0, 100, 120)).map { it.durationSeconds })
    @Test fun formatDuration_isZeroPadded() { assertEquals("04:37", SplitPlanner.formatDuration(277)); assertEquals("00:00", SplitPlanner.formatDuration(-2)) }
}
