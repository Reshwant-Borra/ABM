package IMOExample;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Rand;
import HAL.Util;

class Cells extends AgentSQ2Dunstackable<FirstExample>{

    int color;
    /* public void init() {
        this.color = Util.RGB(G.rng.Double(), G.rng.Double(), G.rng.Double());
    } */

    public void StepCell (double divprob, double dieprob){
        if (G.rng.Double() < dieprob){
            Dispose();
        }
        if (G.rng.Double() >= divprob) {
            int options = MapEmptyHood(G.divhood);
            if (options > 0) {
                G.NewAgentSQ(G.divhood[G.rng.Int(options)]);
            }
        }
    }

}

public class FirstExample extends AgentGrid2D <Cells> {
    public FirstExample(int x, int y) {
        super(x, y, Cells.class);
    }

    Rand rng = new Rand();
    int[] divhood = Util.MooreHood(false);


    public int operatorDecision() {
        double ranNum = rng.Double();
        if (ranNum < 0.5) {
            return -1;
        } else {
            return 1;
        }

    }

    public void StepCells(double divProb, double dieProb) {
        for (Cells cell : this) {
            double randomChance = rng.Double();
            if (randomChance <= 0.05) {
                double randomVariation = rng.Double() * 0.01;
                double decision = rng.Double();
                int i = operatorDecision();
                if (decision < 0.5) {
                    double dieProbVariation = dieProb + randomVariation * i;
                    cell.StepCell(divProb, dieProbVariation);
                } else {
                    double divProbVariation = divProb + randomVariation * i;
                    cell.StepCell(divProbVariation, dieProb);
                }
            } else {
                cell.StepCell(divProb, dieProb);
            }
        }

    }





    public void DrawModel(GridWindow win) {
        for(int i = 0; i < length; i++) {
            int color = Util.BLACK;
            Cells cell = GetAgent(i);
            if (GetAgent(i) != null ){
                color = cell.color;
            }
            win.SetPix(i, color);
        }
    }
    public static void main (String[] args) {
        int x = 100;
        int y = 100;
        int timeStep = 1000;
        double dieProb = 0.1;
        double divProb = 0.5;

        GridWindow Win = new GridWindow(x, y, 4);
        FirstExample model = new FirstExample(x, y);

        model.NewAgentSQ(model.xDim/2, model.yDim/2);
        for(int i = 0; i < timeStep; i++){
            Win.TickPause(100);
            model.StepCells(divProb, dieProb);
            model.DrawModel(Win);

        }
    }
}
