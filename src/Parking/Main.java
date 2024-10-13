package Parking;

import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {
        Parking parking = new Parking(5, 8,
                LocalTime.of(6, 0), LocalTime.of(20, 59), 3600);

        Thread autoParker = new Thread(new CarsAutoParker(parking), "Генератор машин");
        autoParker.start();
    }
}
