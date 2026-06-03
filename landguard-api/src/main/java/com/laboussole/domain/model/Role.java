package com.laboussole.domain.model;

/**
 * RBAC roles supported by the platform.
 * <ul>
 *   <li>{@link #CITIZEN} — landowner / heir using the public app.</li>
 *   <li>{@link #OFFICER} — cadastral or surveillance officer.</li>
 *   <li>{@link #LEGAL} — notary, lawyer, or tribunal user generating legal evidence.</li>
 *   <li>{@link #BANKER} — financial institution evaluating land collateral.</li>
 *   <li>{@link #ADMIN} — platform operator with full privileges.</li>
 * </ul>
 */
public enum Role {
    CITIZEN,
    OFFICER,
    LEGAL,
    BANKER,
    ADMIN;

    /** Spring Security expects authorities prefixed with {@code ROLE_}. */
    public String authority() {
        return "ROLE_" + name();
    }
}
