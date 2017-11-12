package com.desiremc.core.commands.punishment;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.command.ValidCommand;
import com.desiremc.core.parsers.PlayerParser;
import com.desiremc.core.parsers.StringParser;
import com.desiremc.core.session.Rank;
import com.desiremc.core.validators.SenderOutranksTargetValidator;

public class KickCommand extends ValidCommand
{

    public KickCommand()
    {
        super("kick", "Kick a user from the server.", Rank.JRMOD, ValidCommand.ARITY_REQUIRED_VARIADIC, new String[] { "target", "reason" });
        
        addParser(new PlayerParser(), "target");
        addParser(new StringParser(), "reason");
        
        addValidator(new SenderOutranksTargetValidator(), "target");
    }

    @Override
    public void validRun(CommandSender sender, String label, Object... args)
    {
        Player player = (Player) sender;
        Player target = (Player) args[0];

        target.kickPlayer(DesireCore.getLangHandler().renderMessage("staff.kick-message", "{player}", player.getName(), "{reason}", args[1]));
    }
}
