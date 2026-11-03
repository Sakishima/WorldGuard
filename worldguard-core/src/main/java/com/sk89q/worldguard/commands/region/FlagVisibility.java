/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.commands.region;

import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Determines which flags a sender can actually edit in a given region.
 * This only controls presentation; flag commands still enforce their permissions.
 */
final class FlagVisibility {

    private FlagVisibility() {
    }

    static List<Flag<?>> filter(List<Flag<?>> flags, RegionPermissionModel permissions, ProtectedRegion region) {
        return flags.stream()
                .filter(flag -> canEdit(permissions, region, flag))
                .collect(Collectors.toList());
    }

    static boolean canEdit(RegionPermissionModel permissions, ProtectedRegion region, Flag<?> flag) {
        // The command checks this region-level permission before modifying a flag.
        if (!permissions.maySetFlag(region)) {
            return false;
        }

        // Some administrators have generic flag permissions; regular players may
        // have permissions for individual values only (e.g. pvp.allow).
        if (permissions.maySetFlag(region, flag) || permissions.maySetFlag(region, flag, null)) {
            return true;
        }

        if (flag instanceof StateFlag) {
            return permissions.maySetFlag(region, flag, "allow")
                    || permissions.maySetFlag(region, flag, "deny");
        }

        if (flag instanceof BooleanFlag) {
            return permissions.maySetFlag(region, flag, "true")
                    || permissions.maySetFlag(region, flag, "false");
        }

        // For arbitrary-value flags there is no finite set of possible values.
        // The generic or unset permission above is the reliable way to expose them.
        return false;
    }
}
