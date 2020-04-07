package com.glassboxgames.rubato;

import java.text.DecimalFormat;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
import com.glassboxgames.util.*;

/**
 * Mode controller for the main gameplay loop.
 */
public class GameMode implements Screen {
  public enum GameState {
    /** Before the game has started */
    INTRO,
    /** While we are playing the game */
    PLAY,
  }

  // GRAPHICS AND SOUND RESOURCES
  /** The file for the background image to scroll */
  private static String BACKGROUND_FILE = "Backgrounds/Realism Update/Realistic-Forest.png";
  /** The file for the font */
  private static final String FONT_FILE = "Fonts/LucidaGrande.ttf";
  /** The font size */
  private static final int FONT_SIZE = 24;
  /** The text offset */
  private static final float TEXT_OFFSET = 20.0f;

  // Loaded assets
  /** The font for giving messages to the player */
  private BitmapFont displayFont;
  /** The background image for the game */
  private Texture background;
  /** Array tracking all loaded assets (for unloading purposes) */
  private Array<String> assets;

  /** Gravity **/
  protected static float GRAVITY = -70f;
  /** Level width */
  protected static float LEVEL_WIDTH = 30f;
  /** Level height */
  protected static float LEVEL_HEIGHT = 10.8f;
  
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

  /** The player entity */
  protected Player player;
  /** The list of platforms */
  protected Array<Platform> platforms;
  /** The list of enemies */
  protected Array<Enemy> enemies;

  /** Whether debug mode is on */
  private boolean debug = false;
  /** Whether dev mode is on */
  private boolean devMode = false;
  /** Numerical selector for devMode */
  private int devSelect = -1;

  /**
   * Instantiate a GameMode.
   * @param c the canvas to draw on
   * @param l the listener for exiting the screen
   */
  public GameMode(GameCanvas c, ScreenListener l) {
    assets = new Array();
    canvas = c;
    listener = l;
    gameState = GameState.INTRO;

    // Initialize game world
    world = new World(new Vector2(0, GRAVITY), false);
    world.setContactListener(CollisionController.getInstance());

    // Initialize entity state machines
    Player.states = State.readStates("Adagio/");
    Platform.states = State.readStates("Tilesets/");
    Enemy.states = State.readStates("Enemies/Drone/");
  }

  /**
   * Adds the assets to load for the game to the given manager.
   * @param manager asset manager to use
   */
  public void preloadContent(AssetManager manager) {
    manager.load(BACKGROUND_FILE, Texture.class);
    assets.add(BACKGROUND_FILE);
    for (State state : Player.states) {
      state.preloadContent(manager);
    }
    for (State state : Platform.states) {
      state.preloadContent(manager);
    }
    for (State state : Enemy.states) {
      state.preloadContent(manager);
    }
    FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    size2Params.fontFileName = FONT_FILE;
    size2Params.fontParameters.size = FONT_SIZE;
    manager.load(FONT_FILE, BitmapFont.class, size2Params);
    assets.add(FONT_FILE);
  }

  /**
   * Returns a texture for the given file if loaded, otherwise returns null.
   * @param manager the asset manager to use
   * @param file the file path
   */
  private Texture createTexture(AssetManager manager, String file) {
    if (manager.isLoaded(file)) {
      Texture texture = manager.get(file, Texture.class);
      texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      return texture;
    }
    return null;
  }

