package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a platform object in Rubato.
 */
public class Platform extends Entity {
  /** Type indices */
  public static final int TYPE_SIMPLE = 0;
  public static final int TYPE_BOTTOM_SPIKES = 1;
  public static final int TYPE_LEFT_SPIKES = 2;
  public static final int TYPE_TOP_SPIKES = 3;
  public static final int TYPE_RIGHT_SPIKES = 4;
  public static final int TYPE_CRUMBLING = 5;
  public static final int TYPE_PLAINS = 6;
  public static final int TYPE_FOREST = 7;
  public static final int TYPE_MOUNTAIN = 8;
  public static final int TYPE_DESERT = 9;

  
  /** Number of frames for a crumbling block to crumble */
  public static final int CRUMBLING_TIME = 60;
  
  /** Platform states (with one state per type) */
  public static Array<State> states = null;

  /** Whether this platform has been visited */
  private boolean visited;
  /** Whether this platform should be removed */
  private boolean remove;
  
  /**
   * Initializes a platform with the specified parameters.
   * @param x x-coordinate of lower left corner
   * @param y y-coordinate of lower left corner
   * @param type the type index of the platform
   */
  public Platform(float x, float y, int type) {
    super(x, y, type);
    bodyDef.type = BodyDef.BodyType.StaticBody;
  }

  /**
   * Initializes platform states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Platforms/Grass/");
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void update(float delta) {
    if (stateIndex == TYPE_CRUMBLING) {
      super.update(delta, visited ? (float)getState().getLength() / CRUMBLING_TIME : 0);
      if (getCount() >= getState().getLength()) {
        remove = true;
      }
    } else {
      super.update(delta);
    }
  }

  /**
   * Returns whether this platform should be removed.
   * Currently only applicable for crumbling blocks.
   */
  public boolean shouldRemove() {
    return remove;
  }

  /**
   * Sets this platform as visited.
   */
  public void visit() {
    visited = true;
  }
}
