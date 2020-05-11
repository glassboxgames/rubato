package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.glassboxgames.rubato.serialize.*;

/**
 * Singleton controller for reading from and writing to the save file.
 */
public class SaveController {
  /** Savegame file */
  private static final String SAVE_FILE = "Data/save.json";

  /** Save data cache */
  private SaveData data;

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
    try {
      data = Shared.JSON.fromJson(SaveData.class, Gdx.files.local(SAVE_FILE));
    } catch (Exception e) {
      data = Shared.JSON.fromJson(SaveData.class, Gdx.files.internal(SAVE_FILE));
    }
  }

  /**
   * Writes the current data cache to the save file.
   */
  private void writeSave() {
    Gdx.files.local(SAVE_FILE).writeString(Shared.JSON.prettyPrint(data), false);
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
}
