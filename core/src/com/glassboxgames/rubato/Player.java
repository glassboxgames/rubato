package com.glassboxgames.rubato;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
    //Physic constraints
    public static final float THRUST_FACTOR = 0.2f;
    public static final float FORWARD_DAMPING = 0.9f;
    /** */
    public static final float MAX_SPEED = 15f;

    //character movement
    Vector2 position;
    Vector2 velocity;
    boolean isJumping;
    boolean isGrounded;

    Player(int x, int y) {
        position = new Vector2(x,y);
        velocity = new Vector2(0, 0);
        this.isJumping = false;
    }

    /**
     * Returns the position of the player
     * @return todo:
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Return the vector2 velocity of the player
     * @return return the velocity of the player
     */
    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * todo:
     * @return
     */
    public boolean isJumping() {
        return isJumping;
    }

    /**
     * todo:
     * @param x
     */
    public void setJump(boolean x) {
        isJumping = x;
    }
    /** todo: reminder, we should focus on imperative programming
     * instead of having a jump() function, we should have a function called setJump*/
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
