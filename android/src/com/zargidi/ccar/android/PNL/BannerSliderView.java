package com.zargidi.ccar.android.PNL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.zargidi.ccar.android.R;

import java.util.List;



public class BannerSliderView extends RelativeLayout {

  private ImageView imgBanner;
  private LinearLayout btnClose;
  private ProgressBar progressBar;
  private LinearLayout indicatorContainer;

  private List<ApiResponse.Banner> banners;
  private int index = 0;

  private final int DURATION = 3500; // مدت زمان هر اسلاید
  private Handler handler = new Handler();
  private Runnable timer;

  private boolean isPaused = false;
  private int savedProgress = 0;

  public BannerSliderView(Context ctx) {
    super(ctx);
    init(ctx);
  }

  public BannerSliderView(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
    init(ctx);
  }

  private void init(Context context) {
    LayoutInflater.from(context).inflate(R.layout.banner_slider, this, true);

    imgBanner = findViewById(R.id.imgBanner);
    progressBar = findViewById(R.id.bannerProgress);
    indicatorContainer = findViewById(R.id.indicatorContainer);
    btnClose = findViewById(R.id.btnClose);

    btnClose.setOnClickListener(v -> {
      if (getParent() != null)
        ((ViewGroup) getParent()).removeView(this);
    });
  }

  public void setBanners(List<ApiResponse.Banner> list) {
    this.banners = list;
    buildIndicators();
    showBanner(0);
  }

  // ساخت دایره‌های پایین
  private void buildIndicators() {
    indicatorContainer.removeAllViews();
    float density = getResources().getDisplayMetrics().density;

    int size = (int) (16 * density);
    int margin = (int) (6 * density);

    for (int i = 0; i < banners.size(); i++) {
      View dot = new View(getContext());
      LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(size, size);
      lp.setMargins(margin, 0, margin, 0);
      dot.setLayoutParams(lp);
      dot.setBackgroundResource(R.drawable.indicator_inactive);

      final int pos = i;
      dot.setOnClickListener(v -> showBanner(pos));

      indicatorContainer.addView(dot);
    }
  }

  // آپدیت دایره‌ها
  private void updateIndicators(int pos) {
    for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
      View d = indicatorContainer.getChildAt(i);
      d.setBackgroundResource(i == pos ?
        R.drawable.indicator_active :
        R.drawable.indicator_inactive);
    }
  }

  // نمایش یک اسلاید
  private void showBanner(int pos) {
    if (timer != null) handler.removeCallbacks(timer);

    if (pos < 0 || pos >= banners.size()) return;

    index = pos;
    ApiResponse.Banner b = banners.get(pos);

    Glide.with(getContext()).load(b.image_url).into(imgBanner);
    updateIndicators(pos);

    imgBanner.setOnClickListener(v -> {
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(b.click_url));
      getContext().startActivity(i);
    });

    imgBanner.setOnTouchListener((v, event) -> {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          pauseTimer();
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
          resumeTimer();
          break;
      }
      return false;
    });

    // اگر آخرین بنر بود → ریست به اول
    if (pos == banners.size() - 1) {
      startLoopTimer();   // حلقه‌ای
    } else {
      startTimer();       // حرکت معمولی
    }
  }

  // توقف هنگام لمس
  private void pauseTimer() {
    if (timer != null) handler.removeCallbacks(timer);
    isPaused = true;
    savedProgress = progressBar.getProgress();
  }

  // ادامه بعد از لمس
  private void resumeTimer() {
    if (!isPaused) return;
    isPaused = false;

    int remaining = DURATION - savedProgress;

    timer = new Runnable() {
      long start = System.currentTimeMillis();

      @Override
      public void run() {
        long elapsed = System.currentTimeMillis() - start;
        int progress = savedProgress + (int) elapsed;

        progressBar.setProgress(progress);

        if (progress < DURATION) {
          handler.postDelayed(this, 20);
        } else {
          showBanner(index < banners.size() - 1 ? index + 1 : 0);
        }
      }
    };
    handler.post(timer);
  }

  // تایمر اسلاید معمولی (به اسلاید بعدی)
  private void startTimer() {
    progressBar.setProgress(0);
    progressBar.setMax(DURATION);

    timer = new Runnable() {
      long start = System.currentTimeMillis();

      @Override
      public void run() {
        long elapsed = System.currentTimeMillis() - start;

        progressBar.setProgress((int) elapsed);

        if (elapsed < DURATION) {
          handler.postDelayed(this, 20);
        } else {
          showBanner(index + 1);
        }
      }
    };
    handler.post(timer);
  }

  // تایمر حلقه‌ای (از آخر → اول)
  private void startLoopTimer() {

    progressBar.setProgress(0);
    progressBar.setMax(DURATION);

    timer = new Runnable() {
      long start = System.currentTimeMillis();

      @Override
      public void run() {
        long elapsed = System.currentTimeMillis() - start;

        progressBar.setProgress((int) elapsed);

        if (elapsed < DURATION) {
          handler.postDelayed(this, 20);
        } else {
          showBanner(0); // برگشت به بنر اول
        }
      }
    };
    handler.post(timer);
  }
}
