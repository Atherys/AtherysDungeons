package com.atherys.dungeons.model;

import com.atherys.party.entity.Party;

import java.util.Objects;

public class QueuedParty {

    private Party party;

    private Dungeon dungeon;

    public QueuedParty() {
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }
}
