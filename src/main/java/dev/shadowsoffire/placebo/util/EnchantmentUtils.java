package dev.shadowsoffire.placebo.util;

import net.minecraft.world.entity.player.Player;

/**
 * Lightly stolen from Bookshelf
 */
public class EnchantmentUtils {

    /**
     * Attempts to charge the player an experience point cost. If the player can not afford the full amount they will
     * not be charged and false will be returned.
     *
     * @param player The player to charge.
     * @param cost   The amount to charge the player in experience points.
     * @return True if the amount was paid.
     */
    public static boolean chargeExperience(Player player, int cost) {
        final int playerExperience = getExperience(player);

        if (playerExperience >= cost) {
            player.giveExperiencePoints(-cost);

            // Due to rounding errors, the bar can get stuck displaying nothing when it should be empty.
            if (getExperience(player) <= 0) player.experienceProgress = 0F;
            return true;
        }
        return false;
    }

    /**
     * Calculates the amount of experience points the player currently has. This should be used in favour of {@link
     * Player#totalExperience} which deceptively does not track the amount of experience the player currently has.
     * <p>
     * Contrary to popular belief the {@link Player#totalExperience} value actually loosely represents how much
     * experience points the player has earned during their current life. This value is akin to the old player score
     * metric and appears to be predominantly legacy code. Relying on this value is often incorrect as negative changes
     * to the player level such as enchanting, the anvil, and the level command will not reduce this value.
     *
     * @param player The player to calculate the total experience points of.
     * @return The amount of experience points held by the player.
     */
    public static int getExperience(Player player) {
        // Start by calculating how many EXP points the player's current level is worth.
        int exp = getTotalExperienceForLevel(player.experienceLevel);

        // Add the amount of experience points the player has earned towards their next level.
        exp += player.experienceProgress * getTotalExperienceForLevel(player.experienceLevel + 1);

        return exp;
    }

    /**
     * Calculates the amount of experience the passed level is worth.<br>
     * Reference: https://minecraft.wiki/w/Experience#Leveling_up
     *
     * @param level The target level.
     * @return The amount of experience required to reach the given level when starting from the previous level.
     */
    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level > 30) return 112 + (level - 31) * 9;
        if (level > 15) return 37 + (level - 16) * 5;
        return 7 + (level - 1) * 2;
    }

    /**
     * Calculates the difference in experience points between the start and target levels.
     *
     * @param start  The starting level.
     * @param target The target level.
     * @return The amount of experience required to go from the starting level to the target level.
     */
    public static int getExperienceDifference(int start, int target) {
        if (target < start || start < 0) throw new IllegalArgumentException("Invalid start/target");

        if (target == start) return 0;

        int expReq = 0;
        for (int lvl = start + 1; lvl <= target; lvl++) {
            expReq += getExperienceForLevel(lvl);
        }
        return expReq;
    }

    /**
     * Calculates the total experience required to reach the given level when starting at level 0.
     *
     * @param level The target level.
     * @return The amount of experience required to reach the target level.
     */
    public static int getTotalExperienceForLevel(int level) {
        return getExperienceDifference(0, level);
    }
}
