package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.glassboxgames.util.*;

public class GDXRoot extends Game implements ScreenListener {
  /** Drawing context to display graphics (VIEW CLASS) */
  private GameCanvas canvas;
  /** Manager for loading assets */
  private AssetManager manager;
  /** Mode for loading assets */
  private LoadingMode loading;
  /** Mode for playing the game */
  private GameMode playing;

  public GDXRoot() {
    manager = new AssetManager();
    // Add font support to the asset manager
    FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class,
                      new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf",
                      new FreetypeFontLoader(resolver));
  }

  @Override
  public void create() {
    canvas = new GameCanvas();
    // canvas.setFullscreen(true, false);
    loading = new LoadingMode(canvas, manager, this);
    playing = new GameMode(canvas, this);
    playing.preloadContent(manager);
    setScreen(loading);
  }

  @Override
  public void resize(int width, int height) {
    canvas.resize();
    super.resize(width, height);
  }

  @Override
  public void dispose() {
    Screen screen = getScreen();
    setScreen(null);
    screen.dispose();
    canvas.dispose();
    canvas = null;

    playing.unloadContent(manager);
    manager.clear();
    manager.dispose();
    super.dispose();
  }

  @Override
  public void exitScreen(Screen screen, int exitCode) {
    if (exitCode != 0) {
      Gdx.app.error("GDXRoot",
                    "Exited with error code " + exitCode, new RuntimeException());
      Gdx.app.exit();
    } else if (screen == loading) {
      playing.loadContent(manager);
      setScreen(playing);
    } else if (screen == playing) {
      loading.dispose();
      playing.dispose();
      Gdx.app.exit();
    }
  }
}
