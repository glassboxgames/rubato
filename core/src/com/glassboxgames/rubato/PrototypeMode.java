package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Primary controller class for the gameplay prototype.
 */
public class PrototypeMode implements ContactListener, Screen {
  private boolean debug = false;

  public enum GameState {
    /** Before the game has started */
    INTRO,
    /** While we are playing the game */
    PLAY,
  }

  /** AssetManager to load game assets (textures, sounds, etc.) */
  private AssetManager manager;

  // GRAPHICS AND SOUND RESOURCES
  /** The file for the background image to scroll */
  private static String BACKGROUND_FILE = "Backgrounds/Realism Update/Realistic-Forest.png";
  /** The file for the idle image */
  private static final String ADAGIO_IDLE = "Adagio/00 Filmstrips/wait-strip.png";
  /** The file for the walking filmstrip */
  private static final String ADAGIO_WALK = "Adagio/00 Filmstrips/walk-strip75.png";
  /** The file for the jumping filmstrip */
  private static final String ADAGIO_JUMP = "Adagio/00 Filmstrips/jump-strip75.png";
  /** The file for the dashing filmstrip */
  private static final String ADAGIO_DASH = "Adagio/00 Filmstrips/dash-strip75.png";
  /** The file for the attacking filmstrip */
  private static final String ADAGIO_ATTACK = "Adagio/00 Filmstrips/tornado-strip150.png";
  /** The file for the enemy image */
  private static final String ENEMY_FILE = "enemy.png";
  /** The file for the platform tile */
  private static final String PLATFORM_FILE = "Tilesets/Grass/edge-n.png";

  // Loaded assets
  /** The background image for the game */
  private Texture background;
  /** Texture for Adagio idling */
  private Texture adagioIdleTexture;
  /** Texture for Adagio walking */
  private Texture adagioWalkTexture;
  /** Texture for Adagio jumping */
  private Texture adagioJumpTexture;
  /** Texture for Adagio dashing */
  private Texture adagioDashTexture;
  /** Texture for Adagio attacking */
  private Texture adagioAttackTexture;
  /** Texture for enemies */
  private Texture enemyTexture;
  /** Texture for platforms */
  private Texture platformTexture;

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

  /**
   * Instantiate a PrototypeMode.
   * @param gameCanvas the canvas to draw on
   */
  public PrototypeMode(GameCanvas gameCanvas) {
    // Start loading with the asset manager
    manager = new AssetManager();
    assets = new Array();
    canvas = gameCanvas;
    gameState = GameState.INTRO;

    // Initialize game world
    world = new World(new Vector2(0, GRAVITY), false);
    world.setContactListener(this);
  }

  public void preloadContent(AssetManager manager) {
    manager.load(BACKGROUND_FILE, Texture.class);
    assets.add(BACKGROUND_FILE);
    manager.load(ADAGIO_IDLE, Texture.class);
    assets.add(ADAGIO_IDLE);
    manager.load(ADAGIO_WALK, Texture.class);
    assets.add(ADAGIO_WALK);
    manager.load(ADAGIO_JUMP, Texture.class);
    assets.add(ADAGIO_JUMP);
    manager.load(ADAGIO_DASH, Texture.class);
    assets.add(ADAGIO_DASH);
    manager.load(ADAGIO_ATTACK, Texture.class);
    assets.add(ADAGIO_ATTACK);
    manager.load(ENEMY_FILE, Texture.class);
    assets.add(ENEMY_FILE);
    manager.load(PLATFORM_FILE, Texture.class);
    assets.add(PLATFORM_FILE);
  }

  private Texture createTexture(AssetManager manager, String file) {
    if (manager.isLoaded(file)) {
      Texture texture = manager.get(file, Texture.class);
      //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      return texture;
    }
    // TODO: Fix this loadContent bug
    return new Texture(Gdx.files.internal(file));
  }

  public void loadContent(AssetManager manager) {
    background = createTexture(manager, BACKGROUND_FILE);
    adagioIdleTexture = createTexture(manager, ADAGIO_IDLE);
    adagioWalkTexture = createTexture(manager, ADAGIO_WALK);
    adagioJumpTexture = createTexture(manager, ADAGIO_JUMP);
    adagioDashTexture = createTexture(manager, ADAGIO_DASH);
    adagioAttackTexture = createTexture(manager, ADAGIO_ATTACK);
    enemyTexture = createTexture(manager, ENEMY_FILE);
    platformTexture = createTexture(manager, PLATFORM_FILE);
    platformTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
  }

  public void unloadContent(AssetManager manager) {
    for (String s : assets) {
      if (manager.isLoaded(s)) {
        manager.unload(s);
      }
    }
  }

  @Override
  public void beginContact(Contact contact) {
    Fixture f1 = contact.getFixtureA();
    Fixture f2 = contact.getFixtureB();
    Object d1 = f1.getUserData();
    Object d2 = f2.getUserData();
    CollisionController cc = CollisionController.getInstance();
    cc.startCollision(d1, d2);
  }
  
  @Override
  public void endContact(Contact contact) {
    Fixture f1 = contact.getFixtureA();
    Fixture f2 = contact.getFixtureB();
    Object d1 = f1.getUserData();
    Object d2 = f2.getUserData();
    CollisionController cc = CollisionController.getInstance();
    cc.endCollision(d1, d2);
  }

