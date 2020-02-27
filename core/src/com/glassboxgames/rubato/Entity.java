package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.glassboxgames.util.*;
/**
 * Abstract class representing a rectangular entity (i.e. collision-experiencing object).
 */
public abstract class Entity {
  /** Position of the entity */
  public Vector2 pos;
  /** Size of this entity */
  public Vector2 dim;
  /** Temporary vector for calculations */
  private Vector2 temp = new Vector2();

  /** CURRENT image for this object. May change over time. */
  protected FilmStrip animator;

  /** Scaling factor for velocity updates */
  private float VELOCITY_SCALE = 400;

  /**
   * Instantiates a new entity with the given parameters.
   * @param x the x-coordinate
   * @param y the y-coordinate
   * @param w width
   * @param h height
   */
  public Entity(float x, float y, float w, float h) {
    pos = new Vector2(x, y);
    dim = new Vector2(w, h);
  }
    
  /**
   * Updates this entity.
   * @param delta time since the last update
   */
  public abstract void update(float delta);
  
  public void setTexture(Texture texture) {
    animator = new FilmStrip(texture,1,1,1);
  }

  public Texture getTexture() {
    return animator == null ? null : animator.getTexture();
  }
}
