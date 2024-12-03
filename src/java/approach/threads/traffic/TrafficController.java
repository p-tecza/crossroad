package approach.threads.traffic;

import approach.threads.Car;
import approach.threads.utils.Location;
import approach.threads.utils.Spawner;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

public class TrafficController {
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveLeftTurnLeftPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveLeftTurnRightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveLeftGoStraightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveRightTurnLeftPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveRightTurnRightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveRightGoStraightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveBotTurnLeftPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveBotTurnRightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveBotGoStraightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveTopTurnLeftPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveTopTurnRightPossible = new ConcurrentHashMap<>();
    public volatile ConcurrentMap<Integer, Car> ifEmptyDriveTopGoStraightPossible = new ConcurrentHashMap<>();

    volatile public ConcurrentLinkedDeque<Car> leftRoadBlockingCarsReference;
    volatile public ConcurrentLinkedDeque<Car> rightRoadBlockingCarsReference;
    volatile public ConcurrentLinkedDeque<Car> botRoadBlockingCarsReference;
    volatile public ConcurrentLinkedDeque<Car> topRoadBlockingCarsReference;

    private final Object crossroadLock = Spawner.getCrossroadLock();

    public TrafficController() {
        this.topRoadBlockingCarsReference = new ConcurrentLinkedDeque<>();
        this.botRoadBlockingCarsReference = new ConcurrentLinkedDeque<>();
        this.leftRoadBlockingCarsReference = new ConcurrentLinkedDeque<>();
        this.rightRoadBlockingCarsReference = new ConcurrentLinkedDeque<>();
    }

    public void blockForLeftTurn(Location location, Car c) {
        synchronized (crossroadLock) {
            System.out.println(location + " BLOCKING path for left turn");
            switch (location) {
                case BOT -> {
                    this.ifEmptyDriveBotGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
//                    this.ifEmptyDriveLeftTurnRightPossible.putIfAbsent(c.getId(), c);
                }
                case TOP -> {
                    this.ifEmptyDriveTopGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
//                    this.ifEmptyDriveRightTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
                case LEFT -> {
                    this.ifEmptyDriveBotGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
//                    this.ifEmptyDriveTopTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnRightPossible.putIfAbsent(c.getId(), c);
                }
                default -> { // RIGHT
                    this.ifEmptyDriveBotGoStraightPossible.putIfAbsent(c.getId(), c);
//                    this.ifEmptyDriveBotTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public void unblockForLeftTurn(Location location, Car c) {
        synchronized (crossroadLock) {
            System.out.println(location + " UNBLOCKING path for left turn");
            switch (location) {
                case BOT -> {
                    this.ifEmptyDriveBotGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveBotTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveLeftGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
//                    this.ifEmptyDriveLeftTurnRightPossible.remove(c.getId());
                }
                case TOP -> {
                    this.ifEmptyDriveTopGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
//                    this.ifEmptyDriveRightTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
                }
                case LEFT -> {
                    this.ifEmptyDriveBotGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveTopGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
//                    this.ifEmptyDriveTopTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnRightPossible.remove(c.getId());
                }
                default -> { // RIGHT
                    this.ifEmptyDriveBotGoStraightPossible.remove(c.getId());
//                    this.ifEmptyDriveBotTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveTopGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public void blockForGoStraight(Location location, Car c) {
        synchronized (crossroadLock) {
            switch (location) {
                case BOT -> {
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
                case TOP -> {
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
                case LEFT -> {
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnRightPossible.putIfAbsent(c.getId(), c);
                }
                default -> { // RIGHT
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveBotTurnRightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public void unblockForGoStraight(Location location, Car c) {
        synchronized (crossroadLock) {
            switch (location) {
                case BOT -> {
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
                }
                case TOP -> {
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
                }
                case LEFT -> {
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveBotGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveTopGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnRightPossible.remove(c.getId());
                }
                default -> { // RIGHT
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveBotGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveBotTurnRightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveTopGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public void blockForTurnRight(Location location, Car c) {
        synchronized (crossroadLock) {
            switch (location) {
                case BOT -> {
                    this.ifEmptyDriveBotTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightGoStraightPossible.putIfAbsent(c.getId(), c);
                }
                case TOP -> {
                    this.ifEmptyDriveTopTurnLeftPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftGoStraightPossible.putIfAbsent(c.getId(), c);
                }
                case LEFT -> {
                    this.ifEmptyDriveBotGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveLeftTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
                default -> { // RIGHT
                    this.ifEmptyDriveTopGoStraightPossible.putIfAbsent(c.getId(), c);
                    this.ifEmptyDriveRightTurnLeftPossible.putIfAbsent(c.getId(), c);
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public void unblockForTurnRight(Location location, Car c) {
        synchronized (crossroadLock) {
            switch (location) {
                case BOT -> {
                    this.ifEmptyDriveBotTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveRightGoStraightPossible.remove(c.getId());
                }
                case TOP -> {
                    this.ifEmptyDriveTopTurnLeftPossible.remove(c.getId());
                    this.ifEmptyDriveLeftGoStraightPossible.remove(c.getId());
                }
                case LEFT -> {
                    this.ifEmptyDriveBotGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveLeftTurnLeftPossible.remove(c.getId());
                }
                default -> { // RIGHT
                    this.ifEmptyDriveTopGoStraightPossible.remove(c.getId());
                    this.ifEmptyDriveRightTurnLeftPossible.remove(c.getId());
                }
            }
            crossroadLock.notifyAll();
        }
    }

    public Car getOccupyingRoadCarByLocation(Location l) {
        synchronized (crossroadLock) {
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
}
