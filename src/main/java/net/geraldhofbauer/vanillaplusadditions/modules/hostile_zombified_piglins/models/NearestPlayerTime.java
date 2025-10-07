package net.geraldhofbauer.vanillaplusadditions.modules.hostile_zombified_piglins.models;


import net.minecraft.world.entity.player.Player;

public record NearestPlayerTime(Player player, long timeStamp) implements Comparable<NearestPlayerTime> {
    @Override
    public int compareTo(NearestPlayerTime other) {
        return Long.compare(this.timeStamp, other.timeStamp);
    }
}
