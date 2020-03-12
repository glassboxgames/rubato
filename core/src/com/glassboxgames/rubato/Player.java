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
  protected static final float JUMP_IMPULSE = 0.85f;
  /** Movement impulse */
  protected static final float MOVE_IMPULSE = 1f;
  /** Horizontal damping */
  protected static final float MOVE_DAMPING = 10f;
  /** Max horizontal speed */
  protected static final float MAX_X_SPEED = 4f;
  /** Max vertical speed */
  protected static final float MAX_Y_SPEED = 12f;
  /** Min jump duration */
  protected static final int MIN_JUMP_DURATION = 6;
  /** Max jump duration */
  protected static final int MAX_JUMP_DURATION = 12;
  /** Dash duration */
  protected static final int DASH_DURATION = 40; // keep this above 10
  /** Dash speed */
  protected static final float DASH_SPEED = 15f;
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
  public static final int NUM_STATES = 10;
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

  /** Fixture definition */
  protected FixtureDef def1, def2;
  /** Current fixture index */
  protected int mode;
  /** Fixture */
  protected Fixture fixture;
  /** Player dimensions */
  protected Vector2 dim;
  /** Current horizontal movement of the character */
  protected float movement;
  /** Horizontal dash direction of the player (-1 for left, 1 for right) [different than dir] */
  protected int hdir;
  /** Vertical direction of the player (-1 for down, 1 for up) */
  protected int vdir;
  /** Ground sensor for the player */
  protected GroundSensor groundSensor;

  /** Whether the player is currently alive */
  protected boolean alive;
  /** Whether the player is currently on a platform */
  protected boolean grounded;
  /** Current dash length so far */
  protected int dashTime;
  /** Dash cooldown */
  protected int dashCooldown;
  /** Dash direction */
  protected int[] dashDir;
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
   * @param w width
   * @param h height
   * @param numStates number of entity states
   */
  public Player(float x, float y, float w, float h, int numStates) {
    super(x, y, numStates);
    dim = new Vector2(w, h);

    mode = 0;
    def1 = new FixtureDef();
    PolygonShape shape1 = new PolygonShape();
    shape1.setAsBox(dim.x / 2, dim.y / 2);
    def1.shape = shape1;
    def1.friction = FRICTION;
    def1.density = DENSITY;
    
    def2 = new FixtureDef();
    CircleShape shape2 = new CircleShape();
    shape2.setRadius(dim.x);
    def2.shape = shape2;
    def2.friction = FRICTION;
    def2.density = DENSITY;
    
    groundSensor = new GroundSensor(this, dim);
    jumpTime = 0;
    jumpDuration = 0;
    dashTime = 0;
    dashCooldown = 0;
    enemiesHit = new Array();
    alive = true;
    hdir = 0;
    vdir = 0;
    dashDir = new int[2];
  }

  @Override
  public boolean activatePhysics(World world) {
    if (!super.activatePhysics(world)) {
      return false;
    }
    fixture = body.createFixture(def1);
    fixture.setUserData(this);
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
   * Tries to start a player dash.
   */
  public void tryDash() {
    if (dashTime <= 0 && dashCooldown <= 0 && !isAttacking()) {
      dashTime = DASH_DURATION;
      dashDir[0] = vdir == 0 ? super.dir : hdir;
      dashDir[1] = vdir;
      setState(STATE_DASH);
    }
  }

  /**
   * Tries to start a player attack.
   */
  public void tryAttack() {
    if (!isAttacking()) {
      body.destroyFixture(fixture);
      if (mode == 0) {
        fixture = body.createFixture(def2);
        fixture.setUserData(this);
        mode = 1;
      } else {
        fixture = body.createFixture(def1);
        fixture.setUserData(this);
        mode = 0;
      }

      if (isGrounded()) {
        if (vdir > 0) {
          setState(STATE_UP_GND_ATTACK);
        } else {
          setState(STATE_GND_ATTACK);
        }
      }
      else {
        if (vdir > 0) {
          setState(STATE_UAIR_ATTACK);
        } else if (vdir < 0) {
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
  public void setGrounded(boolean value) { grounded = value; }
  
  /**
   * Returns whether the player is attacking.
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
   * Sets the player's horizontal movement.
   */
  public void setHorizontal(int horizontal) {
    movement = hdir = horizontal;
  }

  /**
   * Sets the player's vertical direction.
   */
  public void setVertical(int vertical) {
    vdir = vertical;
  }

  /**
   * Tries to move the player by the horizontal movement
   */
  public void tryMove() {
    if (movement != 0) {
      if (!isAttacking()) {
        if (movement > 0) {
          faceRight();
        } else if (movement < 0) {
          faceLeft();
        }
      }
    }
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_GND_ATTACK:
    case STATE_UP_GND_ATTACK:
      if (getState().done) {
        enemiesHit.clear();
        setState(movement != 0 ? STATE_WALK : STATE_IDLE);
      }
      break;
    case STATE_AIR_ATTACK:
    case STATE_UAIR_ATTACK:
    case STATE_DAIR_ATTACK:
      if (isGrounded()) {
        enemiesHit.clear();
        setState(movement != 0 ? STATE_WALK : STATE_IDLE);
      } else if (getState().done) {
        enemiesHit.clear();
        setState(STATE_FALL);
      }
      break;
    case STATE_DASH:
      if (getState().done) {
        dashTime = 0;
        dashCooldown = DASH_DURATION;
        if (isGrounded()) {
          setState(movement != 0 ? STATE_WALK : STATE_IDLE);
        } else {
          setState(STATE_FALL);
        }
      }
      break;
    case STATE_JUMP:
      if (jumpTime >= jumpDuration) {
        setState(STATE_FALL);
        jumpTime = jumpDuration = 0;
      }
      if (isGrounded()) {
        setState(movement != 0 ? STATE_WALK : STATE_IDLE);
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

    float vx = 0;
    float vy = 0;
    if (dashTime <= 0) {
      if (movement != 0) {
        temp.set(MOVE_IMPULSE * movement, 0);
        body.applyLinearImpulse(temp, getPosition(), true);
        vx = Math.min(MAX_X_SPEED, Math.max(-MAX_X_SPEED, getVelocity().x));
      } else {
        // damping
        temp.set(-MOVE_DAMPING * getVelocity().x, 0);
        body.applyForce(temp, getPosition(), true);
        vx = Math.min(MAX_X_SPEED, Math.max(-MAX_X_SPEED, getVelocity().x));
      }

      if (jumpTime < jumpDuration) {
        temp.set(0, JUMP_IMPULSE);
        body.applyLinearImpulse(temp, getPosition(), true);
        jumpTime++;
      }
      vy = MathUtils.clamp(getVelocity().y, -MAX_Y_SPEED, MAX_Y_SPEED);

      if (!grounded) {
        vx -= vx / MOVE_DAMPING;
      }
    } else {
      int hDirection = dashDir[0];
      int vDirection = dashDir[1];
      float factor = Math.abs(hDirection) == 1 && Math.abs(vDirection) == 1
        ? (float) Math.sqrt(2)/2 : 1;
      vx = factor * hDirection * DASH_SPEED;
      vy = factor * vDirection * DASH_SPEED;
      dashTime--;
    }

    if (dashCooldown > 0) {
      dashCooldown--;
    }

    body.setLinearVelocity(vx, vy);
  }

  @Override
  public void drawPhysics(GameCanvas canvas) {
    Vector2 pos = getPosition();
    if (mode == 0) {
      canvas.drawPhysics((PolygonShape)fixture.getShape(), Color.RED,
                         pos.x, pos.y, 0);
    } else {
      canvas.drawPhysics((CircleShape)fixture.getShape(), Color.RED,
                         pos.x, pos.y);
    }
    canvas.drawPhysics(groundSensor.getShape(), Color.RED,
                       pos.x, pos.y, 0);
    if (isHitboxActive()) {
      CircleShape shape = new CircleShape();
      pos = getPosition().add(temp.set(ATTACK_POS).scl(getDirection(), 1));
      shape.setPosition(pos);
      shape.setRadius(ATTACK_SIZE);
      canvas.drawPhysics(shape, Color.RED, pos.x, pos.y);
    }
  }
}
