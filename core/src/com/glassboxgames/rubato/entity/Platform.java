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
  public static final int TYPE_TB_FOREST = 0;
  public static final int TYPE_T_FOREST = 1;
  public static final int TYPE_M_FOREST = 2;
  public static final int TYPE_B_FOREST = 3;
  public static final int TYPE_TB_PLAINS = 4;
  public static final int TYPE_T_PLAINS = 5;
  public static final int TYPE_M_PLAINS = 6;
  public static final int TYPE_B_PLAINS = 7;
  public static final int TYPE_TB_DESERT = 8;
  public static final int TYPE_T_DESERT = 9;
  public static final int TYPE_M_DESERT = 10;
  public static final int TYPE_B_DESERT = 11;
  public static final int TYPE_TB_MOUNTAINS = 12;
  public static final int TYPE_T_MOUNTAINS = 13;
  public static final int TYPE_M_MOUNTAINS = 14;
  public static final int TYPE_B_MOUNTAINS = 15;
  
  public static final int TYPE_B_WOOD_SPIKES = 16;
  public static final int TYPE_L_WOOD_SPIKES = 17;
  public static final int TYPE_T_WOOD_SPIKES = 18;
  public static final int TYPE_R_WOOD_SPIKES = 19;
  public static final int TYPE_B_STONE_SPIKES = 20;
  public static final int TYPE_L_STONE_SPIKES = 21;
  public static final int TYPE_T_STONE_SPIKES = 22;
  public static final int TYPE_R_STONE_SPIKES = 23;
  public static final int TYPE_CRUMBLING = 24;
  
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
    return states = State.readStates("Platforms/");
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
