package com.example.loopingtvplayer;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

final class SolarTimes {
    final String sunrise, sunset, nextSunrise;
    private final int riseMinute, setMinute, nextRiseMinute;

    SolarTimes(String sunrise, String sunset, String nextSunrise) {
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.nextSunrise = nextSunrise;
        riseMinute = minute(sunrise);
        setMinute = minute(sunset);
        nextRiseMinute = minute(nextSunrise);
    }

    private static int minute(String iso) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT);
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
        ParsePosition position = new ParsePosition(0);
        Date date = format.parse(iso, position);
        if (date == null || position.getIndex() != iso.length()) {
            throw new IllegalArgumentException("Invalid solar time: " + iso);
        }
        Calendar calendar = Calendar.getInstance(format.getTimeZone());
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    String label(float hour) {
        float minutes = hour * 60;
        if (minutes >= riseMinute && minutes < setMinute) {
            return "G\u00fcn bat\u0131m\u0131  " + clock(setMinute);
        }
        return "G\u00fcn do\u011fumu  " + clock(minutes < riseMinute ? riseMinute : nextRiseMinute);
    }

    boolean isForDay(String day) { return sunrise.startsWith(day + "T"); }

    boolean isDaylight(float hour) { return hour * 60 >= riseMinute && hour * 60 < setMinute; }

    float sceneHour(float hour) {
        float rise = riseMinute / 60f, set = setMinute / 60f;
        float noon = (rise + set) / 2;
        if (hour < rise - 0.75f) return 0;
        if (hour < rise) return 5 + (hour - rise + 0.75f) / 0.75f;
        if (hour < noon) return 6 + (hour - rise) / (noon - rise) * 6;
        if (hour < set - 0.5f) return 12 + (hour - noon) / (set - 0.5f - noon) * 5;
        if (hour < set + 0.6f) return 17 + (hour - set + 0.5f) / 1.1f * 2;
        return 19;
    }

    private static String clock(int minute) {
        return String.format(Locale.ROOT, "%02d:%02d", minute / 60, minute % 60);
    }
}
