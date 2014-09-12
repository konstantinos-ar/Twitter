/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Administrator
 */
public class peakFinding {

    //επιστρέφει τον μέσο ορο του C
    static double getMean(Integer[] c) {
        double sum = 0.0;
        for (double a : c) {
            sum += a;
        }
        return sum / c.length;
    }

    //επιστρέφει τη διακυμανση του πίνακα C
    static double getVariance(Integer[] c) {
        double mean = getMean(c);
        double temp = 0;
        for (double a : c) {
            temp += (mean - a) * (mean - a);
        }
        return temp / c.length;
    }

    //�?�?�?ια μέθοδος ευρεσης των κορυφων (peaks) με βάση τον αλγοριθμο του OPAD
    public static ArrayList<String> peakFinder(Integer[] c, double a, int k, int t) {
        double mean = c[0];
        double meandev = getVariance(Arrays.copyOfRange(c, 0, k));
        int start;
        ArrayList<String> windows = new ArrayList<String>();
        int end;
        //==================Υλοποίηση οπως περιγράφεται στο paper===============
        for (int i = 2; i < c.length; i++) {
            if ((Math.abs(c[i] - mean) / meandev) > t && c[i] > c[i - 1]) {
                start = i - 1;

                while (i < c.length && c[i] > c[i - 1]) {
                    double temp[] = update(mean, meandev, c[i], a);
                    mean = temp[0];
                    meandev = temp[1];
                    i++;
                }
                end = i;
                while (i < c.length && c[i] > c[start]) {

                    if (Math.abs(c[i] - mean) / meandev > t && c[i] > c[i - 1]) {
                        end = --i;
                        break;
                    } else {
                        double temp[] = update(mean, meandev, c[i], a);
                        mean = temp[0];
                        meandev = temp[1];
                        end = i++;
                    }
                }
                windows.add(Integer.toString(start) + " " + Integer.toString(end));
            } else {
                double temp[] = update(mean, meandev, c[i], a);
                mean = temp[0];
                meandev = temp[1];

            }
        }
        return windows;
    }

    //Ενημέρωση στατιστικων
    public static double[] update(double oldmean, double oldmeandev, int updatevalue, double a) {
        double diff = Math.abs(oldmean - updatevalue);
        double[] newValues = new double[2];
        newValues[0] = (a * updatevalue + (1 - a) * oldmean);
        newValues[1] = (a * diff + (1 - a) * oldmeandev);
        return newValues;
    }

}
