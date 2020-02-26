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
 * This is the primary controller class for the Gameplay prototype
 * This is our collision controller and physics for now
 * */
public class PrototypeMode implements Screen {

    public enum GameState {
        // todo: note that this is copied from lab3, we should change this for our game as we see fit
        /** Before the game has started */
        INTRO,
        /** While we are playing the game */
        PLAY,
        /** When the ships is dead (but shells still work) */
        OVER
    }

    //information about our player
    Player player;
    Texture playerTexture;
    Sprite playerSprite;

    SpriteBatch batch;

    public void preLoadContent(AssetManager manager) {

    }
    public void loadContent(AssetManager manager) {

    }
    public void unloadContent(AssetManager manager) {

    }

    /** reference to canvas to draw content */
    private GameCanvas canvas;

    /** variable to track the game */
    private GameState gameState;
    /** whether this game mode is active*/
    private boolean active;
    //ADD VARIABLES TO DEBUG BELOW

    /**
     * todo:
     * @param canvas
     */
    public PrototypeMode(GameCanvas canvas) {
        this.canvas = canvas;
        gameState = GameState.INTRO;

    }

    /**
     * todo:
     */
    private void update(float delta) {

        switch(gameState) {
            case INTRO:
                gameState = GameState.PLAY;
                makePlayer();
            case PLAY:
                //todo: move this code to play
                InputController.getInstance().readInput();
                player.move((int) InputController.getInstance().getHorizontal());
                if (InputController.getInstance().getVertical() > 0.0f) {
                    if (!player.isJumping) {
                        player.jump();
                    }
                }
                System.out.println(player.getPosition().x + " " + player.getPosition().y);
            case OVER:
                //todo: what to do when the game is over
        }
    }

    /**
     * todo: process a single frame
     * @param delta
     */
    private void play(float delta) {

    }
    /**
     * todo:
     * @param delta
     */
    private void draw(float delta) {
        playerSprite.setPosition(player.getPosition().x, player.getPosition().y);
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        playerSprite.draw(batch);
        batch.end();
    }
    /** delta is included in the render function of screen
     *
     * @param delta
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
            //todo: implement quiting after draw function in the future
        }
    }

    /**
     * todo:
     * @param width
     * @param height
     */
    public void resize(int width, int height) {

    }

    /**
     * todo:
     */
    public void pause() {

    }

    /**
     * todo:
     */
    public void resume() {

    }

    /**
     * todo:
     */
    public void show() {
        active = true;
    }

    /**
     * todo:
     */
    public void hide() {
        active = false;
    }

    /**
     * todo:
     */
    public void dispose() {
        player = null;
        playerTexture = null;
        playerSprite = null;
        batch = null;
        canvas = null;
    }

    //probably move these functions to a difference class
    public void makePlayer() {
        //create a player at location x,y
        player = new Player(0, -400);
        //change path to a img.
        playerTexture = new Texture(Gdx.files.internal("adagio.png"));
        playerSprite = new Sprite(playerTexture);
        playerSprite.setPosition(player.getPosition().x, player.getPosition().y);
        playerSprite.scale(-.9f);

        batch = new SpriteBatch();
    }
}
