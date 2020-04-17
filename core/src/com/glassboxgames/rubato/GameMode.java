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
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
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
  /** Exit code for completing the level */
  public static final int EXIT_COMPLETE = 1;
  /** Exit code for resetting the level */
  public static final int EXIT_RESET = 2;
  /** Exit code for editing the level */
  public static final int EXIT_EDIT = 3;

  // GRAPHICS AND SOUND RESOURCES
  /** The file for the parry meter */
  private static final String PARRY_METER_FILE = "User Interface/Play Screen/parrying_meter_6274x1171.png";
  /** The file for the font */
  private static final String FONT_FILE = "Fonts/Rajdhani-Bold.ttf";
  /** The font size */
  private static final int FONT_SIZE = 24;
  /** The draw offset */
  private static final float DRAW_OFFSET = 20.0f;

  // Loaded assets
  /** The background image */
  private Texture background;
  /** The parry meter image */
  private Texture parryMeter;
  /** The font for giving messages to the player */
  private BitmapFont displayFont;
  /** Array tracking all loaded assets (for unloading purposes) */
  private Array<String> assets;

  /** Gravity **/
  private static final float GRAVITY = -50f;
  
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

  /** List of all entity states currently loaded */
  protected Array<State> states;
  /** The current level */
  protected LevelContainer level;
  /** The position of the camera */
  protected Vector2 cameraPos;

  /** The upper left corner of the visible canvas **/
  protected Vector2 uiPos;
  /** Whether debug mode is on */
  protected boolean debug;
  /** Whether dev mode is on */
  protected boolean devMode;
  /** Numerical selector for devMode */
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
    FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    size2Params.fontFileName = FONT_FILE;
    size2Params.fontParameters.size = FONT_SIZE;
    manager.load(FONT_FILE, BitmapFont.class, size2Params);
    assets.add(FONT_FILE);
    manager.load(PARRY_METER_FILE, Texture.class);
    assets.add(PARRY_METER_FILE);
    for (State state : states) {
      state.preloadContent(manager);
    }
  }

  /**
   * Pulls the loaded textures from the asset manager for the game.
   * @param manager the asset manager to use
   */
  public void loadContent(AssetManager manager) {
    parryMeter = manager.get(PARRY_METER_FILE, Texture.class);
    displayFont = manager.get(FONT_FILE, BitmapFont.class);
    for (State state : states) {
      state.loadContent(manager);
    }
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
      InputController input = InputController.getInstance();
      input.readInput();
      if (input.didExit()) {
        level.deactivatePhysics(world);
        listener.exitScreen(this, EXIT_MENU);
        return;
      }
      if (input.didReset()) {
        level.deactivatePhysics(world);
        listener.exitScreen(this, EXIT_RESET);
        return;
      }
      if (editable && input.didEdit()) {
        level.deactivatePhysics(world);
        listener.exitScreen(this, EXIT_EDIT);
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
              Player.jumpImpulse = (float) (Math.round((Player.jumpImpulse + devChange * 0.05) * 100.0) / 100.0); // handle precision error
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
              Player.parryCapacity += devChange * 10;
              break;
            case 9:
              Player.parryGain += devChange * 5;
              break;
            case 0:
              Enemy.damage += devChange * 5;
              break;
          }
        }
      }

      boolean complete = true;
      for (Checkpoint checkpoint : level.getCheckpoints()) {
        checkpoint.update(delta);
        complete = complete && checkpoint.isActivated();
      }
      if (complete && !editable && input.didContinue()) {
        level.deactivatePhysics(world);
        listener.exitScreen(this, EXIT_COMPLETE);
        return;
      }
      
      Player player = level.getPlayer();
      if (player.isAlive()) {
        player.setInputVector(input.getHorizontal(), input.getVertical());
        player.tryMove();
        if (input.didJump()) {
          player.tryJump();
        }
        if (input.didDash()) {
          player.tryDash();
        }
        if (input.didAttack()) {
          player.tryAttack();
        }
        if (input.didParry()) {
          player.tryParry();
        } else {
          player.endParry();
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
      for (Checkpoint checkpoint : level.getCheckpoints()) {
        checkpoint.sync();
      }
      
      world.step(1 / 60f, 8, 3);
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
      Vector2 pos = player.getPosition().scl(Constants.PPM);
      cameraPos.set(pos);
      canvas.moveCamera(cameraPos,
                        level.getWidth() * Constants.PPM,
                        level.getHeight() * Constants.PPM);
      uiPos.set(MathUtils.clamp(pos.x - canvas.getWidth() / 2,
                                     0,
                                     level.getWidth() * Constants.PPM
                                     - canvas.getWidth()) + DRAW_OFFSET,
                     MathUtils.clamp(pos.y + canvas.getHeight() / 2,
                                     canvas.getHeight(),
                                     level.getHeight() * Constants.PPM) - DRAW_OFFSET);
    }

    level.draw(canvas, debug);

    // TODO un-hardcode; parry meter UI (lot of this is temp code)
    float parryMeterWidth = canvas.getWidth() / 2.5f;
    float parryMeterHeight = parryMeterWidth * 1171/6274;
    float parry = player.getParry();
    String resource = new DecimalFormat("#.##").format(parry);
    String total = new DecimalFormat("#.##").format(player.parryCapacity);
    int lives = (int) Math.max(Math.ceil(parry / Enemy.DAMAGE), 1);
    canvas.begin();
    canvas.draw(parryMeter, Color.WHITE, 0, parryMeterHeight,
                uiPos.x, uiPos.y, parryMeterWidth, parryMeterHeight);
    canvas.drawText(resource + "/" + total, displayFont, Color.BLACK,
                    uiPos.x + 4.5f * DRAW_OFFSET, uiPos.y - 2.2f * DRAW_OFFSET);
    if (player.isAlive()) {
      for (int i = 0; i < lives; i++) {
        canvas.drawText("o", displayFont, Color.RED,
          uiPos.x + parryMeterWidth - (2.6f + i) * DRAW_OFFSET, uiPos.y - 2.2f * DRAW_OFFSET);
      }
    }
    // end temp code
    canvas.end();

    if (devMode) {
      float xOffset = uiPos.x;
      float yOffset = uiPos.y - parryMeterHeight - DRAW_OFFSET;
      float deltaOffset = 2 * DRAW_OFFSET;
      canvas.begin();
      drawText(1, "Jump Impulse", Player.jumpImpulse, Player.JUMP_IMPULSE,
               xOffset, yOffset);
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
      drawText(8, "Parry Capacity", Player.parryCapacity, Player.PARRY_CAPACITY,
               xOffset, yOffset - 7 * deltaOffset);
      drawText(9, "Parry Gain", Player.parryGain, Player.PARRY_GAIN,
               xOffset, yOffset - 8 * deltaOffset);
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
    canvas.drawText(text, displayFont, color, x, y);
  }

  @Override
  public void render(float delta) {
    if (active) {
      update(delta);
      draw();
    }
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
  }

  @Override
  public void hide() {
    active = false;
  }

  @Override
  public void dispose() {
    if (world != null) {
      world.dispose();
    }
    level = null;
  }
}
