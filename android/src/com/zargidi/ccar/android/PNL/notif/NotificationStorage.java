package com.zargidi.ccar.android.PNL.notif;

import android.content.Context;

import com.zargidi.ccar.android.PNL.ApiResponse;

import java.util.List;



public class NotificationStorage {

  private static final String PREF_NAME = "notif_read";

  public static boolean isRead(Context ctx, String id) {
    return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
      .getBoolean("read_" + id, false);
  }

  public static void markAsRead(Context ctx, String id) {
    ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
      .edit().putBoolean("read_" + id, true).apply();
  }

  public static int countUnread(Context ctx, List<ApiResponse.Notification> list) {
    int count = 0;
    for (ApiResponse.Notification n : list) {
      if (!isRead(ctx, n.id)) count++;
    }
    return count;
  }
}