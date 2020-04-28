package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.serialize.*;
import com.glassboxgames.util.*;

public class GDXRoot extends Game implements ScreenListener {
  /** Drawing context to display graphics */
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

  /** Current chapter key */
  private String chapter;
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
  }

  @Override
  public void create() {
    canvas = new GameCanvas();
    loading = new LoadingMode(canvas, manager, this);
    mainMenu = new MainMenu(this);
    playing = new GameMode(canvas, this);
    editing = new EditorMode(this);
    mainMenu.preloadContent(manager);
    playing.preloadContent(manager);
    editing.preloadContent(manager);
    for (String path : Shared.BACKGROUND_PATHS.values()) {
      manager.load(path, Texture.class);
    }
    chapter = "forest";
    setScreen(loading);
  }

  @Override
  public void resize(int width, int height) {
    canvas.resize();
    mainMenu.resize(width, height);
    playing.resize(width, height);
    editing.resize(width, height);
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
    for (String path : Shared.BACKGROUND_PATHS.values()) {
      if (manager.isLoaded(path)) {
        manager.unload(path);
      }
    }
    manager.clear();
    manager.dispose();
    super.dispose();
  }

  @Override
  public void exitScreen(Screen screen, int exitCode) {
    Array<LevelData> levels = Shared.CHAPTER_LEVELS.get(chapter);
    if (screen == loading) {
      if (exitCode == LoadingMode.EXIT_DONE) {
        mainMenu.loadContent(manager);
        playing.loadContent(manager);
        editing.loadContent(manager);
        setScreen(mainMenu);
      } else {
        Gdx.app.error("GDXRoot", "Exited loading mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == mainMenu) {
      if (exitCode == MainMenu.EXIT_PLAY) {
        level = levels.get(levelIndex);
        playing.initLevel(level, manager, false);
        setScreen(playing);
      } else if (exitCode == MainMenu.EXIT_EDITOR) {
        setScreen(editing);
      }
    } else if (screen == playing) {
      if (exitCode == GameMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == GameMode.EXIT_LEVELS) {
        // TODO add level selector
        setScreen(mainMenu);
      } else if (exitCode == GameMode.EXIT_COMPLETE) {
        // TODO add chapter advance
        levelIndex++;
        if (levelIndex >= levels.size) {
          levelIndex = 0;
          level = levels.get(levelIndex);
          setScreen(mainMenu);
        } else {
          level = levels.get(levelIndex);
          playing.initLevel(level, manager, false);
        }
      } else if (exitCode == GameMode.EXIT_RESET) {
        playing.initLevel(level, manager, playing.isEditable());
        setScreen(playing);
      } else if (exitCode == GameMode.EXIT_EDIT) {
        setScreen(editing);
      } else {
        Gdx.app.error("GDXRoot", "Exited playing mode with error code " + exitCode,
                      new RuntimeException());
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
