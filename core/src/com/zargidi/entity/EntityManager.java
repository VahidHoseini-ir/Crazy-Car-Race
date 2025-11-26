package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.zargidi.ccar.MainGame;
import com.zargidi.ccar.TextureManager;
import com.zargidi.screen.GameScreen;

public class EntityManager {

    private CarOne carOne;
    private CarTwo carTwo;
    private ScoreBoard scoreBoard;

    // فقط TreeOne / TreeTwo به‌عنوان آیتم امتیازی
    private Array<Entity> items;

    private Sound collectSound;
    private FileHandle collectSoundFile;

    private boolean gameOver = false;

    private float spawnTimerSeconds = 0f;

    private int c1x = 100;
    private int c2x = 330;

    // برای ایجاد موجودیت‌ها
    private static int speed;
    private static int screenHeight;
    private static int screenWidth;

    // برای محاسبات فاصله عمودی و حذف
    private static int baseItemHeight;

    // فاصله‌ی زمانی بین موج‌ها (هر موج = ۱ یا ۲ آیتم)
    private static final float MAX_SPAWN_INTERVAL = 1.1f;  // اوایل بازی
    private static final float MIN_SPAWN_INTERVAL = 0.45f; // اواخر بازی

    // تنظیم hitbox (هرچه کمتر → برخورد سخت‌تر / دقیق‌تر)
    private static final float HITBOX_SCALE_X = 0.55f; // ۵۵٪ عرض اصلی
    private static final float HITBOX_SCALE_Y = 0.55f; // ۵۵٪ ارتفاع اصلی

    public EntityManager() {
        speed = GameScreen.speed;
        screenHeight = Gdx.graphics.getHeight();
        screenWidth = Gdx.graphics.getWidth();

        // برای فاصله‌ها از ارتفاع tree1 استفاده می‌کنیم
        baseItemHeight = TextureManager.TREE1.getHeight();

        // جای اولیهٔ ماشین‌ها
        c1x = (1 * (screenWidth / 8)) - (TextureManager.CAR1.getWidth() / 2);
        c2x = (5 * (screenWidth / 8)) - (TextureManager.CAR2.getWidth() / 2);

        carOne = new CarOne(new Vector2(c1x, 20), new Vector2(0, 0));
        carTwo = new CarTwo(new Vector2(c2x, 20), new Vector2(0, 0));

        items = new Array<Entity>();

        // امتیاز بالای تصویر
        scoreBoard = new ScoreBoard(new Vector2(screenWidth / 2f, screenHeight), new Vector2(0, 0));

        // صدای جمع کردن آیتم (اگر فعلاً نمی‌خوای، می‌تونی play رو کامنت کنی)
        collectSoundFile = Gdx.files.internal("sounds/coin_gold_sound.wav");
        collectSound = Gdx.audio.newSound(collectSoundFile);

        // warm-up برای جلوگیری از لگ اولین پخش
        collectSound.play(0f);
        collectSound.stop();
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        spawnTimerSeconds += delta;

        // هر چند ثانیه یک موج جدید آیتم
        if (spawnTimerSeconds >= getCurrentSpawnInterval()) {
            createItemWave();
            spawnTimerSeconds = 0f;
        }

        carOne.update();
        carTwo.update();

        for (Entity e : items) {
            e.update();
        }

        // منطق گرفتن / از دست دادن آیتم‌ها
        checkItemLogic();

        scoreBoard.update();

        // پاک‌سازی آیتم‌های خیلی پایین (احتیاط)
        removeUnUsedEntities();
    }

    /** وقتی سرعت از GameScreen عوض شد، جهت حرکت آیتم‌ها هم باید تندتر شود */
    public void onSpeedChanged(int newSpeed) {
        speed = newSpeed;

        for (Entity entity : items) {
            entity.direction.y = -newSpeed;
        }
    }

