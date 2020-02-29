package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
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
  protected static final float DENSITY = 1f;
  /** Friction */
  protected static final float FRICTION = 0f;
  /** Jump impulse */
  protected static final float JUMP_IMPULSE = 2f;
  /** Movement impulse */
  protected static final float MOVE_IMPULSE = 1f;
  /** Horizontal damping */
  protected static final float MOVE_DAMPING = 5f;
  /** Max horizontal speed */
  protected static final float MAX_X_SPEED = 3.5f;
  /** Max vertical speed */
  protected static final float MAX_Y_SPEED = 8f;
  /** Min jump duration */
  protected static final int MIN_JUMP_DURATION = 3;
  /** Max jump duration */
  protected static final int MAX_JUMP_DURATION = 10;
  /** Attack duration */
  protected static final int ATTACK_DURATION = 30;
  /** Attack cooldown */
  protected static final int ATTACK_COOLDOWN = 60;
  /** Ground sensor height */
  protected static final float SENSOR_HEIGHT = 0.05f;
  /** Name of the player */
  protected static final String PLAYER_NAME = "Player";
  /** Name of the ground sensor */
  protected static final String SENSOR_NAME = "GroundSensor";
  
  /** Current animation frame */
  protected float animFrame;
  /** Current animation filmstrip length */
  protected int totalFrames;
  /** Direction the player is facing (1 for right, -1 for left) */
  protected int dir;
  /** Player dimensions */
  protected Vector2 dim;
  /** Current horizontal movement of the character (from the input) */
  protected float movement;

  /** Ground sensor fixture definition */
  protected FixtureDef sensorDef;
  /** Ground sensor fixture */
  protected Fixture sensorFixture;

  /** Whether the player is currently on a platform */
  protected boolean isGrounded;
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
    dim = new Vector2(w / Constants.PPM, h / Constants.PPM);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(dim.x / 2, dim.y / 2);
    fixtureDef.shape = shape;
    fixtureDef.friction = FRICTION;
    fixtureDef.density = DENSITY;
    sensorDef = new FixtureDef();
    PolygonShape sensorShape = new PolygonShape();
    sensorShape.setAsBox(dim.x / 2, SENSOR_HEIGHT, temp.set(dim).scl(-0.5f), 0);
    sensorDef.isSensor = true;
    sensorDef.shape = sensorShape;
    
    animFrame = 0;
    totalFrames = 0;
    dir = 1;
    jumpTime = 0;
    jumpDuration = 0;
    attackTime = 0;
    attackCooldown = 0;
  }

  @Override
  public boolean activatePhysics(World world) {
    if (!super.activatePhysics(world)) {
      return false;
    }
    fixture.setUserData(PLAYER_NAME);
    sensorFixture = body.createFixture(sensorDef);
    sensorFixture.setUserData(SENSOR_NAME);
    return true;
  }

  /**
   * Tries to start a player jump or extend an existing jump.
   */
  public void tryJump() {
    if (jumpDuration > 0 && jumpDuration < MAX_JUMP_DURATION) {
      jumpDuration++;
    } else if (isGrounded) {
      jumpDuration = MIN_JUMP_DURATION;
    }
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
   * Returns whether the player is standing on a platform.
   */
  public boolean isGrounded() {
    return isGrounded;
  }

  /**
   * Sets the player's grounded state.
   */
  public void setGrounded(boolean value) {
    System.out.println(value);
    isGrounded = value;
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
      body.applyLinearImpulse(temp, getPosition(), true);
    } else {
      // damping
      temp.set(-MOVE_DAMPING * getVelocity().x, 0);
      body.applyForce(temp, getPosition(), true);
    }

    if (jumpTime < jumpDuration) {
      jumpTime++;
      temp.set(0, JUMP_IMPULSE);
      body.applyLinearImpulse(temp, getPosition(), true);
    } else if (jumpDuration > 0) {
      jumpTime = jumpDuration = 0;
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

  @Override
  public void drawPhysics(GameCanvas canvas) {
    canvas.drawPhysics((PolygonShape)fixture.getShape(), Color.RED,
                       getPosition().x, getPosition().y, 0,
                       Constants.PPM, Constants.PPM);
    canvas.drawPhysics((PolygonShape)sensorFixture.getShape(), Color.RED,
                       getPosition().x + dim.x / 2, getPosition().y, 0,
                       Constants.PPM, Constants.PPM);
  }
}
