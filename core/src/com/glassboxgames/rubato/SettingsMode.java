package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.util.*;

import java.util.HashMap;
import java.util.Map;
import static java.util.Map.Entry;

public class SettingsMode implements Screen {
  /** Exit code for play screen */
  public static final int EXIT_MENU = 0;

  /** Asset paths */
  public static final String HIGHLIGHT_FILE = "User Interface/Highlight/highlight.png";
  public static final String NO_HIGHLIGHT_FILE = "User Interface/Highlight/no_highlight.png";
  public static final String ADAGIO_ICON_FILE = "User Interface/Main Menu/adagio_head_51x61.png";
  public static final String SEMIBOLD_FONT_FILE = "Fonts/Rajdhani-SemiBold.ttf";
  public static final String REGULAR_FONT_FILE = "Fonts/Rajdhani-Regular.ttf";
  public static final String DEFAULT_BACKGROUND = "Backgrounds/Mountains/mountains.png";
  protected static final String EDITOR_FILE = "Data/editor.json";
  protected static final String UI_SKIN = "Data/uiskin.json";
  protected static final String SLIDER_BG = "User Interface/Buttons/slider_rect.png";
  protected static final String SLIDER_KNOB = "User Interface/Buttons/slider_point.png";

  /** Font keys */
  public static final String TITLE_FONT = "title_font.ttf";
  public static final String HIGHLIGHT_FONT = "highlight_font.ttf";
  public static final String BUTTON_FONT = "button_font.ttf";

  /** Loaded assets */
  protected Texture highlightTexture, noHighlightTexture, adagioIconTexture, sliderBGTexture, sliderKnobTexture;
  protected BitmapFont titleFont, buttonFont, highlightFont;

  /** Constants */
  protected static final int BUTTON_HEIGHT = 40; // 72

  /** Button map for the UI */
  protected ObjectMap<String, Button> uiMap;
  /** Button map for the level */
  protected ObjectMap<String, Array<Button>> levelMap;
  /** Array tracking loaded assets */
  protected Array<String> assets = new Array<String>();
  /** Button styles */
  TextButton.TextButtonStyle buttonStyle, highlightStyle;
  /** Whether this mode is active */
  protected boolean active;
  /** Stage for the level */
  protected Stage levelStage;
  /** Stage for the editor UI */
  protected Stage stage;
  /** Table for the menu */
  protected Table table;
  /** Listener to call when exiting */
  protected ScreenListener listener;
  /** Array of menu buttons */
  protected Array<TextButton> buttons;
  /** Current menu index */
  protected int index;
  /** Input processor for the editor */
  protected InputProcessor inputProcessor;
  /** Controls list as map */
  protected HashMap<String, Integer> controls;
  /** Controls list */
  protected Array<Label> controlList;
  /** Remapping */
  protected boolean mapping;


  /**
   * Instantiates the main menu controller.
   * @param listener listener for exit
   */
  public SettingsMode(ScreenListener listener) {
    this.listener = listener;
    assets = new Array<String>();
    stage = new Stage();
    levelStage = new Stage();
    inputProcessor = new InputMultiplexer(stage, levelStage);
    uiMap = new ObjectMap<String, Button>();
    levelMap = new ObjectMap<String, Array<Button>>();
    controls = new HashMap<String, Integer>() {{
        put("level reset", Input.Keys.R);
        put("move left", Input.Keys.LEFT);
        put("move right", Input.Keys.RIGHT);
        put("climb up", Input.Keys.UP);
        put("climb down", Input.Keys.DOWN);
        put("jump", Input.Keys.SPACE);
        put("attack", Input.Keys.F);
        put("dash", Input.Keys.D);
      }};
    buttons = new Array<TextButton>();
    controlList = new Array<Label>();
    index = 0;
    mapping = false;
  }

