package com.example.loopingtvplayer;

import org.junit.Test;
import static org.junit.Assert.*;

public class WeatherDataTest {
    private WeatherData data(float wind, long fetchedAt) {
        return new WeatherData("A", "K", "AT", "KT", "", null, wind, 75, fetchedAt);
    }

    @Test public void missingExpiredAndFutureTimestampsUseNeutralScene() {
        assertEquals(3, data(10, 0).scenario(1000));
        assertEquals(10, data(10, 1000).scenario(1000));
        assertEquals(10, data(10, 1000).scenario(1000 + WeatherData.MAX_AGE_MS - 1));
        assertEquals(3, data(10, 1000).scenario(1000 + WeatherData.MAX_AGE_MS));
        assertEquals(3, data(10, 1000).scenario(999));
    }

    @Test public void malformedWindCannotCorruptAnimationCoordinates() {
        assertEquals(0f, data(Float.NaN, 1).windKmh, 0);
        assertEquals(0f, data(Float.POSITIVE_INFINITY, 1).windKmh, 0);
        assertEquals(0f, data(-5, 1).windKmh, 0);
        assertEquals(150f, data(1000, 1).windKmh, 0);
        assertEquals(15.5f, data(15.5f, 1).windKmh, 0);
    }
}
