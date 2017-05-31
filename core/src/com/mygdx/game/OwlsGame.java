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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.mygdx.game.Joystick.returnTouchpadStyle;

public class OwlsGame extends ApplicationAdapter {

	private Socket socket;
	private float timer = 0f;
	private float updateTime = 0.100f;

//	private float timerL = 0;
//	private float updateTimeL = 2f;
//	private float startTime = 0;
//	private float latency = 0;

	private SpriteBatch batch;
	private World world;
	private Stage stage;

	private Platform ground, platform1, platform2, platform3, platform4, platform5;
	private Wall wallLeft, wallRight, ceiling; //this is necessary

	private Player player1;
	private HashMap<String, Player> oppPlayers;
	private HashMap<String, Bullet> oppBullets;
	private float timerB = 0f;
	private float updateTimeB = 0.100f;

	private Sprite playerSprite, playerSprite2, oppPlayerSprite, bulletSprite;
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;
	private Joystick joystick;
	private static final float WIDTH = 64;
	private static final float HEIGHT = 36;
	private static final float GRAVITY = -12.0f;
	private float sidewaysVelocity = WIDTH/6;
	private float upwardsVelocity = HEIGHT/2;
	private float downwardsVelocity = HEIGHT*5/12;
	private float bulletSideVel = WIDTH/2;
	private float bulletUpDownVel = HEIGHT;
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
				, Gdx.graphics.getHeight()/8, Gdx.graphics.getHeight()/8);

		//create state and add the touchpad and bullet buttons as actors
		stage = new Stage(new ScreenViewport());

		stage.addActor(joystick);
		stage.addActor(shooterUI.topButton); stage.addActor(shooterUI.bottomButton);
		stage.addActor(shooterUI.leftButton); stage.addActor(shooterUI.rightButton);

		Gdx.input.setInputProcessor(stage);

		//create your player
		playerSprite = new Sprite(new Texture("whitecircle.png"));
		playerSprite2 = new Sprite(new Texture("whitecircle.png"));
		oppPlayerSprite = new Sprite(new Texture("whitecircle.png"));

		bulletSprite = new Sprite(new Texture("bullet.png"));

		player1 = new Player(playerSprite, 3*HEIGHT/40, 3*HEIGHT/40, 0, -HEIGHT/5, world);
		oppPlayers = new HashMap<String, Player>();
		oppBullets = new HashMap<String, Bullet>();

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

		//server stuff, should be at end of create
		connectSocket();
		configSocketEvents();

	}

//	//update latency
//	public void updateLatency() {
//		startTime = System.currentTimeMillis();
//		socket.emit("ping");
//	}

	//server stuff
	public void connectSocket() { //connect to socket (client side)
		try {
			socket = IO.socket("http://192.168.3.226:8080");
			socket.connect();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void configSocketEvents() { //client side server stuff, as important as (and similar to) world listener

		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected");
			}
		}).on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try{
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "My id: "+ id);
				} catch(JSONException e) {
					Gdx.app.log("SocketIO", "Problem retrieving JSON");
				}
			}
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) { //i artificially limit to one new player aka 1v1 me irl
				JSONObject data = (JSONObject) args[0];
				try{
					String id = data.getString("id");
					oppPlayers.put(id, new Player(oppPlayerSprite, 3*HEIGHT/40, 3*HEIGHT/40, 0, 0, world));
					oppPlayers.get(id).getPlayerBody().setGravityScale(0f);
					Gdx.app.log("SocketIO", "New Player Connected: "+ id);
				} catch(JSONException e) {
					Gdx.app.log("SocketIO", "Problem retrieving JSON");
				}
			}
		}).on("playerDisconnected", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try{
					String id = data.getString("id");
					removeBodySafely(oppPlayers.get(id).getPlayerBody());
					oppPlayers.remove(id);
				} catch(JSONException e) {
					Gdx.app.log("SocketIO", "Error getting disconnected");
				}
			}
		}).on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONArray objects = (JSONArray) args[0];
				try{
					for(int i = 0; i<objects.length(); i++) {
						Player existingPlayer = new Player(playerSprite2, 3*HEIGHT/40, 3*HEIGHT/40, 0, 0, world);
						float posX = ((Double)objects.getJSONObject(i).getDouble("x")).floatValue();
						float posY = ((Double)objects.getJSONObject(i).getDouble("y")).floatValue();
						existingPlayer.getPlayerSprite().setPosition(posX, posY);
						oppPlayers.put(objects.getJSONObject(i).getString("id"), existingPlayer);
					}
				} catch(JSONException e) {
					Gdx.app.log("SocketIO", "Error getting player IDs");
				}
			}
		}).on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try{
					String id = data.getString("id");
					double vx = data.getDouble("vx");
					double vy = data.getDouble("vy");
					double x = data.getDouble("x");
					double y = data.getDouble("y");
					if(!oppPlayers.isEmpty()) {
						oppPlayers.get(id).getPlayerBody().setTransform((float)x, (float)y, 0);
						oppPlayers.get(id).getPlayerBody().setLinearVelocity((float)vx, (float)vy);
					}
				} catch(JSONException e) {
					Gdx.app.log("SocketIO", "Error updating player position");
				}
			}
		})
