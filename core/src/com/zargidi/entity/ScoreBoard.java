package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.zargidi.ccar.MainGame;

public class ScoreBoard extends Entity {

    // فاصله‌ی امن از بالای صفحه (برای اینکه داخل ناچ نرود)
    private static final float SAFE_TOP_MARGIN_PX = 72f; // اگر هنوز نزدیک ناچ بود، این را بیشتر کن

    private BitmapFont labelFont;   // SCORE / BEST
    private BitmapFont valueFont;   // اعداد
    private GlyphLayout layout;

    private String scoreText = "0";
    private String bestText  = "0";

    private float paddingX = 32f;
    private float paddingY = 22f;

    private float columnWidth;
    private float labelHeight;
    private float valueHeight;

    private float scoreLabelWidth, bestLabelWidth;
    private float scoreValueWidth, bestValueWidth;

    private int lastScore  = -1;
    private int lastBest   = -1;
    private int lastScreenWidth;
    private int lastScreenHeight;

    public ScoreBoard(Vector2 pos, Vector2 direction) {
        // تکسچر موقت
        super(createPlaceholderTexture(), pos, direction);
        this.eType = 4;

        layout = new GlyphLayout();

        // --- اسکیل فونت نسبت به عرض صفحه (برای اینکه ریز نباشد) ---
        float baseWidth   = 720f; // عرض مرجع
        float scaleFactor = Gdx.graphics.getWidth() / baseWidth;
        scaleFactor       = Math.max(1.0f, Math.min(scaleFactor, 2.0f)); // بین ۱ و ۲

        valueFont = new BitmapFont();
        valueFont.getData().setScale(3.0f * scaleFactor);
        valueFont.setColor(Color.WHITE);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(2.2f * scaleFactor);
        labelFont.setColor(Color.WHITE);

        lastScreenWidth  = Gdx.graphics.getWidth();
        lastScreenHeight = Gdx.graphics.getHeight();

        rebuildBackground();
    }

    private static final String PREF_NAME   = "userScore";
    private static final String KEY_SCORE   = "scoreCurrent";

    @Override
    public void update() {
        pos.add(direction);

        int score = (MainGame.scoreCurrent != null) ? MainGame.scoreCurrent : 0;
        int best  = (MainGame.scoreHigh    != null) ? MainGame.scoreHigh    : 0;

        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        prefs.putInteger(KEY_SCORE, score);
        prefs.flush();


        if (score != lastScore || best != lastBest || screenSizeChanged()) {
            lastScore = score;
            lastBest  = best;

            scoreText = String.valueOf(score);
            bestText  = String.valueOf(best);

            rebuildBackground();
        }
    }

    public void render(SpriteBatch sb) {
        sb.draw(texture, pos.x, pos.y);

        float labelY = pos.y + texture.getHeight() - paddingY;
        float valueY = pos.y + paddingY + valueHeight;

        // ✔️ دو تا مرکز بر اساس عرض واقعی تکسچر
        float panelWidth = texture.getWidth();
        float half       = panelWidth / 2f;

        float centerLeft  = pos.x + half * 0.5f;   // مرکز نیمه‌ی چپ
        float centerRight = pos.x + half * 1.5f;   // مرکز نیمه‌ی راست

        // رنگ‌ها
        Color scoreColor = new Color(0.0f, 0.95f, 1f, 1f);
        Color bestColor  = new Color(1f, 0.35f, 0.90f, 1f);
        Color shadow     = new Color(0f, 0f, 0f, 0.7f);

        // ── SCORE ──
        labelFont.setColor(shadow);
        labelFont.draw(sb, "SCORE",
          centerLeft - scoreLabelWidth / 2f, labelY - 2f);
        valueFont.setColor(shadow);
        valueFont.draw(sb, scoreText,
          centerLeft - scoreValueWidth / 2f, valueY - 2f);

        labelFont.setColor(scoreColor);
        labelFont.draw(sb, "SCORE",
          centerLeft - scoreLabelWidth / 2f, labelY);
        valueFont.setColor(scoreColor);
        valueFont.draw(sb, scoreText,
          centerLeft - scoreValueWidth / 2f, valueY);

        // ── BEST ──
        labelFont.setColor(shadow);
        labelFont.draw(sb, "BEST",
          centerRight - bestLabelWidth / 2f, labelY - 2f);
        valueFont.setColor(shadow);
        valueFont.draw(sb, bestText,
          centerRight - bestValueWidth / 2f, valueY - 2f);

        labelFont.setColor(bestColor);
        labelFont.draw(sb, "BEST",
          centerRight - bestLabelWidth / 2f, labelY);
        valueFont.setColor(bestColor);
        valueFont.draw(sb, bestText,
          centerRight - bestValueWidth / 2f, valueY);

        // برگردوندن رنگ‌ها
        labelFont.setColor(Color.WHITE);
        valueFont.setColor(Color.WHITE);
    }

