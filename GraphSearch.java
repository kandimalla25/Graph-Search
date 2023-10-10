import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Pair {

    private Float distance;
    private int vertex;

    public Pair(Float distance, int vertex) {
        this.distance = distance;
        this.vertex = vertex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Pair)) {
            return false;
        }

        Pair incomingPair = (Pair) o;
        return this.distance == incomingPair.distance && this.vertex == incomingPair.vertex;
    }

    @Override
    public int hashCode() {
        int hashcode = 31;
        hashcode = hashcode * 17 + this.distance.hashCode();
        hashcode = hashcode * 17 + this.vertex;
        return hashcode;
    }

    public Float getDistance() {
        return this.distance;
    }

    public int getVertex() {
        return this.vertex;
    }
}

class Search {

    public static float LARGE_FLOAT = (float) 99999.0;

    private HashMap<String, Integer> assignNodesToNumbers(List<List<String>> data) {
        HashSet<String> nodesInSystem = new HashSet<>();
        HashMap<String, Integer> nodeNameToIndex = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {
            String currentSrc = data.get(i).get(0);
            String currentTarget = data.get(i).get(1);
            nodesInSystem.add(currentSrc);
            nodesInSystem.add(currentTarget);
        }

        int currentIndex = 0;
        for (String node: nodesInSystem) {
            nodeNameToIndex.put(node, currentIndex);
            currentIndex++;
        }

        return nodeNameToIndex;
    }
    
    private HashMap<Integer, String> assignNumbersToNodes(HashMap<String, Integer> nodesToNumbers) {
        HashMap<Integer, String> numbersToNodes = new HashMap<>();
        for (String node: nodesToNumbers.keySet()) {
            numbersToNodes.put(nodesToNumbers.get(node), node);
        }
        return numbersToNodes;
    }

    private void populateAdjMapAndDistanceBetweenNodes(HashMap<Integer, List<Pair>> adjMap, float[][] distanceBetweenNodes, List<List<String>> data, HashMap<String, Integer> nodeNameToIndex) {
        for (int i = 0; i < distanceBetweenNodes.length; i++) {
            Arrays.fill(distanceBetweenNodes[i], LARGE_FLOAT);
        }
        for (int i = 0; i < data.size(); i++) {
            int currentSrc = nodeNameToIndex.get(data.get(i).get(0));
            int currentTarget = nodeNameToIndex.get(data.get(i).get(1));
            Float currentDistance = Float.parseFloat(data.get(i).get(2));

            if (!adjMap.containsKey(currentSrc)) {
                adjMap.put(currentSrc, new ArrayList<Pair>());
            }
            if (!adjMap.containsKey(currentTarget)) {
                adjMap.put(currentTarget, new ArrayList<Pair>());
            }

            adjMap.get(currentSrc).add(new Pair(currentDistance, currentTarget));
            adjMap.get(currentTarget).add(new Pair(currentDistance, currentSrc));
            // populate the distance between the nodes as well
            distanceBetweenNodes[currentSrc][currentSrc] = 0;
            distanceBetweenNodes[currentTarget][currentTarget] = 0;
            distanceBetweenNodes[currentSrc][currentTarget] = currentDistance;
            distanceBetweenNodes[currentTarget][currentSrc] = currentDistance;
        }
    }

    private void printPathSrcTarget(String source, String target, HashMap<Integer, Integer> previous, HashMap<String, Integer> nodeNameToIndex, HashMap<Integer, String> nodeIndexToName, float[][] distanceBetweenNodes) {
        List<String> path = new ArrayList<>();
        int currentNode = nodeNameToIndex.get(target);
        while (currentNode != -1) {
            path.add(nodeIndexToName.get(currentNode));
            currentNode = previous.getOrDefault(currentNode, -1);
        }
        if (path.size() > 1) {
            float distance = 0;
            for (int i = path.size() - 1; i > 0; i--) {
                distance += distanceBetweenNodes[nodeNameToIndex.get(path.get(i))][nodeNameToIndex.get(path.get(i - 1))];
            }
            System.out.println("Distance: " + distance + " km");
            System.out.println("Route:");
            for (int i = path.size() - 1; i > 0; i--) {
                System.out.println(path.get(i) + " to " + path.get(i - 1) + ", " + distanceBetweenNodes[nodeNameToIndex.get(path.get(i))][nodeNameToIndex.get(path.get(i - 1))] + " km");
            }
        } else if (path.size() == 1 && path.get(0).equals(source)) {
            System.out.println("Distance: 0.0 km");
            System.out.println("Route:");
            System.out.println(path.get(0) + " to " + path.get(0) + ", " + distanceBetweenNodes[nodeNameToIndex.get(path.get(0))][nodeNameToIndex.get(path.get(0))] + " km");
        } else {
            System.out.println("Distance: infinity");
            System.out.println("Route:");
            System.out.println("None");
        }
    }

