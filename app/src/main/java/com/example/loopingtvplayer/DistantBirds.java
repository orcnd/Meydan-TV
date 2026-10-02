package com.example.loopingtvplayer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import java.util.Random;

final class DistantBirds {
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path wings = new Path(), towers = new Path();
    private final float[] x = new float[12], y = new float[12], speed = new float[12], size = new float[12];
    private final boolean[] active = new boolean[12];
    private float spawnTimer = 0.3f;

    DistantBirds() {
        tower(0.160f, 0.349f, 0.017f);
        tower(0.402f, 0.218f, 0.022f);
        tower(0.635f, 0.022f, 0.024f);
        towers.addRect(0, 0.39f, 1, 1, Path.Direction.CW);
        towers.addOval(new RectF(0.511f, 0.331f, 0.616f, 0.455f), Path.Direction.CW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void tower(float center, float top, float width) {
        towers.moveTo(center, top);
        towers.lineTo(center + width * 0.65f, top + 0.08f);
        towers.lineTo(center + width, top + 0.12f);
        towers.lineTo(center + width * 0.7f, 0.65f);
        towers.lineTo(center - width * 0.7f, 0.65f);
        towers.lineTo(center - width, top + 0.12f);
        towers.lineTo(center - width * 0.65f, top + 0.08f);
        towers.close();
    }

    void draw(Canvas canvas, RectF rect, float dt, float elapsed, boolean daylightClear) {
        if (!daylightClear) {
            java.util.Arrays.fill(active, false);
            spawnTimer = 0.3f;
            return;
        }
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnTimer = (1.5f + random.nextFloat() * 3) / 1.2f;
            boolean right = random.nextBoolean();
            float height = 0.08f + random.nextFloat() * 0.26f;
            int count = 1 + random.nextInt(3);
            for (int i = 0; i < active.length && count > 0; i++) {
                if (active[i]) continue;
                active[i] = true;
                x[i] = right ? -0.01f - count * 0.014f : 1.01f + count * 0.014f;
                y[i] = height + count * 0.009f;
                speed[i] = (right ? 1 : -1) * (0.025f + random.nextFloat() * 0.018f);
                size[i] = 0.0014f + random.nextFloat() * 0.0015f;
                count--;
            }
        }
        canvas.save();
        canvas.translate(rect.left, rect.top);
        canvas.scale(rect.width(), rect.height());
        canvas.clipPath(towers, Region.Op.DIFFERENCE);
        paint.setColor(0xCC293239);
        paint.setStrokeWidth(0.00075f);
        for (int i = 0; i < active.length; i++) {
            if (!active[i]) continue;
            x[i] += speed[i] * dt;
            if (x[i] < -0.1f || x[i] > 1.1f) { active[i] = false; continue; }
            float cy = y[i] + (float) Math.sin(elapsed * 0.65f + i) * 0.002f;
            float flap = (float) Math.sin(elapsed * 9 + i) * size[i] * 0.9f;
            wings.rewind();
            wings.moveTo(x[i] - size[i], cy - flap);
            wings.quadTo(x[i] - size[i] * 0.5f, cy - size[i] * 0.6f, x[i], cy);
            wings.quadTo(x[i] + size[i] * 0.5f, cy - size[i] * 0.6f, x[i] + size[i], cy - flap);
            canvas.drawPath(wings, paint);
        }
        canvas.restore();
    }
}
