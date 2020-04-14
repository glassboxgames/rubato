// package com.glassboxgames.rubato.entity;

// import com.badlogic.gdx.physics.box2d.BodyDef;
// import com.badlogic.gdx.utils.Array;

// public class Drone extends Enemy {
//     /** Drone states */
//     public static Array<State> states = null;
//     /** Drone state constants */
//     public static final int STATE_WANDER = 0;

//     /** Max Health */
//     protected static final float MAX_HEALTH = 10;
//     /** Maximum speed */
//     protected static final float MAX_SPEED = 3f;
//     /** Movement range */
//     protected static final float MOVE_RANGE = 2f;
//     /** Movement limits */
//     protected float minX, maxX;
//     /**
//      * Initializes an enemy with the specified parameters.
//      *
//      * @param x x-coordinate
//      * @param y y-coordinate
//      */
//     public Drone(float x, float y) {
//         super(x, y);
//         bodyDef.type = BodyDef.BodyType.KinematicBody;
//         health = MAX_HEALTH;
//         minX = x - MOVE_RANGE;
//         maxX = x + MOVE_RANGE;


//     }

//     @Override
//     public Array<State> getStates() {
//         return states;
//     }

//     @Override
//     public void update(float delta) {
//         super.update(delta);
//         switch (stateIndex) {
//             case STATE_WANDER:
//                 if (getPosition().x >= maxX) {
//                     faceLeft();
//                 } else if (getPosition().x <= minX) {
//                     faceRight();
//                 }
//                 body.setLinearVelocity(MAX_SPEED * getDirection(), 0);
//                 break;
//         }
//     }
//     @Override
//     public void leaveState() {
//         //this is required to reset the frame counter
//         super.leaveState();
//     }
//     @Override
//     public void advanceState() {
//         //advance state does not need a super.advanceState()
//         switch (stateIndex) {
//             case STATE_WANDER:
//                 break;
//         }

//     }
//     @Override
//     public Array<Enemy> startAttack() {

//         return null;
//     }
//     @Override
//     public float getMaxHealth() {
//         return MAX_HEALTH;
//     }
// }
