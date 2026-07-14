import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Обобщённая навигация по истории состояний (стек с возможностью двигаться
 * вперёд и назад). Разработана Харченко Я.К. на итерации 0 для заглушечных
 * состояний; на итерации 1 используется по прямому назначению — хранит
 * снимки состояния алгоритма Флойда-Уоршелла ({@code GuiController.StepSnapshot})
 * после каждого шага, что и даёт возможность релизовать «Шаг назад» (сам
 * алгоритм Гриценко умеет шагать только вперёд).
 *
 * @param <T> тип состояния, хранимого в истории
 */
public class StepHistory<T> {

    private final List<T> states = new ArrayList<>();
    private int currentIndex = -1;

    public void push(T state) {
        Objects.requireNonNull(state, "state не может быть null");
        states.add(state);
        currentIndex = states.size() - 1;
    }

    public T stepForward() {
        if (!canStepForward()) {
            throw new IllegalStateException("Нет следующего состояния: currentIndex=" + currentIndex);
        }
        currentIndex++;
        return current();
    }

    public T stepBackward() {
        if (!canStepBackward()) {
            throw new IllegalStateException("Нет предыдущего состояния: currentIndex=" + currentIndex);
        }
        currentIndex--;
        return current();
    }

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
