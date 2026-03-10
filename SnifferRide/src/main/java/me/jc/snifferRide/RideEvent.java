package me.jc.snifferRide;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sniffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;


import java.util.*;

public class RideEvent implements Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, BukkitTask> rideTasks = new HashMap<>();
    private Set<UUID> cooldown = new HashSet<>();
    public RideEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    private Set<UUID> messageCooldown = new HashSet<>();

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!(entity instanceof Sniffer sniffer)) return;

        event.setCancelled(true);

        // Already riding something
        if (!sniffer.getPassengers().isEmpty()) return;

        sniffer.addPassenger(player);
        AttributeInstance HeightAttribute = sniffer.getAttribute(Attribute.STEP_HEIGHT);
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(),
                "step_height_boost",
                0.6,
                AttributeModifier.Operation.ADD_NUMBER);
        if(HeightAttribute != null) {
            HeightAttribute.addTransientModifier(modifier);
        }
        AttributeInstance JumpAttribute = sniffer.getAttribute(Attribute.SAFE_FALL_DISTANCE);
        AttributeModifier fallModifier = new AttributeModifier(UUID.randomUUID(),
                "fall_height_boost",
                2,
                AttributeModifier.Operation.ADD_NUMBER);
        if(JumpAttribute != null) {
            JumpAttribute.addTransientModifier(fallModifier);
        }






        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            // Stop conditions
            if (!sniffer.isValid()
                    || !sniffer.getPassengers().contains(player)
                    || player.isSneaking()) {

                sniffer.removePassenger(player);


                BukkitTask t = rideTasks.remove(player.getUniqueId());
                if (t != null) t.cancel();
                return;
            }
            sniffer.getPathfinder().stopPathfinding();
            // Paper-only input check
            double forwardSpeed = 0.25;
            double backwardSpeed = 0.05;
            Vector base = player.getLocation().getDirection();
            base.setY(0);
            base.normalize();
            float yaw = player.getLocation().getYaw();

            if (player.getCurrentInput().isForward()) {
                Vector move = base.clone().multiply(forwardSpeed);
                sniffer.setVelocity(move);
            }

            else if (player.getCurrentInput().isBackward()) {
                Vector move = base.clone().multiply(-backwardSpeed);
                sniffer.setVelocity(move);
                sniffer.setRotation(yaw, 0);}
            if(player.getCurrentInput().isJump() && sniffer.isOnGround()){

                if (cooldown.contains(player.getUniqueId())) {

                    if (!messageCooldown.contains(player.getUniqueId())) {
                        player.sendMessage(
                                MiniMessage.miniMessage().deserialize("<red>The sniffer is too tired to jump right now!")
                        );
                        messageCooldown.add(player.getUniqueId());

                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            messageCooldown.remove(player.getUniqueId());
                        }, 30L);
                    }

                    return;
                }

                Vector jumpStrength = player.getLocation().getDirection();
                jumpStrength.setY(0);
                jumpStrength.normalize();

                double forwardStrength = plugin.getConfig().getDouble("SnifferJumpSpeed");
                double upwardStrength = plugin.getConfig().getDouble("SnifferJumpHeight");

                Vector leap = jumpStrength.multiply(forwardStrength);
                leap.setY(upwardStrength);

                sniffer.setVelocity(leap);

                cooldown.add(player.getUniqueId());

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    cooldown.remove(player.getUniqueId());
                }, 60L);
            }


        }, 0L, 1L);

        rideTasks.put(player.getUniqueId(), task);
    }
}