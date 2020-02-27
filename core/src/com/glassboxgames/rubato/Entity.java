package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.glassboxgames.util.*;
/**
 * Abstract class representing a rectangular textured entity.
 */
public abstract class Entity {
  /** Default animation speed */
  private static float DEFAULT_ANIMATION_SPEED = 0.25f;

  /** Position of the entity */
  public Vector2 pos;
  /** Temporary vector for calculations */
  private Vector2 temp = new Vector2();

  /** CURRENT image for this object. May change over time. */
  private FilmStrip animator;
  /** Current animation frame */
  private float animFrame;
  /** Speed of animation for this entity */
  private float animSpeed;
  /** Total frames for current animation */
  private int totalFrames;

  /**
   * Instantiates a new entity with the given parameters.
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  public Entity(float x, float y) {
    pos = new Vector2(x, y);
    animator = null;
    animFrame = 0;
    animSpeed = DEFAULT_ANIMATION_SPEED;
    totalFrames = 0;
  }
    
  /**
   * Updates this entity's animation frame.
   * @param delta time since the last update
   */
  public void update(float delta) {
    animFrame += animSpeed;
    if (animFrame >= totalFrames) {
      animFrame -= totalFrames;
    }
    animator.setFrame((int)animFrame);
  }

  /**
   * Sets a static texture for this entity.
   * @param texture the texture to set
   */
  public void setTexture(Texture texture) {
    setTexture(texture, 1, 1, 1);
  }

  /**
   * Sets the texture with the given filmstrip parameters.
   * @param texture the texture to set
   * @param rows number of rows in filmstrip
   * @param cols number of columns in filmstrip
   * @param size number of frames in filmstrip
   */
  public void setTexture(Texture texture, int rows, int cols, int size) {
    FilmStrip newAnimator = new FilmStrip(texture, rows, cols, size);
    if (animator == null || !texture.equals(animator.getTexture())) {
      animFrame = 0;
      totalFrames = size;
      animator = newAnimator;
    }
  }

  /**
   * Returns the texture for this entity.
   */
  public Texture getTexture() {
    return animator == null ? null : animator.getTexture();
  }

  /**
   * Returns the current filmstrip for this entity.
   */
  public FilmStrip getFilmStrip() {
    return animator;
  }
}
