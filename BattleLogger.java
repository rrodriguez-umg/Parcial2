package utils;

import java.util.*;
import java.util.stream.Collectors;

public class BattleLogger {
    private final List<String> log = new ArrayList<>();
    private final List<Integer> damageHistory = new ArrayList<>();
    private final Map<String, Integer> eventCounter = new HashMap<>();
    private int misses = 0;

    public void log(String actor, String event) {
        log.add(actor + ": " + event);
        eventCounter.put(actor, eventCounter.getOrDefault(actor, 0) + 1);
    }

    public void logDamage(int damage) {
        damageHistory.add(damage);
    }

    public void logMiss() {
        misses++;
    }

    public void printSummary() {
        System.out.println("\n--- Resumen de la batalla ---");

        System.out.println("\nEventos:");
        log.forEach(System.out::println);

        System.out.println("\nFallos totales: " + misses);

        System.out.println("\nTop 3 golpes más fuertes:");
        damageHistory.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .forEach(dmg -> System.out.println(" - " + dmg + " de daño"));

        double avg = damageHistory.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0.0);
        System.out.printf("\nPromedio de daño: %.2f\n", avg);

        System.out.println("\nEventos por actor:");
        eventCounter.forEach((actor, count) ->
                System.out.println(" - " + actor + ": " + count));
    }
}
