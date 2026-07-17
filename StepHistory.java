import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// история состояний для шагов вперёд/назад
public class StepHistory<T> {

    private final List<T> states = new ArrayList<>();
    private int currentIndex = -1;

    // добавить состояние в историю
    public void push(T state) {
        Objects.requireNonNull(state, "state не может быть null");
        states.add(state);
        currentIndex = states.size() - 1;
    }

    // шаг вперёд
    public T stepForward() {
        if (!canStepForward()) {
            throw new IllegalStateException("Нет следующего состояния: currentIndex=" + currentIndex);
        }
        currentIndex++;
        return current();
    }

    // шаг назад
    public T stepBackward() {
        if (!canStepBackward()) {
            throw new IllegalStateException("Нет предыдущего состояния: currentIndex=" + currentIndex);
        }
        currentIndex--;
        return current();
    }

    // текущее состояние
    public T current() {
        if (currentIndex < 0 || currentIndex >= states.size()) {
            throw new IllegalStateException("История состояний пуста");
        }
        return states.get(currentIndex);
    }

    public boolean canStepForward() {
        return currentIndex < states.size() - 1;
    }

    public boolean canStepBackward() {
        return currentIndex > 0;
    }

    public int currentIndexValue() {
        return currentIndex;
    }

    public int size() {
        return states.size();
    }

    public boolean isEmpty() {
        return states.isEmpty();
    }
}