import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

class Bootstrapper{
    private final Map<String, BootstrapperCollum> bootstrapper;
    private final ReentrantLock lock;
    private final Condition condition;

    static class BootstrapperCollum{
        private final Set<String> neighbours;
        private boolean isVisited;

        public BootstrapperCollum(Set<String> neighbours){
            this.neighbours = neighbours;
            this.isVisited = false;
        }

        public Set<String> getNeighbours(){
            return neighbours;
        }

        public boolean getIsVisited(){
            return isVisited;
        }

        public void setIsVisited(boolean isVisited){
            this.isVisited = isVisited;
        }
    }

    public Bootstrapper(String path) throws FileNotFoundException, IOException{
        this.bootstrapper = new HashMap<>();
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();

        File file = new File(path);
        try (Scanner scanner = new Scanner(file)) {
            String[] line;
            while(scanner.hasNextLine()){
                line = scanner.nextLine().split(":");
                Set<String> neighbours = new TreeSet<>(List.of(line[1].split(",")));
                this.bootstrapper.put(line[0], new BootstrapperCollum(neighbours));
            }
        } 
    }

    public String getNeighbours(String node){
        lock.lock();
        try{
            Set<String> neighbours = bootstrapper.get(node).getNeighbours();
            bootstrapper.get(node).setIsVisited(true);
            condition.signalAll();
            Iterator<String> iterator = neighbours.iterator();
            StringBuilder sb = new StringBuilder();
            while(iterator.hasNext()){
                sb.append(iterator.next());
                if(iterator.hasNext()){
                    sb.append(",");
                }
            }
            return sb.toString();
        }finally{
            lock.unlock();
        }
    }

    public void isfull() throws InterruptedException{
        lock.lock();
        try{
            while(!this.bootstrapper.values().stream().allMatch(BootstrapperCollum::getIsVisited)){
                condition.await();
            }
        }finally{
            lock.unlock();
        }
    }
}