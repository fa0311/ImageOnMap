/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2020)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2020)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.ImageUtils;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.text.ActionBar;
import fr.zcraft.zlib.tools.text.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

@CommandInfo (name =  "update", usageParameters = "<new url> [stretched|covered] \"<map name to update>\"")
public class UpdateCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        final Player player = playerSender();
        ImageUtils.ScalingType scaling;

        URL url;

        if(args.length < 1) throwInvalidArgument(I.t("You must give an URL and a map name to update."));
        if(args.length < 2) throwInvalidArgument(I.t("You must give a map name to update."));

        switch(args[1]) {

            case "stretched":
                scaling = ImageUtils.ScalingType.STRETCHED;
                break;
            case "covered":
                scaling = ImageUtils.ScalingType.COVERED;
                break;
            default:
                scaling = ImageUtils.ScalingType.CONTAINED;
        }
        ImageMap map;
        if(scaling.equals(ImageUtils.ScalingType.CONTAINED))
            map=getMapFromArgs(player,1);
        else
            map=getMapFromArgs(player,2);
        try
        {
            url = new URL(args[0]);
            MapManager.load();

            Integer[] size={1,1};
            if(map.getType()== ImageMap.Type.POSTER)
                size=map.getSize( new HashMap<String, Object>(),map.getUserUUID(),map.getId());
            int width=size[0],height=size[1];
            try {
                ActionBar.sendPermanentMessage(player, ChatColor.DARK_GREEN + I.t("Updating..."));
                ImageRendererExecutor.update(url, scaling, player.getUniqueId(), map, width, height, new WorkerCallback<ImageMap>() {
                    @Override
                    public void finished(ImageMap result) {
                        ActionBar.removeMessage(player);
                        MessageSender.sendActionBarMessage(player, ChatColor.DARK_GREEN + I.t("The map was updated using the new image!"));
                    }
                    @Override
                    public void errored(Throwable exception) {
                        player.sendMessage(I.t("{ce}Map rendering failed: {0}", exception.getMessage()));

                        PluginLogger.warning("Rendering from {0} failed: {1}: {2}",
                                player.getName(),
                                exception.getClass().getCanonicalName(),
                                exception.getMessage());
                    }
                });
            }
            finally {
                ActionBar.removeMessage(player);
            }
        }
        catch(MalformedURLException  ex)
        {
            throwInvalidArgument(I.t("Invalid URL."));
        }
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.UPDATE.grantedTo(sender);
    }
}
