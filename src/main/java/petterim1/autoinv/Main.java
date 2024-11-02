package petterim1.autoinv;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import cn.nukkit.item.ItemExpBottle;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.plugin.PluginBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.concurrent.ThreadLocalRandom;

public class Main extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void BlockBreakEvent(BlockBreakEvent e) {
        Player p = e.getPlayer();
        int exp = e.getDropExp();
        if (exp <= 0) {
            return;
        }

        e.setDropExp(0);

        playXpSound(p);

        if (e.getBlock().canSilkTouch() && e.getItem() != null && e.getItem().hasEnchantment(Enchantment.ID_SILK_TOUCH)) {
            return;
        }

        if (mending(p, exp)) {
            return;
        }

        p.addExperience(exp);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() == PlayerInteractEvent.Action.PHYSICAL) {
            return;
        }

        if (!(e.getItem() instanceof ItemExpBottle)) {
            return;
        }

        e.setCancelled(true);

        Player p = e.getPlayer();
        PlayerInventory inventory = p.getInventory();

        playXpSound(p);

        if (!p.isCreative()) {
            inventory.decreaseCount(inventory.getHeldItemIndex());
        }

        int exp = ThreadLocalRandom.current().nextInt(3, 12);

        if (mending(p, exp)) {
            return;
        }

        p.addExperience(exp);
    }

    private static boolean mending(Player p, int exp) {
        PlayerInventory inventory = p.getInventory();

        IntArrayList itemsWithMending = new IntArrayList();
        for (int i = 0; i < 4; i++) {
            if (inventory.getArmorItem(i).getEnchantment(Enchantment.ID_MENDING) != null) {
                itemsWithMending.add(inventory.getSize() + i);
            }
        }

        Item offhand = p.getOffhandInventory().getItem(0);
        if (offhand.getId() == Item.SHIELD && offhand.hasEnchantment(Enchantment.ID_MENDING)) {
            itemsWithMending.add(-1);
        }

        if (!itemsWithMending.isEmpty()) {
            int itemToRepair = itemsWithMending.getInt(ThreadLocalRandom.current().nextInt(itemsWithMending.size()));
            boolean isOffhand = itemToRepair == -1;

            Item repaired = isOffhand ? offhand : inventory.getItem(itemToRepair);
            if (repaired instanceof ItemTool || repaired instanceof ItemArmor) {
                if (repaired.getDamage() > 0) {
                    int dmg = repaired.getDamage() - (exp << 1);
                    if (dmg < 0) {
                        dmg = 0;
                    }

                    repaired.setDamage(dmg);

                    if (isOffhand) {
                        p.getOffhandInventory().setItem(0, repaired);
                    } else {
                        inventory.setItem(itemToRepair, repaired);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private static void playXpSound(Player p) {
        LevelEventPacket pk = new LevelEventPacket();
        pk.evid = LevelEventPacket.EVENT_SOUND_EXPERIENCE_ORB;
        pk.x = (float) p.x;
        pk.y = (float) p.y;
        pk.z = (float) p.z;
        pk.data = 0;
        p.dataPacket(pk);
    }
}
