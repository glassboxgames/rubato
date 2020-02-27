package com.glassboxgames.rubato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
  /** Whether the reset button was pressed */
  private boolean resetPressed;
  /** Whether the attack button was pressed */
  private boolean attackPressed;
  /** Whether the jump button was pressed */
  private boolean jumpPressed;

  /** How much did we move horizontally */
  private int horizontal;

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
  private InputController() {}

  /**
   * Returns the horizontal movement input from the player.
   */
  public int getHorizontal() {
    return horizontal;
  }

  /**
   * Returns whether the player reset the game.
   */
  public boolean didReset() {
    return resetPressed;
  }

  /**
   * Returns whether the player pressed attack.
   */
  public boolean didAttack() {
    return attackPressed;
  }

  /**
   * Returns whether the player input a jump.
   */
  public boolean didJump() {
    return jumpPressed;
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
    attackPressed = Gdx.input.isKeyPressed(Input.Keys.F);
    jumpPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

    horizontal = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      horizontal += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      horizontal -= 1;
    }
  }
}
