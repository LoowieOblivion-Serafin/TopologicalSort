package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import util.CSVHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Aplicación principal que maneja la interfaz gráfica del sistema
 * de prerrequisitos de cursos usando JavaFX
 */
public class MainApplication extends Application {
    private TextArea outputArea;
    private CourseGraph courseGraph;
    private List<Course> lastSortedCourses;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Prerrequisitos de Cursos - Ordenamiento Topológico");

        // Crear el layout principal
        BorderPane root = new BorderPane();

        // Crear la barra de menú
        MenuBar menuBar = createMenuBar(primaryStage);

        // Crear el área de contenido principal
        VBox centerContent = createCenterContent();

        // Configurar el layout
        root.setTop(menuBar);
        root.setCenter(centerContent);

        // Crear y mostrar la escena
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Mostrar mensaje de bienvenida
        showWelcomeMessage();
    }

    /**
     * Crea la barra de menú con todas las opciones disponibles
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // Menú Archivo
        Menu fileMenu = new Menu("Archivo");
        MenuItem loadItem = new MenuItem("Cargar CSV...");
        MenuItem exportItem = new MenuItem("Exportar Resultado...");
        MenuItem exitItem = new MenuItem("Salir");

        loadItem.setOnAction(e -> loadCSVFile(stage));
        exportItem.setOnAction(e -> exportResults(stage));
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(loadItem, exportItem, new SeparatorMenuItem(), exitItem);

        // Menú Procesar
        Menu processMenu = new Menu("Procesar");
        MenuItem generateItem = new MenuItem("Generar Ordenamiento Topológico");
        MenuItem clearItem = new MenuItem("Limpiar Resultados");

        generateItem.setOnAction(e -> generateTopologicalSort());
        clearItem.setOnAction(e -> clearOutput());

        processMenu.getItems().addAll(generateItem, clearItem);

        // Menú Ayuda
        Menu helpMenu = new Menu("Ayuda");
        MenuItem aboutItem = new MenuItem("Acerca de...");
        MenuItem formatItem = new MenuItem("Formato CSV");

        aboutItem.setOnAction(e -> showAboutDialog());
        formatItem.setOnAction(e -> showFormatHelp());

        helpMenu.getItems().addAll(formatItem, aboutItem);

        menuBar.getMenus().addAll(fileMenu, processMenu, helpMenu);
        return menuBar;
    }

    /**
     * Crea el contenido central de la aplicación
     */
    private VBox createCenterContent() {
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(10));

        // Título
        Label titleLabel = new Label("Resultados del Ordenamiento Topológico");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Área de texto para mostrar resultados
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(25);
        outputArea.setStyle("-fx-font-family: 'Courier New', monospace;");

        // Scroll pane para el área de texto
        ScrollPane scrollPane = new ScrollPane(outputArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        centerContent.getChildren().addAll(titleLabel, scrollPane);
        return centerContent;
    }

    /**
     * Muestra mensaje de bienvenida al iniciar la aplicación
     */
    private void showWelcomeMessage() {
        outputArea.setText("=== SISTEMA DE PRERREQUISITOS DE CURSOS ===\n\n");
        outputArea.appendText("Bienvenido al sistema de ordenamiento topológico de cursos.\n\n");
        outputArea.appendText("INSTRUCCIONES:\n");
        outputArea.appendText("1. Vaya a Archivo → Cargar CSV para seleccionar su archivo de cursos\n");
        outputArea.appendText("2. Use Procesar → Generar Ordenamiento para obtener la secuencia de cursos\n");
        outputArea.appendText("3. Puede exportar los resultados usando Archivo → Exportar Resultado\n\n");
        outputArea.appendText("Para ver el formato requerido del CSV, vaya a Ayuda → Formato CSV\n");
    }

    /**
     * Carga un archivo CSV con la información de los cursos
     */
    private void loadCSVFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV de cursos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                outputArea.setText("Cargando archivo: " + file.getName() + "\n\n");

                // Leer cursos del archivo CSV
                List<Course> courses = CSVHandler.readCoursesFromCSV(file.getPath());
                courseGraph = new CourseGraph();

                // Construir el grafo de dependencias
                outputArea.appendText("Construyendo grafo de dependencias...\n");
                for (Course course : courses) {
                    courseGraph.addCourse(course);
                }

                // Agregar las relaciones de prerrequisitos
                for (Course course : courses) {
                    for (String prereq : course.getPrerequisites()) {
                        courseGraph.addPrerequisite(course.getCode(), prereq);
                    }
                }

                outputArea.appendText("✓ Archivo cargado exitosamente\n");
                outputArea.appendText("✓ " + courses.size() + " cursos encontrados\n\n");

                // Mostrar resumen de cursos cargados
                outputArea.appendText("CURSOS CARGADOS:\n");
                outputArea.appendText("================\n");
                for (Course course : courses) {
                    outputArea.appendText(course.getCode() + " - " + course.getName());
                    if (!course.getPrerequisites().isEmpty()) {
                        outputArea.appendText(" (Prerrequisitos: " +
                                String.join(", ", course.getPrerequisites()) + ")");
                    }
                    outputArea.appendText("\n");
                }
                outputArea.appendText("\nAhora puede generar el ordenamiento topológico.\n");

            } catch (Exception e) {
                outputArea.setText("❌ ERROR al cargar el archivo:\n");
                outputArea.appendText(e.getMessage() + "\n\n");
                outputArea.appendText("Verifique que el archivo tenga el formato correcto.\n");
                outputArea.appendText("Use Ayuda → Formato CSV para ver el formato requerido.");
            }
        }
    }

    /**
     * Genera el ordenamiento topológico de los cursos
     */
    private void generateTopologicalSort() {
        if (courseGraph == null) {
            outputArea.setText("❌ ERROR: No hay cursos cargados.\n");
            outputArea.appendText("Por favor, cargue un archivo CSV primero usando Archivo → Cargar CSV");
            return;
        }

        try {
            outputArea.setText("Procesando ordenamiento topológico...\n\n");

            // Ejecutar el algoritmo de ordenamiento topológico
            lastSortedCourses = courseGraph.topologicalSort();

            outputArea.appendText("✓ Ordenamiento completado exitosamente\n\n");
            outputArea.appendText("=== ORDEN SUGERIDO DE CURSOS ===\n");
            outputArea.appendText("=================================\n\n");

            // Mostrar la secuencia ordenada
            for (int i = 0; i < lastSortedCourses.size(); i++) {
                Course course = lastSortedCourses.get(i);
                outputArea.appendText(String.format("%2d. %s - %s\n",
                        (i + 1), course.getCode(), course.getName()));
            }

            outputArea.appendText("\n=== ANÁLISIS ===\n");
            outputArea.appendText("Total de cursos: " + lastSortedCourses.size() + "\n");
            outputArea.appendText("✓ No se detectaron ciclos en los prerrequisitos\n");
            outputArea.appendText("✓ La secuencia respeta todas las dependencias\n\n");
            outputArea.appendText("Puede exportar estos resultados usando Archivo → Exportar Resultado");

        } catch (Exception e) {
            outputArea.setText("❌ ERROR en el ordenamiento topológico:\n\n");
            outputArea.appendText(e.getMessage() + "\n\n");
            outputArea.appendText("POSIBLES CAUSAS:\n");
            outputArea.appendText("• Existe un ciclo en los prerrequisitos\n");
            outputArea.appendText("• Un curso hace referencia a un prerrequisito que no existe\n");
            outputArea.appendText("• Error en el formato del archivo CSV\n\n");
            outputArea.appendText("Revise los datos y vuelva a intentar.");
        }
    }

    /**
     * Exporta los resultados a un archivo de texto
     */
    private void exportResults(Stage stage) {
        if (lastSortedCourses == null || lastSortedCourses.isEmpty()) {
            showAlert("No hay resultados para exportar",
                    "Primero debe generar el ordenamiento topológico.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar resultados");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
        );
        fileChooser.setInitialFileName("ordenamiento_cursos.txt");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("ORDEN SUGERIDO DE CURSOS\n");
                writer.write("========================\n\n");

                for (int i = 0; i < lastSortedCourses.size(); i++) {
                    Course course = lastSortedCourses.get(i);
                    writer.write(String.format("%2d. %s - %s\n",
                            (i + 1), course.getCode(), course.getName()));
                }

                writer.write("\nTotal de cursos: " + lastSortedCourses.size() + "\n");
                writer.write("Generado por: Sistema de Prerrequisitos de Cursos\n");

                showAlert("Exportación exitosa",
                        "Los resultados se guardaron en: " + file.getAbsolutePath());

            } catch (IOException e) {
                showAlert("Error al exportar",
                        "No se pudo guardar el archivo: " + e.getMessage());
            }
        }
    }

    /**
     * Limpia el área de resultados
     */
    private void clearOutput() {
        showWelcomeMessage();
    }

    /**
     * Muestra información sobre el formato del archivo CSV
     */
    private void showFormatHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Formato del archivo CSV");
        alert.setHeaderText("Formato requerido para el archivo CSV");

        String content = "El archivo CSV debe tener las siguientes columnas:\n\n" +
                "1. Código del curso (ej: CS101)\n" +
                "2. Nombre del curso (ej: Introducción a la Programación)\n" +
                "3. Prerrequisitos (códigos separados por punto y coma)\n\n" +
                "EJEMPLO:\n" +
                "Código,Nombre,Prerrequisitos\n" +
                "CS101,Introducción a la Programación,\n" +
                "MA101,Matemáticas Básicas,\n" +
                "CS201,Estructuras de Datos,CS101;MA101\n" +
                "CS301,Algoritmos,CS201\n\n" +
                "NOTAS:\n" +
                "• La primera fila debe contener los encabezados\n" +
                "• Si un curso no tiene prerrequisitos, deje la columna vacía\n" +
                "• Use punto y coma (;) para separar múltiples prerrequisitos";

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra información sobre la aplicación
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Sistema de Prerrequisitos de Cursos");
        alert.setContentText("Versión 1.0\n\n" +
                "Esta aplicación utiliza el algoritmo de ordenamiento topológico\n" +
                "para determinar un orden válido en el que un estudiante\n" +
                "puede tomar los cursos respetando todos los prerrequisitos.\n\n" +
                "Desarrollado con Java y JavaFX\n" +
                "Algoritmo: Ordenamiento Topológico (Algoritmo de Kahn)");
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de alerta
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
