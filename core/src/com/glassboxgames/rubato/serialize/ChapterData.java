package com.glassboxgames.rubato.serialize;

import com.badlogic.gdx.utils.*;

/**
 * Simple chapter data serialization class.
 */
public class ChapterData {
  /** Short name of the chapter */
  public String key;
  /** Array of paths to the maps in this chapter */
  public Array<String> maps;
}