    /*
     * Prints the target path
     * 
     */
    public void uninformedSearch(List<List<String>> data, String source, String target) {
        HashSet<Integer> visited = new HashSet<>();
        // we will have to assign nodes numbers & assign them back
        HashMap<String, Integer> nodeNameToIndex = assignNodesToNumbers(data);
        HashMap<Integer, String> nodeIndexToName = assignNumbersToNodes(nodeNameToIndex);
        HashMap<Integer, List<Pair>> adjMap = new HashMap<>();
        float[][] distanceBetweenNodes = new float[nodeNameToIndex.values().size()][nodeNameToIndex.values().size()];
        populateAdjMapAndDistanceBetweenNodes(adjMap, distanceBetweenNodes, data, nodeNameToIndex);
        HashMap<Integer, Integer> previous = new HashMap<>();
        HashMap<Integer, Float> distanceFromSource = new HashMap<>();
        PriorityQueue<Pair> minHeap =  new PriorityQueue<>(Comparator.comparing(Pair::getDistance));
        // assuming x is the cost & y is the vertex in question

        // initialize all distances from source to infinity
        for (int value: nodeNameToIndex.values()) {
            distanceFromSource.put(value, LARGE_FLOAT);
        }
        distanceFromSource.put(nodeNameToIndex.get(source), (float) 0.0);

        // distanceFromSource for the source is 0 initially
        int nodesPopped = 0;
        int nodesExpanded = 0;
        int nodesGenerated = 0;
        minHeap.add(new Pair(distanceFromSource.get(nodeNameToIndex.get(source)), nodeNameToIndex.get(source)));
        while (!minHeap.isEmpty()) {
            Pair currentPair = minHeap.poll();
            nodesPopped++;
            if (visited.contains(currentPair.getVertex())) {
                continue;
            }
            nodesGenerated++;
            visited.add(currentPair.getVertex());

            for (Pair neighbor: adjMap.get(currentPair.getVertex())) {
                if (!(visited.contains(neighbor.getVertex()))) {
                    float calcDistanceFromSrc = distanceFromSource.get(currentPair.getVertex()) + distanceBetweenNodes[currentPair.getVertex()][neighbor.getVertex()];
                    nodesExpanded++;
                    nodesGenerated++;
                    if (calcDistanceFromSrc < distanceFromSource.get(neighbor.getVertex())) {  // this works out in a way you don't get infinity issues
                        distanceFromSource.put(neighbor.getVertex(), calcDistanceFromSrc);
                        previous.put(neighbor.getVertex(), currentPair.getVertex());           // we visited neighbor via currentPair
                        minHeap.add(new Pair(calcDistanceFromSrc, neighbor.getVertex()));
                    } else {
                        minHeap.add(new Pair(distanceFromSource.get(neighbor.getVertex()), neighbor.getVertex()));
                    }
                }
            }
        }

        System.out.println("Nodes Popped: " + nodesPopped);
        System.out.println("Nodes Expanded: " + nodesExpanded);
        System.out.println("Nodes Generated: " + nodesGenerated);
        printPathSrcTarget(source, target, previous, nodeNameToIndex, nodeIndexToName, distanceBetweenNodes);
    }

