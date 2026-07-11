import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepHistoryTest {

    @Test
    void newHistoryIsEmpty() {
        StepHistory<String> history = new StepHistory<>();
        assertTrue(history.isEmpty());
        assertEquals(0, history.size());
        assertFalse(history.canStepForward());
        assertFalse(history.canStepBackward());
        assertThrows(IllegalStateException.class, history::current);
    }

    @Test
    void pushAddsStateAndMovesToIt() {
        StepHistory<String> history = new StepHistory<>();
        history.push("a");
        history.push("b");

        assertEquals(2, history.size());
        assertEquals("b", history.current());
        assertFalse(history.canStepForward());
        assertTrue(history.canStepBackward());
    }

    @Test
    void stepBackwardAndForwardNavigateHistory() {
        StepHistory<String> history = new StepHistory<>();
        history.push("шаг 0");
        history.push("шаг 1");
        history.push("шаг 2");

        assertEquals("шаг 2", history.current());

        assertEquals("шаг 1", history.stepBackward());
        assertEquals("шаг 0", history.stepBackward());
        assertFalse(history.canStepBackward());

        assertEquals("шаг 1", history.stepForward());
        assertEquals("шаг 2", history.stepForward());
        assertFalse(history.canStepForward());
    }

    @Test
    void stepForwardAtEndThrows() {
        StepHistory<String> history = new StepHistory<>();
        history.push("единственное состояние");

        assertFalse(history.canStepForward());
        assertThrows(IllegalStateException.class, history::stepForward);
    }

    @Test
    void stepBackwardAtStartThrows() {
        StepHistory<String> history = new StepHistory<>();
        history.push("a");
        history.push("b");
        history.stepBackward();

        assertFalse(history.canStepBackward());
        assertThrows(IllegalStateException.class, history::stepBackward);
    }

    @Test
    void currentIndexValueTracksPosition() {
        StepHistory<String> history = new StepHistory<>();
        history.push("a");
        history.push("b");
        history.push("c");

        assertEquals(2, history.currentIndexValue());
        history.stepBackward();
        assertEquals(1, history.currentIndexValue());
    }
}
