package blockescape;

import java.util.*;

/** Console check: BFS-solves each level, then replays the solution through GameController. */
public class LevelCheck {

    static final Block.Direction[] DIRS = Block.Direction.values();

    public static void main(String[] args) {
        LevelManager lm = new LevelManager();
        GameRules rules = new GameRules();
        int failures = 0;

        for (int n = 1; n <= lm.getTotalLevels(); n++) {
            Level lvl = lm.loadLevel(n);
            Board base = lvl.getBoard();
            Block start = new Block(lvl.getStartRow(), lvl.getStartCol());

            // state = block + activated switches (as sorted string)
            Map<String, String[]> parent = new HashMap<>();
            Map<String, List<int[]>> sw = new HashMap<>();
            Map<String, Block> blk = new HashMap<>();
            Deque<String> q = new ArrayDeque<>();
            String k0 = key(start, new ArrayList<>());
            parent.put(k0, null); sw.put(k0, new ArrayList<>()); blk.put(k0, start);
            q.add(k0);
            String goalKey = null;

            while (!q.isEmpty() && goalKey == null) {
                String k = q.poll();
                Block b = blk.get(k);
                List<int[]> active = sw.get(k);
                Board board = build(lm, n, active);
                for (Block.Direction d : DIRS) {
                    GameRules.MoveResult r = rules.move(board, b, d);
                    if (r.getStatus() == GameRules.Status.FALLEN) continue;
                    Block nb = r.getBlock();
                    List<int[]> na = new ArrayList<>(active);
                    if (nb.isUpright()) {
                        Tile t = board.getTile(nb.getRow(), nb.getCol());
                        if (t.isSwitch() && !contains(na, nb.getRow(), nb.getCol())) {
                            Board tmp = build(lm, n, active);
                            if (lm.activateSwitch(nb.getRow(), nb.getCol())) {
                                na.add(new int[]{nb.getRow(), nb.getCol()});
                            }
                        }
                    }
                    String nk = key(nb, na);
                    if (parent.containsKey(nk)) continue;
                    parent.put(nk, new String[]{k, d.name()});
                    sw.put(nk, na); blk.put(nk, nb);
                    if (r.getStatus() == GameRules.Status.WON) { goalKey = nk; break; }
                    q.add(nk);
                }
            }

            if (goalKey == null) {
                System.out.println("Level " + n + ": NOT SOLVABLE");
                failures++;
                continue;
            }
            LinkedList<Block.Direction> path = new LinkedList<>();
            for (String k = goalKey; parent.get(k) != null; k = parent.get(k)[0]) {
                path.addFirst(Block.Direction.valueOf(parent.get(k)[1]));
            }

            // Replay through the real controller (earlier levels first)
            pending = path;
            GameController c = new GameController();
            boolean ok = replayChain(c, n, solutions);
            solutions.put(n, path);
            System.out.println("Level " + n + ": solvable in " + path.size() + " moves "
                    + path + (ok ? "  [controller replay OK]" : "  [controller replay FAILED]"));
            if (!ok) failures++;
        }
        System.out.println(failures == 0 ? "ALL LEVELS PASS" : failures + " FAILURE(S)");
    }

    static final Map<Integer, List<Block.Direction>> solutions = new HashMap<>();

    static boolean replayChain(GameController c, int n, Map<Integer, List<Block.Direction>> sols) {
        for (int i = 1; i < n; i++) {
            if (!play(c, sols.get(i))) return false;
            if (!c.nextLevel()) return false;
        }
        boolean ok = play(c, solutionsPending(n));
        return ok && c.getStatus() == GameRules.Status.WON
                && c.getMoves() == solutionsPending(n).size();
    }

    static List<Block.Direction> pending;
    static List<Block.Direction> solutionsPending(int n) { return pending; }

    static boolean play(GameController c, List<Block.Direction> moves) {
        for (Block.Direction d : moves) {
            if (!c.handleMove(d)) return false;
        }
        return c.getStatus() == GameRules.Status.WON;
    }

    static boolean contains(List<int[]> l, int r, int c) {
        for (int[] s : l) if (s[0] == r && s[1] == c) return true;
        return false;
    }

    static Board build(LevelManager lm, int n, List<int[]> active) {
        Level l = lm.loadLevel(n);
        for (int[] s : active) lm.activateSwitch(s[0], s[1]);
        return l.getBoard();
    }

    static String key(Block b, List<int[]> a) {
        List<String> s = new ArrayList<>();
        for (int[] x : a) s.add(x[0] + "," + x[1]);
        Collections.sort(s);
        return b + "|" + s;
    }
}
