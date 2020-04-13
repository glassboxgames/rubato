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
  /** Jump impulse */
  public static final float JUMP_IMPULSE = 1f;
  public static float jumpImpulse = JUMP_IMPULSE;
  /** Movement impulse */
  public static final float MOVE_IMPULSE = 1f;
  /** Horizontal move damping */
  public static final float MOVE_DAMPING = 10f;
  /** Max horizontal speed */
  public static final float MAX_X_SPEED = 4f;
  public static float maxXSpeed = MAX_X_SPEED;
  /** Max vertical speed */
  public static final float MAX_Y_SPEED = 10f;
  public static float maxYSpeed = MAX_Y_SPEED;
  /** Min jump duration */
  public static final int MIN_JUMP_DURATION = 3;
  public static int minJumpDuration = MIN_JUMP_DURATION;
  /** Max jump duration */
  public static final int MAX_JUMP_DURATION = 18;
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
  /** Attack damage */
  public static final float ATTACK_DAMAGE = 3f;
  /** Parry capacity */
  public static final float PARRY_CAPACITY = 100f;
  public static float parryCapacity = PARRY_CAPACITY;
  /** Parry gain */
  public static final float PARRY_GAIN = 20f;
  public static float parryGain = PARRY_GAIN;

  /** Player state constants */
  public static final int STATE_IDLE = 0;
  public static final int STATE_RUN = 1;
  public static final int STATE_FALL = 2;
  public static final int STATE_JUMP = 3;
  public static final int STATE_DASH = 4;
  public static final int STATE_GND_ATTACK = 5;
  public static final int STATE_UP_GND_ATTACK = 6;
  public static final int STATE_AIR_ATTACK = 7;
  public static final int STATE_DAIR_ATTACK = 8;
  public static final int STATE_UAIR_ATTACK = 9;

  /** Player states */
  public static Array<State> states = null;
  
  /** Normalized vector indicating the directions the player is pressing */
  protected Vector2 input;

  /** Whether the player is currently alive */
  protected boolean alive;
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
  /** Amount of parrying resource */
  protected float parry;
  /** Whether the player is currently parrying */
  protected boolean isParrying;

  /** Enemies that have been hit by the current active attack */
  protected Array<Enemy> enemiesHit;
  /** Entities that the player is currently using as ground */
  protected Array<Entity> entitiesUnderfoot;

  /**
   * Instantiates a player with the given parameters.
   * @param x x-coordinate of center
   * @param y y-coordinate of center
   */
  public Player(float x, float y) {
    super(x, y);
    alive = true;
    input = new Vector2();
    // TODO fix hardcoded dims
    jumpTime = -1;
    jumpDuration = -1;
    dashTime = -1;
    enemiesHit = new Array<>();
    entitiesUnderfoot = new Array<>();
    hasDash = false;
    dashDir = new Vector2();
    parry = 0;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public boolean activatePhysics(World world) {
    if (!super.activatePhysics(world)) {
      return false;
    }
    setState(STATE_IDLE);
    return true;
  }

  /**
   * Tries to start a player jump or extend an existing jump.
   */
  public void tryJump() {
    if (isGrounded() && !isAttacking() && !isDashing()) {
      setState(STATE_JUMP);
      jumpTime = 0;
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
      setState(STATE_DASH);
      body.setGravityScale(0f);
      hasDash = false;
      dashTime = 0;
      if (input.isZero()) {
        dashDir.set(dir, 0f);
      } else {
        dashDir.set(input);
      }
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
   * Tries to parry.
   */
  public void tryParry() {
    if (!isAttacking() && parry > 0) {
      isParrying = true;
    }
  }

  /**
   * Stops parrying.
   */
  public void endParry() {
    isParrying = false;
  }

  /**
   * Returns whether the player is alive currently.
   */
  public boolean isAlive() {
    return alive;
  }

  /**
   * Sets the player's alive state.
   */
  public void setAlive(boolean value) {
    alive = value;
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
   * Returns whether the player is parrying.
   */
  public boolean isParrying() {
    return isParrying;
  }

  /**
   * Adds parry resource.
   */
  public void addParry(float amount) {
    parry = Math.min(parry + amount, parryCapacity);
    System.out.println(parry);
  }

  /**
   * Returns whether the player is grounded.
   */
  public boolean isGrounded() {
    return !entitiesUnderfoot.isEmpty();
  }

  /**
   * Adds an entity to the list of entities underfoot.
   */
  public void addUnderfoot(Entity entity) {
    if (!entitiesUnderfoot.contains(entity, true)) {
      entitiesUnderfoot.add(entity);
    }
  }

  /**
   * Removes an entity from the list of entities underfoot.
   */
  public void removeUnderfoot(Entity entity) {
    if (entitiesUnderfoot.contains(entity, true)) {
      entitiesUnderfoot.removeValue(entity, true);
    }
  }

  /**
   * Returns the array of enemies hit by the current attack.
   */
  public Array<Enemy> getEnemiesHit() {
    return enemiesHit;
  }

  /**
   * Returns the player's parry resource.
   */
  public float getParry() {
    return parry;
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
      if (!isAttacking() && !isDashing()) {
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
    super.leaveState();
    switch (stateIndex) {
    case STATE_DASH:
      body.setGravityScale(1f);
      dashTime = -1;
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
      if (!getState().isLooping() && count >= getState().getLength()) {
        setState(input.x != 0 ? STATE_RUN : STATE_IDLE);
      }
      break;
    case STATE_AIR_ATTACK:
    case STATE_UAIR_ATTACK:
    case STATE_DAIR_ATTACK:
      if (isGrounded()) {
        setState(input.x != 0 ? STATE_RUN : STATE_IDLE);
      } else if (count >= getState().getLength()) {
        setState(STATE_FALL);
      }
      break;
    case STATE_DASH:
      if (dashTime >= dashDuration) {
        if (isGrounded()) {
          setState(input.x != 0 ? STATE_RUN : STATE_IDLE);
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
        setState(input.x != 0 ? STATE_RUN : STATE_IDLE);
      }
      break;
    case STATE_RUN:
      if (!isGrounded()) {
        setState(STATE_FALL);
      } else if (input.x == 0) {
        setState(STATE_IDLE);
      }
      break;
    case STATE_IDLE:
      if (!isGrounded()) {
        setState(STATE_FALL);
      } else if (input.x != 0) {
        setState(STATE_RUN);
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
      }
      
      if (input.x != 0) {
        temp.set(MOVE_IMPULSE * Math.signum(input.x), 0);
        body.applyLinearImpulse(temp, getPosition(), true);
      } else {
        // damping
        temp.set(-MOVE_DAMPING * getVelocity().x, 0);
        body.applyForce(temp, getPosition(), true);
      }

      if (jumpTime >= 0) {
        if (jumpTime < jumpDuration) {
          temp.set(0, jumpImpulse);
          body.applyLinearImpulse(temp, getPosition(), true);
          jumpTime++;
        } else {
          jumpTime = jumpDuration = -1;
        }
      }

      vel.set(MathUtils.clamp(getVelocity().x, -maxXSpeed, maxXSpeed),
              MathUtils.clamp(getVelocity().y, -maxYSpeed, maxYSpeed));
    }
    
    if (dashTime >= 0) {
      dashTime++;
    }

    if (isParrying) {
      if (parry <= 0) {
        endParry();
      } else {
        parry--;
      }
    }
    
    body.setLinearVelocity(vel);
  }
}
