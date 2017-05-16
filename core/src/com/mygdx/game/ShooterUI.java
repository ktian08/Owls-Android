package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ShooterUI {

    protected ShooterButtonActor bottomButton;
    protected ShooterButtonActor topButton;
    protected ShooterButtonActor leftButton;
    protected ShooterButtonActor rightButton;

    float commonDistance;

    public ShooterUI(float commonDistance) {

        bottomButton = new ShooterButtonActor();
        topButton = new ShooterButtonActor();
        leftButton = new ShooterButtonActor();
        rightButton = new ShooterButtonActor();

        this.commonDistance = commonDistance;

    }

    public void configureShooterUI(float bottomButtonX, float bottomButtonY, float bottomButtonWidth, float bottomButtonHeight) {

        bottomButton.setX(bottomButtonX); bottomButton.setY(bottomButtonY);
        bottomButton.setWidth(bottomButtonWidth); bottomButton.setHeight(bottomButtonHeight); //make sure it stays in bounds

        topButton.setX(bottomButton.getX()); topButton.setY(bottomButton.getY()+commonDistance*2);
        topButton.setWidth(bottomButton.getWidth()); topButton.setHeight(bottomButton.getHeight());

        rightButton.setX(bottomButton.getX()+commonDistance); rightButton.setY(bottomButton.getY()+commonDistance);
        rightButton.setWidth(bottomButton.getWidth()); rightButton.setHeight(bottomButton.getWidth()); //make sure it stays in bounds!

        leftButton.setX(bottomButton.getX()-commonDistance); leftButton.setY(rightButton.getY());
        leftButton.setWidth(bottomButton.getWidth()); leftButton.setHeight(bottomButton.getWidth());

    }

    private class ShooterButtonActor extends Actor {
        Sprite button;

        public ShooterButtonActor() {
            Texture texture = new Texture("shootButton.png");
            button = new Sprite(texture);
        }

        @Override
        public void draw(Batch batch, float alpha) {
            batch.draw(button, getX(), getY(), getWidth(), getHeight());
        }
    }

}

