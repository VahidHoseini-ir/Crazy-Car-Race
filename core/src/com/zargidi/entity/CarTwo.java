package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class CarTwo extends Car {

    public CarTwo(Vector2 pos, Vector2 direction) {
        super("CAR2", "R", new Texture(Gdx.files.internal("car2.png")), pos, direction);
    }

    // برای مولتی‌تاچ
    private static final int MAX_POINTERS = 10;
    private boolean[] prevTouched = new boolean[MAX_POINTERS];

    @Override
    public void update() {

        // حرکت بر اساس direction فعلی
        pos.add(direction);

        float leftLaneX  = (5 * Gdx.graphics.getWidth() / 8f) - (carWidth / 2f);
        float rightLaneX = (7 * Gdx.graphics.getWidth() / 8f) - (carWidth / 2f);

        int screenWidth = Gdx.graphics.getWidth();
        float half      = screenWidth / 2f;

        boolean movedThisFrame = false;

        // 🔹 مولتی‌تاچ: تمام pointerها را چک می‌کنیم
        for (int i = 0; i < MAX_POINTERS; i++) {

            boolean nowTouched = Gdx.input.isTouched(i);

            // یعنی این pointer همین الان تازه روی صفحه اومده (justTouched برای این انگشت)
            if (nowTouched && !prevTouched[i]) {

                int touchX = Gdx.input.getX(i);

                // CarTwo فقط لمس نیمه‌ی راست صفحه را گوش می‌دهد
                if (touchX > half) {

                    // حتماً از equals استفاده کن، نه ==
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

            // وضعیت لمس فعلی برای فریم بعدی
            prevTouched[i] = nowTouched;
        }

        // اگر این فریم هیچ تپی برای این ماشین نبود، جهت را صفر کن
        if (!movedThisFrame) {
            setDirection(0, 0);
        }

        // حرکت نرم به سمت targetX
        smoothMoveToTarget(Gdx.graphics.getDeltaTime());

        // آپدیت افکت دود
        this.smokeEffect.setPosition(this.pos.x + carWidth / 2f,
          this.pos.y + carHeight / 10f);
        this.smokeEffect.update(Gdx.graphics.getDeltaTime());
    }
}
