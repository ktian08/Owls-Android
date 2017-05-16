package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player {

    Sprite playerSprite;

    public Player(Sprite sprite, float width, float height, float xPos, float yPos) {

        playerSprite = sprite;
        playerSprite.setSize(width, height);
        playerSprite.setPosition(xPos, yPos);

    }

}
