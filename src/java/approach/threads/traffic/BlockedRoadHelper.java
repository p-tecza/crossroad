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
//        System.out.println(c.getColor() + " BLOCKING ROAD");
        Location location = c.getLocation();
        switch (location){
            case TOP -> {
                if(!this.trafficController.topRoadBlockingCarsReference.contains(c)){
                    this.trafficController.topRoadBlockingCarsReference.add(c);
                }
            }
            case BOT -> {
                if(!this.trafficController.botRoadBlockingCarsReference.contains(c)){
                    this.trafficController.botRoadBlockingCarsReference.add(c);
                }
            }
            case LEFT -> {
                if(!this.trafficController.leftRoadBlockingCarsReference.contains(c)){
                    this.trafficController.leftRoadBlockingCarsReference.add(c);
                }
            }
            default -> { // RIGHT
                if(!this.trafficController.rightRoadBlockingCarsReference.contains(c)){
                    this.trafficController.rightRoadBlockingCarsReference.add(c);
                }
            }
        }
    }

    public synchronized void unblockCorrespondingRoad(Location location) {
//        System.out.println(location + " UNBLOCKING ROAD" );
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
        Car firstBlockingCar, lastBlockingCar;
        Queue<Car> carQueueToAppendIfOccupied;
        switch (location){
            case TOP -> {
                firstBlockingCar = this.trafficController.topRoadBlockingCarsReference.peek();
                lastBlockingCar = this.trafficController.topRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.topRoadBlockingCarsReference;
            }
            case BOT -> {
                firstBlockingCar = this.trafficController.botRoadBlockingCarsReference.peek();
                lastBlockingCar = this.trafficController.botRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.botRoadBlockingCarsReference;
            }
            case LEFT -> {
                firstBlockingCar = this.trafficController.leftRoadBlockingCarsReference.peek();
                lastBlockingCar = this.trafficController.leftRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.leftRoadBlockingCarsReference;
//                System.out.println("LEFT ROAD BLOCK size: "+ this.trafficController.leftRoadBlockingCarsReference.size());
            }
            default -> { // RIGHT
                firstBlockingCar = this.trafficController.rightRoadBlockingCarsReference.peek();
                lastBlockingCar = this.trafficController.rightRoadBlockingCarsReference.peekLast();
                carQueueToAppendIfOccupied = this.trafficController.rightRoadBlockingCarsReference;
            }
        }

        if(firstBlockingCar == null || lastBlockingCar == null || firstBlockingCar.equals(c)) return true;
        boolean res = !(Math.abs(lastBlockingCar.carLocationData.getX() - c.carLocationData.getX()) < 75)
                || !(Math.abs(lastBlockingCar.carLocationData.getY() - c.carLocationData.getY()) < 75);

        if(!res && !carQueueToAppendIfOccupied.contains(c)){
            carQueueToAppendIfOccupied.add(c);
        }

//        System.out.println("LEFT ROAD BLOCK size: "+ this.trafficController.leftRoadBlockingCarsReference.size());

        return res;
    }

}
