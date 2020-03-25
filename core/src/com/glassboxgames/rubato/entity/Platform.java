package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.rubato.GameCanvas;

/**
 * Class representing a simple rectangular platform in Rubato.
 */
public class Platform extends BoxEntity {
  /** Friction */
  protected static final float FRICTION = 0f;

  /** Dimensions for single tile of platform */
  protected Vector2 baseDim;
  
  /**
   * Initializes a platform with the specified parameters.
   * @param x x-coordinate of lower left corner
   * @param y y-coordinate of lower left corner
   * @param w width
   * @param h height
   * @param baseW width for single tile
   * @param baseH height for single tile
   */
  public Platform(float x, float y, float w, float h, float baseW, float baseH) {
    super(x, y, w, h);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    fixtureDef.friction = FRICTION;
    baseDim = new Vector2(baseW, baseH);
  }

  @Override
  public void draw(GameCanvas canvas) {
    Vector2 pos = getPosition();
    float w = baseDim.x;
    float h = baseDim.y;
    for (float x = -dim.x / 2; x < dim.x / 2; x += baseDim.x) {
      for (float y = -dim.y / 2; y < dim.y / 2; y += baseDim.y) {
        canvas.draw(getState().getTexture(), Color.WHITE,
                    0, 0,
                    pos.x + x, pos.y + y,
                    w, h);
      }
    }
  }
}
