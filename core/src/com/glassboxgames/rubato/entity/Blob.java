package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;

public class Blob extends Enemy {
  /** Blob state variables */
  public static final int STATE_IDLE_UP = 0;
  public static final int STATE_IDLE_DOWN = 1;
  /** Blob state array */
  public static Array<State> states = null;
  /** Health */
  private static final float MAX_HEALTH = 1f;
  /** Bobbing velocity */
  private static final float BOB_SPEED = 1f;
  /** Number of frames per bob segment */
  private static final int BOB_FRAMES = 60;

  /**
   * Initializes a blob enemy with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Blob(float x, float y) {
    super(x, y, STATE_IDLE_UP);
    bodyDef.type = BodyDef.BodyType.KinematicBody;
  }

  /**
   * Initializes blob states.
   */
  public static Array<State> initStates() {
    states = State.readStates("Enemies/Blob/");
    return states;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void advanceState(){
    switch (stateIndex) {
    case STATE_IDLE_UP:
      if (getCount() >= BOB_FRAMES) {
        setState(STATE_IDLE_DOWN);
      }
      break;
    case STATE_IDLE_DOWN:
      if (getCount() >= BOB_FRAMES) {
        setState(STATE_IDLE_UP);
      }
    }
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case (STATE_IDLE_UP):
      body.setLinearVelocity(0, BOB_SPEED);
      break;
    case (STATE_IDLE_DOWN):
      body.setLinearVelocity(0, -BOB_SPEED);
      break;
    }
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }
}
