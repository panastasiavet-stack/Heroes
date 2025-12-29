package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.EdgeDistance;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;
import java.util.stream.Collectors;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;
    private static final int INFINITY = Integer.MAX_VALUE;

    // 8 направлений движения (включая диагонали)
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Вверх, вниз, влево, вправо
            {-1, -1}, {1, 1}, {-1, 1}, {1, -1}  // Диагонали
    };

    @Override
    public List<Edge> getTargetPath(Unit attacker, Unit target, List<Unit> allUnits) {
        // Матрицы для алгоритма
        int[][] distances = new int[WIDTH][HEIGHT];
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        Edge[][] previous = new Edge[WIDTH][HEIGHT];

        // Инициализация матриц
        initializeMatrices(distances, visited);

        // Приоритетная очередь для алгоритма Dijkstra
        PriorityQueue<EdgeDistance> queue = new PriorityQueue<>(
                Comparator.comparingInt(EdgeDistance::getDistance)
        );

        int startX = attacker.getxCoordinate();
        int startY = attacker.getyCoordinate();
        distances[startX][startY] = 0;
        queue.add(new EdgeDistance(startX, startY, 0));

        // Собираем координаты непроходимых клеток (живые юниты, кроме атакующего и цели)
        Set<String> obstacles = getAllObstacles(allUnits, attacker, target);

        // Алгоритм поиска пути
        while (!queue.isEmpty()) {
            EdgeDistance current = queue.poll();
            int currentX = current.getX();
            int currentY = current.getY();

            // Если уже посещали эту клетку, пропускаем
            if (visited[currentX][currentY]) {
                continue;
            }

            visited[currentX][currentY] = true;

            // Если достигли цели
            if (currentX == target.getxCoordinate() && currentY == target.getyCoordinate()) {
                break;
            }

            // Исследуем соседние клетки
            exploreNeighbors(currentX, currentY, distances, visited, previous,
                    queue, obstacles, target);
        }

        // Восстанавливаем путь или возвращаем пустой список
        return reconstructPath(startX, startY, target, previous, attacker);
    }

    private void initializeMatrices(int[][] distances, boolean[][] visited) {
        for (int x = 0; x < WIDTH; x++) {
            Arrays.fill(distances[x], INFINITY);
            Arrays.fill(visited[x], false);
        }
    }

    private Set<String> getAllObstacles(List<Unit> allUnits, Unit attacker, Unit target) {
        Set<String> obstacles = new HashSet<>();

        for (Unit unit : allUnits) {
            // Пропускаем атакующего, цель и мертвых юнитов
            if (unit == attacker || unit == target || !unit.isAlive()) {
                continue;
            }

            int x = unit.getxCoordinate();
            int y = unit.getyCoordinate();
            obstacles.add(x + "," + y);
        }

        return obstacles;
    }

    private void exploreNeighbors(int currentX, int currentY,
                                  int[][] distances, boolean[][] visited,
                                  Edge[][] previous, PriorityQueue<EdgeDistance> queue,
                                  Set<String> obstacles, Unit target) {
        for (int[] direction : DIRECTIONS) {
            int neighborX = currentX + direction[0];
            int neighborY = currentY + direction[1];

            // Проверяем, является ли соседняя клетка проходимой
            if (!isValidCell(neighborX, neighborY, obstacles, target)) {
                continue;
            }

            // Если клетка уже посещена, пропускаем
            if (visited[neighborX][neighborY]) {
                continue;
            }

            // Рассчитываем новое расстояние
            int newDistance = distances[currentX][currentY] + 1;

            // Если нашли более короткий путь
            if (newDistance < distances[neighborX][neighborY]) {
                distances[neighborX][neighborY] = newDistance;
                previous[neighborX][neighborY] = new Edge(currentX, currentY);
                queue.add(new EdgeDistance(neighborX, neighborY, newDistance));
            }
        }
    }

    private boolean isValidCell(int x, int y, Set<String> obstacles, Unit target) {
        // Проверяем границы поля
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return false;
        }

        String cellKey = x + "," + y;

        // Проверяем, является ли клетка препятствием
        if (obstacles.contains(cellKey)) {
            return false;
        }

        // Проверяем, является ли клетка целью (цель всегда проходима)
        if (x == target.getxCoordinate() && y == target.getyCoordinate()) {
            return true;
        }

        return true;
    }

    private List<Edge> reconstructPath(int startX, int startY, Unit target,
                                       Edge[][] previous, Unit attacker) {
        int targetX = target.getxCoordinate();
        int targetY = target.getyCoordinate();

        // Если путь не найден
        if (previous[targetX][targetY] == null) {
            System.out.println("Unit " + attacker.getName() +
                    " cannot find path to attack unit " + target.getName());
            return new ArrayList<>();
        }

        // Восстанавливаем путь от цели к началу
        List<Edge> path = new ArrayList<>();
        int currentX = targetX;
        int currentY = targetY;

        while (currentX != startX || currentY != startY) {
            path.add(new Edge(currentX, currentY));
            Edge prev = previous[currentX][currentY];
            currentX = prev.getX();
            currentY = prev.getY();
        }

        // Добавляем стартовую точку
        path.add(new Edge(startX, startY));

        // Разворачиваем путь (от старта к цели)
        Collections.reverse(path);

        return path;
    }

    // Дополнительный метод для эвристической функции (для алгоритма A*)
    private int heuristic(int x1, int y1, int x2, int y2) {
        // Манхэттенское расстояние
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);

        // Или евклидово расстояние
        // return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    // Альтернативная версия с алгоритмом A*
    public List<Edge> getTargetPathAStar(Unit attacker, Unit target, List<Unit> allUnits) {
        // Матрицы для алгоритма
        int[][] gScore = new int[WIDTH][HEIGHT];
        int[][] fScore = new int[WIDTH][HEIGHT];
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        Edge[][] previous = new Edge[WIDTH][HEIGHT];

        // Инициализация
        initializeMatrices(gScore, visited);
        for (int x = 0; x < WIDTH; x++) {
            Arrays.fill(fScore[x], INFINITY);
        }

        Set<String> obstacles = getAllObstacles(allUnits, attacker, target);

        int startX = attacker.getxCoordinate();
        int startY = attacker.getyCoordinate();
        int targetX = target.getxCoordinate();
        int targetY = target.getyCoordinate();

        // Приоритетная очередь для A*
        PriorityQueue<EdgeDistance> openSet = new PriorityQueue<>(
                Comparator.comparingInt(EdgeDistance::getDistance)
        );

        gScore[startX][startY] = 0;
        fScore[startX][startY] = heuristic(startX, startY, targetX, targetY);
        openSet.add(new EdgeDistance(startX, startY, fScore[startX][startY]));

        while (!openSet.isEmpty()) {
            EdgeDistance current = openSet.poll();
            int currentX = current.getX();
            int currentY = current.getY();

            if (visited[currentX][currentY]) {
                continue;
            }

            visited[currentX][currentY] = true;

            if (currentX == targetX && currentY == targetY) {
                break;
            }

            for (int[] direction : DIRECTIONS) {
                int neighborX = currentX + direction[0];
                int neighborY = currentY + direction[1];

                if (!isValidCell(neighborX, neighborY, obstacles, target)) {
                    continue;
                }

                // Предполагаем, что стоимость каждого шага = 1
                int tentativeGScore = gScore[currentX][currentY] + 1;

                if (tentativeGScore < gScore[neighborX][neighborY]) {
                    previous[neighborX][neighborY] = new Edge(currentX, currentY);
                    gScore[neighborX][neighborY] = tentativeGScore;
                    fScore[neighborX][neighborY] = tentativeGScore +
                            heuristic(neighborX, neighborY, targetX, targetY);

                    if (!openSet.stream().anyMatch(e -> e.getX() == neighborX && e.getY() == neighborY)) {
                        openSet.add(new EdgeDistance(neighborX, neighborY, fScore[neighborX][neighborY]));
                    }
                }
            }
        }

        return reconstructPath(startX, startY, target, previous, attacker);
    }
}