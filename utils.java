import java.util.*;
import java.util.stream.Collectors;

public class utils {
    private final List<String> log = new ArrayList<>();
    private final List<Integer> damageHistory = new ArrayList<>();
    private final Map<String, Integer> eventCounter = new HashMap<>();
    private int misses = 0;

    // Guarda un mensaje en el log
    public void log(String actor, String event) {
        String message = actor + ": " + event;
        log.add(message);
        eventCounter.put(actor, eventCounter.getOrDefault(actor, 0) + 1);
    }

    // Guarda el daño infligido
    public void logDamage(int damage) {
        damageHistory.add(damage);
    }

    // Cuenta fallos
    public void logMiss() {
        misses++;
    }

    // Muestra todo el resumen al final
    public void printSummary() {
        System.out.println("\n--- Resumen de la batalla ---");

        // Log de eventos
        System.out.println("\n📜 Eventos:");
        log.forEach(System.out::println);

        // Total de fallos
        System.out.println("\n❌ Fallos totales: " + misses);

        // Top 3 golpes más fuertes
        System.out.println("\n💥 Top 3 golpes más fuertes:");
        damageHistory.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .forEach(dmg -> System.out.println(" - " + dmg + " de daño"));

        // Promedio de daño
        double avg = damageHistory.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0.0);
        System.out.printf("\n📊 Promedio de daño: %.2f\n", avg);

        // Conteo de eventos por actor
        System.out.println("\n📈 Eventos por actor:");
        eventCounter.forEach((actor, count) ->
                System.out.println(" - " + actor + ": " + count + " eventos")
        );
    }
}
