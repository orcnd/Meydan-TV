package com.example.loopingtvplayer;

final class WeatherScenario {
    static final String[] NAMES = {"A\u00e7\u0131k", "Az bulutlu", "Par\u00e7al\u0131 bulutlu", "Kapal\u0131", "Sisli", "\u00c7isenti", "Ya\u011fmur", "Sa\u011fanak", "Karla kar\u0131\u015f\u0131k ya\u011fmur", "Hafif kar", "Yo\u011fun kar", "F\u0131rt\u0131na", "Dolu"};
    static final int COUNT = NAMES.length;
    static final int MAX_PARTICLES = 1000;
    static int fromWmo(int code) {
        switch (code) {
            case 0: return 0;
            case 1: return 1;
            case 2: return 2;
            case 45: case 48: return 4;
            case 51: case 53: case 55: return 5;
            case 56: case 57: case 66: case 67: return 8;
            case 61: case 63: case 80: return 6;
            case 65: case 81: case 82: return 7;
            case 71: case 73: case 77: case 85: return 9;
            case 75: case 86: return 10;
            case 95: return 11;
            case 96: case 99: return 12;
            default: return 3;
        }
    }
    static boolean snow(int type) { return type == 8 || type == 9 || type == 10; }
    static boolean wet(int type) { return type >= 5 && !snow(type); }
    static int particles(int type) {
        if (type < 5) return 0;
        if (type == 5) return 65;
        if (type == 9) return 110;
        if (type == 10) return MAX_PARTICLES;
        if (type == 7 || type == 11) return 260;
        return 170;
    }
}
