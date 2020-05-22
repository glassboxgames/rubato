package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;
import com.glassboxgames.util.*;

/**
 * Abstract class representing a textured entity.
 */
public abstract class Entity {
  /** The body definition for this entity */
  protected BodyDef bodyDef;
  /** The body for this entity */
  protected Body body;
  /** Hitbox colliders for this entity */
  protected Array<Collider> hitboxes;
  /** Hurtbox colliders for this entity */
  protected Array<Collider> hurtboxes;
  /** Sensor colliders for this entity */
  protected ObjectMap<String, Collider> sensors;
  /** Direction the entity is facing (1 for right, -1 for left) */
  protected int dir;
  /** Current entity state, represented as an integer */
  protected int stateIndex;
  /** Initial entity state */
  protected int initIndex;
  /** Number of frames spent in current state */
  protected float count;

  /** Temp vector for calculations */
  protected Vector2 temp = new Vector2();
  /** Cache for position vector */
  private Vector2 posCache = new Vector2();
  /** Cache for velocity vector */
  private Vector2 velCache = new Vector2();

  /**
   * Instantiates a new entity with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   * @param i initial state index
   */
  public Entity(float x, float y, int i) {
    dir = 1;
    initIndex = i;
    stateIndex = -1;
    bodyDef = new BodyDef();
    bodyDef.position.set(x, y);
    bodyDef.active = true;
    bodyDef.awake = true;
    bodyDef.allowSleep = true;
    bodyDef.gravityScale = 1;
    bodyDef.fixedRotation = true;
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    hitboxes = new Array<Collider>();
    hurtboxes = new Array<Collider>();
    sensors = new ObjectMap<String, Collider>();
  }

  /**
   * Returns an array of states for this entity.
   */
  public abstract Array<State> getStates();

  /**
   * Returns the angle.
   */
  public float getAngle() {
    return body == null ? bodyDef.angle : body.getAngle();
  }

  /**
   * Returns the texture.
   */
  public Texture getTexture() {
    return getState().getTexture(getCount());
  }

  /**
   * Returns the position vector. Always returns a copy in the same vector.
   */
  public Vector2 getPosition() {
    return posCache.set(body == null ?
                        bodyDef.position : body.getPosition());
  }

  /**
   * Sets the position of the entity.
   */
  public void setPosition(Vector2 pos) {
    if (body == null) {
      bodyDef.position.set(pos);
    } else {
      body.setTransform(pos, getAngle());
    }
  }

  /**
   * Returns the velocity vector. Always returns a copy in the same vector.
   */
  public Vector2 getVelocity() {
    return velCache.set(body == null ?
                        bodyDef.linearVelocity : body.getLinearVelocity());
  }

  /**
   * Sets the entity's direction.
   */
  public void setDirection(int dir) {
    this.dir = dir;
  }

  /**
   * Returns direction this entity is facing (1 for right, -1 for left).
   */
  public int getDirection() {
    return dir;
  }

  /**
   * Returns the Box2D body of this entity.
   */
  public Body getBody() {
    return body;
  }
  
  /**
   * Sets the direction of this entity to 1 (right).
   */
  public void faceRight() {
    dir = 1;
  }

  /**
   * Sets the direction of this entity to -1 (left).
   */
  public void faceLeft() {
    dir = -1;
  }

  /**
   * Turns this entity around.
   */
  public void turnAround() {
    dir *= -1;
  }

  /**
   * Adds this entity as a physics object in the given world.
   */
  public boolean activatePhysics(World world) {
    body = world.createBody(bodyDef);
    setState(initIndex);
    return body != null;
  }

  /**
   * Removes this entity as a physics object in the given world.
   */
  public void deactivatePhysics(World world) {
    if (body != null) {
      world.destroyBody(body);
      body = null;
      bodyDef.active = false;
    }
  }

  /**
   * Reflects a shape horizontally across its origin.
   */
  private Shape reflectShape(Shape shape) {
    if (shape instanceof PolygonShape) {
      PolygonShape pShape = (PolygonShape)shape;
      PolygonShape newShape = new PolygonShape();
      int n = pShape.getVertexCount();
      Vector2[] vertices = new Vector2[n];
      for (int i = 0; i < n; i++) {
        Vector2 vertex = new Vector2();
        pShape.getVertex(i, vertex);
        vertex.scl(-1, 1);
        vertices[i] = vertex;
      }
      newShape.set(vertices);
      return newShape;
    } else if (shape instanceof CircleShape) {
      CircleShape newShape = new CircleShape();
      newShape.setRadius(shape.getRadius());
      newShape.setPosition(((CircleShape)shape).getPosition().scl(-1, 1));
      return newShape;
    }
    return null;
  }

  /**
   * Creates a collider with the given fixture definition.
   */
  private Collider createCollider(FixtureDef def, Collider.Type type) {
    Fixture fixture;
    if (dir < 0) {
      FixtureDef newDef = new FixtureDef();
      newDef.density = def.density;
      newDef.friction = def.friction;
      newDef.isSensor = def.isSensor;
      newDef.shape = reflectShape(def.shape);
      fixture = body.createFixture(newDef);
    } else {
      fixture = body.createFixture(def);
    }
    Collider collider = new Collider(this, fixture, type);
    fixture.setUserData(collider);
    return collider;
  }
  
  /**
   * Updates this entity's state.
   * Call sync() after updating to ensure colliders match the state.
   * @param delta time since the last update
   */
  public void update(float delta) {
    update(delta, 0.5f);
  }

