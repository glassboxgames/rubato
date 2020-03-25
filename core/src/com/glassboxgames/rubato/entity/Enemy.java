package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Class representing a simple path-following enemy in Rubato.
 */
public class Enemy extends BoxEntity {
  /** Maximum health */
  protected static final float MAX_HEALTH = 10;
  /** Maximum speed */
  protected static final float MAX_SPEED = 3f;
  /** Movement range */
  protected static final float MOVE_RANGE = 2f;
  /** Friction */
  protected static final float FRICTION = 0f;

  /** Current health */
  protected float health;
  /** Movement limits */
  protected float minX, maxX;

  /** Represent the previous position of the enemy */
  protected Vector2 prevPosition;
  /** Represent the previous velocity of the enemy */
  protected Vector2 prevVelocity;
  /** Cache for prevPosition Calculation */
  protected Vector2 prevPosCache = new Vector2(0, 0);
  /** Cache for prevVelocity Calculation */
  protected Vector2 prevVelCache = new Vector2(0, 0);

  /**
   * Initializes an enemy with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   */
  public Enemy(float x, float y, float w, float h) {
    super(x, y, w, h);

    bodyDef.type = BodyDef.BodyType.KinematicBody;
    fixtureDef.friction = FRICTION;

    health = MAX_HEALTH;
    minX = x - MOVE_RANGE;
    maxX = x + MOVE_RANGE;

    prevPosition = getPosition();
    prevVelocity = getVelocity();
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

  @Override
  public void update(float delta) {
    super.update(delta);
    if (getPosition().x >= maxX) {
      faceLeft();
    } else if (getPosition().x <= minX) {
      faceRight();
    }
    body.setLinearVelocity(MAX_SPEED * getDirection(), 0);
    timeslow(delta);
  }

  public void timeslow(float delta) {
    float tsf = health / MAX_HEALTH;
    prevPosCache.x = prevPosition.x * (1 - tsf) + getPosition().x * tsf;
    prevPosCache.y = prevPosition.y * (1 - tsf) + getPosition().y * tsf;
    prevVelCache.x = prevVelocity.x * (1 - tsf) + getVelocity().x * tsf;
    prevVelCache.y = prevVelocity.y * (1 - tsf) + getVelocity().y * tsf;

    prevPosition = prevPosCache;
    prevVelocity = prevVelCache;

    body.setTransform(prevPosCache, body.getAngle());
    body.setLinearVelocity(prevVelCache);
  }
}
