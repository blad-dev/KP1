package cafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {

    static final Semaphore tables = new Semaphore(3);
    static final Semaphore waiter = new Semaphore(1);

    private static boolean isAvailableHours = true;

    public static synchronized boolean isOpen () {
        return isAvailableHours;
    }

    public static synchronized void closeCafe () {
        isAvailableHours = false;
        System.err.println("=============Кафе закрили================");
    }

    public static void main(String[] args) throws InterruptedException {
        List<Thread> people = new ArrayList<Thread>();
        Runnable cafe = () -> {
            int i = 0;
            while(isOpen()) {
                people.addLast(new Thread(new People(), String.valueOf(i)));
                people.getLast().start();
                i++;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        Thread cafeThread = new Thread(cafe, "Кафе");
        cafeThread.start();
        Thread.sleep(12000);
        closeCafe();
        for(Thread thread : people){
            thread.join();
        }

        System.err.println("=============Персонал пішов додому================");

    }

}
