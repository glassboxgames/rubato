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
  private static final int STATE_RETURN = 3;

  /** Wyrm states */
  public static Array<State> states = null;

  /** Dive speed */
  private static final float DIVE_SPEED = 6f;
  /** Return speed */
  private static final float RETURN_SPEED = 4.5f;
  /** Return threshold */
  private static final float THRESHOLD = 0.1f;
  /** Max health */
  private static final float MAX_HEALTH = 1f;
  /** Initial coordinates */
  private Vector2 initPos;
  /** Dive target position */
  private Vector2 divePos;
  /** Temp calculation vector */
  private Vector2 temp;

  /**
   * Instantiates a wyrm enemy with the given parameters.
   */
  public Wyrm(float x, float y) {
    super(x, y, STATE_IDLE);
    bodyDef.type = BodyDef.BodyType.KinematicBody;
    initPos = new Vector2();
    divePos = new Vector2();
    temp = new Vector2();
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_IDLE:
      if (getTarget() != null) {
        setState(STATE_WINDUP);
      }
      break;
    case STATE_WINDUP:
      if (getCount() >= getState().getLength()) {
        setState(STATE_ATTACK);
      }
      break;
    case STATE_ATTACK:
      if (getPosition().dst(divePos) < THRESHOLD) {
        setState(STATE_RETURN);
      }
      break;
    case STATE_RETURN:
      if (getPosition().dst(initPos) < THRESHOLD) {
        setState(STATE_IDLE);
      }
      break;
    }
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_IDLE:
      body.setLinearVelocity(0, 0);
      break;
    case STATE_WINDUP:
      divePos.set(getTarget());
      initPos.set(getPosition());
      if (divePos.x > initPos.x) {
        faceRight();
      } else {
        faceLeft();
      }
      break;
    case STATE_ATTACK:
      body.setLinearVelocity(temp.set(divePos).sub(initPos).setLength(DIVE_SPEED));
      break;
    case STATE_RETURN:
      body.setLinearVelocity(temp.set(initPos).sub(getPosition()).setLength(RETURN_SPEED));
      break;
    }
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }
}