  /**
   * Updates this entity's state.
   * Call sync() before stepping again to ensure colliders match the state.
   * @param delta time since the last update
   * @param incr increment size for count
   */
  public void update(float delta, float incr) {
    count += incr;
    advanceState();
  }

  /**
   * Recreates this entity's colliders based on the current entity state.
   */
  public void sync() {
    for (Collider hitbox : hitboxes) {
      body.destroyFixture(hitbox.getFixture());
    }
    for (Collider hurtbox : hurtboxes) {
      body.destroyFixture(hurtbox.getFixture());
    }
    for (Collider sensor : sensors.values()) {
      body.destroyFixture(sensor.getFixture());
    }
    hitboxes.clear();
    hurtboxes.clear();
    sensors.clear();

    State state = getState();
    for (FixtureDef def : state.getHurtboxDefs(getCount())) {
      hurtboxes.add(createCollider(def, Collider.Type.HURTBOX));
    }
    for (FixtureDef def : state.getHitboxDefs(getCount())) {
      hitboxes.add(createCollider(def, Collider.Type.HITBOX));
    }
    ObjectMap<String, FixtureDef> sensorDefs = state.getSensorDefs(getCount());
    for (String name : sensorDefs.keys()) {
      sensors.put(name, createCollider(sensorDefs.get(name), Collider.Type.valueOf(name.toUpperCase())));
    }
  }

  /**
   * Returns the internal animation count (float).
   */
  public float getInternalCount() {
    return count;
  }
  
  /**
   * Returns the current count of the animation as an int.
   */
  public int getCount() {
    return (int) count;
  }

  /**
   * Returns the current entity state.
   */
  public State getState() {
    return getStates().get(stateIndex);
  }

  /**
   * Sets the entity state to the given entity state index,
   * calling transition functions if the new state is different.
   * @param i entity state index
   */
  public void setState(int i) {
    if (i != stateIndex) {
      leaveState();
      stateIndex = i;
      enterState();
    }
  }

  /**
   * Executes any initial state changes before entering current state.
   * Called when new state is set.
   */
  public void enterState() {
    count = 0;
  }

  /**
   * Executes any final state changes before leaving current state.
   * Called before new state is set.
   */
  public void leaveState() {}
  
  /**
   * Transitions entity states based on current entity state.
   * Inputs should be set as flags in the entity and read in the implementing body.
   */
  public void advanceState() {}

  /**
   * Draws this entity to the given canvas.
   */
  public void draw(GameCanvas canvas) {
    Texture texture = getTexture();
    float w = texture.getWidth();
    float h = texture.getHeight();
    Vector2 pos = getPosition().scl(Shared.PPM);
    canvas.draw(texture, Color.WHITE,
                dir * w / 2, h / 2,
                pos.x, pos.y,
                dir * w, h);
  }

  /**
   * Draws a hitbox/hurtbox shape to the canvas.
   */
  protected void drawPhysicsShape(GameCanvas canvas, Shape shape, Color color) {
    Vector2 pos = getPosition();
    if (shape instanceof CircleShape) {
      Vector2 spos = ((CircleShape)shape).getPosition();
      canvas.drawPhysics((CircleShape)shape, color, pos.x + spos.x, pos.y + spos.y,
                         Shared.PPM, Shared.PPM);
    } else if (shape instanceof PolygonShape) {
      canvas.drawPhysics((PolygonShape)shape, color, pos.x, pos.y, 0,
                         Shared.PPM, Shared.PPM);
    }
  }
  
  /**
   * Draws this entity's physics outline (hurtboxes) to the given canvas.
   */
  public void drawPhysics(GameCanvas canvas) {
    for (Collider hitbox : hitboxes) {
      drawPhysicsShape(canvas, hitbox.getFixture().getShape(), Color.RED);
    }
    for (Collider hurtbox : hurtboxes) {
      drawPhysicsShape(canvas, hurtbox.getFixture().getShape(), Color.BLUE);
    }
    for (Collider sensor : sensors.values()) {
      drawPhysicsShape(canvas, sensor.getFixture().getShape(), Color.GREEN);
    }
  }

  /**
   * Class used to provide metadata during collision detection.
   */
  public static class Collider {
    /** The possible collider types */
    public enum Type {
      HITBOX,
      HURTBOX,
      GROUND,
      AHEAD,
      BEHIND,
      FRONT_EDGE,
      BACK_EDGE,
      VISION,
      FORWARD,
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
     * Returns whether this collider is an ahead sensor.
     */
    public boolean isAheadSensor() {
      return type == Type.AHEAD;
    }

    /**
     * Returns whether this collider is a behind sensor.
     */
    public boolean isBehindSensor() {
      return type == Type.BEHIND;
    }

    /**
     * Returns whether this collider is a front edge sensor.
     */
    public boolean isFrontEdgeSensor() {
      return type == Type.FRONT_EDGE;
    }

    /**
     * Returns whether this collider is a back edge sensor.
     */
    public boolean isBackEdgeSensor() {
      return type == Type.BACK_EDGE;
    }

    /**
     * Returns whether this collider is a center sensor.
     */
    public boolean isCenterSensor() {
      return type == Type.CENTER;
    }

    /**
     * Returns whether this collider is a vision sensor.
     */
    public boolean isVisionSensor() {
      return type == Type.VISION;
    }
  }
}
