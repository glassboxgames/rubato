package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.entity.*;
import com.glassboxgames.rubato.serialize.*;
import com.glassboxgames.util.*;

/**
 * Mode controller for the level editor.
 */
public class EditorMode implements Screen {
  /** Exit code for returning to menu */
  public static final int EXIT_MENU = 0;
  /** Exit code for playtesting */
  public static final int EXIT_TEST = 1;

  /** Default editor background */
  protected static final String DEFAULT_BACKGROUND = "forest";
  /** Default level width */
  protected static final float DEFAULT_WIDTH = 30f;
  /** Default level height */
  protected static final float DEFAULT_HEIGHT = 10f;
  
  /** Editor image path file */
  protected static final String EDITOR_FILE = "Config/editor.json";
  /** UI font file */
  protected static final String FONT_FILE = "Fonts/Rajdhani-Regular.ttf";
  /** UI font size */
  protected static final int FONT_SIZE = 18;
  /** Map movement speed */
  protected static final int MAP_MOVE_SPEED = 15;
  /** Ghost grid size */
  protected static final int GRID_SIZE = 10;

  /** Listener for exit events */
  protected ScreenListener listener;
  /** Font for the UI */
  protected BitmapFont font;
  /** Whether this mode is active */
  protected boolean active;
  /** Stage for the level */
  protected Stage levelStage;
  /** Stage for the editor UI */
  protected Stage uiStage;
  /** Input processor for the editor */
  protected InputProcessor inputProcessor;

  /** Texture path map */
  protected ObjectMap<String, String> pathMap;
  /** Texture map */
  protected ObjectMap<String, Texture> textureMap;
  /** Button map for the UI */
  protected ObjectMap<String, ImageButton> uiMap;
  /** Current ghost button */
  protected ImageButton ghost;
  /** Button map for the level */
  protected ObjectMap<String, Array<ImageButton>> levelMap;
  /** Array tracking loaded assets */
  protected Array<String> assets = new Array<String>();
  /** Background key */
  protected String background = DEFAULT_BACKGROUND;
  /** Dimensions of the current level */
  protected float width, height;
  
  /**
   * Instantiates the editor mode controller.
   * @param listener the screen exit listener
   */
  public EditorMode(ScreenListener listener) {
    this.listener = listener;
    // TODO ui stage should use viewports
    uiStage = new Stage();
    levelStage = new Stage();
    inputProcessor = new InputMultiplexer(uiStage, levelStage);
    textureMap = new ObjectMap<String, Texture>();
    pathMap = Shared.JSON.fromJson(ObjectMap.class, Gdx.files.internal(EDITOR_FILE));
    pathMap.putAll(Shared.BACKGROUND_PATHS);
    uiMap = new ObjectMap<String, ImageButton>();
    levelMap = new ObjectMap<String, Array<ImageButton>>();
  }

  /**
   * Preloads the assets for the level editor with the given manager.
   */
  public void preloadContent(AssetManager manager) {
    FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    size2Params.fontFileName = FONT_FILE;
    size2Params.fontParameters.size = FONT_SIZE;
    manager.load(FONT_FILE, BitmapFont.class, size2Params);
    assets.add(FONT_FILE);
    for (String path : pathMap.values()) {
      manager.load(path, Texture.class);
      assets.add(path);
    }
  }

  /**
   * Loads the assets for the level editor with the given manager.
   */
  public void loadContent(AssetManager manager) {
    font = manager.get(FONT_FILE, BitmapFont.class);
    for (String key : pathMap.keys()) {
      textureMap.put(key, manager.get(pathMap.get(key), Texture.class));
    }

    String[] keys = new String[] {
      "player", "checkpoint", "simple", "crumbling",
      "bottom_spikes", "left_spikes", "top_spikes", "right_spikes",
      "spider", "wisp", "wyrm", "blob",
    };
    int buttonSize = 50;
    int buttonSpacing = 20;
    for (int i = 0; i < keys.length; i++) {
      createUIButton(keys[i],
                     buttonSpacing + i * (buttonSize + buttonSpacing),
                     Gdx.graphics.getHeight() - buttonSize - buttonSpacing,
                     buttonSize, buttonSize);
    }
    width = DEFAULT_WIDTH * Shared.SCALED_PPM;
    height = DEFAULT_HEIGHT * Shared.SCALED_PPM;
    Image img = new Image(textureMap.get(background));
    img.setWidth(width);
    img.setHeight(height);
    levelStage.addActor(img);
  }

  /**
   * Unloads the assets for the level editor with the given manager.
   */
  public void unloadContent(AssetManager manager) {
    for (String s : assets) {
      if (manager.isLoaded(s)) {
        manager.unload(s);
      }
    }
  }

  /**
   * Returns the x coordinate of the mouse with bottom left origin.
   */
  private float getMouseX() {
    return Gdx.input.getX();
  }

