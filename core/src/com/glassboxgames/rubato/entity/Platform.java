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
  public static final int TYPE_SPIKES = 1;
  public static final int TYPE_CRUMBLING = 2;
  
  /** Platform states (with one state per type) */
  public static Array<State> states = null;

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
  public Array<State> initStates() {
    states = State.readStates("Tilesets/");
    return states;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }
}
