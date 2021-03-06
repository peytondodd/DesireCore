package com.desiremc.core.api.newcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.desiremc.core.DesireCore;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.utils.StringUtils;

public abstract class ValidBaseCommand extends ValidCommand
{

    protected HelpCommand helpCommand;

    protected List<ValidCommand> subCommands;

    /**
     * Constructs a new base command.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param requiredRank the required rank to use this command.
     * @param aliases any aliases for this command.
     */
    protected ValidBaseCommand(String name, String description, Rank requiredRank, String[] aliases)
    {
        super(name, description, requiredRank, aliases);

        subCommands = new LinkedList<>();
        helpCommand = new HelpCommand(this);
        addSubCommand(helpCommand);
    }

    /**
     * Constructs a new base command with no aliases.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param requiredRank the required rank to use this command.
     */
    protected ValidBaseCommand(String name, String description, Rank requiredRank)
    {
        this(name, description, requiredRank, new String[0]);
    }

    /**
     * Constructs a new base command with required rank {@link Rank#GUEST}.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param aliases any aliases for this command.
     */
    protected ValidBaseCommand(String name, String description, String[] aliases)
    {
        this(name, description, Rank.GUEST, aliases);
    }

    /**
     * Constructs a new base command with no aliases and the required rank of {@link Rank#GUEST}.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     */
    protected ValidBaseCommand(String name, String description)
    {
        this(name, description, Rank.GUEST, new String[0]);
    }

    @Override
    protected void process(Session sender, String[] label, String[] rawArguments)
    {
        ValidCommand sub;
        if (rawArguments.length == 0 || (sub = getSubCommand(rawArguments[0])) == null)
        {
            helpCommand.process(sender, label, new String[0]);
        }
        else
        {
            if (sender.getRank().getId() >= getRequiredRank().getId() && sender.getRank().getId() >= sub.getRequiredRank().getId())
            {
                sub.process(sender, StringUtils.add(label, rawArguments[0]), Arrays.copyOfRange(rawArguments, 1, rawArguments.length));
            }
            else
            {
                DesireCore.getLangHandler().sendRenderMessage(sender, "no_permissions", true, false);
            }
        }
    }

    @Override
    public List<String> processTabComplete(Session sender, String[] rawArguments)
    {
        if (rawArguments.length == 1)
        {
            return getSubCommandNames(sender, rawArguments[0]);
        }
        else
        {
            return getSubCommand(rawArguments[0]).processTabComplete(sender, Arrays.copyOfRange(rawArguments, 1, rawArguments.length));

        }
    }

    /**
     * Add a new sub command to this base command.
     * 
     * @param subCommand the sub command to add.
     */
    public void addSubCommand(ValidCommand subCommand)
    {
        subCommands.add(subCommand);
    }

    /**
     * Searches for a sub command by the given name. This can be either the command's name or one of it's aliases.
     * 
     * @param label the label sent by the player.
     * @return the sub command if one is found.
     */
    public ValidCommand getSubCommand(String label)
    {
        for (ValidCommand command : subCommands)
        {
            if (command.matches(label))
            {
                return command;
            }
        }
        return null;
    }

    /**
     * Get the name all sub commands whose name or one if it's aliases starts with the given string. The name for each
     * command will be whichever piece was provided, whether that be the alias or the name.
     * 
     * @param start the beginning of the label.
     * @return the command labels if any are found.
     */
    public List<String> getSubCommandNames(Session sender, String start)
    {
        List<String> commandNames = new LinkedList<>();
        if (getRequiredRank().getId() > sender.getRank().getId())
        {
            return commandNames;
        }
        String match;
        for (ValidCommand sub : subCommands)
        {
            if ((match = sub.getMatchingAlias(start)) != null && sub.getRequiredRank().getId() <= sender.getRank().getId())
            {
                commandNames.add(match);
            }
        }
        return commandNames;
    }

    /**
     * Get a view of all the sub commands. This is not able to be modified and doing so will throw an exception.
     * 
     * @return all the sub commands.
     */
    public List<ValidCommand> getSubCommands()
    {
        return Collections.unmodifiableList(subCommands);
    }

    protected List<ValidCommand> getUsableCommands(Session sender)
    {
        List<ValidCommand> usable = new ArrayList<>();
        for (ValidCommand command : subCommands)
        {
            if (command.getRequiredRank().getId() <= sender.getRank().getId())
            {
                usable.add(command);
            }
        }
        return usable;
    }

    @Override
    public void validRun(Session sender, String[] label, List<CommandArgument<?>> arguments)
    {
    }

}
