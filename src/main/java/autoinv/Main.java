package autoinv;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.List;

public class Main extends PluginBase implements Listener {

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        Item[] itemsToAdd = e.getDrops();
        List<Item> notAdded = new ArrayList<>();
        for (int i = 0; i < itemsToAdd.length; i++) {
            if (inv.canAddItem(itemsToAdd[i])) {
                inv.addItem(itemsToAdd[i]);
            } else {
                notAdded.add(itemsToAdd[i]);
            }
        }
        e.setDrops(notAdded.toArray(new Item[0]));
    }
}
