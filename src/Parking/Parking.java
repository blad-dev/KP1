package Parking;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Semaphore;

import static java.lang.Math.min;

public class Parking {
    public static final int secondsInAnHour = 60 * 60;
    public static final int minutesInAnHour = 60;
    public static final int secondsInAMinute = 60;
    public static final int millisecondsInAMinute = 60 * 1000;
    private final int daytimeAmountOfParkingSlots;
    private final int nighttimeAmountOfParkingSlots;
    private final Deque<String> idOfParkedCars;

    private final Semaphore parkingSlots;
    private final LocalTime beginningOfTheDay;
    private final LocalTime endOfTheDay;
    private enum TimeOfDay {
        Daytime,
        Nighttime,
        NotSetYet,
    }
    private TimeOfDay currentTimeOfDay;
    Deque<String> towedCarsId;
    private AutoUpdater autoUpdater;
    private Thread towTruck;


    public Parking(int daytimeAmountOfParkingSlots, int nighttimeAmountOfParkingSlots, LocalTime beginningOfTheDay, LocalTime endOfTheDay, int speedUpCoefficient){
        checkArguments(daytimeAmountOfParkingSlots, nighttimeAmountOfParkingSlots, beginningOfTheDay, endOfTheDay);

        this.daytimeAmountOfParkingSlots = daytimeAmountOfParkingSlots;
        this.nighttimeAmountOfParkingSlots = nighttimeAmountOfParkingSlots;
        this.beginningOfTheDay = beginningOfTheDay;
        this.endOfTheDay = endOfTheDay;
        checkSpeedUpCoefficient(speedUpCoefficient);

        currentTimeOfDay = TimeOfDay.NotSetYet;
        idOfParkedCars = new ArrayDeque<>();
        parkingSlots = new Semaphore(0);
        towedCarsId = new ArrayDeque<>();
        initAutoUpdater(speedUpCoefficient);
    }
    public void interruptAutoUpdater(){
        autoUpdater.interrupt();
    }
    public void resumeAutoUpdater(){
        autoUpdater.reinitTimer();
        autoUpdater.run();
    }
    public void setSpeedUpCoefficient(int speedUpCoefficient){
        checkSpeedUpCoefficient(speedUpCoefficient);
        autoUpdater.speedUpCoefficient = speedUpCoefficient;
    }
    public void setTime(LocalTime time){
        autoUpdater.localTime = time;
        interruptAutoUpdater();
        resumeAutoUpdater();
    }


    public boolean tryToPark(String carId) throws InterruptedException {
        if(idOfParkedCars.contains(carId)){
            System.out.printf("Машина з індексом \"%s\" вже припаркована\n", carId);
            return true;
        }
        if(!parkingSlots.tryAcquire()){
            System.out.printf("Машина з індексом \"%s\" хотіла припаркуватися, але місць немає\n", carId);
            return false;
        }
        idOfParkedCars.addLast(carId);
        System.out.printf("Машина з індексом \"%s\" паркується\n", carId);
        Thread.sleep((5*millisecondsInAMinute) / autoUpdater.speedUpCoefficient);
        System.out.printf("Машина з індексом \"%s\" припаркувалася\n", carId);
        return true;
    }
    public boolean tryLeaveParking(String carId) throws InterruptedException {
        if(!idOfParkedCars.contains(carId)){
            if(towedCarsId.contains(carId))
                System.err.printf("Машину з індексом \"%s\" хотіли забрати, але її евакуюють\n", carId);
            else
                System.out.printf("Машину з індексом \"%s\" хотіли забрати, але її немає на парковці\n", carId);
            return false;
        }
        idOfParkedCars.remove(carId);
        System.out.printf("Машина з індексом \"%s\" покидає парковку\n", carId);
        Thread.sleep((5*millisecondsInAMinute) / autoUpdater.speedUpCoefficient);
        System.out.printf("Машина з індексом \"%s\" поїхала з парковки\n", carId);
        parkingSlots.release();
        return true;
    }
    public void updateTime(LocalTime time){
        TimeOfDay previousTime = currentTimeOfDay;
        updateCurrentTime(time);
        updateParkingSlots(previousTime);
        Deque<String> allCarsOnParking = new ArrayDeque<>();
        allCarsOnParking.addAll(towedCarsId);
        allCarsOnParking.addAll(idOfParkedCars);
        System.out.printf("Час оновлено: %s, машини на парковці: %s, їх кількість: %d\n", time.toString(), allCarsOnParking.toString(), allCarsOnParking.size());
    }



