package com.glassboxgames.rubato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
  /** Whether the reset button was pressed */
  protected boolean resetPressed;
  /** Whether the attack button was pressed */
  protected boolean attackPressed;

  /** How much did we move horizontally */
  private float horizontal;
  /** How much did we move vertically */
  private float vertical;

  /** The singleton instance of the input controller */
  private static InputController controller = null;

  /**
   * Returns the singleton instance of the input controller.
   */
  public static InputController getInstance() {
    if (controller == null) {
      controller = new InputController();
    }
    return controller;
  }

  /**
   * Create a new input controller. Only used to create the singleton.
   * @see getInstance
   */
  private InputController() {

  }

  /**
   * Returns the horizontal movement input from the player.
   */
  public float getHorizontal() {
    return horizontal;
  }

  /**
   * Returns the vertical movement input from the player.
   */
  public float getVertical() {
    return vertical;
  }

  /**
   * Reads the input from the player.
   */
  public void readInput() {
    readKeyboard();
  }

  /**
   * Reads the input from the player's keyboard.
   */
  private void readKeyboard() {
    // TODO: maybe secondary to allow gamepad override
    resetPressed = Gdx.input.isKeyPressed(Input.Keys.R);
    attackPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

    // Directional Controls
    horizontal = vertical = 0.0f;
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      horizontal += 1.0f;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      horizontal -= 1.0f;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
      vertical += 1.0f;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      vertical -= 1.0f;
    }
  }
}
