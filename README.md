# Simple3DViewer - Продвинутый 3D-редактор

## Описание проекта

Simple3DViewer - это мощное JavaFX-приложение для просмотра и редактирования 3D моделей. Проект реализует объектно-ориентированную архитектуру с поддержкой загрузки/сохранения моделей, системы сцен, обработки ошибок и расширяемой архитектурой.

## Архитектура

### Система ввода/вывода (IO)

#### ModelLoader - Абстрактный загрузчик моделей
```java
public abstract class ModelLoader {
    public final Model load(Path path) throws ModelLoadingException
    public final Model loadFromContent(String content) throws ModelLoadingException
    protected abstract boolean supportsExtension(Path path)
    protected abstract Model parseContent(String content) throws ModelLoadingException
    public abstract String[] getSupportedExtensions()
}
```

**Реализации:**
- **ObjLoader** - загрузчик моделей в формате OBJ
- Поддерживает вершины, нормали, текстурные координаты
- Полная валидация данных
- Обработка ошибок с указанием строки

#### ModelSaver - Абстрактный сохранитель моделей
```java
public abstract class ModelSaver {
    public final void save(Model model, Path path) throws ModelSavingException
    public final void save(Model model, Path path, SaveSettings settings) throws ModelSavingException
    protected abstract String generateContent(Model model, SaveSettings settings) throws ModelSavingException
    protected abstract boolean supportsExtension(Path path)
    public abstract String[] getSupportedExtensions()
}
```

**Реализации:**
- **ObjSaver** - сохранитель моделей в формате OBJ
- Настраиваемая точность чисел
- Управление экспортом нормалей и текстурных координат

#### ModelIOFactory - Фабрика загрузчиков/сохранителей
```java
public class ModelIOFactory {
    public static ModelLoader createLoader(Path path) throws ModelIOFactoryException
    public static ModelSaver createSaver(Path path) throws ModelIOFactoryException
    public static boolean supportsLoading(Path path)
    public static boolean supportsSaving(Path path)
}
```

### Система сцен

#### SceneElement - Базовый элемент сцены
```java
public abstract class SceneElement {
    public abstract Vector3f getPosition()
    public abstract void setPosition(Vector3f position)
    public abstract BoundingBox getBoundingBox()
    public void update(float deltaTime)
    public void dispose()
}
```

#### SceneModel - Модель в сцене
```java
public class SceneModel extends SceneElement {
    public Model getModel()
    public Vector3f getRotation()
    public Vector3f getScale()
    public void translate(Vector3f delta)
    public void rotate(Vector3f deltaRotation)
    public void scaleBy(float scaleFactor)
}
```

#### Scene - Менеджер сцены
```java
public class Scene {
    public boolean addElement(SceneElement element)
    public boolean removeElement(SceneElement element)
    public List<SceneElement> getElements()
    public List<SceneElement> getSelectedElements()
    public void selectElement(SceneElement element)
    public void clearSelection()
    public void addCamera(Camera camera)
}
```

#### BoundingBox - Ограничивающий параллелепипед
```java
public class BoundingBox {
    public void expandToInclude(Vector3f point)
    public boolean intersects(BoundingBox other)
    public boolean contains(Vector3f point)
    public Vector3f getCenter()
    public Vector3f getSize()
}
```

### Обработка ошибок

#### ErrorDialogs - Диалоговые окна ошибок
```java
public class ErrorDialogs {
    public static void showModelLoadingError(ModelLoadingException exception, Stage ownerStage)
    public static void showModelSavingError(ModelSavingException exception, Stage ownerStage)
    public static void showGeneralError(String title, String headerText, String contentText, Throwable exception, Stage ownerStage)
    public static Optional<ButtonType> showWarning(String title, String headerText, String contentText, Stage ownerStage)
}
```

#### Исключения
- **ModelLoadingException** - ошибки загрузки моделей
- **ModelSavingException** - ошибки сохранения моделей
- **ModelIOFactoryException** - ошибки фабрики IO

## Функциональность

### Загрузка и сохранение моделей
- Поддержка формата OBJ
- Валидация данных при загрузке
- Настраиваемые параметры сохранения
- Обработка ошибок с понятными сообщениями

### Система сцен
- Множество моделей в одной сцене
- Выбор и трансформация моделей
- Bounding box для оптимизации
- Поддержка камер

