package com.glassboxgames.rubato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
  /** Whether the debug code was entered */
  private boolean debug;

  /** Whether the exit button was pressed */
  private boolean exitPressed;
  /** Whether the reset button was pressed */
  private boolean resetPressed;
  /** Whether the jump button was pressed */
  private boolean jumpPressed;
  /** Whether the dash button was pressed */
  private boolean dashPressed;
  /** Whether the attack button was pressed */
  private boolean attackPressed;

  /** Horizontal movement input */
  private int horizontal;
  /** Vertical direction input */
  private int vertical;

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
   */
  private InputController() {}

  /**
   * Returns whether the debug code was entered.
   */
  public boolean didDebug() {
    return debug;
  }

  /**
   * Returns whether the player exit the game.
   */
  public boolean didExit() {
    return exitPressed;
  }

  /**
   * Returns whether the player reset the game.
   */
  public boolean didReset() {
    return resetPressed;
  }

  /**
   * Returns the horizontal movement input from the player.
   */
  public int getHorizontal() {
    return horizontal;
  }

  /**
   * Returns the vertical direction input from the player.
   */
  public int getVertical() {
    return vertical;
  }

  /**
   * Returns whether the player pressed attack.
   */
  public boolean didAttack() {
    return attackPressed;
  }

  /**
   * Returns whether the player pressed dash.
   */
  public boolean didDash() { return dashPressed; }

  /**
   * Returns whether the player input a jump.
   */
  public boolean didJump() { return jumpPressed; }

  /**
   * Reads the input from the player.
   */
  public void readInput() { readKeyboard(); }

  /**
   * Reads the input from the player's keyboard.
   */
  private void readKeyboard() {
    debug = Gdx.input.isKeyJustPressed(Input.Keys.SLASH);

    // TODO: maybe secondary to allow gamepad override

    exitPressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
    resetPressed = Gdx.input.isKeyJustPressed(Input.Keys.R);
    attackPressed = Gdx.input.isKeyPressed(Input.Keys.F);
    dashPressed = Gdx.input.isKeyPressed(Input.Keys.D);
    jumpPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

    horizontal = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      horizontal += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      horizontal -= 1;
    }

    vertical = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
      vertical += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      vertical -= 1;
    }
  }
}
