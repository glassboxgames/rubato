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

  public GDXRoot() {}

  @Override
  public void create() {
    canvas = new GameCanvas();
    PrototypeMode mode = new PrototypeMode(canvas);
    setScreen(mode);
  }

  @Override
  public void dispose() {
    // Unload all of the resources
    super.dispose();
  }
}
