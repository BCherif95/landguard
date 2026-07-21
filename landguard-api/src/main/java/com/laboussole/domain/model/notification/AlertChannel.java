package com.laboussole.domain.model.notification;

/**
 * Delivery channels for monitoring alerts, ordered by dispatch urgency.
 *
 * <p>SMS goes first on purpose: it is the only channel that reaches an owner
 * standing on their land in an area with no data coverage, and it is the one
 * that has to beat the squatters' concrete. Push and e-mail serve the diaspora,
 * who are not the ones who can physically intervene within the hour.
 */
public enum AlertChannel {

    SMS(0),
    PUSH(1),
    EMAIL(2);

    private final int dispatchPriority;

    AlertChannel(int dispatchPriority) {
        this.dispatchPriority = dispatchPriority;
    }

    /** Lower wins — the queue drains ascending. */
    public int dispatchPriority() {
        return dispatchPriority;
    }
}
