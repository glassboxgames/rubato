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
public class SelectMode implements Screen {
  /** Exit code for returning to menu */
  public static final int EXIT_MENU = 0;
  /** Exit code for selecting level */
  public static final int EXIT_PLAY = 1;

  /** Savegame file */
  private static final String SAVE_FILE = "Data/save.json";
  /** Level number font key */
  private static final String LEVEL_NUMBER_FONT = "level_select_level_number_font.ttf";
  /** Level number font size */
  private static final int LEVEL_NUMBER_FONT_SIZE = 36;
  /** Number of levels to show at once */
  private static final int LEVELS_SHOWN = 5;

  /** Array tracking loaded assets */
  private Array<String> assets = new Array<String>();

  /** Listener for exit events */
  private ScreenListener listener;
  /** Level number font */
  private BitmapFont levelNumberFont;
  /** Whether this mode is active */
  private boolean active;
  /** Stage for the UI */
  private Stage stage;
  /** Group for level chooser */
  private HorizontalGroup levelChooser;
  /** Button styles for level chooser */
  private ImageTextButton.ImageTextButtonStyle lockedStyle, unlockedStyle;

  /** Current chapter */
  private int chapter;
  /** Number of chapters unlocked */
  private int chaptersUnlocked;
  /** Current level index */
  private int level;
  /** Number of levels unlocked */
  private int levelsUnlocked;
  /** Current page of levels */
  private int page;
  /** Chapter buttons */
  private Array<ImageButton> chapterButtons;
  /** Chapter backgrounds */
  private Array<Image> backgrounds;
  /** Level button arrays, ordered by chapter */
  private Array<Array<ImageTextButton>> chapterLevels;
  /** Current level label */
  private Label currLevelLabel;

  /**
   * Instantiates the level selector mode controller.
   * @param listener the screen exit listener
   */
  public SelectMode(ScreenListener listener) {
    this.listener = listener;
    stage = new Stage();
    chapterLevels = new Array<Array<ImageTextButton>>();
    chapterButtons = new Array<ImageButton>();
    backgrounds = new Array<Image>();
    page = -1;
  }

