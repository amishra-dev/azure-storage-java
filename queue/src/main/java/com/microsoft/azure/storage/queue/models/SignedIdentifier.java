/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 * <p>
 * Code generated by Microsoft (R) AutoRest Code Generator.
 * Changes may cause incorrect behavior and will be lost if the code is
 * regenerated.
 */

package com.microsoft.azure.storage.queue.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * signed identifier.
 */
@JacksonXmlRootElement(localName = "SignedIdentifier")
public final class SignedIdentifier {
    /**
     * a unique id.
     */
    @JsonProperty(value = "Id", required = true)
    private String id;

    /**
     * The access policy.
     */
    @JsonProperty(value = "AccessPolicy", required = true)
    private AccessPolicy accessPolicy;

    /**
     * Get the id value.
     *
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id
     *         the id value to set.
     *
     * @return the SignedIdentifier object itself.
     */
    public SignedIdentifier withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the accessPolicy value.
     *
     * @return the accessPolicy value.
     */
    public AccessPolicy accessPolicy() {
        return this.accessPolicy;
    }

    /**
     * Set the accessPolicy value.
     *
     * @param accessPolicy
     *         the accessPolicy value to set.
     *
     * @return the SignedIdentifier object itself.
     */
    public SignedIdentifier withAccessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        return this;
    }
}