package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.glassboxgames.util.*;
/**
 * Abstract class representing a rectangular textured entity.
 */
public abstract class Entity {
  /** Default animation speed */
  private static float DEFAULT_ANIMATION_SPEED = 0.25f;

  /** Dimensions of the entity */
  public Vector2 dim;
  /** Temporary vector for calculations */
  private Vector2 temp = new Vector2();

  /** CURRENT image for this object. May change over time. */
  private FilmStrip animator;
  /** Current animation frame */
  private float animFrame;
  /** Speed of animation for this entity */
  private float animSpeed;
  /** Total frames for current animation */
  private int totalFrames;

  /** The body information of the current entity */
  protected BodyDef bodyInfo;
  /** The Fixture information of the current entity */
  protected FixtureDef fixtureInfo;


  /// Caching objects
  /** A cache value for when the user wants to access the body position */
  protected Vector2 positionCache = new Vector2();
  /** A cache value for when the user wants to access the linear velocity */
  protected Vector2 velocityCache = new Vector2();

  /**
   * Instantiates a new entity at position (0,0)
   */
  public Entity() {
    this(0.0f, 0.0f);
  }

  /**
   * Instantiates a new entity with the given parameters.
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  public Entity(float x, float y) {
    Vector2 pos = new Vector2(x, y);
    dim = new Vector2();
    animator = null;
    animFrame = 0;
    animSpeed = DEFAULT_ANIMATION_SPEED;
    totalFrames = 0;

    // Allocate body information
    bodyInfo = new BodyDef();
    bodyInfo.awake = true;
    bodyInfo.active = true;
    bodyInfo.allowSleep = true;
    bodyInfo.gravityScale = 1.0f;
    bodyInfo.position.set(pos);
    bodyInfo.type = BodyDef.BodyType.DynamicBody;
    bodyInfo.fixedRotation = false;
    bodyInfo.position.set(pos);
    bodyInfo.angle = 0;

    // Initialize default fixture
    fixtureInfo = new FixtureDef();
  }
    
  /**
   * Updates this entity's animation frame.
   * @param delta time since the last update
   */
  public void update(float delta) {
    animFrame += animSpeed;
    if (animFrame >= totalFrames) {
      animFrame -= totalFrames;
    }
    animator.setFrame((int)animFrame);
  }

  /**
   * Sets a static texture for this entity.
   * @param texture the texture to set
   */
  public void setTexture(Texture texture) {
    setTexture(texture, 1, 1, 1);
  }

  /**
   * Sets the texture with the given filmstrip parameters.
   * @param texture the texture to set
   * @param rows number of rows in filmstrip
   * @param cols number of columns in filmstrip
   * @param size number of frames in filmstrip
   */
  public void setTexture(Texture texture, int rows, int cols, int size) {
    FilmStrip newAnimator = new FilmStrip(texture, rows, cols, size);
    if (animator == null || !texture.equals(animator.getTexture())) {
      animFrame = 0;
      totalFrames = size;
      animator = newAnimator;
      dim.set(animator.getRegionWidth(), animator.getRegionHeight());
    }
  }

  /**
   * Returns the texture for this entity.
   */
  public Texture getTexture() {
    return animator == null ? null : animator.getTexture();
  }

  /**
   * Returns the current filmstrip for this entity.
   */
  public FilmStrip getFilmStrip() {
    return animator;
  }

  public Vector2 getPos() {
    return positionCache.set(bodyInfo.position);
  }
  public Vector2 getVel() {
    return velocityCache.set(bodyInfo.linearVelocity);
  }
  /**
   * Draws this entity to the given canvas.
   */
  public abstract void draw(GameCanvas canvas);
   //canvas.draw(getFilmStrip(), Color.WHITE, dim.x / 2, 0, pos.x + dim.x / 2, pos.y, dim.x, dim.y);

}
