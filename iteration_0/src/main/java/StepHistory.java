import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Обобщённая навигация по истории состояний (стек с возможностью двигаться
 * вперёд и назад), не завязанная на конкретный тип шага алгоритма.
 *
 * Итерация 0 (Харченко Я.К.): используется с заглушечными состояниями
 * (например, {@code String}). В итерации 1 сюда будут приходить настоящие
 * шаги алгоритма Флойда-Уоршелла от Гриценко — тип {@code T} намеренно
 * обобщённый, чтобы не переписывать этот класс при интеграции.
 *
 * @param <T> тип состояния, хранимого в истории
 */
public class StepHistory<T> {

    private final List<T> states = new ArrayList<>();
    private int currentIndex = -1;

    /**
     * Добавляет новое состояние в конец истории и делает его текущим.
     *
     * @param state состояние (не может быть {@code null})
     */
    public void push(T state) {
        Objects.requireNonNull(state, "state не может быть null");
        states.add(state);
        currentIndex = states.size() - 1;
    }

    /**
     * Переходит на одно состояние вперёд, если это возможно.
     *
     * @return новое текущее состояние
     * @throws IllegalStateException если двигаться вперёд некуда
     */
    public T stepForward() {
        if (!canStepForward()) {
            throw new IllegalStateException("Нет следующего состояния: currentIndex=" + currentIndex);
        }
        currentIndex++;
        return current();
    }

    /**
     * Переходит на одно состояние назад, если это возможно.
     *
     * @return новое текущее состояние
     * @throws IllegalStateException если двигаться назад некуда
     */
    public T stepBackward() {
        if (!canStepBackward()) {
            throw new IllegalStateException("Нет предыдущего состояния: currentIndex=" + currentIndex);
        }
        currentIndex--;
        return current();
    }

    /**
     * @return текущее состояние
     * @throws IllegalStateException если история пуста
     */
    public T current() {
        if (currentIndex < 0 || currentIndex >= states.size()) {
            throw new IllegalStateException("История состояний пуста");
        }
        return states.get(currentIndex);
    }

    /** @return можно ли сейчас шагнуть вперёд */
    public boolean canStepForward() {
        return currentIndex < states.size() - 1;
    }

    /** @return можно ли сейчас шагнуть назад */
    public boolean canStepBackward() {
        return currentIndex > 0;
    }

    /** @return индекс текущего состояния (0-based), -1 если история пуста */
    public int currentIndexValue() {
        return currentIndex;
    }

    /** @return количество состояний в истории */
    public int size() {
        return states.size();
    }

    /** @return true, если в истории нет ни одного состояния */
    public boolean isEmpty() {
        return states.isEmpty();
    }
}
