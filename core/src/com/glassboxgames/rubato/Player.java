package com.glassboxgames.rubato;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.glassboxgames.util.*;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Jump force */
  private static float JUMP_FORCE = 4f;
  /** Gravity */
  private static float GRAVITY = 1f;
  /** Max vertical speed */
  private static float MAX_Y_SPEED = 8;
  /** Max horizontal speed */
  private static float MAX_X_SPEED = 5;
  /** Min jump duration */
  private static int MIN_JUMP_DURATION = 5;
  /** Max jump duration */
  private static int MAX_JUMP_DURATION = 15;
  /** Attack cooldown */
  private static int ATTACK_COOLDOWN = 30;
  
  /** Current animation frame */
  private float animFrame;
  /** Current animation filmstrip length */
  private int totalFrames;
  /** Velocity of the player */
  public Vector2 vel;
  /** Direction the player is facing (1 for right, -1 for left) */
  private int dir;
  /** Temp vector for calculations */
  private Vector2 temp = new Vector2();

  /** Current frame count since jump input */
  private int jumpTime;
  /** Current total jump duration, can be extended; 0 if not jumping */
  private int jumpDuration;

  /** Current frame count since attack input */
  private int attackTime;
  /** Current attack cooldown, 0 if not attacking */
  private int attackCooldown;

  public Player(float x, float y) {
    super(x, y);
    animFrame = 0;
    totalFrames = 0;
    vel = new Vector2(0, 0);
    dir = 1;
    jumpTime = 0;
    jumpDuration = 0;
    attackTime = 0;
    attackCooldown = 0;
  }

  /**
   * Tries to start a player jump or extend an existing jump.
   */
  public void tryJump() {
    if (jumpDuration > 0 && jumpDuration < MAX_JUMP_DURATION) {
      jumpDuration++;
    } else if (pos.y <= 0) {
      jumpDuration = MIN_JUMP_DURATION;
    }
  }

  /**
   * Tries to start a player attack.
   */
  public void tryAttack() {
    if (attackCooldown == 0) {
      attackCooldown = ATTACK_COOLDOWN;
    }
  }

  /**
   * Returns whether the player is attacking.
   */
  public boolean isAttacking() {
    return attackTime == 1;
  }
  
  /**
   * Gets the player's current facing direction (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Tries to set the player's horizontal movement.
   */
  public void tryMove(float input) {
    vel.x = input * MAX_X_SPEED;
    if (input != 0) {
      dir = (int)input;
    }
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    if (attackTime < attackCooldown) {
      attackTime++;
    } else if (attackCooldown > 0) {
      attackTime = attackCooldown = 0;
    }

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

  @Override
  public void draw(GameCanvas canvas) {
    canvas.draw(getFilmStrip(), Color.WHITE,
                dim.x * dir / 2, 0,
                pos.x, pos.y,
                dim.x * dir, dim.y);
  }
}
