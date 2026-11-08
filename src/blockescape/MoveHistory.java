//generic code 

package blockescape;

import java.util.ArrayDeque;
import java.util.Deque;

public class MoveHistory<T> {
    private final Deque<T> history = new ArrayDeque<>();

    public void save(T state) {
        if (state == null) {
            throw new IllegalArgumentException(
                "State cannot be null"
            );
        }
        history.push(state);
    }

    public T undo() {
        if (history.isEmpty()) {
            return null;
        }
        return history.pop();
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public void clear() {
        history.clear();
    }

    public int size() {
        return history.size();
    }
}