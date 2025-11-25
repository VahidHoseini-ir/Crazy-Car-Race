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


/**
 * Created by ilimturan on 18/01/15.
 */
public class EntityManager {

    private CarOne carOne;
    private CarTwo carTwo;
    private ScoreBoard scoreBoard;
    private Array<Entity> treesAndGolds;

    private static class SpawnPattern {
        final int[] treeLanes;
        final int[] coinLanes;

        SpawnPattern(int[] treeLanes, int[] coinLanes) {
            this.treeLanes = treeLanes;
            this.coinLanes = coinLanes;
        }
    }

    private final Array<Array<SpawnPattern>> stagedPatterns = new Array<Array<SpawnPattern>>();
    private int patternCursor = 0;
    private int lastStage = 0;
    private boolean useTreeOneNext = true;

    private Sound coinGoldSound;

    private ParticleEffect goldEffect;
    private FileHandle goldEffectFile;
    private FileHandle goldEffectImg;
    private FileHandle goldEffectSound;

    private boolean gameOver = false;

    private float spawnTimerSeconds = 0f;

    private int c1x = 100;
    private int c2x = 330;

    /**
     * for create entities
     */
    private static int speed;
    private static int screenHeight;
    private static int screenWidth;
    private static int entityHeight;
    private static int entityWidthHalf;

    private static final float MAX_SPAWN_INTERVAL = 1.3f;
    private static final float MIN_SPAWN_INTERVAL = 0.55f;

    public EntityManager() {
        /**
         * for create entities
         */
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

        setupSpawnPatterns();

        //Gold effects
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


    private float getDifficultyFactor() {
        return MathUtils.clamp(MainGame.scoreCurrent / 200f, 0f, 1f);
    }

    private int getStageByScore() {
        return MathUtils.clamp(MainGame.scoreCurrent / 50, 0, stagedPatterns.size - 1);
    }

    private float getPatternSpacing() {
        // Keep early spacing generous, tighten slowly as speed and score rise.
        return MathUtils.lerp(screenHeight * 0.32f, screenHeight * 0.2f, getDifficultyFactor());
    }

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

        float spawnY = screenHeight + getPatternSpacing();

        Array<Vector2> lanePositions = new Array<Vector2>();
        lanePositions.add(new Vector2((1 * screenWidth / 8f) - entityWidthHalf, spawnY));
        lanePositions.add(new Vector2((3 * screenWidth / 8f) - entityWidthHalf, spawnY));
        lanePositions.add(new Vector2((5 * screenWidth / 8f) - entityWidthHalf, spawnY));
        lanePositions.add(new Vector2((7 * screenWidth / 8f) - entityWidthHalf, spawnY));

        SpawnPattern pattern = getNextPattern();

        for (int laneIndex : pattern.treeLanes) {
            Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
            if (useTreeOneNext) {
                treesAndGolds.add(new TreeOne(spawnPos, new Vector2(0, -speed)));
            } else {
                treesAndGolds.add(new TreeTwo(spawnPos, new Vector2(0, -speed)));
            }
            useTreeOneNext = !useTreeOneNext;
        }

        for (int laneIndex : pattern.coinLanes) {
            Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
            spawnPos.y += entityHeight * 0.6f;
            treesAndGolds.add(new CoinGold(spawnPos, new Vector2(0, -speed)));
        }

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

    private void setupSpawnPatterns() {
        // Stage 0: intro, single obstacles with safe coin options
        Array<SpawnPattern> stage0 = new Array<SpawnPattern>();
        stage0.add(new SpawnPattern(new int[]{1}, new int[]{0, 3}));
        stage0.add(new SpawnPattern(new int[]{2}, new int[]{0, 1}));
        stage0.add(new SpawnPattern(new int[]{0}, new int[]{2, 3}));

        // Stage 1 (50+): introduce paired trees but keep plenty of breathing room
        Array<SpawnPattern> stage1 = new Array<SpawnPattern>();
        stage1.add(new SpawnPattern(new int[]{1}, new int[]{0, 2}));
        stage1.add(new SpawnPattern(new int[]{0, 3}, new int[]{1}));
        stage1.add(new SpawnPattern(new int[]{2}, new int[]{0, 1, 3}));
        stage1.add(new SpawnPattern(new int[]{1, 2}, new int[]{0}));

        // Stage 2 (100+): mostly two trees, occasional third lane blocked lightly
        Array<SpawnPattern> stage2 = new Array<SpawnPattern>();
        stage2.add(new SpawnPattern(new int[]{0, 2}, new int[]{1, 3}));
        stage2.add(new SpawnPattern(new int[]{1, 3}, new int[]{0, 2}));
        stage2.add(new SpawnPattern(new int[]{0, 2, 3}, new int[]{1}));
        stage2.add(new SpawnPattern(new int[]{1, 2}, new int[]{0, 3}));

        // Stage 3 (150+): up to three trees but still one lane plus coins as relief
        Array<SpawnPattern> stage3 = new Array<SpawnPattern>();
        stage3.add(new SpawnPattern(new int[]{0, 1, 3}, new int[]{2}));
        stage3.add(new SpawnPattern(new int[]{0, 2, 3}, new int[]{1}));
        stage3.add(new SpawnPattern(new int[]{1, 2}, new int[]{0, 3}));
        stage3.add(new SpawnPattern(new int[]{0, 2}, new int[]{1, 3}));

        // Stage 4+ (200+): gentle bump in density; cap growth to avoid spikes
        Array<SpawnPattern> stage4 = new Array<SpawnPattern>();
        stage4.add(new SpawnPattern(new int[]{0, 1, 2}, new int[]{3}));
        stage4.add(new SpawnPattern(new int[]{1, 2, 3}, new int[]{0}));
        stage4.add(new SpawnPattern(new int[]{0, 2, 3}, new int[]{1}));
        stage4.add(new SpawnPattern(new int[]{0, 1, 3}, new int[]{2}));

        stagedPatterns.add(stage0);
        stagedPatterns.add(stage1);
        stagedPatterns.add(stage2);
        stagedPatterns.add(stage3);
        stagedPatterns.add(stage4);
    }

    private SpawnPattern getNextPattern() {
        int stage = getStageByScore();

        if (stage != lastStage) {
            patternCursor = 0;
            lastStage = stage;
        }

        Array<SpawnPattern> patterns = stagedPatterns.get(stage);
        SpawnPattern pattern = patterns.get(patternCursor);
        patternCursor = (patternCursor + 1) % patterns.size;
        return pattern;
    }

}
