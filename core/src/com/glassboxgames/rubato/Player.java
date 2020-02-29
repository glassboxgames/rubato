package com.glassboxgames.rubato;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.util.*;

import java.beans.VetoableChangeListener;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Density */
  protected static float DENSITY = 1f;
  /** Friction */
  protected static float FRICTION = 0f;
  /** Jump impulse */
  protected static float JUMP_IMPULSE = 4f;
  /** Movement impulse */
  protected static float MOVE_IMPULSE = 1f;
  /** Horizontal damping */
  protected static float MOVE_DAMPING = 5f;
  /** Max horizontal speed */
  protected static float MAX_X_SPEED = 3.5f;
  /** Max vertical speed */
  protected static float MAX_Y_SPEED = 8f;
  /** Min jump duration */
  protected static int MIN_JUMP_DURATION = 5;
  /** Max jump duration */
  protected static int MAX_JUMP_DURATION = 15;
  /** Attack duration */
  protected static int ATTACK_DURATION = 30;
  /** Attack cooldown */
  protected static int ATTACK_COOLDOWN = 60;
  
  /** Current animation frame */
  protected float animFrame;
  /** Current animation filmstrip length */
  protected int totalFrames;
  /** Direction the player is facing (1 for right, -1 for left) */
  protected int dir;
  /** Current horizontal movement of the character (from the input) */
  protected float movement;

  /** Is the player currently jumping */
  protected boolean isJumping;
  /** Current frame count since jump input */
  protected int jumpTime;
  /** Current total jump duration, can be extended; 0 if not jumping */
  protected int jumpDuration;
  /** Is the player currently attacking */
  protected boolean isAttacking;
  /** Current frame count since attack input */
  protected int attackTime;
  /** Current attack cooldown, 0 if not attacking */
  protected int attackCooldown;
  /** Current attack hitbox, if it exists */
  public Hitbox hitbox;

  /**
   * Instantiates a player with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   */
  public Player(float x, float y, float w, float h) {
    super(x, y);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(w / 2 / Constants.PPM, h / 2 / Constants.PPM);
    fixtureDef.shape = shape;
    fixtureDef.friction = FRICTION;
    fixtureDef.density = DENSITY;

    animFrame = 0;
    totalFrames = 0;
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
    // TODO fix
    if (jumpDuration > 0 && jumpDuration < MAX_JUMP_DURATION) {
      jumpDuration++;
    } else if (getPosition().y <= 0) {
      jumpDuration = MIN_JUMP_DURATION;
    }
  }

  /**
   * Returns whether the player is jumping.
   */
  public boolean isJumping() {
    return jumpDuration > 0;
  }

  /**
   * Tries to start a player attack.
   * @return whether an attack was started successfully
   */
  public boolean tryAttack() {
    if (attackCooldown == 0) {
      attackCooldown = ATTACK_COOLDOWN;
      return true;
    }
    return false;
  }

  /**
   * Returns whether the player is mid-attack.
   */
  public boolean isAttacking() {
    return attackCooldown > 0 && attackTime < ATTACK_DURATION;
  }
  
  /**
   * Gets the player's current facing direction (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Tries to set the player's horizontal movement.
   * @param input player input (1 for right, -1 for left, 0 for none)
   */
  public void tryMove(float input) {
    movement = input;
    if (input > 0) {
      dir = 1;
    } else if (input < 0) {
      dir = -1;
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

    if (movement != 0) {
      temp.set(MOVE_IMPULSE * movement, 0);
      System.out.println("first: " + temp + " " + getPosition() + " " + getVelocity());
      body.applyLinearImpulse(temp, getPosition(), true);
      System.out.println("second: " + temp + " " + getPosition() + " " + getVelocity());
    } else {
      // damping
      temp.set(-MOVE_DAMPING * getVelocity().x, 0);
      System.out.println(temp + " " + getPosition() + " " + getVelocity());
      body.applyForce(temp, getPosition(), true);
    }

    float vx = Math.min(MAX_X_SPEED, Math.max(-MAX_X_SPEED, getVelocity().x));
    float vy = Math.min(MAX_Y_SPEED, Math.max(-MAX_Y_SPEED, getVelocity().y));
    body.setLinearVelocity(vx, vy);
  }
  
  @Override
  public void draw(GameCanvas canvas) {
    float w = animator.getWidth();
    float h = animator.getHeight();
    canvas.draw(animator, Color.WHITE,
                dir * w / 2, h / 2,
                getPosition().x * Constants.PPM, getPosition().y * Constants.PPM,
                dir * w, h);
  }
}
