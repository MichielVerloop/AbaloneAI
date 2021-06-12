// Generated code -- CC0 -- No Rights Reserved -- http://www.redblobgames.com/grids/hexagons/
// Adapted by Michiel Verloop to work for JUnit.
// Code made compliant with checkstyle by Michiel Verloop

package model.hex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HexTest {
	//
    Hex center;
    Hex rightNeighbor;
    Hex rightUpperRightNeighbor;

    @BeforeEach
    void setUp() throws Exception {
        center = new Hex(0, 0, 0);
        rightNeighbor = new Hex(1, -1, 0);
        rightUpperRightNeighbor = new Hex(2, -1, -1);
    }

    @Test
    void testSetup() {
        assertEquals(rightNeighbor, center.neighbour(Direction.RIGHT));
    }

    @Test
    void testEquals() {
        assertNotEquals(center, rightNeighbor);
        assertEquals(rightNeighbor, new Hex(1, -1, 0));
    }

    @Test
    void testHashcode() {
        assertEquals(new Hex(0, 0, 0).hashCode(), center.hashCode());
    }

    @Test
    void testArithmetic() {
        assertEquals(rightNeighbor, center.add(rightNeighbor));
        assertEquals(rightUpperRightNeighbor, rightNeighbor.add(new Hex(1, 0, -1)));
        assertEquals(new Hex(4, -10, 6), new Hex(1, -3, 2).add(new Hex(3, -7, 4)));
        assertEquals(new Hex(-2, 4, -2), new Hex(1, -3, 2).subtract(new Hex(3, -7, 4)));
    }

    @Test
    void testDirection() {
        assertEquals(new Hex(0, -1, 1), Hex.direction(Direction.LOWER_RIGHT));
    }

    @Test
    void testNeighbor() {
        assertEquals(new Hex(1, -3, 2), new Hex(1, -2, 1).neighbour(Direction.LOWER_RIGHT));
    }
    
    @Test
    void testNeighborDistance() {
    	// Identity
    	assertEquals(
    			new Hex(1, -1, 0),
    			new Hex(1, -1, 0).neighbour(Direction.UPPER_RIGHT, 0));
    	
    	// immediate neighbour
    	assertEquals(
    			new Hex(2, -1, -1),
    			new Hex(1, -1, 0).neighbour(Direction.UPPER_RIGHT, 1));
    
    	// inverse immediate neighbour.
    	assertEquals(
    			new Hex(0, -1, 1),
    			new Hex(1, -1, 0).neighbour(Direction.UPPER_RIGHT, -1));
    	
    	// Neighbour of the neighbour, distance 2.
    	assertEquals(
    			new Hex(3, -1, -2),
    			new Hex(1, -1, 0).neighbour(Direction.UPPER_RIGHT, 2));
    }

    @Test
    void testDistance() {
        assertEquals(7, new Hex(3, -7, 4).distance(center));
    }

    @Test
    void testRotateRight() {
    	Hex hex = new Hex(1, -3, 2);
        assertEquals(hex.rotateRight(), new Hex(-2, -1, 3));

        assertEquals(hex.rotateRight().rotateRight(),
                     hex.rotateRight(2));
        assertEquals(hex, hex.rotateRight(6));
    }

    @Test
    void testRotateLeft() {
    	Hex hex = new Hex(1, -3, 2);
        assertEquals(hex.rotateLeft(), new Hex(3, -2, -1));
        assertEquals(hex.rotateLeft().rotateLeft(),
                 hex.rotateLeft(2));
        assertEquals(hex, hex.rotateLeft(6));
    }

    @Test
    void testHexRound() {
        final FractionalHex a = new FractionalHex(0.0, 0.0, 0.0);
        final FractionalHex b = new FractionalHex(1.0, -1.0, 0.0);
        final FractionalHex c = new FractionalHex(0.0, -1.0, 1.0);
        assertEquals(new Hex(5, -10, 5), 
                     new FractionalHex(0.0, 0.0, 0.0)
                         .hexLerp(new FractionalHex(10.0, -20.0, 10.0), 0.5).hexRound());
        assertEquals(a.hexRound(), 
                     a.hexLerp(b, 0.499).hexRound());
        assertEquals(b.hexRound(),
                     a.hexLerp(b, 0.501).hexRound());
        assertEquals(a.hexRound(), 
                     new FractionalHex(a.coordQ * 0.4 + b.coordQ * 0.3 + c.coordQ * 0.3,
                                        a.coordR * 0.4 + b.coordR * 0.3 + c.coordR * 0.3,
                                        a.coordS * 0.4 + b.coordS * 0.3 + c.coordS * 0.3).hexRound());
        assertEquals(c.hexRound(),
                     new FractionalHex(a.coordQ * 0.3 + b.coordQ * 0.3 + c.coordQ * 0.4,
                             a.coordR * 0.3 + b.coordR * 0.3 + c.coordR * 0.4,
                             a.coordS * 0.3 + b.coordS * 0.3 + c.coordS * 0.4).hexRound());
    }

    @SuppressWarnings("serial")
	@Test
    void testHexLinedraw() {
        assertEquals(new ArrayList<Hex>() {{
                add(new Hex(0, 0, 0));
                add(new Hex(0, -1, 1));
                add(new Hex(0, -2, 2));
                add(new Hex(1, -3, 2));
                add(new Hex(1, -4, 3));
                add(new Hex(1, -5, 4));
            }},
            FractionalHex.hexLinedraw(
                    new Hex(0, 0, 0),
                    new Hex(1, -5, 4)
            ));
        
        List<Hex> startingPositionTwoPlayerLower = new ArrayList<Hex>() {{
                add(new Hex(-4, 0, 4));
                add(new Hex(-3, -1, 4));
                add(new Hex(-2, -2, 4));
                add(new Hex(-1, -3, 4));
                add(new Hex(0, -4, 4));

                add(new Hex(-4, 1, 3));
                add(new Hex(-3, 0, 3));
                add(new Hex(-2, -1, 3));
                add(new Hex(-1, -2, 3));
                add(new Hex(0, -3, 3));
                add(new Hex(1, -4, 3));

                add(new Hex(-2, 0, 2));
                add(new Hex(-1, -1, 2));
                add(new Hex(0, -2, 2));
            }};

        assertEquals(startingPositionTwoPlayerLower,
            new ArrayList<Hex>() {{
                    addAll(FractionalHex.hexLinedraw(
                        new Hex(-4, 0, 4),
                        new Hex(0, -4, 4)));
                    addAll(FractionalHex.hexLinedraw(
                        new Hex(-4, 1, 3),
                        new Hex(1, -4, 3)));
                    addAll(FractionalHex.hexLinedraw(
                        new Hex(-2, 0, 2),
                        new Hex(0, -2, 2)));
                }}
        );
    }



    // @author Michiel Verloop
    @Test
    void testRing() {
        // TODO
        assertThrows(IllegalArgumentException.class, () -> center.ring(0));
        HashSet<Hex> neighbours = new HashSet<>();
        for (Direction d : Direction.values()) {
            neighbours.add(center.neighbour(d));
        }
        assertEquals(neighbours, center.ring(1));
    }

    // @author Michiel Verloop
    @SuppressWarnings("serial")
	@Test
    void testRotateArray() {
		List<Hex> startingPositionTwoPlayerLower = new ArrayList<Hex>() {{
                addAll(FractionalHex.hexLinedraw(
                    new Hex(-4, 0, 4),
                    new Hex(0, -4, 4)));
                addAll(FractionalHex.hexLinedraw(
                    new Hex(-4, 1, 3),
                    new Hex(1, -4, 3)));
                addAll(FractionalHex.hexLinedraw(
                    new Hex(-2, 0, 2),
                    new Hex(0, -2, 2)));
            }};

        List<Hex> startingPositionTwoPlayerUpper = new ArrayList<Hex>() {{
                addAll(FractionalHex.hexLinedraw(
                    new Hex(4, 0, -4),
                    new Hex(0, 4, -4)));
                addAll(FractionalHex.hexLinedraw(
                    new Hex(4, -1, -3),
                    new Hex(-1, 4, -3)));
                addAll(FractionalHex.hexLinedraw(
                    new Hex(2, 0, -2),
                    new Hex(0, 2, -2)));
            }};
        assertEquals(startingPositionTwoPlayerLower,
            Hex.rotateArrayLeft(Hex.rotateArrayRight(startingPositionTwoPlayerLower, 1), 1));
        assertEquals(startingPositionTwoPlayerUpper,
            Hex.rotateArrayRight(startingPositionTwoPlayerLower, 3));

    }

    // @author Michiel Verloop
    @Test
    void testBuild() {
        HashSet<Hex> hexes = Hex.build(3);
        assertEquals(1, Hex.build(1).size());
        assertEquals(19, hexes.size());
        assertTrue(hexes.contains(Hex.direction(Direction.LEFT).scale(2)));
        assertFalse(hexes.contains(Hex.direction(Direction.LEFT).scale(3)));

        assertEquals(61, Hex.build(5).size());
    }
    
    // @author Michiel Verloop
    @Test
    void testIsNeighbourOf() {
    	// The same hex as itself will not be considered a neighbour.
    	assertFalse(center.isNeighbourOf(center));
    	
    	// The ring of hexes around the direction at radius 1 consists only of neighbours.
    	for (Hex neighbour : center.ring(1)) {
    		assertTrue(center.isNeighbourOf(neighbour));
    	}
    	
    	// A ring with radius 2 or higher will not be considered a neighbour.
    	for (Hex distantHex : center.ring(2)) {
    		assertFalse(center.isNeighbourOf(distantHex));
    	}
    }
    
    // @author Michiel Verloop
    @Test
    void testIsDistantNeighbourOf() {
    	// The same hex as itself will not be considered a (distant) neighbour.
    	assertFalse(center.isDistantNeighbourOf(center));
    	
    	// An immediate neighbour will be considered a (distant) neighbour.
    	for (Hex neighbour : center.ring(1)) {
    		assertTrue(center.isDistantNeighbourOf(neighbour));
    	}
    	
    	// A neighbour scaled with distance 2 or higher will be considered a distant neighbour.
    	for (Hex neighbour : center.ring(1)) {
    		Hex scaledNeighbour = neighbour.scale(2);
    		assertTrue(center.isDistantNeighbourOf(scaledNeighbour));
    	}
    	
    	// A neighbour scaled with distance 3 or higher will be considered a distant neighbour.
    	for (Hex neighbour : center.ring(1)) {
    		Hex scaledNeighbour = neighbour.scale(3);
    		assertTrue(center.isDistantNeighbourOf(scaledNeighbour));
    	}
    	
    	// A ring of 2 with all of the distant neighbours removed will not consist of any distant neighbours.
    	Set<Hex> ring2 = center.ring(2);
    	Set<Hex> distantNeighboursAt2 = center.ring(1).stream()
    			.map(x -> x.scale(2))
    			.collect(Collectors.toSet());
    	ring2.removeAll(distantNeighboursAt2);
    	for (Hex notADistantNeighbour : ring2) {
    		assertFalse(center.isDistantNeighbourOf(notADistantNeighbour));
    	}
    }
    
    // @author Michiel Verloop
    @Test
    void testisInline() {
    	// A null argument should return assertionError
    	assertThrows(AssertionError.class, () -> Hex.isLine(null, true));
    	
    	// An empty list of hexes will be true because there are no marbles.
    	Set<Hex> hexes = new HashSet<>();
    	assertTrue(Hex.isLine(hexes, true));
    	
    	// A list consisting of one marble will hold true because none of the marbles are not neighbours.
    	hexes.add(center);
    	assertTrue(Hex.isLine(hexes, true));
    	
    	// A list consisting of 2 marbles will only form a continuous line if they are immediate neighbours.
    	hexes.add(center.neighbour(Direction.LEFT));
    	assertTrue(Hex.isLine(hexes, true));
    	
    	hexes.remove(center.neighbour(Direction.LEFT));
    	hexes.add(center.neighbour(Direction.LEFT, 2));
    	assertTrue(Hex.isLine(hexes, false));
    	assertFalse(Hex.isLine(hexes, true));
    	
    	hexes.remove(center.neighbour(Direction.LEFT, 2));
    	hexes.add(center.neighbour(Direction.LEFT).neighbour(Direction.UPPER_LEFT));
    	assertFalse(Hex.isLine(hexes, true));
    	assertFalse(Hex.isLine(hexes, false));
    	
    	// A list consisting of 3 or marbles will only hold true if and only if all marbles are on one line.
    	// One discontinuous line
    	hexes.remove(center.neighbour(Direction.LEFT).neighbour(Direction.UPPER_LEFT));
    	hexes.add(center.neighbour(Direction.RIGHT));
    	hexes.add(center.neighbour(Direction.LEFT, 2));
    	assertTrue(Hex.isLine(hexes, false));
    	assertFalse(Hex.isLine(hexes, true));
    	
    	// One continous line
    	hexes.add(center.neighbour(Direction.LEFT));
    	assertTrue(Hex.isLine(hexes, true));
    	assertTrue(Hex.isLine(hexes, false));
    	
    	// Not a line at all
    	hexes.add(center.neighbour(Direction.UPPER_LEFT));
    	assertFalse(Hex.isLine(hexes, true));
    	assertFalse(Hex.isLine(hexes, false));
    }
    
    @Test
    void testDivide() {
    	// Center hex divided by anything != 0 is still the center hex.
    	assertEquals(center, center.divide(5));
    	assertEquals(center, center.divide(-8));
    	
    	
    	// Division by 0 throws:
    	assertThrows(ArithmeticException.class, () -> center.divide(0));
    	
    	
    	// Division by 2 works well for well-defined rounding:
    	assertEquals(new Hex(-2, 2, 0), new Hex(-4, 4, 0).divide(2));
    	assertEquals(new Hex(-2, 1, 1), new Hex(-4, 2, 2).divide(2));
    	
    	
    	// Adding 2 hexes and dividing it by 2 gives the middle point:
    	assertEquals(new Hex(-2, 1, 1), new Hex(-2, 2, 0).add(new Hex(-2, 0, 2)).divide(2));
    	
    	
    	// If 2 hexes don't give a nice middle, the bias is constant, namely
    	// to the left, upper left and upper right, 
    	
    	// left
    	assertEquals(new Hex(-1, 1, 0), new Hex(-1, 1, 0).divide(2));
    	assertEquals(center, new Hex(1, -1, 0).divide(2));
    	
    	// upper left
    	assertEquals(new Hex(0, 1, -1), new Hex(0, 1, -1).divide(2));
    	assertEquals(center, new Hex(0, -1, 1).divide(2));
    	
    	// upper right
    	assertEquals(new Hex(1, 0, -1), new Hex(1, 0, -1).divide(2));
    	assertEquals(center, new Hex(-1, 0, 1).divide(2));
    	
    	
    	// all neighbours of the center return the center when divided by 3 or more.
    	for (Hex hex : center.ring(1)) {
    		for (int i = 3; i < 10; i++)
    		assertEquals(center, hex.divide(i));
    	}
    	
    }
    
    @Test
    void testCenterMass() {
    	// center mass of any ring is the hex that the ring is formed on.
    	for (Hex center : Hex.build(5)) {
    		for (int i = 1; i < 10; i++)
    		assertEquals(center, Hex.centerMass(center.ring(i)));    		
    	}
    	
    	// center mass in the exact middle of 3 hexes prioritizes the upper left hex.
    	Hex[] hexes = {center, new Hex(1, -1, 0), new Hex(0, -1, 1)};
    	assertEquals(center, Hex.centerMass(new ArrayList<>(Arrays.asList(hexes))));
    	
    	// Standard starting position center is at -2, -1, 3 (the middle, but shifted to the left because there is no middle)
    	List<Hex> startingPosition = new ArrayList<>();
    	startingPosition.addAll(FractionalHex.hexLinedraw(new Hex(-4, 0, 4), new Hex(0, -4, 4)));
    	startingPosition.addAll(FractionalHex.hexLinedraw(new Hex(-4, 1, 3), new Hex(1, -4, 3)));
    	startingPosition.addAll(FractionalHex.hexLinedraw(new Hex(-2, 0, 2), new Hex(0, -2, 2)));
    	assertEquals(new Hex(-2, -1, 3), Hex.centerMass(startingPosition));
    	
    }
}
