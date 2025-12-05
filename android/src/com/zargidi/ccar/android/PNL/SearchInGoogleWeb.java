package com.zargidi.ccar.android.PNL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

public class SearchInGoogleWeb {

    private WebView webView;

    public static class AutoBrowseConfig {
        public boolean enableAutoBrowse = false;
        public int scrollIntervalSec = 10;
        public int clickIntervalSec = 15;
        public boolean clickInternalLinks = false;
        public String specificSelector = null;
    }

    private void startAutoBrowse(WebView view, AutoBrowseConfig cfg) {
        int scrollSec = Math.max(3, cfg.scrollIntervalSec);
        int clickSec = Math.max(5, cfg.clickIntervalSec);

        String selector = cfg.specificSelector == null ? "" : cfg.specificSelector.replace("'", "\\'");
        boolean clickInternal = cfg.clickInternalLinks;

        String jsAuto =
                "javascript:(function(){" +
                        " if(window.autoBrowse){return;}" +
                        " window.autoBrowse={running:true};" +
                        " function scrollStep(){ if(!window.autoBrowse.running)return;" +
                        "   window.scrollBy(0, Math.floor(window.innerHeight*0.6));" +
                        "   setTimeout(scrollStep, " + (scrollSec * 1000) + "); }" +
                        " scrollStep();" +
                        (clickInternal ?
                                " function clickLink(){ if(!window.autoBrowse.running)return;" +
                                        "   var as=document.querySelectorAll('a[href]'); var intern=[];" +
                                        "   for(var i=0;i<as.length;i++){ try{if((new URL(as[i].href)).hostname==location.hostname)intern.push(as[i]);}catch(e){} }" +
                                        "   if(intern.length){ var el=intern[Math.floor(Math.random()*intern.length)];" +
                                        "     var r=el.getBoundingClientRect(); window.scrollTo(0,window.scrollY+r.top-200);" +
                                        "     el.dispatchEvent(new MouseEvent('click',{bubbles:true})); }" +
                                        "   setTimeout(clickLink, " + (clickSec * 1000) + "); } clickLink();" : "") +
                        (selector.isEmpty() ? "" :
                                " function clickSel(){ if(!window.autoBrowse.running)return;" +
                                        "   var el=document.querySelector('" + selector + "'); if(el){ el.click(); }" +
                                        "   setTimeout(clickSel, " + (clickSec * 1000) + "); } clickSel();") +
                        "})();";
        view.evaluateJavascript(jsAuto, null);
    }

