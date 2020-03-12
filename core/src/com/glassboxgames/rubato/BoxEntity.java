package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

/**
 * Abstract class representing a box-shaped entity.
 */
public abstract class BoxEntity extends Entity {
  /** Fixture definition */
  protected FixtureDef fixtureDef;
  /** Fixture */
  protected Fixture fixture;
  /** Dimensions */
  protected Vector2 dim;

  /** Cache for dimensions */
  protected Vector2 dimCache = new Vector2(0, 0);

  /**
   * Instantiate a BoxEntity with the given parameters.
   * @param x x-coordinate of center
   * @param y y-coordinate of center
   * @param w width
   * @param h height
   * @param numStates number of entity states
   */
  public BoxEntity(float x, float y, float w, float h, int numStates) {
    super(x, y, numStates);
    dim = new Vector2(w, h);

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(dim.x / 2, dim.y / 2);
    fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
  }

  @Override
  public boolean activatePhysics(World world) {
    if (!super.activatePhysics(world)) {
      return false;
    }
    fixture = body.createFixture(fixtureDef);
    fixture.setUserData(this);
    return true;
  }

  /**
   * Returns a copy of the dimension vector.
   */
  public Vector2 getDimensions() {
    return dimCache.set(dim);
  }

  @Override
  public void drawPhysics(GameCanvas canvas) {
    canvas.drawPhysics((PolygonShape)fixture.getShape(), Color.RED,
                       getPosition().x, getPosition().y, 0);
  }
}
