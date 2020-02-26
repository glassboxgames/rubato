package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Constants for physics */
  public static final float THRUST_FACTOR = 0.2f;
  public static final float FORWARD_DAMPING = 0.9f;
  public static final float MAX_SPEED = 15f;

  Vector2 position;
  Vector2 velocity;
  boolean isJumping;
  boolean isGrounded;

  public Player(int x, int y) {
    position = new Vector2(x,y);
    velocity = new Vector2(0, 0);
    isJumping = false;
  }

  /**
   * Returns the vector position of the player.
   */
  public Vector2 getPosition() {
    return position;
  }

  /**
   * Returns the vector velocity of the player.
   */
  public Vector2 getVelocity() {
    return velocity;
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
  
  /** todo: reminder, we should focus on imperative programming
   * instead of having a jump() function, we should have a function called setJump*/
  public void jump() {
    System.out.println("Big bouncy time");
  }

  public void move(int direction) {
    if (direction != 0) {
      // Thrust key pressed; increase the ship velocity.
      if (velocity.x < MAX_SPEED) velocity.add(THRUST_FACTOR, 0);

      if (direction == -1) {
        position.x -= velocity.x;
      } else {
        position.x += velocity.x;
      }
    } else {
      velocity.scl(FORWARD_DAMPING, 0);
    }
  }
}