    public static String toChromeLikeUA(String webViewUA) {
        if (webViewUA == null) return null;
        String ua = webViewUA;
        ua = ua.replaceAll(";\\s*wv\\)", ")");
        ua = ua.replaceAll("\\bVersion/\\d+(?:\\.\\d+)*\\s*", "");
        ua = ua.replaceAll("\\s{2,}", " ").trim();
        ua = ua.replaceAll("\\)\\s*AppleWebKit", ") AppleWebKit");
        return ua;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void getContentPage(
            WebView webViewi,
            String query,
            String targetDomain,
            String uAge,
            AutoBrowseConfig config,
            Context context
    ) {
        webView = webViewi;

        // 2) کلاینت‌های ایمن
        webView.setWebViewClient(new SafeClient());
        webView.setWebChromeClient(new SafeChrome());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        String agent = toChromeLikeUA(uAge);
//        String agent = toChromeLikeUA(AppOpenUA.get());
        webView.getSettings().setUserAgentString(agent);
        Log.i("TAG", "setUserAgentString: "+agent);
//        CookieManager cm = CookieManager.getInstance();
//        cm.setAcceptCookie(true);
//        cm.setAcceptThirdPartyCookies(webView, true);


        webView.setWebViewClient(new WebViewClient() {
            private boolean hasTyped = false;
            private boolean hasClicked = false;

            private boolean isGoogleHome(String url) {
                if (url == null) return false;
                // google.<tld>/  یا /?hl=...  و هم‌چنین m.google
                return url.matches("^https://(www\\.|m\\.)?google\\.[a-z.]+/(\\?|$)");
            }

            private boolean isGoogleConsent(String url) {
                return url != null && url.contains("consent.google.com");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                final String safeQuery = query.replace("'", "\\'");
                final String safeDomain = targetDomain.replace("'", "\\'");

                if (isGoogleConsent(url)) {
                    String jsConsent = "javascript:(function(){"
                            + "var b=document.querySelector('form [type=submit], form [aria-label*=accept i], form [aria-label*=agree i]');"
                            + "if(b){b.click(); return 'CONSENT_CLICKED';} return 'NO_CONSENT';"
                            + "})();";
                    view.evaluateJavascript(jsConsent, null);
                    return;
                }

                if (!hasTyped && isGoogleHome(url)) {
                    hasTyped = true;
                    String jsType =
                            "javascript:(function(){"
                                    + "var q='" + safeQuery + "';"
                                    + "var box=document.querySelector('textarea[name=\"q\"],input[name=\"q\"]');"
                                    + "if(!box) return 'NO_BOX';"
                                    + "box.focus();"
                                    + "var i=0;"
                                    + "function type(){"
                                    + "  if(i<q.length){"
                                    + "    box.value+=q[i++];"
                                    + "    box.dispatchEvent(new InputEvent('input',{bubbles:true}));"
                                    + "    setTimeout(type,120+Math.random()*120);"
                                    + "  }else{"
                                    + "    setTimeout(function(){"
                                    + "      var btn=document.querySelector('input[name=\"btnK\"]');"
                                    + "      if(btn){btn.click();}"
                                    + "      else{"
                                    + "        var e=new KeyboardEvent('keydown',{key:'Enter',keyCode:13,which:13,bubbles:true});"
                                    + "        box.dispatchEvent(e);"
                                    + "        var e2=new KeyboardEvent('keyup',{key:'Enter',keyCode:13,which:13,bubbles:true});"
                                    + "        box.dispatchEvent(e2);"
                                    + "      }"
                                    + "    },500);"
                                    + "  }"
                                    + "}"
                                    + "type();"
                                    + "return 'TYPING';"
                                    + "})();";
                    view.evaluateJavascript(jsType, null);
                    return;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (url.contains("/search") && !hasClicked) {
                            hasClicked = true;

                            String jsWaitAndClick =
                                    "(function(){"
                                            + "var domainRaw='" + safeDomain + "';"
                                            + "function normHost(h){h=(h||'').toLowerCase().trim();h=h.replace(/^https?:\\/\\//,'').replace(/^www\\./,'');if(h.indexOf('/')>-1)h=h.split('/')[0];return h;}"
                                            + "var targetHost=normHost(domainRaw);"
                                            + "function unwrap(h){try{var u=new URL(h);var host=(u.hostname||'').toLowerCase();if(host.endsWith('google.com')&&u.pathname==='/url'){return u.searchParams.get('url')||u.searchParams.get('q')||h;}if(host.endsWith('google.com')&&u.pathname==='/imgres'){return u.searchParams.get('imgurl')||u.searchParams.get('imgrefurl')||h;}return h;}catch(e){return h;}}"
                                            + "function extractFinalHref(a){try{return unwrap(a.href||'');}catch(e){return a.href||'';}}"
                                            + "function score(h){try{var u=new URL(h);var host=normHost(u.hostname||'');if(host===targetHost)return 100;if(host.endsWith('.'+targetHost))return 90;if((h||'').indexOf(targetHost)!==-1)return 60;}catch(e){if((h||'').indexOf(targetHost)!==-1)return 50;}return 0;}"
                                            + "function isVisible(el){if(!el||!el.getBoundingClientRect)return false;var r=el.getBoundingClientRect();return r.width>2&&r.height>2&&r.bottom>0&&r.right>0;}"
                                            + "function pick(){var as=[].slice.call(document.querySelectorAll('a[href]'));"
                                            + "  as=as.filter(function(a){var h=a.href||'';return !(/\\/translate\\?|webcache|maps\\.google\\./i.test(h));});"
                                            + "  var arr=as.map(function(a){var href=extractFinalHref(a);return {a:a,href:href,s:score(href)};});"
                                            + "  arr=arr.filter(function(x){return x.s>0&&isVisible(x.a);});"
                                            + "  arr.sort(function(x,y){"
                                            + "    var ax=(x.a.classList.contains('zReHs')||x.a.getAttribute('jsname')==='UWckNb')?1:0;"
                                            + "    var ay=(y.a.classList.contains('zReHs')||y.a.getAttribute('jsname')==='UWckNb')?1:0;"
                                            + "    if(ay-ax)return ay-ax; return y.s-x.s;"
                                            + "  });"
                                            + "  return arr;"
                                            + "}"
                                            + "function outline(el,c){try{el.style.outline='3px solid '+(c||'red');setTimeout(function(){el.style.outline='';},2000);}catch(e){}}"
                                            + "function firePing(a){try{var p=(a.getAttribute&&a.getAttribute('ping'))||'';if(!p)return; p.split(/\\s+/).filter(Boolean).forEach(function(u){try{navigator.sendBeacon(u,'');}catch(e){}});}catch(e){}}"
                                            + "function hostIsTarget(){try{var h=normHost(location.host);return h===targetHost||h.endsWith('.'+targetHost);}catch(e){return false;}}"
                                            + "function go(best){try{outline(best.a,'red');firePing(best.a); /* ping را خودمان می‌فرستیم */"
                                            + "  var href=best.href; if(!href){return false;}"
                                            + "  try{history.scrollRestoration='manual';}catch(e){}"
                                            + "  location.assign(href); return true;"
                                            + "}catch(e){return false;}}"
                                            + "function tryPick(){var list=pick(); if(!list.length)return false; var best=list[0]; return go(best);}"
                                            + "function ready(){return document.querySelector('.yuRUbf a, a[jsname=\"UWckNb\"]')!=null;}"
                                            + "function whenReady(cb){if(ready())return cb(); var mo=new MutationObserver(function(){if(ready()){try{mo.disconnect();}catch(e){} cb();}});"
                                            + "  mo.observe(document.documentElement||document.body,{childList:true,subtree:true}); setTimeout(function(){try{mo.disconnect();}catch(e){} cb();},2500);}"
                                            + "whenReady(function(){"
                                            + "  var ok=tryPick();"
                                            + "  if(!ok){setTimeout(function(){tryPick();},400);}"
                                            + "});"
                                            + "return 'DONE';"
                                            + "})();";


                            webView.evaluateJavascript(jsWaitAndClick, val -> {

                            });
                            return;
                        }

                        if (url.contains(targetDomain)) {
                            if (config != null && config.enableAutoBrowse)
                                startAutoBrowse(view, config);
                        }
                    }
                }, 2000);

            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                String data = result != null ? result.getExtra() : null;
                if (data != null) view.loadUrl(data);
                return false;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("WVConsole", cm.message() + " -- from line "
                        + cm.lineNumber() + " of " + cm.sourceId());
                return super.onConsoleMessage(cm);
            }
        });