  /**
   * Preloads the assets for the main menu with the given manager.
   */
  public void preloadContent(AssetManager manager) {
    FreetypeFontLoader.FreeTypeFontLoaderParameter titleParams =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    titleParams.fontFileName = SEMIBOLD_FONT_FILE;
    titleParams.fontParameters.size = 40;
    manager.load(TITLE_FONT, BitmapFont.class, titleParams);
    assets.add(TITLE_FONT);

    FreetypeFontLoader.FreeTypeFontLoaderParameter highlightParams =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    highlightParams.fontFileName = SEMIBOLD_FONT_FILE;
    highlightParams.fontParameters.size = 20;
    manager.load(HIGHLIGHT_FONT, BitmapFont.class, highlightParams);
    assets.add(HIGHLIGHT_FONT);

    FreetypeFontLoader.FreeTypeFontLoaderParameter buttonParams =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    buttonParams.fontFileName = REGULAR_FONT_FILE;
    buttonParams.fontParameters.size = 20;
    manager.load(BUTTON_FONT, BitmapFont.class, buttonParams);
    assets.add(BUTTON_FONT);

    manager.load(HIGHLIGHT_FILE, Texture.class);
    assets.add(HIGHLIGHT_FILE);
    manager.load(NO_HIGHLIGHT_FILE, Texture.class);
    assets.add(NO_HIGHLIGHT_FILE);
    manager.load(ADAGIO_ICON_FILE, Texture.class);
    assets.add(ADAGIO_ICON_FILE);
    manager.load(DEFAULT_BACKGROUND, Texture.class);
    assets.add(DEFAULT_BACKGROUND);
    manager.load(SLIDER_BG, Texture.class);
    assets.add(SLIDER_BG);
    manager.load(SLIDER_KNOB, Texture.class);
    assets.add(SLIDER_KNOB);
  }

  /**
   * Adds a menu option at the given index.
   */
  private void addMenuOption(int index, String text) {
    TextButton button = new TextButton(text, buttonStyle);
    //        button.padLeft(200);
    button.getLabel().setAlignment(Align.left);
    button.setHeight(BUTTON_HEIGHT);
    if (index >= buttons.size) {
      buttons.setSize(index + 1);
    }
    uiMap.put(text, button);
    buttons.set(index, button);
  }

