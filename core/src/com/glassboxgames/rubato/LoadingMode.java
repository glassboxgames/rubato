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
  private GameCanvas canvas;
  /** Asset manager for loading */
  private AssetManager manager;
  /** Listener to update mode */
  private ScreenListener listener;
  /** Filmstrip for the loading animation */
  private FilmStrip strip;
  /** Filmstrip index */
  private float index;
  /** Whether the manager is done */
  private boolean done;

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
    strip = new FilmStrip(new Texture(Gdx.files.internal("User Interface/Loading/strip.png")), 1, 25);
  }

  @Override
  public void render(float delta) {
    canvas.clear();
    if (!done) {
      if (manager.isFinished()) {
        done = true;
        listener.exitScreen(this, EXIT_DONE);
      } else {
        manager.update((int) (delta * 250));
      }
    }
    canvas.begin();
    canvas.draw(strip, Color.WHITE, 50, 50, strip.getWidth(), strip.getHeight());
    canvas.end();
    index += 0.5f;
    strip.setFrame((int)index % strip.getSize());
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
