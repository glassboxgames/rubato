package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
    public static final float THRUST_FACTOR = 1f;
    public static final float FORWARD_DAMPING = 0.9f;
    public static final float MAX_SPEED = 8f;

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
            // Move key pressed; increase the player velocity.
            if (velocity.len() < MAX_SPEED) {
                if (direction == -1) {
                    velocity.add(-THRUST_FACTOR, 0);
                } else {
                    velocity.add(THRUST_FACTOR, 0);
                }
            }
            position.x += velocity.x;
        } else {
            velocity.scl(FORWARD_DAMPING, 0);
        }
    }

    /* 0 is not moving, 1 up-right, 2 is right, 3 is right-down, 4 is down, 5 is down-left, 6 is left, 7 is up-left, 8 is up */
    public int moving() {
        boolean right = velocity.x > 0;
        boolean up = velocity.y > 0;
        boolean left = velocity.x < 0;
        boolean down = velocity.y < 0;
        if (right) {
            if (up) {
                return 1;
            }
            else if (down) {
                return 3;
            }
            return 2;
        }
        else if (left) {
            if (up) {
                return 7;
            }
            else if (down) {
                return 5;
            }
            return 6;
        }
        else if (up) {
            return 8;
        }
        else if (down) {
            return 4;
        }
        return 0;
    }



}
