package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.utils.Array;
import com.glassboxgames.rubato.GameCanvas;

public class Wisp extends Enemy {
  /** Wisp states */
  public static Array<State> states = null;
  /** Wisp state constants */
  private static final int STATE_IDLE = 0;
  private static final int STATE_WINDUP = 1;
  private static final int STATE_ATTACK = 2;

  /** Wisp attributes */
  private static final float MAX_HEALTH = 1f;
  /** Cooldown between attacks */
  private static final int ATTACK_COOLDOWN = 30;
  /** Projectile offset */
  private static final Vector2 PROJECTILE_OFFSET = new Vector2(0.2f, 0.2f);
  /** Projectile velocity */
  private static final Vector2 PROJECTILE_VELOCITY = new Vector2(3f, 0f);
  /** Projectile life */
  private static final int PROJECTILE_LIFE = 60;

  /** Array of spawned projectiles */
  private Array<Enemy> spawned;
  /** Temp vector for calculations */
  private Vector2 temp;

  /**
   * Initializes a wisp enemy with the given parameters.
   * @param x x coordinate
   * @param y y coordinate
   */
  public Wisp(float x, float y) {
    super(x, y, STATE_IDLE);
    spawned = new Array<Enemy>();
    temp = new Vector2();
    bodyDef.type = BodyDef.BodyType.StaticBody;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_WINDUP:
      float delta = getTarget().x - getPosition().x;
      if (delta > 0) {
        faceRight();
      } else if (delta < 0) {
        faceLeft();
      }
      break;
    case STATE_ATTACK:
      Vector2 pos = getPosition().add(temp.set(PROJECTILE_OFFSET).scl(getDirection(), 1));
      Vector2 vel = temp.set(PROJECTILE_VELOCITY).scl(getDirection(), 1);
      spawned.add(new Projectile(pos.x, pos.y, vel, PROJECTILE_LIFE));
      break;
    }
  }
  
  @Override
  public void advanceState() {
    super.advanceState();
    switch (stateIndex) {
    case STATE_IDLE:
      if (getTarget() != null && getCount() >= ATTACK_COOLDOWN) {
        setState(STATE_WINDUP);
      }
      break;
    case STATE_WINDUP:
      if (getCount() >= getState().getLength()) {
        setState(STATE_ATTACK);
      }
      break;
    case STATE_ATTACK:
      if (getCount() >= getState().getLength()) {
        setState(STATE_IDLE);
      }
      break;
    }
  }

  /**
   * Returns the array of spawned projectiles.
   */
  public Array<Enemy> getSpawned() {
    return spawned;
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }
}
