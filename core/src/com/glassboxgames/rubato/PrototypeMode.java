package com.glassboxgames.rubato;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

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
  private static final String ADAGIO_IDLE = "adagio.png";

  // GRAPHICS AND SOUND RESOURCES
  /** The file for the background image to scroll */
  private static String BKGD_FILE = "sunset-forest-ice.png";
  private static final String ADAGIO_WALK = "walk-strip50.png";

  // Loaded assets
  /** The background image for the game */
  private Texture background;
  /** Texture for Adagio idling */
  private Texture adagioIdleTexture;
  /** Texture for Adagio walking */
  private Texture adagioWalkTexture;

  /** Track all loaded assets (for unloading purposes) */
  private Array<String> assets;

  public void preLoadContent(AssetManager manager) {
    manager.load(BKGD_FILE,Texture.class);
    assets.add(BKGD_FILE);
    manager.load(ADAGIO_IDLE, Texture.class);
    assets.add(ADAGIO_IDLE);
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
      adagioIdleTexture = createTexture(manager,ADAGIO_IDLE);
      adagioWalkTexture = createTexture(manager,ADAGIO_WALK);

  }

  public void unloadContent(AssetManager manager) {
    for(String s : assets) {
      if (manager.isLoaded(s)) {
        manager.unload(s);
      }
    }
  }

  /** Canvas on which to draw content */
  private GameCanvas canvas;

  /** Current state of the game */
  private GameState gameState;
  /** Whether this game mode is active */
  private boolean active;

  /**
   * Initialize an instance of this game mode.
   * @param canvas the canvas to draw on
   */
  public PrototypeMode(GameCanvas canvas) {
    // Start loading with the asset manager
    active = false;
    manager = new AssetManager();
    assets = new Array();
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
      background = new Texture(Gdx.files.internal(BKGD_FILE));
      adagioIdleTexture = new Texture(Gdx.files.internal(ADAGIO_IDLE));
      adagioWalkTexture = new Texture(Gdx.files.internal(ADAGIO_WALK));
      player = new Player(Player.ADAGIO_WIDTH/2, Player.ADAGIO_HEIGHT/2);
      player.setTexture(adagioIdleTexture,1,1,1);
      break;
    case PLAY:
      loadContent(manager);
      input.readInput();
      player.setMove(input.getHorizontal());
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
    canvas = null;
  }
}
