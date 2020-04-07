package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a simple rectangular platform in Rubato.
 */
public class Platform extends Entity {
  /** Platform states */
  public static Array<State> states = null;

  /**
   * Initializes a platform with the specified parameters.
   * @param x x-coordinate of lower left corner
   * @param y y-coordinate of lower left corner
   */
  public Platform(float x, float y) {
    super(x, y);
    bodyDef.type = BodyDef.BodyType.StaticBody;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }
}
