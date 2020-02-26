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

    public GDXRoot() { }

    @Override
    public void create() {
        canvas  = new GameCanvas();
        player = new Player(0, -400);

        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal("adagio.png"));
        playerSprite = new Sprite(playerTexture);
        playerSprite.setPosition(player.getX(), player.getY());
        playerSprite.scale(-.9f);
    }

    public void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.move(-1);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.move(1);
        } else {
            player.move(0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (!player.isJumping()) {
                player.jump();
            }
        }
        System.out.println(x + " " + y);

        playerSprite.setPosition(player.getX(), player.getY());

        /** TODO: CHECK FOR COLLISIONS **/
    }

    @Override
    public void render() {
        update();

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