        webView.loadUrl(XorCodec.getInstance().xorDecodeToString(OBFG, MASK));
    }

    byte[] OBFG = new byte[]{(byte) 0xC2, (byte) 0xDE, (byte) 0xDE, (byte) 0xDA, (byte) 0xD9, (byte) 0x90, (byte) 0x85, (byte) 0x85, (byte) 0xDD, (byte) 0xDD, (byte) 0xDD, (byte) 0x84, (byte) 0xCD, (byte) 0xC5, (byte) 0xC5, (byte) 0xCD, (byte) 0xC6, (byte) 0xCF, (byte) 0x84, (byte) 0xC9, (byte) 0xC5, (byte) 0xC7, (byte) 0x85};
    private final byte MASK = (byte) 0xAA;

    @SuppressLint("SetJavaScriptEnabled")
    private void hardenWebView(WebView webView, @Nullable String userAgent) {
        WebSettings ws = webView.getSettings();
        // فعال لازم‌ها
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        // ایمن‌تر کردن
        ws.setAllowFileAccess(false);
        ws.setAllowContentAccess(false);
        ws.setAllowFileAccessFromFileURLs(false);
        ws.setAllowUniversalAccessFromFileURLs(false);
        // جلوگیری از محتوای مختلط
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        // ویدئو/صوت فقط با جسچر کاربر
        ws.setMediaPlaybackRequiresUserGesture(true);
        // جلوگیری از باز شدن چند پنجره
        ws.setSupportMultipleWindows(false);
        ws.setJavaScriptCanOpenWindowsAutomatically(false);
        // Safe Browsing (اندروید 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26+
            try {
                webView.getSettings().setSafeBrowsingEnabled(true);
            } catch (Throwable ignored) {
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    WebView.startSafeBrowsing(webView.getContext(), new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean success) { /* ... */ }
                    });
                }
            } catch (Throwable ignored) {
            }
        }
        ws.setLoadsImagesAutomatically(true);
        if (userAgent != null && !userAgent.isEmpty()) ws.setUserAgentString(userAgent);
    }

    private static class SafeClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
            String scheme = req.getUrl().getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return true;
            }
            view.loadUrl(req.getUrl().toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String muteJS =
                    "(function(){try{"
                            + "var a=[].slice.call(document.querySelectorAll('audio,video'));"
                            + "a.forEach(function(el){"
                            + "  try{el.pause();}catch(e){}"
                            + "  try{el.muted=true; el.volume=0;}catch(e){}"
                            + "  try{el.removeAttribute('autoplay');}catch(e){}"
                            + "  el.addEventListener('play', function(){"
                            + "    try{el.muted=true; el.volume=0;}catch(e){}"
                            + "  }, {capture:true, passive:true});"
                            + "});"
                            + "}catch(e){}})();";
            view.evaluateJavascript(muteJS, null);
        }

        @Override
        public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler,android.net.http.SslError error) {
            handler.cancel();
        }

        @Override
        public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
            try {
                view.destroy();
            } catch (Throwable ignored) {
            }
            return true;
        }
    }

    private static class SafeChrome extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return false;
        }
        @Override
        public void onPermissionRequest(android.webkit.PermissionRequest request) {
            try {
                request.deny();
            } catch (Throwable ignored) {
            }
        }
        @Override
        public void onShowCustomView(android.view.View view, CustomViewCallback callback) {
            if (callback != null) callback.onCustomViewHidden();
        }
        @Override
        public boolean onJsAlert(WebView v, String url, String msg, android.webkit.JsResult r) {
            r.cancel();
            return true;
        }
        @Override
        public boolean onJsConfirm(WebView v, String url, String msg, android.webkit.JsResult r) {
            r.cancel();
            return true;
        }
        @Override
        public boolean onJsPrompt(WebView v, String url, String msg, String def, android.webkit.JsPromptResult r) {
            r.cancel();
            return true;
        }
    }


}
