package OOPCode;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Gui.PlotLine;
import HAL.Gui.UIPlot;
import HAL.Gui.UIWindow;
import HAL.Rand;
import HAL.Util;
import IMOExample.FirstExample;

import java.util.ArrayList;
import java.util.HashSet;
import HAL.Tools.FileIO;

class Cells extends AgentSQ2Dunstackable<GridEx> {
    public void init(double ID, double divProb, double dieProb, int color) {
        this.ID = ID; this.divProb = divProb; this.dieProb = dieProb; this.color = color;

    }

    Rand rng = new Rand();
    int color = Util.RGB(rng.Double(), rng.Double(), rng.Double());
    double ID = rng.Double();
    double divProb = 0.5;
    double dieProb = 0.1;
    double mutationVariance;
    double decision;
    double MUTATION = 0.02;
    boolean mutationCheck = false;
    int[] divhood = Util.MooreHood(false);

    public void stepCell() {
        G.totalCells++;
        if (rng.Double() < MUTATION) {
            mutationCheck = true;
        }
        if (rng.Double() < this.dieProb){
            G.cellDeath++;
            Dispose();
        }
        else if (rng.Double() >= this.divProb) {
            int options = MapEmptyHood(divhood);
            if (options > 0) {
                Cells daughter =  G.NewAgentSQ(divhood[rng.Int(options)]);
                G.cellDivision++;
                if (mutationCheck) {
                    daughter.init(this.ID, this.divProb, this.dieProb, this.color);
                    daughter.decision = rng.Double();
                    daughter.mutationVariance = rng.Double() * 0.01;
                    daughter.ID = rng.Double();
                    daughter.mutationCheck = false;
                    this.mutationCheck = false;
                    if (daughter.decision < 0.5) {
                        daughter.dieProb = daughter.dieProb + daughter.mutationVariance * operatorDecision();
                    }
                    else {
                        daughter.divProb = daughter.divProb + daughter.mutationVariance * operatorDecision();
                    }

                    daughter.color = Util.RGB(rng.Double(), rng.Double(), rng.Double());
                }
                else {
                    daughter.init(this.ID, this.divProb, this.dieProb, this.color);
                }
            }
        }


    }
    public int operatorDecision() {
        int decisionOperator = rng.Int(2);
        if (decisionOperator == 1) {
            return 1;
        } else {
            return -1;
        }
    }

}

public class GridEx extends AgentGrid2D<Cells> {
    Rand rng = new Rand();
    double cellCount = 0;
    int cellDeath = 0;
    int totalCells = 0;
    int cellDivision = 0;

    public GridEx(int x, int y) {
        super(x, y, OOPCode.Cells.class);
    }

    public void StepCells() {
        for (OOPCode.Cells cell : this) {
            cell.stepCell();
        }

    }

    /*public double deathAverage() {
        double deathTotal = 0;
        int cellCount = 0;
        double initialDiv = 0.1;

        for(Cells cell : this) {
            deathTotal += cell.dieProb - initialDiv;
            cellCount += 1;
        }

        if (cellCount == 0) {
            return 0;
        }

        return (deathTotal / cellCount);
    }
     */

    public double deathAverage() {
        return (double) cellDeath/totalCells;
    }

    /* public double divisionAverage() {
        double divisionAverage = 0;
        double initialDiv = 0.5;
        int cellCount = 0;

        for(Cells cell : this) {
            divisionAverage += cell.divProb - initialDiv;
            cellCount += 1;
        }

        if (cellCount == 0) {
            return 0;
        }

        return (divisionAverage / cellCount);
    }
     */
    public double divisionAverage() {
        return (double) cellDivision/totalCells;
    }
    public int totalMutations() {
        int mutationCount = 0;
        HashSet<Double> mutantIDs = new HashSet<>();
        for(Cells cell : this) {
            mutantIDs.add(cell.ID);
        }
        return mutantIDs.size();
    }

    public void DrawModel(GridWindow win) {
        for (int i = 0; i < length; i++) {
            int color = Util.BLACK;
            Cells cell = GetAgent(i);
            if (GetAgent(i) != null) {
                color = cell.color;

            }
            win.SetPix(i, color);
        }
    }

