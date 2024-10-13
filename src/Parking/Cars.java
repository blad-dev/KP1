package Parking;

import java.util.List;

public class Cars {
    public static Thread createParkingCar(Parking parking, String idOfCar, List<String> addCarIfParked){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(parking.tryToPark(Thread.currentThread().getName()))
                        addCarIfParked.addLast(idOfCar);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, idOfCar);
    }
    public static Thread createLeavingCar(Parking parking, String idOfCar){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    parking.tryLeaveParking(Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, idOfCar);
    }
}
