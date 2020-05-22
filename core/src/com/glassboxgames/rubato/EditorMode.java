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
import com.glassboxgames.rubato.entity.Tooltip;
import com.glassboxgames.rubato.serialize.*;
import com.glassboxgames.util.*;

import java.util.Comparator;

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
  /** Ghost grid size */
  private static final int GRID_SIZE = 5;
  /** Ghost snap threshold */
  private static final float SNAP_THRESHOLD = 200f;
  /** Map movement speed */
  private static final int MAP_MOVE_SPEED = 15;

  /** Listener for exit events */
  private ScreenListener listener;
  /** Whether this mode is active */
  private boolean active;
  /** Stage for the level */
  private Stage levelStage;
  /** Stage for the editor UI */
  private Stage uiStage;
  /** Input processor for the editor */
  private InputProcessor inputProcessor;

  /** Map of background drawables */
  private ObjectMap<String, Drawable> backgroundMap;
  /** Current background image */
  private Image background;
  /** Map of editor groups */
  private OrderedMap<String, OrderedMap<String, Array<String>>> editorGroups;
  /** Current ghost */
  private Ghost ghost;
  /** Button map for the level */
  private OrderedMap<String, Array<ImageButton>> levelMap;
  /** Entity buttons, sorted by y coordinate */
  private Array<ImageButton> levelButtons;
  /** Current chapter name */
  private String chapterName;
  /** Chapter button map */
  private ObjectMap<String, Button> chapterButtonMap;

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
    backgroundMap = new ObjectMap<String, Drawable>();
    levelMap = new OrderedMap<String, Array<ImageButton>>();
    levelButtons = new Array<ImageButton>();
    chapterName = Shared.CHAPTER_NAMES.get(Shared.CHAPTER_FOREST);
    chapterButtonMap = new ObjectMap<String, Button>();
  }

  /**
   * Initializes the level editor UI.
   */
  public void initUI() {
    Table table = new Table();
    table.setFillParent(true);
    table.top().left().pad(20);
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
    table.add(toolbar).padBottom(10).left().row();

    Table biomes = new Table();
    biomes
      .add(new Label("BIOME", new Label.LabelStyle(Shared.getFont("editor.header.ttf"), Color.WHITE)))
      .padBottom(10).left().row();
    for (String name : Shared.CHAPTER_NAMES) {
      Drawable deselected = Shared.getDrawable("choice_deselected");
      Drawable selected = Shared.getDrawable("choice_selected");
      ImageTextButton.ImageTextButtonStyle style =
        new ImageTextButton.ImageTextButtonStyle();
      style.font = Shared.getFont("editor.item.ttf");
      style.imageUp = deselected;
      style.imageChecked = selected;
      final ImageTextButton button =
        new ImageTextButton(name.substring(0, 1).toUpperCase() + name.substring(1), style);
      button.getImageCell().padRight(10);
      final String newChapterName = name;
      button.addListener(new ClickListener(Input.Buttons.LEFT) {
        public void clicked(InputEvent e, float x, float y) {
          chapterName = newChapterName;
        }
      });
      backgroundMap.put(name, Shared.getDrawable(name));
      chapterButtonMap.put(name, button);
      biomes.add(button).padBottom(10).left().row();
    }
    table.add(biomes).padLeft(10).padBottom(20).left().row();
    
    for (String groupName : editorGroups.keys()) {
      Table group = new Table();
      group.padLeft(10).padBottom(20);
      group
        .add(new Label(groupName.toUpperCase(),
                       new Label.LabelStyle(Shared.getFont("editor.header.ttf"), Color.WHITE)))
        .padBottom(10).left().row();
      Table icons = new Table();
      group.add(icons).left().row();
      for (String icon : editorGroups.get(groupName).keys()) {
        icons.add(createUIButton(icon, editorGroups.get(groupName).get(icon)))
          .width(35).height(35).padRight(10).left();
      }
      table.add(group).padBottom(20).left().row();
    }

    background = new Image(backgroundMap.get(chapterName));
    background.setAlign(Align.bottomLeft);
    background.setScale(Shared.BACKGROUND_SCALE);
    levelStage.addActor(background);
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
    return new ImageButton(Shared.getDrawable(key));
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
        if (options == null) {
          options = new Array<String>();
          options.add(key);
          index = 0;
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
    int index = levelButtons.size;
    for (int i = 0; i < levelButtons.size; i++) {
      if (button.getY() < levelButtons.get(i).getY()) {
        index = i;
        break;
      }
    }
    levelButtons.insert(index, button);
    for (int i = 0; i < levelButtons.size; i++) {
      levelButtons.get(i).setZIndex(i);
    }
    background.toBack();
  }
  
  /**
   * Loads the level from the given file.
   * @param data level data
   */
  public void loadLevel(LevelData data) {
    clear();
    levelStage.addActor(background);
    chapterName = data.chapter;
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
    if (data.tooltips != null) {
      for (TooltipData tooltipData : data.tooltips) {
        createLevelButton(tooltipData.type,
                          tooltipData.x * Shared.PPM,
                          tooltipData.y * Shared.PPM);
      }
    }
    if (data.altar != null) {
      createLevelButton("altar",
                        data.altar.x * Shared.PPM,
                        data.altar.y * Shared.PPM);
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
    data.enemies = new Array<EnemyData>();
    data.platforms = new Array<PlatformData>();
    data.tooltips = new Array<TooltipData>();
    for (String key : levelMap.keys()) {
      boolean isEnemy = false;
      for (Array<String> textures : editorGroups.get("Enemies").values()) {
        if (textures.contains(key, false)) {
          isEnemy = true;
          break;
        }
      }

      boolean isPlatform = false;
      for (Array<String> textures : editorGroups.get("Platforms").values()) {
        if (textures.contains(key, false)) {
          isPlatform = true;
          break;
        }
      }

      if (key.equals("player")) {
        ImageButton button = levelMap.get(key).get(0);
        data.player = new PlayerData();
        data.player.x = getCenterX(button) / Shared.PPM;
        data.player.y = getCenterY(button) / Shared.PPM;
      } else if (key.equals("checkpoint")) {
        ImageButton button = levelMap.get(key).get(0);
        data.checkpoint = new CheckpointData();
        data.checkpoint.x = getCenterX(button) / Shared.PPM;
        data.checkpoint.y = getCenterY(button) / Shared.PPM;
      } else if (isEnemy) {
        for (ImageButton button : levelMap.get(key)) {
          EnemyData enemy = new EnemyData();
          enemy.type = key;
          enemy.x = getCenterX(button) / Shared.PPM;
          enemy.y = getCenterY(button) / Shared.PPM;
          data.enemies.add(enemy);
        }
      } else if (isPlatform) {
        for (ImageButton button : levelMap.get(key)) {
          PlatformData platform = new PlatformData();
          platform.type = key;
          platform.x = getCenterX(button) / Shared.PPM;
          platform.y = getCenterY(button) / Shared.PPM;
          data.platforms.add(platform);
        }
      } else if (key.equals("attack_card") || key.equals("jump_card") || key.equals("run_card") ||
                 key.equals("pause_card") || key.equals("reset_card")) {
        for (ImageButton button : levelMap.get(key)) {
          TooltipData tooltip = new TooltipData();
          tooltip.type = key;
          tooltip.x = getCenterX(button) / Shared.PPM;
          tooltip.y = getCenterY(button) / Shared.PPM;
          data.tooltips.add(tooltip);
        }
      } else if (key.equals("altar")) {
        ImageButton button = levelMap.get(key).get(0);
        data.altar = new AltarData();
        data.altar.x = getCenterX(button) / Shared.PPM;
        data.altar.y = getCenterY(button) / Shared.PPM;
      }
    }
    data.platforms.sort(new Comparator<PlatformData>() {
      @Override
      public int compare(PlatformData o1, PlatformData o2) {
        return (int) Math.signum(o1.y - o2.y);
      }
    });


    float furthestX = data.altar != null ? data.altar.x + (Gdx.graphics.getWidth() / 2 / Shared.PPM) : 0;
    float furthestY = (data.altar != null ? data.altar.y : 0) + Gdx.graphics.getHeight() / 2 / Shared.PPM;
    for (PlatformData platform : data.platforms) {
      furthestX = Math.max(furthestX, platform.x);
      furthestY = Math.max(furthestY, platform.y);
    }
    data.width = MathUtils.clamp(furthestX,
                                 Gdx.graphics.getWidth() / Shared.PPM,
                                 background.getWidth() / Shared.PPM);
    data.height = furthestY + Gdx.graphics.getHeight() / 2 / Shared.PPM;

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
        Gdx.files.external(Shared.EXTERNAL_PATH + text).writeString(Shared.JSON.prettyPrint(exportLevel()),
                                                                    false);
      }
    }, "Save level to file", "", "Relative to ~/Rubato/");
  }

  /**
   * Triggers a load prompt.
   */
  private void promptLoad() {
    Gdx.input.getTextInput(new Input.TextInputListener() {
      public void canceled() {}

      public void input(String text) {
        loadLevel(Shared.JSON.fromJson(LevelData.class, Gdx.files.external(Shared.EXTERNAL_PATH + text)));
      }
    }, "Load level from file", "", "Relative to ~/Rubato/");
  }

  /**
   * Triggers playtesting.
   */
  private void playtest() {
    Array<ImageButton> players = levelMap.get("player");
    if (players != null && !players.isEmpty()) {
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
        float width = ghost.button.getWidth();
        float height = ghost.button.getHeight();
        float x = getViewportX() + getMouseX() - width / 2;
        float y = getViewportY() + getMouseY() - height / 2;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
          Vector2 temp = new Vector2(x, y);
          float dist = -1;
          ImageButton closest = null;
          for (Array<ImageButton> buttons : levelMap.values()) {
            for (ImageButton button : buttons) {
              float newDist = temp.dst(button.getX(), button.getY());
              if (closest == null || newDist < dist) {
                dist = newDist;
                closest = button;
              }
            }
          }
        
          if (closest != null && dist < SNAP_THRESHOLD) {
            float otherX = closest.getX();
            float otherY = closest.getY();
            float otherWidth = closest.getWidth();
            float otherHeight = closest.getHeight();
            temp.sub(otherX, otherY);
            if (Math.abs(temp.x) < Math.abs(temp.y)) {
              x = otherX;
            } else {
              y = otherY;
            }
          }
        }
        
        ghost.button.setPosition(GRID_SIZE * Math.round(x / GRID_SIZE) - getViewportX(),
                                 GRID_SIZE * Math.round(y / GRID_SIZE) - getViewportY());
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
          ghost.changeTexture(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
          ghost.changeTexture(1);
        }
      }

      for (String name : chapterButtonMap.keys()) {
        chapterButtonMap.get(name).setChecked(name.equals(chapterName));
      }
      background.setDrawable(backgroundMap.get(chapterName));

      // Camera movement
      Camera camera = levelStage.getCamera();
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
    MusicController.getInstance().play("adagio");
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
        style.imageUp = Shared.getDrawable(texture);
        styles.add(style);
      }
      button = new ImageButton(styles.get(index));
      button.getColor().a = 0.7f;
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
