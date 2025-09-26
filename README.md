import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class PokemonMiniGame {
    public static void main(String[] args) {
        new Game().start();
    }
}

class Game {
    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();


    private final List<Event> battleLog = new ArrayList<>();
    private final List<Integer> damageHistory = new ArrayList<>();

    private final Map<Integer, Supplier<Pokemon>> pokedex = new LinkedHashMap<>();

    public Game() {
        pokedex.put(1, () -> new Charmander("Charmander"));
        pokedex.put(2, () -> new Squirtle("Squirtle"));
        pokedex.put(3, () -> new Bulbasaur("Bulbasaur"));
        pokedex.put(4, () -> new Pikachu("Pikachu"));
    }

    public void start() {
        System.out.println("=== Bienvenido al Mini-Pokémon (con Streams y Exceptions) ===\n");

        System.out.print("Escribe tu nombre: ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) playerName = "Jugador";

        Pokemon player = null;
        while (player == null) {
            try {
                mostrarPokedex();
                System.out.print("Elige tu Pokémon (número): ");
                String raw = scanner.nextLine();
                int choice = Integer.parseInt(raw);
                if (!pokedex.containsKey(choice)) throw new InvalidChoiceException("Número fuera de opciones");
                player = pokedex.get(choice).get();
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida (no es un número). Intenta de nuevo.");
            } catch (InvalidChoiceException e) {
                System.out.println("Elección inválida: " + e.getMessage());
            }
        }


        Pokemon cpu;
        do {
            int r = random.nextInt(pokedex.size()) + 1;
            cpu = pokedex.get(r).get();
        } while (cpu.getSpecies().equals(player.getSpecies()));

        System.out.printf("%s eligió %s (HP: %d).\n", playerName, player.getSpecies(), player.getMaxHP());
        System.out.printf("CPU eligió %s (HP: %d).\n\n", cpu.getSpecies(), cpu.getMaxHP());


        boolean playerTurn = true; // el jugador inicia
        while (player.isAlive() && cpu.isAlive()) {
            if (playerTurn) {
                playerTurn(playerName, player, cpu);
            } else {
                cpuTurn("CPU", cpu, player);
            }
            System.out.println();
            mostrarHP(playerName, player, "CPU", cpu);
            System.out.println("-----------------------------------\n");
            playerTurn = !playerTurn;
        }


        System.out.println("=== FIN DE LA BATALLA ===");
        if (player.isAlive() && !cpu.isAlive()) System.out.println(playerName + " ¡ha ganado la batalla! 🎉");
        else if (!player.isAlive() && cpu.isAlive()) System.out.println("CPU ha ganado la batalla. Mejor suerte la próxima.");
        else System.out.println("Empate técnico: ambos Pokémon se debilitaron al mismo tiempo.");

        imprimirResumenEstadisticas(playerName, player, cpu);
    }

    private void mostrarPokedex() {
        System.out.println("Pokémon disponibles:");
        pokedex.forEach((k, sup) -> System.out.println(k + ") " + sup.get().getSpecies()));
    }

    private void playerTurn(String playerName, Pokemon player, Pokemon enemy) {
        System.out.println("--- Turno del jugador: elige un ataque ---");

        List<Attack> available = player.getAttacks().stream()
                .sorted(Comparator.comparingDouble(Attack::getAccuracy).reversed()) // por precisión descendente
                .collect(Collectors.toList());

        for (int i = 0; i < available.size(); i++) {
            Attack a = available.get(i);
            System.out.printf("%d) %s (precisión %.2f)\n", i + 1, a.getName(), a.getAccuracy());
        }

        Attack chosen = null;
        while (chosen == null) {
            try {
                System.out.print("Selecciona número de ataque: ");
                String raw = scanner.nextLine();
                int idx = Integer.parseInt(raw) - 1;
                if (idx < 0 || idx >= available.size()) throw new InvalidChoiceException("Índice de ataque fuera de rango");
                chosen = available.get(idx);
                performAttack(playerName, player, enemy, chosen);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida (no es un número). Intenta de nuevo.");
            } catch (InvalidChoiceException e) {
                System.out.println("Elección inválida: " + e.getMessage());
            } catch (AttackMissedException e) {
                // excepción para ataque fallido (unchecked) -> registrar y continuar
                String msg = String.format("%s intentó %s pero falló.", playerName, e.getAttackName());
                battleLog.add(new Event(playerName, e.getAttackName(), false, 0, enemy.getCurrentHP()));
                System.out.println(msg);
            }
        }
    }

    private void cpuTurn(String cpuName, Pokemon cpu, Pokemon enemy) {
        System.out.println("--- Turno de la CPU ---");

        List<Attack> sorted = cpu.getAttacks().stream()
                .sorted(Comparator.comparingDouble(Attack::getAccuracy).reversed())
                .collect(Collectors.toList());

        Attack pick = sorted.get(0);
        try {
            performAttack(cpuName, cpu, enemy, pick);
        } catch (AttackMissedException e) {
            String msg = String.format("%s intentó %s pero falló.", cpuName, e.getAttackName());
            battleLog.add(new Event(cpuName, e.getAttackName(), false, 0, enemy.getCurrentHP()));
            System.out.println(msg);
        }
    }

    private void performAttack(String actorName, Pokemon attacker, Pokemon defender, Attack attack) throws AttackMissedException {
        System.out.printf("%s usa %s...\n", actorName, attack.getName());
        double roll = random.nextDouble();
        if (roll > attack.getAccuracy()) {

            throw new AttackMissedException(attack.getName());
        }

        int damage = attack.getDamageRule().applyDamage(attacker, defender);
        defender.receiveDamage(damage);

        Event ev = new Event(actorName, attack.getName(), true, damage, defender.getCurrentHP());
        battleLog.add(ev);
        damageHistory.add(damage);

        System.out.printf("¡Golpeó! Infligió %d puntos de daño. %s queda con %d HP.\n",
                damage, defender.getSpecies(), defender.getCurrentHP());
    }

    private void mostrarHP(String playerName, Pokemon player, String cpuName, Pokemon cpu) {
        System.out.printf("%s (%s): %d/%d HP\n", playerName, player.getSpecies(), player.getCurrentHP(), player.getMaxHP());
        System.out.printf("%s (%s): %d/%d HP\n", cpuName, cpu.getSpecies(), cpu.getCurrentHP(), cpu.getMaxHP());
    }

    private void imprimirResumenEstadisticas(String playerName, Pokemon player, Pokemon cpu) {
        System.out.println("\n--- Resumen de eventos (breve log) ---");
        battleLog.forEach(e -> System.out.println(e.toShortString()));


        long totalMisses = battleLog.stream().filter(e -> !e.isHit()).count();
        System.out.println("Total de fallos: " + totalMisses);

        List<Event> top3 = battleLog.stream()
                .filter(Event::isHit)
                .sorted(Comparator.comparingInt(Event::getDamage).reversed())
                .limit(3)
                .collect(Collectors.toList());

        System.out.println("Top 3 golpes más fuertes:");
        if (top3.isEmpty()) System.out.println("  (no hubo golpes acertados)");
        else top3.forEach(e -> System.out.printf("  %s - %s: %d dmg\n", e.getActor(), e.getAttackName(), e.getDamage()));

        double avg = damageHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.printf("Promedio de daño (golpes acertados): %.2f\n", avg);

        Map<String, Long> eventosPorActor = battleLog.stream()
                .collect(Collectors.groupingBy(Event::getActor, Collectors.counting()));
        System.out.println("Conteo de eventos por actor: " + eventosPorActor);


        System.out.println("Daño total por ataque (acertados):");
        Map<String, Integer> damageByAttack = battleLog.stream()
                .filter(Event::isHit)
                .collect(Collectors.groupingBy(Event::getAttackName, Collectors.summingInt(Event::getDamage)));
        damageByAttack.forEach((atk, dmg) -> System.out.println("  " + atk + ": " + dmg));
    }
}


abstract class Pokemon {
    private final String species;
    private final String type;
    private final int maxHP;
    private int currentHP;
    protected final List<Attack> attacks = new ArrayList<>();

