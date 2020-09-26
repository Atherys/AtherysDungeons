package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class PterodactylConfig {

    @Setting("app-url")
    public String APPLICATION_URL = "http://localhost:8080";

    @Setting("application-token")
    public String APPLICATION_TOKEN = "ptero-application-token";

    @Setting("client-token")
    public String CLIENT_TOKEN = "ptero-account-token";

    @Setting("node-id")
    public long NODE_ID = -1;

    @Setting("allocations-ip-address")
    public String ALLOCATIONS_IP_ADDRESS = "172.18.0.1";

    @Setting("user-id")
    public long USER_ID = -1;

    @Setting("ports")
    public List<String> PORTS = new ArrayList<>();

    @Setting("location-ids")
    public List<Long> LOCATION_IDS = Arrays.asList(1L);

}
