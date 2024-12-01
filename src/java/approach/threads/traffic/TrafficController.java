package approach.threads.traffic;

import approach.threads.Car;
import approach.threads.utils.Location;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class TrafficController {
    volatile public boolean driveLeftTurnLeftPossible;
    volatile public boolean driveLeftTurnRightPossible;
    volatile public boolean driveLeftGoStraightPossible;
    volatile public boolean driveRightTurnLeftPossible;
    volatile public boolean driveRightTurnRightPossible;
    volatile public boolean driveRightGoStraightPossible;
    volatile public boolean driveBotTurnLeftPossible;
    volatile public boolean driveBotTurnRightPossible;
    volatile public boolean driveBotGoStraightPossible;
    volatile public boolean driveTopTurnLeftPossible;
    volatile public boolean driveTopTurnRightPossible;
    volatile public boolean driveTopGoStraightPossible;

    volatile public ArrayDeque<Car> leftRoadBlockingCarsReference;
    volatile public ArrayDeque<Car> rightRoadBlockingCarsReference;
    volatile public ArrayDeque<Car> botRoadBlockingCarsReference;
    volatile public ArrayDeque<Car> topRoadBlockingCarsReference;

//    volatile public boolean rightWaiting;
//    volatile public boolean leftWaiting;
//    volatile public boolean topWaiting;
//    volatile public boolean botWaiting;

    public TrafficController() {
        this.driveLeftTurnLeftPossible = true;
        this.driveLeftTurnRightPossible = true;
        this.driveLeftGoStraightPossible = true;
        this.driveRightTurnLeftPossible = true;
        this.driveRightTurnRightPossible = true;
        this.driveRightGoStraightPossible = true;
        this.driveBotTurnLeftPossible = true;
        this.driveBotTurnRightPossible = true;
        this.driveBotGoStraightPossible = true;
        this.driveTopTurnLeftPossible = true;
        this.driveTopTurnRightPossible = true;
        this.driveTopGoStraightPossible = true;
        this.topRoadBlockingCarsReference = new ArrayDeque<>();
        this.botRoadBlockingCarsReference = new ArrayDeque<>();
        this.leftRoadBlockingCarsReference = new ArrayDeque<>();
        this.rightRoadBlockingCarsReference = new ArrayDeque<>();
    }

    public synchronized void blockForLeftTurn(Location location) {
        switch (location) {
            case BOT -> {
                this.driveBotGoStraightPossible = false;
                this.driveBotTurnRightPossible = false;
                this.driveRightGoStraightPossible = false;
                this.driveRightTurnLeftPossible = false;
                this.driveLeftGoStraightPossible = false;
                this.driveLeftTurnLeftPossible = false;
            }
            case TOP -> {
                this.driveTopGoStraightPossible = false;
                this.driveTopTurnRightPossible = false;
                this.driveRightGoStraightPossible = false;
                this.driveRightTurnLeftPossible = false;
                this.driveLeftGoStraightPossible = false;
                this.driveLeftTurnLeftPossible = false;
            }
            case LEFT -> {
                this.driveBotGoStraightPossible = false;
                this.driveBotTurnRightPossible = false;
                this.driveTopGoStraightPossible = false;
                this.driveTopTurnLeftPossible = false;
                this.driveLeftGoStraightPossible = false;
                this.driveLeftTurnLeftPossible = false;
            }
            default -> { // RIGHT
                this.driveBotGoStraightPossible = false;
                this.driveBotTurnRightPossible = false;
                this.driveRightGoStraightPossible = false;
                this.driveRightTurnLeftPossible = false;
                this.driveTopGoStraightPossible = false;
                this.driveTopTurnLeftPossible = false;
            }
        }
    }

    public synchronized void unblockForLeftTurn(Location location) {
        switch (location) {
            case BOT -> {
                this.driveBotGoStraightPossible = true;
                this.driveBotTurnRightPossible = true;
                this.driveRightGoStraightPossible = true;
                this.driveRightTurnLeftPossible = true;
                this.driveLeftGoStraightPossible = true;
                this.driveLeftTurnLeftPossible = true;
            }
            case TOP -> {
                this.driveTopGoStraightPossible = true;
                this.driveTopTurnRightPossible = true;
                this.driveRightGoStraightPossible = true;
                this.driveRightTurnLeftPossible = true;
                this.driveLeftGoStraightPossible = true;
                this.driveLeftTurnLeftPossible = true;
            }
            case LEFT -> {
                this.driveBotGoStraightPossible = true;
                this.driveBotTurnRightPossible = true;
                this.driveTopGoStraightPossible = true;
                this.driveTopTurnLeftPossible = true;
                this.driveLeftGoStraightPossible = true;
                this.driveLeftTurnLeftPossible = true;


            }
            default -> { // RIGHT
                this.driveBotGoStraightPossible = true;
                this.driveBotTurnRightPossible = true;
                this.driveRightGoStraightPossible = true;
                this.driveRightTurnLeftPossible = true;
                this.driveTopGoStraightPossible = true;
                this.driveTopTurnLeftPossible = true;
            }
        }
    }

    public synchronized void blockForGoStraight(Location location) {
        switch (location) {
            case BOT -> {
                this.driveBotTurnLeftPossible = false;
                this.driveRightGoStraightPossible = false;
                this.driveRightTurnLeftPossible = false;
                this.driveRightTurnRightPossible = false;
                this.driveLeftGoStraightPossible = false;
                this.driveLeftTurnLeftPossible = false;
            }
            case TOP -> {
                this.driveTopTurnLeftPossible = false;
                this.driveRightGoStraightPossible = false;
                this.driveRightTurnLeftPossible = false;
                this.driveRightTurnRightPossible = false;
                this.driveLeftGoStraightPossible = false;
                this.driveLeftTurnLeftPossible = false;
            }
            case LEFT -> {
                this.driveBotTurnLeftPossible = false;
                this.driveBotGoStraightPossible = false;
                this.driveBotTurnRightPossible = false;
                this.driveRightTurnLeftPossible = false;
                this.driveTopGoStraightPossible = false;
                this.driveTopTurnLeftPossible = false;
            }
            default -> { // RIGHT
                this.driveBotTurnLeftPossible = false;
                this.driveBotGoStraightPossible = false;
                this.driveBotTurnRightPossible = false;
                this.driveLeftTurnLeftPossible = false;
                this.driveTopGoStraightPossible = false;
                this.driveTopTurnLeftPossible = false;
            }
        }
    }

    public synchronized void unblockForGoStraight(Location location) {
        switch (location) {
            case BOT -> {
                this.driveBotTurnLeftPossible = true;
                this.driveRightGoStraightPossible = true;
                this.driveRightTurnLeftPossible = true;
                this.driveRightTurnRightPossible = true;
                this.driveLeftGoStraightPossible = true;
                this.driveLeftTurnLeftPossible = true;
            }
            case TOP -> {
                this.driveTopTurnLeftPossible = true;
                this.driveRightGoStraightPossible = true;
                this.driveRightTurnLeftPossible = true;
                this.driveRightTurnRightPossible = true;
                this.driveLeftGoStraightPossible = true;
                this.driveLeftTurnLeftPossible = true;
            }
            case LEFT -> {
                this.driveBotTurnLeftPossible = true;
                this.driveBotGoStraightPossible = true;
                this.driveBotTurnRightPossible = true;
                this.driveRightTurnLeftPossible = true;
                this.driveTopGoStraightPossible = true;
                this.driveTopTurnLeftPossible = true;
            }
            default -> { // RIGHT
                this.driveBotTurnLeftPossible = true;
                this.driveBotGoStraightPossible = true;
                this.driveBotTurnRightPossible = true;
                this.driveLeftTurnLeftPossible = true;
                this.driveTopGoStraightPossible = true;
                this.driveTopTurnLeftPossible = true;
            }
        }
    }

    public synchronized void blockForTurnRight(Location location) {
        switch (location) {
            case BOT -> {
                this.driveBotTurnLeftPossible = false;
                this.driveRightGoStraightPossible = false;
            }
            case TOP -> {
                this.driveTopTurnLeftPossible = false;
                this.driveLeftGoStraightPossible = false;
            }
            case LEFT -> {
                this.driveBotGoStraightPossible = false;
                this.driveRightTurnLeftPossible = false;
            }
            default -> { // RIGHT
                this.driveTopGoStraightPossible = false;
                this.driveLeftTurnLeftPossible = false;
            }
        }
    }

    public synchronized void unblockForTurnRight(Location location) {
        switch (location) {
            case BOT -> {
                this.driveBotTurnLeftPossible = true;
                this.driveRightGoStraightPossible = true;
            }
            case TOP -> {
                this.driveTopTurnLeftPossible = true;
                this.driveLeftGoStraightPossible = true;
            }
            case LEFT -> {
                this.driveBotGoStraightPossible = true;
                this.driveRightTurnLeftPossible = true;
            }
            default -> { // RIGHT
                this.driveTopGoStraightPossible = true;
                this.driveLeftTurnLeftPossible = true;
            }
        }
    }

    public synchronized Car getOccupyingRoadCarByLocation(Location l) {
        if (!this.botRoadBlockingCarsReference.isEmpty() && l == Objects.requireNonNull(this.botRoadBlockingCarsReference.peek()).getLocation()) {
            return botRoadBlockingCarsReference.peek();
        } else if (!this.topRoadBlockingCarsReference.isEmpty() && l == Objects.requireNonNull(this.topRoadBlockingCarsReference.peek()).getLocation()) {
            return this.topRoadBlockingCarsReference.peek();
        } else if (!this.leftRoadBlockingCarsReference.isEmpty() && l == Objects.requireNonNull(this.leftRoadBlockingCarsReference.peek()).getLocation()) {
            return this.leftRoadBlockingCarsReference.peek();
        } else if (!this.rightRoadBlockingCarsReference.isEmpty() && l == Objects.requireNonNull(this.rightRoadBlockingCarsReference.peek()).getLocation()) {
            return this.rightRoadBlockingCarsReference.peek();
        }
        return null;
    }


}
