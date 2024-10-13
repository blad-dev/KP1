package Parking;

import java.util.ArrayList;
import java.util.List;

public class CarsAutoParker implements Runnable{
    Parking parking;
    List<String> cars = new ArrayList<>();
    CarsAutoParker(Parking parking){
        this.parking = parking;
    }
    private void sleepFor(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
        System.err.printf("Потік \"%s\" почав своє виконання\n", Thread.currentThread().getName());
        while (true) {
            sleepFor(1000 + (int)(Math.random() * 301) - 50);

            for(int i = 0; i < cars.size(); ++i){
                if (Math.random() < 0.035) {
                    String id = cars.get(i);
                    Cars.createLeavingCar(parking, id).start();
                    sleepFor((int)(Math.random() * 100));
                    cars.remove(id);
                    --i;
                }
            }
            while (Math.random() < 0.5) {
                String randomId = String.valueOf((int)(100 * Math.random()));
                Cars.createParkingCar(parking, randomId, cars).start();
                sleepFor((int)(Math.random() * 100));
            }
        }
    }
}