  /**
   * Returns the y coordinate of the mouse with bottom left origin.
   */
  private float getMouseY() {
    return Gdx.graphics.getHeight() - Gdx.input.getY();
  }

  /**
   * Returns the x coordinate of the viewport's lower left corner.
   */
  private float getViewportX() {
    return (int)levelStage.getCamera().position.x - Gdx.graphics.getWidth() / 2;
  }

  /**
   * Returns the y coordinate of the viewport's lower left hand corner.
   */
  private float getViewportY() {
    return (int)levelStage.getCamera().position.y - Gdx.graphics.getHeight() / 2;
  }
  
  /**
   * Creates a UI button with the given key.
   * @param key the key for the button
   * @param x the x coordinate
   * @param y the y coordinate
   * @param w the button width
   * @param h the button height
   */
  private void createUIButton(final String key, float x, float y, float w, float h) {
    final ImageButton button = new ImageButton(new TextureRegionDrawable(textureMap.get(key)));
    button.setPosition(x, y);
    button.setWidth(w);
    button.setHeight(h);
    button.getImage().setScaling(Scaling.fit);
    button.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        createGhostButton(key);
      }
    });
    uiMap.put(key, button);
    uiStage.addActor(button);
  }

  /**
   * Creates a ghost button with the given key.
   */
  private void createGhostButton(final String key) {
    if (ghost == null) {
      ghost = new ImageButton(new TextureRegionDrawable(textureMap.get(key)));
      ghost.getImage().setScale(Shared.SCALE);
      ghost.getColor().a = 0.5f;
      ghost.addListener(new ClickListener(Input.Buttons.LEFT) {
        public void clicked(InputEvent event, float x, float y) {
          createLevelButton(key,
                            getViewportX() + ghost.getX() + ghost.getWidth() / 2,
                            getViewportY() + ghost.getY() + ghost.getHeight() / 2);
        }
      });
      ghost.addListener(new ClickListener(Input.Buttons.RIGHT) {
        public void clicked(InputEvent event, float x, float y) {
          ghost.remove();
          ghost = null;
        }
      });
      uiStage.addActor(ghost);
    }
  }

  /**
   * Creates a level button with the given key.
   * @param key the key for the button
   * @param x the x coordinate
   * @param y the y coordinate
   */
  private void createLevelButton(final String key, float x, float y) {
    final ImageButton button = new ImageButton(new TextureRegionDrawable(textureMap.get(key)));
    button.setPosition(x - button.getWidth() / 2, y - button.getHeight() / 2);
    button.getImage().setScale(Shared.SCALE);
    button.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent event, float x, float y) {
        createGhostButton(key);
        levelMap.get(key).removeValue(button, true);
        button.remove();
      }
    });
    button.addListener(new ClickListener(Input.Buttons.RIGHT) {
      public void clicked(InputEvent event, float x, float y) {
        levelMap.get(key).removeValue(button, true);
        button.remove();
      }
    });
    if (!levelMap.containsKey(key)) {
      levelMap.put(key, new Array<ImageButton>());
    }
    levelMap.get(key).add(button);
    levelStage.addActor(button);
  }
  
  /**
   * Loads the level from the given file.
   * @param data level data
   * @param manager asset manager to use
   */
  public void loadLevel(LevelData data) {
    clear();
    
    width = data.width * Shared.SCALED_PPM;
    height = data.height * Shared.SCALED_PPM;
    background = data.background;
    Image img = new Image(textureMap.get(background));
    img.setWidth(width);
    img.setHeight(height);
    levelStage.addActor(img);
    if (data.player != null) {
      createLevelButton("player",
                        data.player.x * Shared.SCALED_PPM,
                        data.player.y * Shared.SCALED_PPM);
    }
    for (EnemyData enemyData : data.enemies) {
      createLevelButton(enemyData.type,
                        enemyData.x * Shared.SCALED_PPM,
                        enemyData.y * Shared.SCALED_PPM);
    }
    for (PlatformData platformData : data.platforms) {
      createLevelButton(platformData.type,
                        platformData.x * Shared.SCALED_PPM,
                        platformData.y * Shared.SCALED_PPM);
    }
    if (data.checkpoint != null) {
      createLevelButton("checkpoint",
                        data.checkpoint.x * Shared.SCALED_PPM,
                        data.checkpoint.y * Shared.SCALED_PPM);
    }
    levelStage.getCamera().position.set(Gdx.graphics.getWidth() / 2,
                                        Gdx.graphics.getHeight() / 2,
                                        levelStage.getCamera().position.z);
  }

  /**
   * Returns the x coordinate of the center of an actor.
   */
  private float getCenterX(Actor actor) {
    return actor.getX() + actor.getWidth() / 2;
  }

  /**
   * Returns the y coordinate of the center of an actor.
   */
  private float getCenterY(Actor actor) {
    return actor.getY() + actor.getHeight() / 2;
  }
  
  /**
   * Serializes the level being edited as a LevelData object.
   */
  public LevelData exportLevel() {
    LevelData data = new LevelData();
    data.background = background;
    data.width = width / Shared.SCALED_PPM;
    data.height = height / Shared.SCALED_PPM;
    data.enemies = new Array<EnemyData>();
    data.platforms = new Array<PlatformData>();
    for (String key : levelMap.keys()) {
      switch (key) {
      case "player":
        {
          ImageButton button = levelMap.get(key).get(0);
          data.player = new PlayerData();
          data.player.x = getCenterX(button) / Shared.SCALED_PPM;
          data.player.y = getCenterY(button) / Shared.SCALED_PPM;
          break;
        }
      case "spider":
      case "wisp":
      case "wyrm":
      case "blob":
        {
          for (ImageButton button : levelMap.get(key)) {
            EnemyData enemy = new EnemyData();
            enemy.type = key;
            enemy.x = getCenterX(button) / Shared.SCALED_PPM;
            enemy.y = getCenterY(button) / Shared.SCALED_PPM;
            data.enemies.add(enemy);
          }
          break;
        }
      case "simple":
      case "crumbling":
      case "bottom_spikes":
      case "left_spikes":
      case "top_spikes":
      case "right_spikes":
        {
          for (ImageButton button : levelMap.get(key)) {
            PlatformData platform = new PlatformData();
            platform.type = key;
            platform.x = getCenterX(button) / Shared.SCALED_PPM;
            platform.y = getCenterY(button) / Shared.SCALED_PPM;
            data.platforms.add(platform);
          }
          break;
        }
      case "checkpoint":
        {
          ImageButton button = levelMap.get(key).get(0);
          data.checkpoint = new CheckpointData();
          data.checkpoint.x = getCenterX(button) / Shared.SCALED_PPM;
          data.checkpoint.y = getCenterY(button) / Shared.SCALED_PPM;
          break;
        }
      }
    }
    return data;
  }

  /**
   * Clear the editor state.
   */
  public void clear() {
    if (ghost != null) {
      ghost.remove();
      ghost = null;
    }
    levelMap.clear();
    levelStage.clear();
  }

  @Override
  public void render(float delta) {
    if (active) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        listener.exitScreen(this, EXIT_MENU);
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
        listener.exitScreen(this, EXIT_TEST);
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
        // save
        Gdx.input.getTextInput(new Input.TextInputListener() {
          public void canceled() {}

          public void input(String text) {
            Gdx.files.local(text).writeString(Shared.JSON.prettyPrint(exportLevel()), false);
          }
        }, "Save level to file", "Levels/", "");
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
        // load
        Gdx.input.getTextInput(new Input.TextInputListener() {
          public void canceled() {}

          public void input(String text) {
            loadLevel(Shared.JSON.fromJson(LevelData.class, Gdx.files.local(text)));
          }
        }, "Load level from file", "Levels/", "");
      }

      if (ghost != null) {
        float x = getViewportX() + getMouseX() - ghost.getWidth() / 2;
        float y = getViewportY() + getMouseY() - ghost.getHeight() / 2;
        ghost.setPosition(GRID_SIZE * Math.round(x / GRID_SIZE) - getViewportX(),
                          GRID_SIZE * Math.round(y / GRID_SIZE) - getViewportY());
      }

      // Camera movement
      Camera camera = levelStage.getCamera();
      int screenWidth = Gdx.graphics.getWidth();
      int screenHeight = Gdx.graphics.getHeight();
      if (Gdx.input.isKeyPressed(Input.Keys.A)) {
        camera.position.x -= MAP_MOVE_SPEED;
      }
      if (Gdx.input.isKeyPressed(Input.Keys.D)) {
        camera.position.x += MAP_MOVE_SPEED;
      }
      if (Gdx.input.isKeyPressed(Input.Keys.S)) {
        camera.position.y -= MAP_MOVE_SPEED;
      }
      if (Gdx.input.isKeyPressed(Input.Keys.W)) {
        camera.position.y += MAP_MOVE_SPEED;
      }
      camera.position.x = MathUtils.clamp(camera.position.x,
                                          screenWidth / 2,
                                          width - screenWidth / 2);
      camera.position.y = MathUtils.clamp(camera.position.y,
                                          screenHeight / 2,
                                          height - screenHeight / 2);
      camera.update();

      Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
      levelStage.act(delta);
      levelStage.draw();
      uiStage.act(delta);
      uiStage.draw();
    }
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void show() {
    active = true;
    Gdx.input.setInputProcessor(inputProcessor);
  }

  @Override
  public void hide() {
    if (ghost != null) {
      ghost.remove();
      ghost = null;
    }
    active = false;
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    uiStage.dispose();
    levelStage.dispose();
  }
}
