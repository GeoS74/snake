# Snake

java 24.0.1 2025-04-15
javafx-sdk-24.0.1

Перед сборкой скачать библиотеку JavaFX, скопировать папку `lib` в корень проекта.

Дли Windows надо также скопировать папку `bin` в корень проекта.

### Сборка проекта

```bash
javac -encoding utf-8 --module-path lib/ --add-modules javafx.controls -d build/ src/main/java/com/geos/snake/*.java
```

### Сборка JAR

```bash
jar cvfm application/app.jar META-INF/MANIFEST.MF -C build/ . image/
```

###  Запуск JAR

```bash
java --module-path lib/ --add-modules javafx.controls -jar application/app.jar
```

