package approach.threads;

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
    private double radianRotationForTranslationBuffer;



    private static final Map<DriveDirection, Color> colorMapping = new HashMap<>();

    static {
        colorMapping.put(DriveDirection.TURN_LEFT, Color.RED);
        colorMapping.put(DriveDirection.STRAIGHT, Color.BLUE);
        colorMapping.put(DriveDirection.TURN_RIGHT, Color.ORANGE);
    }

    public Car(AnimationScreen screen) {
//        this.location = Spawner.getLocationFromRandomLocation();
//        this.location = Location.BOT;
//        this.location = Location.TOP;
//        this.location = Location.LEFT;
        this.location = Location.RIGHT;
        this.carLocationData = Spawner.getDataBasedOnLocation(this.location);
//        this.driveDirection = Spawner.determineRandomDriveDirection();
        this.driveDirection = DriveDirection.TURN_RIGHT;
//        this.driveDirection = DriveDirection.TURN_LEFT;
//        this.driveDirection = DriveDirection.STRAIGHT;
        this.color = colorMapping.get(this.driveDirection);
        this.screen = screen;
        this.radianRotation = 0;
        this.radianRotationForTranslation = pickRadianRotationForTranslation();
        this.radianRotationForTranslationBuffer = this.radianRotationForTranslation;
        this.radianRotationDx = driveDirection == DriveDirection.TURN_LEFT ? .01 : 0.02;
    }

    public DriveDirection getDriveDirection() {
        return this.driveDirection;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                double x = carLocationData.getX();
                double y = carLocationData.getY();

                calculateRadForTurn();

                screen.drawCar(x, y, carLocationData.getWidth(), carLocationData.getHeight(), color);
//                System.out.println(this.hashCode() + " DZIALAM SOBIE");

                double xForceApplied = x + determineXForceToBeApplied();
                double yForceForceApplied = y + determineYForceToBeApplied();

                carLocationData.setX(xForceApplied);
                carLocationData.setY(yForceForceApplied);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void calculateRadForTurn() {

        System.out.println("X: " + this.carLocationData.getX() + " Y: " + this.carLocationData.getY());

        if (this.driveDirection == DriveDirection.TURN_RIGHT) {
            if ((checkIfMiddleOfCrossroad() || turnStarted) && !turnFinished) {
                this.turnStarted = true;
                System.out.println("POWINIENEM SKRECAC W PRAWO");
                if(Math.abs(this.radianRotationForTranslation - this.radianRotationForTranslationBuffer) >= 1.57079632){
                    this.turnFinished = true;
                    return;
                }
                this.radianRotation += radianRotationDx;
                this.radianRotationForTranslation += radianRotationDx;
//                if(this.radianRotation > 1.5708) this.radianRotation = 1.5708;
            }
        } else if (this.driveDirection == DriveDirection.TURN_LEFT) {
            if ((checkIfMiddleOfCrossroad() || turnStarted) && !turnFinished) {
                this.turnStarted = true;
                System.out.println("POWINIENEM SKRECAC W LEWO");
                if(Math.abs(this.radianRotationForTranslation - this.radianRotationForTranslationBuffer) >= 1.57079632){
                    this.turnFinished = true;
                    return;
                }
                this.radianRotation -= radianRotationDx;
                this.radianRotationForTranslation -= radianRotationDx;
//                if(this.radianRotation > 1.5708) this.radianRotation = 1.5708;
            }
        }
    }

    private boolean checkIfMiddleOfCrossroad() {
        return this.carLocationData.getX() > 290 && this.carLocationData.getX() < 460
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

    private double pickRadianRotationForTranslation(){
        if(this.location == Location.BOT){
            return -1.57079632;
        }else if(this.location == Location.TOP){
            return 1.57079632;
        }else if(this.location == Location.LEFT){
            return 0;
        }else{
            return 3.14159264;
        }
    }

}
