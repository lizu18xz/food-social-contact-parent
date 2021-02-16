package demo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class RedisLock {

    private static final String LOCK_SUCCESS = "OK";
    private static final long UNLOCK_SUCCESS = 1L;

    /**
     * 尝试获取分布式锁
     *
     * @param jedis      Redis客户端
     * @param lockKey    锁
     * @param value      锁的值
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryLock(Jedis jedis, String lockKey,
                                  String value, int expireTime) {
        // 自旋锁
        while (true) {
            // set key value ex seconds nx(只有键不存在的时候才会设置key)
            String result = jedis.set(lockKey, value,
                    SetParams.setParams().ex(expireTime).nx());
            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
        }
    }

    /**
     * 释放分布式锁
     *
     * @param jedis   Redis客户端
     * @param lockKey 锁
     * @return 是否释放成功
     */
    public static boolean unlock(Jedis jedis, String lockKey) {
        Long result = jedis.del(lockKey);
        if (UNLOCK_SUCCESS == result) {
            return true;
        }
        return false;
    }

}