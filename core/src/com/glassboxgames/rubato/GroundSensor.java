package com.glassboxgames.rubato;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.glassboxgames.rubato.entity.Player;

/**
 * Class representing a ground sensor for an player.
 */
public class GroundSensor {
  /** Sensor height */
  protected static final float SENSOR_HEIGHT = 0.02f;
  /** Sensor width factor */
  protected static final float SENSOR_WIDTH_FACTOR = 0.5f;

  /** Player to which this ground sensor is attached */
  protected Player player;
  /** Relative position for this sensor */
  protected Vector2 pos;
  /** Fixture definition for the sensor */
  protected FixtureDef fixtureDef;
  /** Fixture for the sensor */
  protected Fixture fixture;

  /**
   * Instantiates a ground sensor for the given player.
   * @param player player to attach to
   * @param dim bounding box for the player
   */
  public GroundSensor(Player p, Vector2 dim) {
    player = p;
    pos = new Vector2(0, -dim.y / 2 + SENSOR_HEIGHT);
    fixtureDef = new FixtureDef();
    fixtureDef.isSensor = true;
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(dim.x / 2 * SENSOR_WIDTH_FACTOR, SENSOR_HEIGHT, pos, 0);
    fixtureDef.shape = shape;
  }

  /**
   * Adds the fixture for this sensor to Box2D.
   * @return whether the fixture was created successfully
   */
  public boolean activatePhysics() {
    fixture = player.getBody().createFixture(fixtureDef);
    fixture.setUserData(this);
    return true;
  }

  /**
   * Returns the player for this sensor.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the shape of this sensor.
   */
  public PolygonShape getShape() {
    return (PolygonShape)fixture.getShape();
  }
}
