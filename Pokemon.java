package model;

import java.util.List;

public abstract class Pokemon {
    protected String name;
    protected Type type;
    protected int maxHP;
    protected int currentHP;
    protected List<Attack> attacks;

    public Pokemon(String name, Type type, int maxHP, List<Attack> attacks) {
        this.name = name;
        this.type = type;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attacks = attacks;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public boolean isAlive() {
        return currentHP > 0;
    }

    public void reduceHP(int damage) {
        currentHP -= damage;
        if (currentHP < 0) currentHP = 0;
    }

    public List<Attack> getAttacks() {
        return attacks;
    }
}

