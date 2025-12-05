package com.zargidi.ccar.android.PNL.notif;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class SeenStorage {

  private static final String KEY = "seen_ids";

  public static void saveSeen(Context ctx, List<String> ids) {
    String joined = String.join(",", ids);
    ctx.getSharedPreferences("notif", Context.MODE_PRIVATE)
      .edit().putString(KEY, joined).apply();
  }

  public static List<String> loadSeen(Context ctx) {
    String raw = ctx.getSharedPreferences("notif", Context.MODE_PRIVATE)
      .getString(KEY, "");
    List<String> list = new ArrayList<>();
    if (!raw.isEmpty()) {
      for (String s : raw.split(",")) {
        if (!s.trim().isEmpty())
          list.add(s.trim());
      }
    }
    return list;
  }
}

