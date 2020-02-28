package com.glassboxgames.rubato;

import com.badlogic.gdx.graphics.*;
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
   */
  public Enemy(float x, float y) {
    super(x, y);
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
    if (getPos().x >= maxX) {
      dir = -1;
    } else if (getPos().x <= minX) {
      dir = 1;
    }
    float speed = MAX_SPEED * dir * health / MAX_HEALTH;
    getPos().x += speed;
  }

  @Override
  public void draw(GameCanvas canvas) {
    canvas.draw(getFilmStrip(), Color.WHITE,
                dim.x * dir / 2, 0,
                getPos().x, getPos().y,
                dim.x * dir, dim.y);
  }
}
