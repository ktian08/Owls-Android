package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ShooterUI {

    public ShooterButtonActor bottomButton;
    public ShooterButtonActor topButton;
    public ShooterButtonActor leftButton;
    public ShooterButtonActor rightButton;
    public boolean bottomTouch, topTouch, leftTouch, rightTouch;
    public ClickListener bottomListener, topListener, leftListener, rightListener;

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
        bottomButton.setBounds(bottomButton.getX(), bottomButton.getY(), bottomButton.getWidth(), bottomButton.getHeight());

        bottomListener = new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                bottomTouch = true;
                topTouch = false;
                rightTouch = false;
                leftTouch = false;
            }
        };
        bottomButton.addListener(bottomListener);

        topButton.setX(bottomButton.getX()); topButton.setY(bottomButton.getY()+commonDistance*2);
        topButton.setWidth(bottomButton.getWidth()); topButton.setHeight(bottomButton.getHeight());
        topButton.setBounds(topButton.getX(), topButton.getY(), topButton.getWidth(), topButton.getHeight());

        topListener = new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                bottomTouch = false;
                topTouch = true;
                rightTouch = false;
                leftTouch = false;
            }
        };
        topButton.addListener(topListener);

        rightButton.setX(bottomButton.getX()+commonDistance); rightButton.setY(bottomButton.getY()+commonDistance);
        rightButton.setWidth(bottomButton.getWidth()); rightButton.setHeight(bottomButton.getWidth()); //make sure it stays in bounds!
        rightButton.setBounds(rightButton.getX(), rightButton.getY(), rightButton.getWidth(), rightButton.getHeight());

        rightListener = new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                bottomTouch = false;
                topTouch = false;
                rightTouch = true;
                leftTouch = false;
            }
        };
        rightButton.addListener(rightListener);

        leftButton.setX(bottomButton.getX()-commonDistance); leftButton.setY(rightButton.getY());
        leftButton.setWidth(bottomButton.getWidth()); leftButton.setHeight(bottomButton.getWidth());
        leftButton.setBounds(leftButton.getX(), leftButton.getY(), leftButton.getWidth(), leftButton.getHeight());

        leftListener = new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                bottomTouch = false;
                topTouch = false;
                rightTouch = false;
                leftTouch = true;
            }
        };
        leftButton.addListener(leftListener);


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

    //at end of render loop so doesn't keep continuously shooting
    public void resetBooleans() {
        bottomTouch = false;
        topTouch = false;
        rightTouch = false;
        leftTouch = false;
    }

}

