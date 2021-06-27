package autoinv;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.plugin.PluginBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends PluginBase implements Listener {

    private static final boolean BLOCKS = true;

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        if (BLOCKS) {
            Item[] itemsToAdd = e.getDrops();
            int count = itemsToAdd.length;
            if (count > 0) {
                List<Item> notAdded = new ArrayList<>();
                for (Item item : itemsToAdd) {
                    if (inv.canAddItem(item)) {
                        inv.addItem(item);
                    } else {
                        notAdded.add(item);
                    }
                }
                e.setDrops(notAdded.toArray(new Item[0]));
            }
        }
        int xp = e.getDropExp();
        if (xp > 0) {
            if (e.getItem() != null && !(e.getBlock().canSilkTouch() && e.getItem().hasEnchantment(Enchantment.ID_SILK_TOUCH))) {
                ArrayList<Integer> itemsWithMending = new ArrayList<>();
                int size = inv.getSize();
                for (int i = 0; i < 4; i++) {
                    if (inv.getArmorItem(i).getEnchantment(Enchantment.ID_MENDING) != null) {
                        itemsWithMending.add(size + i);
                    }
                }
                if (itemsWithMending.isEmpty()) {
                    p.addExperience(xp);
                } else {
                    Integer itemToRepair = itemsWithMending.get(ThreadLocalRandom.current().nextInt(itemsWithMending.size()));
                    Item toRepair = inv.getItem(itemToRepair);
                    if (toRepair instanceof ItemTool || toRepair instanceof ItemArmor) {
                        if (toRepair.getDamage() > 0) {
                            int dmg = toRepair.getDamage() - 2;
                            if (dmg < 0) {
                                dmg = 0;
                            }
                            toRepair.setDamage(dmg);
                            inv.setItem(itemToRepair, toRepair);
                        }
                    }
                }
            }
            e.setDropExp(0);
        }
    }
}
