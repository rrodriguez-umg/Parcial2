package model;

import java.util.Arrays;

public class Bulbasaur extends Pokemon {

    public Bulbasaur() {
        super("Bulbasaur", Type.GRASS, 105,
                Arrays.asList(
                        new Attack("Tackle", 90, (a, d) -> 10),
                        new Attack("Vine Whip", 75, (a, d) -> 20),
                        new Attack("Razor Leaf", 65, (a, d) -> 30)
                )
        );
    }
}
