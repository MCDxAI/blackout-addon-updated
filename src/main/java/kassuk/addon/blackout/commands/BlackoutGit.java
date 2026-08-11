package kassuk.addon.blackout.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * @author KassuK
 */

public class BlackoutGit extends Command {
    public BlackoutGit() {
        super("blackoutinfo", "Gives the Blackout GitHub");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            info("https://github.com/H1ggsK/BlackOut");
            return SINGLE_SUCCESS;
        });
    }
}
