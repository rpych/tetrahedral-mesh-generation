package app;

import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;

public class Reader {
    public static void main(String[] args) {
        Reader reader = new Reader();
        try {
            ModelGraph graph = reader.readGraphFromMgf(args[0]);
            graph.display();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ModelGraph readGraphFromMgf(String path) throws IOException {
        ModelGraph graph = new ModelGraph("Graph");

        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            Map<Character, List<String>> entities = stream.collect(Collectors.groupingBy(line -> line.charAt(0)));
            Map<String, Vertex> vertices = entities.get('N').stream()
                    .map(node -> node.split(","))
                    .collect(Collectors.toMap(n -> n[1],
                            n -> graph.insertVertex(n[1],
                                    VertexType.SIMPLE_NODE,
                                    new Point3d(parseDouble(n[2]), parseDouble(n[3]), parseDouble(n[4])), parseBoolean(n[5]))));
            Map<String, InteriorNode> interiors = entities.get('H').stream()
                    .map(node -> node.split(","))
                    .collect(Collectors.toMap(n -> n[1],
                            n -> graph.insertInterior(n[1],
                                    new Point3d(parseDouble(n[2]), parseDouble(n[3]), parseDouble(n[4])), parseBoolean(n[5]))));
            entities.get('E').stream().map(edge -> edge.split(",")).forEach(e -> {
                if (interiors.containsKey(e[2])) {
                    graph.insertEdge(e[1], interiors.get(e[2]), vertices.get(e[3]), parseBoolean(e[4]), "fill-color: pink;");
                } else if (interiors.containsKey(e[3])) {
                    graph.insertEdge(e[1], vertices.get(e[2]), interiors.get(e[3]), parseBoolean(e[4]), "fill-color: pink;");
                } else if (vertices.containsKey(e[2]) && vertices.containsKey(e[3])) {
                    graph.insertEdge(e[1], vertices.get(e[2]), vertices.get(e[3]), parseBoolean(e[4]));
                } else {
                    System.err.println("Edge not found: " + e[1]);
                }
            });
        }

        return graph;
    }

    private boolean parseBoolean(String bool) {
        return bool.equals("true");
    }
}
