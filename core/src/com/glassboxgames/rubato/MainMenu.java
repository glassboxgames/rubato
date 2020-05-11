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

  /** Button styles */
  TextButton.TextButtonStyle selectedStyle, deselectedStyle;
  /** Whether this mode is active */
  protected boolean active;
  /** Stage for the menu */
  protected Stage stage;
  /** Listener to call when exiting */
  protected ScreenListener listener;
  /** Array of menu buttons */
  protected Array<TextButton> buttons;
  /** Current menu index */
  protected int index;

  /**
   * Instantiates the main menu controller.
   * @param listener listener for exit
   */
  public MainMenu(ScreenListener listener) {
    this.listener = listener;
    stage = new Stage();
    buttons = new Array<TextButton>();
    index = -1;
  }

  /**
   * Adds a menu option at the given index.
   */
  private void addMenuOption(final int i, String text) {
    final TextButton button = new TextButton(text, deselectedStyle);
    button.padRight(90);
    button.getLabel().setAlignment(Align.right);
    button.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        chooseOption();
      }

      public void enter(InputEvent e, float x, float y, int pointer, Actor from) {
        index = i;
      }

      public void exit(InputEvent e, float x, float y, int pointer, Actor from) {
        index = -1;
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
    Table table = new Table();
    stage.addActor(table);
    table.setFillParent(true);
    table.right().bottom().padBottom(90);

    selectedStyle = new TextButton.TextButtonStyle();
    selectedStyle.up = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("highlight"));
    selectedStyle.font = Shared.FONT_MAP.get("main_menu.selected.ttf");
    selectedStyle.fontColor = Color.WHITE;
    deselectedStyle = new TextButton.TextButtonStyle();
    deselectedStyle.up = new TextureRegionDrawable(Shared.TEXTURE_MAP.get("no_highlight"));
    deselectedStyle.font = Shared.FONT_MAP.get("main_menu.deselected.ttf");
    deselectedStyle.fontColor = Color.WHITE;

    addMenuOption(EXIT_PLAY, "play");
    addMenuOption(EXIT_EDITOR, "editor");
    addMenuOption(EXIT_SETTINGS, "settings");
    addMenuOption(EXIT_QUIT, "quit");

    HorizontalGroup title = new HorizontalGroup();
    title.addActor(new Label("rubat", new Label.LabelStyle(Shared.FONT_MAP.get("main_menu.title.ttf"),
                                                           Color.WHITE)));
    title.addActor(new Image(Shared.TEXTURE_MAP.get("adagio_head_icon")));
    title.space(8).padRight(90).right();
    table.add(title).right();

    for (TextButton button : buttons) {
      table.row();
      table.add(button).right();
    }
  }

  /**
   * Chooses the current menu option.
   */
  private void chooseOption() {
    listener.exitScreen(this, index);
  }

  @Override
  public void render(float delta) {
    if (active) {
      InputController input = InputController.getInstance();
      input.readInput();
      
      if (input.pressedExit()) {
        listener.exitScreen(this, EXIT_QUIT);
      }

      for (int i = 0; i < buttons.size; i++) {
        buttons.get(i).setStyle(index == i ? selectedStyle : deselectedStyle);
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