    /**
     * سختی بر اساس امتیاز:
     *  - تا ۱۰: ۰ (خیلی راحت)
     *  - از ۱۰ تا حدود ۲۰۰: به تدریج  → ۱
     *  - بالاتر از ۲۰۰: ۱
     */
    private float getDifficultyFactor() {
        if (MainGame.scoreCurrent <= 10) {
            return 0f;
        }
        float t = (MainGame.scoreCurrent - 10f) / 190f;
        return MathUtils.clamp(t, 0f, 1f);
    }

    /** فاصلهٔ عمودی بین موج‌ها (هرچه سخت‌تر → موج‌ها نزدیک‌تر) */
    private float getPatternSpacing() {
        float maxSpacing = screenHeight * 0.75f;  // اوایل: خلوت
        float minSpacing = screenHeight * 0.35f;  // اواخر: شلوغ‌تر
        return MathUtils.lerp(maxSpacing, minSpacing, getDifficultyFactor());
    }

    /** فاصله‌ی زمانی بین موج‌ها (با کمی تصادفی بودن برای غیرقابل پیش‌بینی شدن) */
    private float getCurrentSpawnInterval() {
        float diff = getDifficultyFactor();
        float base = MathUtils.lerp(MAX_SPAWN_INTERVAL, MIN_SPAWN_INTERVAL, diff);

        // ضرب در یک فاکتور رندوم ۰.۸ تا ۱.۲ → ریتم موج‌ها یکنواخت نیست
        float randomFactor = MathUtils.random(0.8f, 1.2f);
        return base * randomFactor;
    }

    public void render(SpriteBatch sb) {
        carOne.render(sb);
        carTwo.render(sb);

        for (Entity e : items) {
            e.render(sb);
        }

        scoreBoard.render(sb);
    }

    /**
     * یک موج آیتم می‌سازیم:
     * - ۴ لاین داریم (۰ و ۱ سمت چپ، ۲ و ۳ سمت راست)
     * - سمت چپ فقط TreeOne می‌آید
     * - سمت راست فقط TreeTwo می‌آید
     * - بسته به سختی: گاهی فقط چپ، گاهی فقط راست، گاهی هر دو
     * - ارتفاع‌ها کمی رندوم تا طبیعی‌تر شود
     */
    private void createItemWave() {

        float baseY = screenHeight + getPatternSpacing();

        // ۴ لاین
        Array<Vector2> lanePositions = new Array<Vector2>();
        int halfTreeWidth = TextureManager.TREE1.getWidth() / 2;

        lanePositions.add(new Vector2((1 * screenWidth / 8f) - halfTreeWidth, baseY)); // lane 0
        lanePositions.add(new Vector2((3 * screenWidth / 8f) - halfTreeWidth, baseY)); // lane 1
        lanePositions.add(new Vector2((5 * screenWidth / 8f) - halfTreeWidth, baseY)); // lane 2
        lanePositions.add(new Vector2((7 * screenWidth / 8f) - halfTreeWidth, baseY)); // lane 3

        float diff = getDifficultyFactor();

        // احتمال این‌که در یک موج، "هم چپ، هم راست" آیتم داشته باشیم
        float bothProb = MathUtils.lerp(0.10f, 0.6f, diff);
        float r = MathUtils.random();

        boolean spawnLeft = false;
        boolean spawnRight = false;

        if (r < bothProb) {
            // هر دو طرف آیتم داشته باشند
            spawnLeft = true;
            spawnRight = true;
        } else {
            // فقط یک طرف (یا چپ یا راست)
            if (MathUtils.randomBoolean()) {
                spawnLeft = true;
            } else {
                spawnRight = true;
            }
        }

        float baseOffset = baseItemHeight * MathUtils.random(0.4f, 1.1f);
        boolean anyItem = false;

        // --- جادهٔ چپ: لاین‌های ۰ و ۱ → TreeOne ---
        if (spawnLeft) {
            int laneIndex = MathUtils.randomBoolean() ? 0 : 1;
            Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
            float localOffset = baseOffset + baseItemHeight * MathUtils.random(-0.2f, 0.3f);
            spawnPos.y += localOffset;

            items.add(new TreeOne(spawnPos, new Vector2(0, -speed)));
            anyItem = true;
        }

        // --- جادهٔ راست: لاین‌های ۲ و ۳ → TreeTwo ---
        if (spawnRight) {
            int laneIndex = MathUtils.randomBoolean() ? 2 : 3;
            Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
            float rightOffset = baseOffset + baseItemHeight * MathUtils.random(0.0f, 0.6f);
            spawnPos.y += rightOffset;

            items.add(new TreeTwo(spawnPos, new Vector2(0, -speed)));
            anyItem = true;
        }

        // اگر به هر دلیلی آیتمی ساخته نشد، یکی بسازیم که بازی خالی نشود
        if (!anyItem) {
            boolean leftSide = MathUtils.randomBoolean();
            if (leftSide) {
                int laneIndex = MathUtils.randomBoolean() ? 0 : 1;
                Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
                spawnPos.y += baseItemHeight * 0.7f;
                items.add(new TreeOne(spawnPos, new Vector2(0, -speed)));
            } else {
                int laneIndex = MathUtils.randomBoolean() ? 2 : 3;
                Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
                spawnPos.y += baseItemHeight * 0.7f;
                items.add(new TreeTwo(spawnPos, new Vector2(0, -speed)));
            }
        }
    }

