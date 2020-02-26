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

/**
 * Primary controller class for the gameplay prototype.
 */
public class PrototypeMode implements Screen {
  public enum GameState {
    // TODO: note that this is copied from lab3, we should change this for our game as we see fit
    /** Before the game has started */
    INTRO,
    /** While we are playing the game */
    PLAY,
    /** When the ships is dead (but shells still work) */
    OVER,
  }

  /** The player entity */
  private Player player;
  /** The player scale amount */
  private static final String PLAYER_FILE = "adagio.png";
  /** The player scale amount */
  private static final float PLAYER_SCALE = -0.9f;
  /** Objects for rendering the player */
  private Texture playerTexture;
  private Sprite playerSprite;

  private SpriteBatch batch;

  public void preLoadContent(AssetManager manager) {

  }
  
  public void loadContent(AssetManager manager) {

  }
  
  public void unloadContent(AssetManager manager) {

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
    this.canvas = canvas;
    gameState = GameState.INTRO;
  }

  /**
   * TODO
   */
  private void update(float delta) {
    switch (gameState) {
    case INTRO:
      gameState = GameState.PLAY;
      makePlayer();
      break;
    case PLAY:
      // TODO: move this code to play
      InputController.getInstance().readInput();
      player.move((int) InputController.getInstance().getHorizontal());
      if (InputController.getInstance().getVertical() > 0.0f) {
        if (!player.isJumping) {
          player.jump();
        }
      }
      System.out.println(player.getPosition().x + " " + player.getPosition().y);
      break;
    case OVER:
      // TODO: add what to do when the game is over
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
    playerSprite.setPosition(player.getPosition().x, player.getPosition().y);
    Gdx.gl.glClearColor(1, 1, 1, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    batch.begin();

    // Draw player
    if (player.movingLeft()) {
      playerSprite.setFlip(true,false);
    }
    else if (player.movingRight()) {
      playerSprite.setFlip(false,false);
    }
    playerSprite.draw(batch);

    batch.end();
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

  // TODO: probably move to a different class
  public void makePlayer() {
    player = new Player(0, -400);
    playerTexture = new Texture(Gdx.files.internal(PLAYER_FILE));
    playerSprite = new Sprite(playerTexture);
    playerSprite.setPosition(player.getPosition().x, player.getPosition().y);
    playerSprite.scale(PLAYER_SCALE);
    batch = new SpriteBatch();
  }
}
