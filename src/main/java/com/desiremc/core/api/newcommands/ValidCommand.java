package com.desiremc.core.api.newcommands;

import com.desiremc.core.DesireCore;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import com.desiremc.core.tickets.TicketHandler;
import com.desiremc.core.utils.CollectionUtils;
import com.desiremc.core.utils.StringUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class ValidCommand
{

    protected String name;

    protected String description;

    protected Rank requiredRank;

    protected boolean blocksConsole;

    protected String[] aliases;

    protected LinkedList<SenderValidator> senderValidators;

    protected ArrayList<CommandArgument<?>> arguments;

    protected Table<Integer, Class<?>, Object> values;

    /**
     * Constructs a new command with the given arguments. This will initialize the arguments list as well as the values
     * table. Additionally, it will automatically convert all aliases to lowercase.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param requiredRank the required rank for the command.
     * @param blocksConsole if this command is unusable by the console.
     * @param aliases the aliases of the command.
     */
    public ValidCommand(String name, String description, Rank requiredRank, boolean blocksConsole, String[] aliases)
    {
        this.name = name;
        this.description = description;
        this.requiredRank = requiredRank;
        this.blocksConsole = blocksConsole;
        this.aliases = aliases;
        this.senderValidators = new LinkedList<>();
        this.arguments = new ArrayList<>();
        this.values = HashBasedTable.create();
        for (int i = 0; i < aliases.length; i++)
        {
            aliases[i] = aliases[i] == null ? "" : aliases[i].toLowerCase();
        }
    }

    /**
     * Constructs a new command without any aliases.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param requiredRank the required rank for the command.
     * @param blocksConsole if this command is unusable by the console.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description, Rank requiredRank, boolean blocksConsole)
    {
        this(name, description, requiredRank, blocksConsole, new String[0]);
    }

    /**
     * Constructs a new command that is usable by the console.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param requiredRank the required rank for the command.
     * @param aliases the aliases of the command.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description, Rank requiredRank, String[] aliases)
    {
        this(name, description, requiredRank, false, aliases);
    }

    /**
     * Constructs a new command with the rank of {@link Rank#GUEST}.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param blocksConsole if this command is unusable by the console.
     * @param aliases the aliases of the command.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description, boolean blocksConsole, String[] aliases)
    {
        this(name, description, Rank.GUEST, blocksConsole, aliases);
    }

    /**
     * Constructs a new command without any aliases and is usable by the console.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param requiredRank the required rank for the command.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description, Rank requiredRank)
    {
        this(name, description, requiredRank, false, new String[0]);
    }

    /**
     * Constructs a new command without any aliases and the rank of {@link Rank#GUEST}.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param blocksConsole if this command is unusable by the console.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description, boolean blocksConsole)
    {
        this(name, description, Rank.GUEST, blocksConsole, new String[0]);
    }

    /**
     * Constructs a new command with the rank of {@link Rank#GUEST} and is usable by the console.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @param aliases the aliases of the command.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description, String[] aliases)
    {
        this(name, description, Rank.GUEST, false, aliases);
    }

    /**
     * Constructs a new command without any aliases, the rank of {@link Rank#GUEST}, and is usable by the console.
     * 
     * @param name the name of the command.
     * @param description the description of the command.
     * @see #ValidCommand(String, String, Rank, boolean, String[])
     */
    public ValidCommand(String name, String description)
    {
        this(name, description, Rank.GUEST, false, new String[0]);
    }

    /**
     * This method runs the command. It goes through each argument and will process and validate it. Then, so long as
     * that passes, it will run the command's system. Last it will clear out any saved table data and stored argument
     * values.
     * 
     * @param sender the sender of the command.
     * @param label the label from which the command was sent
     * @param rawArguments the unparsed and non-validated arguments.
     */
    protected void process(Session sender, String[] label, String[] rawArguments)
    {
        if (rawArguments.length < getMinimumLength() || rawArguments.length > getMaximumLength())
        {
            DesireCore.getLangHandler().sendUsageMessage(sender.getSender(), StringUtils.compile(label), (Object[]) getArgumentNames());
            return;
        }
        for (SenderValidator senderValidator : senderValidators)
        {
            if (!senderValidator.validate(sender))
            {
                return;
            }
        }

        if (rawArguments.length == 0 && blocksConsole() && sender.isConsole())
        {
            DesireCore.getLangHandler().sendRenderMessage(sender, "only_players", true, false);
            return;
        }
        CommandArgument<?> argument;
        for (int i = 0; i < rawArguments.length; i++)
        {
            argument = getArgument(i);

            // this should never happen, it is here exclusively to prevent potential errors that were not caught with exceptions earlier
            if (argument == null)
            {
                DesireCore.getLangHandler().sendUsageMessage(sender.getSender(), StringUtils.compile(label), (Object[]) getArgumentNames());
                return;
            }

            if (blocksConsole() && sender.isConsole() && !argument.allowsConsole())
            {
                DesireCore.getLangHandler().sendRenderMessage(sender.getSender(), "only_players", true, false);
                return;
            }

            if (!argument.process(sender, label, !argument.hasVariableLength() ? rawArguments[i] : StringUtils.compile(Arrays.copyOfRange(rawArguments, i, rawArguments.length))))
            {
                return;
            }

            // terminate if the argument had variable length.
            if (argument.hasVariableLength())
            {
                i = rawArguments.length;
            }
        }
        try
        {
            validRun(sender, label, arguments);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            sender.getSender().sendMessage("§4An error occured. Contact a staff member immediately.");
            TicketHandler.openTicket(SessionHandler.getConsoleSession(), "Error running /" + StringUtils.compile(label) + ". Contact a dev.");
        }
        arguments.forEach(arg -> arg.clearValue());
        clearTable();
    }

    /**
     * Process the tab complete for the given command. This also takes into account the fact the last argument provided
     * is what is supposed to be changed. The arguments passed include only the arguments for the command, and not the
     * actual label used. The raw arguments passed should always have at least a length of 1.
     * 
     * @param sender the person who sent the tab request.
     * @param rawArguments the arguments already typed by the player.
     * @return the suggestions for tab complete.
     */
    public List<String> processTabComplete(Session sender, String[] rawArguments)
    {
        Iterator<CommandArgument<?>> it = arguments.iterator();
        int i = 0;
        CommandArgument<?> argument = null;
        while (it.hasNext() && i < rawArguments.length)
        {
            argument = it.next();
            i++;
        }
        if (argument != null)
        {
            if (!argument.hasRequiredRank() || sender.getRank().getId() >= argument.getRequiredRank().getId())
            {
                return argument.getRecommendations(sender, rawArguments[rawArguments.length - 1]);
            }
            else
            {
                return Arrays.asList();
            }
        }
        else
        {
            return CommandHandler.defaultTabComplete(sender, rawArguments[rawArguments.length - 1]);
        }
    }

    /**
     * Runs the command after all processing has already been completed. The label is an array of the label used by this
     * command as well as any parent command. The arguments will always be there, whether they are used or not. To check
     * if optional arguments were used, call the method {@link CommandArgument#hasValue()}.
     * 
     * @param sender the sender of the command.
     * @param label the label of the command.
     * @param arguments the arguments of the command.
     */
    public abstract void validRun(Session sender, String[] label, List<CommandArgument<?>> arguments);

    /**
     * The table of the already processed values. -1 corresponds to the sender. Every other number corresponds to the
     * ordinal of the argument. In the process of running the command, it will always grab the sender's session.
     * 
     * @return the already processed values.
     */
    public Table<Integer, Class<?>, Object> getValues()
    {
        return values;
    }

    /**
     * Clear all the stored values in the table that were added from the command's usage.
     */
    private void clearTable()
    {
        values.clear();
    }

    /**
     * @return an array of all the names of the arguments.
     */
    public String[] getArgumentNames()
    {
        String[] argumentNames = new String[arguments.size()];
        int i = 0;
        for (CommandArgument<?> argument : arguments)
        {
            argumentNames[i] = argument.getName();
            i++;
        }
        return argumentNames;
    }

    /**
     * The lowest possible length that the number of raw arguments is capable of being. This is the count of the number
     * of arguments that are not optional.
     * 
     * @return the minimum length of the raw command arguments.
     */
    protected int getMinimumLength()
    {
        int minimumLength = 0;
        for (CommandArgument<?> argument : arguments)
        {
            if (!argument.isOptional())
            {
                minimumLength++;
            }
        }
        return minimumLength;
    }

    /**
     * The highest possible length that the number of raw arguments is capable of being. For non-variable-length
     * commands, this is the size of all the arguments. For variable-length commands, it is {@link Integer#MAX_VALUE}.
     * 
     * @return the maximum length of the raw command arguments.
     */
    protected int getMaximumLength()
    {
        if (arguments.size() == 0)
        {
            return 0;
        }
        if (CollectionUtils.getLast(arguments).hasVariableLength())
        {
            return Integer.MAX_VALUE;
        }
        return arguments.size();
    }

    /**
     * Adds a new argument to be used by the command. Will throw an {@link IllegalArgumentException} in one of two
     * cases. First, if the given argument is required and the previous argument is not optional. Second, if the
     * argument before it is of variable length. Both of these cases are not supported as there is no perfect way to
     * ensure that the arguments will always capture the desired input.
     * 
     * @param argument the new argument
     */
    protected void addArgument(CommandArgument<?> argument)
    {
        if (arguments.size() != 0)
        {

            if (!argument.isOptional() && CollectionUtils.getLast(arguments).isOptional())
            {
                throw new IllegalArgumentException("Required arguments can only follow other required arguments.");
            }
            if (CollectionUtils.getLast(arguments).hasVariableLength())
            {
                throw new IllegalArgumentException("Arguments of variable length must be the last argument.");
            }
        }
        argument.setOrdinal(arguments.size());
        argument.setCommand(this);
        arguments.add(argument);
    }

    /**
     * Remove an existing argument from the command.
     * 
     * @param argument the existing argument.
     * @return whether or not the argument still existed.
     */
    protected boolean removeArgument(CommandArgument<?> argument)
    {
        return arguments.remove(argument);
    }

    /**
     * Remove an existing argument from the command, referenced by name.
     * 
     * @param argumentName the name of the argument.
     * @return whether or not the argument existed.
     */
    protected boolean removeArgument(String argumentName)
    {
        return removeArgument(getArgument(argumentName));
    }

    /**
     * Return an unmodifiable view of the arguments for this command. To add a new argument use
     * {@link #addArgument(CommandArgument)}. To remove an existing argument use
     * {@link #removeArgument(CommandArgument)}.
     * 
     * @return the current existing arguments.
     */
    public List<CommandArgument<?>> getArguments()
    {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * @param ordinal the ordinal.
     * @return the argument at the given ordinal.
     */
    protected CommandArgument<?> getArgument(int ordinal)
    {
        for (CommandArgument<?> argument : arguments)
        {
            if (argument.getOrdinal() == ordinal)
            {
                return argument;
            }
        }
        return null;
    }

    /**
     * Get a particular argument that has the given name. This will return null if the argument is not found. Not case
     * sensitive.
     * 
     * @param argumentName the name of the argument.
     * @return the argument with the given name.
     */
    protected CommandArgument<?> getArgument(String argumentName)
    {
        argumentName = argumentName.toLowerCase();
        for (CommandArgument<?> arg : arguments)
        {
            if (arg.getName().equals(argumentName))
            {
                return arg;
            }
        }
        return null;
    }

    /**
     * Adds a validator to be run on the player before the command itself starts processing the information.
     * 
     * @param senderValidator the new validator
     */
    protected void addSenderValidator(SenderValidator senderValidator)
    {
        senderValidators.add(senderValidator);
    }

    /**
     * Checks whether the passed in command string matches this particular valid command,
     * 
     * @param label the label of the command.
     * @return {@code true} if the parameter matches the command. Otherwise, returns {@code false}.
     */
    protected boolean matches(String label)
    {
        label = label.toLowerCase();
        if (label == null)
        {
            return false;
        }
        if (label.equals(getName()))
        {
            return true;
        }
        for (String alias : aliases)
        {
            if (label.equals(alias))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param start the start of the alias to search for.
     * @return the name or alias that starts with the given string.
     */
    protected String getMatchingAlias(String start)
    {
        start = start.toLowerCase();
        if (name.startsWith(start))
        {
            return name;
        }
        for (String alias : aliases)
        {
            if (alias.startsWith(start))
            {
                return alias;
            }
        }
        return null;
    }

    /**
     * @return all other names this command could be referenced by besides it's name.
     */
    public String[] getAliases()
    {
        return aliases;
    }

    /**
     * @return {@code true} if this command is unusable by the console unless overridden by an argument.
     */
    public boolean blocksConsole()
    {
        return blocksConsole;
    }

    /**
     * @return the rank required to run this command.
     */
    public Rank getRequiredRank()
    {
        return requiredRank;
    }

    /**
     * Returns the description of the command. This is given to Bukkit when the command is properly registered within
     * their system. There is no method to change this, and if it is changed via reflection, that change will not be
     * reflected within Bukkit's command system.
     * 
     * @return the description of the command.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the name of the command. This is what is used to register the command within Bukkit, as well as the
     * primary way to reference the command elsewhere. There is no method to change this, and if it is changed via
     * reflection, that change will not be reflected within Bukkit's command system.
     * 
     * @return the name of the command.
     */
    public String getName()
    {
        return name.toLowerCase();
    }

}
