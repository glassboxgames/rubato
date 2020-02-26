package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

/**
 * Abstract class representing any entity (i.e. collision-experiencing object).
 */
public abstract class Entity {
  /** Position of the entity */
  public Vector2 pos;
  /** Size of this entity */
  public Vector2 dim;

  /**
   * Updates this entity.
   * @param delta time since the last update
   */
  public abstract void update(float delta);
}
