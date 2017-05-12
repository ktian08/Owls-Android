package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class OwlsGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private World world;
	private Body body;
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;
	private ShapeRenderer shapeRenderer;

	private float width;
	private float height;
	private float heightGround;
	
	@Override
	public void create () {
		//initialize the batch
		batch = new SpriteBatch();
		//create world with -10 gravity in vert direction
		world = new World(new Vector2(0.0f, -10.0f), true);
		//initialize width and height (of screen)
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		//create ground platform physics box
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		heightGround = height/10;
		bodyDef.position.set(0, -height/2+heightGround/2);

		FixtureDef fixtureDef = new FixtureDef();
		PolygonShape ground = new PolygonShape();
		ground.setAsBox(width, heightGround/2);
		fixtureDef.shape = ground;

		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		ground.dispose(); //dispose of box shape, body already created
		//create shape renderer
		shapeRenderer = new ShapeRenderer();
		//set debug renderer
		debugRenderer = new Box2DDebugRenderer();
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
		debugMatrix = batch.getProjectionMatrix().cpy();
		//execute
		batch.begin();
		batch.end();
		//debug physics boxes/bodies
		debugRenderer.render(world, debugMatrix);
		//draw ground platform rectangle
		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.rect(-width/2, -height/2, width, heightGround);
		shapeRenderer.end();
	}
	
	@Override
	public void dispose () {
		//prevent memory leaks!!!
		batch.dispose();
		world.dispose();
		shapeRenderer.dispose();
		debugRenderer.dispose();
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