    protected Pokemon(String species, String type, int maxHP) {
        this.species = species;
        this.type = type;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
    }

    public String getSpecies() { return species; }
    public String getType() { return type; }
    public int getMaxHP() { return maxHP; }
    public int getCurrentHP() { return currentHP; }
    public List<Attack> getAttacks() { return Collections.unmodifiableList(attacks); }

    public boolean isAlive() { return currentHP > 0; }

    public void receiveDamage(int dmg) {
        currentHP = Math.max(0, currentHP - dmg);
    }
}


class Charmander extends Pokemon {
    public Charmander(String name) {
        super(name, "Fuego", 60);
        // ataques comunes y únicos — regla de daño con lambdas (polimorfismo por tipo implementado en reglas)
        attacks.add(new Attack("Scratch", 0.95, (att, def) -> 6 + new Random().nextInt(5)));
        attacks.add(new Attack("Ember", 0.90, (att, def) -> 8 + new Random().nextInt(6)));
        attacks.add(new Attack("Flamethrower", 0.75, (att, def) -> {
            int base = 14 + new Random().nextInt(6);
            // efecto super efectivo contra Planta
            if (def.getType().equalsIgnoreCase("Planta")) base *= 2;
            return base;
        }));
    }
}

class Squirtle extends Pokemon {
    public Squirtle(String name) {
        super(name, "Agua", 65);
        attacks.add(new Attack("Tackle", 0.96, (att, def) -> 5 + new Random().nextInt(6)));
        attacks.add(new Attack("Water Gun", 0.88, (att, def) -> {
            int base = 9 + new Random().nextInt(6);
            if (def.getType().equalsIgnoreCase("Fuego")) base *= 2;
            return base;
        }));
        attacks.add(new Attack("Bubble", 0.92, (att, def) -> 7 + new Random().nextInt(5)));
    }
}

class Bulbasaur extends Pokemon {
    public Bulbasaur(String name) {
        super(name, "Planta", 62);
        attacks.add(new Attack("Vine Whip", 0.94, (att, def) -> 7 + new Random().nextInt(5)));
        attacks.add(new Attack("Razor Leaf", 0.86, (att, def) -> 10 + new Random().nextInt(6)));
        attacks.add(new Attack("Solar Beam", 0.70, (att, def) -> {
            int base = 16 + new Random().nextInt(6);
            if (def.getType().equalsIgnoreCase("Agua")) base *= 2;
            return base;
        }));
    }
}

class Pikachu extends Pokemon {
    public Pikachu(String name) {
        super(name, "Eléctrico", 58);
        attacks.add(new Attack("Quick Attack", 0.97, (att, def) -> 5 + new Random().nextInt(4)));
        attacks.add(new Attack("Thunder Shock", 0.85, (att, def) -> 11 + new Random().nextInt(6)));
        attacks.add(new Attack("Thunderbolt", 0.74, (att, def) -> {
            int base = 15 + new Random().nextInt(7);
            if (def.getType().equalsIgnoreCase("Agua")) base *= 2;
            return base;
        }));
    }
}

@FunctionalInterface
interface DamageRule {
    int applyDamage(Pokemon attacker, Pokemon defender);
}

class Attack {
    private final String name;
    private final double accuracy; // 0.0 - 1.0
    private final DamageRule damageRule;

