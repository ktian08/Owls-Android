package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;

public class Bullet implements Pool.Poolable {

    public Body bulletBody;
    public Sprite bulletSprite;
    private Fixture bulletFixture;
    public boolean toBeRemoved = false;
    public boolean toBeFreed = false;
    private float vx = 0;
    private float x;
    private float vy = 0;
    private float y;
    private int shootOption = 0;


    public Bullet(int shootOption, Sprite playerSprite, World world) {

        //create sprite
        bulletSprite = new Sprite(new Texture("bullet.png"));
        bulletSprite.setSize(playerSprite.getHeight()/4, playerSprite.getHeight()/4); //size sprite

        //needs a small distance between bullet and player so doesn't register as collision
        float smallDistance = playerSprite.getHeight()/4;

        this.shootOption = shootOption;

        //set initial sprite position
        if(shootOption==1) { //shoot left
            bulletSprite.setPosition(playerSprite.getX()-bulletSprite.getWidth()-smallDistance, playerSprite.getY()+playerSprite.getHeight()/2-bulletSprite.getHeight()/2);
        } else if(shootOption == 2) { //up
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()/2-bulletSprite.getWidth()/2, playerSprite.getY()+playerSprite.getHeight()+smallDistance);
        } else if(shootOption == 3) { //right
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()+smallDistance, playerSprite.getY()+playerSprite.getHeight()/2-bulletSprite.getHeight()/2);
        } else if(shootOption == 4) { //down
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()/2-bulletSprite.getWidth()/2, playerSprite.getY()-bulletSprite.getHeight()-smallDistance);
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
        bulletFixture.setUserData(this); //for world listener
        bulletBody.setGravityScale(0f); //turn off gravity

        bulletCircle.dispose();

    }


    public void setVx(float vel) {vx = vel; bulletBody.setLinearVelocity(vx, vy);}

    public void setVy(float vel) {vy = vel; bulletBody.setLinearVelocity(vx, vy);}

    public float getVx() {return vx;}

    public float getVy() {return vy;}

    public float getX() {
        return bulletBody.getPosition().x;
    }

    public float getY() {
        return bulletBody.getPosition().y;
    }

    public void setX(float pos) {
        bulletBody.setTransform(pos, bulletBody.getPosition().y, 0);
    }

    public void setY(float pos) {
        bulletBody.setTransform(bulletBody.getPosition().x, pos, 0);
    }

    public int getShootOption() {return shootOption;}

    public void setBulletSprite(int shootOption, Texture bulletS, Sprite playerSprite) {
        bulletSprite = new Sprite(bulletS);
        bulletSprite.setSize(playerSprite.getHeight()/4, playerSprite.getHeight()/4); //size sprite

        //needs a small distance between bullet and player so doesn't register as collision
        float smallDistance = playerSprite.getHeight()/4;

        //set initial sprite position
        if(shootOption==1) { //shoot left
            bulletSprite.setPosition(playerSprite.getX()-bulletSprite.getWidth()-smallDistance, playerSprite.getY()+playerSprite.getHeight()/2-bulletSprite.getHeight()/2);
        } else if(shootOption == 2) { //up
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()/2-bulletSprite.getWidth()/2, playerSprite.getY()+playerSprite.getHeight()+smallDistance);
        } else if(shootOption == 3) { //right
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()+smallDistance, playerSprite.getY()+playerSprite.getHeight()/2-bulletSprite.getHeight()/2);
        } else if(shootOption == 4) { //down
            bulletSprite.setPosition(playerSprite.getX()+playerSprite.getWidth()/2-bulletSprite.getWidth()/2, playerSprite.getY()-bulletSprite.getHeight()-smallDistance);
        }
    }

    public void setShootOption(int shootOption) {
        this.shootOption =  shootOption;
    }

    @Override
    public void reset() {
        toBeRemoved = false;
        toBeFreed = false;
        shootOption = 0;
    }
}
