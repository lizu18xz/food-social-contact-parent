package demo.reentrant;

public class Lock {

    private boolean isLocked = false;

    public synchronized void lock() {
        while (isLocked) {
            try {
                System.out.println(Thread.currentThread().getName());
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isLocked = true;
    }

    public synchronized void unlock() {
        isLocked = false;
        notify();
    }

}