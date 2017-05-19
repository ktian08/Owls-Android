package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Wall {

    private Body wallBody;
    private Fixture wallFixture;

    public World world;

    public Wall(float xPos1, float yPos1, float xPos2, float yPos2, World world) {

        this.world = world;

        wallBody = createWallBody(xPos1, yPos1, xPos2, yPos2);

    }

    private Body createWallBody(float xPos1, float yPos1, float xPos2, float yPos2) {

        BodyDef bodyDef = new BodyDef(); //create bodyDef
        bodyDef.type = BodyDef.BodyType.StaticBody;

        FixtureDef fixtureDef = new FixtureDef(); //create fixtureDef
        EdgeShape shape = new EdgeShape();
        shape.set(xPos1, yPos1, xPos2, yPos2);
        fixtureDef.shape = shape;

        wallBody = world.createBody(bodyDef); //create the body
        wallFixture = wallBody.createFixture(fixtureDef);
        wallFixture.setUserData(this); //for world listener
        shape.dispose();

        return wallBody;

    }


}
