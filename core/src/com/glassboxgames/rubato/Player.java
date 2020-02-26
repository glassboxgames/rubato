package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
    public static final float THRUST_FACTOR = 0.2f;
    public static final float FORWARD_DAMPING = 0.9f;
    public static final float MAX_SPEED = 15f;

    Vector2 position;
    Vector2 velocity;
    boolean isJumping;

    Player(int x, int y) {
        position = new Vector2(x,y);
        velocity = new Vector2(0, 0);
        this.isJumping = false;
    }

    /** Getters */
    public int getX() {
        return (int)position.x;
    }

    public int getY() {
        return (int)position.y;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void jump() {
        System.out.println("Big bouncy time");
    }

    public void move(int direction) {
        if (direction != 0) {
            // Thrust key pressed; increase the ship velocity.
            if (velocity.x < MAX_SPEED) velocity.add(THRUST_FACTOR, 0);

            if (direction == -1) {
                position.x -= velocity.x;
            } else {
                position.x += velocity.x;
            }

        } else {
            velocity.scl(FORWARD_DAMPING, 0);
        }
    }

}
