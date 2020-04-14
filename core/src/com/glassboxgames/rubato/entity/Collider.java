package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;
import com.glassboxgames.util.*;

/**
 * Class used to provide metadata during collision detection.
 */
public class Collider {
  /** The possible collider types */
  public enum Type {
    HITBOX,
    HURTBOX,
    GROUND,
    EDGE,
    VISION,
    FORWARD,
    UP,
    DOWN,
    CENTER,
  }

  /** The entity involved in the collision */
  public Entity entity;
  /** The fixture of the entity that collided */
  public Fixture fixture;
  /** The type of the involved collider */
  public Type type;

  /**
   * Instantiates a collider object.
   * @param entity
   * @param fixture
   * @param type
   */
  public Collider(Entity entity, Fixture fixture, Type type) {
    this.entity = entity;
    this.fixture = fixture;
    this.type = type;
  }

  /**
   * Returns the entity associated with this collider.
   */
  public Entity getEntity() {
    return entity;
  }
  
  /**
   * Returns the fixture for this collider.
   */
  public Fixture getFixture() {
    return fixture;
  }

  /**
   * Returns whether this collider is a hitbox.
   */
  public boolean isHitbox() {
    return type == Type.HITBOX;
  }

  /**
   * Returns whether this collider is a hurtbox.
   */
  public boolean isHurtbox() {
    return type == Type.HURTBOX;
  }

  /**
   * Returns whether this collider is a ground sensor.
   */
  public boolean isGroundSensor() {
    return type == Type.GROUND;
  }

  /**
   * Returns whether this collider is a forward sensor.
   */
  public boolean isForwardSensor() {
    return type == Type.FORWARD;
  }

  /**
   * Returns whether this collider is an up sensor.
   */
  public boolean isUpSensor() {
    return type == Type.UP;
  }

  /**
   * Returns whether this collider is a down sensor.
   */
  public boolean isDownSensor() {
    return type == Type.DOWN;
  }

  /**
   * Returns whether this collider is a center sensor.
   */
  public boolean isCenterSensor() {
    return type == Type.CENTER;
  }

  /**
   * Returns whether this collider is an edge sensor.
   */
  public boolean isEdgeSensor() {
    return type == Type.EDGE;
  }

  /**
   * Returns whether this collider is a vision sensor.
   */
  public boolean isVisionSensor() {
    return type == Type.VISION;
  }
}
  
