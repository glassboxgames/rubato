package com.glassboxgames.rubato;

/**
 * Class representing a main player character in Rubato.
 */
public class Player extends Entity {
    int x;
    int y;
    boolean isJumping;
    Player(int x, int y) {
        super(x, y);

        this.isJumping = false;
    }

    /** Getters and Setters */
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void jump() {
        System.out.println("Big bouncy time");
    }

    public void move(String direction) {
        if (direction == "right") {
            System.out.println("moving right");
        }
        if (direction == "left") {
            System.out.println("moving left");
        }
    }

}
