package com.example.loopingtvplayer;

import org.junit.Test;
import static org.junit.Assert.*;

public class SolarTimesTest {
    private final SolarTimes solar = new SolarTimes("2026-10-03T06:45",
        "2026-10-03T18:30", "2026-10-04T06:46");

    @Test public void daylightShowsSunsetAndNightShowsNextSunrise() {
        assertEquals("G\u00fcn do\u011fumu  06:45", solar.label(0));
        assertEquals("G\u00fcn do\u011fumu  06:45", solar.label(6.74f));
        assertEquals("G\u00fcn bat\u0131m\u0131  18:30", solar.label(6.75f));
        assertEquals("G\u00fcn bat\u0131m\u0131  18:30", solar.label(12));
        assertEquals("G\u00fcn bat\u0131m\u0131  18:30", solar.label(18.49f));
        assertEquals("G\u00fcn do\u011fumu  06:46", solar.label(18.5f));
        assertEquals("G\u00fcn do\u011fumu  06:46", solar.label(23));
    }

    @Test(expected = IllegalArgumentException.class) public void invalidTimesAreRejected() {
        new SolarTimes("", "2026-10-03T18:30", "2026-10-04T06:46");
    }
    @Test public void sceneLightingFollowsSolarTimesAndRejectsPreviousDay() {
        assertTrue(solar.isForDay("2026-10-03"));
        assertFalse(solar.isForDay("2026-10-04"));
        assertEquals(0f, solar.sceneHour(1), 0.001f);
        assertEquals(6f, solar.sceneHour(6.75f), 0.001f);
        assertEquals(12f, solar.sceneHour((6.75f + 18.5f) / 2), 0.001f);
        assertEquals(19f, solar.sceneHour(23), 0.001f);
        assertTrue(solar.isDaylight(12));
        assertFalse(solar.isDaylight(23));
    }
}
