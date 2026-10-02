package com.example.loopingtvplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class LiveInfo {
    private final SharedPreferences cache;
    private ScheduledExecutorService worker;
    volatile WeatherData snapshot;
    volatile boolean weatherFailed;
    private int generation;

    LiveInfo(Context context) {
        cache = context.getSharedPreferences("live-info", Context.MODE_PRIVATE);
        weatherFailed = true;
        SolarTimes solar;
        try {
            solar = new SolarTimes(cache.getString("sunrise", ""),
                cache.getString("sunset", ""), cache.getString("nextSunrise", ""));
        } catch (IllegalArgumentException ignored) { solar = null; }
        snapshot = new WeatherData(cache.getString("alanya", "--"), cache.getString("konya", "--"),
            cache.getString("alanyaTomorrow", "--"), cache.getString("konyaTomorrow", "--"),
            cache.getString("weatherTime", ""), solar, cache.getFloat("windKmh", 0),
            cache.getInt("konyaCode", 3), cache.getLong("fetchedAt", 0));
    }

    synchronized void start() {
        if (worker != null) return;
        worker = Executors.newSingleThreadScheduledExecutor();
        int currentGeneration = ++generation;
        worker.scheduleWithFixedDelay(() -> refresh(currentGeneration), 0, 5, TimeUnit.MINUTES);
    }

    synchronized void stop() {
        generation++;
        if (worker != null) worker.shutdownNow();
        worker = null;
    }

    private void refresh(int currentGeneration) {
        try {
            JSONArray cities = new JSONArray(fetch("https://api.open-meteo.com/v1/forecast?latitude=36.5437,37.871&longitude=31.9998,32.505&current=temperature_2m,weather_code,wind_speed_10m&daily=temperature_2m_min,temperature_2m_max,weather_code,sunrise,sunset&forecast_days=2&timezone=Europe%2FIstanbul"));
            String a = weather(cities.getJSONObject(0).getJSONObject("current"));
            String k = weather(cities.getJSONObject(1).getJSONObject("current"));
            String at = tomorrow(cities.getJSONObject(0).getJSONObject("daily"));
            String kt = tomorrow(cities.getJSONObject(1).getJSONObject("daily"));
            String time = cities.getJSONObject(0).getJSONObject("current").getString("time").replace('T', ' ');
            JSONObject daily = cities.getJSONObject(1).getJSONObject("daily");
            SolarTimes solar = new SolarTimes(daily.getJSONArray("sunrise").getString(0),
                daily.getJSONArray("sunset").getString(0), daily.getJSONArray("sunrise").getString(1));
            WeatherData data = new WeatherData(a, k, at, kt, time, solar,
                (float) cities.getJSONObject(1).getJSONObject("current").getDouble("wind_speed_10m"),
                cities.getJSONObject(1).getJSONObject("current").getInt("weather_code"), System.currentTimeMillis());
            synchronized (this) {
                if (currentGeneration != generation || Thread.currentThread().isInterrupted()) return;
                cache.edit().putString("alanya", a).putString("konya", k).putString("alanyaTomorrow", at).putString("konyaTomorrow", kt).putString("weatherTime", time)
                    .putString("sunrise", solar.sunrise).putString("sunset", solar.sunset).putString("nextSunrise", solar.nextSunrise).putFloat("windKmh", data.windKmh)
                    .putInt("konyaCode", data.konyaCode).putLong("fetchedAt", data.fetchedAt).apply();
                snapshot = data;
                weatherFailed = false;
            }
            Log.i("LiveInfo", "Konya wind km/h=" + data.windKmh);
            Log.i("LiveInfo", "Konya sunrise=" + solar.sunrise + " sunset=" + solar.sunset);
            Log.i("LiveInfo", "Weather updated: " + a + " / " + k + "; tomorrow: " + at + " / " + kt);
        } catch (Exception error) {
            synchronized (this) {
                if (currentGeneration != generation || Thread.currentThread().isInterrupted()) return;
                weatherFailed = true;
            }
            Log.w("LiveInfo", "Weather update failed", error);
        }
    }

    private String weather(JSONObject current) throws Exception {
        return String.format(new Locale("tr", "TR"), "%.0f\u00b0C  %s", current.getDouble("temperature_2m"), condition(current.getInt("weather_code")));
    }

    private String tomorrow(JSONObject daily) throws Exception {
        return String.format(new Locale("tr", "TR"), "Yar\u0131n  %.0f / %.0f\u00b0C  %s",
            daily.getJSONArray("temperature_2m_min").getDouble(1),
            daily.getJSONArray("temperature_2m_max").getDouble(1),
            condition(daily.getJSONArray("weather_code").getInt(1)));
    }

    private String condition(int code) {
        return WeatherScenario.NAMES[WeatherScenario.fromWmo(code)];
    }

    private String fetch(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        try {
            if (connection.getResponseCode() != 200) throw new java.io.IOException("HTTP " + connection.getResponseCode());
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[4096];
                int count;
                while ((count = input.read(chunk)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    if (buffer.size() + count > 1024 * 1024) throw new java.io.IOException("Forecast response too large");
                    buffer.write(chunk, 0, count);
                }
                return buffer.toString(StandardCharsets.UTF_8.name());
            }
        } finally { connection.disconnect(); }
    }
}
