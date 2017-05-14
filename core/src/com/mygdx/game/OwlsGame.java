package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class OwlsGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private Sprite block, player;
	private Stage stage;
	private World world;
	private Body body, body2, body3, bodyP;
	private float pVelX;
	private float pVelY;
	private boolean inAir = false;
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;
	private Touchpad joystick;
	private static final float WIDTH = 64;
	private static final float HEIGHT = 36;
	private static final float GRAVITY = -10.0f;
	private float heightGround;

	@Override
	public void create () {

		//initialize the batch
		batch = new SpriteBatch();

		//create world with -10 gravity in vert direction
		world = new World(new Vector2(0.0f, GRAVITY), true);

		//set camera
		camera = new OrthographicCamera(WIDTH, HEIGHT);

		//create the joystick
		Pixmap background = new Pixmap(Gdx.files.internal("touchBackground.png"));
		Pixmap background2 = new Pixmap(Gdx.graphics.getHeight()/3, Gdx.graphics.getHeight()/3, background.getFormat()); //resizing the texture!
		background2.drawPixmap(background, 0, 0, background.getWidth(), background.getHeight()
				, 0, 0, background2.getWidth(), background2.getHeight());
		Texture jBackground = new Texture(background2);
		background.dispose();
		background2.dispose();
		Pixmap knob = new Pixmap(Gdx.files.internal("touchKnob.png"));
		Pixmap knob2 = new Pixmap(Gdx.graphics.getHeight()/8, Gdx.graphics.getHeight()/8, knob.getFormat()); //resizing the texture!
		knob2.drawPixmap(knob, 0, 0, knob.getWidth(), knob.getHeight()
				, 0, 0, knob2.getWidth(), knob2.getHeight());
		Texture jKnob = new Texture(knob2);
		knob.dispose();
		knob2.dispose();

		joystick = returnJoystick(0, jBackground, jKnob);
		joystick.setBounds(Gdx.graphics.getWidth()/20, Gdx.graphics.getHeight()/10 //origin is bottom left for stage
				, jBackground.getWidth(), jBackground.getWidth()); //joystick is set to screen viewport, so treat as if it's the whole screen not the world

		//create state and add the touchpad as actor
		stage = new Stage(new ScreenViewport());
		stage.addActor(joystick);

		Gdx.input.setInputProcessor(stage);

		//create player sprite
		Texture i = new Texture("whitecircle.png");
		player = new Sprite(i); //400x400 pixels

		player.setSize(HEIGHT/10, HEIGHT/10);
		player.setPosition(0, 10);

		//create player physics box
		BodyDef bodyDefP = new BodyDef();
		bodyDefP.type = BodyDef.BodyType.DynamicBody;
		bodyDefP.position.set(player.getX()+player.getWidth()/2, player.getY()+player.getHeight()/2);

		FixtureDef fixtureDefP = new FixtureDef();
		CircleShape playerCircle = new CircleShape();
		playerCircle.setRadius(player.getWidth()/2);
		fixtureDefP.shape = playerCircle;
		fixtureDefP.density = 4f;
		fixtureDefP.restitution = 0;

		bodyP = world.createBody(bodyDefP);
		bodyP.createFixture(fixtureDefP);

		playerCircle.dispose();

		//create the building block for all platforms
		Texture img = new Texture("block.png");
		block = new Sprite(img); //50x50 pixels
		img.dispose();

		//create ground platform physics box
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		heightGround = HEIGHT/10; //ground height
		bodyDef.position.set(0, -HEIGHT/2+heightGround/2);

		FixtureDef fixtureDef = new FixtureDef();
		PolygonShape ground = new PolygonShape();
		ground.setAsBox(WIDTH, heightGround/2);
		fixtureDef.shape = ground;

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		ground.dispose(); //dispose of box shape, body already created

		//create ground block
		block.setSize(WIDTH, heightGround); //block is now width by heightGround
		block.setPosition(-WIDTH/2, -HEIGHT/2); //set position to bottom left

		//create left wall physics box
		BodyDef bodyDef2 = new BodyDef();
		bodyDef2.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef2 = new FixtureDef();
		EdgeShape wall1 = new EdgeShape();
		wall1.set(-WIDTH/2, -HEIGHT/2, -WIDTH/2, HEIGHT/2);
		fixtureDef2.shape = wall1;

		body2 = world.createBody(bodyDef2);
		body2.createFixture(fixtureDef2);

		wall1.dispose();

		//create right wall physics box
		BodyDef bodyDef3 = new BodyDef();
		bodyDef3.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef3 = new FixtureDef();
		EdgeShape wall2 = new EdgeShape();
		wall2.set(WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2);
		fixtureDef3.shape = wall2;

		body3 = world.createBody(bodyDef3);
		body3.createFixture(fixtureDef3);

		wall2.dispose();

		//set debug renderer
		debugRenderer = new Box2DDebugRenderer();
	}

	@Override
	public void render () {

		//update camera and world
		camera.update();
		world.step(Gdx.graphics.getDeltaTime(), 6, 2); //update world

		//clear the background and allow stuff to print on screen
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//change batch to perspective of camera
		batch.setProjectionMatrix(camera.combined);
		debugMatrix = batch.getProjectionMatrix().cpy();

		//give body of player velocity, inAir is false at beginning of call
		pVelX = joystick.getKnobPercentX()*WIDTH/4;

		if(joystick.getKnobPercentY()>0.95 || joystick.getKnobPercentY()<-0.95) { //joystick is in jump range
			if(inAir) {
				bodyP.applyForceToCenter(0, -10f*bodyP.getMass(), false); //gravity acts on it, no jump
				if(bodyP.getLinearVelocity().y==0) {
					inAir = false;
				}
			} else {
				pVelY = joystick.getKnobPercentY()*1/Math.abs(joystick.getKnobPercentY())*HEIGHT/2; //impart an upwards velocity
				bodyP.setLinearVelocity(pVelX, pVelY);
				inAir = true;
			}
		} else { //not in jump range
			if(inAir) {
				bodyP.applyForceToCenter(0, -10f * bodyP.getMass(), false); //gravity acts on it, no jump
				if (bodyP.getLinearVelocity().y == 0) {
					inAir = false;
				}
			} else {
				pVelY = 0;
				bodyP.setLinearVelocity(pVelX, pVelY);
				inAir = false;
			}
		}

		//change position of player based on body
		player.setPosition(bodyP.getPosition().x-player.getWidth()/2, bodyP.getPosition().y-player.getHeight()/2);

		//execute batch
		batch.begin();
		block.draw(batch);
		player.draw(batch);
		batch.end();

		//move stage for joystick
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		//debug physics boxes/bodies
		debugRenderer.render(world, debugMatrix);

	}

	@Override
	public void dispose () {

		//prevent memory leaks!!!
		batch.dispose();
		world.dispose();
		debugRenderer.dispose();
		stage.dispose();

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	//to create the joystick with given pictures
	public Touchpad returnJoystick(float deadzoneRadius, Texture jBackground, Texture jKnob) {

		Skin joystickSkin = new Skin();

		joystickSkin.add("touchBackground", jBackground);
		joystickSkin.add("touchKnob", jKnob);

		Touchpad.TouchpadStyle joystickStyle = new Touchpad.TouchpadStyle();

		Drawable touchBackground = joystickSkin.getDrawable("touchBackground");
		Drawable touchKnob = joystickSkin.getDrawable("touchKnob");

		joystickStyle.background = touchBackground;
		joystickStyle.knob = touchKnob;

		Touchpad stick = new Touchpad(deadzoneRadius, joystickStyle);

		return stick;
	}
}
