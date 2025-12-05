package com.zargidi.ccar.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.zargidi.ccar.MainGame;

public class MainActivity extends FragmentActivity implements NavigationHost, MainMenuFragment.MenuActions, GameOverFragment.GameOverActions, MainGame.MyGameCallback, AndroidFragmentApplication.Callbacks {

    private static final String PREF_SCORE = "userScore";
    private static final String KEY_GAME_WIN = "gameWin";
    private static final String KEY_SCORE_CURRENT = "scoreCurrent";
    private static final String KEY_SCORE_HIGH = "scoreHigh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            showMainMenu();
        }
    }

    @Override
    public void showMainMenu() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        replaceFragment(new MainMenuFragment(), false);
    }

    @Override
    public void onPlaySelected(boolean devMode, int level) {
        replaceFragment(GameFragment.newInstance(devMode, level), true);
    }

    @Override
    public void onHowToPlaySelected() {
        replaceFragment(new HowToPlayFragment(), true);
    }

    @Override
    public void onOpenUrl(Uri uri) {
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
        overridePendingTransition(R.anim.fade_in_soft, R.anim.fade_out_soft);
    }

    @Override
    public void onReplayRequested() {
        replaceFragment(GameFragment.newInstance(false, 1), true);
    }

    @Override
    public void onBackToMenuRequested() {
        showMainMenu();
    }

    @Override
    public void onOpenStore(Uri uri) {
        onOpenUrl(uri);
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public void showGameOverScreen(Integer scoreCurrent, Integer scoreHigh, Boolean gameWin) {
        runOnUiThread(() -> {
            SharedPreferences prefs = getSharedPreferences(PREF_SCORE, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean(KEY_GAME_WIN, gameWin);
            editor.putInt(KEY_SCORE_CURRENT, scoreCurrent);

            int oldHigh = prefs.getInt(KEY_SCORE_HIGH, 0);
            if (scoreCurrent > oldHigh) {
                editor.putInt(KEY_SCORE_HIGH, scoreCurrent);
            }

            editor.apply();

            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            replaceFragment(new GameOverFragment(), false);
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out_soft);
        });
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out_soft, R.anim.slide_in_up, R.anim.fade_out_soft)
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }

        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof GameOverFragment) {
            onBackToMenuRequested();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
