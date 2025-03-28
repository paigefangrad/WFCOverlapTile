package Matrix;

import Tiles.Direction;
import Tiles.Tile;
import Tiles.TileSet;
import Util.Tuple;

import java.util.*;

public class Solver {
    private static final int MAX_RECURSION_DEPTH = 10;

    private final Matrix matrix;
    private final TileSet tileSet;
    private final Random random;
    private final float randomRatio;
    private boolean running = false;
    private final boolean allowImperfect;
    private HashSet<Cell> cells = new HashSet<>();

    /**
     *
     * @param matrix matrix to do work on.
     * @param tileSet the set of tiles for rule checking.
     * @param allowImperfect allow the solver to continue with tile conflictions instead of failing.
     * @param randomRatio ratio of pure random to weighted random for cell collapse, where 1 is pure random.
     * @param seed (optional) seed of the randomness.
     */
    public Solver(Matrix matrix, TileSet tileSet, boolean allowImperfect, float randomRatio, long seed) {
        this.matrix = matrix;
        this.tileSet = tileSet;
        this.allowImperfect = allowImperfect;
        this.randomRatio = randomRatio;
        this.random = new Random(seed);
    }

    public Solver(Matrix matrix, TileSet tileSet, boolean allowImperfect, float randomRatio) {
        this(matrix, tileSet, allowImperfect, randomRatio, (long)(System.currentTimeMillis() + Math.random()));
    }

    public boolean step() {
        if (!running) {
            setupSolver();
        }

        for (Cell cell : cells) {
            cell.setChecked(false);
        }

        // Get lowest entropy cells
        float minEntropy = Float.POSITIVE_INFINITY;
        ArrayList<Cell> lowest = new ArrayList<>();
        for (Cell cell : cells) {
            if (!cell.isCollapsed()) {
                if (cell.getEntropy() < minEntropy) {
                    minEntropy = cell.getEntropy();
                    lowest.clear();
                    lowest.add(cell);
                } else if (cell.getEntropy() == minEntropy) {
                    lowest.add(cell);
                }
            }
        }

        if (lowest.isEmpty()) {
            running = false;
            return true;
        }

        Cell chosenCell = lowest.get(random.nextInt(lowest.size()));
        if (chosenCell.getOptions().isEmpty()) {
            System.out.println("SOLVE PROBLEM - STOPPING");
            throw new SolveException("Could not solve");
        }

        List<Tile> options = new ArrayList<>(chosenCell.getOptions());
        Tile pick;

        // ratio of pure random to weighted random for cell collapse, where 1 is pure random. interesting to play with
        if (random.nextFloat() < randomRatio) {
            pick = options.get(random.nextInt(options.size()));
        } else {
            pick = weightedRandomSelection(options);
        }

        // Collapse cell
        chosenCell.clearOptions();
        chosenCell.addOption(pick);

        // Reduce entropy
        reduceEntropy(chosenCell, 0);

        // collapse anything with only one possibility
        for (Cell cell : cells) {
            if (cell.getOptions().size() == 1) {
                cell.setCollapsed(true);
                reduceEntropy(cell, 0);
            }
        }
        return false;
    }

    // Recursive reduction of entropy
    private void reduceEntropy(Cell cell, int depth) {
        if (depth > MAX_RECURSION_DEPTH || cell.isChecked()) {
            return;
        }

        cell.calculateEntropy();
        cell.setChecked(true);

        Tuple<Cell, Direction>[] neighbours = matrix.getNeighbours(cell.getPosX(), cell.getPosY());
        for (Tuple<Cell, Direction> neighbour : neighbours) {
            if (checkNeighbourOptions(cell, neighbour.getFirst(), neighbour.getSecond())) {
                reduceEntropy(neighbour.getFirst(), depth + 1);
            }
        }

    }

    // check if neighbour remains consistent with cell adjacency
    boolean checkNeighbourOptions(Cell cell, Cell neighbour, Direction dir) {
        if (!neighbour.isCollapsed()) {
            ArrayList<Tile> validOptions = new ArrayList<>();
            // get all options' direction possibilities
            for (Tile option : cell.getOptions()) {
                validOptions.addAll(option.getPossibleInDir(dir));
            }

            // filter neighbour's options
            ArrayList<Tile> newOptions = new ArrayList<>();
            for (Tile neighbourOption : neighbour.getOptions()) {
                if (validOptions.contains(neighbourOption)) {
                    newOptions.add(neighbourOption);
                }
            }

            // TODO ignoring no option cases is a temp "fix". consider backtracking
            if (allowImperfect && newOptions.isEmpty()) return false;

            // update any new options changes
            if (newOptions.size() < neighbour.getOptions().size()) {
                HashSet<Tile> neighbourOptions = neighbour.getOptions();

                // Keep only options present in newOptions
                Set<Tile> valid = new HashSet<>(newOptions);
                neighbourOptions.retainAll(valid);
                neighbour.calculateEntropy();

                return true;
            }
        }
        return false;
    }

    private void setupSolver() {
        cells = matrix.getCells();
        running = true;
        for (Cell cell : cells) {
            cell.setOptions(new HashSet<>(tileSet.getTiles()));
            cell.setCollapsed(false);
        }
    }

    private Tile weightedRandomSelection(List<Tile> options) {
        // total frequency
        double totalFrequency = 0;
        for (Tile tile : options) {
            totalFrequency += tile.getFrequency();
        }

        double value = random.nextDouble() * totalFrequency;

        // Find the tile that corresponds to the random value
        double cumulativeFrequency = 0;
        for (Tile tile : options) {
            cumulativeFrequency += tile.getFrequency();
            if (value <= cumulativeFrequency) {
                return tile;
            }
        }

        // Fallback (should never happen unless frequencies are all zero)
        return options.get(options.size() - 1);
    }
}
