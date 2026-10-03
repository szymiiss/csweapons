package com.example.csweapons;

public record GunStats(float damage, int cooldown, double range, float spread, int magSize,
                       int reloadTicks, float volume, float pitch, boolean loud, boolean scoped) {}
