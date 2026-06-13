package util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LockUtil {

    private final int lockWaitTime;
    private final LockPool<String> lockPool = new LockPool<>(ReentrantLock::new);
    private static final int DEFAULT_LOCK_WAIT_TIME_SECONDS = 5;

    public LockUtil()
    {
        this(DEFAULT_LOCK_WAIT_TIME_SECONDS);
    }

    public LockUtil(int lockWaitTime)
    {
        this.lockWaitTime = lockWaitTime;
    }

    public ProcessResult<Object> lockAndApply(String objectId, Consumer<String> consumer, long updateTimeStamp)
    {
        return lockAndApply(objectId, id -> {
            consumer.accept((String) id);
            return ProcessResult.EVENT_PROCESS_RESULT_SUCCESS;
        }, updateTimeStamp);
    }

    public <T> ProcessResult<T> lockAndApply(String objectId,
                                             Function<Object, ProcessResult<T>> consumer,
                                             long updateTimeStamp)
    {
        System.out.println(String.format("lockAndApply()-> Acquiring lock on %s @%s, updateTimeStamp %s", objectId, Thread.currentThread(), updateTimeStamp));
        LockWrapper lockWrapper = lockPool.borrowObject(objectId);
        Lock lock = lockWrapper.getLock();
        String errormsg = null;
        boolean isLocked = false;
        try
        {
            long startTime = System.currentTimeMillis();
            isLocked = lock.tryLock(lockWaitTime, TimeUnit.SECONDS);
            if(!isLocked)
            {
                long endTime = System.currentTimeMillis();
                return new ProcessResult<>(ProcessResultType.LOCK_TIMEOUT, "timeOut "+ (endTime-startTime)+ " ms when acquiring lock on objectid: "+ objectId, null);
            }
            return processRequest(lockWrapper, objectId, consumer, updateTimeStamp);
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            errormsg = "interrupted when aquiring lock on objectid: "+ objectId;
            return new ProcessResult<>(ProcessResultType.ERROR, errormsg, null);
        } finally {
            if(isLocked)
            {
                lock.unlock();
            }
        }
    }

    private <T> ProcessResult<T> processRequest(LockWrapper lockWrapper,
                                                String objectId,
                                                Function<Object, ProcessResult<T>> consumer,
                                                long updateTimeStamp)
    {
        String errorMsg = null;
        try
        {
            System.out.println(String.format("lockAndApply()-> Lock acquired on %s @%s", objectId, Thread.currentThread()));

            if(lockWrapper.getUpdateTimeStamp() > updateTimeStamp)
            {
                errorMsg = "lock requested by older version of event with objectid: "+ objectId +
                        ". The requesting event updateTimeStamp: "+ updateTimeStamp + "but current timestap:"+ lockWrapper.getUpdateTimeStamp();
                return new ProcessResult<>(ProcessResultType.STALE, errorMsg, null);
            }else
            {
                lockWrapper.setUpdateTimeStamp(updateTimeStamp);
                return consumer.apply(objectId);
            }
        } catch (Exception e)
        {
            errorMsg = "Exception processing event on object id: "+ objectId + ", error: "+ e.getMessage();
            System.out.println(String.format("lockAndApply()-> Exception excountered while processing for objectid: %s. Error %s ", objectId, errorMsg));
            return new ProcessResult<>(ProcessResultType.ERROR, errorMsg, null);
        }
    }


    private class LockWrapper
    {
        private final Lock lock;
        private long updateTimeStamp;
        private final int borrowCount;

        private LockWrapper(Lock lock, int borrowCount, long updateTimeStamp)
        {
            this.lock = lock;
            this.borrowCount = borrowCount ==0?1:borrowCount;
            this.updateTimeStamp = updateTimeStamp;
        }

        public void setUpdateTimeStamp(long updateTimeStamp) { this.updateTimeStamp = updateTimeStamp;}

        public Lock getLock()
        {
            return lock;
        }

        public long getUpdateTimeStamp() {
            return updateTimeStamp;
        }

        public LockWrapper increment()
        {
            return new LockWrapper(lock, borrowCount+1, updateTimeStamp);
        }

        public LockWrapper decrement()
        {
            return new LockWrapper(lock, borrowCount-1, 0);
        }
    }
    private class LockPool<K>
    {
        private final ConcurrentMap<K, LockWrapper> pool = new ConcurrentHashMap<>();
        private final Supplier<Lock> supplier;

        public LockPool(Supplier<Lock> supplier)
        {
            this.supplier = supplier;
        }

        public LockWrapper borrowObject(K key)
        {
            return pool.compute(key, (k, existingCountWrapper)->{
                if(existingCountWrapper!=null)
                {
                    return existingCountWrapper.increment();
                } else
                {
                    Lock newObject = supplier.get();
                    return new LockWrapper(newObject,0,0);
                }
            });
        }

        public void returnObject(K key) {
            pool.compute(key, (k, existingCountWrapper) -> {
                if (existingCountWrapper != null) {
                    LockWrapper newWrapper = existingCountWrapper.decrement();
                    return newWrapper.borrowCount == 0 ? null : newWrapper;
                } else {
                    throw new IllegalArgumentException("Attempt to return kry that is not borrowed");
                }
            });
        }
    }
}
