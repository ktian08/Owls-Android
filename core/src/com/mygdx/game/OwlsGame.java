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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.mygdx.game.Joystick.returnTouchpadStyle;

public class OwlsGame extends ApplicationAdapter {

	private ServerHandler serverHandler;

	private SpriteBatch batch;
	private World world;
	private Stage stage;

	private Platform ground, platform1, platform2, platform3, platform4, platform5;
	private Wall wallLeft, wallRight, ceiling; //this is necessary
	private Player player1;

	private Sprite playerSprite;
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;
	private Joystick joystick;
	private static final float WIDTH = 64;
	private static final float HEIGHT = 36;
	private static final float GRAVITY = -12.0f;
	private ShooterUI shooterUI;

	@Override
	public void create () {

//		//create server handler
		serverHandler = new ServerHandler();
		serverHandler.connectSocket();

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
				, Gdx.graphics.getHeight()/8, Gdx.graphics.getHeight()/8);

		//create state and add the touchpad and bullet buttons as actors
		stage = new Stage(new ScreenViewport());

		stage.addActor(joystick);
		stage.addActor(shooterUI.topButton); stage.addActor(shooterUI.bottomButton);
		stage.addActor(shooterUI.leftButton); stage.addActor(shooterUI.rightButton);

		Gdx.input.setInputProcessor(stage);

		//create player1
		playerSprite = new Sprite(new Texture("whitecircle.png"));
		player1 = new Player(playerSprite, 3*HEIGHT/40, 3*HEIGHT/40, 0, -HEIGHT/5, world);

		//create platforms
		ground = new Platform(WIDTH, HEIGHT/10, -WIDTH/2, -HEIGHT/2, world);
		platform1 = new Platform(WIDTH/5, HEIGHT/30, -WIDTH/5, -31*HEIGHT/120, world);
		platform2 = new Platform(WIDTH/5, HEIGHT/30, -WIDTH/2, -8*HEIGHT/120, world);
		platform3 = new Platform(WIDTH/5, HEIGHT/30, -WIDTH/5, 15*HEIGHT/120, world);
		platform4 = new Platform(WIDTH/5, HEIGHT/30, WIDTH/8, 32*HEIGHT/120, world);
		platform5 = new Platform(WIDTH/5, HEIGHT/30, WIDTH/4, -8*HEIGHT/120, world);

		//create walls
		wallLeft = new Wall(-WIDTH/2, -HEIGHT/2, -WIDTH/2, HEIGHT/2, world);
		wallRight = new Wall(WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2, world);
		ceiling = new Wall(-WIDTH/2, HEIGHT/2, WIDTH/2, HEIGHT/2, world);

		//world contact listener, of utmost importance
		setOnContactEffects(world);

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
		player1.move(joystick, HEIGHT/2, 5*HEIGHT/12, WIDTH/6);

		//change position of player based on body
		player1.updatePosition();

		//shoot bullets in proper directions + add delay
		player1.clickToShoot(shooterUI, WIDTH/2, HEIGHT, 300f);

		//change position of bullets
		player1.updateBulletPositions();

		//execute batch
		batch.begin();

		ground.getPlatformSprite().draw(batch); //ground
		platform1.getPlatformSprite().draw(batch); //platform
		platform2.getPlatformSprite().draw(batch); //platform2
		platform3.getPlatformSprite().draw(batch); //platform3
		platform4.getPlatformSprite().draw(batch); //platform4
		platform5.getPlatformSprite().draw(batch); //platform5

		player1.getPlayerSprite().draw(batch); //player sprite
		player1.drawAllBullets(batch); //draw all bullets

		batch.end();

		//move stage for joystick
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		//destroy all toBeRemoved bullets
		for(int i = 0; i<player1.bulletList.size(); i++) {
			if(player1.bulletList.get(i).toBeRemoved) {
				removeBodySafely(player1.bulletList.get(i).bulletBody);
				player1.bulletList.remove(i);
			}
		}

		//debug physics boxes/bodies
		debugRenderer.render(world, debugMatrix);

		//update shooterUI booleans so doesn't continuously shooting
		shooterUI.resetBooleans();

	}

	//set the world contact listener
	public void setOnContactEffects(World world) {
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
			}

			@Override
			public void endContact(Contact contact) {
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();
				float platformY = 0; float playerY = 0; //for first condition

				if((fixtureA.getUserData() instanceof Platform && fixtureB.getUserData() instanceof Player) //collision between player and platform
						|| (fixtureA.getUserData() instanceof Player && fixtureB.getUserData() instanceof Platform)) {
					if (fixtureA.getUserData() instanceof Platform) {
						platformY = fixtureA.getBody().getPosition().y;
						playerY = fixtureB.getBody().getPosition().y;
					} else if (fixtureA.getUserData() instanceof Player) {
						platformY = fixtureA.getBody().getPosition().y;
						playerY = fixtureB.getBody().getPosition().y;
					}
					if (playerY < (platformY + 4 * player1.getPlayerSprite().getHeight() / 6)) { // the player is below
						contact.setEnabled(false);
					}
				}
				if((fixtureA.getUserData() instanceof Bullet && !(fixtureB.getUserData() instanceof Bullet))
						|| (fixtureB.getUserData() instanceof Bullet && !(fixtureA.getUserData() instanceof Bullet))) { //collision b/w bullet and something not a bullet
					if(fixtureA.getUserData() instanceof Bullet) {
						((Bullet) fixtureA.getUserData()).toBeRemoved = true;
					} else if(fixtureB.getUserData() instanceof Bullet) {
						((Bullet) fixtureB.getUserData()).toBeRemoved = true;
					}
				}
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

	}

	//remove body method
	public void removeBodySafely(Body body) {
		final Array<JointEdge> list = body.getJointList();
		while (list.size > 0) {
			world.destroyJoint(list.get(0).joint);
		}
		// actual remove
		world.destroyBody(body);
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
