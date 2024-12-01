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

    private final Object crossroadLock = Spawner.getCrossroadLock();
    private final Object roadLock = Spawner.getRoadLock();
    private final static Object deadLockResolver = new Object();

    //    private Thread threadReference;
    private static final Map<DriveDirection, Color> colorMapping = new HashMap<>();

    static {
        colorMapping.put(DriveDirection.TURN_LEFT, Color.RED);
        colorMapping.put(DriveDirection.STRAIGHT, Color.BLUE);
        colorMapping.put(DriveDirection.TURN_RIGHT, Color.ORANGE);
    }

    public Car(AnimationScreen screen, Location location, TrafficController trafficController, int id) {
        this.id = id;
        this.trafficController = trafficController;
        this.blockedRoadHelper = new BlockedRoadHelper(this.trafficController);
        this.location = location;
        this.carLocationData = Spawner.getDataBasedOnLocation(this.location);
        this.driveDirection = Spawner.determineRandomDriveDirection();

//        this.carLocationData = Spawner.getDataBasedOnLocation(location);
//        this.driveDirection = deleteAfter;

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
            synchronized (crossroadLock) {
                double x = carLocationData.getX();
                double y = carLocationData.getY();

                if (!this.blockedRoadHelper.checkIfCanRideThatRoad(this) && !enteredMiddle) {
                    try {
                        System.out.println(this.id + ". CZEKAM W KORKU");
                        crossroadLock.wait();
//                        wait();
//                        Thread.sleep(100);
                        System.out.println(this.id + ". JEDE W KORKU");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                if (checkIfMiddleOfCrossroad() && !checkIfRidePossible(this.location, this.driveDirection) && !enteredMiddle) {
                    this.blockedRoadHelper.blockCorrespondingRoad(this);
                    crossroadLock.notifyAll();
//                    notifyAll();
                    try {
                        System.out.println(this.id + ". CZEKAM NA ZAKRET");
                        crossroadLock.wait();
//                        wait();
//                        Thread.sleep(100);
                        System.out.println(this.id + ". ZAKRECAM");
                    } catch (InterruptedException e) {
//                        System.out.println("GOT NOTIFIED");
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                Car laneOccupyingCar = this.trafficController.getOccupyingRoadCarByLocation(this.location);
                calculateRadForTurn();
                if (laneOccupyingCar == this && enteredMiddle) {
                    this.blockedRoadHelper.unblockCorrespondingRoad(this.location);
//                    notifyAll();
                    crossroadLock.notifyAll();
                }


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
            } else if(this.enteredMiddle){
                unblockTrafficAfterMovingThroughMiddle(this.location, this.driveDirection);
            }
        }

    }


    private boolean prepareForTurn() {
        if(!this.turnFinished) blockTrafficWhileMovingThroughMiddle(this.location, this.driveDirection);
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
        synchronized (deadLockResolver) {
            if (this.enteredMiddle) return;
            System.out.println("BLOCKING: "+l +":"+d);
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
    }

    private void unblockTrafficAfterMovingThroughMiddle(Location l, DriveDirection d) {
        synchronized (deadLockResolver) {
            if (this.exitedMiddle) return;
            System.out.println("UNBLOCKING: "+l +":"+d);
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
    }

    private synchronized boolean checkIfRidePossible(Location location, DriveDirection driveDirection) {


//        System.out.println("LOCATION: " + location + " DD: " + driveDirection);
        switch (location) {
            case LEFT -> {
                if (driveDirection == DriveDirection.TURN_LEFT) {
//                    System.out.println("LL:" + this.trafficController.ifEmptyDriveRightTurnLeftPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveRightTurnLeftPossible.isEmpty();
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
//                    System.out.println("LR:" + this.trafficController.ifEmptyDriveRightTurnRightPossible.isEmpty());

//                    if (!this.trafficController.ifEmptyDriveRightTurnRightPossible.isEmpty()) {
//                        this.trafficController.ifEmptyDriveRightTurnRightPossible.keySet().stream().forEach(k ->
//                                System.out.println(this.trafficController.ifEmptyDriveRightTurnRightPossible.get(k).getId())
//                        );
//                    }

                    return this.trafficController.ifEmptyDriveRightTurnRightPossible.isEmpty();
                } else {
//                    System.out.println("LF:" + this.trafficController.ifEmptyDriveRightGoStraightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveRightGoStraightPossible.isEmpty();
                }
            }
            case BOT -> {
                if (driveDirection == DriveDirection.TURN_LEFT) {
//                    System.out.println("BL:" + this.trafficController.ifEmptyDriveTopTurnLeftPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveTopTurnLeftPossible.isEmpty();
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
//                    System.out.println("BR:" + this.trafficController.ifEmptyDriveTopTurnRightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveTopTurnRightPossible.isEmpty();
                } else {
//                    System.out.println("BF:" + this.trafficController.ifEmptyDriveTopGoStraightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveTopGoStraightPossible.isEmpty();
                }
            }
            case TOP -> {
                if (driveDirection == DriveDirection.TURN_LEFT) {
//                    System.out.println("TL:" + this.trafficController.ifEmptyDriveBotTurnLeftPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveBotTurnLeftPossible.isEmpty();
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
//                    System.out.println("TR:" + this.trafficController.ifEmptyDriveBotTurnRightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveBotTurnRightPossible.isEmpty();
                } else {
//                    System.out.println("TF:" + this.trafficController.ifEmptyDriveBotGoStraightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveBotGoStraightPossible.isEmpty();
                }
            }
            default -> { // RIGHT
                if (driveDirection == DriveDirection.TURN_LEFT) {
//                    System.out.println("RL:" + this.trafficController.ifEmptyDriveLeftTurnLeftPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveLeftTurnLeftPossible.isEmpty();
                } else if (driveDirection == DriveDirection.TURN_RIGHT) {
//                    System.out.println("RR:" + this.trafficController.ifEmptyDriveLeftTurnRightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveLeftTurnRightPossible.isEmpty();
                } else {
//                    System.out.println("RF:" + this.trafficController.ifEmptyDriveLeftGoStraightPossible.isEmpty());
                    return this.trafficController.ifEmptyDriveLeftGoStraightPossible.isEmpty();
                }
            }
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
