package com.zargidi.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.zargidi.camera.OrthoCamera;
import com.zargidi.ccar.MainGame;
import com.zargidi.entity.EntityManager;

public class GameScreen extends Screen {

    private OrthoCamera camera;
    private EntityManager entityManager;

    private Pixmap px;
    private Texture backgroundImg1;
    private Texture backgroundImg2;

    // سرعتی که بقیه جاها هم استفاده می‌کنن
    public static int speed;

    // سرعت‌های پایه
    private float slowSpeed;   // سرعت آسان (ابتدای بازی)
    private float fastSpeed;   // حداکثر سرعت (وقتی بازی خیلی سخت می‌شود)

    private int moveY = 0;

    // تنظیمات سخت شدن بر اساس امتیاز
    private static final int SPEED_START_SCORE = 10;   // تا این امتیاز سرعت ثابت
    private static final int MAX_DIFFICULTY_SCORE = 200; // بعد از این امتیاز به حداکثر سرعت می‌رسیم

    @Override
    public void create() {

        // می‌تونیم سرعت را نسبی بر اساس ارتفاع صفحه تنظیم کنیم
        int sh = Gdx.graphics.getHeight();

        // این‌ها را اگر بازی خیلی کند/تیز بود، فقط همین تقسیم‌ها را عوض کن
        slowSpeed = sh / 200f;   // سرعت راحت
        fastSpeed = sh / 60f;    // سرعت سخت

        // سرعت اولیه
        speed = Math.max(1, Math.round(slowSpeed));

        // امتیازها
        MainGame.scoreCurrent = 0;
        if (MainGame.scoreHigh == null) {
            MainGame.scoreHigh = 0;
        }

        camera = new OrthoCamera();
        entityManager = new EntityManager();

        // لود بک‌گراند
        px = new Pixmap(Gdx.files.internal("background.png"));
        backgroundImg1 = new Texture(px);
        backgroundImg1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        backgroundImg2 = backgroundImg1;
    }

    @Override
    public void update() {
        camera.update();
        updateSpeedByScore();  // ⬅️ اینجا بر اساس امتیاز سخت می‌کنیم
        entityManager.update();
    }

    /**
     * سرعت بازی بر اساس امتیاز زیاد می‌شود.
     * ۰ تا ۴۰ → سرعت = slowSpeed
     * ۴۰ تا ۲۰۰ → به‌تدریج از slowSpeed به fastSpeed
     * بالاتر از ۲۰۰ → سرعت = fastSpeed
     */
    private void updateSpeedByScore() {
        int score = MainGame.scoreCurrent;

        float t; // بین 0 و 1 برای LERP

        if (score <= SPEED_START_SCORE) {
            // هنوز زوده برای سخت شدن → سرعت آسان ثابت
            t = 0f;
        } else {
            // از ۴۰ به بعد شروع کن کم‌کم سخت‌تر کردن
            float effectiveScore = score - SPEED_START_SCORE;
            float range = MAX_DIFFICULTY_SCORE - SPEED_START_SCORE; // 200 - 40 = 160

            t = effectiveScore / range; // نرمال‌سازی بین 0 و 1
            if (t > 1f) t = 1f;         // بیشتر از 1 نشود
        }

        // سرعت هدف بین slowSpeed و fastSpeed
        float targetSpeed = MathUtils.lerp(slowSpeed, fastSpeed, t);

        int updatedSpeed = Math.max(1, Math.round(targetSpeed));
        if (updatedSpeed != speed) {
            speed = updatedSpeed;
            // اگر موجودات بازی به سرعت وابسته‌اند
            entityManager.onSpeedChanged(speed);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(camera.combined);

        int backW = Gdx.graphics.getWidth();
        int backH = Gdx.graphics.getHeight();

        // حرکت بک‌گراند مثل قبل، بر اساس سرعت (بدون deltaTime)
        moveY += speed;

        // ✅ قبل از رسم، wrap کنیم تا همیشه 0 <= moveY < backH
        if (moveY >= backH) {
            moveY = moveY % backH;   // یا moveY -= backH;
        }

        sb.begin();
        sb.draw(backgroundImg1, 0, -moveY, backW, backH);
        sb.draw(backgroundImg2, 0, backH - moveY, backW, backH);
        sb.end();

        sb.begin();
        entityManager.render(sb);
        sb.end();
    }


    @Override
    public void resize(int width, int height) {
        camera.resize();
    }

    @Override
    public void dispose() {
        if (backgroundImg1 != null) backgroundImg1.dispose();
        if (px != null) px.dispose();
        Gdx.app.exit();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
