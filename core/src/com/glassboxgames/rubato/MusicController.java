package com.glassboxgames.rubato;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;

/**
 * Controller for playing music.
 */
public class MusicController {
  /** Currently playing music */
  private Music music;
  /** Key for currently playing music */
  private String key;

  /** The singleton instance */
  private static MusicController controller = null;

  /**
   * Instantiates a music controller.
   */
  private MusicController() {}

  /**
   * Returns the singleton instance of this controller.
   */
  public static MusicController getInstance() {
    if (controller == null) {
      controller = new MusicController();
    }
    return controller;
  }
  
  /**
   * Plays the music from the given key.
   */
  public void play(String key) {
    if (!key.equals(this.key)) {
      if (music != null) {
        music.dispose();
      }
      this.key = key;
      music = Gdx.audio.newMusic(Gdx.files.internal(Shared.getMusicPath(key)));
      resetVolume();
      music.setLooping(true);
      music.play();
    }
  }

  /**
   * Stops the music.
   */
  public void stop() {
    if (music != null) {
      music.stop();
    }
  }

  /**
   * Resets the current music volume from the save.
   */
  public void resetVolume() {
    if (music != null) {
      music.setVolume(SaveController.getInstance().getMusicVolume());
    }
  }

  /**
   * Disposes this controller.
   */
  public void dispose() {
    if (music != null) {
      music.dispose();
    }
  }
}
