package com.glassboxgames.rubato.serialize;

import com.badlogic.gdx.utils.*;

/**
 * Simple chapter data serialization class.
 */
public class ChapterData {
  /** Short name of the chapter */
  public String key;
  /** Path to the background for the levels of this chapter */
  public String background;
  /** Array of paths to the maps in this chapter */
  public Array<String> maps;
}
