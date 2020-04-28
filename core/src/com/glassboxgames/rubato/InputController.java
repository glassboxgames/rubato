package com.glassboxgames.rubato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputController {
  /** Whether the debug input was entered */
  private boolean debug;
  /** Whether the devMode input was entered */
  private boolean devMode;
  /** The devSelect input 0-9 */
  private int devSelect;
  /** The devChange increment/decrement input */
  private int devChange;

  /** Whether the exit button was pressed */
  private boolean exitPressed;
  /** Whether the reset button was pressed */
  private boolean resetPressed;
  /** Whether the edit button was pressed */
  private boolean editPressed;
  /** Whether the jump button was pressed */
  private boolean jumpPressed;
  /** Whether the jump button was held */
  private boolean jumpHeld;
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
   * Returns whether the debug input was entered.
   */
  public boolean didDebug() {
    return debug;
  }

  /**
   * Returns whether the devMode input was entered.
   */
  public boolean didDevMode() {
    return devMode;
  }

  /**
   * Returns the devSelect input.
   */
  public int getDevSelect() {
    return devSelect;
  }

  /**
   * Returns the devChange input.
   */
  public int getDevChange() {
    return devChange;
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
   * Returns whether the player presesd the edit button.
   */
  public boolean didEdit() {
    return editPressed;
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
   * Returns whether the player input a jump.
   */
  public boolean didJump() {
    return jumpPressed;
  }

  /**
   * Returns whether the player kept holding jump.
   */
  public boolean didHoldJump() {
    return jumpHeld;
  }

  /**
   * Returns whether the player pressed dash.
   */
  public boolean didDash() {
    return dashPressed;
  }

  /**
   * Returns whether the player pressed attack.
   */
  public boolean didAttack() {
    return attackPressed;
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
    debug = Gdx.input.isKeyJustPressed(Input.Keys.SLASH);
    devMode = Gdx.input.isKeyJustPressed(Input.Keys.PERIOD);
    if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
      devSelect = 1;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
      devSelect = 2;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
      devSelect = 3;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
      devSelect = 4;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
      devSelect = 5;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
      devSelect = 6;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
      devSelect = 7;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) {
      devSelect = 8;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
      devSelect = 9;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
      devSelect = 0;
    } else {
      devSelect = -1;
    }
    devChange = 0;
    if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
      devChange += 1;
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
      devChange -= 1;
    }

    // TODO: maybe secondary to allow gamepad override

    exitPressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
    resetPressed = Gdx.input.isKeyJustPressed(Input.Keys.R);
    editPressed = Gdx.input.isKeyPressed(Input.Keys.E);
    attackPressed = Gdx.input.isKeyJustPressed(Input.Keys.F);
    dashPressed = Gdx.input.isKeyJustPressed(Input.Keys.D);
    jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    jumpHeld = Gdx.input.isKeyPressed(Input.Keys.SPACE);

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
