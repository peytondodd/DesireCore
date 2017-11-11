package com.desiremc.core.commands.punishment;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.LangHandler;
import com.desiremc.core.api.command.ValidCommand;
import com.desiremc.core.parsers.PlayerSessionParser;
import com.desiremc.core.punishment.Punishment;
import com.desiremc.core.punishment.PunishmentHandler;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.validators.PlayerIsBannedValidator;
import com.desiremc.core.validators.PlayerIsNotBlacklistedValidator;
import org.bukkit.command.CommandSender;

public class UnbanCommand extends ValidCommand
{

    private static final LangHandler LANG = DesireCore.getLangHandler();

    public UnbanCommand()
    {
        super("unban", "Unban a user from the server.", Rank.MODERATOR, new String[] { "target" });
        addParser(new PlayerSessionParser(), "target");

        addValidator(new PlayerIsBannedValidator(), "target");
        addValidator(new PlayerIsNotBlacklistedValidator(), "target");
    }

    @Override
    public void validRun(CommandSender sender, String label, Object... args)
    {
        Session target = (Session) args[0];

        LANG.sendRenderMessage(sender, "ban.unban_message", "{player}", target.getName());
        Punishment p = target.isBanned();
        p.setRepealed(true);
        PunishmentHandler.getInstance().save(p);
    }

}