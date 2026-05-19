package com.skyblockexp.teamsapi.model;

/**
 * Classifies the type of a power history entry.
 */
public enum TeamPowerHistoryType {

    /**
     * A positive power gain.
     */
    GAIN,

    /**
     * A negative power change caused by a loss.
     */
    LOSS,

    /**
     * A manual or system correction that may be positive or negative.
     */
    ADJUSTMENT
}
