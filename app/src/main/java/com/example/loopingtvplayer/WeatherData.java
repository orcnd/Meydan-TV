package com.example.loopingtvplayer;

final class WeatherData {
    static final long MAX_AGE_MS = 24 * 60 * 60 * 1000L;
    final String alanya, konya, alanyaTomorrow, konyaTomorrow, weatherTime;
    final SolarTimes solarTimes;
    final float windKmh;
    final int konyaCode;
    final long fetchedAt;

    WeatherData(String alanya, String konya, String alanyaTomorrow, String konyaTomorrow,
            String weatherTime, SolarTimes solarTimes, float windKmh, int konyaCode, long fetchedAt) {
        this.alanya = alanya;
        this.konya = konya;
        this.alanyaTomorrow = alanyaTomorrow;
        this.konyaTomorrow = konyaTomorrow;
        this.weatherTime = weatherTime;
        this.solarTimes = solarTimes;
        this.windKmh = Float.isNaN(windKmh) || Float.isInfinite(windKmh) ? 0 : Math.max(0, Math.min(windKmh, 150));
        this.konyaCode = konyaCode;
        this.fetchedAt = fetchedAt;
    }

    boolean isStale(long now) {
        return fetchedAt <= 0 || now < fetchedAt || now - fetchedAt >= MAX_AGE_MS;
    }

    int scenario(long now) { return isStale(now) ? 3 : WeatherScenario.fromWmo(konyaCode); }
}
