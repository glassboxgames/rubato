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
  /** Dash duration */
  public static final int DASH_DURATION = 10;
  public static int dashDuration = DASH_DURATION;
  /** Dash speed */
  public static final float DASH_SPEED = 15f;
  public static float dashSpeed = DASH_SPEED;
  /** Attack damage */
  public static final float ATTACK_DAMAGE = 3f;
  /** Attack duration */
  public static final int ATTACK_DURATION = 10;
  public static int attackDuration = ATTACK_DURATION;
  /** Attack cooldown */
  public static final int ATTACK_COOLDOWN = 20;
  /** Parry capacity */
  public static final float PARRY_CAPACITY = 200f;
  public static float parryCapacity = PARRY_CAPACITY;
  /** Parry gain */
  public static final float PARRY_GAIN = 20f;
  public static float parryGain = PARRY_GAIN;
  /** Invulnerability duration */
  public static final float INVULNERABLE_DURATION = 30f;
  public static float invulnerableDuration = INVULNERABLE_DURATION;


  /** Player state constants */
  public static final int STATE_IDLE = 0;
  public static final int STATE_RUN = 1;
  public static final int STATE_FALL = 2;
  public static final int STATE_JUMP = 3;
  public static final int STATE_DASH = 4;

  /** Player states */
  public static Array<State> states = null;
  
  /** Normalized vector indicating the directions the player is pressing */
  protected Vector2 input;

  /** Whether the player is currently alive */
  protected boolean alive;
  /** Whether the player has a dash available */
  protected boolean hasDash;
  /** Time until next dash is available */
  protected int dashCooldown;
  /** Current dash length so far */
  protected int dashTime;
  /** Dash direction */
  protected Vector2 dashDir;
  /** Current jump length so far */
  protected int jumpTime;
  /** How long the player held jump */
  protected int jumpDuration;

  /** How long the player has been attacking */
  protected int attackTime;
  /** Remaining attack cooldown */
  protected int attackCooldown;

  /** Whether the player is attacking forward */
  protected boolean forwardAttack;
  /** Whether the player is attacking upward */
  protected boolean upAttack;
  /** Whether the player is attacking downward */
  protected boolean downAttack;

  /** Amount of parrying resource */
  protected float parry;
  /** Whether the player is currently parrying */
  protected boolean isParrying;
  /** Invulnerability time after player is hit */
  protected float invulnerableTime;

  /** Enemies that have been hit by the current active attack */
  protected ObjectSet<Enemy> enemiesHit;
  /** Entities that the player is currently using as ground */
  protected ObjectSet<Entity> entitiesUnderfoot;

  /**
   * Instantiates a player with the given parameters.
   * @param x x-coordinate of center
   * @param y y-coordinate of center
   */
  public Player(float x, float y) {
    super(x, y, STATE_IDLE);
    input = new Vector2();
    // TODO fix hardcoded dims
    attackTime = -1;
    jumpTime = -1;
    jumpDuration = -1;
    dashTime = -1;
    invulnerableTime = -1;
    dashCooldown = -1;
    parry = parryCapacity / 2;
    enemiesHit = new ObjectSet<Enemy>();
    entitiesUnderfoot = new ObjectSet<Entity>();
    alive = true;
    hasDash = false;
    dashDir = new Vector2();
  }

  /**
   * Initialize player states.
   */
  public static Array<State> initStates() {
    states = State.readStates("Adagio/");
    return states;
  }

  @Override
  public Array<State> getStates() {
    return states;
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
    if (!isAttacking() && hasDash && dashCooldown < 0 && dashTime < 0 ) {
      setState(STATE_DASH);
      body.setGravityScale(0f);
      hasDash = false;
      dashCooldown = DASH_COOLDOWN;
      dashTime = 0;
      if (input.isZero()) {
        dashDir.set(getDirection(), 0f);
      } else {
        dashDir.set(input);
      }
    }
  }

  /**
   * Tries to start a player attack.
   */
  public void tryAttack() {
    if (!isAttacking() && attackCooldown < 0) {
      attackTime = 0;
      attackCooldown = ATTACK_COOLDOWN;
      if (input.y > 0) {
        upAttack = true;
      } else if (!isGrounded() && input.y < 0) {
        downAttack = true;
      } else {
        forwardAttack = true;
      }
    }
  }

  /**
   * Tries to parry
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
    return attackTime >= 0 && attackTime < attackDuration;
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
  public void changeParry(float amount) {
    float newParry = parry + amount;
    if (amount < 0) {
      if (invulnerableTime < 0) {
        if (newParry < 0) {
          alive = false;
          parry = 0;
        } else {
          invulnerableTime = invulnerableDuration;
          parry = newParry;
        }
      }
    } else {
      parry = Math.min(newParry, parryCapacity);
    }
  }

  /**
   * Returns whether the player is attacking forward.
   */
  public boolean isAttackingForward() {
    return forwardAttack;
  }

  /**
   * Returns whether the player is attacking upwards.
   */
  public boolean isAttackingUp() {
    return upAttack;
  }

  /**
   * Returns whether the player is attacking downwards.
   */
  public boolean isAttackingDown() {
    return downAttack;
  }

  /**
   * Adds an entity to the list of entities underfoot.
   */
  public void addUnderfoot(Entity entity) {
    entitiesUnderfoot.add(entity);
  }

  /**
   * Removes an entity from the list of entities underfoot.
   */
  public void removeUnderfoot(Entity entity) {
    entitiesUnderfoot.remove(entity);
  }

  /**
   * Returns whether the player is grounded.
   */
  public boolean isGrounded() {
    return !entitiesUnderfoot.isEmpty();
  }

  /**
   * Returns the set of enemies hit by the current attack.
   */
  public ObjectSet<Enemy> getEnemiesHit() {
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
    }
  }
  
  @Override
  public void advanceState() {
    switch (stateIndex) {
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
      if (dashCooldown >= 0) {
        dashCooldown--;
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

      if (attackTime >= 0) {
        if (attackTime < attackDuration) {
          attackTime++;
        } else {
          attackTime = -1;
          forwardAttack = upAttack = downAttack = false;
          enemiesHit.clear();
        }
      } else if (attackCooldown >= 0) {
        attackCooldown--;
      }
      vel.set(MathUtils.clamp(getVelocity().x, -maxXSpeed, maxXSpeed),
              MathUtils.clamp(getVelocity().y, -maxYSpeed, maxYSpeed));
    }

    body.setLinearVelocity(vel);
    
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

    if (invulnerableTime >= 0) {
      invulnerableTime--;
    }
  }

  @Override
  public void draw(GameCanvas canvas) {
    super.draw(canvas);
    if (isAttacking()) {
      canvas.end();
      canvas.beginDebug(Constants.PPM, Constants.PPM);
      String key = isAttackingUp() ? "up" : (isAttackingDown() ? "down" : "forward");
      drawPhysicsShape(canvas, sensors.get(key).getFixture().getShape(), Color.GREEN);
      canvas.endDebug();
      canvas.begin(Constants.PPM, Constants.PPM);
    }
  }
}
