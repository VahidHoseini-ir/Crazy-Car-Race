package com.zargidi.ccar.android;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GameOverFragment extends Fragment {

    public interface GameOverActions {
        void onReplayRequested();
        void onBackToMenuRequested();
        void onOpenStore(Uri uri);
    }

    private static final String PREF_SCORE = "userScore";
    private static final String KEY_GAME_WIN = "gameWin";
    private static final String KEY_SCORE_CURRENT = "scoreCurrent";
    private static final String KEY_SCORE_HIGH = "scoreHigh";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_game_over, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() == null) {
            return;
        }

        SharedPreferences userScores = getActivity().getSharedPreferences(PREF_SCORE, 0);
        boolean gameWin = userScores.getBoolean(KEY_GAME_WIN, false);
        int scoreCurrent = userScores.getInt(KEY_SCORE_CURRENT, 0);
        int scoreHigh = userScores.getInt(KEY_SCORE_HIGH, 0);

        if (gameWin) {
            TextView txtTitle = view.findViewById(R.id.txt_game_over_title);
            txtTitle.setText("WOAH!\nYOU ARE BEST");
        }

        TextView txtScoreCurrent = view.findViewById(R.id.label_score_current);
        txtScoreCurrent.setText(String.valueOf(scoreCurrent));

        TextView txtScoreHigh = view.findViewById(R.id.label_score_high);
        txtScoreHigh.setText(String.valueOf(scoreHigh));

        ImageView replayButton = view.findViewById(R.id.buttonReplay);
        Animation shakeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        replayButton.clearAnimation();
        replayButton.setAnimation(shakeAnim);

        view.findViewById(R.id.buttonReplay).setOnClickListener(v -> replayGame());
        view.findViewById(R.id.buttonBackToLevel).setOnClickListener(v -> backToLevel());
        view.findViewById(R.id.buttonPlayMarketStar).setOnClickListener(v -> openStore("https://play.google.com/store/apps/developer?id=Zargidi%20Games"));
        view.findViewById(R.id.buttonPlayMarketHeart).setOnClickListener(v -> openStore("https://play.google.com/store/apps/details?id=com.zargidi.ccar.android"));
    }

    private void backToLevel() {
        GameOverActions host = getGameOverActionsHost();
        if (host != null) {
            host.onBackToMenuRequested();
        }
    }

    private void replayGame() {
        GameOverActions host = getGameOverActionsHost();
        if (host != null) {
            host.onReplayRequested();
        }
    }

    private void openStore(String url) {
        GameOverActions host = getGameOverActionsHost();
        if (host != null) {
            host.onOpenStore(Uri.parse(url));
        }
    }

    @Nullable
    private GameOverActions getGameOverActionsHost() {
        if (getActivity() instanceof GameOverActions) {
            return (GameOverActions) getActivity();
        }
        return null;
    }
}
