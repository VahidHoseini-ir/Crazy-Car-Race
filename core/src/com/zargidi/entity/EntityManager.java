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
    private float spawnTimer = 0f;
    private float baseSpawnInterval = 1.35f;
    private float minSpawnInterval = 0.55f;

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

        float scoreBoardX = (screenWidth - TextureManager.SCOREBOARD.getWidth()) / 2f;
        float scoreBoardY = Gdx.graphics.getHeight() - (TextureManager.SCOREBOARD.getHeight()) - (Gdx.graphics.getHeight() / 30f);
        scoreBoard = new ScoreBoard(new Vector2(scoreBoardX, scoreBoardY), new Vector2(0, 0));

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

        carOne.update();
        carTwo.update();

        for (Entity e : treesAndGolds) {
            e.update();
        }

        goldEffect.update(Gdx.graphics.getDeltaTime());
        scoreBoard.update();


    }


    public void render(SpriteBatch sb) {

        spawnTimer += Gdx.graphics.getDeltaTime();
        if (spawnTimer >= calculateSpawnInterval()) {
            removeUnUsedEntities();
            createGoldsAndTrees();
            spawnTimer = 0f;
        }

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

        int lane = MathUtils.random(1, getMaxLanePattern());

        float difficultyBonus = Math.min(screenHeight / 10f, MainGame.scoreCurrent * screenHeight / 600f);
        float minOffset = (screenHeight / 6f) + difficultyBonus / 2f;
        float maxOffset = (screenHeight / 3f) + difficultyBonus;

        float fallSpeed = getCurrentFallSpeed();

        Vector2 lane1V = new Vector2((1 * screenWidth / 8) - entityWidthHalf, screenHeight + MathUtils.random(minOffset, maxOffset));
        Vector2 lane2V = new Vector2((3 * screenWidth / 8) - entityWidthHalf, screenHeight + MathUtils.random(minOffset, maxOffset));
        Vector2 lane3V = new Vector2((5 * screenWidth / 8) - entityWidthHalf, screenHeight + MathUtils.random(minOffset, maxOffset));
        Vector2 lane4V = new Vector2((7 * screenWidth / 8) - entityWidthHalf, screenHeight + MathUtils.random(minOffset, maxOffset));


        if (lane == 1) {
            treesAndGolds.add(new TreeOne(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane3V, new Vector2(0, -fallSpeed)));

        } else if (lane == 2) {
            treesAndGolds.add(new TreeTwo(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 3) {
            treesAndGolds.add(new TreeOne(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane2V, new Vector2(0, -fallSpeed)));

        } else if (lane == 4) {
            treesAndGolds.add(new TreeOne(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 5) {
            treesAndGolds.add(new TreeOne(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane1V, new Vector2(0, -fallSpeed)));

        } else if (lane == 6) {
            treesAndGolds.add(new TreeOne(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 7) {
            treesAndGolds.add(new TreeOne(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 8 || lane == 9) {
            treesAndGolds.add(new TreeTwo(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeOne(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 10 || lane == 11) {
            treesAndGolds.add(new TreeOne(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 12 || lane == 13) {
            treesAndGolds.add(new CoinGold(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeOne(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane4V, new Vector2(0, -fallSpeed)));

        } else if (lane == 14 || lane == 15) {
            treesAndGolds.add(new CoinGold(lane1V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeTwo(lane2V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new TreeOne(lane3V, new Vector2(0, -fallSpeed)));
            treesAndGolds.add(new CoinGold(lane4V, new Vector2(0, -fallSpeed)));

        }

    }

    private float calculateSpawnInterval() {
        float difficulty = MathUtils.clamp(MainGame.scoreCurrent / 25f, 0f, 10f);
        float interval = baseSpawnInterval - (difficulty * 0.08f);
        return MathUtils.clamp(interval, minSpawnInterval, baseSpawnInterval);
    }

    private int getMaxLanePattern() {
        int patternBoost = MathUtils.clamp(MainGame.scoreCurrent / 10, 0, 8);
        return MathUtils.clamp(4 + patternBoost, 5, 15);
    }

    private float getCurrentFallSpeed() {
        float boost = MathUtils.clamp(MainGame.scoreCurrent * 0.15f, 0f, speed * 0.8f);
        return speed + boost;
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
