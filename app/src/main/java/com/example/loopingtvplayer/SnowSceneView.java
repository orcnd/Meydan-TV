package com.example.loopingtvplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.RadialGradient;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public final class SnowSceneView extends View {
    private static final float[] WET_EXPOSURE = {0.33f, 0.55f, 1.10f, 1.20f, 1.05f, 0.9f};
    private static final float[] TREE_EXPOSURE = {0.08f, 0.34f, 0.83f, 1f, 0.88f, 0.62f};
    private final Shader cloudShader = new RadialGradient(0, 0, 0.25f,
        new int[]{0xFF657786, 0x80657786, 0x00657786}, new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
    private final Shader overlayShader = new LinearGradient(0, 0, 0, 440,
        new int[]{0xA0000000, 0x50000000, 0x00000000}, null, Shader.TileMode.CLAMP);
    private final ColorMatrix colorMatrix = new ColorMatrix();
    private ColorMatrixColorFilter wetFilter;
    private float wetBrightness = Float.NaN, treeBrightness = Float.NaN;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF backgroundRect = new RectF();
    private final Random random = new Random(17);
    private final float[] x = new float[WeatherScenario.MAX_PARTICLES], y = new float[WeatherScenario.MAX_PARTICLES], depth = new float[WeatherScenario.MAX_PARTICLES];
    private final TreeSway treeSway = new TreeSway();
    private final DistantBirds birds = new DistantBirds();
    private Bitmap background;
    private Bitmap rainBackground;
    private Bitmap backdropTrees;
    private final Paint treePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap[] dayScenes = new Bitmap[6];
    private final Bitmap[] dryScenes = new Bitmap[6];
    private int weatherType, previousWeather = -1;
    private final SceneClock clock = new SceneClock();
    private int loggedHour = -1;
    private final LiveInfo info;
    private WeatherData data;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean rain;
    private boolean running;
    private long lastFrame;
    private float elapsed;
    private int frames;

    public SnowSceneView(Context context) {
        super(context);
        info = new LiveInfo(context);
        data = info.snapshot;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        try (InputStream input = context.getAssets().open("mevlana_snow.png")) {
            background = BitmapFactory.decodeStream(input);
            if (background == null) throw new IOException("Invalid snow image");
        } catch (IOException exception) {
            throw new IllegalStateException("Scene background missing", exception);
        }
        try (InputStream input = context.getAssets().open("mevlana_rain.png")) {
            rainBackground = BitmapFactory.decodeStream(input);
            if (rainBackground == null) throw new IOException("Invalid rain image");
        } catch (IOException exception) {
            throw new IllegalStateException("Rain background missing", exception);
        }
        try (InputStream input = context.getAssets().open("mevlana_backdrop_trees.png")) {
            backdropTrees = BitmapFactory.decodeStream(input);
            if (backdropTrees == null) throw new IOException("Invalid tree overlay");
        } catch (IOException exception) {
            throw new IllegalStateException("Tree backdrop missing", exception);
        }
        String[] sceneNames = {"night", "dawn", "morning", "noon", "afternoon"};
        for (int i = 0; i < sceneNames.length; i++) {
            try (InputStream input = context.getAssets().open("mevlana_" + sceneNames[i] + ".png")) {
                dayScenes[i] = BitmapFactory.decodeStream(input);
                if (dayScenes[i] == null) throw new IOException("Invalid image");
            } catch (IOException exception) {
                throw new IllegalStateException("Day scene missing: " + sceneNames[i], exception);
            }
        }
        dayScenes[5] = background;
        String[] dryNames = {"night", "dawn", "morning", "noon", "afternoon", "evening"};
        for (int i = 0; i < dryNames.length; i++) {
            try (InputStream input = context.getAssets().open("mevlana_dry_" + dryNames[i] + ".png")) {
                dryScenes[i] = BitmapFactory.decodeStream(input);
                if (dryScenes[i] == null) throw new IOException("Invalid dry image");
            } catch (IOException exception) {
                throw new IllegalStateException("Dry scene missing: " + dryNames[i], exception);
            }
        }
        for (int i = 0; i < x.length; i++) {
            x[i] = random.nextFloat();
            y[i] = random.nextFloat();
            depth[i] = random.nextFloat();
        }
    }

    public void start() {
        info.start();
        running = true;
        lastFrame = SystemClock.uptimeMillis();
        invalidate();
    }

    public void stop() {
        running = false;
        info.stop();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        float scale = Math.max((float) width / background.getWidth(), (float) height / background.getHeight());
        float w = background.getWidth() * scale, h = background.getHeight() * scale;
        backgroundRect.set((width - w) / 2, (height - h) / 2, (width + w) / 2, (height + h) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = SystemClock.uptimeMillis();
        long wallTime = System.currentTimeMillis();
        clock.update(wallTime);
        data = info.snapshot;
        float dt = running ? Math.min((now - lastFrame) / 1000f, 0.08f) : 0;
        lastFrame = now;
        elapsed += dt;
        weatherType = data.scenario(wallTime);
        rain = WeatherScenario.wet(weatherType) || weatherType == 8;
        if (weatherType != previousWeather) {
            previousWeather = weatherType;
            Log.i("SceneLive", "Konya scene: " + WeatherScenario.NAMES[weatherType]);
        }
        paint.setAlpha(255);
        drawDayBackground(canvas);
        SolarTimes solar = todaySolar();
        boolean daylight = solar == null ? clock.hour >= 7 && clock.hour < 19 : solar.isDaylight(clock.hour);
        birds.draw(canvas, backgroundRect, dt, elapsed, (weatherType == 0 || weatherType == 2) && daylight);
        paint.setColor(Color.WHITE);
        for (int i = 0; i < WeatherScenario.particles(weatherType); i++) {
            float d = depth[i];
            boolean drop = rain && (weatherType != 8 || i % 2 == 0);
            boolean hail = weatherType == 12;
            y[i] += dt * (drop ? 0.35f + d * 0.55f : 0.018f + d * 0.055f);
            x[i] += dt * (drop ? (weatherType == 11 ? 0.18f : 0.045f) : 0.006f + d * 0.008f + Math.min(data.windKmh, 60) * 0.0004f + (float) Math.sin(elapsed * 0.6f + i) * 0.009f);
            if (y[i] > 1.02f) { y[i] = -0.02f; x[i] = random.nextFloat(); }
            if (x[i] > 1.02f) x[i] = -0.02f;
            if (x[i] < -0.02f) x[i] = 1.02f;
            paint.setAlpha((int) (65 + d * 155));
            float radius = (0.65f + d * d * 3.3f) * getHeight() / 1080f;
            if (drop && !hail) {
                paint.setAlpha((int) (28 + d * 65));
                paint.setStrokeWidth((0.6f + d * 0.5f) * getHeight() / 1080f);
                canvas.drawLine(x[i] * getWidth(), y[i] * getHeight(), x[i] * getWidth() - radius,
                    y[i] * getHeight() - (7 + d * 12) * getHeight() / 1080f, paint);
            } else {
                canvas.drawCircle(x[i] * getWidth(), y[i] * getHeight(), radius, paint);
            }
        }
        drawInfo(canvas);
        if (++frames == 120) Log.i("SnowScene", "120 live frames rendered: " + getWidth() + "x" + getHeight());
        if (running) postInvalidateDelayed(33);
    }

    private void drawDayBackground(Canvas canvas) {
        SolarTimes solar = todaySolar();
        float time = solar == null ? clock.hour : solar.sceneHour(clock.hour);
        int segment = LightingTimeline.segment(time);
        int from = LightingTimeline.SCENES[segment], to = LightingTimeline.SCENES[segment + 1];
        float mix = (time - LightingTimeline.KEY_HOURS[segment]) /
            (LightingTimeline.KEY_HOURS[segment + 1] - LightingTimeline.KEY_HOURS[segment]);
        paint.setAlpha(255);
        Bitmap[] scenes = WeatherScenario.snow(weatherType) ? dayScenes : dryScenes;
        if (WeatherScenario.wet(weatherType)) {
            float brightness = WET_EXPOSURE[from] * (1 - mix) + WET_EXPOSURE[to] * mix;
            if (Float.isNaN(wetBrightness) || Math.abs(brightness - wetBrightness) > 0.001f) {
                wetBrightness = brightness;
                colorMatrix.setScale(brightness, brightness, brightness, 1);
                wetFilter = new ColorMatrixColorFilter(colorMatrix);
            }
            paint.setColorFilter(wetFilter);
            drawScene(canvas, rainBackground);
            paint.setColorFilter(null);
        } else {
            drawScene(canvas, scenes[from]);
        }
        if (from != to && !WeatherScenario.wet(weatherType)) {
            paint.setAlpha(Math.round(255 * mix));
            drawScene(canvas, scenes[to]);
        }
        paint.setAlpha(255);
        drawBackdropTrees(canvas, from, to, mix);
        drawAtmosphere(canvas, time);
        int hour = (int) clock.hour;
        if (hour != loggedHour) {
            loggedHour = hour;
            Log.i("SceneLive", String.format(java.util.Locale.ROOT, "%02d:00 scene=%d->%d", hour, from, to));
        }
    }

    private SolarTimes todaySolar() {
        SolarTimes solar = data.solarTimes;
        return solar != null && solar.isForDay(clock.day) ? solar : null;
    }

    private void drawScene(Canvas canvas, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, null, backgroundRect, paint);
        treeSway.draw(canvas, bitmap, backgroundRect, paint, elapsed, data.windKmh);
    }

    private void drawBackdropTrees(Canvas canvas, int from, int to, float mix) {
        float light = TREE_EXPOSURE[from] * (1 - mix) + TREE_EXPOSURE[to] * mix;
        if (WeatherScenario.wet(weatherType)) light *= 0.7f;
        if (WeatherScenario.snow(weatherType)) light *= 0.82f;
        if (Float.isNaN(treeBrightness) || Math.abs(light - treeBrightness) > 0.001f) {
            treeBrightness = light;
            colorMatrix.setScale(light, light, light, 1);
            treePaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        }
        canvas.drawBitmap(backdropTrees, null, backgroundRect, treePaint);
    }

    private void drawAtmosphere(Canvas canvas, float time) {
        if (weatherType >= 1 && weatherType <= 3) {
            paint.setColor(weatherType == 3 ? 0xFF202A34 : 0xFF536271);
            paint.setAlpha(weatherType == 3 ? (time >= 6 && time < 19 ? 100 : 45) : weatherType * 18);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            canvas.save();
            canvas.translate(backgroundRect.left, backgroundRect.top);
            canvas.scale(backgroundRect.width(), backgroundRect.height());
            paint.setAlpha(weatherType == 1 ? 65 : weatherType == 2 ? 125 : 210);
            for (int i = 0; i < 7; i++) {
                float cx = ((elapsed * 0.002f + i * 0.19f) % 1.4f) - 0.2f;
                canvas.save();
                canvas.translate(cx, 0.12f + (i % 3) * 0.085f);
                canvas.scale(1, 0.42f);
                paint.setShader(cloudShader);
                canvas.drawCircle(0, 0, 0.25f, paint);
                paint.setShader(null);
                canvas.restore();
            }
            canvas.restore();
        }
        if (weatherType == 4) {
            float drift = (float) Math.sin(elapsed * 0.13f) * getHeight() * 0.06f;
            paint.setShader(new LinearGradient(0, getHeight()*0.25f + drift, 0, getHeight() + drift,
                new int[]{0x409AAAB8,0xB0B8C7CF,0x207E929F}, null, Shader.TileMode.CLAMP));
            paint.setAlpha(255);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);
        }
        if (weatherType == 11 && elapsed % 13f < 0.16f) {
            paint.setColor(Color.WHITE); paint.setAlpha(55);
            canvas.drawRect(0,0,getWidth(),getHeight(),paint);
        }
        paint.setColor(Color.WHITE); paint.setAlpha(255);
    }

    private void drawInfo(Canvas canvas) {
        // Use a 1920x1080 layout space so TV density does not change text proportions.
        canvas.save();
        canvas.scale(getWidth() / 1920f, getHeight() / 1080f);
        paint.setAlpha(255);
        paint.setShader(overlayShader);
        canvas.drawRect(0, 0, 1920, 440, paint);
        paint.setShader(null);
        textPaint.setColor(Color.WHITE);
        textPaint.setShadowLayer(3, 0, 2, 0xB0000000);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textPaint.setTextSize(88);
        canvas.drawText(clock.clockText, 1850, 122, textPaint);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textPaint.setTextSize(29);
        canvas.drawText(clock.dateText, 1845, 170, textPaint);
        textPaint.setTextSize(24);
        SolarTimes solar = todaySolar();
        canvas.drawText(solar == null ? "G\u00fcne\u015f saatleri  --:--" : solar.label(clock.hour), 1845, 210, textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        infoRow(canvas, "ALANYA", data.alanya, 72);
        tomorrowRow(canvas, data.alanyaTomorrow, 143);
        infoRow(canvas, "KONYA", data.konya, 205);
        tomorrowRow(canvas, data.konyaTomorrow, 276);
        textPaint.setTextSize(15);
        textPaint.setColor(0x80DEE5EC);
        textPaint.setShadowLayer(3, 0, 2, 0x58000000);
        boolean oldData = System.currentTimeMillis() - data.fetchedAt > 15 * 60 * 1000L;
        canvas.drawText("Open-Meteo  " + data.weatherTime + (data.fetchedAt == 0 ? "  / ba\u011flant\u0131 bekleniyor" : info.weatherFailed || oldData ? "  / son veri" : ""), 72, 319, textPaint);
        textPaint.clearShadowLayer();
        canvas.restore();
    }

    private void tomorrowRow(Canvas canvas, String value, float baseline) {
        textPaint.setColor(0xFFE1E9F1);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textPaint.setTextSize(23);
        if (textPaint.measureText(value) > 600) textPaint.setTextSize(20);
        canvas.drawText(value, 72, baseline, textPaint);
    }

    private void infoRow(Canvas canvas, String label, String value, float top) {
        textPaint.setColor(0xFFDCE4EE);
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textPaint.setTextSize(17);
        canvas.drawText(label, 72, top, textPaint);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textPaint.setTextSize(30);
        if (textPaint.measureText(value) > 530) textPaint.setTextSize(24);
        canvas.drawText(value, 72, top + 36, textPaint);
    }
}
