import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class PacketQueue {
    private final LinkedList<Packet> queue;
    private final ReentrantLock lock;
    private final Condition notEmpty;

    public PacketQueue() {
        queue = new LinkedList<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public void add(Packet packet) {
        lock.lock();
        try {
            queue.add(packet);

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Packet remove() throws InterruptedException {
        lock.lock();
        try {
            while(queue.isEmpty())
                notEmpty.await();
            return queue.pop();
        } finally {
            lock.unlock();
        }
    }
}
