package model.helpers;

import model.ModelGraph;
import parallel.BreakingGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadLockContainer {

    private ConcurrentMap<String, Boolean> threadsExecutionFinished;
    public final Lock lock;
    private final Condition shouldWaitToRun;
    public ModelGraph graph;

    public ThreadLockContainer(ConcurrentMap<String, Boolean> threadsExecutionFinished, ModelGraph graph){
        this.threadsExecutionFinished = threadsExecutionFinished;
        this.graph  = graph;
        lock = new ReentrantLock();
        shouldWaitToRun = lock.newCondition();
    }

    public void setGraph(ModelGraph _graph){
        graph = _graph;
    }

    public void checkSuperiorThreadFinished(String superior, String current) throws InterruptedException {
        lock.lock();
        while(!threadsExecutionFinished.get(superior) && ( !superior.equals(current)) )
                shouldWaitToRun.await();




        lock.unlock();
    }

    public void setCurrentThreadFinished(String threadName){
        lock.lock();

        System.out.println("THREAD IN CONTAINER = "+ threadName);

        threadsExecutionFinished.put(threadName, true);

        shouldWaitToRun.signalAll();
        lock.unlock();
    }

    public void checkSuperiorThreadFinishedPart(String superior, String current) throws InterruptedException {
        lock.lock();
        while(!threadsExecutionFinished.get(superior) && ( !superior.equals(current)) )
            shouldWaitToRun.await();
    }

    public void unlock(){
        lock.unlock();
    }

    public void setCurrentThreadFinishedPart(String threadName){
        lock.lock();

        System.out.println("THREAD IN CONTAINER PART = "+ threadName);

        threadsExecutionFinished.put(threadName, true);

        shouldWaitToRun.signalAll();
    }

    public void signalThreads(){
        shouldWaitToRun.signalAll();
    }

}
