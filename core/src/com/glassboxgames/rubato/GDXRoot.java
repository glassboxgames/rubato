package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.*;

public class GDXRoot extends Game {
    int x,y;
    /** AssetManager to load game assets (textures, sounds, etc.) */
    private AssetManager manager;
    /** Drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    /** Player instance */
    private Player player;
    /** Sprite batch */
    private SpriteBatch batch;
    /** Player sprite */
    private Sprite playerSprite;
    /** The player image */
    private static final String PLAYER_FILE = "../../assets/adagio.png";
    /** The player texture */
    private static Texture playerTexture;

    public GDXRoot() {
        manager = new AssetManager();
    }

    public void PreLoadContent() {
        manager.load(PLAYER_FILE, Texture.class);
    }

    public void LoadContent() {
        playerTexture = manager.get(PLAYER_FILE, Texture.class);
    }

    @Override
    public void create() {
        canvas  = new GameCanvas();
        player = new Player(20, 0);

        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal("adagio.png"));
        playerSprite = new Sprite(playerTexture);
        playerSprite.setPosition(0, -100);
        playerSprite.scale(-.8f);
        update();
    }

    public void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.move("left");
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.move("right");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (!player.isJumping()) {
                player.jump();
            }
        }
        System.out.println(x + " " + y);

        /** TODO: CHECK FOR COLLISIONS **/
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        playerSprite.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        // Unload all of the resources
        batch.dispose();
        playerTexture.dispose();
        super.dispose();
    }
}
