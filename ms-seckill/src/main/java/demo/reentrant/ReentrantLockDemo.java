package demo.reentrant;

public class ReentrantLockDemo {

    private int count = 0;
    private ReentrantLock lock = new ReentrantLock();

    private void call() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        inc();
        lock.unlock();
    }

    private void inc() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i =0; i < 500; i++) {
            count ++;
        }
        lock.unlock();
    }

    // 可重入锁
    public static void main(String[] args) throws Exception {
        ReentrantLockDemo demo = new ReentrantLockDemo();
        Thread t1 = new Thread(()-> demo.call());
        Thread t2 = new Thread(() -> demo.call());
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(demo.count);
    }
}