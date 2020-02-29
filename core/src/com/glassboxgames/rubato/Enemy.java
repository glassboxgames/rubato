package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.util.*;

/**
 * Class representing a simple path-following enemy in Rubato.
 */
public class Enemy extends Entity {
  /** Maximum health */
  protected static final float MAX_HEALTH = 10;
  /** Maximum speed */
  protected static final float MAX_SPEED = 3;
  /** Movement range */
  protected static final float MOVE_RANGE = 200;
  /** Enemy name */
  protected static final String ENEMY_NAME = "Enemy";

  /** Enemy dimensions */
  protected Vector2 dim;
  /** Current health */
  protected float health;
  /** Movement limits */
  protected float minX, maxX;
  /** Current direction */
  protected int dir;

  /** Represents how much the enemy has been slowed */
  protected float timeslowfactor;
  /** Represent the previous position of the enemy */
  protected Vector2 prevPosition;
  /** Represent the previous velocity of the enemy */
  protected Vector2 prevVelocity;
  /** Cache for prevPosition Calculation */
  protected Vector2 prevPosCache = new Vector2(0, 0);
  /** Cache for prevVelocity Calculation */
  protected Vector2 prevVelCache = new Vector2(0, 0);
  /** Cache for dimensions */
  protected Vector2 dimCache = new Vector2(0, 0);

  /**
   * Initializes an enemy with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   */
  public Enemy(float x, float y, float w, float h) {
    super(x, y, ENEMY_NAME);
    dim = new Vector2(w, h);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(dim.x / 2, dim.y / 2);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    fixtureDef.shape = shape;

    health = MAX_HEALTH;
    minX = x - MOVE_RANGE;
    maxX = x + MOVE_RANGE;
    dir = 1;

    timeslowfactor = 0.0f;
    prevPosition = getPosition();
    prevVelocity = getVelocity();
  }

  /**
   * Returns direction this enemy is facing (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Returns a copy of the dimension vector.
   */
  public Vector2 getDimensions() {
    return dimCache.set(dim);
  }

  /**
   * Damage this enemy by the given amount.
   * @param damage damage value
   */
  public void lowerHealth(float damage) {
    health = Math.max(0, health - damage);
  }

  /**
   * Returns whether this enemy is suspended (dead).
   */
  public boolean isSuspended() {
    return health == 0;
  }

  /**
   * Returns the time slow factor of the enemy. (1 is normal speed, 0 is frozen)
   */
  public float getTimeSlowFactor() {
    return timeslowfactor;
  }

  /**
   * Sets the time slow factor for the enemy.
   * @param tsf the time slow factor to set
   */
  public void setTimeSlowFactor(float tsf) {
    timeslowfactor = tsf;
  }
  
  @Override
  public void update(float delta) {
    if (getPosition().x >= maxX) {
      dir = -1;
    } else if (getPosition().x <= minX) {
      dir = 1;
    }
    float movement = MAX_SPEED * dir * health / MAX_HEALTH;
    temp.set(getPosition());
    temp.x += movement;
    body.setTransform(temp, 0);
    timeslow(delta);
  }

  public void timeslow(float delta) {
    prevPosCache.x = prevPosition.x*(1-timeslowfactor)+getPosition().x*timeslowfactor;
    prevPosCache.y = prevPosition.y*(1-timeslowfactor)+getPosition().y*timeslowfactor;
    prevVelCache.x = prevVelocity.x*(1-timeslowfactor)+getPosition().x*timeslowfactor;
    prevVelCache.y = prevVelocity.y*(1-timeslowfactor)+getPosition().y*timeslowfactor;

    prevPosition = prevPosCache;
    prevVelocity = prevVelCache;

    body.setTransform(prevPosCache, body.getAngle());
    body.setLinearVelocity(prevVelCache);
  }

  @Override
  public void draw(GameCanvas canvas) {
    float w = animator.getWidth();
    float h = animator.getHeight();
    canvas.draw(animator, Color.WHITE,
                dir * w / 2, h / 2,
                getPosition().x * Constants.PPM, getPosition().y * Constants.PPM,
                dir * w, h);
  }
}