    public static ArrayList<Integer> runModel(double divProb, double dieProb) {
        int timeStep = 1000;
        GridEx model = new GridEx(100, 100);
        Cells firstCell = model.NewAgentSQ(model.xDim / 2, model.yDim / 2);
        firstCell.init(0.65, divProb, dieProb, Util.RGB(1.0, 1.0, 1.0));
        GridWindow Win = new GridWindow(100, 100, 4);
        ArrayList<Integer> holder = new ArrayList<>();
        for (int i = 0; i < timeStep; i++) {
            Win.TickPause(0);
            model.StepCells();
            model.DrawModel(Win);
            if (i % 10 == 0) {
                model.totalCells = 0;
                model.cellDeath = 0;
                model.cellDivision = 0;
                holder.add(model.Pop());
            }
        }
        return holder;
    }

    public static void main(String[] args) {
        int x = 100;
        int y = 100;
        int timeStep = 1000;
        ArrayList<Integer> baseline = new ArrayList<>();

        GridWindow Win = new GridWindow(x, y, 4);
        GridEx model = new GridEx(x, y);

        UIWindow win = new UIWindow("Time vs Population", true);
        UIPlot popCells = new UIPlot(250, 250, 2);
        UIPlot deathPlot = new UIPlot(250, 250, 2);
        UIPlot idCount = new UIPlot(250, 250, 2);

        win.AddCol(0, popCells);
        win.AddCol(1, deathPlot);
        win.AddCol(1, idCount);

        Cells firstCell = model.NewAgentSQ(model.xDim / 2, model.yDim / 2);
        firstCell.init(0.65, 0.5, 0.1, Util.RGB(1.0, 1.0, 1.0));
        PlotLine popLine = popCells.AddLine(Util.RED);
        PlotLine deathAverage = deathPlot.AddLine(Util.BLUE);
        PlotLine divisionAverage = deathPlot.AddLine(Util.GREEN);
        PlotLine cellIDCount = idCount.AddLine(Util.WHITE);

        win.RunGui();

        FileIO out = new FileIO("SimulationDataBasics.csv", "w");
        FileIO growthTrj1 = new FileIO("GrowthTrajectory1.csv", "w");
        FileIO growthTrj2 = new FileIO("GrowthTrajectory2.csv", "w");
        FileIO sensitivityAnalysis = new FileIO("SensitivityAnalysis.csv", "w");

        out.Write("Total Cell Count, Death Average, Division Average, Total Mutations, Time\n");
        growthTrj1.Write("Baseline,High Death, High Division,Time\n");
        growthTrj2.Write("Baseline,Low Death, Low Division,Time\n");
        sensitivityAnalysis.Write("Baseline,Death Variance,Division Variance,Parameter Change\n");

        for (int i = 0; i < timeStep; i++) {
            Win.TickPause(0);
            model.StepCells();
            model.DrawModel(Win);
            if (i % 10 == 0) {
                popLine.AddSegment(i, model.Pop());
                deathAverage.AddSegment(i, model.deathAverage());
                divisionAverage.AddSegment(i, model.divisionAverage());
                cellIDCount.AddSegment(i, model.totalMutations());
                System.out.println(model.Pop());
                System.out.println(model.deathAverage());
                System.out.println(model.divisionAverage());
                System.out.println(model.totalMutations());
                out.Write(model.Pop() + "," + model.deathAverage() + "," + model.divisionAverage() + "," + model.totalMutations() + "," + i + "\n" );
                baseline.add(model.Pop());
                model.totalCells = 0;
                model.cellDeath = 0;
                model.cellDivision = 0;
            }
        }
        out.Close();
        ArrayList<Integer> divChange = new ArrayList<>();
        ArrayList<Integer> dieChange = new ArrayList<>();
        //Plot 2 Run
        divChange = runModel(0.45, 0.1);
        dieChange = runModel(0.5, 0.15);
        for(int i = 0; i < divChange.size(); i++) {
            int j = i * 10;
            growthTrj1.Write(baseline.get(i) + "," + dieChange.get(i) + "," + divChange.get(i) + "," + j +"\n");
        }
        growthTrj1.Close();
        divChange.clear();
        dieChange.clear();
        divChange = runModel(0.55, 0.1);
        dieChange = runModel(0.5, 0.05);
        for(int i = 0; i < divChange.size(); i++) {
            int j = i * 10;
            growthTrj2.Write(baseline.get(i) + "," + dieChange.get(i) + "," + divChange.get(i) + "," + j +"\n");
        }
        growthTrj2.Close();

    }
}