### Графический интерфейс
- JavaFX-based интерфейс
- Диалоговые окна для ошибок
- Меню для загрузки/сохранения
- Управление камерой (WASD + клавиши)

### Математическая основа
- Vector3f, Vector2f - базовые векторы
- Поддержка преобразований моделей
- Bounding box вычисления

## Использование

### Загрузка модели
```java
ModelLoader loader = ModelIOFactory.createLoader(Paths.get("model.obj"));
Model model = loader.load(Paths.get("model.obj"));

SceneModel sceneModel = new SceneModel("model1", "My Model", model);
scene.addElement(sceneModel);
```

### Сохранение модели
```java
ModelSaver saver = ModelIOFactory.createSaver(Paths.get("output.obj"));
saver.save(model, Paths.get("output.obj"));
```

### Работа со сценой
```java
Scene scene = new Scene("Main Scene");
scene.addElement(sceneModel);
scene.selectElement(sceneModel);

List<SceneElement> selected = scene.getSelectedElements();
```

## Расширение системы

### Добавление нового формата
1. Создать класс-наследник ModelLoader
2. Реализовать методы parseContent, supportsExtension
3. Зарегистрировать в ModelIOFactory

### Добавление нового элемента сцены
1. Создать класс-наследник SceneElement
2. Реализовать методы getPosition, setPosition, getBoundingBox
3. Добавить в сцену через scene.addElement()

## Сборка и запуск

### Сборка
```bash
mvn clean compile
```

### Тестирование
```bash
mvn test
```

### Создание JAR
```bash
mvn package
```

### Запуск
```bash
java -cp target/classes com.cgvsu.Main
```

## Структура проекта

```
src/
├── main/java/com/cgvsu/
│   ├── io/                 # Система ввода/вывода
│   │   ├── ModelLoader.java
│   │   ├── ModelSaver.java
│   │   ├── ObjLoader.java
│   │   ├── ObjSaver.java
│   │   └── ModelIOFactory.java
│   ├── scene/              # Система сцен
│   │   ├── Scene.java
│   │   ├── SceneElement.java
│   │   ├── SceneModel.java
│   │   └── BoundingBox.java
│   ├── ui/                 # Пользовательский интерфейс
│   │   └── ErrorDialogs.java
│   ├── math/               # Математические классы
│   │   ├── Vector3f.java
│   │   └── Vector2f.java
│   ├── model/              # Модель данных
│   │   ├── Model.java
│   │   └── Polygon.java
│   ├── render_engine/      # Движок рендеринга
│   │   ├── RenderEngine.java
│   │   ├── Camera.java
│   │   └── GraphicConveyor.java
│   ├── GuiController.java  # Контроллер интерфейса
│   ├── Simple3DViewer.java # Главный класс приложения
│   └── Main.java
└── test/java/com/cgvsu/io/  # Тесты
    ├── ObjLoaderTest.java
    └── ObjSaverTest.java
```

## Тестирование

Проект содержит комплексные unit-тесты:
- **ObjLoaderTest** - 12 тестов загрузки OBJ файлов
- **ObjSaverTest** - тесты сохранения моделей

Запуск тестов:
```bash
mvn test -Dtest=ObjLoaderTest
mvn test -Dtest=ObjSaverTest
```

## Будущие улучшения

### Для первого разработчика
- Удаление частей модели (вершин/полигонов)
- Современный интерфейс с темами
- Финальный деплой приложения

### Для второго разработчика
- Собственная математическая библиотека (замена vecmath)
- Переделка на вектора-столбцы
- Аффинные преобразования в GUI

### Для третьего разработчика
- Триангуляция и вычисление нормалей
- Растеризация полигонов с Z-буфером
- Текстурирование и освещение
- Режимы отрисовки

## Технологии

- **Java 17** - основной язык
- **JavaFX** - графический интерфейс
- **Maven** - система сборки
- **JUnit 5** - тестирование
- **vecmath** - временная математическая библиотека

## Авторы

Проект разработан командой из трех человек:
1. **Зеленев Иван** - UI/UX + Сцена + Загрузка моделей
2. **Карпов Александр** - Математическая основа + Трансформации
3. **Зеленев Иван** - Рендеринг + Освещение + Камеры
4. Добавление литера и тестов плюс в планах создание собственного пайплайна

## Лицензия

Проект разработан в образовательных целях.