import java.io.*;

class Bootstrapper{
    private final Map<String, BootstrapperCollum> bootstrapper;
    private final ReentrantLock lock;
    private final Condition condition;

    static class BootstrapperCollum{
        private final Set<String> neighbors;
        private boolean isVisited;

        public BootstrapperCollum(Set<String> neighbors){
            this.neighbors = neighbors;
            this.isVisited = false;
        }

        public Set<String> getNeighbors(){
            return this.neighbors;
        }

        public boolean getIsVisited(){
            return this.isVisited;
        }

        public void setIsVisited(boolean isVisited){
            this.isVisited = isVisited;
        }
    }
}