package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;

public class Projectile extends Enemy {
  /** Projectile state variables */
  public static final int STATE_ACTIVE = 0;
  public static final int STATE_SUSPENDED = 1;
  /** Projectile state array */
  public static Array<State> states = null;

  /** Health of the projectile */
  private static final float MAX_HEALTH = 1f;

  /** Projectile velocity */
  private Vector2 velocity;
  /** Life of the projectile in frames */
  private int life;

  /**
   * Initializes a projectile with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param velocity velocity of projectile
   * @param life life of projectile
   */
  public Projectile(float x, float y, Vector2 velocity, int life) {
    super(x, y, STATE_ACTIVE);
    this.velocity = new Vector2(velocity);
    this.life = life;
    bodyDef.gravityScale = 0f;
  }

  /**
   * Initializes projectile states.
   */
  public static Array<State> initStates() {
    states = State.readStates("Enemies/Projectile/");
    return states;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_ACTIVE:
      body.setLinearVelocity(velocity);
      break;
    }
  }

  @Override
  public void advanceState() {
    super.advanceState();
    switch (stateIndex) {
    case STATE_ACTIVE:
      if (isSuspended()) {
        setState(STATE_SUSPENDED);
        break;
      }
    }
  }
    
  @Override
  public void update(float delta) {
    super.update(delta);
    if (!isSuspended() && getCount() >= life) {
      setRemove(true);
    }
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }
}
