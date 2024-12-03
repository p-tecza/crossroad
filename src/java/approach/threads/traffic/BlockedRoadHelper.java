package approach.threads.traffic;

import approach.threads.Car;
import approach.threads.utils.Location;
import approach.threads.utils.Spawner;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockedRoadHelper {
    private final TrafficController trafficController;

    private final Object crossroadLock = Spawner.getCrossroadLock();

    public BlockedRoadHelper(TrafficController trafficController) {
        this.trafficController = trafficController;
    }

    private final Set<Integer> alreadyUnblockedCars = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Set<Integer> alreadyBlockedCars = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void blockCorrespondingRoad(Car c) {
        synchronized (crossroadLock) {
            if (alreadyBlockedCars.contains(c.getId())) return;
            alreadyBlockedCars.add(c.getId());

            Location location = c.getLocation();
            switch (location) {
                case TOP -> {
                    if (!this.trafficController.topRoadBlockingCarsReference.contains(c)) {
                        this.trafficController.topRoadBlockingCarsReference.add(c);
                    }
                }
                case BOT -> {
                    if (!this.trafficController.botRoadBlockingCarsReference.contains(c)) {
                        this.trafficController.botRoadBlockingCarsReference.add(c);
                    }
                }
                case LEFT -> {
                    if (!this.trafficController.leftRoadBlockingCarsReference.contains(c)) {
                        this.trafficController.leftRoadBlockingCarsReference.add(c);
                    }
                }
                default -> { // RIGHT
                    if (!this.trafficController.rightRoadBlockingCarsReference.contains(c)) {
                        this.trafficController.rightRoadBlockingCarsReference.add(c);
                    }
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public void unblockCorrespondingRoad(Car c) {
        synchronized (crossroadLock) {
            if (alreadyUnblockedCars.contains(c.getId()) || !alreadyBlockedCars.contains(c.getId())) return;
            alreadyUnblockedCars.add(c.getId());

            Location location = c.getLocation();
            switch (location) {
                case TOP -> {
                    if (this.trafficController.topRoadBlockingCarsReference.isEmpty()) return;
                    this.trafficController.topRoadBlockingCarsReference.remove(c);
                }
                case BOT -> {
                    if (this.trafficController.botRoadBlockingCarsReference.isEmpty()) return;
                    this.trafficController.botRoadBlockingCarsReference.remove(c);
                }
                case LEFT -> {
                    if (this.trafficController.leftRoadBlockingCarsReference.isEmpty()) return;
                    this.trafficController.leftRoadBlockingCarsReference.remove(c);
                }
                default -> { // RIGHT
                    if (this.trafficController.rightRoadBlockingCarsReference.isEmpty()) return;
                    this.trafficController.rightRoadBlockingCarsReference.remove(c);
                }
            }
        }
        crossroadLock.notifyAll();
    }

    public boolean checkIfCanRideThatRoad(Car c) {
        synchronized (crossroadLock) {
            Location location = c.getLocation();
            Car firstBlockingCar, lastBlockingCar;
            Queue<Car> carQueueToAppendIfOccupied;
            switch (location) {
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
                }
                default -> { // RIGHT
                    firstBlockingCar = this.trafficController.rightRoadBlockingCarsReference.peek();
                    lastBlockingCar = this.trafficController.rightRoadBlockingCarsReference.peekLast();
                    carQueueToAppendIfOccupied = this.trafficController.rightRoadBlockingCarsReference;
                }
            }

            if (firstBlockingCar == null || lastBlockingCar == null || firstBlockingCar.equals(c)) return true;
            boolean res = false;
            if (!carQueueToAppendIfOccupied.contains(c)) {
                res = (Math.abs(lastBlockingCar.carLocationData.getX() - c.carLocationData.getX()) > 75
                        || Math.abs(lastBlockingCar.carLocationData.getY() - c.carLocationData.getY()) > 75);
                if (!res) {
                    carQueueToAppendIfOccupied.add(c);
                }
            } else {
                Car nextCarInQueue = null;
                for (Car itCar : carQueueToAppendIfOccupied) {
                    if (itCar == c) {
                        break;
                    }
                    nextCarInQueue = itCar;
                }
                if (nextCarInQueue == null) {
                    res = true;
                } else {
                    res = (Math.abs(nextCarInQueue.carLocationData.getX() - c.carLocationData.getX()) > 75
                            || Math.abs(nextCarInQueue.carLocationData.getY() - c.carLocationData.getY()) > 75);
                }
            }

            return res;
        }
    }


    public boolean lockedDoesNotAlreadyContain(int id) {
        return !this.alreadyBlockedCars.contains(id);
    }

}
