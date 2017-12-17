package com.desiremc.core.commands.staff;

import com.desiremc.core.api.command.ValidCommand;
import com.desiremc.core.parsers.PlayerSessionParser;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import com.desiremc.core.staff.StaffHandler;
import com.desiremc.core.validators.PlayerSessionIsOnlineValidator;
import com.desiremc.core.validators.PlayerValidator;
import org.bukkit.command.CommandSender;

public class StaffRestoreCommand extends ValidCommand
{
    public StaffRestoreCommand(String name, String... aliases)
    {
        super(name, "Restore a players inventory.", Rank.HELPER, new String[] {"target"}, aliases);

        addParser(new PlayerSessionParser(), "target");

        addValidator(new PlayerValidator());
        addValidator(new PlayerSessionIsOnlineValidator(), "target");
    }

    @Override
    public void validRun(CommandSender sender, String label, Object... args)
    {
        Session player = SessionHandler.getSession(sender);
        Session target = (Session) args[0];

        StaffHandler.getInstance().restoreInventory(player, target);
    }
}