    public void informedSearch(List<List<String>> data, String source, String target, List<List<String>> heuristicEstimates) {
        HashSet<Integer> visited = new HashSet<>();
        // we will have to assign nodes numbers & assign them back
        HashMap<String, Integer> nodeNameToIndex = assignNodesToNumbers(data);
        HashMap<Integer, String> nodeIndexToName = assignNumbersToNodes(nodeNameToIndex);
        HashMap<Integer, List<Pair>> adjMap = new HashMap<>();
        float[][] distanceBetweenNodes = new float[nodeNameToIndex.values().size()][nodeNameToIndex.values().size()];
        populateAdjMapAndDistanceBetweenNodes(adjMap, distanceBetweenNodes, data, nodeNameToIndex);
        HashMap<Integer, Integer> previous = new HashMap<>();
        HashMap<Integer, Float> distanceFromSource = new HashMap<>();
        // this is used for informed search
        HashMap<Integer, Float> distanceFromTarget = new HashMap<>();
        HashMap<Integer, Float> heuristicDistance = new HashMap<>();
        PriorityQueue<Pair> minHeap =  new PriorityQueue<>(Comparator.comparing(Pair::getDistance));
        // assuming x is the cost & y is the vertex in question

        // initialize all distances from source to infinity
        for (int value: nodeNameToIndex.values()) {
            distanceFromSource.put(value, LARGE_FLOAT);
        }
        distanceFromSource.put(nodeNameToIndex.get(source), (float) 0.0);
        // initialize all distances to target as infinity
        for (int value: nodeNameToIndex.values()) {
            distanceFromTarget.put(value, LARGE_FLOAT);
        }

        // initialize heuristic distances
        for (List<String> heuristicEstimate: heuristicEstimates) {
            String node = heuristicEstimate.get(0);
            float estimate = Float.valueOf(heuristicEstimate.get(1));
            heuristicDistance.put(nodeNameToIndex.get(node), estimate);
        }

        int nodesPopped = 0;
        int nodesExpanded = 0;
        int nodesGenerated = 0;
        distanceFromTarget.put(nodeNameToIndex.get(source), heuristicDistance.get(nodeNameToIndex.get(source)));
        minHeap.add(new Pair(distanceFromTarget.get(nodeNameToIndex.get(source)), nodeNameToIndex.get(source)));
        while (!minHeap.isEmpty()) {
            Pair currentPair = minHeap.poll();
            nodesPopped++;
            if (visited.contains(currentPair.getVertex())) {
                continue;
            }
            nodesGenerated++;
            if (currentPair.getVertex() == nodeNameToIndex.get(target)) {
                System.out.println("Nodes Popped: " + nodesPopped);
                System.out.println("Nodes Expanded: " + nodesExpanded);
                System.out.println("Nodes Generated: " + nodesGenerated);
                printPathSrcTarget(source, target, previous, nodeNameToIndex, nodeIndexToName, distanceBetweenNodes);
                return;
            }
            visited.add(currentPair.getVertex());

            for (Pair neighbor: adjMap.get(currentPair.getVertex())) {
                if (!(visited.contains(neighbor.getVertex()))) {
                    float calcDistance = distanceFromSource.get(currentPair.getVertex()) + distanceBetweenNodes[currentPair.getVertex()][neighbor.getVertex()]; // g distance
                    float calcHeuristicDist = calcDistance + heuristicDistance.get(neighbor.getVertex()); // f = g + h
                    nodesExpanded++;
                    nodesGenerated++;
                    if (calcHeuristicDist < distanceFromTarget.get(neighbor.getVertex())) {   // if current f < previous f distance
                        distanceFromTarget.put(neighbor.getVertex(), calcHeuristicDist);
                        distanceFromSource.put(neighbor.getVertex(), calcDistance);
                        previous.put(neighbor.getVertex(), currentPair.getVertex());          // we visited neighbor via currentPair
                        minHeap.add(new Pair(calcHeuristicDist, neighbor.getVertex()));
                    }
                }
            }
        }

        System.out.println("Nodes Popped: " + nodesPopped);
        System.out.println("Nodes Expanded: " + nodesExpanded);
        System.out.println("Nodes Generated: " + nodesGenerated);
        printPathSrcTarget(source, target, previous, nodeNameToIndex, nodeIndexToName, distanceBetweenNodes);
    }

    public List<List<String>> readInput(String filePath) {
        List<List<String>> lines = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            while (line != null && !line.equals("END OF INPUT")) {
                // split each line on whitespace & store into a list of Strings
                List<String> currentLine = Arrays.asList(line.split("\\s+"));
                lines.add(currentLine);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    } 

}

public class GraphSearch {

    public static void main(String[] args) {
        Search search = new Search();
        if (args.length < 3) {
            System.out.println("The program has fewer arguments than expected!");
        }
        String inputFilePath = args[0];
        String sourceNode = args[1];
        String targetNode = args[2];
        List<List<String>> data = search.readInput(inputFilePath);
        if (args.length == 4) {
            String informedSearchFilePath = args[3];
            List<List<String>> heuristicEstimates = search.readInput(informedSearchFilePath);
            search.informedSearch(data, sourceNode, targetNode, heuristicEstimates);
        } else {
            search.uninformedSearch(data, sourceNode, targetNode);
        }
    }
}