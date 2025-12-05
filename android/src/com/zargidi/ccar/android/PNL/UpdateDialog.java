package com.zargidi.ccar.android.PNL;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.encoders.json.BuildConfig;
import com.zargidi.ccar.android.R;


public class UpdateDialog {

  private final Context context;
  private Dialog dialog;
  private ApiResponse.UpdateNotification data;

  public interface OnCloseListener {
    void onClosed();
  }

  public UpdateDialog(Context ctx, ApiResponse.UpdateNotification update) {
    this.context = ctx;
    this.data = update;
  }

  public void show(OnCloseListener listener) {

    int appVersionCode = BuildConfig.VERSION_CODE; // ورژن‌کد فعلی اپ
    int serverVersionCode = Integer.parseInt(data.version); // تبدیل String به int


    if (appVersionCode < serverVersionCode) {

    dialog = new Dialog(context);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    dialog.setContentView(R.layout.fragment_update);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.setCancelable(false);

    TextView txtTitle = dialog.findViewById(R.id.txtTitle);
    TextView txtDesc = dialog.findViewById(R.id.txtDesc);
    Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
    Button btnRefuse = dialog.findViewById(R.id.btnRefuse);

    txtTitle.setText(data.title);
    txtDesc.setText(data.message);

    StoreChoice choice = selectBestMarket();
    btnConfirm.setText(choice.label);
    btnConfirm.setOnClickListener(v -> open(choice.url));

    btnRefuse.setOnClickListener(v -> {
      dialog.dismiss();
      if (listener != null) listener.onClosed();
    });

    dialog.show();
  }

  }
  // انتخاب بهترین مارکت
  private StoreChoice selectBestMarket() {

    if (!data.google_play.action_url.isEmpty() &&
      exists("com.android.vending"))
      return new StoreChoice(data.google_play.action_url, data.google_play.text);

    if (!data.bazaar.action_url.isEmpty() &&
      exists("com.farsitel.bazaar"))
      return new StoreChoice(data.bazaar.action_url, data.bazaar.text);

    if (!data.myket.action_url.isEmpty() &&
      exists("ir.mservices.market"))
      return new StoreChoice(data.myket.action_url, data.myket.text);

    return new StoreChoice(data.direct_download.action_url, data.direct_download.text);
  }

  private boolean exists(String pkg) {
    try {
      context.getPackageManager().getPackageInfo(pkg, 0);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void open(String url) {
    try {
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      context.startActivity(i);
    } catch (Exception ignored) {
    }
  }

  private static class StoreChoice {
    public String url;
    public String label;

    public StoreChoice(String u, String l) {
      url = u;
      label = l;
    }
  }
}
