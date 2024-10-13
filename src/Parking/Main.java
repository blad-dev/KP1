package Parking;

import java.time.LocalTime;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Parking parking = new Parking(5, 8,
                LocalTime.of(6, 0), LocalTime.of(20, 59), 3600);

        Thread autoParker = new Thread(new CarsAutoParker(parking), "Генератор машин");
        autoParker.start();
    }
}