  /**
   * Preloads the assets for the level editor with the given manager.
   */
  public void preloadContent(AssetManager manager) {
    manager.load(LEVEL_NUMBER_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.BOLD_FONT_FILE, LEVEL_NUMBER_FONT_SIZE));
    assets.add(LEVEL_NUMBER_FONT);
  }

  // private void createLabels() {
  //   Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
  //   labelStyle.font.getData().setScale(1.25f);
  //   currLevelLabel = new Label("Pillar " + (checkpointIndex + 1), labelStyle);
  //   currLevelLabel.setPosition(Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 100);
  //   currLevelLabel.setWidth(100);
  //   currLevelLabel.setHeight(Gdx.graphics.getHeight()/12f);
  //   stage.addActor(currLevelLabel);

  //   Label timeLabel = new Label("Time: 00:02:00", labelStyle);
  //   timeLabel.setPosition(Gdx.graphics.getWidth() - 120, Gdx.graphics.getHeight() - 130);
  //   timeLabel.setWidth(100);
  //   timeLabel.setHeight(Gdx.graphics.getHeight()/12f);
  //   stage.addActor(timeLabel);

  //   ImageButton playButton =
  //     new ImageButton(new TextureRegionDrawable(textureMap.get("play_default")),
  //                     null,
  //                     new TextureRegionDrawable(textureMap.get("play_highlight")));
  //   playButton.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 180);
  //   playButton.setWidth(50);
  //   playButton.setHeight(50);
  //   playButton.getImage().setScaling(Scaling.fit);
  //   playButton.addListener(new ClickListener(Input.Buttons.LEFT) {
  //     public void clicked(InputEvent event, float x, float y) {
  //       listener.exitScreen(this, EXIT_PLAY);
  //     }
  //   });
  //   stage.addActor(playButton);
  // }

  // private void updateCurrLevel() {
  //   currLevelLabel.setText("Pillar " + (checkpointIndex + 1));
  // }

  // /**
  //  * Creates the UI buttons.
  //  * Called when level selector is opened and when the chapter is changed.
  //  */
  // public void createUI(int biome) {
  //   Image bg = new Image(textureMap.get(biome));
  //   // switch (biome) {
  //   // case Shared.BIOME_FOREST:
  //   //   bg = new Image(textureMap.get("forest"));
  //   //   bg.setWidth(3000f/2);
  //   //   bg.setHeight(3000f/2);
  //   // } else if (biome == 1) {
  //   //   bg = new Image(textureMap.get("plains"));
  //   //   bg.setWidth(3000f/2);
  //   //   bg.setHeight(3000f/2);
  //   // } else if (biome == 2) {
  //   //   bg = new Image(textureMap.get("desert"));
  //   //   bg.setWidth(3000f/2);
  //   //   bg.setHeight(3000f/3);
  //   // } else {
  //   //   bg = new Image(textureMap.get("mountains"));
  //   // }
  //   stage.addActor(bg);
  //   int buttonSize = 75;
  //   int buttonSpacing = 150;
  //   for (int i = 0; i < levelTotal; i++) {
  //     createCheckpointButton(i,
  //             300 + i * (buttonSize + buttonSpacing),
  //             Gdx.graphics.getHeight() / 5,
  //             buttonSize, buttonSize * 2f);
  //   }
  //   buttonSpacing = 40;
  //   for (int i = 0; i < 4; i++) {
  //     createBiomeButton(i, 40 + (i % 2 == 0 ? -1 : 1) * (15),
  //             Gdx.graphics.getHeight() - 50 -(buttonSpacing * (i+1)), 60, 60);
  //   }
  //   createLabels();
  //   biomes[biome].setChecked(true);
  // }

  /**
   * Loads the assets for the level selector with the given manager.
   */
  public void loadContent(AssetManager manager) {
    levelNumberFont = manager.get(LEVEL_NUMBER_FONT, BitmapFont.class);

    for (int i = 0; i < Shared.CHAPTER_NAMES.size; i++) {
      String name = Shared.CHAPTER_NAMES.get(i);
      final ImageButton button =
        new ImageButton(new TextureRegionDrawable(Shared.TEXTURE_MAP.get(name + "_dark")), null,
                        new TextureRegionDrawable(Shared.TEXTURE_MAP.get(name + "_light")));
      // hex magic
      int buttonSize = 60;
      button.setX(40 + (i % 2) * buttonSize * (float)Math.sqrt(3) / 4);
      button.setY(Gdx.graphics.getHeight() - (40 + buttonSize + i * buttonSize * 3 / 4));
      button.setWidth(buttonSize);
      button.setHeight(buttonSize);
      chapterButtons.add(button);
      stage.addActor(button);
    }

    Table table = new Table();
    // ImageButton left = new ImageButton(new TextureRegionDrawable(Shared.TEXTURE_MAP.get("arrow_left")));
    // table.add(left).left().expandX();
    
    levelChooser = new HorizontalGroup();
    lockedStyle = new ImageTextButton.ImageTextButtonStyle();
    lockedStyle.imageUp = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("pillar_off"));
    lockedStyle.font = levelNumberFont;
    lockedStyle.fontColor = Color.WHITE;
    unlockedStyle = new ImageTextButton.ImageTextButtonStyle();
    unlockedStyle.imageUp = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("pillar_on"));
    unlockedStyle.imageChecked = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("pillar_selected"));
    unlockedStyle.font = levelNumberFont;
    unlockedStyle.fontColor = Color.WHITE;
    for (int c = 0; c < Shared.CHAPTER_LEVELS.size; c++) {
      Array<ImageTextButton> levels = new Array<ImageTextButton>();
      for (int l = 0; l < Shared.CHAPTER_LEVELS.get(c).size; l++) {
        ImageTextButton button = new ImageTextButton(Integer.toString(l + 1), lockedStyle);
        button.clearChildren();
        button.add(button.getLabel()).padBottom(40).row();
        button.add(button.getImage());
        levels.add(button);
      }
      chapterLevels.add(levels);
    }

    levelChooser.space(60);
    table.add(levelChooser).center();
    // ImageButton right = new ImageButton(new TextureRegionDrawable(Shared.TEXTURE_MAP.get("arrow_right")));
    // table.add(right).right().expandX();
    
    table.setFillParent(true);
    table.center().bottom().pad(0, 40, 100, 40);
    stage.addActor(table);
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
   * Updates based on the player's saved progress.
   */
  public void refresh() {
    SaveData data = Shared.JSON.fromJson(SaveData.class, Gdx.files.local(SAVE_FILE));
    chaptersUnlocked = data.chaptersUnlocked;
    levelsUnlocked = data.levelsUnlocked;
  }

  /**
   * Returns the selected chapter.
   */
  public int getChapter() {
    return chapter;
  }

  /**
   * Returns the selected level.
   */
  public int getLevel() {
    return level;
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
   * Returns the number of levels unlocked in the given chapter.
   */
  private int getNumUnlockedLevels(int chapter) {
    if (chapter < chaptersUnlocked - 1) {
      return Shared.CHAPTER_LEVELS.get(chapter).size;
    } else if (chapter > chaptersUnlocked - 1) {
      return 0;
    } else {
      return levelsUnlocked;
    }
  }
  
  @Override
  public void render(float delta) {
    if (active) {
      int totalChapters = Shared.CHAPTER_LEVELS.size;
      int unlocked = getNumUnlockedLevels(chapter);
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && chapter < chaptersUnlocked && level < unlocked) {
        listener.exitScreen(this, EXIT_PLAY);
        return;
      }

      if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
        listener.exitScreen(this, EXIT_MENU);
        return;
      }

      int oldChapter = chapter;
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        chapter = (chapter + totalChapters - 1) % totalChapters;
        unlocked = getNumUnlockedLevels(chapter);
        level = 0;
        page = -1;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        chapter = (chapter + 1) % totalChapters;
        unlocked = getNumUnlockedLevels(chapter);
        level = 0;
        page = -1;
      }
      
      Array<ImageTextButton> levelButtons = chapterLevels.get(chapter);
      int totalLevels = levelButtons.size;

      if (unlocked > 0) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
          level = (level + unlocked - 1) % unlocked;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
          level = (level + 1) % unlocked;
        }
      } 

      for (ImageButton button : chapterButtons) {
        button.setChecked(button == chapterButtons.get(chapter));
      }

      int newPage = level / LEVELS_SHOWN;
      if (newPage != page) {
        page = newPage;
        levelChooser.clear();
        for (int i = 0; i < LEVELS_SHOWN; i++) {
          int index = newPage * LEVELS_SHOWN + i;
          if (index < levelButtons.size) {
            levelChooser.addActor(levelButtons.get(index));
          }
        }
      }

      for (int i = 0; i < totalLevels; i++) {
        Button button = levelButtons.get(i);
        button.setChecked(i == level);
        button.setStyle(i < unlocked ? unlockedStyle : lockedStyle);
      }

      Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
      stage.act(delta);
      stage.draw();
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
    Gdx.input.setInputProcessor(stage);
    refresh();
  }

  @Override
  public void hide() {
    active = false;
    Gdx.input.setInputProcessor(null);
  }

  @Override
  public void dispose() {
    stage.dispose();
  }
}
