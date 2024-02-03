import javafx.application.Application;
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
    private List<VBox> ganttBoxes = new ArrayList<>();

    // private int totalTurnaroundTime;
    // private double averageTurnaroundTime;
    // private int totalWaitingTime;
    // private double averageWaitingTime;

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
        ganttChartArea.setSpacing(5);
        ganttChartArea.setStyle("-fx-background-color: #ffffff;");

        // UI components
        numProcessesField = new TextField();
        errorLabel = new Label();
        Button submitButton = new Button("Generate");
        processTable = new TableView<>();
        calculationArea = new TextArea();
        calculationArea.setEditable(false);
        calculationArea.setWrapText(true);
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
        TableColumn<Process, String> turnaroundTimeCol = new TableColumn<>("Turnaround Time");
        turnaroundTimeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().turnaroundTime)));
        TableColumn<Process, String> waitingTimeCol = new TableColumn<>("Waiting Time");
        waitingTimeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().waitingTime)));

        processTable.getColumns().addAll(processIdCol, arrivalTimeCol, burstTimeCol,priorityCol, waitingTimeCol, turnaroundTimeCol);
        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Layout
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

        // Handle number of process
        numProcessesField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Clear previous error message
            errorLabel.setText("");
            if (!newValue.matches("\\d*")) {
                errorLabel.setText("Please enter a valid number.");
                errorLabel.setTextFill(Color.RED);
                return;
            }

            int numProcesses = Integer.parseInt(newValue);
            if (numProcesses < 3 || numProcesses > 10) {
                errorLabel.setText("The number of processes must be within the range of 3 - 10");
                errorLabel.setTextFill(Color.RED);
            }
        });

        submitButton.setOnAction(e -> nonPreemptiveSJF());

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<Process> getNumberOfProcesses() {
        List<Process> processes = new ArrayList<>();

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
            return getProcessesDetails(numProcesses);
        }
    }

    // Get process details from the user
    private List<Process> getProcessesDetails(int numProcesses) {
        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < numProcesses; i++) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setContentText("Enter Arrival Time, Burst Time, and Priority for Process " + i + " (separated by commas):");
            dialog.setHeaderText(null);
            dialog.showAndWait();

            String userInput = dialog.getResult();      
            String[] inputs = userInput.split(",");

            int arrivalTime = Integer.parseInt(inputs[0]);
            int burstTime = Integer.parseInt(inputs[1]);
            int priority = Integer.parseInt(inputs[2]);

            processes.add(new Process(i, arrivalTime, burstTime, priority));
        }
        return processes;
    }

    // // Helper method to get an integer input from the user
    // private int getIntFromUser(String prompt) {
    //     TextInputDialog dialog = new TextInputDialog();
    //     dialog.setContentText(prompt);
    //     dialog.setHeaderText(null);
    //     dialog.showAndWait();
    //     return Integer.parseInt(dialog.getResult());
    // }

    // public String toString() {
    //     return String.format("Process %d: Arrival Time=%d, Burst Time=%d, Waiting Time=%d, Turnaround Time=%d\n",
    //     i, arrivalTime, burstTime, waitingTime, turnaroundTime);
    // }
    

    private void nonPreemptiveSJF(){
        List<Process> processes = getNumberOfProcesses();
        int numProcesses = Integer.parseInt(numProcessesField.getText());

        // Check if the number of processes is within the valid range
        if (numProcesses < 3 || numProcesses > 10) {
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
                drawGanttChart(selectedProcess.i, currentTime);
            }
            ganttChartBox.getChildren().addAll(ganttBoxes);

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

            errorLabel.setText("");
    }

    private boolean isFirstProcess = true;
    private void drawGanttChart(int processId, int currentTime) {
        Rectangle ganttBox = new Rectangle(30, 20);
        ganttBox.setStyle("-fx-fill: lightblue; -fx-stroke: black;");
    
        Label processLabel = new Label("P" + processId);
        processLabel.setMinWidth(30);
        processLabel.setAlignment(Pos.CENTER);
        processLabel.setTranslateY(-20);

        // StackPane stack = new StackPane();
        // stack.getChildren().addAll(ganttBox);

        // Label zeroLabel = new Label("0");
        // zeroLabel.setMinWidth(15);
        // zeroLabel.setAlignment(Pos.CENTER_LEFT);
        // zeroLabel.setStyle("-fx-text-fill: black;");
        // zeroLabel.setTranslateY(-20);

        Label timeLabel = new Label(isFirstProcess ? "0\t" + String.valueOf(currentTime) : String.valueOf(currentTime));
        timeLabel.setMinWidth(28);
        timeLabel.setAlignment(Pos.CENTER_RIGHT);
        timeLabel.setTranslateX(5);
        timeLabel.setTranslateY(-20);

        VBox ganttBoxWithLabel = new VBox(ganttBox, processLabel, timeLabel);
        ganttBoxWithLabel.setAlignment(Pos.CENTER);
        ganttBoxes.add(ganttBoxWithLabel);

        if (isFirstProcess) {
            ganttBoxWithLabel.setTranslateX(3);
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
