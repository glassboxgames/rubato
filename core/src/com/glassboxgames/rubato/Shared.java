package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.serialize.*;

/**
 * Class for shared constants and utility methods. Should not be instantiated.
 */
public final class Shared {
  /** Screen pixels per Box2D meter */
  public static final float PPM = 75f;
  /** Background image scaling */
  public static final float BACKGROUND_SCALE = 0.4f;

  /** Chapter constants */
  public static final int CHAPTER_FOREST = 0;
  public static final int CHAPTER_PLAINS = 1;
  public static final int CHAPTER_DESERT = 2;
  public static final int CHAPTER_MOUNTAINS = 3;
  public static final Array<String> CHAPTER_NAMES = new Array<String>();

  /** External user data path */
  public static final String EXTERNAL_PATH = "Rubato/";
  /** Path to chapters */
  public static final String CHAPTERS_FILE = "Data/chapters.json";
  /** Saved settings file */
  public static final String SETTINGS_FILE = "Data/settings.json";
  /** File containing the texture shortname map */
  public static final String TEXTURE_MAP_FILE = "Data/textures.json";
  /** File containing the font shortname map */
  public static final String FONT_MAP_FILE = "Data/fonts.json";
  /** File containing the sound shortname map */
  public static final String SOUND_MAP_FILE = "Data/sounds.json";
  /** File containing the music shortname map */
  public static final String MUSIC_MAP_FILE = "Data/music.json";

  /** JSON serializer/deserializer */
  public static final Json JSON = new Json();

  /** Map of texture shortnames to file paths */
  public static final OrderedMap<String, String> TEXTURE_PATHS =
    JSON.fromJson(OrderedMap.class, Gdx.files.internal(TEXTURE_MAP_FILE));
  /** Map of texture shortnames to textures */
  public static final OrderedMap<String, Texture> TEXTURE_MAP = new OrderedMap<String, Texture>();

  /** Map of sound shortnames to sound file paths */
  public static final OrderedMap<String, String> SOUND_PATHS =
    JSON.fromJson(OrderedMap.class, Gdx.files.internal(SOUND_MAP_FILE));
  /** Map of music shortnames to music file paths */
  public static final OrderedMap<String, String> MUSIC_PATHS =
    JSON.fromJson(OrderedMap.class, Gdx.files.internal(MUSIC_MAP_FILE));

  /** Font files, by weight */
  public static final Array<String> FONT_FILES = new Array<String>();
  /** Map of font shortnames to font metadata arrays [weight, size, spacing] */
  public static final OrderedMap<String, Array<Float>> FONT_METADATA =
    JSON.fromJson(OrderedMap.class, Gdx.files.internal(FONT_MAP_FILE));
  /** Map of font shortnames to font objects */
  public static final OrderedMap<String, BitmapFont> FONT_MAP = new OrderedMap<String, BitmapFont>();

  /** Array of level data arrays, ordered by chapter */
  public static final Array<Array<LevelData>> CHAPTER_LEVELS = new Array<Array<LevelData>>();

  /** Shape renderer for overlays */
  public static final ShapeRenderer OVERLAY_RENDERER = new ShapeRenderer();
  /** Classic teal color */
  public static final Color TEAL = new Color(0f, 1f, 0.82f, 1f);

  /** Sound files */
  public static final String GRASS_RUN_SOUND = "Sounds/Running/Grass.mp3";
  public static final String DASH_SOUND = "Sounds/Dash/Dash.mp3";
  public static final String ATTACK_SWING_SOUND = "Sounds/Attacking/AttackSwing.mp3";
  public static final String CHECKPOINT_SOUND = "Sounds/Environment/Checkpoint.mp3";
  public static final String ATTACK_HIT_SOUND = "Sounds/Attacking/AttackHit.mp3";
  public static final String DEATH_SOUND = "Sounds/Death/Death.mp3";

  /** Player action strings */
  public static final String ACTION_UP = "up";
  public static final String ACTION_DOWN = "down";
  public static final String ACTION_LEFT = "left";
  public static final String ACTION_RIGHT = "right";
  public static final String ACTION_JUMP = "jump";
  public static final String ACTION_ATTACK = "attack";
  public static final String ACTION_RESET = "reset";

  static {
    JSON.setOutputType(JsonWriter.OutputType.json);

    FONT_FILES.add("Fonts/Rajdhani-Regular.ttf");
    FONT_FILES.add("Fonts/Rajdhani-SemiBold.ttf");
    FONT_FILES.add("Fonts/Rajdhani-Bold.ttf");

    Array<ChapterData> chapters = JSON.fromJson(Array.class, ChapterData.class,
                                                Gdx.files.internal(CHAPTERS_FILE));
    for (ChapterData chapter : chapters) {
      Array<LevelData> maps = new Array<LevelData>();
      for (String mapPath : chapter.maps) {
        maps.add(JSON.fromJson(LevelData.class, Gdx.files.internal(mapPath)));
      }
      CHAPTER_NAMES.add(chapter.key);
      CHAPTER_LEVELS.add(maps);
    }

    ShaderProgram.pedantic = false;
  }

  /**
   * Creates font loader parameters with the given info.
   * @param weight font weight
   * @param size font size
   * @param spacing character spacing
   */
  public static FreetypeFontLoader.FreeTypeFontLoaderParameter
    createFontLoaderParams(int weight, int size, int spacing) {
    FreetypeFontLoader.FreeTypeFontLoaderParameter params =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    params.fontFileName = FONT_FILES.get(weight);
    params.fontParameters.size = size;
    params.fontParameters.spaceX = spacing;
    return params;
  }

  /**
   * Returns the texture for the given key.
   */
  public static Texture getTexture(String key) {
    return TEXTURE_MAP.get(key);
  }

  /**
   * Returns a drawable-wrapped texture for the given key.
   */
  public static TextureRegionDrawable getDrawable(String key) {
    return new TextureRegionDrawable(getTexture(key));
  }

  /**
   * Returns the font for the given key.
   */
  public static BitmapFont getFont(String key) {
    return FONT_MAP.get(key);
  }

  /**
   * Returns the sound path for the given key.
   */
  public static String getSoundPath(String key) {
    return SOUND_PATHS.get(key);
  }

  /**
   * Returns the music path for the given key.
   */
  public static String getMusicPath(String key) {
    return MUSIC_PATHS.get(key);
  }

  /**
   * Draw an overlay with the given color.
   */
  public static void drawOverlay(Color color) {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    ShapeRenderer renderer = OVERLAY_RENDERER;
    renderer.begin(ShapeRenderer.ShapeType.Filled);
    renderer.setColor(color);
    renderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    renderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  /**
   * Draw a black overlay with the given alpha.
   */
  public static void drawOverlay(float alpha) {
    drawOverlay(new Color(0, 0, 0, alpha));
  }

  /**
   * Formats milliseconds as a time string.
   */
  public static String formatTime(long millis) {
    long secs = millis / 1000;
    long mins = secs / 60;
    long hrs = mins / 60;
    return hrs == 0
      ? String.format("%d:%02d.%03d", mins % 60, secs % 60, millis % 1000)
      : String.format("%d:%02d:%02d.%03d", hrs, mins % 60, secs % 60, millis % 1000);
  }
}
