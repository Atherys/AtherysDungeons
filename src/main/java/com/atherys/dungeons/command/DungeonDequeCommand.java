package com.atherys.dungeons.command;

import com.atherys.core.command.PlayerCommand;
import com.atherys.core.command.annotation.Aliases;
import com.atherys.core.command.annotation.Permission;
import com.atherys.dungeons.AtherysDungeons;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import javax.annotation.Nonnull;

@Aliases("deque")
@Permission("atherysdungeons.dungeon.deque")
public class DungeonDequeCommand implements PlayerCommand {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull Player source, @Nonnull CommandContext args) throws CommandException {
        AtherysDungeons.getInstance().getDungeonFacade().dequePlayerParty(source);
        return CommandResult.success();
    }
}
