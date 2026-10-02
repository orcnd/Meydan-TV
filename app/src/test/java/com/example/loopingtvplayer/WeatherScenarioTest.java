package com.example.loopingtvplayer;

import org.junit.Test;
import static org.junit.Assert.*;

public class WeatherScenarioTest {
    @Test public void liveWeatherCodesSelectAppropriateScenes() {
        assertEquals(2, WeatherScenario.fromWmo(2));
        assertEquals(3, WeatherScenario.fromWmo(3));
        assertEquals(4, WeatherScenario.fromWmo(48));
        assertEquals(6, WeatherScenario.fromWmo(61));
        assertEquals(7, WeatherScenario.fromWmo(65));
        assertEquals(8, WeatherScenario.fromWmo(67));
        assertEquals(9, WeatherScenario.fromWmo(71));
        assertEquals(10, WeatherScenario.fromWmo(75));
        assertEquals(11, WeatherScenario.fromWmo(95));
        assertEquals(12, WeatherScenario.fromWmo(99));
        assertEquals(3, WeatherScenario.fromWmo(-1));
    }
    @Test public void particleCountsFitBufferAndHeavySnowIsDense() {
        for (int type = 0; type < WeatherScenario.COUNT; type++) {
            assertTrue(WeatherScenario.particles(type) <= WeatherScenario.MAX_PARTICLES);
        }
        assertTrue(WeatherScenario.particles(10) >= 8 * WeatherScenario.particles(9));
        assertEquals(0, WeatherScenario.particles(0));
        assertEquals(0, WeatherScenario.particles(3));
    }
}
