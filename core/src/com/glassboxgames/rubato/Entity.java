package com.glassboxgames.rubato;

/**
 * Abstract class representing any entity (i.e. collision-experiencing object).
 */
public abstract class Entity {
    int x;
    int y;
    Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
