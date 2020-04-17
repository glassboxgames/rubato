package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
import com.glassboxgames.util.*;

/**
 * Mode controller for loading assets.
 */
public class LoadingMode implements Screen {
  /** Exit code for finishing asset loading */
  public static final int EXIT_DONE = 0;
  
  /** Canvas to draw on */
  protected GameCanvas canvas;
  /** Asset manager for loading */
  protected AssetManager manager;
  /** Listener to update mode */
  protected ScreenListener listener;

  /**
   * Instantiate a LoadingMode.
   * @param canvas the game canvas
   * @param manager the asset manager
   * @param listener the screen listener
   */
  public LoadingMode(GameCanvas canvas, AssetManager manager, ScreenListener listener) {
    this.canvas = canvas;
    this.manager = manager;
    this.listener = listener;
  }

  @Override
  public void render(float delta) {
    canvas.clear();
    if (manager.isFinished()) {
      listener.exitScreen(this, EXIT_DONE);
    } else {
      manager.update((int) (delta * 1000));
    }
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void show() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {}
}
