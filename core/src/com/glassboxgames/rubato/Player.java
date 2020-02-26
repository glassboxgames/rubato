package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  private boolean isJumping;
  private boolean isGrounded;
  private int dir;

  public Player(int x, int y) {
    pos = new Vector2(x, y);
    vel = new Vector2(0, 0);
    dim = new Vector2(50, 100);
    dir = 1;
    isJumping = false;
    isGrounded = false;
  }

  /**
   * Returns whether the player is jumping.
   */
  public boolean isJumping() {
    return isJumping;
  }

  /**
   * Sets the player's jump state.
   */
  public void setJump(boolean x) {
    isJumping = x;
  }

  /**
   * Gets the player's current facing direction (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Sets the player's horizontal movement.
   */
  public void move(float input) {
    vel.x = input;
    if (input < 0 && dir > 0) {
      dir = -1;
    } else if (input > 0 && dir < 0) {
      dir = 1;
    }
  }
}