    private void checkSpeedUpCoefficient(int speedUpCoefficient){
        if(speedUpCoefficient <= 0)
            throw new IllegalArgumentException("Speed up coefficient can only be a natural number!");

        LocalTime durationOfDay = endOfTheDay.minusHours(beginningOfTheDay.getHour()).minusMinutes(beginningOfTheDay.getMinute()).minusSeconds(beginningOfTheDay.getSecond());
        int durationOfDayInSeconds = durationOfDay.getHour()*secondsInAnHour + durationOfDay.getMinute()*secondsInAMinute + durationOfDay.getSecond();
        final int secondsInOneDay = 24 * secondsInAnHour;
        int minimalDurationOfTimeOfTheDay = min(secondsInOneDay - durationOfDayInSeconds, durationOfDayInSeconds);
        if(speedUpCoefficient >= minimalDurationOfTimeOfTheDay)
            throw new IllegalArgumentException("Speed up coefficient can only be smaller than night or day in duration!");
    }
    private void checkArguments(int daytimeAmountOfParkingSlots, int nighttimeAmountOfParkingSlots, LocalTime beginningOfTheDay, LocalTime endOfTheDay){
        if(daytimeAmountOfParkingSlots < 0)
            throw new IllegalArgumentException("Daytime amount of parking slots cannot be negative!");
        if(nighttimeAmountOfParkingSlots < 0)
            throw new IllegalArgumentException("Nighttime amount of parking slots cannot be negative!");
        if(beginningOfTheDay.isAfter(endOfTheDay))
            throw new IllegalArgumentException("Beginning of the day cannot be after its end, only night beginning can!");
    }
    private void initAutoUpdater(int speedUpCoefficient){
        autoUpdater = new AutoUpdater(this, speedUpCoefficient);
        autoUpdater.run();
    }
    private int numberOfSlotsByTimeOfDay(TimeOfDay timeOfDay){
        return switch (timeOfDay) {
            case Daytime -> daytimeAmountOfParkingSlots;
            case Nighttime -> nighttimeAmountOfParkingSlots;
            case NotSetYet -> 0;
        };
    }
    private int changeInNumberOfSlots(TimeOfDay timeBefore, TimeOfDay timeAfter){
        return numberOfSlotsByTimeOfDay(timeAfter) - numberOfSlotsByTimeOfDay(timeBefore);
    }
    private TimeOfDay getTimeOfDay(LocalTime time){
        if(time.isBefore(beginningOfTheDay) || time.isAfter(endOfTheDay))
            return TimeOfDay.Nighttime;
        return TimeOfDay.Daytime;
    }
    private void updateCurrentTime(LocalTime time){
        currentTimeOfDay = getTimeOfDay(time);
    }
    private void removeCars(Deque<String> carsIdToRemove){
        if(towTruck == null || towTruck.getState() != Thread.State.RUNNABLE){
            towTruck = new Thread(new TowTruck(parkingSlots, carsIdToRemove, autoUpdater.speedUpCoefficient), "Евакуатор");
            towTruck.start();
        }
    }
    private void reduceSlots(int slotsReduction) {
        for(int i = 0; i < slotsReduction; ++i){
            if(!parkingSlots.tryAcquire()){
                towedCarsId.addLast(idOfParkedCars.removeFirst());
            }
        }
        if(!towedCarsId.isEmpty())
            removeCars(towedCarsId);
    }
    private void updateParkingSlots(TimeOfDay previosTimeOfDay) {
        final int changeInNumberOfSlots = changeInNumberOfSlots(previosTimeOfDay, currentTimeOfDay);
        if(changeInNumberOfSlots > 0){
            parkingSlots.release(changeInNumberOfSlots);
        }
        else if (changeInNumberOfSlots < 0) {
            int numberOfSlotsToReduce = -changeInNumberOfSlots;
            reduceSlots(numberOfSlotsToReduce);
        }
    }

}
