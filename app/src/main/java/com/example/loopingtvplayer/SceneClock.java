package com.example.loopingtvplayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

final class SceneClock {
    private final TimeZone zone = TimeZone.getTimeZone("Europe/Istanbul");
    private final Calendar calendar = Calendar.getInstance(zone);
    private final SimpleDateFormat clock = format("HH:mm");
    private final SimpleDateFormat date = format("d MMMM EEEE");
    private final SimpleDateFormat isoDate = format("yyyy-MM-dd");
    private long lastMinute = Long.MIN_VALUE;
    float hour;
    String clockText = "", dateText = "", day = "";

    private SimpleDateFormat format(String pattern) {
        SimpleDateFormat result = new SimpleDateFormat(pattern, new Locale("tr", "TR"));
        result.setTimeZone(zone);
        return result;
    }

    void update(long timestamp) {
        calendar.setTimeInMillis(timestamp);
        hour = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f
            + calendar.get(Calendar.SECOND) / 3600f;
        if (timestamp / 60000 == lastMinute) return;
        lastMinute = timestamp / 60000;
        Date value = new Date(timestamp);
        clockText = clock.format(value);
        dateText = date.format(value);
        day = isoDate.format(value);
    }
}
