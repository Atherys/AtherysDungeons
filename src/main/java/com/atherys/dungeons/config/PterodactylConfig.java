package com.atherys.dungeons.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PterodactylConfig {

    @Setting("app-url")
    public String APPLICATION_URL = "http://localhost:8080";

    @Setting("api-token")
    public String APPLICATION_API_TOKEN = "ptero-api-token";

}
