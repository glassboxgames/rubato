package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;

/**
 * Class representing a time pillar checkpoint in Rubato.
 */
public class Checkpoint extends Entity {
  /** States */
  public static final int STATE_INACTIVE = 0;
  public static final int STATE_ACTIVE = 1;
  
  /** Checkpoint states */
  public static Array<State> states = null;
  
  /** Whether this checkpoint has been activated */
  private boolean activated;

  /**
   * Initializes a checkpoint with the specified parameters.
   * @param x x-coordinate of lower left corner
   * @param y y-coordinate of lower left corner
   */
  public Checkpoint(float x, float y) {
    super(x, y, STATE_INACTIVE);
    bodyDef.type = BodyDef.BodyType.StaticBody;
  }

  /**
   * Initializes checkpoint states.
   */
  public static Array<State> initStates() {
    states = State.readStates("Checkpoints/");
    return states;
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  @Override
  public void advanceState() {
    switch (stateIndex) {
    case STATE_INACTIVE:
      if (activated) {
        setState(STATE_ACTIVE);
      }
      break;
    }
  }

  /**
   * Activates this checkpoint.
   */
  public void activate() {
    activated = true;
  }

  /**
   * Returns whether this checkpoint has been activated.
   */
  public boolean isActivated() {
    return activated;
  }
}
