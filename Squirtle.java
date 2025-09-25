package model;

import java.util.Arrays;

public class Squirtle extends Pokemon {

    public Squirtle() {
        super("Squirtle", Type.WATER, 110,
                Arrays.asList(
                        new Attack("Tackle", 90, (a, d) -> 10),
                        new Attack("Water Gun", 75, (a, d) -> 20),
                        new Attack("Bubble Beam", 65, (a, d) -> 30)
                )
        );
    }
}
