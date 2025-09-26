package com.swifteats.domain.catalog;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Restaurant {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String state;
    private List<String> tags;
    private boolean open;
    private OffsetDateTime updatedAt;

    public Restaurant(UUID id, String name, String address, String city, String state, List<String> tags, boolean open, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.tags = tags;
        this.open = open;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public List<String> getTags() { return tags; }
    public boolean isOpen() { return open; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}