    // ──────────────────────── helpers

    private static Texture createPlaceholderTexture() {
        Pixmap placeholder = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        placeholder.setColor(0, 0, 0, 0);
        placeholder.fill();
        Texture t = new Texture(placeholder);
        placeholder.dispose();
        return t;
    }

    private void rebuildBackground() {
        // اندازه‌ی متن‌ها برای چینش
        layout.setText(labelFont, "SCORE");
        scoreLabelWidth = layout.width;
        labelHeight     = layout.height;

        layout.setText(labelFont, "BEST");
        bestLabelWidth = layout.width;

        layout.setText(valueFont, scoreText);
        scoreValueWidth = layout.width;
        valueHeight     = layout.height;

        layout.setText(valueFont, bestText);
        bestValueWidth = layout.width;

        columnWidth = Math.max(
          Math.max(scoreLabelWidth, scoreValueWidth),
          Math.max(bestLabelWidth,  bestValueWidth)
        );

        float width  = columnWidth * 2f + paddingX * 3f;
        float height = labelHeight + valueHeight + paddingY * 3f;

        // حداقل ۷۰٪ عرض صفحه، تا روی موبایل کشیده و شیک باشد
        float minWidth = Gdx.graphics.getWidth() * 0.7f;
        if (width < minWidth) width = minWidth;

        Pixmap pixmap = new Pixmap(
          Math.max(1, Math.round(width)),
          Math.max(1, Math.round(height)),
          Pixmap.Format.RGBA8888
        );

        drawPanel(pixmap);

        if (texture != null) texture.dispose();
        texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear); // آنتی‌الیاس بهتر
        pixmap.dispose();

        // ── جای‌گیری: بالا + فاصله از ناچ ──
        pos.x = (Gdx.graphics.getWidth() - texture.getWidth()) / 2f;
        pos.y = Gdx.graphics.getHeight() - texture.getHeight() - SAFE_TOP_MARGIN_PX;

        lastScreenWidth  = Gdx.graphics.getWidth();
        lastScreenHeight = Gdx.graphics.getHeight();
    }

    private boolean screenSizeChanged() {
        return lastScreenWidth  != Gdx.graphics.getWidth()
          || lastScreenHeight != Gdx.graphics.getHeight();
    }

    /**
     * یک پنل مدرن، تیره و شفاف با گوشه‌های گرد و حاشیه‌ی نرم
     */
    private void drawPanel(Pixmap pixmap) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();
        int radius = 22;

        // پس‌زمینه: بنفش خیلی تیره + شفاف (روی نئون خیلی خوب می‌نشیند)
        Color bg = new Color(0.03f, 0.01f, 0.09f, 0.88f);
        pixmap.setColor(bg);
        fillRoundedRect(pixmap, radius, 0, 0, w, h);

        // یک حاشیه‌ی داخلی روشن‌تر برای حس شیشه‌ای
        pixmap.setColor(0.35f, 0.60f, 1f, 0.35f);
        pixmap.drawRectangle(2, 2, w - 4, h - 4);

        // حاشیه‌ی بیرونی نرم
        pixmap.setColor(0.7f, 0.8f, 1f, 0.55f);
        pixmap.drawRectangle(1, 1, w - 2, h - 2);
    }

    private void fillRoundedRect(Pixmap pixmap, int radius,
                                 int x, int y, int width, int height) {
        pixmap.fillRectangle(x + radius, y, width - 2 * radius, height);
        pixmap.fillRectangle(x, y + radius, width, height - 2 * radius);
        pixmap.fillCircle(x + radius, y + radius, radius);
        pixmap.fillCircle(x + width - radius - 1, y + radius, radius);
        pixmap.fillCircle(x + radius, y + height - radius - 1, radius);
        pixmap.fillCircle(x + width - radius - 1, y + height - radius - 1, radius);
    }
}
