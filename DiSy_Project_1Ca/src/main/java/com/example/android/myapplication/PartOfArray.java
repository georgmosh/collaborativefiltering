package com.example.android.myapplication;

/*This Class illustrates a part of an Array and all
methods tha are needed in order to calculate Xu and Yi matrices
 */

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.linear.LUDecomposition;

import java.io.Serializable;


public class PartOfArray implements Serializable {
    //These Variables are Realmatrices Or Vectors of the given part of Array ..Their names are the same with those inside the paper

    private RealMatrix Y,C;

    public RealMatrix Y() {return Y;} //debugging

    public PartOfArray(RealMatrix C, RealMatrix Y){
        this.C=C.copy();
        this.Y=Y.copy();
    }


    //param Array C & row u
    public RealMatrix calculateCuMatrix(RealMatrix C){
        RealMatrix Cu=MatrixUtils.createRealDiagonalMatrix(C.getRow(0));
        return Cu;
    }

    //Calculate Yt*Y
    public RealMatrix precalculateYY(RealMatrix Y){
        RealMatrix yReverse=Y.transpose();
        return yReverse.multiply(Y);
    }

    //Calculate Xu
    //Xu=inverse((Yt*Y)+Yt*(Cu-I)*Y +lI)*Yt*Cu*p(u)
    public RealMatrix calculate_XU(){
        double l=0.1;
        RealMatrix YtY = precalculateYY(Y);
        RealMatrix Yt = Y.transpose();
        RealMatrix Cu=calculateCuMatrix(C);
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(Cu.getColumnDimension());
        RealMatrix CuPu = MatrixUtils.createRealMatrix(Cu.getColumnDimension(),1);

        //calculate CuPu
        for (int j=0;j < Cu.getColumnDimension();j++){
            if(Cu.getEntry(j,j)==1){
                CuPu.setEntry(j,0,0);
            }
            else {
                CuPu.setEntry(j, 0,Cu.getEntry(j, j));
            }
        }
        RealMatrix finalXU=Cu.subtract(I);
        finalXU=finalXU.preMultiply(Yt);
        finalXU=finalXU.multiply(Y);
        finalXU=finalXU.add(YtY);
        I=I.scalarMultiply(l);

        RealMatrix SI = I.getSubMatrix(0,Worker.XY.getColumnDimension()-1,0,Worker.XY.getColumnDimension()-1);
        finalXU=finalXU.add(SI);
        LUDecomposition decompXU = new LUDecomposition(finalXU);
        finalXU = decompXU.getSolver().getInverse();
        finalXU=finalXU.multiply(Yt);
        finalXU=finalXU.multiply(CuPu);
        return finalXU;
    }

}

