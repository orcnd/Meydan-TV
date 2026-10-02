package com.example.loopingtvplayer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.IdentityHashMap;

final class TreeSway {
    // The two foreground conifers, measured in photograph coordinates.
    private static final float[][] TREES = {{0.323f, 0.573f, 0.384f, 0.87f},
        {0.687f, 0.655f, 0.740f, 0.874f}};
    private final IdentityHashMap<Bitmap, Bitmap[]> patches = new IdentityHashMap<>();
    private final float[] vertices = new float[5 * 9 * 2];

    void draw(Canvas canvas, Bitmap image, RectF rect, Paint paint, float time, float windKmh) {
        if (windKmh <= 0) return;
        Bitmap[] parts = patches.get(image);
        if (parts == null) {
            parts = new Bitmap[TREES.length];
            for (int i = 0; i < parts.length; i++) {
                float[] t = TREES[i];
                int left = Math.round(t[0] * image.getWidth());
                int top = Math.round(t[1] * image.getHeight());
                parts[i] = Bitmap.createBitmap(image, left, top,
                    Math.round(t[2] * image.getWidth()) - left,
                    Math.round(t[3] * image.getHeight()) - top);
            }
            patches.put(image, parts);
        }
        for (int i = 0; i < parts.length; i++) {
            float[] t = TREES[i];
            Bitmap part = parts[i];
            float amplitude = 6 * Math.min(windKmh / 25f, 1.8f) * image.getWidth() / 1920f;
            float wave = (float) (Math.sin(time * 1.7 + i) + 0.35 * Math.sin(time * 3.2 + i));
            int n = 0;
            for (int row = 0; row <= 8; row++) {
                float v = row / 8f;
                for (int col = 0; col <= 4; col++) {
                    float u = col / 4f;
                    // Keep every patch edge fixed so adjacent paving and masonry do not move.
                    float weight = (float) Math.sin(u * Math.PI) * (float) Math.sin(v * Math.PI) * (1 - v);
                    vertices[n++] = u * part.getWidth() + amplitude * wave * weight;
                    vertices[n++] = v * part.getHeight();
                }
            }
            canvas.save();
            canvas.translate(rect.left + t[0] * rect.width(), rect.top + t[1] * rect.height());
            canvas.scale(rect.width() / image.getWidth(), rect.height() / image.getHeight());
            canvas.drawBitmapMesh(part, 4, 8, vertices, 0, null, 0, paint);
            canvas.restore();
        }
    }
}
