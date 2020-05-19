package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.audio.*;
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
  /** Mode for drawing cutscenes */
  private CutsceneMode cutsceneMode;
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
    for (String path : Shared.SOUND_PATHS.values()) {
      manager.load(path, Sound.class);
    }
    for (String key : Shared.FONT_METADATA.keys()) {
      Array<Float> metadata = Shared.FONT_METADATA.get(key);
      manager.load(key, BitmapFont.class, Shared.createFontLoaderParams(metadata.get(0).intValue(),
                                                                        metadata.get(1).intValue(),
                                                                        metadata.get(2).intValue()));
    }
    canvas = new GameCanvas();
    loadingMode = new LoadingMode(canvas, manager, this);
    mainMenu = new MainMenu(this);
    gameMode = new GameMode(canvas, this);
    cutsceneMode = new CutsceneMode(canvas, this);
    editorMode = new EditorMode(this);
    selectMode = new SelectMode(this);
    settingsMode = new SettingsMode(this);
    gameMode.preloadContent(manager);
    setScreen(loadingMode);
  }

  @Override
  public void resize(int width, int height) {
    canvas.resize();
    mainMenu.resize(width, height);
    gameMode.resize(width, height);
    cutsceneMode.resize(width, height);
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
    for (String path : Shared.SOUND_PATHS.values()) {
      if (manager.isLoaded(path)) {
        manager.unload(path);
      }
    }
    for (String key : Shared.FONT_METADATA.keys()) {
      if (manager.isLoaded(key)) {
        manager.unload(key);
      }
    }
    gameMode.unloadContent(manager);
    mainMenu.dispose();
    gameMode.dispose();
    cutsceneMode.dispose();
    editorMode.dispose();
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
          Texture texture = manager.get(Shared.TEXTURE_PATHS.get(key), Texture.class);
          texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
          Shared.TEXTURE_MAP.put(key, texture);
        }
        for (String key : Shared.SOUND_PATHS.keys()) {
          SoundController.getInstance().allocate(manager, Shared.SOUND_PATHS.get(key));
        }
        for (String key : Shared.FONT_METADATA.keys()) {
          Shared.FONT_MAP.put(key, manager.get(key, BitmapFont.class));
        }

        gameMode.loadContent(manager);
        mainMenu.initUI();
        selectMode.initUI();
        gameMode.initUI();
        editorMode.initUI();
        settingsMode.initUI();

        setScreen(mainMenu);
      } else {
        Gdx.app.error("GDXRoot", "Exited loading mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == mainMenu) {
      if (exitCode == MainMenu.EXIT_PLAY) {
        SaveController save = SaveController.getInstance();
        boolean newGame = save.getLevelsUnlocked(0) == 0;
        if (newGame) {
          level = levels.get(levelIndex);
          if (levels.size > 0) {
            save.setLevelsUnlocked(chapterIndex, 1);
          } else if (chapterIndex < Shared.CHAPTER_NAMES.size - 1) {
            save.setLevelsUnlocked(chapterIndex + 1, 1);
          }
          cutsceneMode.setCutscene(Shared.CHAPTER_NAMES.get(0) + "_cutscene");
          setScreen(cutsceneMode);
        } else {
          setScreen(selectMode);
        }
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
        gameMode.initLevel(level, manager, false, false);
        setScreen(gameMode);
      }
    } else if (screen == gameMode) {
      if (exitCode == GameMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == GameMode.EXIT_LEVELS) {
        setScreen(selectMode);
      } else if (exitCode == GameMode.EXIT_COMPLETE) {
        SaveController save = SaveController.getInstance();
        int unlocked = save.getLevelsUnlocked(chapterIndex);
        if (levelIndex == unlocked - 1) {
          if (unlocked < levels.size) {
            save.setLevelsUnlocked(chapterIndex, unlocked + 1);
          } else if (chapterIndex < Shared.CHAPTER_NAMES.size - 1) {
            unlocked = save.getLevelsUnlocked(chapterIndex + 1);
            if (unlocked < 1) {
              save.setLevelsUnlocked(chapterIndex + 1, 1);
            }
          }
        }
        long millis = gameMode.getLevelTime();
        System.out.printf("completed in %d:%02d.%03d\n",
                          millis / 1000 / 60, millis / 1000 % 60, millis % 1000);
        levelIndex++;
        if (levelIndex >= levels.size) {
          if (chapterIndex < Shared.CHAPTER_NAMES.size - 1) {
            levelIndex = 0;
            chapterIndex++;
            levels = Shared.CHAPTER_LEVELS.get(chapterIndex);
            level = levels.get(levelIndex);
            cutsceneMode.setCutscene(Shared.CHAPTER_NAMES.get(chapterIndex) + "_cutscene");
          } else {
            level = null;
            cutsceneMode.setCutscene("end_cutscene");
          }
          setScreen(cutsceneMode);
        } else {
          level = levels.get(levelIndex);
          gameMode.initLevel(level, manager, levelIndex == levels.size - 1, false);
        }
      } else if (exitCode == GameMode.EXIT_RESET) {
        gameMode.initLevel(level, manager, gameMode.isCompletion(), gameMode.isEditable());
      } else if (exitCode == GameMode.EXIT_EDIT) {
        setScreen(editorMode);
      } else if (exitCode == GameMode.EXIT_CHECKPOINT) {
        // checkpoint logic
      } else {
        Gdx.app.error("GDXRoot", "Exited playing mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == cutsceneMode) {
      if (exitCode == CutsceneMode.EXIT_ESCAPE) {
        setScreen(selectMode);
      } else if (exitCode == CutsceneMode.EXIT_COMPLETE) {
        if (level != null) {
          gameMode.initLevel(level, manager, false, false);
          setScreen(gameMode);
        } else {
          // TODO credit screen?
          setScreen(mainMenu);
        }
      } else {
        Gdx.app.exit();
      }
    } else if (screen == editorMode) {
      if (exitCode == EditorMode.EXIT_MENU) {
        setScreen(mainMenu);
      } else if (exitCode == EditorMode.EXIT_TEST) {
        level = editorMode.exportLevel();
        gameMode.initLevel(level, manager, false, true);
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
