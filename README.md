# Java Code Stepper

Java Code Stepper is a visual Java code execution tracer built with Java Swing and JavaParser. It allows you to paste Java code and step through it line-by-line while visualizing the variable state, call stack, and console output.

## Features

- **Visual Stepping:** Step forward and backward through the execution of your Java code.
- **Dark IDE Theme:** Beautiful dark interface inspired by modern IDEs.
- **Variable Watcher:** Real-time table showing all variables in scope, with changes highlighted.
- **Call Stack Viewer:** Real-time view of the method call stack.
- **Console Output:** Real-time console log.
- **Supported Java Subset:** Includes variables, math, if/else, for/while/do-while loops, recursion, 1D/2D arrays, basic strings, ArrayList, and HashMap.
- **Built-in Examples:** Preloaded with classic examples like Bubble Sort, Fibonacci, and FizzBuzz.

## Requirements

- **Java Development Kit (JDK):** Version 17 or higher.
- **Maven:** To build the project and download the JavaParser dependency.

## How to Compile

Navigate to the directory containing this `README.md` and `pom.xml`, and run:

```bash
mvn clean package
```

This will download the `javaparser` dependency and compile the application into a single "fat JAR" with dependencies included.

## How to Run

After compiling, run the application using the generated JAR file in the `target` directory:

```bash
java -jar target/java-code-stepper-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Alternatively, if you are using an IDE like IntelliJ IDEA, Eclipse, or VS Code, you can simply open the project and run the main class directly:
`com.codestepper.Main`

## Usage

1. Launch the application.
2. Click **Paste / Load Code** in the bottom left.
3. Select an example from the dropdown, or paste your own code.
4. Click **Analyze & Load**.
5. Use the control bar at the bottom to step through the code (**Next Step ▶**, **◀ Prev Step**), or turn on the **Auto-step** checkbox to run it automatically.
