package me.braydon.limiter;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Braydon
 */
public class ChunkEntityLimiter extends JavaPlugin implements Listener {
	private final Map<EntityType, Integer> limits = new HashMap<>();
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		for (EntityType entityType : EntityType.values()) {
			if (!entityType.isSpawnable()) { // If the entity cannot be spawned in the world, continue
				continue;
			}
			int limit = getConfig().getInt("limits." + entityType.name(), -1);
			if (limit == -1) { // If there is no limit for the entity type, continue
				continue;
			}
			// Setting the limit for the entity type
			limits.put(entityType, limit);
			getLogger().info(entityType.name() + " Limit: " + limit);
		}
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntitySpawn(EntitySpawnEvent event) {
		handleEntitySpawn(event, event.getEntityType(), event.getLocation().getChunk());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHangingSpawn(HangingPlaceEvent event) {
		handleEntitySpawn(event, event.getEntity().getType(), event.getBlock().getChunk());
	}
	
	/**
	 * Handle entity spawning for the given {@link Cancellable} event with the given
	 * {@link EntityType} at the given {@link Chunk}.
	 *
	 * @param event the spawn entity event
	 * @param entityType the type of the entity spawned
	 * @param chunk the chunk the entity was spawned
	 */
	private void handleEntitySpawn(Cancellable event, EntityType entityType, Chunk chunk) {
		if (event.isCancelled()) { // If another plugin cancels this event, return
			return;
		}
		Integer limit = limits.get(entityType);
		if (limit == null) { // If the spawned entity type has no limit, return
			return;
		}
		// Loop through all the entities in the given chunk and count the amount
		// of entities with the same entity type as the one provided
		int entities = 0;
		for (Entity chunkEntity : chunk.getEntities()) {
			if (chunkEntity.getType() == entityType) {
				entities++;
			}
		}
		// If the limit has been reached for this chunk, cancel the event
		if (entities > limit) {
			event.setCancelled(true);
		}
	}
}