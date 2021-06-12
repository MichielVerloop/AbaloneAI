// Generated code -- CC0 -- No Rights Reserved -- http://www.redblobgames.com/grids/hexagons/
// Javadoc provided by Michiel Verloop
// Code made compliant with checkstyle by Michiel Verloop
// Functions with @author Michiel Verloop made by Michiel Verloop

package model.hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Hex implements Comparable<Hex> {
	//

    @SuppressWarnings("serial")
	public static HashMap<Direction, Hex> directions = new HashMap<>() {
        {
            put(Direction.UPPER_RIGHT, new Hex(1, 0, -1));
            put(Direction.RIGHT, new Hex(1, -1, 0));
            put(Direction.LOWER_RIGHT, new Hex(0, -1, 1));
            put(Direction.LOWER_LEFT, new Hex(-1, 0, 1));
            put(Direction.LEFT, new Hex(-1, 1, 0));
            put(Direction.UPPER_LEFT, new Hex(0, 1, -1));
        }
    };
	
    /**
     * Creates an hexagonal grid from the given radius.
     * @author Michiel Verloop 
     * @param radius the distance from this the center hexagon to the outside layer of hexagons.
     * @requires radius > 0
     * @return a hashSet of type Hex of all hexagons centered around the given hexagon within a given radius.
     * @throws IllegalArgumentException if radius <= 0
     */
    public static HashSet<Hex> build(int radius) {
        if (!(radius > 0)) {
            throw new IllegalArgumentException("radius should equal 1 or higher");
        }
        HashSet<Hex> hexes = new HashSet<>();
        Hex center = new Hex(0, 0, 0);
        hexes.add(center);
        for (int i = 1; i < radius; i++) {
            hexes.addAll(center.ring(i));
        }
        return hexes;
    }
    
    /**
     * Rotates an array of Hexagons 60 * times degrees to the right.
     * @author Michiel Verloop
     * @param list ArrayList of Hexagons to be rotated.
     * @requires list != null, list.stream.allMatch((e -> e != null))
     * @ensures result != null, result.stream.allMatch(e -> e != null)) 
     * @return An ArrayList of Hexagons rotated 60 * times degrees to the right.
     */
    public static List<Hex> rotateArrayRight(List<Hex> list, int times) {
        ArrayList<Hex> result = new ArrayList<>();
        for (Hex hex : list) {
            result.add(hex.rotateRight(times));
        }
        return result;
    }
    
    /**
     * Rotates an array of Hexagons 60 degrees to the left.
     * @author Michiel Verloop
     * @param list ArrayList of Hexagons to be rotated.
     * @requires list != null, list.stream.allMatch((e -> e != null))
     * @ensures result != null, result.stream.allMatch(e -> e != null)) 
     * @return An ArrayList of Hexagons rotated 60 degrees to the left.
     */
    public static ArrayList<Hex> rotateArrayLeft(List<Hex> list, int times) {
        ArrayList<Hex> result = new ArrayList<>();
        for (Hex hex : list) {
            result.add(hex.rotateLeft(times));
        }
        return result;
    }
    
    /**
     * Returns true if all the coordinates are on one line.
     * @param coords An unordered list of coordinates.
     * @param continuous Boolean which determines whether the line has to be continuous.
     * @return True if the coordinates are all on one line. If the list is 
     *     empty or of size 1, it will always return true.
     *     In all other cases, false will be returned.
     */
    public static boolean isLine(Set<Hex> coords, boolean continuous) {
    	assert (coords != null);
    	Iterator<Hex> it;
    	switch (coords.size()) {
    		case 0:
    		// fall-through
    		case 1:
    			return true;
    		case 2:
    			it = coords.iterator();
    			return continuous ? it.next().isNeighbourOf(it.next()) : 
    				it.next().isDistantNeighbourOf(it.next());
    		default:
    			it = coords.iterator();
    			Hex source = it.next(); // You just need some hex.
    			Hex secondHex = it.next();
    			if (!source.isDistantNeighbourOf(secondHex)) {
    				return false;
    			}
				Hex direction = source.subtract(secondHex);
				// Get the axis that is 0
				boolean q = direction.coordQ == 0;
				boolean r = direction.coordR == 0;
				boolean s = direction.coordS == 0;
				
				// For all remaining hexes, check if they share the same axis. 
				// If at least one of them doesn't, the marbles are not on one line.
				// If all marbles do, they are on one line.
				Hex smallest = source;
				Hex biggest = source;
				it = coords.iterator();
				while (it.hasNext()) {
					Hex hex = it.next();
					Hex offsetHex = hex.subtract(source);
					
					if (q) {
						if (offsetHex.coordQ != 0) {
							return false;
						}
						if (hex.coordR > biggest.coordR) {
							biggest = hex;
						} else if (hex.coordR < smallest.coordR) {
							smallest = hex;
						}
						
					} else if (r) {
						if (offsetHex.coordR != 0) {
							return false;
						}
						if (hex.coordS > biggest.coordS) {
							biggest = hex;
						} else if (hex.coordS < smallest.coordS) {
							smallest = hex;
						}
					} else if (s) {
						if (offsetHex.coordS != 0) {
							return false;
						}
						if (hex.coordQ > biggest.coordQ) {
							biggest = hex;
						} else if (hex.coordQ < smallest.coordQ) {
							smallest = hex;
						}
					}
				}
				if (continuous) {
					return biggest.distance(smallest) + 1 == coords.size();
				}
				// if not continuous
				return true;
    	}
    }
    
    public final int coordQ;
    public final int coordR;
    public final int coordS;
        
    /**
     * Constructs a <code>Hex</code> given the coordinates q r s.
     * @param q x axis
     * @param r y axis
     * @param s z axis
     * @throws IllegalArgumentException if q + r + s != 0, to ensure that each
     *     Hex is representable by exactly one set of coordinates
     */
    public Hex(int q, int r, int s) {
        this.coordQ = q;
        this.coordR = r;
        this.coordS = s;
        if (q + r + s != 0) {
            throw new IllegalArgumentException("q + r + s must be 0");
        }
    }
    

    /**
     * Adds two Hexagons to return a new Hex which is equivalent to offsetting
     * either of the Hexagons with the other. 
     * @param b The other Hex
     * @return a new Hex with the combined coordinates of this and b.
     */
    public Hex add(Hex b) {
        return new Hex(coordQ + b.coordQ, coordR + b.coordR, coordS + b.coordS);
    }

    /**
     * Subtracts b from this to return a new Hex which is equivalent to offsetting
     * this with -b. 
     * @param b The other Hex
     * @return a new Hex with the coordinates of this minus the coordinates of b.
     */
    public Hex subtract(Hex b) {
        return new Hex(coordQ - b.coordQ, coordR - b.coordR, coordS - b.coordS);
    }

    
    /**
     * Scales the coordinates of the current Hex by factor k.
     * @param k Scaling factor
     * @return The Hex corresponding to scaled coordinates of this.
     */
    public Hex scale(int k) {
        return new Hex(coordQ * k, coordR * k, coordS * k);
    }
    
    /**
     * Divides the coordinates of the current Hex by factor k.
     * @param k Divisor, unequal 0
     * @return The Hex corresponding to the divided coordinates of this.
     * @throws 
     */
    public Hex divide(double k) {
    	if (k == 0) {
    		throw new ArithmeticException("Division by 0.");
    	}
    	return new FractionalHex(coordQ / k + 1e-06, coordR / k + 2e-06, coordS / k - 3e-06).hexRound();
    }

    /**
     * 
     */
    public static Hex centerMass(Collection<Hex> hexes) {
    	Hex combined = new Hex(0, 0, 0);
    	for (Hex hex : hexes) {
    		combined = combined.add(hex);
    	}
    	return combined.divide(hexes.size());
    }

    /**
     * Rotates the Hex by 60 degrees to the left around the center.
     * @return The Hex corresponding to the rotated coordinates of this.
     */
    public Hex rotateLeft() {
    	return new Hex(-coordR, -coordS, -coordQ);
    }

    /**
     * Rotates the Hex by 60 * times degrees to the left around the center.
     * @author Michiel Verloop
     * @param times the number of times the hexagon should be rotated 60 degrees to the left.
     * @return The Hex corresponding to the rotated coordinates of this.
     */
    public Hex rotateLeft(int times) {
        Hex hex = this;
        for (int i = 0; i < (times % 6); i++) {
            hex = hex.rotateLeft();
        }
        return hex;
    }
    
    /**
     * Rotates the Hex by 60 degrees to the right around the center.
     * @return The Hex corresponding to the rotated coordinates of this.
     */
    public Hex rotateRight() {
    	return new Hex(-coordS, -coordQ, -coordR);
    }

    
    /**
     * Rotates the Hex by 60 * times degrees to the right around the center.
     * @author Michiel Verloop
     * @param times the number of times the hexagon should be rotated 60 degrees to the right.
     * @return The Hex corresponding to the rotated coordinates of this.
     */
    public Hex rotateRight(int times) {
        Hex hex = this;
        for (int i = 0; i < (times % 6); i++) {
            hex = hex.rotateRight();
        }
        return hex;
    }
    
    /**
     * Returns the Hex corresponding to the direction.
     * @param direction The direction for which a Hex will be returned.
     * @return The Hex corresponding to the direction.
     */
    public static Hex direction(Direction direction) {
        return Hex.directions.get(direction);
    }

    /**
     * Returns the neighbour of this in the given direction.
     * @param direction The direction in which the neighbour of this will be returned.
     * @return The hexagon that neighbours this in direction.
     */
    public Hex neighbour(Direction direction) {
        return add(Hex.direction(direction));
    }
    
    /**
     * Returns the Xth neighbour of this in the given direction.
     * @param direction The direction in which the neighbour is returned.
     * @param distance The X - how far away from this the neighbour is. Can be any integer.
     * @return the Xth neighbour of this in the given direction.
     */
    public Hex neighbour(Direction direction, int distance) {
    	return add(Hex.direction(direction).scale(distance));
    }

    /**
     * Returns the length of this Hex.
     * @return How many Hexagons this is away from the center hexagon.
     */
    public int length() {
        return (int)((Math.abs(coordQ) + Math.abs(coordR) + Math.abs(coordS)) / 2);
    }

    /**
     * Returns the distance between this hexagon and b.
     * @param b The other Hex.
     * @return The distance between this hexagon and b. 
     *     Equivalent to subtracting this with b and taking the length of the result.
     */
    public int distance(Hex b) {
        return subtract(b).length();
    }
    
    /**
     * Returns whether the given hex is a neighbour of this hex.
     * @param b The other hex.
     * @return True if and only if the distance between both hexes equals 1.
     */
    public boolean isNeighbourOf(Hex b) {
    	return this.distance(b) == 1;
    }
    
    /**
     * Returns whether the given hex is a distant neighbour of this hex.
     * @param b The other hex.
     * @return True if and only if you can reach the other hex while only traveling in one 
     *     direction only, and travel for at least 1 hex. As such, anything isNeighbourOf will
     *     consider a neighbour is also considered a neighbour by this function.
     */
    public boolean isDistantNeighbourOf(Hex b) {
    	// We exploit the fact that distant neighbours of the center are always a scalar of 
    	// its immediate neighbours. As such, if we subtract b with  this hex, its relative
    	// position will be maintained, after which we can check if it has at least one
    	// coordinate at 0, but not all three coordinates at 0 (it would be equal to this).
    	Hex hex = b.subtract(this);
    	return (!hex.equals(new Hex(0, 0, 0)))
    			&& (hex.coordQ == 0 || hex.coordR == 0 || hex.coordS == 0);
    }
    
    /**
     * Returns a ring of hexagons centered around the given hexagon at the given radius. 
     * @param radius the distance from this to the ring.
     * @requires radius > 0  
     * @return a HashSet of type Hex of all hexagons centered around the given hexagon at the given radius.
     * @throws IllegalArgumentException if radius <= 0
     */
    public HashSet<Hex> ring(int radius) {
        if (!(radius > 0)) {
            throw new IllegalArgumentException("Radius should be greater than 0 and was " + radius + ".");
        }

        HashSet<Hex> results = new HashSet<>();

        Hex hex = this.add(direction(Direction.LEFT).scale(radius));
        for (Direction d : Direction.values()) {
            for (int j = 0; j < radius; j++) {
                results.add(hex);
                hex = hex.neighbour(d);
            }
        }

        return results;
    }
    
    /**
     * Returns a list of the first <code>number</code> neighbours in the given direction.
     * @author Michiel Verloop
     * @param dir The direction of the neighbour compared to this.
     * @param number The number of neighbours 
     * @return a list of the first <code>number</code> neighbours in the given direction.
     */
    public List<Hex> neighbours(Direction dir, int number) {
        List<Hex> neighbours = new ArrayList<>();
        Hex current = this;
        for (int i = 0; i < number; i++) {
            current = current.neighbour(dir);
            neighbours.add(current);
        }
        return neighbours;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Hex) {
            return ((Hex) other).coordQ == this.coordQ
                && ((Hex) other).coordR == this.coordR
                && ((Hex) other).coordS == this.coordS;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return ((Integer) coordQ).hashCode() * 3
             + ((Integer) coordR).hashCode() * 5
             + ((Integer) coordS).hashCode() * 7;
    }
    
    @Override
    public String toString() {
        return "q: " + coordQ + ", r: " + coordR + ", s: " + coordS + "\n";
    }

    /**
     * A Hex which is higher than another Hex is considered smaller.
     * A Hex which is more to the left than another Hex at the same height is considered smaller.
     * @author Michiel Verloop
     */
    @Override
    public int compareTo(Hex o) {
        if (this.coordS < o.coordS) {
            return -1;
        } else if (this.coordS > o.coordS) {
            return 1;
        } else {
            if (this.coordQ < o.coordQ) {
                return -1;
            } else if (this.coordQ > o.coordQ) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}









