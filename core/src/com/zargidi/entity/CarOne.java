package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class CarOne extends Car {

    public CarOne(Vector2 pos, Vector2 direction) {
        super("CAR1", "L", new Texture(Gdx.files.internal("car1.png")), pos, direction);
    }

    private static final int MAX_POINTERS = 10;
    private boolean[] prevTouched = new boolean[MAX_POINTERS];

    @Override
    public void update() {

        // حرکت بر اساس direction فعلی
        pos.add(direction);

        float leftLaneX  = (1 * Gdx.graphics.getWidth() / 8f) - (carWidth / 2f);
        float rightLaneX = (3 * Gdx.graphics.getWidth() / 8f) - (carWidth / 2f);

        int screenWidth = Gdx.graphics.getWidth();
        float half      = screenWidth / 2f;

        boolean movedThisFrame = false;

        // 🔹 مولتی‌تاچ واقعی — بررسی همه‌ی انگشت‌ها
        for (int i = 0; i < MAX_POINTERS; i++) {

            boolean nowTouched = Gdx.input.isTouched(i);

            // فقط وقتی تازه لمس شده باشد
            if (nowTouched && !prevTouched[i]) {

                int touchX = Gdx.input.getX(i);

                // فقط لمس نیمه چپ برای CarOne
                if (touchX < half) {

                    if ("L".equals(lane)) {
                        targetX = rightLaneX;
                        lane = "R";
                    } else {
                        targetX = leftLaneX;
                        lane = "L";
                    }

                    movedThisFrame = true;
                }
            }

            // ذخیره وضعیت فعلی برای فریم بعد
            prevTouched[i] = nowTouched;
        }

        // اگر این فریم هیچ تپی نبود → حرکت نکن
        if (!movedThisFrame) {
            setDirection(0, 0);
        }

        // حرکت نرم
        smoothMoveToTarget(Gdx.graphics.getDeltaTime());

        // افکت دود
        this.smokeEffect.setPosition(this.pos.x + carWidth / 2f,
          this.pos.y + carHeight / 10f);
        this.smokeEffect.update(Gdx.graphics.getDeltaTime());
    }
}
