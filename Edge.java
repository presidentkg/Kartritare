public class Edge<T> {
    private T destination;
    private int weight;
    private String name;

    public Edge(T destination, int weight, String name) {
        this.destination = destination;
        this.weight = weight;
        this.name = name;
    }
    public T getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int newWeight) {
        weight = newWeight;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "to " + destination.toString() + " by " + name + " takes " + weight;
    }
}