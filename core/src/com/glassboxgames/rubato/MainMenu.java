package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

/**
 * Mode controller for the main menu.
 */
public class MainMenu implements Screen {
  /** Exit code for play screen */
  public static final int EXIT_PLAY = 0;
  /** Exit code for editor screen */
  public static final int EXIT_EDITOR = 1;
  /** Exit code for settings screen */
  public static final int EXIT_SETTINGS = 2;
  /** Exit code to quit */
  public static final int EXIT_QUIT = 3;

  /** Whether this mode is active */
  private boolean active;
  /** Stage for the menu */
  private Stage stage;
  /** Listener to call when exiting */
  private ScreenListener listener;
  /** Array of menu buttons */
  private Array<ImageTextButton> buttons;
  /** Current menu index */
  private int index;
  /** Whether we are exiting this mode */
  private boolean exiting;

  /**
   * Instantiates the main menu controller.
   * @param listener listener for exit
   */
  public MainMenu(ScreenListener listener) {
    this.listener = listener;
    stage = new Stage();
    buttons = new Array<ImageTextButton>();
    index = -1;
  }

  /**
   * Adds a menu option at the given index.
   */
  private void addMenuOption(final int i, String key) {
    final ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle();
    style.imageUp = Shared.getDrawable(key + "_button_deselected");
    style.imageOver = Shared.getDrawable(key + "_button_selected");
    style.font = Shared.getFont("main_menu.deselected.ttf");
    style.fontColor = Color.WHITE;
    style.overFontColor = Shared.TEAL;
    final ImageTextButton button = new ImageTextButton(key.toUpperCase(), style);
    button.clearChildren();
    button.add(button.getImage());
    button.add(button.getLabel());
    button.row().align(Align.center + Align.left);
    button.getImageCell().padRight(30);
    button.getLabel().setFontScale(5f / 6);
    button.padBottom(20);

    final SoundController soundController = SoundController.getInstance();

    button.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        chooseOption(i);
        String checkpointSound = Shared.SOUND_PATHS.get("checkpoint");
        soundController.play(checkpointSound, checkpointSound, false);
      }

      public void enter(InputEvent e, float x, float y, int pointer, Actor fromActor) {
        if (!exiting) {
          button.getLabel().setFontScale(1);
        }
      }
      
      public void exit(InputEvent e, float x, float y, int pointer, Actor to) {
        if (!exiting) {
          button.getLabel().setFontScale(5f / 6);
        }
      }
    });
    if (i >= buttons.size) {
      buttons.setSize(i + 1);
    }
    buttons.set(i, button);
  }
  
  /**
   * Initializes the UI for the main menu.
   */
  public void initUI() {
    Image background = new Image(Shared.getTexture("menu_background"));
    background.setWidth(Gdx.graphics.getWidth());
    background.setHeight(Gdx.graphics.getHeight());
    stage.addActor(background);

    Table table = new Table();
    stage.addActor(table);
    table.setFillParent(true);
    table.left().bottom().padLeft(60).padBottom(50);

    addMenuOption(EXIT_PLAY, "play");
    addMenuOption(EXIT_EDITOR, "editor");
    addMenuOption(EXIT_SETTINGS, "settings");
    addMenuOption(EXIT_QUIT, "quit");

    table.add(new Image(Shared.getTexture("logo"))).padBottom(40).left();

    for (ImageTextButton button : buttons) {
      table.row();
      table.add(button).left();
    }
  }

  /**
   * Chooses the given menu option.
   */
  private void chooseOption(int index) {
    exiting = true;
    listener.exitScreen(this, index);
  }

  @Override
  public void render(float delta) {
    if (active) {
      SoundController.getInstance().update();
      InputController input = InputController.getInstance();
      input.readInput();
      
      if (input.pressedExit()) {
        exiting = true;
        listener.exitScreen(this, EXIT_QUIT);
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
    for (ImageTextButton button : buttons) {
      button.getLabel().setFontScale(5f / 6);
    }
    MusicController.getInstance().play("adagio");
  }

  @Override
  public void hide() {
    exiting = false;
    active = false;
    Gdx.input.setInputProcessor(null);
    index = -1;
  }

  @Override
  public void dispose() {
    stage.dispose();
  }
}
