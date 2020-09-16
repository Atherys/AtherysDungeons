package com.atherys.dungeons.exception;

import com.atherys.dungeons.AtherysDungeons;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DungeonsCommandException extends CommandException {
    public DungeonsCommandException(Object... message) {
        super(AtherysDungeons.getInstance().getDungeonsMessagingFacade().formatError(TextColors.RED, message));
    }
}
