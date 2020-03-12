package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

/**
 * Abstract class representing a textured entity.
 */
public abstract class Entity {
  /** Default animation speed */
  protected static float DEFAULT_ANIMATION_SPEED = 0.25f;
  
  /** The body definition for this entity */
  protected BodyDef bodyDef;
  /** The body for this entity */
  protected Body body;
  /** Direction the entity is facing (1 for right, -1 for left) */
  protected int dir;

  /** Container for the entity state */
  protected class State {
    /** Fixture for this state */
    public Fixture fixture;
    /** Number of frames this state has been active */
    public int activeTime;
    /** Filmstrip for the animation */
    public FilmStrip filmStrip;
    /** Speed of animation */
    public float speed;
    /** Current animation frame */
    public float frame;
    /** Length of animation in frames */
    public int length;
    /** Whether to loop the animation */
    public boolean loop;
    /** Whether the animation is finished (always false if looping) */
    public boolean done;

    /**
     * Update this entity state.
     * @param delta time since last animation frame
     */
    public void update(float delta) {
      activeTime++;
      frame += speed;
      if (frame >= length) {
        if (loop) {
          frame -= length;
        } else {
          // stay on last frame? TODO change
          done = true;
          frame = length - 1;
        }
      }
      filmStrip.setFrame((int)frame);
    }
  }
  
  /** Current entity state, represented as an integer */
  protected int stateIndex;
  /** Number of frames since last state change */
  protected int stateDuration;
  /** Current animation frame */
  protected float frame;
  /** Array of entity states, indexed by animation state */
  protected Array<State> states;

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
   * @param numStates number of states for this entity
   */
  public Entity(float x, float y, int numStates) {
    dir = 1;
    bodyDef = new BodyDef();
    bodyDef.position.set(x, y);
    bodyDef.active = true;
    bodyDef.awake = true;
    bodyDef.allowSleep = true;
    bodyDef.gravityScale = 1;
    bodyDef.fixedRotation = true;
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    states = new Array<State>();
    for (int i = 0; i < numStates; i++) {
      states.add(null);
    }
  }

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
    getState().update(delta);
    advanceState();
  }

  /**
   * Returns the current entity state.
   */
  public State getState() {
    return states.get(stateIndex);
  }

  /**
   * Sets the entity state to the given entity state index.
   * @param i entity state index
   */
  public void setState(int i) {
    stateIndex = i;
    State state = getState();
    state.activeTime = 0;
    state.frame = 0;
    state.done = false;
  }
  
  /**
   * Initializes the entity state at the given index with the given filmstrip texture.
   * @param texture the texture to set (as a filmstrip)
   */
  public void initState(int i, Texture texture) {
    initState(i, texture, 1, 1, 1, DEFAULT_ANIMATION_SPEED, false);
  }

  /**
   * Initializes the entity state at the given index with the given filmstrip parameters.
   * @param i the index of the state
   * @param texture the texture to set
   * @param rows number of rows in filmstrip
   * @param cols number of columns in filmstrip
   * @param size number of frames in filmstrip
   * @param speed frame speed multiplier
   * @param loop whether to loop the filmstrip
   */
  public void initState(int i, Texture texture, int rows, int cols, int size, float speed, boolean loop) {
    State state = new State();
    state.frame = 0;
    state.speed = speed;
    state.length = size;
    state.filmStrip = new FilmStrip(texture, rows, cols, size);
    state.loop = loop;
    states.set(i, state);
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
    FilmStrip filmStrip = getState().filmStrip;
    float w = filmStrip.getWidth() / Constants.PPM;
    float h = filmStrip.getHeight() / Constants.PPM;
    Vector2 pos = getPosition();
    canvas.draw(filmStrip, Color.WHITE,
                dir * w / 2, h / 2,
                pos.x, pos.y,
                dir * w, h);
  }

  /**
   * Draws this entity's physics outline (hurtboxes) to the given canvas.
   */
  public abstract void drawPhysics(GameCanvas canvas);
}
