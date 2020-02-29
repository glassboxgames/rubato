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
    shape.setAsBox(w / 2 / Constants.PPM, h / 2 / Constants.PPM);
    bodyDef.gravityScale = 0;
    fixtureDef.shape = shape;

    health = MAX_HEALTH;
    minX = x - MOVE_RANGE;
    maxX = x + MOVE_RANGE;
    dir = 1;
  }

  /**
   * Returns direction this enemy is facing (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
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
