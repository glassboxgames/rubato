package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.serialize.*;

/**
 * Class for shared constants and utility methods. Should not be instantiated.
 */
public final class Shared {
  /** Screen pixels per Box2D meter */
  public static final float PPM = 100f;
  /** Final scaling factor */
  public static final float SCALE = 0.75f;
  /** Final scaled PPM */
  public static final float SCALED_PPM = PPM * SCALE;

  /** Bold font file path */
  public static final String BOLD_FONT_FILE = "Fonts/Rajdhani-Bold.ttf";
  /** Semibold font file path */
  public static final String SEMIBOLD_FONT_FILE = "Fonts/Rajdhani-SemiBold.ttf";
  /** Regular font file path */
  public static final String REGULAR_FONT_FILE = "Fonts/Rajdhani-Regular.ttf";
  /** Default font spacing */
  public static final int DEFAULT_FONT_SPACING = 8;

  /** JSON serializer/deserializer */
  public static final Json JSON = new Json();
  /** Path to chapters */
  public static final String CHAPTERS_FILE = "Config/chapters.json";

  /** Map of chapter names to background paths */
  public static final ObjectMap<String, String> BACKGROUND_PATHS =
    new ObjectMap<String, String>();
  /** Map of chapter names to level data arrays */
  public static final ObjectMap<String, Array<LevelData>> CHAPTER_LEVELS =
    new ObjectMap<String, Array<LevelData>>();

  static {
    JSON.setOutputType(JsonWriter.OutputType.json);

    Array<ChapterData> chapters = JSON.fromJson(Array.class, ChapterData.class,
                                                Gdx.files.internal(CHAPTERS_FILE));
    for (ChapterData chapter : chapters) {
      Array<LevelData> maps = new Array<LevelData>();
      for (String mapPath : chapter.maps) {
        maps.add(JSON.fromJson(LevelData.class, Gdx.files.internal(mapPath)));
      }
      CHAPTER_LEVELS.put(chapter.key, maps);
      BACKGROUND_PATHS.put(chapter.key, chapter.background);
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
