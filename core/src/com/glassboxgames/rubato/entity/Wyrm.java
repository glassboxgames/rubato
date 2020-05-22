package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.utils.Array;
import com.glassboxgames.rubato.GameCanvas;

public class Wyrm extends Enemy {
  /** Wyrm state constants */
  private static final int STATE_IDLE = 0;
  private static final int STATE_WINDUP = 1;
  private static final int STATE_ATTACK = 2;

  /** Wyrm states */
  public static Array<State> states = null;

  /** Drift speed */
  private static final float DRIFT_SPEED = 1f;
  /** Attack cooldown */
  private static final int ATTACK_COOLDOWN = 105;
  /** Dive speed */
  private static final float DIVE_SPEED = 7f;
  /** Dive duration */
  private static final int DIVE_DURATION = 90;
  /** Max health */
  private static final float MAX_HEALTH = 1f;

  /** Dive direction */
  private Vector2 diveDir;
  /** Current attack cooldown */
  private int attackCooldown;
  /** Temp calculation vector */
  private Vector2 temp;

  /**
   * Instantiates a wyrm enemy with the given parameters.
   */
  public Wyrm(float x, float y) {
    super(x, y, STATE_IDLE);
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.gravityScale = 0f;
    diveDir = new Vector2();
    temp = new Vector2();
  }

  /**
   * Initializes wyrm states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Enemies/Wyrm/");
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  /**
   * Stops this wyrm's attack.
   */
  public void cancelAttack() {
    if (stateIndex == STATE_ATTACK) {
      setState(STATE_IDLE);
    }
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_IDLE:
      if (getTarget() != null && attackCooldown <= 0) {
        setState(STATE_WINDUP);
      }
      break;
    case STATE_WINDUP:
      if (getCount() >= getState().getLength()) {
        setState(STATE_ATTACK);
      }
      break;
    case STATE_ATTACK:
      if (getCount() > DIVE_DURATION) {
        setState(STATE_IDLE);
      }
      break;
    }
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_ATTACK:
      body.setLinearVelocity(temp.set(diveDir).setLength(DIVE_SPEED));
      break;
    }
  }

  @Override
  public void leaveState() {
    switch (stateIndex) {
    case STATE_ATTACK:
      attackCooldown = ATTACK_COOLDOWN;
      body.setLinearVelocity(0, 0);
      break;
    }
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    if (!isSuspended()) {
      if (attackCooldown > 0) {
        attackCooldown--;
      }

      if (getVelocity().x > 0) {
        faceRight();
      } else if (getVelocity().x < 0) {
        faceLeft();
      }

      if (getTarget() != null) {
        diveDir.set(getTarget()).sub(getPosition()).nor();
        if (stateIndex != STATE_ATTACK) {
          body.setLinearVelocity(temp.set(diveDir).setLength(DRIFT_SPEED));
        }
      }
    }
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }
}
