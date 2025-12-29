package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;
import java.util.stream.Collectors;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        int currentRound = 1;

        List<Unit> playerUnits = playerArmy.getUnits().stream()
                .filter(Unit::isAlive)
                .collect(Collectors.toList());

        List<Unit> computerUnits = computerArmy.getUnits().stream()
                .filter(Unit::isAlive)
                .collect(Collectors.toList());

        // Если одна из армий уже пустая, битва не начинается
        if (playerUnits.isEmpty() || computerUnits.isEmpty()) {
            System.out.println("Battle cannot start: one of the armies has no living units");
            return;
        }

        Set<Unit> unitsActedThisRound = new HashSet<>();

        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            boolean allUnitsActed = true;

            // Очереди приоритета для юнитов, отсортированных по атаке
            PriorityQueue<Unit> playerQueue = new PriorityQueue<>(
                    Comparator.comparingInt(Unit::getBaseAttack).reversed()
            );
            PriorityQueue<Unit> computerQueue = new PriorityQueue<>(
                    Comparator.comparingInt(Unit::getBaseAttack).reversed()
            );

            // Обновляем списки живых юнитов
            playerUnits = playerArmy.getUnits().stream()
                    .filter(Unit::isAlive)
                    .collect(Collectors.toList());
            computerUnits = computerArmy.getUnits().stream()
                    .filter(Unit::isAlive)
                    .collect(Collectors.toList());

            // Добавляем в очередь только тех, кто ещё не действовал в этом раунде
            playerQueue.addAll(playerUnits.stream()
                    .filter(unit -> !unitsActedThisRound.contains(unit))
                    .collect(Collectors.toList()));
            computerQueue.addAll(computerUnits.stream()
                    .filter(unit -> !unitsActedThisRound.contains(unit))
                    .collect(Collectors.toList()));

            // Симуляция раунда
            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {
                // Ход юнита игрока
                if (!playerQueue.isEmpty()) {
                    Unit playerUnit = playerQueue.poll();
                    Unit target = performUnitAttack(playerUnit);

                    if (target != null && !target.isAlive() &&
                            !unitsActedThisRound.contains(target)) {
                        allUnitsActed = false;
                        break;
                    }

                    unitsActedThisRound.add(playerUnit);
                }

                // Ход юнита компьютера
                if (!computerQueue.isEmpty()) {
                    Unit computerUnit = computerQueue.poll();
                    Unit target = performUnitAttack(computerUnit);

                    if (target != null && !target.isAlive() &&
                            !unitsActedThisRound.contains(target)) {
                        allUnitsActed = false;
                        break;
                    }

                    unitsActedThisRound.add(computerUnit);
                }
            }

            // Если все юниты действовали в этом раунде, завершаем раунд
            if (allUnitsActed) {
                printRoundSummary(currentRound, playerUnits.size(), computerUnits.size());
                currentRound++;
                unitsActedThisRound.clear();
            }
        }

        // Битва завершена
        printBattleResult(playerUnits.size(), computerUnits.size());
    }

    private Unit performUnitAttack(Unit attacker) throws InterruptedException {
        Unit target = attacker.getProgram().attack();
        printBattleLog.printBattleLog(attacker, target);
        return target;
    }

    private void printRoundSummary(int round, int playerUnitCount, int computerUnitCount) {
        System.out.println();
        System.out.println("Round " + round + " is over!");
        System.out.println("Player army has " + playerUnitCount + " units");
        System.out.println("Computer army has " + computerUnitCount + " units");
        System.out.println();
    }

    private void printBattleResult(int playerUnitCount, int computerUnitCount) {
        System.out.println("Battle is over!");

        if (playerUnitCount == 0 && computerUnitCount == 0) {
            System.out.println("It's a draw!");
        } else if (playerUnitCount > 0) {
            System.out.println("Player wins!");
        } else {
            System.out.println("Computer wins!");
        }
    }
}