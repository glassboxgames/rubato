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
  public static final String[] levelPaths = new String[] {
    "Levels/wall.json",
    "Levels/easy.json",
    "Levels/medium.json",
    "Levels/hard.json",
  };

  /** Drawing context to display graphics (VIEW CLASS) */
  private GameCanvas canvas;
  /** Manager for loading assets */
  private AssetManager manager;
  /** Mode for the main menu */
  private MainMenu mainMenu;
  /** Mode for loading assets */
  private LoadingMode loading;
  /** Mode for playing the game */
  private GameMode playing;
  /** Mode for editing levels */
  private EditorMode editing;
  /** Which screen to switch to after loading */
  private Screen nextScreen;
  
  /** Level data array */
  private LevelData[] levels;
  /** Current level index */
  private int levelIndex;
  /** Current level data */
  private LevelData level;

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
    mainMenu = new MainMenu(this);
    loading = new LoadingMode(canvas, manager, this);
    playing = new GameMode(canvas, this);
    editing = new EditorMode(this);
    mainMenu.preloadContent(manager);
    playing.preloadContent(manager);
    editing.preloadContent(manager);
    for (String path : Constants.BACKGROUND_MAP.values()) {
      manager.load(path, Texture.class);
    }
    for (int i = 0; i < levelPaths.length; i++) {
      levels[i] = Constants.JSON.fromJson(LevelData.class,
                                          Gdx.files.internal(levelPaths[i]));
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
    mainMenu.unloadContent(manager);
    editing.unloadContent(manager);
    playing.unloadContent(manager);
    mainMenu.dispose();
    editing.dispose();
    playing.dispose();
    setScreen(null);
    canvas.dispose();
    canvas = null;
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
      if (exitCode == LoadingMode.EXIT_DONE) {
        if (nextScreen == null) {
          mainMenu.loadContent(manager);
          setScreen(mainMenu);
        } else if (nextScreen == playing) {
          playing.loadContent(manager);
          level = levels[levelIndex];
          playing.initLevel(level, manager, false);
          setScreen(playing);
        } else if (nextScreen == editing) {
          editing.loadContent(manager);
          playing.loadContent(manager);
          setScreen(editing);
        }
      } else {
        Gdx.app.error("GDXRoot", "Exited loading mode with error code " + exitCode, new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == mainMenu) {
      if (exitCode == MainMenu.EXIT_PLAY) {
        nextScreen = playing;
        setScreen(loading);
      } else if (exitCode == MainMenu.EXIT_EDITOR) {
        nextScreen = editing;
        setScreen(loading);
      }
    } else if (screen == playing) {
      if (exitCode == GameMode.EXIT_MENU) {
        levelIndex = 0;
        setScreen(mainMenu);
      } else if (exitCode == GameMode.EXIT_COMPLETE) {
        levelIndex++;
        if (levelIndex >= levels.length) {
          levelIndex = 0;
          setScreen(mainMenu);
        } else {
          level = levels[levelIndex];
          playing.initLevel(level, manager, false);
        }
      } else if (exitCode == GameMode.EXIT_RESET) {
        playing.initLevel(level, manager, playing.isEditable());
        setScreen(playing);
      } else if (exitCode == GameMode.EXIT_EDIT) {
        setScreen(editing);
      } else {
        Gdx.app.error("GDXRoot", "Exited playing mode with error code " + exitCode, new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == editing) {
      if (exitCode == EditorMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == EditorMode.EXIT_TEST) {
        level = editing.exportLevel();
        playing.initLevel(level, manager, true);
        setScreen(playing);
      } else {
        Gdx.app.exit();
      }
    }
  }
}
