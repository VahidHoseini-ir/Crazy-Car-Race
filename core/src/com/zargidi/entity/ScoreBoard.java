package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.zargidi.ccar.MainGame;

/**
 * Created by ilimturan on 20/01/15.
 */
public class ScoreBoard extends Entity {

    BitmapFont userScoreTable;
    BitmapFont scoreLabelFont;
    String userScoreCurrent;
    private Texture backgroundTexture;
    private int padding = 26;

    public ScoreBoard(Vector2 pos, Vector2 direction) {

        super(new Texture(Gdx.files.internal("score_board.png")), pos, direction);
        this.eType = 4;
        userScoreTable = new BitmapFont();
        userScoreTable.getData().setScale(3.5F);
        userScoreTable.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        scoreLabelFont = new BitmapFont();
        scoreLabelFont.getData().setScale(2.2f);
        scoreLabelFont.setColor(new Color(0.93f, 0.88f, 0.3f, 1f));

        int bgWidth = texture.getWidth() + padding * 2;
        int bgHeight = texture.getHeight() + padding * 2;
        Pixmap pixmap = new Pixmap(bgWidth, bgHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.55f));
        pixmap.fillRectangle(0, 0, bgWidth, bgHeight);
        pixmap.setColor(new Color(1f, 1f, 1f, 0.45f));
        pixmap.drawRectangle(2, 2, bgWidth - 4, bgHeight - 4);
        backgroundTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update() {
        pos.add(direction);

        if (MainGame.scoreCurrent > 0) {
            userScoreCurrent = "" + MainGame.scoreCurrent;
        } else {
            userScoreCurrent = "0";
        }
    }

    public void render(SpriteBatch sb) {

        float bgX = pos.x - padding;
        float bgY = pos.y - padding / 1.5f;
        sb.draw(backgroundTexture, bgX, bgY);

        sb.draw(texture, pos.x, pos.y);
        scoreLabelFont.draw(sb, "SCORE", (this.pos.x + this.texture.getWidth() / 2f) - 55f, (this.pos.y + this.texture.getHeight()) - 22f);
        userScoreTable.draw(sb, "" + MainGame.scoreCurrent, (this.pos.x + this.texture.getWidth() / 2f) - 15f, (this.pos.y + this.texture.getHeight() / 2f));

    }


}
