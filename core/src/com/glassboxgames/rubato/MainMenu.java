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

  /** Asset paths */
  protected static final String HIGHLIGHT_FILE =
    "User Interface/Highlight/highlight.png";
  protected static final String DEFAULT_FILE =
    "User Interface/Highlight/no_highlight.png";
  protected static final String ADAGIO_ICON_FILE =
    "User Interface/Main Menu/adagio_head_51x61.png";

  /** Font files */
  protected static final String TITLE_FONT = "main_menu_title_font.ttf";
  protected static final String HIGHLIGHT_FONT = "main_menu_highlight_font.ttf";
  protected static final String OPTION_FONT = "main_menu_option_font.ttf";

  /** UI element sizes */
  protected static final int TITLE_FONT_SIZE = 96;
  protected static final int HIGHLIGHT_FONT_SIZE = 48;
  protected static final int OPTION_FONT_SIZE = 48;
  protected static final int BUTTON_HEIGHT = 72;
  protected static final int PADDING = 90;

  /** Loaded assets */
  protected Texture highlightBackground, optionBackground, adagioIcon;
  protected BitmapFont titleFont, highlightFont, optionFont;


  /** Array of loaded assets */
  protected Array<String> assets;
  /** Button styles */
  TextButton.TextButtonStyle highlightStyle, optionStyle;
  /** Whether this mode is active */
  protected boolean active;
  /** Stage for the menu */
  protected Stage stage;
  /** Table for the menu */
  protected Table table;
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
    assets = new Array<String>();
    stage = new Stage();
    table = new Table();
    stage.addActor(table);
    Gdx.input.setInputProcessor(stage);
    buttons = new Array<TextButton>();
  }

  /**
   * Preloads the assets for the main menu with the given manager.
   */
  public void preloadContent(AssetManager manager) {
    manager.load(TITLE_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.SEMIBOLD_FONT_FILE,
                                               TITLE_FONT_SIZE));
    assets.add(TITLE_FONT);

    manager.load(HIGHLIGHT_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.SEMIBOLD_FONT_FILE,
                                               HIGHLIGHT_FONT_SIZE));
    assets.add(HIGHLIGHT_FONT);

    manager.load(OPTION_FONT, BitmapFont.class,
                 Shared.createFontLoaderParams(Shared.REGULAR_FONT_FILE,
                                               OPTION_FONT_SIZE));
    assets.add(OPTION_FONT);

    manager.load(HIGHLIGHT_FILE, Texture.class);
    assets.add(HIGHLIGHT_FILE);
    manager.load(DEFAULT_FILE, Texture.class);
    assets.add(DEFAULT_FILE);
    manager.load(ADAGIO_ICON_FILE, Texture.class);
    assets.add(ADAGIO_ICON_FILE);
  }

  /**
   * Adds a menu option at the given index.
   */
  private void addMenuOption(int index, String text) {
    TextButton button = new TextButton(text, optionStyle);
    button.padRight(PADDING);
    button.getLabel().setAlignment(Align.right);
    button.setHeight(BUTTON_HEIGHT);
    if (index >= buttons.size) {
      buttons.setSize(index + 1);
    }
    buttons.set(index, button);
  }
  
  /**
   * Loads the assets for the main menu with the given manager.
   */
  public void loadContent(AssetManager manager) {
    titleFont = manager.get(TITLE_FONT, BitmapFont.class);
    highlightFont = manager.get(HIGHLIGHT_FONT, BitmapFont.class);
    optionFont = manager.get(OPTION_FONT, BitmapFont.class);

    highlightBackground = manager.get(HIGHLIGHT_FILE, Texture.class);
    optionBackground = manager.get(DEFAULT_FILE, Texture.class);
    adagioIcon = manager.get(ADAGIO_ICON_FILE, Texture.class);

    table.setFillParent(true);
    table.right().bottom().padBottom(PADDING);

    highlightStyle = new TextButton.TextButtonStyle();
    highlightStyle.up = new TextureRegionDrawable(highlightBackground);
    highlightStyle.font = highlightFont;
    highlightStyle.fontColor = Color.WHITE;
    optionStyle = new TextButton.TextButtonStyle();
    optionStyle.up = new TextureRegionDrawable(optionBackground);
    optionStyle.font = optionFont;
    optionStyle.fontColor = Color.WHITE;

    addMenuOption(EXIT_PLAY, "play");
    addMenuOption(EXIT_EDITOR, "editor");

    HorizontalGroup title = new HorizontalGroup();
    title.addActor(new Label("rubat", new Label.LabelStyle(titleFont, Color.WHITE)));
    title.addActor(new Image(adagioIcon));
    title.space(Shared.DEFAULT_FONT_SPACING).padRight(PADDING).right();
    table.add(title).right();

    for (TextButton button : buttons) {
      table.row();
      table.add(button).right();
    }
  }

  /**
   * Unloads the assets for the main menu with the given manager.
   */
  public void unloadContent(AssetManager manager) {
    for (String s : assets) {
      if (manager.isLoaded(s)) {
        manager.unload(s);
      }
    }
  }

  @Override
  public void render(float delta) {
    if (active) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        Gdx.app.exit();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        listener.exitScreen(this, index);
        return;
      }

      int last = index;
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        index = (index - 1 + buttons.size) % buttons.size;
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        index = (index + 1) % buttons.size;
      }
      buttons.get(last).setStyle(optionStyle);
      buttons.get(index).setStyle(highlightStyle);

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
  }

  @Override
  public void hide() {
    active = false;
  }

  @Override
  public void dispose() {
    stage.dispose();
  }
}
