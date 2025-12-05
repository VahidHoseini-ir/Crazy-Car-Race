package com.zargidi.ccar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Preferences;
import com.zargidi.screen.GameScreen;
import com.zargidi.screen.ScreenManager;

public class MainGame extends ApplicationAdapter {

    private MyGameCallback myGameCallback;

    public static Boolean gameOver = false;
    public static Boolean gameWin  = false;

    private boolean gameOverHandled = false;

    public static Integer scoreCurrent = 0;
    public static Integer scoreHigh    = 0;

    SpriteBatch batch;

    public static boolean debug = false;
    public static int level = 1;

    private boolean disposed = false;

    // اسم و کلید برای Preferences (همین‌ها را در Activity اندروید هم استفاده کن)
    private static final String PREF_NAME   = "userScore";
    private static final String KEY_BEST    = "scoreHigh";

    public interface MyGameCallback {
        void showGameOverScreen(Integer scoreCurrent, Integer scoreHigh, Boolean gameWin);
    }

    public MainGame(Boolean devMode, int selectedLevel){
        debug = devMode;
        level = selectedLevel;
    }

    @Override
    public void create () {
        batch = new SpriteBatch();

        // لود کردن Best Score از Preferences
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        scoreHigh = prefs.getInteger(KEY_BEST, 0);

        ScreenManager.setCurrentScreen(new GameScreen());
    }

    @Override
    public void render () {

        if (ScreenManager.getCurrentScreen() != null) {
            ScreenManager.getCurrentScreen().update();
            ScreenManager.getCurrentScreen().render(batch);
        }

        if (gameOver && !gameOverHandled) {

            // اگر رکورد جدید زدیم، ذخیره کن
            if (scoreCurrent > scoreHigh) {
                scoreHigh = scoreCurrent;

                Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
                prefs.putInteger(KEY_BEST, scoreHigh);
                prefs.flush();
            }

            // اول GameOverScreen اندروید، بعد بستن LibGDX
            if (myGameCallback != null) {
                myGameCallback.showGameOverScreen(scoreCurrent, scoreHigh, gameWin);
            }

            ScreenManager.clearScreen();
            dispose();

            gameOverHandled = true;
            gameOver = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (ScreenManager.getCurrentScreen() != null) {
            ScreenManager.getCurrentScreen().resize(width, height);
        }
    }

    @Override
    public void pause() {
        if (ScreenManager.getCurrentScreen() != null) {
            ScreenManager.getCurrentScreen().pause();
        }
    }

    @Override
    public void resume() {
        if (ScreenManager.getCurrentScreen() != null) {
            ScreenManager.getCurrentScreen().resume();
        }
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;

        if (ScreenManager.getCurrentScreen() != null) {
            ScreenManager.getCurrentScreen().dispose();
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }

    public void setMyGameCallback(MyGameCallback callback) {
        myGameCallback = callback;
    }
}
