package com.pm.clashbenchdetectionsystem.scoring;

public final class CbdsCalculator {

    private CbdsCalculator() {}
    public static AttackScore calculateAttack(int stars, double destructionPct,
                                              int attackerMapPos, int defenderMapPos,
                                              int attackerTh, int defenderTh) {

        int baseScore = baseScore(stars);
        int destructionMod = destructionModifier(stars, destructionPct);
        int positionMod = positionModifier(attackerMapPos, defenderMapPos);
        int thMod = thModifier(attackerTh, defenderTh);
        int topBase = topBaseBonus(defenderMapPos);
        int gimme = gimmePenalty(defenderMapPos, stars);

        int total = baseScore + destructionMod + positionMod + thMod + topBase + gimme;

        return new AttackScore(baseScore, destructionMod, positionMod, thMod, topBase, gimme, total);
    }


    static int baseScore(int stars) {
        return switch (stars) {
            case 3 -> 100;
            case 2 -> 50;
            case 1 -> 10;
            default -> 0;
        };
    }

    static int destructionModifier(int stars, double destructionPct) {
        if (stars == 3) return 0;

        return switch (stars) {
            case 2 -> {
                if (destructionPct >= 95) yield 15;
                if (destructionPct >= 87) yield 10;
                if (destructionPct >= 70) yield 0;
                if (destructionPct >= 50) yield -10;
                yield -20;
            }
            case 1 -> {
                if (destructionPct >= 70) yield 0;
                if (destructionPct >= 50) yield -10;
                yield -20;
            }
            default -> { // 0 stars
                if (destructionPct >= 50) yield -10;
                yield -25;
            }
        };
    }

    static int positionModifier(int attackerMapPos, int defenderMapPos) {
        // Positive diff = weaker player (high pos) attacking stronger base (low pos) = attacking UP → reward
        // Negative diff = stronger player (low pos) attacking weaker base (high pos) = attacking DOWN → penalize
        int diff = attackerMapPos - defenderMapPos;

        if (diff >= 5) return 20;
        if (diff >= 3) return 10;
        if (diff >= -2) return 0;
        if (diff >= -4) return -10;
        return -15;
    }

    static int thModifier(int attackerTh, int defenderTh) {
        int diff = attackerTh - defenderTh;

        if (diff < 0) return 10;
        if (diff == 0) return 0;
        return -10;
    }

    static int topBaseBonus(int defenderMapPos) {
        return defenderMapPos <= 3 ? 15 : 0;
    }

    static int gimmePenalty(int defenderMapPos, int stars) {
        return (defenderMapPos >= 13 && stars < 3) ? -20 : 0;
    }
}
