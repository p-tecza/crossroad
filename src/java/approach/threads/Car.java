package approach.threads;

import approach.threads.traffic.BlockedRoadHelper;
import approach.threads.traffic.TrafficController;
import approach.threads.utils.CarLocationData;
import approach.threads.utils.DriveDirection;
import approach.threads.utils.Location;
import approach.threads.utils.Spawner;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Car implements Runnable {

    public CarLocationData carLocationData;
    private Location location;
    private final Color color;
    private final AnimationScreen screen;
    private final DriveDirection driveDirection;
    private double radianRotation;
    private double radianRotationForTranslation;
    private double radianRotationDx;
    private boolean turnStarted = false;
    private boolean turnFinished = false;

    private boolean enteredMiddle = false;
    private boolean exitedMiddle = false;
    private double radianRotationForTranslationBuffer;

    private final TrafficController trafficController;
    private final BlockedRoadHelper blockedRoadHelper;

    //    private Thread threadReference;
    private static final Map<DriveDirection, Color> colorMapping = new HashMap<>();

    static {
        colorMapping.put(DriveDirection.TURN_LEFT, Color.RED);
        colorMapping.put(DriveDirection.STRAIGHT, Color.BLUE);
        colorMapping.put(DriveDirection.TURN_RIGHT, Color.ORANGE);
    }

    public Car(AnimationScreen screen, Location location, TrafficController trafficController) {
        this.trafficController = trafficController;
        this.blockedRoadHelper = new BlockedRoadHelper(this.trafficController);
        this.location = location;
//        this.location = Location.BOT;
//        this.location = Location.TOP;
//        this.location = Location.LEFT;
//        this.location = Location.RIGHT;
        this.carLocationData = Spawner.getDataBasedOnLocation(this.location);
        this.driveDirection = Spawner.determineRandomDriveDirection();
//        this.driveDirection = DriveDirection.TURN_RIGHT;
//        this.driveDirection = DriveDirection.TURN_LEFT;
//        this.driveDirection = DriveDirection.STRAIGHT;
        this.color = colorMapping.get(this.driveDirection);
        this.screen = screen;
        this.radianRotation = 0;
        this.radianRotationForTranslation = pickRadianRotationForTranslation();
        this.radianRotationForTranslationBuffer = this.radianRotationForTranslation;
        this.radianRotationDx = driveDirection == DriveDirection.TURN_LEFT ? .01 : 0.02;
        Spawner.addCarToCollection(this);
    }

    public DriveDirection getDriveDirection() {
        return this.driveDirection;
    }

    public Location getLocation() {
        return this.location;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public void run() {
        while (!outOfMap()) {
            synchronized (this) {
                double x = carLocationData.getX();
                double y = carLocationData.getY();

                if(!this.blockedRoadHelper.checkIfCanRideThatRoad(this) && !enteredMiddle){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                if (checkIfMiddleOfCrossroad() && !checkIfRidePossible(this.location, this.driveDirection) && !enteredMiddle) {
                    this.blockedRoadHelper.blockCorrespondingRoad(this);
                    notifyAll();
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                Car laneOccupyingCar = this.trafficController.getOccupyingRoadCarByLocation(this.location);
                if (laneOccupyingCar == this) {
                    this.blockedRoadHelper.unblockCorrespondingRoad(this.location);
                    notifyAll();
                }

                calculateRadForTurn();
                screen.drawCar(x, y, carLocationData.getWidth(), carLocationData.getHeight(), color);

                double xForceApplied;
                double yForceApplied;
                if (turnFinished) {
                    xForceApplied = x + determineDxAfterTurn();
                    yForceApplied = y + determineDyAfterTurn();
                } else {
                    xForceApplied = x + determineXForceToBeApplied();
                    yForceApplied = y + determineYForceToBeApplied();
                }
                carLocationData.setX(xForceApplied);
                carLocationData.setY(yForceApplied);

//                System.out.println(this.carLocationData.getX());

            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Spawner.removeCarFromCollection(this);
    }


    private void calculateRadForTurn() {
        if (this.driveDirection == DriveDirection.TURN_RIGHT) {
            if ((checkIfMiddleOfCrossroad() || turnStarted) && !turnFinished) {
                if (prepareForTurn()) return;
                this.radianRotation += radianRotationDx;
                this.radianRotationForTranslation += radianRotationDx;
            }
        } else if (this.driveDirection == DriveDirection.TURN_LEFT) {
            if ((checkIfMiddleOfCrossroad() || turnStarted) && !turnFinished) {
                if (prepareForTurn()) return;
                this.radianRotation -= radianRotationDx;
                this.radianRotationForTranslation -= radianRotationDx;
            }
        } else {
            if (checkIfMiddleOfCrossroad()) {
                blockTrafficWhileMovingThroughMiddle(this.location, this.driveDirection);
            } else {
                unblockTrafficAfterMovingThroughMiddle(this.location, this.driveDirection);
            }
        }

    }


    private boolean prepareForTurn() {
        blockTrafficWhileMovingThroughMiddle(this.location, this.driveDirection);
        this.turnStarted = true;
        if (Math.abs(this.radianRotationForTranslation - this.radianRotationForTranslationBuffer) >= 1.57079632) {
            this.turnFinished = true;
            unblockTrafficAfterMovingThroughMiddle(this.location, this.driveDirection);
            return true;
        }
        return false;
    }

    private boolean checkIfMiddleOfCrossroad() {
        return this.carLocationData.getX() > 290 && this.carLocationData.getX() < 440
                && this.carLocationData.getY() > 180 && this.carLocationData.getY() < 325;
    }

    private double determineXForceToBeApplied() {
        if (this.driveDirection != DriveDirection.STRAIGHT && turnStarted && !turnFinished) {
            return carLocationData.getSpeed() * Math.cos(this.radianRotationForTranslation);
        } else {
            return carLocationData.getDx();
        }
    }

    private double determineYForceToBeApplied() {
        if (this.driveDirection != DriveDirection.STRAIGHT && turnStarted && !turnFinished) {
            return carLocationData.getSpeed() * Math.sin(this.radianRotationForTranslation);
        } else {
            return carLocationData.getDy();
        }
    }

    public double getRadianRotation() {
        return radianRotation;
    }

    private double pickRadianRotationForTranslation() {
        if (this.location == Location.BOT) {
            return -1.57079632;
        } else if (this.location == Location.TOP) {
            return 1.57079632;
        } else if (this.location == Location.LEFT) {
            return 0;
        } else {
            return 3.14159264;
        }
    }

    private double determineDyAfterTurn() {
        switch (this.location) {
            case BOT, TOP -> {
                return 0;
            }
            default -> {
                if (this.location == Location.RIGHT) {
                    if (this.driveDirection == DriveDirection.TURN_RIGHT) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    if (this.driveDirection == DriveDirection.TURN_RIGHT) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }
    }

    private double determineDxAfterTurn() {
        switch (this.location) {
            case RIGHT, LEFT -> {
                return 0;
            }
            default -> {
                if (this.location == Location.TOP) {
                    if (this.driveDirection == DriveDirection.TURN_RIGHT) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    if (this.driveDirection == DriveDirection.TURN_RIGHT) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }
    }

    private boolean outOfMap() {
        return this.carLocationData.getX() > 900 || this.carLocationData.getX() < -100
                || this.carLocationData.getY() > 700 || this.carLocationData.getY() < -100;
    }

//    public void setThreadReference(Thread threadReference){
//        this.threadReference = threadReference;
//    }

    private void blockTrafficWhileMovingThroughMiddle(Location l, DriveDirection d) {
        if (this.enteredMiddle) return;
        this.enteredMiddle = true;
        switch (d) {
            case TURN_LEFT -> {
                this.trafficController.blockForLeftTurn(l);
            }
            case TURN_RIGHT -> {
                this.trafficController.blockForTurnRight(l);
            }
            default -> {
                this.trafficController.blockForGoStraight(l);
            }
        }
        notifyAll();
    }

    private void unblockTrafficAfterMovingThroughMiddle(Location l, DriveDirection d) {
        if (this.exitedMiddle) return;
        this.exitedMiddle = true;
        switch (d) {
            case TURN_LEFT -> {
                this.trafficController.unblockForLeftTurn(l);
            }
            case TURN_RIGHT -> {
                this.trafficController.unblockForTurnRight(l);
            }
            default -> {
                this.trafficController.unblockForGoStraight(l);
            }
        }
        notifyAll();
    }

    private synchronized boolean checkIfRidePossible(Location location, DriveDirection driveDirection) {
        switch (location) {
            case LEFT -> {
                if (driveDirection == DriveDirection.TURN_LEFT) {
                    return this.trafficController.driveRightTurnLeftPossible;
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                    return this.trafficController.driveRightTurnRightPossible;
                } else {
                    return this.trafficController.driveRightGoStraightPossible;
                }
            }
            case BOT -> {
                if (driveDirection == DriveDirection.TURN_LEFT) {
                    return this.trafficController.driveTopTurnLeftPossible;
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                    return this.trafficController.driveTopTurnRightPossible;
                } else {
                    return this.trafficController.driveTopGoStraightPossible;
                }
            }
            case TOP -> {
                if (driveDirection == DriveDirection.TURN_LEFT) {
                    return this.trafficController.driveBotTurnLeftPossible;
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                    return this.trafficController.driveBotTurnRightPossible;
                } else {
                    return this.trafficController.driveBotGoStraightPossible;
                }
            }
            default -> { // RIGHT
                if (driveDirection == DriveDirection.TURN_LEFT) {
                    return this.trafficController.driveLeftTurnLeftPossible;
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                    return this.trafficController.driveLeftTurnRightPossible;
                } else {
                    return this.trafficController.driveLeftGoStraightPossible;
                }
            }
        }
    }

}