  /**
   * Pulls the loaded textures from the asset manager for the game.
   * @param manager the asset manager to use
   */
  public void loadContent(AssetManager manager) {
    displayFont = manager.get(FONT_FILE, BitmapFont.class);
    background = createTexture(manager, BACKGROUND_FILE);
    for (State state : Player.states) {
      state.loadContent(manager);
    }
    for (State state : Platform.states) {
      state.loadContent(manager);
    }
    for (State state : Enemy.states) {
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
    for (State state : Player.states) {
      state.unloadContent(manager);
    }
    for (State state : Platform.states) {
      state.unloadContent(manager);
    }
    for (State state : Enemy.states) {
      state.unloadContent(manager);
    }
  }

  /**
   * Resets the world.
   */
  public void reset() {
    for (Platform platform : platforms) {
      platform.deactivatePhysics(world);
    }
    for (Enemy enemy : enemies) {
      enemy.deactivatePhysics(world);
    }
    player.deactivatePhysics(world);
    platforms.clear();
    enemies.clear();
    world.dispose(); 

    world = new World(new Vector2(0, GRAVITY), false);
    world.setContactListener(CollisionController.getInstance());
    gameState = GameState.INTRO;
  }

  /**
   * Updates the state of the game.
   * @param delta time in seconds since last frame
   */
  protected void update(float delta) {
    if (gameState == GameState.INTRO) {
      player = new Player(1f, 2f);
      player.activatePhysics(world);
      player.setAlive(true);

      platforms = new Array();
      for (float x = 0.25f; x < LEVEL_WIDTH - 0.25f; x += 0.5f) {
        Platform platform = new Platform(x, 0.25f);
        platform.activatePhysics(world);
        platforms.add(platform);
      }

      enemies = new Array();
      Enemy enemy = new Enemy(6f, 1.5f);
      enemy.activatePhysics(world);
      enemies.add(enemy);

      gameState = GameState.PLAY;
    } else if (gameState == GameState.PLAY) {
      InputController input = InputController.getInstance();
      input.readInput();
      if (input.didExit()) {
        Gdx.app.exit();
        return;
      }
      if (input.didReset()) {
        reset();
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
              Player.dashCooldown += devChange * 1;
              break;
            case 7:
              Player.dashDuration += devChange * 1;
              break;
            case 8:
              Player.dashSpeed += devChange * 0.5;
              break;
            case 9:
              break;
            case 0:
              break;
          }
        }
      }

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

        player.update(delta);
        player.sync();
      }
      for (Enemy enemy : enemies) {
        enemy.update(delta);
        enemy.sync();
      }
      for (Platform platform : platforms) {
        platform.update(delta);
        platform.sync();
      }
      world.step(1 / 60f, 8, 3);
    }
  }

  /**
   * Draw the current game state to the canvas.
   */
  protected void draw() {
    if (player.isAlive()) {
      canvas.moveCamera(player.getPosition().scl(Constants.PPM),
                        LEVEL_WIDTH * Constants.PPM, LEVEL_HEIGHT * Constants.PPM);
    }
    canvas.clear();
    canvas.begin();
    canvas.drawBackground(background);
    canvas.end();
    canvas.begin(Constants.PPM, Constants.PPM);
    for (Platform platform : platforms) {
      platform.draw(canvas);
    }
    for (Enemy enemy : enemies) {
      enemy.draw(canvas);
    }
    if (player.isAlive()) {
      player.draw(canvas);
    }
    canvas.end();

    if (debug) {
      canvas.beginDebug(Constants.PPM, Constants.PPM);
      for (Platform platform : platforms) {
        platform.drawPhysics(canvas);
      }
      for (Enemy enemy : enemies) {
        enemy.drawPhysics(canvas);
      }
      if (player.isAlive()) {
        player.drawPhysics(canvas);
      }
      canvas.endDebug();
    }

    if (devMode) {
      float xOffset = MathUtils.clamp(player.getPosition().scl(Constants.PPM).x - canvas.getWidth() / 2,
                                      0,
                                      LEVEL_WIDTH * Constants.PPM - canvas.getWidth()) + TEXT_OFFSET;
      float yOffset = MathUtils.clamp(player.getPosition().scl(Constants.PPM).y + canvas.getHeight() / 2,
                                      canvas.getHeight(),
                                      canvas.getHeight() + LEVEL_HEIGHT * Constants.PPM) - TEXT_OFFSET;
      float deltaOffset = 2 * TEXT_OFFSET;
      canvas.begin();
      drawText(1, "Jump Impulse", Player.jumpImpulse, Player.JUMP_IMPULSE, xOffset, yOffset);
      drawText(2, "Max X Speed", Player.maxXSpeed, Player.MAX_X_SPEED, xOffset, yOffset - deltaOffset);
      drawText(3, "Max Y Speed", Player.maxYSpeed, Player.MAX_Y_SPEED, xOffset, yOffset - 2 * deltaOffset);
      drawText(4, "Min Jump Duration", Player.minJumpDuration, Player.MIN_JUMP_DURATION,
               xOffset, yOffset - 3 * deltaOffset);
      drawText(5, "Max Jump Duration", Player.maxJumpDuration, Player.MAX_JUMP_DURATION,
               xOffset, yOffset - 4 * deltaOffset);
      drawText(6, "Dash Cooldown", Player.dashCooldown, Player.DASH_COOLDOWN,
               xOffset, yOffset - 5 * deltaOffset);
      drawText(7, "Dash Duration", Player.dashDuration, Player.DASH_DURATION,
               xOffset, yOffset - 6 * deltaOffset);
      drawText(8, "Dash Speed", Player.dashSpeed, Player.DASH_SPEED, xOffset, yOffset - 7 * deltaOffset);
      canvas.end();
    }

  }

  /**
   * Draws devMode text
   * TODO
   * @param num
   */
  private void drawText(int num, String name, float value, float oldValue, float x, float y) {
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
    String text = "[" + num + "] " + name + ": " + new DecimalFormat("#.##").format(value);
    canvas.drawText(text, displayFont, color, x, y);
    canvas.drawText(text, displayFont, color, x - 1, y); // bold effect
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
    player = null;
    canvas = null;
  }
}
