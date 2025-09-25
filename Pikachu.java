package model;

import java.util.Arrays;

public class Pikachu extends Pokemon {

    public Pikachu() {
        super("Pikachu", Type.ELECTRIC, 95,
                Arrays.asList(
                        new Attack("Quick Attack", 90, (a, d) -> 10),
                        new Attack("Thunder Shock", 75, (a, d) -> 20),
                        new Attack("Thunderbolt", 65, (a, d) -> 30)
                )
        );
    }
}

