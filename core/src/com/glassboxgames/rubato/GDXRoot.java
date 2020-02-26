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
    /** Drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    /** Player instance */
    private Player player;
    /** Sprite batch */
    private SpriteBatch batch;
    /** Player sprite */
    private Sprite playerSprite;
    /** The player sprite scale amount */
    private static final float PLAYER_SCALE = 0.2f;
    /** The player image */
    private static final String PLAYER_FILE = "adagio.png";
    /** The player texture */
    private static Texture playerTexture;

    public GDXRoot() { }

    @Override
    public void create() {
        canvas  = new GameCanvas();
        // controller = new EntityController();
        player = new Player(200, 100);

        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal(PLAYER_FILE));
        playerSprite = new Sprite(playerTexture);
        playerSprite.setPosition(player.getX(), player.getY());
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
        // System.out.println(player.getX() + " " + player.getY()); // for debugging
        playerSprite.setPosition(player.getX(), player.getY());

        /** TODO: CHECK FOR COLLISIONS */
    }

    @Override
    public void render() {
        update();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        canvas.begin();
        // Draw player sprite
        int xScale = 1;
        if (player.moving() > 5 && player.moving() < 8) {
            xScale = -1;
        }
        System.out.println(player.moving());
        canvas.draw(playerSprite,new Color(1f,1f,1f,1f),playerSprite.getOriginX(),playerSprite.getOriginY(),player.getX(),player.getY(),0,PLAYER_SCALE*xScale,PLAYER_SCALE);


        canvas.end();
    }

    @Override
    public void dispose() {
        // Unload all of the resources
        canvas.dispose();
        playerTexture.dispose();
        super.dispose();
    }
}
