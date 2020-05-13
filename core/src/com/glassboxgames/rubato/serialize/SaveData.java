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
  /** Time tracker */
  public ObjectMap<String, TimeData> times;

  /**
   * Simple class for serializing time statistics for a chapter.
   */
  public static class TimeData {
    /** Total seconds spent on this chapter */
    public long total;
    /** Record times (milliseconds), indexed by level */
    public Array<Long> records;
  }
}
