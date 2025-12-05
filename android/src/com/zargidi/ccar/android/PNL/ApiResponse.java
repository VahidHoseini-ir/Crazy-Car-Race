package com.zargidi.ccar.android.PNL;

import java.util.List;

public class ApiResponse {
    public String min_app_version;
    public List<Banner> banner;
    public UpdateNotification update_notification;
    public List<Notification> notifications;
    public List<DynamicPayload> dynamic_payloads;
    public class UpdateNotification {
        public String title;
        public String message;
        public String version;
        public StoreInfo bazaar;
        public StoreInfo direct_download;
        public StoreInfo google_play;
        public StoreInfo myket;
    }

    public class Banner {
        public String id;
        public String image_url;
        public String click_url;
        public boolean visibility;
    }
    public class StoreInfo {
        public String text;
        public String action_url;
        public String appstorename;
    }

    public class Notification {
        public String id;
        public String title;
        public String message;
        public String type;
        public List<Action> actions;

        public class Action {
            public String text;
            public String action_url;
        }
    }
    public class DynamicPayload {
        public String anct;
        public String dn;
        public String urA;
    }

}
