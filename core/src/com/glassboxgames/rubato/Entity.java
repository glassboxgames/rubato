package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

/**
 * Abstract class representing any entity (i.e. collision-experiencing object).
 */
public abstract class Entity {
  /** Position of the entity */
  public Vector2 pos;
  /** Velocity of the entity */
  public Vector2 vel;
  /** Size of this entity */
  public Vector2 dim;
  /** Temporary vector for calculations */
  private Vector2 temp = new Vector2();

  /** Scaling factor for velocity updates */
  private float VELOCITY_SCALE = 400;

  /**
   * Updates this entity.
   * @param delta time since the last update
   */
  public void update(float delta) {
    temp.set(vel).scl((int)(delta * VELOCITY_SCALE));
    pos.add(temp);
  }
}
