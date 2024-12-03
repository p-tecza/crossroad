package approach.executor;

import approach.threads.AnimationScreen;
import approach.threads.Car;
import approach.threads.traffic.BlockedRoadHelper;
import approach.threads.traffic.TrafficController;
import approach.threads.utils.Location;
import approach.threads.utils.Spawner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ExecutorFrameController {

    private JFrame frame;
    private final AnimationScreen screen;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public ExecutorFrameController() {
        frame = new JFrame("Crossroad");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.GREEN);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLayout(new GridLayout());

        screen = new AnimationScreen(Spawner.getCrossroadLock());

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {

            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent event) {

            }

            @Override
            public void windowDeiconified(WindowEvent event) {

            }
        });
        frame.add(screen);

        frame.pack();
        frame.setVisible(true);
        screen.setGraphics();
        TrafficController trafficController = new TrafficController();
        BlockedRoadHelper blockedRoadHelper = new BlockedRoadHelper(trafficController);
        spawnRandomCars(32, trafficController, blockedRoadHelper);
    }

    public void spawnRandomCars(int num, TrafficController trafficController, BlockedRoadHelper blockedRoadHelper) {
        new Thread(() -> {
            int n = num;
            int idIt = 0;

            while (n > 0) {
                Location loc = Spawner.getLocationFromRandomLocation();
                if (loc == null) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                Car c = new Car(screen, loc, trafficController, blockedRoadHelper, idIt++);
                screen.appendCar(c);

                threadPool.execute(c);
                n--;

                try {
                    Thread.sleep(num - 4 <= n ? 150 : 400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}



