package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Constants for physics */
  public static final float THRUST_FACTOR = 1f;
  public static final float FORWARD_DAMPING = 0.9f;
  public static final float MAX_SPEED = 10f;

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
   * Sets the player's jump state.
   */
  public void setJump(boolean jump) {
    isJumping = jump;
  }
  
  /** TODO: reminder, we should focus on imperative programming
   * instead of having a jump() function, we should have a function called setJump*/
  public void jump() {
    System.out.println("Big bouncy time");
  }

/**
 * Move the player.
 */
  public void move(int direction) {
    if (direction != 0) {
      // Move key pressed; increase the player velocity.
      if (velocity.len() < MAX_SPEED) {
        if (direction == -1) {
          velocity.add(-THRUST_FACTOR, 0);
        } else {
          velocity.add(THRUST_FACTOR, 0);
        }
      }
      position.x += velocity.x;
    } else {
      velocity.scl(FORWARD_DAMPING, 0);
    }
  }

  /**
   * Whether or not the player is moving left
   */
  public boolean movingLeft() {
    return moving() > 5 && moving() < 8;
  }

  /**
   * Whether or not the player is moving right
   */
  public boolean movingRight() {
    return moving() > 0 && moving() < 4;
  }

  /* 0 is not moving, 1 up-right, 2 is right, 3 is right-down, 4 is down, 5 is down-left, 6 is left, 7 is up-left, 8 is up */
  private int moving() {
    boolean right = velocity.x > 0;
    boolean up = velocity.y > 0;
    boolean left = velocity.x < 0;
    boolean down = velocity.y < 0;
    if (right) {
      if (up) {
        return 1;
      }
      else if (down) {
        return 3;
      }
      return 2;
    }
    else if (left) {
      if (up) {
        return 7;
      }
      else if (down) {
        return 5;
      }
      return 6;
    }
    else if (up) {
      return 8;
    }
    else if (down) {
      return 4;
    }
    return 0;
  }

}
