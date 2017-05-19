package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Platform {

    private Sprite platformSprite;
    private Body platformBody;
    private Fixture platformFixture;
    public float platformWidth; public float platformHeight;

    public World world;

    public Platform(float width, float height, float xPos, float yPos, World world) {

        this.world = world;
        platformHeight = height; platformWidth = width;

        Texture texture = new Texture("block.png");  //create sprite with position, size
        platformSprite = new Sprite(texture);
        texture.dispose();
        platformSprite.setPosition(xPos, yPos);
        platformSprite.setSize(width, height);

        platformBody = createPlatformBody();

    }

    private Body createPlatformBody() {

        BodyDef bodyDef = new BodyDef(); //create bodyDef
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(platformSprite.getX()+platformSprite.getWidth()/2, platformSprite.getY()+platformSprite.getHeight()/2);

        FixtureDef fixtureDef = new FixtureDef(); //create fixtureDef
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(platformWidth/2, platformHeight/2);
        fixtureDef.shape = shape;

        platformBody = world.createBody(bodyDef); //create the body
        platformFixture = platformBody.createFixture(fixtureDef);
        platformFixture.setUserData(this); //for world listener
        shape.dispose();

        return platformBody;

    }

    public Sprite getPlatformSprite() {
        return platformSprite;
    }

}
