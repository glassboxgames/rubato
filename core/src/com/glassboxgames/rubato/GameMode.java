package com.glassboxgames.rubato;

import java.text.DecimalFormat;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
import com.glassboxgames.rubato.entity.Tooltip;
import com.glassboxgames.rubato.serialize.*;
import com.glassboxgames.util.*;

/**
 * Mode controller for the main gameplay loop.
 */
public class GameMode implements Screen {
  /**
   * Enumeration of possible game states.
   */
  public enum GameState {
    /** Before the game has started */
    INTRO,
    /** While we are playing the game */
    PLAY,
    /** In the final altar scene */
    ALTAR,
  }

  /** Exit code for returning to the menu */
  public static final int EXIT_MENU = 0;
  /** Exit code for returning to the level selector */
  public static final int EXIT_LEVELS = 1;
  /** Exit code for completing the level */
  public static final int EXIT_COMPLETE = 2;
  /** Exit code for resetting the level */
  public static final int EXIT_RESET = 3;
  /** Exit code for editing the level */
  public static final int EXIT_EDIT = 4;
  /** Exit code for reaching the checkpoint */
  public static final int EXIT_CHECKPOINT = 5;

  /** Pause code for resuming */
  public static final int PAUSE_RESUME = 0;
  /** Pause code for resetting */
  public static final int PAUSE_RESET = 1;
  /** Pause code for returning to level selector */
  public static final int PAUSE_LEVELS = 2;
  /** Pause code for returning to main menu */
  public static final int PAUSE_MENU = 3;

  /** Dev mode draw offset */
  private static final float DEV_DRAW_OFFSET = 20f;

  /** Gravity **/
  private static final float GRAVITY = -50f;
  /** Bottom boundary */
  private static final float Y_BOUND = 0.3f;

  /** Array tracking all loaded assets (for unloading purposes) */
  private Array<String> assets;
  
  /** Whether this screen is in the process of exiting */
  private boolean exiting;
  /** Exit code for when the screen exits */
  private int exitCode;

  /** Canvas on which to draw content */
  private GameCanvas canvas;
  /** Listener to handle exiting */
  private ScreenListener listener;
  /** Current state of the game */
  private GameState gameState;
  /** Whether this game mode is active */
  private boolean active;
  /** The Box2D world */
  private World world;

  /** Whether this game mode is paused */
  private boolean paused;
  /** Pause menu buttons */
  private Array<Button> pauseButtons;
  /** Pause menu button group */
  private HorizontalGroup pauseButtonGroup;

  /** Chapter info label */
  private Label chapterInfo;
  /** Chapter icon */
  private Image chapterIcon;
  /** Chapter complete label */
  private Label chapterComplete;
  /** Chapter time label */
  private Label chapterTime;

  /** Game stage */
  private Stage gameStage;
  /** Pause menu stage */
  private Stage pauseStage;
  /** The stage for chapter completion */
  private Stage chapterStage;

  /** List of all entity states currently loaded */
  private Array<State> states;
  /** Current level */
  private LevelContainer level;

  /** Upper left corner of the visible canvas **/
  private Vector2 uiPos;
  /** Whether debug mode is on */
  private boolean debug;
  /** Whether dev mode is on */
  private boolean devMode;
  /** Numerical selector for dev mode */
  private int devSelect;
  /** Whether the current level is editable */
  private boolean editable;
  /** When the player timer started (in ms); resets on resume */
  private long startTime;

  /** Data for the next level */
  private LevelData nextData;
  /** Editable status for the next level */
  private boolean nextEditable;
  
  /**
   * Instantiate a GameMode.
   * @param canvas the canvas to draw on
   * @param listener the listener for exiting the screen
   */
  public GameMode(GameCanvas canvas, ScreenListener listener) {
    this.canvas = canvas;
    this.listener = listener;

    assets = new Array();
    uiPos = new Vector2();
    devSelect = -1;

    world = new World(new Vector2(0, GRAVITY), false);
    world.setContactListener(CollisionController.getInstance());

    // Initialize entity state machines
    states = new Array<State>();
    states.addAll(Player.initStates());
    states.addAll(Checkpoint.initStates());
    states.addAll(Platform.initStates());
    states.addAll(Projectile.initStates());
    states.addAll(Spider.initStates());
    states.addAll(Wisp.initStates());
    states.addAll(Wyrm.initStates());
    states.addAll(Blob.initStates());
    states.addAll(Altar.initStates());
    states.addAll(Tooltip.initStates());
  }

