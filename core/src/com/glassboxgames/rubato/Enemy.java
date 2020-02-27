package com.glassboxgames.rubato;

/**
 * Class representing a simple path-following enemy in Rubato.
 */
public class Enemy extends Entity {
  /** Maximum health */
  private static float MAX_HEALTH = 10;
  /** Maximum speed */
  private static float MAX_SPEED = 5;
  /** Movement range */
  private static float MOVE_RANGE = 50;

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
    super(x, y, w, h);
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
    if (pos.x >= maxX) {
      dir = -1;
    } else if (pos.x <= minX) {
      dir = 1;
    }
    float speed = MAX_SPEED * dir * health / MAX_HEALTH;
    pos.x += speed;
  }
}
