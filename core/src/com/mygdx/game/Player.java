package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class Player {

    private Sprite playerSprite;
    private Body playerBody;
    public boolean inAir;
    private float velChangeY, impulseY, velChangeX, impulseX;
    public World world;

    public ArrayList<Bullet> bulletList;

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
        fixtureDefP.restitution = 0.05f;

        playerBody = world.createBody(bodyDefP);
        playerBody.createFixture(fixtureDefP);
        playerBody.setUserData("player"); //for the world listener

        playerCircle.dispose();

        return playerBody;
    }

    public Sprite getPlayerSprite() {
        return playerSprite;
    }

    public Body getPlayerBody() {
        return playerBody;
    }

    public void move (Joystick joystick, Body bodyCeiling, float downwardsVelocity, float upwardsVelocity, float sidewaysVelocity) {
        if(joystick.getKnobPercentY()>0.6 || joystick.getKnobPercentY()<-0.6) { //joystick is in jump range
            if (inAir) {
                if (joystick.getKnobPercentY() < 0) { //quick fall
                    velChangeY = -1*downwardsVelocity - getPlayerBody().getLinearVelocity().y;
                    impulseY = getPlayerBody().getMass() * velChangeY;
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
                if (getPlayerBody().getLinearVelocity().y == 0 && bodyCeiling.getPosition().y - getPlayerBody().getPosition().y > getPlayerSprite().getHeight() / 2) {
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
    }

    //update position of player sprite
    public void updatePosition() {

        playerSprite.setPosition(playerBody.getPosition().x-playerSprite.getWidth()/2, playerBody.getPosition().y-playerSprite.getHeight()/2);

    }

    //click to shoot button
    public void clickToShoot(ShooterUI shooterUI, float xVel, float yVel) {
        if(shooterUI.leftTouch) {
           shoot(1, xVel, yVel);
        } else if(shooterUI.topTouch) {
            shoot(2, xVel, yVel);
        } else if(shooterUI.rightTouch) {
            shoot(3, xVel, yVel);
        } else if(shooterUI.bottomTouch) {
            shoot(4, xVel, yVel);
        }
    }

    //shoot bullet
    public void shoot(int shootOption, float xVel, float yVel) {

        //create bullet first
        Bullet bullet = new Bullet(shootOption, playerSprite, world);
        bulletList.add(bullet); //add to array list of bullets for player

        //choose direction to shoot in
        if(shootOption==1) { //shoot left
            bullet.bulletBody.setLinearVelocity(-xVel, 0);
        } else if(shootOption == 2) { //up
            bullet.bulletBody.setLinearVelocity(playerBody.getLinearVelocity().x, yVel);
        } else if(shootOption == 3) { //right
            bullet.bulletBody.setLinearVelocity(xVel, 0);
        } else if(shootOption == 4) { //down
            bullet.bulletBody.setLinearVelocity(playerBody.getLinearVelocity().x, -yVel);
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