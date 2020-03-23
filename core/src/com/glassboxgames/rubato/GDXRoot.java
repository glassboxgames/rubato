package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;

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
    manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
    manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
  }

  @Override
  public void create() {
    canvas = new GameCanvas();
    // canvas.setFullscreen(true, false);
    PrototypeMode mode = new PrototypeMode(manager, canvas);
    setScreen(mode);
  }
}
