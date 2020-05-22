package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

public class Spider extends Enemy {
  /** Spider state constants */
  public static final int STATE_IDLE = 0;
  public static final int STATE_WANDER = 1;
  public static final int STATE_WINDUP = 2;
  public static final int STATE_ATTACK = 3;
  /** Spider states */
  public static Array<State> states = null;

  /** Max horizontal speed */
  private static final float MAX_X_SPEED = 1f;
  /** Max vertical speed */
  private static final float MAX_Y_SPEED = 8f;
  /** Max health */
  private static final float MAX_HEALTH = 1f;
  /** Attack launch impulse */
  private static final Vector2 ATTACK_IMPULSE = new Vector2(5f, 6f);

  /** Temporary vector */
  private Vector2 temp = new Vector2();
  /** Set of entities considered underfoot */
  private ObjectSet<Entity> entitiesUnderfoot;
  /** Set of entities ahead */
  private ObjectSet<Entity> entitiesAhead;
  /** Set of entities ahead */
  private ObjectSet<Entity> entitiesBehind;
  /** Set of entities at front edge */
  private ObjectSet<Entity> entitiesAtFrontEdge;
  /** Set of entities at back edge */
  private ObjectSet<Entity> entitiesAtBackEdge;

  /**
   * Instantiates a spider enemy with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Spider(float x, float y) {
    super(x, y, STATE_IDLE);
    entitiesUnderfoot = new ObjectSet<Entity>();
    entitiesAhead = new ObjectSet<Entity>();
    entitiesBehind = new ObjectSet<Entity>();
    entitiesAtFrontEdge = new ObjectSet<Entity>();
    entitiesAtBackEdge = new ObjectSet<Entity>();
  }

  /**
   * Initializes spider states.
   */
  public static Array<State> initStates() {
    states = State.readStates("Enemies/Spider/");
    return states;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  /**
   * Returns whether this spider can move forward.
   */
  private boolean canMove() {
    return entitiesAhead.isEmpty() && !entitiesAtFrontEdge.isEmpty();
  }

  /**
   * Returns whether this spider can turn around.
   */
  private boolean canTurn() {
    return entitiesBehind.isEmpty() && !entitiesAtBackEdge.isEmpty();
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_IDLE:
      if (getTarget() != null) {
        setState(STATE_WINDUP);
      } else if (canMove() || canTurn()) {
        setState(STATE_WANDER);
      }
      break;
    case STATE_WANDER:
      if (getTarget() != null) {
        setState(STATE_WINDUP);
      } else if (!canMove() && !canTurn()) {
        setState(STATE_IDLE);
      }
      break;
    case STATE_WINDUP:
      if (getCount() >= getState().getLength()) {
        setState(STATE_ATTACK);
      }
      break;
    case STATE_ATTACK:
      if (isGrounded() && getCount() >= getState().getLength()) {
        setState(STATE_IDLE);
      }
      break;
    }
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_IDLE:
      body.setLinearVelocity(0, 0);
      break;
    case STATE_WINDUP:
      body.setLinearVelocity(0, 0);
      float delta = getTarget().x - getPosition().x;
      if (delta > 0) {
        faceRight();
      } else if (delta < 0) {
        faceLeft();
      }
      break;
    case STATE_ATTACK:
      body.applyLinearImpulse(temp.set(ATTACK_IMPULSE).scl(getDirection(), 1), getPosition(), true);
      body.setGravityScale(0.6f);
      break;
    }
  }

  @Override
  public void leaveState() {
    super.leaveState();
    switch (stateIndex) {
    case STATE_ATTACK:
      body.setGravityScale(1.0f);
      break;
    }
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    switch (stateIndex) {
    case STATE_WANDER:
      if (entitiesAtFrontEdge.isEmpty() || !entitiesAhead.isEmpty()) {
        turnAround();
      }
      body.setLinearVelocity(MAX_X_SPEED * getDirection(), 0);
      break;
    }
    body.setLinearVelocity(getVelocity().x, MathUtils.clamp(getVelocity().y, -MAX_Y_SPEED, MAX_Y_SPEED));
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }

  /**
   * Adds an underfoot entity.
   */
  public void addUnderfoot(Entity entity) {
    entitiesUnderfoot.add(entity);
  }

  /**
   * Removes an underfoot entity.
   */
  public void removeUnderfoot(Entity entity) {
    entitiesUnderfoot.remove(entity);
  }

  /**
   * Adds an ahead entity.
   */
  public void addAhead(Entity entity) {
    entitiesAhead.add(entity);
  }

  /**
   * Removes an ahead entity.
   */
  public void removeAhead(Entity entity) {
    entitiesAhead.remove(entity);
  }

  /**
   * Adds a behind entity.
   */
  public void addBehind(Entity entity) {
    entitiesBehind.add(entity);
  }

  /**
   * Removes a behind entity.
   */
  public void removeBehind(Entity entity) {
    entitiesBehind.remove(entity);
  }

  /**
   * Adds a front edge entity.
   */
  public void addAtFrontEdge(Entity entity) {
    entitiesAtFrontEdge.add(entity);
  }

  /**
   * Removes a front edge entity.
   */
  public void removeAtFrontEdge(Entity entity) {
    entitiesAtFrontEdge.remove(entity);
  }

  /**
   * Adds a back edge entity.
   */
  public void addAtBackEdge(Entity entity) {
    entitiesAtBackEdge.add(entity);
  }

  /**
   * Removes a back edge entity.
   */
  public void removeAtBackEdge(Entity entity) {
    entitiesAtBackEdge.remove(entity);
  }

  /**
   * Returns whether this enemy is grounded.
   */
  public boolean isGrounded() {
    return !entitiesUnderfoot.isEmpty();
  }
}
