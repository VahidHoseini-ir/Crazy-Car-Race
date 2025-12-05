package com.zargidi.ccar.android.PNL.notif;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zargidi.ccar.android.PNL.ApiResponse;
import com.zargidi.ccar.android.R;

import java.util.List;

public class NotificationDialog {

  public interface OnCloseListener { void onClosed(); }

  private final Context context;
  private Dialog dialog;
  private LinearLayout notificationContainer;
  private TextView txtCountUnread;
  private ImageView btnClose;
  private final List<ApiResponse.Notification> notifications;

  public NotificationDialog(Context ctx, List<ApiResponse.Notification> list) {
    this.context = ctx;
    this.notifications = list;
  }

  public void show(OnCloseListener listener) {

    if (!(context instanceof Activity)) return;

    dialog = new Dialog(context);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.setContentView(R.layout.dialog_notifications);
    dialog.setCancelable(false);

    dialog.getWindow().setLayout(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.MATCH_PARENT
    );

    notificationContainer = dialog.findViewById(R.id.notificationContainer);
    txtCountUnread = dialog.findViewById(R.id.txtCountUnread);
    btnClose = dialog.findViewById(R.id.btnClose);

    updateUnreadCount();
    addNotifications();

    btnClose.setOnClickListener(v -> {
      dialog.dismiss();
      if (listener != null) listener.onClosed();
    });

    dialog.show();
  }

  private void addNotifications() {

    LayoutInflater inflater = LayoutInflater.from(context);
    notificationContainer.removeAllViews();

    for (ApiResponse.Notification n : notifications) {

      View item = inflater.inflate(R.layout.item_layout_message, notificationContainer, false);

      LinearLayout root = item.findViewById(R.id.root_element);
      LinearLayout actionsLayout = item.findViewById(R.id.actions);
      ImageView unread = item.findViewById(R.id.UnreadView);
      TextView title = item.findViewById(R.id.txttitle);
      TextView msg = item.findViewById(R.id.txtMessage);

      TextView action1 = item.findViewById(R.id.action1);
      TextView action2 = item.findViewById(R.id.action2);

      title.setText(n.title);
      msg.setText(n.message);

      boolean isRead = NotificationStorage.isRead(context, n.id);
      unread.setVisibility(isRead ? View.GONE : View.VISIBLE);

      // ---------- هندل اکشن‌ها براساس لیست ----------

      // پنهان کن ابتدا
      action1.setVisibility(View.GONE);
      action2.setVisibility(View.GONE);

      if (n.actions != null && !n.actions.isEmpty()) {

        if (n.actions.size() >= 1) {
          ApiResponse.Notification.Action a = n.actions.get(0);
          action1.setVisibility(View.VISIBLE);
          action1.setText(a.text);

          action1.setOnClickListener(v -> {
            try {
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(a.action_url));
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(intent);
            } catch (Exception ignored) {}
          });
        }

        if (n.actions.size() >= 2) {
          ApiResponse.Notification.Action a2 = n.actions.get(1);
          action2.setVisibility(View.VISIBLE);
          action2.setText(a2.text);

          action2.setOnClickListener(v -> {
            try {
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(a2.action_url));
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(intent);
            } catch (Exception ignored) {}
          });
        }
      }

      // ---------- کلیک روی Root ----------

      root.setOnClickListener(v -> {

        boolean isOpen = actionsLayout.getVisibility() == View.VISIBLE;

        if (isOpen) {
          // بسته شود
          actionsLayout.setVisibility(View.GONE);
          msg.setMaxLines(2);

        } else {
          // باز شود
          actionsLayout.setVisibility(View.VISIBLE);
          msg.setMaxLines(Integer.MAX_VALUE);

          // خوانده‌شده
          NotificationStorage.markAsRead(context, n.id);
          unread.setVisibility(View.GONE);
          updateUnreadCount();
        }
      });

      notificationContainer.addView(item);
    }
  }

  private void updateUnreadCount() {
    int unread = 0;
    for (ApiResponse.Notification n : notifications) {
      if (!NotificationStorage.isRead(context, n.id)) unread++;
    }
    txtCountUnread.setText(unread == 0 ?
      "همه پیام‌ها خوانده شده‌اند" :
      unread + " پیام خوانده‌نشده");
  }
}
