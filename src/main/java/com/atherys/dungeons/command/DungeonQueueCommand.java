package com.atherys.dungeons.command;

import com.atherys.core.command.ParameterizedCommand;
import com.atherys.core.command.PlayerCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Permission;
import com.atherys.dungeons.AtherysDungeons;
import com.atherys.dungeons.model.Dungeon;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;

@Aliases("queue")
@Permission("atherysdungeons.dungeon.queue")
public class DungeonQueueCommand implements PlayerCommand, ParameterizedCommand {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull Player source, @Nonnull CommandContext args) throws CommandException {
        AtherysDungeons.getInstance().getDungeonFacade().queuePlayerParty(
                source,
                args.<Dungeon>getOne("dungeon").orElse(null)
        );

        return CommandResult.success();
    }


    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.choices(Text.of("dungeon"), AtherysDungeons.getInstance().getDungeonFacade().getDungeons())
        };
    }
}
