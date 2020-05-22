package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.serialize.*;

/**
 * Singleton controller for reading from and writing to the save file.
 */
public class SaveController {
  /** Internal path to default savegame file */
  private static final String INTERNAL_SAVE_FILE = "Data/save.json";
  /** External path to savegame file */
  private static final String EXTERNAL_SAVE_FILE = Shared.EXTERNAL_PATH + "save.json";

  /** Left key */
  private static final String KEY_LEFT = "left";
  /** Right key */
  private static final String KEY_RIGHT = "right";
  /** Run action */
  private static final String ACTION_RUN = "run";

  /** Save data cache */
  private SaveData data;
  /** Save data internal */
  private SaveData internalData;

  /** Singleton instance */
  private static SaveController controller = null;
  
  /**
   * Returns the singleton instance of the save controller.
   */
  public static SaveController getInstance() {
    if (controller == null) {
      controller = new SaveController();
    }
    return controller;
  }

  /**
   * Initializes a save controller.
   * Only used for instantiating the singleton.
   */
  private SaveController() {
    internalData = Shared.JSON.fromJson(SaveData.class, Gdx.files.internal(INTERNAL_SAVE_FILE));
    try {
      data = Shared.JSON.fromJson(SaveData.class, Gdx.files.external(EXTERNAL_SAVE_FILE));
      if (data.unlocked == null) {
        data.unlocked = new ObjectMap<>(internalData.unlocked);
      }
      if (data.bindings == null) {
        data.bindings = new ObjectMap<>(internalData.bindings);
      }
      if (data.times == null) {
        data.times = new ObjectMap<>(internalData.times);
      }
      writeSave();
    } catch (Exception e) {
      data = Shared.JSON.fromJson(SaveData.class, Gdx.files.internal(INTERNAL_SAVE_FILE));
    }
  }

  /**
   * Writes the current data cache to the save file.
   */
  private void writeSave() {
    Gdx.files.external(EXTERNAL_SAVE_FILE).writeString(Shared.JSON.prettyPrint(data), false);
  }

  /**
   * Returns the number of unlocked levels in the given chapter.
   */
  public int getLevelsUnlocked(String chapter) {
    return data.unlocked.get(chapter);
  }

  /**
   * Returns the number of unlocked levels in the given chapter.
   */
  public int getLevelsUnlocked(int chapterIndex) {
    return getLevelsUnlocked(Shared.CHAPTER_NAMES.get(chapterIndex));
  }

  /**
   * Sets the number of unlocked levels in the given chapter.
   */
  public void setLevelsUnlocked(String chapter, int levels) {
    data.unlocked.put(chapter, levels);
    writeSave();
  }

  /**
   * Sets the number of unlocked levels in the given chapter.
   */
  public void setLevelsUnlocked(int chapterIndex, int levels) {
    setLevelsUnlocked(Shared.CHAPTER_NAMES.get(chapterIndex), levels);
  }

  /**
   * Returns the key bound to the given action.
   */
  public String getBoundKey(String action) {
    return data.bindings.get(action);
  }

  /**
   * Returns the key code bound to the given action.
   */
  public int getBoundKeycode(String action) {
    return Input.Keys.valueOf(getBoundKey(action));
  }

  /**
   * Binds the given action to the given key.
   */
  public void bindKey(String action, String key) {
    data.bindings.put(action, key);
    writeSave();
  }

  /**
   * Returns whether the bindings are the default bindings.
   */
  public boolean isDefaultBinding(String action) {
    try {
      if (action.equals(ACTION_RUN)) {
        String defaultLeft = internalData.bindings.get(KEY_LEFT);
        String defaultRight = internalData.bindings.get(KEY_RIGHT);
        String savedLeft = data.bindings.get(KEY_LEFT);
        String savedRight = data.bindings.get(KEY_RIGHT);
        return defaultLeft.equals(savedLeft) && defaultRight.equals(savedRight);
      } else {
        String defaultKey = internalData.bindings.get(action);
        String savedKey = data.bindings.get(action);
        return defaultKey.equals(savedKey);
      }
    } catch (Exception e) { // action is not rebindable
      return true;
    }
  }

  /**
   * Gets the total time spent on the given chapter in milliseconds.
   */
  public long getTimeSpent(String chapter) {
    return data.times.get(chapter);
  }

  /**
   * Sets the total time spent on the given chapter.
   */
  public void setTimeSpent(String chapter, long time) {
    data.times.put(chapter, time);
    writeSave();
  }

  /**
   * Adds the given time to the total time spent on the given chapter.
   */
  public void addTimeSpent(String chapter, long time) {
    setTimeSpent(chapter, getTimeSpent(chapter) + time);
  }

  /**
   * Sets the sound volume.
   */
  public void setSoundVolume(float volume) {
    data.sound = volume;
    writeSave();
  }

  /**
   * Returns the sound volume.
   */
  public float getSoundVolume() {
    return data.sound;
  }

  /**
   * Sets the music volume.
   */
  public void setMusicVolume(float volume) {
    data.music = volume;
    writeSave();
  }

  /**
   * Returns the music volume.
   */
  public float getMusicVolume() {
    return data.music;
  }
}
