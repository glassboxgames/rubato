package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.serialize.*;
import com.glassboxgames.util.*;

public class GDXRoot extends Game implements ScreenListener {
  /** Fade state values */
  private static final int FADE_NONE = 0;
  private static final int FADE_OUT = 1;
  private static final int FADE_DELAY = 2;
  private static final int FADE_IN = 3;
  private static final int NUM_FADE_STATES = 4;

  /** Fade state duration */
  private static final int FADE_STATE_DURATION = 15;

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

  /** Next screen for transition */
  private Screen nextScreen;
  /** Current fade state */
  private int fadeState;
  /** Current fade state counter */
  private int fadeCount;
  /** Whether the credits have been shown */
  private boolean credits;

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

  /**
   * Starts the transition to the given screen.
   */
  public void setNextScreen(Screen next) {
    if (fadeState == FADE_NONE) {
      nextScreen = next;
      fadeState = FADE_OUT;
      fadeCount = 0;
    }
  }

  @Override
  public void render() {
    super.render();
    if (fadeState != FADE_NONE) {
      float alpha;
      switch (fadeState) {
      case FADE_OUT:
        alpha = (float)fadeCount / FADE_STATE_DURATION;
        break;
      case FADE_IN:
        alpha = 1 - (float)fadeCount / FADE_STATE_DURATION;
        break;
      default:
        alpha = 1;
        break;
      }
      Shared.drawOverlay(alpha);
      fadeCount++;
      if (fadeCount > FADE_STATE_DURATION) {
        if (fadeState == FADE_DELAY) {
          setScreen(nextScreen);
        }
        fadeCount = 0;
        fadeState = (fadeState + 1) % NUM_FADE_STATES;
      }
    }
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
    MusicController.getInstance().dispose();
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
    SoundController soundController = SoundController.getInstance();
    SaveController saveController = SaveController.getInstance();
    Array<LevelData> levels = Shared.CHAPTER_LEVELS.get(chapterIndex);
    if (screen == loadingMode) {
      if (exitCode == LoadingMode.EXIT_DONE) {
        for (String key : Shared.TEXTURE_PATHS.keys()) {
          Texture texture = manager.get(Shared.TEXTURE_PATHS.get(key), Texture.class);
          texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
          Shared.TEXTURE_MAP.put(key, texture);
        }
        for (String key : Shared.SOUND_PATHS.keys()) {
          soundController.allocate(manager, Shared.getSoundPath(key));
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

        soundController.setVolume(saveController.getSoundVolume());
        setNextScreen(mainMenu);

        // Pixmap pixmap = new Pixmap(Gdx.files.internal(CURSOR_FILE));
        // int xHotspot = pixmap.getWidth() / 2;
        // int yHotspot = pixmap.getHeight() / 2;
        // Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot));
        // pixmap.dispose();
      } else {
        Gdx.app.error("GDXRoot", "Exited loading mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == mainMenu) {
      if (exitCode == MainMenu.EXIT_PLAY) {
        if (saveController.getLevelsUnlocked(0) == 0) {
          level = levels.get(levelIndex);
          if (levels.size > 0) {
            saveController.setLevelsUnlocked(chapterIndex, 1);
          } else if (chapterIndex < Shared.CHAPTER_NAMES.size - 1) {
            saveController.setLevelsUnlocked(chapterIndex + 1, 1);
          }
          cutsceneMode.setNextCutscene(Shared.CHAPTER_NAMES.get(0) + "_cutscene", 60, 1);
          setNextScreen(cutsceneMode);
        } else {
          setNextScreen(selectMode);
        }
      } else if (exitCode == MainMenu.EXIT_EDITOR) {
        setNextScreen(editorMode);
      } else if (exitCode == MainMenu.EXIT_SETTINGS) {
        setNextScreen(settingsMode);
      } else if (exitCode == MainMenu.EXIT_QUIT) {
        Gdx.app.exit();
      }
    } else if (screen == selectMode) {
      if (exitCode == SelectMode.EXIT_MENU) {
        setNextScreen(mainMenu);
      } else if (exitCode == SelectMode.EXIT_PLAY) {
        chapterIndex = selectMode.getChapter();
        levelIndex = selectMode.getLevel();
        level = Shared.CHAPTER_LEVELS.get(chapterIndex).get(levelIndex);
        gameMode.setNextLevel(level, false);
        setNextScreen(gameMode);
      }
    } else if (screen == gameMode) {
      if (exitCode == GameMode.EXIT_MENU) {
        setNextScreen(mainMenu);
      } else if (exitCode == GameMode.EXIT_LEVELS) {
        setNextScreen(selectMode);
      } else if (exitCode == GameMode.EXIT_COMPLETE) {
        int unlocked = saveController.getLevelsUnlocked(chapterIndex);
        if (levelIndex == unlocked - 1) {
          if (unlocked < 9 || (chapterIndex != 3 && unlocked < 10)) {
            saveController.setLevelsUnlocked(chapterIndex, unlocked + 1);
          } else if (chapterIndex < Shared.CHAPTER_NAMES.size - 1) {
            unlocked = saveController.getLevelsUnlocked(chapterIndex + 1);
            if (unlocked < 1) {
              saveController.setLevelsUnlocked(chapterIndex + 1, 1);
            }
          }
        }
        levelIndex++;
        if (levelIndex >= levels.size) {
          if (chapterIndex < Shared.CHAPTER_NAMES.size - 1) {
            levelIndex = 0;
            chapterIndex++;
            levels = Shared.CHAPTER_LEVELS.get(chapterIndex);
            level = levels.get(levelIndex);
            cutsceneMode.setNextCutscene(Shared.CHAPTER_NAMES.get(chapterIndex) + "_cutscene", 60, 1);
          } else {
            level = null;
            cutsceneMode.setNextCutscene("end_cutscene", 60, 1);
          }
          setNextScreen(cutsceneMode);
        } else {
          level = levels.get(levelIndex);
          gameMode.setNextLevel(level, false);
          setNextScreen(gameMode);
        }
      } else if (exitCode == GameMode.EXIT_RESET) {
        setNextScreen(gameMode);
      } else if (exitCode == GameMode.EXIT_EDIT) {
        setNextScreen(editorMode);
      } else if (exitCode == GameMode.EXIT_CHECKPOINT) {
        // checkpoint logic
      } else {
        Gdx.app.error("GDXRoot", "Exited playing mode with error code " + exitCode,
                      new RuntimeException());
        Gdx.app.exit();
      }
    } else if (screen == cutsceneMode) {
      if (exitCode == CutsceneMode.EXIT_ESCAPE) {
        setNextScreen(selectMode);
      } else if (exitCode == CutsceneMode.EXIT_COMPLETE) {
        if (level != null) {
          gameMode.setNextLevel(level, false);
          setNextScreen(gameMode);
        } else if (!credits) {
          MusicController.getInstance().play("adagio");
          credits = true;
          cutsceneMode.setNextCutscene("credits", 240, 1);
          setNextScreen(cutsceneMode);
        } else {
          credits = false;
          setNextScreen(mainMenu);
        }
      } else {
        Gdx.app.exit();
      }
    } else if (screen == editorMode) {
      if (exitCode == EditorMode.EXIT_MENU) {
        setNextScreen(mainMenu);
      } else if (exitCode == EditorMode.EXIT_TEST) {
        level = editorMode.exportLevel();
        gameMode.setNextLevel(level, true);
        setNextScreen(gameMode);
      } else {
        Gdx.app.exit();
      }
    } else if (screen == selectMode) {
      if (exitCode == SelectMode.EXIT_MENU) {
        setNextScreen(mainMenu);
      } else if (exitCode == SelectMode.EXIT_PLAY) {
        setNextScreen(gameMode);
      } else {
        Gdx.app.exit();
      }
    }
    else if (screen == settingsMode) {
      if (exitCode == SelectMode.EXIT_MENU) {
        setNextScreen(mainMenu);
      } else {
        Gdx.app.exit();
      }
    }
  }
}
