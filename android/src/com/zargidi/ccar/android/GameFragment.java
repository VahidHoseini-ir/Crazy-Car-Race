package com.zargidi.ccar.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.zargidi.ccar.MainGame;

public class GameFragment extends AndroidFragmentApplication {

    private static final String ARG_DEV_MODE = "devMode";
    private static final String ARG_LEVEL = "level";

    public static GameFragment newInstance(boolean devMode, int level) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DEV_MODE, devMode);
        args.putInt(ARG_LEVEL, level);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() == null) {
            return null;
        }

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        boolean devMode = getArguments() != null && getArguments().getBoolean(ARG_DEV_MODE, false);
        int level = getArguments() != null ? getArguments().getInt(ARG_LEVEL, 1) : 1;

        MainGame game = new MainGame(devMode, level);
        if (getActivity() instanceof MainGame.MyGameCallback) {
            game.setMyGameCallback((MainGame.MyGameCallback) getActivity());
        }

        return initializeForView(game, config);
    }
}
