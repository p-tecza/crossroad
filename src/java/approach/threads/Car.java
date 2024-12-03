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

    private final int id;
    public CarLocationData carLocationData;
    private final Location location;
    private final Color color;
    private final AnimationScreen screen;
    private final DriveDirection driveDirection;
    private double radianRotation;
    private double radianRotationForTranslation;
    private final double radianRotationDx;
    private boolean turnStarted = false;
    private boolean turnFinished = false;
    private boolean enteredMiddle = false;
    private boolean exitedMiddle = false;
    private final double radianRotationForTranslationBuffer;
    private final TrafficController trafficController;
    private final BlockedRoadHelper blockedRoadHelper;
    private final Object crossroadLock = Spawner.getCrossroadLock();
    private static final Map<DriveDirection, Color> colorMapping = new HashMap<>();

    static {
        colorMapping.put(DriveDirection.TURN_LEFT, Color.RED);
        colorMapping.put(DriveDirection.STRAIGHT, Color.BLUE);
        colorMapping.put(DriveDirection.TURN_RIGHT, Color.ORANGE);
    }

    public Car(AnimationScreen screen, Location location, TrafficController trafficController,
               BlockedRoadHelper blockedRoadHelper, int id) {
        this.id = id;
        this.trafficController = trafficController;
        this.blockedRoadHelper = blockedRoadHelper;
        this.location = location;
        this.carLocationData = Spawner.getDataBasedOnLocation(this.location);
        this.driveDirection = Spawner.determineRandomDriveDirection();
        this.color = colorMapping.get(this.driveDirection);
        this.screen = screen;
        this.radianRotation = 0;
        this.radianRotationForTranslation = pickRadianRotationForTranslation();
        this.radianRotationForTranslationBuffer = this.radianRotationForTranslation;
        this.radianRotationDx = driveDirection == DriveDirection.TURN_LEFT ? .01 : 0.02;
        Spawner.addCarToCollection(this);
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
            synchronized (crossroadLock) {
                double x = carLocationData.getX();
                double y = carLocationData.getY();

                if (!this.blockedRoadHelper.checkIfCanRideThatRoad(this) && !enteredMiddle) {
                    try {
                        crossroadLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                if (checkIfMiddleOfCrossroad() && !checkIfRidePossible(this.location, this.driveDirection) && !enteredMiddle) {
                    if (this.blockedRoadHelper.lockedDoesNotAlreadyContain(this.getId())) {
                        this.blockedRoadHelper.blockCorrespondingRoad(this);
                    }

                    crossroadLock.notifyAll();
                    try {
                        crossroadLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
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
            } else if (this.enteredMiddle) {
                unblockTrafficAfterMovingThroughMiddle(this.location, this.driveDirection);
            }
        }

    }


    private boolean prepareForTurn() {
        if (!this.turnFinished) blockTrafficWhileMovingThroughMiddle(this.location, this.driveDirection);
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

    private void blockTrafficWhileMovingThroughMiddle(Location l, DriveDirection d) {
        if (this.enteredMiddle) return;
        this.enteredMiddle = true;
        switch (d) {
            case TURN_LEFT -> {
                this.trafficController.blockForLeftTurn(l, this);
            }
            case TURN_RIGHT -> {
                this.trafficController.blockForTurnRight(l, this);
            }
            default -> {
                this.trafficController.blockForGoStraight(l, this);
            }
        }
    }

    private void unblockTrafficAfterMovingThroughMiddle(Location l, DriveDirection d) {
        if (this.exitedMiddle) return;
        this.exitedMiddle = true;
        switch (d) {
            case TURN_LEFT -> {
                this.trafficController.unblockForLeftTurn(l, this);
            }
            case TURN_RIGHT -> {
                this.trafficController.unblockForTurnRight(l, this);
            }
            default -> {
                this.trafficController.unblockForGoStraight(l, this);
            }
        }
    }

    private boolean checkIfRidePossible(Location location, DriveDirection driveDirection) {

        synchronized (crossroadLock) {
            boolean res = false;
            switch (location) {
                case LEFT -> {
                    if (driveDirection == DriveDirection.TURN_LEFT) {
                        res = this.trafficController.ifEmptyDriveRightTurnLeftPossible.isEmpty();
                    } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                        res = this.trafficController.ifEmptyDriveRightTurnRightPossible.isEmpty();
                    } else {
                        res = this.trafficController.ifEmptyDriveRightGoStraightPossible.isEmpty();
                    }
                }
                case BOT -> {
                    if (driveDirection == DriveDirection.TURN_LEFT) {
                        res = this.trafficController.ifEmptyDriveTopTurnLeftPossible.isEmpty();
                    } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                        res = this.trafficController.ifEmptyDriveTopTurnRightPossible.isEmpty();
                    } else {
                        res = this.trafficController.ifEmptyDriveTopGoStraightPossible.isEmpty();
                    }
                }
                case TOP -> {
                    if (driveDirection == DriveDirection.TURN_LEFT) {
                        res = this.trafficController.ifEmptyDriveBotTurnLeftPossible.isEmpty();
                    } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                        res = this.trafficController.ifEmptyDriveBotTurnRightPossible.isEmpty();
                    } else {
                        res = this.trafficController.ifEmptyDriveBotGoStraightPossible.isEmpty();
                    }
                }
                default -> { // RIGHT
                    if (driveDirection == DriveDirection.TURN_LEFT) {
                        res = this.trafficController.ifEmptyDriveLeftTurnLeftPossible.isEmpty();
                    } else if (driveDirection == DriveDirection.TURN_RIGHT) {
                        res = this.trafficController.ifEmptyDriveLeftTurnRightPossible.isEmpty();
                    } else {
                        res = this.trafficController.ifEmptyDriveLeftGoStraightPossible.isEmpty();
                    }
                }
            }
            if (res) {
                this.blockedRoadHelper.unblockCorrespondingRoad(this);
            }
            return res;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Car car = (Car) obj;
        return car.id == this.id;
    }

    public int getId() {
        return this.id;
    }

}
