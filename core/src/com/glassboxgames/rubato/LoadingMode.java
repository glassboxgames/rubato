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
  /** Canvas to draw on */
  protected GameCanvas canvas;
  /** Asset manager for loading */
  protected AssetManager manager;
  /** Listener to update mode */
  protected ScreenListener listener;

  /**
   * Instantiate a LoadingMode.
   * @param c the game canvas
   * @param m the asset manager
   */
  public LoadingMode(GameCanvas c, AssetManager m, ScreenListener l) {
    canvas = c;
    manager = m;
    listener = l;
  }

  @Override
  public void render(float delta) {
    if (manager.isFinished()) {
      listener.exitScreen(this, 0);
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
