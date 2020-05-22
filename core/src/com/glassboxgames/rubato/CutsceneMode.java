package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

/**
 * Mode controller for cutscenes.
 */
public class CutsceneMode implements Screen {
  /** Exit code for escaping the cutscene mode */
  public static final int EXIT_ESCAPE = 0;
  /** Exit code for completing the cutscene */
  public static final int EXIT_COMPLETE = 1;

  /** Fraction of screen width to use */
  private static final float SCALE = 0.6f;

  /** Whether this mode is active */
  private boolean active;
  /** Listener to call when exiting */
  private ScreenListener listener;

  /** Whether this screen is in the process of exiting */
  private boolean exiting;
  /** Exit code for when the screen exits */
  private int exitCode;

  /** Canvas on which to draw content */
  private GameCanvas canvas;
  /** Camera position */
  private Vector2 cameraPos;

  /** Current pause time */
  private int startPauseTime, endPauseTime;
  
  /** Cutscene texture */
  private Texture cutscene;
  /** Next cutscene texture */
  private Texture nextCutscene;
  /** Cutscene width */
  private float width;
  /** Cutscene height */
  private float height;
  /** Scroll rate */
  private int scrollRate;

  /**
   * Instantiates the cutscene mode controller.
   * @param listener listener for exit
   */
  public CutsceneMode(GameCanvas canvas, ScreenListener listener) {
    this.canvas = canvas;
    this.listener = listener;

    cutscene = null;
    cameraPos = new Vector2();
  }

  /**
   * Sets the next cutscene texture for this instance.
   */
  public void setNextCutscene(String key, int pauseTime, int rate) {
    nextCutscene = Shared.getTexture(key);
    startPauseTime = endPauseTime = pauseTime;
    scrollRate = rate;
  }

  /**
   * Updates the state of the cutscene.
   * @param delta time in seconds since last frame
   */
  protected void update(float delta) {
    InputController input = InputController.getInstance();
    input.readInput();
    if (input.pressedExit()) {
      if (!exiting) {
        exiting = true;
        listener.exitScreen(this, EXIT_COMPLETE);
      }
    }

    int rate = scrollRate;
    if (input.heldJump()) {
      rate = 0;
    } else {
      int horizontal = 0;
      if (input.heldLeft()) {
        horizontal -= 1;
      }
      if (input.heldRight()) {
        horizontal += 1;
      }
      if (horizontal < 0) {
        rate = -5;
      } else if (horizontal > 0) {
        rate = 5;
      }
    }

    if (startPauseTime > 0) {
      startPauseTime--;
    } else if (cameraPos.y > Gdx.graphics.getHeight() / 2) {
      cameraPos.y = MathUtils.clamp(cameraPos.y - rate,
        Gdx.graphics.getHeight() / 2, height - Gdx.graphics.getHeight() / 2);
    } else if (endPauseTime > 0) {
      endPauseTime--;
    } else {
      if (!exiting) {
        exiting = true;
        listener.exitScreen(this, EXIT_COMPLETE);
      }
    }
  }

  /**
   * Draw the current game state to the canvas.
   */
  private void draw() {
    canvas.clear();
    canvas.moveCamera(cameraPos);
    canvas.begin();
    canvas.drawBackground(cutscene, Color.WHITE, width, height);
    canvas.end();
  }

  @Override
  public void render(float delta) {
    if (active) {
      update(delta);
      draw();
    }
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void show() {
    cutscene = nextCutscene;
    width = Gdx.graphics.getWidth() * SCALE;
    height = cutscene.getHeight() * Gdx.graphics.getWidth() / cutscene.getWidth() * SCALE;
    cameraPos.set(width / 2, height - Gdx.graphics.getHeight() / 2);
    active = true;
  }

  @Override
  public void hide() {
    exiting = false;
    active = false;
  }

  @Override
  public void dispose() {}
}
