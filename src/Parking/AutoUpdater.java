package Parking;


import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class AutoUpdater implements Runnable{
    Parking parkingToUpdate;
    int speedUpCoefficient;
    LocalTime localTime;
    Timer timer;
    AutoUpdater(Parking parkingToUpdate, int speedUpCoefficient){
        this.parkingToUpdate = parkingToUpdate;
        this.speedUpCoefficient = speedUpCoefficient;
        localTime = LocalTime.of(0, 0);
        timer = new Timer();
    }
    @Override
    public void run() {
        parkingToUpdate.updateTime(localTime);
        localTime = localTime.plusSeconds(speedUpCoefficient);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                parkingToUpdate.updateTime(localTime);
                localTime = localTime.plusSeconds(speedUpCoefficient);
            }
        },1000, 1000);
    }
    public void reinitTimer(){
        timer = new Timer();
    }
    void interrupt(){
        timer.cancel();
    }
}
