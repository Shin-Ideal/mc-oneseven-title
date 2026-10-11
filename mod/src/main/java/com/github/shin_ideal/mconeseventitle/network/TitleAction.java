package com.github.shin_ideal.mconeseventitle.network;

public enum TitleAction {

    TITLE(0),
    SUBTITLE(1),
    ACTIONBAR(2),
    TIMES(3),
    CLEAR(4),
    RESET(5);

    private final int id;

    TitleAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TitleAction fromId(int id) {
        for (TitleAction action : values()) {
            if (action.id == id) {
                return action;
            }
        }
        return null;
    }
}
