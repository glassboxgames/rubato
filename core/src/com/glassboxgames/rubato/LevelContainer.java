package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.glassboxgames.rubato.entity.Enemy;
import com.glassboxgames.rubato.entity.Platform;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;

/**
 * This class represents everything inside a single level. It should also be able to parse information from a json file.
 * This only contains the information of a level and does not do anything with it.
 */
public class LevelContainer {
    private Array<Platform> platforms;
    private Array<Enemy> enemies;
    private Vector2 spawn;

    /** level represents a JSON containing our level information */
    private JSONObject level;

    public LevelContainer() {
        import_from_json("core/assets/Levels/test.json");
    }

    /**
     * reads a json file and instantiate parameters according to the json file
     */
    public void import_from_json(String file) {
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(file)){

            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            System.out.println(jsonObject);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
