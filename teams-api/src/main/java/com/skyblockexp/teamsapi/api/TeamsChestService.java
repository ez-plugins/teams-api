package com.skyblockexp.teamsapi.api;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

/**
 * Optional extension service for team chest management.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to support
 * team chest operations register an implementation separately via
 * {@link TeamsAPI#registerChestProvider(org.bukkit.plugin.Plugin, TeamsChestService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isChestAvailable()} before calling {@link TeamsAPI#getChestService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade gracefully.</p>
 */
public interface TeamsChestService {

    /**
     * The default team chest identifier used by single-chest providers.
     */
    String DEFAULT_CHEST_ID = "default";

    /**
     * Returns all chest identifiers currently available for the team.
     *
     * <p>The returned collection should be an unmodifiable snapshot and must never
     * be {@code null}. Providers that only support a single chest may keep the
     * default implementation, which exposes {@link #DEFAULT_CHEST_ID}.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return an unmodifiable collection of chest identifiers; never {@code null}
     */
    default Collection<String> getChestIds(final UUID teamId) {
        return Collections.singleton(DEFAULT_CHEST_ID);
    }

    /**
     * Returns all items currently stored in the team's chest.
     *
     * <p>The returned collection should be an unmodifiable snapshot and must never
     * be {@code null}. Slot ordering and presentation are provider-defined.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return an unmodifiable collection of {@link ItemStack}; never {@code null},
     *         empty if the chest is empty or does not exist
     */
    Collection<ItemStack> getContents(UUID teamId);

    /**
     * Returns all items currently stored in a specific team chest.
     *
     * <p>Providers that support only one chest may keep the default implementation.
     * In that case, only {@link #DEFAULT_CHEST_ID} is supported.</p>
     *
     * @param teamId  the UUID of the team; must not be {@code null}
     * @param chestId the chest identifier; must not be {@code null}
     * @return an unmodifiable collection of {@link ItemStack}; never {@code null},
     *         empty if the chest is empty or does not exist
     */
    default Collection<ItemStack> getContents(final UUID teamId, final String chestId) {
        if (DEFAULT_CHEST_ID.equals(chestId)) {
            return getContents(teamId);
        }
        return Collections.emptyList();
    }

    /**
     * Replaces the entire contents of the team's default chest.
     *
     * <p>Providers that do not support full chest replacement may keep this default
     * implementation, which returns {@code false}.</p>
     *
     * @param teamId   the UUID of the team; must not be {@code null}
     * @param contents the new chest contents snapshot; must not be {@code null}
     * @return {@code true} if the contents were replaced, {@code false} otherwise
     */
    default boolean setContents(final UUID teamId, final Collection<ItemStack> contents) {
        return false;
    }

    /**
     * Replaces the entire contents of a specific team chest.
     *
     * <p>Providers that support only one chest may keep the default implementation.
     * In that case, only {@link #DEFAULT_CHEST_ID} is supported.</p>
     *
     * @param teamId   the UUID of the team; must not be {@code null}
     * @param chestId  the chest identifier; must not be {@code null}
     * @param contents the new chest contents snapshot; must not be {@code null}
     * @return {@code true} if the contents were replaced, {@code false} otherwise
     */
    default boolean setContents(
            final UUID teamId,
            final String chestId,
            final Collection<ItemStack> contents) {
        if (DEFAULT_CHEST_ID.equals(chestId)) {
            return setContents(teamId, contents);
        }
        return false;
    }

    /**
     * Attempts to add one item stack to the team's chest.
     *
     * <p>Stacking behavior, slot selection, and partial merges are provider-defined.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @param item   the item stack to add; must not be {@code null}
     * @return {@code true} if the item was added, {@code false} otherwise
     */
    boolean addItem(UUID teamId, ItemStack item);

    /**
     * Attempts to add one item stack to a specific team chest.
     *
     * <p>Providers that support only one chest may keep the default implementation.
     * In that case, only {@link #DEFAULT_CHEST_ID} is supported.</p>
     *
     * @param teamId  the UUID of the team; must not be {@code null}
     * @param chestId the chest identifier; must not be {@code null}
     * @param item    the item stack to add; must not be {@code null}
     * @return {@code true} if the item was added, {@code false} otherwise
     */
    default boolean addItem(final UUID teamId, final String chestId, final ItemStack item) {
        if (DEFAULT_CHEST_ID.equals(chestId)) {
            return addItem(teamId, item);
        }
        return false;
    }

    /**
     * Attempts to remove one matching item stack from the team's chest.
     *
     * <p>Matching behavior and partial stack removal semantics are provider-defined.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @param item   the item stack to remove; must not be {@code null}
     * @return {@code true} if an item was removed, {@code false} otherwise
     */
    boolean removeItem(UUID teamId, ItemStack item);

    /**
     * Attempts to remove one matching item stack from a specific team chest.
     *
     * <p>Providers that support only one chest may keep the default implementation.
     * In that case, only {@link #DEFAULT_CHEST_ID} is supported.</p>
     *
     * @param teamId  the UUID of the team; must not be {@code null}
     * @param chestId the chest identifier; must not be {@code null}
     * @param item    the item stack to remove; must not be {@code null}
     * @return {@code true} if an item was removed, {@code false} otherwise
     */
    default boolean removeItem(final UUID teamId, final String chestId, final ItemStack item) {
        if (DEFAULT_CHEST_ID.equals(chestId)) {
            return removeItem(teamId, item);
        }
        return false;
    }
}