    /** hitbox را کوچیک‌تر از کل تکسچر می‌کنیم تا برخورد طبیعی‌تر بشود */
    private Rectangle scaledRect(Rectangle r, float scaleX, float scaleY) {
        float newW = r.width * scaleX;
        float newH = r.height * scaleY;
        float newX = r.x + (r.width - newW) / 2f;
        float newY = r.y + (r.height - newH) / 2f;
        return new Rectangle(newX, newY, newW, newH);
    }

    /**
     * منطق آیتم‌ها:
     * - اگر ماشین‌ها به TreeOne / TreeTwo بخورند → امتیاز + صدا + حذف آیتم
     * - اگر آیتم از پایین صفحه رد شود بدون این‌که گرفته شود → gameOver
     */
    private void checkItemLogic() {

        // hitbox کوچک‌شدهٔ ماشین‌ها
        Rectangle car1Box = scaledRect(carOne.getBounds(), HITBOX_SCALE_X, HITBOX_SCALE_Y);
        Rectangle car2Box = scaledRect(carTwo.getBounds(), HITBOX_SCALE_X, HITBOX_SCALE_Y);

        for (int i = 0; i < items.size; i++) {
            Entity e = items.get(i);

            if (!(e instanceof TreeOne) && !(e instanceof TreeTwo)) {
                continue;
            }

            Rectangle itemBox = scaledRect(e.getBounds(), HITBOX_SCALE_X, HITBOX_SCALE_Y);

            boolean collected = car1Box.overlaps(itemBox) || car2Box.overlaps(itemBox);

            if (collected) {
                MainGame.scoreCurrent++;
                // اگر صدا اذیت می‌کند، این خط را کامنت کن
                // collectSound.play(0.7f);

                items.removeIndex(i);
                i--;

                if (MainGame.scoreCurrent > 9998) {
                    MainGame.gameOver = true;
                    MainGame.gameWin = true;
                }
                continue;
            }

            // اگر آیتم کامل از پایین صفحه رد شود و هنوز گرفته نشده → باخت
            if (e.pos.y + baseItemHeight < 0) {
                if (!MainGame.debug) {
                    MainGame.gameOver = true;
                }
                items.removeIndex(i);
                i--;
            }
        }
    }

    /** پاک‌سازی چیزهای خیلی دور از صفحه (صرفاً برای تمیز بودن حافظه) */
    public void removeUnUsedEntities() {
        for (int i = items.size - 1; i >= 0; i--) {
            Entity e = items.get(i);
            if (e == null || e.pos.y < -baseItemHeight * 2f) {
                items.removeIndex(i);
            }
        }
    }

    public boolean isGameOver() {
        return gameOver || MainGame.gameOver;
    }
}
