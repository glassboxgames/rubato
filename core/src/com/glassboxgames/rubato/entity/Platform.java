package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a platform object in Rubato.
 */
public class Platform extends Entity {
  /** Type enum */
  public enum Type {
    FOREST_C_T, FOREST_C_M, FOREST_C_B, FOREST_C_TB,
    FOREST_L_T, FOREST_L_M, FOREST_L_B, FOREST_L_TB,
    FOREST_R_T, FOREST_R_M, FOREST_R_B, FOREST_R_TB,

    PLAINS_C_T, PLAINS_C_M, PLAINS_C_B, PLAINS_C_TB,
    PLAINS_L_T, PLAINS_L_M, PLAINS_L_B, PLAINS_L_TB,
    PLAINS_R_T, PLAINS_R_M, PLAINS_R_B, PLAINS_R_TB,

    DESERT_C_T, DESERT_C_M, DESERT_C_B, DESERT_C_TB,
    DESERT_L_T, DESERT_L_M, DESERT_L_B, DESERT_L_TB,
    DESERT_R_T, DESERT_R_M, DESERT_R_B, DESERT_R_TB,

    MOUNTAINS_C_T, MOUNTAINS_C_M, MOUNTAINS_C_B, MOUNTAINS_C_TB,
    MOUNTAINS_L_T, MOUNTAINS_L_M, MOUNTAINS_L_B, MOUNTAINS_L_TB,
    MOUNTAINS_R_T, MOUNTAINS_R_M, MOUNTAINS_R_B, MOUNTAINS_R_TB,
  
    B_WOOD_SPIKES, L_WOOD_SPIKES, T_WOOD_SPIKES, R_WOOD_SPIKES,
    B_STONE_SPIKES, L_STONE_SPIKES, T_STONE_SPIKES, R_STONE_SPIKES,

    CRUMBLING,
  }

  /** Number of frames for a crumbling block to crumble */
  public static final int CRUMBLING_TIME = 90;
  
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
    if (stateIndex == Type.CRUMBLING.ordinal()) {
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
