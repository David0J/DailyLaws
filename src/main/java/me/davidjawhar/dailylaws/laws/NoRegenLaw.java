package me.davidjawhar.dailylaws.laws;

import me.davidjawhar.dailylaws.Law;
import me.davidjawhar.dailylaws.LawContext;

public class NoRegenLaw implements Law {
    @Override public String id() { return "NO_REGEN"; }
    @Override public String description() { return "Natural regen disabled"; }

    @Override public void onActivate(LawContext ctx) {}
    @Override public void onDeactivate(LawContext ctx) {}
}