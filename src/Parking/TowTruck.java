package Parking;

import java.util.Deque;
import java.util.concurrent.Semaphore;

public class TowTruck implements Runnable {
    private static final int millisecondsInAMinute = 60 * 1000;
    Semaphore parkingSlots;
    Deque<String> towedCarsId;
    int speedUpCoefficient;
    TowTruck(Semaphore parkingSlots,  Deque<String> towedCarsId, int speedUpCoefficient){
        this.parkingSlots = parkingSlots;
        this.towedCarsId = towedCarsId;
        this.speedUpCoefficient = speedUpCoefficient;
    }
    @Override
    public void run() {
        System.err.printf("Потік \"%s\" розпочав роботу, машини для евакуації: %s, їх кількість: %d\n", Thread.currentThread().getName(), towedCarsId.toString(), towedCarsId.size());
        while (!towedCarsId.isEmpty()){
            String towedCarId = towedCarsId.removeFirst();
            System.out.printf("Машину з індексом \"%s\" евакуйовують\n", towedCarId);
            try {
                Thread.sleep((5 * millisecondsInAMinute) / speedUpCoefficient);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.err.printf("Машина з індексом \"%s\" була евакуйована\n", towedCarId);
        }
        System.err.printf("Потік \"%s\" закінчив свою роботу\n", Thread.currentThread().getName());
    }
}
