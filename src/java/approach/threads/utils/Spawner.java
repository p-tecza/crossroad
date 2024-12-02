package approach.threads.utils;

import approach.threads.AnimationScreen;
import approach.threads.Car;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Spawner {

    private static Map<Integer, Car> instantiatedCars = new HashMap<>();
    private static final int carWidth = 50;
    private static final int carHeight = 24;
    public static Map<Location, CarLocationData> mapping = new HashMap<>();

//    private static final Object carObjectsLock = new Object();
    private static final Object crossroadLock = new Object();
    private static final Object roadLock = new Object();

    static {
        mapping.put(Location.LEFT, new CarLocationData(0, 295, 1, 0, carWidth, carHeight));
        mapping.put(Location.RIGHT, new CarLocationData(736, 241, -1, 0, carWidth, carHeight));
        mapping.put(Location.TOP, new CarLocationData(351, 0, 0, 1, carHeight, carWidth));
        mapping.put(Location.BOT, new CarLocationData(405, 514, 0, -1, carHeight, carWidth));
    }

//    public static Object getCarObjectsLock() {
//        return carObjectsLock;
//    }

    public static Object getCrossroadLock() {
        return crossroadLock;
    }

    public static Object getRoadLock(){
        return roadLock;
    }

    public static CarLocationData getDataBasedOnLocation(Location l) {
        CarLocationData toCopyFrom = mapping.get(l);
        return new CarLocationData(
                toCopyFrom.getX(),
                toCopyFrom.getY(),
                toCopyFrom.getDx(),
                toCopyFrom.getDy(),
                toCopyFrom.getWidth(),
                toCopyFrom.getHeight()
        );
    }

    public static Location getLocationFromRandomLocation() {
        List<Location> availableLocations = tryToFindRandomLocationForCar();
        if (availableLocations.isEmpty()) return null;
        return availableLocations.get(new Random().nextInt(availableLocations.size()));
    }

    public static DriveDirection determineRandomDriveDirection() {
        Random r = new Random();
        switch (r.nextInt(3)) {
            case 0 -> {
                return DriveDirection.TURN_LEFT;
            }
            case 1 -> {
                return DriveDirection.TURN_RIGHT;
            }
            default -> {
                return DriveDirection.STRAIGHT;
            }
        }
    }

    public static void addCarToCollection(Car c) {
        synchronized (crossroadLock) {
            instantiatedCars.put(c.hashCode(), c);
        }

    }

    public static void removeCarFromCollection(Car c) {
        synchronized (crossroadLock) {
            instantiatedCars.remove(c.hashCode());
//            System.out.println("LEFTOVER CARS SIZE: "+ instantiatedCars.size());
        }
    }

    private static List<Location> tryToFindRandomLocationForCar() {
        List<Location> availableLocations = new ArrayList<>();
        for (Location locationEntry : mapping.keySet()) {
            CarLocationData locationData = mapping.get(locationEntry);
            final boolean[] locationOk = {true};
            synchronized (crossroadLock) {
                instantiatedCars.values().forEach(c -> {
                            if (c.getLocation() == locationEntry) {
                                if (Math.abs(locationData.getX() - c.carLocationData.getX()) < 75 &&
                                        Math.abs(locationData.getY() - c.carLocationData.getY()) < 75) {
                                    locationOk[0] = false;
//                            System.out.println(Math.abs(locationData.getX() - c.carLocationData.getX()));
//                            System.out.println(Math.abs(locationData.getY() - c.carLocationData.getY()));
//                                    System.out.println("LOC DATA:" + locationData.getX());
//                                    System.out.println("CAR DATA:" + c.carLocationData.getX());
//                            System.out.println("NIE MA MIEJSCA");
                                }
                            }
                        }
                );
            }
            if (locationOk[0]) availableLocations.add(locationEntry);
        }
//        System.out.println("Ilosc dostepnych: " + availableLocations);
        return availableLocations;
    }

//    public static CarLocationData spawnInLocation

}
