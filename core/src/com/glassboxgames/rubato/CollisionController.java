package com.glassboxgames.rubato;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class CollisionController{

    /** The singleton instance of the collision controller */
    private static CollisionController controller = null;

    /**
     * Returns the singleton instance of the collision controller.
     */
    public static CollisionController getInstance() {
        if (controller == null) {
            controller = new CollisionController();
        }
        return controller;
    }

    /**
     * Create a new collision controller. Only used to create the singleton.
     */
    private CollisionController() {}

    public void startCollision(Object o1, Object o2) {
        if (o1 instanceof Player && o2 instanceof Platform){
            startCollision((Player) o1, (Platform) o2);
        } else if (o2 instanceof Player && o1 instanceof Platform) {
            startCollision((Player) o2, (Platform) o1);
        } else if (o1 instanceof Player && o2 instanceof Enemy) {
            startCollision((Player) o1, (Enemy) o2);
        } else if (o2 instanceof Player && o1 instanceof Enemy) {
            startCollision((Player) o2, (Enemy) o1);
        }
    }
    public void endCollision(Object o1, Object o2) {
        if (o1 instanceof Player && o2 instanceof Platform){
            endCollision((Player) o1, (Platform) o2);
        } else if (o2 instanceof Player && o1 instanceof Platform) {
            endCollision((Player) o2, (Platform) o1);
        } else if (o1 instanceof Player && o2 instanceof Enemy) {
            endCollision((Player) o1, (Enemy) o2);
        } else if (o2 instanceof Player && o1 instanceof Enemy) {
            endCollision((Player) o2, (Enemy) o1);
        }
    }
    /**
     * Handles the start collision between player and enemy
     * @param p the player character
     * @param e the enemy
     */
    public void startCollision(Player p, Enemy e) {
        if (e.isSuspended()) {
            if (p!= null) { p.setGrounded(true); }
        } else {
            p.isAlive = false;
        }
    }
    public void startCollision(Enemy e, Player p) {
        startCollision(p,e);
    }

    /**
     * Handles the start collision between player and enemy
     * @param p the player character
     * @param e the enemy
     */
    public void endCollision(Player p, Enemy e) {
        if (e.isSuspended()) {
            if (p!= null) { p.setGrounded(false); }
        } else {

        }
    }
    public void endCollision(Enemy e, Player p) {
        startCollision(p,e);
    }

    /**
     * Handles the start collision between players and platforms.
     * @param player
     * @param platform
     */
    public void startCollision(Player player, Platform platform) {
        if (player != null) {
            player.setGrounded(true);
        }
    }
    public void startCollision(Platform platform, Player player) {
        startCollision(player, platform);
    }

    /**
     * Handles the end collision between players and platforms.
     * @param player
     * @param platform
     */
    public void endCollision(Player player, Platform platform) {
        if (player != null) {
            player.setGrounded(false);
        }
    }
    public void endCollision(Platform platform, Player player) {
        startCollision(player, platform);
    }


}
