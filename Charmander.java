package model;

import java.util.Arrays;

public class Charmander extends Pokemon {

    public Charmander() {
        super("Charmander", Type.FIRE, 100,
                Arrays.asList(
                        new Attack("Scratch", 90, (a, d) -> 10),
                        new Attack("Ember", 75, (a, d) -> 20),
                        new Attack("Flamethrower", 65, (a, d) -> 30)
                )
        );
    }
}

