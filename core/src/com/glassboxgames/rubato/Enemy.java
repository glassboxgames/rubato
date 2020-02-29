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
  private static float MAX_HEALTH = 10;
  /** Maximum speed */
  private static float MAX_SPEED = 3;
  /** Movement range */
  private static float MOVE_RANGE = 200;

  /** Current health */
  private float health;
  /** Movement limits */
  private float minX, maxX;
  /** Current direction */
  private int dir;

  /** Represents how much the enemy has been slowed */
  private float timeslowfactor;
  /** Represent the previous position of the enemy */
  private Vector2 prevPosition;
  /** Represent the previous velocity of the enemy */
  private Vector2 prevVelocity;
  /** Represent cache for prevPosition Calculation */
  private Vector2 prevPosCache = new Vector2(0,0);
  /** Represent cache for prevVelocity Calculation */
  private Vector2 prevVelCache = new Vector2(0,0);

  /**
   * Initializes an enemy with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   */
  public Enemy(float x, float y, float w, float h) {
    super(x, y);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(w / 2, h / 2);
    bodyDef.gravityScale = 0;
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

  /** Returns the time slow factor of the enemy. 1 being normal speed and 0 being frozen in time
   *
   * @return the current time slow factor of the enemy
   */
  public float getTimeSlowFactor() {
    return timeslowfactor;
  }

  /** Sets the time slow factor for the enemy. 1 being normal speed and 0 being frozen in time.
   *
   * @param tsf the time slow factor to set to this enemy
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
    System.out.println("velocity of enemy" + body.getLinearVelocity());
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
                getPosition().x, getPosition().y,
                dir * w, h);
  }
}
