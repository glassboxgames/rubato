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
  public static final String HIGHLIGHT_FILE = "User Interface/Highlight/highlight.png";
  public static final String NO_HIGHLIGHT_FILE = "User Interface/Highlight/no_highlight.png";
  public static final String ADAGIO_ICON_FILE = "User Interface/Main Menu/adagio_head_51x61.png";
  public static final String SEMIBOLD_FONT_FILE = "Fonts/Rajdhani-SemiBold.ttf";
  public static final String REGULAR_FONT_FILE = "Fonts/Rajdhani-Regular.ttf";

  /** Font keys */
  public static final String TITLE_FONT = "title_font.ttf";
  public static final String HIGHLIGHT_FONT = "highlight_font.ttf";
  public static final String BUTTON_FONT = "button_font.ttf";

  /** Loaded assets */
  protected Texture highlightTexture, noHighlightTexture, adagioIconTexture;
  protected BitmapFont titleFont, buttonFont, highlightFont;

  /** Constants */
  protected static final int BUTTON_HEIGHT = 72;

  /** Array of loaded assets */
  protected Array<String> assets;
  /** Button styles */
  TextButton.TextButtonStyle buttonStyle, highlightStyle;
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
    Gdx.input.setInputProcessor(stage);
    table = new Table();
    stage.addActor(table);
    buttons = new Array<TextButton>();
  }

  /**
   * Preloads the assets for the main menu with the given manager.
   */
  public void preloadContent(AssetManager manager) {
    FreetypeFontLoader.FreeTypeFontLoaderParameter titleParams =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    titleParams.fontFileName = SEMIBOLD_FONT_FILE;
    titleParams.fontParameters.size = 96;
    manager.load(TITLE_FONT, BitmapFont.class, titleParams);
    assets.add(TITLE_FONT);

    FreetypeFontLoader.FreeTypeFontLoaderParameter highlightParams =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    highlightParams.fontFileName = SEMIBOLD_FONT_FILE;
    highlightParams.fontParameters.size = 48;
    manager.load(HIGHLIGHT_FONT, BitmapFont.class, highlightParams);
    assets.add(HIGHLIGHT_FONT);

    FreetypeFontLoader.FreeTypeFontLoaderParameter buttonParams =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    buttonParams.fontFileName = REGULAR_FONT_FILE;
    buttonParams.fontParameters.size = 48;
    manager.load(BUTTON_FONT, BitmapFont.class, buttonParams);
    assets.add(BUTTON_FONT);

    manager.load(HIGHLIGHT_FILE, Texture.class);
    assets.add(HIGHLIGHT_FILE);
    manager.load(NO_HIGHLIGHT_FILE, Texture.class);
    assets.add(NO_HIGHLIGHT_FILE);
    manager.load(ADAGIO_ICON_FILE, Texture.class);
    assets.add(ADAGIO_ICON_FILE);
  }

  /**
   * Adds a menu option at the given index.
   */
  private void addMenuOption(int index, String text) {
    TextButton button = new TextButton(text, buttonStyle);
    button.padRight(90);
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
    buttonFont = manager.get(BUTTON_FONT, BitmapFont.class);

    highlightTexture = manager.get(HIGHLIGHT_FILE, Texture.class);
    noHighlightTexture = manager.get(NO_HIGHLIGHT_FILE, Texture.class);
    adagioIconTexture = manager.get(ADAGIO_ICON_FILE, Texture.class);

    table.setFillParent(true);
    table.right().bottom().padBottom(90);

    highlightStyle = new TextButton.TextButtonStyle();
    highlightStyle.up = new TextureRegionDrawable(highlightTexture);
    highlightStyle.font = highlightFont;
    highlightStyle.fontColor = Color.WHITE;
    buttonStyle = new TextButton.TextButtonStyle();
    buttonStyle.up = new TextureRegionDrawable(noHighlightTexture);
    buttonStyle.font = buttonFont;
    buttonStyle.fontColor = Color.WHITE;

    addMenuOption(EXIT_PLAY, "p l a y");
    addMenuOption(EXIT_EDITOR, "e d i t o r");
    HorizontalGroup title = new HorizontalGroup();
    title.addActor(new Label("r u b a t ",
                             new Label.LabelStyle(titleFont, Color.WHITE)));
    title.addActor(new Image(adagioIconTexture));
    title.padRight(90);
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
      if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
        listener.exitScreen(this, index);
        return;
      }

      int last = index;
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        index = (index - 1 + buttons.size) % buttons.size;
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        index = (index + 1) % buttons.size;
      }
      buttons.get(last).setStyle(buttonStyle);
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
