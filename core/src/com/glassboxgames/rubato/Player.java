package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Velocity of the player */
  public Vector2 vel;
  /** Temp vector for calculations */
  private Vector2 temp = new Vector2();
  /** Current total jump duration, can be extended */
  private float jumpDuration;
  /** Current jump time */
  private float jumpTime;
  /** Direction the player is facing (1 for right, -1 for left) */
  private int dir;

  /** Jump force */
  private static float JUMP_FORCE = 4f;
  /** Gravity */
  private static float GRAVITY = 1f;
  /** Min jump duration */
  private static float MIN_JUMP_DURATION = 5;
  /** Max jump duration */
  private static float MAX_JUMP_DURATION = 15;
  /** Max vertical speed */
  private static float MAX_Y_SPEED = 8;
  /** Max horizontal speed */
  private static float MAX_X_SPEED = 5;
  
  public Player(int x, int y) {
    pos = new Vector2(x, y);
    vel = new Vector2(0, 0);
    dim = new Vector2(50, 100);
    dir = 1;
    jumpDuration = 0;
  }

  /**
   * Sets the player's jump state.
   */
  public void setJump(boolean jump) {
    if (jump) {
      if (jumpDuration > 0 && jumpDuration < MAX_JUMP_DURATION) {
        jumpDuration++;
      } else if (pos.y <= 0) {
        jumpDuration = MIN_JUMP_DURATION;
      }
    }
  }

  /**
   * Gets the player's current facing direction (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Sets the player's horizontal movement intention.
   */
  public void setMove(float input) {
    vel.x = input * MAX_X_SPEED;
    if (input != 0) {
      dir = (int)input;
    }
  }

  @Override
  public void update(float delta) {
    if (jumpTime < jumpDuration) {
      vel.y += JUMP_FORCE;
      jumpTime++;
    } else {
      jumpTime = jumpDuration = 0;
    }
    vel.y -= GRAVITY;
    vel.y = Math.max(-MAX_Y_SPEED, Math.min(MAX_Y_SPEED, vel.y));
    if (pos.y <= 0 && vel.y <= 0) {
      vel.y = 0;
    }
    pos.add(vel);
  }
}
