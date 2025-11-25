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

/**
 * Created by ilimturan on 18/01/15.
 */
public class GameScreen extends Screen {

    private OrthoCamera camera;
    private EntityManager entityManager;


    Pixmap px;
    Texture backgroundImg1;
    Texture backgroundImg2;

    public static int speed;
    private float slowSpeed;
    private float normalSpeed;
    private float fastSpeed;
    private float elapsedTime;

    private static final float NORMAL_DELAY = 12f;
    private static final float FAST_DELAY = 28f;
    private static final float TRANSITION_DURATION = 6f;
    int moveY = 0;


    @Override
    public void create() {
        // set game speed progression
        int sh = Gdx.graphics.getHeight();
        if (MainGame.debug) {
            slowSpeed = sh / 200f;
            normalSpeed = sh / 120f;
            fastSpeed = sh / 80f;
        } else {
            slowSpeed = sh / 100f;
            normalSpeed = sh / 80f;
            fastSpeed = sh / 60f;
        }

        speed = Math.max(1, Math.round(slowSpeed));
        elapsedTime = 0f;

        //set user score
        MainGame.scoreCurrent = 0;
        if (MainGame.scoreHigh == null) MainGame.scoreHigh = 0;

        camera = new OrthoCamera();
        entityManager = new EntityManager();

        //For background img
        px = new Pixmap(Gdx.files.internal("background.png"));
        backgroundImg1 = new Texture(px);
        backgroundImg1.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        backgroundImg2 = backgroundImg1;

    }

    @Override
    public void update() {
        camera.update();
        updateSpeedProgression();
        entityManager.update();

    }

    private void updateSpeedProgression() {
        elapsedTime += Gdx.graphics.getDeltaTime();

        float targetSpeed = slowSpeed;

        if (elapsedTime > FAST_DELAY) {
            float progress = Math.min((elapsedTime - FAST_DELAY) / TRANSITION_DURATION, 1f);
            targetSpeed = MathUtils.lerp(normalSpeed, fastSpeed, progress);
        } else if (elapsedTime > NORMAL_DELAY) {
            float progress = Math.min((elapsedTime - NORMAL_DELAY) / TRANSITION_DURATION, 1f);
            targetSpeed = MathUtils.lerp(slowSpeed, normalSpeed, progress);
        }

        int updatedSpeed = Math.max(1, Math.round(targetSpeed));
        if (updatedSpeed != speed) {
            speed = updatedSpeed;
            entityManager.onSpeedChanged(speed);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(camera.combined);

        moveY = moveY + speed;

        sb.begin();
        int backW = Gdx.graphics.getWidth();
        int backH = Gdx.graphics.getHeight();
        sb.draw(backgroundImg1, 0, 0 - moveY, backW, backH);
        sb.draw(backgroundImg2, 0, backH - moveY, backW, backH);
        sb.end();

        if (moveY >= backH) moveY = 0;
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
        //System.out.println("dispose");
        Gdx.app.exit();

    }

    @Override
    public void pause() {
        //System.out.println("pause");
    }

    @Override
    public void resume() {
        //System.out.println("resume");
    }
}
