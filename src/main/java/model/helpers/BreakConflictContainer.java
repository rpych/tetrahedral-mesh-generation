package model.helpers;

import model.ModelGraph;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class BreakConflictContainer {

    public static final int DUMMY = -1;
    public String threadName;
    public Map<String, BreakSimulationNode> threadFirstConflicts;
    public int firstConflictStepNoWithOtherThreads;

    public BreakConflictContainer(){
        this.threadName = "";
        this.threadFirstConflicts = new ConcurrentSkipListMap<>();
        this.firstConflictStepNoWithOtherThreads = DUMMY;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getFirstConflictStepNoWithOtherThreads() {
        return firstConflictStepNoWithOtherThreads;
    }

    public void addConflictWithThread(String otherThreadName, BreakSimulationNode node){
        if(threadFirstConflicts.size() == 0){
            firstConflictStepNoWithOtherThreads = node.getStepNo();
        }
        else if(node.getStepNo() < firstConflictStepNoWithOtherThreads){
            firstConflictStepNoWithOtherThreads = node.getStepNo();
        }
        threadFirstConflicts.put(otherThreadName, node);
    }

    @Override
    public String toString(){
        return threadName + "::CONF_SIZE=" + threadFirstConflicts.size() + "::FIRST_CONFLICT=" + firstConflictStepNoWithOtherThreads + "::CONF="+threadFirstConflicts.toString();
    }
}
