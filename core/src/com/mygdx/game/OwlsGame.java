package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class OwlsGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private World world;
	private Body body;
	private OrthographicCamera camera;
	private BitmapFont font;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;

	private static final float WORLD_WIDTH = 80;
	private static final float WORLD_HEIGHT = 45;

	private static final float PIXELS_TO_METERS = 50f;
	
	@Override
	public void create () {
		//initialize the batch
		batch = new SpriteBatch();
		//create world with -10 gravity in vert direction
		world = new World(new Vector2(0.0f, -10.0f), true);
		//create ground platform
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(0,0);

		FixtureDef fixtureDef = new FixtureDef();
		EdgeShape edge = new EdgeShape();
		float w = Gdx.graphics.getWidth()/PIXELS_TO_METERS;
		float h = Gdx.graphics.getHeight()/PIXELS_TO_METERS-50/PIXELS_TO_METERS;
		edge.set(-w/2, -h/2, w/2, -h/2);
		fixtureDef.shape = edge;

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		edge.dispose();
		//set debug renderer
		debugRenderer = new Box2DDebugRenderer();
		//pick font
		font = new BitmapFont();
		font.setColor(Color.BLACK);
		//set camera
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void render () {
		//update camera and world
		camera.update();
		world.step(1.0f/45f, 6, 2); //update at 45 times per second (45 Hz)
		//clear the background and allow stuff to print on screen
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//change batch to perspective of camera
		batch.setProjectionMatrix(camera.combined);
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS,
				PIXELS_TO_METERS, 0);
		//execute
		batch.begin();
		batch.end();

		debugRenderer.render(world, debugMatrix);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		world.dispose();
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
