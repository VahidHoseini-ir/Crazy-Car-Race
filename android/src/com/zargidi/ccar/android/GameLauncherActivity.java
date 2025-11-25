package com.zargidi.ccar.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.zargidi.ccar.MainGame;

public class GameLauncherActivity extends AndroidApplication implements MainGame.MyGameCallback {

    public boolean gameOver = false;
    public static Boolean devMode = false;
    public static int level = 1;
    private AlertDialog gameOverDialog;

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

        runOnUiThread(() -> {
            if (gameOverDialog != null && gameOverDialog.isShowing()) {
                return;
            }

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (24 * getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, padding / 2);

            TextView title = new TextView(this);
            title.setText(gameWin ? "پیروزی!" : "باختی!");
            title.setTextSize(20f);
            layout.addView(title);

            TextView currentScore = new TextView(this);
            currentScore.setText("امتیاز فعلی: " + scoreCurrent);
            currentScore.setTextSize(18f);
            layout.addView(currentScore);

            TextView bestScore = new TextView(this);
            bestScore.setText("بهترین امتیاز: " + prefs.getInt("scoreHigh", scoreHigh));
            bestScore.setTextSize(16f);
            layout.addView(bestScore);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            builder.setCancelable(false);
            builder.setPositiveButton("بازی دوباره", (dialogInterface, i) -> restartGame());
            builder.setNegativeButton("منوی اصلی", (dialogInterface, i) -> backToMenu());
            builder.setNeutralButton("خروج", (dialogInterface, i) -> finish());

            gameOverDialog = builder.create();
            gameOverDialog.show();
        });
    }

    private void restartGame() {
        Intent intent = new Intent(this, GameLauncherActivity.class);
        intent.putExtra("LEVEL", level);
        intent.putExtra("DEV_MODE", devMode);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void backToMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
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
