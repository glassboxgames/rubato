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
     * Returns a singleton instance of the input controller
     * @return
     */
    public static InputController getInstance() {
        if (controller == null) {
            controller = new InputController();
        }
        return controller;
    }

    /** Create a new input controller, this should not be callable outside of this class*/
    private InputController() {

    }
    /**
     * returns the left and right movement of the character
     * @return
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * return the up and down movement of the character
     * @return
     */
    public float getVertical() {
        return vertical;
    }

    /**
     * generic read input function. Modify to expend inputs to gamepad
     */
    public void readInput() {
        readKeyboard();
    }

    /**
     * read the keyboard controlls
     */
    private void readKeyboard() {
        //todo: maybe secondary to allow gamepad override

        resetPressed = Gdx.input.isKeyPressed(Input.Keys.R);
        attackPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        //Directional Controls
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
