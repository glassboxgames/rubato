package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

public class GDXRoot extends Game implements ScreenListener {
  // TODO replace with JSON
  public static final String[] levelPaths = new String[] {
    "data/level.json",
  };

  /** Drawing context to display graphics (VIEW CLASS) */
  private GameCanvas canvas;
  /** Manager for loading assets */
  private AssetManager manager;
  /** Mode for loading assets */
  private LoadingMode loading;
  /** Mode for playing the game */
  private GameMode playing;
  /** Mode for editing levels */
  private EditorMode editing;
  /** Level data array */
  private LevelData[] levels;
  /** Current level index */
  private int levelIndex;
  /** Json conversion object */
  private Json json;

  public GDXRoot() {
    manager = new AssetManager();
    // Add font support to the asset manager
    FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class,
                      new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf",
                      new FreetypeFontLoader(resolver));
    levels = new LevelData[levelPaths.length];
  }

  @Override
  public void create() {
    canvas = new GameCanvas();
    loading = new LoadingMode(canvas, manager, this);
    playing = new GameMode(canvas, this);
    editing = new EditorMode(this);
    json = new Json();
    json.setOutputType(JsonWriter.OutputType.json);
    playing.preloadContent(manager);
    editing.preloadContent(manager);
    for (int i = 0; i < levelPaths.length; i++) {
      levels[i] = json.fromJson(LevelData.class,
                                Gdx.files.internal(levelPaths[i]));
      manager.load(levels[i].background, Texture.class);
    }
    levelIndex = 0;
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
    editing.unloadContent(manager);
    playing.unloadContent(manager);
    for (LevelData data : levels) {
      if (manager.isLoaded(data.background)) {
        manager.unload(data.background);
      }
    }
    manager.clear();
    manager.dispose();
    super.dispose();
  }

  @Override
  public void exitScreen(Screen screen, int exitCode) {
    if (screen == loading) {
      if (exitCode == 0) {
        playing.loadContent(manager);
        playing.initLevel(levels[levelIndex], manager);
        System.out.println("Starting level " + levelIndex + " loaded from file " + levelPaths[levelIndex]);
        setScreen(playing);
      } else {
        Gdx.app.error("GDXRoot", "Exited loading mode with error code " + exitCode, new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == playing) {
      if (exitCode == 0) {
        levelIndex++;
        if (levelIndex >= levels.length) {
          Gdx.app.exit();
        } else {
          setScreen(loading);
        }
      } else if (exitCode == 1) {
        setScreen(loading);
      } else if (exitCode == 2) {
        editing.loadContent(manager);
        editing.initLevel(levels[levelIndex], manager);
        setScreen(editing);
      } else {
        Gdx.app.error("GDXRoot", "Exited playing mode with error code " + exitCode, new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == editing) {
      if (exitCode == 1) {
        LevelData data = editing.exportLevel();
        levels[levelIndex] = data;
        Gdx.files.local(levelPaths[levelIndex]).writeString(json.prettyPrint(data), false);
        System.out.println("Saved level to " + levelPaths[levelIndex]);
        playing.initLevel(data, manager);
        setScreen(playing);
      } else {
        dispose();
        Gdx.app.exit();
      }
    }
  }
}
