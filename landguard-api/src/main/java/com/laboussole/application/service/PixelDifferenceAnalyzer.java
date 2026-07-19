package com.laboussole.application.service;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

/**
 * Pixel-gap analysis engine (PRD Feature 03.1 / 4.1): compares two successive
 * images of the same parcel and scores <em>structural</em> change.
 *
 * <p>Mali adaptation: seasonal colour swings (greening during the rainy season,
 * drying in the dry season) shift hues across the whole scene but barely alter
 * edge topology. The comparison is therefore performed on gradient (edge)
 * structure computed from luminance, not on raw colour — a wall, a brick pile
 * or a foundation slab creates new edges; a browning field does not. A global
 * hue shift between the two images is additionally reported as
 * {@code seasonalShiftDetected} so callers can annotate the snapshot.
 */
@Service
public class PixelDifferenceAnalyzer {

    /** Working resolution: both images are resampled onto this square grid. */
    private static final int GRID = 96;
    /** Cell size (in grid pixels) for local edge-density comparison. */
    private static final int CELL = 8;
    /** Minimum gradient magnitude for a pixel to count as an edge. */
    private static final double EDGE_THRESHOLD = 24.0;
    /** A cell is "changed" when its edge density grows by more than this. */
    private static final double CELL_DENSITY_DELTA = 0.12;
    /** Mean per-channel shift (0-255) beyond which we call the change seasonal. */
    private static final double SEASONAL_HUE_SHIFT = 18.0;

    public record ImageryAnalysis(
            int structuralChangeScore,
            int confidenceScore,
            boolean seasonalShiftDetected) {
    }

    public ImageryAnalysis analyze(BufferedImage before, BufferedImage after) {
        double[][] lumBefore = luminanceGrid(before);
        double[][] lumAfter = luminanceGrid(after);

        boolean[][] edgesBefore = edgeMap(lumBefore);
        boolean[][] edgesAfter = edgeMap(lumAfter);

        int cells = GRID / CELL;
        int changedCells = 0;
        int clusteredCells = 0;
        boolean[][] changed = new boolean[cells][cells];

        for (int cy = 0; cy < cells; cy++) {
            for (int cx = 0; cx < cells; cx++) {
                double densityBefore = edgeDensity(edgesBefore, cx, cy);
                double densityAfter = edgeDensity(edgesAfter, cx, cy);
                if (densityAfter - densityBefore > CELL_DENSITY_DELTA) {
                    changed[cy][cx] = true;
                    changedCells++;
                }
            }
        }
        // New buildings are spatially contiguous: count changed cells having a
        // changed neighbour. Isolated cells are more likely sensor noise.
        for (int cy = 0; cy < cells; cy++) {
            for (int cx = 0; cx < cells; cx++) {
                if (changed[cy][cx] && hasChangedNeighbour(changed, cx, cy)) {
                    clusteredCells++;
                }
            }
        }

        int totalCells = cells * cells;
        int structuralScore = clamp((int) Math.round(250.0 * changedCells / totalCells));
        // Confidence rises with spatial clustering: all-isolated changes yield
        // low confidence even when many cells moved.
        double clusterRatio = changedCells == 0 ? 0 : (double) clusteredCells / changedCells;
        int confidence = clamp((int) Math.round(structuralScore * (0.4 + 0.6 * clusterRatio)));

        return new ImageryAnalysis(structuralScore, confidence, isSeasonalShift(before, after));
    }

    private static double[][] luminanceGrid(BufferedImage image) {
        double[][] grid = new double[GRID][GRID];
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int rgb = sample(image, x, y);
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                grid[y][x] = 0.2126 * r + 0.7152 * g + 0.0722 * b;
            }
        }
        return grid;
    }

    /** Sobel gradient magnitude thresholded into a binary edge map. */
    private static boolean[][] edgeMap(double[][] lum) {
        boolean[][] edges = new boolean[GRID][GRID];
        for (int y = 1; y < GRID - 1; y++) {
            for (int x = 1; x < GRID - 1; x++) {
                double gx = (lum[y - 1][x + 1] + 2 * lum[y][x + 1] + lum[y + 1][x + 1])
                        - (lum[y - 1][x - 1] + 2 * lum[y][x - 1] + lum[y + 1][x - 1]);
                double gy = (lum[y + 1][x - 1] + 2 * lum[y + 1][x] + lum[y + 1][x + 1])
                        - (lum[y - 1][x - 1] + 2 * lum[y - 1][x] + lum[y - 1][x + 1]);
                edges[y][x] = Math.hypot(gx, gy) > EDGE_THRESHOLD * 4;
            }
        }
        return edges;
    }

    private static double edgeDensity(boolean[][] edges, int cellX, int cellY) {
        int count = 0;
        for (int y = cellY * CELL; y < (cellY + 1) * CELL; y++) {
            for (int x = cellX * CELL; x < (cellX + 1) * CELL; x++) {
                if (edges[y][x]) count++;
            }
        }
        return (double) count / (CELL * CELL);
    }

    private static boolean hasChangedNeighbour(boolean[][] changed, int cx, int cy) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = cx + dx, ny = cy + dy;
                if (ny >= 0 && ny < changed.length && nx >= 0 && nx < changed.length
                        && changed[ny][nx]) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A large uniform shift of the green/red balance across the whole scene. */
    private static boolean isSeasonalShift(BufferedImage before, BufferedImage after) {
        double shift = Math.abs(greenRedBalance(after) - greenRedBalance(before));
        return shift > SEASONAL_HUE_SHIFT;
    }

    private static double greenRedBalance(BufferedImage image) {
        double sum = 0;
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int rgb = sample(image, x, y);
                sum += ((rgb >> 8) & 0xFF) - ((rgb >> 16) & 0xFF);
            }
        }
        return sum / (GRID * GRID);
    }

    /** Nearest-neighbour sample of the source image at grid position (x, y). */
    private static int sample(BufferedImage image, int x, int y) {
        int sx = Math.min(image.getWidth() - 1, x * image.getWidth() / GRID);
        int sy = Math.min(image.getHeight() - 1, y * image.getHeight() / GRID);
        return image.getRGB(sx, sy);
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
