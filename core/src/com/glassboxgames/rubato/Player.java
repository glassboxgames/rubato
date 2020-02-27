package com.glassboxgames.rubato;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.glassboxgames.util.*;

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
  /** Current animation frame for Adagio */
  private float animframe;

  /** How fast we change frames (one frame per 4 calls to update) */
  private static final float ANIMATION_SPEED = 0.25f;
  /** The number of animation frames in our walk filmstrip */
  private static int totalFrames;

  /** Adagio's width, in pixels */
  public static final int   ADAGIO_WIDTH = 50;
  /** Adagio's height, in pixels */
  public static final int   ADAGIO_HEIGHT = 100;

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

  public void setTexture(Texture texture, int rows, int cols, int size) {
    animator = new FilmStrip(texture,rows,cols,size);
    totalFrames = size;
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

    // Increase animation frame, but only if trying to move
    if (vel.x != 0) {
      animframe += ANIMATION_SPEED;
      if (animframe >= totalFrames) {
        animframe -= totalFrames;
      }
    }
  }

  public void draw(GameCanvas canvas) {
    animator.setFrame((int)animframe);
    // TODO: Fix heading
    canvas.draw(animator, Color.WHITE, 0, 0, pos.x, pos.y, ADAGIO_WIDTH, ADAGIO_HEIGHT);
  }
}
