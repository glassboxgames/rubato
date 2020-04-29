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
  private LoadingMode loadingMode;
  /** Mode for playing the game */
  private GameMode gameMode;
  /** Mode for editing levels */
  private EditorMode editorMode;
  /** Mode for selecting level */
  private SelectMode selectMode;
  /** Mode for settings screen */
  private SettingsMode settingsMode;

  /** Current chapter index */
  private int chapterIndex;
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
    for (String path : Shared.TEXTURE_PATHS.values()) {
      manager.load(path, Texture.class);
    }
    canvas = new GameCanvas();
    loadingMode = new LoadingMode(canvas, manager, this);
    mainMenu = new MainMenu(this);
    gameMode = new GameMode(canvas, this);
    editorMode = new EditorMode(this);
    selectMode = new SelectMode(this);
    settingsMode = new SettingsMode(this);
    mainMenu.preloadContent(manager);
    gameMode.preloadContent(manager);
    editorMode.preloadContent(manager);
    selectMode.preloadContent(manager);
    settingsMode.preloadContent(manager);
    setScreen(loadingMode);
  }

  @Override
  public void resize(int width, int height) {
    canvas.resize();
    mainMenu.resize(width, height);
    gameMode.resize(width, height);
    editorMode.resize(width, height);
    super.resize(width, height);
  }

  @Override
  public void dispose() {
    for (String path : Shared.TEXTURE_PATHS.values()) {
      if (manager.isLoaded(path)) {
        manager.unload(path);
      }
    }
    mainMenu.unloadContent(manager);
    editorMode.unloadContent(manager);
    gameMode.unloadContent(manager);
    selectMode.unloadContent(manager);
    settingsMode.unloadContent(manager);
    mainMenu.dispose();
    editorMode.dispose();
    gameMode.dispose();
    selectMode.dispose();
    settingsMode.dispose();
    setScreen(null);
    canvas.dispose();
    canvas = null;
    manager.clear();
    manager.dispose();
    super.dispose();
  }

  @Override
  public void exitScreen(Screen screen, int exitCode) {
    Array<LevelData> levels = Shared.CHAPTER_LEVELS.get(chapterIndex);
    if (screen == loadingMode) {
      if (exitCode == LoadingMode.EXIT_DONE) {
        for (String key : Shared.TEXTURE_PATHS.keys()) {
          Shared.TEXTURE_MAP.put(key, manager.get(Shared.TEXTURE_PATHS.get(key), Texture.class));
        }
        mainMenu.loadContent(manager);
        gameMode.loadContent(manager);
        editorMode.loadContent(manager);
        selectMode.loadContent(manager);
        settingsMode.loadContent(manager);
        setScreen(mainMenu);
      } else {
        Gdx.app.error("GDXRoot", "Exited loading mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == mainMenu) {
      if (exitCode == MainMenu.EXIT_PLAY) {
        setScreen(selectMode);
      } else if (exitCode == MainMenu.EXIT_EDITOR) {
        setScreen(editorMode);
      } else if (exitCode == MainMenu.EXIT_SETTINGS) {
        setScreen(settingsMode);
      } else if (exitCode == MainMenu.EXIT_QUIT) {
        Gdx.app.exit();
      }
    } else if (screen == selectMode) {
      if (exitCode == SelectMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == SelectMode.EXIT_PLAY) {
        chapterIndex = selectMode.getChapter();
        levelIndex = selectMode.getLevel();
        level = Shared.CHAPTER_LEVELS.get(chapterIndex).get(levelIndex);
        gameMode.initLevel(level, manager, false);
        setScreen(gameMode);
      }
    } else if (screen == gameMode) {
      if (exitCode == GameMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == GameMode.EXIT_LEVELS) {
        setScreen(selectMode);
      } else if (exitCode == GameMode.EXIT_COMPLETE) {
        // TODO add chapter advance
        levelIndex++;
        if (levelIndex >= levels.size) {
          levelIndex = 0;
          level = levels.get(levelIndex);
          setScreen(mainMenu);
        } else {
          level = levels.get(levelIndex);
          gameMode.initLevel(level, manager, false);
        }
      } else if (exitCode == GameMode.EXIT_RESET) {
        gameMode.initLevel(level, manager, gameMode.isEditable());
      } else if (exitCode == GameMode.EXIT_EDIT) {
        setScreen(editorMode);
      } else {
        Gdx.app.error("GDXRoot", "Exited playing mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == editorMode) {
      if (exitCode == EditorMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == EditorMode.EXIT_TEST) {
        level = editorMode.exportLevel();
        gameMode.initLevel(level, manager, true);
        setScreen(gameMode);
      } else {
        Gdx.app.exit();
      }
    } else if (screen == selectMode) {
      if (exitCode == SelectMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == SelectMode.EXIT_PLAY) {
        // TODO: convert pillar index into level index
        // levelIndex = selectMode.getSelectedCheckpoint();
        setScreen(gameMode);
      } else {
        Gdx.app.exit();
      }
    }
    else if (screen == settingsMode) {
      if (exitCode == SelectMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else {
        Gdx.app.exit();
      }
    }
  }
}
