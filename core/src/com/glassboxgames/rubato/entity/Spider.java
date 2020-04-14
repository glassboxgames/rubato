package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

public class Spider extends Enemy {
  /** Spider state constants */
  public static final int STATE_WANDER = 0;
  public static final int STATE_WINDUP = 1;
  public static final int STATE_ATTACK = 2;
  /** Spider states */
  public static Array<State> states = null;

  /** Max horizontal speed */
  private static final float MAX_SPEED = 1.5f;
  /** Max health */
  private static final float MAX_HEALTH = 1f;
  /** Attack launch force */
  private static final Vector2 ATTACK_FORCE = new Vector2(400, 300);

  /** Temporary vector */
  private Vector2 temp = new Vector2();
  /** Whether the spider is at an edge */
  private boolean edge;
  /** Set of entities considered underfoot */
  private ObjectSet<Entity> entitiesUnderfoot;

  /**
   * Instantiates a spider enemy with the given parameters.
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Spider(float x, float y) {
    super(x, y, STATE_WANDER);
    entitiesUnderfoot = new ObjectSet<Entity>();
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

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_WANDER:
      if (getTarget() != null) {
        setState(STATE_WINDUP);
      }
      break;
    case STATE_WINDUP:
      if (getCount() >= getState().getLength()) {
        setState(STATE_ATTACK);
      }
      break;
    case STATE_ATTACK:
      if (isGrounded() && getCount() >= getState().getLength()) {
        setState(STATE_WANDER);
      }
      break;
    }
  }

  @Override
  public void enterState() {
    super.enterState();
    switch (stateIndex) {
    case STATE_WANDER:
      body.setGravityScale(1f);
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
      body.setGravityScale(0.2f);
      body.applyForce(temp.set(ATTACK_FORCE).scl(getDirection(), 1), getPosition(), true);
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
      if (isAtEdge()) {
        turnAround();
      }
      body.setLinearVelocity(MAX_SPEED * getDirection(), 0);
      break;
    case STATE_ATTACK:
      if (getCount() >= getState().getLength() && isGrounded()) {
        body.setLinearVelocity(0, 0);
      }
      break;
    }
  }

  @Override
  public float getMaxHealth() {
    return MAX_HEALTH;
  }

  /**
   * Sets whether this enemy's sensor detects an edge.
   */
  public void setEdge(boolean edge) {
    this.edge = edge;
  }

  /**
   * Returns whether this enemy's sensor detects an edge.
   */
  public boolean isAtEdge() {
    return edge;
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
   * Returns whether this enemy is grounded.
   */
  public boolean isGrounded() {
    return !entitiesUnderfoot.isEmpty();
  }
}
