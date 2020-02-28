package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;

/**
 * Primary controller class for the gameplay prototype.
 */
public class PrototypeMode implements Screen, ContactListener {
  @Override
  public void beginContact(Contact contact) {

  }

  @Override
  public void endContact(Contact contact) {

  }

  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {

  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {

  }

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
  private static String BACKGROUND_FILE = "Backgrounds/sunset-forest-yellow.png";
  /** The file for the idle image */
  private static final String ADAGIO_IDLE = "Adagio/wait-strip.png";
  /** The file for the walking filmstrip */
  private static final String ADAGIO_WALK = "Adagio/walk-strip75.png";
  /** The file for the jumping filmstrip */
  private static final String ADAGIO_JUMP = "Adagio/jump-strip75.png";
  /** The file for the attacking filmstrip */
  private static final String ADAGIO_ATTACK = "Adagio/tornado-strip150.png";
  /** The file for the enemy image */
  private static final String ENEMY_FILE = "enemy.png";

  // Loaded assets
  /** The background image for the game */
  private Texture background;
  /** Texture for Adagio idling */
  private Texture adagioIdleTexture;
  /** Texture for Adagio walking */
  private Texture adagioWalkTexture;
  /** Texture for Adagio jumping */
  private Texture adagioJumpTexture;
  /** Texture for Adagio attacking */
  private Texture adagioAttackTexture;
  /** Texture for enemies */
  private Texture enemyTexture;

  /** Array tracking all loaded assets (for unloading purposes) */
  private Array<String> assets;

  /** Canvas on which to draw content */
  private GameCanvas canvas;
  /** Current state of the game */
  private GameState gameState;
  /** Whether this game mode is active */
  private boolean active;

  /** The player entity */
  private Player player;
  /** List of enemies */
  private Array<Enemy> enemies;
  /** The world of the protoype level */
  private World world;

  public void preloadContent(AssetManager manager) {
    manager.load(BACKGROUND_FILE, Texture.class);
    assets.add(BACKGROUND_FILE);
    manager.load(ADAGIO_IDLE, Texture.class);
    assets.add(ADAGIO_IDLE);
    manager.load(ADAGIO_WALK, Texture.class);
    assets.add(ADAGIO_WALK);
    manager.load(ADAGIO_JUMP, Texture.class);
    assets.add(ADAGIO_JUMP);
    manager.load(ADAGIO_ATTACK, Texture.class);
    assets.add(ADAGIO_ATTACK);
    manager.load(ENEMY_FILE, Texture.class);
    assets.add(ENEMY_FILE);
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
    adagioAttackTexture = createTexture(manager, ADAGIO_ATTACK);
    enemyTexture = createTexture(manager, ENEMY_FILE);
  }

  public void unloadContent(AssetManager manager) {
    for (String s : assets) {
      if (manager.isLoaded(s)) {
        manager.unload(s);
      }
    }
  }

  /**
   * Initialize an instance of this game mode.
   * @param gameCanvas the canvas to draw on
   */
  public PrototypeMode(GameCanvas gameCanvas) {
    // Start loading with the asset manager
    manager = new AssetManager();
    assets = new Array<String>();
    canvas = gameCanvas;
    gameState = GameState.INTRO;


    // Initialize game world
    Vector2 temp = new Vector2(10f, 10f);
    world = new World(temp, false);

  }

  /**
   * TODO
   */
  private void update(float delta) {
    switch (gameState) {
    case INTRO: {
      loadContent(manager);
      preloadContent(manager);
      manager.finishLoading();
      loadContent(manager);
      player = new Player(50, 0);
      player.setTexture(adagioIdleTexture);
      player.activatePhysics(world);
      Enemy enemy = new Enemy(600, 75);
      enemy.setTexture(enemyTexture);
      enemies = new Array<Enemy>(new Enemy[] {enemy});
      gameState = GameState.PLAY;
      //remove later
      break;
    }
    case PLAY: {
      InputController input = InputController.getInstance();
      input.readInput();
      if (input.didExit()) {
        // TODO fix this cleanup bug
        Gdx.app.exit();
        System.exit(0);
        break;
      }
      if (input.didReset()) {
        gameState = GameState.INTRO;
        break;
      }

      float horizontal = input.getHorizontal();
      player.setMove(horizontal);
      if (input.didJump()) {
        player.setJump();
      }
      if (input.didAttack()) {
        player.setAttack();
      }
      if (player.isAttacking()) {
        player.setTexture(adagioAttackTexture, 1, 11, 11);
      } else if (player.isJumping()) {
        player.setTexture(adagioJumpTexture, 1, 9, 9);
      } else if (horizontal != 0) {
        player.setTexture(adagioWalkTexture, 1, 10, 10);
      } else {
        player.setTexture(adagioIdleTexture, 1, 1, 1);
      }
      player.update(delta);

      System.out.println("player body x: " + player.getPos().x + ", y: " + player.getPos().y);
      System.out.println("player vel x: " + player.getVel().x + ", y: " + player.getPos().y);
      for (Enemy enemy : enemies) {
        enemy.update(delta);
        if (player.isAttacking()) {
          System.out.println(enemy.getPos().x - player.getPos().x + " " + player.getDirection());
          if ((enemy.getPos().x - player.getPos().x) * player.getDirection() < 100
              && (Math.abs(enemy.getPos().x - player.getPos().x) < 50)) {
            System.out.println("hit");
          }
        }
      }
      world.step(1f / 60, 6, 2);
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
    canvas.begin();
    canvas.drawBackground(background);
    player.draw(canvas);
    for (Enemy enemy : enemies) {
      enemy.draw(canvas);
    }
    canvas.end();

    /** debug draw*/
    canvas.beginDebug();
    player.drawPhysics(canvas);
    canvas.endDebug();
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
}
