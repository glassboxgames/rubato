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

  /** Editor metadata file */
  private static final String EDITOR_FILE = "Data/editor.json";
  /** Default level width */
  private static final float DEFAULT_WIDTH = 30f;
  /** Default level height */
  private static final float DEFAULT_HEIGHT = 18f;
  /** Ghost grid size */
  private static final int GRID_SIZE = 10;

  /** UI font key */
  private static final String FONT = "level_editor_font.ttf";
  /** UI font size */
  private static final int FONT_SIZE = 18;
  /** Map movement speed */
  private static final int MAP_MOVE_SPEED = 15;

  /** Listener for exit events */
  private ScreenListener listener;
  /** Font for the UI */
  private BitmapFont font;
  /** Whether this mode is active */
  private boolean active;
  /** Stage for the level */
  private Stage levelStage;
  /** Stage for the editor UI */
  private Stage uiStage;
  /** Input processor for the editor */
  private InputProcessor inputProcessor;

  /** Map of editor groups */
  private OrderedMap<String, OrderedMap<String, Array<String>>> editorGroups;
  /** Button map for the UI */
  private ObjectMap<String, ImageButton> uiMap;
  /** Current ghost */
  private Ghost ghost;
  /** Button map for the level */
  private OrderedMap<String, Array<ImageButton>> levelMap;
  /** Array tracking loaded assets */
  private Array<String> assets = new Array<String>();
  /** Current chapter name */
  private String chapterName;
  /** Dimensions of the current level */
  private float width, height;
  
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
    OrderedMap<String, JsonValue> parsed =
      Shared.JSON.fromJson(OrderedMap.class, Gdx.files.internal(EDITOR_FILE));
    editorGroups = new OrderedMap<String, OrderedMap<String, Array<String>>>();
    for (String groupName : parsed.keys()) {
      editorGroups.put(groupName, new OrderedMap<String, Array<String>>());
      for (JsonValue value : parsed.get(groupName)) {
        editorGroups.get(groupName).put(value.name(), new Array<String>(value.asStringArray()));
      }
    }
    uiMap = new ObjectMap<String, ImageButton>();
    levelMap = new OrderedMap<String, Array<ImageButton>>();
    chapterName = Shared.CHAPTER_NAMES.get(Shared.CHAPTER_FOREST);
    width = DEFAULT_WIDTH;
    height = DEFAULT_HEIGHT;
  }

  /**
   * Preloads the assets for the level editor with the given manager.
   */
  public void preloadContent(AssetManager manager) {
    manager.load(FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.SEMIBOLD_FONT_FILE, FONT_SIZE, 0));
    assets.add(FONT);
  }

  /**
   * Loads the assets for the level editor with the given manager.
   */
  public void loadContent(AssetManager manager) {
    font = manager.get(FONT, BitmapFont.class);

    Table table = new Table();
    table.setFillParent(true);
    table.top().left().pad(10);
    uiStage.addActor(table);

    Table toolbar = new Table();
    ImageButton home = createImageButton("home_icon");
    toolbar.left().padBottom(20);
    toolbar.add(home).padRight(10);
    home.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        exitToMenu();
      }
    });
    ImageButton load = createImageButton("load_icon");
    toolbar.add(load).padRight(10);
    load.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        promptLoad();
      }
    });
    ImageButton save = createImageButton("save_icon");
    toolbar.add(save).padRight(10);
    save.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        promptSave();
      }
    });
    ImageButton play = createImageButton("play_icon");
    toolbar.add(play).padRight(10);
    play.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        playtest();
      }
    });
    table.add(toolbar).left().row();
    
    for (String groupName : editorGroups.keys()) {
      Table group = new Table();
      group.padBottom(20);
      Label label = new Label(groupName, new Label.LabelStyle(font, Color.WHITE));
      group.add(label).padLeft(10).padBottom(10).left().row();
      Table icons = new Table();
      group.add(icons).left().row();
      for (String icon : editorGroups.get(groupName).keys()) {
        icons.add(createUIButton(icon, editorGroups.get(groupName).get(icon)))
          .width(35).height(35).padLeft(10).left();
      }
      table.add(group).left().row();
    }

    Image img = new Image(Shared.TEXTURE_MAP.get(chapterName));
    img.setAlign(Align.bottomLeft);
    img.setScale(Shared.BACKGROUND_SCALE);
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
   * Creates an ImageButton from the given texture shortname.
   */
  private ImageButton createImageButton(String key) {
    return new ImageButton(new TextureRegionDrawable(Shared.TEXTURE_MAP.get(key)));
  }

  /**
   * Creates a UI button.
   * @param key the key for the button
   */
  private Button createUIButton(final String key, final Array<String> options) {
    final ImageButton button = createImageButton(key);
    button.getImage().setScaling(Scaling.fit);
    button.addListener(new ChangeListener() {
      public void changed(ChangeEvent event, Actor actor) {
        ghost = new Ghost(options, 0);
      }
    });
    uiMap.put(key, button);
    return button;
  }

  /**
   * Creates a level button with the given key.
   * @param key the key for the button
   * @param x the x coordinate
   * @param y the y coordinate
   */
  private void createLevelButton(final String key, float x, float y) {
    final ImageButton button = createImageButton(key);
    button.setPosition(x - button.getWidth() / 2, y - button.getHeight() / 2);
    button.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent event, float x, float y) {
        Array<String> options = null;
        int index = -1;
        for (OrderedMap<String, Array<String>> map : editorGroups.values()) {
          boolean found = false;
          for (String icon : map.keys()) {
            index = map.get(icon).indexOf(key, false);
            if (index != -1) {
              options = map.get(icon);
              found = true;
              break;
            }
          }
          if (found) {
            break;
          }
        }
        ghost = new Ghost(options, index);
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
   */
  public void loadLevel(LevelData data) {
    clear();
    
    width = data.width;
    height = data.height;
    chapterName = data.chapter;
    Image img = new Image(Shared.TEXTURE_MAP.get(chapterName));
    img.setWidth(width * Shared.PPM);
    img.setHeight(height * Shared.PPM);
    levelStage.addActor(img);
    if (data.player != null) {
      createLevelButton("player",
                        data.player.x * Shared.PPM,
                        data.player.y * Shared.PPM);
    }
    for (EnemyData enemyData : data.enemies) {
      createLevelButton(enemyData.type,
                        enemyData.x * Shared.PPM,
                        enemyData.y * Shared.PPM);
    }
    for (PlatformData platformData : data.platforms) {
      createLevelButton(platformData.type,
                        platformData.x * Shared.PPM,
                        platformData.y * Shared.PPM);
    }
    if (data.checkpoint != null) {
      createLevelButton("checkpoint",
                        data.checkpoint.x * Shared.PPM,
                        data.checkpoint.y * Shared.PPM);
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
    data.chapter = chapterName;
    data.width = width;
    data.height = height;
    data.enemies = new Array<EnemyData>();
    data.platforms = new Array<PlatformData>();
    for (String key : levelMap.keys()) {
      switch (key) {
      case "player":
        {
          ImageButton button = levelMap.get(key).get(0);
          data.player = new PlayerData();
          data.player.x = getCenterX(button) / Shared.PPM;
          data.player.y = getCenterY(button) / Shared.PPM;
          break;
        }
      case "checkpoint": 
        {
          ImageButton button = levelMap.get(key).get(0);
          data.checkpoint = new CheckpointData();
          data.checkpoint.x = getCenterX(button) / Shared.PPM;
          data.checkpoint.y = getCenterY(button) / Shared.PPM;
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
            enemy.x = getCenterX(button) / Shared.PPM;
            enemy.y = getCenterY(button) / Shared.PPM;
            data.enemies.add(enemy);
          }
          break;
        }
      case "tb_forest":
      case "t_forest":
      case "m_forest":
      case "b_forest":
      case "b_wood_spikes":
      case "l_wood_spikes":
      case "t_wood_spikes":
      case "r_wood_spikes":
        {
          for (ImageButton button : levelMap.get(key)) {
            PlatformData platform = new PlatformData();
            platform.type = key;
            platform.x = getCenterX(button) / Shared.PPM;
            platform.y = getCenterY(button) / Shared.PPM;
            data.platforms.add(platform);
          }
          break;
        }
      }
    }
    return data;
  }

  /**
   * Removes the current ghost.
   */
  public void removeGhost() {
    if (ghost != null) {
      ghost.button.remove();
      ghost = null;
    }
  }

  /**
   * Clear the editor state.
   */
  public void clear() {
    removeGhost();
    levelMap.clear();
    levelStage.clear();
  }

  /**
   * Triggers an exit to the main menu.
   */
  private void exitToMenu() {
    listener.exitScreen(this, EXIT_MENU);
  }

  /**
   * Triggers a save prompt.
   */
  private void promptSave() {
    Gdx.input.getTextInput(new Input.TextInputListener() {
      public void canceled() {}

      public void input(String text) {
        Gdx.files.local(text).writeString(Shared.JSON.prettyPrint(exportLevel()), false);
      }
    }, "Save level to file", "Levels/", "");
  }

  /**
   * Triggers a load prompt.
   */
  private void promptLoad() {
    Gdx.input.getTextInput(new Input.TextInputListener() {
      public void canceled() {}

      public void input(String text) {
        loadLevel(Shared.JSON.fromJson(LevelData.class, Gdx.files.local(text)));
      }
    }, "Load level from file", "Levels/", "");
  }

  /**
   * Triggers playtesting.
   */
  private void playtest() {
    Array<ImageButton> players = levelMap.get("player");
    Array<ImageButton> checkpoints = levelMap.get("checkpoint");
    if (players != null && !players.isEmpty() && checkpoints != null && !checkpoints.isEmpty()) {
      listener.exitScreen(this, EXIT_TEST);
    }
  }

  @Override
  public void render(float delta) {
    if (active) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        exitToMenu();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
        playtest();
        return;
      }

      if (ghost != null) {
        float x = getViewportX() + getMouseX() - ghost.button.getWidth() / 2;
        float y = getViewportY() + getMouseY() - ghost.button.getHeight() / 2;
        ghost.button.setPosition(GRID_SIZE * Math.round(x / GRID_SIZE) - getViewportX(),
                                 GRID_SIZE * Math.round(y / GRID_SIZE) - getViewportY());
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
          ghost.changeTexture(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
          ghost.changeTexture(1);
        }
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
                                          width * Shared.PPM - screenWidth / 2);
      camera.position.y = MathUtils.clamp(camera.position.y,
                                          screenHeight / 2,
                                          height * Shared.PPM - screenHeight / 2);
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
    removeGhost();
    active = false;
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    uiStage.dispose();
    levelStage.dispose();
  }

  /**
   * Class representing a ghost for placement.
   */
  private class Ghost {
    /** Current index */
    public int index;
    /** Array of ghost button styles */
    public Array<ImageButton.ImageButtonStyle> styles;
    /** Current button */
    public ImageButton button;

    /**
     * Initializes a ghost.
     */
    public Ghost(final Array<String> textures, int startIndex) {
      index = startIndex;
      styles = new Array<ImageButton.ImageButtonStyle>();
      for (String texture : textures) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(Shared.TEXTURE_MAP.get(texture));
        styles.add(style);
      }
      button = new ImageButton(styles.get(index));
      button.getColor().a = 0.5f;
      button.addListener(new ClickListener(Input.Buttons.LEFT) {
        public void clicked(InputEvent event, float x, float y) {
          createLevelButton(textures.get(index),
                            getViewportX() + ghost.button.getX() + ghost.button.getWidth() / 2,
                            getViewportY() + ghost.button.getY() + ghost.button.getHeight() / 2);
        }
      });
      button.addListener(new ClickListener(Input.Buttons.RIGHT) {
        public void clicked(InputEvent event, float x, float y) {
          button.remove();
        }
      });
      button.addListener(new InputListener() {
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
          changeTexture(amount);
          return true;
        }
      });
      uiStage.addActor(button);
      uiStage.setScrollFocus(button);
    }

    /**
     * Changes the currently selected texture.
     */
    public void changeTexture(int direction) {
      index = (index + styles.size + direction) % styles.size;
      button.setStyle(styles.get(index));
    }
  }
}
