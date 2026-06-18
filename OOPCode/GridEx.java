package OOPCode;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Rand;
import HAL.Util;
import IMOExample.FirstExample;

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
        double MUTATION = 0.012;
        boolean mutationCheck = false;
        int[] divhood = Util.MooreHood(false);

        public void stepCell() {
            if (rng.Double() < MUTATION) {
                mutationCheck = true;
            }
            if (rng.Double() < this.dieProb){
                Dispose();
            }
            else if (rng.Double() >= this.divProb) {
                int options = MapEmptyHood(divhood);
                if (options > 0) {
                   Cells daughter =  G.NewAgentSQ(divhood[rng.Int(options)]);
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
        public GridEx(int x, int y) {
            super(x, y, OOPCode.Cells.class);
        }
        public void StepCells() {
            for (OOPCode.Cells cell : this) {
                cell.stepCell();
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

            GridWindow Win = new GridWindow(x, y, 4);
            GridEx model = new GridEx(x, y);

            Cells firstCell = model.NewAgentSQ(model.xDim/2, model.yDim/2);
            firstCell.init(0.65, 0.5, 0.1, Util.RGB(1.0,1.0,1.0));
            for(int i = 0; i < timeStep; i++){
                Win.TickPause(100);
                model.StepCells();
                model.DrawModel(Win);
                System.out.println(model.Pop());
            }
        }
    }