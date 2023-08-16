/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.managers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.atlauncher.Gsons;
import com.atlauncher.data.Instance;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicSolderModpack;
import com.atlauncher.network.DownloadException;
import com.atlauncher.utils.TechnicApi;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class TechnicModpackUpdateManager {

    // Technic Non Solder instance update checking
    private static final Map<String, BehaviorSubject<Optional<TechnicModpack>>>
        TECHNIC_INSTANCE_LATEST_VERSION = new HashMap<>();

    // Technic Solder instance update checking
    private static final Map<String, BehaviorSubject<Optional<TechnicSolderModpack>>>
        TECHNIC_SOLDER_INSTANCE_LATEST_VERSION = new HashMap<>();

    public static BehaviorSubject<Optional<TechnicModpack>> getSubject(Instance instance){
        TECHNIC_INSTANCE_LATEST_VERSION.putIfAbsent(
            instance.id,
            BehaviorSubject.createDefault(Optional.empty())
        );
        return TECHNIC_INSTANCE_LATEST_VERSION.get(instance.id);
    }

    public static BehaviorSubject<Optional<TechnicSolderModpack>> getSolderSubject(Instance instance){
        TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.putIfAbsent(
            instance.id,
            BehaviorSubject.createDefault(Optional.empty())
        );
        return TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.get(instance.id);
    }

    public static TechnicModpack getUpToDateModpack(Instance instance) {
        return getSubject(instance).getValue().orElse(null);
    }

    public static TechnicSolderModpack getUpToDateSolderModpack(Instance instance) {
        return getSolderSubject(instance).getValue().orElse(null);
    }

    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.technic.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to Technic Modpack instances");

        InstanceManager.getInstances().parallelStream()
                .filter(i -> i.isTechnicPack() && i.launcher.checkForUpdates).forEach(i -> {
                    TechnicModpack technicModpack = null;

                    try {
                        technicModpack = TechnicApi.getModpackBySlugWithThrow(i.launcher.technicModpack.name);
                    } catch (DownloadException e) {
                        if (e.response != null) {
                            LogManager.debug(Gsons.DEFAULT.toJson(e.response));

                            if (e.statusCode == 404) {
                                LogManager.error(String.format(
                                        "Technic pack with name of %s no longer exists, disabling update checks.",
                                        i.launcher.technicModpack.displayName));
                                i.launcher.checkForUpdates = false;
                                i.save();
                            }
                        }
                    } catch (IOException e) {
                        LogManager.logStackTrace(e);
                    }

                    if (technicModpack != null & i.isTechnicSolderPack()) {
                        TechnicSolderModpack technicSolderModpack = TechnicApi.getSolderModpackBySlug(
                                technicModpack.solder,
                                technicModpack.name);

                        getSolderSubject(i).onNext(Optional.ofNullable(technicSolderModpack));
                    } else {
                        getSubject(i).onNext(Optional.ofNullable(technicModpack));
                    }
                });

        PerformanceManager.end();
    }
}
