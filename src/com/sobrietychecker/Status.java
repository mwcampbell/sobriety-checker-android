package com.sobrietychecker;

public final class Status {
    public static final int GOOD = 0;
    public static final int SLIPPING = 1;
    public static final int HELP_NOW = 2;
    public static final int INVALID = -1;

    public static final String GOOD_NAME = "good";
    public static final String SLIPPING_NAME = "slipping";
    public static final String HELP_NOW_NAME = "helpNow";

    private Status() {
    }

    public static int fromName(String name) {
        if (name.equalsIgnoreCase(GOOD_NAME)) {
            return GOOD;
        } else if (name.equalsIgnoreCase(SLIPPING_NAME)) {
            return SLIPPING;
        } else if (name.equalsIgnoreCase(HELP_NOW_NAME)) {
            return HELP_NOW;
        } else {
            return INVALID;
        }
    }

    public static final String toName(int status) {
        switch (status) {
        case GOOD:
            return GOOD_NAME;
        case SLIPPING:
            return SLIPPING_NAME;
        case HELP_NOW:
            return HELP_NOW_NAME;
        default:
            throw new IllegalArgumentException();
        }
    }
}
