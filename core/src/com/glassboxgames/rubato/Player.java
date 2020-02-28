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
  private static int ATTACK_COOLDOWN = 45;
  
  /** Current animation frame */
  private float animFrame;
  /** Current animation filmstrip length */
  private int totalFrames;
  /** Direction the player is facing (1 for right, -1 for left) */
  private int dir;
  /** Current Horizontal movement of the character (from the input) */
  private float movement;

  /** Is the player currently jumping */
  private boolean isJumping;
  /** Current frame count since jump input */
  private int jumpTime;
  /** Current total jump duration, can be extended; 0 if not jumping */
  private int jumpDuration;
  /** Is the player currently attacking */
  private boolean isAttacking;
  /** Current frame count since attack input */
  private int attackTime;
  /** Current attack cooldown, 0 if not attacking */
  private int attackCooldown;

  /** Shape of the current player */
  private PolygonShape shape;
  /** Body of the current player */
  private Body body;
  /** Fixture of the current player */
  private Fixture fixture;

  /** Temp vector for calculations */
  private Vector2 temp = new Vector2();

  public Player(float x, float y) {
    super(x, y);
    animFrame = 0;
    totalFrames = 0;
    dir = 1;
    jumpTime = 0;
    jumpDuration = 0;
    attackTime = 0;
    attackCooldown = 0;



  }
  public boolean activatePhysics(World world) {

    bodyInfo.type = BodyDef.BodyType.DynamicBody;
    bodyInfo.active = true;
    bodyInfo.fixedRotation = true;
    body = world.createBody(bodyInfo);
    body.setUserData(this);

    if (body != null) {
      shape = new PolygonShape();
      shape.setAsBox(30,80, getPos(), 0);
      fixtureInfo = new FixtureDef();
      fixtureInfo.shape = shape;
      fixture = body.createFixture(fixtureInfo);
      return true;
    }
    bodyInfo.active = false;
    return false;
  }

  /**
   * Tries to start a player jump or extend an existing jump.
   */
  public void setJump() {
    if (jumpDuration > 0 && jumpDuration < MAX_JUMP_DURATION) {
      jumpDuration++;
    } else if (getPos().y <= 0) {
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
   */
  public void setAttack() {
    if (attackCooldown == 0) {
      attackCooldown = ATTACK_COOLDOWN;
    }
  }

  /**
   * Returns whether the player is attacking.
   */
  public boolean isAttacking() {
    return attackCooldown > 0;
  }
  
  /**
   * Gets the player's current facing direction (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  public Vector2 getPos() {
    return (body == null ? super.getPos() : body.getPosition());
  }
  public Vector2 getVel() {
    return (body == null ? super.getVel() : body.getLinearVelocity());
  }
  /**
   * Tries to set the player's horizontal movement.
   */
  public void setMove(float input) {
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
    Vector2 temp = new Vector2(movement+body.getPosition().x,body.getPosition().y);
    body.setTransform(temp, 0);


    /** old jump + movement code
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
     */
  }

  @Override
  public void draw(GameCanvas canvas) {
    canvas.draw(getFilmStrip(), Color.WHITE,
                dim.x * dir / 2, 0,
                getPos().x, getPos().y,
                dim.x * dir, dim.y);
  }
  public void drawPhysics(GameCanvas canvas) {
    canvas.drawPhysics((PolygonShape) fixture.getShape(), Color.RED, getPos().x,getPos().y) ;
  }
}
