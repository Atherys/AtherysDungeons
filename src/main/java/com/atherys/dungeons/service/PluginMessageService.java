package com.atherys.dungeons.service;

import com.atherys.dto.AtherysGsonBuilder;
import com.atherys.dto.RedirectPlayersDTO;
import com.atherys.dto.RegisterServerDTO;
import com.atherys.dto.UnregisterServerDTO;
import com.atherys.dungeons.AtherysDungeons;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBinding;

@Singleton
public class PluginMessageService {

    private Gson gson;

    private ChannelBinding.RawDataChannel proxyChannel;

    public void init() {
        proxyChannel = Sponge.getChannelRegistrar().getOrCreateRaw(AtherysDungeons.getInstance(), "atherysproxyplugin:main");
        gson = AtherysGsonBuilder.getGson();
    }

    public void proxyRequestServerRegistration(RegisterServerDTO dto) {
        proxyRequest(RegisterServerDTO.KEY, gson.toJson(dto));
    }

    public void proxyRequestServerUnregistration(UnregisterServerDTO dto) {
        proxyRequest(UnregisterServerDTO.KEY, gson.toJson(dto));
    }

    public void proxyRequestRedirectPlayers(RedirectPlayersDTO dto) {
        proxyRequest(RedirectPlayersDTO.KEY, gson.toJson(dto));
    }

    private void proxyRequest(String key, String json) {
        byte[] dataPackage = transformDataPackage(key, json);

        proxyChannel.sendToServer((buffer) -> {
            buffer.writeByteArray(dataPackage);
        });
    }

    private byte[] transformDataPackage(String key, String json) {
        byte[] dataPackage = new byte[json.length() + 2];

        byte[] jsonBytes = json.getBytes();

        System.arraycopy(jsonBytes, 0, dataPackage, 2, jsonBytes.length);

        byte[] keyBytes = key.getBytes();
        dataPackage[0] = keyBytes[0];
        dataPackage[1] = keyBytes[1];

        return dataPackage;
    }
}
