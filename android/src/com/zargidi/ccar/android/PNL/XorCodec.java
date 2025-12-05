package com.zargidi.ccar.android.PNL;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class XorCodec {

    public static XorCodec instance = null;

    public static XorCodec getInstance() {
        if (instance == null) {
            instance = new XorCodec();
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * دیکد با XOR و برگرداندن بایت‌های خام (immutable)
     */
    public byte[] xorDecodeToBytes(byte[] obf, byte mask) {
        if (obf == null || obf.length == 0) return new byte[0];
        byte[] out = new byte[obf.length];
        for (int i = 0; i < obf.length; i++) {
            // & 0xFF برای جلوگیری از sign-extension
            out[i] = (byte) ((obf[i] ^ mask) & 0xFF);
        }
        return out;
    }

    /**
     * دیکد با XOR و تبدیل به رشته با UTF-8 (در صورت خطا، ISO-8859-1)
     */
    public String xorDecodeToString(byte[] obf, byte mask) {
        byte[] decoded = xorDecodeToBytes(obf, mask);
        try {
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            // اگر محتوا متن نبود یا UTF-8 معتبر نبود، از ISO-8859-1 استفاده می‌کنیم
            return new String(decoded, Charset.forName("ISO-8859-1"));
        }
    }

    /**
     * اینکد مجدد (XOR کردن) — معکوس دیکد است
     */
    public byte[] xorEncode(byte[] plain, byte mask) {
        if (plain == null || plain.length == 0) return new byte[0];
        byte[] out = new byte[plain.length];
        for (int i = 0; i < plain.length; i++) {
            out[i] = (byte) ((plain[i] ^ mask) & 0xFF);
        }
        return out;
    }
}