    public Attack(String name, double accuracy, DamageRule rule) {
        this.name = name;
        this.accuracy = accuracy;
        this.damageRule = rule;
    }

    public String getName() { return name; }
    public double getAccuracy() { return accuracy; }
    public DamageRule getDamageRule() { return damageRule; }
}

class Event {
    private final String actor;      // "Jugador" o "CPU" (o nombre dle jugador)
    private final String attackName;
    private final boolean hit;
    private final int damage;        // daño infligido (0 si falló)
    private final int defenderRemainingHP;

    public Event(String actor, String attackName, boolean hit, int damage, int defenderRemainingHP) {
        this.actor = actor;
        this.attackName = attackName;
        this.hit = hit;
        this.damage = damage;
        this.defenderRemainingHP = defenderRemainingHP;
    }

    public String getActor() { return actor; }
    public String getAttackName() { return attackName; }
    public boolean isHit() { return hit; }
    public int getDamage() { return damage; }
    public int getDefenderRemainingHP() { return defenderRemainingHP; }

    public String toShortString() {
        if (hit) return String.format("%s usó %s y acertó (%d dmg). Enemigo HP: %d", actor, attackName, damage, defenderRemainingHP);
        else return String.format("%s usó %s y falló.", actor, attackName);
    }
}


class InvalidChoiceException extends Exception {
    public InvalidChoiceException(String message) { super(message); }
}
class AttackMissedException extends RuntimeException {
    private final String attackName;
    public AttackMissedException(String attackName) {
        super("Attack missed: " + attackName);
        this.attackName = attackName;
    }
    public String getAttackName() { return attackName; }
}
