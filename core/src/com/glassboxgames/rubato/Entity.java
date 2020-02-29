package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.util.*;

/**
 * Abstract class representing a textured entity.
 */
public abstract class Entity {
  /** Default animation speed */
  protected static float DEFAULT_ANIMATION_SPEED = 0.25f;
  
  /** The body definition for this entity */
  protected BodyDef bodyDef;
  /** The fixture definition for this entity */
  protected FixtureDef fixtureDef;
  /** The body for this entity */
  protected Body body;
  /** The fixture for this entity */
  protected Fixture fixture;
  
  /** CURRENT image for this object. May change over time. */
  protected FilmStrip animator;
  /** Current animation frame */
  protected float animFrame;
  /** Speed of animation for this entity */
  protected float animSpeed;
  /** Total frames for current animation */
  protected int totalFrames;

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
    bodyDef = new BodyDef();
    bodyDef.position.set(x, y).scl(1 / Constants.PPM);
    bodyDef.active = true;
    bodyDef.awake = true;
    bodyDef.allowSleep = true;
    bodyDef.gravityScale = 1;
    bodyDef.fixedRotation = true;
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    fixtureDef = new FixtureDef();

    animator = null;
    animFrame = 0;
    animSpeed = 0;
    totalFrames = 0;
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
   * Adds this entity as a physics object in the given world.
   */
  public boolean activatePhysics(World world) {
    body = world.createBody(bodyDef);
    body.setUserData(this);
    if (body != null) {
      fixture = body.createFixture(fixtureDef);
      return true;
    }
    return false;
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
    setTexture(texture, 1, 1, 1, DEFAULT_ANIMATION_SPEED);
  }

  /**
   * Sets the texture with the given filmstrip parameters.
   * @param texture the texture to set
   * @param rows number of rows in filmstrip
   * @param cols number of columns in filmstrip
   * @param size number of frames in filmstrip
   */
  public void setTexture(Texture texture, int rows, int cols, int size, float speed) {
    FilmStrip newAnimator = new FilmStrip(texture, rows, cols, size);
    if (animator == null || !texture.equals(animator.getTexture())) {
      animFrame = 0;
      animSpeed = speed;
      totalFrames = size;
      animator = newAnimator;
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

  /**
   * Draws this entity to the given canvas.
   */
  public void draw(GameCanvas canvas) {
    float w = animator.getWidth();
    float h = animator.getHeight();
    canvas.draw(animator, Color.WHITE,
                w / 2, h / 2,
                getPosition().x * Constants.PPM, getPosition().y * Constants.PPM,
                w, h);
  }

  /**
   * Draws the physics outline of this entity to the given canvas.
   */
  public void drawPhysics(GameCanvas canvas) {
    Shape shape = fixture.getShape();
    switch (shape.getType()) {
    case Polygon:
      canvas.drawPhysics((PolygonShape)shape, Color.RED,
                         getPosition().x, getPosition().y, 0,
                         Constants.PPM, Constants.PPM);
      break;
    case Circle:
      canvas.drawPhysics((CircleShape)shape, Color.RED,
                         getPosition().x, getPosition().y,
                         Constants.PPM, Constants.PPM);
      break;
    }
  }
}
