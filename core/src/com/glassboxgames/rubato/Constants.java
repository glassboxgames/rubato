package com.glassboxgames.rubato;

import com.badlogic.gdx.utils.*;

/**
 * Class for constants. Should not be instantiated.
 */
public final class Constants {
  /** Screen pixels per Box2D meter */
  public static final float PPM = 100f;
  /** Map of background keys to file paths */
  public static final ObjectMap<String, String> BACKGROUND_MAP = new ObjectMap<String, String>();
  /** JSON serializer/deserializer */
  public static final Json JSON = new Json();

  static {
    BACKGROUND_MAP.put("forest", "Backgrounds/Forest/forest.png");
    BACKGROUND_MAP.put("plains", "Backgrounds/Plains/plains.png");
    BACKGROUND_MAP.put("desert", "Backgrounds/Desert/desert.png");
    BACKGROUND_MAP.put("mountains", "Backgrounds/Mountains/mountains.png");
    JSON.setOutputType(JsonWriter.OutputType.json);
  }
}