  private String rebindKey() {
    System.out.println("reach");
    while (mapping) {
      for (int i = 0; i < 256; i++) {
        if (Gdx.input.isKeyPressed(i)) {
          mapping = false;
          return Input.Keys.toString(i);
        }
      }
    }
    return null;
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

    highlightStyle = new TextButton.TextButtonStyle();
    highlightStyle.up = new TextureRegionDrawable(highlightTexture);
    highlightStyle.font = highlightFont;
    highlightStyle.fontColor = Color.WHITE;
    buttonStyle = new TextButton.TextButtonStyle();
    buttonStyle.up = new TextureRegionDrawable(noHighlightTexture);
    buttonStyle.font = buttonFont;
    buttonStyle.fontColor = Color.WHITE;
    buttonStyle.font.getData().setScale(0.75f);

    int i = 0;
    for (String s : controls.keySet()) {
      addMenuOption(i, s);
      i++;
    }

    Container<Table> tableContainer = new Container<Table>();
    float sw = Gdx.graphics.getWidth();
    float sh = Gdx.graphics.getHeight();
    float cw = sw * 0.5f;
    float ch = sh * 0.5f;

    tableContainer.setSize(sw, ch);
    tableContainer.setPosition(60, (sh-ch)/2.0f);
    tableContainer.fillX();

    table = new Table();

    HorizontalGroup sound = new HorizontalGroup();
    sound.addActor(new Label("S O U N D",
                             new Label.LabelStyle(highlightFont, Color.WHITE)));
    table.add(sound).left();
    table.row().fillX();
    Label musicLabel = new Label("music", new Label.LabelStyle(buttonFont, Color.WHITE));
    table.add(musicLabel).left();

    sliderBGTexture = manager.get(SLIDER_BG, Texture.class);
    sliderKnobTexture = manager.get(SLIDER_KNOB, Texture.class);
    Slider.SliderStyle style = new Slider.SliderStyle(new TextureRegionDrawable(sliderBGTexture), new TextureRegionDrawable(sliderKnobTexture));
    Slider musicSlider = new Slider(0, 1f, .1f, false, style);
    Container<Slider> container = new Container<Slider>(musicSlider);
    container.setTransform(true);
    container.size(350, 15);
    container.setActor(musicSlider);
    table.add(container).padLeft(400);

    table.row().fillX();
    Label soundFX = new Label("sound effects", new Label.LabelStyle(buttonFont, Color.WHITE));
    table.add(soundFX).left();
    Slider fxSlider = new Slider(0, 100f, .01f, false, style);
    Container<Slider> fxContainer = new Container<Slider>(fxSlider);
    fxContainer.setTransform(true);
    fxContainer.size(350, 15);
    fxContainer.setActor(fxSlider);
    table.add(fxContainer).padLeft(400);

    table.row();
    HorizontalGroup title = new HorizontalGroup();
    title.addActor(new Label("C O N T R O L S",
                             new Label.LabelStyle(highlightFont, Color.WHITE)));
    table.add(title);

    controlList.setSize(buttons.size);
    for (TextButton button : buttons) {
      table.row().fillX();
      table.add(button.getLabel()).left();
      String ctl = button.getText().toString();
      int keycode = controls.get(ctl);
      final Label keyVal = new Label(Input.Keys.toString(keycode),
                                     new Label.LabelStyle(buttonFont, Color.WHITE));
      table.add(keyVal).right().padLeft(700);
      keyVal.addListener(new ClickListener(Input.Buttons.LEFT) {
        public void clicked(InputEvent event, float x, float y) {
          mapping = true;
          String newKey = rebindKey();
          if (newKey != null) keyVal.setText(newKey);
          System.out.println(newKey);
          mapping = false;
        }
      });
      controlList.set(buttons.indexOf(button, true), keyVal);
    }

    Image img = new Image(manager.get(DEFAULT_BACKGROUND, Texture.class));
    //        img.setWidth(3000f/2f);
    //        img.setHeight(3000f/2f);
    levelStage.addActor(img);
    tableContainer.setActor(table);
    table.left().bottom();
    stage.addActor(tableContainer);
  }

  /**
   * Unloads the assets with the given manager.
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
        listener.exitScreen(this, EXIT_MENU);
        stage.clear();
        return;
      }
      // TODO: THIS LOGIC IS BROKEN
      String newKey = "";
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        while (newKey.equals("")) {
          for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyPressed(i)) newKey = Input.Keys.toString(i);
          }
        }
        //                table.getChildren().get(index);
        //                listener.exitScreen(this, index);
        System.out.println("new key is: " + newKey);
        return;
      }

      int last = index;
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        index = (index - 1 + controlList.size) % controlList.size;
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        index = (index + 1) % controlList.size;
      }
      //            buttons.get(last).setStyle(buttonStyle);
      Label.LabelStyle highlightLabelStyle = new Label.LabelStyle(buttonFont, Color.WHITE);
      highlightLabelStyle.background = new TextureRegionDrawable(highlightTexture);
      controlList.get(last).setStyle(new Label.LabelStyle(buttonFont, Color.WHITE));
      controlList.get(index).setStyle(highlightLabelStyle);

      Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
      levelStage.act(delta);
      levelStage.draw();
      Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      ShapeRenderer renderer = new ShapeRenderer();
      renderer.begin(ShapeRenderer.ShapeType.Filled);
      renderer.setColor(new Color(0,.404f,0.31f,0.6f));
      renderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      renderer.end();
      Gdx.gl.glDisable(GL20.GL_BLEND);
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