  @Override
  public void preSolve(Contact contact, Manifold manifold) {}

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {}

  /**
   * TODO
   */
  private void update(float delta) {
    switch (gameState) {
    case INTRO: {
      preloadContent(manager);
      manager.finishLoading();
      loadContent(manager);

      player = new Player(1f, 2f, 0.3f, 1f, Player.NUM_STATES);
      player.initState(Player.STATE_IDLE, adagioIdleTexture);
      player.initState(Player.STATE_WALK, adagioWalkTexture, 1, 10, 10, 0.25f, true);
      player.initState(Player.STATE_FALL, adagioIdleTexture);
      player.initState(Player.STATE_JUMP, adagioJumpTexture, 1, 9, 9, 0.25f, false);
      player.initState(Player.STATE_DASH, adagioDashTexture, 1, 2, 2, 0.2f, false);
      player.initState(Player.STATE_GND_ATTACK, adagioAttackTexture, 1, 11, 11, 0.4f, false);
      player.initState(Player.STATE_AIR_ATTACK, adagioAttackTexture, 1, 11, 11, 0.4f, false);
      player.activatePhysics(world);
      player.setAlive(true);

      Platform platform = new Platform(LEVEL_WIDTH / 2f, 0.25f, LEVEL_WIDTH, 0.5f, 0.5f, 0.5f);
      platform.initState(0, platformTexture);
      platform.activatePhysics(world);
      platforms = new Array(new Platform[] {platform});

      Enemy enemy = new Enemy(6f, 1.5f, 1.5f, 0.6f);
      enemy.initState(0, enemyTexture);
      enemy.activatePhysics(world);
      enemies = new Array(new Enemy[] {enemy});

      gameState = GameState.PLAY;
      break;
    }
    case PLAY: {
      InputController input = InputController.getInstance();
      input.readInput();
      if (input.didExit()) {
        Gdx.app.exit();
        break;
      }
      if (input.didReset()) {
        reset();
        break;
      }
      if (input.didDebug()) {
        debug = !debug;
      }

      if (player.isAlive()) {
        player.setHorizontal(input.getHorizontal());
        player.setVertical(input.getVertical());
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
      }
      
      for (Enemy enemy : enemies) {
        if (player.isAlive()
            && player.isHitboxActive()
            && !player.getEnemiesHit().contains(enemy, true)) {
          // TODO make this not manual
          Vector2 center = new Vector2(Player.ATTACK_POS)
            .scl(player.getDirection(), 0)
            .add(player.getPosition());
          Circle circle = new Circle(center, Player.ATTACK_SIZE);
          Vector2 dim = enemy.getDimensions();
          Vector2 corner = new Vector2(dim).scl(-0.5f).add(enemy.getPosition());
          Rectangle rectangle = new Rectangle(corner.x, corner.y, dim.x, dim.y);
          if (Intersector.overlaps(circle, rectangle)) {
            player.getEnemiesHit().add(enemy);
            enemy.lowerHealth(Player.ATTACK_DAMAGE);
          }
        }
        enemy.update(delta);
      }
      world.step(1 / 60f, 8, 3);
      break;
    }
    }
  }

  /**
   * TODO: process a single frame
   * @param delta
   */
  private void play(float delta) {}

  /**
   * TODO:
   * @param delta
   */
  private void draw(float delta) {
    if (player.isAlive()) {
      canvas.moveCamera(player.getPosition().scl(Constants.PPM),
                        LEVEL_WIDTH * Constants.PPM, LEVEL_HEIGHT * Constants.PPM);
    }
    canvas.clear();
    canvas.begin();
    canvas.drawBackground(background);
    canvas.end();
    canvas.begin(Constants.PPM, Constants.PPM);
    for (Enemy enemy : enemies) {
      enemy.draw(canvas);
    }
    for (Platform platform : platforms) {
      platform.draw(canvas);
    }
    if (player.isAlive()) {
      player.draw(canvas);
    }
    canvas.end();

    if (debug) {
      canvas.beginDebug(Constants.PPM, Constants.PPM);
      for (Enemy enemy : enemies) {
        enemy.drawPhysics(canvas);
      }
      for (Platform platform : platforms) {
        platform.drawPhysics(canvas);
      }
      if (player.isAlive()) {
        player.drawPhysics(canvas);
      }
      canvas.endDebug();
    }
  }

  /**
   * TODO:
   * @param delta
   */
  public void render(float delta) {
    if (active) {
      update(delta);
      draw(delta);
      // TODO: implement quitting after draw function in the future
    }
  }

  /**
   * TODO:
   * @param width
   * @param height
   */
  public void resize(int width, int height) {}

  /**
   * TODO:
   */
  public void pause() {}

  /**
   * TODO:
   */
  public void resume() {}

  /**
   * TODO:
   */
  public void show() {
    active = true;
  }

  /**
   * TODO:
   */
  public void hide() {
    active = false;
  }

  /**
   * TODO:
   */
  public void dispose() {
    player = null;
    canvas = null;
    unloadContent(manager);
  }

  /**
   * Manages resetting the world.
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
    world.dispose(); // TODO I think we need to reset the world but it crashes whenever we do.
    world = new World(new Vector2(0, GRAVITY), false);
    world.setContactListener(this);
    gameState = GameState.INTRO;
  }
}
