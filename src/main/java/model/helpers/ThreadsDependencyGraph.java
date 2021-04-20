package model.helpers;

import app.Config;
import controller.TransformatorForLayers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ThreadsDependencyGraph {

    private Map<String, List<String>> adjacencyMap;
    private Map<String, Boolean> visited;
    private Optional<String> cycledThreadNameOpt;
    private String firstDependentThread;
    private List<BreakConflictContainer> breakInfoWithoutConf;
    private Map<String, String> directSuperiorThreadInTSMap;

    public ThreadsDependencyGraph(Deque<BreakConflictContainer> breakConflictContainers,
                                  List<BreakConflictContainer> breakInfoWithoutConf){
        this.visited = new HashMap<>();
        this.adjacencyMap = new HashMap<>();
        this.breakInfoWithoutConf = breakInfoWithoutConf;
        this.directSuperiorThreadInTSMap = new ConcurrentHashMap<>();
        for(BreakConflictContainer container: breakConflictContainers){
            adjacencyMap.put(container.getThreadName(), new LinkedList<>());
            visited.put(container.getThreadName(), false);
        }
        prepareThreadsDAG(breakConflictContainers);
    }

    public Map<String, String> getDirectSuperiorThreadInTSMap(){
        String startingThreadNode = getStartingThreadNode();
        initBeforeGraphTraversal();
        List<String> topologicalOrder = getTopologicalOrder(startingThreadNode);
        String directSuperiorThreadName = "DUMMY"; //FUNCTIONALITY TO CHECK
        for(String threadName: topologicalOrder){
            if(isThreadNodeIndependent(threadName) || threadName.equals(firstDependentThread)){
                directSuperiorThreadInTSMap.put(threadName, threadName);
            }
            else{
                directSuperiorThreadInTSMap.put(threadName, directSuperiorThreadName);
            }
            directSuperiorThreadName = threadName;
        }
        return directSuperiorThreadInTSMap;
    }

    public Map<String, List<String>> getAdjacencyMap(){
        return adjacencyMap;
    }

    public List<String> getTopologicalOrder(String startingThreadNode){
        Stack<String> topologicalStack = new Stack<>();
        if(breakInfoWithoutConf.size() != Config.THREADS_IN_POOL)
            DFSForTopologicalSorting(startingThreadNode, topologicalStack);

        List<String> TSThreads = new LinkedList<>();
        for(BreakConflictContainer cont: breakInfoWithoutConf){
            TSThreads.add(cont.getThreadName());
        }
        if(breakInfoWithoutConf.size() == Config.THREADS_IN_POOL){
            System.out.println("FULL PARALLEL- START_THREAD = "+ startingThreadNode +", TOPOLOGICALLY_SORTED_THREADS = "+ TSThreads);
            return TSThreads;
        }
        firstDependentThread = topologicalStack.peek();
        while(!topologicalStack.empty()) {
            TSThreads.add(topologicalStack.pop());
        }
        System.out.println("START_THREAD = "+ startingThreadNode +", TOPOLOGICALLY_SORTED_THREADS = "+ TSThreads);
        return TSThreads;
    }

    public String getStartingThreadNode(){
        Set<String> threadsNames = adjacencyMap.keySet();
        String spareStartingThread = null;
        for(String threadName: threadsNames) {
            if(isThreadNodeIndependent(threadName)) continue;
            if(spareStartingThread == null) spareStartingThread = threadName;

            boolean shouldBeFirstNodeInTS = true;
            for (Map.Entry<String, List<String>> threadInfo : adjacencyMap.entrySet()) {
                if (!threadInfo.getKey().equals(threadName) && threadInfo.getValue().contains(threadName)) {
                    shouldBeFirstNodeInTS = false;
                    break;
                }
            }
            if(shouldBeFirstNodeInTS) return threadName;
        }
        return spareStartingThread;
    }

    public void DFSForTopologicalSorting(String startThreadNode, Stack<String> stack){
        DFS(startThreadNode, stack);
        for(Map.Entry<String, List<String>> threadInfo: adjacencyMap.entrySet()){
            if(!isThreadNodeIndependent(threadInfo.getKey()) && !visited.get(threadInfo.getKey())){
                DFS(threadInfo.getKey(), stack);
            }
        }
    }

    public void DFS(String threadName, Stack<String> stack){
        visited.put(threadName, true);
        if(!adjacencyMap.containsKey(threadName)) return; //added
        for(String neighbour: adjacencyMap.get(threadName)){
            if(!visited.get(neighbour)){
                DFS(neighbour, stack);
            }
        }
        //push threadName on stack when all his neighbours have been visited
        stack.push(threadName);
    }

    public boolean isThreadNodeIndependent(String threadName){
        for(BreakConflictContainer cont: breakInfoWithoutConf){
            if(cont.getThreadName().equals(threadName)) return true;
        }
        return false;
    }

    public void prepareThreadsDAG(Deque<BreakConflictContainer> breakConflictContainers){
        for(BreakConflictContainer container: breakConflictContainers){
            createThreadAdjacencyList(container, breakConflictContainers);
        }

        removeCyclesInAdjacencyMap();
    }

    public void createThreadAdjacencyList(BreakConflictContainer container, Deque<BreakConflictContainer> breakConflictContainers){
        for(Map.Entry<String, BreakSimulationNode> breakInfo: container.threadFirstConflicts.entrySet()){
            String currentContThreadName = container.getThreadName();
            String neighbourThreadName = breakInfo.getKey();
            BreakConflictContainer neighbourThreadConflictCont = breakConflictContainers.stream()
                                                                                    .filter(el -> el.getThreadName().equals(neighbourThreadName))
                                                                                    .collect(Collectors.toList())
                                                                                    .get(0);

            System.out.println("neighbourThreadConflictCont = "+ neighbourThreadConflictCont.toString()+ ", currentContThreadName = "+ currentContThreadName
            + ", neighbourThreadName = "+ neighbourThreadName);
            int conflictStepNo = neighbourThreadConflictCont.threadFirstConflicts.get(currentContThreadName).getStepNo();
            if(isCurrThreadSuperiorToNeighbour(breakInfo.getValue().getStepNo(), currentContThreadName, conflictStepNo, neighbourThreadName)){
                adjacencyMap.get(currentContThreadName).add(neighbourThreadName);
            }
        }
    }

    public boolean isCurrThreadSuperiorToNeighbour(int conflictStepNoCurrThread, String currentThreadName,
                                                 int conflictStepNoNeighbourThread, String anotherThreadName){

        //filter direct cycles between threads
        boolean res = !(adjacencyMap.get(anotherThreadName).contains(currentThreadName));
        return (conflictStepNoCurrThread >= conflictStepNoNeighbourThread) && res;
    }

    //filter indirect (transitive) cycles
    public void removeCyclesInAdjacencyMap(){
        //Map<String, List<String>> adjacencyMapCopy = new HashMap<>(adjacencyMap);
        for(Map.Entry<String, List<String>> threadInfo: adjacencyMap.entrySet()){
            String searchedThreadName = threadInfo.getKey();
            initBeforeGraphTraversal();
            cycledThreadNameOpt = Optional.empty();
            hasCycleInGivenNode(searchedThreadName, threadInfo.getKey());
            if(cycledThreadNameOpt.isPresent()){
                String cycledThreadName = cycledThreadNameOpt.get();
                adjacencyMap.get(cycledThreadName).remove(searchedThreadName);
            }
        }
    }

    public void initBeforeGraphTraversal(){
        firstDependentThread = null;
        for(String key: visited.keySet()){
            visited.put(key, false);
        }
    }

    public void hasCycleInGivenNode(String searchedThreadNodeName, String currThreadName){
        visited.put(currThreadName, true);
        if(adjacencyMap.get(currThreadName).contains(searchedThreadNodeName)){
            System.out.println("Found cycled thread = " + currThreadName);
            //TransformatorForLayers.meshLogger.log("Found cycled thread = " + currThreadName);
            cycledThreadNameOpt = Optional.of(currThreadName);
        }

        for(String neighbour: adjacencyMap.get(currThreadName)){
            if(!visited.get(neighbour)){
                hasCycleInGivenNode(searchedThreadNodeName, neighbour);
            }
        }
    }
}
