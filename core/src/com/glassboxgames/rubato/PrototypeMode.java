package com.glassboxgames.rubato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.glassboxgames.util.*;

/**
 * Primary controller class for the gameplay prototype.
 */
public class PrototypeMode implements Screen {
  public enum GameState {
    /** Before the game has started */
    INTRO,
    /** While we are playing the game */
    PLAY,
  }

  /** AssetManager to load game assets (textures, sounds, etc.) */
  private AssetManager manager;
  /** The player entity */
  private Player player;
  /** The player scale amount */
  private static final String PLAYER_FILE = "adagio.png";

  // GRAPHICS AND SOUND RESOURCES
  /** The file for the background image to scroll */
  private static String BACKGROUND_FILE = "sunset-forest-ice.png";
  /** The file for the walking filmstrip */
  private static final String ADAGIO_WALK = "walk-strip50.png";
  
  /** Texture for the player */
  private Texture playerTexture;
  /** The background image for the game */
  private Texture background;
  /** Texture for walking */
  private Texture adagioWalkTexture;

  /** Sprite for the player */
  private Sprite playerSprite;

  /** Array tracking all loaded assets (for unloading purposes) */
  private Array<String> assets;

  /** Batch to draw multiple sprites at once */
  private SpriteBatch batch;

  /** Canvas on which to draw content */
  private GameCanvas canvas;

  /** Current state of the game */
  private GameState gameState;
  /** Whether this game mode is active */
  private boolean active;

  public void preloadContent(AssetManager manager) {
    manager.load(BACKGROUND_FILE, Texture.class);
    assets.add(BACKGROUND_FILE);
    manager.load(ADAGIO_WALK, Texture.class);
    assets.add(ADAGIO_WALK);
  }

  private Texture createTexture(AssetManager manager, String file) {
    if (manager.isLoaded(file)) {
      Texture texture = manager.get(file, Texture.class);
      texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      return texture;
    }
    return null;
  }

  public void loadContent(AssetManager manager) {
    // background = createTexture(manager, BACKGROUND_FILE);
    adagioWalkTexture = createTexture(manager, ADAGIO_WALK);
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
   * @param canvas the canvas to draw on
   */
  public PrototypeMode(GameCanvas canvas) {
    // Start loading with the asset manager
    active = false;
    manager = new AssetManager();
    assets = new Array<String>();
    this.canvas = canvas;
    gameState = GameState.INTRO;
  }

  /**
   * TODO
   */
  private void update(float delta) {

    InputController input = InputController.getInstance();
    switch (gameState) {
    case INTRO:
      gameState = GameState.PLAY;
      player = new Player(0, 0);
      playerTexture = new Texture(Gdx.files.internal("adagio.png"));
      playerSprite = new Sprite(playerTexture);
      playerSprite.setSize((int)player.dim.x, (int)player.dim.y);
      adagioWalkTexture = new Texture(Gdx.files.internal(ADAGIO_WALK));
      background = new Texture(Gdx.files.internal(BACKGROUND_FILE));
      player.setTexture(adagioWalkTexture);
      batch = new SpriteBatch();
      break;
    case PLAY:
      loadContent(manager);
      input.readInput();
      player.setMove(input.getHorizontal());
      player.setJump(input.didJump());
      player.update(delta);
      break;
    }
  }

  /**
   * TODO: process a single frame
   * @param delta
   */
  private void play(float delta) {

  }

  /**
   * TODO:
   * @param delta
   */
  private void draw(float delta) {
    canvas.begin();
    canvas.drawBackground(background,0,-100);
    player.draw(canvas);
    canvas.end();
    // Gdx.gl.glClearColor(1, 1, 1, 1);
    // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    // batch.begin();
    // playerSprite.setPosition(player.pos.x, player.pos.y);
    // playerSprite.setFlip(player.getDirection() < 0, false);
    // playerSprite.draw(batch);
    // batch.end();
  }

  /** 
   * TODO:
   * @param delta
   */
  public void render(float delta) {
    if (active) {
      update(delta);
      draw(delta);
      // TODO: implement quiting after draw function in the future
    }
  }

  /**
   * TODO:
   * @param width
   * @param height
   */
  public void resize(int width, int height) {

  }

  /**
   * TODO:
   */
  public void pause() {

  }

  /**
   * TODO:
   */
  public void resume() {

  }

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
    playerTexture = null;
    playerSprite = null;
    batch = null;
    canvas = null;
  }
}
