import java.util.*;

public class ListGraph<T> implements Graph<T> {

    private Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<Edge<T>>());
    }

    public void remove(T node) {
        if(!nodes.containsKey(node)) {
            throw new NoSuchElementException();
        }
        for(Edge<T> edge:nodes.get(node)) {
            nodes.get(edge.getDestination()).remove(getEdgeBetween(edge.getDestination(), node));
        }
        nodes.get(node).clear();
        nodes.remove(node);
    }

    public void connect(T node1, T node2, String name, int weight) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException();
        }
        if (weight < 0) {
            throw new IllegalArgumentException();
        }
        if(getEdgeBetween(node1, node2) != null) {
            throw new IllegalStateException();
        }
        Edge<T> edge1 = new Edge<T>(node1, weight, name);
        Edge<T> edge2 = new Edge<T>(node2, weight, name);
        nodes.get(node1).add(edge2);
        nodes.get(node2).add(edge1);
    }

    public void disconnect(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException();
        }
        if(getEdgeBetween(node1, node2) == null || getEdgeBetween(node2, node1) == null) {
            throw new IllegalStateException();
        }
        nodes.get(node1).remove(getEdgeBetween(node1, node2));
        nodes.get(node2).remove(getEdgeBetween(node2, node1));
    }

    public void setConnectionWeight(T node1, T node2, int newWeight) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2) || getEdgeBetween(node1, node2) == null || getEdgeBetween(node2, node1) == null) {
            throw new NoSuchElementException();
        }
        if (newWeight < 0) {
            throw new IllegalArgumentException();
        }
        getEdgeBetween(node1, node2).setWeight(newWeight);
        getEdgeBetween(node2, node1).setWeight(newWeight);
    }

    public Set<T> getNodes() {
        return new HashSet<T>(nodes.keySet());
    }

    public Collection<Edge<T>> getEdgesFrom(T node) {
        if(!nodes.containsKey(node)) {
            throw new NoSuchElementException();
        }
        return new HashSet<Edge<T>>(nodes.get(node));
    }

    public Edge<T> getEdgeBetween(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            throw new NoSuchElementException();
        }
        for(Edge<T> edge:nodes.get(node1)) {
            if (edge.getDestination().equals(node2)) {
                return edge;
            }
        }
        return null;
    }

    public String toString() {
        String s = "";
        for(T node:nodes.keySet()) {
            s += node.toString() + ":\n";
            for(Edge<T> edge:nodes.get(node)) {
                s += edge.toString() + "\n";
            }
        }
        return s;
    }

    public boolean pathExists(T node1, T node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
            return false;
        }
        Set<T> visited = new HashSet<T>();
        depthFirstSearch(node1, visited);
        return visited.contains(node2);
    }

    private void depthFirstSearch(T where, Set<T> visited) {
        visited.add(where);
        for(Edge<T> edge:nodes.get(where)) {
            if(!visited.contains(edge.getDestination())) {
                depthFirstSearch(edge.getDestination(), visited);
            }
        }

    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> via){
        List<Edge<T>> path = new ArrayList<>();
        T where = to;
        while(where != from) {
            path.add(getEdgeBetween(where, via.get(where)));
            where = via.get(where);
        }
        Collections.reverse(path);
        return path;
    }

    public List<Edge<T>> getPath(T node1, T node2){
        LinkedList<T> que = new LinkedList<T>();
        Set<T> visited = new HashSet<T>();
        Map<T, T> via = new HashMap<>();
        visited.add(node1);
        que.addLast(node1);
        while(!que.isEmpty()) {
            T from = que.poll();
            for(Edge<T> edge:nodes.get(from)) {
                T to = edge.getDestination();
                if(!visited.contains(to)) {
                    visited.add(to);
                    que.add(to);
                    via.put(to, from);
                }
            }
        }
        if(!visited.contains(node2)) {
            return null;
        }
        return gatherPath(node1, node2, via);
    }
}