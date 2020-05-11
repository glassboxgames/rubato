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
  /** Max horizontal speed */
  public static final float MAX_X_SPEED = 4f;
  public static float maxXSpeed = MAX_X_SPEED;
  /** Max vertical speed */
  public static final float MAX_Y_SPEED = 8f;
  public static float maxYSpeed = MAX_Y_SPEED;
  /** Movement impulse */
  public static final float MOVE_IMPULSE = 1f;
  /** Horizontal move damping */
  public static final float MOVE_DAMPING = 10f;
  /** Jump impulse */
  public static final Vector2 JUMP_IMPULSE = new Vector2(0f, 5f);
  /** Min jump duration */
  public static final int MIN_JUMP_DURATION = 3;
  public static int minJumpDuration = MIN_JUMP_DURATION;
  /** Max jump duration */
  public static final int MAX_JUMP_DURATION = 12;
  public static int maxJumpDuration = MAX_JUMP_DURATION;
  /** Attack damage */
  public static final float ATTACK_DAMAGE = 3f;
  /** Attack impulse */
  public static final Vector2 ATTACK_IMPULSE = new Vector2(0f, 5f);
  /** Attack impulse duration */
  public static final int ATTACK_IMPULSE_DURATION = 8;
  /** Attack cooldown */
  public static final int ATTACK_COOLDOWN = 0;

  /** Player state constants */
  public static final int STATE_IDLE = 0;
  public static final int STATE_RUN = 1;
  public static final int STATE_FALL = 2;
  public static final int STATE_JUMP = 3;
  public static final int STATE_ATTACK = 4;
  public static final int STATE_DEAD = 5;

  /** Player states */
  public static Array<State> states = null;
  
  /** Normalized vector indicating the directions the player is pressing */
  private Vector2 input;

  /** Current jump length so far */
  private int jumpTime;
  /** How long the player has held jump */
  private int jumpDuration;

  /** How long the player has been attacking */
  private int attackTime;
  /** Remaining attack cooldown */
  private int attackCooldown;

  /** Whether the player is currently active */
  protected boolean active;
  /** Enemies that have been hit by the current active attack */
  private ObjectSet<Enemy> enemiesHit;
  /** Entities that the player is currently using as ground */
  private ObjectSet<Entity> entitiesUnderfoot;
  /** Entities that the player is adjacent to */
  private ObjectSet<Entity> entitiesAdjacent;

  /** The singleton instance of the sound controller */
  SoundController soundController = SoundController.getInstance();

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
    enemiesHit = new ObjectSet<Enemy>();
    entitiesUnderfoot = new ObjectSet<Entity>();
    entitiesAdjacent = new ObjectSet<Entity>();
    active = true;
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
    if ((stateIndex != STATE_DEAD) && !isJumping() && isGrounded()) {
      setState(STATE_JUMP);
    } 
  }

  /**
   * Tries to extend an existing jump.
   */
  public void tryExtendJump() {
    if ((stateIndex != STATE_DEAD) && isJumping()) {
      if (jumpDuration < maxJumpDuration) {
        jumpDuration++;
      }
    }
  }

  /**
   * Tries to start a player attack.
   */
  public void tryAttack() {
    if ((stateIndex != STATE_DEAD) && !isAttacking() && attackCooldown < 0) {
      soundController.play(Shared.ATTACK_SWING_SOUND, Shared.ATTACK_SWING_SOUND, false, 0.1f);
      setState(STATE_ATTACK);
    }
  }

  /**
   * Returns whether the player is active (alive or dying).
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets whether the player is alive.
   */
  public void setAlive(boolean value) {
    if (value) {
      if (stateIndex == STATE_DEAD) {
        setState(STATE_IDLE);
      }
    } else if (stateIndex != STATE_DEAD) {
      soundController.play(Shared.DEATH_SOUND, Shared.DEATH_SOUND, false, 0.25f);
      setState(STATE_DEAD);
    }
  }

  /**
   * Returns whether the player is running.
   */
  public boolean isRunning() {
    return stateIndex == STATE_RUN;
  }

  /**
   * Returns whether the player is jumping.
   */
  public boolean isJumping() {
    return stateIndex == STATE_JUMP;
  }
  
  /**
   * Returns whether the player is attacking.
   */
  public boolean isAttacking() {
    return stateIndex == STATE_ATTACK;
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
   * Returns the set of enemies hit by the current attack.
   */
  public ObjectSet<Enemy> getEnemiesHit() {
    return enemiesHit;
  }

  /**
   * Sets the player's input vector.
   */
  public void setInputVector(float x, float y) {
    if (stateIndex != STATE_DEAD) {
      input.set(x, y);
    }
  }

  /**
   * Faces the player based on their input.
   */
  public void tryFace() {
    if (stateIndex != STATE_DEAD) {
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
    case STATE_ATTACK:
      attackTime = 0;
      break;
    case STATE_JUMP:
      jumpTime = 0;
      jumpDuration = minJumpDuration;
      break;
    case STATE_DEAD:
      body.setGravityScale(0f);
      break;
    }
  }

  @Override
  public void leaveState() {
    super.leaveState();
    switch (stateIndex) {
    case STATE_ATTACK:
      attackTime = -1;
      attackCooldown = ATTACK_COOLDOWN;
      enemiesHit.clear();
      break;
    }
  }
  
  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_ATTACK:
      if (getCount() > getState().getLength()) {
        setState(STATE_FALL);
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

    if (stateIndex == STATE_DEAD) {
      body.setLinearVelocity(new Vector2());
      if (getCount() >= getState().getLength()) {
        active = false;
      }
      return;
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
        body.applyLinearImpulse(JUMP_IMPULSE, getPosition(), true);
        jumpTime++;
      } else {
        jumpTime = jumpDuration = -1;
      }
    }

    if (isAttacking()) {
      attackTime++;
      if (attackTime < ATTACK_IMPULSE_DURATION) {
        body.applyLinearImpulse(ATTACK_IMPULSE, getPosition(), true);
      }
    } else if (attackCooldown >= 0) {
      attackCooldown--;
    }

    /* ------- all physics should be applied before this line! ------- */
      
    body.setLinearVelocity(getVelocity().set(MathUtils.clamp(getVelocity().x, -maxXSpeed, maxXSpeed),
                                             MathUtils.clamp(getVelocity().y, -maxYSpeed, maxYSpeed)));
  }

  @Override
  public boolean activatePhysics(World world) {
    return super.activatePhysics(world);
  }

  @Override
  public void draw(GameCanvas canvas) {
    if (active) {
      super.draw(canvas);
    }
  }
}
