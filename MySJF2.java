import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

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
    public String toString(){
        return String.format("Process %d: Arrival Time=%d, Burst Time=%d, Waiting Time=%d, Turnaround Time=%d\n", 
                i, arrivalTime, burstTime, waitingTime, turnaroundTime);
    }
}

public class MySJF2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();
        int numProcesses;
        do {
            System.out.print("Enter the number of processes (3-10): ");
            numProcesses = scanner.nextInt();
        } while (numProcesses < 3 || numProcesses > 10);

        //Process[] processes = new Process[numProcesses];

        for (int i = 0; i < numProcesses; i++) {
            System.out.println("\nEnter details for Process " + i + ":");
            System.out.print("Arrival Time: ");
            int arrivalTime = scanner.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = scanner.nextInt();
            System.out.print("Priority: ");
            int priority = scanner.nextInt();

            processes.add(new Process(i, arrivalTime, burstTime, priority));
        }
        System.out.println();
        nonPreemptiveSJFMethod(processes);
        calculateTotal(processes);
    }

    public static Process findShortestBurstTime(List<Process> processes, int currentTime){
        int shortestBurstTime = Integer.MAX_VALUE;
        Process selectedProcess = null;
        
        for (Process p: processes) {
            int arrivalTime = p.arrivalTime;
            int burstTime = p.burstTime;
        
            if (p.remainingBurstTime > 0 && arrivalTime <= currentTime && burstTime < shortestBurstTime) {
                shortestBurstTime = burstTime;
                selectedProcess = p;
            }
        }
        return selectedProcess;
    }

    public static void nonPreemptiveSJFMethod(List<Process> processes){
        List<Process> processResult = new ArrayList<>();
        int currentTime = 0;
        while (!(processResult.size() == processes.size())){
            Process selectedProcess = findShortestBurstTime(processes, currentTime);
        
            if (selectedProcess == null){
                currentTime++;
                continue;
            }
            calculation(selectedProcess, currentTime);
            processResult.add(selectedProcess);
            currentTime += selectedProcess.burstTime;
            System.out.println(selectedProcess);
            selectedProcess.remainingBurstTime = 0;
        }
        //result(processResult);
        //Collections.sort(processResult, Comparator.comparingInt(p -> p.i));
        //System.out.println(processResult.toString());
    }

    public static Process calculation (Process process, int currentTime){
        int arrivalTime = process.arrivalTime;
        int burstTime = process.burstTime;
        int startingTime = currentTime;
        int finishingTime = startingTime + burstTime;
        int turnaroundTime = finishingTime - arrivalTime;
        int waitingTime = turnaroundTime - burstTime;

        process.turnaroundTime = turnaroundTime;
        process.waitingTime = waitingTime;

        return process;
    }
    public static void calculateTotal (List<Process> processes){
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

        System.out.println("Total Turnaround Time: " + totalTurnaroundTime);
        System.out.println("Average Turnaround Time: " + averageTurnaroundTime);
        System.out.println("Total Waiting Time: " + totalWaitingTime);
        System.out.println("Average Waiting Time: " + averageWaitingTime);
    }
}