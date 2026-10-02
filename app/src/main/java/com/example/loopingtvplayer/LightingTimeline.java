package com.example.loopingtvplayer;

final class LightingTimeline {
    static final float[] KEY_HOURS = {0, 5, 6, 9, 12, 15, 17, 19, 24};
    static final int[] SCENES = {0, 0, 1, 2, 3, 4, 5, 0, 0};

    static int segment(float time) {
        for (int i = 0; i < KEY_HOURS.length - 1; i++) {
            if (time < KEY_HOURS[i + 1]) return i;
        }
        return KEY_HOURS.length - 2;
    }
}
