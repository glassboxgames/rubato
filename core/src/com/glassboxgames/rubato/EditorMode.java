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
import com.glassboxgames.util.*;

/**
 * Mode controller for the level editor.
 */
public class EditorMode implements Screen {
  /** UI font file */
  protected static final String FONT_FILE = "User Interface/Rajdhani-Regular.ttf";
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

  /** Texture path map */
  protected ObjectMap<String, String> pathMap;
  /** Texture map */
  protected ObjectMap<String, Texture> textureMap;
  /** Button map for the UI */
  protected ObjectMap<String, Button> uiMap;
  /** Current ghost button */
  protected Button ghost;
  /** Button map for the level */
  protected ObjectMap<String, Array<Button>> levelMap;
  /** Array tracking loaded assets */
  protected Array<String> assets = new Array<String>();
  /** Background path */
  protected String backgroundPath;
  /** Dimensions of the current level */
  protected float width, height;
  
  protected boolean initialized = false;

  /**
   * Instantiates the editor mode controller.
   * @param listener the screen exit listener
   */
  public EditorMode(ScreenListener listener) {
    this.listener = listener;
    // TODO ui stage should use viewports
    uiStage = new Stage();
    levelStage = new Stage();
    Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, levelStage));
    textureMap = new ObjectMap<String, Texture>();
    Json json = new Json();
    pathMap = json.fromJson(ObjectMap.class, Gdx.files.internal("data/editor.json"));
    uiMap = new ObjectMap<String, Button>();
    levelMap = new ObjectMap<String, Array<Button>>();
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
    final ImageButton button =
      new ImageButton(new TextureRegionDrawable(textureMap.get(key)));
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
    final Button button = new ImageButton(new TextureRegionDrawable(textureMap.get(key)));
    button.setPosition(x - button.getWidth() / 2, y - button.getHeight() / 2);
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
      levelMap.put(key, new Array<Button>());
    }
    levelMap.get(key).add(button);
    levelStage.addActor(button);
  }

  /**
   * Initializes the level editor with the given level data.
   * @param data level data
   * @param manager asset manager to use
   */
  public void initLevel(LevelData data, AssetManager manager) {
    clear();

    String[] keys = new String[] {"player", "drone", "simple", "spikes"};
    for (int i = 0; i < keys.length; i++) {
      createUIButton(keys[i], 20, Gdx.graphics.getHeight() - 70 * (i + 1), 50, 50);
    }
    
    backgroundPath = data.background;
    width = data.width * Constants.PPM;
    height = data.height * Constants.PPM;
    levelStage.addActor(new Image(manager.get(backgroundPath, Texture.class)));
    if (data.player != null) {
      createLevelButton("player",
                        data.player.x * Constants.PPM,
                        data.player.y * Constants.PPM);
    }
    for (LevelData.EnemyData enemyData : data.enemies) {
      createLevelButton(enemyData.type,
                        enemyData.x * Constants.PPM,
                        enemyData.y * Constants.PPM);
    }
    for (LevelData.PlatformData platformData : data.platforms) {
      createLevelButton(platformData.type,
                        platformData.x * Constants.PPM,
                        platformData.y * Constants.PPM);
    }
    levelStage.getCamera().position.set(Gdx.graphics.getWidth() / 2,
                                        Gdx.graphics.getHeight() / 2,
                                        levelStage.getCamera().position.z);
  }

  /**
   * Serializes the level being edited as a LevelData object.
   */
  public LevelData exportLevel() {
    LevelData data = new LevelData();
    data.background = backgroundPath;
    data.width = width / Constants.PPM;
    data.height = height / Constants.PPM;
    data.enemies = new Array<LevelData.EnemyData>();
    data.platforms = new Array<LevelData.PlatformData>();
    for (String key : levelMap.keys()) {
      switch (key) {
      case "player":
        {
          Button button = levelMap.get(key).get(0);
          data.player = new LevelData.PlayerData();
          data.player.x = (button.getX() + button.getWidth() / 2) / Constants.PPM;
          data.player.y = (button.getY() + button.getHeight() / 2) / Constants.PPM;
          break;
        }
      case "drone":
        {
          for (Button button : levelMap.get(key)) {
            LevelData.EnemyData enemy = new LevelData.EnemyData();
            enemy.type = key;
            enemy.x = (button.getX() + button.getWidth() / 2) / Constants.PPM;
            enemy.y = (button.getY() + button.getHeight() / 2) / Constants.PPM;
            data.enemies.add(enemy);
          }
          break;
        }
      case "simple":
      case "spikes":
        {
          for (Button button : levelMap.get(key)) {
            LevelData.PlatformData platform = new LevelData.PlatformData();
            platform.type = key;
            platform.x = (button.getX() + button.getWidth() / 2) / Constants.PPM;
            platform.y = (button.getY() + button.getHeight() / 2) / Constants.PPM;
            data.platforms.add(platform);
          }
          break;
        }
      }
    }
    return data;
  }

  /**
   * Clear the level map.
   */
  public void clear() {
    uiMap.clear();
    uiStage.clear();
    levelMap.clear();
    levelStage.clear();
  }

  @Override
  public void render(float delta) {
    if (active) {
      if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
        Gdx.app.exit();
        return;
      }
      if (Gdx.input.isKeyPressed(Input.Keys.P)) {
        listener.exitScreen(this, 1);
        return;
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

      Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
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
  }

  @Override
  public void hide() {
    active = false;
  }

  @Override
  public void dispose() {
    uiStage.dispose();
    levelStage.dispose();
  }
}
