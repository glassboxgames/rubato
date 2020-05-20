package com.glassboxgames.rubato.serialize;

import com.badlogic.gdx.utils.*;

/**
 * Simple class for serializing savegame data.
 */
public class SaveData {
  /** Unlocked level count mapping */
  public ObjectMap<String, Integer> unlocked;
  /** Custom key mapping */
  public ObjectMap<String, String> bindings;
  /** Total time tracker */
  public ObjectMap<String, Long> times;
  /** Music volume setting */
  public float music;
  /** Sound volume setting */
  public float sound;
}
