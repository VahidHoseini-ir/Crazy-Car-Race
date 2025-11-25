package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.zargidi.ccar.MainGame;

/**
 * Created by ilimturan on 20/01/15.
 */
public class ScoreBoard extends Entity {

    BitmapFont userScoreTable;
    BitmapFont labelFont;
    GlyphLayout labelLayout;
    GlyphLayout scoreLayout;
    String userScoreCurrent;
    float paddingX = 32f;
    float paddingY = 22f;
    int lastScreenWidth;
    int lastScreenHeight;

    public ScoreBoard(Vector2 pos, Vector2 direction) {

        super(createPlaceholderTexture(), pos, direction);
        this.eType = 4;
        userScoreTable = new BitmapFont();
        userScoreTable.getData().setScale(3.2f);
        userScoreTable.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        labelFont = new BitmapFont();
        labelFont.getData().setScale(2.4f);
        labelFont.setColor(1f, 1f, 1f, 0.92f);

        labelLayout = new GlyphLayout();
        scoreLayout = new GlyphLayout();
        userScoreCurrent = "0";
        lastScreenWidth = Gdx.graphics.getWidth();
        lastScreenHeight = Gdx.graphics.getHeight();
        rebuildBackground();
    }

    @Override
    public void update() {
        pos.add(direction);

        String nextScore = MainGame.scoreCurrent > 0 ? String.valueOf(MainGame.scoreCurrent) : "0";
        if (!nextScore.equals(userScoreCurrent) || screenSizeChanged()) {
            userScoreCurrent = nextScore;
            rebuildBackground();
        }
    }

    public void render(SpriteBatch sb) {

        sb.draw(texture, pos.x, pos.y);

        float centerX = pos.x + texture.getWidth() / 2f;
        float labelY = pos.y + texture.getHeight() - paddingY;
        float scoreY = pos.y + paddingY + scoreLayout.height;

        labelFont.draw(sb, "SCORE", centerX - labelLayout.width / 2f, labelY);
        userScoreTable.draw(sb, userScoreCurrent, centerX - scoreLayout.width , scoreY);

    }


    private static Texture createPlaceholderTexture() {
        Pixmap placeholder = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        placeholder.setColor(0, 0, 0, 0);
        placeholder.fill();
        Texture t = new Texture(placeholder);
        placeholder.dispose();
        return t;
    }

    private void rebuildBackground() {
        labelLayout.setText(labelFont, "SCORE");
        scoreLayout.setText(userScoreTable, userScoreCurrent);

        float width = Math.max(labelLayout.width, scoreLayout.width) + paddingX * 2f;
        float height = labelLayout.height + scoreLayout.height + paddingY * 3f;

        Pixmap pixmap = new Pixmap(Math.max(1, Math.round(width)), Math.max(1, Math.round(height)), Pixmap.Format.RGBA8888);
        drawRoundedRect(pixmap, 14, 0, 0, pixmap.getWidth(), pixmap.getHeight());

        if (texture != null) {
            texture.dispose();
        }
        texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pixmap.dispose();

        float marginLeft = 24f;

        pos.x = marginLeft;
        pos.y = Gdx.graphics.getHeight() - height - 24f;

        lastScreenWidth = Gdx.graphics.getWidth();
        lastScreenHeight = Gdx.graphics.getHeight();
    }

    private boolean screenSizeChanged() {
        return lastScreenWidth != Gdx.graphics.getWidth() || lastScreenHeight != Gdx.graphics.getHeight();
    }

    private void drawRoundedRect(Pixmap pixmap, int radius, int x, int y, int width, int height) {
        pixmap.setColor(0f, 0f, 0f, 0.65f);
        pixmap.fillRectangle(x + radius, y, width - 2 * radius, height);
        pixmap.fillRectangle(x, y + radius, width, height - 2 * radius);
        pixmap.fillCircle(x + radius, y + radius, radius);
        pixmap.fillCircle(x + width - radius - 1, y + radius, radius);
        pixmap.fillCircle(x + radius, y + height - radius - 1, radius);
        pixmap.fillCircle(x + width - radius - 1, y + height - radius - 1, radius);

        pixmap.setColor(1f, 1f, 1f, 0.35f);
        pixmap.drawRectangle(x + 1, y + 1, width - 2, height - 2);
    }


}
