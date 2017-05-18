package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.mygdx.game.Joystick.returnTouchpadStyle;

public class OwlsGame extends ApplicationAdapter {

	private SpriteBatch batch;
	private World world;
	private Stage stage;

	private Sprite block, block2, playerSprite;
	private Player player1;
	private Body body, body2, body3, body4, body5;
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;
	private Joystick joystick;
	private static final float WIDTH = 64;
	private static final float HEIGHT = 36;
	private static final float GRAVITY = -12.0f;
	private float heightGround;
	private float heightPlatform;
	private ShooterUI shooterUI;

	@Override
	public void create () {

		//initialize the batch
		batch = new SpriteBatch();

		//create world with -12 gravity in vert direction
		world = new World(new Vector2(0.0f, GRAVITY), true);

		//set camera
		camera = new OrthographicCamera(WIDTH, HEIGHT);

		//create the joystick
		FileHandle backgroundHandle = Gdx.files.internal("touchBackground.png");
		FileHandle knobHandle = Gdx.files.internal("touchKnob.png");

		Texture jBackground = resizedTexture(backgroundHandle, Gdx.graphics.getHeight()/3, Gdx.graphics.getHeight()/3, 0, 0, 0, 0);
		Texture jKnob = resizedTexture(knobHandle, Gdx.graphics.getHeight()/8, Gdx.graphics.getHeight()/8, 0, 0, 0, 0);

		Touchpad.TouchpadStyle joystickStyle = returnTouchpadStyle(jBackground, jKnob);
		joystick = new Joystick(0, joystickStyle);
		joystick.setBounds(Gdx.graphics.getWidth()/20, Gdx.graphics.getHeight()/10 //origin is bottom left for stage
				, jBackground.getWidth(), jBackground.getWidth()); //joystick is set to screen viewport, so treat as if it's the whole screen not the world

		//create shooter UI

		shooterUI = new ShooterUI(Gdx.graphics.getHeight()/10);
		shooterUI.configureShooterUI(Gdx.graphics.getWidth()*5/6, Gdx.graphics.getHeight()/10
				, Gdx.graphics.getHeight()/10, Gdx.graphics.getHeight()/10);

		//create state and add the touchpad and bullet buttons as actors
		stage = new Stage(new ScreenViewport());

		stage.addActor(joystick);
		stage.addActor(shooterUI.topButton); stage.addActor(shooterUI.bottomButton);
		stage.addActor(shooterUI.leftButton); stage.addActor(shooterUI.rightButton);

		Gdx.input.setInputProcessor(stage);

		//create player1
		playerSprite = new Sprite(new Texture("whitecircle.png"));
		player1 = new Player(playerSprite, HEIGHT/10, HEIGHT/10, 0, 0, world);

		//create building blocks for all platforms
		Texture img = new Texture("block.png");
		block = new Sprite(img); //50x50 pixels
		block2 = new Sprite(img);
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

		//create left wall
		BodyDef bodyDef2 = new BodyDef();
		bodyDef2.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef2 = new FixtureDef();
		EdgeShape wall1 = new EdgeShape();
		wall1.set(-WIDTH/2, -HEIGHT/2, -WIDTH/2, HEIGHT/2);
		fixtureDef2.shape = wall1;

		body2 = world.createBody(bodyDef2);
		body2.createFixture(fixtureDef2);

		wall1.dispose();

		//create right wall
		BodyDef bodyDef3 = new BodyDef();
		bodyDef3.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef3 = new FixtureDef();
		EdgeShape wall2 = new EdgeShape();
		wall2.set(WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2);
		fixtureDef3.shape = wall2;

		body3 = world.createBody(bodyDef3);
		body3.createFixture(fixtureDef3);

		wall2.dispose();

		//create ceiling
		BodyDef bodyDef4 = new BodyDef();
		bodyDef4.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef4 = new FixtureDef();
		EdgeShape ceiling = new EdgeShape();
		ceiling.set(-WIDTH/2, HEIGHT/2, WIDTH/2, HEIGHT/2);
		fixtureDef4.shape = ceiling;

		body4 = world.createBody(bodyDef4);
		body4.createFixture(fixtureDef4);

		ceiling.dispose();

		//create platform physics box
		heightPlatform = HEIGHT/10;

		BodyDef bodyDef5 = new BodyDef();
		bodyDef5.type = BodyDef.BodyType.StaticBody;
		bodyDef5.position.set(0, -HEIGHT/4+heightPlatform/2);

		FixtureDef fixtureDef5 = new FixtureDef();
		PolygonShape platform = new PolygonShape();

		platform.setAsBox(WIDTH/8, heightPlatform/2);
		fixtureDef5.shape = platform;

		body5 = world.createBody(bodyDef5);
		body5.createFixture(fixtureDef5);
		body5.setUserData("platform"); //for the world listener

		platform.dispose();

		//create platform block
		block2.setSize(WIDTH/4, heightPlatform);
		block2.setPosition(-WIDTH/8, -HEIGHT/4);

		//world contact listener, of utmost importance
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				// Check to see if the collision is between the second sprite and the bottom of the screen
				// If so apply a random amount of upward force to both objects... just because

			}

			@Override
			public void endContact(Contact contact) {
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();
				float platformY = 0;
				float playerY = 0;
				if(fixtureA.getBody().getUserData() == "platform" && fixtureB.getBody().getUserData() == "player" //collision between player and platform
						|| fixtureA.getBody().getUserData() == "player" && fixtureB.getBody().getUserData() == "platform") {
					if (fixtureA.getBody().getUserData() == "platform") {
						platformY = fixtureA.getBody().getPosition().y;
						playerY = fixtureB.getBody().getPosition().y;
					} else if (fixtureA.getBody().getUserData() == "player") {
						platformY = fixtureA.getBody().getPosition().y;
						playerY = fixtureB.getBody().getPosition().y;
					}
					if (playerY < (platformY + 100 * player1.getPlayerSprite().getHeight() / 101)) { // the player is below
						contact.setEnabled(false);
					}
				}
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});


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

		//conditions for player to move
		player1.move(joystick, body4, HEIGHT/3, 5*HEIGHT/12, WIDTH/6);

		//change position of player based on body
		player1.updatePosition();

		//shoot bullets in proper directions
		player1.clickToShoot(shooterUI, HEIGHT, HEIGHT);

		//change position of bullets
		player1.updateBulletPositions();

		//execute batch
		batch.begin();
		block.draw(batch); //ground
		block2.draw(batch); //platform
		player1.getPlayerSprite().draw(batch); //player sprite
		player1.drawAllBullets(batch); //draw all bullets
		batch.end();

		//move stage for joystick
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		//debug physics boxes/bodies
		debugRenderer.render(world, debugMatrix);

		//update shooterUI booleans so doesn't continuously shooting
		shooterUI.resetBooleans();

	}

	//return resized texture
	public Texture resizedTexture(FileHandle handle,
								  int width, int height,
								  int srcx, int srcy,
								  int dstx, int dsty) {

		Pixmap pix = new Pixmap(handle);
		Pixmap pix2 = new Pixmap(width, height, pix.getFormat());
		pix2.drawPixmap(pix, srcx, srcy, pix.getWidth(), pix.getHeight(), dstx, dsty, pix2.getWidth(), pix2.getHeight());

		Texture texture = new Texture(pix2);
		pix.dispose();
		pix2.dispose();

		return texture;
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



}
