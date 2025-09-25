package model;

import exceptions.AttackMissedException;

import java.util.Random;

@FunctionalInterface
interface DamageRule {
    int calculateDamage(Pokemon attacker, Pokemon defender);
}

public class Attack {
    private final String name;
    public final int precision; // visible para CPU (puedes usar getter si prefieres)
    private final DamageRule damageRule;

    private static final Random random = new Random();

    public Attack(String name, int precision, DamageRule damageRule) {
        this.name = name;
        this.precision = precision;
        this.damageRule = damageRule;
    }

    public String getName() {
        return name;
    }

    public int execute(Pokemon attacker, Pokemon defender) throws AttackMissedException {
        int chance = random.nextInt(100);
        if (chance >= precision) {
            throw new AttackMissedException(attacker.getName() + " falló el ataque " + name);
        }
        int damage = damageRule.calculateDamage(attacker, defender);
        defender.reduceHP(damage);
        return damage;
    }

    @Override
    public String toString() {
        return name + " (Precisión: " + precision + "%)";
    }
}
