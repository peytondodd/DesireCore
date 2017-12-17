package com.desiremc.core.commands.staff;

import com.desiremc.core.api.StaffAPI;
import com.desiremc.core.api.command.ValidCommand;
import com.desiremc.core.parsers.PlayerParser;
import com.desiremc.core.session.Rank;
import com.desiremc.core.validators.PlayerValidator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffFollowCommand extends ValidCommand
{

    public StaffFollowCommand()
    {
        super("follow", "Follow a player", Rank.HELPER, new String[] {"target"}, "mount", "ride", "leash", "lead");
        addParser(new PlayerParser(), "target");
        
        addValidator(new PlayerValidator());
    }

    @Override
    public void validRun(CommandSender sender, String label, Object... args)
    {
        StaffAPI.mount((Player) sender, (Player) args[0]);
    }

}
