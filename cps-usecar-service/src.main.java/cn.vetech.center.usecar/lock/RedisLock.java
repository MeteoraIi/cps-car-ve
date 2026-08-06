package cn.vetech.center.usecar.lock;


import cn.vetech.center.usecar.common.redis.RedisCacheManage;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisLock {

    /**
     * 日志工具
     */
    private Logger logger = LoggerFactory.getLogger(RedisLock.class);

    @Autowired
    private RedisCacheManage redisCacheManage;

    private static final String LOCK_SCRIPT =
            "if redis.call('SET', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then return 1; elseif redis.call('get', KEYS[1]) == ARGV[1] then return 2; else return 0; end";

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]); elseif redis.call('EXISTS', KEYS[1]) == 0 then return 2; else return 0; end";




    /**
     *   如果当前加锁对象是锁的持有者，则直接返回成功且不会重置过期时间，
     *   如果当前加锁对象是是锁的持有者，则等到waitTime时间，直到加锁成功
     *   如果当前锁没有持有者，则直接加锁
     * @param name
     * @param owner
     * @param expireTime 锁的过期时间
     * @param waitTime
     * @return
     * @throws InterruptedException
     */
    public boolean tryLock(String name,String owner,long expireTime,long waitTime) throws InterruptedException {
        long start = System.currentTimeMillis();
        long retryInterval = 100; // 初始重试间隔
        while (System.currentTimeMillis() - start < waitTime*1000) {
            if (lock(name,owner,expireTime)) {
                return true;
            }
            // 指数退避
            Thread.sleep(retryInterval);
            retryInterval = Math.min(retryInterval * 2, 1000); // 最大间隔1秒
        }
        return false;
    }

    private boolean lock(String name,String owner,long expireTime){
        try {
            RedisTemplate redisTemplate = redisCacheManage.getRedisTemplate();
            DefaultRedisScript<String> script = new DefaultRedisScript<>();
            script.setScriptText(LOCK_SCRIPT);
            script.setResultType(String.class);
            Object result = exec(redisTemplate,script,Lists.newArrayList(name),owner,expireTime);
            if(result!=null && (Long)result == 1){
                return true;
            }
            if(result!=null && (Long)result == 2){
                logger.info("锁[{}]的持有者为当前加锁者[{}]本身重入",name,owner);
                return true;
            }
        }catch (Exception e){
            logger.error("加锁异常，name={},owner={}",name,owner);
            unlock(name,owner);
        }
        return false;
    }

    public Object unlock(String name,String owner){
        try {
            RedisTemplate redisTemplate = redisCacheManage.getRedisTemplate();
            RedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT,Long.class);
            Object result = exec(redisTemplate,script,Lists.newArrayList(name), owner);
            if(result!=null && (Long)result == 1){
                logger.info("锁[{}]的持有者[{}]释放锁成功",name,owner);
            }else if(result!=null && (Long)result == 2){
                logger.info("锁[{}]无持有者，无需解锁操作",name);
            }else{
                logger.info("锁[{}]的持有者不是[{}]不能解锁",name,owner);
            }
            return result;
        }catch (Exception e){
            logger.error("释放锁[{}]异常",name,e);
        }
        return null;
    }

    private Object exec(RedisTemplate redisTemplate,RedisScript script,List<String> keys, Object... args){
        // 将可变参转为 List
        List<String> list = new ArrayList<>();
        for (Object arg : args) {
            list.add(arg.toString());
        }
        // 将 RedisScript 转换为 string 类型的脚本
        String scriptScriptAsString = script.getScriptAsString();
        return redisTemplate.execute((RedisCallback<Object>) connection -> {
            // 获取连接
            Object nativeConnection = connection.getNativeConnection();
            // 集群模式的实例
            if (nativeConnection instanceof JedisCluster) {
                return ((JedisCluster) nativeConnection).eval(scriptScriptAsString, keys, list);
            }
            // 单机模式的实例
            if (nativeConnection instanceof Jedis) {
                return ((Jedis) nativeConnection).eval(scriptScriptAsString, keys, list);
            }
            return null;
        });
    }

    public boolean tryBiasedLock(String name,String owner,long biasedTime){
        boolean result = false;
        try {
            result = tryLock(name, owner, biasedTime, biasedTime * 2);
            //偏向锁不释放锁
        } catch (InterruptedException e) {
            logger.error("获取偏向锁异常，name={},owner={}",name,owner,e);
            //异常时释放锁
            unlock(name,owner);
        }
        return result;
    }

    public Object releaseBiasedLock(String name,String owner){
        try {
            return unlock(name,owner);
        }catch (Exception e){
            logger.error("释放偏向锁异常,name={},owner={}",name,owner,e);
        }
        return null;
    }
}
