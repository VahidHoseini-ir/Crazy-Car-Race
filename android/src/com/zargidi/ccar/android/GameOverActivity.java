package com.zargidi.ccar.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class GameOverActivity extends Activity {

    TextView txtTitle;
    TextView txtScoreCurrent;
    TextView txtScoreHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        SharedPreferences userScores = getSharedPreferences("userScore", MODE_PRIVATE);
        Boolean gameWin = userScores.getBoolean("gameWin", false);
        int scoreCurrent = userScores.getInt("scoreCurrent", 0);
        int scoreHigh = userScores.getInt("scoreHigh", 0);

        if (gameWin) {
            txtTitle = findViewById(R.id.txt_game_over_title);
            txtTitle.setText("WOAH!\nYOU ARE BEST");
        }

        txtScoreCurrent = findViewById(R.id.label_score_current);
        txtScoreCurrent.setText(String.valueOf(scoreCurrent));

        txtScoreHigh = findViewById(R.id.label_score_high);
        txtScoreHigh.setText(String.valueOf(scoreHigh));

        ImageView replayButton = findViewById(R.id.buttonReplay);
        Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        replayButton.clearAnimation();
        replayButton.setAnimation(shakeAnim);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    public void menuButtonClicked(View v) {
        int id = v.getId();

        if (id == R.id.buttonReplay) {
            replayGame();
        }
        else if (id == R.id.buttonBackToLevel) {
            backToLevel();
        }
        else if (id == R.id.buttonPlayMarketStar) {
            playMarketStar();
        }
        else if (id == R.id.buttonPlayMarketHeart) {
            playMarketHeart();
        }
    }

    public void backToLevel() {
        Intent intent = new Intent(getApplicationContext(), SelectLevelActivity.class);
        startActivity(intent);
    }

    public void replayGame() {
        Intent intent = new Intent(getApplicationContext(), GameLauncherActivity.class);
        startActivity(intent);
    }

    public void playMarketStar() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Zargidi%20Games"));
        startActivity(intent);
    }

    public void playMarketHeart() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.zargidi.ccar.android"));
        startActivity(intent);
    }

}
