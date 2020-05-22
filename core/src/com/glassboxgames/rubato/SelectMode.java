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
  private ImageTextButton.ImageTextButtonStyle lockedLevelStyle, unlockedLevelStyle;
  /** Button style arrays for chapter chooser */
  private Array<ImageButton.ImageButtonStyle> lockedChapterStyles, unlockedChapterStyles;

  /** Current chapter */
  private int chapter;
  /** Current level index */
  private int level;
  /** Current page of levels */
  private int page;
  /** Chapter buttons */
  private Array<ImageButton> chapterButtons;
  /** Chapter backgrounds */
  private Array<Drawable> backgroundDrawables;
  /** Chapter background image */
  private Image background;
  /** Level button arrays, ordered by chapter */
  private Array<Array<ImageTextButton>> levelButtonsByChapter;
  /** Current array of level buttons */
  private Array<ImageTextButton> levelButtons;
  /** Chapter labels */
  private Label chapterName, chapterTime, chapterMillis;
  /** Arrow buttons */
  private ImageButton leftArrow, rightArrow;

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
    backgroundDrawables = new Array<Drawable>();
    lockedChapterStyles = new Array<ImageButton.ImageButtonStyle>();
    unlockedChapterStyles = new Array<ImageButton.ImageButtonStyle>();
  }

  /**
   * Initializes the UI.
   */
  public void initUI() {
    final SaveController save = SaveController.getInstance();

    background = new Image();
    background.setColor(1, 1, 1, 0.75f);
    background.setAlign(Align.bottomLeft);
    background.setScaling(Scaling.fillY);
    background.setWidth(Gdx.graphics.getWidth());
    background.setHeight(Gdx.graphics.getHeight());
    stage.addActor(background);

    ImageButton home = new ImageButton(Shared.getDrawable("home_icon"));
    home.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        exitToMenu();
      }
    });
    home.setX(20);
    home.setY(Gdx.graphics.getHeight() - home.getHeight() - 20);
    stage.addActor(home);
  
    for (int i = 0; i < Shared.CHAPTER_NAMES.size; i++) {
      String name = Shared.CHAPTER_NAMES.get(i);
      backgroundDrawables.add(Shared.getDrawable(name));

      ImageButton.ImageButtonStyle unlockedStyle = new ImageButton.ImageButtonStyle();
      unlockedStyle.imageUp = Shared.getDrawable(name + "_unlocked");
      unlockedStyle.imageOver = Shared.getDrawable(name + "_hovered");
      unlockedStyle.imageChecked = Shared.getDrawable(name + "_selected");
      unlockedChapterStyles.add(unlockedStyle);
      ImageButton.ImageButtonStyle lockedStyle = new ImageButton.ImageButtonStyle();
      lockedStyle.imageUp = Shared.getDrawable(name + "_locked");
      lockedChapterStyles.add(lockedStyle);
      
      final ImageButton button =
        new ImageButton(save.getLevelsUnlocked(name) > 0 ? unlockedStyle : lockedStyle);
      // hex magic
      int buttonSize = 60;
      button.setX(40 + (i % 2) * buttonSize * (float)Math.sqrt(3) / 4);
      button.setY(Gdx.graphics.getHeight() - (70 + buttonSize + i * buttonSize * 3 / 4));
      button.setWidth(buttonSize);
      button.setHeight(buttonSize);
      final int newChapter = i;
      button.addListener(new ClickListener(Input.Buttons.LEFT) {
        public void clicked(InputEvent e, float x, float y) {
          chapter = newChapter;
          page = 0;
        }
      });
      chapterButtons.add(button);
      stage.addActor(button);
    }

    Table table = new Table();
    leftArrow = new ImageButton(Shared.getDrawable("arrow_left"));
    leftArrow.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        page--;
        updateChooser();
      }
    });
    table.add(leftArrow).left().expandX();
    
    levelChooser = new HorizontalGroup();
    lockedLevelStyle = new ImageTextButton.ImageTextButtonStyle();
    lockedLevelStyle.imageUp = Shared.getDrawable("pillar_off");
    lockedLevelStyle.font = Shared.getFont("select.level_number.ttf");
    lockedLevelStyle.fontColor = Color.WHITE;
    unlockedLevelStyle = new ImageTextButton.ImageTextButtonStyle();
    unlockedLevelStyle.imageUp = Shared.getDrawable("pillar_on");
    unlockedLevelStyle.imageOver = Shared.getDrawable("pillar_selected");
    unlockedLevelStyle.font = Shared.getFont("select.level_number.ttf");
    unlockedLevelStyle.fontColor = Color.WHITE;

    final SoundController soundController = SoundController.getInstance();

    for (int c = 0; c < Shared.CHAPTER_LEVELS.size; c++) {
      Array<ImageTextButton> buttons = new Array<ImageTextButton>();
      for (int l = 0; l < Shared.CHAPTER_LEVELS.get(c).size - 1; l++) {
        if (c == Shared.CHAPTER_LEVELS.size - 1 && l == Shared.CHAPTER_LEVELS.get(c).size - 2) {
          continue;
        }
        final ImageTextButton button = new ImageTextButton(Integer.toString(l + 1), lockedLevelStyle);
        button.clearChildren();
        button.add(button.getLabel()).padBottom(40).row();
        button.add(button.getImage());
        final int newLevel = l;
        button.addListener(new ClickListener(Input.Buttons.LEFT) {
          public void clicked(InputEvent e, float x, float y) {
            if (newLevel < save.getLevelsUnlocked(chapter)) {
              String checkpointSound = Shared.SOUND_PATHS.get("checkpoint");
              level = newLevel;
              soundController.play(checkpointSound, checkpointSound, false);
              play();
            }
          }
        });
        buttons.add(button);
      }
      levelButtonsByChapter.add(buttons);
    }

    levelChooser.space(60);
    table.add(levelChooser).center();

    rightArrow = new ImageButton(Shared.getDrawable("arrow_right"));
    rightArrow.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        page++;
        updateChooser();
      }
    });
    table.add(rightArrow).right().expandX();
    
    table.setFillParent(true);
    table.center().bottom().pad(0, 40, 100, 40);
    stage.addActor(table);

    table = new Table();
    table.setFillParent(true);
    table.top().right().pad(30);
    Table group = new Table();
    chapterName = new Label("", new Label.LabelStyle(Shared.getFont("select.chapter_name.ttf"), Color.WHITE));
    table.add(chapterName).right().row();
    chapterTime = new Label("", new Label.LabelStyle(Shared.getFont("select.chapter_time.ttf"), Color.WHITE));
    group.add(chapterTime).bottom().padRight(12);
    chapterMillis = new Label("",
                              new Label.LabelStyle(Shared.getFont("select.chapter_millis.ttf"), Color.WHITE));
    group.add(chapterMillis).bottom().padBottom(4);
    table.add(group).right().row();
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
    leftArrow.setVisible(page > 0);
    rightArrow.setVisible(page < getNumPages() - 1);
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
   * Exits to the main menu.
   */
  private void exitToMenu() {
    listener.exitScreen(this, EXIT_MENU);
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
      SoundController.getInstance().update();
      InputController input = InputController.getInstance();
      SaveController save = SaveController.getInstance();
      input.readInput();

      if (input.pressedExit()) {
        exitToMenu();
        return;
      }

      background.setDrawable(backgroundDrawables.get(chapter));

      String curName = Shared.CHAPTER_NAMES.get(chapter);
      chapterName.setText(curName);
      String time = Shared.formatTime(SaveController.getInstance().getTimeSpent(curName));
      int index = time.indexOf(".");
      chapterTime.setText(time.substring(0, index));
      chapterMillis.setText(time.substring(index));

      for (int i = 0; i < chapterButtons.size; i++) {
        String name = Shared.CHAPTER_NAMES.get(i);
        ImageButton button = chapterButtons.get(i);
        button.setStyle(save.getLevelsUnlocked(name) > 0
                        ? unlockedChapterStyles.get(i) : lockedChapterStyles.get(i));
        button.setChecked(chapter == i);
      }

      levelButtons = levelButtonsByChapter.get(chapter);
      updateChooser();
      int unlocked = save.getLevelsUnlocked(chapter);

      for (int i = 0; i < levelButtons.size; i++) {
        levelButtons.get(i).setStyle(i < unlocked ? unlockedLevelStyle : lockedLevelStyle);
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
    MusicController.getInstance().play("adagio");
  }

  @Override
  public void hide() {
    active = false;
    Gdx.input.setInputProcessor(null);
    page = 0;
  }

  @Override
  public void dispose() {
    stage.dispose();
  }
}
