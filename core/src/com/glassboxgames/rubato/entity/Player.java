package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Density */
  public static final float DENSITY = 1f;
  /** Friction */
  public static final float FRICTION = 0f;
  /** Jump impulse */
  public static final float JUMP_IMPULSE = 0.65f;
  public static float jumpImpulse = JUMP_IMPULSE;
  /** Movement impulse */
  public static final float MOVE_IMPULSE = 1f;
  /** Horizontal move damping */
  public static final float MOVE_DAMPING = 10f;
  /** Max horizontal speed */
  public static final float MAX_X_SPEED = 4f;
  public static float maxXSpeed = MAX_X_SPEED;
  /** Max vertical speed */
  public static final float MAX_Y_SPEED = 12f;
  public static float maxYSpeed = MAX_Y_SPEED;
  /** Min jump duration */
  public static final int MIN_JUMP_DURATION = 6;
  public static int minJumpDuration = MIN_JUMP_DURATION;
  /** Max jump duration */
  public static final int MAX_JUMP_DURATION = 12;
  public static int maxJumpDuration = MAX_JUMP_DURATION;
  /** Dash cooldown */
  public static final int DASH_COOLDOWN = 40;
  public static int dashCooldown = DASH_COOLDOWN;
  /** Dash duration */
  public static final int DASH_DURATION = 10;
  public static int dashDuration = DASH_DURATION;
  /** Dash speed */
  public static final float DASH_SPEED = 15f;
  public static float dashSpeed = DASH_SPEED;

  /** Attack hitbox position, relative to center */
  public static final Vector2 ATTACK_POS = new Vector2(0.4f, 0f);
  /** Attack hitbox radius */
  public static final float ATTACK_SIZE = 0.45f;
  /** Attack hitbox start frame */
  public static final int ATTACK_START = 5;
  /** Attack hitbox end frame */
  public static final int ATTACK_END = 25;
  /** Attack damage */
  public static final float ATTACK_DAMAGE = 3f;

  /** Player state constants */
  public static final int STATE_IDLE = 0;
  public static final int STATE_WALK = 1;
  public static final int STATE_FALL = 2;
  public static final int STATE_JUMP = 3;
  public static final int STATE_DASH = 4;
  public static final int STATE_GND_ATTACK = 5;
  public static final int STATE_UP_GND_ATTACK = 6;
  public static final int STATE_AIR_ATTACK = 7;
  public static final int STATE_DAIR_ATTACK = 8;
  public static final int STATE_UAIR_ATTACK = 9;

  /** Normalized vector indicating the directions the player is pressing */
  protected Vector2 input;
  /** Ground sensor for the player */
  protected GroundSensor groundSensor;

  /** Whether the player is currently alive */
  protected boolean alive;
  /** Whether the player is currently on a platform */
  protected boolean grounded;
  /** Whether the player has a dash available */
  protected boolean hasDash;
  /** Current dash length so far */
  protected int dashTime;
  /** Dash direction */
  protected Vector2 dashDir;
  /** Current jump length so far */
  protected int jumpTime;
  /** How long the player held jump */
  protected int jumpDuration;

  /** Enemies that have been hit by the current active attack */
  protected Array<Enemy> enemiesHit;

  /**
   * Instantiates a player with the given parameters.
   * @param x x-coordinate of center
   * @param y y-coordinate of center
   */
  public Player(float x, float y) {
    super(x, y);
    input = new Vector2();
    // TODO fix hardcoded dims
    groundSensor = new GroundSensor(this, new Vector2(0.3f, 1f));
    jumpTime = -1;
    jumpDuration = -1;
    dashTime = -1;
    enemiesHit = new Array();
    alive = true;
    hasDash = false;
    dashDir = new Vector2();
  }

  @Override
  public boolean activatePhysics(World world) {
    if (!super.activatePhysics(world)) {
      return false;
    }
    setState(STATE_IDLE);
    return groundSensor.activatePhysics();
  }

  /**
   * Tries to start a player jump or extend an existing jump.
   */
  public void tryJump() {
    if (isGrounded() && !isAttacking()) {
      setState(STATE_JUMP);
      jumpDuration = minJumpDuration;
    } else if (stateIndex == STATE_JUMP && jumpDuration < maxJumpDuration) {
      jumpDuration++;
    }
  }

  /**
   * Tries to start a player dash.
   */
  public void tryDash() {
    if (hasDash && dashTime < 0 && !isAttacking()) {
      body.setGravityScale(0f);
      hasDash = false;
      dashTime = 0;
      if (input.isZero()) {
        dashDir.set(dir, 0f);
      } else {
        dashDir.set(input);
      }
      setState(STATE_DASH);
    }
  }

  /**
   * Tries to start a player attack.
   */
  public void tryAttack() {
    if (!isAttacking() && !isDashing()) {
      if (isGrounded()) {
        if (input.y > 0) {
          setState(STATE_UP_GND_ATTACK);
        } else {
          setState(STATE_GND_ATTACK);
        }
      } else {
        if (input.y > 0) {
          setState(STATE_UAIR_ATTACK);
        } else if (input.y < 0) {
          setState(STATE_DAIR_ATTACK);
        } else {
          setState(STATE_AIR_ATTACK);
        }
      }
    }
  }

  /**
   * Returns whether the player is alive currently.
   */
  public boolean isAlive() {
    return alive;
  }

  /**
   * Sets the player's alive state.
   * @param value value to set
   */
  public void setAlive(boolean value) {
    alive = value;
  }
  
  /**
   * Returns whether the player is standing on a platform.
   */
  public boolean isGrounded() {
    return grounded;
  }

  /**
   * Sets the player's grounded state.
   * @param value value to set
   */
  public void setGrounded(boolean value) {
    grounded = value;
  }

  /**
   * Returns whether the player is dashing.
   */
  public boolean isDashing() {
    return stateIndex == STATE_DASH;
  }
  
  /**
   * Returns whether the player is attacking.
   */
  public boolean isAttacking() {
    return stateIndex == STATE_GND_ATTACK
      || stateIndex == STATE_UP_GND_ATTACK
      || stateIndex == STATE_AIR_ATTACK
      || stateIndex == STATE_DAIR_ATTACK
      || stateIndex == STATE_UAIR_ATTACK;
  }

  /**
   * Returns whether the player's attack hitbox is active.
   */
  public boolean isHitboxActive() {
    return isAttacking()
      && getState().getCount() >= ATTACK_START
      && getState().getCount() < ATTACK_END;
  }

  /**
   * Returns the array of enemies hit by the current attack.
   */
  public Array<Enemy> getEnemiesHit() {
    return enemiesHit;
  }

  /**
   * Sets the player's input vector.
   */
  public void setInputVector(float x, float y) {
    input.set(x, y);
  }

  /**
   * Tries to move the player based on the horizontal input.
   */
  public void tryMove() {
    if (input.x != 0) {
      if (!isAttacking()) {
        if (input.x > 0) {
          faceRight();
        } else if (input.x < 0) {
          faceLeft();
        }
      }
    }
  }

  @Override
  public void leaveState() {
    switch (stateIndex) {
    case STATE_DASH:
      body.setGravityScale(1f);
      break;
    case STATE_JUMP:
      jumpTime = jumpDuration = -1;
      break;
    case STATE_GND_ATTACK:
    case STATE_UP_GND_ATTACK:
    case STATE_AIR_ATTACK:
    case STATE_DAIR_ATTACK:
    case STATE_UAIR_ATTACK:
      enemiesHit.clear();
      break;
    }
  }
  
  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_GND_ATTACK:
    case STATE_UP_GND_ATTACK:
      if (getState().done) {
        setState(input.x != 0 ? STATE_WALK : STATE_IDLE);
      }
      break;
    case STATE_AIR_ATTACK:
    case STATE_UAIR_ATTACK:
    case STATE_DAIR_ATTACK:
      if (isGrounded()) {
        setState(input.x != 0 ? STATE_WALK : STATE_IDLE);
      } else if (getState().done) {
        setState(STATE_FALL);
      }
      break;
    case STATE_DASH:
      if (dashTime >= dashDuration) {
        if (isGrounded()) {
          setState(input.x != 0 ? STATE_WALK : STATE_IDLE);
        } else {
          setState(STATE_FALL);
        }
      }
      break;
    case STATE_JUMP:
      if (jumpTime >= jumpDuration) {
        setState(STATE_FALL);
      }
      break;
    case STATE_FALL:
      if (isGrounded()) {
        setState(input.x != 0 ? STATE_WALK : STATE_IDLE);
      }
      break;
    case STATE_WALK:
      if (input.x == 0) {
        setState(STATE_IDLE);
      }
      break;
    case STATE_IDLE:
      if (input.x != 0) {
        setState(STATE_WALK);
      }
      break;
    }
  }

  @Override
  public void update(float delta) {
    super.update(delta);

    Vector2 vel = new Vector2();
    if (isDashing()) {
      vel.set(dashDir).setLength(dashSpeed);
    } else {
      if (isGrounded()) {
        hasDash = true;
      } else {
        // vx -= vx / moveDamping;
      }
      
      if (input.x != 0) {
        temp.set(MOVE_IMPULSE * Math.signum(input.x), 0);
        body.applyLinearImpulse(temp, getPosition(), true);
      } else {
        // damping
        temp.set(-MOVE_DAMPING * getVelocity().x, 0);
        body.applyForce(temp, getPosition(), true);
      }

      if (jumpTime < jumpDuration) {
        temp.set(0, jumpImpulse);
        body.applyLinearImpulse(temp, getPosition(), true);
        jumpTime++;
      }

      vel.set(MathUtils.clamp(getVelocity().x, -maxXSpeed, maxXSpeed),
              MathUtils.clamp(getVelocity().y, -maxYSpeed, maxYSpeed));
    }
    
    if (dashTime >= 0 && dashTime < dashCooldown) {
      dashTime++;
    } else {
      dashTime = -1;
    }
    
    body.setLinearVelocity(vel);
  }

  @Override
  public void drawPhysics(GameCanvas canvas) {
    super.drawPhysics(canvas);
    Vector2 pos = getPosition();
    canvas.drawPhysics(groundSensor.getShape(), Color.RED, pos.x, pos.y, 0f);
    if (isHitboxActive()) {
      CircleShape shape = new CircleShape();
      pos = getPosition().add(temp.set(ATTACK_POS).scl(getDirection(), 1));
      shape.setPosition(pos);
      shape.setRadius(ATTACK_SIZE);
      canvas.drawPhysics(shape, Color.RED, pos.x, pos.y);
    }
  }
}
