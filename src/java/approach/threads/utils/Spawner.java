package approach.threads.utils;

import java.util.*;

public class Spawner {

    /*
        norm = 3.;
        double angle = random() * 2 * PI;
        dx = norm * cos(angle);
        dy = norm * sin(angle);
     */
    private static final int carWidth = 50;
    private static final int carHeight = 24;
    public static Map<Location, CarLocationData> mapping = new HashMap<>();
    static{
        mapping.put(Location.LEFT, new CarLocationData(0, 295,1,0, carWidth, carHeight));
        mapping.put(Location.RIGHT, new CarLocationData(736, 241,-1,0, carWidth, carHeight));
        mapping.put(Location.TOP, new CarLocationData(351,0,0, 1, carHeight, carWidth));
        mapping.put(Location.BOT, new CarLocationData(405,514,0, -1, carHeight, carWidth));
    }

    public static CarLocationData getDataBasedOnLocation(Location l){
        return mapping.get(l);
    }

    public static Location getLocationFromRandomLocation(){
        return new ArrayList<>(mapping.keySet()).get(new Random().nextInt(4));
    }

    public static DriveDirection determineRandomDriveDirection(){
        Random r = new Random();
        switch(r.nextInt(3)){
            case 0 -> { return DriveDirection.TURN_LEFT; }
            case 1 -> { return DriveDirection.TURN_RIGHT; }
            default -> { return DriveDirection.STRAIGHT; }
        }
    }
}
