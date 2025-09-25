import model.*;
import exceptions.*;
import java.util.*;
import java.util.stream.Collectors;

public class Game {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // Lista de Pokémon iniciales
        List<Pokemon> starters = List.of(
                new Charmander(),
                new Squirtle(),
                new Bulbasaur(),
                new Pikachu()
        );

        System.out.println("¡Bienvenido al mini juego Pokémon!");
        System.out.print("Ingresa tu nombre: ");
        String playerName = scanner.nextLine();

        // Mostrar opciones al jugador
        System.out.println("\nElige tu Pokémon:");
        for (int i = 0; i < starters.size(); i++) {
            System.out.println((i + 1) + ". " + starters.get(i).getName());
        }

        Pokemon playerPokemon = null;

        // Selección con validación y manejo de excepciones
        while (playerPokemon == null) {
            try {
                System.out.print("Selecciona (1-" + starters.size() + "): ");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > starters.size()) {
                    throw new InvalidChoiceException("Opción inválida, intenta de nuevo.");
                }
                playerPokemon = starters.get(choice - 1);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingresa un número válido.");
            } catch (InvalidChoiceException e) {
                System.out.println(e.getMessage());
            }
        }

        // Elegir Pokémon CPU distinto
        final String nombreJugadorPokemon = playerPokemon.getName();
        List<Pokemon> opcionesCPU = starters.stream()
                .filter(p -> !p.getName().equals(nombreJugadorPokemon))
                .collect(Collectors.toList());

        Pokemon cpuPokemon = opcionesCPU.get(random.nextInt(opcionesCPU.size()));

        System.out.println("Has elegido a " + playerPokemon.getName());
        System.out.println("La CPU eligió a " + cpuPokemon.getName());

        BattleLogger logger = new BattleLogger();

        boolean playerTurn = true;

        // Ciclo de batalla
        while (playerPokemon.isAlive() && cpuPokemon.isAlive()) {

            if (playerTurn) {
                System.out.println("\nTurno de " + playerName);
                List<Attack> attacks = playerPokemon.getAttacks();

                for (int i = 0; i < attacks.size(); i++) {
                    System.out.println((i + 1) + ". " + attacks.get(i).getName());
                }

                try {
                    System.out.print("Elige un ataque: ");
                    int atkChoice = Integer.parseInt(scanner.nextLine());
                    if (atkChoice < 1 || atkChoice > attacks.size()) {
                        throw new InvalidChoiceException("Ataque inválido, intenta de nuevo.");
                    }
                    Attack attack = attacks.get(atkChoice - 1);
                    int damage = attack.execute(playerPokemon, cpuPokemon);
                    System.out.println(playerName + " usó " + attack.getName() + " y causó " + damage + " de daño.");
                    logger.log(playerName, attack.getName() + " causó " + damage + " de daño.");
                    logger.logDamage(damage);
                } catch (InvalidChoiceException e) {
                    System.out.println(e.getMessage());
                    continue; // repetir el turno
                } catch (AttackMissedException e) {
                    System.out.println("El ataque falló!");
                    logger.log(playerName, "falló el ataque.");
                    logger.logMiss();
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida, ingresa un número.");
                    continue; // repetir turno
                }
            } else {
                System.out.println("\nTurno de la CPU:");
                List<Attack> cpuAttacks = cpuPokemon.getAttacks();
                Attack cpuAttack = cpuAttacks.get(random.nextInt(cpuAttacks.size()));

                try {
                    int damage = cpuAttack.execute(cpuPokemon, playerPokemon);
                    System.out.println("CPU usó " + cpuAttack.getName() + " y causó " + damage + " de daño.");
                    logger.log("CPU", cpuAttack.getName() + " causó " + damage + " de daño.");
                    logger.logDamage(damage);
                } catch (AttackMissedException e) {
                    System.out.println("CPU falló el ataque!");
                    logger.log("CPU", "falló el ataque.");
                    logger.logMiss();
                }
            }

            // Mostrar HP actuales
            System.out.println("\nHP actuales:");
            System.out.println(playerName + " (" + playerPokemon.getName() + "): " + playerPokemon.getCurrentHP() + "/" + playerPokemon.getMaxHP());
            System.out.println("CPU (" + cpuPokemon.getName() + "): " + cpuPokemon.getCurrentHP() + "/" + cpuPokemon.getMaxHP());

            playerTurn = !playerTurn; // alternar turno
        }

        // Resultado
        System.out.println("\n--- Resultado de la batalla ---");
        if (playerPokemon.isAlive() && !cpuPokemon.isAlive()) {
            System.out.println(playerName + " ganó la batalla!");
        } else if (!playerPokemon.isAlive() && cpuPokemon.isAlive()) {
            System.out.println("La CPU ganó la batalla!");
        } else {
            System.out.println("¡Empate!");
        }

        System.out.println("\n--- Resumen de la batalla ---");
        logger.printSummary();

        scanner.close();
    }
}
