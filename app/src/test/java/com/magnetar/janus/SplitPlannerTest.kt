package com.magnetar.janus

import com.magnetar.janus.model.SplitPlanner
import com.magnetar.janus.model.ConversionSupport
import com.magnetar.janus.model.MediaKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitPlannerTest {
    @Test fun conversionSupport_exposesMp4ForAudioAndVideo() {
        assertEquals(listOf("MP4"), ConversionSupport.supportedOutputContainers(MediaKind.AUDIO))
        assertEquals(listOf("MP4"), ConversionSupport.supportedOutputContainers(MediaKind.VIDEO))
        assertTrue(ConversionSupport.canRemuxToMp4(MediaKind.AUDIO))
        assertTrue(ConversionSupport.canRemuxToMp4(MediaKind.VIDEO))
    }
    @Test fun presetDurations_areWhatsAppPresets() =
        assertEquals(listOf(30L, 60L, 90L), SplitPlanner.presetDurations())

    @Test fun validateDuration_acceptsPositiveDurationWithinMedia() =
        assertTrue(SplitPlanner.validateDuration(60, 277))

    @Test fun validateDuration_rejectsInvalidValues() {
        assertFalse(SplitPlanner.validateDuration(0, 277))
        assertFalse(SplitPlanner.validateDuration(-1, 277))
        assertFalse(SplitPlanner.validateDuration(300, 277))
        assertFalse(SplitPlanner.validateDuration(1, 0))
    }

    @Test fun automaticSegments_coverWholeMedia() =
        assertEquals(listOf(60L, 60L, 60L, 60L, 37L), SplitPlanner.automaticSegments(277, 60).map { it.durationSeconds })

    @Test fun automaticSegments_allowPresetLongerThanShortMedia() =
        assertEquals(listOf(12L), SplitPlanner.automaticSegments(12, 30).map { it.durationSeconds })

    @Test(expected = IllegalArgumentException::class)
    fun automaticSegments_rejectZeroSegmentDuration() { SplitPlanner.automaticSegments(60, 0) }

    @Test(expected = IllegalArgumentException::class)
    fun automaticSegments_rejectZeroMediaDuration() { SplitPlanner.automaticSegments(0, 60) }

    @Test fun manualSegments_sortDeduplicateAndIgnoreOutOfRangeCuts() =
        assertEquals(listOf(30L, 30L, 40L), SplitPlanner.manualSegments(100, listOf(60, 30, 30, 0, 100, 120)).map { it.durationSeconds })

    @Test fun manualSegments_withoutCuts_returnsWholeMedia() =
        assertEquals(listOf(100L), SplitPlanner.manualSegments(100, emptyList()).map { it.durationSeconds })

    @Test(expected = IllegalArgumentException::class)
    fun manualSegments_rejectZeroMediaDuration() { SplitPlanner.manualSegments(0, emptyList()) }

    @Test fun segmentDuration_isEndMinusStart() =
        assertEquals(7L, com.magnetar.janus.model.Segment(3, 10).durationSeconds)

    @Test fun formatDuration_supportsMinutesHoursAndNegativeInput() {
        assertEquals("04:37", SplitPlanner.formatDuration(277))
        assertEquals("01:01:01", SplitPlanner.formatDuration(3_661))
        assertEquals("00:00", SplitPlanner.formatDuration(-2))
    }
}
