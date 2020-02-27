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
    if (pos.x >= maxX) {
      dir = -1;
    } else if (pos.x <= minX) {
      dir = 1;
    }
    float speed = MAX_SPEED * dir * health / MAX_HEALTH;
    pos.x += speed;
  }

  public void draw(GameCanvas canvas) {
    FilmStrip animator = getFilmStrip();
    float w = animator.getRegionWidth();
    float h = animator.getRegionHeight();
    canvas.draw(animator, Color.WHITE, w + dir * w / 2, 0, pos.x + w, pos.y, dir * w, h);
  }
}
