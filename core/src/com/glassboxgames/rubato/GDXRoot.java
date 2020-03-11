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
  /** AssetManager to load game assets (textures, sounds, etc.) */
  private AssetManager manager;
  /** Drawing context to display graphics (VIEW CLASS) */
  private GameCanvas canvas;

  public GDXRoot() {
    // Start loading with the asset manager
    manager = new AssetManager();

    // Add font support to the asset manager
    FileHandleResolver resolver = new InternalFileHandleResolver();
  }

  @Override
  public void create() {
    canvas = new GameCanvas();
    // canvas.setFullscreen(true, false);
    PrototypeMode mode = new PrototypeMode(canvas);
    setScreen(mode);
  }
}
