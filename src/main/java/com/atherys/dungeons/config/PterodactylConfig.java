package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class PterodactylConfig {

    @Setting("app-url")
    public String APPLICATION_URL = "http://localhost:8080";

    @Setting("api-token")
    public String APPLICATION_API_TOKEN = "ptero-api-token";

    @Setting("node-id")
    public long NODE_ID = -1;

    @Setting("allocations-ip-address")
    public String ALLOCATIONS_IP_ADDRESS = "172.18.0.1";

    @Setting("ports")
    public List<String> PORTS = new ArrayList<>();

}
