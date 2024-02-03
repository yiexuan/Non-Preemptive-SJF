import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Fx extends Application {
    private TextField numProcessesField;
    private Label errorLabel;
    private TableView<Process> processTable;
    private static TextArea calculationArea;
    private VBox ganttChartArea;
    private HBox ganttChartBox;
    private VBox root; 

    private int totalTurnaroundTime;
    private double averageTurnaroundTime;
    private int totalWaitingTime;
    private double averageWaitingTime;

    public static void main(String[] args) {
        launch(args);
    }

    class Process {
        int i;
        int arrivalTime;
        int burstTime;
        int priority;
        int turnaroundTime;
        int waitingTime;
        int remainingBurstTime;

        Process(int i, int arrivalTime, int burstTime, int priority) {
            this.i = i;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
            this.turnaroundTime = 0;
            this.waitingTime = 0;
            this.remainingBurstTime = burstTime;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Non-Preemptive SJF Scheduling");

        root = new VBox(10);
        root.setPadding(new Insets(10));
        ganttChartArea = new VBox();
        ganttChartArea.setSpacing(5); // Adjust the spacing as needed
        ganttChartArea.setStyle("-fx-background-color: #ffffff;"); // Optional background color
        // UI components
        numProcessesField = new TextField();
        errorLabel = new Label();
        Button submitButton = new Button("Generate");
        processTable = new TableView<>();
        calculationArea = new TextArea();
        calculationArea.setEditable(false);
        calculationArea.setWrapText(true);
        // ganttChartArea = new TextArea();
        // ganttChartArea.setEditable(false);
        ganttChartBox = new HBox();

        // Table columns
        TableColumn<Process, String> processIdCol = new TableColumn<>("Process ID");
        processIdCol.setCellValueFactory(data -> new SimpleStringProperty("P" + data.getValue().i));
        TableColumn<Process, String> arrivalTimeCol = new TableColumn<>("Arrival Time");
        arrivalTimeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().arrivalTime)));
        TableColumn<Process, String> burstTimeCol = new TableColumn<>("Burst Time");
        burstTimeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().burstTime)));
        TableColumn<Process, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().priority)));
        TableColumn<Process, String> waitingTimeCol = new TableColumn<>("Waiting Time");
        waitingTimeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().waitingTime)));
        TableColumn<Process, String> turnaroundTimeCol = new TableColumn<>("Turnaround Time");
        turnaroundTimeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().turnaroundTime)));

        processTable.getColumns().addAll(processIdCol, arrivalTimeCol, burstTimeCol,priorityCol, waitingTimeCol, turnaroundTimeCol);
        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Layout
        //VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(
                new Label("Enter the number of processes (3-10):"),
                numProcessesField,
                errorLabel,
                submitButton,
                processTable,
                new Label("Calculation: "),
                calculationArea,
                new Label("Gantt Chart:"),
                ganttChartArea
        );

        // Event handling
        numProcessesField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Clear previous error message
            errorLabel.setText("");
            if (!newValue.matches("\\d*")) {
                // Display error message if non-numeric characters are entered
                errorLabel.setText("Please enter a valid number.");
                errorLabel.setTextFill(Color.RED);
                return;
            }

            int numProcesses = Integer.parseInt(newValue);
            if (numProcesses < 3 || numProcesses > 10) {
                // Display error message if the number is out of range
                errorLabel.setText("The number of processes must be within the range of 3 - 10");
                errorLabel.setTextFill(Color.RED);
            }
        });

        submitButton.setOnAction(e -> nonPreemptiveSJF());

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<Process> getProcessesFromUser() {
        List<Process> processes = new ArrayList<>();

        // Validate input until a valid number of processes is entered
        while (true) {
            String input = numProcessesField.getText();
            if (!input.matches("\\d*")) {
                errorLabel.setText("Please enter a valid number.");
                errorLabel.setTextFill(Color.RED);
                return processes;
            }

            int numProcesses = Integer.parseInt(input);
            if (numProcesses < 3 || numProcesses > 10) {
                errorLabel.setText("The number of processes must be within the range of 3 - 10");
                errorLabel.setTextFill(Color.RED);
                return processes;
            }

            // If the input is valid, clear the error label and proceed
            errorLabel.setText("");
            return getProcessesFromUser(numProcesses);
        }
    }
    // Helper method to get process details from the user
    private List<Process> getProcessesFromUser(int numProcesses) {
        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < numProcesses; i++) {
            int arrivalTime = getIntFromUser("Enter Arrival Time for Process " + i + ":");
            int burstTime = getIntFromUser("Enter Burst Time for Process " + i + ":");
            int priority = getIntFromUser("Enter Priority for Process " + i + ":");

            processes.add(new Process(i, arrivalTime, burstTime, priority));
        }
        return processes;
    }

    // Helper method to get an integer input from the user
    private int getIntFromUser(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setContentText(prompt);
        dialog.setHeaderText(null);
        dialog.showAndWait();
        return Integer.parseInt(dialog.getResult());
    }

    // public String toString() {
    //     return String.format("Process %d: Arrival Time=%d, Burst Time=%d, Waiting Time=%d, Turnaround Time=%d\n",
    //     i, arrivalTime, burstTime, waitingTime, turnaroundTime);
    // }
    

    private void nonPreemptiveSJF(){
        List<Process> processes = getProcessesFromUser();
        int numProcesses = Integer.parseInt(numProcessesField.getText());

              // Check if the number of processes is within the valid range
        if (numProcesses < 3 || numProcesses > 10) {
            // Display an error message or take appropriate action
            errorLabel.setText("Please enter a valid number of processes (3-10).");
            errorLabel.setTextFill(Color.RED);
            return;
        }

            // Perform scheduling and display the result
            List<Process> processResult = new ArrayList<>();
            int currentTime = 0;
            while (!(processResult.size() == processes.size())) {
                Process selectedProcess = findShortestBurstTime(processes, currentTime);

                if (selectedProcess == null) {
                    currentTime++;
                    continue;
                }
                calculation(selectedProcess, currentTime);
                processResult.add(selectedProcess);
                currentTime += selectedProcess.burstTime;
                selectedProcess.remainingBurstTime = 0;
                drawGanttBox(selectedProcess.i, currentTime);
            }

            // Sort the processes based on their process IDs
            List<Process> sortedProcess = processResult;
            Collections.sort(sortedProcess, Comparator.comparingInt(p -> p.i));

            // Display result in TableView
            ObservableList<Process> processObservableList = FXCollections.observableArrayList(sortedProcess);
            processTable.setItems(processObservableList);
            processTable.setMaxHeight((numProcesses + 1) * 25);

            // Display Gantt Chart in TextArea
            ganttChartArea.getChildren().clear(); // Clear previous content
            ganttChartArea.getChildren().add(ganttChartBox);

            String calculationResult = calculateTotal(processes);
            calculationArea.setText(calculationResult);
            // double calHeight = 24;
            // calculationArea.setPrefHeight(5 * calHeight);
            errorLabel.setText("");
    }

    private boolean isFirstProcess = true;

    private void drawGanttBox(int processId, int currentTime) {
        // Create a rectangle for the Gantt chart box
        Rectangle ganttBox = new Rectangle(30, 20);
        ganttBox.setStyle("-fx-fill: lightblue; -fx-stroke: black;");
    
        // Set the position of the rectangle in the Gantt chart
        //ganttBox.setTranslateX(currentTime * 30); // Adjust the width as needed
    
        // Create a label to display the process ID
        Label processLabel = new Label("P" + processId);
        processLabel.setMinWidth(28);
        processLabel.setAlignment(Pos.CENTER);
        processLabel.setTranslateY(-20);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(ganttBox, processLabel);

        Label zeroLabel = new Label("0");
        zeroLabel.setMinWidth(15);
        zeroLabel.setAlignment(Pos.CENTER_LEFT);
        zeroLabel.setStyle("-fx-text-fill: black;");
        zeroLabel.setTranslateY(-20);

        Label timeLabel = new Label(isFirstProcess ? "0\t" + String.valueOf(currentTime) : String.valueOf(currentTime));
        timeLabel.setMinWidth(30);
        timeLabel.setAlignment(Pos.CENTER_RIGHT);
        timeLabel.setTranslateY(-20);

        HBox timeBox = new HBox(zeroLabel, timeLabel);
        timeBox.setAlignment(Pos.CENTER);

        VBox cell = isFirstProcess ? new VBox(zeroLabel, stack) : new VBox(stack, timeLabel);
        cell.setAlignment(Pos.CENTER);

        VBox ganttBoxWithLabel = new VBox(ganttBox, processLabel, timeLabel);
        ganttBoxWithLabel.setAlignment(Pos.CENTER);
        // Add the rectangle and label to the Gantt chart box
        ganttChartBox.getChildren().add(ganttBoxWithLabel);

        if (isFirstProcess) {
            isFirstProcess = false;
        }

    }

    public static Process findShortestBurstTime(List<Process> processes, int currentTime) {
        int shortestBurstTime = Integer.MAX_VALUE;
        Process selectedProcess = null;

        for (Process p : processes) {
            int arrivalTime = p.arrivalTime;
            int burstTime = p.burstTime;

            if (p.remainingBurstTime > 0 && arrivalTime <= currentTime && burstTime < shortestBurstTime) {
                shortestBurstTime = burstTime;
                selectedProcess = p;
            }
        }
        return selectedProcess;
    }

    public static void calculation(Process process, int currentTime) {
        int arrivalTime = process.arrivalTime;
        int burstTime = process.burstTime;
        int startingTime = currentTime;
        int finishingTime = startingTime + burstTime;
        int turnaroundTime = finishingTime - arrivalTime;
        int waitingTime = turnaroundTime - burstTime;

        process.turnaroundTime = turnaroundTime;
        process.waitingTime = waitingTime;
    }

    public static String calculateTotal (List<Process> processes){
        int totalTurnaroundTime = 0;
        int totalWaitingTime = 0;
        int i = 0;
        for(Process p: processes){
            totalTurnaroundTime += p.turnaroundTime;
            totalWaitingTime += p.waitingTime;
            i++;
        }
        double averageTurnaroundTime = (double) totalTurnaroundTime / i;
        double averageWaitingTime = (double) totalWaitingTime / i;

         // Set the calculated values to the label
        return String.format("Total Turnaround Time: %d\nAverage Turnaround Time: %.2f\nTotal Waiting Time: %d\nAverage Waiting Time: %.2f",
        totalTurnaroundTime, averageTurnaroundTime, totalWaitingTime, averageWaitingTime);
    }
}
