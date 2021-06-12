// Generated code -- CC0 -- No Rights Reserved -- http://www.redblobgames.com/grids/hexagons/
// Javadoc provided by Michiel Verloop
// Code made compliant with checkstyle by Michiel Verloop

package model.hex;

import java.util.ArrayList;

public class FractionalHex {

    /**
     * Constructs a fractional Hex, which is a Hex which allows for fractions in the coordinates.
     * @param q The first coordinate.
     * @param r The second coordinate.
     * @param s The third coordinate.
     */
    public FractionalHex(double q, double r, double s) {
        this.coordQ = q;
        this.coordR = r;
        this.coordS = s;
        if (Math.round(q + r + s) != 0) {
            throw new IllegalArgumentException("q + r + s must be 0");
        }
    }

    public final double coordQ;
    public final double coordR;
    public final double coordS;

    /**
     * Rounds the coordinates of the current fractional hex to convert it into the corresponding Hex.
     * @return A Hex with the same coordinates as the rounded coordinates from this fractional hex.
     */
    public Hex hexRound() {
        int qi = (int)(Math.round(coordQ));
        int ri = (int)(Math.round(coordR));
        int si = (int)(Math.round(coordS));
        double diffQ = Math.abs(qi - coordQ);
        double diffR = Math.abs(ri - coordR);
        double diffS = Math.abs(si - coordS);
        if (diffQ > diffR && diffQ > diffS) {
            qi = -ri - si;
        }    else if (diffR > diffS) {
            ri = -qi - si;
        } else {
            si = -qi - ri;
        }

        return new Hex(qi, ri, si);
    }


    /**
     * Interpolate from this hex to another.
     * @param b interpolate to this hex.
     * @param t determines how far along this line the result lies.
     */
    public FractionalHex hexLerp(FractionalHex b, double t) {
        return new FractionalHex(
                coordQ * (1.0 - t) + b.coordQ * t,
                coordR * (1.0 - t) + b.coordR * t,
                coordS * (1.0 - t) + b.coordS * t
        );
    }


    /**
     * Draws a line between (inclusive) two Hexagons and returns an ArrayList of all Hexagons that the line covers.
     * @param a First Hex
     * @param b Second Hex
     * @return An ArrayList of type Hex containing all Hexagons in between and including the two Hexagons.
     */
    public static ArrayList<Hex> hexLinedraw(Hex a, Hex b) {
        int dist = a.distance(b);
        FractionalHex nudgeA = new FractionalHex(a.coordQ + 1e-06, a.coordR + 1e-06, a.coordS - 2e-06);
        FractionalHex nudgeB = new FractionalHex(b.coordQ + 1e-06, b.coordR + 1e-06, b.coordS - 2e-06);
        ArrayList<Hex> results = new ArrayList<Hex>();
        double step = 1.0 / Math.max(dist, 1);
        for (int i = 0; i <= dist; i++) {
            results.add(nudgeA.hexLerp(nudgeB, step * i).hexRound());
        }
        return results;
    }
}
