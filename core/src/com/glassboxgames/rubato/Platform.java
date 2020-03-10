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
  /** Dimensions for entire platform */
  protected Vector2 dim;
  /** Dimensions for single tile of platform */
  protected Vector2 baseDim;
  
  /**
   * Initializes a platform with the specified parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param w width
   * @param h height
   * @param baseW width for single tile
   * @param baseH height for single tile
   */
  public Platform(float x, float y, float w, float h, float baseW, float baseH) {
    super(x, y, 1);
    dim = new Vector2(w, h);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(dim.x / 2, dim.y / 2, new Vector2(dim).scl(0.5f), 0f);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    fixtureDef.shape = shape;
    fixtureDef.friction = FRICTION;
    baseDim = new Vector2(baseW, baseH);
  }

  @Override
  public void draw(GameCanvas canvas) {
    Vector2 pos = getPosition();
    for (float x = pos.x; x < pos.x + dim.x; x += baseDim.x) {
      for (float y = pos.y; y < pos.y + dim.y; y += baseDim.y) {
        canvas.draw(getState().filmStrip, Color.WHITE, 0, 0, x, y, baseDim.x, baseDim.y);
      }
    }
  }
}
