package com.zargidi.ccar.android.PNL.notif;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zargidi.ccar.android.PNL.ApiResponse;
import com.zargidi.ccar.android.R;

import java.util.ArrayList;
import java.util.List;


public class SmartNotificationManager {

  private final Context context;
  private List<ApiResponse.Notification> notifications;

  public SmartNotificationManager(Context ctx) {
    this.context = ctx;
  }

  public void handleNotifications(List<ApiResponse.Notification> list) {

    if (!(context instanceof Activity)) return;

    this.notifications = list;

    List<String> newIds = getNewIds(list);

    if (!newIds.isEmpty()) {
      showDialog(list);
    } else {
      showBadge();
    }

    // همیشه seen را sync کن
    saveSeen(list);
  }

  // -------------------------------------------
  // پیام جدید = پیامی که ID آن قبلاً در Seen نبود
  private List<String> getNewIds(List<ApiResponse.Notification> list) {

    List<String> seen = SeenStorage.loadSeen(context);
    List<String> newOnes = new ArrayList<>();

    for (ApiResponse.Notification n : list) {
      if (!seen.contains(n.id)) {
        newOnes.add(n.id);
      }
    }

    return newOnes;
  }

  // ذخیرهٔ Seen
  private void saveSeen(List<ApiResponse.Notification> list) {
    List<String> ids = new ArrayList<>();
    for (ApiResponse.Notification n : list)
      ids.add(n.id);
    SeenStorage.saveSeen(context, ids);
  }

  // -------------------------------------------
  // نمایش دیالوگ
  private void showDialog(List<ApiResponse.Notification> list) {
    NotificationDialog dialog = new NotificationDialog(context, list);
    dialog.show(() -> {
      // وقتی دیالوگ بسته شد → Badge نشان بده
      showBadge();
    });
  }

  // -------------------------------------------
  // نمایش Badge داخل Activity
  private void showBadge() {

    if (!(context instanceof Activity)) return;

    Activity act = (Activity) context;
    ViewGroup root = act.findViewById(android.R.id.content);

    // 1) اگر هیچ پیامی نداریم → هر بجی هست حذف کن و برگرد
    int total = (notifications == null) ? 0 : notifications.size();
    if (total == 0) {
      for (int i = root.getChildCount() - 1; i >= 0; i--) {
        View v = root.getChildAt(i);
        if ("notif_badge".equals(v.getTag())) {
          root.removeView(v);
        }
      }
      return;
    }

    // 2) هر بج قدیمی را پاک کن (فقط با tag خودمان)
    for (int i = root.getChildCount() - 1; i >= 0; i--) {
      View v = root.getChildAt(i);
      if ("notif_badge".equals(v.getTag())) {
        root.removeView(v);
      }
    }

    // 3) ساخت Badge جدید
    View badge = LayoutInflater.from(context)
      .inflate(R.layout.layout_notif_badge, root, false);

    // حتماً tag بگذار تا بعداً بشود همین را پیدا و حذف کرد
    badge.setTag("notif_badge");

    TextView txt = badge.findViewById(R.id.notif_count);

    // 4) فقط رفتار عدد به unread ربط دارد، نه خود بج
    int unread = NotificationStorage.countUnread(context, notifications);
    if (unread > 0) {
      txt.setVisibility(View.VISIBLE);
      txt.setText(String.valueOf(unread));
    } else {
      // پیام هست ولی نخونده نداریم → خود بج باشه، عدد مخفی
      txt.setVisibility(View.GONE);
    }

    // قابل کلیک
    badge.setClickable(true);
    badge.setOnClickListener(v -> {
      NotificationDialog dialog = new NotificationDialog(context, notifications);
      dialog.show(() -> {
        // بعد از بستن دیالوگ، وضعیت را دوباره چک کن
        showBadge();
      });
    });

    // موقعیت
    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    );
    lp.gravity = Gravity.TOP | Gravity.LEFT;
    lp.leftMargin = 200;
    lp.rightMargin = 200;
    lp.topMargin = 200;
    badge.setLayoutParams(lp);

    root.addView(badge);

    // انیمیشن ظاهر شدن
    badge.setAlpha(0f);
    badge.animate().alpha(1f).setDuration(300).start();

    // درگ و حذف
    badge.setOnTouchListener(new View.OnTouchListener() {

      float dX, dY;
      boolean isDragging = false;
      float startX, startY;

      @Override
      public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {

          case MotionEvent.ACTION_DOWN:
            dX = view.getX() - event.getRawX();
            dY = view.getY() - event.getRawY();
            startX = event.getRawX();
            startY = event.getRawY();
            isDragging = false;
            return true;

          case MotionEvent.ACTION_MOVE:
            float diffX = Math.abs(event.getRawX() - startX);
            float diffY = Math.abs(event.getRawY() - startY);

            if (diffX > 10 || diffY > 10) {
              isDragging = true;

              view.animate()
                .x(event.getRawX() + dX)
                .y(event.getRawY() + dY)
                .setDuration(0)
                .start();
            }

            return true;

          case MotionEvent.ACTION_UP:

            // اگر درگ نبود → کلیک
            if (!isDragging) {
              view.performClick();
              return true;
            }

            // اگر درگ بود → حذف یا برگشت
            float distance = Math.abs(view.getX() - 200);

            if (distance > 200) {
              view.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> root.removeView(view))
                .start();
            } else {
              view.animate()
                .x(200)
                .y(200)
                .setDuration(200)
                .start();
            }

            return true;
        }

        return false;
      }
    });

    // 5) حذف خودکار بعد از 20 ثانیه (همیشه برای این بج)
    new Handler().postDelayed(() -> {
      if (badge.getParent() != null) {
        badge.animate().alpha(0f).setDuration(300)
          .withEndAction(() -> root.removeView(badge));
      }
    }, 20000);
  }

}
