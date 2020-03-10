package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
  /** Density */
  protected static final float DENSITY = 1f;
  /** Friction */
  protected static final float FRICTION = 0f;
  /** Jump force */
  protected static final float JUMP_IMPULSE = 0.5f;
  /** Movement impulse */
  protected static final float MOVE_IMPULSE = 1f;
  /** Horizontal damping */
  protected static final float MOVE_DAMPING = 10f;
  /** Max horizontal speed */
  protected static final float MAX_X_SPEED = 4f;
  /** Max vertical speed */
  protected static final float MAX_Y_SPEED = 11f;
  /** Min jump duration */
  protected static final int MIN_JUMP_DURATION = 6;
  /** Max jump duration */
  protected static final int MAX_JUMP_DURATION = 12;
  /** Attack hitbox position, relative to center */
  protected static final Vector2 ATTACK_POS = new Vector2(0.4f, 0f);
  /** Attack hitbox radius */
  protected static final float ATTACK_SIZE = 0.45f;
  /** Attack hitbox start frame */
  protected static final int ATTACK_START = 5;
  /** Attack hitbox end frame */
  protected static final int ATTACK_END = 25;
  /** Attack damage */
  protected static final float ATTACK_DAMAGE = 3f;

  /** Player state constants */
  public static final int NUM_STATES = 6;
  public static final int STATE_IDLE = 0;
  public static final int STATE_WALK = 1;
  public static final int STATE_FALL = 2;
  public static final int STATE_JUMP = 3;
  public static final int STATE_GND_ATTACK = 4;
  public static final int STATE_AIR_ATTACK = 5;

  /** Player dimensions */
  protected Vector2 dim;
  /** Current horizontal movement of the character (from the input) */
  protected float movement;
  /** Ground sensor for the player */
  protected GroundSensor groundSensor;

  /** Whether the player is currently alive */
  protected boolean alive;
  /** Whether the player is currently on a platform */
  protected boolean grounded;
  /** Current jump length so far */
  protected int jumpTime;
  /** How long the player held jump */
  protected int jumpDuration;

  /** Enemies that have been hit by the current active attack */
  protected Array<Enemy> enemiesHit;


  /**
   * Instantiates a player with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   * @param numStates number of entity states
   */
  public Player(float x, float y, float w, float h, int numStates) {
    super(x, y, numStates);
    dim = new Vector2(w, h);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(dim.x / 2, dim.y / 2);
    fixtureDef.shape = shape;
    fixtureDef.friction = FRICTION;
    fixtureDef.density = DENSITY;
    groundSensor = new GroundSensor(this, dim);
    jumpTime = 0;
    jumpDuration = 0;
    dir = 1;
    enemiesHit = new Array();
    alive = true;
  }

  @Override
  public boolean activatePhysics(World world) {
    if (!super.activatePhysics(world)) {
      return false;
    }
    return groundSensor.activatePhysics();
  }

  /**
   * Tries to start a player jump or extend an existing jump.
   */
  public void tryJump() {
    if (isGrounded() && !isAttacking()) {
      setState(STATE_JUMP);
      jumpDuration = MIN_JUMP_DURATION;
    } else if (stateIndex == STATE_JUMP && jumpDuration < MAX_JUMP_DURATION) {
      jumpDuration++;
    }
  }

  /**
   * Tries to start a player attack.
   */
  public void tryAttack() {
    if (!isAttacking()) {
      setState(isGrounded() ? STATE_GND_ATTACK : STATE_AIR_ATTACK);
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
   * Returns whether the player is mid-attack-animation.
   */
  public boolean isAttacking() {
    return stateIndex == STATE_GND_ATTACK || stateIndex == STATE_AIR_ATTACK;
  }

  /**
   * Returns whether the player's attack hitbox is active.
   */
  public boolean isHitboxActive() {
    return isAttacking()
      && getState().activeTime >= ATTACK_START
      && getState().activeTime < ATTACK_END;
  }

  /**
   * Returns the array of enemies hit by the current attack.
   */
  public Array<Enemy> getEnemiesHit() {
    return enemiesHit;
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
    if (!isAttacking()) {
      if (input > 0) {
        dir = 1;
      } else if (input < 0) {
        dir = -1;
      }
    }
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_GND_ATTACK:
      if (getState().done) {
        enemiesHit.clear();
        setState(movement != 0 ? STATE_WALK : STATE_IDLE);
      }
      break;
    case STATE_AIR_ATTACK:
      if (isGrounded()) {
        enemiesHit.clear();
        setState(movement != 0 ? STATE_WALK : STATE_IDLE);
      } else if (getState().done) {
        enemiesHit.clear();
        setState(STATE_FALL);
      }
      break;
    case STATE_JUMP:
      if (jumpTime >= jumpDuration) {
        setState(STATE_FALL);
        jumpTime = jumpDuration = 0;
      }
      break;
    case STATE_FALL:
      if (isGrounded()) {
        setState(movement != 0 ? STATE_WALK : STATE_IDLE);
      }
      break;
    case STATE_WALK:
      if (movement == 0) {
        setState(STATE_IDLE);
      }
      break;
    case STATE_IDLE:
      if (movement != 0) {
        setState(STATE_WALK);
      }
      break;
    }
  }

  @Override
  public void update(float delta) {
    super.update(delta);

    if (movement != 0) {
      temp.set(MOVE_IMPULSE * movement, 0);
      body.applyLinearImpulse(temp, getPosition(), true);
    } else {
      // damping
      temp.set(-MOVE_DAMPING * getVelocity().x, 0);
      body.applyForce(temp, getPosition(), true);
    }

    if (jumpTime < jumpDuration) {
      temp.set(0, JUMP_IMPULSE);
      body.applyLinearImpulse(temp, getPosition(), true);
      jumpTime++;
    } 
    
    float vx = Math.min(MAX_X_SPEED, Math.max(-MAX_X_SPEED, getVelocity().x));
    float vy = Math.min(MAX_Y_SPEED, Math.max(-MAX_Y_SPEED, getVelocity().y));
    body.setLinearVelocity(vx, vy);
  }

  @Override
  public void drawPhysics(GameCanvas canvas) {
    Vector2 pos = getPosition();
    canvas.drawPhysics((PolygonShape)fixture.getShape(), Color.RED,
                       pos.x, pos.y, 0);
    canvas.drawPhysics(groundSensor.getShape(), Color.RED,
                       pos.x, pos.y, 0);
    if (isHitboxActive()) {
      CircleShape shape = new CircleShape();
      shape.setPosition(getPosition().add(temp.set(ATTACK_POS).scl(dir, 1)));
      shape.setRadius(ATTACK_SIZE);
      canvas.drawPhysics(shape, Color.RED, shape.getPosition().x, shape.getPosition().y);
    }
  }
}
