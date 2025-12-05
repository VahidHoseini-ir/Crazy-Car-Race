package com.zargidi.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.zargidi.ccar.MainGame;
import com.zargidi.ccar.TextureManager;

/**
 * Created by ilimturan on 18/01/15.
 */
public abstract  class Car extends Entity {

    protected String lane = "L";
    protected int carWidth;
    protected int carHeight;
    protected ParticleEffect smokeEffect;
    protected float targetX;
    protected float laneSwitchSpeed = 10f;

    public Car(String name, String lane, Texture texture, Vector2 pos, Vector2 direction) {

        super(texture, pos, direction);

        this.eType = 1;

        if(name.equals("CAR1")){
            carWidth = TextureManager.CAR1.getWidth();
            carHeight = TextureManager.CAR1.getHeight();
        }else if(name.equals("CAR2")){
            carWidth = TextureManager.CAR2.getWidth();
            carHeight = TextureManager.CAR2.getHeight();
        }

        targetX = pos.x;


        smokeEffect = new ParticleEffect();
        smokeEffect.load(Gdx.files.internal("effects/car_smoke_1.p"), Gdx.files.internal("effect_img"));
        smokeEffect.setPosition(this.pos.x + carWidth / 2, this.pos.y + carHeight / 10 );
        smokeEffect.start();
    }


    protected void smoothMoveToTarget(float delta) {
        if (Math.abs(targetX - pos.x) < 0.5f) {
            pos.x = targetX;
            return;
        }

        float t = Math.min(1f, delta * laneSwitchSpeed);
        pos.x = MathUtils.lerp(pos.x, targetX, t);
    }



    @Override
    public void render(SpriteBatch sb){
        smokeEffect.draw(sb);
        sb.draw(texture, this.pos.x, this.pos.y);
    }
}
