package com.glassboxgames.rubato.entity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.glassboxgames.rubato.*;
import com.glassboxgames.util.*;

/**
 * Class representing an animation state for an entity.
 */
public class State {
  /**
   * Class to represent state metadata for JSON serialization.
   */
  class StateMetadata {
    String name, path;
    boolean loop;
  }
  
  /**
   * Class to represent a hurtbox for JSON serialization.
   */
  class HurtboxData {
    float x, y, width, height, angle;
  }

  /**
   * Class to represent an animation frame for JSON serialization.
   */
  class FrameData {
    String file;
    Array<HurtboxData> hurtboxes;
  }

  /**
   * Class to represent an in-memory frame.
   */
  class Frame {
    String path;
    Texture texture;
    Array<FixtureDef> hurtboxDefs;
  }

  /** Filename of the state master file */
  protected static final String STATE_FILE = "states.json";
  /** Filename of the frame config file */
  protected static final String FRAME_FILE = "frames.json";
  
  /** Whether to loop the animation */
  protected boolean loop;
  /** Whether the animation is finished (always false if looping) */
  protected boolean done;
  /** Current animation frame count */
  protected int count;
  /** Array of frame data */
  protected Array<FrameData> frameDataList;
  /** Array of frames */
  protected Array<Frame> frames;

  /**
   * Creates an array of states from the given root path.
   * Requires the presence of a `states.json` file at that path.
   * @param path
   */
  public static Array<State> readStates(String path) {
    Json json = new Json();
    Array<StateMetadata> metadataList = json.fromJson(Array.class, StateMetadata.class,
                                                      Gdx.files.internal(path + STATE_FILE));
    Array<State> states = new Array<State>();
    for (StateMetadata metadata : metadataList) {
      states.add(new State(path + metadata.path, metadata.loop));
    }
    return states;
  }

  /**
   * Instantiates an entity state.
   * @param path path to the directory containing the entity state data
   */
  public State(String path, boolean loop) {
    this.loop = loop;
    frames = new Array<Frame>();
    Json json = new Json();
    frameDataList = json.fromJson(Array.class, FrameData.class,
                                  Gdx.files.internal(path + FRAME_FILE).readString());
    for (FrameData frameData : frameDataList) {
      Frame frame = new Frame();
      frame.path = path + frameData.file;
      frame.hurtboxDefs = new Array<FixtureDef>();
      for (HurtboxData hurtboxData : frameData.hurtboxes) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(hurtboxData.width, hurtboxData.height,
                       new Vector2(hurtboxData.x, hurtboxData.y),
                       hurtboxData.angle);
        FixtureDef def = new FixtureDef();
        def.density = 1f;
        def.friction = 0f;
        def.shape = shape;
        frame.hurtboxDefs.add(def);
      }
      frames.add(frame);
    }
  }

  /**
   * Preloads the textures for this state.
   * @param manager asset manager to use
   */
  public void preloadContent(AssetManager manager) {
    for (Frame frame : frames) {
      manager.load(frame.path, Texture.class);
    }
  }

  /**
   * Loads the textures for this state.
   * @param manager asset manager to use
   */
  public void loadContent(AssetManager manager) {
    for (Frame frame : frames) {
      frame.texture = manager.get(frame.path, Texture.class);
      frame.texture.setFilter(Texture.TextureFilter.Linear,
                              Texture.TextureFilter.Linear);
    }
  }

  /**
   * Unloads the textures for this state.
   * @param manager asset manager to use
   */
  public void unloadContent(AssetManager manager) {
    for (Frame frame : frames) {
      if (manager.isLoaded(frame.path)) {
        manager.unload(frame.path);
      }
    }
  }

  /**
   * Updates this entity state.
   */
  public void update() {
    if (count >= frames.size && !loop) {
      done = true;
    } else {
      count++;
    }
  }

  /**
   * Returns the number of frames that have passed.
   */
  public int getCount() {
    return count;
  }

  /**
   * Returns the current frame object for this state.
   */
  protected Frame getFrame() {
    return frames.get(count % frames.size);
  }
  
  /**
   * Returns the current texture of this state.
   */
  public Texture getTexture() {
    return getFrame().texture;
  }

  /**
   * Returns the current hurtbox fixture definition array of this state.
   */
  public Array<FixtureDef> getHurtboxDefs() {
    return getFrame().hurtboxDefs;
  }

  /**
   * Resets this state for re-animation.
   */
  public void reset() {
    count = 0;
    done = false;
  }
}
