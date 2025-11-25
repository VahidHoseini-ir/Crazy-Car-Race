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

        float difficulty = getDifficultyFactor();
        float spreadMin = MathUtils.lerp(screenHeight * 0.25f, screenHeight * 0.08f, difficulty);
        float spreadMax = MathUtils.lerp(screenHeight * 0.35f, screenHeight * 0.18f, difficulty);
        float spawnY = screenHeight + MathUtils.random(spreadMin, spreadMax);

        Array<Vector2> lanePositions = new Array<Vector2>();
        lanePositions.add(new Vector2((1 * screenWidth / 8f) - entityWidthHalf, spawnY));
        lanePositions.add(new Vector2((3 * screenWidth / 8f) - entityWidthHalf, spawnY));
        lanePositions.add(new Vector2((5 * screenWidth / 8f) - entityWidthHalf, spawnY));
        lanePositions.add(new Vector2((7 * screenWidth / 8f) - entityWidthHalf, spawnY));

        Array<Integer> laneOrder = new Array<Integer>(new Integer[]{0, 1, 2, 3});
        laneOrder.shuffle();

        int treesToSpawn = MathUtils.clamp(1 + MathUtils.round(difficulty * 2.2f), 1, 3);
        int coinsToSpawn = MathUtils.clamp(1 + MathUtils.round(1 + difficulty * 2f), 1, 3);

        Array<Integer> occupiedLanes = new Array<Integer>();

        for (int i = 0; i < treesToSpawn && i < laneOrder.size; i++) {
            int laneIndex = laneOrder.get(i);
            Vector2 spawnPos = lanePositions.get(laneIndex).cpy();
            if ((i + laneIndex) % 2 == 0) {
                treesAndGolds.add(new TreeOne(spawnPos, new Vector2(0, -speed)));
            } else {
                treesAndGolds.add(new TreeTwo(spawnPos, new Vector2(0, -speed)));
            }
            occupiedLanes.add(laneIndex);
        }

        Array<Integer> coinLanes = new Array<Integer>(laneOrder);
        for (Integer occupiedLane : occupiedLanes) {
            coinLanes.removeValue(occupiedLane, false);
        }
        coinLanes.shuffle();

        for (int i = 0; i < coinsToSpawn && i < coinLanes.size; i++) {
            int laneIndex = coinLanes.get(i);
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

}
