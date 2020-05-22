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
 * Mode controller for the settings screen.
 */
public class SettingsMode implements Screen {
  /** Exit code for play screen */
  public static final int EXIT_MENU = 0;

  /** Font keys */
  private static final String HEADER_FONT = "settings_header.ttf";
  private static final String ITEM_FONT = "settings_item.ttf";
  /** Allowed keys */
  private static final ObjectSet<String> ALLOWED_KEYS = new ObjectSet<String>();

  static {
    ALLOWED_KEYS.addAll("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                           "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                           "Left", "Up", "Right", "Down", "Space",
                           "L-Shift", "L-Ctrl", "L-Alt", "R-Shift", "R-Ctrl", "R-Alt");
  }

  /** Fonts */
  private BitmapFont headerFont, itemFont;
  /** Array tracking loaded assets */
  private Array<String> assets = new Array<String>();

  /** Binding button styles */
  private TextButton.TextButtonStyle selectedStyle, deselectedStyle;
  /** Binding description style */
  private Label.LabelStyle labelStyle;

  /** Array of bindings */
  private Array<Binding> bindings;
  /** Current index */
  private int index;
  /** Whether the user is currently rebinding */
  private boolean rebinding;
  
  /** Whether this mode is active */
  protected boolean active;
  /** Stage for the settings UI */
  protected Stage stage;
  /** Listener to call when exiting */
  protected ScreenListener listener;

  /**
   * Instantiates the settings controller.
   * @param listener listener for exit
   */
  public SettingsMode(ScreenListener listener) {
    this.listener = listener;
    stage = new Stage();
    bindings = new Array<Binding>();
  }

  /**
   * Initializes the settings UI.
   */
  public void initUI() {
    final SaveController saveController = SaveController.getInstance();

    Image background = new Image(Shared.getTexture("menu_background"));
    background.setColor(1, 1, 1, 0.55f);
    background.setWidth(Gdx.graphics.getWidth());
    background.setHeight(Gdx.graphics.getHeight());
    stage.addActor(background);

    Label.LabelStyle headerStyle = new Label.LabelStyle(Shared.getFont("settings.header.ttf"), Color.WHITE);
    selectedStyle = new TextButton.TextButtonStyle();
    selectedStyle.font = Shared.getFont("settings.selected.ttf");
    selectedStyle.fontColor = Color.WHITE;
    deselectedStyle = new TextButton.TextButtonStyle();
    deselectedStyle.font = Shared.getFont("settings.deselected.ttf");
    deselectedStyle.fontColor = Color.WHITE;
    deselectedStyle.overFontColor = Shared.TEAL;
    labelStyle = new Label.LabelStyle(Shared.getFont("settings.label.ttf"), Color.WHITE);

    ImageButton home = new ImageButton(Shared.getDrawable("home_icon"));
    home.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent e, float x, float y) {
        exitToMenu();
      }
    });
    home.setX(20);
    home.setY(Gdx.graphics.getHeight() - home.getHeight() - 20);
    stage.addActor(home);

    final Table table = new Table();
    stage.addActor(table);
    table.setFillParent(true);
    table.pad(150, 90, 150, 90);

    table.add(new Label("AUDIO", headerStyle)).padTop(40).left().row();

    Table music = new Table();
    music.add(new Label("music", labelStyle)).left().growX();
    final Slider musicSlider = new Slider(0f, 1f, 0.004f, false,
                                          new Slider.SliderStyle(Shared.getDrawable("slider_track"),
                                                                 Shared.getDrawable("slider_knob")));
    musicSlider.setValue(saveController.getMusicVolume());
    musicSlider.addListener(new ChangeListener() {
      public void changed(ChangeEvent e, Actor actor) {
        saveController.setMusicVolume(musicSlider.getValue());
        MusicController.getInstance().resetVolume();
      }
    });
    music.add(musicSlider).width(300).right();
    table.add(music).growX().row();

    Table sound = new Table();
    sound.add(new Label("sfx", labelStyle)).left().growX();
    final Slider soundSlider = new Slider(0f, 1f, 0.004f, false,
                                          new Slider.SliderStyle(Shared.getDrawable("slider_track"),
                                                                 Shared.getDrawable("slider_knob")));
    soundSlider.setValue(saveController.getSoundVolume());
    soundSlider.addListener(new ChangeListener() {
      public void changed(ChangeEvent e, Actor actor) {
        saveController.setSoundVolume(soundSlider.getValue());
        SoundController.getInstance().setVolume(soundSlider.getValue());
      }
    });
    sound.add(soundSlider).width(300).right();
    table.add(sound).growX().row();
    
    table.add(new Label("CONTROLS", headerStyle)).padTop(40).left().row();
    createBinding("left");
    createBinding("right");
    createBinding("jump");
    createBinding("attack");
    createBinding("reset");

    for (Binding binding : bindings) {
      Table item = new Table();
      item.add(binding.label).left().growX();
      item.add(binding.button).right();
      table.add(item).growX().row();
    }
  }

  /**
   * Creates a binding item.
   */
  private void createBinding(String action) {
    final Binding binding = new Binding();
    binding.action = action;
    binding.label = new Label(action, labelStyle);
    binding.key = SaveController.getInstance().getBoundKey(binding.action);
    binding.button = new TextButton(binding.key, deselectedStyle);
    binding.button.getLabel().setAlignment(Align.right);
    binding.button.addListener(new ClickListener(Input.Buttons.LEFT) {
      public void clicked(InputEvent event, float x, float y) {
        startRebinding(binding.index);
      }
    });
    binding.button.addListener(new InputListener() {
      public boolean keyDown(InputEvent e, int keycode) {
        String key = Input.Keys.toString(keycode);
        if (ALLOWED_KEYS.contains(key)) {
          for (Binding other : bindings) {
            if (other != binding && other.key.equals(key)) {
              other.setKey(binding.key);
              break;
            }
          }
          binding.setKey(key);
          cancelRebinding();
        }
        return true;
      }
    });
    binding.index = bindings.size;
    bindings.add(binding);
  }

  /**
   * Returns to the main menu.
   */
  private void exitToMenu() {
    cancelRebinding();
    listener.exitScreen(this, EXIT_MENU);
  }

  /**
   * Starts a rebinding.
   */
  private void startRebinding(int i) {
    index = i;
    rebinding = true;
    TextButton button = bindings.get(i).button;
    button.setText("_");
    stage.setKeyboardFocus(button);
  }

  /**
   * Cancels rebinding.
   */
  private void cancelRebinding() {
    rebinding = false;
    Binding binding = bindings.get(index);
    binding.button.setText(binding.key);
    stage.unfocusAll();
  }
  
  @Override
  public void render(float delta) {
    if (active) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        if (rebinding) {
          cancelRebinding();
        } else {
          exitToMenu();
          return;
        }
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
  }

  @Override
  public void dispose() {
    stage.dispose();
  }

  /**
   * Wrapper class for a binding item.
   */
  private class Binding {
    /** Label for description */
    public Label label;
    /** Button for rebinding */
    public TextButton button;
    /** Currently bound key */
    public String key;
    /** Index in binding array */
    public int index;
    /** Action string */
    public String action;

    /**
     * Binds the given key.
     */
    public void setKey(String newKey) {
      key = newKey;
      SaveController.getInstance().bindKey(action, key);
      button.setText(key);
    }
  }
}
