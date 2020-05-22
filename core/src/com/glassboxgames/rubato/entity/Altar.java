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

  /** Whether the player is seen */
  private boolean seen;
  /** Whether the player is close */
  private boolean close;

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
   * Sets the player seen state.
   */
  public void setPlayerSeen(boolean value) {
    seen = value;
  }

  /**
   * Returns the seen state.
   */
  public boolean isPlayerSeen() {
    return seen;
  }

  /**
   * Sets the player close state.
   */
  public void setPlayerClose(boolean value) {
    close = value;
  }

  /**
   * Returns the close state.
   */
  public boolean isPlayerClose() {
    return close;
  }
}
