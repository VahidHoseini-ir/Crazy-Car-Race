package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.zargidi.ccar.MainGame;
import com.zargidi.ccar.TextureManager;
import com.zargidi.screen.GameScreen;

public class EntityManager {

    private CarOne carOne;
    private CarTwo carTwo;
    private ScoreBoard scoreBoard;
    private Array<Entity> treesAndGolds;

    private Sound coinGoldSound;

    private ParticleEffect goldEffect;
    private FileHandle goldEffectFile;
    private FileHandle goldEffectImg;
    private FileHandle goldEffectSound;

    private boolean gameOver = false;

    private float spawnTimerSeconds = 0f;

    private int c1x = 100;
    private int c2x = 330;

    // برای ایجاد موجودیت‌ها
    private static int speed;
    private static int screenHeight;
    private static int screenWidth;
    private static int entityHeight;
    private static int entityWidthHalf;

    // فاصله‌ی زمانی بین موج‌ها (هر موج: چند درخت و سکه)
    private static final float MAX_SPAWN_INTERVAL = 2.3f;  // اوایل بازی: موج‌های کم
    private static final float MIN_SPAWN_INTERVAL = 0.9f;  // اواخر بازی: موج‌های بیشتر

    // برای تنوع در نوع درخت
    private boolean useTreeOneNext = true;

    public EntityManager() {
        speed = GameScreen.speed;
        screenHeight = Gdx.graphics.getHeight();
        screenWidth = Gdx.graphics.getWidth();
        entityHeight = TextureManager.COINGOLD.getHeight();
        entityWidthHalf = TextureManager.COINGOLD.getWidth() / 2;

        c1x = (1 * (screenWidth / 8)) - (TextureManager.CAR1.getWidth() / 2);
        c2x = (5 * (screenWidth / 8)) - (TextureManager.CAR2.getWidth() / 2);

        carOne = new CarOne(new Vector2(c1x, 20), new Vector2(0, 0));
        carTwo = new CarTwo(new Vector2(c2x, 20), new Vector2(0, 0));

        treesAndGolds = new Array<Entity>();

        scoreBoard = new ScoreBoard(new Vector2(screenWidth / 2f, screenHeight), new Vector2(0, 0));

        // Gold effects
        goldEffectFile = Gdx.files.internal("effects/get_golds_1.p");
        goldEffectImg = Gdx.files.internal("effect_img");
        goldEffect = new ParticleEffect();
        goldEffect.load(goldEffectFile, goldEffectImg);

        // Gold sound
        goldEffectSound = Gdx.files.internal("sounds/coin_gold_sound.mp3");
        coinGoldSound = Gdx.audio.newSound(goldEffectSound);
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        spawnTimerSeconds += delta;
        removeUnUsedEntities();

        if (spawnTimerSeconds >= getCurrentSpawnInterval()) {
            createGoldsAndTrees();
            spawnTimerSeconds = 0f;
        }

        carOne.update();
        carTwo.update();

        for (Entity e : treesAndGolds) {
            e.update();
        }

        goldEffect.update(delta);
        scoreBoard.update();
    }

    public void onSpeedChanged(int newSpeed) {
        speed = newSpeed;

        for (Entity entity : treesAndGolds) {
            entity.direction.y = -newSpeed;
        }
    }

    /**
     * سختی بازی بر اساس امتیاز
     *  <= 40  : 0
     *  حدودا 200 به بعد : 1
     */
    private float getDifficultyFactor() {
        if (MainGame.scoreCurrent <= 40) {
            return 0f;
        }
        float t = (MainGame.scoreCurrent - 40f) / 160f; // از 40 تا 200
        return MathUtils.clamp(t, 0f, 1f);
    }

    /** فاصله عمودی بین موج‌ها (هرچه سخت‌تر → فاصله کمتر) */
    private float getPatternSpacing() {
        float maxSpacing = screenHeight * 0.9f; // اوایل: تقریبا یک صفحه فاصله
        float minSpacing = screenHeight * 0.45f; // اواخر: نصف صفحه
        return MathUtils.lerp(maxSpacing, minSpacing, getDifficultyFactor());
    }

    /** فاصله زمانی بین موج‌ها (هرچه سخت‌تر → موج‌های بیشتر) */
    private float getCurrentSpawnInterval() {
        return MathUtils.lerp(MAX_SPAWN_INTERVAL, MIN_SPAWN_INTERVAL, getDifficultyFactor());
    }

    public void render(SpriteBatch sb) {
        carOne.render(sb);
        carTwo.render(sb);
        goldEffect.draw(sb);

        for (Entity e : treesAndGolds) {
            e.render(sb);
        }

        checkCollisions();
        scoreBoard.render(sb);
    }

