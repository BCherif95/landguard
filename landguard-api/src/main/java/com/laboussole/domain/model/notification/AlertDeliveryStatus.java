package com.laboussole.domain.model.notification;

/** Lifecycle of one alert delivery attempt sequence. */
public enum AlertDeliveryStatus {

    /** Queued and due for dispatch at {@code nextAttemptAt}. */
    PENDING,

    /** Claimed by a worker; a dispatch is in progress. */
    IN_FLIGHT,

    /** The channel accepted the message. Terminal. */
    DELIVERED,

    /**
     * Every attempt was exhausted without success — the dead-letter state.
     * Terminal, and deliberately kept in the ledger: an alert that never
     * reached its recipient is evidence in itself when a dispatch is later
     * contested.
     */
    DEAD_LETTER
}
