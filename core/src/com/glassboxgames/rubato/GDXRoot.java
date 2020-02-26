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
        PrototypeMode gamemode = new PrototypeMode(canvas);
        setScreen(gamemode);

    }

    @Override
    public void dispose() {
        // Unload all of the resources
        batch.dispose();
        playerTexture.dispose();
        super.dispose();
    }
}
