package com.example.loopingtvplayer;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import static org.junit.Assert.*;
public class SceneClockTest {
    @Test public void realClockUsesIstanbulEvenWhenDeviceTimezoneDiffers() throws Exception {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
            utc.setTimeZone(TimeZone.getTimeZone("UTC"));
            SceneClock clock = new SceneClock();
            clock.update(utc.parse("2026-10-02 22:30").getTime());
            assertEquals("01:30", clock.clockText);
            assertEquals("2026-10-03", clock.day);
            assertEquals(1.5f, clock.hour, 0.001f);
            assertTrue(clock.dateText.contains("Cumartesi"));
            clock.update(utc.parse("2026-10-03 21:00").getTime());
            assertEquals("00:00", clock.clockText);
            assertEquals("2026-10-04", clock.day);
        } finally { TimeZone.setDefault(original); }
    }
}
