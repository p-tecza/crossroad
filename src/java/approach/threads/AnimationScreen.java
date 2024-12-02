package approach.threads;

import approach.threads.utils.CarLocationData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;

public class AnimationScreen extends JPanel {

    private Graphics2D graphics;
    private final Rectangle2D.Double carRectangle;
    private final List<Car> cars;

    private final Object crossroadLock;
    public AnimationScreen(Object crossroadLock) {
        carRectangle = new Rectangle2D.Double();
        cars = new ArrayList<>();
        this.crossroadLock = crossroadLock;
    }

    public void setGraphics() {
        VolatileImage offscreenImage = createVolatileImage(getWidth(), getHeight());
        graphics = (Graphics2D) offscreenImage.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawCrossroad(null);
    }

    private void drawCrossroad(Graphics2D graphics){
        if(graphics == null) graphics = this.graphics;
        graphics.setColor(Color.GRAY);
        graphics.fillRect(0,230,800,100); // 230 - 276 -> 254 // 330 - 12 = 318 - 23 = 295
        graphics.fillRect(340,0,100,600);
        graphics.setColor(Color.WHITE);
        for(int i=0; i<16; i++){
            if(i != 7 && i != 8){
                graphics.fillRect(50*i,276,25,8);
            }
            graphics.fillRect(386,50*i,8,25);
        }
}

    public synchronized void drawCar(double x, double y, double width, double height, Color color){
        graphics.setColor(color);
        carRectangle.setFrame(x, y, width, height);
        graphics.fill(carRectangle);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        this.setBackground(Color.GREEN);
        drawCrossroad(g2d);
        synchronized (crossroadLock) {
            for (Car c : cars) {
                CarLocationData car = c.carLocationData;
                g2d.setColor(c.getColor());
                if(c.getRadianRotation() != 0){
                    AffineTransform originalTransform = g2d.getTransform();
                    // Przesunięcie układu współrzędnych na pozycję samochodu
                    g2d.translate(car.getX() + car.getWidth() / 2.0, car.getY() + car.getHeight() / 2.0);
                    g2d.rotate(c.getRadianRotation());

//                    g2d.fill(new Rectangle2D.Double(car.getX(), car.getY(), car.getWidth(), car.getHeight()));
                    g2d.fillRect(-car.getWidth() / 2, -car.getHeight() / 2, car.getWidth(), car.getHeight());

                    g2d.setTransform(originalTransform);
                }else{
                    g2d.fill(new Rectangle2D.Double(car.getX(), car.getY(), car.getWidth(), car.getHeight()));
                }

            }
        }
    }
    public void appendCar(Car c){
        this.cars.add(c);
    }

}