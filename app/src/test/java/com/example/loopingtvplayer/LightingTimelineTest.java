package com.example.loopingtvplayer;

import org.junit.Test;
import static org.junit.Assert.*;

public class LightingTimelineTest {
    @Test public void keyframesAreOrderedAndAlwaysResolveValidScenes() {
        assertEquals(LightingTimeline.KEY_HOURS.length, LightingTimeline.SCENES.length);
        for (int i = 0; i < LightingTimeline.KEY_HOURS.length - 1; i++) {
            assertTrue(LightingTimeline.KEY_HOURS[i + 1] > LightingTimeline.KEY_HOURS[i]);
            assertEquals(i, LightingTimeline.segment(LightingTimeline.KEY_HOURS[i]));
            assertTrue(LightingTimeline.SCENES[i] >= 0 && LightingTimeline.SCENES[i] < 6);
        }
        assertEquals(0, LightingTimeline.SCENES[0]);
        assertEquals(0, LightingTimeline.SCENES[LightingTimeline.SCENES.length - 1]);
    }

    @Test public void everyMinuteHasABoundedCrossfade() {
        for (int minute = 0; minute < 1440; minute++) {
            float hour = minute / 60f;
            int segment = LightingTimeline.segment(hour);
            float mix = (hour - LightingTimeline.KEY_HOURS[segment]) /
                (LightingTimeline.KEY_HOURS[segment + 1] - LightingTimeline.KEY_HOURS[segment]);
            assertTrue(mix >= 0 && mix <= 1);
        }
    }
}
