package cafe;

public class People implements Runnable {
    private boolean tableTaken = false;
    private boolean waiterTaken = false;
    public boolean isCafeOpen_ReleaseAndLeaveIfNot() throws InterruptedException {
        if(!Main.isOpen()){
            System.out.printf("Гості з потому %s намагалися сконтактувати персонал, але кафе вже зачинене\n", Thread.currentThread().getName());
            Thread.sleep(20);
            System.err.printf("Гості з потоку %s пішли голодні \n", Thread.currentThread().getName());
            releaseAll();
            return false;
        }
        return true;
    }
    private boolean tryTakeWaiter()throws InterruptedException{
        if(!isCafeOpen_ReleaseAndLeaveIfNot())return false;
        if(!waiterTaken){
            Main.waiter.acquire();
            waiterTaken = true;
        }
        return true;
    }
    private boolean tryTakeTable()throws InterruptedException{
        if(!isCafeOpen_ReleaseAndLeaveIfNot())return false;
        if(!tableTaken){
            Main.tables.acquire();
            tableTaken = true;
        }
        return true;
    }
    private void releaseWaiter()throws InterruptedException{
        if(waiterTaken){
            Main.waiter.release();
            waiterTaken = false;
        }
    }
    private void releaseTable()throws InterruptedException{
        if(tableTaken){
            Main.tables.release();
            tableTaken = false;
        }
    }
    private void releaseAll() throws InterruptedException {
        releaseWaiter();
        releaseTable();
    }
    @Override
    public void run() {
        try {
            System.err.printf("Гості з потоку %s прийшли в кафе і шукають столик \n", Thread.currentThread().getName());
            Thread.sleep(20);
            if(!tryTakeTable())return;



            System.out.printf("Гості з потоку %s сіли за столик і позвали офіціанта \n", Thread.currentThread().getName());
            Thread.sleep(20);
            if(!tryTakeWaiter())return;


            System.out.printf("Офіціант підійшов до гостей з потоку %s \n", Thread.currentThread().getName());

            Thread.sleep(2000);

            System.out.printf("Офіціант прийняв замовлення від гостей з потоку %s \n", Thread.currentThread().getName());
            Thread.sleep(20);
            releaseWaiter();


            Thread.sleep(4000);

            if(!tryTakeWaiter())return;
            System.out.printf("Офіціант приніс замовлення гостям з потоку %s \n", Thread.currentThread().getName());
            Thread.sleep(20);
            releaseWaiter();

            Thread.sleep(4000);

            System.err.printf("Гості з потоку %s поїли і пішли \n", Thread.currentThread().getName());
            Thread.sleep(20);
            releaseAll();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
