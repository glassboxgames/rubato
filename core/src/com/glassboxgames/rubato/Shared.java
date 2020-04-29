package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.serialize.*;

/**
 * Class for shared constants and utility methods. Should not be instantiated.
 */
public final class Shared {
  /** Screen pixels per Box2D meter */
  public static final float PPM = 75f;

  /** Bold font file path */
  public static final String BOLD_FONT_FILE = "Fonts/Rajdhani-Bold.ttf";
  /** Semibold font file path */
  public static final String SEMIBOLD_FONT_FILE = "Fonts/Rajdhani-SemiBold.ttf";
  /** Regular font file path */
  public static final String REGULAR_FONT_FILE = "Fonts/Rajdhani-Regular.ttf";
  /** Default font spacing */
  public static final int DEFAULT_FONT_SPACING = 8;

  /** Chapter constants */
  public static final int CHAPTER_FOREST = 0;
  public static final int CHAPTER_PLAINS = 1;
  public static final int CHAPTER_DESERT = 2;
  public static final int CHAPTER_MOUNTAINS = 3;
  public static final Array<String> CHAPTER_NAMES = new Array<String>();

  /** JSON serializer/deserializer */
  public static final Json JSON = new Json();
  /** Path to chapters */
  public static final String CHAPTERS_FILE = "Data/chapters.json";

  /** File containing the texture shortname map */
  public static final String TEXTURE_MAP_FILE = "Data/textures.json";
  /** Map of texture shortnames to file paths */
  public static final ObjectMap<String, String> TEXTURE_PATHS =
    JSON.fromJson(ObjectMap.class, Gdx.files.internal(TEXTURE_MAP_FILE));
  /** Map of texture shortnames to textures */
  public static final ObjectMap<String, Texture> TEXTURE_MAP =
    new ObjectMap<String, Texture>();
  /** Array of level data arrays, ordered by chapter */
  public static final Array<Array<LevelData>> CHAPTER_LEVELS = new Array<Array<LevelData>>();

  static {
    JSON.setOutputType(JsonWriter.OutputType.json);
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
    createFontLoaderParams(String file, int size, int spacing) {
    FreetypeFontLoader.FreeTypeFontLoaderParameter params =
      new FreetypeFontLoader.FreeTypeFontLoaderParameter();
    params.fontFileName = file;
    params.fontParameters.size = size;
    params.fontParameters.spaceX = spacing;
    return params;
  }

  /**
   * Creates font loader parameters with the given info.
   * @param file path to font file
   * @param size font size
   */
  public static FreetypeFontLoader.FreeTypeFontLoaderParameter
    createFontLoaderParams(String file, int size) {
    return createFontLoaderParams(file, size, DEFAULT_FONT_SPACING);
  }
}
