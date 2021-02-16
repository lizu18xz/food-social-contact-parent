package demo.reentrant;

public class UnReentrantLockDemo {

    private int count = 0;
    private Lock lock = new Lock();

    private void call() {
        lock.lock();
        inc();
        lock.unlock();
    }

    private void inc() {
        lock.lock();
        for (int i =0; i < 500; i++) {
            count ++;
        }
        lock.unlock();
    }

    // 不可重入的效果
    public static void main(String[] args) throws Exception {
        UnReentrantLockDemo unReentrantLockDemo = new UnReentrantLockDemo();
        Thread t1 = new Thread(()-> unReentrantLockDemo.call());
        Thread t2 = new Thread(() -> unReentrantLockDemo.call());
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(unReentrantLockDemo.count);
    }
}