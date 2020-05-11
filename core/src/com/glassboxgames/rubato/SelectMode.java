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

  /** Number of levels to show at once */
  private static final int LEVELS_SHOWN = 5;

  /** Listener for exit events */
  private ScreenListener listener;
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
  /** Current level index */
  private int level;
  /** Current page of levels */
  private int page;
  /** Chapter buttons */
  private Array<ImageButton> chapterButtons;
  /** Chapter backgrounds */
  private Array<Image> backgrounds;
  /** Level button arrays, ordered by chapter */
  private Array<Array<ImageTextButton>> levelButtonsByChapter;
  /** Current array of level buttons */
  private Array<ImageTextButton> levelButtons;
  /** Current level label */
  private Label currLevelLabel;

  /**
   * Instantiates the level selector mode controller.
   * @param listener the screen exit listener
   */
  public SelectMode(ScreenListener listener) {
    this.listener = listener;
    stage = new Stage();
    chapterButtons = new Array<ImageButton>();
    levelButtonsByChapter = new Array<Array<ImageTextButton>>();
    levelButtons = new Array<ImageTextButton>();
    backgrounds = new Array<Image>();
  }

  /**
   * Initializes the UI.
   */
  public void initUI() {
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
      final int newChapter = i;
      button.addListener(new ClickListener(Input.Buttons.LEFT) {
        public void clicked(InputEvent e, float x, float y) {
          chapter = newChapter;
        }
      });
      chapterButtons.add(button);
      stage.addActor(button);
    }

    Table table = new Table();
    ImageButton left = new ImageButton(new TextureRegionDrawable(Shared.TEXTURE_MAP.get("arrow_left")));
    left.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        int n = getNumPages();
        page = (page + n - 1) % n;
        updateChooser();
      }
    });
    table.add(left).left().expandX();
    
    levelChooser = new HorizontalGroup();
    lockedStyle = new ImageTextButton.ImageTextButtonStyle();
    lockedStyle.imageUp = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("pillar_off"));
    lockedStyle.font = Shared.FONT_MAP.get("select.level_number.ttf");
    lockedStyle.fontColor = Color.WHITE;
    unlockedStyle = new ImageTextButton.ImageTextButtonStyle();
    unlockedStyle.imageUp = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("pillar_on"));
    unlockedStyle.imageChecked = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("pillar_selected"));
    unlockedStyle.font = Shared.FONT_MAP.get("select.level_number.ttf");
    unlockedStyle.fontColor = Color.WHITE;
    for (int c = 0; c < Shared.CHAPTER_LEVELS.size; c++) {
      Array<ImageTextButton> buttons = new Array<ImageTextButton>();
      for (int l = 0; l < Shared.CHAPTER_LEVELS.get(c).size; l++) {
        final ImageTextButton button = new ImageTextButton(Integer.toString(l + 1), lockedStyle);
        button.clearChildren();
        button.add(button.getLabel()).padBottom(40).row();
        button.add(button.getImage());
        final int newLevel = l;
        button.addListener(new ClickListener(Input.Buttons.LEFT) {
          public void clicked(InputEvent e, float x, float y) {
            if (newLevel < SaveController.getInstance().getLevelsUnlocked(chapter)) {
              level = newLevel;
              play();
            }
          }

          public void enter(InputEvent e, float x, float y, int pointer, Actor from) {
            button.setChecked(true);
          }

          public void exit(InputEvent e, float x, float y, int pointer, Actor to) {
            button.setChecked(false);
          }
        });
        buttons.add(button);
      }
      levelButtonsByChapter.add(buttons);
    }

    levelChooser.space(60);
    table.add(levelChooser).center();

    ImageButton right = new ImageButton(new TextureRegionDrawable(Shared.TEXTURE_MAP.get("arrow_right")));
    right.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        page = (page + 1) % getNumPages();
        updateChooser();
      }
    });
    table.add(right).right().expandX();
    
    table.setFillParent(true);
    table.center().bottom().pad(0, 40, 100, 40);
    stage.addActor(table);
  }

  /**
   * Returns the number of pages of levels to show for the current chapter.
   */
  private int getNumPages() {
    return (int)Math.ceil((float)levelButtons.size / LEVELS_SHOWN);
  }
  
  /**
   * Updates the level chooser.
   */
  private void updateChooser() {
    levelChooser.clear();
    for (int i = 0; i < LEVELS_SHOWN; i++) {
      int l = page * LEVELS_SHOWN + i;
      if (l < levelButtons.size) {
        levelChooser.addActor(levelButtons.get(l));
      }
    }
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
   * Starts playing the selected level.
   */
  private void play() {
    listener.exitScreen(this, EXIT_PLAY);
  }
  
  @Override
  public void render(float delta) {
    if (active) {
      InputController input = InputController.getInstance();
      SaveController save = SaveController.getInstance();
      input.readInput();

      if (input.pressedExit()) {
        listener.exitScreen(this, EXIT_MENU);
      }

      for (int i = 0; i < chapterButtons.size; i++) {
        chapterButtons.get(i).setChecked(chapter == i);
      }

      levelButtons = levelButtonsByChapter.get(chapter);
      updateChooser();
      int unlocked = save.getLevelsUnlocked(chapter);

      for (int i = 0; i < levelButtons.size; i++) {
        levelButtons.get(i).setStyle(i < unlocked ? unlockedStyle : lockedStyle);
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