    public void createGoldsAndTrees() {

        float baseY = screenHeight + getPatternSpacing();

        // 4 لاین عرضی
        Array<Vector2> lanePositions = new Array<Vector2>();
        lanePositions.add(new Vector2((1 * screenWidth / 8f) - entityWidthHalf, baseY)); // lane 0
        lanePositions.add(new Vector2((3 * screenWidth / 8f) - entityWidthHalf, baseY)); // lane 1
        lanePositions.add(new Vector2((5 * screenWidth / 8f) - entityWidthHalf, baseY)); // lane 2
        lanePositions.add(new Vector2((7 * screenWidth / 8f) - entityWidthHalf, baseY)); // lane 3

        boolean[] laneHasTree = new boolean[4];
        float diff = getDifficultyFactor();

        // --- 1) درخت‌ها: هوشمند و رندوم ---

        // برای هر جاده (0,1) و (2,3) تصمیم می‌گیریم درخت بزاریم یا نه
        boolean anyTree = false;

        for (int road = 0; road < 2; road++) {
            int laneA = (road == 0) ? 0 : 2;
            int laneB = laneA + 1;

            // احتمال وجود درخت روی این جاده
            float treeProb;
            if (diff < 0.25f) {
                treeProb = (road == 0) ? 0.7f : 0.4f;  // اوایل بیشتر سمت ماشین اصلی
            } else if (diff < 0.7f) {
                treeProb = 0.85f;
            } else {
                treeProb = 0.95f; // اواخر تقریبا همیشه درخت
            }

            if (MathUtils.randomBoolean(treeProb)) {
                int laneIndex = MathUtils.randomBoolean() ? laneA : laneB;

                // yOffset کم تا دو درخت دقیق زیر هم نیفتن
                float yOffset = (road == 0) ? 0f : entityHeight * 1.2f;

                addTreeToLane(lanePositions, laneIndex, yOffset);
                laneHasTree[laneIndex] = true;
                anyTree = true;

                // در سختی‌های بالا، احتمال درخت دوم روی همین جاده
                if (diff > 0.6f && MathUtils.randomBoolean(0.4f)) {
                    int otherLane = (laneIndex == laneA) ? laneB : laneA;
                    addTreeToLane(lanePositions, otherLane, yOffset + entityHeight * 0.6f);
                    laneHasTree[otherLane] = true;
                }
            }
        }

        // اگر به هر دلیلی هیچ درختی ساخته نشد، حداقل یکی بسازیم
        if (!anyTree) {
            int laneIndex = MathUtils.random(0, 3);
            addTreeToLane(lanePositions, laneIndex, 0f);
            laneHasTree[laneIndex] = true;
        }

        // --- 2) سکه‌ها: فقط روی لاین‌هایی که درخت ندارند و رندوم ---

        // اوایل سکه زیاد، بعدا کمتر
        float baseCoinChance = 0.8f;
        float minCoinChance = 0.25f;
        float coinChance = MathUtils.lerp(baseCoinChance, minCoinChance, diff);

        for (int laneIndex = 0; laneIndex < lanePositions.size; laneIndex++) {
            if (laneHasTree[laneIndex]) continue; // روی درخت سکه نذار

            if (MathUtils.randomBoolean(coinChance)) {
                Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
                // کمی بالا یا پایین‌بردن سکه برای طبیعی شدن
                spawnPos.y += entityHeight * MathUtils.random(0.4f, 1.0f);
                treesAndGolds.add(new CoinGold(spawnPos, new Vector2(0, -speed)));
            }
        }
    }

    private void addTreeToLane(Array<Vector2> lanePositions, int laneIndex, float yOffset) {
        if (laneIndex < 0 || laneIndex >= lanePositions.size) {
            return;
        }

        Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
        spawnPos.y += yOffset;

        if (useTreeOneNext) {
            treesAndGolds.add(new TreeOne(spawnPos, new Vector2(0, -speed)));
        } else {
            treesAndGolds.add(new TreeTwo(spawnPos, new Vector2(0, -speed)));
        }
        useTreeOneNext = !useTreeOneNext;
    }

    private void checkCollisions() {
        for (Entity e : treesAndGolds) {
            if (carOne.getBounds().contains(e.getBounds()) || carTwo.getBounds().contains(e.getBounds())) {

                if (e instanceof TreeOne || e instanceof TreeTwo) {

                    if (!MainGame.debug) {
                        MainGame.gameOver = true;
                    }
                    e.pos.y = -300;

                } else if (e instanceof CoinGold) {

                    MainGame.scoreCurrent++;
                    coinGoldSound.play();
                    goldEffect.setPosition(e.pos.x, e.pos.y);
                    goldEffect.start();

                    e.pos.y = -300;

                    if (MainGame.scoreCurrent > 9998) {
                        MainGame.gameOver = true;
                        MainGame.gameWin = true;
                    }
                }
            }
        }
    }

    public void removeUnUsedEntities() {
        int index = 0;
        for (Entity e : treesAndGolds) {
            if (e == null) {
                treesAndGolds.removeIndex(index);
            } else if (e.pos.y < -200) {
                treesAndGolds.removeIndex(index);
            }
            index++;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