  /**
   * Adds the assets to load for the game to the given manager.
   * @param manager asset manager to use
   */
  public void preloadContent(AssetManager manager) {
    for (State state : states) {
      state.preloadContent(manager);
    }
  }

  /**
   * Pulls the loaded assets from the asset manager for the game.
   * @param manager the asset manager to use
   */
  public void loadContent(AssetManager manager) {
    for (State state : states) {
      state.loadContent(manager);
    }
  }

  /**
   * Initializes the game mode UI.
   */
  public void initUI() {
    // pause button
    gameStage = new Stage();
    ImageButton pauseButton = new ImageButton(Shared.getDrawable("pause_icon"));
    pauseButton.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        pauseGame();
      }
    });
    pauseButton.setX(20);
    pauseButton.setY(Gdx.graphics.getHeight() - pauseButton.getHeight() - 20);
    gameStage.addActor(pauseButton);

    // pause menu
    pauseStage = new Stage();
    Table pauseTable = new Table();
    pauseTable.setFillParent(true);
    pauseTable.add(new Label("paused",
                             new Label.LabelStyle(Shared.getFont("game.pause_text.ttf"), Color.WHITE)));
    pauseTable.row();

    pauseButtons = new Array<Button>();
    pauseButtonGroup = new HorizontalGroup();
    addPauseButton(PAUSE_RESUME, "resume");
    addPauseButton(PAUSE_RESET, "reset");
    addPauseButton(PAUSE_LEVELS, "levels");
    addPauseButton(PAUSE_MENU, "menu");
    pauseButtonGroup.space(50).padTop(50).rowBottom();

    pauseTable.add(pauseButtonGroup);
    pauseStage.addActor(pauseTable);

    // chapter completion
    chapterStage = new Stage();
    Table chapterTable = new Table();
    chapterStage.addActor(chapterTable);
    chapterTable.setFillParent(true);
    chapterTable.top();

    chapterInfo = new Label("",
      new Label.LabelStyle(Shared.getFont("game.chapter_name.ttf"), Color.WHITE));
    chapterIcon = new Image();
    chapterComplete = new Label("COMPLETE",
      new Label.LabelStyle(Shared.getFont("game.chapter_complete.ttf"), Color.WHITE));
    chapterTime = new Label("",
      new Label.LabelStyle(Shared.getFont("game.chapter_time.ttf"), Color.WHITE));

    chapterTable.add(chapterInfo).pad(40, 0, 10, 0).row();
    chapterTable.add(chapterIcon).row();
    chapterTable.add(chapterComplete).pad(10, 0, 0, 0).row();
    chapterTable.add(chapterTime).pad(0, 0, 10, 0).row();
  }

  /**
   * Adds a button to the pause menu.
   */
  private void addPauseButton(final int index, String key) {
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.imageUp = Shared.getDrawable(key + "_deselected");
    style.imageOver = Shared.getDrawable(key + "_selected");
    final ImageButton button = new ImageButton(style);
    button.row();
    button.add(new Label(key, new Label.LabelStyle(Shared.getFont("game.pause_label.ttf"), Color.WHITE)))
      .padTop(20);
    button.setWidth(100);
    button.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        switch (index) {
        case PAUSE_RESUME:
          resumeGame();
          break;
        case PAUSE_RESET:
          startExit(EXIT_RESET);
          break;
        case PAUSE_LEVELS:
          startExit(EXIT_LEVELS);
          break;
        case PAUSE_MENU:
          startExit(EXIT_MENU);
          break;
        }
      }
    });
    pauseButtonGroup.addActor(button);
    pauseButtons.add(button);
  }

  /**
   * Unloads the assets loaded for the game.
   * @param manager the asset manager to use
   */
  public void unloadContent(AssetManager manager) {
    for (String s : assets) {
      if (manager.isLoaded(s)) {
        manager.unload(s);
      }
    }
    for (State state : states) {
      state.unloadContent(manager);
    }
  }

  /**
   * Sets the level data and parameters for the next level.
   * @param data the serialized level data
   * @param editable whether the level is editable
   */
  public void setNextLevel(LevelData data, boolean editable) {
    nextData = data;
    nextEditable = editable;
  }

  /**
   * Creates the world level.
   */
  public void initLevel() {
    if (level != null) {
      level.deactivatePhysics(world);
    }

    editable = nextEditable;
    level = new LevelContainer(nextData);
    chapterInfo.setVisible(false);
    String chapter = level.getChapter();
    chapterIcon.setDrawable(Shared.getDrawable(chapter + "_plain"));
    chapterComplete.setVisible(false);
    chapterTime.setVisible(false);

    gameState = GameState.INTRO;
    exiting = false;
    resumeGame();
    startTime = TimeUtils.millis();

    MusicController.getInstance().play(chapter);
  }

  /**
   * Populates the chapter completion stage.
   */
  public void setChapterCompletion() {
    String chapter = level.getChapter();
    int chapterNumber = Shared.CHAPTER_NAMES.indexOf(chapter, false) + 1;
    chapterInfo.setText("CHAPTER " + chapterNumber + ": " + chapter.toUpperCase());
    chapterInfo.setVisible(true);
    chapterIcon.setDrawable(Shared.getDrawable(chapter + "_complete"));
    chapterIcon.setVisible(true);
    chapterComplete.setVisible(true);
    long time = SaveController.getInstance().getTimeSpent(level.getChapter());
    chapterTime.setText(Shared.formatTime(time));
    chapterTime.setVisible(true);
  }

  /**
   * Returns whether the level is editable.
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * Checks if any sound effects need to be played
   *
   */
  private void updateSound() {
    SoundController soundController = SoundController.getInstance();
    if (gameState != GameState.INTRO) {
      Player player = level.getPlayer();
      String runSound = Shared.getSoundPath("run_grass");
      if (player.isRunning() && !paused) {
        if (!soundController.isActive(runSound)) {
          soundController.play(runSound, runSound, true);
        }
      } else {
        if (soundController.isActive(runSound)) {
          soundController.stop(runSound);
        }
      }
      if (level.getCheckpoint() != null && level.getCheckpoint().wasJustActivated()) {
        if (level.isCompletion()) {
          MusicController.getInstance().stop();
          String chapterSound = Shared.SOUND_PATHS.get("chapter");
          SoundController.getInstance().play(chapterSound, chapterSound, false);
        } else {
          String checkpointSound = Shared.SOUND_PATHS.get("checkpoint");
          SoundController.getInstance().play(checkpointSound, checkpointSound, false);
        }
      }
    }
    soundController.update();
  }

  /**
   * Exits with the given exit code, triggering a fade out.
   */
  private void startExit(int code) {
    if (!exiting) {
      if (!paused && level.getAltar() == null) {
        SaveController.getInstance().addTimeSpent(level.getChapter(),
                                                  TimeUtils.timeSinceMillis(startTime));
      }
      exiting = true;
      listener.exitScreen(this, code);
    }
  }

  /**
   * Updates the state of the game.
   * @param delta time in seconds since last frame
   */
  private void update(float delta) {
    InputController input = InputController.getInstance();
    input.readInput();
    
    if (paused) {
      if (input.pressedExit()) {
        resumeGame();
      }
      pauseStage.act(delta);
    } else if (gameState == GameState.INTRO) {
      if (level != null) {
        level.activatePhysics(world);
        gameState = GameState.PLAY;
      }
    } else if (gameState == GameState.PLAY) {
      if (level.isCompletion()) {
        chapterStage.act(delta);
      }
      
      if (input.pressedExit()) {
        if (editable) {
          startExit(EXIT_EDIT);
        } else {
          pauseGame();
        }          
        return;
      }
      if (input.pressedDebug()) {
        debug = !debug;
      }
      if (input.pressedDevMode()) {
        devMode = !devMode;
        devSelect = -1;
      }
      if (input.pressedReset()) {
        level.getPlayer().setAlive(false);
      }
      if (devMode) {
        if (input.getDevSelect() != -1) {
          devSelect = input.getDevSelect();
        }
        int devChange = input.getDevChange();
        if (devChange != 0) {
          switch (devSelect) {
          case -1:
            break;
          case 1:
            // Player.jumpImpulse = (float) (Math.round((Player.jumpImpulse + devChange * 0.05) * 100.0) / 100.0); // handle precision error
            break;
          case 2:
            Player.maxXSpeed += devChange * 0.5;
            break;
          case 3:
            Player.maxYSpeed += devChange * 0.5;
            break;
          case 4:
            Player.minJumpDuration += devChange * 1;
            break;
          case 5:
            Player.maxJumpDuration += devChange * 1;
            break;
          }
        }
      }

      Player player = level.getPlayer();
      if (player.isActive()) {
        Vector2 pos = player.getPosition();

        if (pos.x >= level.getWidth()) {
          if (editable) {
            startExit(EXIT_RESET);
          } else {
            startExit(EXIT_COMPLETE);
          }
        }

        if (pos.y < Y_BOUND) {
          player.setAlive(false);
        }

        int horizontal = 0;
        if (input.heldLeft()) {
          horizontal -= 1;
        }
        if (input.heldRight()) {
          horizontal += 1;
        }
          
        player.setInput(horizontal);
        player.tryFace();

        if (input.pressedJump()) {
          player.tryJump();
        } else if (input.heldJump()) {
          player.tryExtendJump();
        }

        if (input.pressedAttack()) {
          player.tryAttack();
        }

        player.update(delta);
      } else {
        startExit(EXIT_RESET);
      }

      Array<Enemy> enemies = level.getEnemies();
      Array<Enemy> removedEnemies = new Array<Enemy>();
      Array<Enemy> addedEnemies = new Array<Enemy>();
      for (Enemy enemy : enemies) {
        if (enemy.shouldRemove()) {
          enemy.deactivatePhysics(world);
          removedEnemies.add(enemy);
        } else {
          enemy.update(delta);
          if (enemy instanceof Wisp) {
            Array<Enemy> spawned = ((Wisp) enemy).getSpawned();
            for (Enemy spawn : spawned) {
              spawn.activatePhysics(world);
              addedEnemies.add(spawn);
            }
            spawned.clear();
          }
        }
      }
      enemies.removeAll(removedEnemies, true);
      enemies.addAll(addedEnemies);

      Array<Platform> platforms = level.getPlatforms();
      Array<Platform> removedPlatforms = new Array<Platform>();
      for (Platform platform : platforms) {
        if (platform.shouldRemove()) {
          platform.deactivatePhysics(world);
          removedPlatforms.add(platform);
        } else {
          platform.update(delta);
        }
      }
      platforms.removeAll(removedPlatforms, true);
        
      Checkpoint checkpoint = level.getCheckpoint();
      if (checkpoint != null) {
        checkpoint.update(delta);
        if (checkpoint.wasJustActivated()) {
          if (level.isCompletion()) {
            setChapterCompletion();
          }
          level.removeRightWall();
          // listener.exitScreen(this, EXIT_CHECKPOINT);
        }
      }

      Altar altar = level.getAltar();
      if (altar != null) {
        altar.update(delta);
        if (altar.isPlayerSeen() && !editable) {
          gameState = GameState.ALTAR;
        }
      }

      Array<Tooltip> tooltips = level.getTooltips();
      for (Tooltip tooltip : tooltips) {
        tooltip.update(delta);
      }

      if (player.isActive()) {
        player.sync();
      }
      for (Enemy enemy : level.getEnemies()) {
        enemy.sync();
      }
      for (Platform platform : level.getPlatforms()) {
        platform.sync();
      }
      if (checkpoint != null) {
        checkpoint.sync();
      }
      if (altar != null) {
        altar.sync();
      }
      for (Tooltip tooltip : tooltips) {
        tooltip.sync();
      }

      world.step(1 / 60f, 8, 3);
      gameStage.act(delta);
    } else if (gameState == GameState.ALTAR) {
      if (input.pressedExit()) {
        if (editable) {
          startExit(EXIT_EDIT);
        } else {
          pauseGame();
        }          
        return;
      }
      Player player = level.getPlayer();
      if (level.getAltar().isPlayerClose()) {
        player.setInput(0);
        player.startEnd();
        if (player.getCount() > player.getState().getLength()) {
          startExit(EXIT_COMPLETE);
        }
      } else {
        player.setInput(1);
      }
      player.update(delta);
      player.sync();

      world.step(1 / 60f, 8, 3);
      gameStage.act(delta);
    }

    updateSound();
  }

  /**
   * Draw the current game state to the canvas.
   */
  private void draw() {
    canvas.clear();

    if (gameState != GameState.INTRO) {
      level.drawBackground(canvas);
      if (level.isCompletion()) {
        Shared.drawOverlay(0.2f);
        chapterStage.draw();
      }
      level.drawEntities(canvas);
      if (debug) {
        level.drawDebug(canvas);
      }

      Player player = level.getPlayer();
      if (!exiting && player.isActive()) {
        Vector2 delta = player.getPosition().scl(Shared.PPM).sub(canvas.getCameraPos()).scl(0.25f);
        float width = level.getWidth() * Shared.PPM;
        float height = level.getHeight() * Shared.PPM;
        canvas.moveCamera(canvas.getCameraPos().add(delta), width, height);
        Vector2 pos = canvas.getCameraPos();
        uiPos.set(MathUtils.clamp(pos.x - canvas.getWidth() / 2,
                                  0, width - canvas.getWidth()) + DEV_DRAW_OFFSET,
                  MathUtils.clamp(pos.y + canvas.getHeight() / 2,
                                  canvas.getHeight(), height) - DEV_DRAW_OFFSET);
      }

      if (devMode) {
        float xOffset = uiPos.x;
        float yOffset = uiPos.y - DEV_DRAW_OFFSET;
        float deltaOffset = 2 * DEV_DRAW_OFFSET;
        canvas.begin();
        // drawText(1, "Jump Impulse", Player.jumpImpulse, Player.JUMP_IMPULSE,
        //          xOffset, yOffset);
        drawText(2, "Max X Speed", Player.maxXSpeed, Player.MAX_X_SPEED,
                 xOffset, yOffset - deltaOffset);
        drawText(3, "Max Y Speed", Player.maxYSpeed, Player.MAX_Y_SPEED,
                 xOffset, yOffset - 2 * deltaOffset);
        drawText(4, "Min Jump Duration", Player.minJumpDuration, Player.MIN_JUMP_DURATION,
                 xOffset, yOffset - 3 * deltaOffset);
        drawText(5, "Max Jump Duration", Player.maxJumpDuration, Player.MAX_JUMP_DURATION,
                 xOffset, yOffset - 4 * deltaOffset);
        // drawText(6, "Dash Duration", Player.dashDuration, Player.DASH_DURATION,
        //  xOffset, yOffset - 5 * deltaOffset);
        // drawText(7, "Dash Speed", Player.dashSpeed, Player.DASH_SPEED,
        //  xOffset, yOffset - 6 * deltaOffset);
        // drawText(0, "Enemy Damage", Enemy.damage, Enemy.DAMAGE,
        //          xOffset, yOffset - 9 * deltaOffset);
        canvas.end();
      }

      if (paused) {
        Shared.drawOverlay(0.4f);
        pauseStage.draw();
      } else {
        gameStage.draw();
      }
    }
  }

  /**
   * Draws text to the screen for a parameter in dev mode.
   * @param num number key for this dev mode parameter
   * @param name name of the parameter
   * @param value the current value of the parameter
   * @param oldValue the original value of the parameter
   * @param x the x position of the text
   * @param y the y position of the text
   */
  private void drawText(int num, String name, float value, float oldValue,
                        float x, float y) {
    Color color;
    if (num == devSelect) {
      if (value != oldValue) {
        color = Color.RED;
      } else {
        color = Color.WHITE;
      }
    } else {
      if (value != oldValue) {
        color = Color.FIREBRICK;
      } else {
        color = Color.BLACK;
      }
    }
    String text =
      "[" + num + "] " + name + ": " + new DecimalFormat("#.##").format(value);
    canvas.drawText(text, Shared.getFont("game.dev.ttf"), color, x, y);
  }

  @Override
  public void render(float delta) {
    if (active) {
      update(delta);
      draw();
    }
  }

  /**
   * Pauses the game.
   */
  private void pauseGame() {
    if (!editable && level.getAltar() == null) {
      SaveController.getInstance().addTimeSpent(level.getChapter(), TimeUtils.timeSinceMillis(startTime));
    }
    Gdx.input.setInputProcessor(pauseStage);
    paused = true;
  }

  /**
   * Resumes the game.
   */
  private void resumeGame() {
    startTime = TimeUtils.millis();
    Gdx.input.setInputProcessor(gameStage);
    paused = false;
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void show() {
    initLevel();
    Gdx.input.setInputProcessor(gameStage);
    active = true;
  }

  @Override
  public void hide() {
    Gdx.input.setInputProcessor(null);
    active = false;
  }

  @Override
  public void dispose() {
    if (gameStage != null) {
      gameStage.dispose();
    }
    if (pauseStage != null) {
      pauseStage.dispose();
    }
    if (chapterStage != null) {
      chapterStage.dispose();
    }
    if (world != null) {
      world.dispose();
      world = null;
    }
    level = null;
  }
}
