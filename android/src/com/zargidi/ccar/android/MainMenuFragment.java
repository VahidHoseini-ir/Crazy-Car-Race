package com.zargidi.ccar.android;

import androidx.annotation.Nullable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class MainMenuFragment extends Fragment {

    public interface MenuActions {
        void onPlaySelected(boolean devMode, int level);
        void onHowToPlaySelected();
        void onOpenUrl(Uri uri);
    }

    private boolean devMode = false;
    private int devModeClickCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView playButton = view.findViewById(R.id.buttonPlay);
        Animation shakeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        playButton.clearAnimation();
        playButton.setAnimation(shakeAnim);

        view.findViewById(R.id.buttonPlay).setOnClickListener(v -> launchGame());
        view.findViewById(R.id.buttonGameCenter).setOnClickListener(v -> openHowToPlay());
        view.findViewById(R.id.buttonPlayMarketStar).setOnClickListener(v -> openUrl("https://play.google.com/store/apps/developer?id=Zargidi%20Games"));
        view.findViewById(R.id.buttonPlayMarketHeart).setOnClickListener(v -> openUrl("https://play.google.com/store/apps/details?id=com.zargidi.ccar.android"));

        view.setOnLongClickListener(v -> {
            devModeClickCount++;
            if (devModeClickCount > 5) {
                devMode = true;
            }
            return false;
        });
    }

    private void launchGame() {
        MenuActions host = getMenuActionsHost();
        if (host != null) {
            host.onPlaySelected(devMode, 1);
        }
    }

    private void openHowToPlay() {
        MenuActions host = getMenuActionsHost();
        if (host != null) {
            host.onHowToPlaySelected();
        }
    }

    private void openUrl(String url) {
        MenuActions host = getMenuActionsHost();
        if (host != null) {
            host.onOpenUrl(Uri.parse(url));
        }
    }

    @Nullable
    private MenuActions getMenuActionsHost() {
        if (getActivity() instanceof MenuActions) {
            return (MenuActions) getActivity();
        }
        return null;
    }
}
