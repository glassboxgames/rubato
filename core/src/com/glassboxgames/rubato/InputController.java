package com.glassboxgames.rubato;

import com.badlogic.gdx.*;

/**
 * Controller for getting inputs from the player.
 */
public class InputController {
  /** Whether the debug input was entered */
  private boolean debugPressed;
  /** Whether the dev mode input was entered */
  private boolean devModePressed;
  /** The devSelect input 0-9 */
  private int devSelect;
  /** The devChange increment/decrement input */
  private int devChange;

  /** Whether the exit button was pressed */
  private boolean exitPressed;
  /** Whether the left button was pressed */
  private boolean leftPressed;
  /** Whether the left button was held */
  private boolean leftHeld;
  /** Whether the right button was pressed */
  private boolean rightPressed;
  /** Whether the right button was held */
  private boolean rightHeld;
  /** Whether the jump button was pressed */
  private boolean jumpPressed;
  /** Whether the jump button was held */
  private boolean jumpHeld;
  /** Whether the attack button was pressed */
  private boolean attackPressed;
  /** Whether the confirm button was pressed */
  private boolean confirmPressed;
  /** Whether the reset button was pressed */
  private boolean resetPressed;

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
  public boolean pressedDebug() {
    return debugPressed;
  }

  /**
   * Returns whether the devMode input was entered.
   */
  public boolean pressedDevMode() {
    return devModePressed;
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
   * Returns whether the player pressed the confirm key (for menus).
   */
  public boolean pressedConfirm() {
    return confirmPressed;
  }

  /**
   * Returns whether the player exit the game.
   */
  public boolean pressedExit() {
    return exitPressed;
  }

  /**
   * Returns whether the player pressed the left button.
   */
  public boolean pressedLeft() {
    return leftPressed;
  }

  /**
   * Returns whether the player kept holding left.
   */
  public boolean heldLeft() {
    return leftHeld;
  }

  /**
   * Returns whether the player pressed the right button.
   */
  public boolean pressedRight() {
    return rightPressed;
  }

  /**
   * Returns whether the player kept holding right.
   */
  public boolean heldRight() {
    return rightHeld;
  }

  /**
   * Returns whether the player input a jump.
   */
  public boolean pressedJump() {
    return jumpPressed;
  }

  /**
   * Returns whether the player kept holding jump.
   */
  public boolean heldJump() {
    return jumpHeld;
  }

  /**
   * Returns whether the player pressed attack.
   */
  public boolean pressedAttack() {
    return attackPressed;
  }

  /**
   * Returns whether the player pressed reset.
   */
  public boolean pressedReset() {
    return resetPressed;
  }

  /**
   * Reads the input from the player.
   */
  public void readInput() {
    SaveController save = SaveController.getInstance();
    
    confirmPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    exitPressed = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    debugPressed = Gdx.input.isKeyJustPressed(Input.Keys.SLASH);
    devModePressed = Gdx.input.isKeyJustPressed(Input.Keys.PERIOD);

    for (int i = 0; i < 10; i++) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.valueOf(i + ""))) {
        devSelect = i;
      }
    }

    devChange = 0;
    if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
      devChange += 1;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
      devChange -= 1;
    }

    leftPressed = Gdx.input.isKeyJustPressed(save.getBoundKeycode(Shared.ACTION_LEFT));
    leftHeld = Gdx.input.isKeyPressed(save.getBoundKeycode(Shared.ACTION_LEFT));
    rightPressed = Gdx.input.isKeyJustPressed(save.getBoundKeycode(Shared.ACTION_RIGHT));
    rightHeld = Gdx.input.isKeyPressed(save.getBoundKeycode(Shared.ACTION_RIGHT));
    attackPressed = Gdx.input.isKeyJustPressed(save.getBoundKeycode(Shared.ACTION_ATTACK));
    jumpPressed = Gdx.input.isKeyJustPressed(save.getBoundKeycode(Shared.ACTION_JUMP));
    jumpHeld = Gdx.input.isKeyPressed(save.getBoundKeycode(Shared.ACTION_JUMP));
    resetPressed = Gdx.input.isKeyJustPressed(save.getBoundKeycode(Shared.ACTION_RESET));
  }
}
