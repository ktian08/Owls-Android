package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Bullet {

    public Body bulletBody;
    public Sprite bulletSprite;
    private Fixture bulletFixture;

    public Bullet(int shootOption, Sprite playerSprite, World world) {

        //create sprite
        bulletSprite = new Sprite(new Texture("bullet.png"));
        bulletSprite.setSize(playerSprite.getHeight()/4, playerSprite.getHeight()/4); //size sprite

        //set initial sprite position
        if(shootOption==1) { //shoot left
            bulletSprite.setPosition(playerSprite.getX()-bulletSprite.getWidth(), playerSprite.getY()+playerSprite.getHeight()/2-bulletSprite.getHeight()/2);
        } else if(shootOption == 2) { //up
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()/2-bulletSprite.getWidth()/2, playerSprite.getY()+playerSprite.getHeight());
        } else if(shootOption == 3) { //right
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth(), playerSprite.getY()+playerSprite.getHeight()/2-bulletSprite.getHeight()/2);
        } else if(shootOption == 4) { //down
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()/2-bulletSprite.getWidth()/2, playerSprite.getY()-bulletSprite.getHeight()/2);
        }

        //configure bullet body
        BodyDef bodyDefB = new BodyDef();
        bodyDefB.type = BodyDef.BodyType.DynamicBody;
        bodyDefB.position.set(bulletSprite.getX()+bulletSprite.getWidth()/2, bulletSprite.getY()+bulletSprite.getHeight()/2);

        FixtureDef fixtureDefB = new FixtureDef();
        CircleShape bulletCircle = new CircleShape();
        bulletCircle.setRadius(bulletSprite.getWidth()/2);
        fixtureDefB.shape = bulletCircle;

        bulletBody = world.createBody(bodyDefB);
        bulletFixture = bulletBody.createFixture(fixtureDefB);
        bulletFixture.setUserData("bullet"); //for world listener
        bulletBody.setGravityScale(0f); //turn off gravity

        bulletCircle.dispose();

    }
}
