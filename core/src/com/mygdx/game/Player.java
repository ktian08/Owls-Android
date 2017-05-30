package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class Player {

    private Sprite playerSprite;
    private Body playerBody;
    private Fixture playerFixture;
    public boolean inAir;
    private float velChangeY, impulseY, velChangeX, impulseX;
    public World world;
    public long timeAfterLastBullet = 0;
    public boolean canShoot = true;
    public ArrayList<Bullet> bulletList;
    public boolean hasMoved = false;
    public boolean hasShot = false;

    public Player(Sprite sprite, float width, float height, float xPos, float yPos, World world) {

        bulletList = new ArrayList<Bullet>();

        this.world = world;

        playerSprite = sprite;
        playerSprite.setSize(width, height);
        playerSprite.setPosition(xPos, yPos);

        playerBody = createPlayerBody();

        if(getPlayerBody().getLinearVelocity().y!=0) {
            inAir = true;
        } else {
            inAir = false;
        }

    }

    private Body createPlayerBody() {

        BodyDef bodyDefP = new BodyDef();
        bodyDefP.type = BodyDef.BodyType.DynamicBody;
        bodyDefP.position.set(playerSprite.getX()+playerSprite.getWidth()/2, playerSprite.getY()+playerSprite.getHeight()/2);

        FixtureDef fixtureDefP = new FixtureDef();
        CircleShape playerCircle = new CircleShape();
        playerCircle.setRadius(playerSprite.getWidth()/2);
        fixtureDefP.shape = playerCircle;
        fixtureDefP.density = 4f;
        fixtureDefP.restitution = 0.01f;

        playerBody = world.createBody(bodyDefP);
        playerFixture = playerBody.createFixture(fixtureDefP);
        playerFixture.setUserData(this); //for the world listener

        playerCircle.dispose();

        return playerBody;

    }

    public Sprite getPlayerSprite() {
        return playerSprite;
    }

    public Body getPlayerBody() {
        return playerBody;
    }

    public void move (Joystick joystick, float downwardsVelocity, float upwardsVelocity, float sidewaysVelocity) {

        if(joystick.getKnobPercentY()>0.5 || joystick.getKnobPercentY()<-0.5) { //joystick is in jump range
            if (inAir) {
                if (joystick.getKnobPercentY() < 0) { //quick fall
                    velChangeY = -downwardsVelocity - getPlayerBody().getLinearVelocity().y;
                    impulseY = getPlayerBody().getMass() * velChangeY * 1.5f;
                    getPlayerBody().applyLinearImpulse(0, impulseY, getPlayerBody().getPosition().x, getPlayerBody().getPosition().y, true);
                }
                if (getPlayerBody().getLinearVelocity().y == 0f) {
                    inAir = false;
                }
            } else {
                velChangeY = upwardsVelocity - getPlayerBody().getLinearVelocity().y;
                impulseY = getPlayerBody().getMass() * velChangeY;
                getPlayerBody().applyLinearImpulse(0, impulseY, getPlayerBody().getPosition().x, getPlayerBody().getPosition().y, true);

                inAir = true;
            }
        } else { //not in jump range
            if(inAir) {
                if (getPlayerBody().getLinearVelocity().y == 0f) {
                    inAir = false;
                }
            } else {
                velChangeX = joystick.getKnobPercentX()*sidewaysVelocity - getPlayerBody().getLinearVelocity().x;
                impulseX = getPlayerBody().getMass()*velChangeX;
                getPlayerBody().applyLinearImpulse(impulseX, 0, getPlayerBody().getPosition().x, getPlayerBody().getPosition().y, true);

                if(getPlayerBody().getLinearVelocity().y != 0f) {
                    inAir = true;
                }
            }
        }

        hasMoved = true;

    }

//    public float getImpulseX(Joystick joystick, float sidewaysVelocity) {
//
//        if(!(joystick.getKnobPercentY()>0.5 || joystick.getKnobPercentY()<-0.5)) {
//            if(!inAir) {
//                velChangeX = joystick.getKnobPercentX()*sidewaysVelocity - getPlayerBody().getLinearVelocity().x;
//                impulseX = getPlayerBody().getMass()*velChangeX;
//                return impulseX;
//            }
//        }
//
//        return 0f;
//
//    }
//
//    public float getImpulseY(Joystick joystick, float downwardsVelocity, float upwardsVelocity) {
//
//        if(joystick.getKnobPercentY()>0.5 || joystick.getKnobPercentY()<-0.5) { //joystick is in jump range
//            if (inAir) {
//                if (joystick.getKnobPercentY() < 0) { //quick fall
//                    velChangeY = -downwardsVelocity - getPlayerBody().getLinearVelocity().y;
//                    impulseY = getPlayerBody().getMass() * velChangeY * 1.5f;
//                    return impulseY;
//                }
//            } else {
//                velChangeY = upwardsVelocity - getPlayerBody().getLinearVelocity().y;
//                impulseY = getPlayerBody().getMass() * velChangeY;
//                return impulseY;
//            }
//        }
//
//        return 0f;
//
//    }

    //update position
    public void updatePlayerPos() {
        playerSprite.setPosition(playerBody.getPosition().x-playerSprite.getWidth()/2, playerBody.getPosition().y-playerSprite.getHeight()/2);
    }

    //click to shoot button
    public void clickToShoot(ShooterUI shooterUI, float xVel, float yVel, float shootDelay) {

        timeAfterLastBullet+=1000* Gdx.graphics.getDeltaTime(); //in milliseconds
        if(timeAfterLastBullet>shootDelay) {
            canShoot = true;
        }

        if(canShoot) {
            if (shooterUI.leftTouch) {
                shoot(1, xVel, yVel);
                canShoot = false;
                timeAfterLastBullet = 0;
                hasShot = true;
            } else if (shooterUI.topTouch) {
                shoot(2, xVel, yVel);
                canShoot = false;
                timeAfterLastBullet = 0;
                hasShot = true;
            } else if (shooterUI.rightTouch) {
                shoot(3, xVel, yVel);
                canShoot = false;
                timeAfterLastBullet = 0;
                hasShot = true;
            } else if (shooterUI.bottomTouch) {
                shoot(4, xVel, yVel);
                canShoot = false;
                timeAfterLastBullet = 0;
                hasShot = true;
            }
        }

    }

    //shoot bullet
    public void shoot(int shootOption, float xVel, float yVel) {

        //create bullet first
        Bullet bullet = new Bullet(shootOption, playerSprite, world);
        bulletList.add(bullet); //add to array list of bullets for player

        //choose direction to shoot in
        if(shootOption==1) { //shoot left
            bullet.setVx(-xVel);
            bullet.setVy(0);
            bullet.bulletBody.setLinearVelocity(-xVel, 0);
        } else if(shootOption == 2) { //up
            bullet.setVx(0);
            bullet.setVy(yVel);
            bullet.bulletBody.setLinearVelocity(0, yVel);
        } else if(shootOption == 3) { //right
            bullet.setVx(xVel);
            bullet.setVy(0);
            bullet.bulletBody.setLinearVelocity(xVel, 0);
        } else if(shootOption == 4) { //down
            bullet.setVx(0);
            bullet.setVy(-yVel);
            bullet.bulletBody.setLinearVelocity(0, -yVel);
        }

    }

    //update bullet positions
    public void updateBulletPositions() {

        for(int i = 0; i<bulletList.size(); i++) {
            bulletList.get(i).bulletSprite.setPosition(bulletList.get(i).bulletBody.getPosition().x - bulletList.get(i).bulletSprite.getWidth() / 2
                    , bulletList.get(i).bulletBody.getPosition().y - bulletList.get(i).bulletSprite.getHeight() / 2);
        }

    }

    //draw the bullet sprites in proper positions
    public void drawAllBullets(Batch batch) {
        for(int i = 0; i<bulletList.size(); i++) {
            bulletList.get(i).bulletSprite.draw(batch);
        }
    }


}
