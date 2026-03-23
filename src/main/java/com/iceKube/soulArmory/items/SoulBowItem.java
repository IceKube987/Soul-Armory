package com.iceKube.soulArmory.items;

public class SoulBowItem extends BaseSoulWeaponItem {
    public SoulBowItem(Properties pProperties) {
        super(pProperties);
        doApplySpeedModifier = true;
    }

    @Override
    public int getGracePeriodTicks() {
        return 0;
    }

    @Override
    public int getSoulDecaySpeed() {
        return 0;
    }

    @Override
    public int getMaxSoul() {
        return 0;
    }

    @Override
    public int getPointPerSpeedPercent() {
        return 0;
    }
}
