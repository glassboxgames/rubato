package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;
import com.glassboxgames.util.*;

/**
 * Abstract class representing a textured entity.
 */
public abstract class Entity {
  /** The body definition for this entity */
  protected BodyDef bodyDef;
  /** The body for this entity */
  protected Body body;
  /** Fixtures for this entity */
  protected Array<Fixture> fixtures;
  /** Direction the entity is facing (1 for right, -1 for left) */
  protected int dir;
  /** Current entity state, represented as an integer */
  protected int stateIndex;
  /** Number of frames spent in current state */
  protected int count;

  /** Temp vector for calculations */
  protected Vector2 temp = new Vector2();
  /** Cache for position vector */
  private Vector2 posCache = new Vector2();
  /** Cache for velocity vector */
  private Vector2 velCache = new Vector2();

  /**
   * Instantiates a new entity with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Entity(float x, float y) {
    dir = 1;
    bodyDef = new BodyDef();
    bodyDef.position.set(x, y);
    bodyDef.active = true;
    bodyDef.awake = true;
    bodyDef.allowSleep = true;
    bodyDef.gravityScale = 1;
    bodyDef.fixedRotation = true;
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    fixtures = new Array<Fixture>();
  }

  /**
   * Returns an array of states for this entity.
   */
  public abstract Array<State> getStates();

  /**
   * Returns the position vector. Always returns a copy the same vector.
   */
  public Vector2 getPosition() {
    return posCache.set(body == null ?
                        bodyDef.position : body.getPosition());
  }

  /**
   * Returns the velocity vector. Always returns a copy in the same vector.
   */
  public Vector2 getVelocity() {
    return velCache.set(body == null ?
                        bodyDef.linearVelocity : body.getLinearVelocity());
  }

  /**
   * Returns direction this entity is facing (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Returns the Box2D body of this entity.
   */
  public Body getBody() {
    return body;
  }
  
  /**
   * Sets the direction of this entity to 1 (right).
   */
  public void faceRight() {
    dir = 1;
  }

  /**
   * Sets the direction of this entity to -1 (left).
   */
  public void faceLeft() {
    dir = -1;
  }

  /**
   * Adds this entity as a physics object in the given world.
   */
  public boolean activatePhysics(World world) {
    body = world.createBody(bodyDef);
    return body != null;
  }

  /**
   * Removes this entity as a physics object in the given world.
   */
  public void deactivatePhysics(World world) {
    if (body != null) {
      // if we need to save the body info, we can do it here if we want
      world.destroyBody(body);
      body = null;
      bodyDef.active = false;
    }
  }

  /**
   * Updates this entity's state.
   * @param delta time since the last update
   */
  public void update(float delta) {
    for (Fixture fixture : fixtures) {
      body.destroyFixture(fixture);
    }
    fixtures.clear();
    count++;
    advanceState();
    State state = getState();
    for (FixtureDef def : state.getHurtboxDefs(count)) {
      Fixture fixture = body.createFixture(def);
      fixture.setUserData(this);
      fixtures.add(fixture);
    }
  }

  /**
   * Returns the current entity state.
   */
  public State getState() {
    return getStates().get(stateIndex);
  }

  /**
   * Sets the entity state to the given entity state index.
   * @param i entity state index
   */
  public void setState(int i) {
    leaveState();
    stateIndex = i;
  }

  /**
   * Executes any final state changes before leaving current state.
   * Called before new state is set.
   */
  public void leaveState() {
    count = 0;
  }
  
  /**
   * Transitions entity states based on current entity state.
   * Inputs should be set as flags in the entity and read in the implementing body.
   */
  public void advanceState() {}

  /**
   * Draws this entity to the given canvas.
   */
  public void draw(GameCanvas canvas) {
    Texture texture = getState().getTexture(count);
    float w = texture.getWidth() / Constants.PPM;
    float h = texture.getHeight() / Constants.PPM;
    Vector2 pos = getPosition();
    canvas.draw(texture, Color.WHITE,
                dir * w / 2, h / 2,
                pos.x, pos.y,
                dir * w, h);
  }

  /**
   * Draws this entity's physics outline (hurtboxes) to the given canvas.
   */
  public void drawPhysics(GameCanvas canvas) {
    Vector2 pos = getPosition();
    for (Fixture fixture : fixtures) {
      Shape shape = fixture.getShape();
      if (shape instanceof CircleShape) {
        Vector2 spos = ((CircleShape)shape).getPosition();
        canvas.drawPhysics((CircleShape)shape, Color.RED, pos.x + spos.x, pos.y + spos.y);
      } else if (shape instanceof PolygonShape) {
        canvas.drawPhysics((PolygonShape)shape, Color.RED, pos.x, pos.y, 0f);
      }
    }
  }
}
