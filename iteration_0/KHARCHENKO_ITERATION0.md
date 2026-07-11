# Итерация 0 — Харченко Я.К. (Контроллер/Ввод)

## Что реализовано

- **`StepHistory<T>`** (`src/main/java/StepHistory.java`) — обобщённый стек истории состояний: `push`, `stepForward`, `stepBackward`, `current`, `canStepForward`, `canStepBackward`. Тип `T` намеренно обобщённый — в итерации 1 сюда подключатся настоящие шаги алгоритма от Гриценко.
- **`StepDemoWindow`** (там же) — отдельное Swing-окно с кнопками «Загрузить», «Шаг вперёд», «Шаг назад» и областью текущего состояния. Кнопки шагов реально листают 5 заглушечных состояний и блокируются на границах истории. Кнопка «Загрузить» — только элемент интерфейса, по клику пишет в лог, что загрузка будет реализована в итерации 1.
- Тесты `StepHistoryTest` (переходы вперёд/назад, поведение на границах, исключения).

Файлы без package (лежат прямо в `src/main/java/` и `src/test/java/`), без внешних зависимостей на этапе компиляции/запуска основного кода.

## Что сознательно не сделано (перенесено на итерацию 1)

- Загрузка файла графа и парсинг в модель.
- Таблица матрицы `dist`.
- Интеграция с окном Кузьмина и алгоритмом Гриценко.

## Как собрать и запустить

```bash
javac -d out src/main/java/StepHistory.java src/main/java/StepDemoWindow.java
java -cp out StepDemoWindow
```

Проверка вручную: кнопки «Шаг вперёд»/«Шаг назад» листают состояния 0–4, неактивны на соответствующих границах; «Загрузить» пишет сообщение в лог.

## Как запустить тесты

Нужен один jar-файл (консольный раннер JUnit 5), скачать один раз и положить в корень проекта:

```
https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar
```

Дальше:

```bash
javac -d out src/main/java/StepHistory.java
javac -cp "out:junit-platform-console-standalone-1.10.2.jar" -d out src/test/java/StepHistoryTest.java
java -jar junit-platform-console-standalone-1.10.2.jar execute -cp out --select-class StepHistoryTest
```

Ожидаемый результат: 6 тестов, `tests successful`, `0 failed`.
