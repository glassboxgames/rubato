package com.glassboxgames.rubato;

import java.text.DecimalFormat;
import com.badlogic.gdx.*;
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

  /** Pause code for resuming */
  public static final int PAUSE_RESUME = 0;
  /** Pause code for resetting */
  public static final int PAUSE_RESET = 1;
  /** Pause code for returning to level selector */
  public static final int PAUSE_LEVELS = 2;
  /** Pause code for returning to main menu */
  public static final int PAUSE_MENU = 3;

  /** Dev mode font key */
  private static final String DEV_FONT = "game_mode_dev_font.ttf";
  /** Dev mode font size */
  private static final int DEV_FONT_SIZE = 24;
  /** Dev mode draw offset */
  private static final float DEV_DRAW_OFFSET = 20f;

  /** Pause menu overlay */
  private static final String PAUSE_OVERLAY_FILE = "User Interface/Play Screen/pause_overlay.png";
  /** Pause menu label font key */
  private static final String PAUSE_LABEL_FONT = "game_mode_pause_label_font.ttf";
  /** Pause menu label font size */
  private static final int PAUSE_LABEL_FONT_SIZE = 36;
  /** Pause menu subtext font key */
  private static final String PAUSE_SUBTEXT_FONT = "game_mode_pause_subtext_font.ttf";
  /** Pause menu subtext font size */
  private static final int PAUSE_SUBTEXT_FONT_SIZE = 48;

  /** Texture paths */
  private static final String RESUME_DEFAULT_FILE = "User Interface/Buttons/resume_button.png";
  private static final String RESUME_HIGHLIGHT_FILE = "User Interface/Buttons/resume_button_highlighted.png";
  private static final String RESET_DEFAULT_FILE = "User Interface/Buttons/reset_button.png";
  private static final String RESET_HIGHLIGHT_FILE = "User Interface/Buttons/reset_button_highlighted.png";
  private static final String LEVELS_DEFAULT_FILE = "User Interface/Buttons/levels_button.png";
  private static final String LEVELS_HIGHLIGHT_FILE = "User Interface/Buttons/levels_button_highlighted.png";
  private static final String MENU_DEFAULT_FILE = "User Interface/Buttons/menu_button.png";
  private static final String MENU_HIGHLIGHT_FILE = "User Interface/Buttons/menu_button_highlighted.png";

  /** Gravity **/
  private static final float GRAVITY = -50f;
  /** Boundary thresholds */
  private static final float X_BOUND = 1;
  private static final float Y_BOUND = 2;
  
  /** Pause overlay texture */
  private Texture pauseOverlay;
  /** Resume button textures */
  private Texture resumeDefault, resumeHighlight;
  /** Reset button textures */
  private Texture resetDefault, resetHighlight;
  /** Level selector button textures */
  private Texture levelsDefault, levelsHighlight;
  /** Back to menu button textures */
  private Texture menuDefault, menuHighlight;
  /** Fonts for pause menu */
  private BitmapFont pauseLabelFont, pauseSubtextFont;
  /** Font for displaying dev mode options */
  private BitmapFont devModeFont;
  /** The background image */
  private Texture background;
  /** Array tracking all loaded assets (for unloading purposes) */
  private Array<String> assets;

  /** Canvas on which to draw content */
  protected GameCanvas canvas;
  /** Listener to handle exiting */
  protected ScreenListener listener;
  /** Current state of the game */
  protected GameState gameState;
  /** Whether this game mode is active */
  protected boolean active;
  /** The Box2D world */
  protected World world;

  /** Whether this game mode is paused */
  protected boolean paused;
  /** Pause menu stage */
  protected Stage pauseStage;
  /** Pause menu table */
  protected Table pauseTable;
  /** Pause menu button group */
  protected HorizontalGroup pauseButtons;
  /** Pause menu button index */
  protected int pauseIndex;

  /** List of all entity states currently loaded */
  protected Array<State> states;
  /** Current level */
  protected LevelContainer level;
  /** Position of the camera */
  protected Vector2 cameraPos;

  /** Upper left corner of the visible canvas **/
  protected Vector2 uiPos;
  /** Whether debug mode is on */
  protected boolean debug;
  /** Whether dev mode is on */
  protected boolean devMode;
  /** Numerical selector for dev mode */
  protected int devSelect;
  /** Whether the current level is editable */
  protected boolean editable;

  /**
   * Instantiate a GameMode.
   * @param canvas the canvas to draw on
   * @param listener the listener for exiting the screen
   */
  public GameMode(GameCanvas canvas, ScreenListener listener) {
    this.canvas = canvas;
    this.listener = listener;

    assets = new Array();
    gameState = GameState.INTRO;
    cameraPos = new Vector2();
    uiPos = new Vector2();
    devSelect = -1;

    // Initialize game world
    world = new World(new Vector2(0, GRAVITY), false);
    world.setContactListener(CollisionController.getInstance());

    // Initialize entity state machines
    states = new Array<State>();
    states.addAll(Player.initStates());
    states.addAll(Shard.initStates());
    states.addAll(Platform.initStates());
    states.addAll(Checkpoint.initStates());
    states.addAll(Projectile.initStates());
    states.addAll(Spider.initStates());
    states.addAll(Wisp.initStates());
    states.addAll(Wyrm.initStates());
  }

  /**
   * Adds the assets to load for the game to the given manager.
   * @param manager asset manager to use
   */
  public void preloadContent(AssetManager manager) {
    manager.load(DEV_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.REGULAR_FONT_FILE,
                                               DEV_FONT_SIZE));
    assets.add(DEV_FONT);
    manager.load(PAUSE_LABEL_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.SEMIBOLD_FONT_FILE,
                                               PAUSE_LABEL_FONT_SIZE));
    assets.add(PAUSE_LABEL_FONT);
    manager.load(PAUSE_SUBTEXT_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.REGULAR_FONT_FILE,
                                               DEV_FONT_SIZE));
    assets.add(PAUSE_SUBTEXT_FONT);

    manager.load(PAUSE_OVERLAY_FILE, Texture.class);
    assets.add(PAUSE_OVERLAY_FILE);
    manager.load(RESUME_DEFAULT_FILE, Texture.class);
    assets.add(RESUME_DEFAULT_FILE);
    manager.load(RESUME_HIGHLIGHT_FILE, Texture.class);
    assets.add(RESUME_HIGHLIGHT_FILE);
    manager.load(RESET_DEFAULT_FILE, Texture.class);
    assets.add(RESET_DEFAULT_FILE);
    manager.load(RESET_HIGHLIGHT_FILE, Texture.class);
    assets.add(RESET_HIGHLIGHT_FILE);
    manager.load(LEVELS_DEFAULT_FILE, Texture.class);
    assets.add(LEVELS_DEFAULT_FILE);
    manager.load(LEVELS_HIGHLIGHT_FILE, Texture.class);
    assets.add(LEVELS_HIGHLIGHT_FILE);
    manager.load(MENU_DEFAULT_FILE, Texture.class);
    assets.add(MENU_DEFAULT_FILE);
    manager.load(MENU_HIGHLIGHT_FILE, Texture.class);
    assets.add(MENU_HIGHLIGHT_FILE);

    for (State state : states) {
      state.preloadContent(manager);
    }
  }

  /**
   * Pulls the loaded textures from the asset manager for the game.
   * Initializes UI elements that depend on those textures.
   * @param manager the asset manager to use
   */
  public void loadContent(AssetManager manager) {
    devModeFont = manager.get(DEV_FONT, BitmapFont.class);
    pauseOverlay = manager.get(PAUSE_OVERLAY_FILE, Texture.class);
    pauseLabelFont = manager.get(PAUSE_LABEL_FONT, BitmapFont.class);
    pauseSubtextFont = manager.get(PAUSE_SUBTEXT_FONT, BitmapFont.class);
    resumeDefault = manager.get(RESUME_DEFAULT_FILE, Texture.class);
    resumeHighlight = manager.get(RESUME_HIGHLIGHT_FILE, Texture.class);
    resetDefault = manager.get(RESET_DEFAULT_FILE, Texture.class);
    resetHighlight = manager.get(RESET_HIGHLIGHT_FILE, Texture.class);
    levelsDefault = manager.get(LEVELS_DEFAULT_FILE, Texture.class);
    levelsHighlight = manager.get(LEVELS_HIGHLIGHT_FILE, Texture.class);
    menuDefault = manager.get(MENU_DEFAULT_FILE, Texture.class);
    menuHighlight = manager.get(MENU_HIGHLIGHT_FILE, Texture.class);

    // Initialize pause menu
    pauseStage = new Stage();
    pauseTable = new Table();
    pauseTable.setFillParent(true);
    pauseTable.add(new Label("paused", new Label.LabelStyle(pauseLabelFont, Color.WHITE)));
    pauseTable.row();

    pauseButtons = new HorizontalGroup();
    addPauseButton("resume", resumeDefault, resumeHighlight);
    addPauseButton("reset", resetDefault, resetHighlight);
    addPauseButton("levels", levelsDefault, levelsHighlight);
    addPauseButton("menu", menuDefault, menuHighlight);
    pauseButtons.space(50).padTop(50).rowBottom();

    pauseTable.add(pauseButtons);
    pauseStage.addActor(pauseTable);

    for (State state : states) {
      state.loadContent(manager);
    }
  }

  /**
   * Adds a button with the given label and texture to the pause menu.
   */
  private void addPauseButton(String text, Texture defaultTexture, Texture highlightTexture) {
    ImageButton button = new ImageButton(new TextureRegionDrawable(defaultTexture),
                                         null,
                                         new TextureRegionDrawable(highlightTexture));
    button.row();
    button.add(new Label(text, new Label.LabelStyle(pauseLabelFont, Color.WHITE))).padTop(20);
    button.setWidth(100);
    pauseButtons.addActor(button);
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
   * Creates the world level.
   * @param data the serialized level data
   * @param manager the asset manager to use
   * @param editable whether the level is editable
   */
  public void initLevel(LevelData data, AssetManager manager, boolean editable) {
    level = new LevelContainer(data, manager);
    gameState = GameState.INTRO;
    this.editable = editable;
  }

  /**
   * Returns whether the level is editable.
   */
  public boolean isEditable() {
    return editable;
  }
  
  /**
   * Updates the state of the game.
   * @param delta time in seconds since last frame
   */
  protected void update(float delta) {
    if (gameState == GameState.INTRO) {
      if (level != null) {
        level.activatePhysics(world);
        gameState = GameState.PLAY;
      }
    } else if (gameState == GameState.PLAY) {
      if (paused) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
          switch (pauseIndex) {
          case PAUSE_RESUME:
            paused = false;
            break;
          case PAUSE_RESET:
            listener.exitScreen(this, EXIT_RESET);
            break;
          case PAUSE_LEVELS:
            listener.exitScreen(this, EXIT_LEVELS);
            break;
          case PAUSE_MENU:
            listener.exitScreen(this, EXIT_MENU);
            break;
          }
        }
        ((Button)pauseButtons.getChild(pauseIndex)).setChecked(false);
        int n = pauseButtons.getChildren().size;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
          pauseIndex = (pauseIndex + n - 1) % n;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
          pauseIndex = (pauseIndex + 1) % n;
        }
        ((Button)pauseButtons.getChild(pauseIndex)).setChecked(true);
        pauseStage.act(delta);
      } else {
        InputController input = InputController.getInstance();
        input.readInput();
        if (input.didExit()) {
          if (editable) {
            listener.exitScreen(this, EXIT_EDIT);
          } else {
            pauseGame();
          }          
          return;
        }
        if (input.didDebug()) {
          debug = !debug;
        }
        if (input.didDevMode()) {
          devMode = !devMode;
          devSelect = -1;
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
            case 6:
              Player.dashDuration += devChange * 1;
              break;
            case 7:
              Player.dashSpeed += devChange * 0.5;
              break;
            case 8:
              break;
            case 9:
              break;
            case 0:
              Enemy.damage += devChange * 5;
              break;
            }
          }
        }

        Player player = level.getPlayer();
        if (player.isAlive()) {
          Vector2 pos = player.getPosition();
          if (pos.x >= level.getWidth() + X_BOUND) {
            listener.exitScreen(this, EXIT_COMPLETE);
            return;
          }
          if (pos.x < -X_BOUND || pos.y < -Y_BOUND) {
            listener.exitScreen(this, EXIT_RESET);
            return;
          }

          player.setInputVector(input.getHorizontal(), input.getVertical());
          player.tryFace();
          player.tryCling();
          if (input.didJump()) {
            player.tryJump();
          } else if (input.didHoldJump()) {
            player.tryExtendJump();
          }
          if (input.didDash()) {
            player.tryDash();
          }
          if (input.didAttack()) {
            player.tryAttack();
          }

          player.update(delta);
        } else {
          player.deactivatePhysics(world);
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
              Array<Enemy> spawned = ((Wisp)enemy).getSpawned();
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

        if (player.isAlive()) {
          player.sync();
        }
        for (Enemy enemy : level.getEnemies()) {
          enemy.sync();
        }
        for (Platform platform : level.getPlatforms()) {
          platform.sync();
        }
        Checkpoint checkpoint = level.getCheckpoint();
        if (checkpoint != null) {
          checkpoint.sync();
        }
      
        world.step(1 / 60f, 8, 3);
      }
    }
  }

  /**
   * Draw the current game state to the canvas.
   */
  protected void draw() {
    canvas.clear();

    if (level == null) {
      return;
    }

    Player player = level.getPlayer();
    if (player.isAlive()) {
      Vector2 pos = player.getPosition().scl(Shared.SCALED_PPM);
      float width = level.getWidth() * Shared.SCALED_PPM;
      float height = level.getHeight() * Shared.SCALED_PPM;
      cameraPos.set(pos);
      canvas.moveCamera(cameraPos, width, height);
      uiPos.set(MathUtils.clamp(pos.x - canvas.getWidth() / 2, 0, width - canvas.getWidth()) + DEV_DRAW_OFFSET,
                MathUtils.clamp(pos.y + canvas.getHeight() / 2, canvas.getHeight(), height) - DEV_DRAW_OFFSET);
    }

    level.draw(canvas, debug);

    if (paused) {
      canvas.begin();
      canvas.drawBackground(pauseOverlay,
                            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      canvas.end();
      
      pauseStage.draw();
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
      drawText(6, "Dash Duration", Player.dashDuration, Player.DASH_DURATION,
               xOffset, yOffset - 5 * deltaOffset);
      drawText(7, "Dash Speed", Player.dashSpeed, Player.DASH_SPEED,
               xOffset, yOffset - 6 * deltaOffset);
      drawText(0, "Enemy Damage", Enemy.damage, Enemy.DAMAGE,
        xOffset, yOffset - 9 * deltaOffset);
      canvas.end();
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
    canvas.drawText(text, devModeFont, color, x, y);
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
    Gdx.input.setInputProcessor(pauseStage);
    paused = true;
  }

  /**
   * Resumes the game.
   */
  private void resumeGame() {
    Gdx.input.setInputProcessor(null);
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
    active = true;
    if (level != null) {
      level.activatePhysics(world);
    }
  }

  @Override
  public void hide() {
    active = false;
    paused = false;
    if (level != null) {
      level.deactivatePhysics(world);
    }
  }

  @Override
  public void dispose() {
    pauseStage.dispose();
    if (world != null) {
      world.dispose();
    }
    level = null;
  }
}