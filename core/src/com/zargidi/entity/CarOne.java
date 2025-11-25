package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by ilimturan on 18/01/15.
 */
public class CarOne extends Car {


    public CarOne(Vector2 pos, Vector2 direction) {

        super("CAR1", "L", new Texture(Gdx.files.internal("car1.png")), pos, direction);

    }

    @Override
    public void update() {

        pos.add(direction);
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.justTouched()) {

            int touchXCoordinant = Gdx.input.getX();

            if (touchXCoordinant < Gdx.graphics.getWidth() / 2) {
                if ("L".equals(lane)) {
                    targetX = (3 * Gdx.graphics.getWidth() / 8f) - (carWidth / 2f);
                    lane = "R";
                } else {
                    targetX = (1 * Gdx.graphics.getWidth() / 8f) - (carWidth / 2f);
                    lane = "L";
                }


            }
        } else {
            setDirection(0, 0);
        }
        moveTowardsLane(delta);
        this.smokeEffect.setPosition(this.pos.x + carWidth / 2f, this.pos.y + carHeight / 10f);
        this.smokeEffect.update(delta);

    }


}
