package com.glassboxgames.rubato;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.glassboxgames.util.*;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  private boolean isJumping;
  private boolean isGrounded;
  private int dir;
  /** Current animation frame for Adagio */
  private float animeframe;

  /** How fast we change frames (one frame per 4 calls to update) */
  private static final float ANIMATION_SPEED = 0.25f;
  /** The number of animation frames in our walk filmstrip */
  private static int totalFrames;
  /** The number of animation frame rows */
  private static int rowFrames;
  /** The number of animation frame columns */
  private static int colFrames;

  /** Adagio's width, in pixels */
  public static final int   ADAGIO_WIDTH = 50;
  /** Adagio's width, in pixels */
  public static final int   ADAGIO_HEIGHT = 100;

  public Player(int x, int y) {
    pos = new Vector2(x, y);
    vel = new Vector2(0, 0);
    dim = new Vector2(50, 100);
    dir = 1;
    isJumping = false;
    isGrounded = false;
  }

  public void setTexture(Texture texture, int rows, int cols, int size) {
    animator = new FilmStrip(texture,rows,cols,size);
    totalFrames = size;
    rowFrames = rows;
    colFrames = cols;
  }

  /**
   * Sets the player's jump state.
   */
  public void setJump(boolean jump) {
    isJumping = jump;
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
  public void setMove(float input) {
    vel.x = input;
    if (input < 0 && dir > 0) {
      dir = -1;
    } else if (input > 0 && dir < 0) {
      dir = 1;
    }
  }

  /**
   * Updates the animation frame and position of this ship.
   *
   * Notice how little this method does.  It does not actively fire the weapon.  It
   * only manages the cooldown and indicates whether the weapon is currently firing.
   * The result of weapon fire is managed by the GameplayController.
   *
   * @param delta Number of seconds since last animation frame
   */
  public void update(float delta) {
    // Call superclass's update
    super.update(delta);

    // Increase animation frame, but only if trying to move
    if (vel.x != 0.0f) {
      animeframe += ANIMATION_SPEED;
      if (animeframe >= totalFrames) {
        animeframe -= totalFrames;
      }
    }
  }

  public void draw(GameCanvas canvas) {
    animator.setFrame((int)animeframe);
    canvas.draw(animator, Color.WHITE, ADAGIO_WIDTH/2, ADAGIO_HEIGHT/2, pos.x, pos.y, ADAGIO_WIDTH, ADAGIO_HEIGHT);
  }
}
