# AtherysDungeons
An instancing and dungeon-generating plugin for the A'therys Horizons server, using Pterodactyl

## Installation & Configuration

1. Add the plugin to the Sponge minecraft server
2. Configure the plugin
    1. Provide the Pterodactyl Application URL in the config ( `pterodactyl.app-url` )
    2. Provide a Pterodactyl API Auth Token in the config ( `pterodactyl.api-token` )
3. Configure the dungeons
    1. Create a new Sponge minecraft server
    2. Configure it with all of your necessary plugins, worlds, configurations, etc.
    3. Download the `server` folder and put it into a `tar.gz` archive
    4. Create a new pterodactyl Pack with it
    4. Add the pack id to the map of dungeons in the configuration ( `dungeons` )
    
### Example Config

```hocon
pterodactyl = {
    app-url = http://localhost:8080
    api-token = ptero-api-token
    allocations-ip-address = "172.18.0.1"
    ports = ["25501", "25502", "25503"] # the number of ports listed here is also the maximum possible number of dungeon instances
}

dungeons = [
    {
        pack-id = 1,
        name = dungeon-1
        min-players = 3
        max-players = 7
        instance-settings = {
            max-memory = 2048
        }
    },
    {
        pack-id = 2,
        name = dungeon-2
        min-players = 5
        max-players = 10
        instance-settings = {
            max-memory = 2048
        }
    }
]
```

## Usage

* `/dungeon queue dungeon-1` - Queues your and your party for the specified dungeon, if there are enough of you ( see `min-players` config for each dungeon ). You may be queued for only 1 dungeon at a time.
* `/dungeon deque` - Remove your party from the queue

Depending on the configured `max-number-of-instances`, your party may not immediately enter into the dungeon it has queued for.
If all the allowed instances are currently in use by other parties, your party will have to wait until one of those instances has been cleared.