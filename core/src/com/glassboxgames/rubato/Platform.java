package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.util.*;

/**
 * Class representing a simple rectangular platform in Rubato.
 */
public class Platform extends Entity {
  /** Friction */
  protected static final float FRICTION = 0f;
  /** Platform name */
  protected static final String PLATFORM_NAME = "Platform";
  
  /**
   * Initializes a platform with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   */
  public Platform(float x, float y, float w, float h) {
    super(x, y, PLATFORM_NAME);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(w / 2, h / 2);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    fixtureDef.shape = shape;
    fixtureDef.friction = FRICTION;
  }
}
