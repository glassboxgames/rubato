package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Jump impulse */
  public static final Vector2 JUMP_IMPULSE = new Vector2(0f, 5f);
  /** Wall jump impulse */
  public static final Vector2 WALLJUMP_IMPULSE = new Vector2(3f, 5f);
  /** Wall jump speed */
  public static final float WALLJUMP_SPEED = 8f;
  /** Movement impulse */
  public static final float MOVE_IMPULSE = 1f;
  /** Horizontal move damping */
  public static final float MOVE_DAMPING = 10f;
  /** Max horizontal speed */
  public static final float MAX_X_SPEED = 3.3f;
  public static float maxXSpeed = MAX_X_SPEED;
  /** Max vertical speed */
  public static final float MAX_Y_SPEED = 8f;
  public static float maxYSpeed = MAX_Y_SPEED;
  /** Min jump duration */
  public static final int MIN_JUMP_DURATION = 3;
  public static int minJumpDuration = MIN_JUMP_DURATION;
  /** Max jump duration */
  public static final int MAX_JUMP_DURATION = 12;
  public static int maxJumpDuration = MAX_JUMP_DURATION;
  /** Dash cooldown */
  public static final int DASH_COOLDOWN = 17;
  /** Dash duration */
  public static final int DASH_DURATION = 12;
  public static int dashDuration = DASH_DURATION;
  /** Dash speed */
  public static final float DASH_SPEED = 10f;
  public static float dashSpeed = DASH_SPEED;
  /** Attack damage */
  public static final float ATTACK_DAMAGE = 3f;
  /** Attack duration */
  public static final int ATTACK_DURATION = 10;
  public static int attackDuration = ATTACK_DURATION;
  /** Attack cooldown */
  public static final int ATTACK_COOLDOWN = 20;
  /** Cling time */
  public static final float CLING_DURATION = 5f;
  /** Slide velocity */
  public static final float SLIDE_VELOCITY = 1.3f;

  /** Player state constants */
  public static final int STATE_IDLE = 0;
  public static final int STATE_RUN = 1;
  public static final int STATE_FALL = 2;
  public static final int STATE_JUMP = 3;
  public static final int STATE_DASH = 4;
  public static final int STATE_CLING = 5;
  public static final int STATE_WALLJUMP = 6;

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
  /** Impulse to apply during jump */
  protected Vector2 jumpImpulse;

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

  /** Enemies that have been hit by the current active attack */
  protected ObjectSet<Enemy> enemiesHit;
  /** Entities that the player is currently using as ground */
  protected ObjectSet<Entity> entitiesUnderfoot;
  /** Entities that the player is adjacent to */
  protected ObjectSet<Entity> entitiesAdjacent;
  /** Amount of time until player starts sliding */
  protected float clingTime;

  /** Time shard */
  protected Shard shard;

  /** Particle effects */
  public ParticleEffect dashEffect;

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
    jumpImpulse = new Vector2();
    dashTime = -1;
    dashCooldown = -1;
    dashDir = new Vector2();
    enemiesHit = new ObjectSet<Enemy>();
    entitiesUnderfoot = new ObjectSet<Entity>();
    entitiesAdjacent = new ObjectSet<Entity>();
    clingTime = -1;
    alive = true;
    hasDash = false;
    shard = new Shard(x, y);

    dashEffect = new ParticleEffect();
    dashEffect.load(Gdx.files.internal("Particles/dash.pe"), Gdx.files.internal("Particles"));
    dashEffect.scaleEffect(0.7f);
  }

  /**
   * Initialize player states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Adagio/");
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  /**
   * Tries to start a jump.
   */
  public void tryJump() {
    if (stateIndex != STATE_JUMP && stateIndex != STATE_WALLJUMP) {
      if (isGrounded() && !isDashing()) {
        setState(STATE_JUMP);
        jumpImpulse.set(JUMP_IMPULSE);
      } else if (isClinging()) {
        setState(STATE_WALLJUMP);
        jumpImpulse.set(WALLJUMP_IMPULSE).scl(-dir, 1);
      }
    } 
  }

  /**
   * Tries to extend an existing jump.
   */
  public void tryExtendJump() {
    if (stateIndex == STATE_JUMP || stateIndex == STATE_WALLJUMP) {
      if (jumpDuration < maxJumpDuration) {
        jumpDuration++;
      }
    }
  }

  /**
   * Tries to start a player dash.
   */
  public void tryDash() {
    if (hasDash && dashCooldown < 0 && dashTime < 0) {
      setState(STATE_DASH);
      dashEffect.start();
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
        shard.startUpAttack();
      } else if (isClinging() || !isGrounded() && input.y < 0) {
        downAttack = true;
        shard.startDownAttack();
      } else {
        forwardAttack = true;
        shard.startForwardAttack();
      }
    }
  }

  /**
   * Tries to cling to a wall.
   */
  public void tryCling() {
    if (onWall() && !isGrounded() && !isDashing() && jumpDuration < 0 && input.x != 0) {
      setState(STATE_CLING);
    }
  }

  /**
   * Returns whether the player is alive.
   */
  public boolean isAlive() {
    return alive;
  }

  /**
   * Sets whether the player is alive.
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
   * Returns whether the player is running
   */
  public boolean isRunning() {
    return isGrounded() && input.x != 0;
  }

  /**
   * Returns whether the player is attacking.
   */
  public boolean isAttacking() {
    return attackTime >= 0 && attackTime < attackDuration;
  }

  /**
   * Returns whether the player is clinging.
   */
  public boolean isClinging() {
    return stateIndex == STATE_CLING;
  }

  /**
   * Returns whether the player is wall jumping.
   */
  public boolean isWallJumping() {
    return stateIndex == STATE_WALLJUMP;
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
   * Adds an entity to the list of entities adjacent.
   */
  public void addAdjacent(Entity entity) {
    entitiesAdjacent.add(entity);
  }

  /**
   * Removes an entity to the list of entities adjacent.
   */
  public void removeAdjacent(Entity entity) {
    entitiesAdjacent.remove(entity);
  }

  /**
   * Returns whether the player is adjacent to a wall.
   */
  public boolean onWall() {
    return !entitiesAdjacent.isEmpty();
  }

  /**
   * Returns the set of enemies hit by the current attack.
   */
  public ObjectSet<Enemy> getEnemiesHit() {
    return enemiesHit;
  }

  /**
   * Sets the player's input vector.
   */
  public void setInputVector(float x, float y) {
    input.set(x, y);
  }

  /**
   * Faces the player based on their input.
   */
  public void tryFace() {
    if (!isClinging() && !isAttacking() && !isDashing()) {
      if (input.x > 0) {
        faceRight();
      } else if (input.x < 0) {
        faceLeft();
      }
    }
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_JUMP:
    case STATE_WALLJUMP:
      jumpTime = 0;
      jumpDuration = minJumpDuration;
      break;
    case STATE_DASH:
      body.setGravityScale(0f);
      hasDash = false;
      dashCooldown = DASH_COOLDOWN;
      dashTime = 0;
      if (input.isZero()) {
        dashDir.set(getDirection(), 0f);
      } else {
        if (input.x != 0) {
          setDirection((int)Math.signum(input.x));
        }
        dashDir.set(input);
      }
      dashEffect.start();
      break;
    case STATE_CLING:
      body.setGravityScale(0f);
      clingTime = CLING_DURATION;
      break;
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
    case STATE_CLING:
      body.setGravityScale(1f);
      break;
    }
  }
  
  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_CLING:
      if (isGrounded()) {
        setState(STATE_IDLE);
      } else if (!onWall()) {
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
    case STATE_WALLJUMP:
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
    Vector2 pos = getPosition().scl(Shared.PPM);
    dashEffect.setPosition(pos.x, pos.y);
    dashEffect.update(delta);

    if (!isAlive()) {
      return;
    }
    super.update(delta);
    Vector2 vel = new Vector2();
    if (isDashing()) {
      vel.set(dashDir).setLength(dashSpeed);
      dashTime++;
    } else {
      if (isGrounded()) {
        hasDash = true;
      }

      if (dashCooldown >= 0) {
        dashCooldown--;
      }
      
      if (input.x != 0) {
        if (!isClinging() || input.x * dir < 0) {
          temp.set(MOVE_IMPULSE * Math.signum(input.x), 0);
          body.applyLinearImpulse(temp, getPosition(), true);
        }
      } else {
        // damping
        temp.set(-MOVE_DAMPING * getVelocity().x, 0);
        body.applyForce(temp, getPosition(), true);
      }

      if (jumpTime >= 0) {
        if (jumpTime < jumpDuration) {
          body.applyLinearImpulse(jumpImpulse, getPosition(), true);
          jumpTime++;
        } else {
          jumpTime = jumpDuration = -1;
        }
      }

      /* ------- all forces and impulses should be applied before this line! ------- */
      
      vel.set(MathUtils.clamp(getVelocity().x, -maxXSpeed, maxXSpeed),
              MathUtils.clamp(getVelocity().y, -maxYSpeed, maxYSpeed));
      
      /* ------- all manual velocities should be applied after this line! ------- */
      
      if (isClinging()) {
        hasDash = true;
        // squash weird bug where player bumps away from wall
        if (input.x * vel.x < 0) {
          vel.x = 0;
        }
        if (clingTime > 0) {
          vel.y = 0;
          clingTime--;
        } else {
          if (input.y < 0) {
            vel.y = -2 * SLIDE_VELOCITY;
          } else {
            vel.y = -SLIDE_VELOCITY;
          }
        }
      }

      /* ------- all manual velocities should be applied before this line! ------- */

      if (attackTime >= 0) {
        if (attackTime < attackDuration) {
          attackTime++;
        } else {
          attackTime = -1;
          forwardAttack = upAttack = downAttack = false;
          enemiesHit.clear();
          shard.stopAttack();
        }
      } else if (attackCooldown >= 0) {
        attackCooldown--;
      }
    }

    body.setLinearVelocity(vel);

    /* ------- all physics manipulations should be applied before this line! ------- */

    shard.update(delta, getPosition(), getDirection());
  }

  @Override
  public boolean activatePhysics(World world) {
    if (super.activatePhysics(world)) {
      return shard.activatePhysics(world);
    }
    return false;
  }

  @Override
  public void draw(GameCanvas canvas) {
    if (isAlive()) {
      super.draw(canvas);
      canvas.drawParticleEffect(dashEffect);
      if (isAttacking()) {
        shard.draw(canvas);
      }
    } else {
      canvas.setShader(Shared.DESAT_SHADER);
    }

  }
}
