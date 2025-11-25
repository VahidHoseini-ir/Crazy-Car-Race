package com.zargidi.ccar.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.zargidi.ccar.MainGame;

public class GameLauncherActivity extends AndroidApplication implements MainGame.MyGameCallback {

    public boolean gameOver = false;
    public static Boolean devMode = false;
    public static int level = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        MainGame game = new MainGame(devMode, level);
        game.setMyGameCallback(this);
        game.gameOver = gameOver;

        initialize(game, config);
    }

    @Override
    public void showGameOverScreen(Integer scoreCurrent, Integer scoreHigh, Boolean gameWin) {

        SharedPreferences prefs = getSharedPreferences("userScore", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("gameWin", gameWin);
        editor.putInt("scoreCurrent", scoreCurrent);

        int oldHigh = prefs.getInt("scoreHigh", 0);
        if (scoreCurrent > oldHigh) {
            editor.putInt("scoreHigh", scoreCurrent);
        }

        editor.apply();

        Intent intent = new Intent(this, GameOverActivity.class);
        startActivity(intent);
    }

    @Override
    public void exit() {
        gameOver = false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }
}
