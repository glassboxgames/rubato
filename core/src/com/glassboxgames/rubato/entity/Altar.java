package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;

/**
 * Class representing the Altar of Time object.
 */
public class Altar extends Entity {
  /** States */
  public static final int STATE_DEFAULT = 0;

  /** Altar states */
  public static Array<State> states = null;

  /** Whether the player is nearby */
  private boolean nearby;

  /**
   * Instantiates the altar with the specified parameters.
   */
  public Altar(float x, float y) {
    super(x, y, STATE_DEFAULT);
    bodyDef.type = BodyDef.BodyType.StaticBody;
  }

  /**
   * Initializes altar states.
   */
  public static Array<State> initStates() {
    return states = State.readStates("Altar/");
  }

  @Override
  public Array<State> getStates() {
    return states;
  }

  /**
   * Sets the player nearby state.
   */
  public void setPlayerNearby(boolean value) {
    nearby = value;
  }

  /**
   * Returns the nearby state.
   */
  public boolean isPlayerNearby() {
    return nearby;
  }
}

