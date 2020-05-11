package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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

  /** Path to chapters */
  public static final String CHAPTERS_FILE = "Data/chapters.json";
  /** Saved settings file */
  public static final String SETTINGS_FILE = "Data/settings.json";
  /** File containing the texture shortname map */
  public static final String TEXTURE_MAP_FILE = "Data/textures.json";

  /** JSON serializer/deserializer */
  public static final Json JSON = new Json();

  /** Map of texture shortnames to file paths */
  public static final OrderedMap<String, String> TEXTURE_PATHS =
    JSON.fromJson(OrderedMap.class, Gdx.files.internal(TEXTURE_MAP_FILE));
  /** Map of texture shortnames to textures */
  public static final OrderedMap<String, Texture> TEXTURE_MAP = new OrderedMap<String, Texture>();

  /** Font files, by weight */
  public static final Array<String> FONT_FILES = new Array<String>();
  /** Bold font file path */
  public static final String BOLD_FONT_FILE = "Fonts/Rajdhani-Bold.ttf";
  /** Semibold font file path */
  public static final String SEMIBOLD_FONT_FILE = "Fonts/Rajdhani-SemiBold.ttf";
  /** Regular font file path */
  public static final String REGULAR_FONT_FILE = "Fonts/Rajdhani-Regular.ttf";

  /** File containing the font shortname map */
  public static final String FONT_MAP_FILE = "Data/fonts.json";
  /** Map of font shortnames to font metadata arrays [weight, size, spacing] */
  public static final OrderedMap<String, Array<Float>> FONT_METADATA =
    JSON.fromJson(OrderedMap.class, Gdx.files.internal(FONT_MAP_FILE));
  /** Map of font shortnames to font objects */
  public static final OrderedMap<String, BitmapFont> FONT_MAP = new OrderedMap<String, BitmapFont>();

  /** Array of level data arrays, ordered by chapter */
  public static final Array<Array<LevelData>> CHAPTER_LEVELS = new Array<Array<LevelData>>();

  /** The desaturation shader */
  public static final ShaderProgram DESAT_SHADER = new ShaderProgram(Gdx.files.internal("Shaders/desat.vsr"), Gdx.files.internal("Shaders/desat.fsr"));

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
  }

  /**
   * Creates font loader parameters with the given info.
   * @param file path to font file
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
}