//				.on("pong", new Emitter.Listener() {
//			@Override
//			public void call(Object... args) {
//				latency = System.currentTimeMillis() - startTime;
//				Gdx.app.log("latency", latency+", "+ startTime);
//			}
				.on("playerShot", new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						JSONObject data = (JSONObject) args[0];
						try {
							String id = data.getString("id");
							double vx = data.getDouble("vx");
							double vy = data.getDouble("vy");
							double x = data.getDouble("x");
							double y = data.getDouble("y");
							int shootOption = data.getInt("shootOption");
							Bullet newBullet = new Bullet(shootOption, oppPlayers.get(id).getPlayerSprite(), world);
							newBullet.setBulletSprite(bulletSprite, oppPlayers.get(id).getPlayerSprite());

							newBullet.setVx((float)vx); newBullet.setVy((float)vy); newBullet.setX((float)x); newBullet.setY((float)y);

							for(HashMap.Entry<String, Player> entry : oppPlayers.entrySet()) {
								entry.getValue().bulletList.add(newBullet);
							}

						} catch (JSONException e) {
							Gdx.app.log("SocketIO", "Error updating bullet position");
						}
					}
				});
	}

	public void updatePositionOnOppScreen(float dt) {

		JSONObject data = new JSONObject();
		try {
			data.put("vx", player1.getPlayerBody().getLinearVelocity().x);
			data.put("vy", player1.getPlayerBody().getLinearVelocity().y);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		timer+=dt;
		if (timer > updateTime && player1.hasMoved && player1!=null)  {
			try {
				data.put("x", player1.getPlayerBody().getPosition().x);
				data.put("y", player1.getPlayerBody().getPosition().y);
				socket.emit("playerMoved", data);
			} catch(JSONException e) {
				Gdx.app.log("Socket.io", "Messed up JSON update");
			}
			timer = 0;
			player1.hasMoved = false;
		}

	}

	public void updateBulletPosOnOppScreen(float dt) {

		JSONObject data = new JSONObject();
		timerB+=dt;
		if(timerB > updateTimeB && player1.hasShot) {
			try{
				Bullet bullet = player1.bulletList.get(player1.bulletList.size()-1);
				data.put("vx", bullet.getVx());
				data.put("vy", bullet.getVy());
				data.put("x", bullet.getX());
				data.put("y", bullet.getY());
				data.put("shootOption", bullet.getShootOption());
				socket.emit("playerShot", data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			player1.hasShot = false;
			timerB = 0;
		}

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

		//timer for latency
//		timerL+=Gdx.graphics.getDeltaTime();
//		if(timerL>updateTimeL) {
//			updateLatency();
//		}

		//move player
		player1.move(joystick, upwardsVelocity, downwardsVelocity, sidewaysVelocity);

		//update position of player sprite
		player1.updatePlayerPos();

		//update your player on opponent's screen
		updatePositionOnOppScreen(Gdx.graphics.getDeltaTime());

		//shoot bullets in proper directions + add delay
		player1.clickToShoot(shooterUI, bulletSideVel, bulletUpDownVel, 300f);

		//change position of bullets
		player1.updateBulletPositions();

		//update bullets on opposing screen
		updateBulletPosOnOppScreen(Gdx.graphics.getDeltaTime());

		//execute batch
		batch.begin();

		ground.getPlatformSprite().draw(batch); //ground
		platform1.getPlatformSprite().draw(batch); //platform
		platform2.getPlatformSprite().draw(batch); //platform2
		platform3.getPlatformSprite().draw(batch); //platform3
		platform4.getPlatformSprite().draw(batch); //platform4
		platform5.getPlatformSprite().draw(batch); //platform5

		//draw player
		player1.getPlayerSprite().draw(batch);

		//draw all bullets from player1
		player1.drawAllBullets(batch);


		//update opposing players from server hashmap
		for(HashMap.Entry<String, Player> entry : oppPlayers.entrySet()) {

			entry.getValue().getPlayerSprite().draw(batch); //draw player sprite
			entry.getValue().updatePlayerPos(); //update position

			entry.getValue().drawAllBullets(batch); //draw bullet sprites
			entry.getValue().updateBulletPositions(); //update bullet positions

		}

		batch.end();

		//move stage for joystick
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		//destroy all toBeRemoved bullets for your player
		for(int i = 0; i<player1.bulletList.size(); i++) {
			if(player1.bulletList.get(i).toBeRemoved) {
				removeBodySafely(player1.bulletList.get(i).bulletBody);
				player1.bulletList.remove(i);
			}
		}

		//destroy all toBeRemoved bullets for your player
		for(HashMap.Entry<String, Player> entry : oppPlayers.entrySet()) {
			for(int i = 0; i<entry.getValue().bulletList.size(); i++) {
				if(entry.getValue().bulletList.get(i).toBeRemoved) {
					removeBodySafely(entry.getValue().bulletList.get(i).bulletBody);
					entry.getValue().bulletList.remove(i);
				}
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
