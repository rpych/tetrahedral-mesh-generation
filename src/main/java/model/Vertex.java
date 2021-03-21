package model;

public class Vertex extends GraphNode {

    private static final String VERTEX_SYMBOL = "V";

    public Vertex(Graph graph, String id, Coordinates coordinates) {
        super(graph, id, VERTEX_SYMBOL, coordinates);
    }

    public static class VertexBuilder {

        private final ModelGraph graph;

        private final String id;

        private double xCoordinate;

        private double yCoordinate;

        private double zCoordinate;

        public VertexBuilder(ModelGraph graph, String id) {
            this.graph = graph;
            this.id = id;
        }

        public VertexBuilder setCoordinates(Coordinates coordinates){
            this.xCoordinate = coordinates.getX();
            this.yCoordinate = coordinates.getY();
            this.zCoordinate = coordinates.getZ();
            return this;
        }

        public VertexBuilder setXCoordinate(double xCoordinate) {
            this.xCoordinate = xCoordinate;
            return this;
        }

        public VertexBuilder setYCoordinate(double yCoordinate) {
            this.yCoordinate = yCoordinate;
            return this;
        }

        public VertexBuilder setZCoordinate(double zCoordinate) {
            this.zCoordinate = zCoordinate;
            return this;
        }

        public Vertex build() {
            return new Vertex(graph, id, new Coordinates(xCoordinate, yCoordinate, zCoordinate));
        }
    }

}
