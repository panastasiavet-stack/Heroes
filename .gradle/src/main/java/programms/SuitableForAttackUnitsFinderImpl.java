package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> battlefieldRows, boolean isPlayerUnit) {
        List<Unit> suitableUnits = new ArrayList<>();

        // Определяем направление атаки (вперед для игрока, назад для врага)
        int attackDirection = isPlayerUnit ? 1 : -1;

        // Проходим по всем строкам (рядам) поля боя
        for (int currentRowIndex = 0; currentRowIndex < battlefieldRows.size(); currentRowIndex++) {
            List<Unit> currentRow = battlefieldRows.get(currentRowIndex);

            // Проверяем каждый юнит в текущем ряду
            for (Unit unit : currentRow) {
                if (!unit.isAlive()) {
                    continue;
                }

                // Определяем, является ли юнит атакующим
                boolean isAttacker = isPlayerUnit ?
                        (currentRowIndex == 2) :  // Предполагаем, что игрок в ряду 2
                        (currentRowIndex == 0);   // Враг в ряду 0

                // Для атакующих юнитов проверяем наличие врагов спереди
                if (isAttacker) {
                    int targetRowIndex = currentRowIndex + attackDirection;

                    if (isValidRowIndex(targetRowIndex, battlefieldRows.size())) {
                        List<Unit> targetRow = battlefieldRows.get(targetRowIndex);

                        // Проверяем, есть ли в целевом ряду враг в той же колонке
                        boolean hasEnemyInSameColumn = targetRow.stream()
                                .anyMatch(targetUnit ->
                                        targetUnit.isAlive() &&
                                                targetUnit.getyCoordinate() == unit.getyCoordinate());

                        // Если врага нет в той же колонке спереди, юнит может атаковать
                        if (!hasEnemyInSameColumn) {
                            suitableUnits.add(unit);
                        }
                    } else {
                        // Если целевой ряд выходит за границы, юнит может атаковать (достиг тыла)
                        suitableUnits.add(unit);
                    }
                }
                // Для юнитов, которые не являются первым рядом, они могут атаковать всегда
                else {
                    suitableUnits.add(unit);
                }
            }
        }

        // Если не найдено подходящих юнитов для атаки
        if (suitableUnits.isEmpty()) {
            System.out.println("Unit cannot find target for attack!");
        }

        return suitableUnits;
    }

    private boolean isValidRowIndex(int rowIndex, int totalRows) {
        return rowIndex >= 0 && rowIndex < totalRows;
    }

    // Альтернативная версия с более простой логикой
    public List<Unit> getSuitableUnitsAlternative(List<List<Unit>> battlefieldRows, boolean isPlayerUnit) {
        List<Unit> suitableUnits = new ArrayList<>();

        // Предполагаем, что поле боя имеет 3 ряда (0, 1, 2)
        // Игрок занимает ряды 1-2, враг занимает ряды 0-1

        for (int row = 0; row < battlefieldRows.size(); row++) {
            List<Unit> rowUnits = battlefieldRows.get(row);

            for (Unit unit : rowUnits) {
                if (!unit.isAlive()) {
                    continue;
                }

                // Проверяем, может ли юнит атаковать
                if (canUnitAttack(unit, row, battlefieldRows, isPlayerUnit)) {
                    suitableUnits.add(unit);
                }
            }
        }

        if (suitableUnits.isEmpty()) {
            System.out.println("No suitable units found for attack!");
        }

        return suitableUnits;
    }

    private boolean canUnitAttack(Unit unit, int currentRow,
                                  List<List<Unit>> battlefieldRows, boolean isPlayerUnit) {
        // Определяем направление атаки
        int direction = isPlayerUnit ? -1 : 1; // Игрок атакует вверх, враг атакует вниз

        // Проверяем следующую строку в направлении атаки
        int targetRow = currentRow + direction;

        // Если целевая строка выходит за пределы, юнит может атаковать (достиг края поля)
        if (targetRow < 0 || targetRow >= battlefieldRows.size()) {
            return true;
        }

        // Проверяем, есть ли враги в той же колонке в следующей строке
        List<Unit> targetRowUnits = battlefieldRows.get(targetRow);

        return targetRowUnits.stream()
                .noneMatch(targetUnit ->
                        targetUnit.isAlive() &&
                                targetUnit.getyCoordinate() == unit.getyCoordinate());
    }
}