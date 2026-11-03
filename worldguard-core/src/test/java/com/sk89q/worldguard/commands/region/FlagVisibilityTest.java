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

import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlagVisibilityTest {

    private static final String REGION = "plot";

    private final ProtectedRegion region = new GlobalProtectedRegion(REGION);

    @Test
    void requiresRegionFlagPermission() {
        RegionPermissionModel permissions = permissions("worldguard.region.flag.flags.pvp.allow.plot");

        assertFalse(FlagVisibility.canEdit(permissions, region, Flags.PVP));
    }

    @Test
    void supportsGenericFlagPermissions() {
        RegionPermissionModel permissions = permissions(
                "worldguard.region.flag.regions.plot",
                "worldguard.region.flag.flags.pvp.plot"
        );

        assertTrue(FlagVisibility.canEdit(permissions, region, Flags.PVP));
        assertFalse(FlagVisibility.canEdit(permissions, region, Flags.BUILD));
    }

    @Test
    void supportsStateValueOnlyPermissions() {
        RegionPermissionModel permissions = permissions(
                "worldguard.region.flag.regions.plot",
                "worldguard.region.flag.flags.pvp.allow.plot"
        );

        assertTrue(FlagVisibility.canEdit(permissions, region, Flags.PVP));
        assertFalse(FlagVisibility.canEdit(permissions, region, Flags.BUILD));
    }

    @Test
    void supportsBooleanValueOnlyPermissions() {
        BooleanFlag addon = new BooleanFlag("addon-toggle");
        RegionPermissionModel permissions = permissions(
                "worldguard.region.flag.regions.plot",
                "worldguard.region.flag.flags.addon-toggle.true.plot"
        );

        assertTrue(FlagVisibility.canEdit(permissions, region, addon));
    }

    @Test
    void supportsUnsetOnlyPermissions() {
        RegionPermissionModel permissions = permissions(
                "worldguard.region.flag.regions.plot",
                "worldguard.region.flag.flags.greeting.unset.plot"
        );

        assertTrue(FlagVisibility.canEdit(permissions, region, Flags.GREET_MESSAGE));
    }

    @Test
    void filtersFlagsWithoutChangingOrder() {
        BooleanFlag addon = new BooleanFlag("addon-toggle");
        List<Flag<?>> registered = Arrays.asList(Flags.PVP, Flags.BUILD, Flags.GREET_MESSAGE, addon);
        RegionPermissionModel permissions = permissions(
                "worldguard.region.flag.regions.plot",
                "worldguard.region.flag.flags.pvp.allow.plot",
                "worldguard.region.flag.flags.greeting.unset.plot",
                "worldguard.region.flag.flags.addon-toggle.true.plot"
        );

        assertEquals(Arrays.asList(Flags.PVP, Flags.GREET_MESSAGE, addon),
                FlagVisibility.filter(registered, permissions, region));
    }

    @Test
    void handlesNoAvailableFlags() {
        RegionPermissionModel permissions = permissions("worldguard.region.flag.regions.plot");

        assertEquals(Collections.emptyList(),
                FlagVisibility.filter(Arrays.asList(Flags.PVP, Flags.BUILD), permissions, region));
    }

    private static RegionPermissionModel permissions(String... grantedNodes) {
        Set<String> grants = new HashSet<>(Arrays.asList(grantedNodes));
        TestPlayer sender = new TestPlayer("builder") {
            @Override
            public boolean hasPermission(String permission) {
                return grants.contains(permission);
            }
        };
        return new RegionPermissionModel(sender);
    }
}
