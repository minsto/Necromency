package com.mickdev.necromency.registry;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.MinionPlayerData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

@EventBusSubscriber(modid = Necromency.MODID)
public final class MinionCommand {

    private MinionCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("minion")
                        .then(Commands.literal("set")
                                .then(Commands.literal("aggressive")
                                        .executes(ctx -> setAggressive(ctx.getSource(), true)))
                                .then(Commands.literal("passive")
                                        .executes(ctx -> setAggressive(ctx.getSource(), false))))
                        .then(Commands.literal("friend")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setRelation(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "player"),
                                                MinionPlayerData.Relation.FRIEND))))
                        .then(Commands.literal("enemy")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setRelation(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "player"),
                                                MinionPlayerData.Relation.ENEMY))))
        );
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static int setAggressive(CommandSourceStack source, boolean aggressive) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinionPlayerData.setAggressive(player, aggressive);
        source.sendSuccess(
                () -> Component.literal(aggressive
                        ? "Minions are set to aggressive"
                        : "Minions are set to passive"),
                false
        );
        return 1;
    }

    private static int setRelation(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            MinionPlayerData.Relation relation) throws CommandSyntaxException {
        ServerPlayer owner = source.getPlayerOrException();
        for (ServerPlayer target : targets) {
            String name = target.getName().getString();
            MinionPlayerData.setRelation(owner, name, relation);
            String msg = switch (relation) {
                case FRIEND -> name + " is now a friend";
                case ENEMY -> name + " is now an enemy";
                default -> name + " relation cleared";
            };
            source.sendSuccess(() -> Component.literal(msg), false);
        }
        return targets.size();
    }
}
