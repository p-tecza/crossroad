package approach.threads.traffic;

import approach.threads.Car;
import approach.threads.utils.Location;

import java.util.Queue;

public class BlockedRoadHelper {
    private final TrafficController trafficController;

    public BlockedRoadHelper(TrafficController trafficController) {
        this.trafficController = trafficController;
    }

    public synchronized void blockCorrespondingRoad(Car c) {
        Location location = c.getLocation();
        switch (location){
            case TOP -> {
                this.trafficController.topRoadBlockingCarsReference.add(c);
            }
            case BOT -> {
                this.trafficController.botRoadBlockingCarsReference.add(c);
            }
            case LEFT -> {
                this.trafficController.leftRoadBlockingCarsReference.add(c);
            }
            default -> { // RIGHT
                this.trafficController.rightRoadBlockingCarsReference.add(c);
            }
        }
    }

    public synchronized void unblockCorrespondingRoad(Location location) {
        switch (location){
            case TOP -> {
                this.trafficController.topRoadBlockingCarsReference.remove();
            }
            case BOT -> {
                this.trafficController.botRoadBlockingCarsReference.remove();
            }
            case LEFT -> {
                this.trafficController.leftRoadBlockingCarsReference.remove();
            }
            default -> { // RIGHT
                this.trafficController.rightRoadBlockingCarsReference.remove();
            }
        }
    }

    public synchronized boolean checkIfCanRideThatRoad(Car c){
        Location location = c.getLocation();
        Car blockingCar;
        Queue<Car> carQueueToAppendIfOccupied;
        switch (location){
            case TOP -> {
                blockingCar = this.trafficController.topRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.topRoadBlockingCarsReference;
            }
            case BOT -> {
                blockingCar = this.trafficController.botRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.botRoadBlockingCarsReference;
            }
            case LEFT -> {
                blockingCar = this.trafficController.leftRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.leftRoadBlockingCarsReference;
            }
            default -> { // RIGHT
                blockingCar = this.trafficController.rightRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.rightRoadBlockingCarsReference;
            }
        }

        if(blockingCar == null || blockingCar == c) return true;
        boolean res = !(Math.abs(blockingCar.carLocationData.getX() - c.carLocationData.getX()) < 75)
                || !(Math.abs(blockingCar.carLocationData.getY() - c.carLocationData.getY()) < 75);

        if(!res){
            carQueueToAppendIfOccupied.add(c);
        }

        System.out.println(this.trafficController.leftRoadBlockingCarsReference);

        return res;
    }

}
