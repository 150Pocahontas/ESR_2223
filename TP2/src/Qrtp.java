import java.util.*;
import java.util.concurrent.locks.*;

public class Qrtp{
    private final LinkedList<Prtp> queue;
    private final Lock lock;
    private final Condition notEmpty;

    public Qrtp(){
        queue = new LinkedList<Prtp>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public void add(Prtp packet){
        lock.lock();
        try{
            queue.add(packet);
            notEmpty.signal();
        }finally{
            lock.unlock();
        }
    }

    public Prtp remove() throws InterruptedException {
        lock.lock();
        try{
            while(queue.isEmpty()){
                notEmpty.await();
            }
            return queue.remove();
        }catch(InterruptedException e){
            return null;
        }finally{
            lock.unlock();
        }
    }
}
