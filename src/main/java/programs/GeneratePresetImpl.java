package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratePresetImpl implements GeneratePreset {
    private static final int MAX_RETRY_COUNT = 100;
    private static final int MAX_UNITS_PER_TYPE = 11;
    private static final int BOARD_WIDTH = 21;
    private static final int BOARD_HEIGHT = 3;

    public Army generate(List<Unit> availableUnits, int budget) {
        Army army = new Army();
        List<Unit> placedUnits = new ArrayList<>();
        Map<String, Integer> unitTypeCount = new HashMap<>();
        Random random = new Random();
        int usedPoints = 0;

        // Сортируем юниты по эффективности (атака/стоимость, затем здоровье/стоимость)
        List<Unit> sortedUnits = availableUnits.stream()
                .sorted(this::compareUnitEfficiency)
                .collect(Collectors.toCollection(LinkedList::new));

        while (budget > 0 && !sortedUnits.isEmpty()) {
            Unit template = sortedUnits.getFirst();
            String unitType = template.getUnitType();
            int cost = template.getCost();
            int currentCount = unitTypeCount.getOrDefault(unitType, 0);

            // Проверяем можно ли добавить юнит этого типа
            if (currentCount < MAX_UNITS_PER_TYPE && budget >= cost) {
                Optional<Coordinates> coordinates = findAvailableCoordinates(
                        placedUnits, unitType, random, 0);

                if (coordinates.isPresent()) {
                    currentCount++;
                    unitTypeCount.put(unitType, currentCount);

                    // Создаем новый юнит с уникальным именем и координатами
                    String uniqueName = unitType + " " + currentCount;
                    Unit newUnit = createUnitWithCoordinates(template, uniqueName,
                            coordinates.get().x(), coordinates.get().y());

                    placedUnits.add(newUnit);
                    army.getUnits().add(newUnit);
                    budget -= cost;
                    usedPoints += cost;

                    System.out.println("Added " + placedUnits.size() + " unit: " + uniqueName);
                } else {
                    System.out.println("No available coordinates found for: " + unitType);
                    sortedUnits.removeFirst();
                }
            } else if (currentCount >= MAX_UNITS_PER_TYPE) {
                // Удаляем этот тип юнитов из списка, если достигли лимита
                sortedUnits.removeIf(unit -> unit.getUnitType().equals(unitType));
            } else {
                // Этот юнит слишком дорогой, пробуем следующий
                sortedUnits.removeFirst();
            }
        }

        System.out.println("Used points: " + usedPoints);
        return army;
    }

    private Optional<Coordinates> findAvailableCoordinates(List<Unit> placedUnits,
                                                           String unitType,
                                                           Random random,
                                                           int attempt) {
        for (int i = attempt; i < MAX_RETRY_COUNT; i++) {
            int x = random.nextInt(BOARD_HEIGHT);
            int y = random.nextInt(BOARD_WIDTH);

            boolean positionOccupied = placedUnits.stream()
                    .anyMatch(unit -> unit.getxCoordinate() == x && unit.getyCoordinate() == y);

            if (!positionOccupied) {
                return Optional.of(new Coordinates(x, y));
            }
        }

        return Optional.empty();
    }

    private int compareUnitEfficiency(Unit unit1, Unit unit2) {
        double efficiency1 = (double) unit1.getBaseAttack() / unit1.getCost();
        double efficiency2 = (double) unit2.getBaseAttack() / unit2.getCost();

        if (Double.compare(efficiency2, efficiency1) != 0) {
            return Double.compare(efficiency2, efficiency1);
        }

        double durability1 = (double) unit1.getHealth() / unit1.getCost();
        double durability2 = (double) unit2.getHealth() / unit2.getCost();
        return Double.compare(durability2, durability1);
    }

    private Unit createUnitWithCoordinates(Unit template, String name, int x, int y) {
        return new Unit(
                name,
                template.getUnitType(),
                template.getHealth(),
                template.getBaseAttack(),
                template.getCost(),
                template.getAttackType(),
                template.getAttackBonuses(),
                template.getDefenceBonuses(),
                x,
                y
        );
    }

    private record Coordinates(int x, int y) {}
}