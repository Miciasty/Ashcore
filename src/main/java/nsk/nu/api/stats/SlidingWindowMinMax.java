package nsk.nu.api.stats;

import java.util.ArrayDeque;
import java.util.Deque;

/** O(1) amortized sliding min/max over the last N samples. */
public final class SlidingWindowMinMax {
    private final int window;
    private int index = 0;
    private final Deque<Node> minQ = new ArrayDeque<>();
    private final Deque<Node> maxQ = new ArrayDeque<>();
    private record Node(int idx, double val){}

    public SlidingWindowMinMax(int window){
        if (window <= 0) throw new IllegalArgumentException("window > 0");
        this.window = window;
    }

    /** Adds x at the next position. */
    public void add(double x){
        while (!minQ.isEmpty() && minQ.peekLast().val() >= x) minQ.removeLast();
        while (!maxQ.isEmpty() && maxQ.peekLast().val() <= x) maxQ.removeLast();
        minQ.addLast(new Node(index, x));
        maxQ.addLast(new Node(index, x));

        int cutoff = index - window + 1;
        while (!minQ.isEmpty() && minQ.peekFirst().idx() < cutoff) minQ.removeFirst();
        while (!maxQ.isEmpty() && maxQ.peekFirst().idx() < cutoff) maxQ.removeFirst();
        index++;
    }
    public double min(){ return minQ.isEmpty() ? Double.NaN : minQ.peekFirst().val(); }
    public double max(){ return maxQ.isEmpty() ? Double.NaN : maxQ.peekFirst().val(); }
}