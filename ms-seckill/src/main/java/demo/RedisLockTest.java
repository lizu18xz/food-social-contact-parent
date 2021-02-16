package demo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.UUID;

public class RedisLockTest {

    private int count = 0;
    private String lockKey = "lock";

    private void call(Jedis jedis) {
        // 加锁
        boolean locked = RedisLock.tryLock(jedis, lockKey,
                UUID.randomUUID().toString(), 60);
        try {
            if (locked) {
                for (int i = 0; i < 500; i++) {
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放锁的时候有bug,有可能会删除其他线程的锁,导致线程不安全
            RedisLock.unlock(jedis, lockKey);
        }
    }

    public static void main(String[] args) throws Exception {
        RedisLockTest redisLockTest = new RedisLockTest();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxTotal(5);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                "192.168.10.101", 6379, 1000, "123456");

        Thread t1 = new Thread(() -> redisLockTest.call(jedisPool.getResource()));
        Thread t2 = new Thread(() -> redisLockTest.call(jedisPool.getResource()));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(redisLockTest.count);
    }

}